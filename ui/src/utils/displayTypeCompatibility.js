/**
 * 显示类型兼容性验证工具
 * 用于验证显示类型与数据库字段类型的兼容性
 */

/**
 * 显示类型与数据库类型的兼容性矩阵
 */
const COMPATIBILITY_MATRIX = {
  'text': [
    'VARCHAR', 'CHAR', 'TEXT', 'TINYTEXT', 'MEDIUMTEXT', 'LONGTEXT',
    'STRING', // 通用字符串类型
    'INT', 'TINYINT', 'SMALLINT', 'MEDIUMINT', 'BIGINT', // 数字也可以作为文本
    'FLOAT', 'DOUBLE', 'DECIMAL', 'NUMERIC',
    'DATE', 'DATETIME', 'TIMESTAMP', 'TIME', 'YEAR' // 日期也可以作为文本
  ],
  'number': [
    'INT', 'TINYINT', 'SMALLINT', 'MEDIUMINT', 'BIGINT',
    'FLOAT', 'DOUBLE', 'DECIMAL', 'NUMERIC', 'REAL'
  ],
  'time': [
    'DATE', 'DATETIME', 'TIMESTAMP', 'TIME', 'YEAR'
  ],
  'range': [
    'DATE', 'DATETIME', 'TIMESTAMP' // 范围选择仅支持完整日期时间类型
  ],
  'dropdown': [
    'VARCHAR', 'CHAR', 'TEXT', 'TINYTEXT',
    'INT', 'TINYINT', 'SMALLINT', 'MEDIUMINT', 'BIGINT',
    'ENUM', 'SET'
  ]
}

/**
 * 检查显示类型与字段类型是否兼容
 * @param {string} displayType - 显示类型
 * @param {string} fieldDataType - 数据库字段类型
 * @returns {boolean} 是否兼容
 *
 * @example
 * isCompatible('text', 'VARCHAR') // returns true
 * isCompatible('number', 'INT') // returns true
 * isCompatible('time', 'DATE') // returns true
 * isCompatible('number', 'VARCHAR') // returns false
 */
export function isCompatible(displayType, fieldDataType) {
  if (!displayType || !fieldDataType) {
    return false
  }

  if (typeof fieldDataType !== 'string') {
    return false
  }

  // 转换为大写并去除空格
  const normalizedFieldType = fieldDataType.toUpperCase().trim()

  // 获取该显示类型支持的数据库类型列表
  const compatibleTypes = COMPATIBILITY_MATRIX[displayType]

  if (!compatibleTypes) {
    return false
  }

  // 精确匹配
  if (compatibleTypes.includes(normalizedFieldType)) {
    return true
  }

  // 模糊匹配（处理带参数的类型，如 VARCHAR(255)）
  for (const compatibleType of compatibleTypes) {
    if (normalizedFieldType.startsWith(compatibleType)) {
      return true
    }
  }

  return false
}

/**
 * 获取与字段类型兼容的显示类型列表
 * @param {string} fieldDataType - 数据库字段类型
 * @returns {Array<string>} 兼容的显示类型列表
 *
 * @example
 * getCompatibleDisplayTypes('VARCHAR') // returns ['text', 'dropdown']
 * getCompatibleDisplayTypes('INT') // returns ['text', 'number', 'dropdown']
 * getCompatibleDisplayTypes('DATETIME') // returns ['text', 'time', 'range']
 */
export function getCompatibleDisplayTypes(fieldDataType) {
  if (!fieldDataType || typeof fieldDataType !== 'string') {
    return []
  }

  const compatibleTypes = []

  for (const displayType of Object.keys(COMPATIBILITY_MATRIX)) {
    if (isCompatible(displayType, fieldDataType)) {
      compatibleTypes.push(displayType)
    }
  }

  return compatibleTypes
}

/**
 * 获取不兼容的原因说明
 * @param {string} displayType - 显示类型
 * @param {string} fieldDataType - 数据库字段类型
 * @returns {string|null} 不兼容的原因，如果兼容则返回 null
 *
 * @example
 * getIncompatibilityReason('number', 'VARCHAR')
 * // returns "数字输入类型不支持字符串类型字段，请使用文本输入或下拉框"
 */
export function getIncompatibilityReason(displayType, fieldDataType) {
  if (isCompatible(displayType, fieldDataType)) {
    return null
  }

  const reasons = {
    'number': `"${fieldDataType}"类型不支持数字输入，建议使用文本输入`,
    'time': `"${fieldDataType}"类型不支持时间选择器，建议使用文本输入`,
    'range': `"${fieldDataType}"类型不支持日期范围选择，建议使用文本输入或时间选择器`,
    'dropdown': `"${fieldDataType}"类型不适合下拉框，建议使用文本输入`
  }

  return reasons[displayType] || `显示类型与字段类型"${fieldDataType}"不兼容`
}

/**
 * 获取显示类型的推荐字段类型
 * @param {string} displayType - 显示类型
 * @returns {Array<string>} 推荐的字段类型列表
 *
 * @example
 * getRecommendedFieldTypes('number')
 * // returns ['INT', 'BIGINT', 'FLOAT', 'DOUBLE', 'DECIMAL', 'NUMERIC']
 */
export function getRecommendedFieldTypes(displayType) {
  return COMPATIBILITY_MATRIX[displayType] || []
}

/**
 * 验证多个字段的兼容性
 * @param {string} displayType - 显示类型
 * @param {Array<Object>} fields - 字段列表，每个字段包含 fieldType 属性
 * @returns {Object} 验证结果，包含 valid 和 incompatibleFields
 *
 * @example
 * const fields = [
 *   { fieldName: 'id', fieldType: 'INT' },
 *   { fieldName: 'name', fieldType: 'VARCHAR' }
 * ]
 * validateMultipleFields('number', fields)
 * // returns { valid: false, incompatibleFields: ['name(VARCHAR)'] }
 */
export function validateMultipleFields(displayType, fields) {
  const incompatibleFields = []

  fields.forEach(field => {
    if (!isCompatible(displayType, field.fieldType)) {
      incompatibleFields.push(
        `${field.fieldName || field.fieldComment || field.fieldId}(${field.fieldType})`
      )
    }
  })

  return {
    valid: incompatibleFields.length === 0,
    incompatibleFields
  }
}

export default {
  isCompatible,
  getCompatibleDisplayTypes,
  getIncompatibilityReason,
  getRecommendedFieldTypes,
  validateMultipleFields
}
