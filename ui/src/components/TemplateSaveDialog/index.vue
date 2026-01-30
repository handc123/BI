<template>
  <el-dialog
    title="保存为模板"
    :visible.sync="dialogVisible"
    width="500px"
    :close-on-click-modal="false"
    :modal="true"
    :append-to-body="true"
    :destroy-on-close="true"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="form" label-width="100px">
      <el-form-item label="模板名称" prop="templateName">
        <el-input
          v-model="form.templateName"
          placeholder="请输入模板名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="模板描述" prop="templateDesc">
        <el-input
          v-model="form.templateDesc"
          type="textarea"
          :rows="3"
          placeholder="请输入模板描述（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { saveAsTemplate } from '@/api/bi/component'

export default {
  name: 'TemplateSaveDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    component: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      form: {
        templateName: '',
        templateDesc: ''
      },
      rules: {
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
          if (val && this.component) {
            // 使用组件名称作为默认模板名称
            this.form.templateName = this.component.name || this.component.componentName || ''
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
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }

        if (!this.component || !this.component.id) {
          this.$message.error('组件信息不完整')
          return
        }

        this.loading = true
        saveAsTemplate(this.component.id, this.form)
          .then(response => {
            this.$message.success('模板保存成功')
            this.$emit('success')
            this.handleClose()
          })
          .catch(error => {
            this.$message.error('模板保存失败：' + (error.msg || error.message))
          })
          .finally(() => {
            this.loading = false
          })
      })
    },

    handleClose() {
      this.dialogVisible = false
      this.$refs.form.resetFields()
      this.form = {
        templateName: '',
        templateDesc: ''
      }
    }
  },
  beforeDestroy() {
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

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
