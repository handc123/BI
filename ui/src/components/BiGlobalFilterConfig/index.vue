<template>
  <div class="global-filter-config">
    <div v-if="filters.length === 0" class="empty-state">
      <el-empty description="暂无全局筛选器" :image-size="60" />
    </div>
    <div v-else class="filter-list">
      <el-collapse v-model="activeFilters" accordion>
        <el-collapse-item
          v-for="(filter, index) in filters"
          :key="filter.id"
          :name="filter.id"
        >
          <template slot="title">
            <div class="filter-title">
              <i :class="getFilterIcon(filter.type)"></i>
              <span>{{ filter.label || '未命名筛选器' }}</span>
              <el-button
                type="text"
                size="mini"
                icon="el-icon-delete"
                class="delete-btn"
                @click.stop="removeFilter(index)"
              />
            </div>
          </template>
          
          <el-form :model="filter" label-width="80px" size="small">
            <el-form-item label="筛选器名称">
              <el-input v-model="filter.label" placeholder="请输入筛选器名称" />
            </el-form-item>
            
            <el-form-item label="筛选器类型">
              <el-select v-model="filter.type" placeholder="请选择类型" style="width: 100%" @change="handleTypeChange(filter)">
                <el-option label="日期范围" value="dateRange" />
                <el-option label="单选" value="single" />
                <el-option label="多选" value="multiple" />
                <el-option label="文本输入" value="text" />
                <el-option label="数值范围" value="numberRange" />
              </el-select>
            </el-form-item>

            <!-- 日期范围 -->
            <el-form-item label="默认值" v-if="filter.type === 'dateRange'">
              <el-date-picker
                v-model="filter.defaultValue"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 100%"
              />
            </el-form-item>

            <!-- 单选/多选 -->
            <template v-if="filter.type === 'single' || filter.type === 'multiple'">
              <el-form-item label="选项来源">
                <el-radio-group v-model="filter.optionSource">
                  <el-radio label="manual">手动输入</el-radio>
                  <el-radio label="dataset">数据集字段</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="选项列表" v-if="filter.optionSource === 'manual'">
                <div class="option-list">
                  <div v-for="(option, optIndex) in filter.options" :key="optIndex" class="option-item">
                    <el-input v-model="option.label" placeholder="显示名称" size="small" style="width: 45%" />
                    <el-input v-model="option.value" placeholder="值" size="small" style="width: 45%; margin-left: 5px" />
                    <el-button
                      type="text"
                      size="mini"
                      icon="el-icon-delete"
                      @click="removeOption(filter, optIndex)"
                    />
                  </div>
                  <el-button size="small" type="text" icon="el-icon-plus" @click="addOption(filter)">添加选项</el-button>
                </div>
              </el-form-item>

              <el-form-item label="数据集" v-if="filter.optionSource === 'dataset'">
                <el-select v-model="filter.datasetId" placeholder="请选择数据集" style="width: 100%" @change="loadDatasetFields(filter)">
                  <el-option
                    v-for="ds in datasetList"
                    :key="ds.id"
                    :label="ds.name"
                    :value="ds.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="字段" v-if="filter.optionSource === 'dataset' && filter.datasetId">
                <el-select v-model="filter.fieldName" placeholder="请选择字段" style="width: 100%">
                  <el-option
                    v-for="field in filter.datasetFields || []"
                    :key="field.name"
                    :label="field.alias || field.name"
                    :value="field.name"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="默认值">
                <el-select
                  v-if="filter.type === 'single'"
                  v-model="filter.defaultValue"
                  placeholder="请选择默认值"
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="option in filter.options || []"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
                <el-select
                  v-else
                  v-model="filter.defaultValue"
                  placeholder="请选择默认值"
                  multiple
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="option in filter.options || []"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </template>

            <!-- 文本输入 -->
            <el-form-item label="默认值" v-if="filter.type === 'text'">
              <el-input v-model="filter.defaultValue" placeholder="请输入默认值" />
            </el-form-item>

            <!-- 数值范围 -->
            <el-form-item label="默认值" v-if="filter.type === 'numberRange'">
              <el-row :gutter="10">
                <el-col :span="11">
                  <el-input-number v-model="filter.defaultValue[0]" placeholder="最小值" style="width: 100%" />
                </el-col>
                <el-col :span="2" style="text-align: center">-</el-col>
                <el-col :span="11">
                  <el-input-number v-model="filter.defaultValue[1]" placeholder="最大值" style="width: 100%" />
                </el-col>
              </el-row>
            </el-form-item>

            <el-divider content-position="left">应用到组件</el-divider>
            
            <el-form-item label="目标组件">
              <div class="target-fields">
                <div v-for="(target, targetIndex) in filter.targetFields" :key="targetIndex" class="target-item">
                  <el-select v-model="target.componentId" placeholder="选择组件" size="small" style="width: 45%">
                    <el-option
                      v-for="comp in availableComponents"
                      :key="comp.i"
                      :label="comp.name"
                      :value="comp.i"
                    />
                  </el-select>
                  <el-input v-model="target.field" placeholder="字段名" size="small" style="width: 45%; margin-left: 5px" />
                  <el-button
                    type="text"
                    size="mini"
                    icon="el-icon-delete"
                    @click="removeTargetField(filter, targetIndex)"
                  />
                </div>
                <el-button size="small" type="text" icon="el-icon-plus" @click="addTargetField(filter)">添加目标</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script>
