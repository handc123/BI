<template>
  <div class="chart-library">
    <div class="chart-type-group">
      <div class="group-title">基础图表</div>
      <div class="chart-grid">
        <div
          v-for="chart in basicCharts"
          :key="chart.type"
          class="chart-item"
          :class="{ active: selectedType === chart.type }"
          @click="selectChart(chart.type)"
        >
          <div class="chart-icon">
            <i :class="chart.icon"></i>
          </div>
          <div class="chart-name">{{ chart.name }}</div>
        </div>
      </div>
    </div>

    <div class="chart-type-group">
      <div class="group-title">高级图表</div>
      <div class="chart-grid">
        <div
          v-for="chart in advancedCharts"
          :key="chart.type"
          class="chart-item"
          :class="{ active: selectedType === chart.type }"
          @click="selectChart(chart.type)"
        >
          <div class="chart-icon">
            <i :class="chart.icon"></i>
          </div>
          <div class="chart-name">{{ chart.name }}</div>
        </div>
      </div>
    </div>

    <div class="chart-type-group">
      <div class="group-title">其他组件</div>
      <div class="chart-grid">
        <div
          v-for="chart in otherCharts"
          :key="chart.type"
          class="chart-item"
          :class="{ active: selectedType === chart.type }"
          @click="selectChart(chart.type)"
        >
          <div class="chart-icon">
            <i :class="chart.icon"></i>
          </div>
          <div class="chart-name">{{ chart.name }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BiChartLibrary',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      selectedType: this.value,
      basicCharts: [
        { type: 'kpi', name: '指标卡', icon: 'el-icon-data-line' },
        { type: 'line', name: '折线图', icon: 'el-icon-trend-charts' },
        { type: 'bar', name: '柱状图', icon: 'el-icon-s-data' }
      ],
      advancedCharts: [
        { type: 'map', name: '地图', icon: 'el-icon-map-location' },
        { type: 'pie', name: '饼图', icon: 'el-icon-pie-chart' },
        { type: 'donut', name: '环形图', icon: 'el-icon-odometer' },
        { type: 'funnel', name: '漏斗图', icon: 'el-icon-funnel-dollar' }
      ],
      otherCharts: [
        { type: 'table', name: '表格', icon: 'el-icon-notebook-2' }
      ]
    }
  },
  watch: {
    value(val) {
      this.selectedType = val
    }
  },
  methods: {
    selectChart(type) {
      this.selectedType = type
      this.$emit('input', type)
      this.$emit('change', type)
    }
  }
}
</script>

<style scoped>
.chart-library {
  padding: 10px;
}

.chart-type-group {
  margin-bottom: 20px;
}

.group-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
  padding-left: 5px;
  border-left: 3px solid #409eff;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

.chart-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 15px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.chart-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.chart-item.active {
  border-color: #409eff;
  background-color: #409eff;
  color: white;
}

.chart-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.chart-name {
  font-size: 12px;
  text-align: center;
}

.chart-item.active .chart-name {
  color: white;
}
</style>
