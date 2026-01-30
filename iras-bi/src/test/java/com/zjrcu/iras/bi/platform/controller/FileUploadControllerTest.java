package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.dto.FileUploadResult;
import com.zjrcu.iras.bi.platform.service.IFileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件上传控制器测试
 *
 * @author iras
 */
class FileUploadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IFileUploadService fileUploadService;

    @InjectMocks
    private DataSourceController dataSourceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dataSourceController).build();
    }

    @Test
    void testUploadFile_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            "id,name\n1,张三\n2,李四".getBytes()
        );

        List<FileUploadResult.ColumnInfo> columns = new ArrayList<>();
        columns.add(new FileUploadResult.ColumnInfo("id", "INTEGER", 0));
        columns.add(new FileUploadResult.ColumnInfo("name", "STRING", 1));

        List<List<Object>> previewData = new ArrayList<>();
        List<Object> row1 = List.of("1", "张三");
        List<Object> row2 = List.of("2", "李四");
        previewData.add(row1);
        previewData.add(row2);

        FileUploadResult result = FileUploadResult.success(
            1L, "test.csv", 100L, 2L, columns, previewData
        );

        when(fileUploadService.uploadFile(any(), anyString(), anyString())).thenReturn(result);

        // 执行测试
        mockMvc.perform(multipart("/bi/datasource/file/upload")
                .file(file)
                .param("name", "测试文件")
                .param("remark", "测试备注"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.dataSourceId").value(1))
                .andExpect(jsonPath("$.data.fileName").value("test.csv"))
                .andExpect(jsonPath("$.data.rowCount").value(2));
    }

    @Test
    void testUploadFile_Failed() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            "invalid content".getBytes()
        );

        FileUploadResult result = FileUploadResult.error("文件解析失败");

        when(fileUploadService.uploadFile(any(), anyString(), anyString())).thenReturn(result);

        // 执行测试
        mockMvc.perform(multipart("/bi/datasource/file/upload")
                .file(file)
                .param("name", "测试文件"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("文件解析失败"));
    }

    @Test
    void testPreviewFileData_Success() throws Exception {
        // 准备测试数据
        List<List<Object>> previewData = new ArrayList<>();
        previewData.add(List.of("1", "张三", "25"));
        previewData.add(List.of("2", "李四", "30"));

        when(fileUploadService.previewFileData(eq(1L), eq(1), eq(10))).thenReturn(previewData);

        // 执行测试
        mockMvc.perform(get("/bi/datasource/file/1/preview")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testDeleteFileDataSource_Success() throws Exception {
        // 准备测试数据
        when(fileUploadService.deleteFileDataSource(1L)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/bi/datasource/file/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteFileDataSource_Failed() throws Exception {
        // 准备测试数据
        when(fileUploadService.deleteFileDataSource(1L)).thenReturn(false);

        // 执行测试
        mockMvc.perform(delete("/bi/datasource/file/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("删除文件数据源失败"));
    }

    @Test
    void testCleanupUnusedFiles_Success() throws Exception {
        // 准备测试数据
        when(fileUploadService.cleanupUnusedFileSources()).thenReturn(5);

        // 执行测试
        mockMvc.perform(post("/bi/datasource/file/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("成功清理 5 个未使用的文件数据源"));
    }
}
