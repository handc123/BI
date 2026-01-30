# ConfigPanel 组件

配置面板组件,用于配置仪表板设计器中的组件和仪表板设置。

## 组件结构

```
ConfigPanel/
├── index.vue              # 主配置面板组件
├── DataConfig.vue         # 数据配置组件
├── StyleConfig.vue        # 样式配置组件
├── AdvancedConfig.vue     # 高级配置组件
└── DashboardConfig.vue    # 仪表板配置组件
```

## 使用方法

### 基本用法

```vue
<template>
  <config-panel
    :target="selectedTarget"
    :target-type="targetType"
    @config-change="handleConfigChange"
  />
</template>

<script>
import ConfigPanel from '@/components/ConfigPanel'

export default {
  components: {
    ConfigPanel
  },
  data() {
    return {
      selectedTarget: null,  // 选中的组件或仪表板
      targetType: 'component' // 'component' 或 'dashboard'
    }
  },
  methods: {
    handleConfigChange({ type, config }) {
      console.log('配置类型:', type)
      console.log('配置内容:', config)
      // 更新组件或仪表板配置
    }
  }
}
</script>
```

## Props

### ConfigPanel

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| target | 配置目标(组件或仪表板) | Object | - | null |
| targetType | 目标类型 | String | component / dashboard | component |

## Events

### ConfigPanel

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| config-change | 配置发生变化时触发 | { type: String, config: Object } |

## 子组件说明

### DataConfig

图表数据配置组件,包含:
- 数据集选择和切换
- 维度配置(类别轴、子类别)
- 指标配置(值轴、聚合方法)
- 高级选项(下钻、过滤器、限制、刷新频率)
- 配置验证

**验证需求**: 3.2, 3.3, 3.4, 3.5, 3.7

### StyleConfig

样式配置组件,支持:
- 图表样式配置(颜色、字体、图例、坐标轴)
- 仪表板样式配置(主题、背景、边距、网格)

**验证需求**: 3.6, 5.2, 5.3, 5.4

### AdvancedConfig

高级配置组件,包含:
- 下钻配置
- 交互配置(工具提示、缩放、图例选择)
- 数据处理(空值处理、排序、限制)
- 刷新配置
- 导出配置
- 性能优化(渐进渲染、采样)

### DashboardConfig

仪表板级配置组件,包含:
- 全局样式配置(配色方案、标题样式、字体)
- 数字格式配置(千位分隔符、单位)
- 货币格式配置
- 日期格式配置
- 其他设置(动画、工具提示、图例)

**验证需求**: 5.5, 5.6

## 配置数据结构

### 组件数据配置

```javascript
{
  datasetId: 'dataset_001',
  dimensions: [
    { field: 'date', axis: 'x', label: '日期' }
  ],
  metrics: [
    { field: 'amount', aggregation: 'sum', label: '金额', axis: 'left' }
  ],
  drillDown: {
    enabled: false,
    dimensions: []
  },
  filters: [
    { field: 'status', operator: 'eq', value: 'active' }
  ],
  limit: 1000,
  refreshInterval: 0
}
```

### 组件样式配置

```javascript
{
  colorScheme: 'default',
  colors: ['#5470c6', '#91cc75', '#fac858'],
  fontFamily: 'Microsoft YaHei',
  titleFontSize: 16,
  titleColor: '#333333',
  titleFontWeight: 'bold',
  legend: {
    show: true,
    position: 'top',
    align: 'center'
  },
  xAxis: {
    show: true,
    name: '',
    labelRotate: 0
  },
  yAxis: {
    show: true,
    name: '',
    position: 'left'
  },
  grid: {
    top: 60,
    right: 20,
    bottom: 60,
    left: 60
  }
}
```

### 仪表板配置

```javascript
{
  colorScheme: 'default',
  colors: ['#5470c6', '#91cc75', '#fac858'],
  titleStyle: {
    fontSize: 16,
    color: '#333333',
    fontWeight: 'bold',
    align: 'left'
  },
  fontFamily: 'Microsoft YaHei',
  baseFontSize: 14,
  numberFormat: {
    thousandsSeparator: true,
    decimalPlaces: 2,
    unit: '',
    unitPosition: 'suffix',
    negativeFormat: 'minus'
  },
  currencyFormat: {
    symbol: '¥',
    position: 'prefix'
  },
  dateFormat: {
    format: 'YYYY-MM-DD',
    timeFormat: 'HH:mm:ss'
  },
  enableAnimation: true,
  animationDuration: 1000,
  enableTooltip: true,
  enableLegend: true
}
```

## 注意事项

1. **配置验证**: DataConfig 组件会自动验证配置完整性,并显示错误消息
2. **数据集字段**: 需要先选择数据集才能配置维度和指标
3. **配置持久化**: 配置变化会通过 `config-change` 事件向上传递,需要在父组件中处理持久化
4. **样式继承**: 仪表板级配置会影响所有组件的默认样式
5. **性能优化**: 大数据量时建议启用渐进渲染和采样

## 集成示例

```vue
<template>
  <div class="dashboard-designer">
    <designer-toolbar />
    <designer-canvas
      :components="components"
      @component-select="handleComponentSelect"
    />
    <config-panel
      :target="selectedTarget"
      :target-type="selectedTargetType"
      @config-change="handleConfigChange"
    />
  </div>
</template>

<script>
import DesignerToolbar from '@/components/DesignerToolbar'
import DesignerCanvas from '@/components/DesignerCanvas'
import ConfigPanel from '@/components/ConfigPanel'

export default {
  components: {
    DesignerToolbar,
    DesignerCanvas,
    ConfigPanel
  },
  data() {
    return {
      components: [],
      selectedComponent: null,
      currentDashboard: null
    }
  },
  computed: {
    selectedTarget() {
      return this.selectedComponent || this.currentDashboard
    },
    selectedTargetType() {
      return this.selectedComponent ? 'component' : 'dashboard'
    }
  },
  methods: {
    handleComponentSelect(component) {
      this.selectedComponent = component
    },
    handleConfigChange({ type, config }) {
      if (this.selectedComponent) {
        // 更新组件配置
        if (type === 'data') {
          this.selectedComponent.dataConfig = config
        } else if (type === 'style') {
          this.selectedComponent.styleConfig = config
        } else if (type === 'advanced') {
          this.selectedComponent.advancedConfig = config
        }
      } else if (this.currentDashboard) {
        // 更新仪表板配置
        if (type === 'dashboard') {
          this.currentDashboard.globalStyle = JSON.stringify(config)
        } else if (type === 'style') {
          this.currentDashboard.canvasConfig = JSON.stringify(config)
        }
      }
    }
  }
}
</script>
```

## 相关文档

- [仪表板设计器需求文档](../../../.kiro/specs/dashboard-designer/requirements.md)
- [仪表板设计器设计文档](../../../.kiro/specs/dashboard-designer/design.md)
- [仪表板设计器任务列表](../../../.kiro/specs/dashboard-designer/tasks.md)
