<template>
  <div class="data-config-panel">
    <!-- 左侧配置区域 (200px) -->
    <div class="config-zone">
      <div class="zone-title">配置区域</div>

      <!-- 指标拖放区 -->
      <div class="drop-zone">
        <div class="zone-label">
          <i class="el-icon-s-data label-icon"></i>
          <span>指标</span>
        </div>
        <draggable
          v-model="metrics"
          class="drop-zone-content"
          :class="{ 'drop-zone-active': activeZone === 'metrics' }"
          group="fields"
          @add="onFieldAdd('metrics', $event)"
        >
          <div
            v-for="field in metrics"
            :key="field.fieldName"
            class="field-item metric-item"
            @click="removeField('metrics', field)"
          >
            <i class="el-icon-s-data field-icon"></i>
            <span class="field-name">{{ field.comment || field.fieldName }}</span>
            <i class="el-icon-close remove-icon"></i>
          </div>
        </draggable>
        <div v-if="metrics.length === 0" class="placeholder">
          拖入指标字段
        </div>
      </div>

      <!-- 维度拖放区 - 根据图表类型显示 -->
      <div v-if="requiresDimension" class="drop-zone">
        <div class="zone-label">
          <i class="el-icon-s-grid label-icon"></i>
          <span>维度</span>
        </div>
        <draggable
          v-model="dimensions"
          class="drop-zone-content"
          :class="{ 'drop-zone-active': activeZone === 'dimensions' }"
          group="fields"
          @add="onFieldAdd('dimensions', $event)"
        >
          <div
            v-for="field in dimensions"
            :key="field.fieldName"
            class="field-item dimension-item"
            @click="removeField('dimensions', field)"
          >
            <i class="el-icon-s-grid field-icon"></i>
            <span class="field-name">{{ field.comment || field.fieldName }}</span>
            <i class="el-icon-close remove-icon"></i>
          </div>
        </draggable>
        <div v-if="dimensions.length === 0" class="placeholder">
          拖入维度字段
        </div>
      </div>

      <!-- 过滤器拖放区 -->
      <div class="drop-zone">
        <div class="zone-label">
          <i class="el-icon-filter label-icon"></i>
          <span>过滤器</span>
        </div>
        <draggable
          v-model="filters"
          class="drop-zone-content"
          :class="{ 'drop-zone-active': activeZone === 'filters' }"
          group="fields"
          @add="onFieldAdd('filters', $event)"
        >
          <div
            v-for="field in filters"
            :key="field.fieldName"
            class="field-item filter-item"
            @click="editFilter(field)"
          >
            <i class="el-icon-filter field-icon"></i>
            <span class="field-name">{{ field.comment || field.fieldName }}</span>
            <span class="filter-value">{{ field.operator }} '{{ field.value }}'</span>
            <i class="el-icon-close remove-icon" @click.stop="removeField('filters', field)"></i>
          </div>
        </draggable>
        <div v-if="filters.length === 0" class="placeholder">
          拖入字段作为过滤条件
        </div>
      </div>

      <!-- 数量拖放区 -->
      <div class="drop-zone">
        <div class="zone-label">
          <i class="el-icon-s-marketing label-icon"></i>
          <span>数量</span>
        </div>
        <draggable
          v-model="countField"
          class="drop-zone-content"
          :class="{ 'drop-zone-active': activeZone === 'count' }"
          group="fields"
          @add="onFieldAdd('count', $event)"
        >
          <div
            v-if="countField.length > 0"
            v-for="field in countField"
            :key="field.fieldName"
            class="field-item count-item"
            @click="removeField('count', field)"
          >
            <i class="el-icon-s-marketing field-icon"></i>
            <span class="field-name">{{ field.comment || field.fieldName }}</span>
            <i class="el-icon-close remove-icon"></i>
          </div>
        </draggable>
        <div v-if="countField.length === 0" class="placeholder">
          拖入一个指标字段
        </div>
      </div>

      <!-- 更新按钮 -->
      <el-button
        type="primary"
        size="small"
        class="update-btn"
        @click="handleUpdate"
        :disabled="!isValidConfig"
      >
        <i class="el-icon-refresh"></i>
        更新
      </el-button>
    </div>

    <!-- 右侧字段区域 (200px) -->
    <div class="field-zone">
      <div class="zone-title">数据集</div>

      <!-- 数据源选择 -->
      <div class="selector-group">
        <el-select
          v-model="selectedDataSourceId"
          placeholder="选择数据源"
          size="small"
          class="selector"
          @change="onDataSourceChange"
        >
          <el-option
            v-for="ds in datasources"
            :key="ds.id"
            :label="ds.name || ds.datasourceName || `数据源${ds.id}`"
            :value="ds.id"
          />
        </el-select>
      </div>

      <!-- 数据集选择 -->
      <div class="selector-group">
        <el-select
          v-model="selectedDatasetId"
          placeholder="选择数据集"
          size="small"
          class="selector"
          :disabled="!selectedDataSourceId"
          @change="onDatasetChange"
        >
          <el-option
            v-for="ds in datasets"
            :key="ds.id"
            :label="ds.name || ds.datasetName || `数据集${ds.id}`"
            :value="ds.id"
          />
        </el-select>
      </div>

      <!-- 搜索框 -->
      <div class="search-group">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索字段"
          size="small"
          prefix-icon="el-icon-search"
          clearable
        />
      </div>

      <!-- 字段列表 -->
      <div v-loading="loadingFields" class="field-list">
        <template v-if="!selectedDatasetId">
          <div class="empty-state">
            <i class="el-icon-info"></i>
            <p>请先选择数据集</p>
          </div>
        </template>

        <template v-else-if="displayDimensionFields.length === 0 && displayMetricFields.length === 0">
          <div class="empty-state">
            <i class="el-icon-folder-opened"></i>
            <p>该数据集没有可用字段</p>
          </div>
        </template>

        <template v-else>
          <!-- 维度字段 - 根据图表类型显示 -->
          <div v-if="requiresDimension && displayDimensionFields.length > 0" class="field-group">
            <div class="field-group-title">维度字段</div>
            <draggable
              v-model="displayDimensionFields"
              :sort="false"
              :group="{ name: 'fields', pull: 'clone', put: false }"
              :clone="cloneDimensionField"
              class="field-items"
              draggable=".field-source-item"
            >
              <div
                v-for="field in displayDimensionFields"
                :key="field.fieldName"
                class="field-source-item dimension-source"
                :title="field.comment || field.fieldName"
              >
                <i class="el-icon-s-grid field-icon"></i>
                <span class="field-name">{{ field.comment ? field.comment : field.fieldName }}</span>
                <i class="el-icon-rank drag-handle"></i>
              </div>
            </draggable>
          </div>

          <!-- 指标字段 -->
          <div v-if="displayMetricFields.length > 0" class="field-group">
            <div class="field-group-title">指标字段</div>
            <draggable
              v-model="displayMetricFields"
              :sort="false"
              :group="{ name: 'fields', pull: 'clone', put: false }"
              :clone="cloneMetricField"
              class="field-items"
              draggable=".field-source-item"
            >
              <div
                v-for="field in displayMetricFields"
                :key="field.fieldName"
                class="field-source-item metric-source"
                :title="field.comment || field.fieldName"
              >
                <i class="el-icon-s-data field-icon"></i>
                <span class="field-name">{{ field.comment ? field.comment : field.fieldName }}</span>
                <i class="el-icon-rank drag-handle"></i>
              </div>
            </draggable>
          </div>
        </template>
      </div>
    </div>

    <!-- 过滤器编辑对话框 -->
    <el-dialog
      title="编辑过滤条件"
      :visible.sync="filterDialogVisible"
      width="400px"
    >
      <el-form v-if="editingFilter" label-width="80px" size="small">
        <el-form-item label="字段">
          <el-input v-model="editingFilter.fieldName" disabled />
        </el-form-item>
        <el-form-item label="操作符">
          <el-select v-model="editingFilter.operator" style="width: 100%">
            <el-option label="等于" value="=" />
            <el-option label="不等于" value="!=" />
            <el-option label="大于" value=">" />
            <el-option label="小于" value="<" />
            <el-option label="大于等于" value=">=" />
            <el-option label="小于等于" value="<=" />
            <el-option label="包含" value="like" />
            <el-option label="不包含" value="not like" />
          </el-select>
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="editingFilter.value" placeholder="请输入值" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="filterDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFilter">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import { listDataSource } from '@/api/bi/datasource'
import { listDataset, getDatasetFields } from '@/api/bi/dataset'

