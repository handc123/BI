<template>
  <div class="data-config">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 数据源配置选项卡 -->
      <el-tab-pane label="数据源配置" name="datasource">
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

          <!-- 字段配置区域 - 左右布局 -->
          <el-divider>字段配置</el-divider>
          <div class="field-config-layout">
            <!-- 左侧：字段配置区 -->
            <div class="field-config-area">
              <el-alert
                title="提示：从右侧字段管理区域拖拽字段到下方配置区"
                type="info"
                :closable="false"
                style="margin-bottom: 15px"
              />

              <!-- 维度字段 - 只在需要维度的图表类型下显示 -->
              <el-form-item v-if="requiresDimension" label="维度">
                <div
                  class="drop-zone"
                  :class="{ 'is-over': isDimensionDragOver }"
                  @drop="handleDrop($event, 'dimension')"
                  @dragover.prevent="isDimensionDragOver = true"
                  @dragleave="isDimensionDragOver = false"
                >
                  <div v-if="selectedDimensions.length === 0" class="drop-placeholder">
                    <i class="el-icon-download" style="font-size: 24px; color: #c0c4cc;"></i>
                    <p>从右侧字段管理区域拖拽维度字段到此处</p>
                  </div>
                  <div
                    v-for="(dim, index) in selectedDimensions"
                    :key="index"
                    class="selected-field"
                  >
                    <i class="el-icon-s-operation" style="margin-right: 4px; color: #909399;"></i>
                    <i v-if="dim.isCalculated" class="el-icon-s-operation" style="margin-right: 4px; color: #409eff;"></i>
                    <span>{{ dim.isCalculated ? dim.alias : (dim.comment || dim.fieldName || dim.name) }}</span>
                    <i class="el-icon-close" @click="removeDimension(index)"></i>
                  </div>
                </div>
              </el-form-item>

              <!-- 指标字段 -->
              <el-form-item label="指标">
                <div
                  class="drop-zone"
                  :class="{ 'is-over': isMetricDragOver }"
                  @drop="handleDrop($event, 'metric')"
                  @dragover.prevent="isMetricDragOver = true"
                  @dragleave="isMetricDragOver = false"
                >
                  <div v-if="selectedMetrics.length === 0" class="drop-placeholder">
                    <i class="el-icon-download" style="font-size: 24px; color: #c0c4cc;"></i>
                    <p>从右侧字段管理区域拖拽指标字段到此处</p>
                  </div>
                  <div
                    v-for="(metric, index) in selectedMetrics"
                    :key="index"
                    class="selected-field"
                  >
                    <i class="el-icon-s-operation" style="margin-right: 4px; color: #909399;"></i>
                    <i v-if="metric.isCalculated" class="el-icon-s-operation" style="margin-right: 4px; color: #409eff;"></i>
                    <span>{{ metric.isCalculated ? metric.alias : (metric.comment || metric.fieldName || metric.name) }}</span>
                    <i class="el-icon-close" @click="removeMetric(index)"></i>
                  </div>
                </div>
              </el-form-item>
            </div>

            <!-- 右侧：字段管理面板 -->
            <div class="field-management-area" v-if="dataConfig.datasetId">
              <field-management-panel
                :dataset-id="dataConfig.datasetId"
                :dataset-fields="availableFields"
                :calculated-fields="calculatedFields"
                :chart-type="component.styleConfig && component.styleConfig.chartType"
                @add-calculated-field="handleAddCalculatedField"
                @edit-calculated-field="handleEditCalculatedField"
                @delete-calculated-field="handleDeleteCalculatedField"
                @field-drag-start="handleFieldDragStart"
              />
            </div>
          </div>

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
      </el-tab-pane>

      <!-- 基础指标选项卡 -->
      <el-tab-pane label="基础指标" name="baseMetrics">
        <div class="metrics-panel">
          <div class="metrics-header">
            <el-button
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="handleAddMetric('base')"
              :disabled="!dataConfig.datasetId"
            >
              添加基础指标
            </el-button>
            <el-alert
              v-if="baseMetrics.length > 20"
              title="警告：指标数量超过20个可能影响性能"
              type="warning"
              :closable="false"
              style="margin-left: 10px; flex: 1"
            />
          </div>

          <div class="metrics-list">
            <el-empty v-if="baseMetrics.length === 0" description="暂无基础指标，点击上方按钮添加" />
            <draggable
              v-else
              v-model="baseMetrics"
              handle=".drag-handle"
              @end="handleMetricReorder"
            >
              <div
                v-for="(metric, index) in baseMetrics"
                :key="metric.name"
                class="metric-item"
              >
                <div class="metric-drag">
                  <i class="el-icon-s-operation drag-handle"></i>
                </div>
                <div class="metric-info">
                  <div class="metric-name">
                    <el-tag size="mini" type="success">基础</el-tag>
                    <span class="name">{{ metric.alias }}</span>
                    <span class="identifier">({{ metric.name }})</span>
                  </div>
                  <div class="metric-sql">
                    <el-tooltip :content="getMetricSqlPreview(metric)" placement="top">
                      <span class="sql-preview">{{ getMetricSqlPreview(metric) }}</span>
                    </el-tooltip>
                  </div>
                </div>
                <div class="metric-actions">
                  <el-button
                    type="text"
                    size="small"
                    icon="el-icon-edit"
                    @click="handleEditMetric(metric, index, 'base')"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    icon="el-icon-delete"
                    @click="handleDeleteMetric(index, 'base')"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </draggable>
          </div>
        </div>
      </el-tab-pane>

      <!-- 计算指标选项卡 -->
      <el-tab-pane label="计算指标" name="computedMetrics">
        <div class="metrics-panel">
          <div class="metrics-header">
            <el-button
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="handleAddMetric('computed')"
              :disabled="!dataConfig.datasetId"
            >
              添加计算指标
            </el-button>
            <el-alert
              v-if="totalMetricsCount > 20"
              title="警告：指标数量超过20个可能影响性能"
              type="warning"
              :closable="false"
              style="margin-left: 10px; flex: 1"
            />
          </div>

          <div class="metrics-list">
            <el-empty v-if="computedMetrics.length === 0" description="暂无计算指标，点击上方按钮添加" />
            <draggable
              v-else
              v-model="computedMetrics"
              handle=".drag-handle"
              @end="handleMetricReorder"
            >
              <div
                v-for="(metric, index) in computedMetrics"
                :key="metric.name"
                class="metric-item"
              >
                <div class="metric-drag">
                  <i class="el-icon-s-operation drag-handle"></i>
                </div>
                <div class="metric-info">
                  <div class="metric-name">
                    <el-tag size="mini" type="warning">{{ getComputeTypeLabel(metric.computeType) }}</el-tag>
                    <span class="name">{{ metric.alias }}</span>
                    <span class="identifier">({{ metric.name }})</span>
                  </div>
                  <div class="metric-sql">
                    <el-tooltip :content="getMetricSqlPreview(metric)" placement="top">
                      <span class="sql-preview">{{ getMetricSqlPreview(metric) }}</span>
                    </el-tooltip>
                  </div>
                </div>
                <div class="metric-actions">
                  <el-button
                    type="text"
                    size="small"
                    icon="el-icon-edit"
                    @click="handleEditMetric(metric, index, 'computed')"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    icon="el-icon-delete"
                    @click="handleDeleteMetric(index, 'computed')"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </draggable>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

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

    <!-- 指标配置对话框 -->
    <metric-config-dialog
      :visible.sync="metricDialogVisible"
      :dataset-id="dataConfig.datasetId"
      :metric="currentEditingMetric"
      :available-fields="availableFields"
      :available-metrics="allMetrics"
      @submit="handleMetricSubmit"
    />

    <!-- 计算字段配置对话框 -->
    <calculated-field-dialog
      :visible.sync="calculatedFieldDialogVisible"
      :field="currentEditingCalculatedField"
      :dataset-fields="availableFields"
      :existing-fields="calculatedFields"
      @save="handleCalculatedFieldSubmit"
    />
  </div>
