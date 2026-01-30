<template>
  <div class="dashboard-designer-container">
    <el-row :gutter="20">
      <!-- 左侧：基础信息和组件库 -->
      <el-col :span="5">
        <el-card shadow="never" class="designer-card">
          <div slot="header" class="clearfix">
            <span>基础信息</span>
          </div>
          <el-form :model="dashboard" label-width="80px" size="small">
            <el-form-item label="仪表板名称">
              <el-input v-model="dashboard.name" placeholder="请输入仪表板名称" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="dashboard.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="dashboard.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="designer-card" style="margin-top: 10px">
          <div slot="header" class="clearfix">
            <span>可视化组件</span>
          </div>
          <div class="component-library">
            <el-button
              v-for="viz in visualizationList"
              :key="viz.id"
              class="component-item"
              size="small"
              @click="addComponent(viz)"
            >
              <i :class="getVisualizationIcon(viz.type)"></i>
              {{ viz.name }}
            </el-button>
          </div>
        </el-card>

        <el-card shadow="never" class="designer-card" style="margin-top: 10px">
          <div slot="header" class="clearfix">
            <span>全局筛选器</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              @click="addGlobalFilter"
            >
              <i class="el-icon-plus"></i> 添加
            </el-button>
          </div>
          <bi-global-filter-config v-model="dashboard.filterConfig" />
        </el-card>

        <el-card shadow="never" class="designer-card" style="margin-top: 10px">
          <div slot="header" class="clearfix">
            <span>主题配置</span>
          </div>
          <bi-theme-config v-model="dashboard.themeConfig" />
        </el-card>
      </el-col>

      <!-- 中间：画布区域 -->
      <el-col :span="14">
        <el-card shadow="never" class="designer-card canvas-card">
          <div slot="header" class="clearfix">
            <span>仪表板画布</span>
            <div style="float: right">
              <el-button-group>
                <el-button size="mini" @click="handleUndo" :disabled="!canUndo">
                  <i class="el-icon-refresh-left"></i> 撤销
                </el-button>
                <el-button size="mini" @click="handleRedo" :disabled="!canRedo">
                  <i class="el-icon-refresh-right"></i> 重做
                </el-button>
              </el-button-group>
              <el-button size="mini" @click="handlePreview" style="margin-left: 10px">
                <i class="el-icon-view"></i> 预览
              </el-button>
              <el-button size="mini" @click="handleClear" style="margin-left: 5px">
                <i class="el-icon-delete"></i> 清空
              </el-button>
            </div>
          </div>
          <div class="canvas-container" ref="canvasContainer">
            <grid-layout
              :layout.sync="layoutComponents"
              :col-num="12"
              :row-height="30"
              :is-draggable="true"
              :is-resizable="true"
              :is-mirrored="false"
              :vertical-compact="true"
              :margin="[10, 10]"
              :use-css-transforms="true"
              @layout-updated="handleLayoutUpdated"
            >
              <grid-item
                v-for="item in layoutComponents"
                :key="item.i"
                :x="item.x"
                :y="item.y"
                :w="item.w"
                :h="item.h"
                :i="item.i"
                @resized="handleComponentResized"
                @moved="handleComponentMoved"
              >
                <div class="component-wrapper" :class="{ 'selected': selectedComponentId === item.i }">
                  <div class="component-header">
                    <span class="component-title">{{ item.name }}</span>
                    <div class="component-actions">
                      <el-tooltip content="配置查询条件" placement="top">
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-search"
                          @click.stop="handleQueryConditionConfig(item)"
                        />
                      </el-tooltip>
                      <el-tooltip content="组件配置" placement="top">
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-setting"
                          @click="handleComponentConfig(item)"
                        />
                      </el-tooltip>
                      <el-tooltip content="删除组件" placement="top">
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-delete"
                          @click.stop="handleComponentRemove(item)"
                        />
                      </el-tooltip>
                    </div>
                  </div>
                  <div class="component-content" @click="selectComponent(item.i)">
                    <bi-component-preview
                      :visualization-id="item.visualizationId"
                      :filters="getComponentFilters(item.i)"
                      :theme="dashboard.themeConfig"
                    />
                  </div>
                </div>
              </grid-item>
            </grid-layout>
            <div v-if="layoutComponents.length === 0" class="empty-canvas">
              <i class="el-icon-plus"></i>
              <p>从左侧拖拽或点击可视化组件添加到画布</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：组件配置 -->
      <el-col :span="5">
        <el-card shadow="never" class="designer-card">
          <div slot="header" class="clearfix">
            <span>组件配置</span>
          </div>
          <div v-if="selectedComponent">
            <el-form :model="selectedComponent" label-width="80px" size="small">
              <el-form-item label="组件名称">
                <el-input v-model="selectedComponent.name" />
              </el-form-item>
              <el-form-item label="位置X">
                <el-input-number v-model="selectedComponent.x" :min="0" :max="11" style="width: 100%" />
              </el-form-item>
              <el-form-item label="位置Y">
                <el-input-number v-model="selectedComponent.y" :min="0" style="width: 100%" />
              </el-form-item>
              <el-form-item label="宽度">
                <el-input-number v-model="selectedComponent.w" :min="1" :max="12" style="width: 100%" />
              </el-form-item>
              <el-form-item label="高度">
                <el-input-number v-model="selectedComponent.h" :min="1" style="width: 100%" />
              </el-form-item>
            </el-form>

            <el-divider content-position="left">组件联动</el-divider>
            <bi-component-linkage-config
              :component-id="selectedComponent.i"
              :components="layoutComponents"
              v-model="dashboard.layoutConfig.linkages"
            />
          </div>
          <el-empty v-else description="请选择一个组件进行配置" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 预览对话框 -->
    <el-dialog title="仪表板预览" :visible.sync="previewDialogVisible" width="95%" top="5vh" append-to-body fullscreen>
      <dashboard-preview
        v-if="previewDialogVisible && dashboardId"
        :dashboard-id="dashboardId"
        @close="previewDialogVisible = false"
      />
      <div v-else-if="previewDialogVisible && !dashboardId" style="padding: 40px; text-align: center">
        <el-alert
          title="无法预览"
          description="请先保存仪表板后再进行预览"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
    </el-dialog>

    <!-- 查询条件配置对话框 -->
    <query-condition-config
      :visible.sync="queryConditionDialogVisible"
      :component-id="selectedComponentForCondition && selectedComponentForCondition.componentId || null"
      :dashboard-id="dashboardId"
      :dataset-id="selectedComponentForCondition && selectedComponentForCondition.dataConfig && selectedComponentForCondition.dataConfig.datasetId || null"
      :layout-components="layoutComponents"
      @update="handleConditionUpdate"
    />
  </div>
