import request from '@/utils/request'

// 查询组件列表
export function listComponent(query) {
  return request({
    url: '/bi/dashboard/component/list',
    method: 'get',
    params: query
  })
}

// 获取组件详情
export function getComponent(id) {
  return request({
    url: '/bi/dashboard/component/' + id,
    method: 'get'
  })
}

// 新增组件
export function addComponent(data) {
  return request({
    url: '/bi/dashboard/component',
    method: 'post',
    data: data
  })
}

// 修改组件
export function updateComponent(data) {
  return request({
    url: '/bi/dashboard/component',
    method: 'put',
    data: data
  })
}

// 删除组件
export function delComponent(ids) {
  return request({
    url: '/bi/dashboard/component/' + ids,
    method: 'delete'
  })
}

// 批量更新组件位置
export function batchUpdatePosition(positions) {
  return request({
    url: '/bi/dashboard/component/batch-position',
    method: 'post',
    data: positions
  })
}

// 复制组件
export function copyComponent(id) {
  return request({
    url: '/bi/dashboard/component/' + id + '/copy',
    method: 'post'
  })
}

// 保存为模板
export function saveAsTemplate(id, templateInfo) {
  return request({
    url: '/bi/dashboard/component/' + id + '/template',
    method: 'post',
    data: templateInfo
  })
}

// 查询组件模板列表
export function listComponentTemplate(query) {
  return request({
    url: '/bi/dashboard/component/template/list',
    method: 'get',
    params: query
  })
}

// 获取组件模板详情
export function getComponentTemplate(id) {
  return request({
    url: '/bi/dashboard/component/template/' + id,
    method: 'get'
  })
}

// 修改组件模板
export function updateComponentTemplate(data) {
  return request({
    url: '/bi/dashboard/component/template',
    method: 'put',
    data: data
  })
}

// 删除组件模板
export function delComponentTemplate(ids) {
  return request({
    url: '/bi/dashboard/component/template/' + ids,
    method: 'delete'
  })
}

// 从模板创建组件
export function createFromTemplate(templateId, componentData) {
  return request({
    url: '/bi/dashboard/component/template/' + templateId + '/create',
    method: 'post',
    data: componentData
  })
}
