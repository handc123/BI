<template>
  <div class="data-config">
    <el-form :model="dataConfig" label-width="80px" size="small">
      <!-- 数据源选择 -->
      <el-form-item label="数据源">
        <el-select v-model="dataConfig.datasourceId" placeholder="请选择数据源" @change="handleDatasourceChange">
          <el-option
            v-for="ds in datasources"
            :key="ds.id"
            :label="ds.name || ds.datasourceName || `数据源${ds.id}`"
            :value="ds.id"
          />
        </el-select>
      </el-form-item>

      <!-- 数据集选择 -->
      <el-form-item label="数据集">
        <el-select v-model="dataConfig.datasetId" placeholder="请选择数据集" @change="handleDatasetChange">
          <el-option
            v-for="ds in datasets"
            :key="ds.id"
            :label="ds.name || ds.datasetName || `数据集${ds.id}`"
            :value="ds.id"
          />
        </el-select>
      </el-form-item>

      <!-- 字段配置 -->
      <el-divider>字段配置</el-divider>

      <!-- 维度字段 - 只在需要维度的图表类型下显示 -->
      <el-form-item v-if="requiresDimension" label="维度">
        <div class="field-drag-area">
          <div class="field-source">
            <div class="field-source-title">可用字段</div>
            <div class="field-list">
              <div
                v-for="field in dimensionFields"
                :key="field.fieldName || field.name"
                class="field-item"
                draggable="true"
                @dragstart="handleDragStart($event, field, 'dimension')"
              >
                <i class="el-icon-s-operation"></i>
                <span>{{ field.comment || field.fieldName || field.name }}</span>
              </div>
            </div>
          </div>
          <div class="field-target">
            <div
              class="drop-zone"
              :class="{ 'is-over': isDimensionDragOver }"
              @drop="handleDrop($event, 'dimension')"
              @dragover.prevent="isDimensionDragOver = true"
              @dragleave="isDimensionDragOver = false"
            >
              <div v-if="selectedDimensions.length === 0" class="drop-placeholder">
                拖拽字段到此处
              </div>
              <div
                v-for="(dim, index) in selectedDimensions"
                :key="index"
                class="selected-field"
              >
                <span>{{ dim.comment || dim.fieldName || dim.name }}</span>
                <i class="el-icon-close" @click="removeDimension(index)"></i>
              </div>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 指标字段 -->
      <el-form-item label="指标">
        <div class="field-drag-area">
          <div class="field-source">
            <div class="field-source-title">可用字段</div>
            <div class="field-list">
              <div
                v-for="field in metricFields"
                :key="field.fieldName || field.name"
                class="field-item"
                draggable="true"
                @dragstart="handleDragStart($event, field, 'metric')"
              >
                <i class="el-icon-s-operation"></i>
                <span>{{ field.comment || field.fieldName || field.name }}</span>
              </div>
            </div>
          </div>
          <div class="field-target">
            <div
              class="drop-zone"
              :class="{ 'is-over': isMetricDragOver }"
              @drop="handleDrop($event, 'metric')"
              @dragover.prevent="isMetricDragOver = true"
              @dragleave="isMetricDragOver = false"
            >
              <div v-if="selectedMetrics.length === 0" class="drop-placeholder">
                拖拽字段到此处
              </div>
              <div
                v-for="(metric, index) in selectedMetrics"
                :key="index"
                class="selected-field"
              >
                <span>{{ metric.comment || metric.fieldName || metric.name }}</span>
                <i class="el-icon-close" @click="removeMetric(index)"></i>
              </div>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 过滤条件 -->
      <el-divider>过滤条件</el-divider>

      <el-form-item label="过滤">
        <div class="filter-list">
          <div
            v-for="(filter, index) in dataConfig.filters"
            :key="index"
            class="filter-item"
          >
            <el-select
              v-model="filter.field"
              placeholder="字段"
              size="small"
              style="width: 120px"
            >
              <el-option
                v-for="field in availableFields"
                :key="field.name"
                :label="field.comment || field.name"
                :value="field.name"
              />
            </el-select>
            <el-select
              v-model="filter.operator"
              placeholder="操作符"
              size="small"
              style="width: 100px"
            >
              <el-option label="等于" value="=" />
              <el-option label="不等于" value="!=" />
              <el-option label="大于" value=">" />
              <el-option label="小于" value="<" />
              <el-option label="包含" value="like" />
            </el-select>
            <el-input
              v-model="filter.value"
              placeholder="值"
              size="small"
              style="flex: 1"
            />
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="removeFilter(index)"
            />
          </div>
          <el-button
            type="text"
            icon="el-icon-plus"
            size="small"
            @click="addFilter"
          >
            添加条件
          </el-button>
        </div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleRefresh">
          刷新数据
        </el-button>
        <el-button @click="handlePreview">
          预览数据
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 数据预览对话框 -->
    <el-dialog
      title="数据预览"
      :visible.sync="previewVisible"
      width="800px"
    >
      <el-table :data="previewData" border stripe max-height="400">
        <el-table-column
          v-for="field in previewFields"
          :key="field"
          :prop="field"
          :label="field"
          min-width="120"
        />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDataSource } from '@/api/bi/datasource'
