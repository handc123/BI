package com.zjrcu.iras.bi.platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.dto.FileUploadResult;
import com.zjrcu.iras.bi.platform.mapper.DataSourceMapper;
import com.zjrcu.iras.bi.platform.service.IFileUploadService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.DateUtils;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import com.zjrcu.iras.common.utils.file.FileUploadUtils;
import com.zjrcu.iras.common.utils.uuid.Seq;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 文件上传服务实现
 *
 * @author iras
 */
@Service
public class FileUploadServiceImpl implements IFileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    /**
     * 最大文件大小: 50MB
     */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;

    /**
     * 支持的文件扩展名
     */
    private static final String[] ALLOWED_EXTENSIONS = {"csv", "xls", "xlsx"};

    /**
     * 用于类型检测的样本行数
     */
    private static final int SAMPLE_ROWS_FOR_TYPE_DETECTION = 100;

    /**
     * 预览数据行数
     */
    private static final int PREVIEW_ROWS = 10;

    /**
     * 文件数据源未使用天数阈值
     */
    private static final int UNUSED_DAYS_THRESHOLD = 90;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    @Transactional
    public FileUploadResult uploadFile(MultipartFile file, String name, String remark) {
        try {
            // 1. 验证文件
            validateFile(file);

            // 2. 保存文件到磁盘
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String savedFilePath = saveFile(file, extension);

            // 3. 解析文件并检测列信息
            FileParseResult parseResult = parseFile(savedFilePath, extension);

            // 4. 构建配置JSON
            Map<String, Object> config = buildFileConfig(savedFilePath, originalFilename, 
                                                        file.getSize(), parseResult);

            // 5. 创建数据源记录
            DataSource dataSource = new DataSource();
            dataSource.setName(StringUtils.isNotEmpty(name) ? name : originalFilename);
            dataSource.setType("file");
            dataSource.setConfigMap(config);
            dataSource.setStatus("0");
            dataSource.setRemark(remark);
            dataSource.setCreateBy(SecurityUtils.getUsername());
            dataSource.setCreateTime(DateUtils.getNowDate());

            int result = dataSourceMapper.insertDataSource(dataSource);
            if (result <= 0) {
                // 删除已保存的文件
                deleteFile(savedFilePath);
                return FileUploadResult.error("创建文件数据源失败");
            }

            log.info("文件数据源创建成功: id={}, name={}, file={}", 
                    dataSource.getId(), dataSource.getName(), originalFilename);

            // 6. 返回结果
            return FileUploadResult.success(
                    dataSource.getId(),
                    originalFilename,
                    file.getSize(),
                    parseResult.getRowCount(),
                    parseResult.getColumns(),
                    parseResult.getPreviewData()
            );

        } catch (ServiceException e) {
            log.error("文件上传失败: {}", e.getMessage());
            return FileUploadResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件上传异常", e);
            return FileUploadResult.error("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<List<Object>> previewFileData(Long dataSourceId, int pageNum, int pageSize) {
        try {
            // 获取数据源
            DataSource dataSource = dataSourceMapper.selectDataSourceById(dataSourceId);
            if (dataSource == null) {
                throw new ServiceException("数据源不存在");
            }

            if (!"file".equals(dataSource.getType())) {
                throw new ServiceException("不是文件类型数据源");
            }

            // 获取文件路径
            String filePath = dataSource.getConfigString("filePath");
            String fileName = dataSource.getConfigString("fileName");
            String extension = getFileExtension(fileName);

            // 读取指定页的数据
            return readFileData(filePath, extension, pageNum, pageSize);

        } catch (Exception e) {
            log.error("预览文件数据失败: dataSourceId={}", dataSourceId, e);
            throw new ServiceException("预览文件数据失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteFileDataSource(Long dataSourceId) {
        try {
            // 获取数据源
            DataSource dataSource = dataSourceMapper.selectDataSourceById(dataSourceId);
            if (dataSource == null) {
                return false;
            }

            if (!"file".equals(dataSource.getType())) {
                throw new ServiceException("不是文件类型数据源");
            }

            // 删除文件
            String filePath = dataSource.getConfigString("filePath");
            deleteFile(filePath);

            // 删除数据源记录
            int result = dataSourceMapper.deleteDataSourceById(dataSourceId);
            
            log.info("文件数据源删除成功: id={}, file={}", dataSourceId, filePath);
            
            return result > 0;

        } catch (Exception e) {
            log.error("删除文件数据源失败: dataSourceId={}", dataSourceId, e);
            throw new ServiceException("删除文件数据源失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public int cleanupUnusedFileSources() {
        try {
            // 查询所有文件类型数据源
            DataSource query = new DataSource();
            query.setType("file");
            List<DataSource> fileSources = dataSourceMapper.selectDataSourceList(query);

            int cleanedCount = 0;
            
            // 计算阈值日期 (当前日期 - 90天)
            long thresholdMillis = System.currentTimeMillis() - (UNUSED_DAYS_THRESHOLD * 24L * 60 * 60 * 1000);
            Date thresholdDate = new Date(thresholdMillis);

            for (DataSource dataSource : fileSources) {
                // 检查最后更新时间
                if (dataSource.getUpdateTime() != null && 
                    dataSource.getUpdateTime().before(thresholdDate)) {
                    
                    // 删除文件和数据源
                    deleteFileDataSource(dataSource.getId());
                    cleanedCount++;
                    
                    log.info("清理未使用的文件数据源: id={}, name={}, lastUpdate={}", 
                            dataSource.getId(), dataSource.getName(), dataSource.getUpdateTime());
                }
            }

            return cleanedCount;

        } catch (Exception e) {
            log.error("清理未使用文件数据源失败", e);
            throw new ServiceException("清理未使用文件数据源失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException("文件大小超过限制，最大支持50MB");
        }

        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new ServiceException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        boolean isAllowed = false;
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new ServiceException("不支持的文件格式，仅支持CSV、XLS、XLSX格式");
        }
    }

    /**
     * 保存文件到磁盘
     */
    private String saveFile(MultipartFile file, String extension) throws IOException {
        // 生成唯一文件名
        String datePath = DateUtils.datePath();
        String uniqueId = Seq.getId(Seq.uploadSeqType).toString();
        String fileName = datePath + "/" + uniqueId + "." + extension;

        // 构建完整路径
        String uploadDir = FileUploadUtils.getDefaultBaseDir() + "/bi/files";
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String fullPath = uploadDir + "/" + fileName;
        Path filePath = Paths.get(fullPath);

        // 确保父目录存在
        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }

        // 保存文件
        file.transferTo(filePath.toFile());

        log.info("文件保存成功: {}", fullPath);
        return fullPath;
    }

    /**
     * 解析文件
     */
    private FileParseResult parseFile(String filePath, String extension) {
        try {
            if ("csv".equalsIgnoreCase(extension)) {
                return parseCsvFile(filePath);
            } else if ("xls".equalsIgnoreCase(extension) || "xlsx".equalsIgnoreCase(extension)) {
                return parseExcelFile(filePath, extension);
            } else {
                throw new ServiceException("不支持的文件格式: " + extension);
            }
        } catch (Exception e) {
            log.error("文件解析失败: {}", filePath, e);
            throw new ServiceException("文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析CSV文件
     */
    private FileParseResult parseCsvFile(String filePath) throws IOException {
        FileParseResult result = new FileParseResult();
        List<List<Object>> allData = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // 获取列名
            headers.addAll(csvParser.getHeaderNames());

            // 读取所有数据
            for (CSVRecord record : csvParser) {
                List<Object> row = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.add(record.get(i));
                }
                allData.add(row);
            }

            // 检测列类型
            List<FileUploadResult.ColumnInfo> columns = detectColumnTypes(headers, allData);

            // 获取预览数据
            List<List<Object>> previewData = allData.subList(0, Math.min(PREVIEW_ROWS, allData.size()));

            result.setColumns(columns);
            result.setRowCount((long) allData.size());
            result.setPreviewData(previewData);

            return result;

        } catch (IOException e) {
            throw new ServiceException("CSV文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析Excel文件
     */
    private FileParseResult parseExcelFile(String filePath, String extension) throws IOException {
        FileParseResult result = new FileParseResult();
        List<List<Object>> allData = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            Workbook workbook;
            if ("xls".equalsIgnoreCase(extension)) {
                workbook = new HSSFWorkbook(fis);
            } else {
                workbook = new XSSFWorkbook(fis);
            }

            // 读取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 读取表头
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    headers.add(getCellValueAsString(cell));
                }
            }

            // 读取数据行
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                List<Object> rowData = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    rowData.add(getCellValue(cell));
                }
                allData.add(rowData);
            }

            workbook.close();

            // 检测列类型
            List<FileUploadResult.ColumnInfo> columns = detectColumnTypes(headers, allData);

            // 获取预览数据
            List<List<Object>> previewData = allData.subList(0, Math.min(PREVIEW_ROWS, allData.size()));

            result.setColumns(columns);
            result.setRowCount((long) allData.size());
            result.setPreviewData(previewData);

            return result;

        } catch (Exception e) {
            throw new ServiceException("Excel文件解析失败，请检查文件格式。错误: " + e.getMessage());
        }
    }

    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString() : "";
    }

    /**
     * 检测列类型
     */
    private List<FileUploadResult.ColumnInfo> detectColumnTypes(List<String> headers, List<List<Object>> data) {
        List<FileUploadResult.ColumnInfo> columns = new ArrayList<>();

        for (int i = 0; i < headers.size(); i++) {
            String columnName = headers.get(i);
            String type = detectColumnType(data, i);
            columns.add(new FileUploadResult.ColumnInfo(columnName, type, i));
        }

        return columns;
    }

    /**
     * 检测单列的数据类型
     */
    private String detectColumnType(List<List<Object>> data, int columnIndex) {
        int sampleSize = Math.min(SAMPLE_ROWS_FOR_TYPE_DETECTION, data.size());
        
        int integerCount = 0;
        int decimalCount = 0;
        int dateCount = 0;
        int booleanCount = 0;
        int validCount = 0;

        for (int i = 0; i < sampleSize; i++) {
            if (i >= data.size() || columnIndex >= data.get(i).size()) {
                continue;
            }

            Object value = data.get(i).get(columnIndex);
            if (value == null || value.toString().trim().isEmpty()) {
                continue;
            }

            validCount++;
            String strValue = value.toString().trim();

            // 检测布尔值
            if (isBoolean(strValue)) {
                booleanCount++;
                continue;
            }

            // 检测整数
            if (isInteger(strValue)) {
                integerCount++;
                continue;
            }

            // 检测小数
            if (isDecimal(strValue)) {
                decimalCount++;
                continue;
            }

            // 检测日期
            if (isDate(strValue) || value instanceof Date) {
                dateCount++;
            }
        }

        // 如果没有有效数据，默认为字符串
        if (validCount == 0) {
            return "STRING";
        }

        // 根据比例判断类型（超过80%认为是该类型）
        double threshold = 0.8;
        
        if (booleanCount >= validCount * threshold) {
            return "BOOLEAN";
        }
        if (integerCount >= validCount * threshold) {
            return "INTEGER";
        }
        if (decimalCount >= validCount * threshold) {
            return "DECIMAL";
        }
        if (dateCount >= validCount * threshold) {
            return "DATE";
        }

        // 默认为字符串
        return "STRING";
    }

    /**
     * 判断是否为整数
     */
    private boolean isInteger(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断是否为小数
     */
    private boolean isDecimal(String value) {
        try {
            Double.parseDouble(value);
            return !isInteger(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断是否为布尔值
     */
    private boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) ||
               "是".equals(value) || "否".equals(value) ||
               "1".equals(value) || "0".equals(value);
    }

    /**
     * 判断是否为日期
     */
    private boolean isDate(String value) {
        // 常见日期格式
        String[] datePatterns = {
            "\\d{4}-\\d{2}-\\d{2}",
            "\\d{4}/\\d{2}/\\d{2}",
            "\\d{4}\\d{2}\\d{2}",
            "\\d{2}-\\d{2}-\\d{4}",
            "\\d{2}/\\d{2}/\\d{4}"
        };

        for (String pattern : datePatterns) {
            if (Pattern.matches(pattern, value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 构建文件配置JSON
     */
    private Map<String, Object> buildFileConfig(String filePath, String fileName, 
                                                long fileSize, FileParseResult parseResult) {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", filePath);
        config.put("fileName", fileName);
        config.put("fileSize", fileSize);
        config.put("uploadTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        config.put("columnInfo", parseResult.getColumns());
        config.put("rowCount", parseResult.getRowCount());
        config.put("temporary", true);

        return config;
    }

    /**
     * 读取文件数据（分页）
     */
    private List<List<Object>> readFileData(String filePath, String extension, int pageNum, int pageSize) {
        try {
            if ("csv".equalsIgnoreCase(extension)) {
                return readCsvData(filePath, pageNum, pageSize);
            } else {
                return readExcelData(filePath, extension, pageNum, pageSize);
            }
        } catch (Exception e) {
            log.error("读取文件数据失败: {}", filePath, e);
            throw new ServiceException("读取文件数据失败: " + e.getMessage());
        }
    }

    /**
     * 读取CSV数据（分页）
     */
    private List<List<Object>> readCsvData(String filePath, int pageNum, int pageSize) throws IOException {
        List<List<Object>> result = new ArrayList<>();
        int startRow = (pageNum - 1) * pageSize;
        int endRow = startRow + pageSize;

        try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int currentRow = 0;
            for (CSVRecord record : csvParser) {
                if (currentRow >= startRow && currentRow < endRow) {
                    List<Object> row = new ArrayList<>();
                    for (String value : record) {
                        row.add(value);
                    }
                    result.add(row);
                }
                currentRow++;
                if (currentRow >= endRow) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 读取Excel数据（分页）
     */
    private List<List<Object>> readExcelData(String filePath, String extension, int pageNum, int pageSize) throws IOException {
        List<List<Object>> result = new ArrayList<>();
        int startRow = (pageNum - 1) * pageSize + 1; // +1 跳过表头
        int endRow = startRow + pageSize;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            Workbook workbook;
            if ("xls".equalsIgnoreCase(extension)) {
                workbook = new HSSFWorkbook(fis);
            } else {
                workbook = new XSSFWorkbook(fis);
            }

            Sheet sheet = workbook.getSheetAt(0);
            int currentRow = 0;

            for (Row row : sheet) {
                if (currentRow >= startRow && currentRow < endRow) {
                    List<Object> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        rowData.add(getCellValue(cell));
                    }
                    result.add(rowData);
                }
                currentRow++;
                if (currentRow >= endRow) {
                    break;
                }
            }

            workbook.close();
        }

        return result;
    }

    /**
     * 删除文件
     */
    private void deleteFile(String filePath) {
        try {
            if (StringUtils.isNotEmpty(filePath)) {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("文件删除成功: {}", filePath);
                }
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", filePath, e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 文件解析结果内部类
     */
    private static class FileParseResult {
        private List<FileUploadResult.ColumnInfo> columns;
        private Long rowCount;
        private List<List<Object>> previewData;

        public List<FileUploadResult.ColumnInfo> getColumns() {
            return columns;
        }

        public void setColumns(List<FileUploadResult.ColumnInfo> columns) {
            this.columns = columns;
        }

        public Long getRowCount() {
            return rowCount;
        }

        public void setRowCount(Long rowCount) {
            this.rowCount = rowCount;
        }

        public List<List<Object>> getPreviewData() {
            return previewData;
        }

        public void setPreviewData(List<List<Object>> previewData) {
            this.previewData = previewData;
        }
    }
}
