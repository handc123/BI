<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogVisible"
    width="800px"
    :close-on-click-modal="false"
    :modal="true"
    :append-to-body="true"
    :destroy-on-close="true"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="form" label-width="120px">
      <!-- 指标类型选择 -->
      <el-form-item label="指标类型" prop="metricType">
        <el-radio-group v-model="form.metricType" @change="handleMetricTypeChange">
          <el-radio label="base">基础指标</el-radio>
          <el-radio label="computed">计算指标</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 基础信息 -->
      <el-form-item label="指标名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入指标名称（英文标识符）"
          maxlength="100"
          show-word-limit
        >
          <template slot="append">
            <el-tooltip content="指标名称用作唯一标识，只能包含字母、数字和下划线，且必须以字母或下划线开头" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="指标别名" prop="alias">
        <el-input
          v-model="form.alias"
          placeholder="请输入指标别名（显示名称）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 基础指标表单 -->
      <template v-if="form.metricType === 'base'">
        <el-form-item label="字段" prop="field">
          <el-select
            v-model="form.field"
            placeholder="请选择字段"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="field in availableFields"
              :key="field.name"
              :label="`${field.comment || field.name} (${field.fieldType || field.type})`"
              :value="field.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="聚合函数" prop="aggregation">
          <el-select v-model="form.aggregation" placeholder="请选择聚合函数" style="width: 100%">
            <el-option label="求和 (SUM)" value="SUM" />
            <el-option label="平均值 (AVG)" value="AVG" />
            <el-option label="计数 (COUNT)" value="COUNT" />
            <el-option label="去重计数 (COUNT DISTINCT)" value="COUNT_DISTINCT" />
            <el-option label="最大值 (MAX)" value="MAX" />
            <el-option label="最小值 (MIN)" value="MIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="过滤条件">
          <el-input
            v-model="form.filterCondition"
            type="textarea"
            :rows="2"
            placeholder="可选，例如: status = 'ACTIVE' AND amount > 0"
            maxlength="500"
            show-word-limit
          >
            <template slot="prepend">WHERE</template>
          </el-input>
          <div style="margin-top: 5px; color: #909399; font-size: 12px">
            可选项，用于在聚合前过滤数据。留空表示不过滤。
          </div>
        </el-form-item>
      </template>

      <!-- 计算指标表单 -->
      <template v-if="form.metricType === 'computed'">
        <el-form-item label="计算类型" prop="computeType">
          <el-select v-model="form.computeType" placeholder="请选择计算类型" style="width: 100%" @change="handleComputeTypeChange">
            <el-option label="条件比率" value="conditional_ratio">
              <span>条件比率</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px">如：不良贷款率</span>
            </el-option>
            <el-option label="简单比率" value="simple_ratio">
              <span>简单比率</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px">如：资本充足率</span>
            </el-option>
            <el-option label="条件求和" value="conditional_sum">
              <span>条件求和</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px">如：不良贷款总额</span>
            </el-option>
            <el-option label="自定义表达式" value="custom_expression">
              <span>自定义表达式</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px">如：综合风险指数</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 条件比率表单 -->
        <template v-if="form.computeType === 'conditional_ratio'">
          <el-form-item label="字段" prop="field">
            <el-select
              v-model="form.field"
              placeholder="请选择字段"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="field in availableFields"
                :key="field.name"
                :label="`${field.name} (${field.type})`"
                :value="field.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="分子条件" prop="numeratorCondition">
            <el-input
              v-model="form.numeratorCondition"
              type="textarea"
              :rows="2"
              placeholder="例如: status = 'NPL'"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="分母条件" prop="denominatorCondition">
            <el-input
              v-model="form.denominatorCondition"
              type="textarea"
              :rows="2"
              placeholder="可选，留空表示所有记录。例如: status IN ('NORMAL', 'NPL')"
              maxlength="500"
              show-word-limit
            />
            <div style="margin-top: 5px; color: #909399; font-size: 12px">
              可选项，留空表示使用所有记录作为分母
            </div>
          </el-form-item>

          <el-form-item label="显示为百分比">
            <el-switch v-model="form.asPercentage" />
          </el-form-item>
        </template>

        <!-- 简单比率表单 -->
        <template v-if="form.computeType === 'simple_ratio'">
          <el-form-item label="分子指标" prop="numeratorMetric">
            <el-select
              v-model="form.numeratorMetric"
              placeholder="请选择分子指标"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="metric in availableMetrics"
                :key="metric.name"
                :label="`${metric.alias} (${metric.name})`"
                :value="metric.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="分母指标" prop="denominatorMetric">
            <el-select
              v-model="form.denominatorMetric"
              placeholder="请选择分母指标"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="metric in availableMetrics"
                :key="metric.name"
                :label="`${metric.alias} (${metric.name})`"
                :value="metric.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="显示为百分比">
            <el-switch v-model="form.asPercentage" />
          </el-form-item>
        </template>

        <!-- 条件求和表单 -->
        <template v-if="form.computeType === 'conditional_sum'">
          <el-form-item label="字段" prop="field">
            <el-select
              v-model="form.field"
              placeholder="请选择字段"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="field in availableFields"
                :key="field.name"
                :label="`${field.name} (${field.type})`"
                :value="field.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="条件" prop="condition">
            <el-input
              v-model="form.condition"
              type="textarea"
              :rows="2"
              placeholder="例如: status = 'NPL' AND amount > 1000000"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </template>

        <!-- 自定义表达式表单 -->
        <template v-if="form.computeType === 'custom_expression'">
          <el-form-item label="表达式" prop="expression">
            <el-input
              v-model="form.expression"
              type="textarea"
              :rows="3"
              placeholder="例如: (npl_ratio * 0.4 + overdue_ratio * 0.3 + watch_ratio * 0.3) * 100"
              maxlength="500"
              show-word-limit
            >
              <template slot="append">
                <el-button @click="showMetricReference" icon="el-icon-info">可用指标</el-button>
              </template>
            </el-input>
            <div style="margin-top: 5px; color: #909399; font-size: 12px">
              支持运算符: +、-、*、/、()，可引用已配置的指标名称
            </div>
          </el-form-item>
        </template>
      </template>

      <!-- SQL预览 -->
      <el-form-item label="SQL预览">
        <el-input
          v-model="sqlPreview"
          type="textarea"
          :rows="4"
          readonly
          placeholder="SQL预览将在这里显示"
          style="font-family: 'Courier New', monospace"
        />
        <el-button
          type="text"
          size="small"
          @click="generateSqlPreview"
          style="margin-top: 5px"
          :loading="previewLoading"
        >
          <i class="el-icon-refresh"></i> 生成预览
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 测试结果 -->
    <el-collapse v-if="testResult" style="margin-top: 20px">
      <el-collapse-item title="测试结果" name="1">
        <div v-if="testResult.success">
          <el-alert
            title="测试成功"
            type="success"
            :closable="false"
            style="margin-bottom: 10px"
          >
            <template slot="default">
              执行时间: {{ testResult.duration }}ms | 返回行数: {{ testResult.data ? testResult.data.length : 0 }}
            </template>
          </el-alert>
          <el-table
            :data="testResult.data"
            border
            size="small"
            max-height="300"
            style="width: 100%"
          >
            <el-table-column
              v-for="col in testResult.columns"
              :key="col"
              :prop="col"
              :label="col"
              min-width="120"
            />
          </el-table>
        </div>
        <el-alert
          v-else
          :title="testResult.message || '测试失败'"
          type="error"
          :closable="false"
        />
      </el-collapse-item>
    </el-collapse>

    <!-- 错误消息 -->
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      :closable="true"
      @close="errorMessage = ''"
      style="margin-top: 15px"
    />

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="info"
        @click="handleTest"
        :loading="testLoading"
        :disabled="!canTest"
      >
        <i class="el-icon-video-play"></i> 测试
      </el-button>
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="submitLoading"
      >
        确定
      </el-button>
    </div>

    <!-- 可用指标引用对话框 -->
    <el-dialog
      title="可用指标"
      :visible.sync="metricRefVisible"
      width="500px"
      append-to-body
    >
      <el-table :data="availableMetrics" border size="small">
        <el-table-column prop="name" label="指标名称" width="150" />
        <el-table-column prop="alias" label="指标别名" />
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="insertMetricRef(scope.row.name)"
            >
              插入
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-dialog>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'MetricConfigDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    datasetId: {
      type: Number,
      default: null
    },
    metric: {
      type: Object,
      default: null
    },
    availableFields: {
      type: Array,
      default: () => []
    },
    availableMetrics: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      submitLoading: false,
      testLoading: false,
      previewLoading: false,
      metricRefVisible: false,
      sqlPreview: '',
      testResult: null,
      errorMessage: '',
      form: {
        metricType: 'base',
        name: '',
        alias: '',
        // 基础指标
        field: '',
        aggregation: '',
        filterCondition: '', // 新增：基础指标的过滤条件
        // 计算指标
        computeType: '',
        // 条件比率
        numeratorCondition: '',
        denominatorCondition: '',
        asPercentage: false,
        // 简单比率
        numeratorMetric: '',
        denominatorMetric: '',
        // 条件求和
        condition: '',
        // 自定义表达式
        expression: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入指标名称', trigger: 'blur' },
          {
            pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/,
            message: '指标名称只能包含字母、数字和下划线，且必须以字母或下划线开头',
            trigger: 'blur'
          },
          { max: 100, message: '指标名称长度不能超过100个字符', trigger: 'blur' }
        ],
        alias: [
          { required: true, message: '请输入指标别名', trigger: 'blur' },
          { max: 200, message: '指标别名长度不能超过200个字符', trigger: 'blur' }
        ],
        field: [
          { required: true, message: '请选择字段', trigger: 'change' }
        ],
        aggregation: [
          { required: true, message: '请选择聚合函数', trigger: 'change' }
        ],
        computeType: [
          { required: true, message: '请选择计算类型', trigger: 'change' }
        ],
        numeratorCondition: [
          { required: true, message: '请输入分子条件', trigger: 'blur' },
          { max: 500, message: '分子条件长度不能超过500个字符', trigger: 'blur' }
        ],
        denominatorCondition: [
          { max: 500, message: '分母条件长度不能超过500个字符', trigger: 'blur' }
        ],
        numeratorMetric: [
          { required: true, message: '请选择分子指标', trigger: 'change' }
        ],
        denominatorMetric: [
          { required: true, message: '请选择分母指标', trigger: 'change' }
        ],
        condition: [
          { required: true, message: '请输入条件', trigger: 'blur' },
          { max: 500, message: '条件长度不能超过500个字符', trigger: 'blur' }
        ],
        expression: [
          { required: true, message: '请输入表达式', trigger: 'blur' },
          { max: 500, message: '表达式长度不能超过500个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.metric ? '编辑指标' : '新增指标'
    },
    canTest() {
      return this.datasetId && this.form.name && this.form.alias
    }
  },
  watch: {
    visible: {
      immediate: true,
      handler(val) {
        if (this.dialogVisible !== val) {
          this.dialogVisible = val
          if (val) {
            this.initForm()
          }
        }
      }
    },
    dialogVisible(val) {
      if (val !== this.visible) {
        this.$emit('update:visible', val)
      }
    }
  },
  methods: {
    initForm() {
      if (this.metric) {
        // 编辑模式：加载现有指标配置
        this.form = {
          metricType: this.metric.metricType || (this.metric.computeType ? 'computed' : 'base'),
          name: this.metric.name || '',
          alias: this.metric.alias || '',
          field: this.metric.field || '',
          aggregation: this.metric.aggregation || '',
          filterCondition: this.metric.filterCondition || '',
          computeType: this.metric.computeType || '',
          numeratorCondition: this.metric.numeratorCondition || '',
          denominatorCondition: this.metric.denominatorCondition || '',
          asPercentage: this.metric.asPercentage || false,
          numeratorMetric: this.metric.numeratorMetric || '',
          denominatorMetric: this.metric.denominatorMetric || '',
          condition: this.metric.condition || '',
          expression: this.metric.expression || ''
        }
        // 自动生成SQL预览
        this.$nextTick(() => {
          this.generateSqlPreview()
        })
      } else {
        // 新增模式：重置表单
        this.resetForm()
      }
      this.testResult = null
      this.errorMessage = ''
      this.sqlPreview = ''
    },

    resetForm() {
      this.form = {
        metricType: 'base',
        name: '',
        alias: '',
        field: '',
        aggregation: '',
        filterCondition: '',
        computeType: '',
        numeratorCondition: '',
        denominatorCondition: '',
        asPercentage: false,
        numeratorMetric: '',
        denominatorMetric: '',
        condition: '',
        expression: ''
      }
    },

    handleMetricTypeChange() {
      // 切换指标类型时清空相关字段
      if (this.form.metricType === 'base') {
        this.form.computeType = ''
      } else {
        this.form.field = ''
        this.form.aggregation = ''
      }
      this.sqlPreview = ''
      this.testResult = null
    },

    handleComputeTypeChange() {
      // 切换计算类型时清空相关字段
      this.form.field = ''
      this.form.numeratorCondition = ''
      this.form.denominatorCondition = ''
      this.form.numeratorMetric = ''
      this.form.denominatorMetric = ''
      this.form.condition = ''
      this.form.expression = ''
      this.form.asPercentage = false
      this.sqlPreview = ''
      this.testResult = null
    },

    buildMetricConfig() {
      const config = {
        name: this.form.name,
        alias: this.form.alias
      }

      if (this.form.metricType === 'base') {
        // 基础指标
        return {
          ...config,
          field: this.form.field,
          aggregation: this.form.aggregation,
          filterCondition: this.form.filterCondition || undefined // 只在有值时包含
        }
      } else {
        // 计算指标
        config.computeType = this.form.computeType

        if (this.form.computeType === 'conditional_ratio') {
          return {
            ...config,
            field: this.form.field,
            numeratorCondition: this.form.numeratorCondition,
            denominatorCondition: this.form.denominatorCondition,
            asPercentage: this.form.asPercentage
          }
        } else if (this.form.computeType === 'simple_ratio') {
          return {
            ...config,
            numeratorMetric: this.form.numeratorMetric,
            denominatorMetric: this.form.denominatorMetric,
            asPercentage: this.form.asPercentage
          }
        } else if (this.form.computeType === 'conditional_sum') {
          return {
            ...config,
            field: this.form.field,
            condition: this.form.condition
          }
        } else if (this.form.computeType === 'custom_expression') {
          return {
            ...config,
            expression: this.form.expression
          }
        }
      }

      return config
    },

    generateSqlPreview() {
      this.previewLoading = true
      this.errorMessage = ''

      // 简单的客户端SQL预览生成
      try {
        let sql = ''
        
        if (this.form.metricType === 'base' && this.form.field && this.form.aggregation) {
          const aggFunc = this.form.aggregation === 'COUNT_DISTINCT' 
            ? `COUNT(DISTINCT ${this.form.field})` 
            : `${this.form.aggregation}(${this.form.field})`
          
          if (this.form.filterCondition) {
            sql = `${aggFunc} FILTER (WHERE ${this.form.filterCondition}) AS ${this.form.name || 'metric'}`
          } else {
            sql = `${aggFunc} AS ${this.form.name || 'metric'}`
          }
        } else if (this.form.metricType === 'computed') {
          if (this.form.computeType === 'conditional_ratio' && this.form.field) {
            sql = `SUM(CASE WHEN ${this.form.numeratorCondition || '...'} THEN ${this.form.field} ELSE 0 END) / NULLIF(SUM(CASE WHEN ${this.form.denominatorCondition || '...'} THEN ${this.form.field} ELSE 0 END), 0) AS ${this.form.name || 'metric'}`
          } else if (this.form.computeType === 'simple_ratio') {
            sql = `${this.form.numeratorMetric || '...'} / NULLIF(${this.form.denominatorMetric || '...'}, 0) AS ${this.form.name || 'metric'}`
          } else if (this.form.computeType === 'conditional_sum' && this.form.field) {
            sql = `SUM(CASE WHEN ${this.form.condition || '...'} THEN ${this.form.field} ELSE 0 END) AS ${this.form.name || 'metric'}`
          } else if (this.form.computeType === 'custom_expression') {
            sql = `${this.form.expression || '...'} AS ${this.form.name || 'metric'}`
          }
        }

        this.sqlPreview = sql || '请完善表单以生成SQL预览'
      } catch (error) {
        this.errorMessage = 'SQL预览生成失败: ' + error.message
      } finally {
        this.previewLoading = false
      }
    },

    handleTest() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          this.$message.warning('请完善表单信息')
          return
        }

        if (!this.datasetId) {
          this.$message.error('缺少数据集ID')
          return
        }

        this.testLoading = true
        this.errorMessage = ''
        this.testResult = null

        const metricConfig = this.buildMetricConfig()

        // 构建测试请求
        const testRequest = {
          datasetId: this.datasetId,
          metric: this.form.metricType === 'base' 
            ? { baseMetrics: [metricConfig], computedMetrics: [] }
            : { baseMetrics: [], computedMetrics: [metricConfig] }
        }

        request({
          url: '/bi/dataset/metric/test',
          method: 'post',
          data: testRequest
        })
          .then(response => {
            if (response.code === 200) {
              this.testResult = response.data || response
              this.$message.success('测试成功')
            } else {
              this.errorMessage = response.msg || '测试失败'
            }
          })
          .catch(error => {
            console.error('测试指标失败:', error)
            this.errorMessage = error.msg || error.message || '测试失败'
          })
          .finally(() => {
            this.testLoading = false
          })
      })
    },

    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          this.$message.warning('请完善表单信息')
          return
        }

        this.submitLoading = true
        this.errorMessage = ''

        const metricConfig = this.buildMetricConfig()

        // 先验证配置
        const validateRequest = this.form.metricType === 'base'
          ? { baseMetrics: [metricConfig], computedMetrics: [] }
          : { baseMetrics: [], computedMetrics: [metricConfig] }

        request({
          url: '/bi/dataset/metric/validate',
          method: 'post',
          data: validateRequest
        })
          .then(response => {
            if (response.code === 200) {
              // 验证通过，返回配置
              this.$emit('submit', {
                ...metricConfig,
                metricType: this.form.metricType
              })
              this.$message.success('指标配置成功')
              this.handleClose()
            } else {
              this.errorMessage = response.msg || '验证失败'
            }
          })
          .catch(error => {
            console.error('验证指标配置失败:', error)
            this.errorMessage = error.msg || error.message || '验证失败'
          })
          .finally(() => {
            this.submitLoading = false
          })
      })
    },

    showMetricReference() {
      if (this.availableMetrics.length === 0) {
        this.$message.info('暂无可用指标，请先配置基础指标')
        return
      }
      this.metricRefVisible = true
    },

    insertMetricRef(metricName) {
      // 在光标位置插入指标引用
      this.form.expression = (this.form.expression || '') + metricName
      this.metricRefVisible = false
      this.$message.success('已插入指标引用: ' + metricName)
    },

    handleClose() {
      this.dialogVisible = false
      this.$refs.form.resetFields()
      this.resetForm()
      this.testResult = null
      this.errorMessage = ''
      this.sqlPreview = ''
    }
  },
  beforeDestroy() {
    // 确保组件销毁时清理遮罩层
    this.$nextTick(() => {
      const modals = document.querySelectorAll('.v-modal')
      modals.forEach(modal => {
        const visibleDialogs = document.querySelectorAll('.el-dialog__wrapper[style*="display"]')
        if (visibleDialogs.length === 0) {
          modal.remove()
        }
      })
    })
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-textarea >>> textarea {
  font-family: 'Courier New', Consolas, monospace;
}
</style>
