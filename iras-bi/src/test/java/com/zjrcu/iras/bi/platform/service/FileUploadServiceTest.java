package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.dto.FileUploadResult;
import com.zjrcu.iras.bi.platform.mapper.DataSourceMapper;
import com.zjrcu.iras.bi.platform.service.impl.FileUploadServiceImpl;
import com.zjrcu.iras.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 文件上传服务测试
 *
 * @author iras
 */
class FileUploadServiceTest {

    @Mock
    private DataSourceMapper dataSourceMapper;

    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadFile_NullFile() {
        // 测试空文件
        FileUploadResult result = fileUploadService.uploadFile(null, "test", "test");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("上传文件不能为空"));
    }

    @Test
    void testUploadFile_EmptyFile() {
        // 测试空文件
        MultipartFile emptyFile = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);
        FileUploadResult result = fileUploadService.uploadFile(emptyFile, "test", "test");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("上传文件不能为空"));
    }

    @Test
    void testUploadFile_FileSizeExceeded() {
        // 测试文件大小超过限制 (50MB)
        byte[] largeContent = new byte[51 * 1024 * 1024]; // 51MB
        MultipartFile largeFile = new MockMultipartFile("file", "large.csv", "text/csv", largeContent);
        
        FileUploadResult result = fileUploadService.uploadFile(largeFile, "test", "test");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("文件大小超过限制"));
    }

    @Test
    void testUploadFile_InvalidExtension() {
        // 测试不支持的文件格式
        byte[] content = "test content".getBytes();
        MultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", content);
        
        FileUploadResult result = fileUploadService.uploadFile(invalidFile, "test", "test");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不支持的文件格式"));
    }

    @Test
    void testUploadFile_ValidCsvFile() throws IOException {
        // 准备CSV文件内容
        String csvContent = "id,name,age,salary,active\n" +
                           "1,张三,25,5000.50,true\n" +
                           "2,李四,30,6000.75,false\n" +
                           "3,王五,28,5500.00,true\n";
        
        MultipartFile csvFile = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            csvContent.getBytes()
        );

        // Mock数据源插入成功
        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenAnswer(invocation -> {
            DataSource ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        FileUploadResult result = fileUploadService.uploadFile(csvFile, "测试CSV", "测试备注");

        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getDataSourceId());
        assertEquals("test.csv", result.getFileName());
        assertEquals(3L, result.getRowCount());
        assertNotNull(result.getColumns());
        assertEquals(5, result.getColumns().size());
        
        // 验证列信息
        assertEquals("id", result.getColumns().get(0).getName());
        assertEquals("INTEGER", result.getColumns().get(0).getType());
        
        assertEquals("name", result.getColumns().get(1).getName());
        assertEquals("STRING", result.getColumns().get(1).getType());
        
        assertEquals("age", result.getColumns().get(2).getName());
        assertEquals("INTEGER", result.getColumns().get(2).getType());
        
        assertEquals("salary", result.getColumns().get(3).getName());
        assertEquals("DECIMAL", result.getColumns().get(3).getType());
        
        assertEquals("active", result.getColumns().get(4).getName());
        assertEquals("BOOLEAN", result.getColumns().get(4).getType());

        // 验证预览数据
        assertNotNull(result.getPreviewData());
        assertEquals(3, result.getPreviewData().size());
        
        // 验证数据源插入被调用
        verify(dataSourceMapper, times(1)).insertDataSource(any(DataSource.class));
    }

    @Test
    void testUploadFile_InvalidCsvFormat() {
        // 测试无效的CSV格式
        String invalidCsv = "id,name\n1,张三\n2"; // 缺少列
        
        MultipartFile csvFile = new MockMultipartFile(
            "file", 
            "invalid.csv", 
            "text/csv", 
            invalidCsv.getBytes()
        );

        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenAnswer(invocation -> {
            DataSource ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        // 应该能够处理不完整的行
        FileUploadResult result = fileUploadService.uploadFile(csvFile, "测试", "测试");
        
        // CSV解析器会处理不完整的行，所以应该成功
        assertTrue(result.isSuccess());
    }

    @Test
    void testUploadFile_DataSourceInsertFailed() throws IOException {
        // 测试数据源插入失败
        String csvContent = "id,name\n1,张三\n2,李四\n";
        
        MultipartFile csvFile = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            csvContent.getBytes()
        );

        // Mock数据源插入失败
        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenReturn(0);

        FileUploadResult result = fileUploadService.uploadFile(csvFile, "测试", "测试");

        // 验证结果
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("创建文件数据源失败"));
    }

    @Test
    void testDeleteFileDataSource_Success() {
        // 准备测试数据
        DataSource dataSource = new DataSource();
        dataSource.setId(1L);
        dataSource.setType("file");
        dataSource.setConfig("{\"filePath\":\"/tmp/test.csv\"}");

        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(dataSource);
        when(dataSourceMapper.deleteDataSourceById(1L)).thenReturn(1);

        boolean result = fileUploadService.deleteFileDataSource(1L);

        assertTrue(result);
        verify(dataSourceMapper, times(1)).deleteDataSourceById(1L);
    }

    @Test
    void testDeleteFileDataSource_NotFound() {
        // 测试数据源不存在
        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(null);

        boolean result = fileUploadService.deleteFileDataSource(1L);

        assertFalse(result);
        verify(dataSourceMapper, never()).deleteDataSourceById(any());
    }

    @Test
    void testDeleteFileDataSource_NotFileType() {
        // 测试非文件类型数据源
        DataSource dataSource = new DataSource();
        dataSource.setId(1L);
        dataSource.setType("mysql");

        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(dataSource);

        assertThrows(ServiceException.class, () -> {
            fileUploadService.deleteFileDataSource(1L);
        });
    }

    @Test
    void testPreviewFileData_DataSourceNotFound() {
        // 测试数据源不存在
        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> {
            fileUploadService.previewFileData(1L, 1, 10);
        });
    }

    @Test
    void testPreviewFileData_NotFileType() {
        // 测试非文件类型数据源
        DataSource dataSource = new DataSource();
        dataSource.setId(1L);
        dataSource.setType("mysql");

        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(dataSource);

        assertThrows(ServiceException.class, () -> {
            fileUploadService.previewFileData(1L, 1, 10);
        });
    }

    @Test
    void testCleanupUnusedFileSources() {
        // 测试清理未使用的文件数据源
        when(dataSourceMapper.selectDataSourceList(any(DataSource.class))).thenReturn(List.of());

        int count = fileUploadService.cleanupUnusedFileSources();

        assertEquals(0, count);
        verify(dataSourceMapper, times(1)).selectDataSourceList(any(DataSource.class));
    }

    @Test
    void testUploadFile_DateTypeDetection() throws IOException {
        // 测试日期类型检测
        String csvContent = "id,date,name\n" +
                           "1,2024-01-15,张三\n" +
                           "2,2024-01-16,李四\n" +
                           "3,2024-01-17,王五\n";
        
        MultipartFile csvFile = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            csvContent.getBytes()
        );

        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenAnswer(invocation -> {
            DataSource ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        FileUploadResult result = fileUploadService.uploadFile(csvFile, "测试日期", "测试");

        assertTrue(result.isSuccess());
        assertEquals(3, result.getColumns().size());
        
        // 验证日期列类型
        FileUploadResult.ColumnInfo dateColumn = result.getColumns().get(1);
        assertEquals("date", dateColumn.getName());
        assertEquals("DATE", dateColumn.getType());
    }

    @Test
    void testUploadFile_MixedTypeDefaultsToString() throws IOException {
        // 测试混合类型默认为字符串
        String csvContent = "id,mixed\n" +
                           "1,100\n" +
                           "2,text\n" +
                           "3,200\n";
        
        MultipartFile csvFile = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            csvContent.getBytes()
        );

        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenAnswer(invocation -> {
            DataSource ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        FileUploadResult result = fileUploadService.uploadFile(csvFile, "测试混合类型", "测试");

        assertTrue(result.isSuccess());
        
        // 混合类型应该被识别为字符串
        FileUploadResult.ColumnInfo mixedColumn = result.getColumns().get(1);
        assertEquals("mixed", mixedColumn.getName());
        assertEquals("STRING", mixedColumn.getType());
    }

    @Test
    void testUploadFile_EmptyColumnsDefaultToString() throws IOException {
        // 测试空列默认为字符串
        String csvContent = "id,empty,name\n" +
                           "1,,张三\n" +
                           "2,,李四\n" +
                           "3,,王五\n";
        
        MultipartFile csvFile = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            csvContent.getBytes()
        );

        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenAnswer(invocation -> {
            DataSource ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        FileUploadResult result = fileUploadService.uploadFile(csvFile, "测试空列", "测试");

        assertTrue(result.isSuccess());
        
        // 空列应该被识别为字符串
        FileUploadResult.ColumnInfo emptyColumn = result.getColumns().get(1);
        assertEquals("empty", emptyColumn.getName());
        assertEquals("STRING", emptyColumn.getType());
    }
}
