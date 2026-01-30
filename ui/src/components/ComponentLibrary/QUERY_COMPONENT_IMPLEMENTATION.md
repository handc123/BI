# 查询组件特殊布局实现总结

## 实现日期
2026-01-22

## 需求描述
查询组件需要具有特殊的布局行为：
1. 宽度：100% 画布宽度（数据库存储 1520px，渲染为 100%）
2. 高度：80px（比图表组件更矮）
3. 位置：固定在画布顶部（y=0）
4. 行为：不可拖动、不可调整大小

## 实现方案

### 1. 组件尺寸设置
**文件**: `ui/src/components/ComponentLibrary/index.vue`

在 `getDefaultSize()` 方法中为查询组件设置特殊尺寸：
```javascript
getDefaultSize(componentType, subType) {
  // 查询控件：全宽，固定高度
  if (componentType === 'query') {
    return { width: 1520, height: 80 }
  }
  
  // 其他组件：标准尺寸
  return { width: 760, height: 400 }
}
```

### 2. 自动定位到顶部
**文件**: `ui/src/views/bi/dashboard/designer.vue`

修改 `handleAddComponent()` 方法，查询组件自动定位到顶部：
```javascript
handleAddComponent(componentData) {
  let size, position
  
  if (componentData.type === 'query') {
    size = { width: 1520, height: 80 }
    position = { x: 0, y: 0 } // 固定在顶部
  } else {
    size = componentData.size || { width: 760, height: 400 }
    position = componentData.position || this.findAvailablePosition(size)
  }
  
  // ... 创建组件
}
```

### 3. 智能布局避开查询组件
**文件**: `ui/src/views/bi/dashboard/designer.vue`

修改 `findAvailablePosition()` 方法，其他组件自动避开查询组件占用的空间：
```javascript
findAvailablePosition(size) {
  // 检查是否有查询组件
  const hasQueryComponent = this.components.some(c => c.type === 'query')
  const queryComponentHeight = 80
  
  // 计算起始行：如果有查询组件，从查询组件下方开始
  const startY = hasQueryComponent ? queryComponentHeight : 0
  
  for (let row = 0; row < maxRows; row++) {
    // 如果有查询组件，第一行从 y=80 开始
    const y = startY + (row * rowHeight)
    
    // ... 查找可用位置
  }
}
```

**关键改进**：
- 检测是否存在查询组件
- 如果有查询组件，所有图表组件从 y=80 开始布局
- 如果没有查询组件，图表组件从 y=0 开始布局
- 避免了查询组件遮挡第一排图表的问题

### 4. 禁止拖动
**文件**: `ui/src/components/DesignerCanvas/index.vue`

修改 `handleComponentMouseDown()` 方法，阻止查询组件拖动：
```javascript
handleComponentMouseDown(event, component) {
  if (event.button !== 0) return
  
  // 查询组件不可拖动
  if (component.type === 'query') {
    this.$message.info('查询控件位置固定，不可拖动')
    event.preventDefault()
    return
  }
  
  // ... 正常拖动逻辑
}
```

### 4. 禁止拖动
**文件**: `ui/src/components/DesignerCanvas/index.vue`

修改 `handleResizeStart()` 方法，阻止查询组件调整大小：
```javascript
handleResizeStart(event, component, handle) {
  // 查询组件不可调整大小
  if (component.type === 'query') {
    this.$message.info('查询控件尺寸固定，不可调整')
    event.preventDefault()
    event.stopPropagation()
    return
  }
  
  // ... 正常调整大小逻辑
}
```

### 5. 禁止调整大小
**文件**: `ui/src/components/DesignerCanvas/index.vue`

修改模板，查询组件不显示调整大小手柄：
```vue
<!-- 调整大小手柄 - 查询组件不显示 -->
<div
  v-if="selectedComponentId === component.id && component.type !== 'query'"
  class="resize-handles"
>
  <!-- ... 手柄 -->
</div>
```

### 6. 隐藏调整大小手柄
**文件**: `ui/src/components/DesignerCanvas/index.vue`

添加特殊样式标识查询组件：

