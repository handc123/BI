/**
 * 指标管理 API 服务
 * 包含完整的错误处理和类型定义
 *
 * @author iras
 * @date 2025-02-26
 */

import request from '@/utils/request'
import { Message } from 'element-ui'

/**
 * API 基础路径
 */
const BASE_PATH = '/bi'
const METADATA_PATH = `${BASE_PATH}/metadata`
const LINEAGE_PATH = `${BASE_PATH}/lineage`
const DATA_PATH = `${BASE_PATH}/metric/data`

/**
 * 错误消息映射
 */
const ERROR_MESSAGES = {
  401: '未授权，请重新登录',
  403: '拒绝访问',
  404: '请求的资源不存在',
  500: '服务器内部错误',
  default: '请求失败，请稍后重试'
}

/**
 * 统一错误处理
 * @param {Error} error - 错误对象
 * @param {String} defaultMessage - 默认错误消息
 * @returns {Error} - 处理后的错误
 */
function handleError(error, defaultMessage = '操作失败') {
  console.error('API Error:', error)

  let message = defaultMessage

  if (error.response) {
    // 服务器返回错误
    const { status, data } = error.response
    message = data?.msg || data?.message || ERROR_MESSAGES[status] || ERROR_MESSAGES.default
  } else if (error.request) {
    // 请求已发送但没有收到响应
    message = '网络连接失败，请检查网络'
  } else {
    // 其他错误
    message = error.message || defaultMessage
  }

  // 显示错误提示
  Message.error(message)

  // 返回处理后的错误
  return new Error(message)
}

/**
 * 获取指标元数据
 * @param {Number} id - 指标ID
 * @returns {Promise} - 指标元数据
 */
export function getMetricMetadata(id) {
  return request({
    url: `${METADATA_PATH}/${id}`,
    method: 'get'
  }).catch(error => {
    throw handleError(error, '获取指标元数据失败')
  })
}

/**
 * 查询指标元数据列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 指标列表
 */
export function listMetricMetadata(params) {
  return request({
    url: `${METADATA_PATH}/list`,
    method: 'get',
    params
  }).catch(error => {
    throw handleError(error, '查询指标列表失败')
  })
}

/**
 * 新增指标元数据
 * @param {Object} data - 指标数据
 * @returns {Promise} - 新增结果
 */
export function addMetricMetadata(data) {
  return request({
    url: `${METADATA_PATH}`,
    method: 'post',
    data
  }).catch(error => {
    throw handleError(error, '新增指标失败')
  })
}

/**
 * 修改指标元数据
 * @param {Object} data - 指标数据
 * @returns {Promise} - 修改结果
 */
export function updateMetricMetadata(data) {
  return request({
    url: `${METADATA_PATH}`,
    method: 'put',
    data
  }).catch(error => {
    throw handleError(error, '修改指标失败')
  })
}

/**
 * 删除指标元数据
 * @param {Number|Array} ids - 指标ID或ID数组
 * @returns {Promise} - 删除结果
 */
export function deleteMetricMetadata(ids) {
  return request({
    url: `${METADATA_PATH}/${ids}`,
    method: 'delete'
  }).catch(error => {
    throw handleError(error, '删除指标失败')
  })
}

/**
 * 导出指标元数据
 * @param {Object} params - 查询参数
 * @returns {Promise} - 导出结果
 */
export function exportMetricMetadata(params) {
  return request({
    url: `${METADATA_PATH}/export`,
    method: 'post',
    data: params,
    responseType: 'blob'
  }).then(response => {
    // 处理文件下载
    downloadFile(response, `metric_metadata_${Date.now()}.xlsx`)
  }).catch(error => {
    throw handleError(error, '导出指标失败')
  })
}

/**
 * 获取指标血缘图
 * @param {Number} metricId - 指标ID
 * @param {String} mode - 查看模式(graph|upstream|downstream)
 * @param {Number} maxDepth - 最大深度
 * @returns {Promise} - 血缘图数据
 */
export function getMetricLineage(metricId, mode = 'graph', maxDepth = 5) {
  return request({
    url: `${LINEAGE_PATH}/metric/${metricId}`,
    method: 'get',
    params: { mode, maxDepth }
  }).catch(error => {
    throw handleError(error, '获取指标血缘失败')
  })
}

/**
 * 获取指标上游血缘
 * @param {Number} metricId - 指标ID
 * @param {Number} maxDepth - 最大深度
 * @returns {Promise} - 上游血缘数据
 */
export function getUpstreamLineage(metricId, maxDepth = 5) {
  return request({
    url: `${LINEAGE_PATH}/upstream/${metricId}`,
    method: 'get',
    params: { maxDepth }
  }).catch(error => {
    throw handleError(error, '获取上游血缘失败')
  })
}

/**
 * 获取指标下游血缘
 * @param {Number} metricId - 指标ID
 * @param {Number} maxDepth - 最大深度
 * @returns {Promise} - 下游血缘数据
 */
export function getDownstreamLineage(metricId, maxDepth = 5) {
  return request({
    url: `${LINEAGE_PATH}/downstream/${metricId}`,
    method: 'get',
    params: { maxDepth }
  }).catch(error => {
    throw handleError(error, '获取下游血缘失败')
  })
}

/**
 * 查找指标间的血缘路径
 * @param {Number} sourceId - 源指标ID
 * @param {Number} targetId - 目标指标ID
 * @param {Number} maxDepth - 最大搜索深度
 * @returns {Promise} - 路径列表
 */
