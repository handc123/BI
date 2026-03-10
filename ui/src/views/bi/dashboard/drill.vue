<template>
  <div class="drill-page">
    <div class="drill-header">
      <el-button type="text" icon="el-icon-back" @click="goBack">返回</el-button>
      <div class="drill-title">穿透明细</div>
    </div>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块A：已带入条件（字段只读）</div>
      <div v-if="inheritedEditableRules.length === 0" class="empty-hint">暂无带入条件</div>
      <div v-for="rule in inheritedEditableRules" :key="rule.id" class="inherited-row">
        <div class="rule-label">
          <span class="required">*</span>
          <span>筛选规则：{{ getFieldLabel(rule.field) }}</span>
        </div>

        <el-select
          v-model="rule.operator"
          size="small"
          class="rule-op"
          @change="handleOperatorChange(rule)"
        >
          <el-option v-for="op in operatorOptions" :key="op.value" :label="op.label" :value="op.value" />
        </el-select>

        <div class="rule-value">
          <template v-if="isNullOperator(rule.operator)">
            <el-input size="small" value="无需填写" disabled />
          </template>
          <template v-else-if="isBetweenOperator(rule.operator) && isDateField(rule.field)">
            <el-date-picker
              v-model="rule.value"
              type="daterange"
              value-format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              class="date-range-value"
            />
          </template>
          <template v-else-if="isDateField(rule.field)">
            <el-date-picker
              v-model="rule.value"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="选择日期"
              size="small"
              class="date-single-value"
            />
          </template>
          <template v-else-if="isBetweenOperator(rule.operator)">
            <el-select
              v-model="rule.value"
              multiple
              filterable
              allow-create
              default-first-option
              size="small"
              class="multi-value-select"
              placeholder="请输入两个值并回车"
            />
          </template>
          <template v-else-if="isMultiOperator(rule.operator)">
            <el-select
              v-model="rule.value"
              multiple
              filterable
              allow-create
              default-first-option
              size="small"
              class="multi-value-select"
              placeholder="请输入并回车"
            />
          </template>
          <template v-else>
            <el-input v-model="rule.value" size="small" placeholder="条件值" />
          </template>
        </div>
      </div>

      <div v-if="formulaParseError" class="parse-error">{{ formulaParseError }}</div>

      <div class="sort-header">排序规则</div>
      <div v-if="sortRules.length === 0" class="empty-hint">未配置排序</div>
      <div v-for="sort in sortRules" :key="sort.id" class="sort-row">
        <el-select v-model="sort.field" size="small" class="sort-field" placeholder="字段">
          <el-option v-for="f in fieldOptions" :key="f.value" :label="f.label" :value="f.value" />
        </el-select>
        <el-select v-model="sort.order" size="small" class="sort-order" placeholder="排序方向">
          <el-option label="升序" value="ASC" />
          <el-option label="降序" value="DESC" />
        </el-select>
        <el-button type="text" size="mini" @click="removeSortRule(sort.id)">删除</el-button>
      </div>
      <el-button size="mini" @click="addSortRule">+ 添加排序</el-button>
    </el-card>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块B：条件组（组间 AND/OR）</div>
      <div v-for="(group, gIndex) in ruleGroups" :key="group.id" class="rule-group">
        <div class="rule-group-head">
          <div class="group-left">
            <span class="group-label">条件组 {{ gIndex + 1 }}</span>
            <el-select
              v-if="gIndex > 0"
              v-model="group.relationWithPrev"
              size="mini"
              class="relation-select"
            >
              <el-option label="且（AND）" value="AND" />
              <el-option label="或（OR）" value="OR" />
            </el-select>
          </div>
          <div>
            <el-button type="text" size="mini" @click="addRule(group)">新增条件</el-button>
            <el-button v-if="ruleGroups.length > 1" type="text" size="mini" @click="removeGroup(gIndex)">删除条件组</el-button>
          </div>
        </div>

        <div v-for="(rule, rIndex) in group.rules" :key="rule.id" class="rule-row">
          <el-select v-model="rule.field" size="small" placeholder="字段" class="rule-field">
            <el-option v-for="f in fieldOptions" :key="f.value" :label="f.label" :value="f.value" />
          </el-select>

          <el-select
            v-model="rule.operator"
            size="small"
            placeholder="操作符"
            class="rule-op"
            @change="handleOperatorChange(rule)"
          >
            <el-option v-for="op in operatorOptions" :key="op.value" :label="op.label" :value="op.value" />
          </el-select>

          <div class="rule-value">
            <template v-if="isNullOperator(rule.operator)">
              <el-input size="small" value="无需填写" disabled />
            </template>
            <template v-else-if="isBetweenOperator(rule.operator) && isDateField(rule.field)">
              <el-date-picker
                v-model="rule.value"
                type="daterange"
                value-format="yyyy-MM-dd"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                class="date-range-value"
              />
            </template>
            <template v-else-if="isDateField(rule.field)">
              <el-date-picker
                v-model="rule.value"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="选择日期"
                size="small"
                class="date-single-value"
              />
            </template>
            <template v-else-if="isBetweenOperator(rule.operator)">
              <el-select
                v-model="rule.value"
                multiple
                filterable
                allow-create
                default-first-option
                size="small"
                class="multi-value-select"
                placeholder="请输入两个值并回车"
              />
            </template>
            <template v-else-if="isMultiOperator(rule.operator)">
              <el-select
                v-model="rule.value"
                multiple
                filterable
                allow-create
                default-first-option
                size="small"
                class="multi-value-select"
                placeholder="请输入并回车"
              />
            </template>
            <template v-else>
              <el-input v-model="rule.value" size="small" placeholder="条件值" />
            </template>
          </div>

          <el-button v-if="group.rules.length > 1" type="text" size="mini" @click="removeRule(group, rIndex)">删除</el-button>
        </div>
      </div>

      <div class="rule-actions">
        <el-button size="mini" @click="addGroup">新增条件组</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块C：明细结果</div>
      <div class="result-actions">
        <el-button type="primary" size="small" @click="handleQuery">查询</el-button>
        <el-button size="small" @click="handleReset">重置</el-button>
      </div>
      <el-table :data="tableData" border size="mini" v-loading="loading">
        <el-table-column
          v-for="col in tableColumns"
          :key="col"
          :prop="col"
          :label="col"
          min-width="120"
        />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :current-page.sync="pageNum"
          :page-size="pageSize"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { getDrillConfig, queryDrillByField, queryDrillDetail } from '@/api/bi/drill'
