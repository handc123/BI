# 设计文档：BI平台升级

## 概述

本设计文档描述了将IRAS系统升级为自助式BI报表平台的技术架构和实现方案。该升级将在现有若依框架内集成类似DataEase的BI能力,使业务用户能够独立创建和配置监管分析报表,无需开发人员干预。

### 设计目标

1. **自助式配置**: 业务用户可通过UI配置数据源、数据集、可视化和仪表板
2. **多数据源支持**: 统一接入MySQL、PostgreSQL、ClickHouse、Doris、Oracle和REST API
3. **性能优化**: 通过直连和抽取两种模式支持不同数据量和实时性需求
4. **权限集成**: 完全复用若依框架的认证、组织和权限体系
5. **向后兼容**: 与现有IRAS模块(iras-bi)无缝共存

### 技术栈

**后端新增**:
- HikariCP: 多数据源连接池管理
- Apache Calcite: SQL解析和优化(可选,用于复杂查询)
- Apache PDFBox: PDF报表生成和导出
- Quartz Scheduler: 定时抽取任务调度

**前端新增**:
- VueDraggable: 仪表板拖拽布局(已有依赖)
- ECharts 5.4.0: 可视化渲染(已有依赖)
- Element UI: 表单和组件库(已有依赖)

**数据存储**:
- MySQL: BI元数据存储和抽取数据存储(复用现有MySQL数据库)
- Redis: 查询结果缓存(复用现有Redis)

## 架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue.js)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │数据源管理│  │数据集管理│  │可视化设计│  │仪表板设计│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/JSON
┌─────────────────────────────────────────────────────────────┐
│                    后端层 (Spring Boot)                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              BI平台控制器层                            │  │
│  │  DataSourceController │ DatasetController │           │  │
│  │  VisualizationController │ DashboardController        │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              BI平台服务层                              │  │
│  │  DataSourceService │ DatasetService │                 │  │
│  │  QueryExecutor │ CacheManager │ SchedulerService     │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           数据访问层 (MyBatis)                         │  │
│  │  BI元数据存储 (MySQL) │ BI数据抽取 (MySQL) │ Redis     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                      外部数据源层                             │
│  MySQL │ PostgreSQL │ ClickHouse │ Doris │ Oracle │ API    │
└─────────────────────────────────────────────────────────────┘
```

### 模块划分

BI平台将作为新的子包集成到`iras-bi`模块中:

```
iras-bi/
├── src/main/java/com/zjrcu/iras/bi/
│   ├── platform/                          # 新增BI平台包
│   │   ├── controller/                    # REST控制器
│   │   │   ├── DataSourceController.java
│   │   │   ├── DatasetController.java
│   │   │   ├── VisualizationController.java
│   │   │   └── DashboardController.java
│   │   ├── service/                       # 业务服务
│   │   │   ├── IDataSourceService.java
│   │   │   ├── IDatasetService.java
│   │   │   ├── IQueryExecutor.java
│   │   │   ├── IExportService.java
│   │   │   └── impl/
│   │   ├── domain/                        # 领域实体
│   │   │   ├── DataSource.java
│   │   │   ├── Dataset.java
│   │   │   ├── Visualization.java
│   │   │   └── Dashboard.java
│   │   ├── mapper/                        # MyBatis接口
│   │   ├── engine/                        # 查询引擎
│   │   │   ├── QueryExecutor.java
│   │   │   ├── DataSourceManager.java
│   │   │   └── CacheManager.java
│   │   └── scheduler/                     # 定时任务
│   │       └── DataExtractScheduler.java
│   ├── conversionquality/                 # 现有模块
│   ├── fieldquality/                      # 现有模块
│   └── ...
```

### 数据流

**查询执行流程**:
1. 用户在前端选择仪表板并应用筛选器
2. 前端发送查询请求到DashboardController
3. DashboardController调用QueryExecutor
4. QueryExecutor检查缓存:
   - 命中: 返回缓存结果
   - 未命中: 继续执行
5. 根据数据集类型:
    - 直连: 通过DataSourceManager连接外部数据源执行SQL
    - 抽取: 查询MySQL数据抽取存储表
6. 应用权限过滤(基于若依用户组织)
7. 返回结果并更新缓存
8. 前端使用ECharts渲染可视化

## 组件和接口

### 核心组件

#### 1. DataSourceManager

**职责**: 管理多数据源连接池和连接生命周期

**接口**:
```java
public interface DataSourceManager {
    /**
     * 测试数据源连接
     * @param dataSource 数据源配置
     * @return 连接测试结果
     */
    ConnectionTestResult testConnection(DataSource dataSource);
    
    /**
     * 获取数据源连接
     * @param dataSourceId 数据源ID
     * @return 数据库连接
     */
    Connection getConnection(Long dataSourceId);
    
    /**
     * 释放连接
     * @param connection 数据库连接
     */
    void releaseConnection(Connection connection);
    
    /**
     * 执行API请求
     * @param apiDataSource API数据源配置
     * @return API响应数据
     */
    ApiResponse executeApiRequest(ApiDataSource apiDataSource);
}
```

**实现要点**:
- 使用HikariCP为每个数据源维护独立连接池
- 支持连接池参数配置(最小/最大连接数、超时)
- 数据源凭据使用AES-256加密存储
- 连接失败时提供详细错误信息

#### 2. QueryExecutor

**职责**: 执行数据集查询并应用筛选、计算字段和权限

**接口**:
```java
public interface IQueryExecutor {
    /**
     * 执行数据集查询
     * @param datasetId 数据集ID
     * @param filters 筛选条件
     * @param user 当前用户(用于权限过滤)
     * @return 查询结果
     */
    QueryResult executeQuery(Long datasetId, List<Filter> filters, SysUser user);
    
    /**
     * 执行聚合查询
     * @param datasetId 数据集ID
     * @param dimensions 维度字段
     * @param metrics 度量字段
     * @param filters 筛选条件
     * @param user 当前用户
     * @return 聚合结果
     */
    QueryResult executeAggregation(Long datasetId, List<String> dimensions, 
                                   List<Metric> metrics, List<Filter> filters, SysUser user);
}
```

**实现要点**:
- 直连数据集: 动态生成SQL并执行
- 抽取数据集: 查询MySQL数据抽取存储表
- 应用计算字段(在SELECT子句中计算)
- 应用行级权限(基于用户部门ID添加WHERE条件)
- 查询超时控制(30秒)

#### 3. CacheManager

**职责**: 管理查询结果缓存

**接口**:
```java
public interface CacheManager {
    /**
     * 获取缓存结果
     * @param cacheKey 缓存键
     * @return 缓存的查询结果,如果不存在或已过期则返回null
     */
    QueryResult get(String cacheKey);
    
    /**
     * 存储查询结果到缓存
     * @param cacheKey 缓存键
     * @param result 查询结果
     * @param ttlSeconds 生存时间(秒)
     */
    void put(String cacheKey, QueryResult result, int ttlSeconds);
    
