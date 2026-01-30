# BI平台领域实体类说明

## 概述

本目录包含BI平台升级项目的所有领域实体类，这些类对应数据库表结构，并提供JSON配置的解析功能。

## 实体类列表

### 1. DataSource.java (数据源实体)

**对应表**: `bi_datasource`

**主要字段**:
- `id`: 数据源ID (主键)
- `name`: 数据源名称
- `type`: 数据源类型 (mysql, postgresql, clickhouse, doris, oracle, api, file)
- `config`: 连接配置 (JSON格式，加密存储)
- `status`: 状态 (0正常 1停用)

**特殊功能**:
- `getConfigMap()`: 解析JSON配置为Map对象
- `setConfigMap()`: 将Map对象转换为JSON字符串
- `getConfigValue(key)`: 获取配置项的值
- `getConfigString(key)`: 获取配置项的字符串值
- `getConfigInteger(key)`: 获取配置项的整数值

**继承**: 继承自 `BaseEntity`，包含 createBy, createTime, updateBy, updateTime, remark 等字段

---

### 2. Dataset.java (数据集实体)

**对应表**: `bi_dataset`

**主要字段**:
- `id`: 数据集ID (主键)
- `name`: 数据集名称
- `datasourceId`: 数据源ID (外键)
- `type`: 数据集类型 (direct, extract)
- `queryConfig`: 查询配置 (JSON格式)
- `fieldConfig`: 字段配置 (JSON格式)
- `extractConfig`: 抽取配置 (JSON格式，仅抽取类型)
- `lastExtractTime`: 最后抽取时间
- `rowCount`: 数据行数
- `status`: 状态 (0正常 1停用)

**关联对象**:
- `dataSource`: 关联的数据源对象 (transient)

**特殊功能**:
- `getQueryConfigMap()`: 解析查询配置JSON
- `getFieldConfigMap()`: 解析字段配置JSON
- `getExtractConfigMap()`: 解析抽取配置JSON
- `isDirect()`: 判断是否为直连数据集
- `isExtract()`: 判断是否为抽取数据集

**继承**: 继承自 `BaseEntity`

---

### 3. Visualization.java (可视化实体)

**对应表**: `bi_visualization`

**主要字段**:
- `id`: 可视化ID (主键)
- `name`: 可视化名称
- `datasetId`: 数据集ID (外键)
- `type`: 图表类型 (kpi, line, bar, map, table, pie, donut, funnel)
- `config`: 可视化配置 (JSON格式)

**关联对象**:
- `dataset`: 关联的数据集对象 (transient)

**特殊功能**:
- `getConfigMap()`: 解析可视化配置JSON
- `getConfigValue(key)`: 获取配置项的值
- `isKpi()`, `isLine()`, `isBar()`, `isMap()`, `isTable()`, `isPie()`, `isDonut()`, `isFunnel()`: 判断图表类型

**继承**: 继承自 `BaseEntity`

---

### 4. Dashboard.java (仪表板实体)

**对应表**: `bi_dashboard`

**主要字段**:
- `id`: 仪表板ID (主键)
- `name`: 仪表板名称
- `layoutConfig`: 布局配置 (JSON格式)
- `filterConfig`: 全局筛选器配置 (JSON格式)
- `themeConfig`: 主题配置 (JSON格式)
- `status`: 状态 (0正常 1停用)

**关联对象**:
- `visualizations`: 关联的可视化组件列表 (transient)

**特殊功能**:
- `getLayoutConfigMap()`: 解析布局配置JSON
- `getFilterConfigMap()`: 解析筛选器配置JSON
- `getThemeConfigMap()`: 解析主题配置JSON
- `getComponents()`: 获取布局中的组件列表
- `getLinkages()`: 获取组件联动配置
- `getGlobalFilters()`: 获取全局筛选器列表

**继承**: 继承自 `BaseEntity`

---

### 5. AuditLog.java (审计日志实体)

**对应表**: `bi_audit_log`

**主要字段**:
- `id`: 日志ID (主键)
- `userId`: 用户ID
- `userName`: 用户名
- `operation`: 操作类型
- `resourceType`: 资源类型 (datasource, dataset, dashboard, visualization)
- `resourceId`: 资源ID
- `operationDetail`: 操作详情 (JSON格式)
- `ipAddress`: IP地址
- `createTime`: 操作时间

**特殊功能**:
- `getOperationDetailMap()`: 解析操作详情JSON
- `getDetailValue(key)`: 获取操作详情中的值
- `isCreateOperation()`: 判断是否为创建操作
- `isUpdateOperation()`: 判断是否为更新操作
- `isDeleteOperation()`: 判断是否为删除操作
- `isViewOperation()`: 判断是否为查看操作

