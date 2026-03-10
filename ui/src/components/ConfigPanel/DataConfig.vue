<template>
  <div class="data-config">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="数据源配置" name="datasource">
        <el-form :model="dataConfig" label-width="80px" size="small">
          <div class="dc-toolbar">
            <div class="dc-toolbar__left">
              <el-form-item label="数据源" class="dc-toolbar__item">
                <el-select v-model="dataConfig.datasourceId" placeholder="请选择数据源" @change="handleDatasourceChange">
                  <el-option
                    v-for="ds in datasources"
                    :key="ds.id"
                    :label="ds.name || ds.datasourceName || `数据源${ds.id}`"
                    :value="ds.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="数据集" class="dc-toolbar__item">
                <el-select v-model="dataConfig.datasetId" placeholder="请选择数据集" @change="handleDatasetChange">
                  <el-option
                    v-for="ds in datasets"
                    :key="ds.id"
                    :label="ds.name || ds.datasetName || `数据集${ds.id}`"
                    :value="ds.id"
                  />
                </el-select>
              </el-form-item>
            </div>

          </div>

          <div class="dc-main">
            <div class="dc-cards">
              <section class="dc-card dc-card--fields">
                <header class="dc-card__header">
                  <span class="dc-card__title">字段配置</span>
                </header>
                <div class="dc-card__body">
                  <div class="field-config-layout">
                    <div class="field-config-area">
                      <el-alert
                        title="从右侧字段管理中拖拽字段到维度或指标区域"
                        type="info"
                        :closable="false"
                        class="dc-alert"
                      />

                      <el-form-item v-if="requiresDimension" label="维度">
                        <div
                          class="drop-zone"
                          :class="{ 'is-over': isDimensionDragOver }"
                          @drop="handleDrop($event, 'dimension')"
                          @dragover.prevent="isDimensionDragOver = true"
                          @dragleave="isDimensionDragOver = false"
                        >
                          <div v-if="selectedDimensions.length === 0" class="drop-placeholder">
                            <i class="el-icon-download"></i>
                            <p>拖拽字段到此区域作为维度</p>
                          </div>
                          <div
                            v-for="(dim, index) in selectedDimensions"
                            :key="index"
                            class="selected-field"
                          >
                            <i class="el-icon-s-operation selected-field__icon"></i>
                            <i v-if="dim.isCalculated" class="el-icon-s-operation selected-field__icon selected-field__icon--fx"></i>
                            <span class="selected-field__text">{{ dim.isCalculated ? dim.alias : (dim.comment || dim.fieldName || dim.name) }}</span>
                            <i class="el-icon-close selected-field__remove" @click="removeDimension(index)"></i>
                          </div>
                        </div>
                      </el-form-item>

                      <el-form-item label="指标">
                        <div
                          class="drop-zone"
                          :class="{ 'is-over': isMetricDragOver }"
                          @drop="handleDrop($event, 'metric')"
                          @dragover.prevent="isMetricDragOver = true"
                          @dragleave="isMetricDragOver = false"
                        >
                          <div v-if="selectedMetrics.length === 0" class="drop-placeholder">
                            <i class="el-icon-download"></i>
                            <p>拖拽字段到此区域作为指标</p>
                          </div>
                          <div
                            v-for="(metric, index) in selectedMetrics"
                            :key="index"
                            class="selected-field"
                          >
                            <i class="el-icon-s-operation selected-field__icon"></i>
                            <i v-if="metric.isCalculated" class="el-icon-s-operation selected-field__icon selected-field__icon--fx"></i>
                            <span class="selected-field__text">{{ metric.isCalculated ? metric.alias : (metric.comment || metric.fieldName || metric.name) }}</span>
                            <i class="el-icon-close selected-field__remove" @click="removeMetric(index)"></i>
                          </div>
                        </div>
                      </el-form-item>
                    </div>
                  </div>
                </div>
              </section>

              <section class="dc-card dc-card--filters">
                <header class="dc-card__header">
                  <span class="dc-card__title">筛选条件</span>
                </header>
                <div class="dc-card__body">
                  <el-form-item label="筛选" class="dc-filter-form-item">
                    <div class="filter-list">
                      <div
                        v-for="(filter, index) in dataConfig.filters"
                        :key="index"
                        class="filter-item"
                      >
                        <el-select
                          v-model="filter.field"
                          class="filter-field-select"
                          placeholder="字段"
                          size="small"
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
                          class="filter-op-select"
                          placeholder="运算符"
                          size="small"
                        >
                          <el-option label="等于" value="=" />
                          <el-option label="不等于" value="!=" />
                          <el-option label="大于" value=">" />
                          <el-option label="小于" value="<" />
                          <el-option label="包含" value="like" />
                        </el-select>
                        <el-input
                          v-model="filter.value"
                          class="filter-value-input"
                          placeholder="值"
                          size="small"
                        />
                        <el-button
                          type="text"
                          icon="el-icon-delete"
                          class="filter-delete-btn"
                          @click="removeFilter(index)"
                        />
                      </div>
                      <el-button
                        type="text"
                        icon="el-icon-plus"
                        size="small"
                        @click="addFilter"
                      >
                        添加筛选
                      </el-button>
                    </div>
                  </el-form-item>
                </div>
              </section>

              <div class="dc-actions">
                <el-button @click="handleRefresh">刷新数据</el-button>
                <el-button type="primary" @click="handlePreview">预览数据</el-button>
              </div>
            </div>

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
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="数据预览" :visible.sync="previewVisible" width="800px">
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
import { addMetricMetadata, resolveMetricMetadata } from '@/api/bi/metadata'
import FieldManagementPanel from '@/components/FieldManagementPanel'
import CalculatedFieldDialog from '@/components/CalculatedFieldDialog'

