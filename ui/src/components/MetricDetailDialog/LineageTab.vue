<template>
  <div class="lineage-tab">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="el-icon-loading" />
      <span>加载中...</span>
    </div>

    <!-- 血缘图谱内容 -->
    <div v-else class="lineage-content">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button-group>
          <el-button
            :type="viewMode === 'graph' ? 'primary' : ''"
            size="small"
            icon="el-icon-share"
            @click="changeViewMode('graph')"
          >
            完整图谱
          </el-button>
          <el-button
            :type="viewMode === 'upstream' ? 'primary' : ''"
            size="small"
            icon="el-icon-top"
            @click="changeViewMode('upstream')"
          >
            上游血缘
          </el-button>
          <el-button
            :type="viewMode === 'downstream' ? 'primary' : ''"
            size="small"
            icon="el-icon-bottom"
            @click="changeViewMode('downstream')"
          >
            下游血缘
          </el-button>
        </el-button-group>

        <el-button
          size="small"
          icon="el-icon-refresh"
          :loading="refreshing"
          @click="refreshGraph"
        >
          刷新
        </el-button>

        <el-button
          size="small"
          icon="el-icon-download"
          @click="exportGraph"
        >
          导出图片
        </el-button>

        <el-button
          size="small"
          icon="el-icon-zoom-in"
          @click="zoomIn"
        >
          放大
        </el-button>

        <el-button
          size="small"
          icon="el-icon-zoom-out"
          @click="zoomOut"
        >
          缩小
        </el-button>
      </div>

      <!-- 图谱统计信息 -->
      <div v-if="graphData.metadata" class="graph-stats">
        <el-tag size="small" type="info">节点: {{ graphData.metadata.nodeCount }}</el-tag>
        <el-tag size="small" type="info">关系: {{ graphData.metadata.edgeCount }}</el-tag>
        <el-tag size="small" type="success">模式: {{ viewModeText }}</el-tag>
      </div>

      <!-- ECharts图谱容器 -->
      <div ref="graphContainer" class="graph-container" />

      <!-- 节点详情面板 -->
      <el-drawer
        :visible.sync="nodeDetailVisible"
        title="节点详情"
        direction="rtl"
        size="400px"
      >
        <div v-if="selectedNode" class="node-detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="节点ID">
              {{ selectedNode.id }}
            </el-descriptions-item>
            <el-descriptions-item label="节点名称">
              {{ selectedNode.name }}
            </el-descriptions-item>
            <el-descriptions-item label="节点类型">
              <el-tag :type="getNodeTypeColor(selectedNode.type)" size="small">
                {{ getNodeTypeText(selectedNode.type) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedNode.category" label="分类">
              {{ selectedNode.category }}
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedNode.value !== undefined" label="值">
              {{ selectedNode.value }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 如果是指标节点，显示详细信息 -->
          <div v-if="selectedNode.type === 'metric'" class="metric-detail">
            <el-divider>指标信息</el-divider>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="指标编码">
                {{ selectedNode.metricCode || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="业务定义">
                <div class="text-content">
                  {{ selectedNode.businessDefinition || '暂无定义' }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item v-if="selectedNode.technicalFormula" label="技术公式">
                <div class="code-content">
                  {{ selectedNode.technicalFormula }}
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 操作按钮 -->
          <div class="node-actions">
            <el-button
              v-if="selectedNode.type === 'metric'"
              type="primary"
              size="small"
              icon="el-icon-data-line"
              @click="viewMetricData"
            >
              查看数据
            </el-button>
            <el-button
              v-if="selectedNode.type === 'dataset'"
              type="success"
              size="small"
              icon="el-icon-s-data"
              @click="viewDatasetDetail"
            >
              数据集详情
            </el-button>
          </div>
        </div>
      </el-drawer>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && (!graphData.nodes || graphData.nodes.length === 0)"
      description="暂无血缘数据"
      :image-size="120"
    />
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getMetricLineage, getUpstreamLineage, getDownstreamLineage } from '@/api/bi/metric'

export default {
  name: 'LineageTab',
  props: {
    metricId: {
      type: [Number, String],
      required: true
    },
    metricInfo: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      loading: false,
      refreshing: false,
      viewMode: 'graph', // graph, upstream, downstream
      graphData: {
        nodes: [],
        edges: [],
        metadata: null
      },
      chart: null,
      nodeDetailVisible: false,
      selectedNode: null,
      renderRetryCount: 0  // 渲染重试计数
    }
  },
  computed: {
    viewModeText() {
      const modeMap = {
        graph: '完整图谱',
        upstream: '上游血缘',
        downstream: '下游血缘'
      }
      return modeMap[this.viewMode] || '完整图谱'
    }
  },
  watch: {
    metricId: {
      handler(newVal) {
        if (newVal) {
          this.loadLineageData()
        }
      }
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    async loadLineageData() {
      if (!this.metricId) return

      this.loading = true
      try {
        let res
        if (this.viewMode === 'upstream') {
          res = await getUpstreamLineage(this.metricId, 5)
        } else if (this.viewMode === 'downstream') {
          res = await getDownstreamLineage(this.metricId, 5)
        } else {
          res = await getMetricLineage(this.metricId, 'graph')
        }

        console.log('[LineageTab] API返回结果:', res)

        if (res.code === 200) {
          this.graphData = res.data || { nodes: [], edges: [] }
          console.log('[LineageTab] graphData:', this.graphData)
          console.log('[LineageTab] 节点数:', this.graphData.nodes?.length || 0)
          console.log('[LineageTab] 边数:', this.graphData.edges?.length || 0)

          // 使用 setTimeout 确保 DOM 完全渲染
          setTimeout(() => {
            console.log('[LineageTab] 开始渲染图谱')
            this.renderGraph()
          }, 100)
        } else {
          this.$message.error(res.msg || '获取血缘数据失败')
        }
      } catch (error) {
        console.error('[LineageTab] 加载血缘数据失败:', error)
        this.$message.error('加载血缘数据失败')
      } finally {
        this.loading = false
      }
    },

    changeViewMode(mode) {
      this.viewMode = mode
      this.loadLineageData()
    },

    refreshGraph() {
      this.refreshing = true
      this.loadLineageData().finally(() => {
        this.refreshing = false
      })
    },

    renderGraph() {
      console.log('[LineageTab] renderGraph 被调用')

      if (!this.$refs.graphContainer) {
        console.error('[LineageTab] graphContainer ref 不存在!')
        return
      }

      // 如果已存在图表实例，先销毁
      if (this.chart) {
        this.chart.dispose()
      }

      // 创建图表实例
      this.chart = echarts.init(this.$refs.graphContainer)

      console.log('[LineageTab] 原始数据 - 节点数:', this.graphData.nodes.length, '边数:', this.graphData.edges.length)

      // 构建树型数据结构
      // 找到根节点（当前查看的指标）
      const rootMetricId = String(this.metricId)
      const rootMetric = this.graphData.nodes.find(n => n.id === rootMetricId)

      if (!rootMetric) {
        console.error('[LineageTab] 找不到根节点:', rootMetricId)
        return
      }

      console.log('[LineageTab] 根节点:', rootMetric)

      // 构建节点映射
      const nodeMap = {}
      this.graphData.nodes.forEach(node => {
        nodeMap[node.id] = node
      })

      // 构建邻接表
      const upstreamMap = {} // id -> [upstream node ids]
      const downstreamMap = {} // id -> [downstream node ids]

      this.graphData.nodes.forEach(node => {
        upstreamMap[node.id] = []
        downstreamMap[node.id] = []
      })

      this.graphData.edges.forEach(edge => {
        // edge.source -> edge.target 表示 source流向target
        // 所以对于target来说，source是它的上游
        // 对于source来说，target是它的下游
        if (upstreamMap[edge.target]) {
          upstreamMap[edge.target].push(edge.source)
        }
        if (downstreamMap[edge.source]) {
          downstreamMap[edge.source].push(edge.target)
        }
      })

      console.log('[LineageTab] 上游映射:', upstreamMap)
      console.log('[LineageTab] 下游映射:', downstreamMap)

      // 递归构建树（上游节点作为子节点）
      const buildTree = (nodeId, visited = new Set()) => {
        if (visited.has(nodeId)) {
          return null
        }
        visited.add(nodeId)

        const node = nodeMap[nodeId]
        if (!node) {
          return null
        }

        const treeNode = {
          name: node.name,
          id: node.id,
          value: node.value || 10,
          itemStyle: {
            color: '#1890ff'
          },
          label: {
            show: true,
            fontSize: 13,
            fontWeight: 'bold'
          },
          children: []
        }

        // 添加上游节点作为子节点
        const upstreamIds = upstreamMap[nodeId] || []
        upstreamIds.forEach(childId => {
          const childTree = buildTree(childId, visited)
          if (childTree) {
            treeNode.children.push(childTree)
          }
        })

        return treeNode
      }

      const treeData = buildTree(rootMetricId)
      console.log('[LineageTab] 树型数据:', treeData)

      // 使用ECharts tree图表
      const option = {
        title: {
          text: '指标血缘图谱',
          left: 'center',
          top: 10,
          textStyle: {
            fontSize: 16,
            fontWeight: 600
          }
        },
        tooltip: {
          trigger: 'item',
          triggerOn: 'mousemove',
          formatter: params => {
            const node = params.data
            return `
              <div style="padding: 5px;">
                <strong>${node.name}</strong><br/>
                ID: ${node.id}
              </div>
            `
          }
        },
        series: [
          {
            type: 'tree',
            data: [treeData],

            top: '15%',
            left: '10%',
            bottom: '15%',
            right: '20%',

            symbolSize: 14,
            symbol: 'circle',

            edgeShape: 'curve',
            edgeForkPosition: '63%',
            initialTreeDepth: 3,

            roam: true,
            label: {
              position: 'left',
              verticalAlign: 'middle',
              align: 'right',
              fontSize: 13
            },

            leaves: {
              label: {
                position: 'right',
                verticalAlign: 'middle',
                align: 'left'
              }
            },

            emphasis: {
              focus: 'descendant'
            },

            expandAndCollapse: true,
            animationDuration: 550,
            animationDurationUpdate: 750
          }
        ]
      }

      console.log('[LineageTab] 设置tree option')
      this.chart.setOption(option)

      // 延迟resize
      setTimeout(() => {
        this.chart.resize()
        console.log('[LineageTab] resize完成')
      }, 100)

      // 绑定点击事件
      this.chart.off('click')
      this.chart.on('click', params => {
        if (params.componentType === 'series') {
          const nodeData = params.data
          const originalNode = this.graphData.nodes.find(n => n.id === nodeData.id)
          if (originalNode) {
            this.handleNodeClick(originalNode)
          }
        }
      })

      // 响应式调整
      window.addEventListener('resize', this.handleResize)
    },

    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },

    handleNodeClick(node) {
      this.selectedNode = node
      this.nodeDetailVisible = true
    },

    getNodeSize(type) {
      const sizeMap = {
        metric: 60,
        dataset: 50,
        datasource: 40,
        table: 40,
        field: 30
      }
      return sizeMap[type] || 40
    },

    getNodeColor(type) {
      const colorMap = {
        metric: '#1890ff',
        dataset: '#52c41a',
        datasource: '#fa8c16',
        table: '#722ed1',
        field: '#eb2f96'
      }
      return colorMap[type] || '#909399'
    },

    getNodeTypeColor(type) {
      const colorMap = {
        metric: 'primary',
        dataset: 'success',
        datasource: 'warning',
        table: '',
        field: 'danger'
      }
      return colorMap[type] || 'info'
    },

    getNodeTypeText(type) {
      const typeMap = {
        metric: '指标',
        dataset: '数据集',
        datasource: '数据源',
        table: '数据表',
        field: '字段'
      }
      return typeMap[type] || type
    },

    getEdgeColor(label) {
      if (!label) return '#999'
      const colorMap = {
        aggregation: '#1890ff',
        calculation: '#52c41a',
        filter: '#fa8c16',
        derived_from: '#722ed1',
        depends_on: '#eb2f96'
      }
      return colorMap[label] || '#999'
    },

    getCategories() {
      const types = ['metric', 'dataset', 'datasource', 'table', 'field']
      return types.map(type => ({
        name: this.getNodeTypeText(type),
        itemStyle: {
          color: this.getNodeColor(type)
        }
      }))
    },

    getLegendData() {
      return this.getCategories().map(cat => cat.name)
    },

    zoomIn() {
      if (this.chart) {
        const option = this.chart.getOption()
        const series = option.series[0]
        const currentZoom = series.zoom || 1
        this.chart.setOption({
          series: [{
            zoom: Math.min(currentZoom + 0.2, 3)
          }]
        })
      }
    },

    zoomOut() {
      if (this.chart) {
        const option = this.chart.getOption()
        const series = option.series[0]
        const currentZoom = series.zoom || 1
        this.chart.setOption({
          series: [{
            zoom: Math.max(currentZoom - 0.2, 0.3)
          }]
        })
      }
    },

    exportGraph() {
      if (!this.chart) return

      const url = this.chart.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#fff'
      })

      const link = document.createElement('a')
      link.href = url
      link.download = `lineage_${this.metricId}_${this.viewMode}_${Date.now()}.png`
      link.click()
    },

    viewMetricData() {
      // 切换到数据查询标签页
      this.$emit('switch-to-data', this.selectedNode.id)
      this.nodeDetailVisible = false
    },

    viewDatasetDetail() {
      this.$message.info('数据集详情功能待实现')
    }
  }
}
</script>

<style lang="scss" scoped>
.lineage-tab {
  padding: 20px;
  height: 100%;

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;

    .el-icon-loading {
      font-size: 32px;
      margin-bottom: 10px;
    }
  }

  .lineage-content {
    .toolbar {
      margin-bottom: 15px;
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }

    .graph-stats {
      margin-bottom: 15px;
      display: flex;
      gap: 10px;
    }

    .graph-container {
      width: 100%;
      min-height: 600px;
      height: 600px;
      border: 1px solid #dfe4ed;
      border-radius: 4px;
      background: #fafafa;
      position: relative;
      overflow: visible !important;
    }

    .graph-container canvas {
      position: absolute !important;
      left: 0 !important;
      top: 0 !important;
      z-index: 1 !important;
    }

    .node-detail {
      .metric-detail {
        margin-top: 20px;

        .text-content {
          line-height: 1.6;
          color: #606266;
          white-space: pre-wrap;
          word-break: break-word;
        }

        .code-content {
          background: #f5f7fa;
          padding: 8px;
          border-radius: 4px;
          font-family: 'Courier New', monospace;
          font-size: 12px;
          line-height: 1.5;
          color: #303133;
          white-space: pre-wrap;
          word-break: break-all;
        }
      }

      .node-actions {
        margin-top: 20px;
        display: flex;
        gap: 10px;
      }
    }
  }
}
</style>
