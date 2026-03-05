-- ============================================================
-- 增强数据配置功能 - 添加指标配置字段
-- 创建时间: 2024-01-20
-- 说明: 为 bi_dataset 表添加 config 字段用于存储指标配置(JSON格式)
-- ============================================================

-- 检查字段是否已存在,如果不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'bi_dataset';
SET @columnname = 'config';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TEXT COMMENT ''指标配置(JSON格式)'' AFTER field_config')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================================
-- 字段说明
-- ============================================================
-- config: TEXT类型,用于存储指标配置的JSON数据
--   - 支持大型JSON对象(最大64KB)
--   - 包含基础指标和计算指标的完整配置
--   - 格式参考 MetricConfigDTO.java
--
-- 示例JSON结构:
-- {
--   "baseMetrics": [
--     {
--       "name": "total_loan_amount",
--       "alias": "贷款总额",
--       "field": "loan_amount",
--       "aggregation": "SUM"
--     }
--   ],
--   "computedMetrics": [
--     {
--       "name": "npl_ratio",
--       "alias": "不良贷款率",
--       "computeType": "conditional_ratio",
--       "field": "loan_amount",
--       "numeratorCondition": "loan_status = 'NPL'",
--       "denominatorCondition": "loan_status IN ('NORMAL', 'NPL')",
--       "asPercentage": true
--     }
--   ]
-- }
-- ============================================================

-- 验证字段已添加
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'bi_dataset'
  AND COLUMN_NAME = 'config';

-- ============================================================
-- 执行说明
-- ============================================================
-- 1. 此脚本是幂等的,可以安全地多次执行
-- 2. 如果字段已存在,脚本不会重复添加
-- 3. 建议在测试环境先执行并验证
-- 4. 生产环境执行前请做好数据备份
-- 5. 执行命令: mysql -u root -p iras < sql/add_metric_config_field.sql