export default {
  name: 'DataConfig',
  components: {
    FieldManagementPanel,
    CalculatedFieldDialog
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
        calculatedFields: [] // 璁＄畻瀛楁閰嶇疆
      },
      // 璁＄畻瀛楁閰嶇疆
      calculatedFields: [],
      calculatedFieldDialogVisible: false,
      currentEditingCalculatedField: null,
      // 棰勮
      previewVisible: false,
      previewData: [],
      previewFields: [],
      // 鎷栨嫿鐩稿叧
      isDimensionDragOver: false,
      isMetricDragOver: false,
      selectedDimensions: [],
      selectedMetrics: [],
      draggedField: null,
      dragType: null
    }
  },
  computed: {
    // 鍒ゆ柇褰撳墠鍥捐〃绫诲瀷鏄惁闇€瑕佺淮搴﹀瓧娈?
    requiresDimension() {
      const chartType = this.component?.styleConfig?.chartType
      // 涓嶉渶瑕佺淮搴︾殑鍥捐〃绫诲瀷锛氫华琛ㄧ洏銆佹按鐞冨浘銆佹寚鏍囧崱绛?
      const singleValueCharts = ['gauge', 'liquidfill', 'card', 'metricCard']
      return !singleValueCharts.includes(chartType)
    },
    // 缁村害瀛楁锛堥€氬父鏄瓧绗﹀瀷鎴栨棩鏈熷瀷锛?
    dimensionFields() {
      return this.availableFields.filter(field => {
        // 浼樺厛浣跨敤鍚庣鍒嗙被鐨刦ieldType
        if (field.fieldType === 'dimension') {
          return true
        }
        // 鍏煎澶勭悊锛氭鏌ataType锛堝師濮嬫暟鎹簱绫诲瀷锛?
        const dataType = (field.dataType || field.type || '').toLowerCase()
        return ['varchar', 'char', 'text', 'date', 'datetime', 'timestamp', 'string'].some(t => dataType.includes(t))
      })
    },
    // 鎸囨爣瀛楁锛堥€氬父鏄暟鍊煎瀷锛?
    metricFields() {
      return this.availableFields.filter(field => {
        // 浼樺厛浣跨敤鍚庣鍒嗙被鐨刦ieldType
        if (field.fieldType === 'metric') {
          return true
        }
        // 鍏煎澶勭悊锛氭鏌ataType锛堝師濮嬫暟鎹簱绫诲瀷锛?
        const dataType = (field.dataType || field.type || '').toLowerCase()
        const isMetric = ['int', 'bigint', 'decimal', 'double', 'float', 'numeric', 'smallint', 'tinyint', 'real'].some(t => dataType.includes(t))
        if (isMetric) {
        }
        return isMetric
      })
    }
  },
  mounted() {
    this.loadDatasources()
    this.initDataConfig()
  },
  methods: {
    // 鍒濆鍖栨暟鎹厤缃?
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

        // 鍒濆鍖栧凡閫夊瓧娈?
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

        // 鍒濆鍖栬绠楀瓧娈?        this.calculatedFields = this.component.dataConfig.calculatedFields || []

        // 濡傛灉鏈夋暟鎹泦锛屽姞杞芥暟鎹泦淇℃伅
        if (this.dataConfig.datasetId) {
          this.loadDatasets(this.dataConfig.datasourceId)
          this.loadDatasetFields(this.dataConfig.datasetId)
        }
      }
    },

    // 鍔犺浇鏁版嵁婧愬垪琛?
    async loadDatasources() {
      try {
        const response = await listDataSource({})
        this.datasources = response.rows || []
      } catch (error) {
      }
    },

    // 鍔犺浇鏁版嵁闆嗗垪琛?
    async loadDatasets(datasourceId) {
      if (!datasourceId) {
        this.datasets = []
        return
      }
      try {
        const response = await listDataset({ datasourceId })
        this.datasets = response.rows || []
      } catch (error) {
      }
    },

    // 鍔犺浇鏁版嵁闆嗗瓧娈?
    async loadDatasetFields(datasetId) {
      if (!datasetId) {
        this.availableFields = []
        return
      }
      try {
        const response = await getDatasetFields(datasetId)
        
        if (response.code === 200 && response.data) {
          // 灏嗗瓧娈垫暟鎹槧灏勪负缁熶竴鏍煎紡锛岀‘淇濆寘鍚墍鏈夊繀瑕佸瓧娈?
          this.availableFields = response.data.map(field => ({
            name: field.name || field.fieldName,
            fieldName: field.fieldName || field.name,
            comment: field.comment || field.fieldComment || field.fieldName || field.name,
            fieldType: field.fieldType, // 鍚庣鍒嗙被: 'metric' 鎴?'dimension'
            dataType: field.dataType || field.type, // 鍘熷鏁版嵁搴撶被鍨?
            type: field.type || field.fieldType
          }))
          
          
          // 绔嬪嵆璁＄畻骞惰緭鍑烘寚鏍囧瓧娈?
          const metrics = this.availableFields.filter(field => {
            if (field.fieldType === 'metric') return true
            const dataType = (field.dataType || '').toLowerCase()
            return ['int', 'bigint', 'decimal', 'double', 'float', 'numeric', 'smallint', 'tinyint', 'real'].some(t => dataType.includes(t))
          })
        } else {
          this.availableFields = []
        }
      } catch (error) {
        this.$message.error('鍔犺浇瀛楁澶辫触: ' + (error.message || '鏈煡閿欒'))
        this.availableFields = []
      }
    },

    // 鏁版嵁婧愬彉鍖?
    handleDatasourceChange(datasourceId) {
      this.dataConfig.datasetId = null
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.filters = []
      this.availableFields = []
      this.calculatedFields = []
      this.loadDatasets(datasourceId)
      this.emitChange()
    },

    // 鏁版嵁闆嗗彉鍖?
    handleDatasetChange(datasetId) {
      if (this.calculatedFields.length > 0) {
        this.$confirm(
          '切换数据集会清空当前已配置的计算字段和已拖拽字段，是否继续？',
          '提示',
          {
            confirmButtonText: '继续',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          this.performDatasetChange(datasetId)
        }).catch(() => {
          this.$nextTick(() => {
            this.dataConfig.datasetId = this.component.dataConfig?.datasetId || null
          })
        })
      } else {
        this.performDatasetChange(datasetId)
      }
    },

    // 鎵ц鏁版嵁闆嗗垏鎹?
    performDatasetChange(datasetId) {
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.metrics = []
      this.dataConfig.filters = []
      this.selectedDimensions = []
      this.selectedMetrics = []
      this.calculatedFields = []
      this.loadDatasetFields(datasetId)
      this.emitChange()
    },

    // 鎷栨嫿寮€濮?
    handleDragStart(event, field, type) {
      this.draggedField = field
      this.dragType = type
      event.dataTransfer.effectAllowed = 'copy'
      event.dataTransfer.setData('text/plain', JSON.stringify({ field, type }))
    },

    // 鏀剧疆瀛楁
    handleDrop(event, targetType) {
      event.preventDefault()
      this.isDimensionDragOver = false
      this.isMetricDragOver = false


      if (!this.draggedField) {
        return
      }

      // 妫€鏌ュ瓧娈电被鍨嬫槸鍚﹀尮閰嶇洰鏍囧尯鍩?
      // 璁＄畻瀛楁鏍规嵁鍏秄ieldType鍐冲畾鍙互鏀剧疆鐨勪綅缃?
      const fieldType = this.draggedField.fieldType
      const isCalculatedField = this.draggedField.isCalculated || this.calculatedFields.some(f => f.name === this.draggedField.name)


      if (targetType === 'dimension') {
        // 缁村害鍖哄彧鎺ュ彈dimension绫诲瀷鐨勫瓧娈?
        if (fieldType !== 'dimension' && this.dragType !== 'dimension') {
          this.$message.warning('璇ュ瓧娈典笉鏄淮搴︾被鍨嬶紝鏃犳硶娣诲姞鍒扮淮搴﹀尯')
          this.draggedField = null
          this.dragType = null
          return
        }

        // 妫€鏌ユ槸鍚﹀凡瀛樺湪
        const exists = this.selectedDimensions.some(d => 
          (d.fieldName || d.name) === (this.draggedField.fieldName || this.draggedField.name)
        )
        if (!exists) {
          // 娣诲姞瀛楁锛岀‘淇濆寘鍚繀瑕佷俊鎭?
          const fieldToAdd = {
            ...this.draggedField,
            name: this.draggedField.name || this.draggedField.fieldName,
            fieldName: this.draggedField.fieldName || this.draggedField.name,
            comment: this.draggedField.comment || this.draggedField.alias,
            alias: this.draggedField.alias,
            isCalculated: isCalculatedField
          }
          this.selectedDimensions.push(fieldToAdd)
          this.syncDimensionsToConfig()
        } else {
        }
      } else if (targetType === 'metric') {
        // 鎸囨爣鍖哄彧鎺ュ彈metric绫诲瀷鐨勫瓧娈?
        if (fieldType !== 'metric' && this.dragType !== 'metric') {
          this.$message.warning('璇ュ瓧娈典笉鏄寚鏍囩被鍨嬶紝鏃犳硶娣诲姞鍒版寚鏍囧尯')
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
          this.selectedMetrics.push(fieldToAdd)
          this.syncMetricsToConfig()
        } else {
        }
      }

      this.draggedField = null
      this.dragType = null
    },

    // 绉婚櫎缁村害
    removeDimension(index) {
      this.selectedDimensions.splice(index, 1)
      this.syncDimensionsToConfig()
    },

    // 绉婚櫎鎸囨爣
    removeMetric(index) {
      this.selectedMetrics.splice(index, 1)
      this.syncMetricsToConfig()
    },

    // 鍚屾缁村害鍒伴厤缃?
    syncDimensionsToConfig() {
      this.dataConfig.dimensions = this.selectedDimensions.map(d => ({
        field: d.fieldName || d.name,  // 淇: 浣跨敤 field 鑰屼笉鏄?fieldName
        fieldName: d.fieldName || d.name,  // 淇濈暀 fieldName 浠ュ吋瀹?
        comment: d.comment,
        fieldType: d.fieldType || d.type,
        isCalculated: d.isCalculated || false,
        // 濡傛灉鏄绠楀瓧娈碉紝娣诲姞棰濆淇℃伅
        ...(d.isCalculated ? {
          expression: d.expression,
          aggregation: d.aggregation,
          alias: d.alias,
          metricId: d.metricId,
          metricCode: d.metricCode,
          metricName: d.metricName
        } : {})
      }))
      this.emitChange()
    },

    // 鍚屾鎸囨爣鍒伴厤缃?
    syncMetricsToConfig() {
      this.dataConfig.metrics = this.selectedMetrics.map(m => ({
        field: m.fieldName || m.name,  // 淇: 浣跨敤 field 鑰屼笉鏄?fieldName
        fieldName: m.fieldName || m.name,  // 淇濈暀 fieldName 浠ュ吋瀹?
        comment: m.comment,
        fieldType: m.fieldType || m.type,
        isCalculated: m.isCalculated || false,
        // 濡傛灉鏄绠楀瓧娈碉紝娣诲姞棰濆淇℃伅
        ...(m.isCalculated ? {
          expression: m.expression,
          aggregation: m.aggregation,
          alias: m.alias,
          metricId: m.metricId,
          metricCode: m.metricCode,
          metricName: m.metricName
        } : {})
      }))
      // 涔熷悓姝ュ埌 measures 浠ヤ繚鎸佸悜鍚庡吋瀹?
      this.dataConfig.measures = this.dataConfig.metrics
      this.emitChange()
    },

    // 娣诲姞杩囨护鏉′欢
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

    // 鍒犻櫎杩囨护鏉′欢
    removeFilter(index) {
      this.dataConfig.filters.splice(index, 1)
      this.emitChange()
    },

    // 鍒锋柊鏁版嵁
    handleRefresh() {
      this.$emit('refresh-chart')
    },

    // 棰勮鏁版嵁
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
        this.$message.error('预览数据失败')
      }
    },

    // ========== 璁＄畻瀛楁绠＄悊鏂规硶 ==========

    // 娣诲姞璁＄畻瀛楁
    handleAddCalculatedField() {
      this.currentEditingCalculatedField = null
      this.calculatedFieldDialogVisible = true
    },

    // 缂栬緫璁＄畻瀛楁
    handleEditCalculatedField(field) {
      this.currentEditingCalculatedField = { ...field }
      this.calculatedFieldDialogVisible = true
    },

    // 鍒犻櫎璁＄畻瀛楁
    handleDeleteCalculatedField(field) {
      const isUsedInDimensions = this.selectedDimensions.some(d => d.name === field.name)
      const isUsedInMetrics = this.selectedMetrics.some(m => m.name === field.name)

      if (isUsedInDimensions || isUsedInMetrics) {
        this.$confirm(
          `计算字段“${field.alias}”已在维度或指标中使用，删除后会同步移除对应配置，是否继续？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          this.performDeleteCalculatedField(field)
        }).catch(() => {})
      } else {
        this.$confirm(`确定删除计算字段“${field.alias}”吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.performDeleteCalculatedField(field)
        }).catch(() => {})
      }
    },

    // 鎵ц鍒犻櫎璁＄畻瀛楁
    performDeleteCalculatedField(field) {
      const index = this.calculatedFields.findIndex(f => f.name === field.name)
      if (index >= 0) {
        this.calculatedFields.splice(index, 1)
        
        // 浠庡凡閫夊瓧娈典腑绉婚櫎
        this.selectedDimensions = this.selectedDimensions.filter(d => d.name !== field.name)
        this.selectedMetrics = this.selectedMetrics.filter(m => m.name !== field.name)
        
        this.syncDimensionsToConfig()
        this.syncMetricsToConfig()
        this.$message.success('鍒犻櫎鎴愬姛')
      }
    },

    // 璁＄畻瀛楁鎻愪氦
    async handleCalculatedFieldSubmit(fieldConfig) {
      
      const existingIndex = this.calculatedFields.findIndex(f => f.name === fieldConfig.name)
      
      if (existingIndex >= 0 && (!this.currentEditingCalculatedField || this.currentEditingCalculatedField.name !== fieldConfig.name)) {
        this.$message.error('字段名称已存在，请使用其他名称')
        return
      }

      let savedFieldConfig = { ...fieldConfig }
      if ((savedFieldConfig.fieldType === 'metric' || !savedFieldConfig.fieldType) && this.dataConfig.datasetId) {
        try {
          savedFieldConfig = await this.ensureCalculatedMetricMetadata(savedFieldConfig)
        } catch (e) {
          this.$message.error(e && e.message ? e.message : '计算字段指标元数据处理失败')
          return
        }
      }

      if (this.currentEditingCalculatedField) {
        // 缂栬緫妯″紡
        const index = this.calculatedFields.findIndex(f => f.name === this.currentEditingCalculatedField.name)
        if (index >= 0) {
          this.$set(this.calculatedFields, index, savedFieldConfig)
          this.$message.success('计算字段更新成功')
        }
      } else {
        // 鏂板妯″紡
        this.calculatedFields.push(savedFieldConfig)
        this.$message.success('计算字段新增成功')
      }

      this.calculatedFieldDialogVisible = false
      this.emitChange()
      
      // 寮哄埗鏇存柊瑙嗗浘
      this.$nextTick(() => {
        this.$forceUpdate()
      })
    },

    async ensureCalculatedMetricMetadata(fieldConfig) {
      const metricCode = (fieldConfig.name || '').trim()
      const metricName = (fieldConfig.alias || fieldConfig.name || '').trim()
      if (!metricCode || !metricName) {
        throw new Error('计算字段缺少指标编码或名称')
      }

      const resolvePayload = {
        metricCode,
        metricName,
        datasetId: this.dataConfig.datasetId
      }

      try {
        const resolved = await resolveMetricMetadata(resolvePayload)
        if (resolved && resolved.code === 200 && resolved.data && resolved.data.id) {
          return {
            ...fieldConfig,
            metricId: resolved.data.id,
            metricCode: resolved.data.metricCode || metricCode,
            metricName: resolved.data.metricName || metricName
          }
        }
      } catch (e) {
        // 未命中时继续走创建
      }

      const createPayload = {
        metricCode,
        metricName,
        datasetId: this.dataConfig.datasetId,
        technicalFormula: fieldConfig.expression || '',
        calculationLogic: fieldConfig.expression || '',
        status: '0'
      }
      const created = await addMetricMetadata(createPayload)
      if (created && created.code === 200 && created.data && created.data.id) {
        return {
          ...fieldConfig,
          metricId: created.data.id,
          metricCode: created.data.metricCode || metricCode,
          metricName: created.data.metricName || metricName
        }
      }
      throw new Error((created && created.msg) || '自动注册指标元数据失败')
    },

    // 瀛楁鎷栨嫿寮€濮嬶紙鏉ヨ嚜FieldManagementPanel锛?
    handleFieldDragStart(dragData) {
      
      // dragData 缁撴瀯: { field, type, source }
      const field = dragData.field
      const type = dragData.type
      
      // 瀵逛簬璁＄畻瀛楁锛屼娇鐢ㄥ叾 fieldType 灞炴€?
      if (type === 'calculated') {
        this.draggedField = {
          ...field,
          fieldType: field.fieldType, // 'dimension' 鎴?'metric'
          name: field.name,
          alias: field.alias,
          comment: field.alias,
          isCalculated: true
        }
        this.dragType = field.fieldType // 浣跨敤璁＄畻瀛楁鐨勫疄闄呯被鍨?
      } else {
        // 鏅€氬瓧娈?
        this.draggedField = {
          ...field,
          fieldType: type
        }
        this.dragType = type
      }
    },

    // 瑙﹀彂閰嶇疆鍙樺寲浜嬩欢
    emitChange() {
      const config = {
        datasourceId: this.dataConfig.datasourceId,
        datasetId: this.dataConfig.datasetId,
        dimensions: this.dataConfig.dimensions,
        measures: this.dataConfig.measures,
        metrics: this.dataConfig.metrics,
        filters: this.dataConfig.filters,
        calculatedFields: this.calculatedFields
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
  --bg-page: #f4f6f9;
  --bg-card: #ffffff;
  --bg-subtle: #f8fafc;
  --border: #dfe5ee;
  --text-main: #1f2d3d;
  --text-secondary: #5b6b7c;
  --primary: #2f6fed;
  --primary-soft: #eaf1ff;
  --dc-panel-height: calc(100vh - 260px);

  width: 100%;
  box-sizing: border-box;
  padding: 0;
  background: var(--bg-page);
}

.data-config :deep(.el-tabs--border-card) {
  border: 0;
  box-shadow: none;
  background: transparent;
}

.data-config :deep(.el-tabs--border-card > .el-tabs__content) {
  padding: 8px;
  background: transparent;
  overflow-x: hidden;
}

.data-config :deep(.el-tabs--border-card > .el-tabs__header) {
  border-bottom: 1px solid var(--border);
  background: var(--bg-card);
}

.dc-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-card);
  margin-bottom: 12px;
}

