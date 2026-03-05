# 计算字段显示为 0 的问题修复

## 问题描述
用户创建了计算字段"总贷款余额"，表达式为 `SUM(jkye)`，保存成功后拖拽到图表配置区域，执行刷新数据时图表显示为 0。

## 根本原因
**前后端数据流断裂**：计算字段配置没有从前端正确传递到后端查询执行器。

### 数据流分析
1. **前端 DataConfig.vue**：用户配置计算字段，保存到 `dataConfig.calculatedFields`
2. **前端 ChartWidget.vue**：构建查询请求时**遗漏了 `calculatedFields` 参数**
3. **后端 QueryConditionController**：接收 `ChartQueryRequest`，其中包含 `calculatedFields` 字段
4. **后端 QueryExecutor**：需要 `calculatedFields` 来正确转换计算字段表达式为 SQL

### 问题定位
在 `ui/src/components/ChartWidget/index.vue` 的 `fetchData()` 方法中，构建查询请求时：

```javascript
// 错误的代码（缺少 calculatedFields）
const queryRequest = {
  datasetId: this.config.dataConfig.datasetId,
  dimensions: this.config.dataConfig.dimensions || [],
  metrics: this.config.dataConfig.metrics || [],
  filters: this.buildFilters(),
  limit: this.config.dataConfig.limit || 1000,
  params: this.mergedQueryParams
  // ❌ 缺少 calculatedFields
}
```

## 修复方案

### 1. 前端修复：ChartWidget 传递计算字段配置

**文件**：`ui/src/components/ChartWidget/index.vue`

**修改位置**：`fetchData()` 方法中的查询请求构建

**修改内容**：
```javascript
const queryRequest = {
  datasetId: this.config.dataConfig.datasetId,
  dimensions: this.config.dataConfig.dimensions || [],
  metrics: this.config.dataConfig.metrics || [],
  filters: this.buildFilters(),
  limit: this.config.dataConfig.limit || 1000,
  params: this.mergedQueryParams,
  calculatedFields: this.config.dataConfig.calculatedFields || [] // ✅ 添加计算字段配置
}

console.log('[ChartWidget] 发起查询请求:', queryRequest)
console.log('[ChartWidget] 计算字段配置:', queryRequest.calculatedFields) // ✅ 添加调试日志
```

### 2. 后端修复：QueryExecutor 正确处理计算字段（已完成）

**文件**：`iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java`

**已完成的修复**：
1. ✅ 注入 `CalculatedFieldConverter` 依赖
2. ✅ 修复 `getCalculatedFieldExpression()` 方法，调用 `CalculatedFieldConverter.convertToSQL()`
3. ✅ 添加 `parseDatasetFields()` 辅助方法从数据集配置中提取字段列表
4. ✅ 修复数据库类型获取：从 `dataset.getDataSource().getType()` 获取
5. ✅ 在 `buildAggregationSql()` 中正确处理计算字段的维度和指标

**关键代码**：
```java
private String getCalculatedFieldExpression(String fieldName,
                                            List<CalculatedFieldDTO> calculatedFields,
                                            Dataset dataset) {
    // ... 查找计算字段 ...
    
    try {
        // 获取数据集字段列表
        List<DatasetFieldVO> datasetFields = 
            dataset.getFieldConfigMap() != null ? 
            parseDatasetFields(dataset.getFieldConfigMap()) : 
            new ArrayList<>();
        
        // 获取数据库类型
        String dbType = "mysql";
        if (dataset.getDataSource() != null && dataset.getDataSource().getType() != null) {
            dbType = dataset.getDataSource().getType();
        }
        
        // 使用 CalculatedFieldConverter 转换表达式
        String sql = calculatedFieldConverter.convertToSQL(field, datasetFields, dbType);
        
        // 移除 AS 别名部分
        if (sql.contains(" AS ")) {
            sql = sql.substring(0, sql.indexOf(" AS "));
        }
        
        return sql;
    } catch (Exception e) {
        log.error("[QueryExecutor] 转换计算字段失败: name={}, error={}", fieldName, e.getMessage(), e);
        return field.getExpression();
    }
}
```

## 完整数据流

### 正确的数据流（修复后）
```
用户创建计算字段
  ↓
DataConfig.vue 保存到 dataConfig.calculatedFields
  ↓
用户拖拽计算字段到配置区域
  ↓
用户点击"刷新数据"
  ↓
ChartWidget.fetchData() 构建查询请求
  ↓ 包含 calculatedFields
executeQuery API 调用 /bi/condition/execute
  ↓
QueryConditionController.execute() 接收 ChartQueryRequest
  ↓
QueryConditionService.executeChartQuery()
  ↓
QueryExecutor.executeAggregation()
  ↓ 传递 calculatedFields
buildAggregationSql() 构建 SQL
  ↓
getCalculatedFieldExpression() 转换计算字段
  ↓
CalculatedFieldConverter.convertToSQL()
  ↓
生成正确的 SQL: SUM(jkye) AS 总贷款余额
  ↓
执行查询，返回正确结果
```

## 验证步骤

1. **前端验证**：
   - 打开浏览器开发者工具 Console
   - 创建计算字段"总贷款余额"，表达式 `SUM(jkye)`
   - 拖拽到指标区域
   - 点击"刷新数据"
   - 查看 Console 日志，确认 `queryRequest.calculatedFields` 包含计算字段配置

2. **后端验证**：
   - 查看后端日志
   - 确认 `[QueryExecutor]` 日志显示正确的 SQL 转换
   - 确认生成的 SQL 包含 `SUM(jkye)` 而不是原始字段名

3. **结果验证**：
   - 图表显示正确的数值（不再是 0）
   - 数据与数据库实际值一致

## 相关文件

### 前端文件
- `ui/src/components/ChartWidget/index.vue` - 图表组件，构建查询请求（已修复）
- `ui/src/components/ConfigPanel/DataConfig.vue` - 数据配置面板，管理计算字段
- `ui/src/api/bi/query.js` - 查询 API 定义
- `ui/src/api/bi/condition.js` - 条件查询 API 定义

### 后端文件
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/QueryConditionController.java` - 查询控制器
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/ChartQueryRequest.java` - 查询请求 DTO
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java` - 查询执行器（已修复）
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/CalculatedFieldConverter.java` - 计算字段转换器

## 修复状态
- ✅ 后端 QueryExecutor 修复完成
- ✅ 前端 ChartWidget 修复完成
- ⏳ 等待用户测试验证

## 下一步
1. 重新编译前端代码：`cd ui && npm run dev`
2. 刷新浏览器
3. 测试计算字段功能
4. 如果仍有问题，检查浏览器 Console 和后端日志