    /**
     * 使缓存失效
     * @param datasetId 数据集ID
     */
    void invalidate(Long datasetId);
}
```

**实现要点**:
- 使用若依框架现有的Redis缓存(RedisCache)
- 缓存键格式: `bi:dataset:{datasetId}:filters:{filterHash}`
- 支持配置TTL(1分钟到24小时)
- LRU驱逐策略(Redis配置)
- 数据集更新时自动失效相关缓存
- 使用Redis的EXPIRE命令设置TTL

#### 4. DataExtractScheduler

**职责**: 管理抽取数据集的定时任务

**接口**:
```java
public interface DataExtractScheduler {
    /**
     * 调度抽取任务
     * @param datasetId 数据集ID
     * @param cronExpression Cron表达式
     */
    void scheduleExtract(Long datasetId, String cronExpression);
    
    /**
     * 执行立即抽取
     * @param datasetId 数据集ID
     * @return 抽取结果
     */
    ExtractResult executeExtract(Long datasetId);
    
    /**
     * 取消调度任务
     * @param datasetId 数据集ID
     */
    void cancelSchedule(Long datasetId);
}
```

**实现要点**:
- 使用Quartz Scheduler管理定时任务
- 支持Cron表达式配置(每小时、每天、每周)
- 抽取进度跟踪(百分比)
- 失败重试机制(最多3次)
- 抽取完成后更新元数据(最后抽取时间、行数)

#### 5. ExportService

**职责**: 处理仪表板和组件的导出功能,支持CSV、Excel和PDF格式

**接口**:
```java
public interface IExportService {
    /**
     * 导出表格组件为CSV
     * @param visualizationId 可视化ID
     * @param filters 筛选条件
     * @param response HTTP响应
     */
    void exportToCSV(Long visualizationId, List<Filter> filters, HttpServletResponse response);

    /**
     * 导出表格组件为Excel
     * @param visualizationId 可视化ID
     * @param filters 筛选条件
     * @param response HTTP响应
     */
    void exportToExcel(Long visualizationId, List<Filter> filters, HttpServletResponse response);

    /**
     * 导出仪表板为PDF
     * @param dashboardId 仪表板ID
     * @param filters 全局筛选器
     * @param response HTTP响应
     */
    void exportToPDF(Long dashboardId, List<GlobalFilter> filters, HttpServletResponse response);

