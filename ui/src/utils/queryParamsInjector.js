/**
 * 查询参数注入工具
 * 实现查询条件值到图表查询参数的注入算法
 */

/**
 * 将查询条件值注入到图表查询参数
 * @param {Object} component - 图表组件
 * @param {Array} conditionMappings - 条件映射列表
 * @param {Object} conditionValues - 查询条件值 {conditionId: value}
 * @returns {Object} 合并后的查询参数
 */
export function injectQueryParams(component, conditionMappings, conditionValues) {
  const params = {}

  // 获取该组件的所有映射
  const componentMappings = conditionMappings.filter(
    m => m.componentId === component.id
  )

  // 注入映射的条件值
  componentMappings.forEach(mapping => {
    const conditionValue = conditionValues[mapping.conditionId]
    if (conditionValue !== undefined && conditionValue !== null) {
      params[mapping.fieldName] = conditionValue
    }
  })

  // 合并图表级过滤器
  if (component.dataConfig && component.dataConfig.filters) {
    component.dataConfig.filters.forEach(filter => {
      // 图表级过滤器优先级更高
      if (params[filter.field] === undefined) {
        params[filter.field] = filter.value
      }
    })
  }

  return params
}

/**
 * 获取组件依赖的查询条件ID列表
 * @param {Object} component - 图表组件
 * @param {Array} conditionMappings - 条件映射列表
 * @returns {Array} 条件ID列表
 */
export function getComponentConditionDependencies(component, conditionMappings) {
  return conditionMappings
    .filter(m => m.componentId === component.id)
    .map(m => m.conditionId)
}

/**
 * 获取受条件影响的组件ID列表
 * @param {string} conditionId - 查询条件ID
 * @param {Array} conditionMappings - 条件映射列表
 * @returns {Array} 组件ID列表
 */
export function getAffectedComponents(conditionId, conditionMappings) {
  return conditionMappings
    .filter(m => m.conditionId === conditionId)
    .map(m => m.componentId)
}

/**
 * 批量注入查询参数到多个组件
 * @param {Array} components - 组件列表
 * @param {Array} conditionMappings - 条件映射列表
 * @param {Object} conditionValues - 查询条件值
 * @returns {Object} 组件ID到查询参数的映射 {componentId: params}
 */
export function batchInjectQueryParams(components, conditionMappings, conditionValues) {
  const result = {}

  components.forEach(component => {
    result[component.id] = injectQueryParams(component, conditionMappings, conditionValues)
  })

  return result
}

/**
 * 验证查询参数是否完整
 * @param {Object} component - 图表组件
 * @param {Object} params - 查询参数
 * @param {Array} queryConditions - 查询条件列表
 * @param {Array} conditionMappings - 条件映射列表
 * @returns {Object} 验证结果 {valid: boolean, missing: []}
 */
export function validateQueryParams(component, params, queryConditions, conditionMappings) {
  const missing = []

  // 获取该组件的所有必填条件映射
  const componentMappings = conditionMappings.filter(
    m => m.componentId === component.id
  )

  componentMappings.forEach(mapping => {
    const condition = queryConditions.find(c => c.id === mapping.conditionId)
    if (condition && condition.isRequired === '1') {
      const value = params[mapping.fieldName]
      if (value === undefined || value === null || value === '') {
        missing.push({
          conditionId: condition.id,
          conditionName: condition.conditionName,
          fieldName: mapping.fieldName
        })
      }
    }
  })

  return {
    valid: missing.length === 0,
    missing
  }
}

/**
 * 格式化查询参数值
 * @param {*} value - 原始值
 * @param {Object} condition - 查询条件配置
 * @returns {*} 格式化后的值
 */
export function formatParamValue(value, condition) {
  if (value === undefined || value === null) {
    return null
  }

  switch (condition.conditionType) {
    case 'time':
      // 时间类型格式化
      return formatTimeValue(value, condition.config)

    case 'dropdown':
    case 'select':
      // 下拉选择类型
      return value

    case 'range':
      // 范围类型
      return formatRangeValue(value)

    default:
      return value
  }
}

/**
 * 格式化时间值
 */
function formatTimeValue(value, config) {
  if (!config) return value

  const format = config.format || 'YYYY-MM-DD'

  // 如果是相对时间
  if (config.rangeType === 'relative') {
    return calculateRelativeTime(value)
  }

  // 如果是绝对时间
  if (Array.isArray(value)) {
    // 时间范围
    return value.map(v => formatDate(v, format))
  } else {
    // 单个时间
    return formatDate(value, format)
  }
}

/**
 * 计算相对时间
 */
function calculateRelativeTime(relativeValue) {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())

  switch (relativeValue) {
    case 'today':
      return [today, now]

    case 'yesterday':
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)
      return [yesterday, today]

    case 'last7days':
      const last7days = new Date(today)
      last7days.setDate(last7days.getDate() - 7)
      return [last7days, today]

    case 'last30days':
      const last30days = new Date(today)
      last30days.setDate(last30days.getDate() - 30)
      return [last30days, today]

    case 'thisMonth':
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      return [monthStart, now]

    case 'lastMonth':
      const lastMonthStart = new Date(now.getFullYear(), now.getMonth() - 1, 1)
      const lastMonthEnd = new Date(now.getFullYear(), now.getMonth(), 0)
      return [lastMonthStart, lastMonthEnd]

    default:
      return relativeValue
  }
}

/**
 * 格式化日期
 */
function formatDate(date, format) {
  if (!date) return null

  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 格式化范围值
 */
function formatRangeValue(value) {
  if (Array.isArray(value) && value.length === 2) {
    return {
      min: value[0],
      max: value[1]
    }
  }
  return value
}

/**
 * 合并查询参数
 * @param {Object} baseParams - 基础参数
 * @param {Object} overrideParams - 覆盖参数
 * @returns {Object} 合并后的参数
 */
export function mergeQueryParams(baseParams, overrideParams) {
  return {
    ...baseParams,
    ...overrideParams
  }
}

/**
 * 清理空参数
 * @param {Object} params - 查询参数
 * @returns {Object} 清理后的参数
 */
export function cleanEmptyParams(params) {
  const cleaned = {}

  Object.keys(params).forEach(key => {
    const value = params[key]
    if (value !== undefined && value !== null && value !== '') {
      cleaned[key] = value
    }
  })

  return cleaned
}
