<template>
  <div class="field-linking-panel">
    <div class="panel-header">
      <span class="panel-title">字段关联</span>
    </div>

    <div v-if="!selectedCondition" class="panel-body empty-state">
      <i class="el-icon-connection"></i>
      <p>请先选择一个查询条件</p>
    </div>

    <div v-else class="panel-body">
      <!-- 关联图表列表 (仅当有组件时显示) -->
      <div v-if="availableComponents.length > 0" class="form-section">
        <label class="form-label">选择关联图表</label>
        <div class="chart-list">
          <div
            v-for="component in availableComponents"
            :key="getComponentKey(component)"
            class="chart-list-item"
          >
            <div class="chart-item-header">
              <el-checkbox
                v-model="selectedComponents[getComponentKey(component)]"
                @change="() => handleComponentSelection(getComponentKey(component))"
              >
                <div class="chart-item-title">
                  <i :class="getComponentIcon(component.type)"></i>
                  <span class="chart-name">{{ component.name }}</span>
                  <el-tag size="mini" type="info">{{ getDatasetName(component) }}</el-tag>
                </div>
              </el-checkbox>
            </div>

            <!-- 展开的字段列表 -->
            <div v-if="selectedComponents[getComponentKey(component)]" class="chart-item-fields">
              <!-- 维度字段 -->
              <div v-if="getDimensionFields(component).length > 0" class="field-section">
                <div class="field-section-title">
                  <i class="el-icon-s-grid"></i>
                  <span>维度字段</span>
                </div>
                <el-checkbox-group
                  v-if="selectedComponentFields[getComponentKey(component)]"
                  v-model="selectedComponentFields[getComponentKey(component)].dimensions"
                >
                  <div class="field-tags">
                    <el-checkbox
                      v-for="field in getDimensionFields(component)"
                      :key="field.fieldName || field.field"
                      :label="getFieldLabelValue(field)"
                      border
                      size="small"
                    >
                      {{ field.comment || field.fieldName || field.field || field.label }}
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
              </div>

              <!-- 指标字段 -->
              <div v-if="getMetricFields(component).length > 0" class="field-section">
                <div class="field-section-title">
                  <i class="el-icon-data-line"></i>
                  <span>指标字段</span>
                </div>
                <el-checkbox-group
                  v-if="selectedComponentFields[getComponentKey(component)]"
                  v-model="selectedComponentFields[getComponentKey(component)].metrics"
                >
                  <div class="field-tags">
                    <el-checkbox
                      v-for="field in getMetricFields(component)"
                      :key="field.fieldName || field.field"
                      :label="getFieldLabelValue(field)"
                      border
                      size="small"
                    >
                      {{ field.comment || field.fieldName || field.field || field.label }}
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
              </div>

              <div v-if="getDimensionFields(component).length === 0 && getMetricFields(component).length === 0" class="empty-fields-hint">
                <i class="el-icon-warning"></i>
                该图表未配置维度或指标字段
              </div>
            </div>
          </div>

          <div v-if="availableComponents.length === 0" class="empty-hint">
            <i class="el-icon-info"></i>
            <span>没有可用的图表组件</span>
          </div>
        </div>
      </div>

      <!-- 验证错误提示 -->
      <div v-if="currentValidationError" class="validation-error-alert">
        <i class="el-icon-warning"></i>
        {{ currentValidationError }}
      </div>

      <!-- 已关联字段汇总 -->
      <div v-if="mappedFieldsCount > 0" class="form-section">
        <div class="mapping-summary">
          <i class="el-icon-success"></i>
          <span>已关联 {{ mappedFieldsCount }} 个字段</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getDatasetFields } from '@/api/bi/condition'

