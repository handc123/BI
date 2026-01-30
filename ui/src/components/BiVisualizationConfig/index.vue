<template>
  <div class="visualization-config">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="数据配置" name="data">
        <el-form :model="config" label-width="80px" size="small">
          <el-form-item label="维度">
            <el-select
              v-model="config.dimensions"
              multiple
              placeholder="选择维度字段"
              style="width: 100%"
            >
              <el-option
                v-for="field in dimensionFields"
                :key="field.name"
                :label="field.alias || field.name"
                :value="field.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="指标">
            <div
              v-for="(metric, index) in config.metrics"
              :key="index"
              class="metric-item"
            >
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-select
                    v-model="metric.field"
                    placeholder="选择字段"
                    size="small"
                    @change="handleMetricFieldChange(index)"
                  >
                    <el-option
                      v-for="field in metricFields"
                      :key="field.name"
                      :label="field.alias || field.name"
                      :value="field.name"
                    />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-select
                    v-model="metric.aggregation"
                    placeholder="聚合方式"
                    size="small"
                  >
                    <el-option label="求和" value="SUM" />
                    <el-option label="平均" value="AVG" />
                    <el-option label="计数" value="COUNT" />
                    <el-option label="最大值" value="MAX" />
                    <el-option label="最小值" value="MIN" />
                    <el-option label="不重复计数" value="COUNT_DISTINCT" />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-button
                    type="danger"
                    icon="el-icon-delete"
                    size="small"
                    @click="removeMetric(index)"
                  />
                </el-col>
              </el-row>
            </div>
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="small"
              @click="addMetric"
            >
              添加指标
            </el-button>
          </el-form-item>

          <el-form-item label="筛选条件">
            <div
              v-for="(filter, index) in config.filters"
              :key="index"
              class="filter-item"
            >
              <el-row :gutter="10">
                <el-col :span="7">
                  <el-select
                    v-model="filter.field"
                    placeholder="选择字段"
                    size="small"
                  >
                    <el-option
                      v-for="field in allFields"
                      :key="field.name"
                      :label="field.alias || field.name"
                      :value="field.name"
                    />
                  </el-select>
                </el-col>
                <el-col :span="5">
                  <el-select
                    v-model="filter.operator"
                    placeholder="操作符"
                    size="small"
                  >
                    <el-option label="等于" value="eq" />
                    <el-option label="不等于" value="ne" />
                    <el-option label="大于" value="gt" />
                    <el-option label="大于等于" value="gte" />
                    <el-option label="小于" value="lt" />
                    <el-option label="小于等于" value="lte" />
                    <el-option label="包含" value="like" />
                    <el-option label="在列表中" value="in" />
                    <el-option label="不在列表中" value="not_in" />
                    <el-option label="为空" value="is_null" />
                    <el-option label="不为空" value="not_null" />
                    <el-option label="介于" value="between" />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-input
                    v-if="!['is_null', 'not_null'].includes(filter.operator)"
                    v-model="filter.value"
                    placeholder="输入值"
                    size="small"
                  />
                  <el-input
                    v-if="filter.operator === 'between'"
                    v-model="filter.value2"
                    placeholder="结束值"
                    size="small"
                    style="margin-left: 5px"
                  />
                </el-col>
                <el-col :span="4">
                  <el-button
                    type="danger"
                    icon="el-icon-delete"
                    size="small"
                    @click="removeFilter(index)"
                  />
                </el-col>
              </el-row>
            </div>
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="small"
              @click="addFilter"
            >
              添加筛选
            </el-button>
          </el-form-item>

          <el-form-item label="排序">
            <div
              v-for="(sort, index) in config.sort"
              :key="index"
              class="sort-item"
            >
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-select
                    v-model="sort.field"
                    placeholder="选择字段"
                    size="small"
                  >
                    <el-option
                      v-for="field in allFields"
                      :key="field.name"
                      :label="field.alias || field.name"
                      :value="field.name"
                    />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-select v-model="sort.order" size="small">
                    <el-option label="升序" value="ASC" />
                    <el-option label="降序" value="DESC" />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-button
                    type="danger"
                    icon="el-icon-delete"
                    size="small"
                    @click="removeSort(index)"
                  />
                </el-col>
              </el-row>
            </div>
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="small"
              @click="addSort"
            >
              添加排序
            </el-button>
          </el-form-item>

          <el-form-item label="数据限制">
            <el-input-number
              v-model="config.limit"
              :min="1"
              :max="100000"
              size="small"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="图表配置" name="chart">
        <el-form :model="config.chartOptions" label-width="80px" size="small">
          <el-form-item label="图表标题">
            <el-input v-model="config.chartOptions.title" />
          </el-form-item>

          <template v-if="chartType === 'line' || chartType === 'bar'">
            <el-form-item label="X轴类型">
              <el-select v-model="config.chartOptions.xAxis.type">
                <el-option label="类目轴" value="category" />
                <el-option label="时间轴" value="time" />
                <el-option label="数值轴" value="value" />
              </el-select>
            </el-form-item>

            <el-form-item v-if="config.chartOptions.xAxis.type === 'time'" label="日期格式">
              <el-input v-model="config.chartOptions.xAxis.format" placeholder="YYYY-MM-DD" />
            </el-form-item>

            <el-form-item label="Y轴名称">
              <el-input v-model="config.chartOptions.yAxis.name" />
            </el-form-item>

            <el-form-item v-if="chartType === 'bar'" label="柱状图类型">
              <el-radio-group v-model="config.chartOptions.seriesType">
                <el-radio label="group">分组</el-radio>
                <el-radio label="stack">堆叠</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="chartType === 'line'" label="平滑曲线">
              <el-switch v-model="config.chartOptions.series.smooth" />
            </el-form-item>

            <el-form-item v-if="chartType === 'line'" label="显示符号">
              <el-switch v-model="config.chartOptions.series.showSymbol" />
            </el-form-item>

            <el-form-item v-if="chartType === 'line'" label="填充区域">
              <el-switch v-model="config.chartOptions.series.areaStyle" />
            </el-form-item>
          </template>

          <template v-if="chartType === 'pie' || chartType === 'donut'">
            <el-form-item label="标签格式">
              <el-select v-model="config.chartOptions.labelFormat">
                <el-option label="数值" value="value" />
                <el-option label="百分比" value="percent" />
                <el-option label="数值+百分比" value="both" />
              </el-select>
            </el-form-item>

            <el-form-item v-if="chartType === 'donut'" label="内径百分比">
              <el-input-number
                v-model="config.chartOptions.innerRadius"
                :min="0"
                :max="100"
                :step="10"
              />
              <span class="unit">%</span>
            </el-form-item>
          </template>

          <template v-if="chartType === 'map'">
            <el-form-item label="地图级别">
              <el-select v-model="config.chartOptions.mapLevel">
                <el-option label="省份" value="province" />
                <el-option label="城市" value="city" />
              </el-select>
            </el-form-item>

            <el-form-item label="可视化映射">
              <el-radio-group v-model="config.chartOptions.visualMapType">
                <el-radio label="continuous">连续型" />
                <el-radio label="piecewise">分段型" />
              </el-radio-group>
            </el-form-item>
          </template>

          <template v-if="chartType === 'table'">
            <el-form-item label="分页大小">
              <el-select v-model="config.chartOptions.pageSize">
                <el-option label="10" :value="10" />
                <el-option label="20" :value="20" />
                <el-option label="50" :value="50" />
                <el-option label="100" :value="100" />
              </el-select>
            </el-form-item>

            <el-form-item label="显示序号">
              <el-switch v-model="config.chartOptions.showIndex" />
            </el-form-item>

            <el-form-item label="可排序">
              <el-switch v-model="config.chartOptions.sortable" />
            </el-form-item>
          </template>

          <template v-if="chartType === 'funnel'">
            <el-form-item label="对齐方式">
              <el-select v-model="config.chartOptions.align">
                <el-option label="居中" value="center" />
                <el-option label="左对齐" value="left" />
              </el-select>
            </el-form-item>
          </template>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="样式配置" name="style">
        <el-form :model="config.chartOptions" label-width="100px" size="small">
          <el-form-item label="主题颜色">
            <el-select v-model="config.chartOptions.colorScheme">
              <el-option label="默认" value="default" />
              <el-option label="科技蓝" value="blue" />
              <el-option label="活力橙" value="orange" />
              <el-option label="清新绿" value="green" />
              <el-option label="优雅紫" value="purple" />
            </el-select>
          </el-form-item>

          <el-form-item label="显示图例">
            <el-switch v-model="config.chartOptions.legend.show" />
          </el-form-item>

          <el-form-item label="图例位置" v-if="config.chartOptions.legend.show">
            <el-select v-model="config.chartOptions.legend.position">
              <el-option label="顶部" value="top" />
              <el-option label="底部" value="bottom" />
              <el-option label="左侧" value="left" />
              <el-option label="右侧" value="right" />
            </el-select>
          </el-form-item>

          <el-form-item label="显示提示框">
            <el-switch v-model="config.chartOptions.tooltip.show" />
          </el-form-item>

          <el-form-item label="背景颜色">
            <el-color-picker v-model="config.chartOptions.backgroundColor" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
