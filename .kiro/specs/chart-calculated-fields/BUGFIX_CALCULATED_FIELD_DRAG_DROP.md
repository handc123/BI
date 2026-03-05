# Bug Fix: 计算字段拖拽功能修复

## 问题描述

用户反馈：新建的计算字段显示在计算字段区域后，无法拖拽到维度/指标配置区域。

## 根本原因

`FieldManagementPanel` 组件和 `DataConfig` 组件之间的事件参数结构不匹配：

1. **FieldManagementPanel 发送的事件结构**：
   ```javascript
   const dragData = {
     field: field,      // 字段对象
     type: type,        // 'dimension', 'metric', 或 'calculated'
     source: 'fieldManagement'
   };
   this.$emit('field-drag-start', dragData);
   ```

2. **DataConfig 期望的参数结构**（修复前）：
   ```javascript
   handleFieldDragStart(field, type) {
     this.draggedField = field
     this.dragType = type
   }
   ```

3. **问题**：DataConfig 期望接收两个独立参数 `(field, type)`，但实际接收到的是一个 `dragData` 对象。

## 解决方案

### 1. 修复 handleFieldDragStart 方法

更新 `DataConfig.vue` 中的 `handleFieldDragStart` 方法以正确处理 dragData 对象：

```javascript
handleFieldDragStart(dragData) {
  console.log('[DataConfig] handleFieldDragStart 接收到:', dragData)
  
  // dragData 结构: { field, type, source }
  const field = dragData.field
  const type = dragData.type
  
  // 对于计算字段，使用其 fieldType 属性
  if (type === 'calculated') {
    this.draggedField = {
      ...field,
      fieldType: field.fieldType, // 'dimension' 或 'metric'
      name: field.name,
      alias: field.alias,
      comment: field.alias,
      isCalculated: true
    }
    this.dragType = field.fieldType // 使用计算字段的实际类型
    console.log('[DataConfig] 计算字段拖拽，类型:', field.fieldType, '字段:', this.draggedField)
  } else {
    // 普通字段
    this.draggedField = {
      ...field,
      fieldType: type
    }
    this.dragType = type
    console.log('[DataConfig] 普通字段拖拽，类型:', type, '字段:', this.draggedField)
  }
}
```

**关键改进**：
- 接受 `dragData` 对象而不是独立参数
- 对于计算字段（type === 'calculated'），使用字段的 `fieldType` 属性来确定它是维度还是指标
- 设置 `isCalculated: true` 标记以便在 UI 中正确显示
- 添加调试日志以便追踪拖拽过程

### 2. 增强 handleDrop 方法

更新 `handleDrop` 方法以更好地处理计算字段：

```javascript
// 检查是否为计算字段
const isCalculatedField = this.draggedField.isCalculated || 
  this.calculatedFields.some(f => f.name === this.draggedField.name)

// 添加字段时保留 alias 属性
const fieldToAdd = {
  ...this.draggedField,
  name: this.draggedField.name || this.draggedField.fieldName,
  fieldName: this.draggedField.fieldName || this.draggedField.name,
  comment: this.draggedField.comment || this.draggedField.alias,
  alias: this.draggedField.alias,  // 保留 alias 用于显示
  isCalculated: isCalculatedField
}
```

**关键改进**：
- 检查 `draggedField.isCalculated` 标记
- 保留 `alias` 属性用于显示计算字段的别名
- 添加详细的调试日志

### 3. 添加调试日志

在 `FieldManagementPanel.vue` 和 `DataConfig.vue` 中添加 console.log 语句，以便追踪拖拽事件的完整流程。

## 测试步骤

1. 打开仪表板设计器
2. 选择一个数据集
3. 创建一个新的计算字段（维度类型或指标类型）
4. 保存计算字段
5. 验证计算字段显示在"计算字段"区域
6. 拖拽计算字段到对应的配置区域：
   - 维度类型的计算字段 → 维度配置区
   - 指标类型的计算字段 → 指标配置区
7. 验证字段成功添加到配置区域
8. 验证字段显示正确的名称（alias）
9. 验证字段有计算字段图标标识

## 预期结果

- ✅ 计算字段可以从字段管理面板拖拽到配置区域
- ✅ 维度类型的计算字段只能拖拽到维度区
- ✅ 指标类型的计算字段只能拖拽到指标区
- ✅ 计算字段在配置区域显示其别名（alias）
- ✅ 计算字段有特殊图标标识（蓝色图标）
- ✅ 控制台显示详细的拖拽日志用于调试

## 相关文件

- `ui/src/components/ConfigPanel/DataConfig.vue` - 主要修复
- `ui/src/components/FieldManagementPanel/index.vue` - 添加调试日志
- `.kiro/specs/chart-calculated-fields/requirements.md` - 需求 4: 拖拽使用
- `.kiro/specs/chart-calculated-fields/design.md` - 设计文档

## 修复日期

2026-03-05

## 状态

✅ 已完成 - 代码已修复，等待用户测试验证
