<template>
  <div class="app-container">
    <div class="display-flex">
      <el-card class="card-box" style="width: 100%; height: 700px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-tickets icon-title"></div>
            数据质量概览
          </span>
        </div>
        <el-row style="padding-bottom: 10px; min-width: 900px" :gutter="10">
          <el-col :span="8">
            <div class="display-flex alicenter" style="background: linear-gradient(to left, rgba(75, 122, 224, 1), rgba(75, 122, 224, 0.4)); border-radius: 10px;">
              <div class="transpaarent-circle">
                <i class="el-icon-s-operation font-size-60" style="color: rgb(75, 122, 224);"></i>
              </div>
              <div class="margin-left-50">
                <p class="font-size-18 color-white">规则校验项</p>
                <p class="font-size-30 color-white bold-text">4499</p>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="display-flex alicenter" style="background: linear-gradient(to left, rgba(54, 162, 100, 1), rgba(54, 162, 100, 0.4)); border-radius: 10px">
              <div class="transpaarent-circle">
                <i class="el-icon-check font-size-60" style="color: rgb(54, 162, 100);"></i>
              </div>
              <div class="margin-left-50">
                <p class="font-size-18 color-white">通过项</p>
                <p class="font-size-30 color-white bold-text">3999</p>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="display-flex alicenter" style="background: linear-gradient(to left, rgba(243, 155, 51, 1), rgba(243, 155, 51, 0.4)); border-radius: 10px">
              <div class="transpaarent-circle">
                <i class="el-icon-close font-size-60" style="color: rgb(243, 155, 51);"></i>
              </div>
              <div class="margin-left-50">
                <p class="font-size-18 color-white">未通过项</p>
                <p class="font-size-30 color-white bold-text">1498</p>
              </div>
            </div>
          </el-col>
        </el-row>
        <div id="ruleVerificationLineChart" style="width: 100%; height: 450px"></div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 100%;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            数据质量提升排名
          </span>
        </div>
        <div id="qualityImproveChart" style="width: 100%; height: 450px;"></div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 100%;height: 1430px;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-files icon-title"></div>
            基础数据质量问题
          </span>
        </div>
        <div class="display-flex">
          <el-card class="card-boxs" style="width: 50%;">
            <div slot="header" class="display-flex justify-between alicenter head-backgound">
              <span class="boxs-title">完整性问题</span>
              <div class="boxs-number">45</div>
            </div>
            <el-table :data="basicDataImproveRate" style="height: 470px" class="overflow-true">
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
              <el-table-column label="规则编码" align="center" prop="rule">
                <template slot-scope="scope">
                  <el-tooltip>
                    <div slot="content" v-html="scope.row.ruleDetail"></div>
                    <div>{{ scope.row.rule }}</div>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="通过率" align="center" prop="passRate" width="60px"></el-table-column>
            </el-table>
          </el-card>
          <el-card class="card-boxs" style="width: 50%;">
            <div slot="header" class="display-flex justify-between alicenter head-backgound">
              <span class="boxs-title">规范性问题</span>
              <div class="boxs-number">3999</div>
            </div>
            <el-table :data="basicDataImproveRate" style="height: 470px" class="overflow-true">
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
              <el-table-column label="规则编码" align="center" prop="rule">
                <template slot-scope="scope">
                  <el-tooltip>
                    <div slot="content" v-html="scope.row.ruleDetail"></div>
                    <div>{{ scope.row.rule }}</div>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="通过率" align="center" prop="passRate" width="60px"></el-table-column>
            </el-table>
          </el-card>
        </div>
        <div class="display-flex">
          <el-card class="card-boxs" style="width: 33%;">
            <div slot="header" class="display-flex justify-between alicenter head-backgound">
              <span class="boxs-title">准确性问题</span>
              <div class="boxs-number">8</div>
            </div>
            <el-table :data="basicDataImproveRate" style="height: 470px" class="overflow-true">
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
              <el-table-column label="规则编码" align="center" prop="rule">
                <template slot-scope="scope">
                  <el-tooltip>
                    <div slot="content" v-html="scope.row.ruleDetail"></div>
                    <div>{{ scope.row.rule }}</div>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="通过率" align="center" prop="passRate" width="60px"></el-table-column>
            </el-table>
          </el-card>
          <el-card class="card-boxs" style="width: 33%;">
            <div slot="header" class="display-flex justify-between alicenter head-backgound">
              <span class="boxs-title">时效性问题</span>
              <div class="boxs-number">42</div>
            </div>
            <el-table :data="basicDataImproveRate" style="height: 470px" class="overflow-true">
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
              <el-table-column label="规则编码" align="center" prop="rule">
                <template slot-scope="scope">
                  <el-tooltip>
                    <div slot="content" v-html="scope.row.ruleDetail"></div>
                    <div>{{ scope.row.rule }}</div>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="通过率" align="center" prop="passRate" width="60px"></el-table-column>
            </el-table>
          </el-card>
          <el-card class="card-boxs" style="width: 33%;">
            <div slot="header" class="display-flex justify-between alicenter head-backgound">
              <span class="boxs-title">一致性问题</span>
              <div class="boxs-number">1498</div>
            </div>
            <el-table :data="basicDataImproveRate" style="height: 470px" class="overflow-true">
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
              <el-table-column label="规则编码" align="center" prop="rule">
                <template slot-scope="scope">
                  <el-tooltip>
                    <div slot="content" v-html="scope.row.ruleDetail"></div>
                    <div>{{ scope.row.rule }}</div>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="通过率" align="center" prop="passRate" width="60px"></el-table-column>
            </el-table>
          </el-card>
        </div>

      </el-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default{
  props: {
    orgSelected: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      time:['2025-06-01','2025-06-02','2025-06-03','2025-06-04','2025-06-05','2025-06-06','2025-06-07'],
      basicDataImproveRate: [
        {
          passRate: '87%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>校验表名：贷款明细维度表'
        },
        {
          passRate: '56%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0'
        },
        {
          passRate: '42%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0<br>校验强度：强制性校验'
        },
        {
          passRate: '24%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>校验表名：贷款明细维度表'
        },
        {
          passRate: '52%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0'
        },
        {
          passRate: '76%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0<br>校验强度：强制性校验'
        },
        {
          passRate: '80%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>校验表名：贷款明细维度表'
        },
        {
          passRate: '17%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0'
        },
        {
          passRate: '21%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0<br>校验强度：强制性校验'
        }
      ],
      //  图表初始化
      ruleVerificationLineChart: null,
      qualityImproveChart: null
    }
  },
  watch: {
    // 监听机构变化
    orgSelected(newVal) {
      console.log(newVal)
    }
  },
  mounted() {
    this.initRuleVerificationLineChart()
    this.initQualityImproveChart()
    // 自适应浏览器大小
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前销毁图表实例
    if (this.ruleVerificationLineChart) {
      this.ruleVerificationLineChart.dispose();
      this.ruleVerificationLineChart = null;
    }
    if (this.qualityImproveChart) {
      this.qualityImproveChart.dispose();
      this.qualityImproveChart = null;
    }
    // 移除监听
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initRuleVerificationLineChart() {
      this.ruleVerificationLineChart = echarts.init(document.getElementById('ruleVerificationLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['通过规则项', '未通过规则项'],
          bottom: '5%',
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.time
        },
        yAxis: {
          type: 'value',
          name: '规则校验项'
        },
        series: [
          {
            name: '通过规则项',
            type: 'line',
            stack: 'Total',
            data: [120, 132, 101, 134, 90, 230, 210],
            smooth: true
          },
          {
            name: '未通过规则项',
            type: 'line',
            stack: 'Total',
            data: [220, 182, 191, 234, 290, 330, 310],
            smooth: true
          }
        ]
      }
      this.ruleVerificationLineChart.setOption(option);
    },
    initQualityImproveChart() {
      this.qualityImproveChart = echarts.init(document.getElementById('qualityImproveChart'));
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let result = `<div>规则编号：JY_DKMX01_155</div>`;
            result += `<div> 校验表名：贷款明细维度表</div>`;
            result += `<div> 规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0</div>`;
            result += `<div> 校验强度：强制性校验</div>`;
            result += `<div> 校验大类：</div>`;
            result += `<div> 校验小类：</div>`;
            result += `<div> 近6个月通过率：-/-/45.13%/45.13%45.13%/45.13%</div>`;
            return result
          }
        },
        xAxis: {
          type: 'category',
          data: ['JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155', 'JY_DKMX01_155']
        },
        yAxis: {
          type: 'value',
          name: '提升率(%)'
        },
        series: [
          {
            data: [12.56, 12.05, 11.4, 10.75, 9.28, 9.02, 8.23, 7.05, 6.05, 5.05, 4.05],
            type: 'bar',
            barWidth: '20%',
            label: {
              show: true,          // 显示标签
              position: 'top',     // 位置在顶部
              formatter: '{c}',    // 标签内容为数据值
              color: '#333',       // 标签颜色
              fontSize: 12         // 字体大小
            }
          }
        ]
      };
      this.qualityImproveChart.setOption(option);
    },
    handleResize() {
      this.ruleVerificationLineChart.resize()
      this.qualityImproveChart.resize()
    }
  },
}
</script>
