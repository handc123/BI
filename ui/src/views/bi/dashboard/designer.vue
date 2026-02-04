<template>
  <div class="dashboard-designer-page">
    <!-- 顶部工具栏 -->
    <designer-toolbar
      :dashboard-name="currentDashboard.dashboardName"
      :can-undo="canUndo"
      :can-redo="canRedo"
      :has-unsaved-changes="hasUnsavedChanges"
      @back="handleBack"
      @undo="handleUndo"
      @redo="handleRedo"
      @name-change="handleNameChange"
      @open-component-library="showComponentLibrary = true"
      @preview="handlePreview"
      @save="handleSave"
      @publish="handlePublish"
    />

    <!-- 主编辑区域 -->
    <div class="designer-main">
      <!-- 画布编辑区 -->
      <div class="canvas-container">
        <designer-canvas
          ref="canvas"
          :components="components"
          :query-conditions="queryConditions"
          :grid-size="gridSize"
          :canvas-config="canvasConfig"
          :selected-component-id="selectedComponentId"
          @component-select="handleComponentSelect"
          @component-update="handleComponentUpdate"
          @component-delete="handleComponentDelete"
          @component-copy="handleComponentCopy"
          @open-query-config="showQueryConfig = true"
          @query="handleQuery"
          @reset-query="handleResetQuery"
        />
      </div>

      <!-- 右侧配置面板 - 始终显示 -->
      <div class="config-container">
        <config-panel
          :target="configTarget"
          :target-type="configTargetType"
          @config-change="handleConfigChange"
          @refresh-chart="handleRefreshChart"
        />
      </div>
    </div>

    <!-- 组件库弹窗 -->
    <component-library
      :visible.sync="showComponentLibrary"
      @add-component="handleAddComponent"
    />

    <!-- 查询条件配置弹窗 -->
    <query-condition-config
      :visible.sync="showQueryConfig"
      :component-id="(configTarget && typeof configTarget.id === 'number') ? configTarget.id : null"
      :dashboard-id="dashboardId"
      :dataset-id="configTarget && configTarget.dataConfig && configTarget.dataConfig.datasetId"
      :layout-components="components"
      :conditions="queryConditions"
      :condition-mappings="conditionMappings"
      @update="handleQueryConfigUpdate"
    />

    <!-- 模板保存对话框 -->
    <template-save-dialog
      :visible.sync="showTemplateSave"
      :component="componentToSaveAsTemplate"
      @save="handleTemplateSave"
    />

    <!-- 模板管理对话框 -->
    <template-manage-dialog
      :visible.sync="showTemplateManage"
      @use-template="handleUseTemplate"
    />
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex'
import DesignerToolbar from '@/components/DesignerToolbar'
import DesignerCanvas from '@/components/DesignerCanvas'
import ConfigPanel from '@/components/ConfigPanel'
import ComponentLibrary from '@/components/ComponentLibrary'
import QueryConditionConfig from '@/components/QueryConditionConfig'
import TemplateSaveDialog from '@/components/TemplateSaveDialog'
import TemplateManageDialog from '@/components/TemplateManageDialog'
import {
  getDashboard,
  saveDashboard,
  publishDashboard,
  getDashboardConfig
} from '@/api/bi/dashboard'

