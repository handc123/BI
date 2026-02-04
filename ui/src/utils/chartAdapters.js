/**
 * 图表类型适配器
 * 将数据和配置转换为ECharts配置对象
 */

/**
 * 构建折线图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildLineChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: buildLegend(styleConfig),
    grid: buildGrid(styleConfig),
    xAxis: {
      type: 'category',
      data: chartData.categories || [],
      boundaryGap: false,
      axisLabel: {
        rotate: styleConfig.xAxisLabelRotate || 0,
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      },
      axisLine: {
        lineStyle: {
          color: styleConfig.axisLineColor || '#e0e0e0'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: styleConfig.yAxisFormatter || '{value}',
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      },
      axisLine: {
        lineStyle: {
          color: styleConfig.axisLineColor || '#e0e0e0'
        }
      },
      splitLine: {
        lineStyle: {
          color: styleConfig.splitLineColor || '#f0f0f0'
        }
      }
    },
    series: (chartData.series || []).map((s, index) => ({
      name: s.name,
      type: 'line',
      data: s.data,
      smooth: styleConfig.smooth !== false,
      symbol: styleConfig.showSymbol !== false ? 'circle' : 'none',
      symbolSize: styleConfig.symbolSize || 4,
      lineStyle: {
        width: styleConfig.lineWidth || 2,
        color: getColor(styleConfig.colors, index)
      },
      itemStyle: {
        color: getColor(styleConfig.colors, index)
      },
      areaStyle: styleConfig.showArea ? {
        opacity: styleConfig.areaOpacity || 0.3
      } : undefined
    }))
  }
}

/**
 * 构建柱状图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildBarChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: buildLegend(styleConfig),
    grid: buildGrid(styleConfig),
    xAxis: {
      type: 'category',
      data: chartData.categories || [],
      axisLabel: {
        rotate: styleConfig.xAxisLabelRotate || 0,
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      },
      axisLine: {
        lineStyle: {
          color: styleConfig.axisLineColor || '#e0e0e0'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: styleConfig.yAxisFormatter || '{value}',
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      },
      axisLine: {
        lineStyle: {
          color: styleConfig.axisLineColor || '#e0e0e0'
        }
      },
      splitLine: {
        lineStyle: {
          color: styleConfig.splitLineColor || '#f0f0f0'
        }
      }
    },
    series: (chartData.series || []).map((s, index) => ({
      name: s.name,
      type: 'bar',
      data: s.data,
      barWidth: styleConfig.barWidth || '60%',
      barGap: styleConfig.barGap || '30%',
      itemStyle: {
        color: getColor(styleConfig.colors, index),
        borderRadius: styleConfig.barBorderRadius || [0, 0, 0, 0]
      },
      label: styleConfig.showLabel ? {
        show: true,
        position: 'top',
        formatter: styleConfig.labelFormatter || '{c}'
      } : undefined
    }))
  }
}

/**
 * 构建饼图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildPieChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      trigger: 'item',
      formatter: styleConfig.tooltipFormatter || '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: buildLegend(styleConfig),
    series: [{
      name: styleConfig.seriesName || '数据',
      type: 'pie',
      radius: styleConfig.pieRadius || '50%',
      center: styleConfig.pieCenter || ['50%', '50%'],
      data: chartData.data || [],
      roseType: styleConfig.roseType || undefined,
      itemStyle: {
        borderRadius: styleConfig.itemBorderRadius || 0,
        borderColor: styleConfig.itemBorderColor || '#fff',
        borderWidth: styleConfig.itemBorderWidth || 2
      },
      label: {
        show: styleConfig.showLabel !== false,
        formatter: styleConfig.labelFormatter || '{b}: {d}%',
        fontSize: styleConfig.labelFontSize || 12,
        color: styleConfig.labelColor || '#666'
      },
      labelLine: {
        show: styleConfig.showLabelLine !== false,
        length: styleConfig.labelLineLength || 15,
        length2: styleConfig.labelLineLength2 || 10
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
}

/**
 * 构建仪表盘配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildGaugeChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  const data = chartData.data || []
  const value = data.length > 0 ? data[0].value : 0

  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      formatter: '{a} <br/>{b}: {c}'
    },
    series: [{
      name: styleConfig.seriesName || '指标',
      type: 'gauge',
      min: styleConfig.gaugeMin || 0,
      max: styleConfig.gaugeMax || 100,
      splitNumber: styleConfig.gaugeSplitNumber || 10,
      radius: styleConfig.gaugeRadius || '75%',
      center: styleConfig.gaugeCenter || ['50%', '55%'],
      startAngle: styleConfig.gaugeStartAngle || 225,
      endAngle: styleConfig.gaugeEndAngle || -45,
      axisLine: {
        lineStyle: {
          width: styleConfig.gaugeLineWidth || 30,
          color: styleConfig.gaugeColors || [
            [0.3, '#67e0e3'],
            [0.7, '#37a2da'],
            [1, '#fd666d']
          ]
        }
      },
      pointer: {
        itemStyle: {
          color: styleConfig.pointerColor || 'auto'
        },
        length: styleConfig.pointerLength || '80%',
        width: styleConfig.pointerWidth || 8
      },
      axisTick: {
        distance: styleConfig.axisTickDistance || -30,
        length: styleConfig.axisTickLength || 8,
        lineStyle: {
          color: styleConfig.axisTickColor || '#fff',
          width: styleConfig.axisTickWidth || 2
        }
      },
      splitLine: {
        distance: styleConfig.splitLineDistance || -30,
        length: styleConfig.splitLineLength || 30,
        lineStyle: {
          color: styleConfig.splitLineColor || '#fff',
          width: styleConfig.splitLineWidth || 4
        }
      },
      axisLabel: {
        distance: styleConfig.axisLabelDistance || -40,
        color: styleConfig.axisLabelColor || '#999',
        fontSize: styleConfig.axisLabelFontSize || 12,
        formatter: styleConfig.axisLabelFormatter || undefined
      },
      detail: {
        valueAnimation: true,
        formatter: styleConfig.detailFormatter || '{value}',
        color: styleConfig.detailColor || 'auto',
        fontSize: styleConfig.detailFontSize || 30,
        offsetCenter: styleConfig.detailOffsetCenter || [0, '70%']
      },
      data: [{
        value: value,
        name: data.length > 0 ? data[0].name : ''
      }]
    }]
  }
}

/**
 * 构建散点图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildScatterChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      trigger: 'item',
      formatter: styleConfig.tooltipFormatter || undefined
    },
    legend: buildLegend(styleConfig),
    grid: buildGrid(styleConfig),
    xAxis: {
      type: 'value',
      scale: true,
      axisLabel: {
        formatter: styleConfig.xAxisFormatter || '{value}',
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      },
      splitLine: {
        lineStyle: {
          color: styleConfig.splitLineColor || '#f0f0f0'
        }
      }
    },
    yAxis: {
      type: 'value',
      scale: true,
      axisLabel: {
        formatter: styleConfig.yAxisFormatter || '{value}',
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      },
      splitLine: {
        lineStyle: {
          color: styleConfig.splitLineColor || '#f0f0f0'
        }
      }
    },
    series: (chartData.series || []).map((s, index) => ({
      name: s.name,
      type: 'scatter',
      data: s.data,
      symbolSize: styleConfig.symbolSize || 10,
      itemStyle: {
        color: getColor(styleConfig.colors, index),
        opacity: styleConfig.symbolOpacity || 0.8
      }
    }))
  }
}

/**
 * 构建雷达图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildRadarChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      trigger: 'item'
    },
    legend: buildLegend(styleConfig),
    radar: {
      indicator: chartData.indicator || [],
      shape: styleConfig.radarShape || 'polygon',
      radius: styleConfig.radarRadius || '75%',
      center: styleConfig.radarCenter || ['50%', '50%'],
      splitNumber: styleConfig.radarSplitNumber || 5,
      name: {
        textStyle: {
          color: styleConfig.radarNameColor || '#666',
          fontSize: styleConfig.radarNameFontSize || 12
        }
      },
      splitLine: {
        lineStyle: {
          color: styleConfig.radarSplitLineColor || '#e0e0e0'
        }
      },
      splitArea: {
        show: styleConfig.showRadarSplitArea !== false,
        areaStyle: {
          color: styleConfig.radarSplitAreaColors || ['rgba(114, 172, 209, 0.2)', 'rgba(114, 172, 209, 0.4)']
        }
      },
      axisLine: {
        lineStyle: {
          color: styleConfig.radarAxisLineColor || '#e0e0e0'
        }
      }
    },
    series: [{
      type: 'radar',
      data: chartData.data || [],
      symbol: styleConfig.radarSymbol || 'circle',
      symbolSize: styleConfig.radarSymbolSize || 4,
      lineStyle: {
        width: styleConfig.radarLineWidth || 2
      },
      areaStyle: {
        opacity: styleConfig.radarAreaOpacity || 0.3
      }
    }]
  }
}

/**
 * 构建漏斗图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildFunnelChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      trigger: 'item',
      formatter: styleConfig.tooltipFormatter || '{a} <br/>{b}: {c}'
    },
    legend: buildLegend(styleConfig),
    series: [{
      name: styleConfig.seriesName || '漏斗图',
      type: 'funnel',
      left: styleConfig.funnelLeft || '10%',
      top: styleConfig.funnelTop || 60,
      bottom: styleConfig.funnelBottom || 60,
      width: styleConfig.funnelWidth || '80%',
      min: styleConfig.funnelMin || 0,
      max: styleConfig.funnelMax || 100,
      minSize: styleConfig.funnelMinSize || '0%',
      maxSize: styleConfig.funnelMaxSize || '100%',
      sort: styleConfig.funnelSort || 'descending',
      gap: styleConfig.funnelGap || 2,
      label: {
        show: styleConfig.showLabel !== false,
        position: styleConfig.labelPosition || 'inside',
        formatter: styleConfig.labelFormatter || '{b}: {c}'
      },
      labelLine: {
        length: styleConfig.labelLineLength || 10,
        lineStyle: {
          width: 1,
          type: 'solid'
        }
      },
      itemStyle: {
        borderColor: styleConfig.itemBorderColor || '#fff',
        borderWidth: styleConfig.itemBorderWidth || 1
      },
      emphasis: {
        label: {
          fontSize: 20
        }
      },
      data: chartData.data || []
    }]
  }
}

/**
 * 构建水球图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildLiquidFillChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  const data = chartData.data || []
  const value = data.length > 0 ? data[0].value : 0

  return {
    title: buildTitle(styleConfig),
    series: [{
      type: 'liquidFill',
      data: [value / 100],
      radius: styleConfig.liquidRadius || '75%',
      center: styleConfig.liquidCenter || ['50%', '50%'],
      color: styleConfig.liquidColors || ['#294D99', '#156ACF', '#1598ED', '#45BDFF'],
      backgroundStyle: {
        color: styleConfig.liquidBackgroundColor || '#e3f7ff'
      },
      label: {
        fontSize: styleConfig.labelFontSize || 50,
        color: styleConfig.labelColor || '#294D99',
        formatter: styleConfig.labelFormatter || function(param) {
          return (param.value * 100).toFixed(0) + '%'
        }
      },
      outline: {
        show: styleConfig.showOutline !== false,
        borderDistance: styleConfig.outlineBorderDistance || 8,
        itemStyle: {
          borderWidth: styleConfig.outlineBorderWidth || 8,
          borderColor: styleConfig.outlineBorderColor || '#294D99',
          shadowBlur: styleConfig.outlineShadowBlur || 20,
          shadowColor: styleConfig.outlineShadowColor || 'rgba(0, 0, 0, 0.25)'
        }
      }
    }]
  }
}

/**
 * 构建双轴图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildDualAxisChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  const leftSeries = chartData.leftSeries || []
  const rightSeries = chartData.rightSeries || []

  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: buildLegend(styleConfig),
    grid: buildGrid(styleConfig),
    xAxis: {
      type: 'category',
      data: chartData.categories || [],
      axisLabel: {
        rotate: styleConfig.xAxisLabelRotate || 0,
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      }
    },
    yAxis: [
      {
        type: 'value',
        name: styleConfig.leftYAxisName || '',
        position: 'left',
        axisLabel: {
          formatter: styleConfig.leftYAxisFormatter || '{value}',
          fontSize: styleConfig.axisLabelFontSize || 12,
          color: styleConfig.axisLabelColor || '#666'
        },
        splitLine: {
          lineStyle: {
            color: styleConfig.splitLineColor || '#f0f0f0'
          }
        }
      },
      {
        type: 'value',
        name: styleConfig.rightYAxisName || '',
        position: 'right',
        axisLabel: {
          formatter: styleConfig.rightYAxisFormatter || '{value}',
          fontSize: styleConfig.axisLabelFontSize || 12,
          color: styleConfig.axisLabelColor || '#666'
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      ...leftSeries.map((s, index) => ({
        name: s.name,
        type: s.type || 'bar',
        yAxisIndex: 0,
        data: s.data,
        itemStyle: {
          color: getColor(styleConfig.leftColors, index)
        }
      })),
      ...rightSeries.map((s, index) => ({
        name: s.name,
        type: s.type || 'line',
        yAxisIndex: 1,
        data: s.data,
        smooth: styleConfig.smooth !== false,
        itemStyle: {
          color: getColor(styleConfig.rightColors, index)
        }
      }))
    ]
  }
}

/**
 * 构建热力图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildHeatmapChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    title: buildTitle(styleConfig),
    tooltip: {
      show: true,
      position: 'top',
      formatter: styleConfig.tooltipFormatter || undefined
    },
    grid: buildGrid(styleConfig),
    xAxis: {
      type: 'category',
      data: chartData.xAxisData || [],
      splitArea: {
        show: true
      },
      axisLabel: {
        rotate: styleConfig.xAxisLabelRotate || 0,
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      }
    },
    yAxis: {
      type: 'category',
      data: chartData.yAxisData || [],
      splitArea: {
        show: true
      },
      axisLabel: {
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      }
    },
    visualMap: {
      min: styleConfig.visualMapMin || 0,
      max: styleConfig.visualMapMax || 100,
      calculable: true,
      orient: styleConfig.visualMapOrient || 'horizontal',
      left: styleConfig.visualMapLeft || 'center',
      bottom: styleConfig.visualMapBottom || '15%',
      inRange: {
        color: styleConfig.heatmapColors || ['#50a3ba', '#eac736', '#d94e5d']
      }
    },
    series: [{
      name: styleConfig.seriesName || '热力图',
      type: 'heatmap',
      data: chartData.data || [],
      label: {
        show: styleConfig.showLabel || false,
        fontSize: styleConfig.labelFontSize || 12
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
}

/**
 * 构建表格配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} 表格配置对象(非ECharts)
 */
