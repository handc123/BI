-- ========================================
-- 不良贷款率指标血缘测试数据
-- 基于 non_performing_loan_data 表
-- ========================================

-- 使用 ry-vue 数据库
USE ry-vue;

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 说明：创建完整的银行不良贷款指标血缘体系
-- 血缘链路：
--   不良贷款余额 + 各项贷款余额 → [计算] → 不良贷款率
--   不良贷款率 × 拨备余额 → [计算] → 拨备覆盖率
--   不良贷款余额（按类型）→ [聚合] → 不良贷款总额

-- ========================================
-- 第一步：创建指标元数据
-- ========================================

INSERT INTO bi_metric_metadata (
    metric_code,
    metric_name,
    dataset_id,
    business_definition,
    technical_formula,
    calculation_logic,
    owner_dept,
    data_freshness,
    update_frequency,
    status,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
) VALUES
-- 1. 基础指标：不良贷款余额（次级类）
(
    'NPL_BALANCE_SUBCLASS',
    '不良贷款余额_次级类',
    1,
    '五级分类中的次级类贷款余额总和',
    'SUM(CASE WHEN loan_level = ''次级'' THEN loan_balance ELSE 0 END)',
    '从 non_performing_loan_data 表中筛选贷款等级为"次级"的记录，汇总 loan_balance 字段',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：次级类贷款'
),

-- 2. 基础指标：不良贷款余额（可疑类）
(
    'NPL_BALANCE_DOUBTFUL',
    '不良贷款余额_可疑类',
    1,
    '五级分类中的可疑类贷款余额总和',
    'SUM(CASE WHEN loan_level = ''可疑'' THEN loan_balance ELSE 0 END)',
    '从 non_performing_loan_data 表中筛选贷款等级为"可疑"的记录，汇总 loan_balance 字段',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：可疑类贷款'
),

-- 3. 基础指标：不良贷款余额（损失类）
(
    'NPL_BALANCE_LOSS',
    '不良贷款余额_损失类',
    1,
    '五级分类中的损失类贷款余额总和',
    'SUM(CASE WHEN loan_level = ''损失'' THEN loan_balance ELSE 0 END)',
    '从 non_performing_loan_data 表中筛选贷款等级为"损失"的记录，汇总 loan_balance 字段',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：损失类贷款'
),

-- 4. 基础指标：各项贷款余额
(
    'TOTAL_LOAN_BALANCE',
    '各项贷款余额',
    1,
    '所有类别贷款的余额总和（正常+关注+次级+可疑+损失）',
    'SUM(loan_balance)',
    '从 non_performing_loan_data 表中汇总所有贷款的 loan_balance 字段',
    '信贷管理部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：总贷款余额'
),

-- 5. 聚合指标：不良贷款余额（总计）
(
    'NPL_BALANCE_TOTAL',
    '不良贷款余额_总计',
    1,
    '次级+可疑+损失三类不良贷款余额总和',
    'NPL_BALANCE_SUBCLASS + NPL_BALANCE_DOUBTFUL + NPL_BALANCE_LOSS',
    '将三个等级的不良贷款余额相加得到不良贷款总额',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '聚合指标：不良贷款总额'
),

-- 6. 衍生指标：不良贷款率（核心指标）
(
    'NPL_RATIO',
    '不良贷款率',
    1,
    '不良贷款余额 / 各项贷款余额 × 100%',
    '(NPL_BALANCE_TOTAL / TOTAL_LOAN_BALANCE) * 100',
    '不良贷款率 = (不良贷款余额 / 各项贷款余额) × 100%，用于衡量银行资产质量，监管要求一般不超过5%',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '核心监管指标：不良贷款率'
),

-- 7. 基础指标：贷款拨备余额
(
    'PROVISION_BALANCE',
    '贷款拨备余额',
    1,
    '为不良贷款计提的拨备金额总和',
    'SUM(provision_amount)',
    '从 non_performing_loan_data 表中汇总 provision_amount 字段',
    '财务部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：拨备余额'
),

-- 8. 衍生指标：拨备覆盖率
(
    'PROVISION_COVERAGE_RATIO',
    '拨备覆盖率',
    1,
    '贷款拨备余额 / 不良贷款余额 × 100%',
    '(PROVISION_BALANCE / NPL_BALANCE_TOTAL) * 100',
    '拨备覆盖率 = (贷款拨备余额 / 不良贷款余额) × 100%，监管要求一般不低于150%，反映银行对不良贷款的风险抵御能力',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '监管指标：拨备覆盖率'
),

-- 9. 衍生指标：不良贷款率变化
(
    'NPL_RATIO_CHANGE',
    '不良贷款率变化',
    1,
    '本期不良贷款率 - 上期不良贷款率',
    'NPL_RATIO_CURRENT - NPL_RATIO_PREVIOUS',
    '计算不良贷款率的环比变化，用于监测资产质量趋势',
    '风险管理部',
    'T-1',
    '每周',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '趋势指标：环比变化'
);

-- ========================================
-- 第二步：查看创建的指标
-- ========================================

SELECT
    id,
    metric_code,
    metric_name,
    business_definition,
    technical_formula,
    owner_dept
FROM bi_metric_metadata
WHERE metric_code LIKE 'NPL_%' OR metric_code IN ('TOTAL_LOAN_BALANCE', 'PROVISION_BALANCE')
ORDER BY metric_code;

