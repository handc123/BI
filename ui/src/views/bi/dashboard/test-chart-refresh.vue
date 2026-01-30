<template>
  <div class="test-chart-refresh">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>图表刷新测试页面</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleReset">重置</el-button>
      </div>

      <!-- 查询组件 -->
      <div class="query-section">
        <h3>查询条件</h3>
        <query-widget
          :conditions="queryConditions"
          :show-query-button="true"
          @query="handleQuery"
          @reset="handleReset"
        />
      </div>

      <!-- 图表组件 -->
      <div class="charts-section">
        <h3>图表展示 (多图表并行刷新)</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <div slot="header">销售趋势图 (自动刷新: 30秒)</div>
              <chart-widget
                :config="chartConfig1"
                :query-params="queryValues"
                :condition-mappings="conditionMappings"
                :query-conditions="queryConditions"
                :is-edit-mode="false"
                :widget-style="{ height: '400px' }"
                @data-loaded="handleDataLoaded('chart1', $event)"
                @data-error="handleDataError('chart1', $event)"
              />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <div slot="header">销售分布图 (手动刷新)</div>
              <chart-widget
                :config="chartConfig2"
                :query-params="queryValues"
                :condition-mappings="conditionMappings"
                :query-conditions="queryConditions"
                :is-edit-mode="false"
                :widget-style="{ height: '400px' }"
                @data-loaded="handleDataLoaded('chart2', $event)"
                @data-error="handleDataError('chart2', $event)"
              />
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 日志区域 -->
      <div class="log-section">
        <h3>刷新日志</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in logs"
            :key="index"
            :timestamp="log.timestamp"
            :type="log.type"
          >
            {{ log.message }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script>
import QueryWidget from '@/components/QueryWidget'
import ChartWidget from '@/components/ChartWidget'
import { mapGetters } from 'vuex'

export default {
  name: 'TestChartRefresh',
  components: {
    QueryWidget,
    ChartWidget
  },
  data() {
    return {
      // 查询条件配置
      queryConditions: [
        {
          id: 'cond_001',
          conditionName: '时间范围',
          conditionType: 'time',
          displayOrder: 1,
          isRequired: '1',
          isVisible: '1',
          defaultValue: null,
          config: JSON.stringify({
            granularity: 'day',
            rangeType: 'relative',
            format: 'yyyy-MM-dd'
          })
        },
        {
          id: 'cond_002',
          conditionName: '产品类型',
          conditionType: 'dropdown',
          displayOrder: 2,
          isRequired: '0',
          isVisible: '1',
          defaultValue: null,
          config: JSON.stringify({
            multiple: false,
            options: [
              { label: '全部', value: 'all' },
              { label: '产品A', value: 'product_a' },
              { label: '产品B', value: 'product_b' },
              { label: '产品C', value: 'product_c' }
            ]
          })
        }
      ],

      // 条件映射配置
      conditionMappings: [
        {
          id: 'map_001',
          conditionId: 'cond_001',
          componentId: 'chart_001',
          fieldName: 'date',
          mappingType: 'auto'
        },
        {
          id: 'map_002',
          conditionId: 'cond_002',
          componentId: 'chart_001',
          fieldName: 'product_type',
          mappingType: 'auto'
        },
        {
          id: 'map_003',
          conditionId: 'cond_001',
          componentId: 'chart_002',
          fieldName: 'date',
          mappingType: 'auto'
        },
        {
          id: 'map_004',
          conditionId: 'cond_002',
          componentId: 'chart_002',
          fieldName: 'product_type',
          mappingType: 'auto'
        }
      ],

      // 图表1配置 (带自动刷新)
      chartConfig1: {
        id: 'chart_001',
        type: 'chart',
        name: '销售趋势图',
        dataConfig: {
          datasetId: 1,
          dimensions: [
            { field: 'date', axis: 'x', label: '日期' }
          ],
          metrics: [
            { field: 'sales', aggregation: 'sum', label: '销售额' }
          ],
          filters: [],
          limit: 100,
          refreshInterval: 30  // 30秒自动刷新
        },
        styleConfig: {
          chartType: 'line',
          colors: ['#5470c6'],
          showLegend: true,
          smooth: true
        },
        advancedConfig: {}
      },

      // 图表2配置 (无自动刷新)
      chartConfig2: {
        id: 'chart_002',
        type: 'chart',
        name: '销售分布图',
        dataConfig: {
          datasetId: 1,
          dimensions: [
            { field: 'product_type', axis: 'x', label: '产品类型' }
          ],
          metrics: [
            { field: 'sales', aggregation: 'sum', label: '销售额' }
          ],
          filters: [],
          limit: 100,
          refreshInterval: 0  // 不自动刷新
        },
        styleConfig: {
          chartType: 'bar',
          colors: ['#91cc75'],
          showLegend: true
        },
        advancedConfig: {}
      },

      // 日志
      logs: []
    }
  },
  computed: {
    ...mapGetters('dashboard', ['queryValues'])
  },
  mounted() {
    this.addLog('页面加载完成', 'success')
  },
  methods: {
    /**
     * 处理查询
     */
    handleQuery(values) {
      this.addLog(`执行查询: ${JSON.stringify(values)}`, 'primary')
    },

    /**
     * 处理重置
     */
    handleReset() {
      this.$store.dispatch('dashboard/clearQueryValues')
      this.addLog('重置查询条件', 'info')
    },

    /**
     * 处理数据加载完成
     */
    handleDataLoaded(chartId, data) {
      this.addLog(`${chartId} 数据加载完成`, 'success')
    },

    /**
     * 处理数据加载错误
     */
    handleDataError(chartId, error) {
      this.addLog(`${chartId} 数据加载失败: ${error.message}`, 'danger')
    },

    /**
     * 添加日志
     */
    addLog(message, type = 'info') {
      const timestamp = new Date().toLocaleTimeString()
      this.logs.unshift({
        message,
        type,
        timestamp
      })

      // 限制日志数量
      if (this.logs.length > 20) {
        this.logs.pop()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.test-chart-refresh {
  padding: 20px;
}

.query-section,
.charts-section,
.log-section {
  margin-bottom: 30px;

  h3 {
    margin-bottom: 15px;
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }
}

.log-section {
  max-height: 400px;
  overflow-y: auto;
}
</style>
