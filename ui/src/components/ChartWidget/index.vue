<template>
  <div class="chart-widget" :style="widgetStyle">
    <!-- 编辑模式工具栏 -->
    <div v-if="isEditMode" class="edit-toolbar" @mousedown.stop @click.stop>
      <el-button-group size="mini">
        <el-button icon="el-icon-setting" @click="handleConfig">配置</el-button>
        <el-button icon="el-icon-refresh" @click="refreshChart">刷新</el-button>
      </el-button-group>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="widget-loading">
      <i class="el-icon-loading"></i>
      <span>加载中...</span>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="widget-error">
      <i class="el-icon-warning"></i>
      <span>{{ error }}</span>
      <el-button v-if="!isEditMode" type="text" size="small" @click="refreshChart">
        重试
      </el-button>
    </div>

    <!-- 编辑模式占位符 -->
    <div v-show="isEditMode && !chartData && !chartInstance" class="edit-mode-placeholder">
      <i class="el-icon-s-data"></i>
      <p>{{ config.name || '图表组件' }}</p>
      <p class="placeholder-hint">点击工具栏的"刷新"按钮加载数据</p>
    </div>

    <!-- 图表容器 -->
    <div
      v-show="!loading && !error"
      ref="chartContainer"
      class="chart-container"
      :style="{ height: isEditMode ? 'calc(100% - 40px)' : '100%' }"
    ></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { executeQuery } from '@/api/bi/query'
import { injectQueryParams, validateQueryParams, cleanEmptyParams } from '@/utils/queryParamsInjector'
import { buildChartOption } from '@/utils/chartAdapters'