    /**
     * 异步导出仪表板为PDF(用于大数据量场景)
     * @param dashboardId 仪表板ID
     * @param filters 全局筛选器
     * @return 任务ID
     */
    String exportToPDFAsync(Long dashboardId, List<GlobalFilter> filters);
}
```

**实现要点**:
- **CSV/Excel导出**: 使用现有Apache POI库生成Excel,直接输出CSV文本
- **PDF导出流程**:
  1. 获取仪表板所有可视化配置
  2. 为每个可视化生成ECharts图片(使用ECharts服务器端渲染)
  3. 使用Apache PDFBox创建PDF文档
  4. 添加标题、时间戳、筛选器说明
  5. 将ECharts图片按布局插入PDF页面
  6. 输出PDF文件
- **异步导出**: 超过100行或100页时触发异步任务,完成后通过消息通知用户
- **缓存利用**: 优先使用缓存数据生成图片,减少数据源查询

### REST API接口

#### 数据源管理API

```
POST   /api/bi/datasource/test          # 测试数据源连接
POST   /api/bi/datasource                # 创建数据源
PUT    /api/bi/datasource/{id}          # 更新数据源
DELETE /api/bi/datasource/{id}          # 删除数据源
GET    /api/bi/datasource/{id}          # 获取数据源详情
GET    /api/bi/datasource/list          # 获取数据源列表
```

#### 数据集管理API

```
POST   /api/bi/dataset                   # 创建数据集
PUT    /api/bi/dataset/{id}              # 更新数据集
DELETE /api/bi/dataset/{id}              # 删除数据集
GET    /api/bi/dataset/{id}              # 获取数据集详情
GET    /api/bi/dataset/list              # 获取数据集列表
POST   /api/bi/dataset/{id}/preview      # 预览数据集数据
POST   /api/bi/dataset/{id}/extract      # 立即执行抽取
```

#### 可视化API

```
POST   /api/bi/visualization             # 创建可视化
PUT    /api/bi/visualization/{id}        # 更新可视化
DELETE /api/bi/visualization/{id}        # 删除可视化
GET    /api/bi/visualization/{id}        # 获取可视化配置
POST   /api/bi/visualization/{id}/data   # 获取可视化数据
```

#### 仪表板API

```
POST   /api/bi/dashboard                 # 创建仪表板
PUT    /api/bi/dashboard/{id}            # 更新仪表板
DELETE /api/bi/dashboard/{id}            # 删除仪表板
GET    /api/bi/dashboard/{id}            # 获取仪表板配置
POST   /api/bi/dashboard/{id}/data       # 获取仪表板所有组件数据
POST   /api/bi/dashboard/{id}/export     # 导出仪表板为PDF
POST   /api/bi/dashboard/{id}/share      # 生成共享链接
```

### 权限集成

**与若依框架集成点**:

1. **认证**: 使用现有的JWT token验证
2. **授权**: 通过`@PreAuthorize`注解检查权限
3. **行级安全**: 基于`SysUser.getDeptId()`过滤数据
4. **菜单集成**: 在`sys_menu`表中添加BI平台菜单项

**权限定义**:
```
bi:datasource:add      # 创建数据源
bi:datasource:edit     # 编辑数据源
bi:datasource:remove   # 删除数据源
bi:dataset:add         # 创建数据集
bi:dataset:edit        # 编辑数据集
bi:dataset:remove      # 删除数据集
bi:dashboard:add       # 创建仪表板
bi:dashboard:edit      # 编辑仪表板
bi:dashboard:remove    # 删除仪表板
bi:dashboard:view      # 查看仪表板
bi:dashboard:export    # 导出仪表板
```

## 数据模型

### 元数据表设计

#### bi_datasource (数据源表)

```sql
CREATE TABLE bi_datasource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据源ID',
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    type VARCHAR(20) NOT NULL COMMENT '数据源类型: mysql, postgresql, clickhouse, doris, oracle, api, file',
    config TEXT NOT NULL COMMENT '连接配置(JSON格式,加密存储)',
    status CHAR(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_type (type),
    INDEX idx_status (status)
) COMMENT='BI数据源表';
```

**config字段JSON结构**:
```json
{
  "host": "localhost",
  "port": 3306,
  "database": "iras",
  "username": "root",
  "password": "encrypted_password",
  "connectionPool": {
    "minConnections": 2,
    "maxConnections": 10,
    "connectionTimeout": 30000
  }
}
```

#### bi_dataset (数据集表)

```sql
CREATE TABLE bi_dataset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据集ID',
    name VARCHAR(100) NOT NULL COMMENT '数据集名称',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    type VARCHAR(20) NOT NULL COMMENT '数据集类型: direct, extract',
    query_config TEXT NOT NULL COMMENT '查询配置(JSON格式)',
    field_config TEXT COMMENT '字段配置(JSON格式)',
    extract_config TEXT COMMENT '抽取配置(JSON格式,仅抽取类型)',
    last_extract_time DATETIME COMMENT '最后抽取时间',
    row_count BIGINT COMMENT '数据行数',
    status CHAR(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    FOREIGN KEY (datasource_id) REFERENCES bi_datasource(id),
    INDEX idx_datasource (datasource_id),
    INDEX idx_type (type)
) COMMENT='BI数据集表';
```

**query_config字段JSON结构**:
```json
{
  "sourceType": "table",
  "tableName": "sys_user",
  "sql": "SELECT * FROM sys_user WHERE dept_id = ?",
  "joins": [
    {
      "type": "LEFT",
      "table": "sys_dept",
      "on": "sys_user.dept_id = sys_dept.dept_id"
    }
  ]
}
```

**field_config字段JSON结构**:
```json
{
  "fields": [
    {
      "name": "user_id",
      "alias": "用户ID",
      "type": "BIGINT",
      "visible": true
    },
    {
      "name": "user_name",
      "alias": "用户名",
      "type": "VARCHAR",
      "visible": true
    },
    {
      "name": "profit_margin",
      "alias": "利润率",
      "type": "DECIMAL",
      "calculated": true,
      "expression": "profit / revenue * 100"
    }
  ]
}
```

**extract_config字段JSON结构**:
```json
{
  "schedule": {
    "enabled": true,
    "cronExpression": "0 0 2 * * ?",
    "timezone": "Asia/Shanghai"
  },
  "incremental": false,
  "incrementalField": "update_time"
}
```

#### bi_visualization (可视化表)

```sql
CREATE TABLE bi_visualization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '可视化ID',
    name VARCHAR(100) NOT NULL COMMENT '可视化名称',
    dataset_id BIGINT NOT NULL COMMENT '数据集ID',
    type VARCHAR(20) NOT NULL COMMENT '图表类型: kpi, line, bar, map, table, pie, donut, funnel',
    config TEXT NOT NULL COMMENT '可视化配置(JSON格式)',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    FOREIGN KEY (dataset_id) REFERENCES bi_dataset(id),
    INDEX idx_dataset (dataset_id),
    INDEX idx_type (type)
) COMMENT='BI可视化表';
```

**config字段JSON结构(折线图示例)**:
```json
{
  "dimensions": ["date"],
  "metrics": [
    {
      "field": "loan_balance",
      "aggregation": "SUM",
      "alias": "贷款余额"
    }
  ],
  "filters": [],
  "sort": [
    {
      "field": "date",
      "order": "ASC"
    }
  ],
  "limit": 1000,
  "chartOptions": {
    "xAxis": {
      "type": "time",
      "format": "YYYY-MM-DD"
    },
    "yAxis": {
      "name": "金额(万元)"
    },
    "series": {
      "smooth": true,
      "showSymbol": false
    }
  }
}
```

#### bi_dashboard (仪表板表)

```sql
CREATE TABLE bi_dashboard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仪表板ID',
    name VARCHAR(100) NOT NULL COMMENT '仪表板名称',
    layout_config TEXT NOT NULL COMMENT '布局配置(JSON格式)',
    filter_config TEXT COMMENT '全局筛选器配置(JSON格式)',
    theme_config TEXT COMMENT '主题配置(JSON格式)',
    status CHAR(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_status (status),
    INDEX idx_create_by (create_by)
) COMMENT='BI仪表板表';
```

**layout_config字段JSON结构**:
```json
{
  "components": [
    {
      "id": "comp_1",
      "visualizationId": 1,
      "position": {
        "x": 0,
        "y": 0,
        "w": 6,
        "h": 4
      }
    },
    {
      "id": "comp_2",
      "visualizationId": 2,
      "position": {
        "x": 6,
        "y": 0,
        "w": 6,
        "h": 4
      }
    }
  ],
  "linkages": [
    {
      "source": "comp_1",
      "target": "comp_2",
      "fieldMapping": {
        "region": "region_filter"
      }
    }
  ]
}
```

**filter_config字段JSON结构**:
```json
{
  "filters": [
    {
      "id": "filter_date",
      "type": "dateRange",
      "label": "时间范围",
      "defaultValue": ["2024-01-01", "2024-12-31"],
      "targetFields": [
        {
          "componentId": "comp_1",
          "field": "date"
        },
        {
          "componentId": "comp_2",
          "field": "report_date"
        }
      ]
    }
  ]
}
```

#### bi_extract_data (抽取数据表 - MySQL)

```sql
CREATE TABLE bi_extract_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    dataset_id BIGINT NOT NULL COMMENT '数据集ID',
    data_content TEXT NOT NULL COMMENT '数据内容(JSON格式,包含所有字段)',
    extract_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抽取时间',
    INDEX idx_dataset (dataset_id),
    INDEX idx_extract_time (extract_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BI抽取数据表';
```

**说明**: 此表存储在MySQL数据库中,用于抽取数据集的数据存储。每个数据集的数据以JSON格式存储,支持高效查询。对于大数据量场景,可按数据集ID分表存储(如 bi_extract_data_1, bi_extract_data_2 等)。

#### bi_audit_log (审计日志表)

```sql
CREATE TABLE bi_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    user_name VARCHAR(64) COMMENT '用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型: datasource, dataset, dashboard',
    resource_id BIGINT COMMENT '资源ID',
    operation_detail TEXT COMMENT '操作详情(JSON格式)',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME NOT NULL COMMENT '操作时间',
    INDEX idx_user (user_id),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_time (create_time)
) COMMENT='BI审计日志表';
```

### 领域对象

#### DataSource (数据源)

```java
public class DataSource extends BaseEntity {
    private Long id;
    private String name;
    private DataSourceType type; // MYSQL, POSTGRESQL, CLICKHOUSE, DORIS, ORACLE, API, FILE
    private String config; // JSON配置(加密)
    private String status; // 0正常 1停用
    private String remark;
    
    // 业务方法
    public ConnectionConfig getConnectionConfig() {
        // 解密并解析config JSON
    }
}
```

#### Dataset (数据集)

```java
public class Dataset extends BaseEntity {
    private Long id;
    private String name;
    private Long datasourceId;
    private DatasetType type; // DIRECT, EXTRACT
    private String queryConfig; // JSON配置
    private String fieldConfig; // JSON配置
    private String extractConfig; // JSON配置
    private Date lastExtractTime;
    private Long rowCount;
    private String status;
    private String remark;
    
    // 关联对象
    private DataSource dataSource;
    private List<Field> fields;
    
    // 业务方法
    public QueryConfig getQueryConfig() {
        // 解析queryConfig JSON
    }
    
    public List<Field> getFields() {
        // 解析fieldConfig JSON
    }
}
```

#### Visualization (可视化)

```java
public class Visualization extends BaseEntity {
    private Long id;
    private String name;
    private Long datasetId;
    private VisualizationType type; // KPI, LINE, BAR, MAP, TABLE, PIE, DONUT, FUNNEL
    private String config; // JSON配置
    private String remark;
    
    // 关联对象
    private Dataset dataset;
    
    // 业务方法
    public VisualizationConfig getConfig() {
        // 解析config JSON
    }
}
```

#### Dashboard (仪表板)

```java
public class Dashboard extends BaseEntity {
    private Long id;
    private String name;
    private String layoutConfig; // JSON配置
    private String filterConfig; // JSON配置
    private String themeConfig; // JSON配置
    private String status;
    private String remark;
    
    // 关联对象
    private List<Visualization> visualizations;
    
    // 业务方法
    public LayoutConfig getLayoutConfig() {
        // 解析layoutConfig JSON
    }
    
