package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.ConnectionTestResult;
import com.zjrcu.iras.bi.platform.engine.DataSourceManager;
import com.zjrcu.iras.bi.platform.mapper.DataSourceMapper;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.service.impl.DataSourceServiceImpl;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.AesUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 数据源服务测试类
 *
 * @author iras
 */
@ExtendWith(MockitoExtension.class)
class DataSourceServiceTest {

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Mock
    private DatasetMapper datasetMapper;

    @Mock
    private DataSourceManager dataSourceManager;

    @InjectMocks
    private DataSourceServiceImpl dataSourceService;

    private DataSource testDataSource;

    @BeforeEach
    void setUp() {
        testDataSource = new DataSource();
        testDataSource.setId(1L);
        testDataSource.setName("测试数据源");
        testDataSource.setType("mysql");
        testDataSource.setStatus("0");

        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 3306);
        config.put("database", "test_db");
        config.put("username", "root");
        config.put("password", "password123");
        testDataSource.setConfigMap(config);
    }

    @Test
    void testSelectDataSourceList_Success() {
        // 准备测试数据
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(testDataSource);

        when(dataSourceMapper.selectDataSourceList(any(DataSource.class))).thenReturn(dataSourceList);

        // 执行测试
        List<DataSource> result = dataSourceService.selectDataSourceList(new DataSource());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("********", result.get(0).getConfigMap().get("password"));
        verify(dataSourceMapper, times(1)).selectDataSourceList(any(DataSource.class));
    }

    @Test
    void testSelectDataSourceById_Success() {
        // 准备测试数据
        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(testDataSource);

        // 执行测试
        DataSource result = dataSourceService.selectDataSourceById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("测试数据源", result.getName());
        verify(dataSourceMapper, times(1)).selectDataSourceById(1L);
    }

    @Test
    void testSelectDataSourceById_NotFound() {
        // 准备测试数据
        when(dataSourceMapper.selectDataSourceById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            dataSourceService.selectDataSourceById(999L);
        });
    }

    @Test
    void testInsertDataSource_Success() {
        // 准备测试数据
        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenReturn(1);
        doNothing().when(dataSourceManager).initializeDataSource(any(DataSource.class));

        // 执行测试
        int result = dataSourceService.insertDataSource(testDataSource);

        // 验证结果
        assertEquals(1, result);
        verify(dataSourceMapper, times(1)).insertDataSource(any(DataSource.class));
        verify(dataSourceManager, times(1)).initializeDataSource(any(DataSource.class));
    }

    @Test
    void testInsertDataSource_InvalidConfig() {
        // 准备测试数据 - 缺少必需字段
        DataSource invalidDataSource = new DataSource();
        invalidDataSource.setName("无效数据源");
        invalidDataSource.setType("mysql");
        
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("host", "localhost");
        // 缺少port、database、username
        invalidDataSource.setConfigMap(invalidConfig);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            dataSourceService.insertDataSource(invalidDataSource);
        });
    }

    @Test
    void testUpdateDataSource_Success() {
        // 准备测试数据
        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(testDataSource);
        when(dataSourceMapper.updateDataSource(any(DataSource.class))).thenReturn(1);
        doNothing().when(dataSourceManager).closeDataSource(1L);
        doNothing().when(dataSourceManager).initializeDataSource(any(DataSource.class));

        // 执行测试
        int result = dataSourceService.updateDataSource(testDataSource);

        // 验证结果
        assertEquals(1, result);
        verify(dataSourceMapper, times(1)).updateDataSource(any(DataSource.class));
        verify(dataSourceManager, times(1)).closeDataSource(1L);
        verify(dataSourceManager, times(1)).initializeDataSource(any(DataSource.class));
    }

    @Test
    void testDeleteDataSourceById_Success() {
        // 准备测试数据
        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(testDataSource);
        when(datasetMapper.selectDatasetList(any(Dataset.class))).thenReturn(new ArrayList<>());
        when(dataSourceMapper.deleteDataSourceById(1L)).thenReturn(1);
        doNothing().when(dataSourceManager).closeDataSource(1L);

        // 执行测试
        int result = dataSourceService.deleteDataSourceById(1L);

        // 验证结果
        assertEquals(1, result);
        verify(dataSourceMapper, times(1)).deleteDataSourceById(1L);
        verify(dataSourceManager, times(1)).closeDataSource(1L);
    }

    @Test
    void testDeleteDataSourceById_HasDependency() {
        // 准备测试数据 - 有依赖的数据集
        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(testDataSource);
        
        List<Dataset> datasets = new ArrayList<>();
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setName("依赖数据集");
        dataset.setDatasourceId(1L);
        datasets.add(dataset);
        
        when(datasetMapper.selectDatasetList(any(Dataset.class))).thenReturn(datasets);

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            dataSourceService.deleteDataSourceById(1L);
        });
        
        assertTrue(exception.getMessage().contains("依赖数据集"));
        verify(dataSourceMapper, never()).deleteDataSourceById(1L);
    }

    @Test
    void testDeleteDataSourceByIds_Success() {
        // 准备测试数据
        Long[] ids = {1L, 2L};
        
        DataSource dataSource2 = new DataSource();
        dataSource2.setId(2L);
        dataSource2.setName("测试数据源2");
        dataSource2.setType("mysql");
        
        when(dataSourceMapper.selectDataSourceById(1L)).thenReturn(testDataSource);
        when(dataSourceMapper.selectDataSourceById(2L)).thenReturn(dataSource2);
        when(datasetMapper.selectDatasetList(any(Dataset.class))).thenReturn(new ArrayList<>());
        when(dataSourceMapper.deleteDataSourceById(anyLong())).thenReturn(1);
        doNothing().when(dataSourceManager).closeDataSource(anyLong());

        // 执行测试
        int result = dataSourceService.deleteDataSourceByIds(ids);

        // 验证结果
        assertEquals(2, result);
        verify(dataSourceMapper, times(2)).deleteDataSourceById(anyLong());
    }

    @Test
    void testTestConnection_Success() {
        // 准备测试数据
        ConnectionTestResult expectedResult = ConnectionTestResult.success("连接成功", 100);
        when(dataSourceManager.testConnection(any(DataSource.class))).thenReturn(expectedResult);

        // 执行测试
        ConnectionTestResult result = dataSourceService.testConnection(testDataSource);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("连接成功", result.getMessage());
        verify(dataSourceManager, times(1)).testConnection(any(DataSource.class));
    }

    @Test
    void testTestConnection_Failure() {
        // 准备测试数据
        ConnectionTestResult expectedResult = ConnectionTestResult.failure("连接失败: 无法连接到数据库");
        when(dataSourceManager.testConnection(any(DataSource.class))).thenReturn(expectedResult);

        // 执行测试
        ConnectionTestResult result = dataSourceService.testConnection(testDataSource);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("连接失败"));
        verify(dataSourceManager, times(1)).testConnection(any(DataSource.class));
    }

    @Test
    void testPasswordEncryption() {
        // 准备测试数据
        when(dataSourceMapper.insertDataSource(any(DataSource.class))).thenAnswer(invocation -> {
            DataSource ds = invocation.getArgument(0);
            String encryptedPassword = (String) ds.getConfigMap().get("password");
            
            // 验证密码已加密（不等于原始密码）
            assertNotEquals("password123", encryptedPassword);
            
            // 验证可以解密
            String decrypted = AesUtils.decrypt(encryptedPassword);
            assertEquals("password123", decrypted);
            
            return 1;
        });

        // 执行测试
        dataSourceService.insertDataSource(testDataSource);

        // 验证
        verify(dataSourceMapper, times(1)).insertDataSource(any(DataSource.class));
    }

    @Test
    void testValidateDataSource_EmptyName() {
        // 准备测试数据
        DataSource invalidDataSource = new DataSource();
        invalidDataSource.setName("");
        invalidDataSource.setType("mysql");
        invalidDataSource.setConfigMap(new HashMap<>());

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            dataSourceService.insertDataSource(invalidDataSource);
        });
    }

    @Test
    void testValidateDataSource_EmptyType() {
        // 准备测试数据
        DataSource invalidDataSource = new DataSource();
        invalidDataSource.setName("测试");
        invalidDataSource.setType("");
        invalidDataSource.setConfigMap(new HashMap<>());

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            dataSourceService.insertDataSource(invalidDataSource);
        });
    }

    @Test
    void testValidateDataSource_UnsupportedType() {
        // 准备测试数据
        DataSource invalidDataSource = new DataSource();
        invalidDataSource.setName("测试");
        invalidDataSource.setType("unsupported");
        
        Map<String, Object> config = new HashMap<>();
        config.put("someField", "someValue");
        invalidDataSource.setConfigMap(config);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            dataSourceService.insertDataSource(invalidDataSource);
        });
    }
}