</template>

<script>
import { listDataSource } from '@/api/bi/datasource'
import { listDataset, getDatasetData, getDatasetFields } from '@/api/bi/dataset'
import MetricConfigDialog from '@/components/MetricConfigDialog'
import FieldManagementPanel from '@/components/FieldManagementPanel'
import CalculatedFieldDialog from '@/components/CalculatedFieldDialog'
import draggable from 'vuedraggable'

export default {
  name: 'DataConfig',
  components: {
    MetricConfigDialog,
    FieldManagementPanel,
    CalculatedFieldDialog,
    draggable
  },
  props: {
    component: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      activeTab: 'datasource',
      datasources: [],
      datasets: [],
      availableFields: [],
      dataConfig: {
        datasourceId: null,
        datasetId: null,
        dimensions: [],
        measures: [],
        metrics: [],
        filters: [],
        calculatedFields: [] // 计算字段配置
      },
      // 指标配置
      baseMetrics: [],
      computedMetrics: [],
      metricDialogVisible: false,
      currentEditingMetric: null,
      currentEditingIndex: -1,
      currentEditingType: null,
      // 计算字段配置
      calculatedFields: [],
      calculatedFieldDialogVisible: false,
      currentEditingCalculatedField: null,
      // 预览
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
        // 优先使用后端分类的fieldType
        if (field.fieldType === 'dimension') {
          return true
        }
        // 兼容处理：检查dataType（原始数据库类型）
        const dataType = (field.dataType || field.type || '').toLowerCase()
        return ['varchar', 'char', 'text', 'date', 'datetime', 'timestamp', 'string'].some(t => dataType.includes(t))
      })
    },
    // 指标字段（通常是数值型）
    metricFields() {
      console.log('[DataConfig] 计算metricFields, availableFields:', this.availableFields)
      return this.availableFields.filter(field => {
        // 优先使用后端分类的fieldType
        if (field.fieldType === 'metric') {
          console.log('[DataConfig] 字段被分类为metric:', field)
          return true
        }
        // 兼容处理：检查dataType（原始数据库类型）
        const dataType = (field.dataType || field.type || '').toLowerCase()
        const isMetric = ['int', 'bigint', 'decimal', 'double', 'float', 'numeric', 'smallint', 'tinyint', 'real'].some(t => dataType.includes(t))
        if (isMetric) {
          console.log('[DataConfig] 字段根据dataType被识别为metric:', field)
        }
        return isMetric
      })
    },
    // 所有指标（基础+计算）
    allMetrics() {
      return [...this.baseMetrics, ...this.computedMetrics]
    },
    // 总指标数量
    totalMetricsCount() {
      return this.baseMetrics.length + this.computedMetrics.length
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
          filters: this.component.dataConfig.filters || [],
          calculatedFields: this.component.dataConfig.calculatedFields || []
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

        // 初始化指标配置
        if (this.component.dataConfig.metricConfig) {
          this.baseMetrics = this.component.dataConfig.metricConfig.baseMetrics || []
          this.computedMetrics = this.component.dataConfig.metricConfig.computedMetrics || []
        }

        // 初始化计算字段
        this.calculatedFields = this.component.dataConfig.calculatedFields || []

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
        console.log('[DataConfig] 开始加载数据集字段, datasetId:', datasetId)
        const response = await getDatasetFields(datasetId)
        console.log('[DataConfig] 字段接口响应:', response)
        
        if (response.code === 200 && response.data) {
          // 将字段数据映射为统一格式，确保包含所有必要字段
          this.availableFields = response.data.map(field => ({
            name: field.name || field.fieldName,
            fieldName: field.fieldName || field.name,
            comment: field.comment || field.fieldComment || field.fieldName || field.name,
            fieldType: field.fieldType, // 后端分类: 'metric' 或 'dimension'
            dataType: field.dataType || field.type, // 原始数据库类型
            type: field.type || field.fieldType
          }))
          
          console.log('[DataConfig] 加载字段成功, 共', this.availableFields.length, '个字段')
          console.log('[DataConfig] 字段详情:', this.availableFields)
          
          // 立即计算并输出指标字段
          const metrics = this.availableFields.filter(field => {
            if (field.fieldType === 'metric') return true
            const dataType = (field.dataType || '').toLowerCase()
            return ['int', 'bigint', 'decimal', 'double', 'float', 'numeric', 'smallint', 'tinyint', 'real'].some(t => dataType.includes(t))
          })
          console.log('[DataConfig] 指标字段数量:', metrics.length, '个')
          console.log('[DataConfig] 指标字段列表:', metrics)
        } else {
          console.warn('[DataConfig] 字段接口返回异常:', response)
          this.availableFields = []
        }
      } catch (error) {
        console.error('[DataConfig] 加载字段失败:', error)
        this.$message.error('加载字段失败: ' + (error.message || '未知错误'))
        this.availableFields = []
      }
    },

    // 数据源变化
    handleDatasourceChange(datasourceId) {
      this.dataConfig.datasetId = null
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.filters = []
      this.availableFields = []
      this.baseMetrics = []
      this.computedMetrics = []
      this.calculatedFields = []
      this.loadDatasets(datasourceId)
      this.emitChange()
    },

    // 数据集变化
    handleDatasetChange(datasetId) {
      // 检查是否有计算字段
      if (this.calculatedFields.length > 0) {
        this.$confirm(
          '当前图表包含计算字段。切换数据集将清空所有计算字段。是否继续？',
          '警告',
          {
            confirmButtonText: '继续',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          this.performDatasetChange(datasetId)
        }).catch(() => {
          // 恢复原数据集选择
          this.$nextTick(() => {
            this.dataConfig.datasetId = this.component.dataConfig?.datasetId || null
          })
        })
      } else {
        this.performDatasetChange(datasetId)
      }
    },

    // 执行数据集切换
    performDatasetChange(datasetId) {
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.metrics = []
      this.dataConfig.filters = []
      this.selectedDimensions = []
      this.selectedMetrics = []
      this.baseMetrics = []
      this.computedMetrics = []
      this.calculatedFields = []
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

      console.log('[DataConfig] handleDrop - targetType:', targetType, 'draggedField:', this.draggedField)

      if (!this.draggedField) {
        console.log('[DataConfig] handleDrop - 没有拖拽的字段')
        return
      }

      // 检查字段类型是否匹配目标区域
      // 计算字段根据其fieldType决定可以放置的位置
      const fieldType = this.draggedField.fieldType
      const isCalculatedField = this.draggedField.isCalculated || this.calculatedFields.some(f => f.name === this.draggedField.name)

      console.log('[DataConfig] handleDrop - fieldType:', fieldType, 'isCalculated:', isCalculatedField, 'dragType:', this.dragType)

      if (targetType === 'dimension') {
        // 维度区只接受dimension类型的字段
        if (fieldType !== 'dimension' && this.dragType !== 'dimension') {
          this.$message.warning('该字段不是维度类型，无法添加到维度区')
          console.log('[DataConfig] handleDrop - 字段类型不匹配维度区')
          this.draggedField = null
          this.dragType = null
          return
        }

        // 检查是否已存在
        const exists = this.selectedDimensions.some(d => 
          (d.fieldName || d.name) === (this.draggedField.fieldName || this.draggedField.name)
        )
        if (!exists) {
          // 添加字段，确保包含必要信息
          const fieldToAdd = {
            ...this.draggedField,
            name: this.draggedField.name || this.draggedField.fieldName,
            fieldName: this.draggedField.fieldName || this.draggedField.name,
            comment: this.draggedField.comment || this.draggedField.alias,
            alias: this.draggedField.alias,
            isCalculated: isCalculatedField
          }
          console.log('[DataConfig] handleDrop - 添加维度字段:', fieldToAdd)
          this.selectedDimensions.push(fieldToAdd)
          this.syncDimensionsToConfig()
        } else {
          console.log('[DataConfig] handleDrop - 维度字段已存在')
        }
      } else if (targetType === 'metric') {
        // 指标区只接受metric类型的字段
        if (fieldType !== 'metric' && this.dragType !== 'metric') {
          this.$message.warning('该字段不是指标类型，无法添加到指标区')
          console.log('[DataConfig] handleDrop - 字段类型不匹配指标区')
          this.draggedField = null
          this.dragType = null
          return
        }

        const exists = this.selectedMetrics.some(m => 
          (m.fieldName || m.name) === (this.draggedField.fieldName || this.draggedField.name)
        )
        if (!exists) {
          const fieldToAdd = {
            ...this.draggedField,
            name: this.draggedField.name || this.draggedField.fieldName,
            fieldName: this.draggedField.fieldName || this.draggedField.name,
            comment: this.draggedField.comment || this.draggedField.alias,
            alias: this.draggedField.alias,
            isCalculated: isCalculatedField
          }
          console.log('[DataConfig] handleDrop - 添加指标字段:', fieldToAdd)
          this.selectedMetrics.push(fieldToAdd)
          this.syncMetricsToConfig()
        } else {
          console.log('[DataConfig] handleDrop - 指标字段已存在')
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
        field: d.fieldName || d.name,  // 修复: 使用 field 而不是 fieldName
        fieldName: d.fieldName || d.name,  // 保留 fieldName 以兼容
        comment: d.comment,
        fieldType: d.fieldType || d.type,
        isCalculated: d.isCalculated || false,
        // 如果是计算字段，添加额外信息
        ...(d.isCalculated ? {
          expression: d.expression,
          aggregation: d.aggregation,
          alias: d.alias
        } : {})
      }))
      console.log('[DataConfig] syncDimensionsToConfig - 同步后的dimensions:', this.dataConfig.dimensions)
      this.emitChange()
    },

    // 同步指标到配置
    syncMetricsToConfig() {
      this.dataConfig.metrics = this.selectedMetrics.map(m => ({
        field: m.fieldName || m.name,  // 修复: 使用 field 而不是 fieldName
        fieldName: m.fieldName || m.name,  // 保留 fieldName 以兼容
        comment: m.comment,
        fieldType: m.fieldType || m.type,
        isCalculated: m.isCalculated || false,
        // 如果是计算字段，添加额外信息
        ...(m.isCalculated ? {
          expression: m.expression,
          aggregation: m.aggregation,
          alias: m.alias
        } : {})
      }))
      console.log('[DataConfig] syncMetricsToConfig - 同步后的metrics:', this.dataConfig.metrics)
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

    // ========== 指标配置相关方法 ==========

    // 添加指标
    handleAddMetric(type) {
      if (!this.dataConfig.datasetId) {
        this.$message.warning('请先选择数据集')
        return
      }
      this.currentEditingMetric = null
      this.currentEditingIndex = -1
      this.currentEditingType = type
      this.metricDialogVisible = true
    },

    // 编辑指标
    handleEditMetric(metric, index, type) {
      this.currentEditingMetric = { ...metric }
      this.currentEditingIndex = index
      this.currentEditingType = type
      this.metricDialogVisible = true
    },

    // 删除指标
    handleDeleteMetric(index, type) {
      const metricList = type === 'base' ? this.baseMetrics : this.computedMetrics
      const metric = metricList[index]

      // 检查依赖
      const dependencies = this.checkMetricDependencies(metric.name)
      if (dependencies.length > 0) {
        const depNames = dependencies.map(d => d.alias).join('、')
        this.$confirm(
          `指标 "${metric.alias}" 被以下计算指标引用：${depNames}。删除后这些指标可能无法正常工作，是否继续？`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          this.performDeleteMetric(index, type)
        }).catch(() => {
          // 取消删除
        })
      } else {
        this.$confirm(`确定要删除指标 "${metric.alias}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.performDeleteMetric(index, type)
        }).catch(() => {
          // 取消删除
        })
      }
    },

    // 执行删除指标
    performDeleteMetric(index, type) {
      if (type === 'base') {
        this.baseMetrics.splice(index, 1)
      } else {
        this.computedMetrics.splice(index, 1)
      }
      this.$message.success('删除成功')
      this.syncMetricConfig()
    },

    // 检查指标依赖
    checkMetricDependencies(metricName) {
      const dependencies = []
      
      // 检查计算指标中的引用
      this.computedMetrics.forEach(metric => {
        if (metric.computeType === 'simple_ratio') {
          if (metric.numeratorMetric === metricName || metric.denominatorMetric === metricName) {
            dependencies.push(metric)
          }
        } else if (metric.computeType === 'custom_expression') {
          // 简单检查表达式中是否包含指标名称
          if (metric.expression && metric.expression.includes(metricName)) {
            dependencies.push(metric)
          }
        }
      })

      return dependencies
    },

    // 指标提交
    handleMetricSubmit(metricConfig) {
      if (this.currentEditingIndex >= 0) {
        // 编辑模式
        if (this.currentEditingType === 'base') {
          this.$set(this.baseMetrics, this.currentEditingIndex, metricConfig)
        } else {
          this.$set(this.computedMetrics, this.currentEditingIndex, metricConfig)
        }
        this.$message.success('指标更新成功')
      } else {
        // 新增模式
        // 检查指标名称是否重复
        const exists = this.allMetrics.some(m => m.name === metricConfig.name)
        if (exists) {
          this.$message.error('指标名称已存在，请使用其他名称')
          return
        }

        if (this.currentEditingType === 'base') {
          this.baseMetrics.push(metricConfig)
        } else {
          this.computedMetrics.push(metricConfig)
        }
        this.$message.success('指标添加成功')
      }

      this.syncMetricConfig()
      this.metricDialogVisible = false
    },

    // 指标重新排序
    handleMetricReorder() {
      this.syncMetricConfig()
    },

    // 同步指标配置
    syncMetricConfig() {
      this.emitChange()
    },

    // 获取指标SQL预览
    getMetricSqlPreview(metric) {
      if (!metric) return ''

      if (metric.field && metric.aggregation) {
        // 基础指标
        return `${metric.aggregation}(${metric.field}) AS ${metric.name}`
      } else if (metric.computeType) {
        // 计算指标
        switch (metric.computeType) {
          case 'conditional_ratio':
            return `SUM(CASE WHEN ${metric.numeratorCondition || '...'} THEN ${metric.field} ELSE 0 END) / NULLIF(SUM(CASE WHEN ${metric.denominatorCondition || '...'} THEN ${metric.field} ELSE 0 END), 0)`
          case 'simple_ratio':
            return `${metric.numeratorMetric || '...'} / NULLIF(${metric.denominatorMetric || '...'}, 0)`
          case 'conditional_sum':
            return `SUM(CASE WHEN ${metric.condition || '...'} THEN ${metric.field} ELSE 0 END)`
          case 'custom_expression':
            return metric.expression || '...'
          default:
            return '未知类型'
        }
      }

      return ''
    },

    // 获取计算类型标签
    getComputeTypeLabel(computeType) {
      const labels = {
        'conditional_ratio': '条件比率',
        'simple_ratio': '简单比率',
        'conditional_sum': '条件求和',
        'custom_expression': '自定义表达式'
      }
      return labels[computeType] || '计算'
    },

    // ========== 计算字段管理方法 ==========

    // 添加计算字段
    handleAddCalculatedField() {
      this.currentEditingCalculatedField = null
      this.calculatedFieldDialogVisible = true
    },

    // 编辑计算字段
    handleEditCalculatedField(field) {
      this.currentEditingCalculatedField = { ...field }
      this.calculatedFieldDialogVisible = true
    },

    // 删除计算字段
    handleDeleteCalculatedField(field) {
      // 检查字段是否在使用中
      const isUsedInDimensions = this.selectedDimensions.some(d => d.name === field.name)
      const isUsedInMetrics = this.selectedMetrics.some(m => m.name === field.name)

      if (isUsedInDimensions || isUsedInMetrics) {
        this.$confirm(
          `计算字段 "${field.alias}" 正在图表配置中使用。删除后图表可能无法正常显示，是否继续？`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          this.performDeleteCalculatedField(field)
        }).catch(() => {
          // 取消删除
        })
      } else {
        this.$confirm(`确定要删除计算字段 "${field.alias}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.performDeleteCalculatedField(field)
        }).catch(() => {
          // 取消删除
        })
      }
    },

    // 执行删除计算字段
    performDeleteCalculatedField(field) {
      const index = this.calculatedFields.findIndex(f => f.name === field.name)
      if (index >= 0) {
        this.calculatedFields.splice(index, 1)
        
        // 从已选字段中移除
        this.selectedDimensions = this.selectedDimensions.filter(d => d.name !== field.name)
        this.selectedMetrics = this.selectedMetrics.filter(m => m.name !== field.name)
        
        this.syncDimensionsToConfig()
        this.syncMetricsToConfig()
        this.$message.success('删除成功')
      }
    },

    // 计算字段提交
    handleCalculatedFieldSubmit(fieldConfig) {
      console.log('[DataConfig] 计算字段提交:', fieldConfig)
      console.log('[DataConfig] 当前计算字段列表:', this.calculatedFields)
      
      const existingIndex = this.calculatedFields.findIndex(f => f.name === fieldConfig.name)
      
      if (existingIndex >= 0 && (!this.currentEditingCalculatedField || this.currentEditingCalculatedField.name !== fieldConfig.name)) {
        this.$message.error('字段名称已存在，请使用其他名称')
        return
      }

      if (this.currentEditingCalculatedField) {
        // 编辑模式
        const index = this.calculatedFields.findIndex(f => f.name === this.currentEditingCalculatedField.name)
        if (index >= 0) {
          this.$set(this.calculatedFields, index, fieldConfig)
          console.log('[DataConfig] 计算字段更新成功，新列表:', this.calculatedFields)
          this.$message.success('计算字段更新成功')
        }
      } else {
        // 新增模式
        this.calculatedFields.push(fieldConfig)
        console.log('[DataConfig] 计算字段添加成功，新列表:', this.calculatedFields)
        this.$message.success('计算字段添加成功')
      }

      this.calculatedFieldDialogVisible = false
      this.emitChange()
      
      // 强制更新视图
      this.$nextTick(() => {
        console.log('[DataConfig] nextTick后的计算字段列表:', this.calculatedFields)
        this.$forceUpdate()
      })
    },

    // 字段拖拽开始（来自FieldManagementPanel）
    handleFieldDragStart(dragData) {
      console.log('[DataConfig] handleFieldDragStart 接收到:', dragData)
      
      // dragData 结构: { field, type, source }
      const field = dragData.field
      const type = dragData.type
      
      // 对于计算字段，使用其 fieldType 属性
      if (type === 'calculated') {
        this.draggedField = {
          ...field,
          fieldType: field.fieldType, // 'dimension' 或 'metric'
          name: field.name,
          alias: field.alias,
          comment: field.alias,
          isCalculated: true
        }
        this.dragType = field.fieldType // 使用计算字段的实际类型
        console.log('[DataConfig] 计算字段拖拽，类型:', field.fieldType, '字段:', this.draggedField)
      } else {
        // 普通字段
        this.draggedField = {
          ...field,
          fieldType: type
        }
        this.dragType = type
        console.log('[DataConfig] 普通字段拖拽，类型:', type, '字段:', this.draggedField)
      }
    },

    // 触发配置变化事件
    emitChange() {
      const config = {
        datasourceId: this.dataConfig.datasourceId,
        datasetId: this.dataConfig.datasetId,
        dimensions: this.dataConfig.dimensions,
        measures: this.dataConfig.measures,
        metrics: this.dataConfig.metrics,
        filters: this.dataConfig.filters,
        calculatedFields: this.calculatedFields,
        metricConfig: {
          baseMetrics: this.baseMetrics,
          computedMetrics: this.computedMetrics
        }
      }
      this.$emit('change', config)
      this.$emit('config-change', config)
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
  width: 100%;
  box-sizing: border-box;
}