    public List<GlobalFilter> getGlobalFilters() {
        // 解析filterConfig JSON
    }
}
```


## 错误处理

### 错误分类

#### 1. 数据源连接错误

**场景**: 数据源配置错误、网络问题、凭据失效

**处理策略**:
- 捕获SQLException和连接超时异常
- 返回详细错误信息(隐藏敏感凭据)
- 记录错误日志到bi_audit_log
- 前端显示用户友好的错误提示

**示例**:
```java
try {
    connection = dataSourceManager.getConnection(dataSourceId);
} catch (SQLException e) {
    log.error("数据源连接失败: dataSourceId={}, error={}", dataSourceId, e.getMessage());
    throw new ServiceException("数据源连接失败: " + e.getMessage());
}
```

#### 2. 查询执行错误

**场景**: SQL语法错误、查询超时、权限不足

**处理策略**:
- 设置查询超时(30秒)
- 捕获SQLTimeoutException
- 验证SQL语法(使用Calcite或正则表达式)
- 返回具体错误位置和建议

**示例**:
```java
@Override
public QueryResult executeQuery(Long datasetId, List<Filter> filters, SysUser user) {
    try {
        String sql = buildQuery(datasetId, filters, user);
        Statement stmt = connection.createStatement();
        stmt.setQueryTimeout(30); // 30秒超时
        ResultSet rs = stmt.executeQuery(sql);
        return parseResultSet(rs);
    } catch (SQLTimeoutException e) {
        log.warn("查询超时: datasetId={}, sql={}", datasetId, sql);
        throw new ServiceException("查询超时,建议转换为抽取数据集");
    } catch (SQLException e) {
        log.error("查询执行失败: {}", e.getMessage());
        throw new ServiceException("查询执行失败: " + e.getMessage());
    }
}
```

#### 3. 数据抽取错误

**场景**: 抽取任务失败、数据量过大、磁盘空间不足

**处理策略**:
- 失败重试机制(最多3次,指数退避)
- 保留上一次成功的抽取数据
- 发送通知给管理员
- 记录详细错误日志

**示例**:
```java
@Override
public ExtractResult executeExtract(Long datasetId) {
    int retryCount = 0;
    int maxRetries = 3;
    
    while (retryCount < maxRetries) {
        try {
            // 执行抽取逻辑
            List<Map<String, Object>> data = fetchDataFromSource(datasetId);
            storeToExtractTable(datasetId, data);
            updateExtractMetadata(datasetId, data.size());
            return ExtractResult.success(data.size());
        } catch (Exception e) {
            retryCount++;
            log.error("抽取失败(第{}次重试): datasetId={}, error={}", 
                     retryCount, datasetId, e.getMessage());
            
            if (retryCount >= maxRetries) {
                // 发送通知
                notifyAdministrators(datasetId, e);
                throw new ServiceException("数据抽取失败,已达到最大重试次数");
            }
            
            // 指数退避
            Thread.sleep(1000 * (long)Math.pow(2, retryCount));
        }
    }
}
```

#### 4. 权限验证错误

**场景**: 用户无权访问数据源、数据集或仪表板

**处理策略**:
- 在Controller层使用@PreAuthorize验证功能权限
- 在Service层验证数据权限(行级安全)
- 返回403 Forbidden响应
- 记录未授权访问尝试

**示例**:
```java
@PreAuthorize("@ss.hasPermi('bi:dashboard:view')")
@GetMapping("/{id}")
public AjaxResult getDashboard(@PathVariable Long id) {
    Dashboard dashboard = dashboardService.selectDashboardById(id);
    
    // 验证数据权限
    if (!hasDataPermission(dashboard)) {
        log.warn("用户无权访问仪表板: userId={}, dashboardId={}", 
                SecurityUtils.getUserId(), id);
        return AjaxResult.error(HttpStatus.FORBIDDEN, "无权访问此仪表板");
    }
    
    return AjaxResult.success(dashboard);
}
```

#### 5. 配置验证错误

**场景**: 字段类型转换失败、计算字段表达式错误、JOIN配置错误

**处理策略**:
- 在保存时验证配置
- 提供配置预览功能
- 返回具体的验证错误信息
- 建议修正方案

**示例**:
```java
public void validateDatasetConfig(Dataset dataset) {
    // 验证字段配置
    List<Field> fields = dataset.getFields();
    for (Field field : fields) {
        if (field.isCalculated()) {
            validateExpression(field.getExpression());
        }
    }
    
    // 验证JOIN配置
    if (dataset.hasJoins()) {
        validateJoinConfig(dataset.getQueryConfig().getJoins());
    }
}

private void validateExpression(String expression) {
    try {
        // 使用正则或Calcite验证表达式
        if (!isValidExpression(expression)) {
            throw new ServiceException("计算字段表达式无效: " + expression);
        }
    } catch (Exception e) {
        throw new ServiceException("表达式验证失败: " + e.getMessage());
    }
}
```

### 错误响应格式

所有API错误响应遵循AjaxResult格式:

```json
{
  "code": 500,
  "msg": "数据源连接失败: 连接超时",
  "data": null
}
```

### 日志记录

**日志级别**:
- ERROR: 系统错误、数据库错误、连接失败
- WARN: 查询超时、权限不足、配置警告
- INFO: 正常操作、抽取完成、缓存命中
- DEBUG: 详细SQL、查询参数、性能指标

**日志格式**:
```
[时间] [级别] [类名] - 操作描述: 关键参数, 结果/错误
```

**示例**:
```
2024-01-15 10:30:45 [ERROR] DataSourceManager - 数据源连接失败: dataSourceId=1, error=Connection timeout
2024-01-15 10:31:20 [INFO] QueryExecutor - 查询执行成功: datasetId=5, rows=1250, duration=850ms
2024-01-15 10:32:10 [WARN] CacheManager - 缓存内存使用超过80%: current=85%, evicting LRU entries
```

## 测试策略

### 测试方法

本项目采用**双重测试方法**:

1. **单元测试**: 验证具体示例、边界情况和错误条件
2. **属性测试**: 验证跨所有输入的通用属性

两种测试方法是互补的,共同确保全面覆盖:
- 单元测试捕获具体的错误和边界情况
- 属性测试通过随机化验证一般正确性

### 单元测试

**测试框架**: JUnit 5 + Mockito

**测试范围**:
- Controller层: 测试API端点、权限验证、参数验证
- Service层: 测试业务逻辑、错误处理
- Mapper层: 测试SQL查询(使用MySQL测试容器)
- 工具类: 测试加密、解密、JSON解析

**示例**:
```java
@SpringBootTest
class DataSourceServiceTest {
    
    @Autowired
    private IDataSourceService dataSourceService;
    
    @Test
    void testCreateDataSource_Success() {
        DataSource ds = new DataSource();
        ds.setName("测试数据源");
        ds.setType("mysql");
        ds.setConfig("{\"host\":\"localhost\",\"port\":3306}");
        
        int result = dataSourceService.insertDataSource(ds);
        assertEquals(1, result);
        assertNotNull(ds.getId());
    }
    
    @Test
    void testCreateDataSource_InvalidConfig() {
        DataSource ds = new DataSource();
        ds.setName("测试数据源");
        ds.setType("mysql");
        ds.setConfig("invalid json");
        
        assertThrows(ServiceException.class, () -> {
            dataSourceService.insertDataSource(ds);
        });
    }
    