export default {
  name: 'DataConfigPanel',
  components: {
    draggable
  },
  props: {
    // 父组件传入的组件对象（用于获取图表类型）
    component: {
      type: Object,
      default: null
    },
    // 父组件传入的初始配置
    initialConfig: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      // 配置区域数据
      metrics: [],
      dimensions: [],
      filters: [],
      countField: [],

      // 选中的数据源和数据集
      selectedDataSourceId: null,
      selectedDatasetId: null,

      // 可用的数据源和数据集列表
      datasources: [],
      datasets: [],

      // 字段数据
      dimensionFields: [],
      metricFields: [],
      displayDimensionFields: [],
      displayMetricFields: [],
      loadingFields: false,

      // 搜索关键词
      searchKeyword: '',

      // UI 状态
      activeZone: null,

      // 过滤器编辑
      filterDialogVisible: false,
      editingFilter: null,

      // 加载标志，防止重复加载
      isLoadingConfig: false,
      lastLoadedConfigId: null
    }
  },
  computed: {
    isValidConfig() {
      // 对于不需要维度的图表，只需要有指标即可
      if (!this.requiresDimension) {
        return this.metrics.length > 0
      }
      // 对于需要维度的图表，至少需要指标或维度之一
      return this.metrics.length > 0 || this.dimensions.length > 0
    },
    // 判断当前图表类型是否需要维度字段
    requiresDimension() {
      const chartType = this.component?.styleConfig?.chartType
      // 不需要维度的图表类型：仪表盘、水球图、指标卡等单值图表
      const singleValueCharts = ['gauge', 'liquidfill', 'card', 'metricCard']
      return !singleValueCharts.includes(chartType)
    }
  },
  watch: {
    // 暂时完全禁用所有 watcher，避免无限循环
    /*
    // 监听字段数据变化
    dimensionFields: {
      handler() {
        this.updateDisplayFields()
      },
      deep: true
    },
    metricFields: {
      handler() {
        this.updateDisplayFields()
      },
      deep: true
    },
    // 监听搜索关键词变化
    searchKeyword() {
      this.updateDisplayFields()
    },
    initialConfig: {
      immediate: false,
      handler(config, oldConfig) {
          hasConfig: !!config,
          datasetId: config?.datasetId,
          oldDatasetId: oldConfig?.datasetId
        })
        
        if (this.isLoadingConfig) {
          return
        }

        const configId = config ? `${config.dataSourceId}_${config.datasetId}` : 'empty'
        const oldConfigId = oldConfig ? `${oldConfig.dataSourceId}_${oldConfig.datasetId}` : 'empty'

        if (configId === oldConfigId) {
          return
        }

        if (config) {
          this.loadConfig(config)
        } else {
          this.clearConfig()
          this.selectedDataSourceId = null
          this.selectedDatasetId = null
          this.lastLoadedConfigId = 'empty'
        }
      }
    }
    */
  },
  created() {
    // 为每个组件实例生成唯一ID，用于调试
    this._componentInstanceId = Math.random().toString(36).substr(2, 9)
  },
  mounted() {
    this.loadDatasources()
    
    // 在 mounted 中加载初始配置
    this.$nextTick(() => {
      if (this.initialConfig) {
        this.loadConfig(this.initialConfig)
      }
    })
  },
  methods: {
    // 更新显示的字段列表（搜索过滤）
    updateDisplayFields() {
      if (!this.searchKeyword) {
        this.displayDimensionFields = [...this.dimensionFields]
        this.displayMetricFields = [...this.metricFields]
      } else {
        const keyword = this.searchKeyword.toLowerCase()
        this.displayDimensionFields = this.dimensionFields.filter(field => {
          const name = (field.fieldName || field.name || '').toLowerCase()
          const comment = (field.comment || '').toLowerCase()
          return name.includes(keyword) || comment.includes(keyword)
        })
        this.displayMetricFields = this.metricFields.filter(field => {
          const name = (field.fieldName || field.name || '').toLowerCase()
          const comment = (field.comment || '').toLowerCase()
          return name.includes(keyword) || comment.includes(keyword)
        })
      }
    },

    // 加载数据源列表
    async loadDatasources() {
      try {
        const response = await listDataSource({ pageNum: 1, pageSize: 1000, status: '0' })
        this.datasources = response.rows || []
      } catch (error) {
        console.error('加载数据源失败:', error)
        this.$message.error('加载数据源失败')
      }
    },

    // 加载数据集列表
    async loadDatasources() {
      try {
        const response = await listDataSource({})
        this.datasources = response.rows || []
      } catch (error) {
        console.error('加载数据源失败:', error)
        this.$message.error('加载数据源失败')
      }
    },

    // 数据源改变
    async onDataSourceChange(dataSourceId) {
      this.selectedDatasetId = null
      this.datasets = []
      this.dimensionFields = []
      this.metricFields = []
      this.clearConfig()

      if (!dataSourceId) return

      try {
        const response = await listDataset({ datasourceId: dataSourceId })
        this.datasets = response.rows || []
      } catch (error) {
        console.error('加载数据集失败:', error)
        this.$message.error('加载数据集失败')
        throw error
      }
    },

    // 数据集改变
    async onDatasetChange(datasetId) {
      this.dimensionFields = []
      this.metricFields = []
      this.clearConfig()

      if (!datasetId) {
        // 清空显示字段
        this.updateDisplayFields()
        return
      }

      this.loadingFields = true
      try {
        const response = await getDatasetFields(datasetId)
        const fields = response.data || []

        // 分离维度和指标字段
        this.dimensionFields = fields.filter(f => f.fieldType === 'dimension').map(f => ({
          fieldName: f.fieldName || f.name,
          fieldType: f.fieldType,
          comment: f.comment
        }))

        this.metricFields = fields.filter(f => f.fieldType === 'metric').map(f => ({
          fieldName: f.fieldName || f.name,
          fieldType: f.fieldType,
          comment: f.comment
        }))

        // 更新显示的字段列表
        this.updateDisplayFields()
      } catch (error) {
        console.error('加载字段失败:', error)
        this.$message.error('加载字段失败')
        throw error
      } finally {
        this.loadingFields = false
      }
    },

    // 拖拽开始
    onDragStart(fieldType, event) {
      this.activeZone = fieldType
    },

    // 拖拽结束
    onDragEnd(event) {
      this.activeZone = null
    },

    // 克隆维度字段（拖拽时调用）
    cloneDimensionField(field) {

      // 使用 JSON.parse 来深度复制，避免响应式对象问题
      const cloned = {
        fieldName: field.fieldName || field.name,
        fieldType: 'dimension',
        comment: field.comment
      }
      return cloned
    },

    // 克隆指标字段（拖拽时调用）
    cloneMetricField(field) {

      // 使用 JSON.parse 来深度复制，避免响应式对象问题
      const cloned = {
        fieldName: field.fieldName || field.name,
        fieldType: 'metric',
        comment: field.comment
      }
      return cloned
    },

    // 字段添加到拖放区
    onFieldAdd(zone, event) {

      // vuedraggable 2.x 使用 clone 函数时，数据已经自动添加到目标数组
      // 我们需要从目标数组中获取刚添加的字段（最后一个元素）
      const targetArray = this[zone]
      const addedField = targetArray[targetArray.length - 1]

      if (!addedField) {
        console.error('无法获取拖拽的字段数据')
        this.$message.error('拖拽失败，无法获取字段数据')
        return
      }


      // 获取字段名称
      const fieldName = addedField.fieldName || addedField.name
      if (!fieldName) {
        console.error('字段数据无效，缺少 fieldName:', addedField)
        this.$message.error('字段数据无效')
        return
      }


      // 验证字段类型
      const fieldType = addedField.fieldType || 'dimension'

      if (zone === 'metrics' || zone === 'count') {
        if (fieldType === 'dimension') {
          this.$message.warning('指标区只能接受指标字段')
          // 移除无效字段
          const index = this[zone].findIndex(f => f.fieldName === fieldName)
          if (index > -1) {
            this[zone].splice(index, 1)
          }
          return
        }
      } else if (zone === 'dimensions') {
        if (fieldType === 'metric') {
          this.$message.warning('维度区只能接受维度字段')
          const index = this[zone].findIndex(f => f.fieldName === fieldName)
          if (index > -1) {
            this[zone].splice(index, 1)
          }
          return
        }
      }

      // 数量区只能有一个字段
      if (zone === 'count') {
        if (this.countField.length > 1) {
          this.countField = [this.countField[this.countField.length - 1]]
        }
        // 设置聚合方式
        if (!this.countField[0].aggregation) {
          this.$set(this.countField, 0, {
            ...this.countField[0],
            aggregation: 'count',
            fieldType: 'metric'
          })
        }
      } else if (zone === 'metrics') {
        // 指标区添加默认聚合方式
        const index = this.metrics.findIndex(f => f.fieldName === fieldName)
        if (index > -1 && !this.metrics[index].aggregation) {
          this.$set(this.metrics, index, {
            ...this.metrics[index],
            aggregation: 'sum'
          })
        }
      } else if (zone === 'filters') {
        // 过滤器需要配置操作符和值
        const index = this.filters.findIndex(f => f.fieldName === fieldName)
        if (index > -1) {
          if (!this.filters[index].operator) {
            this.$set(this.filters, index, {
              ...this.filters[index],
              operator: '='
            })
          }
          if (!this.filters[index].value) {
            this.$set(this.filters, index, {
              ...this.filters[index],
              value: ''
            })
          }
          // 自动打开编辑对话框
          this.$nextTick(() => {
            this.editFilter(this.filters[index])
          })
        }
      } else if (zone === 'dimensions') {
      }

    },

    // 字段改变
    onFieldChange(zone, event) {
      if (event.added) {
        const addedField = event.added.element

        // 验证并处理字段
        const fieldName = addedField.fieldName || addedField.name
        const fieldType = addedField.fieldType

        // 验证字段类型
        if (zone === 'metrics' || zone === 'count') {
          if (fieldType === 'dimension') {
            this.$message.warning('指标区只能接受指标字段')
            // 移除无效字段
            const index = this[zone].findIndex(f => f.fieldName === fieldName)
            if (index > -1) {
              this[zone].splice(index, 1)
            }
            return
          }
        } else if (zone === 'dimensions') {
          if (fieldType === 'metric') {
            this.$message.warning('维度区只能接受维度字段')
            const index = this[zone].findIndex(f => f.fieldName === fieldName)
            if (index > -1) {
              this[zone].splice(index, 1)
            }
            return
          }
        }

        // 设置默认聚合方式
        if (zone === 'metrics') {
          const index = this[zone].findIndex(f => f.fieldName === fieldName)
          if (index > -1 && !this[zone][index].aggregation) {
            this.$set(this[zone][index], 'aggregation', 'sum')
          }
        } else if (zone === 'count') {
          const index = this[zone].findIndex(f => f.fieldName === fieldName)
          if (index > -1 && !this[zone][index].aggregation) {
            this.$set(this[zone][index], 'aggregation', 'count')
          }
        } else if (zone === 'filters') {
          const index = this[zone].findIndex(f => f.fieldName === fieldName)
          if (index > -1) {
            if (!this[zone][index].operator) {
              this.$set(this[zone][index], 'operator', '=')
            }
            if (!this[zone][index].value) {
              this.$set(this[zone][index], 'value', '')
            }
            // 自动打开编辑对话框
            this.$nextTick(() => {
              this.editFilter(this[zone][index])
            })
          }
        }
      }
    },

    // 移除字段
    removeField(zone, field) {
      const index = this[zone].findIndex(f => f.fieldName === field.fieldName)
      if (index > -1) {
        this[zone].splice(index, 1)
      }
    },

    // 编辑过滤器
    editFilter(field) {
      this.editingFilter = { ...field }
      this.filterDialogVisible = true
    },

    // 保存过滤器
    saveFilter() {
      if (!this.editingFilter.value) {
        this.$message.warning('请输入过滤值')
        return
      }

      const index = this.filters.findIndex(f => f.fieldName === this.editingFilter.fieldName)
      if (index > -1) {
        this.filters.splice(index, 1, { ...this.editingFilter })
      }

      this.filterDialogVisible = false
      this.editingFilter = null
    },

    // 加载配置
    async loadConfig(config) {
      
      // 防止重复加载
      if (this.isLoadingConfig) {
        return
      }

      this.isLoadingConfig = true

      try {
        if (config.dataSourceId) {
          this.selectedDataSourceId = config.dataSourceId

          // 加载数据集列表
          await this.onDataSourceChange(config.dataSourceId)

          if (config.datasetId) {
            this.selectedDatasetId = config.datasetId

            // 加载字段列表
            await this.onDatasetChange(config.datasetId)

            // 加载配置的字段，并补充 comment 属性
            this.metrics = (config.metrics || []).map(m => {
              // 从 metricFields 中查找匹配的字段，获取 comment
              const matchedField = this.metricFields.find(f => 
                (f.fieldName || f.name) === (m.fieldName || m.name)
              )
              return {
                ...m,
                comment: matchedField?.comment || m.comment
              }
            })
            
            this.dimensions = (config.dimensions || []).map(d => {
              // 从 dimensionFields 中查找匹配的字段，获取 comment
              const matchedField = this.dimensionFields.find(f => 
                (f.fieldName || f.name) === (d.fieldName || d.name)
              )
              return {
                ...d,
                comment: matchedField?.comment || d.comment
              }
            })
            
            this.filters = config.filters || []
            this.countField = config.count ? [config.count] : []

            // 更新最后加载的配置ID
            this.lastLoadedConfigId = `${config.dataSourceId}_${config.datasetId}`
          }
        }
      } catch (error) {
        console.error('[DATACONFIG] 加载配置失败:', error)
        this.$message.error('加载配置失败')
      } finally {
        this.isLoadingConfig = false
      }
    },

    // 清空配置
    clearConfig() {
      this.metrics = []
      this.dimensions = []
      this.filters = []
      this.countField = []
    },

    // 更新配置
    handleUpdate() {

      if (!this.isValidConfig) {
        // 根据图表类型给出不同的提示
        if (!this.requiresDimension) {
          this.$message.warning('请至少配置一个指标')
        } else {
          this.$message.warning('请至少配置一个指标或维度')
        }
        return
      }

      const config = {
        dataSourceId: this.selectedDataSourceId,
        datasetId: this.selectedDatasetId,
        metrics: [...this.metrics],
        dimensions: [...this.dimensions],
        filters: [...this.filters],
        count: this.countField.length > 0 ? { ...this.countField[0] } : null
      }


      // 先发出配置变更事件
      this.$emit('config-change', config)

      // 然后发出刷新图表事件（编辑模式下需要手动触发）
      this.$emit('refresh-chart')

      this.$message.success('配置已更新，正在刷新图表...')
    }
  }
}
</script>

