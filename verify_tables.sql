-- 验证指标血缘表的创建
SELECT
    TABLE_NAME as '表名',
    TABLE_ROWS as '行数',
    CREATE_TIME as '创建时间',
    TABLE_COMMENT as '说明'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'iras'
  AND TABLE_NAME LIKE 'bi_metric%'
  AND TABLE_NAME NOT LIKE 'bi_%'
ORDER BY TABLE_NAME;
