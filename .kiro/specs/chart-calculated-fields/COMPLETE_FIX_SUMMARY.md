# 计算字段功能完整修复总结

## 问题回顾
用户创建计算字段 `SUM(jkye)` 后，图表显示为 0 而不是正确的聚合结果。

## 根本原因（3个问题）

### 问题 1：前端未传递计算字段配置 ❌
**位置**：`ui/src/components/ChartWidget/index.vue` 的 `fetchData()` 方法
**问题**：查询请求中缺少 `calculatedFields` 参数

### 问题 2：后端调用了错误的查询方法 ❌
**位置**：`QueryConditionServiceImpl.executeChartQuery()` 方法
**问题**：调用了 `executeQuery()` 而不是支持计算字段的 `executeAggregation()`

### 问题 3：计算字段双重聚合 ❌
**位置**：`QueryExecutor.buildAggregationSql()` 方法
**问题**：对已包含聚合函数的计算字段再次应用聚合，导致 `SUM(SUM(jkye))`

## 完整修复方案

### ✅ 修复 1：前端传递计算字段配置
**文件**：`ui/src/components/ChartWidget/index.vue`
**修改**：
```javascript
const queryRequest = {
  datasetId: this.config.dataConfig.datasetId,
  dimensions: this.config.dataConfig.dimensions || [],
  metrics: this.config.dataConfig.metrics || [],
  filters: this.buildFilters(),
  limit: this.config.dataConfig.limit || 1000,
  params: this.mergedQueryParams,
  calculatedFields: this.config.dataConfig.calculatedFields || [] // ✅ 添加
}
```

### ✅ 修复 2：后端调用正确的查询方法
**文件**：`QueryConditionServiceImpl.java`
**修改**：
```java
// 如果有维度或指标配置，使用聚合查询（支持计算字段）
if ((queryRequest.getDimensions() != null && !queryRequest.getDimensions().isEmpty()) ||
    (queryRequest.getMetrics() != null && !queryRequest.getMetrics().isEmpty())) {
    
    // 转换配置
    List<String> dimensionFields = extractDimensionFields(queryRequest);
    List<Metric> metricList = extractMetrics(queryRequest);
    
    // ✅ 调用支持计算字段的聚合查询
    result = queryExecutor.executeAggregation(
        queryRequest.getDatasetId(),
        dimensionFields,
        metricList,
        filters,
        queryRequest.getCalculatedFields(), // ✅ 传递计算字段
        SecurityUtils.getLoginUser().getUser()
    );
} else {
    // 没有维度和指标，使用基础查询
    result = queryExecutor.executeQuery(...);
}
```

### ✅ 修复 3：正确处理计算字段聚合
**文件**：`QueryExecutor.java`
**修改**：
```java
// 添加度量字段
for (Metric metric : metrics) {
    if (isCalculatedField(metric.getField(), calculatedFields)) {
        CalculatedFieldDTO calcField = findCalculatedField(...);
        fieldExpr = getCalculatedFieldExpression(...);
        
        // ✅ 检查聚合方式
        if ("AUTO".equalsIgnoreCase(calcField.getAggregation())) {
            // 表达式中已包含聚合函数，直接使用
            selectItems.add(fieldExpr + " AS " + alias);
            continue; // 不再应用聚合函数
        } else {
            // 使用指定的聚合方式
            aggregation = calcField.getAggregation();
        }
    } else {
        // 普通字段
        fieldExpr = metric.getField();
    }
    
    // 应用聚合函数
    selectItems.add(aggregation + "(" + fieldExpr + ") AS " + alias);
}
```

## 数据流（修复后）

```
用户创建计算字段
  name: "总贷款余额"
  expression: "SUM(jkye)"
  aggregation: "AUTO"  ← 关键！表示表达式中已包含聚合
  fieldType: "metric"
    ↓
DataConfig.vue 保存到 dataConfig.calculatedFields
    ↓
用户拖拽到指标区域
    ↓
用户点击"刷新数据"
    ↓
ChartWidget.fetchData() 构建查询请求
  {
    datasetId: 1,
    dimensions: [],
    metrics: [{ field: "总贷款余额" }],
    calculatedFields: [{
      name: "总贷款余额",
      expression: "SUM(jkye)",
      aggregation: "AUTO"
    }]
  }
    ↓
API 调用 /bi/condition/execute
    ↓
QueryConditionController.execute()
    ↓
QueryConditionServiceImpl.executeChartQuery()
  - 检测到有 metrics 配置
  - 转换为 Metric 对象列表
  - 调用 executeAggregation()
    ↓
QueryExecutor.executeAggregation()
    ↓
buildAggregationSql()
  - 遍历 metrics
  - 检测到 "总贷款余额" 是计算字段
  - 获取 aggregation = "AUTO"
  - 直接使用表达式，不再应用聚合
    ↓
getCalculatedFieldExpression()
    ↓
CalculatedFieldConverter.convertToSQL()
  - 解析表达式 "SUM(jkye)"
  - 转换为 SQL: SUM(`jkye`)
    ↓
生成最终 SQL:
  SELECT SUM(`jkye`) AS `总贷款余额`
  FROM t_cbis_t_daikuan_hzwdb
    ↓
执行查询
    ↓
返回结果: { "总贷款余额": 50000000.00 }
    ↓
图表显示正确的值
```