export default {
  name: 'FieldLinkingPanel',
  props: {
    selectedCondition: {
      type: Object,
      default: null
    },
    datasetId: {
      type: Number,
      default: null
    },
    mappings: {
      type: Array,
      default: () => []
    },
    availableTables: {
      type: Array,
      default: () => []
    },
    availableFields: {
      type: Array,
      default: () => []
    },
    layoutComponents: {
      type: Array,
      default: () => []
    },
    validationErrors: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      selectedTable: null,
      selectedFields: [],
      selectAll: false,
      isIndeterminate: false,
      selectedComponents: {}, // 组件ID -> 是否选中
      selectedComponentFields: {}, // 组件ID -> {dimensions: [], metrics: []}
      datasetFieldsCache: {}, // datasetId -> {dimensions: [], metrics: [], all: []}
      updateTimer: null // 防抖计时器
    }
  },
  computed: {
    currentTableFields() {
      if (!this.selectedTable) {
        return []
      }
      return this.availableFields.filter(f => f.tableName === this.selectedTable)
    },

    currentConditionMappings() {
      if (!this.selectedCondition) {
        return []
      }
      return this.mappings.filter(m => m.conditionId === this.selectedCondition.id)
    },

    mappedFieldsCount() {
      return this.currentConditionMappings.length
    },

    currentValidationError() {
      if (!this.selectedCondition) {
        return null
      }
      const errors = this.validationErrors[this.selectedCondition.id]
      if (!errors) {
        return null
      }
      // 优先显示映射相关的错误
      return errors.mappings || errors.displayType || null
    },

    availableComponents() {
      // 过滤出有 dataConfig 的组件（图表组件），排除查询组件
      const components = (this.layoutComponents || []).filter(c => {
        const hasDataConfig = c.dataConfig && c.name
        const isNotQuery = c.type !== 'query'
        return hasDataConfig && isNotQuery
      })

      // 为每个组件加载数据集字段
      components.forEach(component => {
        if (component.dataConfig && component.dataConfig.datasetId) {
          this.loadDatasetFields(component.dataConfig.datasetId, this.getComponentKey(component))
        }
      })

      return components
    }
  },
  watch: {
    selectedCondition: {
      handler(newVal) {
        if (newVal) {
          this.loadMappings()
        } else {
          this.resetSelection()
        }
      },
      immediate: true
    },

    selectedFields(newVal) {
      const allFieldsCount = this.currentTableFields.length
      if (allFieldsCount === 0) {
        this.selectAll = false
        this.isIndeterminate = false
      } else if (newVal.length === 0) {
        this.selectAll = false
        this.isIndeterminate = false
      } else if (newVal.length === allFieldsCount) {
        this.selectAll = true
        this.isIndeterminate = false
      } else {
        this.selectAll = false
        this.isIndeterminate = true
      }
    },

    // 初始化组件字段选择对象
    availableComponents: {
      handler(components) {
        components.forEach(component => {
          const key = this.getComponentKey(component)
          if (!this.selectedComponentFields[key]) {
            this.$set(this.selectedComponentFields, key, { dimensions: [], metrics: [] })
          }
          if (!this.selectedComponents[key]) {
            this.$set(this.selectedComponents, key, false)
          }
        })
      },
      immediate: true
    },

    // 监听字段选择变化
    selectedComponentFields: {
      handler(newVal, oldVal) {
        // 使用防抖，避免频繁更新
        this.debouncedUpdateMappings()
      },
      deep: true
    }
  },
  methods: {
    /**
     * 防抖更新映射
     */
    debouncedUpdateMappings() {
      // 清除之前的计时器
      if (this.updateTimer) {
        clearTimeout(this.updateTimer)
      }

      // 设置新的计时器，300ms 后执行更新
      this.updateTimer = setTimeout(() => {
        this.updateComponentMappings()
      }, 300)
    },

    loadMappings() {
      if (!this.selectedCondition) {
        return
      }

      const mappings = this.currentConditionMappings
      if (mappings.length > 0) {
        // 检查是否有组件映射
        const componentMappings = mappings.filter(m => m.mappingType === 'component')

        if (componentMappings.length > 0) {
          // 恢复组件选择和字段选择
          const componentFieldsMap = {}

          componentMappings.forEach(m => {
            const componentKey = m.componentId
            this.$set(this.selectedComponents, componentKey, true)

            // 初始化字段选择
            if (!componentFieldsMap[componentKey]) {
              componentFieldsMap[componentKey] = { dimensions: [], metrics: [] }
            }

            // 恢复字段选择
            if (m.fieldType === 'dimension') {
              componentFieldsMap[componentKey].dimensions.push(m.fieldName)
            } else if (m.fieldType === 'metric') {
              componentFieldsMap[componentKey].metrics.push(m.fieldName)
            }
          })

          // 批量设置字段选择状态
          Object.keys(componentFieldsMap).forEach(componentKey => {
            this.$set(this.selectedComponentFields, componentKey, componentFieldsMap[componentKey])
          })
        } else {
          // 加载第一个映射的表
          this.selectedTable = mappings[0].tableName
          this.selectedFields = mappings
            .filter(m => m.tableName === this.selectedTable)
            .map(m => m.fieldName)
        }
      } else {
        this.resetSelection()
      }
    },

    resetSelection() {
      this.selectedTable = null
      this.selectedFields = []
      this.selectAll = false
      this.isIndeterminate = false
      this.selectedComponents = {}
      this.selectedComponentFields = {}
    },

    async loadDatasetFields(datasetId, componentKey) {
      // 如果已经加载过该数据集的字段，直接返回
      if (this.datasetFieldsCache[datasetId]) {
        return
      }

      try {
        const response = await getDatasetFields(datasetId)

        if (response.code === 200) {
          const fields = response.data || []

          // 根据字段类型分类
          const dimensions = fields.filter(f => f.fieldType === 'dimension' || f.fieldType === 'DIMENSION')
          const metrics = fields.filter(f => f.fieldType === 'metric' || f.fieldType === 'METRIC')

          // 缓存数据集字段
          this.$set(this.datasetFieldsCache, datasetId, {
            dimensions,
            metrics,
            all: fields
          })
        }
      } catch (error) {
        console.error('加载数据集字段失败:', error)
      }
    },

    getDatasetName(component) {
      if (!component.dataConfig || !component.dataConfig.datasetId) {
        return '未配置数据集'
      }
      return `数据集ID: ${component.dataConfig.datasetId}`
    },

    getDimensionFields(component) {
      const datasetId = component.dataConfig && component.dataConfig.datasetId
      if (!datasetId || !this.datasetFieldsCache[datasetId]) {
        return []
      }
      return this.datasetFieldsCache[datasetId].dimensions || []
    },

    getMetricFields(component) {
      const datasetId = component.dataConfig && component.dataConfig.datasetId
      if (!datasetId || !this.datasetFieldsCache[datasetId]) {
        return []
      }
      return this.datasetFieldsCache[datasetId].metrics || []
    },

    getFieldSelection(componentKey, fieldType) {
      const fields = this.selectedComponentFields[componentKey]
      if (!fields) {
        return []
      }
      return fields[fieldType] || []
    },

    handleComponentSelection(componentKey) {
      // 确保被选中的组件已初始化字段对象
      if (this.selectedComponents[componentKey] && !this.selectedComponentFields[componentKey]) {
        this.$set(this.selectedComponentFields, componentKey, { dimensions: [], metrics: [] })
      }

      // 更新映射
      this.updateComponentMappings()
    },

    handleFieldChange(componentKey, fieldType, value) {
      // v-model 会自动更新 selectedComponentFields
      // 我们只需要更新映射即可
      this.updateComponentMappings()
    },

    handleChartFieldChange() {
      // 字段变化时更新映射
      this.updateComponentMappings()
    },

    updateComponentMappings() {
      if (!this.selectedCondition) {
        return
      }

      // 构建组件字段映射
      const componentMappings = []

      Object.keys(this.selectedComponents).forEach(componentKey => {
        if (!this.selectedComponents[componentKey]) {
          return // 未选中该组件，跳过
        }

        const component = this.availableComponents.find(c => this.getComponentKey(c) === componentKey)
        if (!component) return

        const datasetId = component.dataConfig && component.dataConfig.datasetId
        const cachedData = datasetId && this.datasetFieldsCache[datasetId]
        const allCachedFields = cachedData ? (cachedData.all || []) : []

        const fields = this.selectedComponentFields[componentKey] || { dimensions: [], metrics: [] }

        // 添加维度字段映射
        ;(fields.dimensions || []).forEach(fieldIdentifier => {
          // 精确查找物理字段名匹配的项
          const fieldInfo = allCachedFields.find(
            f => this.getFieldDbName(f) === fieldIdentifier
          )

          // 使用数据库字段名
          const dbFieldName = fieldIdentifier // 因为 label 已经绑定为物理名了

          componentMappings.push({
            conditionId: this.selectedCondition.id,
            componentId: componentKey,
            fieldName: dbFieldName,  // 保存数据库字段名，不是显示名称
            fieldType: 'dimension',
            mappingType: 'component',
            dataType: fieldInfo ? fieldInfo.dataType : null,
            fieldComment: fieldInfo ? (fieldInfo.comment || fieldInfo.fieldName) : fieldIdentifier  // 保存显示名称作为注释
          })
        })

        // 添加指标字段映射
        ;(fields.metrics || []).forEach(fieldIdentifier => {
          // 精确查找物理字段名匹配的项
          const fieldInfo = allCachedFields.find(
            f => this.getFieldDbName(f) === fieldIdentifier
          )

          // 使用数据库字段名
          const dbFieldName = fieldIdentifier // 因为 label 已经绑定为物理名了

          componentMappings.push({
            conditionId: this.selectedCondition.id,
            componentId: componentKey,
            fieldName: dbFieldName,  // 保存数据库字段名，不是显示名称
            fieldType: 'metric',
            mappingType: 'component',
            dataType: fieldInfo ? fieldInfo.dataType : null,
            fieldComment: fieldInfo ? (fieldInfo.comment || fieldInfo.fieldName) : fieldIdentifier  // 保存显示名称作为注释
          })
        })
      })

      // 保留非组件类型的映射
      const otherMappings = this.currentConditionMappings.filter(m => m.mappingType !== 'component')

      const allMappings = [...otherMappings, ...componentMappings]

      console.log('[FieldLinkingPanel] 更新字段映射:', {
        componentId: this.selectedCondition.id,
        mappingsCount: allMappings.length,
        mappings: allMappings.map(m => ({
          fieldName: m.fieldName,
          fieldComment: m.fieldComment,
          fieldType: m.fieldType
        }))
      })

      this.$emit('update-mappings', {
        conditionId: this.selectedCondition.id,
        mappings: allMappings
      })
    },

    handleTableChange(tableName) {
      if (!tableName) {
        this.selectedFields = []
        return
      }

      // 加载该表已映射的字段
      const tableMappings = this.currentConditionMappings.filter(
        m => m.tableName === tableName && m.mappingType !== 'component'
      )
      this.selectedFields = tableMappings.map(m => m.fieldName)
    },

    handleSelectAll(checked) {
      if (checked) {
        this.selectedFields = this.currentTableFields.map(f => f.fieldName)
      } else {
        this.selectedFields = []
      }
      this.handleFieldChange(this.selectedFields)
    },

    handleFieldChange(selectedFields) {
      if (!this.selectedCondition || !this.selectedTable) {
        return
      }

      // 构建新的映射列表
      const newMappings = selectedFields.map(fieldName => {
        // 查找是否已存在映射
        const existingMapping = this.currentConditionMappings.find(
          m => m.tableName === this.selectedTable && m.fieldName === fieldName
        )

        if (existingMapping) {
          return existingMapping
        }

        // 创建新映射
        return {
          conditionId: this.selectedCondition.id,
          tableName: this.selectedTable,
          fieldName: fieldName,
          mappingType: 'auto'
        }
      })

      // 移除当前表的旧映射,添加新映射（保留组件映射）
      const otherMappings = this.currentConditionMappings.filter(
        m => m.tableName !== this.selectedTable && m.mappingType !== 'component'
      )

      // 保留组件映射
      const componentMappings = this.currentConditionMappings.filter(m => m.mappingType === 'component')

      const allMappings = [...otherMappings, ...componentMappings, ...newMappings]

      this.$emit('update-mappings', {
        conditionId: this.selectedCondition.id,
        mappings: allMappings
      })
    },

    isFieldMapped(fieldName) {
      return this.selectedFields.includes(fieldName)
    },

    getComponentKey(component) {
      // 兼容两种组件结构：使用 i 或 id 作为唯一标识
      return component.i || component.id || component.key
    },

    /**
     * 获取字段标签值（用于checkbox的label）
     * 必须返回一个能唯一标识字段的值，通常是数据库字段名
     */
    getFieldLabelValue(field) {
      if (!field) return ''
      // 优先使用数据库原始字段名（dbFieldName），其次使用fieldName，最后使用field属性
      const value = field.dbFieldName || field.fieldName || field.field || ''
      return String(value)
    },

    /**
     * 获取字段的数据库字段名（用于保存映射）
     */
    getFieldDbName(field) {
      if (!field) return ''
      // 优先使用数据库原始字段名
      return field.dbFieldName || field.fieldName || field.field || ''
    },

    getComponentIcon(type) {
      const iconMap = {
        kpi: 'el-icon-data-line',
        line: 'el-icon-data-line',
        bar: 'el-icon-data-analysis',
        pie: 'el-icon-pie-chart',
        donut: 'el-icon-pie-chart',
        table: 'el-icon-s-grid',
        map: 'el-icon-location',
        funnel: 'el-icon-sort'
      }
      return iconMap[type] || 'el-icon-document'
    }
  }
}
</script>

