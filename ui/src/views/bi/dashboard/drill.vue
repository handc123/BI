<template>
  <div class="drill-page">
    <div class="drill-header">
      <el-button type="text" icon="el-icon-back" @click="goBack">返回</el-button>
      <div class="drill-title">穿透明细</div>
    </div>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块A：已带入条件（只读）</div>
      <div class="tag-list">
        <el-tag v-for="item in inheritedConditions" :key="item.key" size="small" type="info">
          {{ item.label }}: {{ item.value }}
        </el-tag>
        <span v-if="inheritedConditions.length === 0" class="empty-hint">暂无已带入条件</span>
      </div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块B：指标筛选规则（多条件组）</div>
      <div class="empty-hint">待实现：多条件组(AND/OR)规则编辑器</div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <div slot="header">区块C：明细结果</div>
      <div class="empty-hint">待实现：明细表格与分页查询</div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'DashboardDrill',
  computed: {
    inheritedConditions() {
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
      return items
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1)
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

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty-hint {
  color: #909399;
}
</style>