export default {
  name: 'ChartWidget',
  props: {
    // 组件配置
    config: {
      type: Object,
      required: true
    },
    // 查询参数(来自查询组件)
    queryParams: {
      type: Object,
      default: () => ({})
    },
    // 条件映射列表
    conditionMappings: {
      type: Array,
      default: () => []
    },
    // 查询条件列表
    queryConditions: {
      type: Array,
      default: () => []
    },
    // 是否编辑模式
    isEditMode: {
      type: Boolean,
      default: false
    },
    // 组件样式
    widgetStyle: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      chartInstance: null,
      chartData: null,
      loading: false,
      error: null,
      refreshTimer: null,
      lastQueryParams: null
    }
  },
  computed: {
    // 合并后的查询参数
    mergedQueryParams() {
      // 注入查询条件参数
      const injectedParams = injectQueryParams(
        this.config,
        this.conditionMappings,
        this.queryParams
      )

      // 合并图表级过滤器
      const chartFilters = {}
      if (this.config.dataConfig && this.config.dataConfig.filters) {
        this.config.dataConfig.filters.forEach(filter => {
          chartFilters[filter.field] = filter.value
        })
      }

      // 清理空参数
      return cleanEmptyParams({
        ...injectedParams,
        ...chartFilters
      })
    },

    // 刷新频率(毫秒)
    refreshInterval() {
      if (!this.config.dataConfig || !this.config.dataConfig.refreshInterval) {
        return 0
      }
      return this.config.dataConfig.refreshInterval * 1000
    }
  },
  watch: {
    // 监听查询参数变化 - 注释掉自动刷新逻辑
    // 只有点击查询按钮时才刷新图表
    /*
    mergedQueryParams: {
      handler(newParams, oldParams) {
        console.log('[ChartWidget] mergedQueryParams 变化:', {
          componentId: this.config.id,
          componentName: this.config.name,
          oldParams,
          newParams
        })

        // 比较参数是否真的变化了
        if (JSON.stringify(newParams) !== JSON.stringify(oldParams)) {
          console.log('[ChartWidget] 参数已变化，触发刷新')
          this.refreshChart()
        } else {
          console.log('[ChartWidget] 参数未变化，跳过刷新')
        }
      },
      deep: true
    },
    */

    // 监听配置变化（仅在非编辑模式下）
    'config.dataConfig': {
      handler(newVal, oldVal) {
        // 编辑模式下不自动刷新
        if (this.isEditMode) {
          return
        }

        // 避免初始化时触发
        if (!oldVal) {
          return
        }

        this.refreshChart()
      },
      deep: true
    },

    // 监听刷新触发器
    'config._refreshTrigger': {
      handler(newVal) {
        if (newVal) {
          this.refreshChart()
        }
      }
    },

    // 监听样式配置变化 - 暂时禁用避免循环
    /*
    'config.styleConfig': {
      handler() {
        if (this.chartInstance && this.chartData) {
          this.updateChartOption()
        }
      },
      deep: true
    },
    */

    // 监听刷新频率变化
    refreshInterval: {
      handler(newInterval) {
        this.setupAutoRefresh(newInterval)
      },
      immediate: false
    }
  },
  mounted() {
    
    try {
      
      // 直接同步初始化，不使用任何异步机制
      this.initChart()

      // 编辑模式下，如果有完整的数据配置，也加载数据用于预览
      if (this.config?.dataConfig?.datasetId) {
        this.fetchData()
      }

      this.setupAutoRefresh(this.refreshInterval)
    } catch (error) {
      console.error('[CHARTWIDGET] ❌ 初始化出错:', error)
      console.error('[CHARTWIDGET] 错误堆栈:', error.stack)
    }

    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    this.clearAutoRefresh()
    window.removeEventListener('resize', this.handleResize)

    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
  },
  methods: {
    /**
     * 初始化图表
     */
    initChart() {
      
      // 检查 ref 是否存在
      if (!this.$refs.chartContainer) {
        console.warn('[CHARTWIDGET] chartContainer ref not found, 延迟重试')
        // 延迟重试，避免阻塞
        setTimeout(() => {
          if (this.$refs.chartContainer) {
            this.initChart()
          } else {
            console.error('[CHARTWIDGET] chartContainer ref 仍然不存在，放弃初始化')
          }
        }, 100)
        return
      }

      // 检查 DOM 元素的尺寸
      const { clientWidth, clientHeight } = this.$refs.chartContainer

      if (clientWidth === 0 || clientHeight === 0) {
        console.warn('[CHARTWIDGET] DOM element has no width or height, 延迟重试', {
          clientWidth,
          clientHeight
        })
        // 延迟重试一次
        setTimeout(() => {
          const { clientWidth: w, clientHeight: h } = this.$refs.chartContainer
          if (w > 0 && h > 0) {
            this.initChart()
          } else {
            console.error('[CHARTWIDGET] 重试失败，放弃初始化')
          }
        }, 200)
        return
      }

      // 销毁旧实例
      if (this.chartInstance) {
        this.chartInstance.dispose()
      }

      // 创建新实例
      try {
        this.chartInstance = echarts.init(this.$refs.chartContainer)
        // 绑定事件
        this.bindChartEvents()
      } catch (error) {
        console.error('[CHARTWIDGET] Failed to initialize echarts', error)
        this.error = '图表初始化失败'
      }
    },

    /**
     * 绑定图表事件
     */
    bindChartEvents() {
      if (!this.chartInstance) {
        return
      }

      // 下钻事件
      if (this.config.advancedConfig && this.config.advancedConfig.drillDown && this.config.advancedConfig.drillDown.enabled) {
        this.chartInstance.on('click', this.handleDrillDown)
      }
    },

    /**
     * 获取数据
     */
    async fetchData() {
      console.log('[ChartWidget] fetchData 被调用:', {
        componentId: this.config.id,
        componentName: this.config.name,
        datasetId: this.config.dataConfig?.datasetId,
        mergedQueryParams: this.mergedQueryParams
      })

      if (!this.config.dataConfig || !this.config.dataConfig.datasetId) {
        console.error('[ChartWidget] 未配置数据集')
        this.error = '未配置数据集'
        return
      }

      // 如果图表实例不存在，先初始化
      if (!this.chartInstance && this.$refs.chartContainer) {
        this.initChart()
      }

      // 验证必填参数
      const validation = validateQueryParams(
        this.config,
        this.mergedQueryParams,
        this.queryConditions,
        this.conditionMappings
      )

      console.log('[ChartWidget] 参数验证结果:', validation)

      if (!validation.valid) {
        const missingNames = validation.missing.map(m => m.conditionName).join('、')
        console.error('[ChartWidget] 必填参数缺失:', missingNames)
        this.error = `请先设置查询条件: ${missingNames}`
        return
      }

      this.loading = true
      this.error = null

      try {
        // 构建查询请求
        const queryRequest = {
          datasetId: this.config.dataConfig.datasetId,
          dimensions: this.config.dataConfig.dimensions || [],
          metrics: this.config.dataConfig.metrics || [],
          filters: this.buildFilters(),
          limit: this.config.dataConfig.limit || 1000,
          params: this.mergedQueryParams
        }

        console.log('[ChartWidget] 发起查询请求:', queryRequest)

        // 执行查询
        const response = await executeQuery(queryRequest)

        console.log('[ChartWidget] 查询响应:', response)

        this.chartData = response.data

        // 渲染图表
        this.$nextTick(() => {
          this.renderChart()
        })

        // 保存最后的查询参数
        this.lastQueryParams = { ...this.mergedQueryParams }

        // 触发数据加载完成事件
        this.$emit('data-loaded', this.chartData)
      } catch (err) {
        console.error('[ChartWidget] 图表数据加载失败:', err)
        this.error = err.message || '数据加载失败'
        this.$emit('data-error', err)
      } finally {
        this.loading = false
      }
    },

    /**
     * 构建过滤条件
     */
    buildFilters() {
      const filters = []

      // 添加图表级过滤器
      if (this.config.dataConfig && this.config.dataConfig.filters) {
        filters.push(...this.config.dataConfig.filters)
      }

      return filters
    },

    /**
     * 渲染图表
     */
    renderChart() {
      console.log('[ChartWidget] renderChart 被调用:', {
        componentId: this.config.id,
        componentName: this.config.name,
        hasChartInstance: !!this.chartInstance,
        hasChartData: !!this.chartData,
        chartDataLength: this.chartData?.length || 0
      })

      if (!this.chartInstance) {
        console.error('[ChartWidget] chartInstance 不存在，无法渲染')
        this.error = '图表未初始化'
        return
      }

      if (!this.chartData) {
        console.warn('[ChartWidget] chartData 为空，无法渲染')
        this.error = '无数据'
        return
      }

      try {
        console.log('[ChartWidget] 开始构建图表配置...')
        const option = this.buildChartOption()
        console.log('[ChartWidget] 图表配置已构建:', option)

        this.chartInstance.setOption(option, true)
        console.log('[ChartWidget] 图表已渲染')
      } catch (err) {
        console.error('[ChartWidget] 图表渲染失败:', err)
        this.error = '图表渲染失败: ' + err.message
      }
    },

    /**
     * 构建图表配置
     */
    buildChartOption() {
      console.log('[ChartWidget] buildChartOption 被调用')

      const styleConfig = this.config.styleConfig || {}
      const dataConfig = this.config.dataConfig || {}
      const chartType = styleConfig.chartType || 'line'

      console.log('[ChartWidget] 图表配置:', {
        chartType,
        hasStyleConfig: !!styleConfig,
        hasDataConfig: !!dataConfig,
        dimensions: dataConfig.dimensions,
        metrics: dataConfig.metrics
      })

      // 转换数据格式
      const transformedData = this.transformChartData(this.chartData, dataConfig)
      console.log('[ChartWidget] 转换后的数据:', transformedData)

      // 使用chartAdapters构建配置
      const option = buildChartOption(chartType, transformedData, styleConfig, dataConfig)

      console.log('[ChartWidget] 最终配置:', option)

      return option
    },

    /**
     * 转换图表数据
     * 将后端返回的原始数据转换为 ECharts 需要的格式
     */
    transformChartData(chartData, dataConfig) {
      
      if (!chartData || !chartData.data || chartData.data.length === 0) {
        console.warn('数据为空')
        return { categories: [], series: [], data: [] }
      }

      const rawData = chartData.data
      const dimensions = dataConfig.dimensions || []
      const metrics = dataConfig.metrics || []
      const chartType = this.config.styleConfig?.chartType || 'line'


      // 如果没有配置维度和指标，返回空数据
      if (dimensions.length === 0 && metrics.length === 0) {
        console.warn('维度和指标都为空')
        return { categories: [], series: [], data: [] }
      }

      // 特殊处理：仪表盘、水球图、指标卡等单值图表
      if (['gauge', 'liquidfill', 'card', 'metricCard'].includes(chartType)) {

        // 对于单值图表，取第一个指标的第一个值
        if (metrics.length > 0) {
          const metric = metrics[0]
          const fieldKey = metric.fieldName || metric.label || metric.field

          const firstRow = rawData[0]
          const value = this.parseNumber(firstRow[fieldKey])


          return {
            data: [{
              value: value,
              name: metric.comment || metric.fieldName || metric.label || metric.field || '无数据'
            }]
          }
        }

        return { data: [{ value: 0, name: '无数据' }] }
      }

      // 获取维度字段（用于 X 轴）
      const dimensionField = dimensions.length > 0 ? dimensions[0] : null
      
      // 提取分类数据（X 轴）
      const categories = []
      const categoryMap = new Map()

      if (dimensionField) {
        // 使用字段的 fieldName 作为数据键，comment 作为显示名称
        const fieldKey = dimensionField.fieldName || dimensionField.label || dimensionField.field

        // 打印第一行数据的所有键
        if (rawData.length > 0) {
        }

        rawData.forEach((row, index) => {
          const categoryValue = row[fieldKey]

          if (categoryValue !== undefined && categoryValue !== null) {
            const categoryStr = String(categoryValue)
            if (!categoryMap.has(categoryStr)) {
              categoryMap.set(categoryStr, categories.length)
              categories.push(categoryStr)
            }
          }
        })
      }


      // 构建系列数据（Y 轴）
      const series = metrics.map(metric => {
        const fieldKey = metric.fieldName || metric.label || metric.field
        const seriesData = []

        if (dimensionField) {
          // 有维度：按维度分组
          const dimFieldKey = dimensionField.fieldName || dimensionField.label || dimensionField.field

          rawData.forEach((row, rowIndex) => {
            const categoryValue = row[dimFieldKey]
            const metricValue = row[fieldKey]


            if (categoryValue !== undefined && categoryValue !== null) {
              const categoryStr = String(categoryValue)
              const index = categoryMap.get(categoryStr)

              if (index !== undefined) {
                seriesData[index] = this.parseNumber(metricValue)
              }
            }
          })
        } else {
          // 无维度：直接使用指标值
          rawData.forEach(row => {
            const metricValue = row[fieldKey]
            seriesData.push(this.parseNumber(metricValue))
          })
        }


        return {
          name: metric.comment || metric.fieldName || metric.label || metric.field,
          data: seriesData
        }
      })

      const result = {
        categories,
        series
      }
      
      
      return result
    },

    /**
     * 解析数字值
     */
    parseNumber(value) {
      if (value === null || value === undefined || value === '') {
        return 0
      }
      const num = Number(value)
      return isNaN(num) ? 0 : num
    },

    /**
     * 更新图表配置
     */
    updateChartOption() {
      if (!this.chartInstance || !this.chartData) return

      const option = this.buildChartOption()
      this.chartInstance.setOption(option, true)
    },

    /**
     * 刷新图表
     */
    refreshChart() {
      console.log('[ChartWidget] refreshChart 被调用:', {
        componentId: this.config.id,
        componentName: this.config.name,
        loading: this.loading
      })

      // 防止重复调用
      if (this.loading) {
        console.log('[ChartWidget] 正在加载中，跳过刷新')
        return
      }
      this.fetchData()
    },

    /**
     * 设置自动刷新
     */
    setupAutoRefresh(interval) {
      // 清除旧定时器
      this.clearAutoRefresh()

      // 如果间隔为0或编辑模式,不启动自动刷新
      if (!interval || interval <= 0 || this.isEditMode) {
        return
      }

      // 启动新定时器
      this.refreshTimer = setInterval(() => {
        this.refreshChart()
      }, interval)
    },

    /**
     * 清除自动刷新
     */
    clearAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },

    /**
     * 处理窗口大小变化
     */
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },

    /**
     * 处理下钻
     */
    handleDrillDown(params) {
      if (!this.config.advancedConfig || !this.config.advancedConfig.drillDown) {
        return
      }

      const drillDownConfig = this.config.advancedConfig.drillDown
      if (!drillDownConfig.enabled || !drillDownConfig.dimensions || drillDownConfig.dimensions.length === 0) {
        return
      }

      // 触发下钻事件
      this.$emit('drill-down', {
        componentId: this.config.id,
        dimension: params.name,
        value: params.value,
        drillDownDimensions: drillDownConfig.dimensions
      })
    },

    /**
     * 处理点击
     */
    handleClick() {
      this.$emit('click', this.config)
    },

    /**
     * 处理配置
     */
    handleConfig() {
      this.$emit('config', this.config)
    }
  }
}
</script>

