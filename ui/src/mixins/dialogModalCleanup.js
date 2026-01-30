/**
 * 对话框遮罩层清理混入（简化版）
 * 用于清理残留的遮罩层
 */
export default {
  methods: {
    /**
     * 清理多余的遮罩层
     */
    cleanupModals() {
      const modals = document.querySelectorAll('.v-modal')
      modals.forEach((modal) => {
        if (modal && modal.parentNode) {
          modal.remove()
        }
      })
    },

    /**
     * 对话框打开后的处理
     */
    handleDialogOpened() {
      this.$nextTick(() => {
        this.cleanupModals()
      })
    },

    /**
     * 对话框关闭后的处理
     */
    handleDialogClosed() {
      setTimeout(() => {
        this.cleanupModals()
      }, 300)
    }
  },
  beforeDestroy() {
    // 清理一次
    this.cleanupModals()
  }
}
