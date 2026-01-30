# ChartWidget 组件

## 概述

ChartWidget 是仪表板设计器的核心图表组件,负责接收查询参数、执行数据查询、渲染图表,并支持自动刷新和并行刷新功能。

## 功能特性

### 1. 查询参数注入

- 自动接收来自 QueryWidget 的查询参数
- 通过 Vuex store 管理查询参数状态
- 使用 `queryParamsInjector` 工具注入参数到图表查询
- 支持条件映射(多对多关系)

### 2. 条件变化触发刷新

- 监听 `mergedQueryParams` 的变化
- 参数变化时自动触发图表刷新
- 深度比较避免不必要的刷新

### 3. 多图表并行刷新

- 多个图表独立监听查询参数
- 参数变化时所有关联图表并行刷新
- 错误隔离:单个图表失败不影响其他图表

### 4. 自动刷新定时器

- 支持配置刷新频率(秒)
- 自动启动/停止定时器
- 编辑模式下禁用自动刷新
- 组件销毁时自动清理定时器

## Props

| 属性 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| config | Object | 是 | - | 图表配置对象 |
| queryParams | Object | 否 | {} | 查询参数(来自 Vuex store) |
| conditionMappings | Array | 否 | [] | 条件映射列表 |
| queryConditions | Array | 否 | [] | 查询条件列表 |
| isEditMode | Boolean | 否 | false | 是否编辑模式 |
| widgetStyle | Object | 否 | {} | 组件样式 |

## Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| data-loaded | chartData | 数据加载完成 |
| data-error | error | 数据加载失败 |
| drill-down | drillDownInfo | 下钻事件 |
| click | config | 组件点击 |
| config | config | 配置按钮点击 |

## 配置对象结构

```javascript
{
  id: 'chart_001',
  type: 'chart',
  name: '销售趋势图',
  dataConfig: {
    datasetId: 1,                    // 数据集ID
    dimensions: [                     // 维度配置
      { field: 'date', axis: 'x', label: '日期' }
    ],
    metrics: [                        // 指标配置
      { field: 'sales', aggregation: 'sum', label: '销售额' }
    ],
    filters: [],                      // 图表级过滤器
    limit: 1000,                      // 结果限制
    refreshInterval: 30               // 刷新频率(秒), 0表示不自动刷新
  },
  styleConfig: {
    chartType: 'line',                // 图表类型
    colors: ['#5470c6'],              // 颜色配置
    showLegend: true,                 // 显示图例
    smooth: true,                     // 平滑曲线
    // ... 其他样式配置
  },
  advancedConfig: {
    drillDown: {                      // 下钻配置
      enabled: false,
      dimensions: []
    }
  }
}
```

## 使用示例

### 基础使用

```vue
<template>
  <chart-widget
    :config="chartConfig"
    :query-params="queryValues"
    :condition-mappings="conditionMappings"
    :query-conditions="queryConditions"
    :is-edit-mode="false"
    @data-loaded="handleDataLoaded"
    @data-error="handleDataError"
  />
</template>

<script>
import ChartWidget from '@/components/ChartWidget'
import { mapGetters } from 'vuex'

export default {
  components: { ChartWidget },
  computed: {
    ...mapGetters('dashboard', ['queryValues'])
  },
  data() {
    return {
      chartConfig: {
        id: 'chart_001',
        type: 'chart',
        name: '销售趋势图',
        dataConfig: {
          datasetId: 1,
          dimensions: [{ field: 'date', axis: 'x', label: '日期' }],
          metrics: [{ field: 'sales', aggregation: 'sum', label: '销售额' }],
          refreshInterval: 30  // 30秒自动刷新
        },
        styleConfig: {
          chartType: 'line'
        }
      },
      conditionMappings: [
        {
          conditionId: 'cond_001',
          componentId: 'chart_001',
          fieldName: 'date'
        }
      ],
      queryConditions: [
        {
          id: 'cond_001',
          conditionName: '时间范围',
          conditionType: 'time',
          isRequired: '1'
        }
      ]
    }
  },
  methods: {
    handleDataLoaded(data) {
      console.log('数据加载完成:', data)
    },
    handleDataError(error) {
      console.error('数据加载失败:', error)
    }
  }
}
</script>
```

### 多图表并行刷新