    @Test
    void testDeleteDataSource_InUse() {
        // 创建数据源和依赖的数据集
        Long dataSourceId = createDataSourceWithDataset();
        
        // 尝试删除应该失败
        assertThrows(ServiceException.class, () -> {
            dataSourceService.deleteDataSourceById(dataSourceId);
        });
    }
}
```

**前端单元测试**: Jest + Vue Test Utils

```javascript
import { mount } from '@vue/test-utils'
import DataSourceList from '@/views/bi/datasource/index.vue'

describe('DataSourceList.vue', () => {
  it('renders datasource list', async () => {
    const wrapper = mount(DataSourceList)
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('.datasource-table').exists()).toBe(true)
  })
  
  it('opens add dialog when clicking add button', async () => {
    const wrapper = mount(DataSourceList)
    await wrapper.find('.add-button').trigger('click')
    
    expect(wrapper.vm.open).toBe(true)
    expect(wrapper.find('.el-dialog').isVisible()).toBe(true)
  })
})
```

### 属性测试

**测试框架**: JUnit-Quickcheck (Java属性测试库)

**配置**: 每个属性测试运行最少100次迭代

**测试标记格式**:
```java
/**
 * Feature: bi-platform-upgrade, Property 1: 数据源连接往返一致性
 */
```

**属性测试将在正确性属性部分定义后实现**

### 集成测试

**测试范围**:
- 端到端API测试(使用RestAssured)
- 数据库集成测试(使用Testcontainers)
- 前后端集成测试(使用Cypress)

**示例**:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class DataSourceIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.2.0");
    
    @LocalServerPort
    private int port;
    
    @Test
    void testDataSourceLifecycle() {
        // 创建数据源
        DataSource ds = createDataSource();
        Long id = given()
            .contentType(ContentType.JSON)
            .body(ds)
            .when()
            .post("/api/bi/datasource")
            .then()
            .statusCode(200)
            .extract()
            .path("data.id");
        
        // 测试连接
        given()
            .contentType(ContentType.JSON)
            .body(ds)
            .when()
            .post("/api/bi/datasource/test")
            .then()
            .statusCode(200)
            .body("data.success", equalTo(true));
        
        // 删除数据源
        given()
            .when()
            .delete("/api/bi/datasource/" + id)
            .then()
            .statusCode(200);
    }
}
```

### 性能测试

**测试工具**: JMeter

**测试场景**:
1. 并发查询测试(100个并发用户)
2. 大数据量查询测试(100万行数据)
3. 缓存性能测试(缓存命中率>80%)
4. 抽取任务性能测试(1000万行数据抽取时间<10分钟)

**性能指标**:
- 直连查询响应时间: <3秒(P95)
- 抽取查询响应时间: <1秒(P95)
- 仪表板加载时间: <5秒(P95)
- 系统吞吐量: >100 QPS

### 测试覆盖率目标

- 代码覆盖率: >80%
- 分支覆盖率: >70%
- 关键路径覆盖率: 100%



## 仪表板设计器详细设计

### 设计目标与原则

#### 产品定位
仪表板设计器是企业级BI系统的核心模块,面向业务人员与数据分析人员,提供基于可视化拖拽的仪表板构建能力,用于将分散的数据分析结果统一组织、展示和交互。

#### 核心设计原则
1. **所见即所得(WYSIWYG)**: 设计时看到的效果即为最终展示效果
2. **图表与查询条件解耦**: 图表只定义"算什么、怎么分组",不固化业务条件
3. **仪表板层统一控制**: 查询条件在仪表板层统一定义和管理
4. **低代码/零SQL**: 通过可视化配置完成复杂分析,无需编写SQL
5. **支持复杂业务分析**: 支持多维度、多指标、条件级联、图表联动

### 整体页面结构

#### 三段式+三栏式布局

```
┌──────────────────────── 顶部工具栏 ────────────────────────┐
│ [返回] [撤销] [重做] 新建仪表板                            │
│ [图表] [查询组件] [富文本] [媒体] [Tab] [更多] [复用]     │
│                                    [预览] [保存] [发布]    │
├────────────────────────────────────────────────┬───────────┤
│                                                │           │
│                                                │           │
│              仪表板画布                        │  右侧     │
│          (图表自由布局)                        │  配置     │
│                                                │  面板     │
│  ┌────────────┐  ┌────────────┐              │           │
│  │  图表组件1  │  │  图表组件2  │              │  [数据]   │
│  │            │  │            │              │  [样式]   │
│  └────────────┘  └────────────┘              │  [高级]   │
│                                                │           │
│  ┌────────────────────────────┐              │           │
│  │      查询组件区域            │              │           │
│  │  [时间] [机构] [指标类型]    │              │           │
│  └────────────────────────────┘              │           │
│                                                │           │
└────────────────────────────────────────────────┴───────────┘
```

### 顶部工具栏设计

#### 基础操作区
- **返回**: 返回仪表板列表
- **撤销/重做**: 支持操作历史回退
- **仪表板名称**: 显示当前编辑的仪表板名称

#### 组件工具区
所有仪表板内容均通过此工具栏创建:

| 组件类型 | 说明 | 子类型 |
|---------|------|--------|
| 图表 | 数据可视化组件 | 指标类、表格类、分析类 |
| 查询组件 | 仪表板级查询条件 | 时间、下拉、输入、数值区间 |
| 富文本 | 文本说明组件 | 标题、段落、列表 |
| 媒体 | 图片、视频组件 | 图片、视频、图标 |
| Tab | 标签页容器 | 多页签切换 |
| 更多 | 其他组件 | iframe、分隔线 |
| 复用 | 组件/模板复用 | 保存的组件、模板库 |

#### 页面操作区
- **预览**: 进入只读展示模式,查看最终效果
- **保存**: 保存配置但不对外发布
- **发布**: 生成可访问的正式仪表板

### 仪表板画布设计

#### 画布能力
1. **自由布局**: 支持多个组件自由放置
2. **拖拽操作**: 
   - 从工具栏拖拽组件到画布
   - 在画布内拖拽调整位置
   - 拉伸调整组件大小
3. **网格系统**: 
   - 24列栅格布局
   - 自动吸附与对齐
   - 网格线辅助定位
4. **多组件管理**: 支持多个图表同时存在

#### 初始引导
当画布为空时显示:
```
┌─────────────────────────────────────┐
│                                     │
│         从顶部工具栏中选择组件       │
│         添加到这里创建仪表板         │
│                                     │
│              [图标]                 │
│                                     │
└─────────────────────────────────────┘
```

#### 组件选中态
选中组件后:
- 显示蓝色高亮边框
- 显示操作按钮: [复制] [删除] [配置] [上移] [下移]
- 右侧配置面板切换到该组件配置

### 图表组件体系

#### 图表类型分类

**1. 指标类**
- 仪表盘: 单值展示,带刻度盘
- 水波图: 百分比展示,水波动画
- 指标卡: 数字+标题+对比

**2. 表格类**
- 明细表: 原始数据展示,支持分页
- 汇总表: 聚合数据展示
- 透视表: 多维度交叉分析

**3. 分析类**
- 折线图/面积图: 趋势分析
- 柱状图/条形图: 对比分析
- 饼图/环形图: 占比分析
- 散点图/气泡图: 分布分析
- 雷达图: 多维度评估
- 漏斗图: 转化分析
- 地图: 地理分布
- 热力图: 密度分析
- 关系图: 关系网络
- 双轴图: 双指标对比

