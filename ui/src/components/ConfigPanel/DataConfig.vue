<template>
  <div class="data-config">
    <el-form :model="dataConfig" label-width="80px" size="small">
      <!-- 数据源选择 -->
      <el-form-item label="数据源">
        <el-select v-model="dataConfig.datasourceId" placeholder="请选择数据源" @change="handleDatasourceChange">
          <el-option
            v-for="ds in datasources"
            :key="ds.id"
            :label="ds.datasourceName"
            :value="ds.id"
          />
        </el-select>
      </el-form-item>

      <!-- 数据集选择 -->
      <el-form-item label="数据集">
        <el-select v-model="dataConfig.datasetId" placeholder="请选择数据集" @change="handleDatasetChange">
          <el-option
            v-for="ds in datasets"
            :key="ds.id"
            :label="ds.datasetName"
            :value="ds.id"
          />
        </el-select>
      </el-form-item>

      <!-- 字段配置 -->
      <el-divider>字段配置</el-divider>

      <!-- 维度字段 -->
      <el-form-item label="维度">
        <el-select
          v-model="dataConfig.dimensions"
          multiple
          placeholder="请选择维度字段"
          style="width: 100%"
        >
          <el-option
            v-for="field in availableFields"
            :key="field.name"
            :label="field.comment || field.name"
            :value="field.name"
          />
        </el-select>
      </el-form-item>

      <!-- 指标字段 -->
      <el-form-item label="指标">
        <el-select
          v-model="dataConfig.measures"
          multiple
          placeholder="请选择指标字段"
          style="width: 100%"
        >
          <el-option
            v-for="field in availableFields"
            :key="field.name"
            :label="field.comment || field.name"
            :value="field.name"
          />
        </el-select>
      </el-form-item>

      <!-- 过滤条件 -->
      <el-divider>过滤条件</el-divider>

      <el-form-item label="过滤">
        <div class="filter-list">
          <div
            v-for="(filter, index) in dataConfig.filters"
            :key="index"
            class="filter-item"
          >
            <el-select
              v-model="filter.field"
              placeholder="字段"
              size="small"
              style="width: 120px"
            >
              <el-option
                v-for="field in availableFields"
                :key="field.name"
                :label="field.comment || field.name"
                :value="field.name"
              />
            </el-select>
            <el-select
              v-model="filter.operator"
              placeholder="操作符"
              size="small"
              style="width: 100px"
            >
              <el-option label="等于" value="=" />
              <el-option label="不等于" value="!=" />
              <el-option label="大于" value=">" />
              <el-option label="小于" value="<" />
              <el-option label="包含" value="like" />
            </el-select>
            <el-input
              v-model="filter.value"
              placeholder="值"
              size="small"
              style="flex: 1"
            />
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="removeFilter(index)"
            />
          </div>
          <el-button
            type="text"
            icon="el-icon-plus"
            size="small"
            @click="addFilter"
          >
            添加条件
          </el-button>
        </div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleRefresh">
          刷新数据
        </el-button>
        <el-button @click="handlePreview">
          预览数据
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 数据预览对话框 -->
    <el-dialog
      title="数据预览"
      :visible.sync="previewVisible"
      width="800px"
    >
      <el-table :data="previewData" border stripe max-height="400">
        <el-table-column
          v-for="field in previewFields"
          :key="field"
          :prop="field"
          :label="field"
          min-width="120"
        />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDataSource } from '@/api/bi/datasource'
import { listDataset, getDatasetData } from '@/api/bi/dataset'