-- ========================================
-- 第三步：创建血缘关系
-- ========================================

-- 血缘1：次级类不良贷款 → 不良贷款总额（聚合）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_SUBCLASS'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_TOTAL'),
    'aggregation',
    '将次级类不良贷款余额加入不良贷款总额',
    5,
    NOW()
);

-- 血缘2：可疑类不良贷款 → 不良贷款总额（聚合）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_DOUBTFUL'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_TOTAL'),
    'aggregation',
    '将可疑类不良贷款余额加入不良贷款总额',
    5,
    NOW()
);

-- 血缘3：损失类不良贷款 → 不良贷款总额（聚合）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_LOSS'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_TOTAL'),
    'aggregation',
    '将损失类不良贷款余额加入不良贷款总额',
    5,
    NOW()
);

-- 血缘4：不良贷款总额 → 不良贷款率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_TOTAL'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO'),
    'calculation',
    '作为分子计算不良贷款率：不良贷款率 = (不良贷款总额 / 各项贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘5：各项贷款余额 → 不良贷款率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'TOTAL_LOAN_BALANCE'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO'),
    'calculation',
    '作为分母计算不良贷款率：不良贷款率 = (不良贷款总额 / 各项贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘6：拨备余额 → 拨备覆盖率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'PROVISION_BALANCE'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'PROVISION_COVERAGE_RATIO'),
    'calculation',
    '作为分子计算拨备覆盖率：拨备覆盖率 = (拨备余额 / 不良贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘7：不良贷款总额 → 拨备覆盖率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_BALANCE_TOTAL'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'PROVISION_COVERAGE_RATIO'),
    'calculation',
    '作为分母计算拨备覆盖率：拨备覆盖率 = (拨备余额 / 不良贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘8：不良贷款率 → 不良贷款率变化（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO_CHANGE'),
    'calculation',
    '计算环比变化：本期不良贷款率 - 上期不良贷款率',
    3,
    NOW()
);

-- ========================================
-- 第四步：查看血缘关系
-- ========================================

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
WHERE upstream.metric_code LIKE 'NPL_%'
   OR upstream.metric_code IN ('TOTAL_LOAN_BALANCE', 'PROVISION_BALANCE')
   OR downstream.metric_code LIKE 'NPL_%'
   OR downstream.metric_code = 'PROVISION_COVERAGE_RATIO'
ORDER BY downstream.metric_code, ml.dependency_strength DESC;

-- ========================================
-- 血缘图谱可视化（文本版）
-- ========================================
--
--                        [各项贷款余额]
--                             |
--                             +-----+
--                                   |
--                                   v
-- [次级] ----+                   [不良贷款率] <---- [不良贷款率变化]
--            |                        |
-- [可疑] ----+-----> [不良贷款总额] ---+
--            |                        |
-- [损失] ----+                        +-----> [拨备覆盖率]
--                                      |
--                           [拨备余额]
--
-- 节点说明：
-- - [次级][可疑][损失]：基础指标，从数据表汇总
-- - [不良贷款总额]：聚合指标，由三个次级指标聚合而成
-- - [各项贷款余额]：基础指标，从数据表汇总
-- - [不良贷款率]：衍生指标，核心监管指标
-- - [拨备余额]：基础指标，从数据表汇总
-- - [拨备覆盖率]：衍生指标，监管指标
-- - [不良贷款率变化]：衍生指标，趋势监测
--
-- ========================================

-- ========================================
-- 第五步：为图表配置指标ID
-- ========================================
-- 说明：将不良贷款率指标（NPL_RATIO）关联到图表组件
-- 使用方法：取消下面的注释，替换 YOUR_COMPONENT_ID 为实际的组件ID
-- ========================================

-- 查看所有图表组件
-- SELECT id, component_name, data_config FROM bi_dashboard_component;

-- 为不良贷款率图表关联指标ID
-- UPDATE bi_dashboard_component
-- SET data_config = JSON_SET(
--     COALESCE(data_config, '{}'),
--     '$.metricId',
--     (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO')
-- )
-- WHERE id = YOUR_COMPONENT_ID;

-- ========================================
-- 验证脚本：检查数据是否正确创建
-- ========================================

-- 检查指标数量
SELECT
    '指标总数' as item,
    COUNT(*) as count
FROM bi_metric_metadata
WHERE metric_code LIKE 'NPL_%' OR metric_code IN ('TOTAL_LOAN_BALANCE', 'PROVISION_BALANCE')

UNION ALL

-- 检查血缘关系数量
SELECT
    '血缘关系数' as item,
    COUNT(*) as count
FROM bi_metric_lineage
WHERE upstream_metric_id IN (
    SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'NPL_%' OR metric_code IN ('TOTAL_LOAN_BALANCE', 'PROVISION_BALANCE')
);

-- ========================================
-- 完成提示
-- ========================================

SELECT '✅ 不良贷款指标血缘数据创建完成！' as message;
SELECT '📊 共创建 9 个指标，8 条血缘关系' as summary;
SELECT '🔍 下一步：为图表组件配置 metricId' as next_step;
SELECT '💡 参考：SELECT id, metric_code, metric_name FROM bi_metric_metadata WHERE metric_code LIKE ''NPL_%'';' as query_example;
