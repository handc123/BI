# BI平台数据库表结构

## 概述

本目录包含BI平台升级所需的数据库表结构SQL脚本。这些表用于存储BI平台的元数据配置和抽取数据。

## 文件说明

- `bi_platform_schema.sql`: BI平台完整表结构定义

## 表结构清单

### 1. bi_datasource (数据源表)
存储所有数据源配置信息，支持多种数据库类型和API数据源。

**关键字段**:
- `id`: 数据源唯一标识
- `name`: 数据源名称
- `type`: 数据源类型 (mysql, postgresql, clickhouse, doris, oracle, api, file)
- `config`: 连接配置(JSON格式，密码加密存储)
- `status`: 状态 (0正常 1停用)

**索引**:
- `idx_type`: 按类型查询
- `idx_status`: 按状态筛选
- `idx_create_time`: 按创建时间排序

### 2. bi_dataset (数据集表)
存储数据集定义，包括直连和抽取两种类型。

**关键字段**:
- `id`: 数据集唯一标识
- `name`: 数据集名称
- `datasource_id`: 关联的数据源ID
- `type`: 数据集类型 (direct直连, extract抽取)
- `query_config`: 查询配置(JSON格式)
- `field_config`: 字段配置(JSON格式)
- `extract_config`: 抽取配置(JSON格式，仅抽取类型)
- `last_extract_time`: 最后抽取时间
- `row_count`: 数据行数

**外键约束**:
- `fk_dataset_datasource`: 关联bi_datasource表，RESTRICT删除策略

**索引**:
- `idx_datasource`: 按数据源查询
- `idx_type`: 按类型筛选
- `idx_status`: 按状态筛选

### 3. bi_visualization (可视化表)
存储可视化组件配置，支持多种图表类型。

**关键字段**:
- `id`: 可视化唯一标识
- `name`: 可视化名称
- `dataset_id`: 关联的数据集ID
- `type`: 图表类型 (kpi, line, bar, map, table, pie, donut, funnel)
- `config`: 可视化配置(JSON格式)

**外键约束**:
- `fk_visualization_dataset`: 关联bi_dataset表，RESTRICT删除策略

**索引**:
- `idx_dataset`: 按数据集查询
- `idx_type`: 按图表类型筛选

### 4. bi_dashboard (仪表板表)
存储仪表板布局和配置信息。

**关键字段**:
- `id`: 仪表板唯一标识
- `name`: 仪表板名称
- `layout_config`: 布局配置(JSON格式)
- `filter_config`: 全局筛选器配置(JSON格式)
- `theme_config`: 主题配置(JSON格式)
- `status`: 状态 (0正常 1停用)

**索引**:
- `idx_status`: 按状态筛选
- `idx_create_by`: 按创建者查询
- `idx_create_time`: 按创建时间排序

### 5. bi_extract_data (抽取数据表)
存储抽取数据集的实际数据，以JSON格式存储。

**关键字段**:
- `id`: 记录唯一标识
- `dataset_id`: 关联的数据集ID
- `data_content`: 数据内容(JSON格式)
- `extract_time`: 抽取时间

**外键约束**:
- `fk_extract_dataset`: 关联bi_dataset表，CASCADE删除策略

**索引**:
- `idx_dataset`: 按数据集查询
- `idx_extract_time`: 按抽取时间查询

**说明**: 对于大数据量场景，可考虑按数据集ID分表存储。

### 6. bi_audit_log (审计日志表)
记录所有BI平台操作的审计日志。

**关键字段**:
- `id`: 日志唯一标识
- `user_id`: 用户ID
- `user_name`: 用户名
- `operation`: 操作类型
- `resource_type`: 资源类型 (datasource, dataset, dashboard, visualization)
- `resource_id`: 资源ID
- `operation_detail`: 操作详情(JSON格式)
- `ip_address`: IP地址
- `create_time`: 操作时间

**索引**:
- `idx_user`: 按用户查询
- `idx_resource`: 按资源类型和ID查询
- `idx_time`: 按时间范围查询
- `idx_operation`: 按操作类型查询

## 外键关系图

```
bi_datasource (数据源)
    ↓ (1:N, RESTRICT)
bi_dataset (数据集)
    ↓ (1:N, RESTRICT)          ↓ (1:N, CASCADE)
bi_visualization (可视化)    bi_extract_data (抽取数据)
```

## 执行说明

### 1. 开发环境

```bash
# 连接到MySQL数据库
mysql -u root -p

# 选择数据库
USE iras;

# 执行SQL脚本
SOURCE sql/bi_platform_schema.sql;

# 验证表创建
SHOW TABLES LIKE 'bi_%';

# 查看表结构
DESC bi_datasource;
DESC bi_dataset;
DESC bi_visualization;
DESC bi_dashboard;
DESC bi_extract_data;
DESC bi_audit_log;
```

### 2. 生产环境

```bash
# 备份现有数据库
mysqldump -u root -p iras > iras_backup_$(date +%Y%m%d_%H%M%S).sql

# 执行SQL脚本
mysql -u root -p iras < sql/bi_platform_schema.sql

# 验证表创建
mysql -u root -p iras -e "SHOW TABLES LIKE 'bi_%';"
```

## 数据库配置

确保MySQL配置满足以下要求：

- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB
- **MySQL版本**: 8.0+

## 注意事项

1. **外键约束**: 
   - 数据源被数据集使用时不能删除 (RESTRICT)
   - 数据集被可视化使用时不能删除 (RESTRICT)
   - 数据集删除时会级联删除抽取数据 (CASCADE)

2. **JSON字段**:
   - `config`, `query_config`, `field_config`等字段存储JSON格式数据
   - 应用层负责JSON的序列化和反序列化
   - 敏感信息(如密码)需要加密后存储

3. **索引优化**:
   - 所有外键字段都建立了索引
   - 常用查询字段(type, status, create_time)建立了索引
   - 根据实际查询模式可能需要添加复合索引

4. **性能考虑**:
   - `bi_extract_data`表可能存储大量数据，建议定期归档
   - 对于超大数据集，考虑按dataset_id分表存储
   - `bi_audit_log`表建议定期清理历史数据

5. **安全性**:
   - 数据源密码必须加密存储
   - 审计日志记录所有敏感操作
   - 通过若依框架的权限系统控制访问

## 后续步骤

1. 创建领域实体类 (Task 1.2)
2. 创建MyBatis Mapper接口和XML (Task 1.3)
3. 实现数据源管理功能 (阶段2)

## 参考文档

- [需求文档](.kiro/specs/bi-platform-upgrade/requirements.md)
- [设计文档](.kiro/specs/bi-platform-upgrade/design.md)
- [任务列表](.kiro/specs/bi-platform-upgrade/tasks.md)
