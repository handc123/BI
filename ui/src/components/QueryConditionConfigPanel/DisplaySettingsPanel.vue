<template>
  <div class="display-settings-panel">
    <div class="panel-header">
      <span class="panel-title">显示设置</span>
    </div>
    
    <div v-if="!selectedCondition" class="panel-body empty-state">
      <i class="el-icon-setting"></i>
      <p>请先选择一个查询条件</p>
    </div>
    
    <div v-else class="panel-body">
      <!-- 显示模式 -->
      <div class="form-section">
        <label class="form-label">显示模式</label>
        <el-radio-group v-model="localConfig.displayMode" size="small" @change="handleConfigChange">
          <el-radio-button label="auto">自动</el-radio-button>
          <el-radio-button label="custom">自定义</el-radio-button>
        </el-radio-group>
        <div class="form-hint">
          自动模式将根据字段类型推断显示类型
        </div>
      </div>
      
      <!-- 显示类型 -->
      <div class="form-section">
        <label class="form-label">
          显示类型
          <span v-if="localConfig.displayMode === 'auto'" class="auto-badge">自动</span>
        </label>
        <el-select
          v-model="localConfig.displayType"
          :disabled="localConfig.displayMode === 'auto'"
          placeholder="请选择显示类型"
          size="small"
          style="width: 100%"
          @change="handleConfigChange"
        >
          <el-option label="时间选择器" value="time" />
          <el-option label="下拉框" value="dropdown" />
          <el-option label="文本输入" value="text" />
          <el-option label="数字输入" value="number" />
          <el-option label="日期范围" value="range" />
          <el-option label="机构树选择器" value="dept_tree" />
        </el-select>
        
        <div v-if="validationErrors.displayType" class="error-message">
          <i class="el-icon-warning"></i>
          {{ validationErrors.displayType }}
        </div>
      </div>
      
      <!-- 时间粒度 (仅时间类型显示) -->
      <div v-if="isTimeType" class="form-section">
        <label class="form-label">
          时间粒度
          <span class="required">*</span>
        </label>
        <el-select
          v-model="localConfig.timeGranularity"
          placeholder="请选择时间粒度"
          size="small"
          style="width: 100%"
          @change="handleConfigChange"
        >
          <el-option label="年" value="year" />
          <el-option label="月" value="month" />
          <el-option label="日" value="day" />
          <el-option label="小时" value="hour" />
          <el-option label="分钟" value="minute" />
          <el-option label="秒" value="second" />
        </el-select>
        
        <div v-if="validationErrors.timeGranularity" class="error-message">
          <i class="el-icon-warning"></i>
          {{ validationErrors.timeGranularity }}
        </div>
      </div>
      
      <!-- 默认时间范围 (仅时间类型显示) -->
      <div v-if="isTimeType" class="form-section">
        <label class="form-label">默认时间范围</label>
        <el-select
          v-model="localConfig.defaultTimeRange"
          placeholder="请选择默认时间范围"
          size="small"
          style="width: 100%"
          @change="handleConfigChange"
        >
          <el-option label="今天" value="today" />
          <el-option label="本周" value="thisWeek" />
          <el-option label="本月" value="thisMonth" />
          <el-option label="本季度" value="thisQuarter" />
          <el-option label="本年" value="thisYear" />
          <el-option label="最近7天" value="last7Days" />
          <el-option label="最近30天" value="last30Days" />
          <el-option label="自定义范围" value="custom" />
        </el-select>
      </div>
      
      <!-- 自定义范围 (仅选择自定义时显示) -->
      <div v-if="isTimeType && localConfig.defaultTimeRange === 'custom'" class="form-section">
        <label class="form-label">自定义范围</label>
        <div class="custom-range">
          <el-date-picker
            v-model="customRangeStart"
            type="datetime"
            placeholder="开始时间"
            size="small"
            style="width: 100%"
            @change="handleCustomRangeChange"
          />
          <span class="range-separator">至</span>
          <el-date-picker
            v-model="customRangeEnd"
            type="datetime"
            placeholder="结束时间"
            size="small"
            style="width: 100%"
            @change="handleCustomRangeChange"
          />
        </div>
        
        <div v-if="validationErrors.customRange" class="error-message">
          <i class="el-icon-warning"></i>
          {{ validationErrors.customRange }}
        </div>
      </div>
      
      <!-- 默认值 -->
      <div class="form-section">
        <label class="form-label">默认值</label>
        
        <!-- 文本输入 -->
        <el-input
          v-if="localConfig.displayType === 'text'"
          v-model="localConfig.defaultValue"
          placeholder="请输入默认值"
          size="small"
          @change="handleConfigChange"
        />
        
        <!-- 数字输入 -->
        <el-input-number
          v-else-if="localConfig.displayType === 'number'"
          v-model="localConfig.defaultValue"
          placeholder="请输入默认值"
          size="small"
          style="width: 100%"
          @change="handleConfigChange"
        />
        
        <!-- 下拉框 -->
        <el-input
          v-else-if="localConfig.displayType === 'dropdown'"
          v-model="localConfig.defaultValue"
          placeholder="请输入默认值"
          size="small"
          @change="handleConfigChange"
        >
          <template slot="append">
            <el-button icon="el-icon-setting" @click="showDropdownOptions = true">
              选项
            </el-button>
          </template>
        </el-input>
        
        <!-- 时间/范围类型 -->
        <el-input
          v-else
          v-model="localConfig.defaultValue"
          placeholder="使用默认时间范围"
          size="small"
          disabled
        />
        
        <div v-if="validationErrors.defaultValue" class="error-message">
          <i class="el-icon-warning"></i>
          {{ validationErrors.defaultValue }}
        </div>
      </div>
      
      <!-- 必填和可见性 -->
      <div class="form-section">
        <el-checkbox v-model="isRequired" @change="handleRequiredChange">
          必填项
        </el-checkbox>
        <el-checkbox v-model="isVisible" @change="handleVisibleChange">
          默认可见
        </el-checkbox>
      </div>
      
      <!-- 验证错误汇总 -->
      <div v-if="hasValidationErrors" class="validation-summary">
        <div class="summary-header">
          <i class="el-icon-warning"></i>
          <span>配置验证失败</span>
        </div>
        <ul class="error-list">
          <li v-for="(error, key) in validationErrors" :key="key">
            {{ error }}
          </li>
        </ul>
      </div>
    </div>
    
    <!-- 下拉选项配置对话框 -->
    <el-dialog
      title="配置下拉选项"
      :visible.sync="showDropdownOptions"
      width="500px"
      append-to-body
    >
      <el-form label-width="80px">
        <el-form-item label="选项列表">
          <div
            v-for="(option, index) in dropdownOptions"
            :key="index"
            class="option-item"
          >
            <el-input
              v-model="option.label"
              placeholder="显示文本"
              size="small"
              style="width: 45%"
            />
            <el-input
              v-model="option.value"
              placeholder="值"
              size="small"
              style="width: 45%"
            />
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="removeOption(index)"
            />
          </div>
          <el-button
            type="text"
            icon="el-icon-plus"
            @click="addOption"
          >
            添加选项
          </el-button>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showDropdownOptions = false">取消</el-button>
        <el-button type="primary" @click="saveDropdownOptions">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'DisplaySettingsPanel',
  props: {
    selectedCondition: {
      type: Object,
      default: null
    },
    fieldTypes: {
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
      localConfig: {
        displayMode: 'auto',
        displayType: 'text',
        timeGranularity: null,
        defaultTimeRange: null,
        customRangeStart: null,
        customRangeEnd: null,
        defaultValue: null,
        dropdownOptions: []
      },
      isRequired: false,
      isVisible: true,
      customRangeStart: null,
      customRangeEnd: null,
      showDropdownOptions: false,
      dropdownOptions: [],
      isUpdatingFromWatcher: false // 防止循环更新的标志
    }
  },
  computed: {
    isTimeType() {
      return this.localConfig.displayType === 'time' || 
             this.localConfig.displayType === 'range'
    },
    
    hasValidationErrors() {
      return Object.keys(this.validationErrors).length > 0
    }
  },
  watch: {
    selectedCondition: {
      handler(newVal) {
        if (newVal) {
          this.loadConfig()
        }
      },
      immediate: true,
      deep: true
    },

    fieldTypes: {
      handler(newTypes) {
        if (newTypes && newTypes.length > 0 && this.localConfig.displayMode === 'auto') {
          this.isUpdatingFromWatcher = true
          this.autoDetectDisplayType(newTypes[0])
          this.$nextTick(() => {
            this.isUpdatingFromWatcher = false
          })
        }
      },
      immediate: true
    }
  },
  methods: {
    autoDetectDisplayType(field) {
      if (!field) {
        return
      }

      // field 现在是一个对象，包含 { dataType, fieldName, comment }
      const dataType = field.dataType || ''
      const type = dataType.toUpperCase()

      console.log('[DisplaySettingsPanel] autoDetectDisplayType 调用:', field)

      const oldDisplayType = this.localConfig.displayType

      // 机构类型检测（优先级最高）
      if (this.isDeptField()) {
        this.localConfig.displayType = 'dept_tree'
        console.log('[DisplaySettingsPanel] 检测到机构字段，自动设置显示类型为 dept_tree')
      }
      // 时间类型
      else if (type.includes('DATE') || type.includes('TIME') || type.includes('TIMESTAMP') || type === 'YEAR') {
        this.localConfig.displayType = 'time'
      }
      // 数值类型
      else if (type.includes('INT') || type.includes('BIGINT') || type.includes('SMALLINT') ||
               type.includes('TINYINT') || type.includes('DECIMAL') || type.includes('NUMERIC') ||
               type.includes('FLOAT') || type.includes('DOUBLE')) {
        this.localConfig.displayType = 'number'
      }
      // 字符串类型默认为文本输入
      else if (type.includes('CHAR') || type.includes('TEXT') || type.includes('STRING')) {
        this.localConfig.displayType = 'text'
      }
      // 默认文本输入
      else {
        this.localConfig.displayType = 'text'
      }

      console.log('[DisplaySettingsPanel] autoDetectDisplayType 完成:', {
        oldDisplayType,
        newDisplayType: this.localConfig.displayType,
        isUpdatingFromWatcher: this.isUpdatingFromWatcher
      })

      // 只有在 displayType 实际改变时才发送事件
      if (oldDisplayType !== this.localConfig.displayType) {
        console.log('[DisplaySettingsPanel] displayType 发生变化，调用 emitUpdate')
        this.emitUpdate()
      } else {
        console.log('[DisplaySettingsPanel] displayType 未变化，跳过 emitUpdate')
      }
    },

    /**
     * 判断是否为机构字段
     */
    isDeptField() {
      if (!this.selectedCondition || !this.fieldTypes || this.fieldTypes.length === 0) {
        return false
      }

      // 获取第一个关联的字段
      const field = this.fieldTypes[0]
      if (!field) return false

      // 检查字段名
      const fieldName = (field.fieldName || field.dbFieldName || '').toLowerCase()
      const fieldComment = (field.comment || '').toLowerCase()

      // 机构相关的关键词
      const deptKeywords = [
        'org', 'dept', 'department', 'organ', // 英文关键词
        '机构', '部门' // 中文关键词
      ]

      // 检查是否匹配机构关键词
      const isMatchKeyword = deptKeywords.some(keyword =>
        fieldName.includes(keyword) || fieldComment.includes(keyword)
      )

      if (isMatchKeyword) {
        console.log('[DisplaySettingsPanel] 检测到机构字段:', { fieldName, fieldComment })
      }

      return isMatchKeyword
    },

    loadConfig() {
      if (!this.selectedCondition) {
        return
      }

      // 如果是从 watcher 更新的，跳过加载避免循环
      if (this.isUpdatingFromWatcher) {
        return
      }

      // 加载配置
      const config = this.selectedCondition.config || {}

      // 保存当前的 displayType（可能是自动检测的结果）
      const currentDisplayType = this.localConfig.displayType

      this.localConfig = {
        displayMode: config.displayMode || 'auto',
        // 如果是自动模式且有自动检测的结果，保留它
        displayType: (config.displayMode === 'auto' && currentDisplayType) || config.displayType || 'text',
        timeGranularity: config.timeGranularity || null,
        defaultTimeRange: config.defaultTimeRange || null,
        customRangeStart: config.customRangeStart || null,
        customRangeEnd: config.customRangeEnd || null,
        defaultValue: this.selectedCondition.defaultValue || null,
        dropdownOptions: config.dropdownOptions || []
      }

      console.log('[DisplaySettingsPanel] loadConfig:', {
        config,
        currentDisplayType,
        newDisplayType: this.localConfig.displayType
      })

      this.isRequired = this.selectedCondition.isRequired === '1'
      this.isVisible = this.selectedCondition.isVisible === '1'

      // 加载自定义范围
      if (this.localConfig.customRangeStart) {
        this.customRangeStart = new Date(this.localConfig.customRangeStart)
      }
      if (this.localConfig.customRangeEnd) {
        this.customRangeEnd = new Date(this.localConfig.customRangeEnd)
      }

      // 加载下拉选项
      this.dropdownOptions = [...this.localConfig.dropdownOptions]
    },
    
    handleConfigChange() {
      this.emitUpdate()
    },
    
    handleRequiredChange(value) {
      this.$emit('update-required', {
        conditionId: this.selectedCondition.id,
        isRequired: value ? '1' : '0'
      })
    },
    
    handleVisibleChange(value) {
      this.$emit('update-visible', {
        conditionId: this.selectedCondition.id,
        isVisible: value ? '1' : '0'
      })
    },
    
    handleCustomRangeChange() {
      this.localConfig.customRangeStart = this.customRangeStart ? 
        this.customRangeStart.toISOString() : null
      this.localConfig.customRangeEnd = this.customRangeEnd ? 
        this.customRangeEnd.toISOString() : null
      this.emitUpdate()
    },
    
    addOption() {
      this.dropdownOptions.push({ label: '', value: '' })
    },
    
    removeOption(index) {
      this.dropdownOptions.splice(index, 1)
    },
    
    saveDropdownOptions() {
      this.localConfig.dropdownOptions = this.dropdownOptions.filter(
        opt => opt.label && opt.value
      )
      this.showDropdownOptions = false
      this.emitUpdate()
    },
    
    emitUpdate() {
      this.$emit('update-display-config', {
        conditionId: this.selectedCondition.id,
        config: { ...this.localConfig },
        defaultValue: this.localConfig.defaultValue
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.display-settings-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  
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
      
      .required {
        color: #f56c6c;
        margin-left: 4px;
      }
      
      .auto-badge {
        font-size: 11px;
        color: #67c23a;
        background: #f0f9ff;
        padding: 2px 6px;
        border-radius: 2px;
        margin-left: 8px;
      }
    }
    
    .form-hint {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
  
  .custom-range {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .range-separator {
      text-align: center;
      color: #909399;
      font-size: 12px;
    }
  }
  
  .error-message {
    margin-top: 8px;
    padding: 8px;
    background: #fef0f0;
    border: 1px solid #fde2e2;
    border-radius: 4px;
    color: #f56c6c;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
    
    i {
      font-size: 14px;
    }
  }
  
  .validation-summary {
    margin-top: 20px;
    padding: 12px;
    background: #fef0f0;
    border: 1px solid #fde2e2;
    border-radius: 4px;
    
    .summary-header {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #f56c6c;
      font-size: 13px;
      font-weight: 600;
      margin-bottom: 8px;
      
      i {
        font-size: 16px;
      }
    }
    
    .error-list {
      margin: 0;
      padding-left: 24px;
      
      li {
        color: #f56c6c;
        font-size: 12px;
        line-height: 1.8;
      }
    }
  }
  
  .option-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }
}
</style>
