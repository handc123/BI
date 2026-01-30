/**
 * 仪表板状态管理模块
 * 管理当前仪表板的配置、状态和全局样式
 */

const state = {
  // 当前仪表板配置
  currentDashboard: {
    id: null,
    dashboardName: '',
    dashboardDesc: '',
    theme: 'light',
    canvasConfig: {
      width: 1520, // 画布实际可用宽度（1920 - 400配置面板）
      height: 1080,
      gridSize: 10,
      margin: { top: 20, right: 20, bottom: 20, left: 20 },
      background: { type: 'color', value: '#ffffff', opacity: 1 }
    },
    globalStyle: {
      colorScheme: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
      titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
      fontFamily: 'Microsoft YaHei'
    },
    status: '0', // 0草稿 1已发布
    publishedVersion: 0
  },
  // 选中的组件ID
  selectedComponentId: null,
  // 是否有未保存的更改
  isDirty: false,
  // 是否处于预览模式
  isPreviewMode: false,
  // 查询条件列表
  queryConditions: [],
  // 条件映射列表
  conditionMappings: [],
  // 查询条件当前值 {conditionId: value}
  queryValues: {}
}

const mutations = {
  /**
   * 设置当前仪表板
   */
  SET_DASHBOARD(state, dashboard) {
    state.currentDashboard = { ...state.currentDashboard, ...dashboard }
  },

  /**
   * 设置选中的组件ID
   */
  SET_SELECTED_COMPONENT(state, componentId) {
    state.selectedComponentId = componentId
  },

  /**
   * 更新仪表板名称
   */
  UPDATE_DASHBOARD_NAME(state, name) {
    state.currentDashboard.dashboardName = name
    state.isDirty = true
  },

  /**
   * 更新仪表板描述
   */
  UPDATE_DASHBOARD_DESC(state, desc) {
    state.currentDashboard.dashboardDesc = desc
    state.isDirty = true
  },

  /**
   * 更新主题
   */
  UPDATE_THEME(state, theme) {
    state.currentDashboard.theme = theme
    state.isDirty = true
  },

  /**
   * 更新画布配置
   */
  UPDATE_CANVAS_CONFIG(state, config) {
    state.currentDashboard.canvasConfig = { ...state.currentDashboard.canvasConfig, ...config }
    state.isDirty = true
  },

  /**
   * 更新全局样式
   */
  UPDATE_GLOBAL_STYLE(state, style) {
    state.currentDashboard.globalStyle = { ...state.currentDashboard.globalStyle, ...style }
    state.isDirty = true
  },

  /**
   * 设置脏标志
   */
  SET_DIRTY(state, isDirty) {
    state.isDirty = isDirty
  },

  /**
   * 设置预览模式
   */
  SET_PREVIEW_MODE(state, isPreview) {
    state.isPreviewMode = isPreview
  },

  /**
   * 重置仪表板状态
   */
  RESET_DASHBOARD(state) {
    state.currentDashboard = {
      id: null,
      dashboardName: '',
      dashboardDesc: '',
      theme: 'light',
      canvasConfig: {
        width: 1520, // 画布实际可用宽度（1920 - 400配置面板）
        height: 1080,
        gridSize: 10,
        margin: { top: 20, right: 20, bottom: 20, left: 20 },
        background: { type: 'color', value: '#ffffff', opacity: 1 }
      },
      globalStyle: {
        colorScheme: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
        titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
        fontFamily: 'Microsoft YaHei'
      },
      status: '0',
      publishedVersion: 0
    }
    state.isDirty = false
    state.isPreviewMode = false
    state.queryConditions = []
    state.conditionMappings = []
    state.queryValues = {}
  },

  /**
   * 设置查询条件列表
   */
  SET_QUERY_CONDITIONS(state, conditions) {
    state.queryConditions = conditions
  },

  /**
   * 设置条件映射列表
   */
  SET_CONDITION_MAPPINGS(state, mappings) {
    state.conditionMappings = mappings
  },

  /**
   * 更新查询条件值
   */
  UPDATE_QUERY_VALUE(state, { conditionId, value }) {
    state.queryValues = { ...state.queryValues, [conditionId]: value }
  },

  /**
   * 批量更新查询条件值
   */
  UPDATE_QUERY_VALUES(state, values) {
    state.queryValues = { ...state.queryValues, ...values }
  },

  /**
   * 清空查询条件值
   */
  CLEAR_QUERY_VALUES(state) {
    state.queryValues = {}
  }
}

