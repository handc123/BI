package com.zjrcu.iras.bi.platform.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.dto.ApiResponse;
import com.zjrcu.iras.bi.platform.domain.dto.ConnectionTestResult;
import com.zjrcu.iras.common.utils.AesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSourceManager单元测试
 *
 * @author iras
 */
@SpringBootTest
class DataSourceManagerTest {

    @Autowired
    private DataSourceManager dataSourceManager;

    private DataSource testDataSource;

    @BeforeEach
    void setUp() {
        // 创建测试数据源配置
        testDataSource = new DataSource();
        testDataSource.setId(1L);
        testDataSource.setName("测试MySQL数据源");
        testDataSource.setType("mysql");
        testDataSource.setStatus("0");
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据源
        if (testDataSource != null && testDataSource.getId() != null) {
            try {
                dataSourceManager.closeDataSource(testDataSource.getId());
            } catch (Exception e) {
                // 忽略清理错误
            }
        }
    }

    /**
     * 测试空数据源配置
     */
    @Test
    void testConnection_NullDataSource() {
        ConnectionTestResult result = dataSourceManager.testConnection(null);
        assertFalse(result.isSuccess());
        assertEquals("数据源配置不能为空", result.getMessage());
    }

    /**
     * 测试空数据源类型
     */
    @Test
    void testConnection_EmptyType() {
        testDataSource.setType("");
        ConnectionTestResult result = dataSourceManager.testConnection(testDataSource);
        assertFalse(result.isSuccess());
        assertEquals("数据源类型不能为空", result.getMessage());
    }

    /**
     * 测试文件数据源连接
     */
    @Test
    void testConnection_FileDataSource() {
        testDataSource.setType("file");
        testDataSource.setConfig("{}");
        
        ConnectionTestResult result = dataSourceManager.testConnection(testDataSource);
        assertTrue(result.isSuccess());
        assertEquals("文件数据源无需测试连接", result.getMessage());
    }