.el-tabs {
  width: 100%;
}

.el-form-item {
  margin-bottom: 16px;
}

/* 字段配置布局 */
.field-config-layout {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.field-config-area {
  flex: 1;
  min-width: 0;
}

.field-management-area {
  width: 280px;
  flex-shrink: 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
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

/* 拖放区域样式 */
.drop-zone {
  min-height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  background: #fff;
  transition: all 0.3s;
}

.drop-zone.is-over {
  border-color: #409eff;
  background: #ecf5ff;
}

.drop-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 130px;
  color: #c0c4cc;
  font-size: 13px;
  gap: 8px;
}

.drop-placeholder p {
  margin: 0;
  text-align: center;
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
}

.selected-field i {
  cursor: pointer;
  font-size: 14px;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.selected-field i:hover {
  opacity: 1;
  color: #f56c6c;
}

/* 指标面板样式 */
.metrics-panel {
  padding: 16px;
}

.metrics-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 10px;
}

.metrics-list {
  max-height: 500px;
  overflow-y: auto;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 10px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  transition: all 0.2s;
}

.metric-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.metric-drag {
  cursor: move;
  color: #909399;
  font-size: 16px;
}

.drag-handle {
  cursor: move;
}

.metric-info {
  flex: 1;
  min-width: 0;
}

.metric-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.metric-name .name {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.metric-name .identifier {
  font-size: 12px;
  color: #909399;
}

.metric-sql {
  font-size: 12px;
  color: #606266;
}

.sql-preview {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Courier New', Consolas, monospace;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 2px;
}

.metric-actions {
  display: flex;
  gap: 8px;
}
</style>
