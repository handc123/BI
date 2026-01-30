import request from '@/utils/request'

// 查询查询条件列表
export function listQueryCondition(query) {
  return request({
    url: '/bi/condition/list',
    method: 'get',
    params: query
  })
}

// 获取查询条件详情
export function getQueryCondition(id) {
  return request({
    url: '/bi/condition/' + id,
    method: 'get'
  })
}

// 新增查询条件
export function addQueryCondition(data) {
  return request({
    url: '/bi/condition',
    method: 'post',
    data: data
  })
}

// 修改查询条件
export function updateQueryCondition(data) {
  return request({
    url: '/bi/condition',
    method: 'put',
    data: data
  })
}

// 删除查询条件
export function delQueryCondition(ids) {
  return request({
    url: '/bi/condition/' + ids,
    method: 'delete'
  })
}

// 获取条件映射
export function getConditionMappings(dashboardId) {
  return request({
    url: '/bi/condition/' + dashboardId + '/mappings',
    method: 'get'
  })
}

// 保存条件映射
export function saveConditionMappings(dashboardId, mappings) {
  return request({
    url: '/bi/condition/' + dashboardId + '/mappings',
    method: 'post',
    data: mappings
  })
}

// 获取级联选项
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

// 批量更新查询条件顺序
export function batchUpdateConditionOrder(conditions) {
  return request({
    url: '/bi/condition/reorder',
    method: 'put',
    data: conditions
  })
}

// 获取查询条件选项(用于下拉框等)
export function getConditionOptions(conditionId, params) {
  return request({
    url: '/bi/condition/fields/' + conditionId,
    method: 'get',
    params: params
  })
}

// 验证查询条件配置
export function validateConditionConfig(config) {
  return request({
    url: '/bi/condition/validate',
    method: 'post',
    data: config
  })
}

// 执行查询(用于图表数据获取)
export function executeQuery(queryRequest) {
  return request({
    url: '/bi/condition/execute',
    method: 'post',
    data: queryRequest
  })
}
