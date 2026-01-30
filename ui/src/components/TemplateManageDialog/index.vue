<template>
  <div>
    <el-dialog
      title="模板管理"
      :visible.sync="dialogVisible"
      width="800px"
      :close-on-click-modal="false"
      :modal="false"
      :modal-append-to-body="false"
      :append-to-body="true"
      :destroy-on-close="true"
      :before-close="handleBeforeClose"
      @opened="handleOpened"
    >
      <div class="template-manage">
        <el-table
          v-loading="loading"
          :data="templates"
          style="width: 100%"
        >
          <el-table-column
            prop="templateName"
            label="模板名称"
            min-width="150"
          />
          <el-table-column
            prop="componentType"
            label="组件类型"
            width="120"
          >
            <template slot-scope="scope">
              <el-tag :type="getComponentTypeTag(scope.row.componentType)">
                {{ getComponentTypeName(scope.row.componentType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="templateDesc"
            label="描述"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column
            prop="createTime"
            label="创建时间"
            width="160"
          >
            <template slot-scope="scope">
              {{ parseTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="150"
            align="center"
          >
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleEdit(scope.row)"
              >编辑</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                style="color: #f56c6c;"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="total > 0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="loadTemplates"
        />
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 编辑模板对话框 - 移到外层，避免嵌套 -->
    <el-dialog
      title="编辑模板"
      :visible.sync="editDialogVisible"
      width="500px"
      :append-to-body="true"
      :modal="false"
      :modal-append-to-body="false"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input
            v-model="editForm.templateName"
            placeholder="请输入模板名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="模板描述" prop="templateDesc">
          <el-input
            v-model="editForm.templateDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-alert
          title="注意：修改模板不会影响已使用该模板创建的组件实例"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 10px;"
        />
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listComponentTemplate, updateComponentTemplate, delComponentTemplate } from '@/api/bi/component'
import dialogModalCleanup from '@/mixins/dialogModalCleanup'

export default {
  name: 'TemplateManageDialog',
  mixins: [dialogModalCleanup],
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      templates: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      editDialogVisible: false,
      updating: false,
      editForm: {
        id: null,
        templateName: '',
        templateDesc: '',
        componentType: '',
        templateConfig: ''
      },
      editRules: {
        templateName: [
          { required: true, message: '请输入模板名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    visible: {
      immediate: true,
      handler(val) {
        if (this.dialogVisible !== val) {
          this.dialogVisible = val
          if (val) {
            this.loadTemplates()
          }
        }
      }
    },
    dialogVisible(val) {
      if (val !== this.visible) {
        this.$emit('update:visible', val)
      }
    }
  },
  methods: {
    // 对话框打开后的处理（覆盖 mixin 的方法）
    handleOpened() {
      // 不做任何处理，因为已经禁用了遮罩层
      this.$nextTick(() => {
        // 确保对话框可点击
        const dialogWrapper = this.$el.querySelector('.el-dialog__wrapper')
        if (dialogWrapper) {
          dialogWrapper.style.pointerEvents = 'auto'
        }
        const dialog = this.$el.querySelector('.el-dialog')
        if (dialog) {
          dialog.style.pointerEvents = 'auto'
        }
      })
    },

    // 清理多余的遮罩层
    cleanupModals() {
      const modals = document.querySelectorAll('.v-modal')
      const visibleDialogs = document.querySelectorAll('.el-dialog__wrapper')
      
      let visibleCount = 0
      visibleDialogs.forEach(dialog => {
        const display = window.getComputedStyle(dialog).display
        if (display !== 'none') {
          visibleCount++
        }
      })
      
      if (modals.length > visibleCount) {
        for (let i = visibleCount; i < modals.length; i++) {
          modals[i].remove()
        }
      }
    },

    // 加载模板列表
    loadTemplates() {
      this.loading = true
      listComponentTemplate(this.queryParams)
        .then(response => {
          this.templates = response.rows || []
          this.total = response.total || 0
        })
        .catch(error => {
          this.$message.error('加载模板失败：' + (error.msg || error.message))
        })
        .finally(() => {
          this.loading = false
        })
    },

    // 编辑模板
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        templateName: row.templateName,
        templateDesc: row.templateDesc,
        componentType: row.componentType,
        templateConfig: row.templateConfig
      }
      this.editDialogVisible = true
    },

    // 更新模板
    handleUpdate() {
      this.$refs.editForm.validate(valid => {
        if (!valid) {
          return
        }

        this.updating = true
        updateComponentTemplate(this.editForm)
          .then(() => {
            this.$message.success('更新成功')
            this.editDialogVisible = false
            this.loadTemplates()
          })
          .catch(error => {
            this.$message.error('更新失败：' + (error.msg || error.message))
          })
          .finally(() => {
            this.updating = false
          })
      })
    },

    // 删除模板
    handleDelete(row) {
      this.$confirm('确定要删除模板"' + row.templateName + '"吗？删除后不可恢复。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delComponentTemplate(row.id)
          .then(() => {
            this.$message.success('删除成功')
            this.loadTemplates()
          })
          .catch(error => {
            this.$message.error('删除失败：' + (error.msg || error.message))
          })
      }).catch(() => {})
    },

    // 获取组件类型名称
    getComponentTypeName(type) {
      const typeMap = {
        chart: '图表',
        query: '查询',
        text: '富文本',
        media: '媒体',
        tabs: '标签页'
      }
      return typeMap[type] || type
    },

    // 获取组件类型标签颜色
    getComponentTypeTag(type) {
      const tagMap = {
        chart: 'primary',
        query: 'success',
        text: 'info',
        media: 'warning',
        tabs: 'danger'
      }
      return tagMap[type] || ''
    },

    // 格式化时间
    parseTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    },

    // 关闭前的处理（Element UI 的 before-close 回调）
    handleBeforeClose(done) {
      // 立即清理遮罩层
      this.cleanupModals()
      
      // 确保页面可交互
      document.body.style.pointerEvents = 'auto'
      document.body.style.overflow = 'auto'
      
      const rootEl = document.querySelector('.dashboard-designer-page')
      if (rootEl) {
        rootEl.style.pointerEvents = 'auto'
      }
      
      // 调用 done() 让对话框关闭
      done()
      
      // 关闭后继续清理
      this.$nextTick(() => {
        this.cleanupModals()
        document.body.style.pointerEvents = 'auto'
        
        setTimeout(() => {
          this.cleanupModals()
          document.body.style.pointerEvents = 'auto'
        }, 50)
        
        setTimeout(() => {
          this.cleanupModals()
          document.body.style.pointerEvents = 'auto'
        }, 300)
      })
    },
    
    // 保留 handleClose 以防其他地方调用
    handleClose() {
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped>
/* 确保对话框内容可点击 */
.template-manage {
  min-height: 400px;
  pointer-events: auto !important;
}

/* 确保所有子元素可点击 */
.template-manage * {
  pointer-events: auto !important;
}

.dialog-footer {
  text-align: right;
}
</style>
