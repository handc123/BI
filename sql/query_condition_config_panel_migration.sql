-- =============================================
-- 查询条件配置面板数据库迁移脚本
-- =============================================

-- 1. 为 bi_query_condition 表添加 component_id 列
ALTER TABLE bi_query_condition 
ADD COLUMN component_id BIGINT COMMENT '组件ID' AFTER dashboard_id;

-- 添加外键约束
ALTER TABLE bi_query_condition 
ADD CONSTRAINT fk_query_condition_component 
FOREIGN KEY (component_id) REFERENCES bi_dashboard_component(id) ON DELETE CASCADE;

-- 添加索引
ALTER TABLE bi_query_condition 
ADD INDEX idx_component_id (component_id);

-- 2. 为 bi_condition_mapping 表添加 table_name 列
ALTER TABLE bi_condition_mapping 
ADD COLUMN table_name VARCHAR(100) COMMENT '表名' AFTER component_id;

-- 添加索引
ALTER TABLE bi_condition_mapping 
ADD INDEX idx_table_name (table_name);

-- 注意: 
-- 1. 此脚本会修改现有表结构,请在执行前备份数据库
-- 2. component_id 字段允许为 NULL,以保持向后兼容性
-- 3. table_name 字段允许为 NULL,以保持向后兼容性
-- 4. 执行此脚本后,需要更新应用程序代码以使用新字段
