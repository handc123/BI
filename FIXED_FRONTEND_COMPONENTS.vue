<!--
  ========================================
  修正后的前端 Vue 组件代码示例
  版本: 1.1
  修正日期: 2025-02-26
  主要修正:
    - 修复 .sync 语法错误，使用显式事件处理
    - 完善组件逻辑和错误处理
    - 添加权限控制
  ========================================
-->

<!-- 1. MetricDetailDialog 主对话框组件 -->
<template>
  <el-dialog
    :visible="visible"
    :title="dialogTitle"
    width="80%"
    :close-on-click-modal="false"
    :before-close="handleClose"
    @update:visible="$emit('update:visible', $event)"
  >
    <el-tabs v-model="activeTab" type="border-card" v-loading="loading">
      <!-- 指标规范标签页 -->
      <el-tab-pane label="指标规范" name="specification">
        <specification-tab
          v-if="activeTab === 'specification'"
          :metric-id="metricId"
          :metric-data="metricData"
        />
      </el-tab-pane>

      <!-- 血缘关系标签页 -->
      <el-tab-pane label="血缘关系" name="lineage" v-hasPermi="['bi:metric:lineage']">
        <lineage-tab
          v-if="activeTab === 'lineage'"
          :metric-id="metricId"
          @node-click="handleNodeClick"
        />
      </el-tab-pane>

      <!-- 数据查询标签页 -->
      <el-tab-pane label="数据查询" name="data" v-hasPermi="['bi:metric:data']">
        <data-query-tab
          v-if="activeTab === 'data'"
          :metric-id="metricId"
          :node-id="selectedNodeId"
        />
      </el-tab-pane>
    </el-tabs>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import SpecificationTab from './SpecificationTab'
import LineageTab from './LineageTab'
import DataQueryTab from './DataQueryTab'
import { getMetricMetadata } from '@/api/bi/metric'

export default {
  name: 'MetricDetailDialog',
  components: {
    SpecificationTab,
    LineageTab,
    DataQueryTab
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    metricId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      activeTab: 'specification',
      loading: false,
      selectedNodeId: null,
      metricData: null
    }
  },
  computed: {
    dialogTitle() {
      return `指标详情 #${this.metricId}`
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.loadMetricData()
      }
    }
  },
  methods: {
    async loadMetricData() {
      this.loading = true
      try {
        const res = await getMetricMetadata(this.metricId)
        this.metricData = res.data
      } catch (error) {
        console.error('加载指标数据失败:', error)
        this.$message.error('加载指标数据失败')
      } finally {
        this.loading = false
      }
    },
    handleNodeClick(nodeId) {
      this.selectedNodeId = nodeId
      this.activeTab = 'data'
    },
    handleClose() {
      this.$emit('update:visible', false)
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>

<!-- 2. ChartWidget 组件修正 -->
<template>
  <div
    ref="chartContainer"
    class="chart-widget"
    :class="{ 'clickable': clickable && !isEditMode }"
    @click="handleClick"
  >
    <!-- 图表内容 -->
    <div v-if="chartConfig" class="chart-content"></div>
    <div v-else class="chart-placeholder">请配置图表</div>
  </div>
</template>

<script>
export default {
  name: 'ChartWidget',
  props: {
    chartConfig: {
      type: Object,
      default: () => ({})
    },
    // 新增：是否可点击
    clickable: {
      type: Boolean,
      default: false
    },
    // 新增：是否为编辑模式
    isEditMode: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleClick() {
      // 编辑模式或不可点击时，忽略点击事件
      if (!this.clickable || this.isEditMode) return

      // 检查是否有关联的指标
      const metricId = this.chartConfig?.dataConfig?.metricId
      if (!metricId) {
        console.warn('图表未关联指标')
        return
      }

      // 触发点击事件，传递完整的信息
      this.$emit('chart-click', {
        type: 'metric',
        id: metricId,
        data: this.chartConfig
      })
    }
  }
}
</script>

<style scoped>
.chart-widget {
  width: 100%;
  height: 100%;
  min-height: 200px;
}

.chart-widget.clickable {
  cursor: pointer;
  transition: all 0.2s;
}

.chart-widget.clickable:hover {
  opacity: 0.9;
  transform: scale(1.01);
}

.chart-content {
  width: 100%;
  height: 100%;
}

.chart-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  background-color: #f5f5f5;
}
</style>

<!-- 3. Dashboard View 组件集成 -->
<template>
  <div class="dashboard-view">
    <!-- 仪表板头部 -->
    <div class="dashboard-header">
      <h1>{{ dashboard.dashboardName }}</h1>
      <p>{{ dashboard.dashboardDesc }}</p>
    </div>

    <!-- 图表组件容器 -->
    <div class="charts-container">
      <chart-widget
        v-for="(chart, index) in charts"
        :key="index"
        :chart-config="chart"
        :clickable="true"
        :is-edit-mode="false"
        @chart-click="handleChartClick"
      />
    </div>

    <!-- 指标详情对话框 -->
    <metric-detail-dialog
      :visible.sync="metricDialogVisible"
      :metric-id="selectedMetricId"
      @close="handleMetricDialogClose"
    />
  </div>
</template>

<script>
import ChartWidget from '@/components/ChartWidget'
import MetricDetailDialog from '@/components/MetricDetailDialog'

export default {
  name: 'DashboardView',
  components: {
    ChartWidget,
    MetricDetailDialog
  },
  data() {
    return {
      dashboard: {},
      charts: [],
      metricDialogVisible: false,
      selectedMetricId: null
    }
  },
  created() {
    this.loadDashboard()
  },
  methods: {
    async loadDashboard() {
      try {
        // 加载仪表板数据
        const dashboardId = this.$route.params.id
        // const res = await getDashboard(dashboardId)
        // this.dashboard = res.data
        // this.charts = res.data.charts || []

        // 模拟数据
        this.dashboard = {
          dashboardName: '示例仪表板',
          dashboardDesc: '这是一个示例仪表板'
        }
        this.charts = [
          { dataConfig: { metricId: 1 } },
          { dataConfig: { metricId: 2 } }
        ]
      } catch (error) {
        console.error('加载仪表板失败:', error)
        this.$message.error('加载仪表板失败')
      }
    },
    handleChartClick(payload) {
      if (payload.type === 'metric') {
        this.selectedMetricId = payload.id
        this.metricDialogVisible = true
      }
    },
    handleMetricDialogClose() {
      this.metricDialogVisible = false
      this.selectedMetricId = null
    }
  }
}
</script>

<style scoped>
.dashboard-view {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 20px;
}

.dashboard-header h1 {
  font-size: 24px;
  margin: 0 0 10px 0;
}

.dashboard-header p {
  color: #666;
  margin: 0;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}
</style>
