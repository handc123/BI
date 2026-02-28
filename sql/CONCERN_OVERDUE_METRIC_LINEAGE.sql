-- ========================================
-- 关注类不良贷款比率和逾期90天以上贷款比率指标血缘
-- 基于 non_performing_loan_data 表
-- ========================================

-- 使用 ry-vue 数据库
USE ry-vue;

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 说明：创建完整的关注类和逾期90天以上贷款指标血缘体系
-- 血缘链路：
--   关注类贷款余额 + 各项贷款余额 → [计算] → 关注类不良贷款比率
--   逾期90天以上贷款余额 + 各项贷款余额 → [计算] → 逾期90天以上贷款比率
--   关注类不良贷款比率 → [聚合] → 地区平均关注类比率
--   逾期90天以上贷款比率 → [聚合] → 地区平均逾期90天比率

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
    del_flag,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
) VALUES
-- 1. 基础指标：关注类贷款余额
(
    'CONCERN_LOAN_BALANCE',
    '关注类贷款余额',
    1,
    '五级分类中的关注类贷款余额总和。关注类贷款是指尽管借款人目前有能力偿还贷款本息，但存在一些可能对偿还产生不利影响的因素',
    'SUM(CASE WHEN loan_level = ''关注'' THEN loan_balance ELSE 0 END)',
    '从 non_performing_loan_data 表中筛选贷款等级为"关注"的记录，汇总 loan_balance 字段',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：关注类贷款余额'
),

-- 2. 基础指标：各项贷款余额（已存在，这里添加注释说明）
-- 注意：如果 TOTAL_LOAN_BALANCE 已存在，则跳过此条
(
    'TOTAL_LOAN_BALANCE',
    '各项贷款余额',
    1,
    '所有类别贷款的余额总和（正常+关注+次级+可疑+损失）。作为计算各类比率指标的分母',
    'SUM(loan_balance)',
    '从 non_performing_loan_data 表中汇总所有贷款的 loan_balance 字段',
    '信贷管理部',
    'T-1',
    '每日',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：总贷款余额，所有比率指标的分母'
),

-- 3. 衍生指标：关注类不良贷款比率（核心指标）
(
    'CONCERN_NPL_RATIO',
    '关注类不良贷款比率',
    1,
    '关注类贷款余额 / 各项贷款余额 × 100%。该指标反映了关注类贷款占总贷款的比例，是衡量资产质量的重要先行指标。关注类比率上升通常预示着未来不良贷款可能增加',
    '(CONCERN_LOAN_BALANCE / TOTAL_LOAN_BALANCE) * 100',
    '关注类不良贷款比率 = (关注类贷款余额 / 各项贷款余额) × 100%。从 non_performing_loan_data 表的 concern_non_performing_loan_ratio 字段直接获取，或通过计算得出',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '核心监管指标：关注类不良贷款比率，资产质量先行指标'
),

-- 4. 基础指标：逾期90天以上贷款余额
(
    'OVERDUE_90DAYS_BALANCE',
    '逾期90天以上贷款余额',
    1,
    '逾期期限在90天以上（含90天）的贷款余额总和。逾期90天是监管判定不良贷款的重要标准之一',
    'SUM(CASE WHEN overdue_days >= 90 THEN loan_balance ELSE 0 END)',
    '从 non_performing_loan_data 表中筛选逾期天数>=90的记录，汇总 loan_balance 字段',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '基础指标：逾期90天以上贷款余额'
),

-- 5. 衍生指标：逾期90天以上贷款比率（核心指标）
(
    'OVERDUE_90DAYS_RATIO',
    '逾期90天以上贷款比率',
    1,
    '逾期90天以上贷款余额 / 各项贷款余额 × 100%。该指标反映逾期90天以上贷款占总贷款的比例，是衡量贷款逾期严重程度的关键指标。根据监管要求，逾期90天以上通常会被认定为不良贷款',
    '(OVERDUE_90DAYS_BALANCE / TOTAL_LOAN_BALANCE) * 100',
    '逾期90天以上贷款比率 = (逾期90天以上贷款余额 / 各项贷款余额) × 100%。从 non_performing_loan_data 表的 overdue_90days_loan_ratio 字段直接获取，或通过计算得出',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '核心监管指标：逾期90天以上贷款比率'
),

-- 6. 聚合指标：地区平均关注类比率
(
    'REGION_AVG_CONCERN_RATIO',
    '地区平均关注类比率',
    1,
    '按地区聚合的平均关注类不良贷款比率。用于地区间资产质量对比分析',
    'AVG(concern_non_performing_loan_ratio) GROUP BY region_name',
    '从 non_performing_loan_data 表按 region_name 分组，计算 concern_non_performing_loan_ratio 的平均值',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '聚合指标：地区平均关注类比率'
),

