/**
 * 对齐工具函数
 * 实现对齐参考线算法和组件对齐功能
 */

/**
 * 计算对齐参考线
 * @param {Object} movingComponent - 正在移动的组件
 * @param {Array} otherComponents - 其他组件列表
 * @param {number} threshold - 对齐阈值(像素)
 * @returns {Array} 参考线列表
 */
export function calculateAlignmentGuides(movingComponent, otherComponents, threshold = 5) {
  const guides = []
  
  const moving = getComponentEdges(movingComponent)
  
  otherComponents.forEach(comp => {
    if (comp.id === movingComponent.id) return
    
    const other = getComponentEdges(comp)
    
    // 检查垂直对齐
    checkVerticalAlignment(moving, other, threshold, guides)
    
    // 检查水平对齐
    checkHorizontalAlignment(moving, other, threshold, guides)
  })
  
  // 去重
  return deduplicateGuides(guides)
}

/**
 * 获取组件的边缘位置
 * @param {Object} component - 组件对象
 * @returns {Object} 边缘位置
 */
function getComponentEdges(component) {
  const { position, size } = component
  
  return {
    left: position.x,
    right: position.x + size.width,
    top: position.y,
    bottom: position.y + size.height,
    centerX: position.x + size.width / 2,
    centerY: position.y + size.height / 2
  }
}

/**
 * 检查垂直对齐
 */
function checkVerticalAlignment(moving, other, threshold, guides) {
  // 左对齐
  if (Math.abs(moving.left - other.left) < threshold) {
    guides.push({
      type: 'vertical',
      position: other.left,
      alignType: 'left',
      range: { start: Math.min(moving.top, other.top), end: Math.max(moving.bottom, other.bottom) }
    })
  }
  
  // 右对齐
  if (Math.abs(moving.right - other.right) < threshold) {
    guides.push({
      type: 'vertical',
      position: other.right,
      alignType: 'right',
      range: { start: Math.min(moving.top, other.top), end: Math.max(moving.bottom, other.bottom) }
    })
  }
  
  // 中心对齐
  if (Math.abs(moving.centerX - other.centerX) < threshold) {
    guides.push({
      type: 'vertical',
      position: other.centerX,
      alignType: 'center',
      range: { start: Math.min(moving.top, other.top), end: Math.max(moving.bottom, other.bottom) }
    })
  }
  
  // 左边对齐到右边
  if (Math.abs(moving.left - other.right) < threshold) {
    guides.push({
      type: 'vertical',
      position: other.right,
      alignType: 'left-to-right',
      range: { start: Math.min(moving.top, other.top), end: Math.max(moving.bottom, other.bottom) }
    })
  }
  
  // 右边对齐到左边
  if (Math.abs(moving.right - other.left) < threshold) {
    guides.push({
      type: 'vertical',
      position: other.left,
      alignType: 'right-to-left',
      range: { start: Math.min(moving.top, other.top), end: Math.max(moving.bottom, other.bottom) }
    })
  }
}

/**
 * 检查水平对齐
 */
function checkHorizontalAlignment(moving, other, threshold, guides) {
  // 顶部对齐
  if (Math.abs(moving.top - other.top) < threshold) {
    guides.push({
      type: 'horizontal',
      position: other.top,
      alignType: 'top',
      range: { start: Math.min(moving.left, other.left), end: Math.max(moving.right, other.right) }
    })
  }
  
  // 底部对齐
  if (Math.abs(moving.bottom - other.bottom) < threshold) {
    guides.push({
      type: 'horizontal',
      position: other.bottom,
      alignType: 'bottom',
      range: { start: Math.min(moving.left, other.left), end: Math.max(moving.right, other.right) }
    })
  }
  
  // 中心对齐
  if (Math.abs(moving.centerY - other.centerY) < threshold) {
    guides.push({
      type: 'horizontal',
      position: other.centerY,
      alignType: 'center',
      range: { start: Math.min(moving.left, other.left), end: Math.max(moving.right, other.right) }
    })
  }
  
  // 顶部对齐到底部
  if (Math.abs(moving.top - other.bottom) < threshold) {
    guides.push({
      type: 'horizontal',
      position: other.bottom,
      alignType: 'top-to-bottom',
      range: { start: Math.min(moving.left, other.left), end: Math.max(moving.right, other.right) }
    })
  }
  
  // 底部对齐到顶部
  if (Math.abs(moving.bottom - other.top) < threshold) {
    guides.push({
      type: 'horizontal',
      position: other.top,
      alignType: 'bottom-to-top',
      range: { start: Math.min(moving.left, other.left), end: Math.max(moving.right, other.right) }
    })
  }
}

