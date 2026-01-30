/**
 * 显示类型推断工具
 * 根据数据库字段类型自动推断合适的显示类型
 */

/**
 * 数据库类型到显示类型的映射关系
 */
const TYPE_MAPPING = {
  // 日期时间类型 -> 时间选择器
  'DATE': 'time',
  'DATETIME': 'time',
  'TIMESTAMP': 'time',
  'TIME': 'time',
  'YEAR': 'time',

  // 数值类型 -> 数字输入
  'INT': 'number',
  'TINYINT': 'number',
  'SMALLINT': 'number',
  'MEDIUMINT': 'number',
  'BIGINT': 'number',
  'FLOAT': 'number',
  'DOUBLE': 'number',
  'DECIMAL': 'number',
  'NUMERIC': 'number',
  'REAL': 'number',

  // 文本类型 -> 文本输入
  'VARCHAR': 'text',
  'CHAR': 'text',
  'TEXT': 'text',
  'TINYTEXT': 'text',
  'MEDIUMTEXT': 'text',
  'LONGTEXT': 'text',
  'STRING': 'text',

  // 枚举和集合类型 -> 下拉框
  'ENUM': 'dropdown',
  'SET': 'dropdown'
}

/**
 * 根据数据库字段类型推断显示类型
 * @param {string} fieldDataType - 数据库字段类型
 * @returns {string} 推断的显示类型
 *
 * @example
 * inferDisplayType('VARCHAR') // returns 'text'
 * inferDisplayType('INT') // returns 'number'
 * inferDisplayType('DATETIME') // returns 'time'
 * inferDisplayType('ENUM') // returns 'dropdown'
 */
export function inferDisplayType(fieldDataType) {
  if (!fieldDataType || typeof fieldDataType !== 'string') {
    return 'text' // 默认返回文本类型
  }

  // 转换为大写并去除空格
  const normalizedType = fieldDataType.toUpperCase().trim()

  // 精确匹配
  if (TYPE_MAPPING[normalizedType]) {
    return TYPE_MAPPING[normalizedType]
  }

  // 模糊匹配（处理带参数的类型，如 VARCHAR(255)）
  for (const [dbType, displayType] of Object.entries(TYPE_MAPPING)) {
    if (normalizedType.startsWith(dbType)) {
      return displayType
    }
  }

  // 默认返回文本类型
  return 'text'
}

/**
 * 获取所有支持的显示类型
 * @returns {Array<string>} 显示类型列表
 */
export function getSupportedDisplayTypes() {
  return ['text', 'number', 'time', 'range', 'dropdown']
}

/**
 * 获取显示类型的显示名称
 * @param {string} displayType - 显示类型
 * @returns {string} 显示名称
 */
export function getDisplayTypeName(displayType) {
  const names = {
    'text': '文本输入',
    'number': '数字输入',
    'time': '时间选择器',
    'range': '日期范围',
    'dropdown': '下拉框'
  }
  return names[displayType] || displayType
}

/**
 * 检查显示类型是否支持默认值
 * @param {string} displayType - 显示类型
 * @returns {boolean} 是否支持默认值
 */
export function supportsDefaultValue(displayType) {
  return ['text', 'number', 'dropdown'].includes(displayType)
}

/**
 * 检查显示类型是否需要时间粒度
 * @param {string} displayType - 显示类型
 * @returns {boolean} 是否需要时间粒度
 */
export function requiresTimeGranularity(displayType) {
  return ['time', 'range'].includes(displayType)
}

export default {
  inferDisplayType,
  getSupportedDisplayTypes,
  getDisplayTypeName,
  supportsDefaultValue,
  requiresTimeGranularity
}