<style scoped lang="scss">
.chart-widget {
  position: relative;
  width: 100%;
  height: 100%;
  min-width: 200px;
  min-height: 200px;
  background: #fff;
  border-radius: 4px;
  overflow: hidden; /* 改回 hidden，强制图表适应容器 */
  box-sizing: border-box;
  /* 强制限制最大宽度 */
  max-width: 100%;
}

.widget-loading,
.widget-error,
.edit-mode-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 14px;
  box-sizing: border-box;

  i {
    font-size: 32px;
    margin-bottom: 10px;
  }

  .el-button {
    margin-top: 10px;
  }
}

.widget-error {
  color: #f56c6c;
}

.edit-mode-placeholder {
  color: #c0c4cc;
  
  i {
    font-size: 48px;
    color: #dcdfe6;
  }
  
  p {
    margin: 8px 0;
    
    &.placeholder-hint {
      font-size: 12px;
      color: #c0c4cc;
    }
  }
}

.chart-container {
  width: 100%;
  height: 100%;
  /* 确保容器有最小尺寸，避免 ECharts 初始化失败 */
  min-width: 100px;
  min-height: 100px;
  box-sizing: border-box;
  /* 强制限制最大宽度，图表必须适应容器 */
  max-width: 100%;
  overflow: hidden;
}

/* 编辑模式工具栏 */
.edit-toolbar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  pointer-events: auto; /* 确保工具栏可以接收点击事件 */

  .el-button-group {
    pointer-events: auto; /* 确保按钮可以接收点击事件 */
  }
}
</style>
