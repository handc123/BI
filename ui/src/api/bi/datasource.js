import request from '@/utils/request'

// 查询数据源列表
export function listDataSource(query) {
  return request({
    url: '/bi/datasource/list',
    method: 'get',
    params: query
  })
}

// 获取数据源详情
export function getDataSource(id) {
  return request({
    url: '/bi/datasource/' + id,
    method: 'get'
  })
}

// 测试数据源连接
export function testDataSource(data) {
  return request({
    url: '/bi/datasource/test',
    method: 'post',
    data: data,
    headers: {
      repeatSubmit: false
    }
  })
}

// 新增数据源
export function addDataSource(data) {
  return request({
    url: '/bi/datasource',
    method: 'post',
    data: data
  })
}

// 修改数据源
export function updateDataSource(data) {
  return request({
    url: '/bi/datasource',
    method: 'put',
    data: data
  })
}

// 删除数据源
export function delDataSource(ids) {
  return request({
    url: '/bi/datasource/' + ids,
    method: 'delete'
  })
}

// 上传文件数据源（multipart）
export function uploadFileDataSource(formData) {
  return request({
    url: '/bi/datasource/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    }
  })
}

// 预览文件数据源数据
export function previewFileDataSource(id, query) {
  return request({
    url: '/bi/datasource/file/' + id + '/preview',
    method: 'get',
    params: query
  })
}

// 删除文件数据源
export function delFileDataSource(id) {
  return request({
    url: '/bi/datasource/file/' + id,
    method: 'delete'
  })
}

// 清理未使用文件数据源
export function cleanupFileDataSource() {
  return request({
    url: '/bi/datasource/file/cleanup',
    method: 'post'
  })
}

