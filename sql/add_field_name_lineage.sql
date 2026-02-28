-- ========================================
-- 为字段名风格的指标创建血缘关系
-- ========================================

USE ry-vue;
SET NAMES utf8mb4;

-- 关注类不良贷款比率（字段名风格）的血缘
INSERT INTO bi_metric_lineage (upstream_metric_id, downstream_metric_id, transformation_type, transformation_logic, dependency_strength, create_time)
VALUES
  (10, 19, 'calculation', '作为分子计算关注类不良贷款比率', 5, NOW()),
  (4, 19, 'calculation', '作为分母计算关注类不良贷款比率', 5, NOW());

-- 逾期90天以上贷款比率（字段名风格）的血缘
INSERT INTO bi_metric_lineage (upstream_metric_id, downstream_metric_id, transformation_type, transformation_logic, dependency_strength, create_time)
VALUES
  (12, 20, 'calculation', '作为分子计算逾期90天以上贷款比率', 5, NOW()),
  (4, 20, 'calculation', '作为分母计算逾期90天以上贷款比率', 5, NOW());

-- 不良贷款率（字段名风格）的血缘
INSERT INTO bi_metric_lineage (upstream_metric_id, downstream_metric_id, transformation_type, transformation_logic, dependency_strength, create_time)
VALUES
  (5, 21, 'calculation', '作为分子计算不良贷款率', 5, NOW()),
  (4, 21, 'calculation', '作为分母计算不良贷款率', 5, NOW());

-- 查看创建的血缘关系
SELECT
    ml.id,
    upstream.metric_code AS upstream_code,
    downstream.metric_code AS downstream_code,
    ml.transformation_type
FROM bi_metric_lineage ml
JOIN bi_metric_metadata upstream ON ml.upstream_metric_id = upstream.id
JOIN bi_metric_metadata downstream ON ml.downstream_metric_id = downstream.id
WHERE downstream.id IN (19, 20, 21)
ORDER BY downstream.id;
