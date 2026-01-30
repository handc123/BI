# QueryConditionConfig 组件

## 概述

QueryConditionConfig 是一个用于配置仪表板查询条件及其与图表字段映射关系的对话框组件。它实现了三列布局设计,提供完整的查询条件管理功能。

## 功能特性

### 1. 三列布局

- **左侧列**: 条件列表管理
- **中间列**: 图表字段映射配置
- **右侧列**: 条件属性配置

### 2. 条件管理功能

- ✅ 添加新查询条件
- ✅ 重命名条件
- ✅ 拖拽排序(使用 vuedraggable)
- ✅ 显示/隐藏切换
- ✅ 删除条件(带确认)

### 3. 字段映射功能

- ✅ 选择要映射的图表组件
- ✅ 自动字段匹配
- ✅ 自定义字段映射
- ✅ 多对多映射支持(一个条件可映射多个图表)
- ✅ 映射类型标识(自动/自定义)

### 4. 条件属性配置

#### 通用属性
- 条件名称
- 条件类型(时间/下拉/单选/多选/数值范围)
- 是否必填
- 是否显示
- 默认值
- 父级条件(级联支持)

#### 时间类型特有配置
- 时间粒度(日/周/月/季度/年)
- 范围类型(相对/绝对)
- 日期格式

#### 下拉/单选/多选类型特有配置
- 是否多选
- 数据源类型(静态/API)
- 静态选项列表编辑
- API数据源配置(URL、标签字段、值字段)

#### 数值范围类型特有配置
- 最小值
- 最大值
- 步长

## 使用方法

```vue
<template>
  <query-condition-config
    :visible.sync="configDialogVisible"
    :dashboard-id="currentDashboard.id"
    :conditions="queryConditions"
    :components="dashboardComponents"
    @save="handleConditionSave"
    @close="handleConfigClose"
  />
</template>

<script>
import QueryConditionConfig from '@/components/QueryConditionConfig'

export default {
  components: {
    QueryConditionConfig
  },
  data() {
    return {
      configDialogVisible: false,
      currentDashboard: { id: 1 },
      queryConditions: [],
      dashboardComponents: []
    }
  },
  methods: {
    handleConditionSave({ conditions, mappings }) {
      this.queryConditions = conditions
      // 更新条件映射
      console.log('保存的映射:', mappings)
    },
    handleConfigClose() {
      this.configDialogVisible = false
    }
  }
}
</script>
```

## Props

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| visible | Boolean | 是 | false | 对话框显示状态 |
| dashboardId | String/Number | 是 | - | 仪表板ID |
| conditions | Array | 否 | [] | 查询条件列表 |
| components | Array | 否 | [] | 仪表板组件列表 |

## Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| save | { conditions, mappings } | 保存配置时触发 |
| close | - | 关闭对话框时触发 |
| update:visible | Boolean | 更新 visible 状态 |

## 数据结构

### QueryCondition (查询条件)

```javascript
{
  id: String/Number,           // 条件ID
  dashboardId: Number,         // 仪表板ID
  conditionName: String,       // 条件名称
  conditionType: String,       // 条件类型: time/dropdown/radio/checkbox/range
  displayOrder: Number,        // 显示顺序
  isRequired: String,          // 是否必填: '0'/'1'
  isVisible: String,           // 是否显示: '0'/'1'
  defaultValue: String,        // 默认值
  config: String,              // 配置JSON字符串
  parentConditionId: Number    // 父条件ID(级联)
}
```

### ConditionMapping (条件映射)

```javascript
{
  conditionId: Number,         // 条件ID
  componentId: Number,         // 组件ID
  fieldName: String,           // 映射字段名
  mappingType: String          // 映射类型: auto/custom
}
```

## 技术实现

### 依赖
- Element UI (对话框、表单、按钮等组件)
- vuedraggable (拖拽排序)
- API 服务 (@/api/bi/query)

### 核心方法

- `initDialog()`: 初始化对话框数据
- `loadConditionMappings()`: 加载条件映射
- `updateChartMappingStatus()`: 更新图表映射状态
- `handleAddCondition()`: 添加新条件
- `handleMappingToggle()`: 切换映射状态
- `handleSave()`: 保存配置
- `validateConfig()`: 验证配置

### 自动字段匹配

组件支持智能字段匹配,当启用条件映射时,会尝试根据条件名称自动匹配图表字段:

```javascript
const autoMatchField = fields.find(f => 
  f.label.includes(this.selectedCondition.conditionName) ||
  this.selectedCondition.conditionName.includes(f.label)
)
```

## 验证需求

该组件实现了以下需求:

- ✅ 需求 4.1: 三列布局(条件列表、图表字段映射、条件配置)
- ✅ 需求 4.2: 条件添加、重命名、排序、显示/隐藏
- ✅ 需求 4.3: 自动字段匹配和自定义映射
- ✅ 需求 4.4: 多对多映射支持
- ✅ 需求 4.5: 条件属性配置(必填、显示类型、默认值等)

## 样式特点

- 响应式三列布局
- 拖拽排序视觉反馈
- 选中状态高亮
- 悬停显示操作按钮
- 空状态友好提示
- 表单紧凑布局

## 注意事项

1. 组件使用 `.sync` 修饰符管理 `visible` 状态
2. 条件配置存储为 JSON 字符串,需要序列化/反序列化
3. 临时条件ID使用 `temp_` 前缀,保存时会生成真实ID
4. 删除条件会同时删除相关的条件映射
5. 支持级联条件配置,但需要后端API支持


## 级联条件功能 (Task 7.3) ⭐

