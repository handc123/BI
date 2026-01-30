package com.zjrcu.iras.bi.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.mapper.VisualizationMapper;
import com.zjrcu.iras.bi.platform.service.impl.DatasetServiceImpl;
import com.zjrcu.iras.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 数据集服务测试类
 *
 * @author iras
 */
@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {

    @Mock
    private DatasetMapper datasetMapper;

    @Mock
    private VisualizationMapper visualizationMapper;

    @Mock
    private IQueryExecutor queryExecutor;

    @InjectMocks
    private DatasetServiceImpl datasetService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    /**
     * 测试查询数据集列表
     */
    @Test
    void testSelectDatasetList() throws Exception {
        // 准备测试数据
        Dataset queryDataset = new Dataset();
        queryDataset.setName("测试");

        List<Dataset> expectedList = Arrays.asList(
                createTestDataset(1L, "数据集1", "direct"),
                createTestDataset(2L, "数据集2", "extract")
        );

        when(datasetMapper.selectDatasetList(queryDataset)).thenReturn(expectedList);

        // 执行测试
        List<Dataset> result = datasetService.selectDatasetList(queryDataset);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("数据集1", result.get(0).getName());
        assertEquals("数据集2", result.get(1).getName());

        verify(datasetMapper, times(1)).selectDatasetList(queryDataset);
    }

    /**
     * 测试根据ID查询数据集
     */
    @Test
    void testSelectDatasetById() throws Exception {
        // 准备测试数据
        Long datasetId = 1L;
        Dataset expectedDataset = createTestDataset(datasetId, "测试数据集", "direct");

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(expectedDataset);

        // 执行测试
        Dataset result = datasetService.selectDatasetById(datasetId);

        // 验证结果
        assertNotNull(result);
        assertEquals(datasetId, result.getId());
        assertEquals("测试数据集", result.getName());

        verify(datasetMapper, times(1)).selectDatasetById(datasetId);
    }

    /**
     * 测试查询数据集时ID为空
     */
    @Test
    void testSelectDatasetById_NullId() {
        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            datasetService.selectDatasetById(null);
        });

        verify(datasetMapper, never()).selectDatasetById(any());
    }

    /**
     * 测试新增数据集
     */
    @Test
    void testInsertDataset() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "新数据集", "direct");

        when(datasetMapper.insertDataset(any(Dataset.class))).thenReturn(1);

        // 执行测试
        int result = datasetService.insertDataset(dataset);

        // 验证结果
        assertEquals(1, result);
        assertEquals("0", dataset.getStatus()); // 验证默认状态

        verify(datasetMapper, times(1)).insertDataset(dataset);
    }

    /**
     * 测试新增数据集时配置验证失败
     */
    @Test
    void testInsertDataset_InvalidConfig() {
        // 准备测试数据 - 缺少查询配置
        Dataset dataset = new Dataset();
        dataset.setName("测试数据集");
        dataset.setDatasourceId(1L);
        dataset.setType("direct");
        // 缺少queryConfig

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            datasetService.insertDataset(dataset);
        });

        verify(datasetMapper, never()).insertDataset(any());
    }

    /**
     * 测试更新数据集
     */
    @Test
    void testUpdateDataset() throws Exception {
        // 准备测试数据
        Long datasetId = 1L;
        Dataset existingDataset = createTestDataset(datasetId, "原数据集", "direct");
        Dataset updateDataset = createTestDataset(datasetId, "更新数据集", "direct");

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(existingDataset);
        when(datasetMapper.updateDataset(any(Dataset.class))).thenReturn(1);

        // 执行测试
        int result = datasetService.updateDataset(updateDataset);

        // 验证结果
        assertEquals(1, result);

        verify(datasetMapper, times(1)).selectDatasetById(datasetId);
        verify(datasetMapper, times(1)).updateDataset(updateDataset);
    }

    /**
     * 测试更新不存在的数据集
     */
    @Test
    void testUpdateDataset_NotFound() throws Exception {
        // 准备测试数据
        Long datasetId = 999L;
        Dataset updateDataset = createTestDataset(datasetId, "更新数据集", "direct");

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            datasetService.updateDataset(updateDataset);
        });

        verify(datasetMapper, times(1)).selectDatasetById(datasetId);
        verify(datasetMapper, never()).updateDataset(any());
    }

    /**
     * 测试删除数据集
     */
    @Test
    void testDeleteDatasetById() throws Exception {
        // 准备测试数据
        Long datasetId = 1L;
        Dataset dataset = createTestDataset(datasetId, "测试数据集", "direct");

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(dataset);
        when(visualizationMapper.selectVisualizationByDatasetId(datasetId)).thenReturn(Collections.emptyList());
        when(datasetMapper.deleteDatasetById(datasetId)).thenReturn(1);

        // 执行测试
        int result = datasetService.deleteDatasetById(datasetId);

        // 验证结果
        assertEquals(1, result);

        verify(datasetMapper, times(1)).selectDatasetById(datasetId);
        verify(visualizationMapper, times(1)).selectVisualizationByDatasetId(datasetId);
        verify(datasetMapper, times(1)).deleteDatasetById(datasetId);
    }

    /**
     * 测试删除有依赖的数据集
     */
    @Test
    void testDeleteDatasetById_HasDependencies() throws Exception {
        // 准备测试数据
        Long datasetId = 1L;
        Dataset dataset = createTestDataset(datasetId, "测试数据集", "direct");

        Visualization visualization = new Visualization();
        visualization.setId(1L);
        visualization.setName("测试可视化");
        visualization.setDatasetId(datasetId);

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(dataset);
        when(visualizationMapper.selectVisualizationByDatasetId(datasetId))
                .thenReturn(Collections.singletonList(visualization));

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            datasetService.deleteDatasetById(datasetId);
        });

        assertTrue(exception.getMessage().contains("可视化使用"));

        verify(datasetMapper, times(1)).selectDatasetById(datasetId);
        verify(visualizationMapper, times(1)).selectVisualizationByDatasetId(datasetId);
        verify(datasetMapper, never()).deleteDatasetById(any());
    }

    /**
     * 测试批量删除数据集
     */
    @Test
    void testDeleteDatasetByIds() throws Exception {
        // 准备测试数据
        Long[] ids = {1L, 2L};

        when(visualizationMapper.selectVisualizationByDatasetId(1L)).thenReturn(Collections.emptyList());
        when(visualizationMapper.selectVisualizationByDatasetId(2L)).thenReturn(Collections.emptyList());
        when(datasetMapper.deleteDatasetByIds(ids)).thenReturn(2);

        // 执行测试
        int result = datasetService.deleteDatasetByIds(ids);

        // 验证结果
        assertEquals(2, result);

        verify(visualizationMapper, times(2)).selectVisualizationByDatasetId(anyLong());
        verify(datasetMapper, times(1)).deleteDatasetByIds(ids);
    }

    /**
     * 测试批量删除时有依赖
     */
    @Test
    void testDeleteDatasetByIds_HasDependencies() throws Exception {
        // 准备测试数据
        Long[] ids = {1L, 2L};
        Dataset dataset1 = createTestDataset(1L, "数据集1", "direct");

        Visualization visualization = new Visualization();
        visualization.setId(1L);
        visualization.setDatasetId(1L);

        when(visualizationMapper.selectVisualizationByDatasetId(1L))
                .thenReturn(Collections.singletonList(visualization));
        when(datasetMapper.selectDatasetById(1L)).thenReturn(dataset1);

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            datasetService.deleteDatasetByIds(ids);
        });

        assertTrue(exception.getMessage().contains("数据集1"));
        assertTrue(exception.getMessage().contains("可视化使用"));

        verify(datasetMapper, never()).deleteDatasetByIds(any());
    }

    /**
     * 测试预览数据集
     */
    @Test
    void testPreviewDataset() {
        // 准备测试数据
        Long datasetId = 1L;
        List<Filter> filters = new ArrayList<>();

        QueryResult expectedResult = QueryResult.success(
                Collections.singletonList(Collections.singletonMap("id", 1)),
                Collections.emptyList(),
                0
        );

        when(queryExecutor.executeQuery(eq(datasetId), eq(filters), isNull()))
                .thenReturn(expectedResult);

        // 执行测试
        QueryResult result = datasetService.previewDataset(datasetId, filters);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRows());

        verify(queryExecutor, times(1)).executeQuery(datasetId, filters, null);
    }

    /**
     * 测试预览数据集时ID为空
     */
    @Test
    void testPreviewDataset_NullId() {
        // 执行测试
        QueryResult result = datasetService.previewDataset(null, null);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("ID不能为空"));

        verify(queryExecutor, never()).executeQuery(any(), any(), any());
    }

    /**
     * 测试验证直连数据集配置
     */
    @Test
    void testValidateDatasetConfig_Direct() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "direct");

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNull(result, "验证应该通过");
    }

    /**
     * 测试验证抽取数据集配置
     */
    @Test
    void testValidateDatasetConfig_Extract() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "extract");

        Map<String, Object> extractConfig = new HashMap<>();
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("enabled", true);
        schedule.put("cronExpression", "0 0 2 * * ?");
        extractConfig.put("schedule", schedule);
        extractConfig.put("incremental", false);

        dataset.setExtractConfig(objectMapper.writeValueAsString(extractConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNull(result, "验证应该通过");
    }

    /**
     * 测试验证配置 - 缺少数据集名称
     */
    @Test
    void testValidateDatasetConfig_MissingName() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, null, "direct");

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("名称不能为空"));
    }

    /**
     * 测试验证配置 - 无效的数据集类型
     */
    @Test
    void testValidateDatasetConfig_InvalidType() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "invalid");

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("类型必须是direct或extract"));
    }

    /**
     * 测试验证配置 - 缺少查询配置
     */
    @Test
    void testValidateDatasetConfig_MissingQueryConfig() {
        // 准备测试数据
        Dataset dataset = new Dataset();
        dataset.setName("测试数据集");
        dataset.setDatasourceId(1L);
        dataset.setType("direct");
        // 缺少queryConfig

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("查询配置不能为空"));
    }

    /**
     * 测试验证配置 - 无效的查询配置JSON
     */
    @Test
    void testValidateDatasetConfig_InvalidQueryConfigJson() {
        // 准备测试数据
        Dataset dataset = new Dataset();
        dataset.setName("测试数据集");
        dataset.setDatasourceId(1L);
        dataset.setType("direct");
        dataset.setQueryConfig("invalid json");

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("JSON格式错误"));
    }

    /**
     * 测试验证配置 - 查询配置缺少sourceType
     */
    @Test
    void testValidateDatasetConfig_MissingSourceType() throws Exception {
        // 准备测试数据
        Dataset dataset = new Dataset();
        dataset.setName("测试数据集");
        dataset.setDatasourceId(1L);
        dataset.setType("direct");

        Map<String, Object> queryConfig = new HashMap<>();
        // 缺少sourceType
        queryConfig.put("tableName", "test_table");

        dataset.setQueryConfig(objectMapper.writeValueAsString(queryConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("sourceType"));
    }

    /**
     * 测试验证配置 - table类型缺少tableName
     */
    @Test
    void testValidateDatasetConfig_MissingTableName() throws Exception {
        // 准备测试数据
        Dataset dataset = new Dataset();
        dataset.setName("测试数据集");
        dataset.setDatasourceId(1L);
        dataset.setType("direct");

        Map<String, Object> queryConfig = new HashMap<>();
        queryConfig.put("sourceType", "table");
        // 缺少tableName

        dataset.setQueryConfig(objectMapper.writeValueAsString(queryConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("tableName"));
    }

    /**
     * 测试验证配置 - sql类型缺少sql
     */
    @Test
    void testValidateDatasetConfig_MissingSql() throws Exception {
        // 准备测试数据
        Dataset dataset = new Dataset();
        dataset.setName("测试数据集");
        dataset.setDatasourceId(1L);
        dataset.setType("direct");

        Map<String, Object> queryConfig = new HashMap<>();
        queryConfig.put("sourceType", "sql");
        // 缺少sql

        dataset.setQueryConfig(objectMapper.writeValueAsString(queryConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("sql"));
    }

    /**
     * 测试验证配置 - 字段配置包含计算字段
     */
    @Test
    void testValidateDatasetConfig_WithCalculatedFields() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "direct");

        Map<String, Object> fieldConfig = new HashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();

        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "id");
        field1.put("alias", "ID");
        field1.put("type", "BIGINT");
        field1.put("visible", true);
        field1.put("calculated", false);
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "profit_margin");
        field2.put("alias", "利润率");
        field2.put("type", "DECIMAL");
        field2.put("visible", true);
        field2.put("calculated", true);
        field2.put("expression", "profit / revenue * 100");
        fields.add(field2);

        fieldConfig.put("fields", fields);
        dataset.setFieldConfig(objectMapper.writeValueAsString(fieldConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNull(result, "验证应该通过");
    }

    /**
     * 测试验证配置 - 计算字段缺少expression
     */
    @Test
    void testValidateDatasetConfig_CalculatedFieldMissingExpression() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "direct");

        Map<String, Object> fieldConfig = new HashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();

        Map<String, Object> field = new HashMap<>();
        field.put("name", "profit_margin");
        field.put("calculated", true);
        // 缺少expression
        fields.add(field);

        fieldConfig.put("fields", fields);
        dataset.setFieldConfig(objectMapper.writeValueAsString(fieldConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("expression"));
    }

    /**
     * 测试验证配置 - 抽取数据集缺少抽取配置
     */
    @Test
    void testValidateDatasetConfig_ExtractMissingConfig() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "extract");
        // 缺少extractConfig

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("抽取配置"));
    }

    /**
     * 测试验证配置 - 抽取配置启用调度但缺少cron表达式
     */
    @Test
    void testValidateDatasetConfig_ExtractMissingCron() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "extract");

        Map<String, Object> extractConfig = new HashMap<>();
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("enabled", true);
        // 缺少cronExpression
        extractConfig.put("schedule", schedule);

        dataset.setExtractConfig(objectMapper.writeValueAsString(extractConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("cronExpression"));
    }

    /**
     * 测试验证配置 - 增量抽取缺少incrementalField
     */
    @Test
    void testValidateDatasetConfig_IncrementalMissingField() throws Exception {
        // 准备测试数据
        Dataset dataset = createTestDataset(null, "测试数据集", "extract");

        Map<String, Object> extractConfig = new HashMap<>();
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("enabled", false);
        extractConfig.put("schedule", schedule);
        extractConfig.put("incremental", true);
        // 缺少incrementalField

        dataset.setExtractConfig(objectMapper.writeValueAsString(extractConfig));

        // 执行测试
        String result = datasetService.validateDatasetConfig(dataset);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("incrementalField"));
    }

    /**
     * 创建测试数据集
     */
    private Dataset createTestDataset(Long id, String name, String type) throws Exception {
        Dataset dataset = new Dataset();
        dataset.setId(id);
        dataset.setName(name);
        dataset.setDatasourceId(1L);
        dataset.setType(type);

        // 设置查询配置
        Map<String, Object> queryConfig = new HashMap<>();
        queryConfig.put("sourceType", "table");
        queryConfig.put("tableName", "test_table");
        dataset.setQueryConfig(objectMapper.writeValueAsString(queryConfig));

        dataset.setStatus("0");

        return dataset;
    }
}
