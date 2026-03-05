# MetricConfigDialog 组件

## 概述

MetricConfigDialog 是一个用于配置数据集指标的对话框组件，支持基础指标和计算指标的配置。

## 功能特性

### 1. 指标类型

- **基础指标**: 使用标准SQL聚合函数(SUM、AVG、COUNT、MAX、MIN)
- **计算指标**: 包含四种类型
  - 条件比率: 基于条件的分子分母比率计算(如不良贷款率)
  - 简单比率: 两个指标之间的直接比率(如资本充足率)
  - 条件求和: 基于条件的聚合计算(如不良贷款总额)
  - 自定义表达式: 支持复杂的数学表达式组合(如综合风险指数)

### 2. 核心功能

- ✅ 指标类型选择(基础/计算)
- ✅ 动态表单(根据类型显示不同字段)
- ✅ 字段选择(从数据集字段列表)
- ✅ 聚合函数选择
- ✅ 条件表达式输入
- ✅ 指标引用选择
- ✅ SQL预览生成
- ✅ 指标测试功能
- ✅ 表单验证(前端验证)
- ✅ 错误消息显示
- ✅ 测试结果展示

## 使用方法

### 基本用法

```vue
<template>
  <div>
    <el-button @click="showDialog">配置指标</el-button>
    
    <metric-config-dialog
      :visible.sync="dialogVisible"
      :dataset-id="datasetId"
      :metric="currentMetric"
      :available-fields="fields"
      :available-metrics="metrics"
      @submit="handleMetricSubmit"
    />
  </div>
</template>

<script>
import MetricConfigDialog from '@/components/MetricConfigDialog'

export default {
  components: {
    MetricConfigDialog
  },
  data() {
    return {
      dialogVisible: false,
      datasetId: 1,
      currentMetric: null,
      fields: [
        { name: 'loan_amount', type: 'DECIMAL' },
        { name: 'loan_status', type: 'VARCHAR' }
      ],
      metrics: [
        { name: 'total_loans', alias: '贷款总额' },
        { name: 'npl_amount', alias: '不良贷款总额' }
      ]
    }
  },
  methods: {
    showDialog() {
      this.dialogVisible = true
    },
    handleMetricSubmit(metricConfig) {
      console.log('指标配置:', metricConfig)
      // 处理指标配置
    }
  }
}
</script>
```

### Props

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| visible | 对话框是否可见 | Boolean | false |
| datasetId | 数据集ID(用于测试) | Number | null |
| metric | 编辑的指标对象(新增时为null) | Object | null |
| availableFields | 可用字段列表 | Array | [] |
| availableMetrics | 可用指标列表(用于简单比率和自定义表达式) | Array | [] |

### Events

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| submit | 提交指标配置 | metricConfig: Object |
| update:visible | 对话框可见性变化 | visible: Boolean |

### 指标配置对象结构

#### 基础指标

```javascript
{
  metricType: 'base',
  name: 'total_loan_amount',
  alias: '贷款总额',
  field: 'loan_amount',
  aggregation: 'SUM'
}
```

#### 条件比率指标

```javascript
{
  metricType: 'computed',
  computeType: 'conditional_ratio',
  name: 'npl_ratio',
  alias: '不良贷款率',
  field: 'loan_amount',
  numeratorCondition: "loan_status = 'NPL'",
  denominatorCondition: "loan_status IN ('NORMAL', 'NPL')",
  asPercentage: true
}
```

#### 简单比率指标

```javascript
{
  metricType: 'computed',
  computeType: 'simple_ratio',
  name: 'capital_adequacy_ratio',
  alias: '资本充足率',
  numeratorMetric: 'total_capital',
  denominatorMetric: 'risk_weighted_assets',
  asPercentage: true
}
```

#### 条件求和指标

```javascript
{
  metricType: 'computed',
  computeType: 'conditional_sum',
  name: 'npl_amount',
  alias: '不良贷款总额',
  field: 'loan_amount',
  condition: "loan_status = 'NPL'"
}
```

#### 自定义表达式指标

```javascript
{
  metricType: 'computed',
  computeType: 'custom_expression',
  name: 'risk_index',
  alias: '综合风险指数',
  expression: '(npl_ratio * 0.4 + overdue_ratio * 0.3 + watch_ratio * 0.3) * 100'
}
```

## API 集成

组件使用以下后端API:

- `POST /bi/dataset/metric/validate` - 验证指标配置
- `POST /bi/dataset/metric/test` - 测试指标计算

## 表单验证规则

### 指标名称
- 必填
- 只能包含字母、数字和下划线
- 必须以字母或下划线开头
- 最大长度: 100字符

### 指标别名
- 必填
- 最大长度: 200字符

### 字段名
- 必填(基础指标、条件比率、条件求和)
- 从可用字段列表中选择

### 聚合函数
- 必填(基础指标)
- 可选值: SUM、AVG、COUNT、MAX、MIN

### 条件表达式
- 必填(条件比率、条件求和)
- 最大长度: 500字符

### 指标引用
- 必填(简单比率)
- 从可用指标列表中选择

### 自定义表达式
- 必填(自定义表达式)
- 最大长度: 500字符
- 支持运算符: +、-、*、/、()

## 特性说明

### SQL预览
- 点击"生成预览"按钮可查看生成的SQL表达式
- 预览在客户端生成，仅供参考

### 指标测试
- 点击"测试"按钮执行实际查询
- 显示查询结果和执行时间
- 需要提供有效的datasetId

### 错误处理
- 前端表单验证
- 后端安全验证(SQL注入防护)
- 清晰的错误消息提示

### 指标引用助手
- 在自定义表达式中点击"可用指标"按钮
- 显示所有可用指标列表
- 点击"插入"可快速添加指标引用

## 注意事项

1. **安全性**: 所有用户输入都会经过后端SQL验证器验证，防止SQL注入
2. **性能**: 建议单个数据集配置不超过20个指标
3. **依赖关系**: 简单比率和自定义表达式需要先配置基础指标
4. **测试功能**: 测试查询会添加LIMIT子句，只返回样本数据

## 相关文档

- [需求文档](../../.kiro/specs/enhanced-data-config/requirements.md)
- [设计文档](../../.kiro/specs/enhanced-data-config/design.md)
- [任务列表](../../.kiro/specs/enhanced-data-config/tasks.md)