</template>

<script>
import { GridLayout, GridItem } from 'vue-grid-layout'
import { listVisualization } from '@/api/bi/visualization'
import { getDashboardConfig, saveDashboardConfig } from '@/api/bi/dashboard'
import BiGlobalFilterConfig from '@/components/BiGlobalFilterConfig'
import BiThemeConfig from '@/components/BiThemeConfig'
import BiComponentLinkageConfig from '@/components/BiComponentLinkageConfig'
import BiComponentPreview from '@/components/BiComponentPreview'
import DashboardPreview from '@/views/bi/dashboard/preview'
import QueryConditionConfig from '@/components/QueryConditionConfig'

export default {
  name: 'BiDashboardDesigner',
  components: {
    GridLayout,
    GridItem,
    BiGlobalFilterConfig,
    BiThemeConfig,
    BiComponentLinkageConfig,
    BiComponentPreview,
    DashboardPreview,
    QueryConditionConfig
  },
  props: {
    dashboardId: {
      type: Number,
      default: null
    },
    dashboardData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      dashboard: {
        id: null,
        name: '',
        dashboardName: '',
        theme: 'light',
        canvasConfig: {
          width: 1920,
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
        layoutConfig: {
          components: [],
          linkages: []
        },
        filterConfig: {
          filters: []
        },
        themeConfig: {
          colorScheme: 'default',
          backgroundColor: '#f5f5f5',
          fontFamily: 'Microsoft YaHei',
          fontSize: 14
        },
        status: '0',
        remark: ''
      },
      layoutComponents: [],
      components: [],
      queryConditions: [],
      conditionMappings: [],
      visualizationList: [],
      selectedComponentId: null,
      selectedComponent: null,
      selectedComponentForCondition: null,
      previewDialogVisible: false,
      queryConditionDialogVisible: false,
      history: [],
      historyIndex: -1,
      nextComponentId: 1,
      loading: false,
      isDirty: false,
      invalidComponents: []
    }
  },
  computed: {
    canUndo() {
      return this.historyIndex > 0
    },
    canRedo() {
      return this.historyIndex < this.history.length - 1
    }
  },
  watch: {
    layoutComponents: {
      handler() {
        this.isDirty = true
      },
      deep: true
    },
    dashboard: {
      handler() {
        this.isDirty = true
      },
      deep: true
    }
  },
  created() {
    this.initDashboard()
    this.loadVisualizations()
  },
  methods: {
    /**
     * 初始化仪表板 - 加载配置
     */
    async initDashboard() {
      if (this.dashboardId) {
        await this.loadDashboardConfig()
      } else if (this.dashboardData && this.dashboardData.id) {
        this.dashboard = { ...this.dashboard, ...this.dashboardData }
        this.parseConfigurations()
      }
      
      this.saveHistory()
      this.isDirty = false
    },

    /**
     * 加载仪表板完整配置
     */
    async loadDashboardConfig() {
      try {
        this.loading = true
        const response = await getDashboardConfig(this.dashboardId)
        const config = response.data

        // 加载仪表板基本信息
        if (config.dashboard) {
          this.dashboard = {
            ...this.dashboard,
            ...config.dashboard
          }
          this.parseConfigurations()
        }

        // 加载组件列表
        if (config.components && config.components.length > 0) {
          this.components = config.components
          this.layoutComponents = this.convertComponentsToLayout(config.components)
          
          // 过滤无效组件
          this.filterInvalidComponents()
        }

        // 加载查询条件
        if (config.queryConditions) {
          this.queryConditions = config.queryConditions
        }

        // 加载条件映射
        if (config.conditionMappings) {
          this.conditionMappings = config.conditionMappings
        }

        this.$message.success('仪表板配置加载成功')
      } catch (error) {
        console.error('加载仪表板配置失败:', error)
        this.$message.error('加载仪表板配置失败: ' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },

    /**
     * 解析JSON配置字符串
     */
    parseConfigurations() {
      // 解析画布配置
      if (typeof this.dashboard.canvasConfig === 'string') {
        try {
          this.dashboard.canvasConfig = JSON.parse(this.dashboard.canvasConfig)
        } catch (e) {
          console.warn('解析canvasConfig失败:', e)
        }
      }

      // 解析全局样式
      if (typeof this.dashboard.globalStyle === 'string') {
        try {
          this.dashboard.globalStyle = JSON.parse(this.dashboard.globalStyle)
        } catch (e) {
          console.warn('解析globalStyle失败:', e)
        }
      }

      // 解析布局配置
      if (typeof this.dashboard.layoutConfig === 'string') {
        try {
          this.dashboard.layoutConfig = JSON.parse(this.dashboard.layoutConfig)
        } catch (e) {
          console.warn('解析layoutConfig失败:', e)
        }
      }

      // 解析筛选器配置
      if (typeof this.dashboard.filterConfig === 'string') {
        try {
          this.dashboard.filterConfig = JSON.parse(this.dashboard.filterConfig)
        } catch (e) {
          console.warn('解析filterConfig失败:', e)
        }
      }

      // 解析主题配置
      if (typeof this.dashboard.themeConfig === 'string') {
        try {
          this.dashboard.themeConfig = JSON.parse(this.dashboard.themeConfig)
        } catch (e) {
          console.warn('解析themeConfig失败:', e)
        }
      }
    },

    /**
     * 将后端组件数据转换为布局组件
     */
    convertComponentsToLayout(components) {
      return components.map(comp => {
        // 解析数据配置
        let dataConfig = {}
        if (typeof comp.dataConfig === 'string') {
          try {
            dataConfig = JSON.parse(comp.dataConfig)
          } catch (e) {
            console.warn('解析组件dataConfig失败:', e)
          }
        } else {
          dataConfig = comp.dataConfig || {}
        }

        return {
          i: `comp_${comp.id}`,
          x: Math.floor(comp.positionX / 10),
          y: Math.floor(comp.positionY / 10),
          w: Math.floor(comp.width / 10),
          h: Math.floor(comp.height / 10),
          visualizationId: dataConfig.datasetId || dataConfig.visualizationId,
          name: comp.componentName,
          type: comp.componentType,
          componentId: comp.id,
          dataConfig: dataConfig
        }
      })
    },

    /**
     * 过滤无效组件并显示警告
     */
    filterInvalidComponents() {
      this.invalidComponents = []
      
      this.layoutComponents = this.layoutComponents.filter(comp => {
        // 检查组件是否有有效的可视化ID
        if (!comp.visualizationId) {
          this.invalidComponents.push({
            name: comp.name,
            reason: '缺少可视化ID'
          })
          return false
        }
        return true
      })

      // 显示警告
      if (this.invalidComponents.length > 0) {
        const message = `发现 ${this.invalidComponents.length} 个无效组件已被跳过: ${this.invalidComponents.map(c => c.name).join(', ')}`
        this.$message.warning(message)
      }

      // 更新nextComponentId
      if (this.layoutComponents.length > 0) {
        const maxId = Math.max(...this.layoutComponents.map(c => {
          const match = c.i.match(/comp_(\d+)/)
          return match ? parseInt(match[1]) : 0
        }))
        this.nextComponentId = maxId + 1
      }
    },

    /**
     * 保存仪表板配置
     */
    async saveDashboard() {
      if (!this.dashboardId) {
        this.$message.error('仪表板ID不存在,无法保存')
        return false
      }

      try {
        this.loading = true

        // 序列化配置
        const config = this.serializeConfiguration()

        // 调用保存接口
        await saveDashboardConfig(this.dashboardId, config)

        this.isDirty = false
        this.$message.success('仪表板配置保存成功')
        return true
      } catch (error) {
        console.error('保存仪表板配置失败:', error)
        this.$message.error('保存仪表板配置失败: ' + (error.message || '未知错误'))
        return false
      } finally {
        this.loading = false
      }
    },

    /**
     * 序列化配置为后端格式
     */
    serializeConfiguration() {
      // 更新仪表板基本信息
      const dashboard = {
        id: this.dashboardId,
        dashboardName: this.dashboard.name || this.dashboard.dashboardName,
        theme: this.dashboard.theme,
        canvasConfig: JSON.stringify(this.dashboard.canvasConfig),
        globalStyle: JSON.stringify(this.dashboard.globalStyle),
        status: this.dashboard.status,
        remark: this.dashboard.remark
      }

      // 转换布局组件为后端组件格式
      const components = this.layoutComponents.map((comp, index) => {
        return {
          id: comp.componentId,
          dashboardId: this.dashboardId,
          componentType: comp.type || 'chart',
          componentName: comp.name,
          positionX: comp.x * 10,
          positionY: comp.y * 10,
          width: comp.w * 10,
          height: comp.h * 10,
          zIndex: index,
          dataConfig: JSON.stringify(comp.dataConfig || {}),
          styleConfig: JSON.stringify(comp.styleConfig || {}),
          advancedConfig: JSON.stringify(comp.advancedConfig || {})
        }
      })

      return {
        dashboard,
        components,
        queryConditions: this.queryConditions,
        conditionMappings: this.conditionMappings
      }
    },
    loadVisualizations() {
      listVisualization({ pageNum: 1, pageSize: 100 }).then(response => {
        this.visualizationList = response.rows || []
      })
    },
    addComponent(visualization) {
      const newComponent = {
        i: `comp_${this.nextComponentId++}`,
        x: 0,
        y: this.layoutComponents.length * 4,
        w: 6,
        h: 8,
        visualizationId: visualization.id,
        name: visualization.name,
        type: visualization.type,
        dataConfig: {
          datasetId: visualization.datasetId,
          visualizationId: visualization.id
        }
      }
      
      this.layoutComponents.push(newComponent)
      this.saveHistory()
      this.$message.success('组件已添加到画布')
    },
    handleComponentConfig(component) {
      this.selectComponent(component.i)
    },

    /**
     * 打开查询条件配置
     */
    handleQueryConditionConfig(component) {
      // 如果组件没有 componentId，提示用户先保存
      if (!component.componentId) {
        this.$message.warning('请先保存仪表板后再配置查询条件')
        return
      }

      this.selectedComponentForCondition = component
      this.queryConditionDialogVisible = true
    },

    /**
     * 处理查询条件配置更新
     */
    handleConditionUpdate(data) {
      this.$message.success('查询条件配置已更新')
      this.isDirty = true
    },

    handleComponentRemove(component) {
      this.$confirm('确定要删除此组件吗？删除组件将同时删除关联的查询条件配置。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const index = this.layoutComponents.findIndex(c => c.i === component.i)
        if (index > -1) {
          // 如果组件有 componentId，需要级联删除查询条件
          if (component.componentId) {
            await this.deleteComponentConditions(component.componentId)
          }

          this.layoutComponents.splice(index, 1)
          if (this.selectedComponentId === component.i) {
            this.selectedComponentId = null
            this.selectedComponent = null
          }
          this.saveHistory()
          this.$message.success('组件已删除')
        }
      }).catch(() => {})
    },

    /**
     * 删除组件的查询条件（级联删除）
     */
    async deleteComponentConditions(componentId) {
      try {
        const { deleteCondition } = await import('@/api/bi/condition')

        // 获取该组件的所有查询条件
        const { listConditions } = await import('@/api/bi/condition')
        const response = await listConditions(componentId)

        if (response.code === 200 && response.rows && response.rows.length > 0) {
          // 删除所有条件
          const deletePromises = response.rows.map(condition =>
            deleteCondition(condition.id)
          )
          await Promise.all(deletePromises)
        }
      } catch (error) {
        console.error('删除组件查询条件失败:', error)
        // 不阻断组件删除流程
      }
    },
    selectComponent(componentId) {
      this.selectedComponentId = componentId
      this.selectedComponent = this.layoutComponents.find(c => c.i === componentId)
    },
    handleLayoutUpdated(newLayout) {
      this.saveHistory()
    },
    handleComponentResized(i, newH, newW, newHPx, newWPx) {
      this.saveHistory()
    },
    handleComponentMoved(i, newX, newY) {
      this.saveHistory()
    },
    addGlobalFilter() {
      if (!this.dashboard.filterConfig.filters) {
        this.dashboard.filterConfig.filters = []
      }
      this.dashboard.filterConfig.filters.push({
        id: `filter_${Date.now()}`,
        type: 'dateRange',
        label: '新筛选器',
        defaultValue: null,
        targetFields: []
      })
    },
    getComponentFilters(componentId) {
      // 获取应用到此组件的全局筛选器
      const filters = []
      if (this.dashboard.filterConfig && this.dashboard.filterConfig.filters) {
        this.dashboard.filterConfig.filters.forEach(filter => {
          if (filter.targetFields) {
            const target = filter.targetFields.find(t => t.componentId === componentId)
            if (target) {
              filters.push({
                field: target.field,
                operator: 'eq',
                value: filter.defaultValue
              })
            }
          }
        })
      }
      return filters
    },
    handlePreview() {
      this.previewDialogVisible = true
    },
    handleClear() {
      this.$confirm('确定要清空画布吗？此操作不可恢复。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.layoutComponents = []
        this.selectedComponentId = null
        this.selectedComponent = null
        this.saveHistory()
        this.$message.success('画布已清空')
      }).catch(() => {})
    },
    saveHistory() {
      const state = JSON.parse(JSON.stringify({
        layoutComponents: this.layoutComponents,
        dashboard: this.dashboard
      }))
      
      // 删除当前索引之后的历史记录
      this.history = this.history.slice(0, this.historyIndex + 1)
      this.history.push(state)
      this.historyIndex++
      
      // 限制历史记录数量
      if (this.history.length > 50) {
        this.history.shift()
        this.historyIndex--
      }
    },
    handleUndo() {
      if (this.canUndo) {
        this.historyIndex--
        this.restoreState(this.history[this.historyIndex])
      }
    },
    handleRedo() {
      if (this.canRedo) {
        this.historyIndex++
        this.restoreState(this.history[this.historyIndex])
      }
    },
    restoreState(state) {
      this.layoutComponents = JSON.parse(JSON.stringify(state.layoutComponents))
      this.dashboard = JSON.parse(JSON.stringify(state.dashboard))
    },
    getVisualizationIcon(type) {
      const iconMap = {
        kpi: 'el-icon-data-line',
        line: 'el-icon-data-line',
        bar: 'el-icon-data-analysis',
        pie: 'el-icon-pie-chart',
        donut: 'el-icon-pie-chart',
        table: 'el-icon-s-grid',
        map: 'el-icon-location',
        funnel: 'el-icon-sort'
      }
      return iconMap[type] || 'el-icon-document'
    },

    /**
     * 获取数据供父组件保存
     */
    getData() {
      // 更新布局配置
      this.dashboard.layoutConfig.components = this.layoutComponents
      
      return {
        name: this.dashboard.name || this.dashboard.dashboardName,
        dashboardName: this.dashboard.name || this.dashboard.dashboardName,
        theme: this.dashboard.theme,
        canvasConfig: JSON.stringify(this.dashboard.canvasConfig),
        globalStyle: JSON.stringify(this.dashboard.globalStyle),
        layoutConfig: JSON.stringify(this.dashboard.layoutConfig),
        filterConfig: JSON.stringify(this.dashboard.filterConfig),
        themeConfig: JSON.stringify(this.dashboard.themeConfig),
        status: this.dashboard.status,
        remark: this.dashboard.remark
      }
    },

    /**
     * 检查是否有未保存的更改
     */
    hasUnsavedChanges() {
      return this.isDirty
    },

    /**
     * 提示未保存更改
     */
    promptUnsavedChanges() {
      if (this.isDirty) {
        return this.$confirm('您有未保存的更改,是否保存？', '提示', {
          confirmButtonText: '保存',
          cancelButtonText: '不保存',
          type: 'warning'
        })
      }
      return Promise.resolve()
    }
  }
}
</script>

<style scoped>
.dashboard-designer-container {
  height: calc(100vh - 200px);
  overflow: auto;
}

.designer-card {
  margin-bottom: 10px;
}

.canvas-card {
  height: calc(100vh - 220px);
}

.canvas-container {
  height: calc(100vh - 300px);
  background: #f5f5f5;
  border: 1px dashed #ddd;
  border-radius: 4px;
  position: relative;
  overflow: auto;
}

.component-library {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.component-item {
  width: 100%;
  text-align: left;
  justify-content: flex-start;
}

.component-wrapper {
  height: 100%;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s;
}

.component-wrapper:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.component-wrapper.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.component-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.component-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.component-actions {
  display: flex;
  gap: 4px;
}

.component-content {
  height: calc(100% - 41px);
  padding: 10px;
  overflow: auto;
}

.empty-canvas {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #909399;
}

.empty-canvas i {
  font-size: 48px;
  margin-bottom: 10px;
}

.empty-canvas p {
  font-size: 14px;
}
</style>
