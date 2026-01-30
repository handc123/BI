/**
 * 默认值处理工具
 * 用于处理查询条件配置中的默认值
 */

/**
 * 时间范围类型到实际时间范围的映射
 */
const TIME_RANGE_MAPPING = {
  'today': () => {
    const now = new Date()
    return {
      start: new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0),
      end: new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
    }
  },
  'thisWeek': () => {
    const now = new Date()
    const dayOfWeek = now.getDay()
    const startOfWeek = new Date(now)
    startOfWeek.setDate(now.getDate() - dayOfWeek)
    startOfWeek.setHours(0, 0, 0, 0)

    const endOfWeek = new Date(startOfWeek)
    endOfWeek.setDate(startOfWeek.getDate() + 6)
    endOfWeek.setHours(23, 59, 59, 999)

    return { start: startOfWeek, end: endOfWeek }
  },
  'thisMonth': () => {
    const now = new Date()
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0)
    const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59)

    return { start: startOfMonth, end: endOfMonth }
  },
  'thisQuarter': () => {
    const now = new Date()
    const quarter = Math.floor(now.getMonth() / 3)
    const startOfQuarter = new Date(now.getFullYear(), quarter * 3, 1, 0, 0, 0)
    const endOfQuarter = new Date(now.getFullYear(), quarter * 3 + 3, 0, 23, 59, 59)

    return { start: startOfQuarter, end: endOfQuarter }
  },
  'thisYear': () => {
    const now = new Date()
    const startOfYear = new Date(now.getFullYear(), 0, 1, 0, 0, 0)
    const endOfYear = new Date(now.getFullYear(), 11, 31, 23, 59, 59)

    return { start: startOfYear, end: endOfYear }
  },
  'last7Days': () => {
    const now = new Date()
    const start = new Date(now)
    start.setDate(now.getDate() - 6)
    start.setHours(0, 0, 0, 0)

    const end = new Date()
    end.setHours(23, 59, 59, 999)

    return { start, end }
  },
  'last30Days': () => {
    const now = new Date()
    const start = new Date(now)
    start.setDate(now.getDate() - 29)
    start.setHours(0, 0, 0, 0)

    const end = new Date()
    end.setHours(23, 59, 59, 999)

    return { start, end }
  }
}

/**
 * 计算时间范围
 * @param {string} rangeType - 时间范围类型
 * @param {string} granularity - 时间粒度
 * @returns {Object} 包含 start 和 end 的时间范围对象
 *
 * @example
 * calculateTimeRange('today', 'day')
 * // returns { start: Date, end: Date }
 * calculateTimeRange('thisMonth', 'month')
 * // returns { start: Date, end: Date }
 */
export function calculateTimeRange(rangeType, granularity) {
  if (!rangeType || !TIME_RANGE_MAPPING[rangeType]) {
    return null
  }

  const range = TIME_RANGE_MAPPING[rangeType]()

  // 根据粒度调整时间范围
  if (granularity && range) {
    return adjustRangeByGranularity(range, granularity)
  }

  return range
}

/**
 * 根据时间粒度调整时间范围
 * @param {Object} range - 原始时间范围
 * @param {string} granularity - 时间粒度
 * @returns {Object} 调整后的时间范围
 */
function adjustRangeByGranularity(range, granularity) {
  const adjusted = { ...range }

  switch (granularity) {
    case 'year':
      // 调整到年份级别
      adjusted.start = new Date(adjusted.start.getFullYear(), 0, 1, 0, 0, 0)
      adjusted.end = new Date(adjusted.end.getFullYear(), 11, 31, 23, 59, 59)
      break
    case 'month':
      // 调整到月份级别
      adjusted.start = new Date(adjusted.start.getFullYear(), adjusted.start.getMonth(), 1, 0, 0, 0)
      adjusted.end = new Date(adjusted.end.getFullYear(), adjusted.end.getMonth() + 1, 0, 23, 59, 59)
      break
    case 'day':
      // 调整到日期级别
      adjusted.start = new Date(adjusted.start.getFullYear(), adjusted.start.getMonth(), adjusted.start.getDate(), 0, 0, 0)
      adjusted.end = new Date(adjusted.end.getFullYear(), adjusted.end.getMonth(), adjusted.end.getDate(), 23, 59, 59)
      break
    case 'hour':
      // 调整到小时级别
      adjusted.start.setMinutes(0, 0, 0)
      adjusted.end.setMinutes(59, 59, 999)
      break
    case 'minute':
      // 调整到分钟级别
      adjusted.start.setSeconds(0, 0)
      adjusted.end.setSeconds(59, 999)
      break
    case 'second':
      // 调整到秒级别
      adjusted.start.setMilliseconds(0)
      adjusted.end.setMilliseconds(999)
      break
  }

  return adjusted
}