export function buildTableOption(chartData, styleConfig = {}, dataConfig = {}) {
  return {
    columns: chartData.columns || [],
    data: chartData.data || [],
    stripe: styleConfig.stripe !== false,
    border: styleConfig.border !== false,
    size: styleConfig.size || 'medium',
    showHeader: styleConfig.showHeader !== false,
    highlightCurrentRow: styleConfig.highlightCurrentRow !== false,
    maxHeight: styleConfig.maxHeight || undefined,
    pagination: styleConfig.pagination !== false,
    pageSize: styleConfig.pageSize || 10
  }
}

/**
 * 构建指标卡配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} 指标卡配置对象
 */
export function buildMetricCardOption(chartData, styleConfig = {}, dataConfig = {}) {
  const data = chartData.data || {}

  return {
    value: data.value || 0,
    title: data.title || '',
    unit: data.unit || '',
    trend: data.trend || 0,
    trendLabel: data.trendLabel || '',
    icon: styleConfig.icon || undefined,
    iconColor: styleConfig.iconColor || '#409EFF',
    valueColor: styleConfig.valueColor || '#303133',
    titleColor: styleConfig.titleColor || '#909399',
    valueFontSize: styleConfig.valueFontSize || 32,
    titleFontSize: styleConfig.titleFontSize || 14,
    showTrend: styleConfig.showTrend !== false,
    backgroundColor: styleConfig.backgroundColor || '#fff',
    borderRadius: styleConfig.borderRadius || 4
  }
}

