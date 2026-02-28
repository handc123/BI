# 数据配置面板实现文档

## 概述

数据配置面板是一个完整的拖放式数据源配置组件，符合需求文档中的所有12个需求。

## 实现的功能

### ✅ 需求 1: 面板布局和结构
- 400px 固定宽度
- 左右两列布局（左侧配置区 200px，右侧字段区 200px）
- 响应式布局，随父容器调整

### ✅ 需求 2: 配置区域拖放区
- 四个拖放区：指标、维度、过滤器、数量
- 空状态显示占位符文本
- 拖拽时高亮显示
- 底部"更新"按钮

### ✅ 需求 3: 字段区域数据源选择
- 数据源选择下拉框
- 数据集选择下拉框（数据源选择后启用）
- 自动获取可用数据集和字段

### ✅ 需求 4: 字段列表显示和搜索
- 搜索输入框，实时过滤字段
- 分组显示："维度字段"和"指标字段"
- 空状态提示信息
- 字段类型图标标识

### ✅ 需求 5: 拖放功能
- 使用 vuedraggable 实现
- 拖拽时视觉反馈
- 拖放区高亮显示
- 字段可在区间移动

### ✅ 需求 6: 拖放区中的字段管理
- 显示字段名称和类型图标
- 点击字段删除（×按钮）
- 指标/维度/过滤器区支持多个字段
- 数量区限制为单个字段

### ✅ 需求 7: 后端 API 集成
- `/bi/datasource/list` - 获取数据源列表
- `/bi/dataset/list?dataSourceId={id}` - 获取数据集列表
- `/bi/dataset/{id}/fields` - 获取字段元数据
- API 调用失败时显示错误消息

### ✅ 需求 8: 配置输出格式
- 发出 `config-change` 事件
- 包含 `dataSourceId`、`datasetId`、`metrics`、`dimensions`、`filters`、`count`
- 验证至少配置一个指标或维度

### ✅ 需求 9: 与仪表板设计器集成
- 作为 Vue 组件挂载
- 通过 `initialConfig` prop 接收现有配置
- 自动加载和显示当前配置

### ✅ 需求 10: 视觉反馈和用户体验
- 加载旋转器（v-loading）
- 拖拽时更改光标
- 高亮边框和背景
- 成功添加时短暂高亮
- 错误消息提示
- 一致的 IRAS 设计系统样式

### ✅ 需求 11: 字段类型验证
- 指标区仅接受指标字段（fieldType: 'metric'）
- 维度区仅接受维度字段（fieldType: 'dimension'）
- 过滤器区接受所有字段类型
- 数量区仅接受指标字段
- 无效字段显示警告消息

### ✅ 需求 12: 持久化和状态管理
- 配置存储在组件的 dataConfig 中
- 选择图表时自动恢复配置
- 支持保存到仪表板

## 文件结构

```
ui/src/components/DataConfigPanel/
└── index.vue           # 主组件文件

ui/src/api/bi/
└── dataset.js          # 添加了 getDatasetFields 方法

ui/src/components/ConfigPanel/
└── index.vue           # 更新为使用新的 DataConfigPanel
```

## 使用方式

### 基本用法

```vue
<template>
  <data-config-panel
    :initial-config="component.dataConfig"
    @config-change="handleConfigChange"
  />
</template>

<script>
import DataConfigPanel from '@/components/DataConfigPanel/index.vue'

export default {
  components: {
    DataConfigPanel
  },
  methods: {
    handleConfigChange(config) {
      console.log('配置更新:', config)
      // config 包含:
      // {
      //   dataSourceId: 1,
      //   datasetId: 2,
      //   metrics: [{ fieldName: 'amount', fieldType: 'metric', aggregation: 'sum' }],
      //   dimensions: [{ fieldName: 'category', fieldType: 'dimension' }],
      //   filters: [{ fieldName: 'date', operator: '>', value: '2024-01-01' }],
      //   count: { fieldName: 'id', fieldType: 'metric', aggregation: 'count' }
      // }
    }
  }
}
</script>
```

### 数据格式

#### 字段元数据格式（后端返回）
```javascript
{
  fieldName: 'amount',
  fieldType: 'metric',  // 或 'dimension'
  comment: '销售金额'
}
```

#### 配置输出格式
```javascript
{
  dataSourceId: 1,
  datasetId: 2,
  metrics: [
    {
      fieldName: 'amount',
      fieldType: 'metric',
      aggregation: 'sum'
    }
  ],
  dimensions: [
    {
      fieldName: 'category',
      fieldType: 'dimension'
    }
  ],
  filters: [
    {
      fieldName: 'date',
      fieldType: 'dimension',
      operator: '>',
      value: '2024-01-01'
    }
  ],
  count: {
    fieldName: 'id',
    fieldType: 'metric',
    aggregation: 'count'
  }
}
```

## 特性说明

### 1. 拖放功能
- 使用 Sortable.js 和 vuedraggable 实现
- 支持跨区域拖动
- 拖动时有视觉反馈

### 2. 字段管理
- 点击字段项可删除
- 过滤器字段点击可编辑操作符和值
- 数量区自动替换现有字段

### 3. 字段验证
- 拖动时验证字段类型
- 无类型自动显示警告并移除

### 4. 过滤器编辑
- 点击过滤器字段打开编辑对话框
- 支持的操作符：=, !=, >, <, >=, <=, like, not like
- 可自定义过滤值

### 5. 搜索功能
- 实时过滤字段列表
- 同时搜索字段名和注释

## 后端 API 要求

确保后端实现以下 API：

### 1. 获取字段元数据
```
GET /bi/dataset/{id}/fields

Response:
{
  "code": 200,
  "data": [
    {
      "fieldName": "category",
      "fieldType": "dimension",
      "comment": "产品类别"
    },
    {
      "fieldName": "amount",
      "fieldType": "metric",
      "comment": "销售金额"
    }
  ]
}
```

## 样式定制

组件使用了以下 Element UI 变量，可以在全局样式中覆盖：

```scss
// 主色调
--color-primary: #409eff;

// 背景色
--background-base: #f5f7fa;

// 边框色
--border-base: #e6e6e6;

// 文字颜色
--color-regular: #606266;
--color-text-primary: #303133;
--color-text-secondary: #909399;
```

## 测试建议

1. **功能测试**
   - 测试数据源和数据集选择
   - 测试字段拖拽功能
   - 测试字段删除功能
   - 测试过滤器编辑
   - 测试配置保存和恢复

2. **边界测试**
   - 没有数据源时
   - 没有数据集时
   - 数据集没有字段时
   - 拖入无效字段类型时
   - 数量区拖入第二个字段时

3. **集成测试**
   - 与仪表板设计器集成
   - 配置保存到数据库
   - 从数据库恢复配置

## 已知限制

1. 后端 API `/bi/dataset/{id}/fields` 需要实现
2. 字段类型 `fieldType` 必须为 'dimension' 或 'metric'
3. 配置面板宽度固定为 400px，不支持响应式调整

## 未来改进

1. 支持自定义聚合方式（avg、max、min 等）
2. 支持字段排序
3. 支持批量操作
4. 支持配置模板
5. 支持字段格式化
