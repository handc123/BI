-- 清理旧的指标血缘表
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `bi_metric_data_query`;
DROP TABLE IF EXISTS `bi_lineage_edge`;
DROP TABLE IF EXISTS `bi_lineage_node`;
DROP TABLE IF EXISTS `bi_metric_lineage`;
DROP TABLE IF EXISTS `bi_metric_metadata`;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Tables dropped successfully' as status;
