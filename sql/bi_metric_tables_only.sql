-- =============================================
-- IRAS Smart BI - Metric Lineage Tables
-- Simplified Schema - Tables Only (No Sample Data)
-- Version: 1.2
-- Date: 2025-02-26
-- =============================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Metric Metadata Table
DROP TABLE IF EXISTS `bi_metric_metadata`;
CREATE TABLE `bi_metric_metadata` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `metric_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '业务唯一编码',
    `metric_name` VARCHAR(100) NOT NULL COMMENT '指标名称',
    `dataset_id` BIGINT COMMENT '关联数据集ID',
    `visualization_id` BIGINT COMMENT '关联可视化ID',
    `business_definition` TEXT COMMENT '业务定义',
    `technical_formula` TEXT COMMENT '技术计算公式',
    `calculation_logic` TEXT COMMENT '计算逻辑说明',
    `owner_dept` VARCHAR(64) COMMENT '责任部门',
    `dept_id` BIGINT COMMENT '所属部门ID(用于行级权限控制)',
    `data_freshness` VARCHAR(20) COMMENT '数据新鲜度(T-1,RT)',
    `update_frequency` VARCHAR(20) COMMENT '更新频率',
    `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` TEXT COMMENT '备注',
    INDEX `idx_metric_code` (`metric_code`),
    INDEX `idx_dataset` (`dataset_id`),
    INDEX `idx_visualization` (`visualization_id`),
    INDEX `idx_dept` (`dept_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标元数据表';

-- 2. Metric Lineage Table
DROP TABLE IF EXISTS `bi_metric_lineage`;
CREATE TABLE `bi_metric_lineage` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `upstream_metric_id` BIGINT NOT NULL COMMENT '上游指标ID',
    `downstream_metric_id` BIGINT NOT NULL COMMENT '下游指标ID',
    `transformation_type` VARCHAR(50) COMMENT '转换类型:aggregation,calculation,filter',
    `transformation_logic` TEXT COMMENT '转换逻辑说明',
    `dependency_strength` INT DEFAULT 1 COMMENT '依赖强度1-5',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_upstream` (`upstream_metric_id`),
    INDEX `idx_downstream` (`downstream_metric_id`),
    INDEX `idx_upstream_downstream` (`upstream_metric_id`, `downstream_metric_id`),
    CONSTRAINT `fk_lineage_upstream` FOREIGN KEY (`upstream_metric_id`) REFERENCES `bi_metric_metadata` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_lineage_downstream` FOREIGN KEY (`downstream_metric_id`) REFERENCES `bi_metric_metadata` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标血缘关系表';

-- 3. Lineage Node Table
DROP TABLE IF EXISTS `bi_lineage_node`;
CREATE TABLE `bi_lineage_node` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `node_type` VARCHAR(20) NOT NULL COMMENT '节点类型:metric,dataset,datasource,table,field',
    `node_id` BIGINT NOT NULL COMMENT '节点业务ID',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `node_attributes` TEXT COMMENT '节点属性(JSON格式)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_node_type_id` (`node_type`, `node_id`),
    UNIQUE KEY `uk_node` (`node_type`, `node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='血缘节点表';

-- 4. Lineage Edge Table
DROP TABLE IF EXISTS `bi_lineage_edge`;
CREATE TABLE `bi_lineage_edge` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `source_node_id` BIGINT NOT NULL COMMENT '源节点ID',
    `target_node_id` BIGINT NOT NULL COMMENT '目标节点ID',
    `edge_type` VARCHAR(50) COMMENT '边类型:flows_to,derived_from,depends_on',
    `edge_attributes` TEXT COMMENT '边属性(JSON格式)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_source` (`source_node_id`),
    INDEX `idx_target` (`target_node_id`),
    CONSTRAINT `fk_edge_source` FOREIGN KEY (`source_node_id`) REFERENCES `bi_lineage_node` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_edge_target` FOREIGN KEY (`target_node_id`) REFERENCES `bi_lineage_node` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='血缘边表';

-- 5. Metric Data Query Templates Table
DROP TABLE IF EXISTS `bi_metric_data_query`;
CREATE TABLE `bi_metric_data_query` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `metric_id` BIGINT NOT NULL COMMENT '指标ID',
    `query_name` VARCHAR(100) COMMENT '查询名称',
    `query_template` TEXT COMMENT 'SQL查询模板,使用{{param}}作为参数占位符',
    `default_parameters` JSON COMMENT '默认参数配置(JSON格式)',
    `default_filters` TEXT COMMENT '默认过滤条件(JSON格式)',
    `required_permissions` VARCHAR(500) COMMENT '所需权限(逗号分隔)',
    `row_limit` INT DEFAULT 1000 COMMENT '默认行数限制',
    `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_metric` (`metric_id`),
    INDEX `idx_metric_status` (`metric_id`, `status`),
    CONSTRAINT `fk_query_metric` FOREIGN KEY (`metric_id`) REFERENCES `bi_metric_metadata` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标数据查询模板表';

-- =============================================
-- Composite Indexes for Performance
-- =============================================

CREATE INDEX `idx_metric_dept_status` ON `bi_metric_metadata` (`dept_id`, `status`, `del_flag`);
CREATE INDEX `idx_metric_dataset_status` ON `bi_metric_metadata` (`dataset_id`, `status`, `del_flag`);
CREATE INDEX `idx_metric_status_code` ON `bi_metric_metadata` (`status`, `metric_code`, `del_flag`);

-- =============================================
-- View for Simplified Lineage Graph Queries
-- =============================================

CREATE OR REPLACE VIEW `v_metric_lineage_graph` AS
SELECT
    ml.id AS edge_id,
    n1.id AS source_node_id,
    n1.node_type AS source_type,
    n1.node_name AS source_name,
    n2.id AS target_node_id,
    n2.node_type AS target_type,
    n2.node_name AS target_name,
    ml.transformation_type,
    ml.transformation_logic,
    ml.dependency_strength
FROM `bi_metric_lineage` ml
INNER JOIN `bi_lineage_node` n1 ON n1.node_type = 'metric' AND n1.node_id = ml.upstream_metric_id
INNER JOIN `bi_lineage_node` n2 ON n2.node_type = 'metric' AND n2.node_id = ml.downstream_metric_id;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Tables created successfully!' as status;
