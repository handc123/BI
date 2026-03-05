# 仪表板设计器组件分析

## 路由和组件映射

### 1. `/dashboard/designer` 路由
- **文件**: `ui/src/views/bi/dashboard/designer.vue`
- **使用的配置面板**: `ConfigPanel` (`ui/src/components/ConfigPanel/index.vue`)
- **数据配置组件**: 现在使用 `DataConfig.vue`（已包含指标配置功能）

### 2. BiDashboardDesigner 组件
- **文件**: `ui/src/components/BiDashboardDesigner/index.vue`
- **使用的配置**: 右侧只有简单的位置/大小配置
- **查询条件配置**: 使用 `QueryConditionConfig` 对话框
- **注意**: 这个组件没有使用 ConfigPanel，也没有完整的数据配置功能

## 数据配置组件的演变

### 原始组件
- **DataConfigPanel** (`ui/src/components/DataConfigPanel/index.vue`)
  - 拖放式字段配置
  - 没有指标配置功能
  - 之前被 ConfigPanel 使用

### 增强组件
- **DataConfig** (`ui/src/components/ConfigPanel/DataConfig.vue`)
  - 包含三个选项卡：
    1. 数据源配置
    2. 基础指标 ⭐ 新增
    3. 计算指标 ⭐ 新增
  - 现在被 ConfigPanel 使用

## 当前状态

✅ **已完成的集成**:
1. ConfigPanel 已更新为使用 DataConfig 组件
2. DataConfig 组件包含完整的指标配置功能
3. API 服务已创建 (`ui/src/api/bi/metric.js`)
4. 后端 API 已实现

## 如何查看指标配置功能

### 方法 1: 通过 /dashboard/designer 路由

1. 访问 http://localhost:82
2. 登录系统
3. 进入 "BI平台" → "仪表板管理"
4. 点击"新建"或"编辑"现有仪表板
5. 在设计器中：
   - 从左侧组件库拖入图表组件
   - 点击图表组件选中它
   - 在右侧配置面板中，点击"数据"选项卡
   - 你会看到三个子选项卡：
     - 数据源配置
     - 基础指标 ⭐
     - 计算指标 ⭐

### 方法 2: 直接访问设计器

访问: http://localhost:82/#/bi/dashboard/designer

## 组件使用关系图

```
/dashboard/designer (路由)
    ↓
designer.vue (页面)
    ↓
ConfigPanel (配置面板)
    ↓
DataConfig (数据配置) ← 包含指标配置功能
    ↓
MetricConfigDialog (指标配置对话框)
```

## 注意事项

1. **BiDashboardDesigner** 组件是一个独立的设计器实现，它没有使用 ConfigPanel
2. 如果你在使用 BiDashboardDesigner，需要单独为它添加指标配置功能
3. 当前的指标配置功能只在通过 ConfigPanel 的设计器中可用

## 验证步骤

1. 清除浏览器缓存（Ctrl+Shift+R）
2. 访问 /dashboard/designer 路由
3. 添加一个图表组件
4. 选中该组件
5. 在右侧配置面板的"数据"选项卡中查看是否有三个子选项卡

## 如果看不到新功能

检查以下内容：
1. 前端是否正在运行（http://localhost:82）
2. 浏览器控制台是否有错误
3. 是否选中了图表组件
4. 是否在正确的"数据"选项卡中