**继承**: 实现 `Serializable` 接口 (不继承BaseEntity，因为审计日志不需要updateBy等字段)

---

## 设计特点

### 1. JSON配置解析

所有包含JSON配置的实体类都提供了：
- `getXxxConfigMap()`: 懒加载解析JSON为Map对象
- `setXxxConfigMap()`: 将Map对象转换为JSON字符串
- 使用 `@JsonIgnore` 和 `transient` 标记解析后的对象，避免序列化和持久化

### 2. 数据验证

使用Jakarta Bean Validation注解：
- `@NotBlank`: 非空验证
- `@NotNull`: 非null验证
- `@Size`: 长度验证

### 3. 关联对象

使用 `transient` 关键字标记关联对象，避免持久化：
- Dataset.dataSource
- Visualization.dataset
- Dashboard.visualizations

### 4. 工具方法

提供便捷的业务方法：
- 类型判断方法 (isDirect, isExtract, isKpi等)
- 配置访问方法 (getConfigValue, getConfigString等)
- 操作类型判断方法 (isCreateOperation, isUpdateOperation等)

### 5. toString方法

使用Apache Commons Lang的ToStringBuilder，采用MULTI_LINE_STYLE格式，便于调试和日志输出。

---

## 与数据库表的对应关系

| 实体类 | 数据库表 | 主键 | 外键 |
|--------|---------|------|------|
| DataSource | bi_datasource | id | - |
| Dataset | bi_dataset | id | datasource_id → bi_datasource.id |
| Visualization | bi_visualization | id | dataset_id → bi_dataset.id |
| Dashboard | bi_dashboard | id | - |
| AuditLog | bi_audit_log | id | - |

---

## 使用示例

### 创建数据源

```java
DataSource dataSource = new DataSource();
dataSource.setName("本地MySQL");
dataSource.setType("mysql");

Map<String, Object> config = new HashMap<>();
config.put("host", "localhost");
config.put("port", 3306);
config.put("database", "iras");
config.put("username", "root");
config.put("password", "encrypted_password");

dataSource.setConfigMap(config);
dataSource.setStatus("0");
dataSource.setRemark("本地MySQL数据源");
```

### 解析数据集配置

```java
Dataset dataset = datasetService.selectDatasetById(1L);

// 获取查询配置
Map<String, Object> queryConfig = dataset.getQueryConfigMap();
String tableName = (String) queryConfig.get("tableName");

// 判断数据集类型
if (dataset.isDirect()) {
    // 直连数据集处理逻辑
} else if (dataset.isExtract()) {
    // 抽取数据集处理逻辑
}
```

### 创建审计日志

```java
AuditLog log = new AuditLog();
log.setUserId(SecurityUtils.getUserId());
log.setUserName(SecurityUtils.getUsername());
log.setOperation("CREATE");
log.setResourceType("datasource");
log.setResourceId(dataSource.getId());

Map<String, Object> detail = new HashMap<>();
detail.put("name", dataSource.getName());
detail.put("type", dataSource.getType());

log.setOperationDetailMap(detail);
log.setIpAddress(IpUtils.getIpAddr());
log.setCreateTime(new Date());
```

---

## 验收标准检查

✅ **所有实体类字段与数据库表一致**
- DataSource: 5个核心字段 + BaseEntity字段
- Dataset: 10个核心字段 + BaseEntity字段
- Visualization: 5个核心字段 + BaseEntity字段
- Dashboard: 6个核心字段 + BaseEntity字段
- AuditLog: 9个核心字段

✅ **包含必要的注解**
- 使用Jakarta Bean Validation注解 (@NotBlank, @NotNull, @Size)
- 使用Jackson注解 (@JsonIgnore, @JsonFormat)
- 不使用MyBatis-Plus注解 (项目使用原生MyBatis)

✅ **JSON配置字段有对应的解析方法**
- DataSource: getConfigMap/setConfigMap
- Dataset: getQueryConfigMap/setQueryConfigMap, getFieldConfigMap/setFieldConfigMap, getExtractConfigMap/setExtractConfigMap
- Visualization: getConfigMap/setConfigMap
- Dashboard: getLayoutConfigMap/setLayoutConfigMap, getFilterConfigMap/setFilterConfigMap, getThemeConfigMap/setThemeConfigMap
- AuditLog: getOperationDetailMap/setOperationDetailMap

---

## 下一步

完成任务 1.2 后，下一步是任务 1.3：创建MyBatis Mapper接口和XML文件，为这些实体类提供数据访问层支持。
