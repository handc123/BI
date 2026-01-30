package com.zjrcu.iras.bi.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.service.IDatasetService;
import com.zjrcu.iras.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BI数据集Controller测试
 *
 * @author iras
 */
class DatasetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IDatasetService datasetService;

    @InjectMocks
    private DatasetController datasetController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(datasetController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * 创建测试数据集
     */
    private Dataset createTestDataset() {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setName("测试数据集");
        dataset.setDatasourceId(1L);
        dataset.setType("direct");
        dataset.setQueryConfig("{\"sourceType\":\"table\",\"tableName\":\"sys_user\"}");
        dataset.setFieldConfig("{\"fields\":[{\"name\":\"user_id\",\"type\":\"BIGINT\"}]}");
        dataset.setStatus("0");
        return dataset;
    }

    /**
     * 测试查询数据集列表
     */
    @Test
    void testList() throws Exception {
        // 准备测试数据
        List<Dataset> datasets = Arrays.asList(createTestDataset());
        when(datasetService.selectDatasetList(any(Dataset.class))).thenReturn(datasets);

        // 执行测试
        mockMvc.perform(get("/bi/dataset/list"))
                .andExpect(status().isOk());

        // 验证调用
        verify(datasetService, times(1)).selectDatasetList(any(Dataset.class));
    }

    /**
     * 测试获取数据集详情 - 成功
     */
    @Test
    void testGetInfo_Success() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        when(datasetService.selectDatasetById(1L)).thenReturn(dataset);

        // 执行测试
        mockMvc.perform(get("/bi/dataset/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试数据集"));

        // 验证调用
        verify(datasetService, times(1)).selectDatasetById(1L);
    }

    /**
     * 测试获取数据集详情 - 不存在
     */
    @Test
    void testGetInfo_NotFound() throws Exception {
        // 准备测试数据
        when(datasetService.selectDatasetById(999L)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/bi/dataset/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("数据集不存在"));

        // 验证调用
        verify(datasetService, times(1)).selectDatasetById(999L);
    }

    /**
     * 测试新增数据集 - 成功
     */
    @Test
    void testAdd_Success() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        dataset.setId(null); // 新增时ID为空
        
        when(datasetService.validateDatasetConfig(any(Dataset.class))).thenReturn(null);
        when(datasetService.insertDataset(any(Dataset.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(post("/bi/dataset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证调用
        verify(datasetService, times(1)).validateDatasetConfig(any(Dataset.class));
        verify(datasetService, times(1)).insertDataset(any(Dataset.class));
    }

    /**
     * 测试新增数据集 - 配置验证失败
     */
    @Test
    void testAdd_ValidationFailed() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        dataset.setId(null);
        
        when(datasetService.validateDatasetConfig(any(Dataset.class)))
                .thenReturn("查询配置JSON格式错误");

        // 执行测试
        mockMvc.perform(post("/bi/dataset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("查询配置JSON格式错误"));

        // 验证调用
        verify(datasetService, times(1)).validateDatasetConfig(any(Dataset.class));
        verify(datasetService, never()).insertDataset(any(Dataset.class));
    }

    /**
     * 测试新增数据集 - 服务异常
     */
    @Test
    void testAdd_ServiceException() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        dataset.setId(null);
        
        when(datasetService.validateDatasetConfig(any(Dataset.class))).thenReturn(null);
        when(datasetService.insertDataset(any(Dataset.class)))
                .thenThrow(new ServiceException("数据源不存在"));

        // 执行测试
        mockMvc.perform(post("/bi/dataset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("新增数据集失败: 数据源不存在"));

        // 验证调用
        verify(datasetService, times(1)).insertDataset(any(Dataset.class));
    }

    /**
     * 测试修改数据集 - 成功
     */
    @Test
    void testEdit_Success() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        
        when(datasetService.validateDatasetConfig(any(Dataset.class))).thenReturn(null);
        when(datasetService.updateDataset(any(Dataset.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(put("/bi/dataset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证调用
        verify(datasetService, times(1)).validateDatasetConfig(any(Dataset.class));
        verify(datasetService, times(1)).updateDataset(any(Dataset.class));
    }

    /**
     * 测试修改数据集 - 配置验证失败
     */
    @Test
    void testEdit_ValidationFailed() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        
        when(datasetService.validateDatasetConfig(any(Dataset.class)))
                .thenReturn("数据集类型必须是direct或extract");

        // 执行测试
        mockMvc.perform(put("/bi/dataset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("数据集类型必须是direct或extract"));

        // 验证调用
        verify(datasetService, times(1)).validateDatasetConfig(any(Dataset.class));
        verify(datasetService, never()).updateDataset(any(Dataset.class));
    }

    /**
     * 测试删除数据集 - 成功
     */
    @Test
    void testRemove_Success() throws Exception {
        // 准备测试数据
        when(datasetService.deleteDatasetByIds(any(Long[].class))).thenReturn(2);

        // 执行测试
        mockMvc.perform(delete("/bi/dataset/1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证调用
        verify(datasetService, times(1)).deleteDatasetByIds(any(Long[].class));
    }

    /**
     * 测试删除数据集 - 有依赖关系
     */
    @Test
    void testRemove_HasDependencies() throws Exception {
        // 准备测试数据
        when(datasetService.deleteDatasetByIds(any(Long[].class)))
                .thenThrow(new ServiceException("数据集被3个可视化使用,无法删除"));

        // 执行测试
        mockMvc.perform(delete("/bi/dataset/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("删除数据集失败: 数据集被3个可视化使用,无法删除"));

        // 验证调用
        verify(datasetService, times(1)).deleteDatasetByIds(any(Long[].class));
    }

    /**
     * 测试预览数据集 - 成功
     */
    @Test
    void testPreview_Success() throws Exception {
        // 准备测试数据
        QueryResult queryResult = QueryResult.success(new ArrayList<>(), new ArrayList<>(), 0L);
        when(datasetService.previewDataset(eq(1L), anyList())).thenReturn(queryResult);

        // 执行测试
        List<Filter> filters = new ArrayList<>();
        mockMvc.perform(post("/bi/dataset/1/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证调用
        verify(datasetService, times(1)).previewDataset(eq(1L), anyList());
    }

    /**
     * 测试预览数据集 - 失败
     */
    @Test
    void testPreview_Failed() throws Exception {
        // 准备测试数据
        QueryResult queryResult = QueryResult.failure("查询执行失败: SQL语法错误");
        when(datasetService.previewDataset(eq(1L), anyList())).thenReturn(queryResult);

        // 执行测试
        List<Filter> filters = new ArrayList<>();
        mockMvc.perform(post("/bi/dataset/1/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("查询执行失败: SQL语法错误"));

        // 验证调用
        verify(datasetService, times(1)).previewDataset(eq(1L), anyList());
    }

    /**
     * 测试预览数据集 - 无筛选条件
     */
    @Test
    void testPreview_NoFilters() throws Exception {
        // 准备测试数据
        QueryResult queryResult = QueryResult.success(new ArrayList<>(), new ArrayList<>(), 0L);
        when(datasetService.previewDataset(eq(1L), isNull())).thenReturn(queryResult);

        // 执行测试 - 不传递filters参数
        mockMvc.perform(post("/bi/dataset/1/preview")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证调用
        verify(datasetService, times(1)).previewDataset(eq(1L), isNull());
    }

    /**
     * 测试执行立即抽取 - 数据集不存在
     */
    @Test
    void testExtract_DatasetNotFound() throws Exception {
        // 准备测试数据
        when(datasetService.selectDatasetById(999L)).thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/bi/dataset/999/extract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("数据集不存在"));

        // 验证调用
        verify(datasetService, times(1)).selectDatasetById(999L);
    }

    /**
     * 测试执行立即抽取 - 非抽取类型数据集
     */
    @Test
    void testExtract_NotExtractType() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        dataset.setType("direct"); // 直连类型
        when(datasetService.selectDatasetById(1L)).thenReturn(dataset);

        // 执行测试
        mockMvc.perform(post("/bi/dataset/1/extract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("只有抽取类型数据集才能执行抽取操作"));

        // 验证调用
        verify(datasetService, times(1)).selectDatasetById(1L);
    }

    /**
     * 测试执行立即抽取 - 功能未实现
     */
    @Test
    void testExtract_NotImplemented() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset();
        dataset.setType("extract"); // 抽取类型
        dataset.setExtractConfig("{\"schedule\":{\"enabled\":true,\"cronExpression\":\"0 0 2 * * ?\"}}");
        when(datasetService.selectDatasetById(1L)).thenReturn(dataset);

        // 执行测试
        mockMvc.perform(post("/bi/dataset/1/extract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("数据抽取功能尚未实现,请等待DataExtractScheduler组件完成"));

        // 验证调用
        verify(datasetService, times(1)).selectDatasetById(1L);
    }

    /**
     * 测试边界情况 - 空ID数组删除
     */
    @Test
    void testRemove_EmptyIds() throws Exception {
        // 准备测试数据
        when(datasetService.deleteDatasetByIds(any(Long[].class)))
                .thenThrow(new ServiceException("数据集ID不能为空"));

        // 执行测试
        mockMvc.perform(delete("/bi/dataset/"))
                .andExpect(status().isNotFound()); // 路径不匹配会返回404
    }

    /**
     * 测试预览数据集 - 服务异常
     */
    @Test
    void testPreview_ServiceException() throws Exception {
        // 准备测试数据
        when(datasetService.previewDataset(eq(1L), anyList()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试
        List<Filter> filters = new ArrayList<>();
        mockMvc.perform(post("/bi/dataset/1/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("预览数据集失败: 数据库连接失败"));

        // 验证调用
        verify(datasetService, times(1)).previewDataset(eq(1L), anyList());
    }
}
