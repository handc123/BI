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
        <!-- 渲染所有组件 -->
        <div
          v-for="component in components"
          :key="component.id"
          class="view-component"
          :style="getComponentStyle(component)"
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
            :query-params="{}"
            :condition-mappings="[]"
            :query-conditions="[]"
            :is-edit-mode="false"
          />

          <!-- 查询组件 -->
          <query-widget
            v-else-if="component.type === 'query'"
            :component-id="component.id"
            :config="component.dataConfig"
            @query="handleQuery"
          />

          <!-- 其他组件类型 -->
          <div v-else class="unsupported-component">
            <i class="el-icon-warning"></i>
            <p>不支持的组件类型: {{ component.type }}</p>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="!loading && components.length === 0"
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
          background: { type: 'color', value: '#f0f2f5', opacity: 1 }
        },
        globalStyle: {
          colorScheme: ['#5470c6', '#91cc75', '#fac858'],
          titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
          fontFamily: 'Microsoft YaHei'
        }
      },
      components: []
    }
  },
  computed: {
    dashboardId() {
      return this.$route.params.id
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
    // 加载仪表板
    async loadDashboard(id) {
      this.loading = true
      try {
        const response = await getDashboardConfig(id)
        const config = response.data

        // 设置仪表板基本信息
        this.currentDashboard = {
          id: config.dashboard.id,
          name: config.dashboard.dashboardName || config.dashboard.name,
          theme: config.dashboard.theme || 'light',
          canvasConfig: this.parseJSON(config.dashboard.canvasConfig, {
            width: 1520,
            height: 1080,
            background: { type: 'color', value: '#f0f2f5', opacity: 1 }
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

        this.$message.success('仪表板加载成功')
      } catch (error) {
        console.error('加载仪表板失败:', error)
        this.$message.error('加载仪表板失败: ' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
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
      // 可以在这里实现全局查询逻辑
    },

    // 获取画布样式
    getCanvasStyle() {
      const config = this.currentDashboard.canvasConfig
      const background = config.background || {}
      
      let backgroundStyle = ''
      if (background.type === 'color') {
        backgroundStyle = background.value || '#f0f2f5'
      } else if (background.type === 'gradient') {
        backgroundStyle = `linear-gradient(${background.direction || '180deg'}, ${background.startColor || '#fff'}, ${background.endColor || '#f0f2f5'})`
      }

      return {
        width: config.width + 'px',
        minHeight: config.height + 'px',
        background: backgroundStyle,
        position: 'relative'
      }
    },

    // 获取组件样式
    getComponentStyle(component) {
      return {
        position: 'absolute',
        left: component.position.x + 'px',
        top: component.position.y + 'px',
        width: component.size.width + 'px',
        height: component.size.height + 'px',
        zIndex: component.zIndex,
        background: '#ffffff',
        border: '1px solid #e4e7ed',
        borderRadius: '4px',
        overflow: 'hidden',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
      }
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

.view-component {
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
