@echo off
chcp 65001 >nul
echo ========================================
echo 指标血缘功能 - 系统状态检查
echo ========================================
echo.

echo [检查1] 数据库表是否存在...
echo 请手动执行以下SQL验证:
echo.
echo SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='iras' AND TABLE_NAME IN ('bi_metric_metadata', 'bi_metric_lineage', 'bi_lineage_node', 'bi_lineage_edge', 'bi_metric_data_query');
echo.

echo [检查2] 指标元数据数量...
echo 请手动执行以下SQL验证:
echo.
echo SELECT COUNT(*) as metric_count FROM bi_metric_metadata;
echo.

echo [检查3] 血缘关系数量...
echo 请手动执行以下SQL验证:
echo.
echo SELECT COUNT(*) as lineage_count FROM bi_metric_lineage;
echo.

echo [检查4] 已配置指标ID的图表组件...
echo 请手动执行以下SQL验证:
echo.
echo SELECT id, component_name, JSON_EXTRACT(data_config, '$.metricId') as metric_id FROM bi_dashboard_component WHERE data_config LIKE '%%"metricId"%%';
echo.

echo ========================================
echo 快速修复方案
echo ========================================
echo.
echo 如果没有任何数据，请按以下步骤操作:
echo.
echo 步骤1: 创建测试数据
echo   mysql -u root -p iras ^< INSERT_TEST_DATA.sql
echo.
echo 步骤2: 为图表关联指标ID
echo   -- 查看测试指标的ID
echo   SELECT id, metric_code, metric_name FROM bi_metric_metadata WHERE metric_code LIKE 'TEST_%%';
echo.
echo   -- 为图表组件关联指标（修改YOUR_COMPONENT_ID和METRIC_ID）
echo   UPDATE bi_dashboard_component
echo   SET data_config = JSON_SET(COALESCE(data_config, '{}'), '$.metricId', METRIC_ID)
echo   WHERE id = YOUR_COMPONENT_ID;
echo.
echo 步骤3: 重启前端服务
echo   cd ui
echo   npm run dev
echo.
echo 步骤4: 访问看板查看页面
echo   http://localhost:80/views/bi/dashboard/view/1
echo.
echo ========================================
echo 完整文档请查看: METRIC_LINEAGE_USER_GUIDE.md
echo ========================================
pause
