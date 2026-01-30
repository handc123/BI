<template>
  <div>
    <el-dialog
      title="组件库"
      :visible.sync="dialogVisible"
      width="900px"
      :close-on-click-modal="false"
      :show-close="false"
      :append-to-body="true"
      :destroy-on-close="false"
      @opened="handleOpened"
      @closed="handleClosed"
    >
    <div class="component-library">
      <!-- 自定义关闭按钮 -->
      <div class="custom-close-btn" @click="handleManualClose">
        <i class="el-icon-close"></i>
      </div>
      <!-- 分类标签 -->
      <el-tabs v-model="activeCategory" type="border-card">
        <!-- 图表组件 -->
        <el-tab-pane label="图表" name="chart">
          <div class="category-content">
            <div class="subcategory">
              <h4 class="subcategory-title">指标类型</h4>
              <div class="component-grid">
                <div
                  v-for="item in chartTypes.indicator"
                  :key="item.type"
                  class="component-card"
                  draggable="true"
                  @dragstart="handleDragStart($event, 'chart', item)"
                  @click="handleAddComponent('chart', item)"
                >
                  <div class="component-icon">
                    <i :class="item.icon"></i>
                  </div>
                  <div class="component-name">{{ item.name }}</div>
                  <div class="component-desc">{{ item.description }}</div>
                </div>
              </div>
            </div>

            <el-divider></el-divider>

            <div class="subcategory">
              <h4 class="subcategory-title">表格类型</h4>
              <div class="component-grid">
                <div
                  v-for="item in chartTypes.table"
                  :key="item.type"
                  class="component-card"
                  draggable="true"
                  @dragstart="handleDragStart($event, 'chart', item)"
                  @click="handleAddComponent('chart', item)"
                >
                  <div class="component-icon">
                    <i :class="item.icon"></i>
                  </div>
                  <div class="component-name">{{ item.name }}</div>
                  <div class="component-desc">{{ item.description }}</div>
                </div>
              </div>
            </div>

            <el-divider></el-divider>

            <div class="subcategory">
              <h4 class="subcategory-title">分析类型</h4>
              <div class="component-grid">
                <div
                  v-for="item in chartTypes.analysis"
                  :key="item.type"
                  class="component-card"
                  draggable="true"
                  @dragstart="handleDragStart($event, 'chart', item)"
                  @click="handleAddComponent('chart', item)"
                >
                  <div class="component-icon">
                    <i :class="item.icon"></i>
                  </div>
                  <div class="component-name">{{ item.name }}</div>
                  <div class="component-desc">{{ item.description }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 查询组件 -->
        <el-tab-pane label="查询" name="query">
          <div class="category-content">
            <div class="component-grid">
              <div
                class="component-card"
                draggable="true"
                @dragstart="handleDragStart($event, 'query', queryComponent)"
                @click="handleAddComponent('query', queryComponent)"
              >
                <div class="component-icon">
                  <i class="el-icon-search"></i>
                </div>
                <div class="component-name">查询组件</div>
                <div class="component-desc">提供统一的查询条件控件</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 富文本组件 -->
        <el-tab-pane label="富文本" name="text">
          <div class="category-content">
            <div class="component-grid">
              <div
                class="component-card"
                draggable="true"
                @dragstart="handleDragStart($event, 'text', textComponent)"
                @click="handleAddComponent('text', textComponent)"
              >
                <div class="component-icon">
                  <i class="el-icon-document"></i>
                </div>
                <div class="component-name">富文本</div>
                <div class="component-desc">添加文本、标题和说明</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 媒体组件 -->
        <el-tab-pane label="媒体" name="media">
          <div class="category-content">
            <div class="component-grid">
              <div
                v-for="item in mediaTypes"
                :key="item.type"
                class="component-card"
                draggable="true"
                @dragstart="handleDragStart($event, 'media', item)"
                @click="handleAddComponent('media', item)"
              >
                <div class="component-icon">
                  <i :class="item.icon"></i>
                </div>
                <div class="component-name">{{ item.name }}</div>
                <div class="component-desc">{{ item.description }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 标签页组件 -->
        <el-tab-pane label="标签页" name="tabs">
          <div class="category-content">
            <div class="component-grid">
              <div
                class="component-card"
                draggable="true"
                @dragstart="handleDragStart($event, 'tabs', tabsComponent)"
                @click="handleAddComponent('tabs', tabsComponent)"
              >
                <div class="component-icon">
                  <i class="el-icon-menu"></i>
                </div>
                <div class="component-name">标签页</div>
                <div class="component-desc">组织多个视图的容器</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 模板 -->
        <el-tab-pane label="模板" name="template">
          <div class="category-content">
            <div class="template-header">
              <el-button
                type="primary"
                size="small"
                icon="el-icon-setting"
                @click="showTemplateManage = true"
              >
                管理模板
              </el-button>
              <el-button
                size="small"
                icon="el-icon-refresh"
                @click="loadTemplates"
              >
                刷新
              </el-button>
            </div>

            <div v-if="loadingTemplates" class="loading-state">
              <i class="el-icon-loading"></i>
              <p>加载中...</p>
            </div>
            <div v-else-if="templates.length === 0" class="empty-state">
              <i class="el-icon-folder-opened"></i>
              <p>暂无保存的模板</p>
              <p class="empty-hint">在组件上右键选择"保存为模板"来创建模板</p>
            </div>
            <div v-else class="component-grid">
              <div
                v-for="template in templates"
                :key="template.id"
                class="component-card template-card"
                draggable="true"
                @dragstart="handleDragStart($event, 'template', template)"
                @click="handleAddComponent('template', template)"
              >
                <div class="component-icon">
                  <i :class="getTemplateIcon(template.componentType)"></i>
                </div>
                <div class="component-name">{{ template.templateName }}</div>
                <div class="component-desc">{{ template.templateDesc || '自定义模板' }}</div>
                <div class="template-actions">
                  <el-button
                    type="text"
                    size="mini"
                    icon="el-icon-delete"
                    @click.stop="handleDeleteTemplate(template)"
                  />
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
    </div>

  </el-dialog>

  <!-- 模板管理对话框 - 移到外层，避免嵌套 -->
  <template-manage-dialog
    v-if="showTemplateManage"
    :visible.sync="showTemplateManage"
    @template-updated="loadTemplates"
  />
  </div>
</template>

<script>
import { listComponentTemplate, delComponentTemplate } from '@/api/bi/component'
import TemplateManageDialog from '@/components/TemplateManageDialog'
import dialogModalCleanup from '@/mixins/dialogModalCleanup'

export default {
  name: 'ComponentLibrary',
  components: {
    TemplateManageDialog
  },
  mixins: [dialogModalCleanup],
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      activeCategory: 'chart',
      templates: [],
      loadingTemplates: false,
      showTemplateManage: false,
      // 图表类型定义
      chartTypes: {
        indicator: [
          { type: 'gauge', name: '仪表盘', description: '显示单个指标的进度', icon: 'el-icon-odometer' },
          { type: 'liquidfill', name: '水球图', description: '以水球形式展示百分比', icon: 'el-icon-water-cup' },
          { type: 'card', name: '指标卡', description: '突出显示关键数字', icon: 'el-icon-data-line' }
        ],
        table: [
          { type: 'table', name: '明细表', description: '显示原始数据记录', icon: 'el-icon-s-grid' },
          { type: 'summary-table', name: '汇总表', description: '显示聚合后的数据', icon: 'el-icon-s-operation' },
          { type: 'pivot-table', name: '透视表', description: '多维度数据分析', icon: 'el-icon-s-data' }
        ],
        analysis: [
          { type: 'line', name: '折线图', description: '展示趋势变化', icon: 'el-icon-data-line' },
          { type: 'bar', name: '柱状图', description: '对比不同类别', icon: 'el-icon-data-analysis' },
          { type: 'pie', name: '饼图', description: '展示占比关系', icon: 'el-icon-pie-chart' },
          { type: 'scatter', name: '散点图', description: '展示分布关系', icon: 'el-icon-s-marketing' },
          { type: 'heatmap', name: '热力图', description: '展示数据密度', icon: 'el-icon-s-platform' },
          { type: 'map', name: '地图', description: '地理数据可视化', icon: 'el-icon-location' },
          { type: 'graph', name: '关系图', description: '展示节点关系', icon: 'el-icon-share' },
          { type: 'dual-axis', name: '双轴图', description: '对比两个指标', icon: 'el-icon-s-data' }
        ]
      },
      // 查询组件
      queryComponent: {
        type: 'query',
        name: '查询组件',
        description: '提供统一的查询条件控件'
      },
      // 富文本组件
      textComponent: {
        type: 'text',
        name: '富文本',
        description: '添加文本、标题和说明'
      },
      // 媒体类型
      mediaTypes: [
        { type: 'image', name: '图片', description: '嵌入图片', icon: 'el-icon-picture' },
        { type: 'video', name: '视频', description: '嵌入视频', icon: 'el-icon-video-camera' }
      ],
      // 标签页组件
      tabsComponent: {
        type: 'tabs',
        name: '标签页',
        description: '组织多个视图的容器'
      }
    }
  },
  watch: {
    visible: {
      immediate: true,
      handler(val) {
        
        if (this.dialogVisible !== val) {
          this.dialogVisible = val
          if (val) {
            // 诊断打开前的状态
            this.diagnoseDialogState('对话框打开前')
            // 对话框打开时加载模板
            this.loadTemplates()
          } else {
            // 诊断关闭前的状态
            this.diagnoseDialogState('对话框关闭前')
          }
        }
      }
    },
    dialogVisible(val) {
      
      if (val !== this.visible) {
        this.$emit('update:visible', val)
      }
      
      // 监听对话框状态变化
      if (!val) {
        this.diagnoseDialogState('dialogVisible 变为 false')
      } else {
        this.diagnoseDialogState('dialogVisible 变为 true')
      }
    },
    activeCategory(val) {
      // 切换到模板标签时加载模板
      if (val === 'template') {
        this.loadTemplates()
      }
    }
  },
  methods: {
    // 新增：诊断对话框状态
    diagnoseDialogState(stage) {

      // 检查 body 的样式
      const bodyStyles = window.getComputedStyle(document.body)
      const bodyState = {
        pointerEvents: bodyStyles.pointerEvents,
        overflow: bodyStyles.overflow,
        paddingRight: bodyStyles.paddingRight
      }

      // 检查遮罩层
      const modals = document.querySelectorAll('.v-modal')

      // 检查对话框包装器
      const dialogs = document.querySelectorAll('.el-dialog__wrapper')

      // 检查当前对话框
      const currentDialog = this.$el?.querySelector('.el-dialog__wrapper')
      if (currentDialog) {
        const styles = window.getComputedStyle(currentDialog)
        const dialogState = {
          display: styles.display,
          visibility: styles.visibility,
          pointerEvents: styles.pointerEvents,
          zIndex: styles.zIndex
        }
      }

    },
    
    // 对话框打开后的处理（覆盖 mixin 的方法）
    handleOpened() {
      // 不做任何处理，因为已经禁用了遮罩层
      this.$nextTick(() => {
        // 确保对话框可点击
        const dialogWrapper = this.$el.querySelector('.el-dialog__wrapper')
        if (dialogWrapper) {
          dialogWrapper.style.pointerEvents = 'auto'
        } else {
          console.warn('[ComponentLibrary] dialogWrapper not found!')
        }
        const dialog = this.$el.querySelector('.el-dialog')
        if (dialog) {
          dialog.style.pointerEvents = 'auto'
        } else {
          console.warn('[ComponentLibrary] dialog not found!')
        }
        
        // 检查遮罩层
        const modals = document.querySelectorAll('.v-modal')
        if (modals.length > 0) {
          console.warn('[ComponentLibrary] Modals detected after dialog opened!')
        }
      })
    },

    // 加载模板列表
    loadTemplates() {
      this.loadingTemplates = true
      listComponentTemplate({})
        .then(response => {
          this.templates = response.rows || []
        })
        .catch(error => {
          console.error('加载模板失败:', error)
          this.$message.error('加载模板失败')
        })
        .finally(() => {
          this.loadingTemplates = false
        })
    },
    // 拖拽开始
    handleDragStart(event, componentType, componentData) {
      const data = {
        type: componentType,
        chartType: componentData.type, // 图表子类型
        name: componentData.name,
        description: componentData.description,
        // 默认尺寸
        size: this.getDefaultSize(componentType, componentData.type)
      }
      event.dataTransfer.setData('component', JSON.stringify(data))
      event.dataTransfer.effectAllowed = 'copy'
    },

    // 点击添加组件
    handleAddComponent(componentType, componentData) {
      
      const component = {
        type: componentType,
        chartType: componentData.type, // 图表子类型
        name: componentData.name,
        description: componentData.description,
        size: this.getDefaultSize(componentType, componentData.type)
      }
      
      
      this.$emit('add-component', component)
      
      
      // 添加组件后立即清理可能产生的遮罩层
      this.$nextTick(() => {
        this.cleanupModals()
        // 确保页面可交互
        document.body.style.pointerEvents = 'auto'
        const rootEl = document.querySelector('.dashboard-designer-page')
        if (rootEl) {
          rootEl.style.pointerEvents = 'auto'
        }
        
        // 检查遮罩层
        const modals = document.querySelectorAll('.v-modal')
      })
      
      this.$message.success('组件已添加到画布')
    },

    // 删除模板
    handleDeleteTemplate(template) {
      this.$confirm('确定要删除此模板吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delComponentTemplate(template.id)
          .then(() => {
            this.$message.success('删除成功')
            // 重新加载模板列表
            this.loadTemplates()
          })
          .catch(error => {
            this.$message.error('删除失败：' + (error.msg || error.message))
          })
      }).catch(() => {})
    },

    // 获取默认尺寸 - 根据组件类型设置不同尺寸
    getDefaultSize(componentType, subType) {
      // 查询控件：全宽，固定高度
      if (componentType === 'query') {
        return { width: 1520, height: 80 } // 全宽，较矮的高度
      }
      
      // 其他组件：画布固定宽度 1520px，一行两个组件，每个组件宽度为 760px
      const standardWidth = 760
      const standardHeight = 400
      
      return { width: standardWidth, height: standardHeight }
    },

    // 获取模板图标
    getTemplateIcon(componentType) {
      const iconMap = {
        chart: 'el-icon-data-line',
        query: 'el-icon-search',
        text: 'el-icon-document',
        media: 'el-icon-picture',
        tabs: 'el-icon-menu'
      }
      return iconMap[componentType] || 'el-icon-document'
    },

    // 手动关闭对话框
    handleManualClose() {
      
      // 诊断关闭前的状态
      this.diagnoseDialogState('手动关闭前')
      
      // 设置为 false，让 Element UI 处理关闭动画
      this.dialogVisible = false
      
    },
    
    // 对话框关闭后的回调（Element UI 关闭动画完成后触发）
    handleClosed() {
      
      // 确保 visible 同步
      this.$emit('update:visible', false)
      
      // 强制清理所有可能残留的元素
      this.$nextTick(() => {
        // 恢复 body 样式
        document.body.style.overflow = ''
        document.body.style.paddingRight = ''
        document.body.style.pointerEvents = 'auto'
        
        // 清理遮罩层
        this.cleanupModals()
        
        // 清理所有隐藏的对话框包装器
        const dialogWrappers = document.querySelectorAll('.el-dialog__wrapper')
        
        dialogWrappers.forEach((wrapper, index) => {
          const display = window.getComputedStyle(wrapper).display
          
          // 如果是隐藏的，直接移除
          if (display === 'none' || !wrapper.querySelector('.el-dialog')) {
            wrapper.remove()
          }
        })
        
        // 恢复页面交互
        const rootEl = document.querySelector('.dashboard-designer-page')
        if (rootEl) {
          rootEl.style.pointerEvents = 'auto'
        }
        
        this.diagnoseDialogState('handleClosed 后')
      })
      
    },
    
    // 废弃的方法，保留以防万一
    handleBeforeClose(done) {
      done()
    },
    
    handleClose() {
      this.handleManualClose()
    }
  }
}
</script>

