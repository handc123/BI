import request from '@/utils/request'

// 查询可视化列表
export function listVisualization(query) {
  return request({
    url: '/bi/visualization/list',
    method: 'get',
    params: query
  })
}

// 获取可视化详情
export function getVisualization(id) {
  return request({
    url: '/bi/visualization/' + id,
    method: 'get'
  })
}

// 新增可视化
export function addVisualization(data) {
  return request({
    url: '/bi/visualization',
    method: 'post',
    data: data
  })
}

// 修改可视化
export function updateVisualization(data) {
  return request({
    url: '/bi/visualization',
    method: 'put',
    data: data
  })
}

// 删除可视化
export function delVisualization(ids) {
  return request({
    url: '/bi/visualization/' + ids,
    method: 'delete'
  })
}

// 获取可视化数据
export function getVisualizationData(id, filters) {
  return request({
    url: '/bi/visualization/' + id + '/data',
    method: 'post',
    data: filters || []
  })
}

// 预览可视化数据
export function previewVisualization(config, datasetId) {
  return request({
    url: '/bi/visualization/preview',
    method: 'post',
    data: {
      config: config,
      datasetId: datasetId
    },
    headers: {
      repeatSubmit: false
    }
  })
}

// 导出表格组件为CSV
export function exportVisualizationToCSV(id, filters) {
  return request({
    url: '/bi/visualization/' + id + '/export/csv',
    method: 'post',
    data: filters || [],
    responseType: 'blob'
  })
}

// 导出表格组件为Excel
export function exportVisualizationToExcel(id, filters) {
  return request({
    url: '/bi/visualization/' + id + '/export/excel',
    method: 'post',
    data: filters || [],
    responseType: 'blob'
  })
}
