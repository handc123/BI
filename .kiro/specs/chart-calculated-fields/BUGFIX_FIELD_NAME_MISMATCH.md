# 计算字段显示为0问题修复 - 字段名不匹配

## 问题描述

用户创建计算字段 `SUM(jkye)` 后，执行刷新数据时图表显示为0，后端报错"维度和度量不能同时为空"。

## 根本原因

前端 `DataConfig.vue` 组件在 `syncMetricsToConfig()` 方法中使用了错误的字段名：

```javascript
// ❌ 错误的实现
syncMetricsToConfig() {
  this.dataConfig.metrics = this.selectedMetrics.map(m => ({
    fieldName: m.fieldName || m.name,  // 使用了 fieldName
    comment: m.comment,
    fieldType: m.fieldType || m.type
  }))
}
```

但是后端 `ChartQueryRequest.MetricConfig` 类期望的字段名是 `field`：

```java
public static class MetricConfig {
    /** 字段名 */
    private String field;  // ✅ 后端期望 field 字段
    
    public String getField() {
        return field;
    }
}
```

后端在 `QueryConditionServiceImpl.executeChartQuery()` 中提取字段名时：

```java
for (ChartQueryRequest.MetricConfig metric : queryRequest.getMetrics()) {
    if (metric.getField() != null) {  // ✅ 使用 getField()
        Metric m = new Metric();
        m.setField(metric.getField());
        // ...
    }
}
```

由于前端发送的是 `fieldName` 而不是 `field`，后端的 `metric.getField()` 返回 `null`，导致：
1. `metricList` 为空
2. 触发"维度和度量不能同时为空"的验证错误

## 修复方案

### 1. 修复 `syncMetricsToConfig()` 方法

```javascript
// ✅ 正确的实现
syncMetricsToConfig() {
  this.dataConfig.metrics = this.selectedMetrics.map(m => ({
    field: m.fieldName || m.name,  // 修复: 使用 field 而不是 fieldName
    fieldName: m.fieldName || m.name,  // 保留 fieldName 以兼容
    comment: m.comment,
    fieldType: m.fieldType || m.type,
    isCalculated: m.isCalculated || false,
    // 如果是计算字段，添加额外信息
    ...(m.isCalculated ? {
      expression: m.expression,
      aggregation: m.aggregation,
      alias: m.alias
    } : {})
  }))
  console.log('[DataConfig] syncMetricsToConfig - 同步后的metrics:', this.dataConfig.metrics)
  // 也同步到 measures 以保持向后兼容
  this.dataConfig.measures = this.dataConfig.metrics
  this.emitChange()
}
```

### 2. 同步修复 `syncDimensionsToConfig()` 方法

为了保持一致性，也修复了维度配置方法：

```javascript
syncDimensionsToConfig() {
  this.dataConfig.dimensions = this.selectedDimensions.map(d => ({
    field: d.fieldName || d.name,  // 修复: 使用 field 而不是 fieldName
    fieldName: d.fieldName || d.name,  // 保留 fieldName 以兼容
    comment: d.comment,
    fieldType: d.fieldType || d.type,
    isCalculated: d.isCalculated || false,
    // 如果是计算字段，添加额外信息
    ...(d.isCalculated ? {
      expression: d.expression,
      aggregation: d.aggregation,
      alias: d.alias
    } : {})
  }))
  console.log('[DataConfig] syncDimensionsToConfig - 同步后的dimensions:', this.dataConfig.dimensions)
  this.emitChange()
}
```

## 修复内容

### 文件: `ui/src/components/ConfigPanel/DataConfig.vue`

1. **添加 `field` 字段**: 确保发送给后端的数据包含 `field` 属性
2. **保留 `fieldName` 字段**: 为了向后兼容，同时保留 `fieldName` 属性
3. **添加计算字段信息**: 对于计算字段，添加 `expression`、`aggregation`、`alias` 等额外信息
4. **添加调试日志**: 输出同步后的配置，便于排查问题

## 数据流

### 修复前

```
前端 DataConfig.vue
  ↓ syncMetricsToConfig()
  { fieldName: "npl_balance", ... }  ❌ 错误的字段名
  ↓ ChartWidget.fetchData()
  ↓ HTTP POST /api/query/execute
  ↓ 后端 QueryConditionServiceImpl.executeChartQuery()
  ↓ metric.getField() → null  ❌ 无法获取字段名
  ↓ metricList 为空
  ↓ 报错: "维度和度量不能同时为空"
```

### 修复后

```
前端 DataConfig.vue
  ↓ syncMetricsToConfig()
  { field: "npl_balance", fieldName: "npl_balance", ... }  ✅ 正确的字段名
  ↓ ChartWidget.fetchData()
  ↓ HTTP POST /api/query/execute
  ↓ 后端 QueryConditionServiceImpl.executeChartQuery()
  ↓ metric.getField() → "npl_balance"  ✅ 成功获取字段名
  ↓ metricList = [Metric{field="npl_balance", aggregation="sum"}]
  ↓ queryExecutor.executeAggregation(...)
  ↓ 返回查询结果
```

## 测试验证

### 测试步骤

1. 创建计算字段 `SUM(jkye)`，命名为 `npl_balance`
2. 将计算字段拖拽到指标区
3. 点击"刷新数据"按钮
4. 检查浏览器控制台日志，确认 metrics 数组包含 `field` 属性
5. 检查后端日志，确认成功执行聚合查询
6. 验证图表显示正确的数据

### 预期结果

- 前端控制台输出: `[DataConfig] syncMetricsToConfig - 同步后的metrics: [{field: "npl_balance", ...}]`
- 后端日志输出: `执行聚合查询: datasetId=X, dimensions=0, metrics=1, calculatedFields=1`
- 图表显示 `SUM(jkye)` 的聚合结果

## 相关文件

- `ui/src/components/ConfigPanel/DataConfig.vue` - 前端数据配置组件（已修复）
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/ChartQueryRequest.java` - 查询请求DTO
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/QueryConditionServiceImpl.java` - 查询服务实现
- `ui/src/components/ChartWidget/index.vue` - 图表组件

## 总结

这是一个典型的前后端字段名不匹配问题。前端使用 `fieldName`，后端期望 `field`，导致数据传递失败。修复方案是在前端同时提供两个字段名，确保向后兼容的同时满足后端要求。

修复完成后，计算字段应该能够正常工作，图表能够显示正确的聚合数据。