<style scoped>
/* 自定义关闭按钮 */
.custom-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
  color: #909399;
  z-index: 10;
  background: transparent;
  border-radius: 50%;
  transition: all 0.3s;
}

.custom-close-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

/* 确保对话框内容可点击 */
.component-library {
  min-height: 500px;
  pointer-events: auto !important;
  position: relative;
}

/* 确保所有子元素可点击 */
.component-library * {
  pointer-events: auto !important;
}

.category-content {
  padding: 20px;
}

.subcategory {
  margin-bottom: 20px;
}

.subcategory-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.component-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.component-card {
  position: relative;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.component-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.component-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 12px;
}

.component-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.component-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.template-card {
  border-color: #67c23a;
}

.template-card:hover {
  border-color: #67c23a;
  box-shadow: 0 2px 12px 0 rgba(103, 194, 58, 0.2);
}

.template-card .component-icon {
  color: #67c23a;
}

.template-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.template-card:hover .template-actions {
  opacity: 1;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.empty-state p {
  font-size: 14px;
  margin: 8px 0;
}

.empty-hint {
  font-size: 12px;
  color: #c0c4cc;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-state i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #409eff;
}

.loading-state p {
  font-size: 14px;
}

.template-header {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-bottom: 20px;
}

.dialog-footer {
  text-align: right;
}

/* 拖拽时的样式 */
.component-card[draggable="true"] {
  user-select: none;
}

.component-card[draggable="true"]:active {
  opacity: 0.8;
  cursor: grabbing;
}

/* 响应式 */
@media (max-width: 768px) {
  .component-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }

  .component-card {
    padding: 16px;
  }

  .component-icon {
    font-size: 36px;
  }

  .component-name {
    font-size: 13px;
  }

  .component-desc {
    font-size: 11px;
  }
}
</style>
