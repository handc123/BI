<template>
  <div class="dashboard-preview" :style="getDashboardStyle()">
    <!-- 预览模式提示 -->
    <div class="preview-banner">
      <i class="el-icon-view"></i>
      <span>预览模式</span>
      <el-button type="text" size="small" @click="handleClose">
        <i class="el-icon-close"></i> 关闭预览
      </el-button>
    </div>

    <!-- 全局筛选器 -->
    <div class="global-filters" v-if="hasGlobalFilters">
      <el-card shadow="never" class="filter-card">
        <div slot="header" class="filter-header">
          <span>筛选条件</span>
          <el-button type="text" size="small" @click="resetFilters">重置</el-button>
        </div>
        <el-form :inline="true" size="small">
          <el-form-item
            v-for="filter in globalFilters"
            :key="filter.id"
            :label="filter.label"
          >
            <!-- 日期范围 -->
            <el-date-picker
              v-if="filter.type === 'dateRange'"
              v-model="filterValues[filter.id]"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              @change="handleFilterChange"
            />
            
            <!-- 单选 -->
            <el-select
              v-else-if="filter.type === 'single'"
              v-model="filterValues[filter.id]"
              placeholder="请选择"
              clearable
              @change="handleFilterChange"
            >
              <el-option
                v-for="option in filter.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            
            <!-- 多选 -->
            <el-select
              v-else-if="filter.type === 'multiple'"
              v-model="filterValues[filter.id]"
              placeholder="请选择"
              multiple
              clearable
              @change="handleFilterChange"
            >
              <el-option
                v-for="option in filter.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 仪表板内容 -->
    <div class="dashboard-content" v-loading="loading">
      <grid-layout
        v-if="layoutComponents.length > 0"
        :layout="layoutComponents"
        :col-num="12"
        :row-height="30"
        :is-draggable="false"
        :is-resizable="false"
        :is-mirrored="false"
        :vertical-compact="true"
        :margin="[10, 10]"
        :use-css-transforms="true"
      >
        <grid-item
          v-for="item in layoutComponents"
          :key="item.i"
          :x="item.x"
          :y="item.y"
          :w="item.w"
          :h="item.h"
          :i="item.i"
        >
          <div class="component-wrapper">
            <div class="component-header">
              <span class="component-title">{{ item.name }}</span>
            </div>
            <div class="component-content">
              <bi-component-preview
                :ref="`component_${item.i}`"
                :visualization-id="item.visualizationId"
                :filters="getComponentFilters(item.i)"
                :theme="themeConfig"
              />
            </div>
          </div>
        </grid-item>
      </grid-layout>
      
      <el-empty v-else description="仪表板暂无内容" />
    </div>

    <!-- 错误提示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
      style="margin: 20px"
    />
  </div>
</template>

<script>
import { GridLayout, GridItem } from 'vue-grid-layout'
import { getDashboardConfig } from '@/api/bi/dashboard'
import BiComponentPreview from '@/components/BiComponentPreview'

