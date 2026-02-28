@echo off
chcp 65001 >nul
echo ======================================
echo 指标血缘API测试脚本
echo ======================================
echo.

echo [1] 测试服务是否运行...
curl -s http://localhost:8080/actuator/health
echo.
echo.

echo [2] 测试Swagger UI可访问性...
echo 请在浏览器中打开: http://localhost:8080/swagger-ui.html
echo.

echo [3] 数据库验证 - 查看指标表
echo SELECT * FROM bi_metric_metadata;
echo SELECT * FROM bi_metric_lineage;
echo.

echo [4] 创建测试数据示例
echo.
echo 创建指标元数据:
echo POST http://localhost:8080/bi/metadata
echo Content-Type: application/json
echo.
echo {
echo   "metricCode": "TEST_001",
echo   "metricName": "测试指标",
echo   "businessDefinition": "测试用指标",
echo   "technicalFormula": "COUNT(*)",
echo   "ownerDept": "技术部",
echo   "dataFreshness": "T-1",
echo   "updateFrequency": "每日",
echo   "status": "0"
echo }
echo.

echo [5] 查看API测试指南
echo 已生成测试指南: API_TESTING_GUIDE.md
echo.

echo ======================================
echo 测试说明:
echo 1. 所有API需要认证token
echo 2. 请通过Swagger UI进行测试（推荐）
echo 3. 或使用此脚本中的curl命令测试
echo 4. 详见 API_TESTING_GUIDE.md
echo ======================================
pause