// ==================== 辅助函数 ====================

/**
 * 构建标题配置
 */
function buildTitle(styleConfig) {
  return {
    text: styleConfig.title || '',
    show: styleConfig.showTitle !== false,
    left: styleConfig.titleLeft || 'center',
    top: styleConfig.titleTop || 'top',
    textStyle: {
      fontSize: styleConfig.titleFontSize || 16,
      fontWeight: styleConfig.titleFontWeight || 'bold',
      color: styleConfig.titleColor || '#333'
    }
  }
}

/**
 * 构建图例配置
 */
function buildLegend(styleConfig) {
  return {
    show: styleConfig.showLegend !== false,
    orient: styleConfig.legendOrient || 'horizontal',
    left: styleConfig.legendLeft || 'center',
    top: styleConfig.legendTop || 'top',
    bottom: styleConfig.legendBottom || undefined,
    right: styleConfig.legendRight || undefined,
    textStyle: {
      fontSize: styleConfig.legendFontSize || 12,
      color: styleConfig.legendColor || '#666'
    }
  }
}

/**
 * 构建网格配置
 */
function buildGrid(styleConfig) {
  // 从 styleConfig.grid 中获取配置，如果没有则使用默认值
  const grid = styleConfig.grid || {}
  
  return {
    // 优化边距：containLabel为true时会自动计算标签空间
    // 但顶部和底部需要适当增加以防止图表线条被截断
    top: grid.top !== undefined ? grid.top : 30,
    right: grid.right !== undefined ? grid.right : 15,
    bottom: grid.bottom !== undefined ? grid.bottom : 30,
    left: grid.left !== undefined ? grid.left : 15,
    containLabel: true  // 自动计算并包含坐标轴标签的空间
  }
}