export default {
  name: 'DashboardPreview',
  components: {
    GridLayout,
    GridItem,
    BiComponentPreview
  },
  props: {
    dashboardId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      dashboard: {
        dashboardName: '',
        theme: 'light',
        canvasConfig: {},
        globalStyle: {},
        status: '0'
      },
      layoutComponents: [],
      components: [],
      queryConditions: [],
      conditionMappings: [],
      globalFilters: [],
      filterValues: {},
      themeConfig: {},
      loading: false,
      error: null
    }
  },
  computed: {
    hasGlobalFilters() {
      return this.globalFilters && this.globalFilters.length > 0
    }
  },
  created() {
    this.loadDashboardConfig()
  },
  methods: {
    /**
     * 加载仪表板配置
     */
    async loadDashboardConfig() {
      try {
        this.loading = true
        this.error = null

        const response = await getDashboardConfig(this.dashboardId)
        const config = response.data

        // 加载仪表板基本信息
        if (config.dashboard) {
          this.dashboard = config.dashboard
          this.parseConfigurations()
        }

        // 加载组件列表
        if (config.components && config.components.length > 0) {
          this.components = config.components
          this.layoutComponents = this.convertComponentsToLayout(config.components)
        }

        // 加载查询条件
        if (config.queryConditions) {
          this.queryConditions = config.queryConditions
          this.globalFilters = this.convertConditionsToFilters(config.queryConditions)
          this.initFilterValues()
        }

        // 加载条件映射
        if (config.conditionMappings) {
          this.conditionMappings = config.conditionMappings
        }

        this.loading = false
      } catch (error) {
        console.error('加载仪表板配置失败:', error)
        this.error = '加载仪表板配置失败: ' + (error.message || '未知错误')
        this.loading = false
      }
    },

    /**
     * 解析JSON配置字符串
     */
    parseConfigurations() {
      if (typeof this.dashboard.canvasConfig === 'string') {
        try {
          this.dashboard.canvasConfig = JSON.parse(this.dashboard.canvasConfig)
        } catch (e) {
          console.warn('解析canvasConfig失败:', e)
        }
      }

      if (typeof this.dashboard.globalStyle === 'string') {
        try {
          this.dashboard.globalStyle = JSON.parse(this.dashboard.globalStyle)
        } catch (e) {
          console.warn('解析globalStyle失败:', e)
        }
      }

      this.themeConfig = this.dashboard.globalStyle || {}
    },

    /**
     * 将后端组件数据转换为布局组件
     */
    convertComponentsToLayout(components) {
      return components.map(comp => {
        let dataConfig = {}
        if (typeof comp.dataConfig === 'string') {
          try {
            dataConfig = JSON.parse(comp.dataConfig)
          } catch (e) {
            console.warn('解析组件dataConfig失败:', e)
          }
        } else {
          dataConfig = comp.dataConfig || {}
        }

        return {
          i: `comp_${comp.id}`,
          x: Math.floor(comp.positionX / 10),
          y: Math.floor(comp.positionY / 10),
          w: Math.floor(comp.width / 10),
          h: Math.floor(comp.height / 10),
          visualizationId: dataConfig.datasetId || dataConfig.visualizationId,
          name: comp.componentName,
          type: comp.componentType,
          componentId: comp.id,
          dataConfig: dataConfig
        }
      })
    },

    /**
     * 将查询条件转换为筛选器
     */
    convertConditionsToFilters(conditions) {
      return conditions.filter(c => c.isVisible === '1').map(condition => {
        let config = {}
        if (typeof condition.config === 'string') {
          try {
            config = JSON.parse(condition.config)
          } catch (e) {
            console.warn('解析条件config失败:', e)
          }
        } else {
          config = condition.config || {}
        }

        return {
          id: condition.id,
          label: condition.conditionName,
          type: condition.conditionType,
          defaultValue: condition.defaultValue,
          options: config.options || [],
          targetFields: this.getConditionTargetFields(condition.id)
        }
      })
    },

    /**
     * 获取条件的目标字段
     */
    getConditionTargetFields(conditionId) {
      return this.conditionMappings
        .filter(m => m.conditionId === conditionId)
        .map(m => ({
          componentId: `comp_${m.componentId}`,
          field: m.fieldName
        }))
    },

    /**
     * 初始化筛选器值
     */
    initFilterValues() {
      this.globalFilters.forEach(filter => {
        this.$set(this.filterValues, filter.id, filter.defaultValue)
      })
    },

    /**
     * 获取组件的筛选器
     */
    getComponentFilters(componentId) {
      const filters = []
      
      this.globalFilters.forEach(filter => {
        if (filter.targetFields) {
          const target = filter.targetFields.find(t => t.componentId === componentId)
          if (target && this.filterValues[filter.id]) {
            filters.push({
              field: target.field,
              operator: 'eq',
              value: this.filterValues[filter.id]
            })
          }
        }
      })
      
      return filters
    },

    /**
     * 处理筛选器变化
     */
    handleFilterChange() {
      // 刷新所有组件
      this.layoutComponents.forEach(comp => {
        this.refreshComponent(comp)
      })
    },

    /**
     * 重置筛选器
     */
    resetFilters() {
      this.initFilterValues()
      this.handleFilterChange()
    },

    /**
     * 刷新组件
     */
    refreshComponent(component) {
      const ref = this.$refs[`component_${component.i}`]
      if (ref && ref[0]) {
        ref[0].refresh()
      }
    },

    /**
     * 关闭预览
     */
    handleClose() {
      this.$emit('close')
    },

    /**
     * 获取仪表板样式
     */
    getDashboardStyle() {
      const theme = this.themeConfig
      if (!theme) return {}
      
      return {
        background: theme.backgroundColor || '#f5f5f5',
        fontFamily: theme.fontFamily || 'Microsoft YaHei',
        fontSize: `${theme.fontSize || 14}px`
      }
    }
  }
}
</script>

<style scoped>
.dashboard-preview {
  min-height: 100vh;
  padding: 20px;
}

.preview-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: #409eff;
  color: white;
  border-radius: 4px;
  margin-bottom: 20px;
}

.preview-banner i {
  font-size: 18px;
}

.preview-banner span {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
}

.preview-banner .el-button {
  color: white;
}

.global-filters {
  margin-bottom: 20px;
}

.filter-card {
  background: white;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-content {
  min-height: 500px;
}

.component-wrapper {
  height: 100%;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.component-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.component-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.component-content {
  height: calc(100% - 45px);
  padding: 10px;
  overflow: auto;
}
</style>