#### 图表使用流程
1. 从工具栏选择图表类型
2. 拖拽放置到画布
3. 在右侧配置面板配置数据
4. 实时预览结果
5. 调整样式和高级选项

### 图表配置设计(Widget Level)

#### 配置面板结构
右侧配置区根据选中对象变化,图表配置包含三个页签:

```
┌─────────────────────┐
│ [数据] [样式] [高级] │
├─────────────────────┤
│                     │
│   配置内容区域       │
│                     │
└─────────────────────┘
```

#### 数据配置(核心)

**1. 数据集选择**
```
数据集: [下拉选择] [编辑数据集]
```

**2. 维度配置**
```
类别轴(X轴):
  [+ 添加维度]
  - 数据日期 [×]
  
子类别(系列拆分):
  [+ 添加子类别]
  - 机构名称 [×]
```

**3. 指标配置**
```
值轴(Y轴):
  [+ 添加指标]
  - 不良贷款率
    聚合方式: [求和 ▼]
    别名: [不良贷款率]
    [×]
```

支持的聚合方式:
- 求和(SUM)
- 平均值(AVG)
- 最大值(MAX)
- 最小值(MIN)
- 记录数(COUNT)
- 去重计数(COUNT DISTINCT)

**4. 高级数据能力**
```
钻取维度: [+ 配置钻取路径]
  省份 → 城市 → 区县

图表级过滤器: [+ 添加过滤条件]
  字段: [选择字段]
  条件: [等于/不等于/大于/小于...]
  值: [输入值]

结果条数限制: [1000 ▼]
刷新频率: [手动 ▼] (手动/5分钟/10分钟/30分钟)
```

#### 图表设计特点
**核心理念**: 图表只定义"算什么、怎么分组",不固化时间、机构等业务条件

**示例**:
- ✅ 正确: 按数据日期统计不良贷款率
- ❌ 错误: 统计2024年1月某机构的不良贷款率

**优势**:
- 图表高度复用
- 查询条件统一维护
- 仪表板结构清晰
- 支持复杂分析场景

#### 样式配置
```
图表标题:
  显示标题: [√]
  标题文本: [月度不良贷款率趋势]
  标题颜色: [#333333]
  标题位置: [居中 ▼]

图表配色:
  配色方案: [默认 ▼]
  自定义颜色: [+ 添加颜色]

图例:
  显示图例: [√]
  图例位置: [底部 ▼]

坐标轴(直角坐标系图表):
  X轴:
    显示: [√]
    标签旋转: [0°]
  Y轴:
    显示: [√]
    轴名称: [金额(万元)]

内边距:
  模式: [统一值 ▼]
  值: [12]

圆角:
  模式: [统一值 ▼]
  值: [4]

背景:
  背景色: [#FFFFFF]
  背景模糊: [ ]
```

#### 高级配置
```
数据标签:
  显示数据标签: [√]
  标签格式: [值 ▼] (值/百分比/值+百分比)

动画效果:
  启用动画: [√]
  动画时长: [1000ms]

交互配置:
  启用缩放: [√]
  启用数据区域缩放: [√]
  启用工具箱: [√]

导出配置:
  允许导出图片: [√]
  允许导出数据: [√]
```

### 查询组件设计(仪表板级核心能力)

#### 查询组件定位
查询组件用于在仪表板层统一定义查询条件,控制多个图表的数据过滤逻辑。

**核心理念**: 条件集中配置,图表统一联动

#### 查询条件设置界面

采用三栏式弹窗结构:

```
┌─────────────────────────────────────────────────────────────┐
│  查询条件设置                                    [保存] [取消] │
├──────────┬──────────────────┬───────────────────────────────┤
│          │                  │                               │
│ 查询条件 │  关联图表与字段  │      查询条件配置             │
│ 列表     │                  │                               │
│          │                  │                               │
│ [+ 新增] │  自动映射 ○      │  条件名称: [时间范围]         │
│          │  自定义   ●      │                               │
│ ● 时间   │                  │  是否必填: [√]                │
│ ○ 机构   │  ☑ 折线图        │                               │
│ ○ 指标   │    数据日期      │  展示类型: [日期范围 ▼]      │
│          │                  │                               │
│          │  ☑ 柱状图        │  时间粒度: [年月日 ▼]        │
│          │    统计日期      │                               │
│          │                  │  默认值: [本月 ▼]            │
│          │  ☐ 饼图          │                               │
│          │    (不参与)      │  时间范围限制:                │
│          │                  │    最早: [2020-01-01]         │
│          │                  │    最晚: [今天]               │
│          │                  │                               │
└──────────┴──────────────────┴───────────────────────────────┘
```

#### 查询条件管理(左侧)

**功能**:
- 新增多个查询条件
- 重命名条件
- 排序条件
- 显示/隐藏条件
- 删除条件

**示例条件**:
- 时间范围
- 机构选择
- 指标类型
- 状态筛选
- 数值区间

#### 条件与图表字段映射(中间)

**映射方式**:
1. **自动映射**: 系统根据字段名称自动匹配
2. **自定义映射**(推荐): 手动指定每个图表的映射字段

**能力说明**:
- 一个查询条件可关联多个图表
- 不同图表可映射不同字段
- 可勾选是否参与过滤

**示例**:
```
时间条件 →
  ☑ 折线图: 数据日期
  ☑ 仪表盘: 数据日期
  ☑ 柱状图: 统计日期
  ☐ 饼图: (不参与过滤)
```

#### 查询条件配置(右侧)

**基础配置**:
```
条件名称: [输入名称]
是否必填: [√/×]
展示类型: [下拉选择]
  - 时间选择器
  - 日期范围选择器
  - 下拉单选
  - 下拉多选
  - 输入框
  - 数值区间
  - 滑块
```

**时间条件扩展配置**:
```
时间粒度:
  ○ 年
  ○ 年月
  ● 年月日

时间范围限制:
  最早时间: [2020-01-01]
  最晚时间: [今天]

默认时间值:
  ○ 今天
  ● 本月
  ○ 本季度
  ○ 本年
  ○ 最近7天
  ○ 最近30天
  ○ 自定义
```

**下拉条件扩展配置**:
```
数据来源:
  ○ 固定选项
  ● 数据集字段

数据集: [选择数据集]
字段: [选择字段]

是否多选: [√]
默认值: [选择默认值]
```

**数值区间配置**:
```
最小值: [0]
最大值: [100]
步长: [1]
单位: [万元]
默认范围: [0] - [100]
```

#### 条件级联能力

支持查询条件之间的依赖关系:

```
级联配置:
  父条件: [机构选择]
  子条件: [网点选择]
  
  级联规则:
    当机构选择变化时 →
    重新加载网点选择的选项列表 →
    筛选条件: 网点.机构ID = 机构选择.值
```

**示例场景**:
1. 先选机构 → 再选网点
2. 先选省份 → 再选城市 → 再选区县
3. 先选产品类型 → 再选具体产品

### 查询联动机制

#### 联动逻辑流程

```
用户操作查询组件
    ↓
查询条件值变化
    ↓
触发条件→字段映射
    ↓
为每个关联图表注入查询参数
    ↓
图表统一刷新数据
    ↓
展示过滤后的结果
```

#### 查询参数注入

