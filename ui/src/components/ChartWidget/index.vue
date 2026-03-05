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
    <div v-show="isEditMode && !chartData && !chartInstance && !isMetricCard && !isTable" class="edit-mode-placeholder">
      <i class="el-icon-s-data"></i>
      <p>{{ config.name || '图表组件' }}</p>
      <p class="placeholder-hint">点击工具栏的"刷新"按钮加载数据</p>
    </div>

    <!-- 指标卡容器（自定义渲染） -->
    <div
      v-if="isMetricCard && !loading && !error"
      class="metric-card-container"
      :class="{ 'with-toolbar': isEditMode, 'clickable': hasClickableContent && !isEditMode }"
      :style="getMetricCardStyle()"
      @click="handleChartClick"
    >
      <div v-if="metricCardConfig" class="metric-card-content">
        <div v-if="metricCardConfig.icon" class="metric-card-icon" :style="{ color: metricCardConfig.iconColor }">
          <i :class="metricCardConfig.icon"></i>
        </div>
        <div class="metric-card-value" :style="{ color: metricCardConfig.valueColor, fontSize: metricCardConfig.valueFontSize + 'px' }">
          {{ formatMetricValue(metricCardConfig.value) }}
          <span v-if="metricCardConfig.unit" class="metric-unit">{{ metricCardConfig.unit }}</span>
        </div>
        <div class="metric-card-title" :style="{ color: metricCardConfig.titleColor, fontSize: metricCardConfig.titleFontSize + 'px' }">
          {{ metricCardConfig.title || config.name || '指标' }}
        </div>
        <div v-if="metricCardConfig.showTrend && metricCardConfig.trend !== undefined" class="metric-card-trend" :class="getTrendClass(metricCardConfig.trend)">
          <i :class="getTrendIcon(metricCardConfig.trend)"></i>
          <span>{{ Math.abs(metricCardConfig.trend) }}%</span>
          <span v-if="metricCardConfig.trendLabel">{{ metricCardConfig.trendLabel }}</span>
        </div>
      </div>
    </div>

    <!-- 表格容器（自定义渲染） -->
    <div
      v-if="isTable && !loading && !error"
      class="table-container"
      :class="{ 'with-toolbar': isEditMode, 'clickable': hasClickableContent && !isEditMode }"
      :style="{ height: isEditMode ? 'calc(100% - 40px)' : '100%' }"
      @click="handleChartClick"
    >
      <el-table
        v-if="tableConfig && tableConfig.data && tableConfig.data.length > 0"
        :data="tableConfig.data"
        :stripe="tableConfig.stripe"
        :border="tableConfig.border"
        :size="tableConfig.size"
        :show-header="tableConfig.showHeader"
        :highlight-current-row="tableConfig.highlightCurrentRow"
        :max-height="tableConfig.maxHeight"
        style="width: 100%; height: 100%"
      >
        <el-table-column
          v-for="(column, index) in getTableColumns(tableConfig.data)"
          :key="index"
          :prop="column.key"
          :label="column.label"
          :min-width="100"
        />
      </el-table>
      <el-empty
        v-else
        description="暂无数据"
        :image-size="100"
      />
    </div>

    <!-- 图表容器（ECharts） -->
    <div
      v-show="!isMetricCard && !isTable && !loading && !error"
      ref="chartContainer"
      class="chart-container"
      :class="{ 'with-toolbar': isEditMode, 'clickable': hasClickableContent && !isEditMode }"
      :style="{ height: isEditMode ? 'calc(100% - 40px)' : '100%' }"
      @click="handleChartClick"
    ></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { executeQuery } from '@/api/bi/query'
