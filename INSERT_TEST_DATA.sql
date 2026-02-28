-- ========================================
-- 指标血缘功能测试数据
-- ========================================

-- 1. 清理旧测试数据（可选）
-- DELETE FROM bi_metric_lineage WHERE downstream_metric_id IN (SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'TEST_%');
-- DELETE FROM bi_metric_metadata WHERE metric_code LIKE 'TEST_%';

-- 2. 插入指标元数据（3个测试指标）
INSERT INTO bi_metric_metadata (metric_code, metric_name, dataset_id, business_definition, technical_formula, calculation_logic, owner_dept, data_freshness, update_frequency, status, create_by, create_time, update_by, update_time, remark) VALUES
('TEST_001', '原销售额', 1, '每日原始销售数据，直接从订单表汇总', 'SUM(order_amount)', '每日从销售订单表汇总订单金额，按日期聚合', '销售部', 'T-1', '每日', '0', 'admin', NOW(), 'admin', NOW(), '基础指标'),
('TEST_002', '月度销售额', 1, '基于原销售额按月聚合', 'SUM(TEST_001)', '将每日销售额按月度汇总聚合', '财务部', 'T-1', '每月', '0', 'admin', NOW(), 'admin', NOW(), '聚合指标'),
('TEST_003', '销售达成率', 1, '月度销售额与目标销售额的比率', '(TEST_002 / target_amount) * 100', '计算月度实际销售额占目标销售额的百分比', '运营部', 'T-1', '每月', '0', 'admin', NOW(), 'admin', NOW(), '衍生指标');

-- 3. 插入血缘关系
-- TEST_001 -> TEST_002 （聚合关系）
INSERT INTO bi_metric_lineage (upstream_metric_id, downstream_metric_id, transformation_type, transformation_logic, dependency_strength, create_time) VALUES
((SELECT id FROM bi_metric_metadata WHERE metric_code = 'TEST_001'),
 (SELECT id FROM bi_metric_metadata WHERE metric_code = 'TEST_002'),
 'aggregation',
 '按月度汇总聚合',
 5,
 NOW());

-- TEST_002 -> TEST_003 （计算关系）
INSERT INTO bi_metric_lineage (upstream_metric_id, downstream_metric_id, transformation_type, transformation_logic, dependency_strength, create_time) VALUES
((SELECT id FROM bi_metric_metadata WHERE metric_code = 'TEST_002'),
 (SELECT id FROM bi_metric_metadata WHERE metric_code = 'TEST_003'),
 'calculation',
 '计算达成率 = (实际销售额 / 目标销售额) * 100',
 3,
 NOW());

-- 4. 查看插入结果
SELECT
    mm.id,
    mm.metric_code,
    mm.metric_name,
    mm.business_definition,
    mm.technical_formula,
    mm.owner_dept
FROM bi_metric_metadata mm
WHERE mm.metric_code LIKE 'TEST_%'
ORDER BY mm.metric_code;

SELECT
    ml.id,
    upstream.metric_code AS upstream_code,
    upstream.metric_name AS upstream_name,
    downstream.metric_code AS downstream_code,
    downstream.metric_name AS downstream_name,
    ml.transformation_type,
    ml.transformation_logic,
    ml.dependency_strength
FROM bi_metric_lineage ml
JOIN bi_metric_metadata upstream ON ml.upstream_metric_id = upstream.id
JOIN bi_metric_metadata downstream ON ml.downstream_metric_id = downstream.id
WHERE upstream.metric_code LIKE 'TEST_%' OR downstream.metric_code LIKE 'TEST_%';

-- ========================================
-- 数据血缘链路：
--
--   TEST_001 (原销售额)
--        ↓ [aggregation: 按月度汇总]
--   TEST_002 (月度销售额)
--        ↓ [calculation: 计算达成率]
--   TEST_003 (销售达成率)
--
-- ========================================

-- 5. 为图表组件配置 metricId
-- 假设要为图表ID=123的组件关联指标TEST_001，执行：
-- UPDATE bi_dashboard_component SET data_config = JSON_SET(data_config, '$.metricId', (SELECT id FROM bi_metric_metadata WHERE metric_code = 'TEST_001')) WHERE id = 123;