**示例**:
```javascript
// 查询条件
{
  "时间范围": ["2024-01-01", "2024-01-31"],
  "机构": ["机构A", "机构B"],
  "指标类型": "不良贷款"
}

// 映射到图表1(折线图)
{
  "datasetId": 1,
  "filters": [
    { "field": "数据日期", "operator": "between", "value": ["2024-01-01", "2024-01-31"] },
    { "field": "机构名称", "operator": "in", "value": ["机构A", "机构B"] },
    { "field": "指标类型", "operator": "eq", "value": "不良贷款" }
  ]
}

// 映射到图表2(柱状图)
{
  "datasetId": 2,
  "filters": [
    { "field": "统计日期", "operator": "between", "value": ["2024-01-01", "2024-01-31"] },
    { "field": "机构代码", "operator": "in", "value": ["A001", "A002"] }
  ]
}
```

#### 核心优势
1. **图表高度复用**: 同一个图表可用于不同的查询场景
2. **查询条件统一维护**: 避免在每个图表中重复配置
3. **仪表板结构清晰**: 查询区域和展示区域分离
4. **支持复杂分析场景**: 多条件组合、条件级联、动态联动

### 仪表板级配置(Global Level)

当未选中具体组件时,右侧展示仪表板配置:

#### 仪表板风格
```
主题:
  ○ 浅色主题
  ● 深色主题
```

#### 整体配置
```
页面边距:
  上: [20]
  下: [20]
  左: [20]
  右: [20]

栅格规则:
  列数: [24]
  行高: [30px]
  间距: [10px]

对齐方式:
  水平对齐: [左对齐 ▼]
  垂直对齐: [顶部对齐 ▼]
```

#### 背景配置
```
背景类型:
  ● 纯色
  ○ 图片
  ○ 渐变

背景色: [#F5F7FA]
透明度: [100%]
```

#### 图表通用样式
```
统一配色:
  启用统一配色: [√]
  配色方案: [默认 ▼]

标题样式:
  字体: [微软雅黑 ▼]
  字号: [14px]
  颜色: [#333333]
  粗细: [正常 ▼]

字体规范:
  正文字体: [微软雅黑 ▼]
  正文字号: [12px]
```

#### 数字格式
```
千分位: [√]
小数位数: [2]
单位:
  ○ 无
  ○ %
  ○ 万
  ● 亿
```

### 系统能力总结

仪表板设计器通过可视化拖拽方式,将数据集、图表组件和查询条件进行解耦组合,实现多图表统一联动、灵活配置与实时预览,满足企业级数据分析与展示需求。

**核心能力**:
1. ✅ 所见即所得的可视化设计
2. ✅ 图表与查询条件完全解耦
3. ✅ 仪表板层统一查询控制
4. ✅ 一个条件关联多个图表
5. ✅ 不同图表映射不同字段
6. ✅ 支持条件级联和动态联动
7. ✅ 低代码/零SQL配置
8. ✅ 支持复杂业务分析场景



### 设计目标

将原有的"可视化管理"和"仪表板管理"两个独立模块合并为一个统一的"仪表板管理"模块,所有图表创建和配置功能都在仪表板设计器中完成,以提高用户体验和工作效率。

### 架构变更

#### 前端架构调整

**移除模块**:
- `ui/src/views/bi/visualization/index.vue` - 独立的可视化管理页面

**新增组件**:
- `ui/src/components/BiChartLibrary/index.vue` - 图表库选择对话框
- `ui/src/components/BiChartLibrary/ChartList.vue` - 图表类型列表
- `ui/src/components/BiChartLibrary/ChartPreview.vue` - 图表预览组件
- `ui/src/components/BiChartLibrary/BasicStyleConfig.vue` - 基础样式配置
- `ui/src/components/BiChartLibrary/chartTypes.js` - 图表类型定义

**修改组件**:
- `ui/src/views/bi/dashboard/index.vue` - 集成图表库和设计器功能
- `ui/src/components/BiDashboardDesigner/index.vue` - 增强设计器工具栏和配置面板

#### 后端架构调整

**保留API**: 虽然前端不再有独立的可视化管理页面,但后端Visualization相关API仍然保留,因为:
- 仪表板中的组件本质上就是可视化组件
- 需要保存和加载组件配置
- 支持组件的复用和模板化

**API端点保持不变**:
- `POST /bi/visualization` - 创建可视化组件(内部使用)
- `PUT /bi/visualization` - 更新可视化组件
- `GET /bi/visualization/{id}` - 获取可视化组件
- `DELETE /bi/visualization/{id}` - 删除可视化组件
- `POST /bi/visualization/{id}/preview` - 预览可视化组件

### 用户界面设计

#### 仪表板设计器主界面

```
┌─────────────────────────────────────────────────────────────────────┐
│  仪表板设计器                                    保存   预览   发布   │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ 工具栏                                                        │  │
│  │ [图表▼] [筛选器] [富文本] [媒体] [Tab] [更多▼]              │  │
│  │ [复制] [删除] [对齐] [层级]                                  │  │
│  └──────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                                                              │  │
│  │                        画布区域                              │  │
│  │                    (拖拽式布局)                              │  │
│  │                                                              │  │
│  │  ┌────────────┐  ┌────────────┐                            │  │
│  │  │  图表组件1  │  │  图表组件2  │                            │  │
│  │  │            │  │            │                            │  │
│  │  └────────────┘  └────────────┘                            │  │
│  │                                                              │  │
│  └──────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ 配置面板                                                      │  │
│  │ [仪表板配置] [数据配置] [图表样式] [图表配色] [图表标题]     │  │
│  │ [直角组件] [数字可视化] [高级样式设置]                       │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

#### 图表库选择对话框

```
┌─────────────────────────────────────────────────────────────────────┐
│  选择图表                                          保存   发布       │
├─────────────────────────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────────────────────────────────────────┐   │
│  │          │  │                                              │   │
│  │ 图表分类 │  │          图表预览区域                        │   │
│  │          │  │                                              │   │
│  │ 仪表盘图表│  │                                              │   │
│  │ 基础柱状图│  │                                              │   │
│  │ 折线图   │  │                                              │   │
│  │ 饼图     │  ├──────────────────────────────────────────────┤   │
│  │ 散点图   │  │ 基础样式配置                                 │   │
│  │ 雷达图   │  │ □ 显示标题  [标题输入框]                    │   │
│  │ 漏斗图   │  │ 标题颜色: [颜色选择器]                       │   │
│  │ 表格     │  │ □ 自适应组件                                 │   │
│  │ 指标卡   │  │ 内边距: [统一值▼] [12]                      │   │
│  │ 查询控件 │  │ 圆角: [统一值▼] [0]                         │   │
│  │          │  │ 边框: [统一值▼] [0]                         │   │
│  │          │  │ □ 背景模糊 [滑块: 0-20]                     │   │
│  │          │  │ 底色: [颜色选择器]                           │   │
│  └──────────┘  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 图表类型定义

#### 支持的图表类型

1. **仪表盘图表**
   - 仪表盘 (gauge)
   - 水位图 (liquid)
   - 进度条 (progress)

2. **基础柱状图**
   - 柱状图 (bar)
   - 条形图 (horizontal-bar)
   - 堆叠柱状图 (stacked-bar)
   - 分组柱状图 (grouped-bar)

