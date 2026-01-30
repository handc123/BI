package com.zjrcu.iras.bi.platform.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.dto.ApiResponse;
import com.zjrcu.iras.bi.platform.domain.dto.ConnectionTestResult;
import com.zjrcu.iras.bi.platform.mapper.DataSourceMapper;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.AesUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源管理器
 * 负责管理多数据源连接池和连接生命周期
 *
 * @author iras
 */
@Component
public class DataSourceManager {
    private static final Logger log = LoggerFactory.getLogger(DataSourceManager.class);

    @Autowired
    private DataSourceMapper dataSourceMapper;

    /**
     * 数据源连接池缓存
     * Key: 数据源ID, Value: HikariDataSource
     */
    private final Map<Long, HikariDataSource> dataSourcePool = new ConcurrentHashMap<>();

    /**
     * 连接超时时间（毫秒）
     */
    private static final int CONNECTION_TIMEOUT = 10000;

    /**
     * 测试查询超时时间（毫秒）
     */
    private static final int TEST_TIMEOUT = 10000;

    /**
     * API请求超时时间（毫秒）
     */
    private static final int API_TIMEOUT = 15000;

    /**
     * 测试数据源连接
     *
     * @param dataSource 数据源配置
     * @return 连接测试结果
     */
    public ConnectionTestResult testConnection(DataSource dataSource) {
        if (dataSource == null) {
            return ConnectionTestResult.failure("数据源配置不能为空");
        }

        String type = dataSource.getType();
        if (StringUtils.isEmpty(type)) {
            return ConnectionTestResult.failure("数据源类型不能为空");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 根据数据源类型进行测试
            if ("api".equalsIgnoreCase(type)) {
                return testApiConnection(dataSource, startTime);
            } else if ("file".equalsIgnoreCase(type)) {
                return ConnectionTestResult.success("文件数据源无需测试连接", 0);
            } else {
                return testDatabaseConnection(dataSource, startTime);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("数据源连接测试失败: type={}, error={}", type, e.getMessage(), e);
            return ConnectionTestResult.failure("连接失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据库连接
     *
     * @param dataSource 数据源配置
     * @param startTime  开始时间
     * @return 连接测试结果
     */
    private ConnectionTestResult testDatabaseConnection(DataSource dataSource, long startTime) {
        Connection connection = null;
        try {
            // 创建临时连接池配置
            HikariConfig config = buildHikariConfig(dataSource);
            config.setConnectionTimeout(TEST_TIMEOUT);

            // 创建临时数据源
            try (HikariDataSource tempDataSource = new HikariDataSource(config)) {
                connection = tempDataSource.getConnection();

                // 获取数据库元数据
                DatabaseMetaData metaData = connection.getMetaData();
                String version = metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();

                long duration = System.currentTimeMillis() - startTime;
                log.info("数据源连接测试成功: name={}, type={}, duration={}ms, version={}",
                        dataSource.getName(), dataSource.getType(), duration, version);

                return ConnectionTestResult.success("连接成功", duration, version);
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("数据库连接测试失败: name={}, type={}, error={}",
                    dataSource.getName(), dataSource.getType(), e.getMessage());
            return ConnectionTestResult.failure("数据库连接失败: " + getDetailedErrorMessage(e));
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("关闭测试连接失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 测试API连接
     *
     * @param dataSource 数据源配置
     * @param startTime  开始时间
     * @return 连接测试结果
     */
    private ConnectionTestResult testApiConnection(DataSource dataSource, long startTime) {
        try {
            Map<String, Object> configMap = dataSource.getConfigMap();
            String url = (String) configMap.get("url");
            String method = (String) configMap.getOrDefault("method", "GET");

            if (StringUtils.isEmpty(url)) {
                return ConnectionTestResult.failure("API URL不能为空");
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                CloseableHttpResponse response;

                if ("POST".equalsIgnoreCase(method)) {
                    HttpPost httpPost = new HttpPost(url);
                    // 添加请求头
                    addHeaders(httpPost, configMap);
                    response = httpClient.execute(httpPost);
                } else {
                    HttpGet httpGet = new HttpGet(url);
                    // 添加请求头
                    addHeaders(httpGet, configMap);
                    response = httpClient.execute(httpGet);
                }

                int statusCode = response.getCode();
                long duration = System.currentTimeMillis() - startTime;

                response.close();

                if (statusCode >= 200 && statusCode < 300) {
                    log.info("API连接测试成功: name={}, url={}, statusCode={}, duration={}ms",
                            dataSource.getName(), url, statusCode, duration);
                    return ConnectionTestResult.success("API连接成功，状态码: " + statusCode, duration);
                } else {
                    log.warn("API连接测试返回非成功状态码: name={}, url={}, statusCode={}",
                            dataSource.getName(), url, statusCode);
                    return ConnectionTestResult.failure("API返回状态码: " + statusCode);
                }
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("API连接测试失败: name={}, error={}", dataSource.getName(), e.getMessage());
            return ConnectionTestResult.failure("API连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据源连接
     *
     * @param dataSourceId 数据源ID
     * @return 数据库连接
     */
    public Connection getConnection(Long dataSourceId) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }

        // 如果连接池未初始化，自动初始化
        if (!isDataSourceInitialized(dataSourceId)) {
            log.info("数据源连接池未初始化，开始自动初始化: dataSourceId={}", dataSourceId);
            
            // 从数据库加载数据源配置并初始化
            try {
                // 注意：这里需要通过 Spring 容器获取 DataSourceMapper
                // 由于 DataSourceManager 是 @Component，可以注入 Mapper
                initializeDataSourceById(dataSourceId);
            } catch (Exception e) {
                log.error("自动初始化数据源连接池失败: dataSourceId={}, error={}", dataSourceId, e.getMessage());
                throw new ServiceException("数据源连接池初始化失败: " + e.getMessage());
            }
        }

        HikariDataSource hikariDataSource = dataSourcePool.get(dataSourceId);
        if (hikariDataSource == null) {
            throw new ServiceException("数据源连接池未初始化，请先初始化数据源: " + dataSourceId);
        }

        try {
            Connection connection = hikariDataSource.getConnection();
            log.debug("获取数据源连接成功: dataSourceId={}", dataSourceId);
            return connection;
        } catch (SQLException e) {
            log.error("获取数据源连接失败: dataSourceId={}, error={}", dataSourceId, e.getMessage());
            throw new ServiceException("获取数据源连接失败: " + getDetailedErrorMessage(e));
        }
    }

    /**
     * 释放连接
     *
     * @param connection 数据库连接
     */
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                log.debug("释放数据源连接成功");
            } catch (SQLException e) {
                log.warn("释放数据源连接失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 初始化数据源连接池
     *
     * @param dataSource 数据源配置
     */
    public void initializeDataSource(DataSource dataSource) {
        if (dataSource == null || dataSource.getId() == null) {
            throw new ServiceException("数据源配置或ID不能为空");
        }

        Long dataSourceId = dataSource.getId();

        // 如果已存在，先关闭旧的连接池
        if (dataSourcePool.containsKey(dataSourceId)) {
            closeDataSource(dataSourceId);
        }

        try {
            HikariConfig config = buildHikariConfig(dataSource);
            HikariDataSource hikariDataSource = new HikariDataSource(config);

            dataSourcePool.put(dataSourceId, hikariDataSource);
            log.info("数据源连接池初始化成功: id={}, name={}, type={}",
                    dataSourceId, dataSource.getName(), dataSource.getType());
        } catch (Exception e) {
            log.error("数据源连接池初始化失败: id={}, name={}, error={}",
                    dataSourceId, dataSource.getName(), e.getMessage());
            throw new ServiceException("数据源连接池初始化失败: " + e.getMessage());
        }
    }

    /**
     * 关闭数据源连接池
     *
     * @param dataSourceId 数据源ID
     */
    public void closeDataSource(Long dataSourceId) {
        HikariDataSource hikariDataSource = dataSourcePool.remove(dataSourceId);
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
            log.info("数据源连接池已关闭: dataSourceId={}", dataSourceId);
        }
    }

    /**
     * 执行API请求
     *
     * @param dataSource API数据源配置
     * @return API响应数据
     */
    public ApiResponse executeApiRequest(DataSource dataSource) {
        if (dataSource == null) {
            return ApiResponse.failure("数据源配置不能为空");
        }

        if (!"api".equalsIgnoreCase(dataSource.getType())) {
            return ApiResponse.failure("数据源类型必须为API");
        }

        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> configMap = dataSource.getConfigMap();
            String url = (String) configMap.get("url");
            String method = (String) configMap.getOrDefault("method", "GET");

            if (StringUtils.isEmpty(url)) {
                return ApiResponse.failure("API URL不能为空");
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                CloseableHttpResponse response;

                if ("POST".equalsIgnoreCase(method)) {
                    HttpPost httpPost = new HttpPost(url);
                    addHeaders(httpPost, configMap);
                    
                    // 添加请求体
                    String body = (String) configMap.get("body");
                    if (StringUtils.isNotEmpty(body)) {
                        httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
                    }
                    
                    response = httpClient.execute(httpPost);
                } else {
                    HttpGet httpGet = new HttpGet(url);
                    addHeaders(httpGet, configMap);
                    response = httpClient.execute(httpGet);
                }

                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                long duration = System.currentTimeMillis() - startTime;

                response.close();

                if (statusCode >= 200 && statusCode < 300) {
                    log.info("API请求执行成功: name={}, url={}, statusCode={}, duration={}ms",
                            dataSource.getName(), url, statusCode, duration);
                    return ApiResponse.success(statusCode, responseBody, duration);
                } else {
                    log.warn("API请求返回非成功状态码: name={}, url={}, statusCode={}, body={}",
                            dataSource.getName(), url, statusCode, responseBody);
                    return ApiResponse.failure("API返回状态码: " + statusCode + ", 响应: " + responseBody);
                }
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("API请求执行失败: name={}, error={}", dataSource.getName(), e.getMessage());
            return ApiResponse.failure("API请求失败: " + e.getMessage());
        }
    }

    /**
     * 构建HikariCP配置
     *
     * @param dataSource 数据源配置
     * @return HikariConfig
     */
    private HikariConfig buildHikariConfig(DataSource dataSource) {
        Map<String, Object> configMap = dataSource.getConfigMap();

        HikariConfig config = new HikariConfig();

        // 构建JDBC URL
        String jdbcUrl = buildJdbcUrl(dataSource.getType(), configMap);
        config.setJdbcUrl(jdbcUrl);

        // 设置用户名和密码（解密）
        String username = (String) configMap.get("username");
        String encryptedPassword = (String) configMap.get("password");

        if (StringUtils.isNotEmpty(username)) {
            config.setUsername(username);
        }

        if (StringUtils.isNotEmpty(encryptedPassword)) {
            try {
                // 检查密码是否已加密，如果是明文则直接使用
                String password;
                if (isEncryptedPassword(encryptedPassword)) {
                    password = AesUtils.decrypt(encryptedPassword);
                    log.debug("数据源密码已解密: dataSourceId={}", dataSource.getId());
                } else {
                    password = encryptedPassword;
                    log.debug("数据源密码为明文，直接使用: dataSourceId={}", dataSource.getId());
                }
                config.setPassword(password);
            } catch (Exception e) {
                log.error("解密数据源密码失败: dataSourceId={}, error={}", dataSource.getId(), e.getMessage());
                throw new ServiceException("解密数据源密码失败");
            }
        }

        // 设置驱动类
        String driverClassName = getDriverClassName(dataSource.getType());
        if (StringUtils.isNotEmpty(driverClassName)) {
            config.setDriverClassName(driverClassName);
        }

        // 连接池配置
        Map<String, Object> poolConfig = (Map<String, Object>) configMap.get("connectionPool");
        if (poolConfig != null) {
            Integer minConnections = (Integer) poolConfig.get("minConnections");
            Integer maxConnections = (Integer) poolConfig.get("maxConnections");
            Integer connectionTimeout = (Integer) poolConfig.get("connectionTimeout");

            if (minConnections != null) {
                config.setMinimumIdle(minConnections);
            } else {
                config.setMinimumIdle(2);
            }

            if (maxConnections != null) {
                config.setMaximumPoolSize(maxConnections);
            } else {
                config.setMaximumPoolSize(10);
            }

            if (connectionTimeout != null) {
                config.setConnectionTimeout(connectionTimeout);
            } else {
                config.setConnectionTimeout(CONNECTION_TIMEOUT);
            }
        } else {
            // 默认连接池配置
            config.setMinimumIdle(2);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(CONNECTION_TIMEOUT);
        }

        // 连接池名称
        config.setPoolName("BI-DataSource-" + dataSource.getId());

        // 连接测试查询
        config.setConnectionTestQuery(getTestQuery(dataSource.getType()));

        // 其他配置
        config.setIdleTimeout(600000); // 10分钟
        config.setMaxLifetime(1800000); // 30分钟

        return config;
    }

    /**
     * 构建JDBC URL
     *
     * @param type      数据源类型
     * @param configMap 配置Map
     * @return JDBC URL
     */
    private String buildJdbcUrl(String type, Map<String, Object> configMap) {
        String host = (String) configMap.get("host");
        Object portObj = configMap.get("port");
        String database = (String) configMap.get("database");

        if (StringUtils.isEmpty(host)) {
            throw new ServiceException("数据源主机地址不能为空");
        }

        int port = portObj instanceof Integer ? (Integer) portObj : Integer.parseInt(portObj.toString());

        switch (type.toLowerCase()) {
            case "mysql":
                // 添加 allowPublicKeyRetrieval=true 以支持 MySQL 8.0+ 的 caching_sha2_password 认证
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                        host, port, database);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "clickhouse":
                return String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
            case "doris":
                return String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            default:
                throw new ServiceException("不支持的数据源类型: " + type);
        }
    }

    /**
     * 获取驱动类名
     *
     * @param type 数据源类型
     * @return 驱动类名
     */
    private String getDriverClassName(String type) {
        switch (type.toLowerCase()) {
            case "mysql":
            case "doris":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "clickhouse":
                return "com.clickhouse.jdbc.ClickHouseDriver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            default:
                return null;
        }
    }

    /**
     * 获取测试查询语句
     *
     * @param type 数据源类型
     * @return 测试查询语句
     */
    private String getTestQuery(String type) {
        switch (type.toLowerCase()) {
            case "mysql":
            case "doris":
                return "SELECT 1";
            case "postgresql":
            case "clickhouse":
                return "SELECT 1";
            case "oracle":
                return "SELECT 1 FROM DUAL";
            default:
                return "SELECT 1";
        }
    }

    /**
     * 添加HTTP请求头
     *
     * @param request   HTTP请求
     * @param configMap 配置Map
     */
    private void addHeaders(org.apache.hc.core5.http.HttpRequest request, Map<String, Object> configMap) {
        Map<String, String> headers = (Map<String, String>) configMap.get("headers");
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(request::addHeader);
        }

        // 默认添加Content-Type
        if (!request.containsHeader("Content-Type")) {
            request.addHeader("Content-Type", "application/json");
        }
    }

    /**
     * 获取详细的错误消息
     *
     * @param e SQL异常
     * @return 详细错误消息
     */
    private String getDetailedErrorMessage(SQLException e) {
        StringBuilder message = new StringBuilder();
        message.append(e.getMessage());

        // 添加SQL状态码
        if (StringUtils.isNotEmpty(e.getSQLState())) {
            message.append(" (SQLState: ").append(e.getSQLState()).append(")");
        }

        // 添加错误代码
        if (e.getErrorCode() != 0) {
            message.append(" (ErrorCode: ").append(e.getErrorCode()).append(")");
        }

        return message.toString();
    }

    /**
     * 判断密码是否已加密
     * 加密后的密码是Base64编码，且长度应该是16的倍数（AES加密后）
     *
     * @param password 密码
     * @return 是否已加密
     */
    private boolean isEncryptedPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return false;
        }
        
        // 检查是否为Base64格式（只包含Base64字符）
        if (!password.matches("^[A-Za-z0-9+/=]+$")) {
            return false;
        }
        
        // Base64解码后的长度应该是16的倍数（AES块大小）
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(password);
            return decoded.length % 16 == 0 && decoded.length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断数据源是否已初始化
     *
     * @param dataSourceId 数据源ID
     * @return 是否已初始化
     */
    public boolean isDataSourceInitialized(Long dataSourceId) {
        return dataSourcePool.containsKey(dataSourceId);
    }

    /**
     * 根据数据源ID初始化连接池
     * 从数据库加载数据源配置并初始化连接池
     *
     * @param dataSourceId 数据源ID
     */
    private void initializeDataSourceById(Long dataSourceId) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }

        // 从数据库加载数据源配置
        DataSource dataSource = dataSourceMapper.selectDataSourceById(dataSourceId);
        if (dataSource == null) {
            throw new ServiceException("数据源不存在: " + dataSourceId);
        }

        if ("1".equals(dataSource.getStatus())) {
            throw new ServiceException("数据源已停用，无法初始化连接池");
        }

        // 只有数据库类型的数据源需要初始化连接池
        if (!isDatabaseType(dataSource.getType())) {
            log.warn("非数据库类型的数据源无需初始化连接池: dataSourceId={}, type={}", 
                    dataSourceId, dataSource.getType());
            return;
        }

        // 初始化连接池
        initializeDataSource(dataSource);
    }

    /**
     * 判断是否为数据库类型
     *
     * @param type 数据源类型
     * @return 是否为数据库类型
     */
    private boolean isDatabaseType(String type) {
        if (StringUtils.isEmpty(type)) {
            return false;
        }
        
        String lowerType = type.toLowerCase();
        return "mysql".equals(lowerType) || 
               "postgresql".equals(lowerType) || 
               "clickhouse".equals(lowerType) || 
               "doris".equals(lowerType) || 
               "oracle".equals(lowerType);
    }

    /**
     * 获取数据源的表列表
     *
     * @param dataSourceId 数据源ID
     * @return 表名列表
     */
    public List<String> getTableList(Long dataSourceId) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }

        Connection connection = null;
        try {
            connection = getConnection(dataSourceId);
            DatabaseMetaData metaData = connection.getMetaData();
            
            List<String> tables = new java.util.ArrayList<>();
            
            // 获取当前连接的数据库名称
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            
            log.debug("获取表列表: dataSourceId={}, catalog={}, schema={}", dataSourceId, catalog, schema);
            
            // 获取指定数据库的表
            // catalog: 数据库名称（MySQL使用）
            // schema: 模式名称（PostgreSQL、Oracle使用）
            // %: 表名模式（匹配所有表）
            // TABLE: 只获取表类型（不包括视图、系统表等）
            try (java.sql.ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    // 过滤掉系统表
                    if (!isSystemTable(tableName)) {
                        tables.add(tableName);
                    }
                }
            }
            
            log.info("获取表列表成功: dataSourceId={}, catalog={}, schema={}, tableCount={}", 
                    dataSourceId, catalog, schema, tables.size());
            return tables;
        } catch (SQLException e) {
            log.error("获取表列表失败: dataSourceId={}, error={}", dataSourceId, e.getMessage());
            throw new ServiceException("获取表列表失败: " + getDetailedErrorMessage(e));
        } finally {
            releaseConnection(connection);
        }
    }

    /**
     * 判断是否为系统表
     *
     * @param tableName 表名
     * @return 是否为系统表
     */
    private boolean isSystemTable(String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            return true;
        }
        
        String lowerTableName = tableName.toLowerCase();
        
        // MySQL系统表
        if (lowerTableName.startsWith("mysql.") || 
            lowerTableName.startsWith("information_schema.") ||
            lowerTableName.startsWith("performance_schema.") ||
            lowerTableName.startsWith("sys.")) {
            return true;
        }
        
        // PostgreSQL系统表
        if (lowerTableName.startsWith("pg_")) {
            return true;
        }
        
        // Oracle系统表
        if (lowerTableName.startsWith("sys_") || 
            lowerTableName.startsWith("dba_") ||
            lowerTableName.startsWith("all_") ||
            lowerTableName.startsWith("user_")) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取表结构信息
     *
     * @param dataSourceId 数据源ID
     * @param tableName    表名
     * @return 表结构信息
     */
    public com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo getTableSchema(Long dataSourceId, String tableName) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }
        if (StringUtils.isEmpty(tableName)) {
            throw new ServiceException("表名不能为空");
        }

        Connection connection = null;
        try {
            connection = getConnection(dataSourceId);
            DatabaseMetaData metaData = connection.getMetaData();
            
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            
            log.debug("获取表结构: dataSourceId={}, tableName={}, catalog={}, schema={}", 
                    dataSourceId, tableName, catalog, schema);
            
            // 获取表注释
            String tableComment = getTableComment(metaData, catalog, schema, tableName);
            
            // 获取主键信息
            java.util.Set<String> primaryKeys = getPrimaryKeys(metaData, catalog, schema, tableName);
            
            // 获取字段列表
            List<com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo.ColumnInfo> columns = new java.util.ArrayList<>();
            
            try (java.sql.ResultSet rs = metaData.getColumns(catalog, schema, tableName, "%")) {
                while (rs.next()) {
                    com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo.ColumnInfo column = 
                            new com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo.ColumnInfo();
                    
                    String columnName = rs.getString("COLUMN_NAME");
                    column.setColumnName(columnName);
                    column.setColumnComment(rs.getString("REMARKS"));
                    column.setDataType(rs.getString("TYPE_NAME"));
                    column.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                    column.setDefaultValue(rs.getString("COLUMN_DEF"));
                    column.setIsPrimaryKey(primaryKeys.contains(columnName));
                    column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                    column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                    
                    // 判断字段类型（维度或指标）
                    column.setFieldType(determineFieldType(column));
                    
                    columns.add(column);
                }
            }
            
            log.info("获取表结构成功: dataSourceId={}, tableName={}, columnCount={}", 
                    dataSourceId, tableName, columns.size());
            
            return new com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo(tableName, tableComment, columns);
        } catch (SQLException e) {
            log.error("获取表结构失败: dataSourceId={}, tableName={}, error={}", 
                    dataSourceId, tableName, e.getMessage());
            throw new ServiceException("获取表结构失败: " + getDetailedErrorMessage(e));
        } finally {
            releaseConnection(connection);
        }
    }