-- 7. 聚合指标：地区平均逾期90天比率
(
    'REGION_AVG_OVERDUE_90_RATIO',
    '地区平均逾期90天以上比率',
    1,
    '按地区聚合的平均逾期90天以上贷款比率。用于地区间逾期风险对比分析',
    'AVG(overdue_90days_loan_ratio) GROUP BY region_name',
    '从 non_performing_loan_data 表按 region_name 分组，计算 overdue_90days_loan_ratio 的平均值',
    '风险管理部',
    'T-1',
    '每日',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '聚合指标：地区平均逾期90天以上比率'
),

-- 8. 衍生指标：关注类比率变化
(
    'CONCERN_RATIO_CHANGE',
    '关注类比率变化',
    1,
    '本期关注类不良贷款比率 - 上期关注类不良贷款比率',
    'CONCERN_NPL_RATIO_CURRENT - CONCERN_NPL_RATIO_PREVIOUS',
    '计算关注类比率的环比变化，用于监测资产质量趋势。正值上升表示资产质量恶化，负值下降表示资产质量改善',
    '风险管理部',
    'T-1',
    '每周',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '趋势指标：关注类比率环比变化'
),

-- 9. 衍生指标：逾期90天比率变化
(
    'OVERDUE_90_RATIO_CHANGE',
    '逾期90天以上比率变化',
    1,
    '本期逾期90天以上贷款比率 - 上期逾期90天以上贷款比率',
    'OVERDUE_90DAYS_RATIO_CURRENT - OVERDUE_90DAYS_RATIO_PREVIOUS',
    '计算逾期90天以上比率的环比变化，用于监测逾期风险趋势。正值上升表示逾期风险增加',
    '风险管理部',
    'T-1',
    '每周',
    '0',
    '0',
    'admin',
    NOW(),
    'admin',
    NOW(),
    '趋势指标：逾期90天以上比率环比变化'
)

ON DUPLICATE KEY UPDATE
    metric_name = VALUES(metric_name),
    business_definition = VALUES(business_definition),
    technical_formula = VALUES(technical_formula),
    calculation_logic = VALUES(calculation_logic),
    update_time = NOW();

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
WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%'
ORDER BY metric_code;

-- ========================================
-- 第三步：创建血缘关系
-- ========================================

-- 血缘1：关注类贷款余额 → 关注类不良贷款比率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_LOAN_BALANCE'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_NPL_RATIO'),
    'calculation',
    '作为分子计算关注类不良贷款比率：关注类比率 = (关注类贷款余额 / 各项贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘2：各项贷款余额 → 关注类不良贷款比率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'TOTAL_LOAN_BALANCE'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_NPL_RATIO'),
    'calculation',
    '作为分母计算关注类不良贷款比率：关注类比率 = (关注类贷款余额 / 各项贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘3：逾期90天以上贷款余额 → 逾期90天以上贷款比率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'OVERDUE_90DAYS_BALANCE'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'OVERDUE_90DAYS_RATIO'),
    'calculation',
    '作为分子计算逾期90天以上贷款比率：逾期90天比率 = (逾期90天以上贷款余额 / 各项贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘4：各项贷款余额 → 逾期90天以上贷款比率（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'TOTAL_LOAN_BALANCE'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'OVERDUE_90DAYS_RATIO'),
    'calculation',
    '作为分母计算逾期90天以上贷款比率：逾期90天比率 = (逾期90天以上贷款余额 / 各项贷款余额) × 100%',
    5,
    NOW()
);

-- 血缘5：关注类不良贷款比率 → 地区平均关注类比率（聚合）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_NPL_RATIO'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'REGION_AVG_CONCERN_RATIO'),
    'aggregation',
    '按地区聚合计算平均关注类比率：AVG(concern_non_performing_loan_ratio) GROUP BY region_name',
    4,
    NOW()
);

-- 血缘6：逾期90天以上贷款比率 → 地区平均逾期90天比率（聚合）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'OVERDUE_90DAYS_RATIO'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'REGION_AVG_OVERDUE_90_RATIO'),
    'aggregation',
    '按地区聚合计算平均逾期90天以上比率：AVG(overdue_90days_loan_ratio) GROUP BY region_name',
    4,
    NOW()
);

-- 血缘7：关注类不良贷款比率 → 关注类比率变化（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_NPL_RATIO'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_RATIO_CHANGE'),
    'calculation',
    '计算环比变化：本期关注类比率 - 上期关注类比率',
    3,
    NOW()
);

-- 血缘8：逾期90天以上贷款比率 → 逾期90天比率变化（计算）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'OVERDUE_90DAYS_RATIO'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'OVERDUE_90_RATIO_CHANGE'),
    'calculation',
    '计算环比变化：本期逾期90天以上比率 - 上期逾期90天以上比率',
    3,
    NOW()
);