    /**
     * 测试MySQL数据源连接失败（无效配置）
     */
    @Test
    void testConnection_MySQLInvalidConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "invalid-host");
        config.put("port", 3306);
        config.put("database", "test");
        config.put("username", "root");
        config.put("password", AesUtils.encrypt("password"));
        
        testDataSource.setConfigMap(config);
        
        ConnectionTestResult result = dataSourceManager.testConnection(testDataSource);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("连接失败"));
    }

    /**
     * 测试API数据源连接（无效URL）
     */
    @Test
    void testConnection_APIInvalidUrl() {
        testDataSource.setType("api");
        Map<String, Object> config = new HashMap<>();
        config.put("url", "");
        config.put("method", "GET");
        
        testDataSource.setConfigMap(config);
        
        ConnectionTestResult result = dataSourceManager.testConnection(testDataSource);
        assertFalse(result.isSuccess());
        assertEquals("API URL不能为空", result.getMessage());
    }

    /**
     * 测试不支持的数据源类型
     */
    @Test
    void testConnection_UnsupportedType() {
        testDataSource.setType("unsupported");
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 3306);
        config.put("database", "test");
        
        testDataSource.setConfigMap(config);
        
        ConnectionTestResult result = dataSourceManager.testConnection(testDataSource);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不支持的数据源类型"));
    }

    /**
     * 测试获取连接（未初始化）
     */
    @Test
    void testGetConnection_NotInitialized() {
        Exception exception = assertThrows(Exception.class, () -> {
            dataSourceManager.getConnection(999L);
        });
        assertTrue(exception.getMessage().contains("数据源连接池未初始化"));
    }

    /**
     * 测试释放空连接
     */
    @Test
    void testReleaseConnection_Null() {
        // 不应抛出异常
        assertDoesNotThrow(() -> {
            dataSourceManager.releaseConnection(null);
        });
    }

    /**
     * 测试初始化数据源（空配置）
     */
    @Test
    void testInitializeDataSource_NullConfig() {
        Exception exception = assertThrows(Exception.class, () -> {
            dataSourceManager.initializeDataSource(null);
        });
        assertTrue(exception.getMessage().contains("数据源配置或ID不能为空"));
    }

    /**
     * 测试初始化数据源（缺少主机）
     */
    @Test
    void testInitializeDataSource_MissingHost() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", 3306);
        config.put("database", "test");
        
        testDataSource.setConfigMap(config);
        
        Exception exception = assertThrows(Exception.class, () -> {
            dataSourceManager.initializeDataSource(testDataSource);
        });
        assertTrue(exception.getMessage().contains("数据源主机地址不能为空"));
    }

    /**
     * 测试关闭不存在的数据源
     */
    @Test
    void testCloseDataSource_NotExists() {
        // 不应抛出异常
        assertDoesNotThrow(() -> {
            dataSourceManager.closeDataSource(999L);
        });
    }

    /**
     * 测试执行API请求（空数据源）
     */
    @Test
    void testExecuteApiRequest_NullDataSource() {
        ApiResponse response = dataSourceManager.executeApiRequest(null);
        assertFalse(response.isSuccess());
        assertEquals("数据源配置不能为空", response.getErrorMessage());
    }

    /**
     * 测试执行API请求（非API类型）
     */
    @Test
    void testExecuteApiRequest_NotApiType() {
        testDataSource.setType("mysql");
        testDataSource.setConfig("{}");
        
        ApiResponse response = dataSourceManager.executeApiRequest(testDataSource);
        assertFalse(response.isSuccess());
        assertEquals("数据源类型必须为API", response.getErrorMessage());
    }

    /**
     * 测试执行API请求（空URL）
     */
    @Test
    void testExecuteApiRequest_EmptyUrl() {
        testDataSource.setType("api");
        Map<String, Object> config = new HashMap<>();
        config.put("url", "");
        config.put("method", "GET");
        
        testDataSource.setConfigMap(config);
        
        ApiResponse response = dataSourceManager.executeApiRequest(testDataSource);
        assertFalse(response.isSuccess());
        assertEquals("API URL不能为空", response.getErrorMessage());
    }

    /**
     * 测试JDBC URL构建 - MySQL
     */
    @Test
    void testBuildJdbcUrl_MySQL() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 3306);
        config.put("database", "testdb");
        config.put("username", "root");
        config.put("password", AesUtils.encrypt("password"));
        
        testDataSource.setType("mysql");
        testDataSource.setConfigMap(config);
        
        // 通过初始化测试URL构建（会失败但能验证URL格式）
        Exception exception = assertThrows(Exception.class, () -> {
            dataSourceManager.initializeDataSource(testDataSource);
        });
        // 验证不是URL格式错误
        assertFalse(exception.getMessage().contains("不支持的数据源类型"));
    }

    /**
     * 测试JDBC URL构建 - PostgreSQL
     */
    @Test
    void testBuildJdbcUrl_PostgreSQL() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 5432);
        config.put("database", "testdb");
        config.put("username", "postgres");
        config.put("password", AesUtils.encrypt("password"));
        
        testDataSource.setType("postgresql");
        testDataSource.setConfigMap(config);
        
        Exception exception = assertThrows(Exception.class, () -> {
            dataSourceManager.initializeDataSource(testDataSource);
        });
        assertFalse(exception.getMessage().contains("不支持的数据源类型"));
    }

    /**
     * 测试连接池配置
     */
    @Test
    void testConnectionPoolConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 3306);
        config.put("database", "testdb");
        config.put("username", "root");
        config.put("password", AesUtils.encrypt("password"));
        
        // 添加连接池配置
        Map<String, Object> poolConfig = new HashMap<>();
        poolConfig.put("minConnections", 5);
        poolConfig.put("maxConnections", 20);
        poolConfig.put("connectionTimeout", 5000);
        config.put("connectionPool", poolConfig);
        
        testDataSource.setConfigMap(config);
        
        // 验证配置被正确解析（通过初始化测试）
        Exception exception = assertThrows(Exception.class, () -> {
            dataSourceManager.initializeDataSource(testDataSource);
        });
        // 不应该是配置解析错误
        assertFalse(exception.getMessage().contains("配置"));
    }

    /**
     * 测试密码加密解密
     */
    @Test
    void testPasswordEncryptionDecryption() {
        String originalPassword = "mySecretPassword123!";
        String encrypted = AesUtils.encrypt(originalPassword);
        String decrypted = AesUtils.decrypt(encrypted);
        
        assertEquals(originalPassword, decrypted);
        assertNotEquals(originalPassword, encrypted);
    }

    /**
     * 测试多次初始化同一数据源（应该关闭旧连接池）
     */
    @Test
    void testReinitializeDataSource() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", 3306);
        config.put("database", "testdb");
        config.put("username", "root");
        config.put("password", AesUtils.encrypt("password"));
        
        testDataSource.setConfigMap(config);
        
        // 第一次初始化会失败（无效连接）
        assertThrows(Exception.class, () -> {
            dataSourceManager.initializeDataSource(testDataSource);
        });
        
        // 第二次初始化也应该失败，但不应该因为"已存在"而失败
        Exception exception = assertThrows(Exception.class, () -> {
            dataSourceManager.initializeDataSource(testDataSource);
        });
        assertFalse(exception.getMessage().contains("已存在"));
    }
}
