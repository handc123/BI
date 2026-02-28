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
 * 构建条形图配置（横向柱状图）- 默认为堆积条形图
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildBarHorizontalChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  console.log('[buildBarHorizontalChartOption] 配置:', { chartData, styleConfig })

  // 默认为堆积条形图，stackType: 'stack'（普通堆积）、'percent'（百分比堆积）
  const stackType = styleConfig.stackType || styleConfig.stack || 'stack'
  const stackId = stackType === 'none' ? null : (stackType === 'percent' ? 'percent-stack' : 'normal-stack')

  // 是否为百分比堆积
  const isPercentStack = stackType === 'percent'

  // 如果是百分比堆积，需要转换数据为百分比
  let processedSeries = chartData.series || []

  if (isPercentStack && processedSeries.length > 0) {
    console.log('[buildBarHorizontalChartOption] 处理百分比堆积')

    // 计算每个类别的总和
    const categorySums = {}
    const categoryCount = (chartData.categories || []).length

    processedSeries.forEach(series => {
      series.data.forEach((value, index) => {
        const category = chartData.categories[index] || `cat-${index}`
        if (!categorySums[category]) {
          categorySums[category] = 0
        }
        categorySums[category] += Math.abs(value || 0)
      })
    })

    console.log('[buildBarHorizontalChartOption] 类别总和:', categorySums)

    // 转换数据为百分比
    processedSeries = processedSeries.map(series => ({
      ...series,
      data: series.data.map((value, index) => {
        const category = chartData.categories[index] || `cat-${index}`
        const sum = categorySums[category] || 1
        const percent = sum > 0 ? ((value || 0) / sum) * 100 : 0
        return Math.round(percent * 100) / 100 // 保留两位小数
      })
    }))
  }

  // 构建系列配置
  const seriesConfig = processedSeries.map((s, index) => {
    const config = {
      name: s.name,
      type: 'bar',
      data: s.data,
      stack: stackId,
      barWidth: styleConfig.barWidth || '60%',
      barGap: styleConfig.barGap || '30%',
      itemStyle: {
        color: getColor(styleConfig.colors, index),
        borderRadius: styleConfig.barBorderRadius || [0, 0, 0, 0]
      }
    }

    // 标签配置
    if (styleConfig.showLabel) {
      config.label = {
        show: true,
        position: styleConfig.labelPosition || 'right',
        formatter: styleConfig.labelFormatter || (isPercentStack ? '{c}%' : '{c}'),
        fontSize: styleConfig.labelFontSize || 12,
        color: styleConfig.labelColor || '#666'
      }

      // 堆积图时标签位置优化
      if (stackType !== 'none') {
        config.label.position = 'inside'
      }
    }

    return config
  })

  // 构建配置对象
  const option = {
    title: buildTitle(styleConfig),
    tooltip: {
      show: styleConfig.showTooltip !== false,
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        // 百分比堆积时的 tooltip 格式化
        if (isPercentStack) {
          let result = `${params[0].axisValue}<br/>`
          params.forEach(item => {
            result += `${item.marker} ${item.seriesName}: ${item.value}%<br/>`
          })
          return result
        }
        // 默认格式化
        return params.map(item => `${item.marker} ${item.seriesName}: ${item.value}`).join('<br/>')
      }
    },
    // 默认不显示图例，可通过 showLegend: true 启用
    legend: styleConfig.showLegend ? buildLegend(styleConfig) : { show: false },
    grid: buildGrid(styleConfig),
    // 条形图：X轴是值轴，Y轴是类别轴（与柱状图相反）
    xAxis: {
      type: 'value',
      name: styleConfig.xAxisName || '',
      nameTextStyle: {
        fontSize: styleConfig.axisNameFontSize || 12,
        color: styleConfig.axisNameColor || '#666'
      },
      axisLabel: {
        formatter: isPercentStack ? '{value}%' : (styleConfig.xAxisFormatter || '{value}'),
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666'
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: styleConfig.axisLineColor || '#e0e0e0'
        }
      },
      splitLine: {
        show: styleConfig.showSplitLine !== false,
        lineStyle: {
          color: styleConfig.splitLineColor || '#f0f0f0',
          type: styleConfig.splitLineType || 'solid'
        }
      },
      max: isPercentStack ? 100 : undefined // 百分比堆积时最大值为100
    },
    yAxis: {
      type: 'category',
      data: chartData.categories || [],
      name: styleConfig.yAxisName || '',
      nameTextStyle: {
        fontSize: styleConfig.axisNameFontSize || 12,
        color: styleConfig.axisNameColor || '#666'
      },
      axisLabel: {
        rotate: styleConfig.yAxisLabelRotate || 0,
        fontSize: styleConfig.axisLabelFontSize || 12,
        color: styleConfig.axisLabelColor || '#666',
        interval: styleConfig.yAxisLabelInterval || 'auto'
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: styleConfig.axisLineColor || '#e0e0e0'
        }
      }
    },
    series: seriesConfig
  }

  // 添加缩略轴（dataZoom）配置 - 默认不显示
  if (styleConfig.showDataZoom === true) {
    const dataZoomConfig = [
      {
        type: 'slider',
        show: true,
        yAxisIndex: 0, // Y轴数据缩放（条形图的类别轴）
        filterMode: 'filter',
        left: styleConfig.dataZoomLeft || '5%',
        right: styleConfig.dataZoomRight || '5%',
        top: styleConfig.dataZoomTop || 'center',
        bottom: styleConfig.dataZoomBottom || 'auto',
        width: styleConfig.dataZoomWidth || 20,
        height: styleConfig.dataZoomHeight || '70%',
        orient: 'vertical', // 条形图使用垂直方向
        labelPrecision: styleConfig.dataZoomLabelPrecision || 0,
        zoomLock: styleConfig.dataZoomLock || false,
        showDetail: styleConfig.dataZoomShowDetail !== false,
        realtime: styleConfig.dataZoomRealtime !== false
      },
      {
        type: 'inside',
        yAxisIndex: 0,
        filterMode: 'filter',
        zoomOnMouseWheel: styleConfig.dataZoomZoomOnMouseWheel !== false,
        moveOnMouseMove: styleConfig.dataZoomMoveOnMouseMove !== false,
        moveOnMouseWheel: styleConfig.dataZoomMoveOnMouseWheel || false
      }
    ]

    option.dataZoom = dataZoomConfig
  }

  // 添加工具箱配置
  if (styleConfig.showToolbox || styleConfig.enableExport) {
    option.toolbox = {
      show: true,
      orient: 'horizontal',
      left: styleConfig.toolboxLeft || 'right',
      top: styleConfig.toolboxTop || 'top',
      feature: {
        saveAsImage: {
          show: styleConfig.enableExport !== false,
          title: '保存为图片',
          type: styleConfig.exportType || 'png',
          pixelRatio: styleConfig.exportPixelRatio || 2,
          backgroundColor: styleConfig.exportBackgroundColor || '#fff'
        },
        dataView: styleConfig.showDataView ? {
          show: true,
          title: '数据视图',
          readOnly: styleConfig.dataViewReadOnly !== false,
          lang: ['数据视图', '关闭', '刷新']
        } : false,
        restore: styleConfig.showRestore ? {
          show: true,
          title: '还原'
        } : false
      }
    }
  }

  console.log('[buildBarHorizontalChartOption] 最终配置:', option)

  return option
}

