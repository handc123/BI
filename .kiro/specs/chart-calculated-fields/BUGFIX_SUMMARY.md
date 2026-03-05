# 计算字段功能修复总结

## 修复的问题

### 问题 1：计算字段拖拽功能不工作
**状态**：✅ 已修复

**原因**：`FieldManagementPanel` 发送的事件参数结构与 `DataConfig` 期望的不匹配

**修复**：
- 修改 `DataConfig.vue` 的 `handleFieldDragStart` 方法接受 `dragData` 对象
- 对于计算字段，使用其 `fieldType` 属性确定是维度还是指标

**文件**：`ui/src/components/ConfigPanel/DataConfig.vue`

---

### 问题 2：计算字段显示为 0
**状态**：✅ 已修复

**原因**：前后端数据流断裂 - 计算字段配置没有从前端传递到后端

**修复**：
1. **前端**：`ChartWidget.vue` 的 `fetchData()` 方法中添加 `calculatedFields` 参数
2. **后端**：`QueryExecutor.java` 正确调用 `CalculatedFieldConverter` 转换计算字段表达式

**文件**：
- `ui/src/components/ChartWidget/index.vue`
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java`

---

## 完整数据流（修复后）

```
用户创建计算字段 "总贷款余额" (SUM(jkye))
  ↓
DataConfig.vue 保存到 dataConfig.calculatedFields
  ↓
用户拖拽计算字段到指标区域
  ↓ (handleFieldDragStart 正确识别字段类型)
selectedMetrics 包含计算字段
  ↓
用户点击"刷新数据"
  ↓
ChartWidget.fetchData() 构建查询请求
  ↓ (包含 calculatedFields 参数)
executeQuery API → /bi/condition/execute
  ↓
QueryConditionController.execute()
  ↓
QueryExecutor.executeAggregation()
  ↓ (传递 calculatedFields)
buildAggregationSql()
  ↓
getCalculatedFieldExpression()
  ↓ (调用 CalculatedFieldConverter)
CalculatedFieldConverter.convertToSQL()
  ↓
生成 SQL: SELECT SUM(jkye) AS 总贷款余额
  ↓
执行查询，返回正确结果
  ↓
图表显示正确数值
```

## 测试步骤

1. 启动前端开发服务器：
   ```bash
   cd ui
   npm run dev
   ```

2. 打开浏览器，进入仪表板设计器

3. 创建计算字段：
   - 名称：总贷款余额
   - 表达式：SUM(jkye)
   - 类型：指标

4. 拖拽计算字段到指标区域

5. 点击"刷新数据"

6. 验证：
   - 浏览器 Console 显示 `calculatedFields` 参数
   - 图表显示正确的数值（不是 0）

## 相关文档
- [计算字段拖拽修复详情](./BUGFIX_CALCULATED_FIELD_DRAG_DROP.md)
- [计算字段显示为 0 修复详情](./BUGFIX_CALCULATED_FIELD_ZERO_VALUE.md)
