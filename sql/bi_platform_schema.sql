-- BI平台升级 - 数据库表结构
-- 创建时间: 2024-01-15
-- 说明: 此脚本创建BI平台所需的所有元数据表和抽取数据表

-- ============================================================
-- 1. bi_datasource (数据源表)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_datasource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据源ID',
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    type VARCHAR(20) NOT NULL COMMENT '数据源类型: mysql, postgresql, clickhouse, doris, oracle, api, file',
    config TEXT NOT NULL COMMENT '连接配置(JSON格式,加密存储)',
    status CHAR(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BI数据源表';

-- ============================================================
-- 2. bi_dataset (数据集表)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_dataset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据集ID',
    name VARCHAR(100) NOT NULL COMMENT '数据集名称',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    type VARCHAR(20) NOT NULL COMMENT '数据集类型: direct, extract',
    query_config TEXT NOT NULL COMMENT '查询配置(JSON格式)',
    field_config TEXT COMMENT '字段配置(JSON格式)',
    extract_config TEXT COMMENT '抽取配置(JSON格式,仅抽取类型)',
    last_extract_time DATETIME DEFAULT NULL COMMENT '最后抽取时间',
    row_count BIGINT DEFAULT 0 COMMENT '数据行数',
    status CHAR(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    INDEX idx_datasource (datasource_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_dataset_datasource FOREIGN KEY (datasource_id) REFERENCES bi_datasource(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BI数据集表';

-- ============================================================
-- 3. bi_visualization (可视化表)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_visualization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '可视化ID',
    name VARCHAR(100) NOT NULL COMMENT '可视化名称',
    dataset_id BIGINT NOT NULL COMMENT '数据集ID',
    type VARCHAR(20) NOT NULL COMMENT '图表类型: kpi, line, bar, map, table, pie, donut, funnel',
    config TEXT NOT NULL COMMENT '可视化配置(JSON格式)',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    INDEX idx_dataset (dataset_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_visualization_dataset FOREIGN KEY (dataset_id) REFERENCES bi_dataset(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BI可视化表';

-- ============================================================
-- 4. bi_dashboard (仪表板表)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_dashboard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仪表板ID',
    name VARCHAR(100) NOT NULL COMMENT '仪表板名称',
    layout_config TEXT NOT NULL COMMENT '布局配置(JSON格式)',
    filter_config TEXT COMMENT '全局筛选器配置(JSON格式)',
    theme_config TEXT COMMENT '主题配置(JSON格式)',
    status CHAR(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    INDEX idx_status (status),
    INDEX idx_create_by (create_by),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BI仪表板表';

-- ============================================================
-- 5. bi_extract_data (抽取数据表)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_extract_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    dataset_id BIGINT NOT NULL COMMENT '数据集ID',
    data_content TEXT NOT NULL COMMENT '数据内容(JSON格式,包含所有字段)',
    extract_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抽取时间',
    INDEX idx_dataset (dataset_id),
    INDEX idx_extract_time (extract_time),
    CONSTRAINT fk_extract_dataset FOREIGN KEY (dataset_id) REFERENCES bi_dataset(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BI抽取数据表';

-- ============================================================
-- 6. bi_audit_log (审计日志表)
-- ============================================================
CREATE TABLE IF NOT EXISTS bi_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    user_name VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型: datasource, dataset, dashboard, visualization',
    resource_id BIGINT DEFAULT NULL COMMENT '资源ID',
    operation_detail TEXT COMMENT '操作详情(JSON格式)',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user (user_id),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_time (create_time),
    INDEX idx_operation (operation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BI审计日志表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 插入示例数据源配置(可选,用于测试)
-- INSERT INTO bi_datasource (name, type, config, status, create_by, remark) VALUES
-- ('本地MySQL', 'mysql', '{"host":"localhost","port":3306,"database":"iras","username":"root","password":"encrypted_password","connectionPool":{"minConnections":2,"maxConnections":10,"connectionTimeout":30000}}', '0', 'admin', '本地MySQL数据源');

-- ============================================================
-- 表结构说明
-- ============================================================
-- 1. bi_datasource: 存储所有数据源配置,支持MySQL、PostgreSQL、ClickHouse、Doris、Oracle、API和文件类型
-- 2. bi_dataset: 存储数据集定义,包括直连和抽取两种类型,关联到数据源
-- 3. bi_visualization: 存储可视化组件配置,支持多种图表类型,关联到数据集
-- 4. bi_dashboard: 存储仪表板布局和配置,包含多个可视化组件
-- 5. bi_extract_data: 存储抽取数据集的实际数据,以JSON格式存储
-- 6. bi_audit_log: 记录所有BI平台操作的审计日志

-- ============================================================
-- 外键约束说明
-- ============================================================
-- 1. bi_dataset.datasource_id -> bi_datasource.id (RESTRICT: 数据源被使用时不能删除)
-- 2. bi_visualization.dataset_id -> bi_dataset.id (RESTRICT: 数据集被使用时不能删除)
-- 3. bi_extract_data.dataset_id -> bi_dataset.id (CASCADE: 数据集删除时级联删除抽取数据)

-- ============================================================
-- 索引说明
-- ============================================================
-- 1. 所有表都有主键索引(id)
-- 2. 类型字段(type, status)建立索引以支持快速筛选
-- 3. 外键字段建立索引以提高JOIN性能
-- 4. 时间字段(create_time, extract_time)建立索引以支持时间范围查询
-- 5. 审计日志表的user_id和resource相关字段建立索引以支持审计查询

-- ============================================================
-- 字符集和排序规则
-- ============================================================
-- 使用utf8mb4字符集和utf8mb4_unicode_ci排序规则,支持完整的Unicode字符集(包括emoji)

-- ============================================================
-- 存储引擎
-- ============================================================
-- 使用InnoDB存储引擎,支持事务、外键约束和行级锁定

-- ============================================================
-- 执行说明
-- ============================================================
-- 1. 在执行此脚本前,请确保已连接到正确的数据库
-- 2. 建议在测试环境先执行并验证
-- 3. 生产环境执行前请做好数据备份
-- 4. 执行命令: mysql -u root -p iras < bi_platform_schema.sql
