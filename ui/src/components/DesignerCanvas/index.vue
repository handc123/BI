<template>
  <!-- 画布外层容器 - 负责滚动 -->
  <div class="designer-canvas-wrapper">
    <!-- 实际画布区域 - 固定 1520px × 1080px -->
    <div
      class="designer-canvas"
      :style="canvasStyle"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @click="handleCanvasClick"
    >
    <!-- 网格背景 -->
    <div 
      v-if="showGrid" 
      class="grid-background"
      :style="gridStyle"
    ></div>

    <!-- 对齐参考线 -->
    <div v-for="guide in alignmentGuides" :key="guide.id" class="alignment-guide" :class="guide.type" :style="getGuideStyle(guide)"></div>

    <!-- 网格吸附区域提示 -->
    <div v-if="showSnapZones && draggingComponentId" class="snap-zones">
      <div class="snap-zone column-1" :style="{ left: '0px', width: '760px' }"></div>
      <div class="snap-zone column-2" :style="{ left: '760px', width: '760px' }"></div>
    </div>

    <!-- 多选对齐工具栏 -->
    <div v-if="selectedComponentIds && selectedComponentIds.length > 1" class="alignment-toolbar">
      <el-button-group>
        <el-button size="mini" icon="el-icon-d-arrow-left" @click="alignSelectedComponents('left')" title="左对齐"></el-button>
        <el-button size="mini" icon="el-icon-d-arrow-right" @click="alignSelectedComponents('right')" title="右对齐"></el-button>
        <el-button size="mini" icon="el-icon-d-arrow-top" @click="alignSelectedComponents('top')" title="顶部对齐"></el-button>
        <el-button size="mini" icon="el-icon-d-arrow-bottom" @click="alignSelectedComponents('bottom')" title="底部对齐"></el-button>
        <el-button size="mini" icon="el-icon-minus" @click="alignSelectedComponents('centerH')" title="水平居中"></el-button>
        <el-button size="mini" icon="el-icon-minus" style="transform: rotate(90deg);" @click="alignSelectedComponents('centerV')" title="垂直居中"></el-button>
      </el-button-group>
    </div>

    <!-- 组件列表 -->
    <div
      v-for="component in components"
      :key="component.id"
      class="component-item"
      :class="{
        'selected': selectedComponentId === component.id || (selectedComponentIds && selectedComponentIds.includes(component.id)),
        'dragging': draggingComponentId === component.id,
        'query-component': component.type === 'query',
        'has-query-component': component.type === 'query'
      }"
      :style="getComponentStyle(component)"
      @click.stop="handleComponentClick(component)"
    >
      <!-- 组件内容 -->
      <div class="component-content">
        <!-- 查询组件不显示标题栏 -->
        <div v-if="component.type !== 'query'" class="component-header" @mousedown="handleComponentMouseDown($event, component)">
          <span class="component-title">
            {{ component.name || getComponentTypeName(component.type) }}
          </span>
          <div class="component-actions" @mousedown.stop>
            <el-dropdown @command="(cmd) => handleComponentCommand(cmd, component)" trigger="click" size="mini">
              <el-button type="text" size="mini" icon="el-icon-more" title="更多操作" />
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="copy">
                  <i class="el-icon-document-copy"></i> 复制
                </el-dropdown-item>
                <el-dropdown-item command="saveAsTemplate">
                  <i class="el-icon-folder-add"></i> 保存为模板
                </el-dropdown-item>
                <el-dropdown-item divided command="toFront">
                  <i class="el-icon-top"></i> 置于顶层
                </el-dropdown-item>
                <el-dropdown-item command="toBack">
                  <i class="el-icon-bottom"></i> 置于底层
                </el-dropdown-item>
                <el-dropdown-item command="bringForward">
                  <i class="el-icon-caret-top"></i> 上移一层
                </el-dropdown-item>
                <el-dropdown-item command="sendBackward">
                  <i class="el-icon-caret-bottom"></i> 下移一层
                </el-dropdown-item>
                <el-dropdown-item divided command="delete" style="color: #f56c6c;">
                  <i class="el-icon-delete"></i> 删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
        <div class="component-body" :class="{ 'has-query-component': component.type === 'query' }">
          <slot :component="component">
            <!-- 根据组件类型渲染实际组件 -->
            <chart-widget
              v-if="component.type === 'chart'"
              :config="component"
              :query-params="queryValues"
              :query-conditions="queryConditions"
              :condition-mappings="conditionMappings"
              :is-edit-mode="true"
              :widget-style="{ width: '100%', height: '100%' }"
              @config="handleComponentClick(component)"
            />
            <!-- 查询组件特殊渲染 -->
            <div v-else-if="component.type === 'query'" class="query-component-wrapper">
              <!-- 编辑模式下的配置按钮 -->
              <div v-if="isEditMode" class="query-config-btn" @click="handleAddQueryCondition">
                <el-button
                  type="primary"
                  icon="el-icon-setting"
                  size="mini"
                  circle
                  title="配置查询条件"
                />
              </div>

              <!-- 查询组件 -->
              <query-widget
                :conditions="storeQueryConditions"
                :is-edit-mode="false"
                :style-config="component.styleConfig || {}"
                @query="handleQuery"
                @reset="handleReset"
              />
            </div>
            <div v-else class="component-placeholder">
              <i :class="getComponentIcon(component.type)"></i>
              <p>{{ getComponentTypeName(component.type) }}</p>
            </div>
          </slot>
        </div>
      </div>

      <!-- 调整大小手柄 - 查询组件不显示 -->
      <div
        v-if="selectedComponentId === component.id && component.type !== 'query'"
        class="resize-handles"
      >
        <div
          v-for="handle in resizeHandles"
          :key="handle"
          :class="['resize-handle', `resize-${handle}`]"
          @mousedown.stop="handleResizeStart($event, component, handle)"
        ></div>
      </div>
    </div>

    <!-- 空画布提示 -->
    <div v-if="components.length === 0" class="empty-canvas">
      <i class="el-icon-plus"></i>
      <p>从组件库拖拽组件到画布</p>
      <p class="empty-hint">或点击工具栏的"组件库"按钮添加组件</p>
    </div>
    </div>
  </div>
