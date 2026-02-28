-- ========================================
-- 自动配置脚本：为图表关联不良贷款率指标
-- ========================================

-- 说明：此脚本会自动查找第一个看板的第一个图表组件，并关联不良贷款率指标
-- 如果需要为特定图表配置，请修改脚本中的组件ID

-- 第一步：查询可用的图表组件
SELECT
    '=== 可用的图表组件 ===' as info;
SELECT
    id,
    component_name,
    data_config,
    dashboard_id
FROM bi_dashboard_component
WHERE component_type = 'chart'
ORDER BY dashboard_id, id
LIMIT 10;

-- 第二步：查询不良贷款率指标的ID
SELECT
    '=== 不良贷款率指标信息 ===' as info;
SELECT
    id AS npl_ratio_id,
    metric_code,
    metric_name,
    business_definition
FROM bi_metric_metadata
WHERE metric_code = 'NPL_RATIO';

-- 第三步：自动关联（选择第一个图表组件）
-- 取消注释下面的代码来执行自动关联

-- UPDATE bi_dashboard_component
-- SET data_config = JSON_SET(
--     COALESCE(data_config, '{}'),
--     '$.metricId',
--     (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO')
-- )
-- WHERE id = (
--     SELECT id
--     FROM bi_dashboard_component
--     WHERE component_type = 'chart'
--     ORDER BY dashboard_id, id
--     LIMIT 1
-- );

-- 验证配置结果
-- SELECT
--     id,
--     component_name,
--     JSON_EXTRACT(data_config, '$.metricId') as metric_id,
--     data_config
-- FROM bi_dashboard_component
-- WHERE JSON_EXTRACT(data_config, '$.metricId') IS NOT NULL;

-- ========================================
-- 手动配置示例
-- ========================================

-- 如果要为特定图表组件配置，请使用以下模板：

-- UPDATE bi_dashboard_component
-- SET data_config = JSON_SET(
--     COALESCE(data_config, '{}'),
--     '$.metricId',
--     10  -- ← 替换为实际的 NPL_RATIO 的 id
-- )
-- WHERE id = 123;  -- ← 替换为实际的图表组件 id

-- ========================================
-- 批量配置示例：为所有图表关联相同的指标
-- ========================================

-- UPDATE bi_dashboard_component
-- SET data_config = JSON_SET(
--     COALESCE(data_config, '{}'),
--     '$.metricId',
--     (SELECT id FROM bi_metric_metadata WHERE metric_code = 'NPL_RATIO')
-- )
-- WHERE component_type = 'chart'
--   AND dashboard_id = 1;  -- ← 只为特定看板的图表配置

-- ========================================
-- 完成后的验证查询
-- ========================================

-- 查看所有已配置指标的图表
SELECT
    '=== 已配置指标ID的图表组件 ===' as info;

SELECT
    c.id AS component_id,
    c.component_name,
    c.dashboard_id,
    JSON_EXTRACT(c.data_config, '$.metricId') AS metric_id,
    m.metric_code,
    m.metric_name
FROM bi_dashboard_component c
LEFT JOIN bi_metric_metadata m ON JSON_EXTRACT(c.data_config, '$.metricId') = m.id
WHERE JSON_EXTRACT(c.data_config, '$.metricId') IS NOT NULL
ORDER BY c.dashboard_id, c.id;

-- ========================================
-- 推荐流程
-- ========================================
-- 1. 执行 INSERT_NPL_METRIC_DATA.sql 创建测试数据
-- 2. 运行本脚本的查询部分，查看可用的组件
-- 3. 记录要配置的组件ID
-- 4. 运行"手动配置示例"中的UPDATE语句
-- 5. 验证配置结果
-- 6. 前往看板查看页面测试点击功能
