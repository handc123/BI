package com.zjrcu.iras.bi.platform.service.impl;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.service.ExportTaskStatus;
import com.zjrcu.iras.bi.platform.service.IDashboardService;
import com.zjrcu.iras.bi.platform.service.IExportService;
import com.zjrcu.iras.bi.platform.service.IVisualizationService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BI导出服务实现
 *
 * @author iras
 */
@Service
public class ExportServiceImpl implements IExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);

    @Autowired
    private IVisualizationService visualizationService;

    @Autowired
    private IDashboardService dashboardService;

    /**
     * 异步导出任务状态存储
     */
    private final Map<String, ExportTaskStatus> exportTasks = new ConcurrentHashMap<>();

    /**
     * 异步导出线程池
     */
    private final ExecutorService exportExecutor = Executors.newFixedThreadPool(3);

    @Override
    public void exportToCSV(Long visualizationId, List<Filter> filters, HttpServletResponse response) {
        if (visualizationId == null) {
            throw new ServiceException("可视化ID不能为空");
        }

        try {
            // 获取可视化数据
            QueryResult result = visualizationService.getVisualizationData(visualizationId, filters);
            if (!result.isSuccess()) {
                throw new ServiceException("获取可视化数据失败: " + result.getErrorMessage());
            }

            // 获取可视化信息
            Visualization visualization = visualizationService.selectVisualizationById(visualizationId);
            if (visualization == null) {
                throw new ServiceException("可视化不存在");
            }

            // 设置响应头
            String fileName = generateFileName(visualization.getName(), "csv");
            setResponseHeaders(response, fileName, "text/csv");

            // 写入CSV数据
            writeCSV(result, response.getWriter());

            log.info("CSV导出成功: visualizationId={}, rows={}", visualizationId, result.getTotalRows());

        } catch (IOException e) {
            log.error("CSV导出失败: visualizationId={}, error={}", visualizationId, e.getMessage());
            throw new ServiceException("CSV导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportToExcel(Long visualizationId, List<Filter> filters, HttpServletResponse response) {
        if (visualizationId == null) {
            throw new ServiceException("可视化ID不能为空");
        }

        try {
            // 获取可视化数据
            QueryResult result = visualizationService.getVisualizationData(visualizationId, filters);
            if (!result.isSuccess()) {
                throw new ServiceException("获取可视化数据失败: " + result.getErrorMessage());
            }

            // 获取可视化信息
            Visualization visualization = visualizationService.selectVisualizationById(visualizationId);
            if (visualization == null) {
                throw new ServiceException("可视化不存在");
            }

            // 设置响应头
            String fileName = generateFileName(visualization.getName(), "xlsx");
            setResponseHeaders(response, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // 写入Excel数据
            writeExcel(result, visualization.getName(), response.getOutputStream());

            log.info("Excel导出成功: visualizationId={}, rows={}", visualizationId, result.getTotalRows());

        } catch (IOException e) {
            log.error("Excel导出失败: visualizationId={}, error={}", visualizationId, e.getMessage());
            throw new ServiceException("Excel导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportToPDF(Long dashboardId, List<Filter> filters, HttpServletResponse response) {
        if (dashboardId == null) {
            throw new ServiceException("仪表板ID不能为空");
        }

        try {
            // 获取仪表板信息
            Dashboard dashboard = dashboardService.selectDashboardById(dashboardId);
            if (dashboard == null) {
                throw new ServiceException("仪表板不存在");
            }

            // 设置响应头
            String fileName = generateFileName(dashboard.getDashboardName(), "pdf");
            setResponseHeaders(response, fileName, "application/pdf");

            // 生成PDF（简化实现，实际需要Apache PDFBox）
            writePDFPlaceholder(dashboard, response.getOutputStream());

            log.info("PDF导出成功: dashboardId={}", dashboardId);

        } catch (IOException e) {
            log.error("PDF导出失败: dashboardId={}, error={}", dashboardId, e.getMessage());
            throw new ServiceException("PDF导出失败: " + e.getMessage());
        }
    }

    @Override
    public String exportToPDFAsync(Long dashboardId, List<Filter> filters) {
        if (dashboardId == null) {
            throw new ServiceException("仪表板ID不能为空");
        }

        // 生成任务ID
        String taskId = UUID.randomUUID().toString();

        // 创建任务状态
        ExportTaskStatus taskStatus = ExportTaskStatus.pending(taskId);
        exportTasks.put(taskId, taskStatus);

        // 提交异步任务
        exportExecutor.submit(() -> {
            try {
                // 更新状态为运行中
                exportTasks.put(taskId, ExportTaskStatus.running(taskId, 10));

                // 获取仪表板信息
                Dashboard dashboard = dashboardService.selectDashboardById(dashboardId);
                if (dashboard == null) {
                    exportTasks.put(taskId, ExportTaskStatus.failed(taskId, "仪表板不存在"));
                    return;
                }

                // 模拟导出过程
                exportTasks.put(taskId, ExportTaskStatus.running(taskId, 50));
                Thread.sleep(2000); // 模拟处理时间

                // 生成下载URL（实际应该保存文件并返回真实URL）
                String downloadUrl = "/api/bi/export/download/" + taskId;
                exportTasks.put(taskId, ExportTaskStatus.completed(taskId, downloadUrl));

                log.info("异步PDF导出成功: taskId={}, dashboardId={}", taskId, dashboardId);

            } catch (Exception e) {
                log.error("异步PDF导出失败: taskId={}, dashboardId={}, error={}", 
                         taskId, dashboardId, e.getMessage());
                exportTasks.put(taskId, ExportTaskStatus.failed(taskId, e.getMessage()));
            }
        });

        return taskId;
    }

    @Override
    public ExportTaskStatus getExportTaskStatus(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }

        ExportTaskStatus status = exportTasks.get(taskId);
        if (status == null) {
            throw new ServiceException("任务不存在: " + taskId);
        }

        return status;
    }

    /**
     * 写入CSV数据
     *
     * @param result 查询结果
     * @param writer 输出流
     * @throws IOException IO异常
     */
    private void writeCSV(QueryResult result, PrintWriter writer) throws IOException {
        List<Map<String, Object>> data = result.getData();
        if (data == null || data.isEmpty()) {
            writer.println("No data available");
            return;
        }

        // 写入表头
        Map<String, Object> firstRow = data.get(0);
        List<String> headers = new ArrayList<>(firstRow.keySet());
        writer.println(String.join(",", headers));

        // 写入数据行
        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                Object value = row.get(header);
                String strValue = value != null ? escapeCSV(value.toString()) : "";
                values.add(strValue);
            }
            writer.println(String.join(",", values));
        }

        writer.flush();
    }

    /**
     * 转义CSV特殊字符
     *
     * @param value 原始值
     * @return 转义后的值
     */
    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 写入Excel数据
     *
     * @param result 查询结果
     * @param sheetName 工作表名称
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    private void writeExcel(QueryResult result, String sheetName, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            List<Map<String, Object>> data = result.getData();
            if (data == null || data.isEmpty()) {
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("No data available");
                workbook.write(outputStream);
                return;
            }

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 写入表头
            Map<String, Object> firstRow = data.get(0);
            List<String> headers = new ArrayList<>(firstRow.keySet());
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 写入数据行
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> rowData = data.get(i);
                for (int j = 0; j < headers.size(); j++) {
                    Object value = rowData.get(headers.get(j));
                    Cell cell = row.createCell(j);
                    setCellValue(cell, value);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 创建表头样式
     *
     * @param workbook 工作簿
     * @return 单元格样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 设置单元格值
     *
     * @param cell 单元格
     * @param value 值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 写入PDF占位符（简化实现）
     *
     * @param dashboard 仪表板
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    private void writePDFPlaceholder(Dashboard dashboard, OutputStream outputStream) throws IOException {
        // 简化实现：输出文本说明
        // 实际实现需要使用Apache PDFBox生成真实PDF
        String content = "PDF Export Placeholder\n\n" +
                        "Dashboard: " + dashboard.getDashboardName() + "\n" +
                        "Export Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n\n" +
                        "Note: Full PDF export requires Apache PDFBox integration.";
        outputStream.write(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成文件名
     *
     * @param baseName 基础名称
     * @param extension 扩展名
     * @return 文件名
     */
    private String generateFileName(String baseName, String extension) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String safeName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        return safeName + "_" + timestamp + "." + extension;
    }

    /**
     * 设置响应头
     *
     * @param response HTTP响应
     * @param fileName 文件名
     * @param contentType 内容类型
     */
    private void setResponseHeaders(HttpServletResponse response, String fileName, String contentType) {
        try {
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        } catch (Exception e) {
            log.warn("设置响应头失败: {}", e.getMessage());
        }
    }
}
