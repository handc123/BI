import request from '@/utils/request'

// 查询仪表板列表
export function listDashboard(query) {
  return request({
    url: '/bi/dashboard/list',
    method: 'get',
    params: query
  })
}

// 获取仪表板详情
export function getDashboard(id) {
  return request({
    url: '/bi/dashboard/' + id,
    method: 'get'
  })
}

// 新增仪表板
export function addDashboard(data) {
  return request({
    url: '/bi/dashboard',
    method: 'post',
    data: data
  })
}

// 修改仪表板
export function updateDashboard(data) {
  return request({
    url: '/bi/dashboard',
    method: 'put',
    data: data
  })
}

// 删除仪表板
export function delDashboard(ids) {
  return request({
    url: '/bi/dashboard/' + ids,
    method: 'delete'
  })
}

// 获取仪表板所有组件数据
export function getDashboardData(id, filters) {
  return request({
    url: '/bi/dashboard/' + id + '/data',
    method: 'post',
    data: filters || []
  })
}

// 生成共享链接
export function generateShareLink(shareForm) {
  return request({
    url: '/bi/dashboard/' + shareForm.dashboardId + '/share',
    method: 'post',
    data: shareForm
  })
}

// 访问共享链接
export function accessShareLink(token) {
  return request({
    url: '/bi/dashboard/share/' + token,
    method: 'get'
  })
}

// 导出仪表板为PDF
export function exportDashboard(id, filters) {
  return request({
    url: '/bi/dashboard/' + id + '/export',
    method: 'post',
    data: filters || [],
    responseType: 'blob'
  })
}

// 异步导出仪表板为PDF
export function exportDashboardAsync(id, filters) {
  return request({
    url: '/bi/dashboard/' + id + '/export/async',
    method: 'post',
    data: filters || []
  })
}

// 获取导出任务状态
export function getExportTaskStatus(taskId) {
  return request({
    url: '/bi/dashboard/export/task/' + taskId,
    method: 'get'
  })
}

// 下载导出文件
export function downloadExportFile(taskId) {
  return request({
    url: '/bi/dashboard/export/download/' + taskId,
    method: 'get',
    responseType: 'blob'
  })
}

// 复制仪表板
export function copyDashboard(id) {
  return request({
    url: '/bi/dashboard/' + id + '/copy',
    method: 'post'
  })
}

// 发布仪表板
export function publishDashboard(id) {
  return request({
    url: '/bi/dashboard/' + id + '/publish',
    method: 'post'
  })
}

// 取消发布仪表板
export function unpublishDashboard(id) {
  return request({
    url: '/bi/dashboard/' + id + '/unpublish',
    method: 'post'
  })
}

// 从已发布仪表板创建草稿副本
export function createDraftFromPublished(id) {
  return request({
    url: '/bi/dashboard/' + id + '/draft',
    method: 'post'
  })
}

// ==================== 仪表板设计器相关接口 ====================

// 获取仪表板配置(包含所有组件)
export function getDashboardConfig(id) {
  return request({
    url: '/bi/dashboard/' + id + '/config',
    method: 'get'
  })
}

// 保存仪表板配置
export function saveDashboardConfig(id, config) {
  return request({
    url: '/bi/dashboard/' + id + '/config',
    method: 'post',
    data: config
  })
}

// 保存仪表板(创建或更新)
export function saveDashboard(config) {
  if (config.dashboard && config.dashboard.id) {
    // 更新现有仪表板
    return saveDashboardConfig(config.dashboard.id, config)
  } else {
    // 创建新仪表板 - 先创建仪表板，再保存配置
    return addDashboard(config.dashboard).then(response => {
      if (response.code === 200) {
        // 后端返回的数据可能在 response.data 或直接在 response 中
        const dashboardId = response.data?.id || response.data
        if (dashboardId) {
          // 创建成功后保存配置
          config.dashboard.id = dashboardId
          return saveDashboardConfig(dashboardId, config).then(configResponse => {
            // 返回包含 ID 的响应
            return {
              ...configResponse,
              data: {
                ...configResponse.data,
                id: dashboardId
              }
            }
          })
        }
      }
      return response
    })
  }
}


