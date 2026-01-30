<template>
  <div class="bi-join-config">
    <div class="toolbar">
      <el-button size="mini" type="primary" icon="el-icon-plus" @click="addJoin">新增 JOIN</el-button>
    </div>
    <el-table :data="localJoins" size="mini" border>
      <el-table-column label="#" width="50">
        <template slot-scope="scope">
          <span>{{ scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" prop="type" width="140">
        <template slot-scope="scope">
          <el-select v-model="scope.row.type" placeholder="JOIN 类型" size="mini">
            <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="表/视图" prop="table" width="220">
        <template slot-scope="scope">
          <el-input v-model="scope.row.table" placeholder="表名或子查询别名" size="mini" />
        </template>
      </el-table-column>
      <el-table-column label="ON 条件" prop="on">
        <template slot-scope="scope">
          <el-input v-model="scope.row.on" placeholder="示例：a.dept_id = b.dept_id" size="mini" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-delete" @click="removeJoin(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'BiJoinConfig',
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      localJoins: [],
      typeOptions: [
        { label: 'INNER JOIN', value: 'INNER' },
        { label: 'LEFT JOIN', value: 'LEFT' },
        { label: 'RIGHT JOIN', value: 'RIGHT' },
        { label: 'FULL OUTER JOIN', value: 'FULL' }
      ]
    }
  },
  watch: {
    value: {
      handler(val) {
        // 避免无限循环：只在值真正改变时更新
        const newVal = Array.isArray(val) ? JSON.parse(JSON.stringify(val)) : []
        if (JSON.stringify(newVal) !== JSON.stringify(this.localJoins)) {
          this.localJoins = newVal
        }
      },
      deep: true,
      immediate: true
    },
    localJoins: {
      handler(val) {
        // 使用 nextTick 避免同步更新导致的循环
        this.$nextTick(() => {
          this.$emit('input', val)
        })
      },
      deep: true
    }
  },
  methods: {
    addJoin() {
      this.localJoins.push({
        type: 'INNER',
        table: '',
        on: ''
      })
    },
    removeJoin(index) {
      this.localJoins.splice(index, 1)
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 8px;
}
</style>

