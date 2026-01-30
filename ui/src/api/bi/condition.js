import request from '@/utils/request'

/**
 * 查询条件配置API服务
 * 提供查询条件管理的前端API调用方法
 */

/**
 * 查询查询条件列表（按组件ID）
 * @param {Number} componentId - 组件ID
 * @returns {Promise} 查询条件列表
 */
export function listConditions(componentId) {
  return request({
    url: '/bi/condition/list',
    method: 'get',
    params: { componentId }
  })
}

/**
 * 获取查询条件详细信息
 * @param {Number} id - 条件ID
 * @returns {Promise} 查询条件详情
 */
export function getCondition(id) {
  return request({
    url: '/bi/condition/' + id,
    method: 'get'
  })
}

/**
 * 新增查询条件
 * @param {Object} data - 查询条件数据
 * @returns {Promise} 操作结果
 */
export function addCondition(data) {
  return request({
    url: '/bi/condition',
    method: 'post',
    data: data
  })
}

/**
 * 修改查询条件
 * @param {Object} data - 查询条件数据
 * @returns {Promise} 操作结果
 */
export function updateCondition(data) {
  return request({
    url: '/bi/condition',
    method: 'put',
    data: data
  })
}

/**
 * 删除查询条件
 * @param {Number|Array} ids - 条件ID或ID数组
 * @returns {Promise} 操作结果
 */
export function deleteCondition(ids) {
  return request({
    url: '/bi/condition/' + ids,
    method: 'delete'
  })
}

/**
 * 保存查询条件配置
 * 用于配置面板保存完整的条件配置（包括条件和映射）
 * @param {Object} data - 查询条件配置DTO
 * @param {Number} data.componentId - 组件ID
 * @param {Number} data.dashboardId - 仪表板ID
 * @param {Array} data.conditions - 条件列表
 * @returns {Promise} 操作结果
 */
export function saveConditionConfig(data) {
  return request({
    url: '/bi/condition/save',
    method: 'post',
    data: data
  })
}

/**
 * 重新排序查询条件
 * 用于拖拽排序后批量更新显示顺序
 * @param {Array} data - 排序数据数组
 * @param {Number} data[].id - 条件ID
 * @param {Number} data[].displayOrder - 显示顺序
 * @returns {Promise} 操作结果
 */
export function reorderConditions(data) {
  return request({
    url: '/bi/condition/reorder',
    method: 'put',
    data: data
  })
}

/**
 * 获取数据集字段列表
 * 用于配置面板显示可用的字段供选择
 * @param {Number} datasetId - 数据集ID
 * @returns {Promise} 字段列表
 */
export function getDatasetFields(datasetId) {
  return request({
    url: '/bi/condition/fields/' + datasetId,
    method: 'get'
  })
}

/**
 * 验证查询条件配置
 * 用于配置面板实时验证配置的有效性
 * @param {Object} data - 查询条件配置DTO
 * @returns {Promise} 验证结果
 */
export function validateConditionConfig(data) {
  return request({
    url: '/bi/condition/validate',
    method: 'post',
    data: data
  })
}

/**
 * 获取条件映射
 * @param {Number} dashboardId - 仪表板ID
 * @returns {Promise} 条件映射列表
 */
export function getMappings(dashboardId) {
  return request({
    url: '/bi/condition/' + dashboardId + '/mappings',
    method: 'get'
  })
}

/**
 * 保存条件映射
 * @param {Number} dashboardId - 仪表板ID
 * @param {Array} mappings - 映射列表
 * @returns {Promise} 操作结果
 */
export function saveMappings(dashboardId, mappings) {
  return request({
    url: '/bi/condition/' + dashboardId + '/mappings',
    method: 'post',
    data: mappings
  })
}

/**
 * 获取级联选项
 * @param {Number} conditionId - 条件ID
 * @param {Object} parentValues - 父级条件值
 * @returns {Promise} 级联选项列表
 */
export function getCascadeOptions(conditionId, parentValues) {
  return request({
    url: '/bi/condition/cascade-options',
    method: 'get',
    params: {
      conditionId,
      ...parentValues
    }
  })
}

/**
 * 执行图表查询
 * 用于获取图表数据
 * @param {Object} queryRequest - 查询请求对象
 * @param {Number} queryRequest.componentId - 组件ID
 * @param {Object} queryRequest.queryParams - 查询参数
 * @returns {Promise} 查询结果
 */
export function executeQuery(queryRequest) {
  return request({
    url: '/bi/condition/execute',
    method: 'post',
    data: queryRequest
  })
}
