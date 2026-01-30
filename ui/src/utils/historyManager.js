/**
 * 历史记录管理器
 * 提供独立的历史记录管理功能(可选的非Vuex实现)
 */

class HistoryManager {
  constructor(maxSize = 50) {
    this.undoStack = []
    this.redoStack = []
    this.maxSize = maxSize
    this.enabled = true
  }

  /**
   * 记录状态快照
   * @param {Object} state - 当前状态
   */
  push(state) {
    if (!this.enabled) return

    // 深拷贝状态
    const snapshot = JSON.parse(JSON.stringify(state))

    this.undoStack.push(snapshot)
    if (this.undoStack.length > this.maxSize) {
      this.undoStack.shift()
    }

    // 清空重做栈
    this.redoStack = []
  }

  /**
   * 撤销操作
   * @param {Object} currentState - 当前状态
   * @returns {Object|null} 上一个状态
   */
  undo(currentState) {
    if (this.undoStack.length === 0) return null

    // 保存当前状态到重做栈
    this.redoStack.push(JSON.parse(JSON.stringify(currentState)))

    // 返回上一个状态
    return this.undoStack.pop()
  }

  /**
   * 重做操作
   * @param {Object} currentState - 当前状态
   * @returns {Object|null} 下一个状态
   */
  redo(currentState) {
    if (this.redoStack.length === 0) return null

    // 保存当前状态到撤销栈
    this.undoStack.push(JSON.parse(JSON.stringify(currentState)))

    // 返回下一个状态
    return this.redoStack.pop()
  }

  /**
   * 是否可以撤销
   * @returns {boolean}
   */
  canUndo() {
    return this.undoStack.length > 0
  }

  /**
   * 是否可以重做
   * @returns {boolean}
   */
  canRedo() {
    return this.redoStack.length > 0
  }

  /**
   * 清空历史记录
   */
  clear() {
    this.undoStack = []
    this.redoStack = []
  }

  /**
   * 启用历史记录
   */
  enable() {
    this.enabled = true
  }

  /**
   * 禁用历史记录
   */
  disable() {
    this.enabled = false
  }

  /**
   * 获取撤销栈大小
   * @returns {number}
   */
  getUndoStackSize() {
    return this.undoStack.length
  }

  /**
   * 获取重做栈大小
   * @returns {number}
   */
  getRedoStackSize() {
    return this.redoStack.length
  }

  /**
   * 批量操作 - 暂时禁用历史记录
   * @param {Function} callback - 批量操作回调
   */
  batch(callback) {
    this.disable()
    try {
      callback()
    } finally {
      this.enable()
    }
  }
}

// 创建单例实例
const historyManager = new HistoryManager()

export default historyManager

// 也导出类，以便创建多个实例
export { HistoryManager }