/**
 * 构建饼图配置
 * @param {Object} chartData - 图表数据
 * @param {Object} styleConfig - 样式配置
 * @param {Object} dataConfig - 数据配置
 * @returns {Object} ECharts配置对象
 */
export function buildPieChartOption(chartData, styleConfig = {}, dataConfig = {}) {
  // 为饼图数据添加颜色
  const data = chartData.data || []
  const dataWithColors = data.map((item, index) => ({
    ...item,
    itemStyle: {
      ...(item.itemStyle || {}),
      color: getColor(styleConfig.colors, index)
    }
  }))

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
      data: dataWithColors,
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

  // 如果没有设置 gaugeColors，从通用 colors 生成
  let gaugeColors = styleConfig.gaugeColors
  if (!gaugeColors && styleConfig.colors && styleConfig.colors.length >= 2) {
    const colors = styleConfig.colors
    gaugeColors = [
      [0.3, colors[0]],
      [0.7, colors[Math.floor(colors.length / 2)] || colors[0]],
      [1, colors[colors.length - 1]]
    ]
  }
  if (!gaugeColors) {
    // 默认颜色
    gaugeColors = [
      [0.3, '#67e0e3'],
      [0.7, '#37a2da'],
      [1, '#fd666d']
    ]
  }

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
          color: gaugeColors
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
  const data = chartData.data || []

  // 为雷达图数据添加颜色
  const dataWithColors = data.map((item, index) => {
    const color = getColor(styleConfig.colors, index)
    return {
      ...item,
      value: item.value,
      name: item.name,
      itemStyle: {
        color: color
      },
      areaStyle: {
        color: color,
        opacity: styleConfig.radarAreaOpacity || 0.3
      },
      lineStyle: {
        color: color,
        width: styleConfig.radarLineWidth || 2
      }
    }
  })

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
      data: dataWithColors,
      symbol: styleConfig.radarSymbol || 'circle',
      symbolSize: styleConfig.radarSymbolSize || 4
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
  const data = chartData.data || []

  // 为漏斗图数据添加颜色
  const dataWithColors = data.map((item, index) => ({
    ...item,
    itemStyle: {
      color: getColor(styleConfig.colors, index),
      borderColor: styleConfig.itemBorderColor || '#fff',
      borderWidth: styleConfig.itemBorderWidth || 1
    }
  }))

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
      emphasis: {
        label: {
          fontSize: 20
        }
      },
      data: dataWithColors
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

  // 如果没有设置 liquidColors，从通用 colors 生成渐变色
  let liquidColors = styleConfig.liquidColors
  if (!liquidColors && styleConfig.colors && styleConfig.colors.length > 0) {
    const baseColor = styleConfig.colors[0]
    // 生成渐变色：从浅到深
    liquidColors = [
      adjustColorBrightness(baseColor, 40),
      adjustColorBrightness(baseColor, 20),
      baseColor,
      adjustColorBrightness(baseColor, -20)
    ]
  }
  if (!liquidColors) {
    // 默认颜色
    liquidColors = ['#294D99', '#156ACF', '#1598ED', '#45BDFF']
  }

  return {
    title: buildTitle(styleConfig),
    series: [{
      type: 'liquidFill',
      data: [value / 100],
      radius: styleConfig.liquidRadius || '75%',
      center: styleConfig.liquidCenter || ['50%', '50%'],
      color: liquidColors,
      backgroundStyle: {
        color: styleConfig.liquidBackgroundColor || '#e3f7ff'
      },
      label: {
        fontSize: styleConfig.labelFontSize || 50,
        color: styleConfig.labelColor || liquidColors[2] || '#294D99',
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

  console.log('[buildDualAxisChartOption] leftSeries:', leftSeries)
  console.log('[buildDualAxisChartOption] rightSeries:', rightSeries)

  // 默认颜色主题 - 左轴使用冷色调，右轴使用暖色调
  const defaultLeftColors = ['#5470c6', '#91cc75', '#fac858', '#73c0de']
  const defaultRightColors = ['#ee6666', '#f39800', '#d67528', '#fc8452']

  // 如果没有设置 leftColors，从通用 colors 生成或使用默认冷色调
  let leftColors = styleConfig.leftColors
  if (!leftColors) {
    if (styleConfig.colors) {
      leftColors = styleConfig.colors
    } else {
      leftColors = defaultLeftColors
    }
  }

  // 如果没有设置 rightColors，从通用 colors 生成或使用默认暖色调
  let rightColors = styleConfig.rightColors
  if (!rightColors) {
    if (styleConfig.colors) {
      const midPoint = Math.ceil(styleConfig.colors.length / 2)
      rightColors = [
        ...styleConfig.colors.slice(midPoint),
        ...styleConfig.colors.slice(0, midPoint)
      ]
    } else {
      rightColors = defaultRightColors
    }
  }

  // 获取左轴和右轴的主色（用于轴标签颜色）
  const leftAxisColor = leftColors[0] || '#5470c6'
  const rightAxisColor = rightColors[0] || '#ee6666'

  console.log('[buildDualAxisChartOption] 左轴颜色:', leftAxisColor, '右轴颜色:', rightAxisColor)

  // 构建系列配置，支持堆积、面积、类型等
  const buildSeriesConfig = (series, yAxisIndex, colors, isLeft) => {
    return series.map((s, index) => {
      const seriesType = s.type || (isLeft ? 'bar' : 'line')

      const config = {
        name: s.name,
        type: seriesType,
        yAxisIndex: yAxisIndex,
        data: s.data,
        itemStyle: {
          color: getColor(colors, index)
        }
      }

      // 面积图配置
      if (seriesType === 'area' || s.areaStyle) {
        config.areaStyle = s.areaStyle || {
          opacity: 0.3
        }
        config.type = 'line' // ECharts 中面积图本质是带区域填充的折线图
      }

      // 堆积配置
      if (s.stack) {
        config.stack = s.stack
        // 堆积柱状图
        if (seriesType === 'bar') {
          config.stack = 'stack-' + yAxisIndex
        }
      }

      // 折线图平滑配置
      if (seriesType === 'line' || seriesType === 'area') {
        config.smooth = s.smooth !== undefined ? s.smooth : (styleConfig.smooth !== false)
      }

      // 柱状图配置
      if (seriesType === 'bar') {
        config.barWidth = styleConfig.barWidth || 'auto'
        config.barMaxWidth = styleConfig.barMaxWidth || 50
      }

      return config
    })
  }

  const allSeries = [
    ...buildSeriesConfig(leftSeries, 0, leftColors, true),
    ...buildSeriesConfig(rightSeries, 1, rightColors, false)
  ]

  console.log('[buildDualAxisChartOption] 最终系列配置:', allSeries)

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
        nameTextStyle: {
          color: leftAxisColor,
          fontSize: styleConfig.axisNameFontSize || 12
        },
        axisLabel: {
          formatter: styleConfig.leftYAxisFormatter || '{value}',
          fontSize: styleConfig.axisLabelFontSize || 12,
          color: leftAxisColor
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: leftAxisColor
          }
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
        nameTextStyle: {
          color: rightAxisColor,
          fontSize: styleConfig.axisNameFontSize || 12
        },
        axisLabel: {
          formatter: styleConfig.rightYAxisFormatter || '{value}',
          fontSize: styleConfig.axisLabelFontSize || 12,
          color: rightAxisColor
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: rightAxisColor
          }
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: allSeries
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
  // 如果没有设置 heatmapColors，从通用 colors 生成渐变色
  let heatmapColors = styleConfig.heatmapColors
  if (!heatmapColors && styleConfig.colors && styleConfig.colors.length >= 2) {
    const colors = styleConfig.colors
    // 生成渐变色：从第一个颜色到最后一个颜色
    heatmapColors = [
      colors[0],
      ...generateGradientColors(colors[0], colors[colors.length - 1], 5)
    ]
  }
  if (!heatmapColors) {
    // 默认颜色
    heatmapColors = ['#50a3ba', '#eac736', '#d94e5d']
  }

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
        color: heatmapColors
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
  console.log('[buildTableOption] 输入的 chartData:', chartData)

  // 如果数据已经是表格格式（直接从数据库查询的结果），直接返回
  if (chartData.data && Array.isArray(chartData.data) && chartData.data.length > 0) {
    console.log('[buildTableOption] 数据已经是表格格式，直接使用')
    return {
      data: chartData.data,
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

  // 如果是图表格式（categories + series），需要转换为表格格式
  const categories = chartData.categories || []
  const series = chartData.series || []

  console.log('[buildTableOption] categories:', categories)
  console.log('[buildTableOption] series:', series)

  // 获取维度字段名（用于第一列）
  const dimensions = dataConfig.dimensions || []
  const dimensionField = dimensions.length > 0
    ? (dimensions[0].fieldName || dimensions[0].field || dimensions[0].comment || '维度')
    : '维度'

  // 构建表格数据
  const tableData = categories.map((category, index) => {
    const row = {
      [dimensionField]: category
    }

    // 添加所有系列的数据
    series.forEach(s => {
      const seriesName = s.name || '指标'
      row[seriesName] = s.data[index] !== undefined ? s.data[index] : null
    })

    return row
  })

  console.log('[buildTableOption] 转换后的表格数据:', tableData)

  return {
    data: tableData,
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
  // chartData.data 是一个数组 [{value: 1.25, name: '不良贷款率'}]
  const dataList = chartData.data || []
  const data = dataList[0] || {}

  return {
    value: data.value !== undefined ? data.value : 0,
    title: data.name || data.title || styleConfig.title || '',
    unit: data.unit || styleConfig.unit || '',
    trend: data.trend !== undefined ? data.trend : (styleConfig.trend || 0),
    trendLabel: data.trendLabel || styleConfig.trendLabel || '',
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
 * 调整颜色亮度
 * @param {String} color - 十六进制颜色
 * @param {Number} amount - 调整量（-255 到 255）
 * @returns {String} 调整后的颜色
 */
function adjustColorBrightness(color, amount) {
  let usePound = false
  if (color[0] === '#') {
    color = color.slice(1)
    usePound = true
  }

  let num = parseInt(color, 16)
  let r = (num >> 16) + amount
  let g = ((num >> 8) & 0x00FF) + amount
  let b = (num & 0x0000FF) + amount

  r = r > 255 ? 255 : r < 0 ? 0 : r
  g = g > 255 ? 255 : g < 0 ? 0 : g
  b = b > 255 ? 255 : b < 0 ? 0 : b

  return (usePound ? '#' : '') + (g | (b << 8) | (r << 16)).toString(16).padStart(6, '0')
}

/**
 * 生成渐变颜色数组
 * @param {String} startColor - 起始颜色
 * @param {String} endColor - 结束颜色
 * @param {Number} steps - 步数
 * @returns {Array} 颜色数组
 */
function generateGradientColors(startColor, endColor, steps) {
  const colors = []
  for (let i = 1; i < steps; i++) {
    const ratio = i / steps
    colors.push(interpolateColor(startColor, endColor, ratio))
  }
  return colors
}

/**
 * 插值两个颜色
 * @param {String} color1 - 颜色1
 * @param {String} color2 - 颜色2
 * @param {Number} ratio - 插值比例 0-1
 * @returns {String} 插值后的颜色
 */
function interpolateColor(color1, color2, ratio) {
  const c1 = hexToRgb(color1)
  const c2 = hexToRgb(color2)

  const r = Math.round(c1.r + (c2.r - c1.r) * ratio)
  const g = Math.round(c1.g + (c2.g - c1.g) * ratio)
  const b = Math.round(c1.b + (c2.b - c1.b) * ratio)

  return `#${[r, g, b].map(x => x.toString(16).padStart(2, '0')).join('')}`
}

/**
 * 十六进制转RGB
 */
function hexToRgb(hex) {
  let c = hex
  if (c[0] === '#') {
    c = c.slice(1)
  }
  const num = parseInt(c, 16)
  return {
    r: (num >> 16) & 255,
    g: (num >> 8) & 255,
    b: num & 255
  }
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
    case 'bar-horizontal':
    case 'barHorizontal':
    case 'bar_horizontal':
      return buildBarHorizontalChartOption(chartData, styleConfig, dataConfig)
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
    case 'liquidfill':
      return buildLiquidFillChartOption(chartData, styleConfig, dataConfig)
    case 'dualAxis':
    case 'dual-axis':
    case 'combination':
      return buildDualAxisChartOption(chartData, styleConfig, dataConfig)
    case 'heatmap':
      return buildHeatmapChartOption(chartData, styleConfig, dataConfig)
    case 'table':
    case 'summary-table':
    case 'pivot-table':
      // 所有表格类型统一处理
      return buildTableOption(chartData, styleConfig, dataConfig)
    case 'card':
    case 'metricCard':
    case 'kpi':
      // 指标卡支持多种类型名称
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