/**
 * 获取颜色
 */
function getColor(colors, index) {
  if (!colors || colors.length === 0) {
    // 默认颜色方案
    const defaultColors = [
      '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
      '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'
    ]
    return defaultColors[index % defaultColors.length]
  }
  return colors[index % colors.length]
}

/**
 * 主适配器函数 - 根据图表类型选择对应的构建函数
 * @param {String} chartType - 图表类型
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} 图表配置对象
 */
export function buildChartOption(chartType, chartData, styleConfig = {}, dataConfig = {}) {
  switch (chartType) {
    case 'line':
      return buildLineChartOption(chartData, styleConfig, dataConfig)
    case 'bar':
      return buildBarChartOption(chartData, styleConfig, dataConfig)
    case 'pie':
      return buildPieChartOption(chartData, styleConfig, dataConfig)
    case 'gauge':
      return buildGaugeChartOption(chartData, styleConfig, dataConfig)
    case 'scatter':
      return buildScatterChartOption(chartData, styleConfig, dataConfig)
    case 'radar':
      return buildRadarChartOption(chartData, styleConfig, dataConfig)
    case 'funnel':
      return buildFunnelChartOption(chartData, styleConfig, dataConfig)
    case 'liquidFill':
      return buildLiquidFillChartOption(chartData, styleConfig, dataConfig)
    case 'dualAxis':
      return buildDualAxisChartOption(chartData, styleConfig, dataConfig)
    case 'heatmap':
      return buildHeatmapChartOption(chartData, styleConfig, dataConfig)
    case 'table':
      return buildTableOption(chartData, styleConfig, dataConfig)
    case 'metricCard':
      return buildMetricCardOption(chartData, styleConfig, dataConfig)
    default:
      // 默认使用折线图
      return buildLineChartOption(chartData, styleConfig, dataConfig)
  }
}

export default {
  buildChartOption,
  buildLineChartOption,
  buildBarChartOption,
  buildPieChartOption,
  buildGaugeChartOption,
  buildScatterChartOption,
  buildRadarChartOption,
  buildFunnelChartOption,
  buildLiquidFillChartOption,
  buildDualAxisChartOption,
  buildHeatmapChartOption,
  buildTableOption,
  buildMetricCardOption
}
