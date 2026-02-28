<template>
  <el-dialog
    :visible.sync="dialogVisible"
    :title="dialogTitle"
    :width="dialogWidth"
    :fullscreen="isFullscreen"
    :close-on-click-modal="false"
    :before-close="handleClose"
    class="metric-detail-dialog multi-metric-dialog"
    append-to-body
    @open="handleOpen"
  >
    <!-- 头部信息 -->
    <div slot="title" class="dialog-header">
      <span>{{ dialogTitle }}</span>
    </div>

    <!-- 操作按钮 -->
    <div class="dialog-toolbar">
      <el-button-group>
        <el-button
          icon="el-icon-full-screen"
          size="small"
          @click="toggleFullscreen"
        >
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
        <el-button
          icon="el-icon-refresh"
          size="small"
          :loading="refreshing"
          @click="handleRefresh"
        >
          刷新
        </el-button>
      </el-button-group>
    </div>

    <!-- 主容器：侧边栏 + 内容区 -->
    <div class="dialog-container">
      <!-- 左侧指标列表 -->
      <div v-if="isMultiMetric" class="metric-sidebar">
        <div class="sidebar-header">
          <h4>指标列表</h4>
          <el-tag size="small" type="info">{{ metricList.length }} 个指标</el-tag>
        </div>
        <div class="metric-list">
          <div
            v-for="metric in metricList"
            :key="metric.id"
            class="metric-item"
            :class="{ active: currentMetricId === metric.id }"
            @click="handleMetricChange(metric.id)"
          >
            <div class="metric-item-header">
              <span class="metric-item-name">{{ metric.label }}</span>
              <el-tag
                v-if="metric.id === currentMetricId"
                type="success"
                size="mini"
              >
                当前
              </el-tag>
            </div>
            <div class="metric-item-code">{{ metric.code }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="metric-content">
        <!-- 标签页 -->
        <el-tabs
          v-model="activeTab"
          type="border-card"
          @tab-click="handleTabClick"
        >
          <!-- 规范标签页 -->
          <el-tab-pane label="指标规范" name="specification">
            <specification-tab
              v-show="activeTab === 'specification'"
              :metric-id="currentMetricId"
              :metric-info="metricInfo"
            />
          </el-tab-pane>

          <!-- 血缘图谱标签页 -->
          <el-tab-pane label="血缘图谱" name="lineage">
            <lineage-tab
              ref="lineageTab"
              v-show="activeTab === 'lineage'"
              :metric-id="currentMetricId"
              :metric-ids="isMultiMetric ? metricIds : null"
              :metric-info="metricInfo"
              @switch-to-data="handleSwitchToData"
            />
          </el-tab-pane>

          <!-- 数据查询标签页 -->
          <el-tab-pane label="数据查询" name="data">
            <data-query-tab
              ref="dataQueryTab"
              v-show="activeTab === 'data'"
              :metric-id="queryMetricId"
              :metric-info="metricInfo"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon class="el-icon-loading" />
      <span>加载中...</span>
    </div>
  </el-dialog>
</template>

<script>
import SpecificationTab from './SpecificationTab.vue'
import LineageTab from './LineageTab.vue'
import DataQueryTab from './DataQueryTab.vue'
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
    // 单个指标ID（保持向后兼容）
    metricId: {
      type: [Number, String],
      default: null
    },
    // 多个指标ID（新增）
    metricIds: {
      type: Array,
      default: () => []
    },
    // 指标列表信息（包含id、name、code）
    metricList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      activeTab: 'specification',
      loading: false,
      refreshing: false,
      isFullscreen: false,
      currentMetricId: null, // 当前选中的指标ID
      queryMetricId: null, // 用于数据查询标签页的指标ID
      metricInfo: {
        id: null,
        metricCode: '',
        metricName: '',
        businessDefinition: '',
        technicalFormula: '',
        calculationLogic: '',
        ownerDept: '',
        dataFreshness: '',
        updateFrequency: '',
        status: '0'
      }
    }
  },
  computed: {
    // 是否为多指标模式
    isMultiMetric() {
      return this.metricIds && this.metricIds.length > 1
    },
    dialogTitle() {
      if (this.isMultiMetric) {
        return `指标详情 - 共 ${this.metricIds.length} 个指标`
      }
      return this.metricInfo.metricName ? `指标详情 - ${this.metricInfo.metricName}` : '指标详情'
    },
    dialogWidth() {
      return this.isFullscreen ? '100%' : (this.isMultiMetric ? '95%' : '90%')
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val) {
        this.initializeMetrics()
      }
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
    },
    metricId: {
      handler(newVal) {
        if (newVal && this.dialogVisible && !this.isMultiMetric) {
          this.currentMetricId = newVal
          this.loadMetricInfo(newVal)
        }
      }
    },
    metricIds: {
      handler(newVal) {
        if (newVal && newVal.length > 0 && this.dialogVisible) {
          this.initializeMetrics()
        }
      }
    }
  },
  methods: {
    // 初始化指标
    initializeMetrics() {
      console.log('[MetricDetailDialog] ===== initializeMetrics 被调用 =====')
      console.log('[MetricDetailDialog] metricId:', this.metricId)
      console.log('[MetricDetailDialog] metricIds:', this.metricIds)
      console.log('[MetricDetailDialog] metricList:', this.metricList)
      console.log('[MetricDetailDialog] isMultiMetric:', this.isMultiMetric)

      if (this.isMultiMetric) {
        // 多指标模式：默认选中第一个
        console.log('[MetricDetailDialog] 多指标模式，选中第一个指标:', this.metricIds[0])
        this.currentMetricId = this.metricIds[0]
        this.loadMetricInfo(this.currentMetricId)
      } else if (this.metricId) {
        // 单指标模式
        console.log('[MetricDetailDialog] 单指标模式，选中指标:', this.metricId)
        this.currentMetricId = this.metricId
        this.loadMetricInfo(this.metricId)
      } else {
        console.log('[MetricDetailDialog] 警告：metricId 和 metricIds 都为空！')
      }
    },

    // 切换指标
    handleMetricChange(metricId) {
      if (this.currentMetricId === metricId) return
      this.currentMetricId = metricId
      this.loadMetricInfo(metricId)

      // 刷新当前标签页
      this.$nextTick(() => {
        if (this.activeTab === 'lineage') {
          const lineageTab = this.$refs.lineageTab
          if (lineageTab && lineageTab.loadLineageData) {
            lineageTab.loadLineageData()
          }
        } else if (this.activeTab === 'data') {
          const dataQueryTab = this.$refs.dataQueryTab
          if (dataQueryTab && dataQueryTab.loadDataById) {
            dataQueryTab.loadDataById()
          }
        }
      })
    },

    async loadMetricInfo(id) {
      const metricId = id || this.currentMetricId
      if (!metricId) return

      this.loading = true
      try {
        const res = await getMetricMetadata(metricId)
        if (res.code === 200) {
          this.metricInfo = res.data || {}
          this.queryMetricId = metricId
        } else {
          this.$message.error(res.msg || '获取指标信息失败')
        }
      } catch (error) {
        console.error('加载指标信息失败:', error)
        this.$message.error('加载指标信息失败')
      } finally {
        this.loading = false
      }
    },

    handleRefresh() {
      this.refreshing = true
      this.loadMetricInfo().finally(() => {
        this.refreshing = false
      })
    },

    handleTabClick(tab) {
      console.log('切换标签页:', tab.name)
      // 使用 $nextTick 确保 DOM 更新后再通知子组件
      this.$nextTick(() => {
        if (tab.name === 'lineage') {
          // 血缘图谱标签页，通知组件加载数据
          const lineageTab = this.$refs.lineageTab
          if (lineageTab && lineageTab.loadLineageData) {
            lineageTab.loadLineageData()
          }
        } else if (tab.name === 'data') {
          // 数据查询标签页，通知组件加载数据
          const dataQueryTab = this.$refs.dataQueryTab
          if (dataQueryTab && dataQueryTab.loadDataById) {
            dataQueryTab.loadDataById()
          }
        }
      })
    },

    handleSwitchToData(metricId) {
      console.log('切换到数据查询标签页，指标ID:', metricId)
      this.queryMetricId = metricId
      this.activeTab = 'data'
      // 使用 $nextTick 确保标签页切换完成后再加载数据
      this.$nextTick(() => {
        const dataQueryTab = this.$refs.dataQueryTab
        if (dataQueryTab && dataQueryTab.loadDataById) {
          dataQueryTab.loadDataById()
        }
      })
    },

    handleOpen() {
      console.log('对话框打开，指标ID:', this.metricId)
    },

    handleClose() {
      this.activeTab = 'specification'
      this.$emit('close')
    },

    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen
    }
  }
}
</script>

