<template>
  <div class="bi-file-datasource-upload">
    <el-form :model="form" size="small" label-width="80px">
      <el-form-item label="名称">
        <el-input v-model="form.name" placeholder="可选：数据源名称（不填则使用文件名）" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" placeholder="可选：备注" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="文件">
        <el-upload
          :action="uploadUrl"
          :headers="headers"
          :data="uploadExtraData"
          :before-upload="handleBeforeUpload"
          :on-success="handleSuccess"
          :on-error="handleError"
          :show-file-list="true"
          :limit="1"
          :on-exceed="handleExceed"
          :disabled="disabled"
          accept=".csv,.xls,.xlsx"
        >
          <el-button size="mini" type="primary" :disabled="disabled">选择文件并上传</el-button>
          <div class="el-upload__tip" slot="tip" v-if="showTip">
            请上传 <b style="color:#f56c6c">{{ fileTypeText }}</b> 文件，大小不超过 <b style="color:#f56c6c">{{ fileSize }}MB</b>
          </div>
        </el-upload>
      </el-form-item>
    </el-form>

    <div v-if="lastResult" class="result">
      <el-alert
        v-if="lastResult.success"
        type="success"
        :closable="false"
        show-icon
        :title="`上传成功：${lastResult.fileName}（${lastResult.rowCount || 0} 行）`"
      />
      <el-alert
        v-else
        type="error"
        :closable="false"
        show-icon
        :title="lastResult.message || '上传失败'"
      />
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'

export default {
  name: 'BiFileDataSourceUpload',
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    fileSize: {
      type: Number,
      default: 50
    },
    fileType: {
      type: Array,
      default: () => ['csv', 'xls', 'xlsx']
    },
    showTip: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      form: {
        name: '',
        remark: ''
      },
      lastResult: null,
      uploadUrl: process.env.VUE_APP_BASE_API + '/bi/datasource/file/upload',
      headers: {
        Authorization: 'Bearer ' + getToken()
      }
    }
  },
  computed: {
    fileTypeText() {
      return (this.fileType || []).join('/')
    },
    uploadExtraData() {
      return {
        name: this.form.name || undefined,
        remark: this.form.remark || undefined
      }
    }
  },
  methods: {
    handleBeforeUpload(file) {
      const nameParts = (file.name || '').split('.')
      const ext = (nameParts[nameParts.length - 1] || '').toLowerCase()
      if (this.fileType && this.fileType.length > 0 && this.fileType.indexOf(ext) < 0) {
        this.$modal.msgError(`文件格式不正确，请上传 ${this.fileTypeText} 格式文件!`)
        return false
      }
      const isLt = file.size / 1024 / 1024 < this.fileSize
      if (!isLt) {
        this.$modal.msgError(`上传文件大小不能超过 ${this.fileSize} MB!`)
        return false
      }
      this.$modal.loading('正在上传文件，请稍候...')
      return true
    },
    handleExceed() {
      this.$modal.msgError('一次只能上传 1 个文件')
    },
    handleSuccess(res) {
      this.$modal.closeLoading()
      // 若依AjaxResult：{code,msg,data}
      const payload = res && res.data ? res.data : null
      this.lastResult = payload
      if (res && res.code === 200 && payload && payload.success) {
        this.$modal.msgSuccess('上传成功')
        this.$emit('success', payload)
      } else {
        this.$modal.msgError((payload && payload.message) || (res && res.msg) || '上传失败')
        this.$emit('error', payload)
      }
    },
    handleError() {
      this.$modal.closeLoading()
      this.lastResult = { success: false, message: '上传失败' }
      this.$modal.msgError('上传失败，请重试')
      this.$emit('error', this.lastResult)
    }
  }
}
</script>

<style scoped>
.result {
  margin-top: 10px;
}
</style>

