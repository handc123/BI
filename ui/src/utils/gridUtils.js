/**
 * 网格工具函数
 * 实现网格吸附算法
 */

/**
 * 将坐标吸附到网格
 * @param {number} value - 原始坐标值
 * @param {number} gridSize - 网格大小
 * @returns {number} 吸附后的坐标值
 */
export function snapToGrid(value, gridSize) {
  if (!gridSize || gridSize <= 0) {
    return value
  }
  return Math.round(value / gridSize) * gridSize
}

/**
 * 将位置吸附到网格
 * @param {Object} position - 位置对象 {x, y}
 * @param {number} gridSize - 网格大小
 * @returns {Object} 吸附后的位置 {x, y}
 */
export function snapPositionToGrid(position, gridSize) {
  return {
    x: snapToGrid(position.x, gridSize),
    y: snapToGrid(position.y, gridSize)
  }
}

/**
 * 将尺寸吸附到网格
 * @param {Object} size - 尺寸对象 {width, height}
 * @param {number} gridSize - 网格大小
 * @param {number} minWidth - 最小宽度
 * @param {number} minHeight - 最小高度
 * @returns {Object} 吸附后的尺寸 {width, height}
 */
export function snapSizeToGrid(size, gridSize, minWidth = 100, minHeight = 100) {
  const snappedWidth = Math.max(snapToGrid(size.width, gridSize), minWidth)
  const snappedHeight = Math.max(snapToGrid(size.height, gridSize), minHeight)
  
  return {
    width: snappedWidth,
    height: snappedHeight
  }
}

/**
 * 处理组件拖拽时的吸附
 * @param {Object} component - 组件对象
 * @param {number} deltaX - X轴移动距离
 * @param {number} deltaY - Y轴移动距离
 * @param {number} gridSize - 网格大小
 * @returns {Object} 新的位置 {x, y}
 */
export function handleDragSnap(component, deltaX, deltaY, gridSize) {
  const newX = component.position.x + deltaX
  const newY = component.position.y + deltaY
  
  return {
    x: snapToGrid(newX, gridSize),
    y: snapToGrid(newY, gridSize)
  }
}

/**
 * 处理组件调整大小时的吸附
 * @param {Object} component - 组件对象
 * @param {Object} newSize - 新尺寸 {width, height}
 * @param {number} gridSize - 网格大小
 * @param {Object} constraints - 约束条件 {minWidth, minHeight, maxWidth, maxHeight}
 * @returns {Object} 吸附后的尺寸 {width, height}
 */
export function handleResizeSnap(component, newSize, gridSize, constraints = {}) {
  const {
    minWidth = 100,
    minHeight = 100,
    maxWidth = Infinity,
    maxHeight = Infinity
  } = constraints
  
  // 吸附到网格
  let width = snapToGrid(newSize.width, gridSize)
  let height = snapToGrid(newSize.height, gridSize)
  
  // 应用约束
  width = Math.max(minWidth, Math.min(width, maxWidth))
  height = Math.max(minHeight, Math.min(height, maxHeight))
  
  return { width, height }
}

/**
 * 检查组件是否在画布范围内
 * @param {Object} component - 组件对象
 * @param {Object} canvasSize - 画布尺寸 {width, height}
 * @returns {boolean} 是否在范围内
 */
export function isWithinCanvas(component, canvasSize) {
  const { position, size } = component
  
  return (
    position.x >= 0 &&
    position.y >= 0 &&
    position.x + size.width <= canvasSize.width &&
    position.y + size.height <= canvasSize.height
  )
}

/**
 * 将组件限制在画布范围内
 * @param {Object} component - 组件对象
 * @param {Object} canvasSize - 画布尺寸 {width, height}
 * @param {number} gridSize - 网格大小
 * @returns {Object} 调整后的位置和尺寸 {position, size}
 */
export function constrainToCanvas(component, canvasSize, gridSize) {
  const { position, size } = component
  
  let x = Math.max(0, position.x)
  let y = Math.max(0, position.y)
  let width = size.width
  let height = size.height
  
  // 确保组件不超出右边界
  if (x + width > canvasSize.width) {
    x = Math.max(0, canvasSize.width - width)
  }
  
  // 确保组件不超出底边界
  if (y + height > canvasSize.height) {
    y = Math.max(0, canvasSize.height - height)
  }
  
  // 如果组件太大，调整尺寸
  if (width > canvasSize.width) {
    width = canvasSize.width
  }
  if (height > canvasSize.height) {
    height = canvasSize.height
  }
  
  // 吸附到网格
  return {
    position: snapPositionToGrid({ x, y }, gridSize),
    size: snapSizeToGrid({ width, height }, gridSize)
  }
}

/**
 * 计算网格线
 * @param {Object} canvasSize - 画布尺寸 {width, height}
 * @param {number} gridSize - 网格大小
 * @returns {Object} 网格线 {vertical: [], horizontal: []}
 */
