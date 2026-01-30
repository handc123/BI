package com.zjrcu.iras.bi.platform.engine;

import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.Metric;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.common.core.domain.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * QueryExecutor单元测试
 *
 * @author iras
 */
@ExtendWith(MockitoExtension.class)
class QueryExecutorTest {

    @Mock
    private DatasetMapper datasetMapper;

    @Mock
    private DataSourceManager dataSourceManager;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData metaData;

    @InjectMocks
    private QueryExecutor queryExecutor;

    private Dataset testDataset;
    private SysUser testUser;

    @BeforeEach
    void setUp() {
        // 创建测试数据集
        testDataset = new Dataset();
        testDataset.setId(1L);
        testDataset.setName("测试数据集");
        testDataset.setDatasourceId(1L);
        testDataset.setType("direct");
        testDataset.setStatus("0");

        // 设置查询配置
        Map<String, Object> queryConfig = new HashMap<>();
        queryConfig.put("sourceType", "table");
        queryConfig.put("tableName", "sys_user");
        testDataset.setQueryConfigMap(queryConfig);

        // 设置字段配置
        Map<String, Object> fieldConfig = new HashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();

        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "user_id");
        field1.put("alias", "用户ID");
        field1.put("type", "BIGINT");
        field1.put("visible", true);
        field1.put("calculated", false);
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "user_name");
        field2.put("alias", "用户名");
        field2.put("type", "VARCHAR");
        field2.put("visible", true);
        field2.put("calculated", false);
        fields.add(field2);

        fieldConfig.put("fields", fields);
        testDataset.setFieldConfigMap(fieldConfig);

        // 创建测试用户
        testUser = new SysUser();
        testUser.setUserId(1L);
        testUser.setDeptId(100L);
    }

    @Test
    void testExecuteQuery_NullDatasetId() {
        // 测试数据集ID为空的情况
        QueryResult result = queryExecutor.executeQuery(null, null, testUser);

        assertFalse(result.isSuccess());
        assertEquals("数据集ID不能为空", result.getErrorMessage());
    }

    @Test
    void testExecuteQuery_DatasetNotFound() {
        // 测试数据集不存在的情况
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(null);

        QueryResult result = queryExecutor.executeQuery(1L, null, testUser);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("数据集不存在"));
    }

    @Test
    void testExecuteQuery_DatasetDisabled() {
        // 测试数据集已停用的情况
        testDataset.setStatus("1");
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);

        QueryResult result = queryExecutor.executeQuery(1L, null, testUser);

        assertFalse(result.isSuccess());
        assertEquals("数据集已停用", result.getErrorMessage());
    }

    @Test
    void testExecuteQuery_DirectDataset_Success() throws SQLException {
        // 测试直连数据集查询成功
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);
        when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);

        // 模拟结果集元数据
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnLabel(1)).thenReturn("user_id");
        when(metaData.getColumnTypeName(1)).thenReturn("BIGINT");
        when(metaData.getColumnLabel(2)).thenReturn("user_name");
        when(metaData.getColumnTypeName(2)).thenReturn("VARCHAR");

        // 模拟结果集数据
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(1)).thenReturn(1L, 2L);
        when(resultSet.getObject(2)).thenReturn("admin", "user");

        QueryResult result = queryExecutor.executeQuery(1L, null, testUser);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getFields().size());
        assertEquals(2, result.getData().size());

        verify(dataSourceManager).releaseConnection(connection);
    }

    @Test
    void testExecuteQuery_WithFilters() throws SQLException {
        // 测试带筛选条件的查询
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);
        when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("user_id");
        when(metaData.getColumnTypeName(1)).thenReturn("BIGINT");
        when(resultSet.next()).thenReturn(false);

        // 创建筛选条件
        List<Filter> filters = new ArrayList<>();
        filters.add(new Filter("user_name", "eq", "admin"));
        filters.add(new Filter("status", "ne", "1"));

        QueryResult result = queryExecutor.executeQuery(1L, filters, testUser);

        assertTrue(result.isSuccess());
        verify(statement).executeQuery(contains("user_name = 'admin'"));
        verify(statement).executeQuery(contains("status != '1'"));
    }

    @Test
    void testExecuteQuery_WithPermissionFilter() throws SQLException {
        // 测试权限过滤(基于用户部门ID)
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);
        when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("user_id");
        when(metaData.getColumnTypeName(1)).thenReturn("BIGINT");
        when(resultSet.next()).thenReturn(false);

        QueryResult result = queryExecutor.executeQuery(1L, null, testUser);

        assertTrue(result.isSuccess());
        // 验证SQL包含部门ID过滤条件
        verify(statement).executeQuery(contains("dept_id = 100"));
    }

    @Test
    void testExecuteQuery_ExtractDataset() {
        // 测试抽取数据集查询
        testDataset.setType("extract");
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);

        try {
            when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            when(resultSet.getMetaData()).thenReturn(metaData);
            when(metaData.getColumnCount()).thenReturn(1);
            when(metaData.getColumnLabel(1)).thenReturn("data_content");
            when(metaData.getColumnTypeName(1)).thenReturn("TEXT");
            when(resultSet.next()).thenReturn(false);

            QueryResult result = queryExecutor.executeQuery(1L, null, testUser);

            assertTrue(result.isSuccess());
            verify(statement).executeQuery(contains("bi_extract_data"));
        } catch (SQLException e) {
            fail("不应该抛出异常");
        }
    }

    @Test
    void testExecuteQuery_SqlTimeout() throws SQLException {
        // 测试查询超时
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);
        when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenThrow(new SQLTimeoutException("查询超时"));

        QueryResult result = queryExecutor.executeQuery(1L, null, testUser);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("查询超时"));
        assertTrue(result.getErrorMessage().contains("抽取数据集"));
    }

    @Test
    void testExecuteAggregation_NullDatasetId() {
        // 测试聚合查询数据集ID为空
        QueryResult result = queryExecutor.executeAggregation(null, null, null, null, testUser);

        assertFalse(result.isSuccess());
        assertEquals("数据集ID不能为空", result.getErrorMessage());
    }

    @Test
    void testExecuteAggregation_EmptyDimensionsAndMetrics() {
        // 测试维度和度量都为空
        QueryResult result = queryExecutor.executeAggregation(1L, new ArrayList<>(), new ArrayList<>(), null, testUser);

        assertFalse(result.isSuccess());
        assertEquals("维度和度量不能同时为空", result.getErrorMessage());
    }

    @Test
    void testExecuteAggregation_Success() throws SQLException {
        // 测试聚合查询成功
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);
        when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);

        // 模拟聚合结果: dept_id, COUNT(*)
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnLabel(1)).thenReturn("dept_id");
        when(metaData.getColumnTypeName(1)).thenReturn("BIGINT");
        when(metaData.getColumnLabel(2)).thenReturn("user_count");
        when(metaData.getColumnTypeName(2)).thenReturn("BIGINT");

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn(100L);
        when(resultSet.getObject(2)).thenReturn(5L);

        List<String> dimensions = Arrays.asList("dept_id");
        List<Metric> metrics = Arrays.asList(new Metric("user_id", "COUNT", "user_count"));

        QueryResult result = queryExecutor.executeAggregation(1L, dimensions, metrics, null, testUser);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRows());
        verify(statement).executeQuery(contains("GROUP BY"));
        verify(statement).executeQuery(contains("COUNT(user_id)"));
    }

    @Test
    void testFilterOperators() throws SQLException {
        // 测试各种筛选操作符
        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);
        when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("user_id");
        when(metaData.getColumnTypeName(1)).thenReturn("BIGINT");
        when(resultSet.next()).thenReturn(false);

        // 测试like操作符
        List<Filter> filters = Arrays.asList(new Filter("user_name", "like", "admin"));
        queryExecutor.executeQuery(1L, filters, testUser);
        verify(statement).executeQuery(contains("LIKE '%admin%'"));

        // 测试in操作符
        reset(statement);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        Filter inFilter = new Filter();
        inFilter.setField("status");
        inFilter.setOperator("in");
        inFilter.setValues(Arrays.asList("0", "1"));
        filters = Arrays.asList(inFilter);
        queryExecutor.executeQuery(1L, filters, testUser);
        verify(statement).executeQuery(contains("IN ("));

        // 测试between操作符
        reset(statement);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        Filter betweenFilter = new Filter();
        betweenFilter.setField("create_time");
        betweenFilter.setOperator("between");
        betweenFilter.setValues(Arrays.asList("2024-01-01", "2024-12-31"));
        filters = Arrays.asList(betweenFilter);
        queryExecutor.executeQuery(1L, filters, testUser);
        verify(statement).executeQuery(contains("BETWEEN"));
    }

    @Test
    void testCalculatedFields() throws SQLException {
        // 测试计算字段
        Map<String, Object> fieldConfig = new HashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();

        // 添加计算字段
        Map<String, Object> calcField = new HashMap<>();
        calcField.put("name", "profit_margin");
        calcField.put("alias", "利润率");
        calcField.put("type", "DECIMAL");
        calcField.put("visible", true);
        calcField.put("calculated", true);
        calcField.put("expression", "profit / revenue * 100");
        fields.add(calcField);

        fieldConfig.put("fields", fields);
        testDataset.setFieldConfigMap(fieldConfig);

        when(datasetMapper.selectDatasetById(anyLong())).thenReturn(testDataset);
        when(dataSourceManager.getConnection(anyLong())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("profit_margin");
        when(metaData.getColumnTypeName(1)).thenReturn("DECIMAL");
        when(resultSet.next()).thenReturn(false);

        QueryResult result = queryExecutor.executeQuery(1L, null, testUser);

        assertTrue(result.isSuccess());
        // 验证SQL包含计算字段表达式
        verify(statement).executeQuery(contains("profit / revenue * 100"));
    }
}
