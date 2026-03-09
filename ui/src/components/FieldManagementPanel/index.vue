<template>
  <div class="field-management-panel">
    <div class="panel-header">
      <div class="panel-header__main">
        <span class="panel-title">字段管理</span>
        <span class="panel-subtitle">拖拽字段到左侧配置区</span>
      </div>
      <span class="panel-total">{{ dimensionFields.length + metricFields.length + calculatedFields.length }}</span>
    </div>

    <div class="field-sections">
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
        console.log('[FieldManagementPanel] calculatedFields 鍙樺寲:', newVal)
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
      console.log('[FieldManagementPanel] 鍙戦€?field-drag-start 浜嬩欢:', dragData)
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
      this.$confirm(`纭畾瑕佸垹闄よ绠楀瓧娈?${field.alias}"鍚?`, '鎻愮ず', {
        confirmButtonText: '纭畾',
        cancelButtonText: '鍙栨秷',
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
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    border-bottom: 1px solid #dfe5ee;
    background: #f8fafc;

    .panel-header__main {
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .panel-title {
      font-size: 13px;
      font-weight: 600;
      color: #1f2d3d;
      line-height: 1.2;
    }

    .panel-subtitle {
      font-size: 12px;
      color: #5b6b7c;
      line-height: 1.2;
      display: none;
    }

    .panel-total {
      min-width: 26px;
      height: 22px;
      padding: 0 6px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #d5dfee;
      border-radius: 11px;
      background: #fff;
      color: #31548f;
      font-size: 12px;
      font-weight: 600;
    }
  }

  .field-sections {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 6px;
  }

  .field-section {
    margin-bottom: 6px;
    border: 1px solid #e3e9f2;
    border-radius: 8px;
    background: #fff;

    .section-header {
      padding: 7px 8px;
      font-size: 12px;
      font-weight: 600;
      color: #3c4e67;
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      user-select: none;
      transition: background-color 0.18s;

      &:hover {
        background: #f7faff;
      }

      i {
        font-size: 12px;
        color: #6f8198;
      }

      .field-count {
        margin-left: auto;
        font-size: 12px;
        color: #7b8da5;
        font-weight: 500;
      }
    }

    .field-list {
      padding: 0 4px 6px;
    }

    .field-item {
      padding: 6px 6px;
      margin: 5px 0 0;
      border-radius: 6px;
      cursor: move;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: border-color 0.18s, background-color 0.18s;
      background: #f9fbff;
      border: 1px solid #dfe7f3;

      &:hover {
        background: #edf4ff;
        border-color: #b8cbf0;
      }

      i {
        font-size: 11px;
        color: #7e90a8;
      }

      .field-name {
        flex: 1;
        font-size: 12px;
        color: #3f526b;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      &.calculated-field {
        cursor: pointer;

        .fx-icon {
          color: #2f6fed;
          font-weight: bold;
        }

        .delete-btn {
          opacity: 0;
          padding: 0;
          font-size: 12px;
          color: #f56c6c;
          transition: opacity 0.18s;
        }

        &:hover .delete-btn {
          opacity: 1;
        }
      }
    }

    .empty-hint {
      padding: 12px 10px;
      text-align: center;
      font-size: 12px;
      color: #8fa0b6;
    }

    .add-field-btn {
      width: calc(100% - 12px);
      margin: 6px;
      border-radius: 6px;
    }
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: max-height 0.18s ease, opacity 0.18s ease;
  max-height: 1000px;
  overflow: hidden;
}

.slide-enter,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>


