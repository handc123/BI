<template>
  <el-dialog
    title="查询条件配置"
    :visible.sync="dialogVisible"
    width="90%"
    :modal="true"
    :append-to-body="true"
    :destroy-on-close="true"
    :close-on-click-modal="false"
    @close="handleClose"
    @open="handleOpen"
  >
    <div v-loading="loading" class="query-config-content">
      <div class="config-layout">
        <!-- 左侧面板: 条件列表 (25%) -->
        <div class="layout-column left-panel">
          <condition-list-panel
            :conditions="localConditions"
            :selected-condition="selectedCondition"
            :validation-errors="validationErrors"
            @add-condition="handleAddCondition"
            @select-condition="handleSelectCondition"
            @delete-condition="handleDeleteCondition"
            @reorder-conditions="handleReorderConditions"
            @update-condition-name="handleUpdateConditionName"
          />
        </div>

        <!-- 中间面板: 字段关联 (40%) -->
        <div class="layout-column middle-panel">
          <field-linking-panel
            :selected-condition="selectedCondition"
            :dataset-id="datasetId"
            :mappings="localMappings"
            :available-tables="availableTables"
            :available-fields="availableFields"
            :layout-components="((layoutComponents && layoutComponents.length > 0) ? layoutComponents : components)"
            :validation-errors="validationErrors"
            @update-mappings="handleUpdateMappings"
          />
        </div>
        
        <!-- 右侧面板: 显示设置 (35%) -->
        <div class="layout-column right-panel">
          <display-settings-panel
            :selected-condition="selectedCondition"
            :field-types="selectedFieldTypes"
            :validation-errors="currentValidationErrors"
            @update-display-config="handleUpdateDisplayConfig"
            @update-required="handleUpdateRequired"
            @update-visible="handleUpdateVisible"
          />
        </div>
      </div>
    </div>
    
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">
        确定
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import ConditionListPanel from '@/components/QueryConditionConfigPanel/ConditionListPanel'
import FieldLinkingPanel from '@/components/QueryConditionConfigPanel/FieldLinkingPanel'
import DisplaySettingsPanel from '@/components/QueryConditionConfigPanel/DisplaySettingsPanel'
import { 
  listConditions, 
  saveConditionConfig, 
  deleteCondition,
  getDatasetFields,
  validateConditionConfig 
} from '@/api/bi/condition'

