# 计算字段聚合处理修复

## 问题描述
当计算字段的表达式中已经包含聚合函数（如 `SUM(jkye)`）时，后端在构建 SQL 时会再次应用聚合函数，导致双重聚合错误：
```sql
SUM(SUM(jkye))  -- 错误！
```

## 根本原因
`QueryExecutor.buildAggregationSql()` 方法对所有指标字段统一应用聚合函数，没有区分：
1. **已聚合的计算字段**：表达式中已包含聚合函数
2. **未聚合的计算字段**：表达式是简单计算

## 解决方案

### 使用 `aggregation` 字段标识聚合类型

`CalculatedFieldDTO` 有 `aggregation` 字段，用于标识聚合方式：
- **AUTO**：表达式中已包含聚合函数，不需要再次聚合
- **SUM/AVG/MAX/MIN/COUNT**：需要应用指定的聚合函数

### 修复后的逻辑

```java
// 检查是否为计算字段
if (calculatedFields != null && isCalculatedField(metric.getField(), calculatedFields)) {
    // 获取计算字段定义
    CalculatedFieldDTO calcField = findCalculatedField(metric.getField(), calculatedFields);
    
    if (calcField != null) {
        // 使用转换后的SQL表达式
        fieldExpr = getCalculatedFieldExpression(metric.getField(), calculatedFields, dataset);
        
        // 检查计算字段的聚合方式
        String calcAggregation = calcField.getAggregation();
        if ("AUTO".equalsIgnoreCase(calcAggregation)) {
            // AUTO: 表达式中已包含聚合函数，直接使用，不再应用聚合
            selectItems.add(fieldExpr + " AS " + alias);
            continue; // 跳过后续的聚合函数应用
        } else {
            // 指定了聚合方式: 使用计算字段定义的聚合方式
            aggregation = calcAggregation;
        }
    }
} else {
    // 普通字段: 直接使用字段名
    fieldExpr = metric.getField();
}

// 应用聚合函数（AUTO 类型的计算字段已经在上面处理，不会执行到这里）
String metricExpr = aggregation + "(" + fieldExpr + ")";
selectItems.add(metricExpr + " AS " + alias);
```

## 示例场景

### 场景 1：已聚合的计算字段（aggregation = AUTO）
```
前端配置:
  - calculatedFields = [{
      name: "总贷款余额",
      expression: "SUM(jkye)",
      aggregation: "AUTO",
      fieldType: "metric"
    }]
  - metrics = [{ field: "总贷款余额" }]

后端处理:
  - isCalculatedField("总贷款余额") → true
  - calcField.getAggregation() → "AUTO"
  - fieldExpr = "SUM(jkye)"
  - 生成 SQL: SUM(jkye) AS 总贷款余额  ✅ 正确！
```

### 场景 2：未聚合的计算字段（aggregation = SUM）
```
前端配置:
  - calculatedFields = [{
      name: "总金额",
      expression: "jkye + lxje",
      aggregation: "SUM",
      fieldType: "metric"
    }]
  - metrics = [{ field: "总金额" }]

后端处理:
  - isCalculatedField("总金额") → true
  - calcField.getAggregation() → "SUM"
  - fieldExpr = "jkye + lxje"
  - 生成 SQL: SUM(jkye + lxje) AS 总金额  ✅ 正确！
```

### 场景 3：原始字段
```
前端配置:
  - metrics = [{ field: "jkye", aggregation: "SUM" }]

后端处理:
  - isCalculatedField("jkye") → false
  - fieldExpr = "jkye"
  - 生成 SQL: SUM(jkye)  ✅ 正确！
```

## 前端配置要求

在 `CalculatedFieldDialog` 中创建计算字段时，需要正确设置 `aggregation` 字段：

1. **如果表达式中已包含聚合函数**（如 `SUM(jkye)`、`AVG(field)`）：
   - 设置 `aggregation = "AUTO"`

2. **如果表达式是简单计算**（如 `field1 + field2`、`field1 * 100`）：
   - 设置 `aggregation = "SUM"` 或其他聚合方式

## 相关文件
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java` ✅
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/CalculatedFieldDTO.java`
- `ui/src/components/CalculatedFieldDialog/index.vue`

## 测试验证
1. 创建计算字段 `SUM(jkye)`，aggregation 设置为 AUTO
2. 拖拽到指标区域
3. 刷新数据
4. 检查后端日志，确认生成的 SQL 是 `SUM(jkye)` 而不是 `SUM(SUM(jkye))`
5. 验证查询结果正确
