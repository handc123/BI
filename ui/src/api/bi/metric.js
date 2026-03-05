import request from '@/utils/request'

/**
 * 验证指标配置
 * 需求: 11.4, 12.1
 * 
 * @param {Object} config - 指标配置对象 (MetricConfigDTO)
 * @returns {Promise} 验证结果
 */
export function validateMetric(config) {
  return request({
    url: '/bi/dataset/metric/validate',
    method: 'post',
    data: config
  })
}

/**
 * 测试指标计算
 * 需求: 11.5, 12.1, 12.2
 * 
 * @param {Number} datasetId - 数据集ID
 * @param {Object} metric - 指标配置对象
 * @returns {Promise} 测试结果，包含样本数据和执行时间
 */
export function testMetric(datasetId, metric) {
  return request({
    url: '/bi/dataset/metric/test',
    method: 'post',
    data: {
      datasetId: datasetId,
      metric: metric
    },
    headers: {
      repeatSubmit: false // 允许重复测试
    }
  })
}

/**
 * 保存指标配置到数据集
 * 需求: 11.4, 12.1
 * 
 * @param {Number} datasetId - 数据集ID
 * @param {Object} config - 指标配置对象 (MetricConfigDTO)
 * @returns {Promise} 保存结果
 */
export function saveMetricConfig(datasetId, config) {
  return request({
    url: `/bi/dataset/${datasetId}/config`,
    method: 'put',
    data: config
  })
}

/**
 * 加载数据集的指标配置
 * 需求: 11.4, 12.1
 * 
 * @param {Number} datasetId - 数据集ID
 * @returns {Promise} 指标配置对象
 */
export function loadMetricConfig(datasetId) {
  return request({
    url: `/bi/dataset/${datasetId}`,
    method: 'get'
  }).then(response => {
    // 从数据集详情中提取指标配置
    if (response.code === 200 && response.data) {
      const dataset = response.data
      // 如果config字段存在且包含metricConfig，返回它
      if (dataset.config) {
        try {
          const config = typeof dataset.config === 'string' 
            ? JSON.parse(dataset.config) 
            : dataset.config
          return {
            code: 200,
            data: config.metricConfig || null,
            msg: '加载成功'
          }
        } catch (e) {
          console.error('解析指标配置失败:', e)
          return {
            code: 500,
            data: null,
            msg: '解析指标配置失败'
          }
        }
      }
      return {
        code: 200,
        data: null,
        msg: '该数据集暂无指标配置'
      }
    }
    return response
  })
}
