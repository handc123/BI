<template>
  <div class="query-widget" :style="widgetStyle">
    <div class="query-conditions" :class="layoutClass">
      <!-- 时间条件 -->
      <div
        v-for="condition in visibleConditions"
        :key="condition.id"
        class="condition-item"
        :class="{ 'is-required': condition.isRequired === '1' }"
      >
        <label class="condition-label">
          {{ condition.conditionName }}
          <span v-if="condition.isRequired === '1'" class="required-mark">*</span>
        </label>

        <!-- 时间选择器 -->
        <el-date-picker
          v-if="condition.conditionType === 'time'"
          v-model="conditionValues[condition.id]"
          :type="getTimePickerType(condition)"
          :format="getTimeFormat(condition)"
          :value-format="getTimeFormat(condition)"
          :placeholder="'请选择' + condition.conditionName"
          :clearable="condition.isRequired !== '1'"
          @change="handleConditionChange(condition)"
          class="condition-input"
        />

        <!-- 下拉选择器(单选) -->
        <el-select
          v-else-if="condition.conditionType === 'dropdown' && !isMultiple(condition)"
          v-model="conditionValues[condition.id]"
          :placeholder="'请选择' + condition.conditionName"
          :clearable="condition.isRequired !== '1'"
          :loading="loadingOptions[condition.id]"
          @change="handleConditionChange(condition)"
          class="condition-input"
        >
          <el-option
            v-for="option in conditionOptions[condition.id]"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <!-- 下拉选择器(多选) -->
        <el-select
          v-else-if="condition.conditionType === 'dropdown' && isMultiple(condition)"
          v-model="conditionValues[condition.id]"
          multiple
          :placeholder="'请选择' + condition.conditionName"
          :clearable="condition.isRequired !== '1'"
          :loading="loadingOptions[condition.id]"
          @change="handleConditionChange(condition)"
          class="condition-input"
        >
          <el-option
            v-for="option in conditionOptions[condition.id]"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <!-- 单选框组 -->
        <el-radio-group
          v-else-if="condition.conditionType === 'radio'"
          v-model="conditionValues[condition.id]"
          @change="handleConditionChange(condition)"
          class="condition-input"
        >
          <el-radio
            v-for="option in conditionOptions[condition.id]"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </el-radio>
        </el-radio-group>

        <!-- 多选框组 -->
        <el-checkbox-group
          v-else-if="condition.conditionType === 'checkbox'"
          v-model="conditionValues[condition.id]"
          @change="handleConditionChange(condition)"
          class="condition-input"
        >
          <el-checkbox
            v-for="option in conditionOptions[condition.id]"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </el-checkbox>
        </el-checkbox-group>

        <!-- 机构树选择器 -->
        <div v-else-if="condition.conditionType === 'dept_tree'" class="condition-input dept-tree-input">
          <el-select
            v-model="conditionValues[condition.id]"
            :placeholder="'请选择' + condition.conditionName"
            :clearable="condition.isRequired !== '1'"
            :loading="loadingDeptTree[condition.id]"
            :popper-append-to-body="true"
            @change="handleDeptTreeChange(condition)"
            class="dept-tree-select"
          >
            <el-option
              v-for="item in flattenDeptTree(deptTreeData[condition.id] || [])"
              :key="item.deptId"
              :label="item.deptName"
              :value="item.deptId"
            >
              <span>{{ item.deptName }}</span>
              <span v-if="item.parentId !== 0" style="color: #8492a6; font-size: 12px; margin-left: 8px;">
                {{ item.parentName }}
              </span>
            </el-option>
          </el-select>
        </div>

        <!-- 数值范围 -->
        <div v-else-if="condition.conditionType === 'range'" class="condition-input range-input">
          <el-input-number
            v-model="conditionValues[condition.id].min"
            :placeholder="'最小值'"
            :min="getRangeMin(condition)"
            :max="getRangeMax(condition)"
            @change="handleConditionChange(condition)"
            controls-position="right"
          />
          <span class="range-separator">-</span>
          <el-input-number
            v-model="conditionValues[condition.id].max"
            :placeholder="'最大值'"
            :min="getRangeMin(condition)"
            :max="getRangeMax(condition)"
            @change="handleConditionChange(condition)"
            controls-position="right"
          />
        </div>
      </div>

      <!-- 查询按钮 -->
      <div class="condition-item query-actions" v-if="showQueryButton">
        <el-button type="primary" @click="handleQuery" :loading="querying">
          查询
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getCascadeOptions } from '@/api/bi/query'
import request from '@/utils/request'

