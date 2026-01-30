<template>
  <div class="config-panel">
    <div class="config-content">
      <!-- 配置标题 -->
      <div class="config-header">
        <h3>{{ configTitle }}</h3>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" type="border-card" class="config-tabs" :key="targetType">
        <!-- 数据配置标签页 (仅组件) -->
        <el-tab-pane v-if="targetType === 'component' && target" label="数据" name="data">
          <data-config
            v-if="activeTab === 'data'"
            :key="target && target.id"
            :initial-config="target && target.dataConfig"
            @config-change="handleDataConfigChange"
            @refresh-chart="handleRefreshChart"
          />
        </el-tab-pane>

        <!-- 样式配置标签页 -->
        <el-tab-pane label="样式" name="style">
          <style-config
            v-if="activeTab === 'style' && target"
            :target="target"
            :target-type="targetType"
            @change="handleStyleConfigChange"
          />
        </el-tab-pane>

        <!-- 高级配置标签页 (仅组件) -->
        <el-tab-pane v-if="targetType === 'component' && target" label="高级" name="advanced">
          <advanced-config
            v-if="activeTab === 'advanced'"
            :component="target"
            @change="handleAdvancedConfigChange"
          />
        </el-tab-pane>

        <!-- 仪表板配置标签页 (仅仪表板) -->
        <el-tab-pane v-if="targetType === 'dashboard' && target" label="仪表板" name="dashboard">
          <dashboard-config
            v-if="activeTab === 'dashboard'"
            :dashboard="target"
            @change="handleDashboardConfigChange"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import DataConfigPanel from '@/components/DataConfigPanel/index.vue'
import StyleConfig from './StyleConfig.vue'
import AdvancedConfig from './AdvancedConfig.vue'
import DashboardConfig from './DashboardConfig.vue'

export default {
  name: 'ConfigPanel',
  components: {
    DataConfig: DataConfigPanel,
    StyleConfig,
    AdvancedConfig,
    DashboardConfig
  },
  props: {
    // 配置目标 (组件或仪表板)
    target: {
      type: Object,
      default: null
    },
    // 目标类型: 'component' | 'dashboard'
    targetType: {
      type: String,
      default: 'component',
      validator: value => ['component', 'dashboard'].includes(value)
    }
  },
  data() {
    return {
      activeTab: 'data'
    }
  },
  computed: {
    configTitle() {
      if (!this.target) return '仪表板配置'
      if (this.targetType === 'component') {
        return this.target.componentName || this.target.name || `${this.getComponentTypeName(this.target.componentType || this.target.type)}组件`
      }
      return this.target.dashboardName || this.target.name || '仪表板配置'
    }
  },
  watch: {
    targetType: {
      handler(newType) {
        // 切换目标类型时重置标签页
        if (newType === 'component') {
          this.activeTab = 'data'
        } else {
          this.activeTab = 'style'
        }
      },
      immediate: true
    },
    target: {
      handler(newTarget, oldTarget) {
        // 只有当目标ID变化时才重置标签页
        // 避免在同一个组件上切换标签页时被重置
        if (newTarget && oldTarget && newTarget.id === oldTarget.id) {
          return
        }
        
        // 目标变化时重置标签页
        if (newTarget && this.targetType === 'component') {
          this.activeTab = 'data'
        } else if (newTarget && this.targetType === 'dashboard') {
          this.activeTab = 'style'
        }
      }
    }
  },
  methods: {
    getComponentTypeName(type) {
      const typeMap = {
        chart: '图表',
        query: '查询',
        text: '富文本',
        media: '媒体',
        tabs: '标签页'
      }
      return typeMap[type] || '未知'
    },
    handleDataConfigChange(config) {
      this.$emit('config-change', {
        type: 'data',
        config: config
      })
    },
    handleStyleConfigChange(config) {
      this.$emit('config-change', {
        type: 'style',
        config: config
      })
    },
    handleAdvancedConfigChange(config) {
      this.$emit('config-change', {
        type: 'advanced',
        config: config
      })
    },
    handleDashboardConfigChange(config) {
      this.$emit('config-change', {
        type: 'dashboard',
        config: config
      })
    },
    handleRefreshChart() {
      // 转发刷新图表事件到父组件
      this.$emit('refresh-chart')
    }
  }
}
</script>

<style scoped>
.config-panel {
  width: 400px;
  min-width: 400px;
  flex-shrink: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-left: 1px solid #e6e6e6;
  box-sizing: border-box;
  position: relative;
  z-index: 100; /* 确保在最上层 */
}

.config-content {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.config-header {
  padding: 16px;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.config-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.config-tabs {
  flex: 1;
  overflow: hidden;
  width: 100%;
}

.config-tabs >>> .el-tabs__content {
  height: calc(100% - 40px);
  overflow-y: auto;
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}

.config-tabs >>> .el-tab-pane {
  height: 100%;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
}

/* 确保所有子组件都是固定 400px 宽度 */
.config-tabs >>> .el-tab-pane > div {
  width: 400px;
  box-sizing: border-box;
  flex-shrink: 0;
}
</style>