<style scoped>
.data-config-panel {
  display: flex;
  width: 400px;
  height: 100%;
  background: #fff;
  border-left: 1px solid #e6e6e6;
}

/* ===== 左侧配置区域 ===== */
.config-zone {
  width: 200px;
  height: 100%;
  background: #f5f7fa;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  padding: 12px;
  box-sizing: border-box;
}

.zone-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e6e6e6;
}

.drop-zone {
  margin-bottom: 12px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fff;
  transition: all 0.3s;
}

.drop-zone:hover {
  border-color: #c0c4cc;
}

.drop-zone-active {
  border-color: #409eff;
  background: #ecf5ff;
}

.zone-label {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  border-bottom: 1px solid #e6e6e6;
}

.label-icon {
  margin-right: 6px;
  font-size: 14px;
  color: #909399;
}

.drop-zone-content {
  min-height: 60px;
  padding: 4px;
}

.placeholder {
  padding: 16px 12px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.field-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  margin-bottom: 4px;
  background: #f5f7fa;
  border: 1px solid #e6e6e6;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s;
}

.field-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.field-icon {
  margin-right: 6px;
  font-size: 14px;
}

.metric-item .field-icon {
  color: #67c23a;
}

.dimension-item .field-icon {
  color: #409eff;
}

.filter-item .field-icon {
  color: #e6a23c;
}

