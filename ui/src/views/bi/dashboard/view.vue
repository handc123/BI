<template>
  <div class="dashboard-view-page">
    <!-- 顶部工具栏 -->
    <div class="view-toolbar">
      <div class="toolbar-left">
        <el-button
          type="text"
          icon="el-icon-back"
          @click="handleBack"
        >返回</el-button>
        <span class="dashboard-title">{{ currentDashboard.name }}</span>
      </div>
      <div class="toolbar-right">
        <el-button
          type="primary"
          size="small"
          icon="el-icon-refresh"
          @click="handleRefresh"
        >刷新</el-button>
        <el-button
          type="success"
          size="small"
          icon="el-icon-edit"
          @click="handleEdit"
          v-hasPermi="['bi:dashboard:edit']"
        >编辑</el-button>
      </div>
    </div>

    <!-- 仪表板内容区域 -->
    <div class="dashboard-view-content" v-loading="loading">
      <div
        class="view-canvas"
        :style="getCanvasStyle()"
      >
        <!-- 查询条件区域 -->
        <div v-if="queryConditions.length > 0" class="query-conditions-bar">
          <query-widget
            :conditions="queryConditions"
            :is-edit-mode="false"
            @query="handleQuery"
            @reset="handleResetQuery"
          />
        </div>

        <!-- 渲染所有组件 - 网格布局，一行两个 -->
        <div class="view-grid">
          <div
            v-for="component in chartComponents"
            :key="component.id"
            class="view-component"
          >
            <!-- 图表组件 -->
            <chart-widget
              v-if="component.type === 'chart'"
              :config="{
                id: component.id,
                dataConfig: component.dataConfig,
                styleConfig: component.styleConfig,
                advancedConfig: component.advancedConfig
              }"
              :query-params="queryParams"
              :condition-mappings="getComponentMappings(component.id)"
              :query-conditions="queryConditions"
              :is-edit-mode="false"
              @metric-click="handleMetricClick"
              @multi-metric-click="handleMultiMetricClick"
              @field-drill-click="handleFieldDrillClick"
            />

            <!-- 其他组件类型 -->
            <div v-else class="unsupported-component">
              <i class="el-icon-warning"></i>
              <p>不支持的组件类型: {{ component.type }}</p>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="!loading && chartComponents.length === 0"
          description="仪表板暂无内容"
          :image-size="200"
        />
      </div>
    </div>

  </div>
</template>

<script>
import { getDashboardConfig } from '@/api/bi/dashboard'
import ChartWidget from '@/components/ChartWidget'
import QueryWidget from '@/components/QueryWidget'

