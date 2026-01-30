# BI平台查询引擎

本目录包含BI平台的核心查询引擎组件。

## 组件说明

### DataSourceManager

数据源管理器，负责管理多数据源连接池和连接生命周期。

**主要功能：**

1. **连接池管理**
   - 使用HikariCP为每个数据源维护独立连接池
   - 支持连接池参数配置（最小/最大连接数、超时时间）
   - 自动管理连接池生命周期

2. **多数据源支持**
   - MySQL
   - PostgreSQL
   - ClickHouse
   - Doris
   - Oracle
   - REST API
   - 文件（CSV/Excel）

3. **安全性**
   - 数据源凭据使用AES-256加密存储
   - 仅在内存中解密凭据，不记录到日志
   - 密码字段在配置中屏蔽显示

4. **连接测试**
   - 支持数据库连接测试（10秒超时）
   - 支持API连接测试（15秒超时）
   - 返回详细的连接信息和错误消息

5. **API请求执行**
   - 支持GET和POST方法
   - 支持自定义请求头和请求体
   - 返回完整的响应数据和状态码

**使用示例：**

```java
@Autowired
private DataSourceManager dataSourceManager;

// 测试数据源连接
ConnectionTestResult result = dataSourceManager.testConnection(dataSource);
if (result.isSuccess()) {
    System.out.println("连接成功: " + result.getMessage());
}

// 初始化数据源连接池
dataSourceManager.initializeDataSource(dataSource);

// 获取连接
Connection connection = dataSourceManager.getConnection(dataSourceId);
try {
    // 执行查询...
} finally {
    // 释放连接
    dataSourceManager.releaseConnection(connection);
}

// 执行API请求
ApiResponse response = dataSourceManager.executeApiRequest(apiDataSource);
if (response.isSuccess()) {
    String body = response.getBody();
    // 处理响应数据...
}

// 关闭数据源连接池
dataSourceManager.closeDataSource(dataSourceId);
```

**配置格式：**

数据库数据源配置（JSON）：
```json
{
  "host": "localhost",
  "port": 3306,
  "database": "iras",
  "username": "root",
  "password": "encrypted_password_here",
  "connectionPool": {
    "minConnections": 2,
    "maxConnections": 10,
    "connectionTimeout": 30000
  }
}
```

API数据源配置（JSON）：
```json
{
  "url": "https://api.example.com/data",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer token",
    "Content-Type": "application/json"
  },
  "body": "{\"query\": \"value\"}"
}
```

**错误处理：**

- 连接失败：返回详细的错误信息，包括SQL状态码和错误代码
- 超时：连接超时10秒，查询超时30秒
- 凭据错误：解密失败时抛出ServiceException
- 配置错误：缺少必需配置项时抛出ServiceException

**性能优化：**

- 连接池复用：避免频繁创建和销毁连接
- 连接测试：使用轻量级测试查询（SELECT 1）
- 超时控制：防止长时间等待
- 资源清理：应用关闭时自动关闭所有连接池

**注意事项：**

1. 数据源密码必须使用AesUtils加密后存储
2. 连接池初始化失败时会抛出异常，需要捕获处理
3. 使用完连接后必须调用releaseConnection释放
4. 应用关闭时会自动清理所有连接池（@PreDestroy）
5. API请求默认超时15秒，可根据需要调整

## 依赖项

- HikariCP: 连接池管理
- MySQL Connector: MySQL驱动
- PostgreSQL Driver: PostgreSQL驱动
- ClickHouse JDBC: ClickHouse驱动
- Oracle JDBC: Oracle驱动
- Apache HttpClient 5: HTTP请求
- AesUtils: 密码加密解密

## 测试

单元测试位于：`iras-bi/src/test/java/com/zjrcu/iras/bi/platform/engine/DataSourceManagerTest.java`

运行测试：
```bash
mvn test -Dtest=DataSourceManagerTest
```

## 后续开发

- QueryExecutor: 查询执行引擎（任务3.1）
- CacheManager: 查询结果缓存（任务5.1）
- DataExtractScheduler: 定时数据抽取（任务3.4）