/**
 * 去重参考线
 */
function deduplicateGuides(guides) {
  const seen = new Set()
  return guides.filter(guide => {
    const key = `${guide.type}-${guide.position}`
    if (seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
}

/**
 * 对齐多个组件 - 左对齐
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function alignLeft(components) {
  if (components.length < 2) return []
  
  const minLeft = Math.min(...components.map(c => c.position.x))
  
  return components.map(c => ({
    id: c.id,
    position: { x: minLeft, y: c.position.y }
  }))
}

/**
 * 对齐多个组件 - 右对齐
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function alignRight(components) {
  if (components.length < 2) return []
  
  const maxRight = Math.max(...components.map(c => c.position.x + c.size.width))
  
  return components.map(c => ({
    id: c.id,
    position: { x: maxRight - c.size.width, y: c.position.y }
  }))
}

/**
 * 对齐多个组件 - 顶部对齐
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function alignTop(components) {
  if (components.length < 2) return []
  
  const minTop = Math.min(...components.map(c => c.position.y))
  
  return components.map(c => ({
    id: c.id,
    position: { x: c.position.x, y: minTop }
  }))
}

/**
 * 对齐多个组件 - 底部对齐
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function alignBottom(components) {
  if (components.length < 2) return []
  
  const maxBottom = Math.max(...components.map(c => c.position.y + c.size.height))
  
  return components.map(c => ({
    id: c.id,
    position: { x: c.position.x, y: maxBottom - c.size.height }
  }))
}

/**
 * 对齐多个组件 - 水平居中对齐
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function alignCenterHorizontal(components) {
  if (components.length < 2) return []
  
  const centerXs = components.map(c => c.position.x + c.size.width / 2)
  const avgCenterX = centerXs.reduce((sum, x) => sum + x, 0) / centerXs.length
  
  return components.map(c => ({
    id: c.id,
    position: { x: avgCenterX - c.size.width / 2, y: c.position.y }
  }))
}

/**
 * 对齐多个组件 - 垂直居中对齐
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function alignCenterVertical(components) {
  if (components.length < 2) return []
  
  const centerYs = components.map(c => c.position.y + c.size.height / 2)
  const avgCenterY = centerYs.reduce((sum, y) => sum + y, 0) / centerYs.length
  
  return components.map(c => ({
    id: c.id,
    position: { x: c.position.x, y: avgCenterY - c.size.height / 2 }
  }))
}

/**
 * 水平分布组件
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function distributeHorizontal(components) {
  if (components.length < 3) return []
  
  // 按X坐标排序
  const sorted = [...components].sort((a, b) => a.position.x - b.position.x)
  
  const first = sorted[0]
  const last = sorted[sorted.length - 1]
  const totalWidth = sorted.reduce((sum, c) => sum + c.size.width, 0)
  const availableSpace = (last.position.x + last.size.width) - first.position.x - totalWidth
  const spacing = availableSpace / (sorted.length - 1)
  
  let currentX = first.position.x + first.size.width + spacing
  
  return sorted.slice(1, -1).map(c => {
    const newPos = { x: currentX, y: c.position.y }
    currentX += c.size.width + spacing
    return { id: c.id, position: newPos }
  })
}

/**
 * 垂直分布组件
 * @param {Array} components - 组件列表
 * @returns {Array} 更新后的组件位置
 */
export function distributeVertical(components) {
  if (components.length < 3) return []
  
  // 按Y坐标排序
  const sorted = [...components].sort((a, b) => a.position.y - b.position.y)
  
  const first = sorted[0]
  const last = sorted[sorted.length - 1]
  const totalHeight = sorted.reduce((sum, c) => sum + c.size.height, 0)
  const availableSpace = (last.position.y + last.size.height) - first.position.y - totalHeight
  const spacing = availableSpace / (sorted.length - 1)
  
  let currentY = first.position.y + first.size.height + spacing
  
  return sorted.slice(1, -1).map(c => {
    const newPos = { x: c.position.x, y: currentY }
    currentY += c.size.height + spacing
    return { id: c.id, position: newPos }
  })
}