export default {
  name: 'QueryWidget',
  props: {
    // 查询条件列表
    conditions: {
      type: Array,
      default: () => []
    },
    // 样式配置
    styleConfig: {
      type: Object,
      default: () => ({
        layout: 'horizontal',
        spacing: 10
      })
    },
    // 是否显示查询按钮
    showQueryButton: {
      type: Boolean,
      default: true
    },
    // 是否编辑模式
    isEditMode: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      conditionValues: {},      // 条件值
      conditionOptions: {},     // 条件选项
      loadingOptions: {},       // 加载选项状态
      querying: false,          // 查询中状态
      deptTreeData: {},         // 机构树数据
      loadingDeptTree: {},      // 机构树加载状态
      deptTreeProps: {          // 机构树属性配置
        label: 'deptName',
        children: 'children',
        value: 'deptId'
      }
    }
  },
  computed: {
    // 可见的条件
    visibleConditions() {
      return this.conditions.filter(c => c.isVisible === '1')
        .sort((a, b) => a.displayOrder - b.displayOrder)
    },
    // 布局类名
    layoutClass() {
      return `layout-${this.styleConfig.layout || 'horizontal'}`
    },
    // 组件样式
    widgetStyle() {
      return {
        '--spacing': `${this.styleConfig.spacing || 10}px`
      }
    }
  },
  watch: {
    conditions: {
      handler(newVal) {
        this.initializeConditions()
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    /**
     * 初始化条件
     */
    initializeConditions() {
      console.log('[QueryWidget] initializeConditions 被调用，条件数量:', this.conditions.length)
      console.log('[QueryWidget] 所有条件的详细信息:', this.conditions.map(c => ({
        id: c.id,
        name: c.conditionName,
        conditionType: c.conditionType,
        isVisible: c.isVisible,
        config: c.config
      })))

      this.conditions.forEach(condition => {
        console.log('[QueryWidget] 处理条件:', {
          id: condition.id,
          name: condition.conditionName,
          conditionType: condition.conditionType,
          isVisible: condition.isVisible
        })

        // 初始化条件值
        if (!(condition.id in this.conditionValues)) {
          this.$set(this.conditionValues, condition.id, this.getDefaultValue(condition))
        }

        // 加载选项
        if (this.needsOptions(condition)) {
          this.loadOptions(condition)
        }

        // 加载机构树数据
        if (condition.conditionType === 'dept_tree') {
          console.log('[QueryWidget] ✅ 检测到 dept_tree 类型，开始加载机构树')
          this.loadDeptTree(condition)
        } else {
          console.log('[QueryWidget] ❌ 不是 dept_tree 类型，当前类型:', condition.conditionType)
        }
      })
    },

    /**
     * 获取默认值
     */
    getDefaultValue(condition) {
      // 对于时间类型，不使用defaultValue（会根据配置动态计算）
      if (condition.conditionType === 'time' || condition.conditionType === 'range') {
        return null
      }

      // 对于数值范围类型，返回对象
      if (condition.conditionType === 'range') {
        return { min: null, max: null }
      }

      // 对于多选类型，返回数组
      if (condition.conditionType === 'checkbox' ||
          (condition.conditionType === 'dropdown' && this.isMultiple(condition))) {
        if (condition.defaultValue) {
          try {
            const parsed = JSON.parse(condition.defaultValue)
            return Array.isArray(parsed) ? parsed : []
          } catch (e) {
            return []
          }
        }
        return []
      }

      // 对于其他类型，尝试解析defaultValue
      if (condition.defaultValue) {
        try {
          const parsed = JSON.parse(condition.defaultValue)
          // 如果解析结果是对象且不是数组，返回null避免日期选择器错误
          if (typeof parsed === 'object' && !Array.isArray(parsed)) {
            return null
          }
          return parsed
        } catch (e) {
          // 如果不是JSON，直接返回字符串值
          return condition.defaultValue
        }
      }

      return null
    },

    /**
     * 判断是否需要加载选项
     */
    needsOptions(condition) {
      return ['dropdown', 'radio', 'checkbox'].includes(condition.conditionType)
    },

    /**
     * 判断是否多选
     */
    isMultiple(condition) {
      const config = this.parseConfig(condition)
      return config.multiple === true
    },

    /**
     * 解析配置
     */
    parseConfig(condition) {
      if (!condition.config) return {}
      try {
        return typeof condition.config === 'string' 
          ? JSON.parse(condition.config) 
          : condition.config
      } catch (e) {
        console.error('解析条件配置失败:', e)
        return {}
      }
    },

    /**
     * 加载选项
     */
    async loadOptions(condition) {
      const config = this.parseConfig(condition)
      
      // 如果是静态选项
      if (config.options && Array.isArray(config.options)) {
        this.$set(this.conditionOptions, condition.id, config.options)
        return
      }
      
      // 如果是API数据源
      if (config.dataSource && config.dataSource.type === 'api') {
        await this.loadOptionsFromApi(condition)
      }
    },

    /**
     * 从API加载选项
     */
    async loadOptionsFromApi(condition) {
      const config = this.parseConfig(condition)
      const dataSource = config.dataSource
      
      this.$set(this.loadingOptions, condition.id, true)
      
      try {
        // 如果有父条件,检查父条件值
        if (condition.parentConditionId) {
          const parentValue = this.conditionValues[condition.parentConditionId]
          if (!parentValue) {
            this.$set(this.conditionOptions, condition.id, [])
            return
          }
          
          // 加载级联选项
          const response = await getCascadeOptions(condition.id, this.conditionValues)
          this.$set(this.conditionOptions, condition.id, response.data || [])
        } else {
          // 加载普通选项
          const response = await this.$axios({
            url: dataSource.url,
            method: 'get',
            params: dataSource.params || {}
          })
          
          const data = response.data || []
          const options = data.map(item => ({
            label: item[dataSource.labelField || 'label'],
            value: item[dataSource.valueField || 'value']
          }))
          
          this.$set(this.conditionOptions, condition.id, options)
        }
      } catch (error) {
        console.error('加载选项失败:', error)
        this.$message.error('加载选项失败')
        this.$set(this.conditionOptions, condition.id, [])
      } finally {
        this.$set(this.loadingOptions, condition.id, false)
      }
    },

    /**
     * 处理条件变化
     */
    handleConditionChange(condition) {
      // 如果有子条件,重新加载子条件选项
      const childConditions = this.conditions.filter(c => c.parentConditionId === condition.id)
      childConditions.forEach(child => {
        // 清空子条件值
        this.$set(this.conditionValues, child.id, this.getDefaultValue(child))
        // 重新加载选项
        this.loadOptions(child)
      })

      // 只在非编辑模式下才更新Vuex store中的查询值
      // 编辑模式下，只在点击查询按钮时才更新
      // if (!this.isEditMode) {
      //   this.$store.dispatch('dashboard/updateQueryValue', {
      //     conditionId: condition.id,
      //     value: this.conditionValues[condition.id]
      //   })
      // }

      // 触发条件变化事件（不自动查询）
      this.$emit('condition-change', {
        conditionId: condition.id,
        value: this.conditionValues[condition.id],
        allValues: this.conditionValues
      })

      // 如果不显示查询按钮,自动触发查询
      if (!this.showQueryButton) {
        this.handleQuery()
      }
    },

    /**
     * 处理查询
     */
    handleQuery() {
      // 验证必填项
      const missingRequired = this.visibleConditions
        .filter(c => c.isRequired === '1')
        .find(c => {
          const value = this.conditionValues[c.id]
          return value === null || value === undefined || value === '' || 
                 (Array.isArray(value) && value.length === 0)
        })
      
      if (missingRequired) {
        this.$message.warning(`请填写必填项: ${missingRequired.conditionName}`)
        return
      }
      
      // 更新Vuex store中的所有查询值
      if (!this.isEditMode) {
        this.$store.dispatch('dashboard/updateQueryValues', this.conditionValues)
      }
      
      this.$emit('query', this.conditionValues)
    },

    /**
     * 处理重置
     */
    handleReset() {
      this.conditions.forEach(condition => {
        this.$set(this.conditionValues, condition.id, this.getDefaultValue(condition))
      })
      this.$emit('reset')
    },

    /**
     * 获取时间选择器类型
     */
    getTimePickerType(condition) {
      const config = this.parseConfig(condition)
      const granularity = config.granularity || 'day'
      
      const typeMap = {
        'day': 'date',
        'week': 'week',
        'month': 'month',
        'quarter': 'month',
        'year': 'year'
      }
      
      return typeMap[granularity] || 'date'
    },

    /**
     * 获取时间格式
     */
    getTimeFormat(condition) {
      const config = this.parseConfig(condition)
      return config.format || 'yyyy-MM-dd'
    },

    /**
     * 获取范围最小值
     */
    getRangeMin(condition) {
      const config = this.parseConfig(condition)
      return config.min !== undefined ? config.min : -Infinity
    },

    /**
     * 获取范围最大值
     */
    getRangeMax(condition) {
      const config = this.parseConfig(condition)
      return config.max !== undefined ? config.max : Infinity
    },

    /**
     * 获取当前条件值
     */
    getConditionValues() {
      return { ...this.conditionValues }
    },

    /**
     * 设置条件值
     */
    setConditionValues(values) {
      Object.keys(values).forEach(key => {
        this.$set(this.conditionValues, key, values[key])
      })
    },

    /**
     * 将机构树扁平化为数组（用于下拉框显示）
     */
    flattenDeptTree(tree) {
      if (!tree || !Array.isArray(tree)) return []

      const result = []
      const flatten = (nodes, parentName = '') => {
        nodes.forEach(node => {
          // 兼容多种 API 返回格式
          const deptId = node.deptId || node.id
          const deptName = node.deptName || node.label
          const parentId = node.parentId || (node.parent ? node.parent.id : 0)

          if (deptId && deptName) {
            result.push({
              deptId: deptId,
              deptName: deptName,
              parentId: parentId,
              parentName: parentName
            })
          }

          if (node.children && Array.isArray(node.children)) {
            flatten(node.children, deptName)
          }
        })
      }

      flatten(tree)
      return result
    },

    /**
     * 加载机构树数据
     */
    async loadDeptTree(condition) {
      console.log('[QueryWidget] 开始加载机构树，conditionId:', condition.id)
      this.$set(this.loadingDeptTree, condition.id, true)

      try {
        const response = await request({
          url: '/system/user/deptTree',
          method: 'get'
        })

        console.log('[QueryWidget] 机构树API响应:', response)
        console.log('[QueryWidget] 机构树原始数据:', response.data)
        console.log('[QueryWidget] 机构树原始数据[0]:', response.data[0])

        const treeData = response.data || []
        this.$set(this.deptTreeData, condition.id, treeData)

        const flattened = this.flattenDeptTree(treeData)
        console.log('[QueryWidget] 机构树已保存，扁平化后:', flattened)
        console.log('[QueryWidget] 扁平化数据长度:', flattened.length)
      } catch (error) {
        console.error('[QueryWidget] 加载机构树失败:', error)
        this.$message.error('加载机构树失败')
        this.$set(this.deptTreeData, condition.id, [])
      } finally {
        this.$set(this.loadingDeptTree, condition.id, false)
      }
    },

    /**
     * 处理机构树变化
     */
    handleDeptTreeChange(condition) {
      this.$emit('condition-change', {
        conditionId: condition.id,
        value: this.conditionValues[condition.id],
        allValues: this.conditionValues
      })

      // 如果不显示查询按钮,自动触发查询
      if (!this.showQueryButton) {
        this.handleQuery()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.query-widget {
  width: 100%;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
  box-sizing: border-box;
}

.query-conditions {
  display: flex;
  gap: var(--spacing, 10px);
  align-items: center;
  flex-wrap: wrap;

  &.layout-horizontal {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
  }

  &.layout-vertical {
    flex-direction: column;
    align-items: stretch;
  }
}

.condition-item {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;

  &.is-required {
    .condition-label {
      font-weight: 500;
    }
  }

  &.query-actions {
    flex-direction: row;
    align-items: center;
    gap: 10px;
  }
}

.condition-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;

  .required-mark {
    color: #f56c6c;
    margin-left: 2px;
  }
}

.condition-input {
  min-width: 160px;

  &.dept-tree-input {
    // 确保机构树选择器有足够的空间显示
    flex: 1;
    min-width: 200px;
    position: relative;
    z-index: 1;
  }

  &.range-input {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-input-number {
      flex: 1;
      min-width: 100px;
    }

    .range-separator {
      color: #909399;
    }
  }
}

// 垂直布局时的样式调整
.layout-vertical {
  .condition-item {
    width: 100%;
  }

  .condition-input {
    width: 100%;
  }
}

// Element UI 组件尺寸调整
.query-widget {
  ::v-deep .el-date-picker,
  ::v-deep .el-input-number,
  ::v-deep .el-input,
  ::v-deep .el-select {
    width: 160px;
  }

  ::v-deep .el-tree-select {
    width: 200px;
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-input-number__increase,
  ::v-deep .el-input-number__decrease {
    height: 28px;
  }

  ::v-deep .el-date-picker .el-input__inner {
    padding: 0 10px;
  }
}
</style>