    /**
     * 获取表数据预览
     *
     * @param dataSourceId 数据源ID
     * @param tableName    表名
     * @param limit        限制行数
     * @return 表数据预览
     */
    public com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData getTablePreview(Long dataSourceId, String tableName, int limit) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }
        if (StringUtils.isEmpty(tableName)) {
            throw new ServiceException("表名不能为空");
        }

        Connection connection = null;
        java.sql.Statement statement = null;
        java.sql.ResultSet resultSet = null;
        
        try {
            connection = getConnection(dataSourceId);
            
            // 先获取表结构以获取字段注释
            com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo schema = getTableSchema(dataSourceId, tableName);
            Map<String, com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo.ColumnInfo> columnMap = 
                    schema.getColumns().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo.ColumnInfo::getColumnName,
                            col -> col
                    ));
            
            // 构建查询SQL
            String sql = String.format("SELECT * FROM %s LIMIT %d", tableName, limit);
            
            log.debug("执行预览查询: dataSourceId={}, sql={}", dataSourceId, sql);
            
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            
            // 获取结果集元数据
            java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // 构建字段元数据
            List<com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData.ColumnMeta> columns = new java.util.ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo.ColumnInfo columnInfo = columnMap.get(columnName);
                
                com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData.ColumnMeta columnMeta = 
                        new com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData.ColumnMeta();
                columnMeta.setName(columnName);
                columnMeta.setComment(columnInfo != null && StringUtils.isNotEmpty(columnInfo.getColumnComment()) 
                        ? columnInfo.getColumnComment() : columnName);
                columnMeta.setType(metaData.getColumnTypeName(i));
                columnMeta.setFieldType(columnInfo != null ? columnInfo.getFieldType() : "DIMENSION");
                
                columns.add(columnMeta);
            }
            
            // 读取数据行
            List<Map<String, Object>> rows = new java.util.ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                rows.add(row);
            }
            
            // 获取总行数
            Long total = getTableRowCount(connection, tableName);
            
            log.info("获取表数据预览成功: dataSourceId={}, tableName={}, previewRows={}, totalRows={}", 
                    dataSourceId, tableName, rows.size(), total);
            
            return new com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData(columns, rows, total);
        } catch (SQLException e) {
            log.error("获取表数据预览失败: dataSourceId={}, tableName={}, error={}", 
                    dataSourceId, tableName, e.getMessage());
            throw new ServiceException("获取表数据预览失败: " + getDetailedErrorMessage(e));
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            releaseConnection(connection);
        }
    }

    /**
     * 获取表注释
     *
     * @param metaData  数据库元数据
     * @param catalog   数据库名
     * @param schema    模式名
     * @param tableName 表名
     * @return 表注释
     */
    private String getTableComment(DatabaseMetaData metaData, String catalog, String schema, String tableName) {
        try (java.sql.ResultSet rs = metaData.getTables(catalog, schema, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                String remarks = rs.getString("REMARKS");
                return StringUtils.isNotEmpty(remarks) ? remarks : tableName;
            }
        } catch (SQLException e) {
            log.warn("获取表注释失败: tableName={}, error={}", tableName, e.getMessage());
        }
        return tableName;
    }

    /**
     * 获取主键列表
     *
     * @param metaData  数据库元数据
     * @param catalog   数据库名
     * @param schema    模式名
     * @param tableName 表名
     * @return 主键列表
     */
    private java.util.Set<String> getPrimaryKeys(DatabaseMetaData metaData, String catalog, String schema, String tableName) {
        java.util.Set<String> primaryKeys = new java.util.HashSet<>();
        try (java.sql.ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            log.warn("获取主键信息失败: tableName={}, error={}", tableName, e.getMessage());
        }
        return primaryKeys;
    }

    /**
     * 判断字段类型（维度或指标）
     *
     * @param column 字段信息
     * @return 字段类型：DIMENSION 或 MEASURE
     */
    private String determineFieldType(com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo.ColumnInfo column) {
        String dataType = column.getDataType().toUpperCase();
        String columnName = column.getColumnName().toLowerCase();
        
        // 主键默认为维度
        if (column.getIsPrimaryKey()) {
            return "DIMENSION";
        }
        
        // 根据字段名判断
        if (columnName.endsWith("_count") || columnName.endsWith("_amount") || 
            columnName.endsWith("_total") || columnName.endsWith("_sum") ||
            columnName.endsWith("_avg") || columnName.endsWith("_rate") ||
            columnName.endsWith("_num") || columnName.endsWith("_number")) {
            return "MEASURE";
        }
        
        // 根据数据类型判断
        if (dataType.contains("INT") || dataType.contains("DECIMAL") || 
            dataType.contains("NUMERIC") || dataType.contains("DOUBLE") || 
            dataType.contains("FLOAT") || dataType.contains("NUMBER")) {
            // 数值类型，但以_id、_code结尾的是维度
            if (columnName.endsWith("_id") || columnName.endsWith("_code")) {
                return "DIMENSION";
            }
            return "MEASURE";
        }
        
        // 其他类型默认为维度
        return "DIMENSION";
    }

    /**
     * 获取表总行数
     *
     * @param connection 数据库连接
     * @param tableName  表名
     * @return 总行数
     */
    private Long getTableRowCount(Connection connection, String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
        try (java.sql.Statement stmt = connection.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            log.warn("获取表行数失败: tableName={}, error={}", tableName, e.getMessage());
            return 0L;
        }
    }

    /**
     * 关闭ResultSet
     *
     * @param resultSet ResultSet
     */
    private void closeResultSet(java.sql.ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("关闭ResultSet失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭Statement
     *
     * @param statement Statement
     */
    private void closeStatement(java.sql.Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.warn("关闭Statement失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭所有数据源连接池
     */
    @PreDestroy
    public void closeAllDataSources() {
        log.info("开始关闭所有数据源连接池，共{}个", dataSourcePool.size());
        dataSourcePool.forEach((id, dataSource) -> {
            try {
                if (!dataSource.isClosed()) {
                    dataSource.close();
                    log.info("数据源连接池已关闭: dataSourceId={}", id);
                }
            } catch (Exception e) {
                log.error("关闭数据源连接池失败: dataSourceId={}, error={}", id, e.getMessage());
            }
        });
        dataSourcePool.clear();
        log.info("所有数据源连接池已关闭");
    }
}
