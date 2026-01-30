# 查询条件配置面板 - 数据库迁移说明

## 概述

本文档说明了为支持查询条件配置面板功能而进行的数据库模式扩展和实体类更新。

## 数据库变更

### 1. bi_query_condition 表

**新增字段:**
- `component_id` (BIGINT) - 组件ID
  - 允许为 NULL（保持向后兼容性）
  - 外键约束: 引用 `bi_dashboard_component(id)` ON DELETE CASCADE
  - 索引: `idx_component_id`

**变更原因:**
- 支持将查询条件与特定组件关联，而不仅仅是仪表板级别
- 实现组件级别的查询条件隔离
- 满足需求 10.1, 10.2

**迁移脚本:**
```sql
ALTER TABLE bi_query_condition 
ADD COLUMN component_id BIGINT COMMENT '组件ID' AFTER dashboard_id;

ALTER TABLE bi_query_condition 
ADD CONSTRAINT fk_query_condition_component 
FOREIGN KEY (component_id) REFERENCES bi_dashboard_component(id) ON DELETE CASCADE;

ALTER TABLE bi_query_condition 
ADD INDEX idx_component_id (component_id);
```

### 2. bi_condition_mapping 表

**新增字段:**
- `table_name` (VARCHAR(100)) - 表名
  - 允许为 NULL（保持向后兼容性）
  - 索引: `idx_table_name`

**变更原因:**
- 支持字段映射时指定具体的数据表
- 允许同一字段名在不同表中的映射
- 满足需求 2.1

**迁移脚本:**
```sql
ALTER TABLE bi_condition_mapping 
ADD COLUMN table_name VARCHAR(100) COMMENT '表名' AFTER component_id;

ALTER TABLE bi_condition_mapping 
ADD INDEX idx_table_name (table_name);
```

## 实体类变更

### 1. QueryCondition.java

**新增字段:**
```java
/** 组件ID */
private Long componentId;
```

**新增方法:**
```java
public void setComponentId(Long componentId)
public Long getComponentId()
```

**更新方法:**
- `toString()` - 包含 componentId 字段

### 2. ConditionMapping.java

**新增字段:**
```java
/** 表名 */
private String tableName;
```

**新增方法:**
```java
public void setTableName(String tableName)
public String getTableName()
```

**更新方法:**
- `toString()` - 包含 tableName 字段

## MyBatis Mapper 变更

### 1. QueryConditionMapper.xml

**更新内容:**
- `QueryConditionResult` resultMap: 添加 `component_id` 映射
- `selectConditionVo` SQL: 添加 `component_id` 列
- `selectConditionList`: 添加 `componentId` 查询条件
- `insertCondition`: 添加 `component_id` 插入逻辑
- `updateCondition`: 添加 `component_id` 更新逻辑

### 2. ConditionMappingMapper.xml

**更新内容:**
- `ConditionMappingResult` resultMap: 添加 `table_name` 映射
- `selectMappingVo` SQL: 添加 `table_name` 列
- `selectMappingList`: 添加 `tableName` 查询条件
- `selectMappingByDashboardId`: 添加 `table_name` 列
- `insertMapping`: 添加 `table_name` 插入逻辑
- `updateMapping`: 添加 `table_name` 更新逻辑

## 迁移步骤

### 1. 备份数据库
```bash
mysqldump -u [username] -p [database_name] > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. 执行迁移脚本
```bash
mysql -u [username] -p [database_name] < sql/query_condition_config_panel_migration.sql
```

### 3. 验证迁移
```sql
-- 检查 bi_query_condition 表结构
DESC bi_query_condition;

-- 检查 bi_condition_mapping 表结构
DESC bi_condition_mapping;

-- 验证外键约束
SELECT 
    CONSTRAINT_NAME, 
    TABLE_NAME, 
    REFERENCED_TABLE_NAME 
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE 
    TABLE_SCHEMA = '[database_name]' 
    AND TABLE_NAME IN ('bi_query_condition', 'bi_condition_mapping');

-- 验证索引
SHOW INDEX FROM bi_query_condition;
SHOW INDEX FROM bi_condition_mapping;
```

### 4. 部署更新的应用代码
- 确保所有实体类和 Mapper XML 文件已更新
- 重新编译项目: `mvn clean package`
- 重启应用服务器

## 向后兼容性

### 现有数据
- 现有的 `bi_query_condition` 记录的 `component_id` 将为 NULL
- 现有的 `bi_condition_mapping` 记录的 `table_name` 将为 NULL
- 这些记录仍然可以正常工作

### 应用程序
- 代码已更新为支持新字段，但不强制要求
- 查询时会自动处理 NULL 值
- 插入和更新操作使用动态 SQL，只在字段有值时才包含

## 回滚步骤

如果需要回滚迁移:

```sql
-- 1. 删除外键约束
ALTER TABLE bi_query_condition 
DROP FOREIGN KEY fk_query_condition_component;

-- 2. 删除索引
ALTER TABLE bi_query_condition 
DROP INDEX idx_component_id;

ALTER TABLE bi_condition_mapping 
DROP INDEX idx_table_name;

-- 3. 删除列
ALTER TABLE bi_query_condition 
DROP COLUMN component_id;

ALTER TABLE bi_condition_mapping 
DROP COLUMN table_name;
```

## 验证清单

- [ ] 数据库备份已完成
- [ ] 迁移脚本已成功执行
- [ ] 表结构验证通过
- [ ] 外键约束已创建
- [ ] 索引已创建
- [ ] 实体类已更新
- [ ] Mapper XML 已更新
- [ ] 代码编译无错误
- [ ] 应用程序已重启
- [ ] 基本功能测试通过

## 相关需求

- **需求 10.1**: 与仪表板组件集成 - 为组件打开配置面板时加载特定条件
- **需求 10.2**: 与仪表板组件集成 - 保存查询条件时与组件 ID 关联
- **需求 2.1**: 字段关联配置 - 在中间面板中显示可用的表和字段

## 注意事项

1. **数据完整性**: 外键约束确保引用完整性，删除组件时会级联删除相关查询条件
2. **性能**: 新增索引提高了按 component_id 和 table_name 查询的性能
3. **NULL 值处理**: 应用程序代码应正确处理 NULL 值情况
4. **测试**: 在生产环境执行前，请在测试环境充分测试

## 后续任务

完成此任务后，可以继续执行以下任务:
- 任务 2: 实现后端 DTO 和验证逻辑
- 任务 3: 实现后端服务层方法
- 任务 4: 实现后端 Mapper 和 XML
- 任务 5: 实现后端 Controller 端点