**模板**:
```vue
<div
  class="component-item"
  :class="{ 
    'selected': selectedComponentId === component.id,
    'dragging': draggingComponentId === component.id,
    'query-component': component.type === 'query'
  }"
>
  <div class="component-header">
    <span class="component-title">
      <i v-if="component.type === 'query'" class="el-icon-lock"></i>
      {{ component.name }}
    </span>
  </div>
</div>
```

**样式**:
```css
/* 查询组件特殊样式 - 固定位置，不可拖动 */
.component-item.query-component {
  cursor: default;
  border-color: #67c23a;
  background: linear-gradient(to right, #f0f9ff 0%, #ffffff 100%);
}

.component-item.query-component:hover {
  border-color: #67c23a;
  box-shadow: 0 2px 8px 0 rgba(103, 194, 58, 0.15);
}

.component-item.query-component .component-header {
  background: linear-gradient(to right, #e1f3d8 0%, #f5f7fa 100%);
  cursor: default;
}
```

## 技术细节

### 布局算法
查询组件和图表组件的布局关系：
1. **查询组件**：固定在 y=0，占用 0-80px 的垂直空间
2. **图表组件**：
   - 如果存在查询组件：从 y=80 开始布局
   - 如果不存在查询组件：从 y=0 开始布局
3. **自动避让**：`findAvailablePosition()` 方法会检测查询组件并自动调整起始位置

### 数据存储
- 数据库中存储的宽度：1520px（画布标准宽度）
- 渲染时转换为：100%（通过 `getComponentStyle()` 方法）
- 高度：80px（固定值）
- 位置：x=0, y=0（固定在顶部）

### 响应式布局
查询组件的宽度使用百分比（100%），会随着画布容器自适应：
- 左侧导航栏展开（200px）：画布宽度自动调整
- 左侧导航栏收起（54px）：画布宽度自动调整
- 查询组件始终占满画布宽度

### 用户体验
1. **视觉反馈**：
   - 绿色边框和渐变背景标识查询组件
   - 锁图标表示位置固定
   - 鼠标悬停时显示不同样式（无拖动光标）

2. **操作提示**：
   - 尝试拖动时显示："查询控件位置固定，不可拖动"
   - 尝试调整大小时显示："查询控件尺寸固定，不可调整"

3. **布局行为**：
   - 添加查询组件时自动放置在顶部
   - 添加图表组件时自动避开查询组件（从 y=80 开始）
   - 不会与其他组件发生位置冲突
   - 不显示调整大小手柄

## 测试建议

1. **功能测试**：
   - 添加查询组件，验证是否自动定位到顶部
   - 尝试拖动查询组件，验证是否被阻止
   - 尝试调整查询组件大小，验证是否被阻止
   - 验证查询组件宽度是否占满画布

2. **样式测试**：
   - 验证查询组件是否显示绿色边框和渐变背景
   - 验证是否显示锁图标
   - 验证是否不显示调整大小手柄

3. **响应式测试**：
   - 展开/收起左侧导航栏，验证查询组件宽度是否自适应
   - 调整浏览器窗口大小，验证布局是否正常

4. **兼容性测试**：
   - 验证与现有图表组件的兼容性
   - 验证保存和加载功能是否正常
   - 验证多个查询组件的行为（如果支持）

## 后续优化建议

1. **多查询组件支持**：
   - 如果需要支持多个查询组件，可以考虑自动堆叠布局
   - 第一个查询组件 y=0，第二个 y=80，以此类推

2. **高度可配置**：
   - 可以考虑允许用户配置查询组件的高度
   - 但仍然保持宽度固定为 100%

3. **拖放排序**：
   - 如果有多个查询组件，可以考虑支持上下拖放排序
   - 但仍然限制在顶部区域

## 相关文件

- `ui/src/components/ComponentLibrary/index.vue` - 组件库，定义默认尺寸
- `ui/src/views/bi/dashboard/designer.vue` - 设计器主页面，处理组件添加
- `ui/src/components/DesignerCanvas/index.vue` - 画布组件，处理拖动和调整大小
- `ui/src/components/DesignerCanvas/RESPONSIVE_LAYOUT.md` - 响应式布局文档

## 版本信息

- Vue.js: 2.6.12
- Element UI: 2.15.14
- 实现人员: Kiro AI Assistant
- 审核状态: 待测试
