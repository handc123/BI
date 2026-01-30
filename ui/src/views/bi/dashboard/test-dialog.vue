<template>
  <div class="test-dialog-page">
    <h1>对话框测试页面</h1>
    <el-button type="primary" @click="showDialog = true">打开测试对话框</el-button>
    
    <el-dialog
      title="测试对话框"
      :visible.sync="showDialog"
      width="600px"
      :modal="false"
      :modal-append-to-body="false"
      :append-to-body="true"
      @opened="handleOpened"
    >
      <div class="test-content">
        <p>这是测试内容</p>
        <el-button @click="handleClick">点击我测试</el-button>
        <el-input v-model="testInput" placeholder="输入测试"></el-input>
      </div>
      
      <div slot="footer">
        <el-button @click="showDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TestDialog',
  data() {
    return {
      showDialog: false,
      testInput: ''
    }
  },
  methods: {
    handleOpened() {
      this.$nextTick(() => {
        const dialogWrapper = document.querySelector('.el-dialog__wrapper')
        if (dialogWrapper) {
          dialogWrapper.style.pointerEvents = 'auto'
          console.log('设置 dialogWrapper pointer-events: auto')
        }
        const dialog = document.querySelector('.el-dialog')
        if (dialog) {
          dialog.style.pointerEvents = 'auto'
          console.log('设置 dialog pointer-events: auto')
        }
      })
    },
    handleClick() {
      this.$message.success('按钮点击成功！')
      console.log('按钮被点击')
    },
    handleConfirm() {
      this.$message.success('确定按钮点击成功！输入值：' + this.testInput)
      this.showDialog = false
    }
  }
}
</script>

<style scoped>
.test-dialog-page {
  padding: 40px;
}

.test-content {
  padding: 20px;
}

.test-content p {
  margin-bottom: 20px;
}

.test-content .el-button {
  margin-bottom: 20px;
}
</style>