-- ========================================
-- 第四步：创建血缘节点（用于可视化图谱）
-- ========================================

-- 创建指标节点
INSERT INTO bi_lineage_node (node_type, node_id, node_name, node_attributes, create_time)
SELECT
    'metric',
    id,
    metric_name,
    JSON_OBJECT(
        'metric_code', metric_code,
        'business_definition', business_definition,
        'technical_formula', technical_formula,
        'owner_dept', owner_dept
    ),
    NOW()
FROM bi_metric_metadata
WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%' OR metric_code = 'TOTAL_LOAN_BALANCE'
ON DUPLICATE KEY UPDATE
    node_name = VALUES(node_name),
    node_attributes = VALUES(node_attributes);

-- ========================================
-- 第五步：创建血缘边（用于可视化图谱）
-- ========================================

INSERT INTO bi_lineage_edge (source_node_id, target_node_id, edge_type, edge_attributes, create_time)
SELECT
    source_node.id,
    target_node.id,
    'derived_from',
    JSON_OBJECT(
        'transformation_type', ml.transformation_type,
        'transformation_logic', ml.transformation_logic,
        'dependency_strength', ml.dependency_strength
    ),
    NOW()
FROM bi_metric_lineage ml
JOIN bi_lineage_node source_node ON source_node.node_type = 'metric' AND source_node.node_id = ml.upstream_metric_id
JOIN bi_lineage_node target_node ON target_node.node_type = 'metric' AND target_node.node_id = ml.downstream_metric_id
WHERE ml.upstream_metric_id IN (
    SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%' OR metric_code = 'TOTAL_LOAN_BALANCE'
)
AND ml.downstream_metric_id IN (
    SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%'
)
ON DUPLICATE KEY UPDATE
    edge_attributes = VALUES(edge_attributes);

-- ========================================
-- 第六步：查看血缘关系
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
WHERE upstream.metric_code LIKE 'CONCERN_%'
   OR upstream.metric_code LIKE 'OVERDUE_%'
   OR upstream.metric_code = 'TOTAL_LOAN_BALANCE'
   OR downstream.metric_code LIKE 'CONCERN_%'
   OR downstream.metric_code LIKE 'OVERDUE_%'
ORDER BY downstream.metric_code, ml.dependency_strength DESC;

-- ========================================
-- 第七步：血缘图谱可视化（文本版）
-- ========================================

-- 显示关注类不良贷款比率血缘图
SELECT '
═══════════════════════════════════════════════════════════
           关注类不良贷款比率血缘图谱
═══════════════════════════════════════════════════════════

                    [各项贷款余额]
                          |
        +-----------------+------------------+
        |                 |                  |
        v                 v                  v
 [关注类贷款余额] → [关注类不良贷款比率] → [关注类比率变化]
        |
        v
 [地区平均关注类比率]

═══════════════════════════════════════════════════════════
' AS graph;

-- 显示逾期90天以上贷款比率血缘图
SELECT '
═══════════════════════════════════════════════════════════
         逾期90天以上贷款比率血缘图谱
═══════════════════════════════════════════════════════════

                    [各项贷款余额]
                          |
        +-----------------+------------------+
        |                 |                  |
        v                 v                  v
 [逾期90天以上余额] → [逾期90天以上比率] → [逾期90天比率变化]
        |
        v
 [地区平均逾期90天比率]

═══════════════════════════════════════════════════════════
' AS graph;

-- ========================================
-- 第八步：完整血缘关系查询（递归CTE）
-- ========================================

-- 查询关注类不良贷款比率的完整血缘链路
WITH RECURSIVE concern_lineage_tree AS (
    -- 起始节点：关注类不良贷款比率
    SELECT
        mm.id,
        mm.metric_code,
        mm.metric_name,
        mm.business_definition,
        0 as level,
        CAST(mm.metric_name AS CHAR(500)) as path
    FROM bi_metric_metadata mm
    WHERE mm.metric_code = 'CONCERN_NPL_RATIO'

    UNION ALL

    -- 递归查询上游
    SELECT
        upstream.id,
        upstream.metric_code,
        upstream.metric_name,
        upstream.business_definition,
        clt.level + 1,
        CONCAT(upstream.metric_name, ' → ', clt.path)
    FROM bi_metric_lineage ml
    JOIN bi_metric_metadata upstream ON ml.upstream_metric_id = upstream.id
    JOIN concern_lineage_tree clt ON ml.downstream_metric_id = clt.id
    WHERE clt.level < 5
)
SELECT * FROM concern_lineage_tree ORDER BY level, metric_code;

