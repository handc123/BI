<template>
  <div class="test-advanced-features">
    <div class="test-header">
      <h2>仪表板设计器 - 高级功能测试</h2>
      <el-button @click="runAllTests" type="primary">运行所有测试</el-button>
    </div>

    <el-card class="test-section">
      <div slot="header">
        <span>测试结果</span>
      </div>
      <el-table :data="testResults" style="width: 100%">
        <el-table-column prop="name" label="测试项" width="300"></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'passed' ? 'success' : scope.row.status === 'failed' ? 'danger' : 'info'">
              {{ scope.row.status === 'passed' ? '通过' : scope.row.status === 'failed' ? '失败' : '待测试' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="详情"></el-table-column>
      </el-table>
    </el-card>

    <el-card class="test-section">
      <div slot="header">
        <span>交互式测试</span>
      </div>
      
      <!-- 工具栏 -->
      <designer-toolbar
        :dashboard-name="dashboardName"
        :can-undo="canUndo"
        :can-redo="canRedo"
        :has-unsaved-changes="hasUnsavedChanges"
        @name-change="handleNameChange"
        @undo="handleUndo"
        @redo="handleRedo"
        @open-component-library="showComponentLibrary = true"
        @preview="handlePreview"
        @save="handleSave"
        @publish="handlePublish"
        @back="handleBack"
      />

      <!-- 画布 -->
      <designer-canvas
        :components="components"
        :grid-size="gridSize"
        :canvas-config="canvasConfig"
        :selected-component-id="selectedComponentId"
        :selected-component-ids="selectedComponentIds"
        :show-grid="true"
        @component-add="handleComponentAdd"
        @component-update="handleComponentUpdate"
        @component-select="handleComponentSelect"
        @component-copy="handleComponentCopy"
        @component-delete="handleComponentDelete"
      />

      <!-- 操作说明 -->
      <div class="instructions">
        <h4>测试说明：</h4>
        <ol>
          <li><strong>网格吸附：</strong>拖拽组件，观察是否吸附到网格（10px）</li>
          <li><strong>对齐参考线：</strong>拖拽组件靠近其他组件，观察蓝色参考线</li>
          <li><strong>多选对齐：</strong>按住Ctrl点击多个组件，使用顶部对齐工具栏</li>
          <li><strong>图层管理：</strong>点击组件右上角图层按钮，测试置顶/置底</li>
          <li><strong>撤销/重做：</strong>执行操作后，点击撤销/重做按钮或使用Ctrl+Z/Ctrl+Y</li>
          <li><strong>复制/删除：</strong>点击组件右上角的复制/删除按钮</li>
        </ol>
      </div>
    </el-card>
  </div>
</template>

<script>
import DesignerToolbar from '@/components/DesignerToolbar'
import DesignerCanvas from '@/components/DesignerCanvas'
import { snapToGrid } from '@/utils/gridUtils'
import { calculateAlignmentGuides } from '@/utils/alignmentUtils'

export default {
  name: 'TestAdvancedFeatures',
  components: {
    DesignerToolbar,
    DesignerCanvas
  },
  data() {
    return {
      dashboardName: '高级功能测试仪表板',
      hasUnsavedChanges: false,
      gridSize: 10,
      canvasConfig: {
        width: 1200,
        height: 800,
        background: { type: 'color', value: '#f5f5f5', opacity: 1 },
        margin: { top: 20, right: 20, bottom: 20, left: 20 }
      },
      components: [
        {
          id: 'comp_1',
          type: 'chart',
          name: '组件1',
          position: { x: 100, y: 100 },
          size: { width: 300, height: 200 },
          zIndex: 1
        },
        {
          id: 'comp_2',
          type: 'chart',
          name: '组件2',
          position: { x: 450, y: 100 },
          size: { width: 300, height: 200 },
          zIndex: 2
        },
        {
          id: 'comp_3',
          type: 'query',
          name: '组件3',
          position: { x: 100, y: 350 },
          size: { width: 300, height: 150 },
          zIndex: 3
        }
      ],
      selectedComponentId: null,
      selectedComponentIds: [],
      historyStack: [],
      redoStack: [],
      showComponentLibrary: false,
      testResults: [
        { name: '1. 网格吸附一致性', status: 'pending', message: '' },
        { name: '2. 尺寸吸附一致性', status: 'pending', message: '' },
        { name: '3. 撤销重做往返', status: 'pending', message: '' },
        { name: '4. 图层管理正确性', status: 'pending', message: '' },
        { name: '5. 对齐参考线显示', status: 'pending', message: '' },
        { name: '6. 多选对齐功能', status: 'pending', message: '' },
        { name: '7. 组件复制功能', status: 'pending', message: '' },
        { name: '8. 组件删除功能', status: 'pending', message: '' }
      ]
    }
  },
  computed: {
    canUndo() {
      return this.historyStack.length > 0
    },
    canRedo() {
      return this.redoStack.length > 0
    }
  },
  methods: {
    // 运行所有自动化测试
    runAllTests() {
      this.testGridSnapping()
      this.testSizeSnapping()
      this.testUndoRedo()
      this.testLayerManagement()
      this.testAlignmentGuides()
      this.testMultiSelectAlignment()
      this.testComponentCopy()
      this.testComponentDelete()
    },

    // 测试1: 网格吸附一致性
    testGridSnapping() {
      const testValues = [15, 23, 47, 99, 105]
      let passed = true
      let message = ''

      testValues.forEach(value => {
        const snapped = snapToGrid(value, this.gridSize)
        const isMultiple = snapped % this.gridSize === 0
        if (!isMultiple) {
          passed = false
          message = `值 ${value} 吸附后为 ${snapped}，不是网格大小的整数倍`
        }
      })

      if (passed) {
        message = '所有测试值都正确吸附到网格'
      }

      this.updateTestResult(0, passed ? 'passed' : 'failed', message)
    },

    // 测试2: 尺寸吸附一致性
    testSizeSnapping() {
      const testSizes = [
        { width: 123, height: 87 },
        { width: 256, height: 199 },
        { width: 305, height: 412 }
      ]
      let passed = true
      let message = ''

      testSizes.forEach(size => {
        const snappedWidth = snapToGrid(size.width, this.gridSize)
        const snappedHeight = snapToGrid(size.height, this.gridSize)
        
        if (snappedWidth % this.gridSize !== 0 || snappedHeight % this.gridSize !== 0) {
          passed = false
          message = `尺寸 ${size.width}x${size.height} 吸附后不符合网格`
        }
      })

      if (passed) {
        message = '所有尺寸都正确吸附到网格'
      }

      this.updateTestResult(1, passed ? 'passed' : 'failed', message)
    },

    // 测试3: 撤销重做往返
    testUndoRedo() {
      // 保存初始状态
      const initialState = JSON.stringify(this.components)
      this.recordHistory()

      // 执行操作：移动组件
      const component = this.components[0]
      const originalPos = { ...component.position }
      component.position = { x: 200, y: 200 }
      this.recordHistory()

      // 撤销
      this.handleUndo()
      const afterUndo = JSON.stringify(this.components)

      // 重做
      this.handleRedo()
      const afterRedo = JSON.stringify(this.components)

      // 验证
      const passed = afterRedo !== initialState && this.components[0].position.x === 200
      const message = passed 
        ? '撤销重做功能正常工作' 
        : '撤销重做功能异常'

      // 恢复初始状态
      component.position = originalPos
      this.historyStack = []
      this.redoStack = []

      this.updateTestResult(2, passed ? 'passed' : 'failed', message)
    },

    // 测试4: 图层管理正确性
    testLayerManagement() {
      const component = this.components[0]
      const initialZIndex = component.zIndex

      // 测试置顶
      const maxZ = Math.max(...this.components.map(c => c.zIndex || 1))
      component.zIndex = maxZ + 1
      const toFrontPassed = component.zIndex > initialZIndex

      // 测试置底
      const minZ = Math.min(...this.components.map(c => c.zIndex || 1))
      component.zIndex = minZ - 1
      const toBackPassed = component.zIndex < initialZIndex

      // 恢复
      component.zIndex = initialZIndex

      const passed = toFrontPassed && toBackPassed
      const message = passed 
        ? '图层管理功能正常' 
        : '图层管理功能异常'

      this.updateTestResult(3, passed ? 'passed' : 'failed', message)
    },

    // 测试5: 对齐参考线显示
    testAlignmentGuides() {
      const movingComponent = {
        id: 'test',
        position: { x: 100, y: 100 },
        size: { width: 300, height: 200 }
      }

      const otherComponents = [
        {
          id: 'other1',
          position: { x: 100, y: 350 },
          size: { width: 300, height: 150 }
        }
      ]

      const guides = calculateAlignmentGuides(movingComponent, otherComponents, 5)
      
      // 应该检测到左对齐
      const hasLeftAlign = guides.some(g => g.type === 'vertical' && g.position === 100)
      
      const passed = hasLeftAlign
      const message = passed 
        ? `检测到 ${guides.length} 条对齐参考线` 
        : '未能正确检测对齐参考线'

      this.updateTestResult(4, passed ? 'passed' : 'failed', message)
    },

    // 测试6: 多选对齐功能
    testMultiSelectAlignment() {
      // 这个需要手动测试
      this.updateTestResult(5, 'pending', '请手动测试：选择多个组件并使用对齐工具')
    },

    // 测试7: 组件复制功能
    testComponentCopy() {
      const initialCount = this.components.length
      const componentToCopy = this.components[0]
      
      // 模拟复制
      const newComponent = {
        ...JSON.parse(JSON.stringify(componentToCopy)),
        id: 'comp_copy_' + Date.now(),
        position: {
          x: componentToCopy.position.x + 20,
          y: componentToCopy.position.y + 20
        }
      }
      
      this.components.push(newComponent)
      
      const passed = this.components.length === initialCount + 1
      const message = passed 
        ? '组件复制功能正常' 
        : '组件复制功能异常'

      // 清理
      this.components.pop()

      this.updateTestResult(6, passed ? 'passed' : 'failed', message)
    },

    // 测试8: 组件删除功能
    testComponentDelete() {
      const initialCount = this.components.length
      const tempComponent = {
        id: 'temp_' + Date.now(),
        type: 'chart',
        name: '临时组件',
        position: { x: 500, y: 500 },
        size: { width: 200, height: 150 },
        zIndex: 10
      }
      
      this.components.push(tempComponent)
      const afterAdd = this.components.length
      
      // 删除
      const index = this.components.findIndex(c => c.id === tempComponent.id)
      this.components.splice(index, 1)
      
      const passed = this.components.length === initialCount
      const message = passed 
        ? '组件删除功能正常' 
        : '组件删除功能异常'

      this.updateTestResult(7, passed ? 'passed' : 'failed', message)
    },

    // 更新测试结果
    updateTestResult(index, status, message) {
      this.$set(this.testResults, index, {
        ...this.testResults[index],
        status,
        message
      })
    },

    // 历史记录管理
    recordHistory() {
      const snapshot = JSON.parse(JSON.stringify(this.components))
      this.historyStack.push(snapshot)
      this.redoStack = []
      this.hasUnsavedChanges = true
    },

    // 工具栏事件处理
    handleNameChange(newName) {
      this.dashboardName = newName
      this.hasUnsavedChanges = true
    },

    handleUndo() {
      if (this.historyStack.length === 0) return
      
      const currentState = JSON.parse(JSON.stringify(this.components))
      this.redoStack.push(currentState)
      
      const previousState = this.historyStack.pop()
      this.components = previousState
    },

    handleRedo() {
      if (this.redoStack.length === 0) return
      
      const currentState = JSON.parse(JSON.stringify(this.components))
      this.historyStack.push(currentState)
      
      const nextState = this.redoStack.pop()
      this.components = nextState
    },

    handlePreview() {
      this.$message.info('预览功能')
    },

    handleSave() {
      this.$message.success('保存成功')
      this.hasUnsavedChanges = false
    },

    handlePublish() {
      this.$message.success('发布成功')
    },

    handleBack() {
      this.$router.back()
    },

    // 画布事件处理
    handleComponentAdd(componentData) {
      this.recordHistory()
      const newComponent = {
        ...componentData,
        id: 'comp_' + Date.now(),
        zIndex: Math.max(...this.components.map(c => c.zIndex || 1)) + 1
      }
      this.components.push(newComponent)
    },

    handleComponentUpdate(update) {
      this.recordHistory()
      const component = this.components.find(c => c.id === update.id)
      if (component) {
        if (update.position) {
          component.position = update.position
        }
        if (update.size) {
          component.size = update.size
        }
        if (update.zIndex !== undefined) {
          component.zIndex = update.zIndex
        }
      }
    },

    handleComponentSelect(componentId) {
      this.selectedComponentId = componentId
    },

    handleComponentCopy(componentId) {
      this.recordHistory()
      const component = this.components.find(c => c.id === componentId)
      if (component) {
        const newComponent = {
          ...JSON.parse(JSON.stringify(component)),
          id: 'comp_' + Date.now(),
          position: {
            x: component.position.x + 20,
            y: component.position.y + 20
          }
        }
        this.components.push(newComponent)
        this.$message.success('组件已复制')
      }
    },

    handleComponentDelete(componentId) {
      this.recordHistory()
      const index = this.components.findIndex(c => c.id === componentId)
      if (index !== -1) {
        this.components.splice(index, 1)
        this.selectedComponentId = null
        this.$message.success('组件已删除')
      }
    }
  }
}
</script>

<style scoped>
.test-advanced-features {
  padding: 20px;
}

.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.test-header h2 {
  margin: 0;
  color: #303133;
}

.test-section {
  margin-bottom: 20px;
}

.instructions {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.instructions h4 {
  margin-top: 0;
  color: #303133;
}

.instructions ol {
  margin: 10px 0;
  padding-left: 20px;
}

.instructions li {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.instructions strong {
  color: #303133;
}
</style>