.count-item .field-icon {
  color: #f56c6c;
}

.field-name {
  flex: 1;
  font-size: 12px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.filter-value {
  margin-left: 8px;
  font-size: 11px;
  color: #909399;
}

.remove-icon {
  margin-left: 6px;
  font-size: 12px;
  color: #909399;
  cursor: pointer;
}

.remove-icon:hover {
  color: #f56c6c;
}

.update-btn {
  margin-top: auto;
  width: 100%;
}

/* ===== 右侧字段区域 ===== */
.field-zone {
  width: 200px;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  padding: 12px;
  box-sizing: border-box;
}

.selector-group {
  margin-bottom: 12px;
}

.selector {
  width: 100%;
}

.search-group {
  margin-bottom: 12px;
}

.field-list {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
  text-align: center;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #c0c4cc;
}

.empty-state p {
  font-size: 13px;
  margin: 0;
}

.field-group {
  margin-bottom: 16px;
}

.field-group-title {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  padding: 8px 0;
  margin-bottom: 8px;
  border-bottom: 1px solid #e6e6e6;
}

.field-items {
  min-height: 20px;
}

.field-source-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  cursor: move;
  transition: all 0.2s;
}

.field-source-item:hover {
  background: #f5f7fa;
  border-color: #409eff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.field-source-item.dimension-source {
  border-left: 3px solid #409eff;
}

.field-source-item.metric-source {
  border-left: 3px solid #67c23a;
}

.field-source-item .field-icon {
  margin-right: 6px;
  font-size: 14px;
}

.dimension-source .field-icon {
  color: #409eff;
}

.metric-source .field-icon {
  color: #67c23a;
}

.field-source-item .field-name {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.field-comment {
  margin-left: 8px;
  font-size: 10px;
  color: #c0c4cc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100px;
  flex-shrink: 0;
}

.drag-handle {
  margin-left: 6px;
  font-size: 14px;
  color: #c0c4cc;
  cursor: move;
}

/* ===== 拖放动画 ===== */
.flip-list-move {
  transition: transform 0.3s;
}

.flip-list-enter-active,
.flip-list-leave-active {
  transition: all 0.3s;
}

.flip-list-enter,
.flip-list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* ===== 滚动条样式 ===== */
.field-list::-webkit-scrollbar {
  width: 6px;
}

.field-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.field-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.field-list::-webkit-scrollbar-track {
  background: #f5f7fa;
}
</style>