export function findLineagePath(sourceId, targetId, maxDepth = 10) {
  return request({
    url: `${LINEAGE_PATH}/path`,
    method: 'get',
    params: { sourceId, targetId, maxDepth }
  }).catch(error => {
    throw handleError(error, '查找血缘路径失败')
  })
}

/**
 * 查询指标数据
 * @param {Number} metricId - 指标ID
 * @param {Object} data - 查询参数
 * @returns {Promise} - 查询结果
 */
export function queryMetricData(metricId, data) {
  // 设置默认值
  const queryParams = {
    pageNum: 1,
    pageSize: 20,
    ...data
  }

  // 验证分页参数
  if (queryParams.pageSize > 10000) {
    Message.warning('单次查询最多返回10000条数据')
    queryParams.pageSize = 10000
  }

  return request({
    url: `${DATA_PATH}/${metricId}/query`,
    method: 'post',
    data: queryParams
  }).catch(error => {
    throw handleError(error, '查询指标数据失败')
  })
}

/**
 * 导出指标数据
 * @param {Number} metricId - 指标ID
 * @param {Object} data - 查询参数
 * @param {String} format - 导出格式(excel|csv)
 * @returns {Promise} - 导出结果
 */
export function exportMetricData(metricId, data, format = 'excel') {
  return request({
    url: `${DATA_PATH}/${metricId}/export`,
    method: 'post',
    data: { ...data, exportFormat: format },
    responseType: 'blob'
  }).then(response => {
    const ext = format === 'csv' ? 'csv' : 'xlsx'
    downloadFile(response, `metric_data_${metricId}_${Date.now()}.${ext}`)
  }).catch(error => {
    throw handleError(error, '导出指标数据失败')
  })
}

/**
 * 获取指标数据概览
 * @param {Number} metricId - 指标ID
 * @returns {Promise} - 概览数据
 */
export function getMetricDataOverview(metricId) {
  return request({
    url: `${DATA_PATH}/${metricId}/overview`,
    method: 'get'
  }).catch(error => {
    throw handleError(error, '获取数据概览失败')
  })
}

/**
 * 创建血缘关系
 * @param {Object} data - 血缘关系数据
 * @returns {Promise} - 创建结果
 */
export function addLineage(data) {
  return request({
    url: `${LINEAGE_PATH}`,
    method: 'post',
    data
  }).catch(error => {
    throw handleError(error, '创建血缘关系失败')
  })
}

/**
 * 删除血缘关系
 * @param {Number|Array} ids - 血缘关系ID或ID数组
 * @returns {Promise} - 删除结果
 */
export function deleteLineage(ids) {
  return request({
    url: `${LINEAGE_PATH}/${ids}`,
    method: 'delete'
  }).catch(error => {
    throw handleError(error, '删除血缘关系失败')
  })
}

/**
 * 批量创建血缘关系
 * @param {Array} lineages - 血缘关系数组
 * @returns {Promise} - 创建结果
 */
export function batchAddLineage(lineages) {
  return request({
    url: `${LINEAGE_PATH}/batch`,
    method: 'post',
    data: { lineages }
  }).catch(error => {
    throw handleError(error, '批量创建血缘关系失败')
  })
}

/**
 * 文件下载辅助函数
 * @param {Blob} blob - 文件数据
 * @param {String} filename - 文件名
 */
function downloadFile(blob, filename) {
  try {
    // 创建 Blob URL
    const url = window.URL.createObjectURL(blob)

    // 创建临时链接
    const link = document.createElement('a')
    link.href = url
    link.download = filename

    // 触发下载
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    Message.success('文件下载成功')
  } catch (error) {
    console.error('文件下载失败:', error)
    Message.error('文件下载失败')
  }
}

/**
 * 权限检查辅助函数
 * @param {String} permission - 权限字符串
 * @returns {Boolean} - 是否有权限
 */
export function hasPermission(permission) {
  // 从 Vuex 或其他地方获取权限列表
  const permissions = localStorage.getItem('permissions') || '[]'
  const permissionList = JSON.parse(permissions)
  return permissionList.includes(permission)
}

/**
 * 检查多个权限（满足其一即可）
 * @param {Array} permissions - 权限数组
 * @returns {Boolean} - 是否有权限
 */
export function hasAnyPermission(permissions) {
  if (!permissions || permissions.length === 0) return true
  return permissions.some(p => hasPermission(p))
}

/**
 * 检查多个权限（需全部满足）
 * @param {Array} permissions - 权限数组
 * @returns {Boolean} - 是否有权限
 */
export function hasAllPermissions(permissions) {
  if (!permissions || permissions.length === 0) return true
  return permissions.every(p => hasPermission(p))
}

// 导出所有 API
export default {
  // 元数据管理
  getMetricMetadata,
  listMetricMetadata,
  addMetricMetadata,
  updateMetricMetadata,
  deleteMetricMetadata,
  exportMetricMetadata,

  // 血缘管理
  getMetricLineage,
  getUpstreamLineage,
  getDownstreamLineage,
  findLineagePath,
  addLineage,
  deleteLineage,
  batchAddLineage,

  // 数据查询
  queryMetricData,
  exportMetricData,
  getMetricDataOverview,

  // 工具函数
  hasPermission,
  hasAnyPermission,
  hasAllPermissions
}
