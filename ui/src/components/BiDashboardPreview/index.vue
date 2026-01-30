<template>
  <div class="bi-dashboard-preview" :style="dashboardStyle">
    <!-- 全局筛选器 -->
    <div v-if="globalFilters.length > 0" class="global-filters">
      <el-form :inline="true" size="small">
        <el-form-item
          v-for="filter in globalFilters"
          :key="filter.id"
          :label="filter.label"
        >
          <!-- 日期范围筛选器 -->
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

          <!-- 单选筛选器 -->
          <el-select
            v-else-if="filter.type === 'single'"
            v-model="filterValues[filter.id]"
            placeholder="请选择"
            @change="handleFilterChange"
          >
            <el-option
              v-for="option in filter.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>

          <!-- 多选筛选器 -->
          <el-select
            v-else-if="filter.type === 'multiple'"
            v-model="filterValues[filter.id]"
            multiple
            placeholder="请选择"
            @change="handleFilterChange"
          >
            <el-option
              v-for="option in filter.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>

          <!-- 文本筛选器 -->
          <el-input
            v-else-if="filter.type === 'text'"
            v-model="filterValues[filter.id]"
            placeholder="请输入"
            @change="handleFilterChange"
          />

          <!-- 数值范围筛选器 -->
          <div v-else-if="filter.type === 'numberRange'" class="number-range">
            <el-input-number
              v-model="filterValues[filter.id][0]"
              :controls="false"
              placeholder="最小值"
              @change="handleFilterChange"
            />
            <span class="range-separator">-</span>
            <el-input-number
              v-model="filterValues[filter.id][1]"
              :controls="false"
              placeholder="最大值"
              @change="handleFilterChange"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="small" @click="applyFilters">应用</el-button>
          <el-button size="small" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 仪表板画布 -->
    <div class="dashboard-canvas">
      <grid-layout
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
                :visualization-id="item.visualizationId"
                :filters="getComponentFilters(item.i)"
                :theme="themeConfig"
              />
            </div>
          </div>
        </grid-item>
      </grid-layout>

      <el-empty v-if="layoutComponents.length === 0" description="仪表板暂无组件" :image-size="100"></el-empty>
    </div>
  </div>
</template>

<script>
import { GridLayout, GridItem } from 'vue-grid-layout'
import BiComponentPreview from '@/components/BiComponentPreview'

export default {
  name: 'BiDashboardPreview',
  components: {
    GridLayout,
    GridItem,
    BiComponentPreview
  },
  props: {
    dashboard: {
      type: Object,
      required: true
    },
    layoutComponents: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      filterValues: {},
      appliedFilters: {}
    }
  },
  computed: {
    globalFilters() {
      if (!this.dashboard.filterConfig || !this.dashboard.filterConfig.filters) {
        return []
      }
      return this.dashboard.filterConfig.filters
    },
    themeConfig() {
      return this.dashboard.themeConfig || {}
    },
    dashboardStyle() {
      const theme = this.themeConfig
      return {
        backgroundColor: theme.backgroundColor || '#f5f5f5',
        fontFamily: theme.fontFamily || 'Microsoft YaHei',
        fontSize: (theme.fontSize || 14) + 'px'
      }
    }
  },
  created() {
    this.initFilterValues()
  },
  methods: {
    initFilterValues() {
      this.globalFilters.forEach(filter => {
        if (filter.type === 'numberRange') {
          this.$set(this.filterValues, filter.id, [null, null])
        } else if (filter.type === 'multiple') {
          this.$set(this.filterValues, filter.id, [])
        } else {
          this.$set(this.filterValues, filter.id, filter.defaultValue || null)
        }
      })
      this.appliedFilters = { ...this.filterValues }
    },
    handleFilterChange() {
      // 筛选器值变化时的处理
    },
    applyFilters() {
      this.appliedFilters = { ...this.filterValues }
      this.$message.success('筛选器已应用')
    },
    resetFilters() {
      this.initFilterValues()
      this.appliedFilters = { ...this.filterValues }
      this.$message.success('筛选器已重置')
    },
    getComponentFilters(componentId) {
      const filters = []
      
      this.globalFilters.forEach(filter => {
        if (filter.targetFields) {
          const target = filter.targetFields.find(t => t.componentId === componentId)
          if (target && this.appliedFilters[filter.id]) {
            filters.push({
              field: target.field,
              operator: this.getFilterOperator(filter.type),
              value: this.appliedFilters[filter.id]
            })
          }
        }
      })
      
      return filters
    },
    getFilterOperator(filterType) {
      const operatorMap = {
        dateRange: 'between',
        single: 'eq',
        multiple: 'in',
        text: 'like',
        numberRange: 'between'
      }
      return operatorMap[filterType] || 'eq'
    }
  }
}
</script>

<style scoped lang="scss">
.bi-dashboard-preview {
  width: 100%;
  min-height: 100%;
  padding: 20px;
}

.global-filters {
  background: white;
  padding: 15px 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

  .number-range {
    display: flex;
    align-items: center;

    .range-separator {
      margin: 0 10px;
      color: #909399;
    }

    .el-input-number {
      width: 120px;
    }
  }
}

.dashboard-canvas {
  min-height: 400px;
  background: transparent;
}

.component-wrapper {
  height: 100%;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
  height: calc(100% - 41px);
  padding: 10px;
  overflow: auto;
}
</style>