.dc-toolbar__left {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.dc-toolbar__item {
  margin-bottom: 0;
  width: 100%;
  max-width: none;
}

.dc-toolbar__item :deep(.el-select) {
  width: 100%;
}

.dc-toolbar__right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.dc-main {
  display: flex;
  gap: 8px;
  align-items: stretch;
  flex-direction: row;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  box-sizing: border-box;
}

.dc-cards {
  width: 228px;
  min-width: 228px;
  max-width: 228px;
  flex: 0 0 228px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: var(--dc-panel-height);
  box-sizing: border-box;
}

.dc-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.dc-actions .el-button {
  flex: 1;
}

.dc-card {
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-card);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.dc-card--fields {
  flex: 1 1 56%;
}

.dc-card--filters {
  flex: 1 1 44%;
}

.dc-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
  padding: 0 12px;
  background: var(--bg-subtle);
  border-bottom: 1px solid var(--border);
}

.dc-card__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
}

.dc-card__body {
  padding: 12px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.dc-alert {
  margin-bottom: 10px;
}

.field-config-layout {
  display: flex;
  gap: 0;
}

.field-config-area {
  flex: 1;
  min-width: 0;
}

.field-management-area {
  width: 148px;
  min-width: 148px;
  max-width: 148px;
  flex: 0 0 148px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-card);
  overflow: hidden;
  height: var(--dc-panel-height);
  max-height: var(--dc-panel-height);
  box-sizing: border-box;
}