/**
 * 验证默认值格式
 * @param {any} value - 默认值
 * @param {string} displayType - 显示类型
 * @returns {Object} 验证结果，包含 valid 和 message
 *
 * @example
 * validateDefaultValue('123', 'number') // returns { valid: true, message: null }
 * validateDefaultValue('abc', 'number') // returns { valid: false, message: '默认值必须是有效的数字' }
 */
export function validateDefaultValue(value, displayType) {
  if (value === null || value === undefined || value === '') {
    return { valid: true, message: null } // 空值是有效的
  }

  switch (displayType) {
    case 'number':
      const num = Number(value)
      if (isNaN(num)) {
        return {
          valid: false,
          message: '默认值必须是有效的数字'
        }
      }
      break

    case 'text':
    case 'dropdown':
      if (typeof value !== 'string') {
        return {
          valid: false,
          message: `默认值必须是文本类型`
        }
      }
      break

    case 'time':
    case 'range':
      // 时间类型使用时间范围，不需要验证默认值
      return {
        valid: true,
        message: null
      }
  }

  return { valid: true, message: null }
}

/**
 * 格式化默认值
 * @param {any} value - 原始值
 * @param {string} displayType - 显示类型
 * @returns {any} 格式化后的值
 *
 * @example
 * formatDefaultValue('123', 'number') // returns 123
 * formatDefaultValue('2024-01-01', 'time') // returns Date object
 */
export function formatDefaultValue(value, displayType) {
  if (value === null || value === undefined || value === '') {
    return null
  }

  switch (displayType) {
    case 'number':
      return Number(value)

    case 'text':
    case 'dropdown':
      return String(value)

    case 'time':
    case 'range':
      // 尝试解析为日期
      const date = new Date(value)
      if (!isNaN(date.getTime())) {
        return date
      }
      return value

    default:
      return value
  }
}

/**
 * 将时间范围转换为字符串格式
 * @param {Object} range - 时间范围对象
 * @param {string} format - 输出格式 ('iso', 'timestamp', 'formatted')
 * @returns {Object} 转换后的时间范围
 */
export function serializeTimeRange(range, format = 'iso') {
  if (!range || !range.start || !range.end) {
    return null
  }

  switch (format) {
    case 'iso':
      return {
        start: range.start.toISOString(),
        end: range.end.toISOString()
      }
    case 'timestamp':
      return {
        start: range.start.getTime(),
        end: range.end.getTime()
      }
    case 'formatted':
      return {
        start: formatDate(range.start),
        end: formatDate(range.end)
      }
    default:
      return range
  }
}

/**
 * 格式化日期为标准字符串
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期字符串
 */
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 获取所有支持的时间范围类型
 * @returns {Array<Object>} 时间范围类型列表
 */
export function getSupportedTimeRanges() {
  return [
    { value: 'today', label: '今天' },
    { value: 'thisWeek', label: '本周' },
    { value: 'thisMonth', label: '本月' },
    { value: 'thisQuarter', label: '本季度' },
    { value: 'thisYear', label: '本年' },
    { value: 'last7Days', label: '最近7天' },
    { value: 'last30Days', label: '最近30天' },
    { value: 'custom', label: '自定义范围' }
  ]
}

/**
 * 获取所有支持的时间粒度
 * @returns {Array<Object>} 时间粒度列表
 */
export function getSupportedTimeGranularities() {
  return [
    { value: 'year', label: '年' },
    { value: 'month', label: '月' },
    { value: 'day', label: '日' },
    { value: 'hour', label: '小时' },
    { value: 'minute', label: '分钟' },
    { value: 'second', label: '秒' }
  ]
}

export default {
  calculateTimeRange,
  validateDefaultValue,
  formatDefaultValue,
  serializeTimeRange,
  getSupportedTimeRanges,
  getSupportedTimeGranularities
}