</template>

<script>
import { snapToGrid } from '@/utils/gridUtils'
import { calculateAlignmentGuides } from '@/utils/alignmentUtils'
import ChartWidget from '@/components/ChartWidget'
import QueryWidget from '@/components/QueryWidget'
import { mapState } from 'vuex'

export default {
  name: 'DesignerCanvas',
  components: {
    ChartWidget,
    QueryWidget
  },
  props: {
    components: {
      type: Array,
      default: () => []
    },
    queryConditions: {
      type: Array,
      default: () => []
    },
    gridSize: {
      type: Number,
      default: 10
    },
    canvasConfig: {
      type: Object,
      default: () => ({
        width: 1920,
        height: 1080,
        background: { type: 'color', value: '#ffffff', opacity: 1 },
        margin: { top: 20, right: 20, bottom: 20, left: 20 }
      })
    },
    selectedComponentId: {
      type: [String, Number],  // 接受字符串或数字
      default: null
    },
    selectedComponentIds: {
      type: Array,
      default: () => []
    },
    showGrid: {
      type: Boolean,
      default: true
    },
    isEditMode: {
      type: Boolean,
      default: true
    }
  },
  watch: {
    components: {
      handler(newVal, oldVal) {
        // 暂时禁用 deep watch，避免性能问题
        // 只监听数组引用变化，不监听内部对象变化
      },
      deep: false,
      immediate: false // 改为 false，避免初始化时触发
    }
  },
  data() {
    return {
      draggingComponentId: null,
      dragStartPos: null,
      dragStartComponentPos: null,
      resizingComponent: null,
      resizeHandle: null,
      resizeStartPos: null,
      resizeStartSize: null,
      alignmentGuides: [],
      resizeHandles: ['nw', 'n', 'ne', 'e', 'se', 's', 'sw', 'w'],
      showSnapZones: true, // 是否显示吸附区域提示
      updateThrottle: null // 节流定时器
    }
  },
  computed: {
    ...mapState('dashboard', ['queryValues']),
    ...mapState('components', ['conditionMappings']),

    // 从 Vuex store 获取查询条件（优先使用 store，否则使用 props）
    // 注意：查询条件保存在 components 模块中，不是 dashboard 模块
    storeQueryConditions() {
      const conditions = this.$store.state.components.queryConditions || []
      console.log('[DesignerCanvas] storeQueryConditions computed:', conditions.length, '个条件')
      return conditions
    },

    canvasStyle() {
      const config = this.canvasConfig || {}
      const style = {
        // 移除固定宽度，让画布自适应容器
        // width 由 CSS 的 100% 控制
        height: `${config.height || 1080}px`
      }

      if (config.background) {
        if (config.background.type === 'color') {
          style.backgroundColor = config.background.value || '#ffffff'
          if (config.background.opacity !== undefined) {
            style.opacity = config.background.opacity
          }
        } else if (config.background.type === 'image') {
          style.backgroundImage = `url(${config.background.value})`
          style.backgroundSize = 'cover'
          style.backgroundPosition = 'center'
        }
      }

      return style
    },
    gridStyle() {
      const gridColor = this.canvasConfig.gridColor || '#e0e0e0'
      const rowHeight = 400
      const columnDividerColor = this.canvasConfig.columnDividerColor || '#409eff'

      // 创建两列布局的网格背景 - 使用百分比
      // 使用蓝色粗线标记列边界（0%, 50%, 100%），灰色细线标记行
      return {
        backgroundSize: `50% ${rowHeight}px`, // 列宽50%，行高400px
        backgroundImage: `
          /* 列边界线 - 蓝色，更粗 */
          linear-gradient(to right, ${columnDividerColor} 2px, transparent 2px),
          /* 行网格线 - 灰色，更细 */
          linear-gradient(to bottom, ${gridColor} 1px, transparent 1px)
        `,
        backgroundPosition: `0 0, 0 0`,
        opacity: 0.5
      }
    }
  },
  methods: {
    // 打开查询条件配置对话框
    handleAddQueryCondition() {
      this.$emit('open-query-config')
    },

    // 处理查询
    handleQuery(queryParams) {
      this.$emit('query', queryParams)
    },

    // 处理重置
    handleReset() {
      this.$emit('reset-query')
    },

    // 拖放处理
    handleDrop(event) {
      event.preventDefault()
      
      const data = event.dataTransfer.getData('component')
      if (!data) return

      try {
        const componentData = JSON.parse(data)
        
        // 使用智能布局算法找到合适的位置
        const position = this.findAvailablePosition(componentData.size)

        this.$emit('component-add', {
          ...componentData,
          position: position
        })
      } catch (error) {
        console.error('Drop error:', error)
        this.$message.error('添加组件失败')
      }
    },

    // 智能布局：找到可用的位置（两列布局）
    findAvailablePosition(size) {
      const componentWidth = 760 // 固定宽度，一行两个
      const componentHeight = 400 // 固定高度
      const canvasWidth = 1520 // 画布实际可用宽度（1920 - 400配置面板）
      const gap = 0 // 组件之间无间隔

      // 如果没有组件，从左上角开始
      if (this.components.length === 0) {
        return { x: 0, y: 0 }
      }

      // 两列布局：第一列 x=0，第二列 x=760
      const columns = [0, 760]
      
      // 按行计算，每行两个组件
      const rowHeight = componentHeight
      const maxRows = Math.floor(this.canvasConfig.height / rowHeight)
      
      // 遍历每一行的每一列
      for (let row = 0; row < maxRows; row++) {
        const y = row * rowHeight
        
        for (let col = 0; col < columns.length; col++) {
          const x = columns[col]
          const testArea = {
            x: x,
            y: y,
            width: componentWidth,
            height: componentHeight
          }

          // 检查是否与现有组件重叠
          const occupiedAreas = this.components.map(c => ({
            x: c.position.x,
            y: c.position.y,
            width: c.size.width,
            height: c.size.height
          }))

          const hasOverlap = occupiedAreas.some(area => 
            this.isOverlapping(testArea, area)
          )

          if (!hasOverlap) {
            return { x, y }
          }
        }
      }

      // 如果画布已满，放在最后并提示
      this.$message.warning('画布空间不足，组件可能重叠')
      const lastRow = Math.floor(this.components.length / 2)
      const lastCol = this.components.length % 2
      return { 
        x: columns[lastCol], 
        y: lastRow * rowHeight
      }
    },

    // 检查两个区域是否重叠
    isOverlapping(area1, area2) {
      return !(
        area1.x + area1.width <= area2.x ||
        area2.x + area2.width <= area1.x ||
        area1.y + area1.height <= area2.y ||
        area2.y + area2.height <= area1.y
      )
    },
    handleDragOver(event) {
      event.preventDefault()
      event.dataTransfer.dropEffect = 'copy'
    },

    // 组件拖拽
    handleComponentMouseDown(event, component) {
      if (event.button !== 0) return // 只处理左键

      // 查询组件不可拖动
      if (component.type === 'query') {
        this.$message.info('查询控件位置固定，不可拖动')
        event.preventDefault()
        return
      }

      this.draggingComponentId = component.id
      this.dragStartPos = { x: event.clientX, y: event.clientY }
      this.dragStartComponentPos = { ...component.position }

      document.addEventListener('mousemove', this.handleComponentMouseMove)
      document.addEventListener('mouseup', this.handleComponentMouseUp)

      event.preventDefault()
    },
    handleComponentMouseMove(event) {
      if (!this.draggingComponentId) return

      // 使用 requestAnimationFrame 优化性能
      if (this.updateThrottle) return
      
      this.updateThrottle = requestAnimationFrame(() => {
        const deltaX = event.clientX - this.dragStartPos.x
        const deltaY = event.clientY - this.dragStartPos.y

        const newX = this.dragStartComponentPos.x + deltaX
        const newY = this.dragStartComponentPos.y + deltaY

        // 使用智能吸附到两列网格
        const { smartSnapToTwoColumnGrid } = require('@/utils/gridUtils')
        const snappedPos = smartSnapToTwoColumnGrid(
          { x: newX, y: newY },
          {
            columnWidth: 760,
            rowHeight: 400,
            threshold: 30, // 30像素内自动吸附
            canvasWidth: 1520
          }
        )

        // 限制在画布范围内（画布实际宽度 1520px）
        const component = this.components.find(c => c.id === this.draggingComponentId)
        if (!component) {
          this.updateThrottle = null
          return
        }

        const maxX = 1520 - component.size.width
        const maxY = (this.canvasConfig.height || 1080) - component.size.height

        let constrainedX = Math.max(0, Math.min(snappedPos.x, maxX))
        let constrainedY = Math.max(0, Math.min(snappedPos.y, maxY))

        // 额外的 X 轴约束：确保组件只能位于两列之一（0 或 760）
        if (constrainedX < 380) {
          constrainedX = 0 // 第一列
        } else if (constrainedX >= 380 && constrainedX < 1140) {
          constrainedX = 760 // 第二列
        } else {
          constrainedX = 0 // 超出范围，回到第一列
        }

        // 计算对齐参考线
        const movingComponent = {
          ...component,
          position: { x: constrainedX, y: constrainedY }
        }
        const otherComponents = this.components.filter(c => c.id !== this.draggingComponentId)
        this.alignmentGuides = calculateAlignmentGuides(movingComponent, otherComponents, 5)

        // 添加网格吸附参考线（显示两列边界）
        this.alignmentGuides.push(
          { type: 'vertical', position: 0, id: 'col-0' },
          { type: 'vertical', position: 760, id: 'col-1' },
          { type: 'vertical', position: 1520, id: 'col-right' }
        )

        // 如果发生了行吸附，显示水平参考线
        if (snappedPos.snapInfo.ySnapped) {
          this.alignmentGuides.push({
            type: 'horizontal',
            position: constrainedY,
            id: 'row-snap'
          })
        }

        this.$emit('component-update', {
          id: this.draggingComponentId,
          position: { x: constrainedX, y: constrainedY }
        })
        
        this.updateThrottle = null
      })
    },
    handleComponentMouseUp() {
      if (this.updateThrottle) {
        cancelAnimationFrame(this.updateThrottle)
        this.updateThrottle = null
      }
      
      this.draggingComponentId = null
      this.dragStartPos = null
      this.dragStartComponentPos = null
      this.alignmentGuides = []

      document.removeEventListener('mousemove', this.handleComponentMouseMove)
      document.removeEventListener('mouseup', this.handleComponentMouseUp)
    },

    // 组件调整大小
    handleResizeStart(event, component, handle) {
      // 查询组件不可调整大小
      if (component.type === 'query') {
        this.$message.info('查询控件尺寸固定，不可调整')
        event.preventDefault()
        event.stopPropagation()
        return
      }

      this.resizingComponent = component
      this.resizeHandle = handle
      this.resizeStartPos = { x: event.clientX, y: event.clientY }
      this.resizeStartSize = { ...component.size }

      document.addEventListener('mousemove', this.handleResizeMove)
      document.addEventListener('mouseup', this.handleResizeEnd)

      event.preventDefault()
      event.stopPropagation()
    },
    handleResizeMove(event) {
      if (!this.resizingComponent) return

      const deltaX = event.clientX - this.resizeStartPos.x
      const deltaY = event.clientY - this.resizeStartPos.y

      let newWidth = this.resizeStartSize.width
      let newHeight = this.resizeStartSize.height
      let newX = this.resizingComponent.position.x
      let newY = this.resizingComponent.position.y

      // 根据手柄位置计算新尺寸
      if (this.resizeHandle.includes('e')) {
        newWidth = this.resizeStartSize.width + deltaX
      }
      if (this.resizeHandle.includes('w')) {
        newWidth = this.resizeStartSize.width - deltaX
        newX = this.resizingComponent.position.x + deltaX
      }
      if (this.resizeHandle.includes('s')) {
        newHeight = this.resizeStartSize.height + deltaY
      }
      if (this.resizeHandle.includes('n')) {
        newHeight = this.resizeStartSize.height - deltaY
        newY = this.resizingComponent.position.y + deltaY
      }

      // 吸附到网格
      newWidth = snapToGrid(newWidth, this.gridSize)
      newHeight = snapToGrid(newHeight, this.gridSize)
      newX = snapToGrid(newX, this.gridSize)
      newY = snapToGrid(newY, this.gridSize)

      // 两列布局的宽度约束：只能是 760px（单列）或 1520px（双列）
      if (this.resizeHandle.includes('e') || this.resizeHandle.includes('w')) {
        if (newWidth < 1140) {
          newWidth = 760 // 单列宽度
        } else {
          newWidth = 1520 // 双列宽度
        }

        // 调整 X 位置以确保在有效列位置
        if (newX < 380) {
          newX = 0 // 第一列
        } else if (newX >= 380 && newX < 1140) {
          newX = 760 // 第二列
        } else {
          newX = 0 // 超出范围，回到第一列
        }
      }

      // 最小尺寸限制
      newWidth = Math.max(380, newWidth) // 最小半列
      newHeight = Math.max(200, newHeight) // 最小高度

      // 限制在画布范围内
      const maxX = 1520 - newWidth
      const maxY = (this.canvasConfig.height || 1080) - newHeight
      newX = Math.max(0, Math.min(newX, maxX))
      newY = Math.max(0, Math.min(newY, maxY))

      this.$emit('component-update', {
        id: this.resizingComponent.id,
        position: { x: newX, y: newY },
        size: { width: newWidth, height: newHeight }
      })
    },
    handleResizeEnd() {
      this.resizingComponent = null
      this.resizeHandle = null
      this.resizeStartPos = null
      this.resizeStartSize = null

      document.removeEventListener('mousemove', this.handleResizeMove)
      document.removeEventListener('mouseup', this.handleResizeEnd)
    },

    // 组件选择
    handleComponentClick(component) {
      this.$emit('component-select', component.id)
    },
    handleCanvasClick() {
      this.$emit('component-select', null)
    },

    // 组件操作
    handleCopy(component) {
      this.$emit('component-copy', component.id)
    },
    handleDelete(component) {
      this.$confirm('确定要删除此组件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('component-delete', component.id)
      }).catch(() => {})
    },

    // 组件操作命令处理
    handleComponentCommand(command, component) {
      switch (command) {
        case 'copy':
          this.handleCopy(component)
          break
        case 'delete':
          this.handleDelete(component)
          break
        case 'saveAsTemplate':
          this.handleSaveAsTemplate(component)
          break
        case 'toFront':
        case 'toBack':
        case 'bringForward':
        case 'sendBackward':
          this.handleLayerCommand(command, component)
          break
      }
    },

    // 保存为模板
    handleSaveAsTemplate(component) {
      this.$emit('save-as-template', component)
    },

    // 图层管理
    handleLayerCommand(command, component) {
      const componentIndex = this.components.findIndex(c => c.id === component.id)
      if (componentIndex === -1) return

      let newZIndex = component.zIndex || 1

      switch (command) {
        case 'toFront':
          // 置于顶层：找到最大z-index并+1
          newZIndex = Math.max(...this.components.map(c => c.zIndex || 1)) + 1
          break
        case 'toBack':
          // 置于底层：找到最小z-index并-1
          newZIndex = Math.min(...this.components.map(c => c.zIndex || 1)) - 1
          break
        case 'bringForward':
          // 上移一层
          newZIndex = (component.zIndex || 1) + 1
          break
        case 'sendBackward':
          // 下移一层
          newZIndex = Math.max(1, (component.zIndex || 1) - 1)
          break
      }

      this.$emit('component-update', {
        id: component.id,
        zIndex: newZIndex
      })
    },

    // 多选对齐工具
    alignSelectedComponents(alignType) {
      if (!this.selectedComponentIds || this.selectedComponentIds.length < 2) {
        this.$message.warning('请至少选择两个组件')
        return
      }

      const selectedComponents = this.components.filter(c => 
        this.selectedComponentIds.includes(c.id)
      )

      let updates = []

      switch (alignType) {
        case 'left':
          // 左对齐：所有组件的x坐标对齐到最左边的组件
          const minX = Math.min(...selectedComponents.map(c => c.position.x))
          updates = selectedComponents.map(c => ({
            id: c.id,
            position: { ...c.position, x: minX }
          }))
          break
        case 'right':
          // 右对齐：所有组件的右边缘对齐到最右边的组件
          const maxRight = Math.max(...selectedComponents.map(c => c.position.x + c.size.width))
          updates = selectedComponents.map(c => ({
            id: c.id,
            position: { ...c.position, x: maxRight - c.size.width }
          }))
          break
        case 'top':
          // 顶部对齐：所有组件的y坐标对齐到最上边的组件
          const minY = Math.min(...selectedComponents.map(c => c.position.y))
          updates = selectedComponents.map(c => ({
            id: c.id,
            position: { ...c.position, y: minY }
          }))
          break
        case 'bottom':
          // 底部对齐：所有组件的底边对齐到最下边的组件
          const maxBottom = Math.max(...selectedComponents.map(c => c.position.y + c.size.height))
          updates = selectedComponents.map(c => ({
            id: c.id,
            position: { ...c.position, y: maxBottom - c.size.height }
          }))
          break
        case 'centerH':
          // 水平居中对齐：所有组件的水平中心对齐
          const avgCenterX = selectedComponents.reduce((sum, c) => 
            sum + c.position.x + c.size.width / 2, 0
          ) / selectedComponents.length
          updates = selectedComponents.map(c => ({
            id: c.id,
            position: { ...c.position, x: avgCenterX - c.size.width / 2 }
          }))
          break
        case 'centerV':
          // 垂直居中对齐：所有组件的垂直中心对齐
          const avgCenterY = selectedComponents.reduce((sum, c) => 
            sum + c.position.y + c.size.height / 2, 0
          ) / selectedComponents.length
          updates = selectedComponents.map(c => ({
            id: c.id,
            position: { ...c.position, y: avgCenterY - c.size.height / 2 }
          }))
          break
      }

      // 批量更新组件位置
      updates.forEach(update => {
        this.$emit('component-update', update)
      })
    },

    // 样式计算
    getComponentStyle(component) {
      // 确保组件有有效的 position 和 size
      if (!component.position || !component.size) {
        console.warn('组件缺少 position 或 size:', component)
        return {
          left: '0%',
          top: '0px',
          width: '50%',
          height: '400px',
          zIndex: 1
        }
      }

      // 将像素值转换为百分比（用于响应式布局）
      // 假设原始设计是基于1520px宽度的两列布局
      // 第一列: x=0 (0%), width=760 (50%)
      // 第二列: x=760 (50%), width=760 (50%)
      
      const originalCanvasWidth = 1520 // 原始设计宽度
      const leftPercent = (component.position.x / originalCanvasWidth) * 100
      const widthPercent = (component.size.width / originalCanvasWidth) * 100

      return {
        left: `${leftPercent}%`,
        top: `${component.position.y}px`,
        width: `${widthPercent}%`,
        height: `${component.size.height}px`,
        zIndex: component.zIndex || 1
      }
    },
    getGuideStyle(guide) {
      if (guide.type === 'vertical') {
        return {
          left: `${guide.position}px`,
          top: 0,
          bottom: 0,
          width: '1px'
        }
      } else {
        return {
          top: `${guide.position}px`,
          left: 0,
          right: 0,
          height: '1px'
        }
      }
    },

    // 组件类型信息
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
    getComponentIcon(type) {
      const iconMap = {
        chart: 'el-icon-data-line',
        query: 'el-icon-search',
        text: 'el-icon-document',
        media: 'el-icon-picture',
        tabs: 'el-icon-menu'
      }
      return iconMap[type] || 'el-icon-document'
    }
  },
  mounted() {
    // 组件已挂载
  },
  beforeDestroy() {
    // 清理事件监听
    document.removeEventListener('mousemove', this.handleComponentMouseMove)
    document.removeEventListener('mouseup', this.handleComponentMouseUp)
    document.removeEventListener('mousemove', this.handleResizeMove)
    document.removeEventListener('mouseup', this.handleResizeEnd)
    
    // 清理动画帧
    if (this.updateThrottle) {
      cancelAnimationFrame(this.updateThrottle)
      this.updateThrottle = null
    }
  }
}
</script>

