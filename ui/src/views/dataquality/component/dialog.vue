<template>
  <div>
    <el-dialog
      :visible.sync="dialogVisible"
      :title="title"
      width="1000px"
      :close-on-click-modal="true"
      :close-on-press-escape="false"
      :show-close="true"
      @close="closeDialog"
    >
      <div class="display-flex justify-between alicenter">
        <el-form label-width="80px" :inline="true">
          <el-form-item label="数据日期">
            <el-date-picker
              v-model="queryParams.date"
              type="date"
              placeholder="选择日期"
              value-format="yyyy-MM-dd"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <el-button type="primary">查询</el-button>
      </div>
      <el-button type="primary" style="margin: 10px">导出</el-button>
      <!-- 一表通字段质量看板 -->
      <el-table :data="basicData" style="height: 420px;" class="overflow-true" v-if="title=='基础数据通过率排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName">
          <template slot-scope="scope">
            <div style="display: inline-flex">
              <div>
                {{ scope.row.orgName }}
              </div>
              <div v-if="scope.row.rank.length > 1" :style="{color: scope.row.rank >= 0 ? 'green' : 'red'}" style="margin-left: 10px;">
                {{ scope.row.rank >= 0 ? '&#8593' : '&#8595' }}
                {{ scope.row.rank.substring(1) }}
              </div>
              <div v-else style="margin-left: 24px;">{{ scope.row.rank }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="通过率" align="center" prop="passRate" width="60px"></el-table-column>
      </el-table>
      <el-table :data="basicData" style="height: 420px;" class="overflow-true" v-if="title=='基础数据通过项排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName">
          <template slot-scope="scope">
            <div style="display: inline-flex">
              <div>
                {{ scope.row.orgName }}
              </div>
              <div v-if="scope.row.rank.length > 1" :style="{color: scope.row.rank >= 0 ? 'green' : 'red'}" style="margin-left: 10px;">
                {{ scope.row.rank >= 0 ? '&#8593' : '&#8595' }}
                {{ scope.row.rank.substring(1) }}
              </div>
              <div v-else style="margin-left: 24px;">{{ scope.row.rank }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="通过率" align="center" prop="passRate" width="60px"></el-table-column>
      </el-table>
      <el-table :data="basicDataImproveRate" style="height: 420px;" class="overflow-true" v-if="title=='本月基础数据提升率排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName"></el-table-column>
        <el-table-column label="规则编码" align="center" prop="rule">
          <template slot-scope="scope">
            <el-tooltip>
              <div slot="content" v-html="scope.row.ruleDetail"></div>
              <div>{{ scope.row.rule }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="提升率" align="center" prop="improveRate" width="60px"></el-table-column>
      </el-table>
      <el-table :data="basicData" style="height: 420px;" class="overflow-true" v-if="title=='本月基础数据提升项排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName"></el-table-column>
        <el-table-column label="提升项" align="center" prop="passRate" width="60px"></el-table-column>
      </el-table>
      <!-- 一表通指标质量看板 -->
      <el-table :data="branchIndexNumData" style="height: 420px;" class="overflow-true" v-if="title=='行社无偏离指标数排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName">
          <template slot-scope="scope">
            <div style="display: inline-flex">
              <div>
                {{ scope.row.orgName }}
              </div>
              <div v-if="scope.row.rank.length > 1" :style="{color: scope.row.rank >= 0 ? 'green' : 'red'}" style="margin-left: 10px;">
                {{ scope.row.rank >= 0 ? '&#8593' : '&#8595' }}
                {{ scope.row.rank.substring(1) }}
              </div>
              <div v-else style="margin-left: 24px;">{{ scope.row.rank }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="指标数" align="center" prop="indexNum" width="60px"></el-table-column>
      </el-table>
      <el-table :data="branchIndexNumData" style="height: 420px;" class="overflow-true" v-if="title=='行社偏离度通过指标数排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName">
          <template slot-scope="scope">
            <div style="display: inline-flex">
              <div>
                {{ scope.row.orgName }}
              </div>
              <div v-if="scope.row.rank.length > 1" :style="{color: scope.row.rank >= 0 ? 'green' : 'red'}" style="margin-left: 10px;">
                {{ scope.row.rank >= 0 ? '&#8593' : '&#8595' }}
                {{ scope.row.rank.substring(1) }}
              </div>
              <div v-else style="margin-left: 24px;">{{ scope.row.rank }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="指标数" align="center" prop="indexNum" width="60px"></el-table-column>
      </el-table>
      <el-table :data="indexDeviationData" style="height: 420px;" class="overflow-true" v-if="title=='指标偏离度排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName"></el-table-column>
        <el-table-column label="指标名称" align="center" prop="indexName"></el-table-column>
        <el-table-column label="偏离度" align="center" prop="deviationPercent"></el-table-column>
      </el-table>
      <el-table :data="branchImproveNumData" style="height: 420px;" class="overflow-true" v-if="title=='行社提升指标数排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName"></el-table-column>
        <el-table-column label="提升指标数" align="center" prop="improveIndexNum"></el-table-column>
      </el-table>
      <!-- 1104提升指标 -->
      <el-table :data="basicData" style="height: 420px;" class="overflow-true" v-if="title=='表格通过数排名'">
        <el-table-column label="排序" width="50" align="center">
          <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
        </el-table-column>
        <el-table-column label="行社名称" align="center" prop="orgName">
          <template slot-scope="scope">
            <div style="display: inline-flex">
              <div>
                {{ scope.row.orgName }}
              </div>
              <div v-if="scope.row.rank.length > 1" :style="{color: scope.row.rank >= 0 ? 'green' : 'red'}" style="margin-left: 10px;">
                {{ scope.row.rank >= 0 ? '&#8593' : '&#8595' }}
                {{ scope.row.rank.substring(1) }}
              </div>
              <div v-else style="margin-left: 24px;">{{ scope.row.rank }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="通过数" align="center" prop="passNum" width="60px"></el-table-column>
      </el-table>
      <!-- 行社指标质量看板 -->
       <el-table :data="indexDeviationData" style="height: 420px" class="overflow-true" v-if="title=='指标偏离度排名 '">
          <el-table-column label="排序" width="50" align="center">
            <template slot-scope="scope">
            <!-- 前三个序号带圆圈背景 -->
            <span
              :class="{'circle-bg': scope.$index < 3, 'normal-index': scope.$index >= 3}"
            >
              {{ scope.$index + 1 }} <!-- 序号从 1 开始 -->
            </span>
          </template>
          </el-table-column>
          <el-table-column label="指标名称" align="center" prop="indexName"></el-table-column>
          <el-table-column label="偏离度" align="center" prop="deviationPercent"></el-table-column>
        </el-table>
      <!-- 分页 -->
      <el-pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        background
        layout="total, prev, pager, next, sizes"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="float: right"
      ></el-pagination>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Dialog',
  props: {
    title: {
      type: String,
      default: ''
    },
    visual: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      queryParams: {
        date: '',
        pageNum: 1,
        pageSize: 10
      },
      basicData: [],
      basicDataImproveRate: [],
      branchIndexNumData: [],
      indexDeviationData: [],
      branchImproveNumData: [],
      total: 0,
    }
  },
  watch: {
    visual(val) {
      this.dialogVisible = val
    }
  },
  methods: {
    closeDialog() {
      this.dialogVisible = false
      this.$emit('update:visual', false)
    },
    handleSizeChange() {},
    handleCurrentChange() {},
  }
}
</script>