import { parseMetricConditions } from '@/utils/metricExpressionParser'

export default {
  name: 'DashboardDrill',
  data() {
    return {
      loading: false,
      drillConfig: null,
      inheritedFormulaConditions: [],
      inheritedEditableRules: [],
      formulaParseError: '',
      sortRules: [],
      ruleGroups: [
        {
          id: Date.now(),
          relationWithPrev: 'AND',
          rules: [{ id: Date.now() + 1, field: '', operator: '=', value: '' }]
        }
      ],
      tableData: [],
      tableColumns: [],
      total: 0,
      pageNum: 1,
      pageSize: 20
    }
  },
  computed: {
    fieldOptions() {
      if (this.drillConfig && Array.isArray(this.drillConfig.fields) && this.drillConfig.fields.length > 0) {
        return this.drillConfig.fields.map(f => ({
          label: f.comment || f.fieldComment || f.fieldName || f.dbFieldName || f.field || '',
          value: f.dbFieldName || f.fieldName || f.field || ''
        })).filter(f => f.value)
      }
      return [
        { label: '数据报送机构ID', value: 'sjbsjgid' },
        { label: '五级分类', value: 'wjfl' },
        { label: '借款余额', value: 'jkye' },
        { label: '贷款类型', value: 'dklx' },
        { label: '统计日期', value: 'load_date' }
      ]
    },
    operatorOptions() {
      return [
        { label: '等于', value: '=' },
        { label: '不等于', value: '!=' },
        { label: '包含', value: 'LIKE' },
        { label: '大于', value: '>' },
        { label: '大于等于', value: '>=' },
        { label: '小于', value: '<' },
        { label: '小于等于', value: '<=' },
        { label: '介于', value: 'BETWEEN' },
        { label: '为空', value: 'IS NULL' },
        { label: '非空', value: 'IS NOT NULL' },
        { label: '属于', value: 'IN' },
        { label: '不属于', value: 'NOT IN' }
      ]
    }
  },
  created() {
    this.initPage()
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },

    parseQuerySnapshot(raw) {
      if (!raw) return {}
      try {
        return JSON.parse(raw)
      } catch (e1) {
        try {
          return JSON.parse(decodeURIComponent(raw))
        } catch (e2) {
          return {}
        }
      }
    },

    parseSnapshotFieldMap(raw) {
      if (!raw) return {}
      try {
        const parsed = JSON.parse(raw)
        return parsed && typeof parsed === 'object' ? parsed : {}
      } catch (e1) {
        try {
          const parsed = JSON.parse(decodeURIComponent(raw))
          return parsed && typeof parsed === 'object' ? parsed : {}
        } catch (e2) {
          return {}
        }
      }
    },

    normalizeLongId(id) {
      if (id === undefined || id === null || id === '') {
        return null
      }
      if (typeof id === 'number' && Number.isFinite(id)) {
        return id
      }
      const text = String(id).trim()
      if (!text) return null
      if (/^\d+$/.test(text)) {
        return Number(text)
      }
      const match = text.match(/(\d+)$/)
      return match && match[1] ? Number(match[1]) : null
    },

    async initPage() {
      const metricId = this.$route.query && this.$route.query.metricId
      const datasetId = this.$route.query && this.$route.query.datasetId
      const metricField = this.$route.query && this.$route.query.metricField
      if (!metricId && !(datasetId && metricField)) {
        this.$message.error('缺少穿透参数，请返回图表页面重试')
        return
      }
      if (metricId) {
        await this.loadDrillConfig(metricId)
        this.loadFormulaConditions()
      } else {
        this.drillConfig = null
        this.inheritedFormulaConditions = []
        this.formulaParseError = ''
      }
      this.initInheritedEditableRules()
      this.handleQuery()
    },

    async loadDrillConfig(metricId) {
      try {
        const res = await getDrillConfig(metricId)
        if (res.code === 200 && res.data) {
          this.drillConfig = res.data
        }
      } catch (e) {
        this.$message.error('加载穿透配置失败')
      }
    },

    loadFormulaConditions() {
      if (!this.drillConfig) {
        return
      }
      try {
        const expression = this.drillConfig.technicalFormula || this.drillConfig.calculationLogic || ''
        const parsed = parseMetricConditions(expression)
        if (parsed.ok) {
          this.inheritedFormulaConditions = parsed.conditions || []
          this.formulaParseError = ''
        } else {
          this.inheritedFormulaConditions = []
          this.formulaParseError = parsed.message || '口径条件解析失败'
        }
      } catch (e) {
        this.inheritedFormulaConditions = []
        this.formulaParseError = '口径条件解析失败'
      }
    },

    initInheritedEditableRules() {
      const querySnapshot = this.parseQuerySnapshot(this.$route.query.querySnapshot)
      const snapshotFieldMap = this.parseSnapshotFieldMap(this.$route.query.querySnapshotFieldMap)
      const baseRules = []
      Object.keys(querySnapshot || {}).forEach(key => {
        const value = querySnapshot[key]
        if (value === undefined || value === null || value === '') return
        const mappedField = snapshotFieldMap[key] || key
        baseRules.push({
          id: `chart_${mappedField}_${key}`,
          field: mappedField,
          operator: '=',
          value: Array.isArray(value) ? value.slice() : value
        })
      })

      this.inheritedFormulaConditions.forEach((item, idx) => {
        if (!item || !item.field) return
        const operator = this.normalizeOperatorForUi(item.operator || '=')
        baseRules.push({
          id: `formula_${idx}_${item.field}`,
          field: item.field,
          operator,
          value: this.normalizeValueForUi(operator, item.value)
        })
      })

      this.inheritedEditableRules = baseRules
        .filter(item => !!item.field)
        .filter(item => !/^\d+$/.test(String(item.field)))
        .map(item => ({
        id: item.id,
        field: item.field,
        operator: item.operator,
        value: this.normalizeValueForUi(item.operator, item.value)
      }))
    },

    addSortRule() {
      this.sortRules.push({
        id: Date.now() + Math.random(),
        field: '',
        order: 'ASC'
      })
    },

    removeSortRule(id) {
      this.sortRules = this.sortRules.filter(item => item.id !== id)
    },

    getFieldLabel(field) {
      const hit = this.fieldOptions.find(item => item.value === field)
      return hit ? hit.label : field
    },

    normalizeOperatorForUi(operator) {
      if (!operator) return '='
      const op = String(operator).trim().toUpperCase()
      if (op === 'EQ' || op === '=') return '='
      if (op === 'NE' || op === '!=' || op === '<>') return '!='
      if (op === 'GT' || op === '>') return '>'
      if (op === 'GTE' || op === '>=') return '>='
      if (op === 'LT' || op === '<') return '<'
      if (op === 'LTE' || op === '<=') return '<='
      if (op === 'IS_NULL' || op === 'IS NULL') return 'IS NULL'
      if (op === 'IS_NOT_NULL' || op === 'IS NOT NULL') return 'IS NOT NULL'
      if (op === 'NOT_IN' || op === 'NOT IN') return 'NOT IN'
      return op
    },

    normalizeValueForUi(operator, value) {
      if (this.isNullOperator(operator)) {
        return ''
      }
      if (this.isBetweenOperator(operator)) {
        if (Array.isArray(value)) {
          return value.slice(0, 2)
        }
        if (typeof value === 'string' && value.includes(',')) {
          const parts = value.split(',').map(v => (v || '').trim()).filter(Boolean)
          return parts.slice(0, 2)
        }
        return []
      }
      if (this.isMultiOperator(operator)) {
        if (Array.isArray(value)) {
          return value
        }
        if (typeof value === 'string') {
          return value.split(',').map(v => (v || '').trim()).filter(Boolean)
        }
        return value ? [String(value)] : []
      }
      return value == null ? '' : value
    },

    handleOperatorChange(rule) {
      if (!rule) return
      rule.value = this.normalizeValueForUi(rule.operator, null)
    },

    isNullOperator(operator) {
      return operator === 'IS NULL' || operator === 'IS NOT NULL'
    },

    isBetweenOperator(operator) {
      return operator === 'BETWEEN'
    },

    isMultiOperator(operator) {
      return operator === 'IN' || operator === 'NOT IN'
    },

    isDateField(field) {
      if (!field) return false
      const meta = this.drillConfig && Array.isArray(this.drillConfig.fields)
        ? this.drillConfig.fields.find(item => {
          const name = item.dbFieldName || item.fieldName || item.field || ''
          return name === field
        })
        : null
      const fieldName = String(field).toLowerCase()
      const comment = meta ? String(meta.comment || meta.fieldComment || '').toLowerCase() : ''
      const dataType = meta ? String(meta.dataType || meta.fieldType || '').toLowerCase() : ''
      return fieldName.includes('date') ||
        fieldName.includes('time') ||
        comment.includes('日期') ||
        dataType.includes('date') ||
        dataType.includes('time')
    },

    addGroup() {
      this.ruleGroups.push({
        id: Date.now() + Math.random(),
        relationWithPrev: 'AND',
        rules: [{ id: Date.now() + Math.random(), field: '', operator: '=', value: '' }]
      })
    },

    removeGroup(index) {
      this.ruleGroups.splice(index, 1)
    },

    addRule(group) {
      group.rules.push({
        id: Date.now() + Math.random(),
        field: '',
        operator: '=',
        value: ''
      })
    },

    removeRule(group, index) {
      group.rules.splice(index, 1)
    },

    toPayloadCondition(rule) {
      if (!rule || !rule.field || !rule.operator) {
        return null
      }
      const condition = {
        field: rule.field,
        operator: rule.operator
      }
      if (this.isNullOperator(rule.operator)) {
        condition.value = null
        return condition
      }
      if (this.isBetweenOperator(rule.operator)) {
        if (!Array.isArray(rule.value) || rule.value.length !== 2 || !rule.value[0] || !rule.value[1]) {
          condition.__invalid = true
          return condition
        }
        condition.values = rule.value
        return condition
      }
      if (this.isMultiOperator(rule.operator)) {
        if (!Array.isArray(rule.value) || rule.value.length === 0) {
          condition.__invalid = true
          return condition
        }
        condition.values = rule.value
        return condition
      }
      if (rule.value === '' || rule.value === null || rule.value === undefined) {
        condition.__invalid = true
        return condition
      }
      condition.value = rule.value
      return condition
    },

    buildCleanCondition(condition) {
      const target = {
        field: condition.field,
        operator: condition.operator
      }
      if (Array.isArray(condition.values)) {
        target.values = condition.values
      } else {
        target.value = Object.prototype.hasOwnProperty.call(condition, 'value') ? condition.value : null
      }
      return target
    },

    handleQuery(page) {
      if (page && Number(page) > 0) {
        this.pageNum = Number(page)
      }
      this.loading = true

      const inheritedConditions = this.inheritedEditableRules
        .filter(item => item.field && item.operator)
        .map(item => this.toPayloadCondition(item))
        .filter(Boolean)

      const ruleGroups = this.ruleGroups.map(group => ({
        relationWithPrev: group.relationWithPrev,
        rules: group.rules
          .filter(rule => rule.field && rule.operator)
          .map(rule => this.toPayloadCondition(rule))
          .filter(Boolean)
      })).filter(group => group.rules.length > 0)

      if (inheritedConditions.some(item => item.__invalid)) {
        this.loading = false
        this.$message.warning('区块A存在未完成条件，请检查后重试')
        return
      }
      if (ruleGroups.some(group => group.rules.some(item => item.__invalid))) {
        this.loading = false
        this.$message.warning('区块B存在未完成条件，请检查后重试')
        return
      }

      const invalidSort = this.sortRules.some(item => (item.field && !item.order) || (!item.field && item.order))
      if (invalidSort) {
        this.loading = false
        this.$message.warning('排序字段和排序方向需同时选择')
        return
      }

      const sortRules = this.sortRules
        .filter(item => item.field && item.order)
        .map(item => ({
          field: item.field,
          order: String(item.order).toUpperCase() === 'DESC' ? 'DESC' : 'ASC'
        }))

      const cleanedInherited = inheritedConditions.map(item => this.buildCleanCondition(item))
      const cleanedRuleGroups = ruleGroups.map(group => ({
        relationWithPrev: group.relationWithPrev,
        rules: group.rules.map(item => this.buildCleanCondition(item))
      }))

      const dashboardId = Number(this.$route.params.dashboardId)
      const componentId = this.normalizeLongId(this.$route.query.componentId)
      const metricId = this.$route.query.metricId ? Number(this.$route.query.metricId) : null

      const request = metricId ? queryDrillDetail({
        metricId,
        dashboardId,
        componentId,
        datasetId: this.drillConfig ? this.drillConfig.datasetId : null,
        inheritedConditions: cleanedInherited,
        ruleGroups: cleanedRuleGroups,
        sortRules,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }) : queryDrillByField({
        datasetId: Number(this.$route.query.datasetId),
        metricField: this.$route.query.metricField,
        metricName: this.$route.query.metricName || this.$route.query.metricField,
        dashboardId,
        componentId,
        inheritedConditions: cleanedInherited,
        ruleGroups: cleanedRuleGroups,
        sortRules,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      })

      request.then(res => {
        if (res.code === 200 && res.data) {
          this.tableData = res.data.rows || []
          this.total = res.data.total || 0
          if (this.tableData.length > 0) {
            this.tableColumns = Object.keys(this.tableData[0])
          } else {
            this.tableColumns = []
          }
        }
      }).catch(() => {
        this.$message.error('明细查询失败')
      }).finally(() => {
        this.loading = false
      })
    },

    handleReset() {
      this.initInheritedEditableRules()
      this.sortRules = []
      this.ruleGroups = [
        {
          id: Date.now(),
          relationWithPrev: 'AND',
          rules: [{ id: Date.now() + 1, field: '', operator: '=', value: '' }]
        }
      ]
      this.tableData = []
      this.tableColumns = []
      this.total = 0
      this.pageNum = 1
    }
  }
}
</script>

<style lang="scss" scoped>
.drill-page {
  padding: 16px;
}

.drill-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;

  .drill-title {
    margin-left: 8px;
    font-size: 16px;
    font-weight: 600;
  }
}

.section-card {
  margin-bottom: 12px;
}

.inherited-row,
.sort-row,
.rule-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.rule-label {
  width: 260px;
  color: #606266;
}

.required {
  color: #f56c6c;
  margin-right: 4px;
}

.sort-header {
  margin: 12px 0 8px;
  font-weight: 600;
}

.parse-error {
  margin-top: 8px;
  color: #e6a23c;
}

.rule-group {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
}

.rule-group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.group-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-label {
  font-weight: 600;
}

.relation-select {
  width: 120px;
}

.rule-field,
.sort-field {
  width: 220px;
}

.rule-op,
.sort-order {
  width: 130px;
}

.rule-value,
.date-range-value,
.date-single-value,
.multi-value-select {
  width: 240px;
}

.rule-actions,
.result-actions {
  margin-bottom: 10px;
}

.pagination-wrap {
  margin-top: 10px;
  text-align: right;
}

.empty-hint {
  color: #909399;
}
</style>
