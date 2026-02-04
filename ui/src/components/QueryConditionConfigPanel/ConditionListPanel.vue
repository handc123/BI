<template>
  <div class="condition-list-panel">
    <div class="panel-header">
      <span class="panel-title">查询条件列表</span>
      <el-button
        type="primary"
        icon="el-icon-plus"
        size="mini"
        @click="handleAdd"
      >
        添加
      </el-button>
    </div>
    
    <div class="panel-body">
      <draggable
        v-model="localConditions"
        :options="dragOptions"
        class="condition-list"
        @end="handleDragEnd"
      >
        <div
          v-for="condition in localConditions"
          :key="condition.id"
          :class="['condition-item', {
            'is-selected': isSelected(condition),
            'has-error': hasValidationError(condition.id)
          }]"
          @click="handleSelect(condition)"
        >
          <div class="condition-content">
            <i class="el-icon-menu drag-handle"></i>

            <div v-if="editingId === condition.id" class="condition-name-edit">
              <el-input
                :ref="`nameInput_${condition.id}`"
                v-model="editingName"
                size="mini"
                @blur="handleNameBlur"
                @keyup.enter.native="handleNameBlur"
              />
            </div>
            <div v-else class="condition-name" @dblclick="handleNameEdit(condition)">
              <span>{{ condition.conditionName }}</span>
              <i v-if="hasValidationError(condition.id)" class="el-icon-warning-outline error-icon"></i>
            </div>

            <el-button
              type="text"
              icon="el-icon-delete"
              size="mini"
              class="delete-btn"
              @click.stop="handleDelete(condition)"
            />
          </div>

          <!-- 验证错误提示 -->
          <div v-if="hasValidationError(condition.id)" class="condition-error-hint">
            <i class="el-icon-warning"></i>
            {{ getFirstError(condition.id) }}
          </div>
        </div>
      </draggable>
      
      <div v-if="localConditions.length === 0" class="empty-state">
        <i class="el-icon-document-add"></i>
        <p>暂无查询条件</p>
        <p class="empty-hint">点击"添加"按钮创建新条件</p>
      </div>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable'

export default {
  name: 'ConditionListPanel',
  components: {
    draggable
  },
  props: {
    conditions: {
      type: Array,
      default: () => []
    },
    selectedCondition: {
      type: Object,
      default: null
    },
    validationErrors: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      editingId: null,
      editingName: '',
      dragOptions: {
        animation: 200,
        handle: '.drag-handle',
        ghostClass: 'ghost'
      }
    }
  },
  computed: {
    localConditions: {
      get() {
        return this.conditions
      },
      set(value) {
        this.$emit('update:conditions', value)
      }
    }
  },
  methods: {
    handleAdd() {
      this.$emit('add-condition')
    },
    
    handleSelect(condition) {
      this.$emit('select-condition', condition)
    },
    
    handleDelete(condition) {
      this.$confirm('确定要删除该查询条件吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('delete-condition', condition.id)
      }).catch(() => {})
    },
    
    handleDragEnd() {
      // 更新显示顺序
      const orders = this.localConditions.map((condition, index) => ({
        id: condition.id,
        displayOrder: index + 1
      }))
      this.$emit('reorder-conditions', orders)
    },
    
    handleNameEdit(condition) {
      this.editingId = condition.id
      this.editingName = condition.conditionName
      this.$nextTick(() => {
        // 使用动态 ref 查找对应的输入框
        const inputRef = this.$refs[`nameInput_${condition.id}`]
        if (inputRef) {
          // Element UI 的 input 组件需要调用 focus 方法或访问内部 input 元素
          if (typeof inputRef.focus === 'function') {
            inputRef.focus()
          } else if (inputRef.$el) {
            const input = inputRef.$el.querySelector('input')
            if (input) {
              input.focus()
            }
          }
        }
      })
    },
    
    handleNameBlur() {
      if (this.editingId && this.editingName.trim()) {
        this.$emit('update-condition-name', {
          id: this.editingId,
          name: this.editingName.trim()
        })
      }
      this.editingId = null
      this.editingName = ''
    },
    
    isSelected(condition) {
      return this.selectedCondition && this.selectedCondition.id === condition.id
    },

    hasValidationError(conditionId) {
      return this.validationErrors[conditionId] &&
             Object.keys(this.validationErrors[conditionId]).length > 0
    },

    getFirstError(conditionId) {
      const errors = this.validationErrors[conditionId]
      if (!errors) {
        return ''
      }
      // 返回第一个错误信息
      const firstKey = Object.keys(errors)[0]
      return errors[firstKey] || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.condition-list-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  
  .panel-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .panel-body {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
  }
  
  .condition-list {
    min-height: 100px;
  }
  
  .condition-item {
    padding: 12px;
    margin-bottom: 8px;
    background: #f5f7fa;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #ecf5ff;
      border-color: #409eff;
    }

    &.is-selected {
      background: #ecf5ff;
      border-color: #409eff;
      box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
    }

    &.has-error {
      border-color: #f56c6c;
      background: #fef0f0;
    }

    &.ghost {
      opacity: 0.5;
      background: #c6e2ff;
    }
  }

  .condition-content {
    display: flex;
    align-items: center;
    gap: 8px;

    .drag-handle {
      cursor: move;
      color: #909399;
      font-size: 16px;

      &:hover {
        color: #606266;
      }
    }

    .condition-name {
      flex: 1;
      font-size: 14px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      display: flex;
      align-items: center;
      gap: 4px;

      .error-icon {
        color: #f56c6c;
        font-size: 16px;
        flex-shrink: 0;
      }
    }

    .condition-name-edit {
      flex: 1;
    }

    .delete-btn {
      color: #f56c6c;
      padding: 0;

      &:hover {
        color: #f56c6c;
        background: transparent;
      }
    }
  }

  .condition-error-hint {
    margin-top: 8px;
    padding: 4px 8px;
    background: #fef0f0;
    border-radius: 2px;
    color: #f56c6c;
    font-size: 11px;
    display: flex;
    align-items: center;
    gap: 4px;

    i {
      font-size: 12px;
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: #909399;
    
    i {
      font-size: 48px;
      margin-bottom: 16px;
      display: block;
    }
    
    p {
      margin: 8px 0;
      font-size: 14px;
    }
    
    .empty-hint {
      font-size: 12px;
      color: #c0c4cc;
    }
  }
}
</style>