export default {
  name: 'DashboardDesigner',
  components: {
    DesignerToolbar,
    DesignerCanvas,
    ConfigPanel,
    ComponentLibrary,
    QueryConditionConfig,
    TemplateSaveDialog,
    TemplateManageDialog
  },
  data() {
    return {
      dashboardId: null,
      pendingDashboardId: null, // 待加载的仪表板 ID
      showComponentLibrary: false,
      showQueryConfig: false,
      showTemplateSave: false,
      showTemplateManage: false,
      componentToSaveAsTemplate: null,
      hasUnsavedChanges: false,
      saveInProgress: false
    }
  },
  computed: {
    ...mapState('dashboard', [
      'currentDashboard',
      'selectedComponentId'
    ]),
    ...mapState('components', [
      'components',
      'queryConditions',
      'conditionMappings'
    ]),
    ...mapGetters('history', [
      'canUndo',
      'canRedo'
    ]),
    gridSize() {
      return this.currentDashboard.canvasConfig?.gridSize || 10
    },
    canvasConfig() {
      return this.currentDashboard.canvasConfig || {}
    },
    configTarget() {
      if (this.selectedComponentId) {
        return this.components.find(c => c.id === this.selectedComponentId)
      }
      return this.currentDashboard
    },
    configTargetType() {
      return this.selectedComponentId ? 'component' : 'dashboard'
    },
    chartComponents() {
      // 过滤出有 dataConfig 的所有可视化组件
      return this.components.filter(c => c.dataConfig && c.name)
    }
  },
  watch: {
    '$route.params.id': {
      immediate: true,
      handler(id) {
        if (id) {
          this.dashboardId = id
          // 不立即加载，等待 mounted 完成后再加载
          this.pendingDashboardId = id
        } else {
          this.initNewDashboard()
        }
      }
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.hasUnsavedChanges && !this.saveInProgress) {
      this.$confirm('您有未保存的更改，确定要离开吗？', '提示', {
        confirmButtonText: '保存并离开',
        cancelButtonText: '不保存',
        distinguishCancelAndClose: true,
        type: 'warning'
      }).then(() => {
        this.handleSave().then(() => {
          next()
        }).catch(() => {
          next(false)
        })
      }).catch(action => {
        if (action === 'cancel') {
          next()
        } else {
          next(false)
        }
      })
    } else {
      next()
    }
  },
  methods: {
    ...mapActions('dashboard', [
      'setCurrentDashboard',
      'updateDashboardName',
      'updateCanvasConfig',
      'updateGlobalStyle',
      'updateGlobalStyle',
      'updateQueryValues',
      'clearQueryValues',
      'selectComponent',
      'clearSelection'
    ]),

    // 清理所有 loading 遮罩
    clearLoadingMasks() {
      const loadingMasks = document.querySelectorAll('.el-loading-mask')
      const modals = document.querySelectorAll('.v-modal')


      // 移除所有 Element UI 的 loading 遮罩
      loadingMasks.forEach(mask => {
        mask.remove()
      })
      // 移除所有模态框遮罩
      modals.forEach(modal => {
        modal.remove()
      })

      // 重置页面的 pointer-events
      const rootEl = document.querySelector('.dashboard-designer-page')
      if (rootEl) {
        rootEl.style.pointerEvents = 'auto'
        rootEl.style.opacity = '1'
        rootEl.style.visibility = 'visible'
      }

      // 检查是否有其他覆盖元素
      this.debugPageState()
    },

    // 调试页面状态
    debugPageState() {
      const page = document.querySelector('.dashboard-designer-page')
      if (!page) {
        return
      }

      const computedStyle = window.getComputedStyle(page)

      // 检查所有可能遮挡页面的元素
      const allElements = document.querySelectorAll('body *')
      let overlayCount = 0

      allElements.forEach((el, index) => {
        if (index > 500) return // 限制检查数量

        const style = window.getComputedStyle(el)
        const rect = el.getBoundingClientRect()

        // 检查固定定位或绝对定位且覆盖大面积的元素
        if ((style.position === 'fixed' || style.position === 'absolute')) {
          if (rect.width > 100 && rect.height > 100) {
            const isCovering = rect.width >= window.innerWidth * 0.8 && rect.height >= window.innerHeight * 0.8
            if (isCovering || style.zIndex > 100) {
              overlayCount++
            }
          }
        }
      })

      // 检查所有可能的遮罩类
      const maskSelectors = [
        '.el-loading-mask',
        '.v-modal',
        '.el-mask',
        '.loading-mask',
        '[class*="mask"]',
        '[class*="overlay"]',
        '[class*="loading"]'
      ]

      maskSelectors.forEach(selector => {
        const elements = document.querySelectorAll(selector)
        if (elements.length > 0) {
          elements.forEach((el, i) => {
            if (i < 3) { // 只显示前3个
              const style = window.getComputedStyle(el)
              const rect = el.getBoundingClientRect()
            }
          })
        }
      })

      // 特别检查：找出所有包含 loading 的元素并详细输出
      const loadingElements = document.querySelectorAll('[class*="loading"]')
      if (loadingElements.length > 0) {
        loadingElements.forEach((el, i) => {
          const style = window.getComputedStyle(el)
          const rect = el.getBoundingClientRect()
        })
      }
    },
    ...mapActions('components', [
      'addComponent',
      'updateComponent',
      'deleteComponent',
      'copyComponent',
      'setComponents',
      'setQueryConditions',
      'setConditionMappings'
    ]),
    ...mapActions('history', [
      'recordState',
      'undo',
      'redo',
      'clearHistory'
    ]),

    async loadDashboard(id) {

      // 不使用 Element UI 的 loading，直接设置标志
      this.isLoadingDashboard = true

      // 设置一个超时保护
      const safetyTimeout = setTimeout(() => {
        this.isLoadingDashboard = false
        this.clearLoadingMasks()
        this.forceEnableInteraction()
      }, 10000)

      try {
        const response = await getDashboardConfig(id)
        const config = response.data

        this.setCurrentDashboard({
          id: config.dashboard.id,
          dashboardName: config.dashboard.dashboardName || config.dashboard.name,
          theme: config.dashboard.theme || 'light',
          canvasConfig: this.parseJSON(config.dashboard.canvasConfig, {
            width: 1520,
            height: 1080,
            gridSize: 10,
            margin: { top: 20, right: 20, bottom: 20, left: 20 },
            background: { type: 'color', value: '#ffffff', opacity: 1 }
          }),
          globalStyle: this.parseJSON(config.dashboard.globalStyle, {
            colorScheme: ['#5470c6', '#91cc75', '#fac858'],
            titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
            fontFamily: 'Microsoft YaHei'
          }),
          status: config.dashboard.status,
          publishedVersion: config.dashboard.publishedVersion
        })

        const components = (config.components || []).map(comp => {
          // 解析配置对象
          const dataConfig = this.parseJSON(comp.dataConfig, {})
          const styleConfig = this.parseJSON(comp.styleConfig, {})
          const advancedConfig = this.parseJSON(comp.advancedConfig, {})
          
          // 返回组件对象（不冻结，因为需要响应式更新）
          return {
            id: comp.id,
            type: comp.componentType,
            name: comp.componentName,
            position: { x: comp.positionX, y: comp.positionY },
            size: { width: comp.width, height: comp.height },
            zIndex: comp.zindex || comp.zIndex || 0,
            dataConfig,
            styleConfig,
            advancedConfig
          }
        })

        // 检查是否有查询组件，如果有，需要调整其他组件的位置
        const hasQueryComponent = components.some(c => c.type === 'query')
        if (hasQueryComponent) {
          const queryComponentHeight = 120

          // 调整所有非查询组件的位置，避免与查询组件重叠
          components.forEach(comp => {
            if (comp.type !== 'query' && comp.position.y < queryComponentHeight) {
              comp.position.y += queryComponentHeight
            }
          })
        }

        
        // 直接设置组件，不使用 $nextTick
        this.setComponents(components)
        
        // 继续设置其他配置
        this.setQueryConditions(config.queryConditions || [])
        this.setConditionMappings(config.conditionMappings || [])
        this.clearHistory()
        this.hasUnsavedChanges = false

        this.$message.success('仪表板加载成功')
      } catch (error) {
        console.error('[DESIGNER] ❌ 加载仪表板失败:', error)
        this.$message.error('加载仪表板失败: ' + (error.message || '未知错误'))
      } finally {
        clearTimeout(safetyTimeout)
        this.isLoadingDashboard = false
        
        // 立即清理遮罩
        this.clearLoadingMasks()
        
        // 立即强制启用交互，不依赖 setTimeout
        this.forceEnableInteraction()
      }
    },

    initNewDashboard() {
      this.setCurrentDashboard({
        id: null,
        dashboardName: '新建仪表板',
        theme: 'light',
        canvasConfig: {
          width: 1520,
          height: 1080,
          gridSize: 10,
          margin: { top: 20, right: 20, bottom: 20, left: 20 },
          background: { type: 'color', value: '#ffffff', opacity: 1 }
        },
        globalStyle: {
          colorScheme: ['#5470c6', '#91cc75', '#fac858'],
          titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
          fontFamily: 'Microsoft YaHei'
        },
        status: '0',
        publishedVersion: 0
      })
      this.setComponents([])
      this.setQueryConditions([])
      this.setConditionMappings([])
      this.clearHistory()
      this.hasUnsavedChanges = false
    },

    handleBack() {
      this.$router.push('/bi/dashboard').catch(error => {
        console.error('路由跳转失败:', error)
      })
    },

    handleUndo() {
      this.undo()
    },

    handleRedo() {
      this.redo()
    },

    handleNameChange(newName) {
      this.updateDashboardName(newName)
      this.recordState()
      this.hasUnsavedChanges = true
    },

    handleComponentSelect(componentId) {
      this.selectComponent(componentId)
    },

    handleComponentUpdate(component) {
      this.updateComponent(component)
      this.recordState()
      this.hasUnsavedChanges = true
    },

    handleComponentDelete(componentId) {
      this.$confirm('确定要删除该组件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.deleteComponent(componentId)
        this.recordState()
        this.$message.success('组件已删除')
      }).catch(() => {})
    },

    handleComponentCopy(componentId) {
      this.copyComponent(componentId)
      this.recordState()
      this.$message.success('组件已复制')
    },

    handleAddComponent(componentData) {
      // 查询组件特殊处理：全宽、固定高度、固定在顶部
      let size, position
      
      if (componentData.type === 'query') {
        size = { width: 1520, height: 120 } // 全宽，增加高度以容纳查询条件
        position = { x: 0, y: 0 } // 固定在顶部

        // 智能调整：将所有在顶部区域的图表组件向下移动
        const queryComponentHeight = 120
        const componentsToAdjust = this.components.filter(c =>
          c.type !== 'query' && c.position.y < queryComponentHeight
        )
        
        if (componentsToAdjust.length > 0) {
          componentsToAdjust.forEach(comp => {
            const newY = comp.position.y + queryComponentHeight
            this.updateComponent({
              id: comp.id,
              updates: {
                position: { x: comp.position.x, y: newY }
              }
            })
          })
          this.$message.info(`已自动调整 ${componentsToAdjust.length} 个组件位置，避免遮挡`)
        }
      } else {
        // 其他组件使用标准尺寸和智能布局
        size = componentData.size || { width: 760, height: 400 }
        position = componentData.position || this.findAvailablePosition(size)
      }

      const newComponent = {
        id: 'comp_' + Date.now(),
        type: componentData.type,
        name: componentData.name || this.getComponentTypeName(componentData.type),
        position: position,
        size: size,
        zIndex: this.components.length,
        dataConfig: componentData.dataConfig || {},
        styleConfig: componentData.styleConfig || (componentData.chartType ? { chartType: componentData.chartType } : {}),
        advancedConfig: componentData.advancedConfig || {}
      }

      this.addComponent(newComponent)
      this.selectComponent(newComponent.id)
      this.recordState()
    },

    findAvailablePosition(size) {
      const componentWidth = 760
      const componentHeight = 400
      const canvasConfig = this.currentDashboard.canvasConfig || { width: 1920, height: 1080 }
      const gap = 0

      // 检查是否有查询组件
      const hasQueryComponent = this.components.some(c => c.type === 'query')
      const queryComponentHeight = 120 // 查询组件的高度
      
      // 如果没有任何组件，从左上角开始
      if (this.components.length === 0) {
        return { x: 0, y: 0 }
      }

      const columns = [0, 760]
      const rowHeight = componentHeight
      const maxRows = Math.floor(canvasConfig.height / rowHeight)
      
      // 计算起始行：如果有查询组件，从查询组件下方开始
      const startY = hasQueryComponent ? queryComponentHeight : 0

      for (let row = 0; row < maxRows; row++) {
        // 如果有查询组件，第一行从 y=80 开始，否则从 y=0 开始
        const y = startY + (row * rowHeight)
        
        // 确保不超出画布高度
        if (y + componentHeight > canvasConfig.height) {
          break
        }

        for (let col = 0; col < columns.length; col++) {
          const x = columns[col]
          const testArea = { x: x, y: y, width: componentWidth, height: componentHeight }

          const occupiedAreas = this.components.map(c => ({
            x: c.position.x,
            y: c.position.y,
            width: c.size.width,
            height: c.size.height
          }))

          const hasOverlap = occupiedAreas.some(area => this.isOverlapping(testArea, area))

          if (!hasOverlap) {
            return { x, y }
          }
        }
      }

      // 如果画布已满，放在最后并提示
      this.$message.warning('画布空间不足，组件可能重叠')
      const lastRow = Math.floor(this.components.length / 2)
      const lastCol = this.components.length % 2
      return { x: columns[lastCol], y: startY + (lastRow * rowHeight) }
    },

    isOverlapping(area1, area2) {
      return !(
        area1.x + area1.width <= area2.x ||
        area2.x + area2.width <= area1.x ||
        area1.y + area1.height <= area2.y ||
        area2.y + area2.height <= area1.y
      )
    },

    handleConfigChange(configChange) {
      if (this.configTargetType === 'component') {
        const updates = {}

        if (configChange.type === 'data') {
          updates.dataConfig = configChange.config
        } else if (configChange.type === 'style') {
          updates.styleConfig = configChange.config
        } else if (configChange.type === 'advanced') {
          updates.advancedConfig = configChange.config
        }

        this.updateComponent({
          id: this.selectedComponentId,
          updates: updates
        })
      } else {
        if (configChange.type === 'dashboard') {
          const config = configChange.config
          if (config.canvasConfig) {
            this.updateCanvasConfig(config.canvasConfig)
          }
          if (config.globalStyle) {
            this.updateGlobalStyle(config.globalStyle)
          }
        } else if (configChange.type === 'style') {
          this.updateGlobalStyle(configChange.config)
        }
      }
      this.recordState()
      this.hasUnsavedChanges = true
    },

    handleQueryConfigUpdate({ conditions, mappings }) {
      this.setQueryConditions(conditions)
      this.setConditionMappings(mappings)
      this.recordState()
    },

    handleQuery(queryParams) {
      console.log('[Designer] 查询参数:', queryParams)

      // 将查询参数保存到Vuex store
      this.updateQueryValues(queryParams)

      // 主动刷新所有图表组件
      this.refreshAllCharts()

      // 显示提示信息
      this.$message.success('查询参数已更新，正在刷新图表...')
    },

    /**
     * 刷新所有图表组件
     */
    refreshAllCharts() {
      // 遍历所有图表组件，触发刷新
      const chartComponents = this.components.filter(c => c.type === 'chart')

      chartComponents.forEach(component => {
        // 通过更新 _refreshTrigger 属性来触发刷新
        const refreshTrigger = Date.now() + Math.random()
        this.updateComponent({
          id: component.id,
          updates: {
            _refreshTrigger: refreshTrigger
          }
        })
      })

      console.log(`[Designer] 已触发 ${chartComponents.length} 个图表组件刷新`)
    },

    handleResetQuery() {
      console.log('[Designer] 重置查询')

      // 清空查询参数
      this.clearQueryValues()

      this.$nextTick(() => {
        this.$message.info('查询已重置')
      })
    },

    handleRefreshChart() {

      if (!this.selectedComponentId) {
        console.warn('没有选中的组件')
        return
      }

      const component = this.components.find(c => c.id === this.selectedComponentId)

      if (!component || component.type !== 'chart') {
        console.warn('组件不存在或不是图表类型')
        return
      }

      const refreshTrigger = Date.now()

      this.updateComponent({
        id: this.selectedComponentId,
        updates: {
          _refreshTrigger: refreshTrigger
        }
      })

    },

    handlePreview() {
      const routeData = this.$router.resolve({
        path: '/bi/dashboard/preview/' + (this.dashboardId || 'temp'),
        query: { preview: true }
      })
      window.open(routeData.href, '_blank')
    },

    async handleSave() {
      if (this.saveInProgress) {
        return Promise.reject('保存中...')
      }

      this.saveInProgress = true
      try {
        const dashboardData = {
          id: this.currentDashboard.id,
          dashboardName: this.currentDashboard.dashboardName,
          theme: this.currentDashboard.theme,
          canvasConfig: JSON.stringify(this.currentDashboard.canvasConfig),
          globalStyle: JSON.stringify(this.currentDashboard.globalStyle),
          status: this.currentDashboard.status || '0'
        }

        const components = this.components.map(comp => {
          const isTempId = typeof comp.id === 'string' && comp.id.startsWith('comp_')
          
          return {
            id: isTempId ? null : comp.id,
            tempId: isTempId ? comp.id : null,
            dashboardId: this.currentDashboard.id,
            componentType: comp.type,
            componentName: comp.name,
            positionX: comp.position.x,
            positionY: comp.position.y,
            width: comp.size.width,
            height: comp.size.height,
            zIndex: comp.zIndex,
            dataConfig: JSON.stringify(comp.dataConfig || {}),
            styleConfig: JSON.stringify(comp.styleConfig || {}),
            advancedConfig: JSON.stringify(comp.advancedConfig || {})
          }
        })

        const config = {
          dashboard: dashboardData,
          components: components,
          queryConditions: this.queryConditions.map(cond => {
            const isTempId = typeof cond.id === 'string' && (cond.id.startsWith('new_') || cond.id.startsWith('cond_'))
            return {
              ...cond,
              id: isTempId ? null : cond.id,
              tempId: isTempId ? cond.id : null,
              config: typeof cond.config === 'string'
                ? cond.config
                : JSON.stringify(cond.config || {})
            }
          }),
          conditionMappings: this.conditionMappings.map(m => {
            const isTempCompId = typeof m.componentId === 'string' && m.componentId.startsWith('comp_')
            const isTempCondId = typeof m.conditionId === 'string' && (String(m.conditionId).startsWith('new') || String(m.conditionId).startsWith('cond'))
            const isTempMappingId = typeof m.id === 'string' && m.id.startsWith('mapping_')

            return {
              ...m,
              id: isTempMappingId ? null : m.id,
              componentId: isTempCompId ? null : m.componentId,
              tempComponentId: isTempCompId ? m.componentId : null,
              conditionId: isTempCondId ? null : m.conditionId,
              tempConditionId: isTempCondId ? m.conditionId : null
            }
          })
        }

        const response = await saveDashboard(config)

        if (response.code === 200) {
          this.hasUnsavedChanges = false
          this.$message.success('保存成功')

          if (!this.dashboardId && response.data && response.data.id) {
            this.dashboardId = response.data.id
            this.setCurrentDashboard({
              ...this.currentDashboard,
              id: response.data.id
            })
          }

          return Promise.resolve()
        } else {
          throw new Error(response.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败: ' + (error.message || '未知错误'))
        return Promise.reject(error)
      } finally {
        this.saveInProgress = false
      }
    },

    async handlePublish() {
      if (!this.dashboardId) {
        this.$message.warning('请先保存仪表板')
        return
      }

      if (this.hasUnsavedChanges) {
        this.$message.warning('请先保存未保存的更改')
        return
      }

      this.$confirm('确定要发布该仪表板吗？发布后将对授权用户可见。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await publishDashboard(this.dashboardId)
          this.$message.success('发布成功')

          this.setCurrentDashboard({
            ...this.currentDashboard,
            status: '1',
            publishedVersion: (this.currentDashboard.publishedVersion || 0) + 1
          })
        } catch (error) {
          console.error('发布失败:', error)
          this.$message.error('发布失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {})
    },

    handleTemplateSave(templateData) {
      this.$message.success('模板保存成功')
    },

    handleUseTemplate(template) {
      this.handleAddComponent(template.config)
    },

    parseJSON(jsonStr, defaultValue = {}) {
      if (!jsonStr) return defaultValue
      try {
        return typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
      } catch (e) {
        console.error('JSON解析失败:', e)
        return defaultValue
      }
    },

    getComponentTypeName(type) {
      const typeNames = {
        chart: '图表组件',
        query: '查询组件',
        text: '富文本组件',
        media: '媒体组件',
        tabs: '标签页组件'
      }
      return typeNames[type] || '未知组件'
    },

    cleanupPage() {
      const wpsElements = document.querySelectorAll('wps-ai-table-capture, [class*="wps_ai"], [class*="wps-ai"]')
      wpsElements.forEach(el => {
        el.style.display = 'none'
        el.style.pointerEvents = 'none'
      })

      const masks = document.querySelectorAll('.el-loading-mask, .v-modal')
      masks.forEach(mask => mask.remove())

      const rootEl = document.querySelector('.dashboard-designer-page')
      if (rootEl) {
        rootEl.style.opacity = '1'
        rootEl.style.pointerEvents = 'auto'
        rootEl.style.visibility = 'visible'
      }
    },

    // 强制启用页面交互
    forceEnableInteraction() {
      
      // 1. 移除所有可能的遮罩层
      const allMasks = document.querySelectorAll('.el-loading-mask, .v-modal, .el-mask, [class*="mask"], [class*="overlay"], [class*="loading"]')
      allMasks.forEach(mask => {
        // 跳过侧边栏和必要的元素
        if (!mask.classList.contains('sidebar-container') && 
            !mask.classList.contains('el-message') &&
            !mask.closest('.el-message')) {
          mask.remove()
        }
      })
      
      // 2. 重置页面根元素
      const page = document.querySelector('.dashboard-designer-page')
      if (page) {
        page.style.pointerEvents = 'auto'
        page.style.opacity = '1'
        page.style.visibility = 'visible'
        page.style.zIndex = 'auto'
      }
      
      // 3. 重置 body 和 html
      document.body.style.pointerEvents = 'auto'
      document.body.style.overflow = 'auto'
      document.documentElement.style.pointerEvents = 'auto'
      
      // 4. 检查并修复所有可能阻止交互的子元素
      const allElements = document.querySelectorAll('.dashboard-designer-page *')
      let fixedCount = 0
      allElements.forEach(el => {
        const style = window.getComputedStyle(el)
        // 如果元素的 pointer-events 是 none 且不是有意为之的
        if (style.pointerEvents === 'none' && 
            !el.disabled && 
            !el.classList.contains('is-disabled') &&
            !el.classList.contains('sidebar-container') &&
            el.tagName !== 'svg' && // SVG 元素可能需要 pointer-events: none
            el.tagName !== 'path') {
          el.style.pointerEvents = 'auto'
          fixedCount++
        }
      })
      
      if (fixedCount > 0) {
      }
      
      // 5. 移除所有高 z-index 的透明遮挡层
      const highZIndexElements = Array.from(document.querySelectorAll('*')).filter(el => {
        const style = window.getComputedStyle(el)
        const zIndex = parseInt(style.zIndex)
        return zIndex > 1000 && style.opacity === '0'
      })
      
      if (highZIndexElements.length > 0) {
        highZIndexElements.forEach(el => {
          if (!el.classList.contains('el-message') && !el.closest('.el-message')) {
            el.remove()
          }
        })
      }
      
      // 6. 测试点击事件 - 找出真正阻止点击的元素
      const testPoint = { x: window.innerWidth / 2, y: window.innerHeight / 2 }
      const elementAtPoint = document.elementFromPoint(testPoint.x, testPoint.y)
      if (elementAtPoint) {
        const style = window.getComputedStyle(elementAtPoint)

        // 如果这个元素阻止了点击，强制修复
        if (style.pointerEvents === 'none' || style.opacity === '0') {
          elementAtPoint.style.pointerEvents = 'auto'
          elementAtPoint.style.opacity = '1'
        }
      }
      
      
      // 7. 延迟再次检查（防止异步创建的遮罩）
      setTimeout(() => {
        const laterMasks = document.querySelectorAll('.el-loading-mask, .v-modal')
        if (laterMasks.length > 0) {
          laterMasks.forEach(mask => mask.remove())
        }

        // 再次测试点击
        const testEl = document.elementFromPoint(window.innerWidth / 2, window.innerHeight / 2)
      }, 500)
      
      // 8. 1秒后再次强制清理
      setTimeout(() => {
        document.body.style.pointerEvents = 'auto'
        document.documentElement.style.pointerEvents = 'auto'
        const page = document.querySelector('.dashboard-designer-page')
        if (page) {
          page.style.pointerEvents = 'auto'
        }
      }, 1000)
    }
  },
  mounted() {

    this.cleanupPage()
    this.clearLoadingMasks()

    // 立即强制启用交互
    this.forceEnableInteraction()

    this.$nextTick(() => {
      // 延迟加载仪表板，确保 DOM 完全就绪
      if (this.pendingDashboardId) {
        setTimeout(() => {
          this.loadDashboard(this.pendingDashboardId)
          this.pendingDashboardId = null // 清除标志
        }, 100)
      }
    })
  },

  beforeDestroy() {
    // 清理其他资源（如果有的话）
    const masks = document.querySelectorAll('.el-loading-mask, .v-modal')
    masks.forEach(mask => mask.remove())
  }
}
</script>

<style lang="scss" scoped>
.dashboard-designer-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  background-color: #f0f2f5;
  overflow: hidden;
  position: relative;
  opacity: 1 !important;
  pointer-events: auto !important;
}

.designer-main {
  display: flex;
  flex: 1;
  overflow: hidden;
  position: relative;
  width: 100%;
  height: 100%;
}

.canvas-container {
  flex: 1;
  overflow-x: auto; /* 允许水平滚动 */
  overflow-y: auto; /* 允许垂直滚动 */
  position: relative;
  min-width: 0;
  background: #f5f5f5;
}

.config-container {
  width: 400px;
  min-width: 400px;
  max-width: 400px;
  flex-shrink: 0;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 100;
  background: #ffffff;
  border-left: 2px solid #409eff;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.15);
  pointer-events: auto !important;
}
</style>

<style lang="scss">
/* 全局样式：简化对话框样式 */
.el-dialog__wrapper {
  z-index: 2005 !important;
  pointer-events: auto !important;
}

.el-dialog {
  pointer-events: auto !important;
}

.el-dialog__header,
.el-dialog__body,
.el-dialog__footer {
  pointer-events: auto !important;
}

.el-dialog__headerbtn {
  pointer-events: auto !important;
  z-index: 1;
}

.el-dialog__close {
  pointer-events: auto !important;
}

.el-dialog .el-button,
.el-dialog .el-input,
.el-dialog .el-tabs,
.el-dialog .el-table,
.el-dialog .component-card {
  pointer-events: auto !important;
}
</style>