export default {
  name: 'DataConfig',
  props: {
    component: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      datasources: [],
      datasets: [],
      availableFields: [],
      dataConfig: {
        datasourceId: null,
        datasetId: null,
        dimensions: [],
        measures: [],
        filters: []
      },
      previewVisible: false,
      previewData: [],
      previewFields: []
    }
  },
  mounted() {
    this.loadDatasources()
    this.initDataConfig()
  },
  methods: {
    // 初始化数据配置
    initDataConfig() {
      if (this.component.dataConfig) {
        this.dataConfig = {
          datasourceId: this.component.dataConfig.datasourceId || null,
          datasetId: this.component.dataConfig.datasetId || null,
          dimensions: this.component.dataConfig.dimensions || [],
          measures: this.component.dataConfig.measures || [],
          filters: this.component.dataConfig.filters || []
        }

        // 如果有数据集，加载数据集信息
        if (this.dataConfig.datasetId) {
          this.loadDatasets(this.dataConfig.datasourceId)
          this.loadDatasetFields(this.dataConfig.datasetId)
        }
      }
    },

    // 加载数据源列表
    async loadDatasources() {
      try {
        const response = await listDataSource({})
        this.datasources = response.rows || []
      } catch (error) {
        console.error('加载数据源失败:', error)
      }
    },

    // 加载数据集列表
    async loadDatasets(datasourceId) {
      if (!datasourceId) {
        this.datasets = []
        return
      }
      try {
        const response = await listDataset({ datasourceId })
        this.datasets = response.rows || []
      } catch (error) {
        console.error('加载数据集失败:', error)
      }
    },

    // 加载数据集字段
    async loadDatasetFields(datasetId) {
      if (!datasetId) {
        this.availableFields = []
        return
      }
      try {
        const response = await getDatasetData(datasetId, { pageSize: 1 })
        if (response.data && response.data.fields) {
          this.availableFields = response.data.fields
        }
      } catch (error) {
        console.error('加载字段失败:', error)
      }
    },

    // 数据源变化
    handleDatasourceChange(datasourceId) {
      this.dataConfig.datasetId = null
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.filters = []
      this.availableFields = []
      this.loadDatasets(datasourceId)
      this.emitChange()
    },

    // 数据集变化
    handleDatasetChange(datasetId) {
      this.dataConfig.dimensions = []
      this.dataConfig.measures = []
      this.dataConfig.filters = []
      this.loadDatasetFields(datasetId)
      this.emitChange()
    },

    // 添加过滤条件
    addFilter() {
      if (!this.dataConfig.filters) {
        this.dataConfig.filters = []
      }
      this.dataConfig.filters.push({
        field: '',
        operator: '=',
        value: ''
      })
    },

    // 删除过滤条件
    removeFilter(index) {
      this.dataConfig.filters.splice(index, 1)
      this.emitChange()
    },

    // 刷新数据
    handleRefresh() {
      this.$emit('refresh-chart')
    },

    // 预览数据
    async handlePreview() {
      if (!this.dataConfig.datasetId) {
        this.$message.warning('请先选择数据集')
        return
      }

      try {
        const response = await getDatasetData(this.dataConfig.datasetId, {
          pageSize: 10,
          dimensions: this.dataConfig.dimensions,
          measures: this.dataConfig.measures,
          filters: this.dataConfig.filters
        })

        if (response.data && response.data.rows) {
          this.previewData = response.data.rows
          this.previewFields = response.data.fields.map(f => f.name)
          this.previewVisible = true
        }
      } catch (error) {
        console.error('预览数据失败:', error)
        this.$message.error('预览数据失败')
      }
    },

    // 触发配置变化事件
    emitChange() {
      this.$emit('change', {
        datasourceId: this.dataConfig.datasourceId,
        datasetId: this.dataConfig.datasetId,
        dimensions: this.dataConfig.dimensions,
        measures: this.dataConfig.measures,
        filters: this.dataConfig.filters
      })
    }
  },
  watch: {
    'dataConfig.dimensions'() {
      this.emitChange()
    },
    'dataConfig.measures'() {
      this.emitChange()
    }
  }
}
</script>

<style scoped>
.data-config {
  padding: 16px;
  width: 400px;
  box-sizing: border-box;
}

.el-form-item {
  margin-bottom: 16px;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.filter-list {
  width: 100%;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.filter-item > * {
  flex-shrink: 0;
}

.filter-item .el-input {
  flex: 1;
  min-width: 0;
}

.el-divider {
  margin: 16px 0;
}
</style>
