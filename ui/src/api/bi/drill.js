import request from '@/utils/request'

export function getDrillConfig(metricId) {
  return request({
    url: `/bi/drill/config/${metricId}`,
    method: 'get'
  })
}

export function queryDrillDetail(data) {
  return request({
    url: '/bi/drill/query',
    method: 'post',
    data
  })
}

export function queryDrillByField(data) {
  return request({
    url: '/bi/drill/queryByField',
    method: 'post',
    data
  })
}