<style lang="scss" scoped>
.field-linking-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #e4e7ed;

  .panel-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;

    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }

  .panel-body {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    &.empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;

      i {
        font-size: 48px;
        margin-bottom: 16px;
      }

      p {
        font-size: 14px;
      }
    }
  }

  .form-section {
    margin-bottom: 20px;

    .form-label {
      display: block;
      font-size: 13px;
      color: #606266;
      margin-bottom: 8px;
      font-weight: 500;
    }
  }

  .chart-list {
    .chart-list-item {
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      margin-bottom: 12px;
      overflow: hidden;

      &:last-child {
        margin-bottom: 0;
      }

      .chart-item-header {
        padding: 12px;
        background: #fafafa;
        border-bottom: 1px solid #e4e7ed;

        ::v-deep .el-checkbox {
          display: flex;
          align-items: center;
          width: 100%;

          .el-checkbox__label {
            width: 100%;
            padding-left: 8px;
          }
        }

        .chart-item-title {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;

          i {
            font-size: 16px;
            color: #409eff;
          }

          .chart-name {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
          }
        }
      }

      .chart-item-fields {
        padding: 12px;
        background: #fff;

        .field-section {
          margin-bottom: 12px;

          &:last-child {
            margin-bottom: 0;
          }

          .field-section-title {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            font-weight: 500;
            color: #606266;
            margin-bottom: 8px;

            i {
              color: #409eff;
              font-size: 14px;
            }
          }

          .field-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;

            ::v-deep .el-checkbox {
              margin: 0;

              .el-checkbox__label {
                padding-left: 0;
              }

              &.is-checked {
                .el-checkbox__label {
                  background-color: #ecf5ff;
                  border-color: #409eff;
                  color: #409eff;
                }
              }
            }
          }
        }

        .empty-fields-hint {
          padding: 20px;
          text-align: center;
          color: #909399;
          font-size: 13px;

          i {
            font-size: 20px;
            margin-right: 4px;
          }
        }
      }
    }

    .empty-hint {
      padding: 40px 20px;
      text-align: center;
      color: #909399;
      font-size: 13px;

      i {
        font-size: 36px;
        margin-bottom: 12px;
        display: block;
      }
    }
  }

  .field-list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .field-list {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 8px;
    background: #fafafa;
  }

  .field-item {
    padding: 8px;
    margin-bottom: 4px;
    background: #fff;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    &:hover {
      background: #f5f7fa;
    }

    ::v-deep .el-checkbox {
      flex: 1;

      .el-checkbox__label {
        width: 100%;
      }
    }

    .field-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .field-name {
        font-size: 13px;
        color: #303133;
      }

      .field-type {
        font-size: 12px;
        color: #909399;
        padding: 2px 6px;
        background: #f0f2f5;
        border-radius: 2px;
      }
    }

    .field-detail {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }

    .mapped-tag {
      margin-left: 8px;
    }
  }

  .empty-fields {
    text-align: center;
    padding: 40px 20px;
    color: #909399;

    i {
      font-size: 36px;
      margin-bottom: 12px;
      display: block;
    }

    p {
      font-size: 13px;
    }
  }

  .mapping-summary {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
    border-radius: 4px;
    color: #409eff;
    font-size: 13px;

    i {
      font-size: 16px;
    }
  }

  .validation-error-alert {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #fef0f0;
    border: 1px solid #fde2e2;
    border-radius: 4px;
    color: #f56c6c;
    font-size: 13px;

    i {
      font-size: 16px;
    }
  }
}
</style>
