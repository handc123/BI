# Bug修复: 新建计算字段后不显示

## 问题描述
用户反馈：新建计算字段点击保存后，计算字段框里没有显示新建的字段。

## 根本原因 ✅ 已找到

**事件名称不匹配**：
- `CalculatedFieldDialog.vue` 发出的事件是 `@save`
- `DataConfig.vue` 监听的事件是 `@submit`

由于事件名称不匹配，`handleCalculatedFieldSubmit` 方法从未被调用，导致新建的计算字段没有添加到 `calculatedFields` 数组中。

## 修复方案 ✅ 已完成

### 修改文件: `ui/src/components/ConfigPanel/DataConfig.vue`

**修改前**:
```vue
<calculated-field-dialog
  :visible.sync="calculatedFieldDialogVisible"
  :field="currentEditingCalculatedField"
  :dataset-fields="availableFields"
  :existing-fields="calculatedFields"
  @submit="handleCalculatedFieldSubmit"
/>
```

**修改后**:
```vue
<calculated-field-dialog
  :visible.sync="calculatedFieldDialogVisible"
  :field="currentEditingCalculatedField"
  :dataset-fields="availableFields"
  :existing-fields="calculatedFields"
  @save="handleCalculatedFieldSubmit"
/>
```

### 额外改进

为了更好地调试和追踪问题，还添加了以下调试日志：

**DataConfig.vue - handleCalculatedFieldSubmit**:
```javascript
handleCalculatedFieldSubmit(fieldConfig) {
  console.log('[DataConfig] 计算字段提交:', fieldConfig)
  console.log('[DataConfig] 当前计算字段列表:', this.calculatedFields)
  // ... 保存逻辑
  console.log('[DataConfig] 计算字段添加成功，新列表:', this.calculatedFields)
  
  // 强制更新视图
  this.$nextTick(() => {
    console.log('[DataConfig] nextTick后的计算字段列表:', this.calculatedFields)
    this.$forceUpdate()
  })
}
```

**FieldManagementPanel.vue - watch calculatedFields**:
```javascript
watch: {
  calculatedFields: {
    handler(newVal) {
      console.log('[FieldManagementPanel] calculatedFields 变化:', newVal)
    },
    immediate: true,
    deep: true
  }
}
```

## 验证步骤

1. 打开浏览器开发者工具 (F12)
2. 在仪表板设计器中打开一个图表的数据配置面板
3. 点击"新建计算字段"
4. 填写字段信息:
   - 英文名称: `test_field`
   - 中文名称: `测试字段`
   - 字段类型: 指标
   - 表达式: `jkye / 1000000`
   - 聚合方式: AUTO
5. 点击"保存"
6. 验证结果:
   - ✅ 对话框关闭
   - ✅ 字段管理面板的"计算字段"区域显示新建的字段
   - ✅ 字段显示中文名称 "测试字段"
   - ✅ 字段前面有 "fx" 图标
   - ✅ 可以拖拽字段到图表配置区

## 预期日志输出

修复后，控制台应该显示：
```
[DataConfig] 计算字段提交: {name: "test_field", alias: "测试字段", fieldType: "metric", expression: "jkye / 1000000", aggregation: "AUTO"}
[DataConfig] 当前计算字段列表: []
[DataConfig] 计算字段添加成功，新列表: [{name: "test_field", alias: "测试字段", ...}]
[FieldManagementPanel] calculatedFields 变化: [{name: "test_field", alias: "测试字段", ...}]
[DataConfig] nextTick后的计算字段列表: [{name: "test_field", alias: "测试字段", ...}]
```

## 相关文件

- ✅ `ui/src/components/ConfigPanel/DataConfig.vue` - 修复事件监听
- ✅ `ui/src/components/FieldManagementPanel/index.vue` - 添加调试日志
- `ui/src/components/CalculatedFieldDialog/index.vue` - 发出 @save 事件

## 总结

这是一个典型的事件名称不匹配问题。修复非常简单，只需要将 `@submit` 改为 `@save` 即可。添加的调试日志可以帮助未来快速定位类似问题。
