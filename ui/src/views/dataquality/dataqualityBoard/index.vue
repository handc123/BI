<template>
  <div class="app-container">
    <el-row>
      <el-col :span="12">
        <el-tabs v-model="activeName">
          <el-tab-pane label="一表通字段质量看板" name="fieldQuality">
          </el-tab-pane>
          <el-tab-pane label="一表通指标质量看板" name="indexQuality">
          </el-tab-pane>
          <el-tab-pane label="一键转1104质量看板" name="1104Quality">
          </el-tab-pane>
          <el-tab-pane label="一键转EAST质量看板" name="EASTQuality" />
        </el-tabs>
      </el-col>
      <el-col :span="4" style="min-height: 20px;">
      </el-col>
      <el-col :span="8">
        <div class="display-flex alicenter" style="min-width: 421px;">
          <div style="font-size: 14px; width: 80px;">机构名称</div>
          <el-select v-model="orgSelected" placeholder="请选择" style="width: 200px">
            <el-option
              v-for="(item, index) in orgOptions"
              :key="index"
              :label="item"
              :value="item">
            </el-option>
          </el-select>
          <div style="font-size: 16px; font-weight: bold; width: 170px; padding: 10px;">数据日期：{{ formattedDate }}</div>
        </div>
      </el-col>
    </el-row>


    <div v-if="activeName === 'fieldQuality'">
      <fieldQuality v-if="orgSelected === '杭州农商联合银行'" />
      <branchFieldQuality v-else :orgSelected="orgSelected"></branchFieldQuality>
    </div>
    <div v-if="activeName === 'indexQuality'">
      <indexQuality v-if="orgSelected === '杭州农商联合银行'" />
      <branchIndexQuality v-else :orgSelected="orgSelected"></branchIndexQuality>
    </div>
    <div v-if="activeName === '1104Quality'">
      <oozfQuality v-if="orgSelected === '杭州农商联合银行'"></oozfQuality>
      <branch1104Quality v-else :orgSelected="orgSelected"></branch1104Quality>
    </div>
    <div v-if="activeName === 'EASTQuality'">
    </div>
  </div>
</template>

<script>
import fieldQuality from "./fieldQuality/index.vue"
import indexQuality from "./indexQuality/index.vue"
import branchFieldQuality from "./branchFieldQuality/index.vue"
import branchIndexQuality from "./branchIndexQuality/index.vue"
import oozfQuality from "./1104Quality/index.vue"
import branch1104Quality from "./branch1104Quality/index.vue"
import { computed } from "vue"
export default {
  components: {
    fieldQuality,
    indexQuality,
    branchFieldQuality,
    branchIndexQuality,
    oozfQuality,
    branch1104Quality
  },
  data() {
    return {
      activeName: "fieldQuality",
      orgSelected: "杭州农商联合银行",
      orgOptions: [
        '杭州农商联合银行',
        '萧山农商联合银行',
        '余杭农商联合银行'
      ],
      currentDate: new Date()
    }
  },
  computed: {
    formattedDate() {
      const month = this.currentDate.getMonth() + 1;
      const day = this.currentDate.getDate();
      return `${month}月${day}日`;
    }
  },
  methods: {
  }
}
</script>