<style scoped>
/* 画布外层容器 - 负责滚动和居中显示 */
.designer-canvas-wrapper {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden; /* 不需要水平滚动 */
  padding: 20px;
  background: #f5f5f5;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

/* 实际画布区域 - 自适应宽度 */
.designer-canvas {
  position: relative;
  width: 100%; /* 自适应容器宽度 */
  min-height: 1080px; /* 最小高度 */
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
  z-index: 1;
  pointer-events: auto;
}

.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.alignment-guide {
  position: absolute;
  background: #409eff;
  pointer-events: none;
  z-index: 999;
}

/* 列分隔线 - 使用更明显的颜色 */
.alignment-guide[id^='col-'] {
  background: #409eff;
  opacity: 0.3;
}

/* 行网格线 - 使用更淡的颜色 */
.alignment-guide[id^='row-'] {
  background: #e0e0e0;
  opacity: 0.5;
}

.alignment-guide.vertical {
  width: 1px;
}

.alignment-guide.horizontal {
  height: 1px;
}

.snap-zones {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.snap-zone {
  position: absolute;
  top: 0;
  bottom: 0;
  background: rgba(64, 158, 255, 0.05);
  border: 2px dashed rgba(64, 158, 255, 0.3);
  transition: all 0.2s;
  pointer-events: none;
}

.alignment-toolbar {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 8px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.component-item {
  position: absolute;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: move;
  transition: box-shadow 0.2s, border-color 0.2s;
  overflow: hidden;
  box-sizing: border-box;
}

.component-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.component-item.selected {
  border-color: #409eff;
  border-width: 2px;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.component-item.dragging {
  opacity: 0.8;
  cursor: grabbing;
}

/* 查询组件特殊样式 - 固定位置，不可拖动 */
.component-item.query-component {
  cursor: default;
  border-color: #67c23a;
  background: linear-gradient(to right, #f0f9ff 0%, #ffffff 100%);
}

.component-item.query-component:hover {
  border-color: #67c23a;
  box-shadow: 0 2px 8px 0 rgba(103, 194, 58, 0.15);
}

.component-item.query-component .component-header {
  background: linear-gradient(to right, #e1f3d8 0%, #f5f7fa 100%);
  cursor: default;
}

/* 查询组件body样式 */
.query-component-body {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 100%;
  padding: 16px;
  background: #f0f9ff;
}

.query-hint {
  font-size: 13px;
  color: #909399;
}

.component-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
  width: 100%;
}

.component-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  cursor: move;
  flex-shrink: 0;
  user-select: none;
}

.component-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.component-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.component-body {
  flex: 1;
  padding: 10px;
  overflow: hidden;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
  position: relative;
}

.component-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.component-placeholder i {
  font-size: 48px;
  margin-bottom: 10px;
}

.component-placeholder p {
  font-size: 14px;
}

/* 调整大小手柄 */
.resize-handles {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  pointer-events: none;
  z-index: 1000;
}

.resize-handle {
  position: absolute;
  background: #409eff;
  pointer-events: auto;
  z-index: 1001;
  transition: transform 0.2s;
}

.resize-handle:hover {
  transform: scale(1.2);
}

.resize-handle.resize-nw,
.resize-handle.resize-ne,
.resize-handle.resize-sw,
.resize-handle.resize-se {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.resize-handle.resize-n,
.resize-handle.resize-s {
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.resize-handle.resize-e,
.resize-handle.resize-w {
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.resize-handle.resize-nw {
  top: -4px;
  left: -4px;
  cursor: nw-resize;
}

.resize-handle.resize-n {
  top: -4px;
  cursor: n-resize;
}

.resize-handle.resize-ne {
  top: -4px;
  right: -4px;
  cursor: ne-resize;
}

.resize-handle.resize-e {
  right: -4px;
  cursor: e-resize;
}

.resize-handle.resize-se {
  bottom: -4px;
  right: -4px;
  cursor: se-resize;
}

.resize-handle.resize-s {
  bottom: -4px;
  cursor: s-resize;
}

.resize-handle.resize-sw {
  bottom: -4px;
  left: -4px;
  cursor: sw-resize;
}

.resize-handle.resize-w {
  left: -4px;
  cursor: w-resize;
}

/* 空画布提示 */
.empty-canvas {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #909399;
  user-select: none;
  pointer-events: none;
}

.empty-canvas i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.empty-canvas p {
  font-size: 16px;
  margin: 8px 0;
}

.empty-hint {
  font-size: 14px;
  color: #c0c4cc;
}
</style>

.resize-handle.resize-nw,
.resize-handle.resize-ne,
.resize-handle.resize-sw,
.resize-handle.resize-se {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.resize-handle.resize-n,
.resize-handle.resize-s {
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.resize-handle.resize-e,
.resize-handle.resize-w {
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.resize-handle.resize-nw {
  top: -4px;
  left: -4px;
  cursor: nw-resize;
}

.resize-handle.resize-n {
  top: -4px;
  cursor: n-resize;
}

.resize-handle.resize-ne {
  top: -4px;
  right: -4px;
  cursor: ne-resize;
}

.resize-handle.resize-e {
  right: -4px;
  cursor: e-resize;
}

.resize-handle.resize-se {
  bottom: -4px;
  right: -4px;
  cursor: se-resize;
}

.resize-handle.resize-s {
  bottom: -4px;
  cursor: s-resize;
}

.resize-handle.resize-sw {
  bottom: -4px;
  left: -4px;
  cursor: sw-resize;
}

.resize-handle.resize-w {
  left: -4px;
  cursor: w-resize;
}

/* 空画布提示 */
.empty-canvas {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #909399;
  user-select: none;
  pointer-events: none;
}

.empty-canvas i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.empty-canvas p {
  font-size: 16px;
  margin: 8px 0;
}

.empty-hint {
  font-size: 14px;

/* 查询组件样式 */
.query-component-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: visible !important; // 允许下拉弹出框超出边界
}

// 查询组件的父容器也要允许溢出
.component-item.has-query-component {
  overflow: visible !important;
}

.component-body.has-query-component {
  overflow: visible !important;
}

.query-config-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
  cursor: pointer;
}
</style>
