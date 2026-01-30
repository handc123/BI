/**
 * 历史记录状态管理模块
 * 实现撤销/重做功能
 */

const MAX_HISTORY_SIZE = 50

const state = {
  // 撤销栈
  undoStack: [],
  // 重做栈
  redoStack: [],
  // 是否启用历史记录
  enabled: true
}

const mutations = {
  /**
   * 推入历史记录
   */
  PUSH_HISTORY(state, snapshot) {
    if (!state.enabled) return
    
    // 深拷贝快照
    const copy = JSON.parse(JSON.stringify(snapshot))
    
    state.undoStack.push(copy)
    
    // 限制栈大小
    if (state.undoStack.length > MAX_HISTORY_SIZE) {
      state.undoStack.shift()
    }
    
    // 清空重做栈
    state.redoStack = []
  },

  /**
   * 撤销操作
   */
  UNDO(state, currentSnapshot) {
    if (state.undoStack.length === 0) return null
    
    // 保存当前状态到重做栈
    const currentCopy = JSON.parse(JSON.stringify(currentSnapshot))
    state.redoStack.push(currentCopy)
    
    // 返回上一个状态
    return state.undoStack.pop()
  },

  /**
   * 重做操作
   */
  REDO(state, currentSnapshot) {
    if (state.redoStack.length === 0) return null
    
    // 保存当前状态到撤销栈
    const currentCopy = JSON.parse(JSON.stringify(currentSnapshot))
    state.undoStack.push(currentCopy)
    
    // 返回下一个状态
    return state.redoStack.pop()
  },

  /**
   * 清空历史记录
   */
  CLEAR_HISTORY(state) {
    state.undoStack = []
    state.redoStack = []
  },

  /**
   * 设置启用状态
   */
  SET_ENABLED(state, enabled) {
    state.enabled = enabled
  }
}

const actions = {
  /**
   * 记录当前状态
   */
  recordState({ commit, rootState }) {
    const snapshot = {
      dashboard: rootState.dashboard.currentDashboard,
      components: rootState.components.components,
      queryConditions: rootState.components.queryConditions,
      conditionMappings: rootState.components.conditionMappings
    }
    commit('PUSH_HISTORY', snapshot)
  },

  /**
   * 撤销操作
   */
  undo({ commit, rootState, dispatch }) {
    const currentSnapshot = {
      dashboard: rootState.dashboard.currentDashboard,
      components: rootState.components.components,
      queryConditions: rootState.components.queryConditions,
      conditionMappings: rootState.components.conditionMappings
    }
    
    const previousState = state.undoStack[state.undoStack.length - 1]
    if (!previousState) return false
    
    commit('UNDO', currentSnapshot)
    
    // 恢复状态
    dispatch('restoreState', previousState)
    
    return true
  },

  /**
   * 重做操作
   */
  redo({ commit, rootState, dispatch }) {
    const currentSnapshot = {
      dashboard: rootState.dashboard.currentDashboard,
      components: rootState.components.components,
      queryConditions: rootState.components.queryConditions,
      conditionMappings: rootState.components.conditionMappings
    }
    
    const nextState = state.redoStack[state.redoStack.length - 1]
    if (!nextState) return false
    
    commit('REDO', currentSnapshot)
    
    // 恢复状态
    dispatch('restoreState', nextState)
    
    return true
  },

  /**
   * 恢复状态
   */
  restoreState({ commit }, snapshot) {
    // 暂时禁用历史记录，避免恢复操作被记录
    commit('SET_ENABLED', false)
    
    // 恢复仪表板状态
    commit('dashboard/SET_DASHBOARD', snapshot.dashboard, { root: true })
    
    // 恢复组件状态
    commit('components/SET_COMPONENTS', snapshot.components, { root: true })
    commit('components/SET_QUERY_CONDITIONS', snapshot.queryConditions, { root: true })
    commit('components/SET_CONDITION_MAPPINGS', snapshot.conditionMappings, { root: true })
    
    // 重新启用历史记录
    commit('SET_ENABLED', true)
  },

  /**
   * 清空历史记录
   */
  clearHistory({ commit }) {
    commit('CLEAR_HISTORY')
  },

  /**
   * 启用历史记录
   */
  enableHistory({ commit }) {
    commit('SET_ENABLED', true)
  },

  /**
   * 禁用历史记录
   */
  disableHistory({ commit }) {
    commit('SET_ENABLED', false)
  }
}

const getters = {
  // 是否可以撤销
  canUndo: state => state.undoStack.length > 0,
  
  // 是否可以重做
  canRedo: state => state.redoStack.length > 0,
  
  // 撤销栈大小
  undoStackSize: state => state.undoStack.length,
  
  // 重做栈大小
  redoStackSize: state => state.redoStack.length,
  
  // 是否启用
  isEnabled: state => state.enabled
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