### 功能概述

级联条件功能允许创建父子依赖关系的查询条件,当父条件的值改变时,子条件的选项会自动更新。

### 核心特性

1. **父级条件选择**: 为任意条件指定父条件,建立级联关系
2. **循环依赖检测**: 自动检测并阻止循环依赖的级联关系
3. **级联参数配置**: 使用 `${conditionId}` 占位符引用父条件值
4. **自动模式切换**: 设置父条件时自动切换到API数据源模式
5. **子条件提示**: 显示当前条件的所有子条件
6. **测试功能**: 提供级联选项加载测试工具

### 级联配置示例

#### 示例1: 机构-支行二级级联

```javascript
// 父条件: 机构选择
{
  id: 'org',
  conditionName: '机构',
  conditionType: 'dropdown',
  config: {
    dataSourceType: 'api',
    dataSource: {
      url: '/api/organizations',
      labelField: 'name',
      valueField: 'code'
    }
  },
  parentConditionId: null
}

// 子条件: 支行选择
{
  id: 'branch',
  conditionName: '支行',
  conditionType: 'dropdown',
  config: {
    dataSourceType: 'api',
    dataSource: {
      url: '/api/branches',
      labelField: 'name',
      valueField: 'code',
      params: {
        orgCode: '${org}'  // 引用父条件值
      }
    }
  },
  parentConditionId: 'org'
}
```

#### 示例2: 省-市-区三级级联

```javascript
// 省份
{
  id: 'province',
  conditionName: '省份',
  conditionType: 'dropdown',
  parentConditionId: null
}

// 城市(依赖省份)
{
  id: 'city',
  conditionName: '城市',
  conditionType: 'dropdown',
  config: {
    dataSource: {
      url: '/api/cities',
      params: { provinceId: '${province}' }
    }
  },
  parentConditionId: 'province'
}

// 区县(依赖城市)
{
  id: 'district',
  conditionName: '区县',
  conditionType: 'dropdown',
  config: {
    dataSource: {
      url: '/api/districts',
      params: { cityId: '${city}' }
    }
  },
  parentConditionId: 'city'
}
```

### 核心算法

#### 1. 级联选项加载算法

```javascript
/**
 * 加载级联条件选项
 * @param {Object} condition - 查询条件
 * @param {Object} parentValues - 父条件值 { conditionId: value }
 * @returns {Promise<Array>} 选项列表
 */
async loadCascadeOptions(condition, parentValues) {
  // 1. 检查是否为级联条件
  if (!condition.parentConditionId) {
    return this.loadNormalOptions(condition)
  }
  
  // 2. 检查父条件是否有值
  const parentValue = parentValues[condition.parentConditionId]
  if (!parentValue) {
    return [] // 父条件未选择,返回空
  }
  
  // 3. 解析配置
  const config = this.parseConfig(condition)
  const dataSource = config.dataSource
  
  // 4. 替换参数中的占位符
  const params = { ...dataSource.params }
  Object.keys(params).forEach(key => {
    const value = params[key]
    if (typeof value === 'string' && value.startsWith('${')) {
      // 提取条件ID: ${conditionId} -> conditionId
      const conditionId = value.slice(2, -1)
      params[key] = parentValues[conditionId]
    }
  })
  
  // 5. 发起API请求
  const response = await this.$axios({
    url: dataSource.url,
    method: 'get',
    params: params
  })
  
  // 6. 转换为选项格式
  const data = response.data || []
  return data.map(item => ({
    label: item[dataSource.labelField || 'label'],
    value: item[dataSource.valueField || 'value']
  }))
}
```

#### 2. 循环依赖检测算法

```javascript
/**
 * 检查是否会造成循环依赖
 * @param {String} parentId - 父条件ID
 * @param {String} childId - 子条件ID
 * @returns {Boolean} 是否存在循环依赖
 */
wouldCreateCircularDependency(parentId, childId) {
  const visited = new Set()
  let current = parentId
  
  while (current) {
    if (current === childId) {
      return true // 发现循环
    }
    
    if (visited.has(current)) {
      return true // 发现循环
    }
    
    visited.add(current)
    
    // 查找当前条件的父条件
    const condition = this.localConditions.find(c => c.id === current)
    current = condition ? condition.parentConditionId : null
  }
  
  return false
}
```

### 新增方法

- `wouldCreateCircularDependency(parentId, childId)`: 检查循环依赖
- `getParentConditionName()`: 获取父条件名称
- `getChildConditions()`: 获取子条件列表
- `handleParentConditionChange(parentId)`: 处理父条件变化
- `addCascadeParam()`: 添加级联参数
- `removeCascadeParam(key)`: 删除级联参数
- `testCascadeOptions()`: 测试级联选项加载
- `loadCascadeOptions(condition, parentValues)`: 加载级联选项
- `loadNormalOptions(condition)`: 加载普通选项
- `parseConfig(condition)`: 解析条件配置

### 验证增强

级联配置验证包括:
- 循环依赖检测
- 级联参数引用检查
- API数据源必填验证

### 最佳实践

1. **命名规范**: 使用清晰的层级命名,如"省份"、"城市"、"区县"
2. **参数命名**: 使用语义化的参数名,如 `parentId`、`provinceId`
3. **错误处理**: API应返回空数组而不是错误,当父条件无效时
4. **性能优化**: 考虑缓存常用的级联选项
5. **用户体验**: 在父条件变化时显示加载状态
6. **数据验证**: 确保父条件值有效后再加载子条件选项

### 需求验证

- ✅ 需求 4.6: 级联条件依赖性支持