.data-config :deep(.el-form-item) {
  margin-bottom: 10px;
}

.field-management-area :deep(.field-management-panel) {
  height: 100%;
}

.dc-filter-form-item {
  margin-bottom: 0;
}

.dc-filter-form-item :deep(.el-form-item__label) {
  width: 52px !important;
  padding-right: 6px;
}

.dc-filter-form-item :deep(.el-form-item__content) {
  margin-left: 52px !important;
}

.filter-list {
  width: 100%;
}

.filter-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  padding: 6px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--bg-subtle);
}

.filter-field-select {
  grid-column: 1 / 3;
  width: 100%;
}

.filter-op-select {
  grid-column: 1;
  width: 100%;
}

.filter-value-input {
  grid-column: 1 / 3;
  width: 100%;
}

.filter-delete-btn {
  grid-column: 2;
  justify-self: end;
  padding: 0;
}

.drop-zone {
  min-height: 126px;
  border: 1px dashed #c9d5e6;
  border-radius: 6px;
  padding: 8px;
  background: #fcfdff;
  transition: border-color 0.2s, background-color 0.2s;
}

.drop-zone.is-over {
  border-color: var(--primary);
  background: var(--primary-soft);
}

.drop-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 106px;
  color: #9aa7b6;
  font-size: 12px;
  gap: 6px;
}