const actions = {
  /**
   * 设置当前仪表板
   */
  setCurrentDashboard({ commit }, dashboard) {
    commit('SET_DASHBOARD', dashboard)
    commit('SET_DIRTY', false)
  },

  /**
   * 加载仪表板
   */
  loadDashboard({ commit }, dashboard) {
    commit('SET_DASHBOARD', dashboard)
    commit('SET_DIRTY', false)
  },

  /**
   * 选择组件
   */
  selectComponent({ commit }, componentId) {
    commit('SET_SELECTED_COMPONENT', componentId)
  },

  /**
   * 清除选择
   */
  clearSelection({ commit }) {
    commit('SET_SELECTED_COMPONENT', null)
  },

  /**
   * 更新仪表板名称
   */
  updateDashboardName({ commit }, name) {
    commit('UPDATE_DASHBOARD_NAME', name)
  },

  /**
   * 更新仪表板描述
   */
  updateDashboardDesc({ commit }, desc) {
    commit('UPDATE_DASHBOARD_DESC', desc)
  },

  /**
   * 更新主题
   */
  updateTheme({ commit }, theme) {
    commit('UPDATE_THEME', theme)
  },

  /**
   * 更新画布配置
   */
  updateCanvasConfig({ commit }, config) {
    commit('UPDATE_CANVAS_CONFIG', config)
  },

  /**
   * 更新全局样式
   */
  updateGlobalStyle({ commit }, style) {
    commit('UPDATE_GLOBAL_STYLE', style)
  },

  /**
   * 标记为已保存
   */
  markAsSaved({ commit }) {
    commit('SET_DIRTY', false)
  },

  /**
   * 进入预览模式
   */
  enterPreviewMode({ commit }) {
    commit('SET_PREVIEW_MODE', true)
  },

  /**
   * 退出预览模式
   */
  exitPreviewMode({ commit }) {
    commit('SET_PREVIEW_MODE', false)
  },

  /**
   * 重置仪表板
   */
  resetDashboard({ commit }) {
    commit('RESET_DASHBOARD')
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
   * 更新查询条件值
   */
  updateQueryValue({ commit }, { conditionId, value }) {
    commit('UPDATE_QUERY_VALUE', { conditionId, value })
  },

  /**
   * 批量更新查询条件值
   */
  updateQueryValues({ commit }, values) {
    commit('UPDATE_QUERY_VALUES', values)
  },

  /**
   * 清空查询条件值
   */
  clearQueryValues({ commit }) {
    commit('CLEAR_QUERY_VALUES')
  }
}

const getters = {
  // 获取当前仪表板
  currentDashboard: state => state.currentDashboard,
  
  // 获取仪表板ID
  dashboardId: state => state.currentDashboard.id,
  
  // 获取仪表板名称
  dashboardName: state => state.currentDashboard.dashboardName,
  
  // 获取主题
  theme: state => state.currentDashboard.theme,
  
  // 获取画布配置
  canvasConfig: state => state.currentDashboard.canvasConfig,
  
  // 获取网格大小
  gridSize: state => state.currentDashboard.canvasConfig.gridSize,
  
  // 获取全局样式
  globalStyle: state => state.currentDashboard.globalStyle,
  
  // 是否有未保存的更改
  isDirty: state => state.isDirty,
  
  // 是否处于预览模式
  isPreviewMode: state => state.isPreviewMode,
  
  // 是否已发布
  isPublished: state => state.currentDashboard.status === '1',
  
  // 获取查询条件列表
  queryConditions: state => state.queryConditions,
  
  // 获取条件映射列表
  conditionMappings: state => state.conditionMappings,
  
  // 获取查询条件值
  queryValues: state => state.queryValues,
  
  // 获取特定条件的值
  getQueryValue: state => conditionId => state.queryValues[conditionId]
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