import { listDataset, getDatasetData } from '@/api/bi/dataset'

export default {
  name: 'DataConfig',
  props: {
    component: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      datasources: [],
      datasets: [],
      availableFields: [],
      dataConfig: {
        datasourceId: null,
        datasetId: null,
        dimensions: [],
        measures: [],
        metrics: [],
        filters: []
      },
      previewVisible: false,
      previewData: [],
      previewFields: [],
      // 拖拽相关
      isDimensionDragOver: false,
      isMetricDragOver: false,
      selectedDimensions: [],
      selectedMetrics: [],
      draggedField: null,
      dragType: null
    }
  },
  computed: {
    // 判断当前图表类型是否需要维度字段
    requiresDimension() {
      const chartType = this.component?.styleConfig?.chartType
      // 不需要维度的图表类型：仪表盘、水球图、指标卡等
      const singleValueCharts = ['gauge', 'liquidfill', 'card', 'metricCard']
      return !singleValueCharts.includes(chartType)
    },
    // 维度字段（通常是字符型或日期型）
    dimensionFields() {
      return this.availableFields.filter(field => {
        const type = (field.fieldType || field.type || '').toLowerCase()
        return ['varchar', 'char', 'text', 'date', 'datetime', 'timestamp'].includes(type)
      })
    },
    // 指标字段（通常是数值型）
    metricFields() {
      return this.availableFields.filter(field => {
        const type = (field.fieldType || field.type || '').toLowerCase()
        return ['int', 'bigint', 'decimal', 'double', 'float', 'numeric'].includes(type)
      })
    }
  },
  mounted() {
    this.loadDatasources()
    this.initDataConfig()
  },
  methods: {
    // 初始化数据配置
    initDataConfig() {
      if (this.component.dataConfig) {
        this.dataConfig = {
          datasourceId: this.component.dataConfig.datasourceId || null,
          datasetId: this.component.dataConfig.datasetId || null,
          dimensions: this.component.dataConfig.dimensions || [],
          measures: this.component.dataConfig.measures || [],
          metrics: this.component.dataConfig.metrics || [],
          filters: this.component.dataConfig.filters || []
        }

        // 初始化已选字段
        this.selectedDimensions = (this.component.dataConfig.dimensions || []).map(d => {
          if (typeof d === 'string') {
            return { fieldName: d, name: d }
          }
          return d
        })
        this.selectedMetrics = (this.component.dataConfig.metrics || this.component.dataConfig.measures || []).map(m => {
          if (typeof m === 'string') {
            return { fieldName: m, name: m }
          }
          return m
        })

        // 如果有数据集，加载数据集信息
        if (this.dataConfig.datasetId) {
          this.loadDatasets(this.dataConfig.datasourceId)
          this.loadDatasetFields(this.dataConfig.datasetId)
        }
      }
    },

    // 加载数据源列表
    async loadDatasources() {
      try {
        const response = await listDataSource({})
        this.datasources = response.rows || []
      } catch (error) {
        console.error('加载数据源失败:', error)
      }
    },

    // 加载数据集列表
    async loadDatasets(datasourceId) {
      if (!datasourceId) {
        this.datasets = []
        return
      }
      try {
        const response = await listDataset({ datasourceId })
        this.datasets = response.rows || []
      } catch (error) {
        console.error('加载数据集失败:', error)
      }
    },

    // 加载数据集字段
    async loadDatasetFields(datasetId) {
      if (!datasetId) {
        this.availableFields = []
        return
      }
      try {
        const response = await getDatasetData(datasetId, { pageSize: 1 })
        if (response.data && response.data.fields) {
          this.availableFields = response.data.fields
        }
      } catch (error) {
        console.error('加载字段失败:', error)
      }
    },

    // 数据源变化
    handleDatasourceChange(datasourceId) {
      this.dataConfig.datasetId = null
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.filters = []
      this.availableFields = []
      this.loadDatasets(datasourceId)
      this.emitChange()
    },

    // 数据集变化
    handleDatasetChange(datasetId) {
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.metrics = []
      this.dataConfig.filters = []
      this.selectedDimensions = []
      this.selectedMetrics = []
      this.loadDatasetFields(datasetId)
      this.emitChange()
    },

    // 拖拽开始
    handleDragStart(event, field, type) {
      this.draggedField = field
      this.dragType = type
      event.dataTransfer.effectAllowed = 'copy'
      event.dataTransfer.setData('text/plain', JSON.stringify({ field, type }))
    },

    // 放置字段
    handleDrop(event, targetType) {
      event.preventDefault()
      this.isDimensionDragOver = false
      this.isMetricDragOver = false

      if (!this.draggedField || this.dragType !== targetType) {
        return
      }

      if (targetType === 'dimension') {
        // 检查是否已存在
        const exists = this.selectedDimensions.some(d => 
          (d.fieldName || d.name) === (this.draggedField.fieldName || this.draggedField.name)
        )
        if (!exists) {
          this.selectedDimensions.push({ ...this.draggedField })
          this.syncDimensionsToConfig()
        }
      } else if (targetType === 'metric') {
        const exists = this.selectedMetrics.some(m => 
          (m.fieldName || m.name) === (this.draggedField.fieldName || this.draggedField.name)
        )
        if (!exists) {
          this.selectedMetrics.push({ ...this.draggedField })
          this.syncMetricsToConfig()
        }
      }

      this.draggedField = null
      this.dragType = null
    },

    // 移除维度
    removeDimension(index) {
      this.selectedDimensions.splice(index, 1)
      this.syncDimensionsToConfig()
    },

    // 移除指标
    removeMetric(index) {
      this.selectedMetrics.splice(index, 1)
      this.syncMetricsToConfig()
    },

    // 同步维度到配置
    syncDimensionsToConfig() {
      this.dataConfig.dimensions = this.selectedDimensions.map(d => ({
        fieldName: d.fieldName || d.name,
        comment: d.comment,
        fieldType: d.fieldType || d.type
      }))
      this.emitChange()
    },

    // 同步指标到配置
    syncMetricsToConfig() {
      this.dataConfig.metrics = this.selectedMetrics.map(m => ({
        fieldName: m.fieldName || m.name,
        comment: m.comment,
        fieldType: m.fieldType || m.type
      }))
      // 也同步到 measures 以保持向后兼容
      this.dataConfig.measures = this.dataConfig.metrics
      this.emitChange()
    },

    // 添加过滤条件
    addFilter() {
      if (!this.dataConfig.filters) {
        this.dataConfig.filters = []
      }
      this.dataConfig.filters.push({
        field: '',
        operator: '=',
        value: ''
      })
    },

    // 删除过滤条件
    removeFilter(index) {
      this.dataConfig.filters.splice(index, 1)
      this.emitChange()
    },

    // 刷新数据
    handleRefresh() {
      this.$emit('refresh-chart')
    },

    // 预览数据
    async handlePreview() {
      if (!this.dataConfig.datasetId) {
        this.$message.warning('请先选择数据集')
        return
      }

      try {
        const response = await getDatasetData(this.dataConfig.datasetId, {
          pageSize: 10,
          dimensions: this.dataConfig.dimensions,
          measures: this.dataConfig.measures,
          filters: this.dataConfig.filters
        })

        if (response.data && response.data.rows) {
          this.previewData = response.data.rows
          this.previewFields = response.data.fields.map(f => f.name)
          this.previewVisible = true
        }
      } catch (error) {
        console.error('预览数据失败:', error)
        this.$message.error('预览数据失败')
      }
    },

    // 触发配置变化事件
    emitChange() {
      this.$emit('change', {
        datasourceId: this.dataConfig.datasourceId,
        datasetId: this.dataConfig.datasetId,
        dimensions: this.dataConfig.dimensions,
        measures: this.dataConfig.measures,
        metrics: this.dataConfig.metrics,
        filters: this.dataConfig.filters
      })
    }
  },
  watch: {
    'dataConfig.dimensions'() {
      this.emitChange()
    },
    'dataConfig.measures'() {
      this.emitChange()
    }
  }
}
</script>

