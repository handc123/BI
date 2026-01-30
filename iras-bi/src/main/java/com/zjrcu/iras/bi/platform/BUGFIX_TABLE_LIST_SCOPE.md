# 修复：表列表范围问题

## 问题描述

在数据集管理页面添加数据集时，选择"表查询"类型后，表名下拉框显示了**所有数据库**的表，而不是只显示**数据源配置中指定的数据库**的表。

## 问题原因

在 `DataSourceManager.getTableList()` 方法中，调用 `DatabaseMetaData.getTables()` 时使用了 `null` 作为 catalog 和 schema 参数：

```java
// 错误的实现
metaData.getTables(null, null, "%", new String[]{"TABLE"})
```

这会导致返回所有数据库的表，而不是当前连接的数据库的表。

## 解决方案

### 修改内容

修改 `DataSourceManager.getTableList()` 方法，使用当前连接的 catalog 和 schema：

```java
// 获取当前连接的数据库名称
String catalog = connection.getCatalog();
String schema = connection.getSchema();

// 使用 catalog 和 schema 限定查询范围
metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})
```

### 关键改进

1. **使用 catalog 参数**：
   - MySQL 使用 catalog 表示数据库名称
   - 通过 `connection.getCatalog()` 获取当前数据库名称

2. **使用 schema 参数**：
   - PostgreSQL、Oracle 使用 schema 表示模式名称
   - 通过 `connection.getSchema()` 获取当前模式名称

3. **过滤系统表**：
   - 添加 `isSystemTable()` 方法过滤系统表
   - 过滤 MySQL 系统表（mysql.*, information_schema.*, performance_schema.*, sys.*）
   - 过滤 PostgreSQL 系统表（pg_*）
   - 过滤 Oracle 系统表（sys_*, dba_*, all_*, user_*）

4. **增强日志**：
   - 记录 catalog 和 schema 信息
   - 便于调试和问题排查

## 技术细节

### JDBC DatabaseMetaData.getTables() 参数说明

```java
ResultSet getTables(
    String catalog,      // 数据库名称（MySQL）或 null
    String schemaPattern, // 模式名称（PostgreSQL/Oracle）或 null
    String tableNamePattern, // 表名模式，% 表示所有
    String[] types       // 表类型：TABLE, VIEW, SYSTEM TABLE 等
)
```

### 不同数据库的行为

| 数据库 | catalog | schema | 说明 |
|--------|---------|--------|------|
| MySQL | 数据库名 | null | catalog 表示数据库 |
| PostgreSQL | 数据库名 | public/自定义 | schema 表示模式 |
| Oracle | null | 用户名/模式名 | schema 表示模式 |
| ClickHouse | 数据库名 | null | catalog 表示数据库 |

## 测试验证

### 测试步骤

1. 创建两个数据源：
   - 数据源A：连接到数据库 `db_a`
   - 数据源B：连接到数据库 `db_b`

2. 在数据集管理页面：
   - 点击"新增数据集"
   - 选择数据源A
   - 选择"表查询"类型
   - 查看表名下拉框

3. 验证结果：
   - ✅ 只显示 `db_a` 数据库的表
   - ✅ 不显示 `db_b` 数据库的表
   - ✅ 不显示系统表

### 预期行为

- **修复前**：显示所有数据库的表（db_a + db_b + 系统表）
- **修复后**：只显示当前数据源配置的数据库的表（db_a）

## 影响范围

### 修改的文件

- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/DataSourceManager.java`
  - 修改 `getTableList()` 方法
  - 新增 `isSystemTable()` 方法

### 影响的功能

- ✅ 数据集管理 - 表名下拉选择
- ✅ 表列表查询 API

### 不影响的功能

- ❌ 数据源管理
- ❌ 数据集查询
- ❌ 其他功能

## 相关问题

### 为什么之前使用 null？

使用 `null` 作为 catalog 和 schema 参数会让 JDBC 驱动返回所有可访问的表，这在某些场景下可能是有用的，但在我们的场景中，我们只需要当前数据库的表。

### 为什么需要过滤系统表？

即使指定了 catalog 和 schema，某些数据库驱动仍然可能返回系统表。过滤系统表可以：
1. 减少列表长度，提高用户体验
2. 避免用户误选系统表
3. 提高查询性能

### 如何处理跨数据库查询？

如果需要跨数据库查询，应该：
1. 创建多个数据源，每个数据源连接到不同的数据库
2. 在数据集中使用 JOIN 配置连接多个数据源
3. 不应该在单个数据源中访问多个数据库

## 后续优化建议

1. **缓存表列表**：
   - 对表列表进行缓存，减少数据库查询
   - 设置合理的缓存过期时间

2. **表信息增强**：
   - 显示表的行数
   - 显示表的大小
   - 显示表的注释

3. **表分类**：
   - 按表类型分类（业务表、配置表等）
   - 按表前缀分组

4. **搜索优化**：
   - 支持拼音搜索
   - 支持模糊匹配
   - 高亮匹配结果

## 完成时间

2026-01-20

## 开发者

Kiro AI Assistant
