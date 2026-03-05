-- ========================================
-- 查询表: t_cbis_t_daikuan_hzwdb 完整信息
-- ========================================

-- 1. 查看表结构
DESC t_cbis_t_daikuan_hzwdb;

-- 2. 查看表的创建语句
SHOW CREATE TABLE t_cbis_t_daikuan_hzwdb\G

-- 3. 查看表的字段详细信息
SELECT 
    COLUMN_NAME as '字段名',
    COLUMN_TYPE as '类型',
    IS_NULLABLE as '可空',
    COLUMN_KEY as '键',
    COLUMN_DEFAULT as '默认值',
    COLUMN_COMMENT as '注释',
    EXTRA as '额外信息'
FROM 
    information_schema.COLUMNS 
WHERE 
    TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 't_cbis_t_daikuan_hzwdb'
ORDER BY 
    ORDINAL_POSITION;

-- 4. 查看表的记录数
SELECT COUNT(*) as '总记录数' FROM t_cbis_t_daikuan_hzwdb;

-- 5. 查看表的前10条数据
SELECT * FROM t_cbis_t_daikuan_hzwdb LIMIT 10;

-- 6. 查看表的索引信息
SHOW INDEX FROM t_cbis_t_daikuan_hzwdb;

-- 7. 查看表的存储引擎和字符集
SELECT 
    TABLE_NAME as '表名',
    ENGINE as '存储引擎',
    TABLE_COLLATION as '字符集',
    TABLE_ROWS as '估计行数',
    AVG_ROW_LENGTH as '平均行长度',
    DATA_LENGTH as '数据大小(字节)',
    INDEX_LENGTH as '索引大小(字节)',
    TABLE_COMMENT as '表注释'
FROM 
    information_schema.TABLES 
WHERE 
    TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 't_cbis_t_daikuan_hzwdb';