import { injectQueryParams, validateQueryParams, cleanEmptyParams } from '@/utils/queryParamsInjector'
import { buildChartOption } from '@/utils/chartAdapters'
import { listMetricMetadata } from '@/api/bi/metric'

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
      lastQueryParams: null,
      resizeObserver: null,  // ResizeObserver 实例
      metricCardConfig: null,  // 指标卡配置
      tableConfig: null  // 表格配置
    }
  },
  computed: {
    // 判断是否为指标卡类型
    isMetricCard() {
      const chartType = this.config.styleConfig?.chartType
      return ['card', 'metricCard', 'kpi'].includes(chartType)
    },
    // 判断是否为表格类型
    isTable() {
      const chartType = this.config.styleConfig?.chartType
      return ['table', 'summary-table', 'pivot-table'].includes(chartType)
    },
    // 是否有关联的指标ID
    hasMetricId() {
      return !!(this.config.dataConfig && this.config.dataConfig.metricId)
    },

    // 是否有可点击的内容（有关联指标或者有数据字段）
    hasClickableContent() {
      return this.hasMetricId || (
        this.config.dataConfig &&
        this.config.dataConfig.metrics &&
        this.config.dataConfig.metrics.length > 0
      )
    },

    //合并后的查询参数
    mergedQueryParams() {
      console.log('[ChartWidget]计算 mergedQueryParams:', {
        componentId: this.config.id,
        componentName: this.config.name,
        conditionMappings: this.conditionMappings,
        queryParams: this.queryParams,
        config: this.config
      })
          
      // 注入查询条件参数
      const injectedParams = injectQueryParams(
        this.config,
        this.conditionMappings,
        this.queryParams
      )
          
      console.log('[ChartWidget] 注入后的参数:', injectedParams)

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
    //监听查询参数变化 - 自动刷新图表
    mergedQueryParams: {
      handler(newParams, oldParams) {
        console.log('[ChartWidget] mergedQueryParams变化:', {
          componentId: this.config.id,
          componentName: this.config.name,
          oldParams,
          newParams
        })
  
        //比较参数是否真的变化了
        if (JSON.stringify(newParams) !== JSON.stringify(oldParams)) {
          console.log('[ChartWidget] 参数已变化，触发刷新')
          this.refreshChart()
        } else {
          console.log('[ChartWidget] 参数未变化，跳过刷新')
        }
      },
      deep: true
    },

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

    // 监听组件尺寸变化，触发图表resize
    'config.size': {
      handler(newVal, oldVal) {
        if (!oldVal || !newVal) {
          return
        }
        // 尺寸变化时，延迟触发resize以确保OM已更新
        this.$nextTick(() => {
          if (this.chartInstance) {
            this.chartInstance.resize()
            console.log('[ChartWidget] 图表已resize，新尺寸:', newVal)
          }
        })
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

    // 监听样式配置变化 - 实时更新图表样式
    'config.styleConfig': {
      handler(newVal, oldVal) {
        console.log('[ChartWidget] styleConfig 变化:', {
          componentId: this.config.id,
          componentName: this.config.name,
          hasNewVal: !!newVal,
          hasOldVal: !!oldVal,
          hasChartInstance: !!this.chartInstance,
          hasChartData: !!this.chartData,
          newValKeys: newVal ? Object.keys(newVal) : [],
          themePreset: newVal?.themePreset
        })

        // 只要有图表实例或是指标卡就更新
        // 这样可以在没有数据的情况下也预览样式
        if (this.chartInstance || this.isMetricCard) {
          this.$nextTick(() => {
            this.updateChartOption()
          })
        }
      },
      deep: true
    },

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

    // 使用 ResizeObserver 监听容器尺寸变化
    this.setupResizeObserver()
  },
  beforeDestroy() {
    this.clearAutoRefresh()
    window.removeEventListener('resize', this.handleResize)

    // 清理 ResizeObserver
    if (this.resizeObserver) {
      this.resizeObserver.disconnect()
      this.resizeObserver = null
    }

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
      // 指标卡和表格不需要初始化ECharts实例
      if (this.isMetricCard || this.isTable) {
        console.log('[CHARTWIDGET] 指标卡/表格类型，跳过ECharts初始化')
        return
      }

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

      // 如果图表实例不存在且不是指标卡，先初始化
      if (!this.chartInstance && !this.isMetricCard && this.$refs.chartContainer) {
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
          params: this.mergedQueryParams,
          calculatedFields: this.config.dataConfig.calculatedFields || [] // 添加计算字段配置
        }

        console.log('[ChartWidget] 发起查询请求:', queryRequest)
        console.log('[ChartWidget] 计算字段配置:', queryRequest.calculatedFields)

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
        isMetricCard: this.isMetricCard,
        isTable: this.isTable,
        hasChartInstance: !!this.chartInstance,
        hasChartData: !!this.chartData,
        chartDataLength: this.chartData?.length || 0
      })

      // 指标卡特殊处理：不使用ECharts渲染
      if (this.isMetricCard) {
        this.renderMetricCard()
        return
      }

      // 表格特殊处理：不使用ECharts渲染
      if (this.isTable) {
        this.renderTable()
        return
      }

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
     * 渲染指标卡
     */
    renderMetricCard() {
      console.log('[ChartWidget] 渲染指标卡')

      if (!this.chartData) {
        console.warn('[ChartWidget] chartData 为空，无法渲染指标卡')
        this.error = '无数据'
        return
      }

      try {
        const option = this.buildChartOption()
        console.log('[ChartWidget] 指标卡配置:', option)

        // 直接设置指标卡配置，触发响应式更新
        this.metricCardConfig = option

        console.log('[ChartWidget] 指标卡已渲染')
      } catch (err) {
        console.error('[ChartWidget] 指标卡渲染失败:', err)
        this.error = '指标卡渲染失败: ' + err.message
      }
    },

    /**
     * 渲染表格
     */
    renderTable() {
      console.log('[ChartWidget] 渲染表格')

      if (!this.chartData) {
        console.warn('[ChartWidget] chartData 为空，无法渲染表格')
        this.error = '无数据'
        return
      }

      try {
        const option = this.buildChartOption()
        console.log('[ChartWidget] 表格配置:', option)

        // 直接设置表格配置，触发响应式更新
        this.tableConfig = option

        console.log('[ChartWidget] 表格已渲染')
      } catch (err) {
        console.error('[ChartWidget] 表格渲染失败:', err)
        this.error = '表格渲染失败: ' + err.message
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

      console.log('[transformChartData] 图表类型:', chartType)

      // 如果没有配置维度和指标，返回空数据
      if (dimensions.length === 0 && metrics.length === 0) {
        console.warn('维度和指标都为空')
        return { categories: [], series: [], data: [] }
      }

      // 特殊处理：饼图、玫瑰图、环形图
      if (['pie', 'rose', 'doughnut'].includes(chartType)) {
        console.log('[transformChartData] 处理饼图类型数据')

        const metric = metrics[0]
        const metricField = metric.fieldName || metric.label || metric.field
        const dimensionField = dimensions.length > 0 ? dimensions[0] : null
        const dimensionFieldKey = dimensionField ? (dimensionField.fieldName || dimensionField.label || dimensionField.field) : null

        const pieData = rawData.map(row => {
          const name = dimensionFieldKey ? row[dimensionFieldKey] : '未命名'
          const value = this.parseNumber(row[metricField])

          return {
            name: String(name),
            value: value
          }
        })

        console.log('[transformChartData] 饼图数据:', pieData)

        return {
          data: pieData
        }
      }

      // 特殊处理：组合图/双轴图
      if (['dualAxis', 'dual-axis', 'combination'].includes(chartType)) {
        console.log('[transformChartData] 处理组合图/双轴图数据')

        const dimensionField = dimensions.length > 0 ? dimensions[0] : null
        const dimFieldKey = dimensionField ? (dimensionField.fieldName || dimensionField.label || dimensionField.field) : null

        // 提取分类数据（X 轴）
        const categories = []
        const categoryMap = new Map()

        if (dimFieldKey) {
          rawData.forEach((row) => {
            const categoryValue = row[dimFieldKey]
            if (categoryValue !== undefined && categoryValue !== null) {
              const categoryStr = String(categoryValue)
              if (!categoryMap.has(categoryStr)) {
                categoryMap.set(categoryStr, categories.length)
                categories.push(categoryStr)
              }
            }
          })
        }

        // 将指标分组为左轴和右轴
        // 默认：第一个指标在左轴，其他在右轴
        const leftMetrics = metrics.filter(m => (m.yAxisIndex || 0) === 0 || !m.yAxisIndex)
        const rightMetrics = metrics.filter(m => (m.yAxisIndex || 0) === 1)

        // 如果没有明确配置yAxisIndex，按指标数量均分
        if (metrics.every(m => !m.yAxisIndex)) {
          const midPoint = Math.ceil(metrics.length / 2)
          leftMetrics.length = 0
          rightMetrics.length = 0
          metrics.forEach((m, i) => {
            if (i < midPoint) {
              leftMetrics.push(m)
            } else {
              rightMetrics.push(m)
            }
          })
        }

        console.log('[transformChartData] 左轴指标:', leftMetrics.length, '右轴指标:', rightMetrics.length)

        // 构建左轴系列
        const leftSeries = leftMetrics.map(metric => {
          const fieldKey = metric.fieldName || metric.label || metric.field
          const seriesType = metric.chartType || metric.type || 'bar'
          const seriesData = []

          if (dimFieldKey) {
            // 有维度：按维度分组
            rawData.forEach((row) => {
              const categoryValue = row[dimFieldKey]
              if (categoryValue !== undefined && categoryValue !== null) {
                const categoryStr = String(categoryValue)
                const index = categoryMap.get(categoryStr)
                if (index !== undefined) {
                  seriesData[index] = this.parseNumber(row[fieldKey])
                }
              }
            })
          } else {
            // 无维度：直接使用指标值
            rawData.forEach(row => {
              seriesData.push(this.parseNumber(row[fieldKey]))
            })
          }

          return {
            name: metric.comment || metric.fieldName || metric.label || metric.field,
            type: seriesType,
            data: seriesData,
            stack: metric.stack || null
          }
        })

        // 构建右轴系列
        const rightSeries = rightMetrics.map(metric => {
          const fieldKey = metric.fieldName || metric.label || metric.field
          const seriesType = metric.chartType || metric.type || 'line'
          const seriesData = []

          if (dimFieldKey) {
            // 有维度：按维度分组
            rawData.forEach((row) => {
              const categoryValue = row[dimFieldKey]
              if (categoryValue !== undefined && categoryValue !== null) {
                const categoryStr = String(categoryValue)
                const index = categoryMap.get(categoryStr)
                if (index !== undefined) {
                  seriesData[index] = this.parseNumber(row[fieldKey])
                }
              }
            })
          } else {
            // 无维度：直接使用指标值
            rawData.forEach(row => {
              seriesData.push(this.parseNumber(row[fieldKey]))
            })
          }

          return {
            name: metric.comment || metric.fieldName || metric.label || metric.field,
            type: seriesType,
            data: seriesData,
            stack: metric.stack || null
          }
        })

        console.log('[transformChartData] 组合图数据 - categories:', categories)
        console.log('[transformChartData] 组合图数据 - leftSeries:', leftSeries)
        console.log('[transformChartData] 组合图数据 - rightSeries:', rightSeries)

        return {
          categories,
          leftSeries,
          rightSeries
        }
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
      console.log('[ChartWidget] updateChartOption 被调用:', {
        isMetricCard: this.isMetricCard,
        hasChartInstance: !!this.chartInstance,
        hasChartData: !!this.chartData,
        willUpdate: !!this.chartInstance || this.isMetricCard
      })

      // 指标卡特殊处理
      if (this.isMetricCard) {
        if (this.chartData) {
          this.renderMetricCard()
        }
        return
      }

      // 只要有图表实例就更新样式，不依赖 chartData
      // 这样可以在没有数据的情况下也预览样式（如主题切换）
      if (!this.chartInstance) {
        console.warn('[ChartWidget] 图表实例不存在，跳过更新')
        return
      }

      const option = this.buildChartOption()
      console.log('[ChartWidget] 应用图表配置:', option)

      this.chartInstance.setOption(option, true)
      console.log('[ChartWidget] 图表配置已应用')
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
     * 设置 ResizeObserver 监听容器尺寸变化
     */
    setupResizeObserver() {
      if (!this.$refs.chartContainer) {
        return
      }

      // 如果浏览器支持 ResizeObserver
      if (typeof ResizeObserver !== 'undefined') {
        this.resizeObserver = new ResizeObserver(() => {
          if (this.chartInstance) {
            // 使用 requestAnimationFrame 优化性能
            requestAnimationFrame(() => {
              if (this.chartInstance) {
                this.chartInstance.resize()
              }
            })
          }
        })

        this.resizeObserver.observe(this.$refs.chartContainer)
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
     * 处理图表点击 - 打开指标详情对话框
     */
    async handleChartClick() {
      // 编辑模式下不处理点击
      if (this.isEditMode) {
        return
      }

      console.log('[ChartWidget] ===== handleChartClick 被调用 =====')
      console.log('[ChartWidget] config.dataConfig:', this.config.dataConfig)

      // 优先检查是否有关联的多指标配置
      const configMetricIds = this.config.dataConfig?.metricIds
      const configMetricList = this.config.dataConfig?.metricList

      if (configMetricIds && configMetricIds.length > 0) {
        console.log('[ChartWidget] 发现多指标配置，触发 multi-metric-click')
        console.log('[ChartWidget] metricIds:', configMetricIds)
        console.log('[ChartWidget] metricList:', configMetricList)
        this.$emit('multi-metric-click', configMetricIds, configMetricList || [])
        return
      }

      // 检查是否有关联的单指标ID（向后兼容）
      const metricId = this.config.dataConfig?.metricId
      if (metricId) {
        console.log('[ChartWidget] 点击图表，打开单指标详情:', metricId)
        this.$emit('metric-click', metricId)
        return
      }

      // 没有关联指标时的处理 - 尝试自动匹配所有指标
      const metrics = this.config.dataConfig?.metrics || []
      console.log('[ChartWidget] 未找到预匹配的指标配置，尝试自动匹配，metrics:', metrics)
      if (metrics.length === 0) {
        // 没有配置指标字段
        this.$message({
          message: '该图表未配置指标字段，请在设计器中拖拽指标字段到"指标"区域',
          type: 'info',
          duration: 2000,
          showClose: true
        })
        console.log('[ChartWidget] 图表未配置指标字段')
        return
      }

      // 尝试根据字段名自动查找所有指标
      try {
        console.log('[ChartWidget] 尝试自动匹配指标字段:', metrics.map(m => m.field || m.fieldName))
        const response = await listMetricMetadata({ pageNum: 1, pageSize: 1000 })
        const allMetrics = response.rows || response.data || []

        // 匹配所有指标
        const matchedMetrics = []
        const metricList = []

        for (const metric of metrics) {
          const fieldName = metric.field || metric.fieldName
          if (!fieldName) continue

          // 根据 metric_code 匹配（字段名即为metric_code）
          const matched = allMetrics.find(m => m.metricCode === fieldName)

          if (matched) {
            matchedMetrics.push(matched.id)
            metricList.push({
              id: matched.id,
              code: matched.metricCode,
              label: metric.label || metric.comment || matched.metricName
            })
            console.log('[ChartWidget] 匹配到指标:', fieldName, '->', matched.metricCode, 'ID:', matched.id)
          }
        }

        if (matchedMetrics.length > 0) {
          // 触发多指标详情对话框
          console.log('[ChartWidget] 准备触发 multi-metric-click 事件:', {
            matchedMetrics,
            metricList,
            count: matchedMetrics.length
          })
          this.$emit('multi-metric-click', matchedMetrics, metricList)
          console.log('[ChartWidget] multi-metric-click 事件已发送')
        } else {
          // 未找到匹配的指标
          const metricNames = metrics.map(m => m.comment || m.fieldName || m.name).join('、')
          this.$message({
            message: `该图表配置了指标字段（${metricNames}），但未在指标元数据表中找到对应的指标定义`,
            type: 'warning',
            duration: 3000,
            showClose: true
          })
          console.log('[ChartWidget] 未找到匹配的指标元数据')
        }
      } catch (error) {
        console.error('[ChartWidget] 查询指标元数据失败:', error)
        const metricNames = metrics.map(m => m.comment || m.fieldName || m.name).join('、')
        this.$message({
          message: `查询指标元数据失败：${error.message || error}`,
          type: 'error',
          duration: 3000,
          showClose: true
        })
      }
    },

    /**
     * 处理配置
     */
    handleConfig() {
      this.$emit('config', this.config)
    },

    /**
     * 获取指标卡样式
     */
    getMetricCardStyle() {
      const styleConfig = this.config.styleConfig || {}
      const height = this.isEditMode ? 'calc(100% - 40px)' : '100%'

      return {
        height,
        backgroundColor: styleConfig.backgroundColor || '#fff',
        borderRadius: (styleConfig.borderRadius || 4) + 'px',
        padding: '20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }
    },

    /**
     * 格式化指标值
     */
    formatMetricValue(value) {
      if (value === null || value === undefined) return '-'

      // 如果是数字，格式化显示
      if (typeof value === 'number') {
        // 如果值很大，使用千分位
        if (Math.abs(value) >= 10000) {
          return value.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
        }
        // 保留两位小数
        return value.toFixed(2).replace(/\.00$/, '')
      }

      return String(value)
    },

    /**
     * 获取趋势样式类
     */
    getTrendClass(trend) {
      if (trend > 0) return 'trend-up'
      if (trend < 0) return 'trend-down'
      return 'trend-flat'
    },

    /**
     * 获取趋势图标
     */
    getTrendIcon(trend) {
      if (trend > 0) return 'el-icon-top'
      if (trend < 0) return 'el-icon-bottom'
      return 'el-icon-minus'
    },

    /**
     * 获取表格列定义
     */
    getTableColumns(data) {
      if (!data || data.length === 0) return []

      const firstRow = data[0]
      const columns = []

      // 获取数据配置中的字段信息
      const dataConfig = this.config.dataConfig || {}
      const dimensions = dataConfig.dimensions || []
      const metrics = dataConfig.metrics || []

      // 构建字段映射（field -> label）
      const fieldMap = new Map()
      dimensions.forEach(dim => {
        const field = dim.field || dim.fieldName
        if (field) {
          fieldMap.set(field, dim.label || dim.comment || dim.fieldName || field)
        }
      })
      metrics.forEach(metric => {
        const field = metric.field || metric.fieldName
        if (field) {
          fieldMap.set(field, metric.label || metric.comment || metric.fieldName || field)
        }
      })

      // 根据字段顺序生成列定义
      Object.keys(firstRow).forEach(key => {
        columns.push({
          key: key,
          label: fieldMap.get(key) || key
        })
      })

      console.log('[ChartWidget] 表格列定义:', columns)
      return columns
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

// 指标卡样式
.metric-card-container {
  width: 100%;
  min-height: 100px;
  box-sizing: border-box;
  transition: all 0.3s ease;

  &.clickable {
    cursor: pointer;

    &:hover {
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
      border-radius: 4px;
    }
  }

  &.with-toolbar {
    position: absolute;
    top: 40px;
    left: 0;
    right: 0;
    bottom: 0;
  }
}

.metric-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 20px;
}

.metric-card-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.8;
}

.metric-card-value {
  font-weight: bold;
  line-height: 1.2;
  margin-bottom: 8px;
  text-align: center;

  .metric-unit {
    font-size: 0.6em;
    margin-left: 4px;
    opacity: 0.7;
    font-weight: normal;
  }
}

.metric-card-title {
  font-weight: 500;
  text-align: center;
  opacity: 0.8;
  margin-bottom: 8px;
}

.metric-card-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 12px;

  &.trend-up {
    color: #67c23a;
    background: rgba(103, 194, 58, 0.1);
  }

  &.trend-down {
    color: #f56c6c;
    background: rgba(245, 108, 108, 0.1);
  }

  &.trend-flat {
    color: #909399;
    background: rgba(144, 147, 153, 0.1);
  }

  i {
    font-size: 16px;
  }
}

.chart-container {
  width: 100%;
  height: 100%;
  /* 确保容器有最小尺寸，避免ECharts初始化失败 */
  min-width: 100px;
  min-height: 100px;
  box-sizing: border-box;
  /* 强制限制最大宽度，图表必须适应容器 */
  max-width: 100%;
  overflow: hidden;

  /* 可点击状态 */
  &.clickable {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
      border-radius: 4px;
    }
  }
}

/* 编辑模式下，图表容器需要向下移动避开工具栏 */
.chart-container.with-toolbar {
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  bottom: 0;
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

// 表格样式
.table-container {
  width: 100%;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: #fff;

  &.clickable {
    cursor: pointer;

    &:hover {
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
      border-radius: 4px;
    }
  }

  &.with-toolbar {
    position: absolute;
    top: 40px;
    left: 0;
    right: 0;
    bottom: 0;
  }

  ::v-deep .el-table {
    font-size: 13px;

    .el-table__header th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }

    .el-table__body tr:hover > td {
      background-color: #f5f7fa;
    }
  }

  ::v-deep .el-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 40px 0;
  }
}
</style>
