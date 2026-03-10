<template>
  <div class="drill-page">
    <div class="drill-header">
      <el-button type="text" icon="el-icon-back" @click="goBack">返回</el-button>
      <div class="drill-title">穿透明细</div>
    </div>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块A：已带入条件（只读）</div>
      <div class="group-title">图表查询条件</div>
      <div class="tag-list">
        <el-tag v-for="item in inheritedChartConditions" :key="item.key" size="small" type="info">
          {{ item.label }}: {{ item.value }}
        </el-tag>
        <span v-if="inheritedChartConditions.length === 0" class="empty-hint">暂无图表带入条件</span>
      </div>

      <div class="group-title">指标口径条件</div>
      <div class="tag-list">
        <div v-for="(item, idx) in inheritedFormulaConditions" :key="'formula-'+idx" class="formula-row">
          <el-tag size="small" type="warning">指标口径</el-tag>
          <el-tag size="small" type="info">{{ item.display }}</el-tag>
          <el-button type="text" size="mini" @click="convertToEditable(item)">转为可编辑条件</el-button>
        </div>
      </div>
      <div v-if="formulaParseError" class="parse-error">
        {{ formulaParseError }}，可手动补充筛选规则
      </div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块B：指标筛选规则（多条件组）</div>
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

          <el-select v-model="rule.operator" size="small" placeholder="操作符" class="rule-op">
            <el-option v-for="op in operatorOptions" :key="op.value" :label="op.label" :value="op.value" />
          </el-select>

          <el-input v-model="rule.value" size="small" placeholder="条件值" class="rule-value" />

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
      <el-table :data="tableData" border size="mini">
        <el-table-column prop="field" label="字段" />
        <el-table-column prop="value" label="值" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getMetricMetadata } from '@/api/bi/metadata'
import { parseMetricConditions } from '@/utils/metricExpressionParser'

export default {
  name: 'DashboardDrill',
  data() {
    return {
      inheritedFormulaConditions: [],
      formulaParseError: '',
      ruleGroups: [
        {
          id: Date.now(),
          relationWithPrev: 'AND',
          rules: [{ id: Date.now() + 1, field: '', operator: '=', value: '' }]
        }
      ],
      tableData: []
    }
  },
  computed: {
    inheritedChartConditions() {
      const query = this.$route.query || {}
      const items = []
      if (query.metricId) {
        items.push({ key: 'metricId', label: '指标', value: query.metricId })
      }
      if (query.componentId) {
        items.push({ key: 'componentId', label: '图表', value: query.componentId })
      }
      if (query.orgId) {
        items.push({ key: 'orgId', label: '机构', value: query.orgId })
      }
      if (query.date) {
        items.push({ key: 'date', label: '日期', value: query.date })
      }
      const snapshot = this.parseQuerySnapshot(query.querySnapshot)
      Object.keys(snapshot).forEach(key => {
        if (['sjbsjgid', 'load_date'].includes(key)) {
          return
        }
        const value = snapshot[key]
        if (value !== undefined && value !== null && value !== '') {
          items.push({
            key: `snapshot_${key}`,
            label: key,
            value: Array.isArray(value) ? value.join(',') : String(value)
          })
        }
      })
      return items
    },
    fieldOptions() {
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
        { label: '非空', value: 'IS NOT NULL' }
      ]
    }
  },
  created() {
    this.loadFormulaConditions()
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

    async loadFormulaConditions() {
      const metricId = this.$route.query && this.$route.query.metricId
      if (!metricId) {
        return
      }

      try {
        const res = await getMetricMetadata(metricId)
        if (res.code !== 200 || !res.data) {
          return
        }
        const expression = res.data.technicalFormula || res.data.calculationLogic || ''
        const parsed = parseMetricConditions(expression)
        if (parsed.ok) {
          this.inheritedFormulaConditions = parsed.conditions
          this.formulaParseError = ''
        } else {
          this.inheritedFormulaConditions = []
          this.formulaParseError = parsed.message || '口径条件解析失败'
        }
      } catch (e) {
        this.formulaParseError = '口径条件解析失败'
      }
    },

    convertToEditable(item) {
      if (!item) return
      const group = this.ruleGroups[0]
      if (!group) return
      group.rules.push({
        id: Date.now() + Math.random(),
        field: item.field || '',
        operator: item.operator || '=',
        value: Array.isArray(item.value) ? item.value.join(',') : (item.value || '')
      })
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

    handleQuery() {
      // 占位：后续接入后端明细查询接口
      const payload = {
        metricId: this.$route.query.metricId,
        inheritedConditions: this.inheritedChartConditions,
        formulaConditions: this.inheritedFormulaConditions,
        ruleGroups: this.ruleGroups
      }
      console.log('[DrillDetail] 查询payload:', payload)
      this.tableData = [
        { field: 'metricId', value: payload.metricId || '-' },
        { field: '条件组数', value: String(payload.ruleGroups.length) }
      ]
    },

    handleReset() {
      this.ruleGroups = [
        {
          id: Date.now(),
          relationWithPrev: 'AND',
          rules: [{ id: Date.now() + 1, field: '', operator: '=', value: '' }]
        }
      ]
      this.tableData = []
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

.group-title {
  margin: 8px 0;
  font-weight: 600;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.formula-row {
  display: flex;
  align-items: center;
  gap: 8px;
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

.rule-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.rule-field {
  width: 220px;
}

.rule-op {
  width: 130px;
}

.rule-value {
  width: 240px;
}

.rule-actions,
.result-actions {
  margin-bottom: 10px;
}

.empty-hint {
  color: #909399;
}
</style>