export default {
  name: 'DashboardView',
  components: {
    ChartWidget,
    QueryWidget
  },
  data() {
    return {
      loading: false,
      currentDashboard: {
        id: null,
        name: '仪表板查看',
        theme: 'light',
        canvasConfig: {
          width: 1520,
          height: 1080,
          gridSize: 10,
          margin: { top: 20, right: 20, bottom: 20, left: 20 },
          background: { type: 'color', value: '#f0f2f5', opacity: 1 },
          // 卡片样式
          card: { enabled: true, shadowType: 'default', shadowColor: 'rgba(0, 0, 0, 0.1)', borderRadius: 8 },
          // 布局配置
          layout: { type: 'grid', columns: 2, gap: 16 },
          // 网格配置
          gridColor: '#e0e0e0',
          columnDividerColor: '#409eff',
          showGrid: true,
          // 响应式配置
          responsive: {
            enabled: false,
            breakpoints: { sm: '768px', md: '1024px', lg: '1520px' }
          }
        },
        globalStyle: {
          colorScheme: ['#5470c6', '#91cc75', '#fac858'],
          titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
          fontFamily: 'Microsoft YaHei'
        }
      },
      components: [],
      queryConditions: [],
      conditionMappings: [],
      queryParams: {}
    }
  },
  computed: {
    dashboardId() {
      return this.$route.params.id
    },
    // 过滤掉 query 类型组件，避免与顶部查询条件栏重复
    chartComponents() {
      return this.components.filter(c => c.type !== 'query')
    }
  },
  created() {
    if (this.dashboardId) {
      this.loadDashboard(this.dashboardId)
    } else {
      this.$message.error('未找到仪表盘ID')
    }
  },
  methods: {
    normalizeLongId(id) {
      if (id === undefined || id === null || id === '') {
        return null
      }
      if (typeof id === 'number' && Number.isFinite(id)) {
        return id
      }
      const text = String(id).trim()
      if (!text) {
        return null
      }
      if (/^\d+$/.test(text)) {
        return Number(text)
      }
      const match = text.match(/(\d+)$/)
      return match && match[1] ? Number(match[1]) : null
    },

    buildSnapshotFieldMap(componentId) {
      const normalizedComponentId = this.normalizeLongId(componentId)
      if (!normalizedComponentId) {
        return {}
      }
      const map = {}
      const conditions = this.queryConditions || []
      const mappings = this.conditionMappings || []
      conditions.forEach(condition => {
        if (!condition || condition.id === undefined || condition.id === null) {
          return
        }
        const hit = mappings.find(m =>
          String(m.conditionId) === String(condition.id) &&
          String(m.componentId) === String(normalizedComponentId) &&
          m.fieldName
        )
        if (hit) {
          map[String(condition.id)] = hit.fieldName
        }
      })
      return map
    },
    // 加载仪表板
    async loadDashboard(id) {
      this.loading = true
      try {
        const response = await getDashboardConfig(id)
        const config = response.data

        console.log('[DashboardView] 从后端获取的完整配置:', config)

        // 设置仪表板基本信息
        this.currentDashboard = {
          id: config.dashboard.id,
          name: config.dashboard.dashboardName || config.dashboard.name,
          theme: config.dashboard.theme || 'light',
          canvasConfig: this.parseJSON(config.dashboard.canvasConfig, {
            width: 1520,
            height: 1080,
            gridSize: 10,
            margin: { top: 20, right: 20, bottom: 20, left: 20 },
            background: { type: 'color', value: '#f0f2f5', opacity: 1 },
            // 卡片样式
            card: { enabled: true, shadowType: 'default', shadowColor: 'rgba(0, 0, 0, 0.1)', borderRadius: 8 },
            // 布局配置
            layout: { type: 'grid', columns: 2, gap: 16 },
            // 网格配置
            gridColor: '#e0e0e0',
            columnDividerColor: '#409eff',
            showGrid: true,
            // 响应式配置
            responsive: {
              enabled: false,
              breakpoints: { sm: '768px', md: '1024px', lg: '1520px' }
            }
          }),
          globalStyle: this.parseJSON(config.dashboard.globalStyle, {
            colorScheme: ['#5470c6', '#91cc75', '#fac858'],
            titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
            fontFamily: 'Microsoft YaHei'
          })
        }

        // 设置组件列表
        this.components = (config.components || []).map(comp => ({
          id: comp.id,
          type: comp.componentType,
          name: comp.componentName,
          position: { x: comp.positionX, y: comp.positionY },
          size: { width: comp.width, height: comp.height },
          zIndex: comp.zindex || comp.zIndex || 0,
          dataConfig: this.parseJSON(comp.dataConfig, {}),
          styleConfig: this.parseJSON(comp.styleConfig, {}),
          advancedConfig: this.parseJSON(comp.advancedConfig, {})
        }))

        // 设置查询条件
        this.queryConditions = (config.queryConditions || []).map(cond => ({
          id: cond.id,
          dashboardId: cond.dashboardId,
          componentId: cond.componentId,
          conditionName: cond.conditionName,
          conditionType: cond.conditionType,
          displayOrder: cond.displayOrder,
          isRequired: cond.isRequired,
          isVisible: cond.isVisible,
          defaultValue: cond.defaultValue,
          config: this.parseJSON(cond.config, {}),
          parentConditionId: cond.parentConditionId
        }))

        // 设置条件映射
        this.conditionMappings = config.conditionMappings || []

        // 初始化查询参数（使用默认值）
        this.initializeQueryParams()

        this.$message.success('仪表板加载成功')
      } catch (error) {
        console.error('加载仪表板失败:', error)
        this.$message.error('加载仪表板失败: ' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },

    // 初始化查询参数
    initializeQueryParams() {
      this.queryParams = {}
      this.queryConditions.forEach(condition => {
        if (condition.defaultValue) {
          this.queryParams[condition.id] = condition.defaultValue
        }
      })
    },

    // 返回列表
    handleBack() {
      this.$router.push('/bi/dashboard')
    },

    // 刷新仪表板
    handleRefresh() {
      if (this.dashboardId) {
        this.loadDashboard(this.dashboardId)
      }
    },

    // 编辑仪表板
    handleEdit() {
      this.$router.push('/bi/dashboard/designer/' + this.dashboardId)
    },

    // 处理查询
    handleQuery(queryData) {
      console.log('[DashboardView] 查询参数:', queryData)
      console.log('[DashboardView] 条件映射:', this.conditionMappings)
      console.log('[DashboardView] 查询条件:', this.queryConditions)

      // 更新查询参数
      this.queryParams = { ...queryData }

      console.log('[DashboardView] 更新后的 queryParams:', this.queryParams)
      console.log('[DashboardView] queryParams keys:', Object.keys(this.queryParams))

      this.$message.success('查询条件已更新，正在刷新图表...')
    },

    // 重置查询
    handleResetQuery() {
      console.log('[DashboardView] 重置查询')
      this.initializeQueryParams()
      this.$message.info('查询已重置')
    },

    // 处理指标点击事件
    handleMetricClick(metricId, componentId) {
      console.log('[DashboardView] 点击指标:', metricId)
      if (!metricId) {
        return
      }
      this.navigateToDrill(metricId, componentId)
    },

    // 处理多指标点击事件
    handleMultiMetricClick(metricIds, metricList, componentId) {
      console.log('[DashboardView] ===== 收到 multi-metric-click 事件 =====')
      console.log('[DashboardView] metricIds:', metricIds)
      console.log('[DashboardView] metricList:', metricList)
      console.log('[DashboardView] metricIds.length:', metricIds ? metricIds.length : 'null')
      if (!metricIds || metricIds.length === 0) {
        console.log('[DashboardView] metricIds 为空或长度为0，不打开对话框')
        return
      }

      // 多指标场景默认取首个（ChartWidget 应确保点击线对应指标排在首位）
      this.navigateToDrill(metricIds[0], componentId)
    },

    // 处理原始字段穿透事件
    handleFieldDrillClick(payload, componentId) {
      if (!payload || !payload.datasetId || !payload.metricField) {
        this.$message.warning('原始字段穿透参数不完整')
        return
      }
      this.navigateToDrillByField(payload, componentId)
    },

    navigateToDrill(metricId, componentId) {
      const snapshot = this.queryParams || {}
      const snapshotFieldMap = this.buildSnapshotFieldMap(componentId)
      const query = {
        metricId: String(metricId),
        querySnapshot: JSON.stringify(snapshot),
        querySnapshotFieldMap: JSON.stringify(snapshotFieldMap)
      }
      const normalizedComponentId = this.normalizeLongId(componentId)
      if (normalizedComponentId !== null) {
        query.componentId = String(normalizedComponentId)
      }

      // 常用透传字段（机构/日期）做便捷展示
      if (snapshot.sjbsjgid !== undefined && snapshot.sjbsjgid !== null) {
        query.orgId = String(snapshot.sjbsjgid)
      }
      if (snapshot.load_date) {
        query.date = String(snapshot.load_date)
      }

      this.$router.push({
        path: `/bi/dashboard/drill/${this.dashboardId}`,
        query
      })
    },

    navigateToDrillByField(payload, componentId) {
      const snapshot = this.queryParams || {}
      const snapshotFieldMap = this.buildSnapshotFieldMap(componentId)
      const query = {
        datasetId: String(payload.datasetId),
        metricField: String(payload.metricField),
        metricName: payload.metricName ? String(payload.metricName) : String(payload.metricField),
        querySnapshot: JSON.stringify(snapshot),
        querySnapshotFieldMap: JSON.stringify(snapshotFieldMap)
      }
      const normalizedComponentId = this.normalizeLongId(componentId)
      if (normalizedComponentId !== null) {
        query.componentId = String(normalizedComponentId)
      }
      if (snapshot.sjbsjgid !== undefined && snapshot.sjbsjgid !== null) {
        query.orgId = String(snapshot.sjbsjgid)
      }
      if (snapshot.load_date) {
        query.date = String(snapshot.load_date)
      }

      this.$router.push({
        path: `/bi/dashboard/drill/${this.dashboardId}`,
        query
      })
    },

    // 获取组件的条件映射
    getComponentMappings(componentId) {
      return this.conditionMappings.filter(m => String(m.componentId) === String(componentId))
    },

    // 获取画布样式
    getCanvasStyle() {
      const config = this.currentDashboard.canvasConfig
      const background = config.background || {}
      const card = config.card || {}

      let backgroundStyle = ''
      if (background.type === 'color') {
        backgroundStyle = background.value || '#f0f2f5'
      } else if (background.type === 'gradient') {
        const gradientType = background.gradientType || 'linear'
        const startColor = background.startColor || '#ffffff'
        const endColor = background.endColor || '#f0f2f5'

        if (gradientType === 'linear') {
          const direction = background.direction || 135
          backgroundStyle = `linear-gradient(${direction}deg, ${startColor}, ${endColor})`
        } else if (gradientType === 'radial') {
          const position = background.position || 'center'
          backgroundStyle = `radial-gradient(circle at ${position}, ${startColor}, ${endColor})`
        }
      } else if (background.type === 'image') {
        return {
          width: '100%',
          minHeight: config.height + 'px',
          backgroundImage: `url(${background.value})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          padding: '20px'
        }
      }

      const style = {
        width: '100%',
        minHeight: config.height + 'px',
        background: backgroundStyle,
        padding: '20px'
      }

      // 应用卡片样式（如果启用）
      if (card.enabled) {
        style.borderRadius = `${card.borderRadius || 8}px`
        if (card.shadowType === 'light') {
          style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.08)'
        } else if (card.shadowType === 'default') {
          style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.12)'
        } else if (card.shadowType === 'strong') {
          style.boxShadow = '0 8px 24px rgba(0, 0, 0, 0.18)'
        }
      }

      return style
    },

    // 工具方法: 解析JSON
    parseJSON(jsonStr, defaultValue = {}) {
      if (!jsonStr) return defaultValue
      try {
        return typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
      } catch (e) {
        console.error('JSON解析失败:', e)
        return defaultValue
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-view-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f0f2f5;
  overflow: hidden;
}

.view-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dashboard-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

.dashboard-view-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.view-canvas {
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.view-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.query-conditions-bar {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  margin-bottom: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  overflow: visible;
}

.view-component {
  height: 400px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  }
}

.unsupported-component {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;

  i {
    font-size: 48px;
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}
</style>
