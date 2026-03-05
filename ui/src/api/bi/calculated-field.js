import request from '@/utils/request'

/**
 * 验证计算字段
 * @param {Object} data - 验证请求数据
 * @param {Number} data.datasetId - 数据集ID
 * @param {Object} data.field - 计算字段配置
 * @param {String} data.field.name - 字段名称
 * @param {String} data.field.alias - 字段别名
 * @param {String} data.field.fieldType - 字段类型 (dimension/metric)
 * @param {String} data.field.expression - 表达式
 * @param {String} data.field.aggregation - 聚合方式
 * @returns {Promise} 返回验证结果
 */
export function validateCalculatedField(data) {
  return request({
    url: '/bi/component/calculated-field/validate',
    method: 'post',
    data: data
  })
}

/**
 * 测试计算字段
 * @param {Object} data - 测试请求数据
 * @param {Number} data.datasetId - 数据集ID
 * @param {Object} data.field - 计算字段配置
 * @returns {Promise} 返回测试结果(样本数据)
 */
export function testCalculatedField(data) {
  return request({
    url: '/bi/component/calculated-field/test',
    method: 'post',
    data: data
  })
}

/**
 * 保存图表配置(包含计算字段)
 * 复用现有的component API保存方法
 * @param {Object} data - 组件数据
 * @param {Object} data.configJson - 组件配置JSON
 * @param {Object} data.configJson.dataConfig - 数据配置
 * @param {Array} data.configJson.dataConfig.calculatedFields - 计算字段列表
 * @returns {Promise} 返回保存结果
 */
export function saveComponentWithCalculatedFields(data) {
  return request({
    url: '/bi/dashboard/component',
    method: 'put',
    data: data
  })
}
