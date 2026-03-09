<template>
  <div class="field-management-panel">
    <div class="panel-header">
      <span class="panel-title">字段管理</span>
    </div>

    <div class="field-sections">
      <!-- 维度字段 -->
      <div class="field-section">
        <div class="section-header" @click="toggleSection('dimension')">
          <i :class="sectionExpanded.dimension ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
          <i class="el-icon-s-grid"></i>
          <span>维度字段</span>
          <span class="field-count">({{ dimensionFields.length }})</span>
        </div>
        <transition name="slide">
          <div v-show="sectionExpanded.dimension" class="field-list">
            <div
              v-for="field in dimensionFields"
              :key="field.name"
              class="field-item"
              draggable="true"
              @dragstart="handleDragStart(field, 'dimension')"
              @dragend="handleDragEnd"
            >
              <i class="el-icon-s-operation"></i>
              <span class="field-name" :title="`${field.comment} (${field.name})`">
                {{ field.comment || field.name }}
              </span>
            </div>
            <div v-if="dimensionFields.length === 0" class="empty-hint">
              暂无维度字段
            </div>
          </div>
        </transition>
      </div>

      <!-- 指标字段 -->
      <div class="field-section">
        <div class="section-header" @click="toggleSection('metric')">
          <i :class="sectionExpanded.metric ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
          <i class="el-icon-s-data"></i>
          <span>指标字段</span>
          <span class="field-count">({{ metricFields.length }})</span>
        </div>
        <transition name="slide">
          <div v-show="sectionExpanded.metric" class="field-list">
            <div
              v-for="field in metricFields"
              :key="field.name"
              class="field-item"
              draggable="true"
              @dragstart="handleDragStart(field, 'metric')"
              @dragend="handleDragEnd"
            >
              <i class="el-icon-s-operation"></i>
              <span class="field-name" :title="`${field.comment} (${field.name})`">
                {{ field.comment || field.name }}
              </span>
            </div>
            <div v-if="metricFields.length === 0" class="empty-hint">
              暂无指标字段
            </div>
          </div>
        </transition>
      </div>

      <!-- 计算字段 -->
      <div class="field-section">
        <div class="section-header" @click="toggleSection('calculated')">
          <i :class="sectionExpanded.calculated ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
          <i class="el-icon-s-operation"></i>
          <span>计算字段</span>
          <span class="field-count">({{ calculatedFields.length }})</span>
        </div>
        <transition name="slide">
          <div v-show="sectionExpanded.calculated">
            <div class="field-list">
              <div
                v-for="field in calculatedFields"
                :key="field.name"
                class="field-item calculated-field"
                draggable="true"
                @dragstart="handleDragStart(field, 'calculated')"
                @dragend="handleDragEnd"
                @click="handleEditField(field)"
              >
                <i class="el-icon-s-operation"></i>
                <i class="el-icon-edit-outline fx-icon"></i>
                <span class="field-name" :title="field.alias">
                  {{ field.alias }}
                </span>
                <el-button
                  type="text"
                  icon="el-icon-delete"
                  class="delete-btn"
                  @click.stop="handleDeleteField(field)"
                ></el-button>
              </div>
              <div v-if="calculatedFields.length === 0" class="empty-hint">
                暂无计算字段
              </div>
            </div>
            <el-button
              type="primary"
              size="small"
              icon="el-icon-plus"
              class="add-field-btn"
              @click="handleAddField"
            >
              新建计算字段
            </el-button>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FieldManagementPanel',
  props: {
    datasetId: {
      type: Number,
      default: null
    },
    datasetFields: {
      type: Array,
      default: () => []
    },
    calculatedFields: {
      type: Array,
      default: () => []
    },
    chartType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      sectionExpanded: {
        dimension: true,
        metric: true,
        calculated: true
      }
    }
  },
  computed: {
    dimensionFields() {
      return this.datasetFields.filter(field => field.fieldType === 'dimension');
    },
    metricFields() {
      return this.datasetFields.filter(field => field.fieldType === 'metric');
    }
  },
  watch: {
    calculatedFields: {
      handler(newVal) {
        console.log('[FieldManagementPanel] calculatedFields 变化:', newVal)
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    toggleSection(section) {
      this.sectionExpanded[section] = !this.sectionExpanded[section]
    },
    handleDragStart(field, type) {
      console.log('[FieldManagementPanel] handleDragStart - field:', field, 'type:', type)
      const dragData = {
        field: field,
        type: type,
        source: 'fieldManagement'
      };
      console.log('[FieldManagementPanel] 发送 field-drag-start 事件:', dragData)
      this.$emit('field-drag-start', dragData);
      // Store in dataTransfer for compatibility
      event.dataTransfer.effectAllowed = 'copy';
      event.dataTransfer.setData('text/plain', JSON.stringify(dragData));
    },
    handleDragEnd() {
      this.$emit('field-drag-end');
    },
    handleAddField() {
      this.$emit('add-calculated-field');
    },
    handleEditField(field) {
      this.$emit('edit-calculated-field', field);
    },
    handleDeleteField(field) {
      this.$confirm(`确定要删除计算字段"${field.alias}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('delete-calculated-field', field);
      }).catch(() => {});
    }
  }
};
</script>

<style scoped lang="scss">
.field-management-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;

  .panel-header {
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    background: #f5f7fa;

    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }

  .field-sections {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;
  }

  .field-section {
    margin-bottom: 8px;

    .section-header {
      padding: 8px 16px;
      font-size: 13px;
      font-weight: 600;
      color: #606266;
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      user-select: none;
      transition: background 0.2s;

      &:hover {
        background: #f5f7fa;
      }

      i {
        font-size: 14px;
        transition: transform 0.3s;
      }

      .field-count {
        margin-left: auto;
        font-size: 12px;
        color: #909399;
        font-weight: normal;
      }
    }

    .field-list {
      padding: 0 8px;
    }

    .field-item {
      padding: 8px 12px;
      margin: 4px 0;
      border-radius: 4px;
      cursor: move;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s;
      background: #f9fafc;
      border: 1px solid #e4e7ed;

      &:hover {
        background: #ecf5ff;
        border-color: #409eff;
      }

      i {
        font-size: 12px;
        color: #909399;
      }

      .field-name {
        flex: 1;
        font-size: 13px;
        color: #606266;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      &.calculated-field {
        cursor: pointer;
        position: relative;

        .fx-icon {
          color: #409eff;
          font-weight: bold;
        }

        .delete-btn {
          opacity: 0;
          padding: 0;
          font-size: 14px;
          color: #f56c6c;
          transition: opacity 0.2s;
        }

        &:hover .delete-btn {
          opacity: 1;
        }
      }
    }

    .empty-hint {
      padding: 16px 12px;
      text-align: center;
      font-size: 12px;
      color: #909399;
    }

    .add-field-btn {
      width: calc(100% - 16px);
      margin: 8px 8px 0;
    }
  }
}

/* 展开/折叠动画 */
.slide-enter-active, .slide-leave-active {
  transition: all 0.3s ease;
  max-height: 1000px;
  overflow: hidden;
}

.slide-enter, .slide-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>