## 关键设计点

### 1. 计算字段的 aggregation 字段
- **AUTO**：表达式中已包含聚合函数（如 `SUM(jkye)`、`AVG(field)`）
  - 后端直接使用表达式，不再应用聚合
  - 生成 SQL: `SUM(jkye)`
  
- **SUM/AVG/MAX/MIN/COUNT**：表达式是简单计算（如 `field1 + field2`）
  - 后端应用指定的聚合函数
  - 生成 SQL: `SUM(field1 + field2)`

### 2. 前端默认设置
`CalculatedFieldDialog` 默认设置 `aggregation: 'AUTO'`，适用于大多数场景。

### 3. 后端智能判断
`QueryExecutor` 根据 `aggregation` 字段智能决定是否应用聚合函数。

## 测试场景

### 场景 1：已聚合的计算字段
```javascript
// 前端配置
{
  name: "总贷款余额",
  expression: "SUM(jkye)",
  aggregation: "AUTO",
  fieldType: "metric"
}

// 生成 SQL
SELECT SUM(`jkye`) AS `总贷款余额` FROM ...

// 结果
{ "总贷款余额": 50000000.00 } ✅
```

### 场景 2：未聚合的计算字段
```javascript
// 前端配置
{
  name: "总金额",
  expression: "jkye + lxje",
  aggregation: "SUM",
  fieldType: "metric"
}

// 生成 SQL
SELECT SUM(`jkye` + `lxje`) AS `总金额` FROM ...

// 结果
{ "总金额": 55000000.00 } ✅
```

### 场景 3：原始字段
```javascript
// 前端配置
metrics: [{ field: "jkye", aggregation: "SUM" }]

// 生成 SQL
SELECT SUM(`jkye`) FROM ...

// 结果
{ "jkye": 50000000.00 } ✅
```

## 验证步骤

1. **重新编译后端**：
   ```bash
   mvn clean compile
   ```

2. **重启后端服务**

3. **重新编译前端**：
   ```bash
   cd ui
   npm run dev
   ```

4. **测试计算字段**：
   - 创建计算字段 `SUM(jkye)`
   - 拖拽到指标区域
   - 点击"刷新数据"
   - 检查浏览器 Console 日志
   - 检查后端日志
   - 验证图表显示正确的值

5. **检查日志关键信息**：
   - 前端：`[ChartWidget] 计算字段配置:` 应显示计算字段数组
   - 后端：`执行聚合查询: datasetId=..., calculatedFields=1`
   - 后端：生成的 SQL 应该是 `SUM(jkye)` 而不是 `SUM(SUM(jkye))`

## 相关文件

### 前端
- `ui/src/components/ChartWidget/index.vue` ✅
- `ui/src/components/ConfigPanel/DataConfig.vue`
- `ui/src/components/CalculatedFieldDialog/index.vue`

### 后端
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/QueryConditionServiceImpl.java` ✅
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java` ✅
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/CalculatedFieldConverter.java`
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/CalculatedFieldDTO.java`
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/ChartQueryRequest.java`

## 修复状态
- ✅ 前端传递计算字段配置
- ✅ 后端调用正确的查询方法
- ✅ 正确处理计算字段聚合
- ⏳ 等待用户测试验证


## 问题 4：前后端字段名不匹配 ❌ → ✅

### 问题描述
用户报错"维度和度量不能同时为空"，即使已经配置了指标。

### 根本原因
**位置**：`ui/src/components/ConfigPanel/DataConfig.vue` 的 `syncMetricsToConfig()` 方法
**问题**：前端使用 `fieldName` 字段，但后端期望 `field` 字段

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

后端 `ChartQueryRequest.MetricConfig` 类：
```java
public static class MetricConfig {
    private String field;  // ✅ 后端期望 field 字段
    