export default {
  name: 'BiVisualizationConfig',
  props: {
    chartType: {
      type: String,
      default: ''
    },
    datasetFields: {
      type: Array,
      default: () => []
    },
    value: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      activeTab: 'data',
      config: {
        dimensions: [],
        metrics: [],
        filters: [],
        sort: [],
        limit: 1000,
        chartOptions: {
          title: '',
          xAxis: {
            type: 'category',
            format: 'YYYY-MM-DD'
          },
          yAxis: {
            name: ''
          },
          series: {
            type: 'group',
            smooth: false,
            showSymbol: false,
            areaStyle: false
          },
          labelFormat: 'both',
          innerRadius: 50,
          mapLevel: 'province',
          visualMapType: 'continuous',
          pageSize: 20,
          showIndex: true,
          sortable: true,
          align: 'center',
          colorScheme: 'default',
          legend: {
            show: true,
            position: 'top'
          },
          tooltip: {
            show: true
          },
          backgroundColor: '#ffffff'
        }
      }
    }
  },
  computed: {
    dimensionFields() {
      return this.datasetFields.filter(f => 
        !['BIGINT', 'INT', 'DECIMAL', 'FLOAT', 'DOUBLE'].includes(f.type)
      )
    },
    metricFields() {
      return this.datasetFields.filter(f => 
        ['BIGINT', 'INT', 'DECIMAL', 'FLOAT', 'DOUBLE'].includes(f.type)
      )
    },
    allFields() {
      return this.datasetFields
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val && Object.keys(val).length > 0) {
          this.config = this.mergeConfig(this.config, val)
        }
      },
      deep: true,
      immediate: true
    },
    config: {
      handler(val) {
        this.$emit('input', val)
        this.$emit('change', val)
      },
      deep: true
    }
  },
  methods: {
    mergeConfig(target, source) {
      const result = JSON.parse(JSON.stringify(target))
      for (const key in source) {
        if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
          result[key] = this.mergeConfig(result[key] || {}, source[key])
        } else {
          result[key] = source[key]
        }
      }
      return result
    },
    addMetric() {
      this.config.metrics.push({
        field: '',
        aggregation: 'SUM',
        alias: ''
      })
    },
    removeMetric(index) {
      this.config.metrics.splice(index, 1)
    },
    handleMetricFieldChange(index) {
      const metric = this.config.metrics[index]
      if (metric.field) {
        const field = this.allFields.find(f => f.name === metric.field)
        if (field) {
          metric.alias = field.alias || field.name
        }
      }
    },
    addFilter() {
      this.config.filters.push({
        field: '',
        operator: 'eq',
        value: '',
        value2: ''
      })
    },
    removeFilter(index) {
      this.config.filters.splice(index, 1)
    },
    addSort() {
      this.config.sort.push({
        field: '',
        order: 'ASC'
      })
    },
    removeSort(index) {
      this.config.sort.splice(index, 1)
    }
  }
}
</script>

<style scoped>
.visualization-config {
  height: 100%;
}

.metric-item,
.filter-item,
.sort-item {
  margin-bottom: 10px;
}

.unit {
  margin-left: 5px;
  color: #909399;
}
</style>