<style scoped>
.data-config {
  padding: 16px;
  width: 400px;
  box-sizing: border-box;
}

.el-form-item {
  margin-bottom: 16px;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.filter-list {
  width: 100%;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.filter-item > * {
  flex-shrink: 0;
}

.filter-item .el-input {
  flex: 1;
  min-width: 0;
}

.el-divider {
  margin: 16px 0;
}

/* 字段拖拽区域 */
.field-drag-area {
  display: flex;
  gap: 12px;
  min-height: 150px;
}

.field-source {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  background: #f5f7fa;
}

.field-source-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.field-list {
  max-height: 200px;
  overflow-y: auto;
}

.field-item {
  padding: 6px 8px;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  cursor: move;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  transition: all 0.2s;

  &:hover {
    background: #ecf5ff;
    border-color: #409eff;
    color: #409eff;
  }

  i {
    font-size: 12px;
    color: #909399;
  }
}

.field-target {
  flex: 1;
}

.drop-zone {
  min-height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  background: #fff;
  transition: all 0.3s;

  &.is-over {
    border-color: #409eff;
    background: #ecf5ff;
  }
}

.drop-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 130px;
  color: #c0c4cc;
  font-size: 13px;
}

.selected-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  margin-bottom: 6px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 3px;
  font-size: 13px;
  color: #409eff;

  i {
    cursor: pointer;
    font-size: 14px;
    opacity: 0.6;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
      color: #f56c6c;
    }
  }
}
</style>