```vue
<template>
  <div>
    <!-- 查询组件 -->
    <query-widget
      :conditions="queryConditions"
      @query="handleQuery"
    />

    <!-- 多个图表 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <chart-widget
          :config="chartConfig1"
          :query-params="queryValues"
          :condition-mappings="conditionMappings"
          :query-conditions="queryConditions"
        />
      </el-col>
      <el-col :span="12">
        <chart-widget
          :config="chartConfig2"
          :query-params="queryValues"
          :condition-mappings="conditionMappings"
          :query-conditions="queryConditions"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import QueryWidget from '@/components/QueryWidget'
import ChartWidget from '@/components/ChartWidget'
import { mapGetters } from 'vuex'

export default {
  components: { QueryWidget, ChartWidget },
  computed: {
    ...mapGetters('dashboard', ['queryValues'])
  },
  methods: {
    handleQuery(values) {
      // QueryWidget 会自动更新 Vuex store
      // 所有 ChartWidget 会自动并行刷新
    }
  }
}
</script>
```

## 工作流程

### 1. 初始化流程

```
组件挂载
  ↓
初始化图表实例 (initChart)
  ↓
获取数据 (fetchData)
  ↓
渲染图表 (renderChart)
  ↓
设置自动刷新 (setupAutoRefresh)
```

### 2. 查询参数变化流程

```
QueryWidget 条件变化
  ↓
更新 Vuex store (updateQueryValue)
  ↓
ChartWidget 监听到 mergedQueryParams 变化
  ↓
触发 refreshChart
  ↓
重新获取数据 (fetchData)
  ↓
重新渲染图表 (renderChart)
```

### 3. 自动刷新流程

```
设置刷新频率 (refreshInterval > 0)
  ↓
启动定时器 (setInterval)
  ↓
定时触发 refreshChart
  ↓
重新获取数据
  ↓
重新渲染图表
```

## 查询参数注入算法

ChartWidget 使用 `queryParamsInjector` 工具进行参数注入:

```javascript
// 1. 获取组件的条件映射
const componentMappings = conditionMappings.filter(
  m => m.componentId === component.id
)

// 2. 注入映射的条件值
componentMappings.forEach(mapping => {
  const conditionValue = conditionValues[mapping.conditionId]
  if (conditionValue !== undefined && conditionValue !== null) {
    params[mapping.fieldName] = conditionValue
  }
})

// 3. 合并图表级过滤器
if (component.dataConfig.filters) {
  component.dataConfig.filters.forEach(filter => {
    params[filter.field] = filter.value
  })
}

// 4. 清理空参数
return cleanEmptyParams(params)
```

## 错误处理

### 1. 数据加载错误

- 捕获异常并显示错误信息
- 提供重试按钮
- 触发 `data-error` 事件
- 不影响其他图表

### 2. 图表渲染错误

- 捕获渲染异常
- 显示错误提示
- 记录错误日志

### 3. 参数验证错误

- 验证必填参数
- 显示缺失参数提示
- 阻止无效查询

## 性能优化

### 1. 避免不必要的刷新

- 深度比较查询参数
- 只在参数真正变化时刷新

### 2. 并行刷新优化

- 多个图表独立请求
- 不阻塞UI渲染
- 错误隔离

### 3. 定时器管理

- 组件销毁时清理定时器
- 编辑模式下禁用自动刷新
- 避免内存泄漏

## 测试

参考测试页面: `ui/src/views/bi/dashboard/test-chart-refresh.vue`

测试场景:
1. 查询参数注入
2. 条件变化触发刷新
3. 多图表并行刷新
4. 自动刷新定时器
5. 错误处理

## 相关文件

- `ui/src/components/ChartWidget/index.vue` - 组件实现
- `ui/src/components/QueryWidget/index.vue` - 查询组件
- `ui/src/utils/queryParamsInjector.js` - 参数注入工具
- `ui/src/store/modules/dashboard.js` - 状态管理
- `ui/src/api/bi/query.js` - 查询API

## 需求映射

- **需求 4.7**: 查询条件值改变时刷新所有关联图表
- **需求 8.1**: 将更新的参数注入所有映射的图表
- **需求 8.2**: 图表接收到新参数时执行查询并刷新
- **需求 8.3**: 多个图表关联到同一条件时并行刷新
- **需求 8.4**: 图表配置刷新频率时自动刷新
