import request from '@/utils/request'

/**
 * 查询指标元数据列表
 */
export function listMetricMetadata(query) {
  return request({
    url: '/bi/metadata/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取指标元数据详细信息
 */
export function getMetricMetadata(id) {
  return request({
    url: '/bi/metadata/' + id,
    method: 'get'
  })
}

/**
 * 根据指标编码获取指标元数据
 */
export function getMetricMetadataByCode(metricCode) {
  return request({
    url: '/bi/metadata/code/' + metricCode,
    method: 'get'
  })
}

/**
 * 新增指标元数据
 */
export function addMetricMetadata(data) {
  return request({
    url: '/bi/metadata',
    method: 'post',
    data: data
  })
}

/**
 * 修改指标元数据
 */
export function updateMetricMetadata(data) {
  return request({
    url: '/bi/metadata',
    method: 'put',
    data: data
  })
}

/**
 * 删除指标元数据
 */
export function delMetricMetadata(ids) {
  return request({
    url: '/bi/metadata/' + ids,
    method: 'delete'
  })
}

/**
 * 导出指标元数据
 */
export function exportMetricMetadata(query) {
  return request({
    url: '/bi/metadata/export',
    method: 'post',
    params: query
  })
}

/**
 * 导入指标元数据
 */
export function importMetricMetadata(data, isUpdateSupport) {
  return request({
    url: '/bi/metadata/import',
    method: 'post',
    params: { isUpdateSupport: isUpdateSupport },
    data: data
  })
}
