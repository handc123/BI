/**
 * 组件状态管理模块
 * 管理仪表板上的所有组件、查询条件和条件映射
 */

const state = {
  // 组件列表
  components: [],
  // 查询条件列表
  queryConditions: [],
  // 条件映射列表
  conditionMappings: [],
  // 当前选中的组件ID
  selectedComponentId: null,
  // 组件ID计数器
  componentIdCounter: 1
}

const mutations = {
  /**
   * 设置组件列表
   */
  SET_COMPONENTS(state, components) {
    state.components = components
  },

  /**
   * 添加组件
   */
  ADD_COMPONENT(state, component) {
    // 如果没有ID，生成一个
    if (!component.id) {
      component.id = `comp_${state.componentIdCounter++}`
    }

    state.components.push(component)
  },

  /**
   * 更新组件
   */
  UPDATE_COMPONENT(state, { id, updates }) {
    const index = state.components.findIndex(c => c.id === id)
    if (index !== -1) {
      const component = state.components[index]

      // 对于每个更新属性，使用 Vue.set 确保响应式更新
      Object.keys(updates).forEach(key => {
        const value = updates[key]

        // 对于嵌套对象，需要特殊处理
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
          // 如果是对象，先合并再设置
          if (component[key] && typeof component[key] === 'object') {
            component[key] = { ...component[key], ...value }
          } else {
            component[key] = { ...value }
          }
        } else {
          // 对于基本类型或数组，直接设置
          component[key] = value
        }
      })

      // 强制触发数组响应式更新
      // 创建一个新数组引用来确保 Vue 检测到变化
      state.components.splice(index, 1, { ...component })
    }
  },

  /**
   * 删除组件
   */
  DELETE_COMPONENT(state, id) {
    const index = state.components.findIndex(c => c.id === id)
    if (index !== -1) {
      state.components.splice(index, 1)
    }
  },

  /**
   * 批量更新组件位置
   */
  BATCH_UPDATE_POSITIONS(state, positions) {
    positions.forEach(pos => {
      const component = state.components.find(c => c.id === pos.id)
      if (component) {
        component.position = pos.position
        component.size = pos.size
        if (pos.zIndex !== undefined) {
          component.zIndex = pos.zIndex
        }
      }
    })
  },

  /**
   * 设置选中的组件
   */
  SET_SELECTED_COMPONENT(state, id) {
    state.selectedComponentId = id
  },

  /**
   * 设置查询条件列表
   */
  SET_QUERY_CONDITIONS(state, conditions) {
    state.queryConditions = conditions
  },

  /**
   * 添加查询条件
   */
  ADD_QUERY_CONDITION(state, condition) {
    state.queryConditions.push(condition)
  },

  /**
   * 更新查询条件
   */
  UPDATE_QUERY_CONDITION(state, { id, updates }) {
    const index = state.queryConditions.findIndex(c => c.id === id)
    if (index !== -1) {
      state.queryConditions[index] = { ...state.queryConditions[index], ...updates }
    }
  },

  /**
   * 删除查询条件
   */
  DELETE_QUERY_CONDITION(state, id) {
    const index = state.queryConditions.findIndex(c => c.id === id)
    if (index !== -1) {
      state.queryConditions.splice(index, 1)
    }
    // 同时删除相关的条件映射
    state.conditionMappings = state.conditionMappings.filter(m => m.conditionId !== id)
  },

  /**
   * 设置条件映射列表
   */
  SET_CONDITION_MAPPINGS(state, mappings) {
    state.conditionMappings = mappings
  },

  /**
   * 添加条件映射
   */
  ADD_CONDITION_MAPPING(state, mapping) {
    state.conditionMappings.push(mapping)
  },

  /**
   * 删除条件映射
   */
  DELETE_CONDITION_MAPPING(state, { conditionId, componentId }) {
    state.conditionMappings = state.conditionMappings.filter(
      m => !(m.conditionId === conditionId && m.componentId === componentId)
    )
  },

  /**
   * 重置组件状态
   */
  RESET_COMPONENTS(state) {
    state.components = []
    state.queryConditions = []
    state.conditionMappings = []
    state.selectedComponentId = null
    state.componentIdCounter = 1
  }
}