export default {
  name: 'QueryConditionConfig',
  components: {
    ConditionListPanel,
    FieldLinkingPanel,
    DisplaySettingsPanel
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    componentId: {
      type: Number,
      default: null
    },
    dashboardId: {
      type: Number,
      default: null
    },
    datasetId: {
      type: Number,
      default: null
    },
    layoutComponents: {
      type: Array,
      default: () => []
    },
    // 支持另一种 props 命名方式（用于 designer.vue）
    components: {
      type: Array,
      default: () => []
    },
    conditions: {
      type: Array,
      default: () => []
    },
    conditionMappings: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      saving: false,
      localConditions: [],
      localMappings: [],
      selectedCondition: null,
      availableTables: [],
      availableFields: [],
      validationErrors: {},
      conditionIdCounter: 1,
      validateTimer: null // 防抖计时器
    }
  },
  computed: {
    selectedFieldTypes() {
      if (!this.selectedCondition) {
        return []
      }

      // 获取当前条件关联的字段类型
      const conditionMappings = this.localMappings.filter(
        m => m.conditionId === this.selectedCondition.id
      )

      const fieldTypes = []

      conditionMappings.forEach(mapping => {
        if (mapping.mappingType === 'component') {
          // 组件映射：返回完整的字段信息
          if (mapping.dataType || mapping.fieldName) {
            fieldTypes.push({
              dataType: mapping.dataType,
              fieldName: mapping.fieldName,
              dbFieldName: mapping.fieldName,
              comment: mapping.fieldComment || mapping.fieldName
            })
          }
        } else {
          // 表映射：从 availableFields 中查找
          const field = this.availableFields.find(
            f => f.tableName === mapping.tableName && f.fieldName === mapping.fieldName
          )
          if (field) {
            fieldTypes.push({
              dataType: field.fieldType || field.dataType,
              fieldName: field.fieldName || field.dbFieldName,
              dbFieldName: field.dbFieldName || field.fieldName,
              comment: field.comment || field.fieldComment || field.fieldName
            })
          }
        }
      })

      console.log('[QueryConditionConfig] selectedFieldTypes:', fieldTypes)
      return fieldTypes
    },
    
    currentValidationErrors() {
      if (!this.selectedCondition) {
        return {}
      }
      return this.validationErrors[this.selectedCondition.id] || {}
    }
  },
  watch: {
    visible(val) {
      if (this.dialogVisible !== val) {
        this.dialogVisible = val
      }
    },
    dialogVisible(val) {
      if (val !== this.visible) {
        this.$emit('update:visible', val)
      }
    },
    // 监听配置变化,实时验证
    localConditions: {
      handler() {
        this.debouncedValidate()
      },
      deep: true
    },
    localMappings: {
      handler() {
        this.debouncedValidate()
      },
      deep: true
    }
  },
  methods: {
    async handleOpen() {
      // 如果传入了 conditions prop，使用传入的数据
      if (this.conditions && this.conditions.length > 0) {
        this.localConditions = [...this.conditions]
        this.localMappings = [...(this.conditionMappings || [])]

        // 更新ID计数器
        if (this.localConditions.length > 0) {
          const maxId = Math.max(...this.localConditions.map(c => c.id || 0))
          this.conditionIdCounter = maxId + 1
        }

        // 如果有条件,默认选中第一个
        if (this.localConditions.length > 0 && !this.selectedCondition) {
          this.selectedCondition = this.localConditions[0]
        }
      } else {
        // 否则从后端加载
        await this.loadConditions()
      }

      await this.loadDatasetFields()
    },

    /**
     * 加载查询条件列表
     */
    async loadConditions() {
      if (!this.componentId) {
        return
      }

      this.loading = true
      try {
        const response = await listConditions(this.componentId)

        if (response.code === 200) {
          // 加载条件列表
          this.localConditions = response.rows || []

          // 加载映射列表
          this.localMappings = []
          this.localConditions.forEach(condition => {
            if (condition.mappings && condition.mappings.length > 0) {
              this.localMappings.push(...condition.mappings)
            }
          })

          // 更新ID计数器
          if (this.localConditions.length > 0) {
            const maxId = Math.max(...this.localConditions.map(c => c.id || 0))
            this.conditionIdCounter = maxId + 1
          }

          // 如果有条件,默认选中第一个
          if (this.localConditions.length > 0 && !this.selectedCondition) {
            this.selectedCondition = this.localConditions[0]
          }
        } else {
          this.$message.error(response.msg || '加载查询条件失败')
        }
      } catch (error) {
        console.error('加载查询条件失败:', error)
        this.$notify.error({
          title: '加载失败',
          message: '无法加载查询条件,请检查网络连接'
        })
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 加载数据集字段列表
     */
    async loadDatasetFields() {
      if (!this.datasetId) {
        return
      }
      
      try {
        const response = await getDatasetFields(this.datasetId)
        
        if (response.code === 200) {
          this.availableFields = response.data || []
          
          // 提取唯一的表列表
          const tableMap = new Map()
          this.availableFields.forEach(field => {
            if (!tableMap.has(field.tableName)) {
              tableMap.set(field.tableName, {
                tableName: field.tableName,
                tableComment: field.tableComment || field.tableName
              })
            }
          })
          this.availableTables = Array.from(tableMap.values())
        }
      } catch (error) {
        console.error('加载数据集字段失败:', error)
      }
    },
    
    /**
     * 添加新条件
     */
    handleAddCondition() {
      const newCondition = {
        id: -this.conditionIdCounter++, // 使用负数ID表示新建
        componentId: this.componentId,
        conditionName: `查询条件${this.localConditions.length + 1}`,
        conditionType: 'text',
        displayOrder: this.localConditions.length + 1,
        isRequired: '0',
        isVisible: '1',
        defaultValue: null,
        config: {
          displayMode: 'auto',
          displayType: 'text',
          timeGranularity: null,
          defaultTimeRange: null,
          dropdownOptions: []
        },
        mappings: []
      }
      
      this.localConditions.push(newCondition)
      this.selectedCondition = newCondition
    },
    
    /**
     * 选择条件
     */
    handleSelectCondition(condition) {
      this.selectedCondition = condition
    },
    
    /**
     * 删除条件
     */
    async handleDeleteCondition(conditionId) {
      const index = this.localConditions.findIndex(c => c.id === conditionId)
      if (index === -1) {
        return
      }
      
      // 如果是已保存的条件,调用后端删除
      if (conditionId > 0) {
        try {
          const response = await deleteCondition(conditionId)
          if (response.code !== 200) {
            this.$message.error(response.msg || '删除失败')
            return
          }
        } catch (error) {
          console.error('删除条件失败:', error)
          this.$message.error('删除失败')
          return
        }
      }
      
      // 从本地列表中移除
      this.localConditions.splice(index, 1)
      
      // 移除相关映射
      this.localMappings = this.localMappings.filter(
        m => m.conditionId !== conditionId
      )
      
      // 如果删除的是当前选中的条件,选中下一个
      if (this.selectedCondition && this.selectedCondition.id === conditionId) {
        this.selectedCondition = this.localConditions.length > 0 
          ? this.localConditions[0] 
          : null
      }
      
      this.$message.success('删除成功')
    },
    
    /**
     * 重新排序条件
     */
    handleReorderConditions(orders) {
      // 更新本地条件的显示顺序
      orders.forEach(order => {
        const condition = this.localConditions.find(c => c.id === order.id)
        if (condition) {
          condition.displayOrder = order.displayOrder
        }
      })
    },
    
    /**
     * 更新条件名称
     */
    handleUpdateConditionName({ id, name }) {
      const condition = this.localConditions.find(c => c.id === id)
      if (condition) {
        condition.conditionName = name
      }
    },
    
    /**
     * 更新字段映射
     */
    handleUpdateMappings({ conditionId, mappings }) {
      // 移除该条件的旧映射
      this.localMappings = this.localMappings.filter(
        m => m.conditionId !== conditionId
      )

      // 添加新映射
      this.localMappings.push(...mappings)

      // 更新条件对象中的映射
      const condition = this.localConditions.find(c => c.id === conditionId)
      if (condition) {
        condition.mappings = mappings
      }
    },
    
    /**
     * 更新显示配置
     */
    handleUpdateDisplayConfig({ conditionId, config, defaultValue }) {
      console.log('[QueryConditionConfig] handleUpdateDisplayConfig 被调用:', {
        conditionId,
        config,
        displayType: config.displayType
      })

      const condition = this.localConditions.find(c => c.id === conditionId)
      if (condition) {
        condition.config = { ...config }
        condition.defaultValue = defaultValue
        condition.conditionType = config.displayType

        console.log('[QueryConditionConfig] 已更新 condition.conditionType:', {
          conditionId,
          newConditionType: condition.conditionType
        })
      }
    },
    
    /**
     * 更新必填状态
     */
    handleUpdateRequired({ conditionId, isRequired }) {
      const condition = this.localConditions.find(c => c.id === conditionId)
      if (condition) {
        condition.isRequired = isRequired
      }
    },
    
    /**
     * 更新可见性
     */
    handleUpdateVisible({ conditionId, isVisible }) {
      const condition = this.localConditions.find(c => c.id === conditionId)
      if (condition) {
        condition.isVisible = isVisible
      }
    },

    /**
     * 防抖验证配置
     */
    debouncedValidate() {
      // 清除之前的计时器
      if (this.validateTimer) {
        clearTimeout(this.validateTimer)
      }

      // 设置新的计时器，500ms 后执行验证
      this.validateTimer = setTimeout(() => {
        this.validateConfig()
      }, 500)
    },

    /**
     * 验证配置
     */
    async validateConfig() {
      this.validationErrors = {}

      // 前端验证
      const errors = {}

      // 验证条件名称唯一性和非空
      const nameMap = new Map()
      this.localConditions.forEach(condition => {
        // 验证名称非空
        if (!condition.conditionName || !condition.conditionName.trim()) {
          if (!errors[condition.id]) {
            errors[condition.id] = {}
          }
          errors[condition.id].conditionName = '条件名称不能为空'
        }

        // 验证名称唯一性
        const name = condition.conditionName.trim()
        if (nameMap.has(name)) {
          if (!errors[condition.id]) {
            errors[condition.id] = {}
          }
          errors[condition.id].conditionName = '条件名称重复'
        } else {
          nameMap.set(name, condition.id)
        }

        // 验证字段映射
        const conditionMappings = this.localMappings.filter(
          m => m.conditionId === condition.id
        )
        if (conditionMappings.length === 0) {
          if (!errors[condition.id]) {
            errors[condition.id] = {}
          }
          errors[condition.id].mappings = '至少需要关联一个字段'
        }

        // 验证时间类型必须有粒度
        const config = condition.config || {}
        if ((config.displayType === 'time' || config.displayType === 'range')
            && !config.timeGranularity) {
          if (!errors[condition.id]) {
            errors[condition.id] = {}
          }
          errors[condition.id].timeGranularity = '时间类型条件必须指定时间粒度'
        }

        // 验证显示类型兼容性
        if (conditionMappings.length > 0) {
          const incompatibleFields = []
          conditionMappings.forEach(mapping => {
            const field = this.availableFields.find(
              f => f.tableName === mapping.tableName && f.fieldName === mapping.fieldName
            )
            // 使用 dataType 而不是 fieldType，因为 fieldType 是 dimension/metric
            // 而 dataType 才是实际的数据库字段类型（如 DATE, VARCHAR）
            if (field && field.dataType && !this.isDisplayTypeCompatible(config.displayType, field.dataType)) {
              incompatibleFields.push(`${field.fieldComment || field.fieldName}(${field.dataType})`)
            }
          })

          if (incompatibleFields.length > 0) {
            if (!errors[condition.id]) {
              errors[condition.id] = {}
            }
            errors[condition.id].displayType =
              `显示类型与字段不兼容: ${incompatibleFields.join(', ')}`
          }
        }

        // 验证默认值格式
        if (condition.defaultValue) {
          const defaultValidationError = this.validateDefaultValueFormat(
            condition.defaultValue,
            config.displayType
          )
          if (defaultValidationError) {
            if (!errors[condition.id]) {
              errors[condition.id] = {}
            }
            errors[condition.id].defaultValue = defaultValidationError
          }
        }

        // 验证自定义时间范围
        if (config.defaultTimeRange === 'custom') {
          if (!config.customRangeStart || !config.customRangeEnd) {
            if (!errors[condition.id]) {
              errors[condition.id] = {}
            }
            errors[condition.id].customRange = '自定义时间范围必须指定开始和结束时间'
          } else if (new Date(config.customRangeStart) > new Date(config.customRangeEnd)) {
            if (!errors[condition.id]) {
              errors[condition.id] = {}
            }
            errors[condition.id].customRange = '开始时间不能晚于结束时间'
          }
        }
      })

      this.validationErrors = errors

      // 如果有前端验证错误,直接返回
      if (Object.keys(errors).length > 0) {
        return {
          valid: false,
          errors: errors
        }
      }

      // 调用后端验证（仅在有 dashboardId 和 componentId 时）
      if (!this.dashboardId || !this.componentId) {
        // 新组件或新仪表板还未保存，跳过后端验证
        return { valid: true, errors: {} }
      }

      try {
        const configDTO = this.buildConfigDTO()
        const response = await validateConditionConfig(configDTO)

        if (response.code === 200) {
          const result = response.data
          if (!result.valid) {
            // 转换后端验证错误格式
            const backendErrors = {}
            if (result.errors && result.errors.length > 0) {
              result.errors.forEach(error => {
                const conditionId = error.conditionId
                if (!backendErrors[conditionId]) {
                  backendErrors[conditionId] = {}
                }
                backendErrors[conditionId][error.field] = error.message
              })
            }
            this.validationErrors = backendErrors
          }
          return result
        }
      } catch (error) {
        console.error('验证配置失败:', error)
        // 如果是仪表板ID或组件ID为空的错误，返回有效（新组件/仪表板还没保存）
        if (error.message && (error.message.includes('仪表板ID不能为空') || error.message.includes('组件ID不能为空'))) {
          return { valid: true, errors: {} }
        }
      }

      return { valid: true, errors: {} }
    },

    /**
     * 检查显示类型与字段类型是否兼容
     */
    isDisplayTypeCompatible(displayType, fieldType) {
      // 定义兼容性映射
      const compatibilityMap = {
        'text': ['VARCHAR', 'CHAR', 'TEXT', 'TINYTEXT', 'MEDIUMTEXT', 'LONGTEXT', 'STRING'],
        'number': ['INT', 'TINYINT', 'SMALLINT', 'MEDIUMINT', 'BIGINT', 'FLOAT', 'DOUBLE', 'DECIMAL', 'NUMERIC'],
        'time': ['DATE', 'DATETIME', 'TIMESTAMP', 'TIME', 'YEAR'],
        'range': ['DATE', 'DATETIME', 'TIMESTAMP', 'INT', 'BIGINT', 'DECIMAL'], // 添加数值类型支持
        'dropdown': ['VARCHAR', 'CHAR', 'INT', 'TINYINT', 'SMALLINT', 'BIGINT']
      }

      const compatibleTypes = compatibilityMap[displayType] || []

      // 如果没有指定显示类型或字段类型，默认兼容
      if (!displayType || !fieldType) {
        return true
      }

      const upperFieldType = fieldType.toUpperCase()
      return compatibleTypes.some(type => upperFieldType.includes(type))
    },

    /**
     * 验证默认值格式
     */
    validateDefaultValueFormat(defaultValue, displayType) {
      if (!defaultValue) {
        return null
      }

      switch (displayType) {
        case 'number':
          if (isNaN(Number(defaultValue))) {
            return '默认值必须是有效的数字'
          }
          break

        case 'text':
        case 'dropdown':
          if (typeof defaultValue !== 'string') {
            return '默认值必须是文本'
          }
          break

        case 'time':
        case 'range':
          // 时间类型使用默认时间范围,不需要验证默认值
          if (defaultValue && typeof defaultValue === 'string') {
            // 检查是否是ISO格式日期
            if (!isNaN(Date.parse(defaultValue))) {
              return null
            }
            return '时间类型的默认值应该通过默认时间范围设置'
          }
          break
      }

      return null
    },
    
    /**
     * 构建配置DTO
     */
    buildConfigDTO() {
      return {
        componentId: this.componentId,
        dashboardId: this.dashboardId,
        conditions: this.localConditions.map(condition => ({
          id: condition.id > 0 ? condition.id : null,
          conditionName: condition.conditionName,
          conditionType: condition.conditionType,
          displayOrder: condition.displayOrder,
          isRequired: condition.isRequired,
          isVisible: condition.isVisible,
          defaultValue: condition.defaultValue,
          config: condition.config,
          mappings: this.localMappings
            .filter(m => m.conditionId === condition.id)
            .map(m => ({
              id: m.id > 0 ? m.id : null,
              tableName: m.tableName,
              fieldName: m.fieldName,
              mappingType: m.mappingType
            }))
        }))
      }
    },
    
    /**
     * 保存配置
     */
    async handleSave() {
      console.log('[QueryConditionConfig] 开始保存配置')

      // 验证配置
      const validationResult = await this.validateConfig()

      console.log('[QueryConditionConfig] 验证结果:', validationResult)

      if (!validationResult.valid) {
        this.$message.error('配置验证失败,请检查错误提示')

        // 如果当前选中的条件有错误,保持选中
        // 否则选中第一个有错误的条件
        if (!this.validationErrors[this.selectedCondition?.id]) {
          const firstErrorConditionId = Object.keys(this.validationErrors)[0]
          if (firstErrorConditionId) {
            const errorCondition = this.localConditions.find(
              c => c.id === parseInt(firstErrorConditionId)
            )
            if (errorCondition) {
              this.selectedCondition = errorCondition
            }
          }
        }

        return
      }

      // 检查是否为新仪表板或新组件
      if (!this.dashboardId || !this.componentId) {
        console.log('[QueryConditionConfig] 新组件，只更新本地状态', {
          conditionsCount: this.localConditions.length,
          mappingsCount: this.localMappings.length
        })

        // 新仪表板或新组件还未保存到数据库，只更新本地状态
        this.$message.success('配置已保存（本地）')
        this.$emit('update', {
          conditions: this.localConditions,
          mappings: this.localMappings
        })
        console.log('[QueryConditionConfig] 已触发 update 事件（本地模式）')
        this.handleClose()
        return
      }

      // 保存配置到后端
      this.saving = true
      try {
        const configDTO = this.buildConfigDTO()
        const response = await saveConditionConfig(configDTO)

        if (response.code === 200) {
          this.$message.success('保存成功')
          this.$emit('update', {
            conditions: this.localConditions,
            mappings: this.localMappings
          })
          console.log('[QueryConditionConfig] 已触发 update 事件（后端模式）')
          this.handleClose()
        } else {
          this.$message.error(response.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存配置失败:', error)
        // 如果是仪表板ID或组件ID为空的错误，更新本地状态
        if (error.message && (error.message.includes('仪表板ID不能为空') || error.message.includes('组件ID不能为空'))) {
          this.$message.warning('配置已保存到本地，保存仪表板后将同步到服务器')
          this.$emit('update', {
            conditions: this.localConditions,
            mappings: this.localMappings
          })
          console.log('[QueryConditionConfig] 已触发 update 事件（错误恢复模式）')
          this.handleClose()
        } else {
          this.$notify.error({
            title: '保存失败',
            message: '无法保存配置,请检查网络连接'
          })
        }
      } finally {
        this.saving = false
      }
    },

    /**
     * 获取所有组件（layoutComponents 和 components 的合并）
     */
    getAllComponents() {
      const layoutComps = this.layoutComponents || []
      const comps = this.components || []
      return [...layoutComps, ...comps]
    },

    /**
     * 获取组件的唯一键
     */
    getComponentKey(component) {
      return component.i || component.id || component.key
    },

    /**
     * 关闭对话框
     */
    handleClose() { 
      // 重置状态
      this.localConditions = []
      this.localMappings = []
      this.selectedCondition = null
      this.validationErrors = {}
      
      this.dialogVisible = false
    }
  },
  beforeDestroy() {
    // 清除验证计时器
    if (this.validateTimer) {
      clearTimeout(this.validateTimer)
      this.validateTimer = null
    }

    // 确保组件销毁时清理遮罩层
    this.$nextTick(() => {
      const modals = document.querySelectorAll('.v-modal');
      modals.forEach(modal => {
        const visibleDialogs = document.querySelectorAll('.el-dialog__wrapper[style*="display"]');
        if (visibleDialogs.length === 0) {
          modal.remove();
        }
      });
    });
  }
}
</script>

<style lang="scss" scoped>
.query-config-content { 
  min-height: 500px;
  max-height: 70vh;
  overflow: hidden;
}

.config-layout {
  display: flex;
  height: 100%;
  min-height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  
  .layout-column {
    height: 100%;
    overflow: hidden;
  }
  
  .left-panel {
    width: 25%;
    min-width: 200px;
  }
  
  .middle-panel {
    width: 40%;
    min-width: 300px;
  }
  
  .right-panel {
    width: 35%;
    min-width: 280px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 1200px) {
  .config-layout {
    overflow-x: auto;
    
    .layout-column {
      flex-shrink: 0;
    }
  }
}
</style>