import { listDataset, getDataset } from '@/api/bi/dataset'

export default {
  name: 'BiGlobalFilterConfig',
  props: {
    value: {
      type: Object,
      default: () => ({ filters: [] })
    },
    availableComponents: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      filters: [],
      activeFilters: [],
      datasetList: []
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val && val.filters) {
          this.filters = val.filters
        }
      },
      immediate: true,
      deep: true
    },
    filters: {
      handler(val) {
        this.$emit('input', { filters: val })
      },
      deep: true
    }
  },
  created() {
    this.loadDatasets()
  },
  methods: {
    loadDatasets() {
      listDataset({ pageNum: 1, pageSize: 100 }).then(response => {
        this.datasetList = response.rows || []
      })
    },
    loadDatasetFields(filter) {
      if (!filter.datasetId) return
      
      getDataset(filter.datasetId).then(response => {
        const dataset = response.data
        if (dataset.fieldConfig && typeof dataset.fieldConfig === 'string') {
          dataset.fieldConfig = JSON.parse(dataset.fieldConfig)
        }
        this.$set(filter, 'datasetFields', dataset.fieldConfig?.fields || [])
      })
    },
    handleTypeChange(filter) {
      // 重置默认值
      if (filter.type === 'dateRange') {
        filter.defaultValue = []
      } else if (filter.type === 'multiple') {
        filter.defaultValue = []
      } else if (filter.type === 'numberRange') {
        filter.defaultValue = [null, null]
      } else {
        filter.defaultValue = null
      }

      // 初始化选项
      if (filter.type === 'single' || filter.type === 'multiple') {
        if (!filter.options) {
          this.$set(filter, 'options', [])
        }
        if (!filter.optionSource) {
          this.$set(filter, 'optionSource', 'manual')
        }
      }
    },
    addOption(filter) {
      if (!filter.options) {
        this.$set(filter, 'options', [])
      }
      filter.options.push({
        label: '',
        value: ''
      })
    },
    removeOption(filter, index) {
      filter.options.splice(index, 1)
    },
    addTargetField(filter) {
      if (!filter.targetFields) {
        this.$set(filter, 'targetFields', [])
      }
      filter.targetFields.push({
        componentId: '',
        field: ''
      })
    },
    removeTargetField(filter, index) {
      filter.targetFields.splice(index, 1)
    },
    removeFilter(index) {
      this.$confirm('确定要删除此筛选器吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.filters.splice(index, 1)
        this.$message.success('筛选器已删除')
      }).catch(() => {})
    },
    getFilterIcon(type) {
      const iconMap = {
        dateRange: 'el-icon-date',
        single: 'el-icon-s-operation',
        multiple: 'el-icon-s-grid',
        text: 'el-icon-edit',
        numberRange: 'el-icon-sort'
      }
      return iconMap[type] || 'el-icon-s-operation'
    }
  }
}
</script>

<style scoped>
.global-filter-config {
  max-height: 400px;
  overflow-y: auto;
}

.empty-state {
  padding: 20px;
  text-align: center;
}

.filter-list {
  padding: 5px;
}

.filter-title {
  display: flex;
  align-items: center;
  width: 100%;
}

.filter-title i {
  margin-right: 8px;
  font-size: 16px;
}

.filter-title span {
  flex: 1;
}

.delete-btn {
  margin-left: auto;
  color: #f56c6c;
}

.option-list,
.target-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item,
.target-item {
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>
