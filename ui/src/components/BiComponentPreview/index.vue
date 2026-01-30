<template>
  <div class="bi-component-preview">
    <div v-if="loading" class="preview-loading">
      <i class="el-icon-loading"></i>
      <span>加载中...</span>
    </div>
    <div v-else-if="error" class="preview-error">
      <i class="el-icon-warning"></i>
      <span>{{ error }}</span>
    </div>
    <div v-else-if="chartData" class="preview-chart" ref="chartContainer"></div>
    <el-empty v-else description="暂无数据" :image-size="80"></el-empty>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getVisualizationData } from '@/api/bi/visualization'

export default {
  name: 'BiComponentPreview',
  props: {
    visualizationId: {
      type: Number,
      required: true
    },
    filters: {
      type: Array,
      default: () => []
    },
    theme: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      error: null,
      chartData: null,
      chartInstance: null
    }
  },
  watch: {
    visualizationId: {
      handler() {
        this.loadData()
      },
      immediate: true
    },
    filters: {
      handler() {
        this.loadData()
      },
      deep: true
    }
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
  },
  methods: {
    loadData() {
      if (!this.visualizationId) return

      this.loading = true
      this.error = null

      getVisualizationData(this.visualizationId, this.filters)
        .then(response => {
          this.chartData = response.data
          this.$nextTick(() => {
            this.renderChart()
          })
        })
        .catch(error => {
          this.error = error.message || '加载数据失败'
        })
        .finally(() => {
          this.loading = false
        })
    },
    renderChart() {
      if (!this.$refs.chartContainer || !this.chartData) return

      // 销毁旧实例
      if (this.chartInstance) {
        this.chartInstance.dispose()
      }

      // 创建新实例
      this.chartInstance = echarts.init(this.$refs.chartContainer)

      // 应用主题配置
      const option = this.buildChartOption()
      this.chartInstance.setOption(option)
    },
    buildChartOption() {
      // 这里应该根据可视化类型和数据构建ECharts配置
      // 简化版本，实际应该根据visualization的config来构建
      return {
        title: {
          text: this.chartData.title || '',
          textStyle: {
            fontFamily: this.theme.fontFamily || 'Microsoft YaHei',
            fontSize: this.theme.fontSize || 14
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: this.chartData.legend || []
        },
        xAxis: {
          type: 'category',
          data: this.chartData.xAxisData || []
        },
        yAxis: {
          type: 'value'
        },
        series: this.chartData.series || []
      }
    },
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.bi-component-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-loading,
.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;

  i {
    font-size: 32px;
    margin-bottom: 10px;
  }
}

.preview-error {
  color: #f56c6c;
}

.preview-chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
}
</style>
