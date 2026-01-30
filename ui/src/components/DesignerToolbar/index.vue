<template>
  <div class="designer-toolbar">
    <div class="toolbar-left">
      <!-- 返回按钮 -->
      <el-button
        size="small"
        icon="el-icon-back"
        @click="handleBack"
      >
        返回
      </el-button>

      <el-divider direction="vertical"></el-divider>

      <!-- 仪表板名称编辑 -->
      <div class="dashboard-name-editor">
        <el-input
          v-if="isEditingName"
          v-model="localDashboardName"
          size="small"
          class="name-input"
          @blur="handleNameBlur"
          @keyup.enter.native="handleNameBlur"
          ref="nameInput"
        />
        <div v-else class="dashboard-name" @click="handleNameClick">
          <span class="name-text">{{ dashboardName || '未命名仪表板' }}</span>
          <i class="el-icon-edit-outline edit-icon"></i>
        </div>
      </div>

      <el-divider direction="vertical"></el-divider>

      <!-- 撤销/重做 -->
      <el-button-group>
        <el-button
          size="small"
          icon="el-icon-refresh-left"
          :disabled="!canUndo"
          @click="handleUndo"
          title="撤销 (Ctrl+Z)"
        >
          撤销
        </el-button>
        <el-button
          size="small"
          icon="el-icon-refresh-right"
          :disabled="!canRedo"
          @click="handleRedo"
          title="重做 (Ctrl+Y)"
        >
          重做
        </el-button>
      </el-button-group>
    </div>

    <div class="toolbar-center">
      <!-- 组件库按钮 -->
      <el-button
        size="small"
        icon="el-icon-plus"
        type="primary"
        @click="handleOpenComponentLibrary"
      >
        组件库
      </el-button>
    </div>

    <div class="toolbar-right">
      <!-- 预览按钮 -->
      <el-button
        size="small"
        icon="el-icon-view"
        @click="handlePreview"
      >
        预览
      </el-button>

      <!-- 保存按钮 -->
      <el-button
        size="small"
        icon="el-icon-document"
        type="success"
        @click="handleSave"
      >
        保存
      </el-button>

      <!-- 发布按钮 -->
      <el-button
        size="small"
        icon="el-icon-upload2"
        type="warning"
        @click="handlePublish"
      >
        发布
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DesignerToolbar',
  props: {
    dashboardName: {
      type: String,
      default: ''
    },
    canUndo: {
      type: Boolean,
      default: false
    },
    canRedo: {
      type: Boolean,
      default: false
    },
    hasUnsavedChanges: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isEditingName: false,
      localDashboardName: ''
    }
  },
  watch: {
    dashboardName: {
      immediate: true,
      handler(val) {
        this.localDashboardName = val
      }
    }
  },
  mounted() {
    // 添加键盘快捷键
    document.addEventListener('keydown', this.handleKeyDown)
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.handleKeyDown)
  },
  methods: {
    
    // 返回
    handleBack() {
      if (this.hasUnsavedChanges) {
        this.$confirm('当前有未保存的更改，确定要离开吗？', '提示', {
          confirmButtonText: '离开',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$emit('back')
        }).catch(() => {})
      } else {
        this.$emit('back')
      }
    },

    // 仪表板名称编辑
    handleNameClick() {
      this.isEditingName = true
      this.$nextTick(() => {
        this.$refs.nameInput.focus()
        this.$refs.nameInput.select()
      })
    },
    handleNameBlur() {
      this.isEditingName = false
      if (this.localDashboardName !== this.dashboardName) {
        if (!this.localDashboardName.trim()) {
          this.$message.warning('仪表板名称不能为空')
          this.localDashboardName = this.dashboardName
          return
        }
        this.$emit('name-change', this.localDashboardName.trim())
      }
    },

    // 撤销/重做
    handleUndo() {
      this.$emit('undo')
    },
    handleRedo() {
      this.$emit('redo')
    },

    // 组件库
    handleOpenComponentLibrary() {
      this.$emit('open-component-library')
    },

    // 预览
    handlePreview() {
      this.$emit('preview')
    },

    // 保存
    handleSave() {
      this.$emit('save')
    },

    // 发布
    handlePublish() {
      this.$emit('publish')
    },

    // 键盘快捷键
    handleKeyDown(event) {
      // Ctrl+Z: 撤销
      if (event.ctrlKey && event.key === 'z' && !event.shiftKey) {
        event.preventDefault()
        if (this.canUndo) {
          this.handleUndo()
        }
      }
      // Ctrl+Y 或 Ctrl+Shift+Z: 重做
      if ((event.ctrlKey && event.key === 'y') || 
          (event.ctrlKey && event.shiftKey && event.key === 'z')) {
        event.preventDefault()
        if (this.canRedo) {
          this.handleRedo()
        }
      }
      // Ctrl+S: 保存
      if (event.ctrlKey && event.key === 's') {
        event.preventDefault()
        this.handleSave()
      }
    }
  }
}
</script>

<style scoped>
.designer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-left {
  flex: 1;
}

.toolbar-center {
  flex: 0 0 auto;
}

.toolbar-right {
  flex: 1;
  justify-content: flex-end;
}

.el-divider--vertical {
  height: 24px;
  margin: 0 8px;
}

/* 仪表板名称编辑 */
.dashboard-name-editor {
  display: flex;
  align-items: center;
}

.dashboard-name {
  display: flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.dashboard-name:hover {
  background-color: #f5f7fa;
}

.name-text {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.edit-icon {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.name-input {
  width: 300px;
}

.name-input >>> .el-input__inner {
  font-size: 16px;
  font-weight: 500;
}

/* 按钮样式调整 */
.el-button-group {
  display: flex;
}

.el-button + .el-button {
  margin-left: 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .toolbar-left,
  .toolbar-right {
    flex: 0 0 auto;
  }

  .name-text {
    max-width: 200px;
  }

  .name-input {
    width: 200px;
  }
}

@media (max-width: 768px) {
  .designer-toolbar {
    flex-wrap: wrap;
    gap: 8px;
  }

  .toolbar-left,
  .toolbar-center,
  .toolbar-right {
    flex: 1 1 100%;
    justify-content: center;
  }

  .name-text {
    max-width: 150px;
  }

  .name-input {
    width: 150px;
  }
}
</style>
