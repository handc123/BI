import request from '@/utils/request'

// 查询数据集列表
export function listDataset(query) {
  return request({
    url: '/bi/dataset/list',
    method: 'get',
    params: query
  })
}

// 获取数据集详情
export function getDataset(id) {
  return request({
    url: '/bi/dataset/' + id,
    method: 'get'
  })
}

// 新增数据集
export function addDataset(data) {
  return request({
    url: '/bi/dataset',
    method: 'post',
    data: data
  })
}

// 修改数据集
export function updateDataset(data) {
  return request({
    url: '/bi/dataset',
    method: 'put',
    data: data
  })
}

// 删除数据集
export function delDataset(ids) {
  return request({
    url: '/bi/dataset/' + ids,
    method: 'delete'
  })
}

// 预览数据集
export function previewDataset(id, filters) {
  return request({
    url: `/bi/dataset/${id}/preview`,
    method: 'post',
    data: filters || []
  })
}

// 立即抽取
export function executeExtract(id) {
  return request({
    url: `/bi/dataset/${id}/extract`,
    method: 'post'
  })
}

// 获取数据源列表（用于下拉选择）
export function getDataSources() {
  return request({
    url: '/bi/datasource/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 1000, status: '0' }
  })
}

// 获取数据源的表列表
export function getDataSourceTables(dataSourceId) {
  return request({
    url: `/bi/datasource/${dataSourceId}/tables`,
    method: 'get'
  })
}

// 获取表结构信息
export function getTableSchema(dataSourceId, tableName) {
  return request({
    url: `/bi/datasource/${dataSourceId}/tables/${tableName}/schema`,
    method: 'get'
  })
}

// 获取表数据预览
export function getTablePreview(dataSourceId, tableName, limit = 10) {
  return request({
    url: `/bi/datasource/${dataSourceId}/tables/${tableName}/preview`,
    method: 'get',
    params: { limit }
  })
}

// 获取数据集数据
export function getDatasetData(id, params) {
  return request({
    url: `/bi/dataset/${id}/data`,
    method: 'post',
    data: params || {}
  })
}

// 获取数据集字段元数据
export function getDatasetFields(id) {
  return request({
    url: `/bi/dataset/${id}/fields`,
    method: 'get'
  })
}
