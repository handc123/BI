<template>
  <div class="data-query-tab">
    <!-- 加载状态 -->
    <div v-if="loading && !dataLoaded" class="loading-container">
      <el-icon class="el-icon-loading" />
      <span>加载中...</span>
    </div>

    <!-- 数据查询内容 -->
    <div v-else class="query-content">
      <!-- 查询条件面板 -->
      <el-card class="filter-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">查询条件</span>
          <el-button
            type="text"
            icon="el-icon-refresh-left"
            @click="resetFilters"
          >
            重置
          </el-button>
        </div>

        <el-form
          ref="filterForm"
          :model="filterForm"
          :inline="true"
          size="small"
          label-width="80px"
        >
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="filterForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="yyyy-MM-dd"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="排序字段">
                <el-select
                  v-model="filterForm.sortField"
                  placeholder="请选择"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="数据时间" value="data_time" />
                  <el-option label="创建时间" value="create_time" />
                  <el-option label="更新时间" value="update_time" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="排序方式">
                <el-select
                  v-model="filterForm.sortOrder"
                  placeholder="请选择"
                  style="width: 100%"
                >
                  <el-option label="升序" value="ASC" />
                  <el-option label="降序" value="DESC" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 高级筛选 -->
          <el-collapse v-model="advancedFilterVisible" class="advanced-filter">
            <el-collapse-item title="高级筛选" name="advanced">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据源">
                    <el-select
                      v-model="filterForm.datasourceId"
                      placeholder="请选择数据源"
                      clearable
                      filterable
                      style="width: 100%"
                    >
                      <el-option
                        v-for="ds in datasourceList"
                        :key="ds.id"
                        :label="ds.name"
                        :value="ds.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="数据集">
                    <el-select
                      v-model="filterForm.datasetId"
                      placeholder="请选择数据集"
                      clearable
                      filterable
                      style="width: 100%"
                    >
                      <el-option
                        v-for="ds in datasetList"
                        :key="ds.id"
                        :label="ds.name"
                        :value="ds.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 自定义过滤条件 -->
              <div class="custom-filters">
                <div
                  v-for="(filter, index) in filterForm.customFilters"
                  :key="index"
                  class="filter-item"
                >
                  <el-select
                    v-model="filter.field"
                    placeholder="字段"
                    style="width: 150px"
                  >
                    <el-option
                      v-for="field in availableFields"
                      :key="field.name"
                      :label="field.label || field.name"
                      :value="field.name"
                    />
                  </el-select>

                  <el-select
                    v-model="filter.operator"
                    placeholder="操作符"
                    style="width: 120px"
                  >
                    <el-option label="等于" value="eq" />
                    <el-option label="不等于" value="ne" />
                    <el-option label="大于" value="gt" />
                    <el-option label="小于" value="lt" />
                    <el-option label="包含" value="like" />
                    <el-option label="介于" value="between" />
                  </el-select>

                  <el-input
                    v-model="filter.value"
                    placeholder="值"
                    style="width: 200px"
                  />

                  <el-button
                    type="danger"
                    icon="el-icon-delete"
                    size="mini"
                    circle
                    @click="removeFilter(index)"
                  />
                </div>

                <el-button
                  type="primary"
                  size="small"
                  icon="el-icon-plus"
                  @click="addFilter"
                >
                  添加条件
                </el-button>
              </div>
            </el-collapse-item>
          </el-collapse>

          <el-row style="margin-top: 20px">
            <el-col :span="24" class="button-group">
              <el-button
                type="primary"
                icon="el-icon-search"
                :loading="querying"
                @click="executeQuery"
              >
                查询
              </el-button>
              <el-button
                v-hasPermi="['bi:metric:data:export']"
                icon="el-icon-download"
                :loading="exporting"
                @click="exportData"
              >
                导出
              </el-button>
              <el-button
                icon="el-icon-refresh"
                @click="refreshCache"
              >
                刷新缓存
              </el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 数据概览 -->
      <el-card v-if="overviewData" class="overview-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">数据概览</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ overviewData.totalRecords || 0 }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ overviewData.dataFreshness || '-' }}</div>
              <div class="stat-label">数据新鲜度</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ overviewData.updateFrequency || '-' }}</div>
              <div class="stat-label">更新频率</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ overviewData.lastUpdate ? formatDate(overviewData.lastUpdate) : '-' }}</div>
              <div class="stat-label">最后更新</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 查询结果 -->
      <el-card class="result-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">查询结果</span>
          <span v-if="total > 0" class="result-count">共 {{ total }} 条记录</span>
        </div>

        <el-table
          v-loading="querying"
          :data="tableData"
          border
          stripe
          size="small"
          :height="tableHeight"
          style="width: 100%"
        >
          <el-table-column
            v-for="column in tableColumns"
            :key="column.prop"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth || 100"
            :sortable="column.sortable"
            show-overflow-tooltip
          >
            <template slot-scope="scope">
              <span v-if="column.type === 'date'">
                {{ formatDate(scope.row[column.prop]) }}
              </span>
              <span v-else-if="column.type === 'number'">
                {{ formatNumber(scope.row[column.prop]) }}
              </span>
              <el-tag v-else-if="column.type === 'tag'" :type="getTagType(scope.row[column.prop])" size="small">
                {{ scope.row[column.prop] }}
              </el-tag>
              <span v-else>{{ scope.row[column.prop] }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" fixed="right">
            <template slot-scope="scope">
              <el-button
                type="text"
                size="small"
                icon="el-icon-view"
                @click="viewDetail(scope.row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <pagination
          v-show="total > 0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="handlePagination"
        />
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      title="数据详情"
      :visible.sync="detailDialogVisible"
      width="800px"
      append-to-body
    >
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item
          v-for="(value, key) in currentRow"
          :key="key"
          :label="key"
        >
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import {
  queryMetricData,
  exportMetricData,
  getMetricDataOverview,
  refreshMetricCache
} from '@/api/bi/metric'
import { listDataSource } from '@/api/bi/datasource'
import { listDataset } from '@/api/bi/dataset'
import Pagination from '@/components/Pagination'

export default {
  name: 'DataQueryTab',
  components: {
    Pagination
  },
  props: {
    metricId: {
      type: [Number, String],
      required: true
    },
    metricInfo: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      loading: false,
      querying: false,
      exporting: false,
      dataLoaded: false,
      advancedFilterVisible: [],
      tableHeight: 500,

      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },

      // 筛选表单
      filterForm: {
        dateRange: [],
        sortField: 'data_time',
        sortOrder: 'DESC',
        datasourceId: null,
        datasetId: null,
        customFilters: []
      },

      // 表格数据
      tableData: [],
      tableColumns: [],
      total: 0,

      // 概览数据
      overviewData: null,

      // 数据源和数据集列表
      datasourceList: [],
      datasetList: [],
      availableFields: [],

      // 详情对话框
      detailDialogVisible: false,
      currentRow: null
    }
  },
  watch: {
    metricId: {
      handler(newVal) {
        if (newVal && this.activeTab === 'data') {
          // 当metricId变化且当前在数据查询标签页时，重新加载数据
          console.log('[DataQueryTab] metricId变化，重新加载数据:', newVal)
          this.loadDataById()
        }
      }
    }
  },
  mounted() {
    // 不在 mounted 时自动加载，等待父组件调用
    // 这样可以避免在标签页未显示时就加载数据
    this.loadDatasourceList()
    this.loadDatasetList()
  },
  methods: {
    async loadDataById() {
      if (!this.metricId) {
        console.warn('[DataQueryTab] metricId为空，无法加载数据')
        return
      }

      console.log('[DataQueryTab] 根据metricId加载数据:', this.metricId)

      // TODO: 先加载概览（后端API待实现）
      // await this.loadOverview()

      // 然后自动执行查询
      await this.executeQuery()
    },

    async loadOverview() {
      if (!this.metricId) return

      try {
        const res = await getMetricDataOverview(this.metricId, '7d')
        if (res.code === 200) {
          this.overviewData = res.data
        }
      } catch (error) {
        console.error('加载概览失败:', error)
      }
    },

    async loadDatasourceList() {
      try {
        const res = await listDataSource({ pageNum: 1, pageSize: 1000 })
        if (res.code === 200) {
          this.datasourceList = res.rows || []
        }
      } catch (error) {
        console.error('加载数据源列表失败:', error)
      }
    },

    async loadDatasetList() {
      try {
        const res = await listDataset({ pageNum: 1, pageSize: 1000 })
        if (res.code === 200) {
          this.datasetList = res.rows || []
        }
      } catch (error) {
        console.error('加载数据集列表失败:', error)
      }
    },

    async executeQuery() {
      if (!this.metricId) return

      this.querying = true
      try {
        // 构建查询请求
        const request = {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize,
          sortField: this.filterForm.sortField,
          sortOrder: this.filterForm.sortOrder
        }

        // 添加时间范围
        if (this.filterForm.dateRange && this.filterForm.dateRange.length === 2) {
          request.startTime = this.filterForm.dateRange[0]
          request.endTime = this.filterForm.dateRange[1]
        }

        // 添加数据源和数据集过滤
        if (this.filterForm.datasourceId) {
          request.datasourceId = this.filterForm.datasourceId
        }
        if (this.filterForm.datasetId) {
          request.datasetId = this.filterForm.datasetId
        }

        // 添加自定义过滤条件
        if (this.filterForm.customFilters.length > 0) {
          request.filters = this.filterForm.customFilters.map(f => ({
            field: f.field,
            operator: f.operator,
            value: f.value
          }))
        }

        const res = await queryMetricData(this.metricId, request)
        if (res.code === 200) {
          this.tableData = res.rows || []
          this.total = res.total || 0
          this.dataLoaded = true

          // 动态生成列
          if (this.tableData.length > 0 && this.tableColumns.length === 0) {
            this.generateTableColumns(this.tableData[0])
          }
        } else {
          this.$message.error(res.msg || '查询失败')
        }
      } catch (error) {
        console.error('查询失败:', error)
        this.$message.error('查询失败')
      } finally {
        this.querying = false
      }
    },

    generateTableColumns(sampleRow) {
      const columns = []
      for (const key in sampleRow) {
        if (Object.prototype.hasOwnProperty.call(sampleRow, key)) {
          let column = {
            prop: key,
            label: this.getFieldLabel(key),
            minWidth: 120,
            sortable: true
          }

          // 根据字段名判断类型
          if (key.includes('time') || key.includes('date')) {
            column.type = 'date'
          } else if (key.includes('status')) {
            column.type = 'tag'
          } else if (typeof sampleRow[key] === 'number') {
            column.type = 'number'
          }

          columns.push(column)
        }
      }

      this.tableColumns = columns
    },

    getFieldLabel(fieldName) {
      // 字段名称映射
      const fieldMap = {
        id: 'ID',
        data_time: '数据时间',
        metric_value: '指标值',
        create_time: '创建时间',
        update_time: '更新时间',
        create_by: '创建人',
        update_by: '更新人',
        remark: '备注'
      }
      return fieldMap[fieldName] || fieldName
    },

    async exportData() {
      if (!this.metricId) return

      // 检查权限
      const hasPermission = this.$auth.hasPermi('bi:metric:data:export')
      if (!hasPermission) {
        this.$message.error('您没有导出权限')
        return
      }

      this.exporting = true
      try {
        const request = {
          pageNum: 1,
          pageSize: 10000,
          sortField: this.filterForm.sortField,
          sortOrder: this.filterForm.sortOrder,
          exportFormat: 'excel'
        }

        if (this.filterForm.dateRange && this.filterForm.dateRange.length === 2) {
          request.startTime = this.filterForm.dateRange[0]
          request.endTime = this.filterForm.dateRange[1]
        }

        await exportMetricData(this.metricId, request)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      } finally {
        this.exporting = false
      }
    },

    async refreshCache() {
      if (!this.metricId) return

      const loading = this.$loading({
        lock: true,
        text: '正在刷新缓存...',
        spinner: 'el-icon-loading'
      })

      try {
        const res = await refreshMetricCache(this.metricId)
        if (res.code === 200) {
          this.$message.success('缓存刷新成功')
          this.loadOverview()
          this.executeQuery()
        } else {
          this.$message.error(res.msg || '刷新失败')
        }
      } catch (error) {
        console.error('刷新缓存失败:', error)
        this.$message.error('刷新缓存失败')
      } finally {
        loading.close()
      }
    },

    resetFilters() {
      this.filterForm = {
        dateRange: [],
        sortField: 'data_time',
        sortOrder: 'DESC',
        datasourceId: null,
        datasetId: null,
        customFilters: []
      }
      this.queryParams.pageNum = 1
      this.executeQuery()
    },

    addFilter() {
      this.filterForm.customFilters.push({
        field: '',
        operator: 'eq',
        value: ''
      })
    },

    removeFilter(index) {
      this.filterForm.customFilters.splice(index, 1)
    },

    handlePagination(params) {
      this.queryParams.pageNum = params.page
      this.queryParams.pageSize = params.limit
      this.executeQuery()
    },

    viewDetail(row) {
      this.currentRow = row
      this.detailDialogVisible = true
    },

    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return d.toLocaleString('zh-CN')
    },

    formatNumber(num) {
      if (typeof num !== 'number') return num
      return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
    },

    getTagType(value) {
      if (value === '0' || value === '正常' || value === '成功') {
        return 'success'
      } else if (value === '1' || value === '异常' || value === '失败') {
        return 'danger'
      } else if (value === '2' || value === '警告') {
        return 'warning'
      }
      return 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.data-query-tab {
  padding: 20px;

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;

    .el-icon-loading {
      font-size: 32px;
      margin-bottom: 10px;
    }
  }

  .query-content {
    .filter-card,
    .overview-card,
    .result-card {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }

      .result-count {
        font-size: 14px;
        color: #909399;
      }
    }

    .advanced-filter {
      margin-top: 10px;
    }

    .custom-filters {
      .filter-item {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 10px;
      }
    }

    .button-group {
      text-align: center;

      .el-button {
        margin: 0 5px;
      }
    }

    .overview-card {
      .stat-item {
        text-align: center;
        padding: 10px;
        background: #f5f7fa;
        border-radius: 4px;

        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #409eff;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
}
</style>