.drop-placeholder i {
  font-size: 20px;
}

.drop-placeholder p {
  margin: 0;
  text-align: center;
}

.selected-field {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  margin-bottom: 6px;
  border: 1px solid #cddcf6;
  border-radius: 6px;
  background: #f2f7ff;
  color: #31548f;
  font-size: 12px;
}

.selected-field:last-child {
  margin-bottom: 0;
}

.selected-field__icon {
  color: #8095b0;
  font-size: 12px;
}

.selected-field__icon--fx {
  color: var(--primary);
}

.selected-field__text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-field__remove {
  color: #8c9cb0;
  cursor: pointer;
  transition: color 0.2s;
}

.selected-field__remove:hover {
  color: #f56c6c;
}

@media (max-width: 1200px) {
  .dc-main {
    flex-direction: column;
  }

  .dc-cards {
    width: 100%;
    min-width: 0;
    max-width: 100%;
    flex: 1 1 auto;
    height: auto;
  }

  .field-management-area {
    width: 100%;
    min-width: 0;
    max-width: 100%;
    flex: 1 1 auto;
    height: auto;
    max-height: none;
  }

  .dc-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .dc-toolbar__left {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .dc-toolbar__item {
    width: 100%;
  }

  .dc-toolbar__right {
    justify-content: flex-end;
  }
}
</style>











