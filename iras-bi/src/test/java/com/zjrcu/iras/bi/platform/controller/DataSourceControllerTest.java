package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.dto.ConnectionTestResult;
import com.zjrcu.iras.bi.platform.service.IDataSourceService;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DataSourceController单元测试
 *
 * @author iras
 */
class DataSourceControllerTest {

    @Mock
    private IDataSourceService dataSourceService;

    @InjectMocks
    private DataSourceController dataSourceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetInfo_Success() {
        // 准备测试数据
        Long dataSourceId = 1L;
        DataSource dataSource = new DataSource();
        dataSource.setId(dataSourceId);
        dataSource.setName("测试数据源");
        dataSource.setType("mysql");

        // 模拟服务层行为
        when(dataSourceService.selectDataSourceById(dataSourceId)).thenReturn(dataSource);

        // 执行测试
        AjaxResult result = dataSourceController.getInfo(dataSourceId);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.get("code"));
        assertNotNull(result.get("data"));
        
        // 验证服务方法被调用
        verify(dataSourceService, times(1)).selectDataSourceById(dataSourceId);
    }

    @Test
    void testTestConnection_Success() {
        // 准备测试数据
        DataSource dataSource = new DataSource();
        dataSource.setName("测试数据源");
        dataSource.setType("mysql");
        dataSource.setConfig("{\"host\":\"localhost\",\"port\":3306}");

        ConnectionTestResult testResult = ConnectionTestResult.success("连接成功", 100L);

        // 模拟服务层行为
        when(dataSourceService.testConnection(any(DataSource.class))).thenReturn(testResult);

        // 执行测试
        AjaxResult result = dataSourceController.testConnection(dataSource);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.get("code"));
        
        // 验证服务方法被调用
        verify(dataSourceService, times(1)).testConnection(any(DataSource.class));
    }

    @Test
    void testTestConnection_Failure() {
        // 准备测试数据
        DataSource dataSource = new DataSource();
        dataSource.setName("测试数据源");
        dataSource.setType("mysql");
        dataSource.setConfig("{\"host\":\"invalid\",\"port\":3306}");

        ConnectionTestResult testResult = ConnectionTestResult.failure("连接失败: 无法连接到主机");

        // 模拟服务层行为
        when(dataSourceService.testConnection(any(DataSource.class))).thenReturn(testResult);

        // 执行测试
        AjaxResult result = dataSourceController.testConnection(dataSource);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("连接失败"));
        
        // 验证服务方法被调用
        verify(dataSourceService, times(1)).testConnection(any(DataSource.class));
    }

    @Test
    void testAdd_Success() {
        // 准备测试数据
        DataSource dataSource = new DataSource();
        dataSource.setName("新数据源");
        dataSource.setType("mysql");
        dataSource.setConfig("{\"host\":\"localhost\",\"port\":3306}");

        ConnectionTestResult testResult = ConnectionTestResult.success("连接成功", 100L);

        // 模拟服务层行为
        when(dataSourceService.testConnection(any(DataSource.class))).thenReturn(testResult);
        when(dataSourceService.insertDataSource(any(DataSource.class))).thenReturn(1);

        // 执行测试
        AjaxResult result = dataSourceController.add(dataSource);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.get("code"));
        
        // 验证服务方法被调用
        verify(dataSourceService, times(1)).testConnection(any(DataSource.class));
        verify(dataSourceService, times(1)).insertDataSource(any(DataSource.class));
    }

    @Test
    void testAdd_ConnectionTestFailed() {
        // 准备测试数据
        DataSource dataSource = new DataSource();
        dataSource.setName("新数据源");
        dataSource.setType("mysql");
        dataSource.setConfig("{\"host\":\"invalid\",\"port\":3306}");

        ConnectionTestResult testResult = ConnectionTestResult.failure("连接失败");

        // 模拟服务层行为
        when(dataSourceService.testConnection(any(DataSource.class))).thenReturn(testResult);

        // 执行测试
        AjaxResult result = dataSourceController.add(dataSource);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("连接测试失败"));
        
        // 验证服务方法被调用
        verify(dataSourceService, times(1)).testConnection(any(DataSource.class));
        verify(dataSourceService, never()).insertDataSource(any(DataSource.class));
    }

    @Test
    void testRemove_Success() {
        // 准备测试数据
        Long[] ids = {1L, 2L};

        // 模拟服务层行为
        when(dataSourceService.deleteDataSourceByIds(ids)).thenReturn(2);

        // 执行测试
        AjaxResult result = dataSourceController.remove(ids);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.get("code"));
        
        // 验证服务方法被调用
        verify(dataSourceService, times(1)).deleteDataSourceByIds(ids);
    }

    @Test
    void testRemove_WithException() {
        // 准备测试数据
        Long[] ids = {1L};

        // 模拟服务层抛出异常（数据源正在使用中）
        when(dataSourceService.deleteDataSourceByIds(ids))
                .thenThrow(new RuntimeException("数据源正在被数据集使用，无法删除"));

        // 执行测试
        AjaxResult result = dataSourceController.remove(ids);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.get("code"));
        assertTrue(result.get("msg").toString().contains("删除数据源失败"));
        
        // 验证服务方法被调用
        verify(dataSourceService, times(1)).deleteDataSourceByIds(ids);
    }
}