<style lang="scss" scoped>
.metric-detail-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px;
    border-bottom: 1px solid #ebeef5;
  }

  .dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    span {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .el-tag {
      margin-left: 10px;
    }
  }

  .dialog-toolbar {
    margin-bottom: 15px;
    text-align: right;
  }

  .loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    z-index: 9999;

    .el-icon {
      font-size: 32px;
      margin-bottom: 10px;
    }
  }
}

// 多指标对话框样式
.multi-metric-dialog {
  ::v-deep .el-dialog__body {
    padding: 0;
  }

  .dialog-container {
    display: flex;
    min-height: 500px;
    max-height: 70vh;
  }

  // 左侧指标列表
  .metric-sidebar {
    width: 280px;
    border-right: 1px solid #ebeef5;
    background: #fafafa;
    display: flex;
    flex-direction: column;

    .sidebar-header {
      padding: 15px 20px;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      align-items: center;
      justify-content: space-between;

      h4 {
        margin: 0;
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }
    }

    .metric-list {
      flex: 1;
      overflow-y: auto;
      padding: 10px;
    }

    .metric-item {
      padding: 12px 15px;
      margin-bottom: 8px;
      background: white;
      border: 1px solid #ebeef5;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
      }

      &.active {
        border-color: #409eff;
        background: #ecf5ff;
      }

      .metric-item-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 6px;
      }

      .metric-item-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .metric-item-code {
        font-size: 12px;
        color: #909399;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  // 右侧内容区
  .metric-content {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    ::v-deep .el-tabs {
      display: flex;
      flex-direction: column;
      height: 100%;

      .el-tabs__header {
        margin: 0;
        flex-shrink: 0;
      }

      .el-tabs__content {
        flex: 1;
        overflow: auto;
      }

      .el-tab-pane {
        height: 100%;
      }
    }
  }
}
</style>