-- 查询逾期90天以上贷款比率的完整血缘链路
WITH RECURSIVE overdue_lineage_tree AS (
    -- 起始节点：逾期90天以上贷款比率
    SELECT
        mm.id,
        mm.metric_code,
        mm.metric_name,
        mm.business_definition,
        0 as level,
        CAST(mm.metric_name AS CHAR(500)) as path
    FROM bi_metric_metadata mm
    WHERE mm.metric_code = 'OVERDUE_90DAYS_RATIO'

    UNION ALL

    -- 递归查询上游
    SELECT
        upstream.id,
        upstream.metric_code,
        upstream.metric_name,
        upstream.business_definition,
        olt.level + 1,
        CONCAT(upstream.metric_name, ' → ', olt.path)
    FROM bi_metric_lineage ml
    JOIN bi_metric_metadata upstream ON ml.upstream_metric_id = upstream.id
    JOIN overdue_lineage_tree olt ON ml.downstream_metric_id = olt.id
    WHERE olt.level < 5
)
SELECT * FROM overdue_lineage_tree ORDER BY level, metric_code;

-- ========================================
-- 第九步：验证脚本：检查数据是否正确创建
-- ========================================

-- 检查指标数量
SELECT
    '关注类和逾期90天指标总数' as item,
    COUNT(*) as count
FROM bi_metric_metadata
WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%'

UNION ALL

-- 检查血缘关系数量
SELECT
    '相关血缘关系数' as item,
    COUNT(*) as count
FROM bi_metric_lineage
WHERE upstream_metric_id IN (
    SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%' OR metric_code = 'TOTAL_LOAN_BALANCE'
)
AND downstream_metric_id IN (
    SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%'
)

UNION ALL

-- 检查血缘节点数量
SELECT
    '血缘节点数' as item,
    COUNT(*) as count
FROM bi_lineage_node
WHERE node_type = 'metric'
AND node_id IN (
    SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%' OR metric_code = 'TOTAL_LOAN_BALANCE'
)

UNION ALL

-- 检查血缘边数量
SELECT
    '血缘边数' as item,
    COUNT(*) as count
FROM bi_lineage_edge
WHERE id IN (
    SELECT le.id
    FROM bi_lineage_edge le
    JOIN bi_lineage_node source ON le.source_node_id = source.id
    JOIN bi_lineage_node target ON le.target_node_id = target.id
    WHERE source.node_id IN (
        SELECT id FROM bi_metric_metadata WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%' OR metric_code = 'TOTAL_LOAN_BALANCE'
    )
);

-- ========================================
-- 第十步：与现有NPL指标的关联关系
-- ========================================

-- 说明：关注类比率与不良贷款率的关联
-- 关注类是不良贷款的先行指标，关注类贷款增加通常预示着未来不良贷款可能增加
-- 建立关注类比率与不良贷款率的关联关系（可选，用于分析）

/*
-- 可选：建立关注类比率与不良贷款率的关联关系（用于趋势分析）
INSERT INTO bi_metric_lineage (
    upstream_metric_id,
    downstream_metric_id,
    transformation_type,
    transformation_logic,
    dependency_strength,
    create_time
) VALUES (
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_NPL_RATIO'),
    (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO'),
    'correlation',
    '关注类比率是不良贷款率的先行指标，关注类比率上升通常预示着未来不良贷款率可能上升',
    2,
    NOW()
);
*/

-- ========================================
-- 完成提示
-- ========================================

SELECT '✅ 关注类和逾期90天以上贷款指标血缘数据创建完成！' as message;
SELECT '📊 共创建 9 个指标，8 条血缘关系' as summary;
SELECT '🔍 下一步：为图表组件配置 metricId' as next_step;
SELECT '💡 参考：SELECT id, metric_code, metric_name FROM bi_metric_metadata WHERE metric_code LIKE ''CONCERN_%'' OR metric_code LIKE ''OVERDUE_%'';' as query_example;

-- ========================================
-- 使用说明
-- ========================================

/*
1. 查看所有创建的指标：
   SELECT id, metric_code, metric_name, business_definition, technical_formula
   FROM bi_metric_metadata
   WHERE metric_code LIKE 'CONCERN_%' OR metric_code LIKE 'OVERDUE_%'
   ORDER BY metric_code;

2. 查看血缘关系图：
   使用前端界面的血缘图谱功能，输入以下任一指标ID：
   - CONCERN_NPL_RATIO（关注类不良贷款比率）
   - OVERDUE_90DAYS_RATIO（逾期90天以上贷款比率）

3. 为图表配置指标ID：
   UPDATE bi_dashboard_component
   SET data_config = JSON_SET(data_config, '$.metricId',
       (SELECT id FROM bi_metric_metadata WHERE metric_code = 'CONCERN_NPL_RATIO')
   )
   WHERE id = YOUR_COMPONENT_ID;

4. 验证数据：
   SELECT data_date, concern_non_performing_loan_ratio, overdue_90days_loan_ratio
   FROM non_performing_loan_data
   ORDER BY data_date DESC
   LIMIT 10;
*/
