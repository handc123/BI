# 计算字段显示为 0 的问题 - 完整修复

## 问题总结
用户创建计算字段 `SUM(jkye)` 后，图表显示为 0 而不是聚合结果。

## 根本原因
**两个关键问题**：
1. 前端 ChartWidget 未传递 `calculatedFields` 到后端
2. 后端 QueryConditionServiceImpl 调用了错误的查询方法（`executeQuery` 而不是 `executeAggregation`）

## 已完成的修复

### ✅ 修复 1：前端传递计算字段配置
**文件**：`ui/src/components/ChartWidget/index.vue`
**位置**：`fetchData()` 方法
**修改**：在查询请求中添加 `calculatedFields: this.config.dataConfig.calculatedFields || []`

### ✅ 修复 2：后端调用正确的查询方法
**文件**：`iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/QueryConditionServiceImpl.java`
**位置**：`executeChartQuery()` 方法
**修改**：
- 检查是否有 dimensions 或 metrics 配置
- 如果有，调用 `queryExecutor.executeAggregation()` 并传递 `calculatedFields`
- 如果没有，调用 `queryExecutor.executeQuery()` 返回原始数据

### ✅ 修复 3：QueryExecutor 正确处理计算字段（之前已完成）
**文件**：`iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java`
**已完成**：
- 注入 `CalculatedFieldConverter` 依赖
- 修复 `getCalculatedFieldExpression()` 方法
- 在 `buildAggregationSql()` 中正确处理计算字段

## 完整数据流（修复后）
```
用户创建计算字段 → DataConfig 保存 → 拖拽到配置区域 → 点击刷新
  ↓
ChartWidget.fetchData() 构建请求（包含 calculatedFields）
  ↓
API 调用 /bi/condition/execute
  ↓
QueryConditionServiceImpl.executeChartQuery()
  ↓
转换 dimensions 和 metrics → 调用 executeAggregation()
  ↓
QueryExecutor.buildAggregationSql()
  ↓
getCalculatedFieldExpression() → CalculatedFieldConverter.convertToSQL()
  ↓
生成 SQL: SUM(jkye) AS 总贷款余额
  ↓
执行查询 → 返回正确结果
```

## 测试步骤
1. 重新编译后端：`mvn clean compile`
2. 重启后端服务
3. 重新编译前端：`cd ui && npm run dev`
4. 刷新浏览器
5. 创建计算字段 `SUM(jkye)`
6. 拖拽到指标区域
7. 点击"刷新数据"
8. 检查浏览器 Console 和后端日志
9. 验证图表显示正确的聚合值

## 相关文件
- `ui/src/components/ChartWidget/index.vue` ✅
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/QueryConditionServiceImpl.java` ✅
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java` ✅
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/CalculatedFieldConverter.java` ✅