const actions = {
  /**
   * 设置组件列表
   */
  setComponents({ commit }, components) {
    commit('SET_COMPONENTS', components)
  },

  /**
   * 加载组件列表
   */
  loadComponents({ commit }, components) {
    commit('SET_COMPONENTS', components)
  },

  /**
   * 设置查询条件列表
   */
  setQueryConditions({ commit }, conditions) {
    commit('SET_QUERY_CONDITIONS', conditions)
  },

  /**
   * 设置条件映射列表
   */
  setConditionMappings({ commit }, mappings) {
    commit('SET_CONDITION_MAPPINGS', mappings)
  },

  /**
   * 添加组件
   */
  addComponent({ commit, rootGetters }, component) {
    
    commit('ADD_COMPONENT', component)
    
    
    // 标记仪表板为脏
    commit('dashboard/SET_DIRTY', true, { root: true })
    
  },

  /**
   * 更新组件
   */
  updateComponent({ commit }, { id, updates }) {
    commit('UPDATE_COMPONENT', { id, updates })
    // 标记仪表板为脏
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 删除组件
   */
  deleteComponent({ commit }, id) {
    commit('DELETE_COMPONENT', id)
    // 如果删除的是选中的组件，清除选择
    if (state.selectedComponentId === id) {
      commit('SET_SELECTED_COMPONENT', null)
    }
    // 标记仪表板为脏
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 复制组件
   */
  copyComponent({ commit, state }, id) {
    const component = state.components.find(c => c.id === id)
    if (component) {
      const copy = JSON.parse(JSON.stringify(component))
      // 生成新ID
      copy.id = `comp_${state.componentIdCounter++}`
      // 偏移位置
      copy.position = {
        x: component.position.x + 20,
        y: component.position.y + 20
      }
      commit('ADD_COMPONENT', copy)
      commit('dashboard/SET_DIRTY', true, { root: true })
      return copy.id
    }
    return null
  },

  /**
   * 批量更新组件位置
   */
  batchUpdatePositions({ commit }, positions) {
    commit('BATCH_UPDATE_POSITIONS', positions)
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 选择组件
   */
  selectComponent({ commit }, id) {
    commit('SET_SELECTED_COMPONENT', id)
  },

  /**
   * 加载查询条件
   */
  loadQueryConditions({ commit }, conditions) {
    commit('SET_QUERY_CONDITIONS', conditions)
  },

  /**
   * 添加查询条件
   */
  addQueryCondition({ commit }, condition) {
    commit('ADD_QUERY_CONDITION', condition)
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 更新查询条件
   */
  updateQueryCondition({ commit }, { id, updates }) {
    commit('UPDATE_QUERY_CONDITION', { id, updates })
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 删除查询条件
   */
  deleteQueryCondition({ commit }, id) {
    commit('DELETE_QUERY_CONDITION', id)
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 加载条件映射
   */
  loadConditionMappings({ commit }, mappings) {
    commit('SET_CONDITION_MAPPINGS', mappings)
  },

  /**
   * 添加条件映射
   */
  addConditionMapping({ commit }, mapping) {
    commit('ADD_CONDITION_MAPPING', mapping)
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 删除条件映射
   */
  deleteConditionMapping({ commit }, { conditionId, componentId }) {
    commit('DELETE_CONDITION_MAPPING', { conditionId, componentId })
    commit('dashboard/SET_DIRTY', true, { root: true })
  },

  /**
   * 重置组件状态
   */
  resetComponents({ commit }) {
    commit('RESET_COMPONENTS')
  }
}

const getters = {
  // 获取所有组件
  components: state => state.components,
  
  // 获取组件数量
  componentCount: state => state.components.length,
  
  // 根据ID获取组件
  getComponentById: state => id => {
    return state.components.find(c => c.id === id)
  },
  
  // 获取选中的组件
  selectedComponent: state => {
    if (!state.selectedComponentId) return null
    return state.components.find(c => c.id === state.selectedComponentId)
  },
  
  // 获取选中的组件ID
  selectedComponentId: state => state.selectedComponentId,
  
  // 获取图表组件列表
  chartComponents: state => {
    return state.components.filter(c => c.type === 'chart')
  },
  
  // 获取查询组件列表
  queryComponents: state => {
    return state.components.filter(c => c.type === 'query')
  },
  
  // 获取所有查询条件
  queryConditions: state => state.queryConditions,
  
  // 根据ID获取查询条件
  getQueryConditionById: state => id => {
    return state.queryConditions.find(c => c.id === id)
  },
  
  // 获取所有条件映射
  conditionMappings: state => state.conditionMappings,
  
  // 获取组件的条件映射
  getComponentMappings: state => componentId => {
    return state.conditionMappings.filter(m => m.componentId === componentId)
  },
  
  // 获取条件的映射
  getConditionMappings: state => conditionId => {
    return state.conditionMappings.filter(m => m.conditionId === conditionId)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