3. **折线图**
   - 折线图 (line)
   - 面积图 (area)
   - 堆叠面积图 (stacked-area)
   - 平滑折线图 (smooth-line)

4. **饼图**
   - 饼图 (pie)
   - 环形图 (doughnut)
   - 玫瑰图 (rose)

5. **散点图**
   - 散点图 (scatter)
   - 气泡图 (bubble)

6. **雷达图**
   - 雷达图 (radar)

7. **漏斗图**
   - 漏斗图 (funnel)

8. **表格**
   - 数据表格 (table)
   - 透视表 (pivot-table)

9. **指标卡**
   - 数字指标卡 (number-card)
   - KPI卡片 (kpi-card)
   - 对比卡片 (comparison-card)

10. **查询控件**
    - 下拉框 (select)
    - 多选下拉框 (multi-select)
    - 日期选择器 (date-picker)
    - 日期范围选择器 (date-range-picker)
    - 输入框 (input)
    - 数字输入框 (number-input)
    - 滑块 (slider)
    - 开关 (switch)

### 数据模型变更

#### Dashboard数据结构增强

```json
{
  "id": 1,
  "name": "销售分析仪表板",
  "description": "销售数据分析",
  "layoutConfig": {
    "grid": {
      "cols": 24,
      "rowHeight": 30
    },
    "components": [
      {
        "i": "comp-1",
        "x": 0,
        "y": 0,
        "w": 12,
        "h": 10,
        "type": "bar",
        "config": {
          "dataConfig": {
            "datasetId": 1,
            "dimensions": ["month"],
            "metrics": ["sales"]
          },
          "basicStyle": {
            "showTitle": true,
            "title": "月度销售额",
            "titleColor": "#333333",
            "autoResize": true,
            "padding": {
              "top": 12,
              "bottom": 12,
              "left": 12,
              "right": 12
            },
            "borderRadius": {
              "topLeft": 4,
              "topRight": 4,
              "bottomLeft": 4,
              "bottomRight": 4
            },
            "backgroundColor": "#ffffff"
          },
          "chartStyle": {
            "colors": ["#5470c6", "#91cc75"],
            "legend": { "show": true, "position": "top" },
            "tooltip": { "show": true }
          },
          "axis": {
            "xAxis": { "show": true, "type": "category" },
            "yAxis": { "show": true, "name": "销售额" }
          }
        }
      }
    ]
  },
  "filterConfig": {
    "filters": []
  },
  "themeConfig": {
    "theme": "light",
    "primaryColor": "#409EFF"
  },
  "status": "0"
}
```

### 组件接口

#### BiChartLibrary组件

**Props**:
```javascript
{
  visible: Boolean,        // 对话框可见性
  dashboardId: Number     // 仪表板ID(可选)
}
```

**Events**:
```javascript
{
  'save': (chartConfig) => {},      // 保存图表配置
  'publish': (chartConfig) => {},   // 发布图表到画布
  'update:visible': (visible) => {} // 更新可见性
}
```

**Methods**:
```javascript
{
  open() {},              // 打开对话框
  close() {},             // 关闭对话框
  reset() {}              // 重置配置
}
```

#### BiDashboardDesigner组件增强

**新增Props**:
```javascript
{
  showChartLibrary: Boolean,  // 是否显示图表库按钮
  enableDragDrop: Boolean     // 是否启用拖拽功能
}
```

**新增Methods**:
```javascript
{
  openChartLibrary() {},      // 打开图表库
  addComponent(config) {},    // 添加组件到画布
  removeComponent(id) {},     // 移除组件
  updateComponent(id, config) {}, // 更新组件配置
  getLayout() {}              // 获取当前布局
}
```

### 工作流程

#### 创建图表流程

1. 用户在仪表板设计器中点击"图表"按钮
2. 打开图表库选择对话框
3. 用户在左侧选择图表类型
4. 右侧显示图表预览和基础样式配置
5. 用户配置基础样式(标题、颜色、内边距等)
6. 用户点击"发布"按钮
7. 图表组件添加到仪表板画布
8. 用户在画布中调整组件位置和大小
9. 用户在右侧配置面板配置数据和高级样式
10. 用户保存仪表板

#### 编辑图表流程

1. 用户在画布中选中图表组件
2. 右侧配置面板显示该组件的配置选项
3. 用户修改配置(数据、样式、标题等)
4. 配置实时应用到组件预览
5. 用户保存仪表板

### 菜单配置变更

#### 删除可视化管理菜单

```sql
-- 删除可视化管理菜单
DELETE FROM sys_menu WHERE menu_name = '可视化管理' AND perms LIKE '%bi:visualization%';
```

#### 更新仪表板管理菜单权限

```sql
-- 更新仪表板管理菜单,添加设计权限
UPDATE sys_menu 
SET perms = 'bi:dashboard:list,bi:dashboard:add,bi:dashboard:edit,bi:dashboard:remove,bi:dashboard:design,bi:dashboard:view'
WHERE menu_name = '仪表板管理';
```

### 迁移策略

#### 现有数据迁移

对于已经创建的可视化组件:
1. 保留`bi_visualization`表和数据
2. 现有可视化组件可以继续在仪表板中使用
3. 新创建的图表组件配置直接保存在仪表板的`layout_config`中
4. 可选:提供迁移工具将独立的可视化组件转换为仪表板内嵌组件

#### 向后兼容

1. 后端Visualization API保持不变
2. 现有仪表板引用的可视化组件继续工作
3. 前端路由移除可视化管理页面,但API调用保持兼容

### 实施计划

#### 阶段1: 图表库组件开发 (3-4天)
- [ ] 创建BiChartLibrary主组件
- [ ] 创建ChartList图表列表组件
- [ ] 创建ChartPreview预览组件
- [ ] 创建BasicStyleConfig配置组件
- [ ] 定义chartTypes图表类型

#### 阶段2: 仪表板设计器增强 (2-3天)
- [ ] 更新工具栏,添加图表按钮
- [ ] 集成图表库对话框
- [ ] 实现拖拽添加组件功能
- [ ] 更新配置面板,支持组件配置

#### 阶段3: 功能迁移和清理 (1-2天)
- [ ] 移除可视化管理页面
- [ ] 更新路由配置
- [ ] 更新菜单SQL脚本
- [ ] 测试现有仪表板兼容性

#### 阶段4: 测试和优化 (2-3天)
- [ ] 功能测试
- [ ] 用户体验优化
- [ ] 性能优化
- [ ] Bug修复

**总计**: 8-12个工作日

### 风险和挑战

1. **用户习惯变更**: 用户需要适应新的操作流程
   - 缓解措施: 提供用户培训和操作指南

2. **现有数据兼容性**: 确保现有仪表板和可视化组件继续工作
   - 缓解措施: 保留后端API,提供数据迁移工具

3. **性能影响**: 大型仪表板的渲染性能
   - 缓解措施: 实现组件懒加载和虚拟滚动

4. **复杂度增加**: 仪表板设计器功能更复杂
   - 缓解措施: 模块化设计,清晰的组件职责划分

### 成功标准

1. 用户可以在仪表板设计器中完成所有图表创建和配置
2. 不再需要访问独立的可视化管理页面
3. 现有仪表板和可视化组件继续正常工作
4. 图表创建流程时间减少50%以上
5. 用户满意度提升(通过用户反馈调查)