export function calculateGridLines(canvasSize, gridSize) {
  const vertical = []
  const horizontal = []
  
  // 垂直线
  for (let x = 0; x <= canvasSize.width; x += gridSize) {
    vertical.push(x)
  }
  
  // 水平线
  for (let y = 0; y <= canvasSize.height; y += gridSize) {
    horizontal.push(y)
  }
  
  return { vertical, horizontal }
}

/**
 * 检查两个组件是否重叠
 * @param {Object} comp1 - 组件1
 * @param {Object} comp2 - 组件2
 * @returns {boolean} 是否重叠
 */
export function isOverlapping(comp1, comp2) {
  const rect1 = {
    left: comp1.position.x,
    right: comp1.position.x + comp1.size.width,
    top: comp1.position.y,
    bottom: comp1.position.y + comp1.size.height
  }
  
  const rect2 = {
    left: comp2.position.x,
    right: comp2.position.x + comp2.size.width,
    top: comp2.position.y,
    bottom: comp2.position.y + comp2.size.height
  }
  
  return !(
    rect1.right <= rect2.left ||
    rect1.left >= rect2.right ||
    rect1.bottom <= rect2.top ||
    rect1.top >= rect2.bottom
  )
}

/**
 * 智能吸附到两列网格布局
 * 支持自由拖拽，但会智能吸附到最近的网格位置
 * @param {Object} position - 当前位置 {x, y}
 * @param {Object} config - 配置 {columnWidth, rowHeight, threshold}
 * @returns {Object} 吸附后的位置 {x, y, snapped}
 */
export function smartSnapToTwoColumnGrid(position, config = {}) {
  const {
    columnWidth = 760,
    rowHeight = 400,
    threshold = 30, // 吸附阈值，像素
    canvasWidth = 1520
  } = config

  // 计算两列的X坐标
  const columns = [0, columnWidth]
  
  // 找到最近的列
  let nearestColumn = columns[0]
  let minXDistance = Math.abs(position.x - columns[0])
  
  columns.forEach(col => {
    const distance = Math.abs(position.x - col)
    if (distance < minXDistance) {
      minXDistance = distance
      nearestColumn = col
    }
  })

  // 计算最近的行
  const row = Math.round(position.y / rowHeight)
  const nearestRow = row * rowHeight
  const yDistance = Math.abs(position.y - nearestRow)

  // 判断是否需要吸附
  const shouldSnapX = minXDistance < threshold
  const shouldSnapY = yDistance < threshold

  return {
    x: shouldSnapX ? nearestColumn : position.x,
    y: shouldSnapY ? nearestRow : position.y,
    snapped: shouldSnapX || shouldSnapY,
    snapInfo: {
      xSnapped: shouldSnapX,
      ySnapped: shouldSnapY,
      column: shouldSnapX ? columns.indexOf(nearestColumn) : -1,
      row: shouldSnapY ? row : -1
    }
  }
}

/**
 * 获取两列布局的网格线
 * @param {Object} config - 配置 {columnWidth, rowHeight, canvasWidth, canvasHeight}
 * @returns {Object} 网格线 {vertical: [], horizontal: []}
 */
export function getTwoColumnGridLines(config = {}) {
  const {
    columnWidth = 760,
    rowHeight = 400,
    canvasWidth = 1520,
    canvasHeight = 1080
  } = config

  const vertical = [0, columnWidth, canvasWidth]
  const horizontal = []

  // 生成水平网格线
  for (let y = 0; y <= canvasHeight; y += rowHeight) {
    horizontal.push(y)
  }

  return { vertical, horizontal }
}

/**
 * 检查位置是否在两列网格的有效位置
 * @param {Object} position - 位置 {x, y}
 * @param {Object} size - 尺寸 {width, height}
 * @param {Object} config - 配置
 * @returns {boolean} 是否有效
 */
export function isValidTwoColumnPosition(position, size, config = {}) {
  const {
    columnWidth = 760,
    canvasWidth = 1520,
    canvasHeight = 1080
  } = config

  // 检查是否在画布范围内
  if (position.x < 0 || position.y < 0) return false
  if (position.x + size.width > canvasWidth) return false
  if (position.y + size.height > canvasHeight) return false

  return true
}

/**
 * 调整位置以适应两列布局
 * @param {Object} position - 位置 {x, y}
 * @param {Object} size - 尺寸 {width, height}
 * @param {Object} config - 配置
 * @returns {Object} 调整后的位置 {x, y}
 */
export function adjustToTwoColumnLayout(position, size, config = {}) {
  const {
    columnWidth = 760,
    rowHeight = 400,
    canvasWidth = 1520,
    canvasHeight = 1080
  } = config

  let { x, y } = position

  // 限制X坐标到两列之一
  if (x < columnWidth / 2) {
    x = 0 // 第一列
  } else if (x < columnWidth * 1.5) {
    x = columnWidth // 第二列
  } else {
    x = 0 // 超出范围，回到第一列
  }

  // 限制Y坐标在画布范围内
  y = Math.max(0, Math.min(y, canvasHeight - size.height))

  // 吸附到行网格
  y = Math.round(y / rowHeight) * rowHeight

  return { x, y }
}