    public String getField() {
        return field;
    }
}
```

后端提取字段名：
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

### ✅ 修复 4：统一字段名
**文件**：`ui/src/components/ConfigPanel/DataConfig.vue`

#### 修复 syncMetricsToConfig() 方法
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

#### 修复 syncDimensionsToConfig() 方法
```javascript
// ✅ 正确的实现
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

### 修复效果

#### 修复前
```
前端发送:
{
  metrics: [{ fieldName: "npl_balance", ... }]  ❌
}
  ↓
后端接收:
metric.getField() → null  ❌
  ↓
metricList 为空
  ↓
报错: "维度和度量不能同时为空"
```

#### 修复后
```
前端发送:
{
  metrics: [{
    field: "npl_balance",  ✅
    fieldName: "npl_balance",  // 兼容
    isCalculated: true,
    expression: "SUM(jkye)",
    aggregation: "AUTO",
    alias: "总贷款余额"
  }]
}
  ↓
后端接收:
metric.getField() → "npl_balance"  ✅
  ↓
metricList = [Metric{field="npl_balance", aggregation="sum"}]
  ↓
queryExecutor.executeAggregation(...)
  ↓
成功执行查询
```

## 完整数据流（包含所有修复）

```
用户创建计算字段
  name: "总贷款余额"
  expression: "SUM(jkye)"
  aggregation: "AUTO"
  fieldType: "metric"
    ↓
DataConfig.vue 保存到 calculatedFields
    ↓
用户拖拽到指标区域
    ↓
DataConfig.syncMetricsToConfig()
  {
    field: "总贷款余额",  ✅ 修复4
    fieldName: "总贷款余额",
    isCalculated: true,
    expression: "SUM(jkye)",
    aggregation: "AUTO"
  }
    ↓
用户点击"刷新数据"
    ↓
ChartWidget.fetchData() 构建查询请求
  {
    datasetId: 1,
    dimensions: [],
    metrics: [{
      field: "总贷款余额",  ✅ 修复4
      fieldName: "总贷款余额"
    }],
    calculatedFields: [{  ✅ 修复1
      name: "总贷款余额",
      expression: "SUM(jkye)",
      aggregation: "AUTO"
    }]
  }
    ↓
API 调用 /bi/condition/execute
    ↓
QueryConditionServiceImpl.executeChartQuery()
  - 提取 metric.getField() → "总贷款余额"  ✅ 修复4
  - 调用 executeAggregation()  ✅ 修复2
    ↓
QueryExecutor.executeAggregation()
    ↓
buildAggregationSql()
  - 检测到 "总贷款余额" 是计算字段
  - 获取 aggregation = "AUTO"
  - 直接使用表达式  ✅ 修复3
    ↓
CalculatedFieldConverter.convertToSQL()
  - 转换为 SQL: SUM(`jkye`)
    ↓
生成最终 SQL:
  SELECT SUM(`jkye`) AS `总贷款余额`
  FROM t_cbis_t_daikuan_hzwdb
    ↓
执行查询
    ↓
返回结果: { "总贷款余额": 50000000.00 }
    ↓
图表显示正确的值 ✅
```

## 所有修复总结

| 问题 | 位置 | 修复状态 |
|------|------|---------|
| 1. 前端未传递计算字段配置 | ChartWidget/index.vue | ✅ 已修复 |
| 2. 后端调用错误的查询方法 | QueryConditionServiceImpl.java | ✅ 已修复 |
| 3. 计算字段双重聚合 | QueryExecutor.java | ✅ 已修复 |
| 4. 前后端字段名不匹配 | DataConfig.vue | ✅ 已修复 |

## 相关文档
- [BUGFIX_CALCULATED_FIELD_ZERO_VALUE.md](./BUGFIX_CALCULATED_FIELD_ZERO_VALUE.md) - 初始问题分析
- [BUGFIX_AGGREGATION_HANDLING.md](./BUGFIX_AGGREGATION_HANDLING.md) - 聚合处理修复
- [BUGFIX_FIELD_NAME_MISMATCH.md](./BUGFIX_FIELD_NAME_MISMATCH.md) - 字段名不匹配修复

## 最终修复状态
- ✅ 前端传递计算字段配置（修复1）
- ✅ 后端调用正确的查询方法（修复2）
- ✅ 正确处理计算字段聚合（修复3）
- ✅ 统一前后端字段名（修复4）
- ⏳ 等待用户测试验证
