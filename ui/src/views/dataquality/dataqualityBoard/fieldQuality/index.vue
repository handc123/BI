<template>
  <div class="app-container">
    <div class="display-flex">
      <el-card class="card-box">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-pie-chart icon-title"></div>
            规则校验项整体通过情况
          </span>
        </div>
        <div id="rulePieChart" style="width: 100%; height: 450px"></div>
      </el-card>
      <el-card class="card-box">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            规则校验通过变动趋势
          </span>
        </div>
        <div id="ruleLineChart" style="width: 100%; height: 450px"></div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 23%; height: 600px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            地区通过率排名
          </span>
        </div>
        <div>
          <el-table :data="areaList" style="height: 100%;" class="overflow-true">
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
            <el-table-column label="地区" align="center" prop="areaName">
              <template slot-scope="scope">
                <div style="display: inline-flex">
                  <div>
                    {{ scope.row.areaName }}
                  </div>
                  <div v-if="scope.row.rank.length > 1" :style="{color: scope.row.rank >= 0 ? 'green' : 'red'}" style="margin-left: 10px;">
                    {{ scope.row.rank >= 0 ? '&#8593' : '&#8595' }}
                    {{ scope.row.rank.substring(1) }}
                  </div>
                  <div v-else style="margin-left: 24px;">{{ scope.row.rank }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="通过率" align="center" prop="passRate"></el-table-column>
          </el-table>
        </div>
      </el-card>
      <el-card class="card-box" style="width: 77%; height: 600px">
        <div slot="header">
          <div class="display-flex alicenter justify-between">
            <span class="head-title display-flex alicenter">
              <div class="el-icon-document icon-title"></div>
              未通过项记录
            </span>
            <div class="display-flex alicenter">
              <p style="min-width: 40px;">地区</p>
              <el-select v-model="area" placeholder="请选择地区" style="width: 150px; margin-left: 10px">
                <el-option
                  v-for="(item,index) in areaOptions"
                  :key="index"
                  :label="item"
                  :value="item">
                </el-option>
              </el-select>
            </div>
          </div>
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="display-flex alicenter justify-between backgoundcolor-ccc">
              <p class="font-size-16">未通过校验项</p>
              <p class="bold-text font-size-24">{{ failedNumber[0] }}</p>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="display-flex alicenter justify-between backgoundcolor-ccc">
              <p class="font-size-16">未通过记录数</p>
              <p class="bold-text font-size-24">{{ failedNumber[1] }}</p>
            </div>
          </el-col>
        </el-row>
        <div class="margin-top-10">
          <div id="failedVerificationChart" class="card-boxs" style="width: 100%; height: 210px;"></div>
          <div id="failedRecordChart" class="card-boxs" style="width: 100%; height: 210px;"></div>
        </div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            基础数据通过率排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('基础数据通过率排名')">更多</el-button>
        </div>
        <div>
          <el-table :data="basicData" style="height: 420px;" class="overflow-true">
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
        </div>
      </el-card>
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            基础数据通过项排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('基础数据通过项排名')">更多</el-button>
        </div>
        <div>
          <el-table :data="basicData" style="height: 420px;" class="overflow-true">
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
        </div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            本月基础数据提升率排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('本月基础数据提升率排名')">更多</el-button>
        </div>
        <div>
          <el-table :data="basicDataImproveRate" style="height: 420px;" class="overflow-true">
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
        </div>
      </el-card>
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            本月基础数据提升项排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('本月基础数据提升项排名')">更多</el-button>
        </div>
        <div>
          <el-table :data="basicData" style="height: 420px;" class="overflow-true">
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
        </div>
      </el-card>
    </div>
    <Dialog :title="title" :visual="visual" @update:visual="handleVisualUpdate" ></Dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import Dialog from '../../component/dialog.vue';
export default{
  components: {
    Dialog
  },
  data() {
    return {
      data:[
        { value: 3455, name: '行社通过率0-20%（不含）' },
        { value: 567, name: '行社通过率20-40%（不含）' },
        { value: 456, name: '行社通过率40-60%（不含）' },
        { value: 567, name: '行社通过率60-80%（不含）' },
        { value: 56, name: '行社通过率80-100%（不含）' },
        { value: 1456, name: '行社通过率100%' }
      ],
      time:['2025-06-01','2025-06-02','2025-06-03','2025-06-04','2025-06-05','2025-06-06','2025-06-07'],
      areaList: [
        {
            areaName: '杭州',
            passRate: '98.87%',
            rank: '+1'
        },
        {
            areaName: '台州',
            passRate: '98.54%',
            rank: '-1'
        },
        {
            areaName: '嘉兴',
            passRate: '94.45%',
            rank: '-'
        },
        {
            areaName: '金华',
            passRate: '94.45%',
            rank: '+1'
        },
        {
            areaName: '湖州',
            passRate: '94.45%',
            rank: '+1'
        },
        {
            areaName: '丽水',
            passRate: '94.45%',
            rank: '+1'
        },
        {
            areaName: '温州',
            passRate: '94.45%',
            rank: '+1'
        },
        {
            areaName: '舟山',
            passRate: '94.45%',
            rank: '+1'
        },
        {
            areaName: '衢州',
            passRate: '94.45%',
            rank: '+1'
        },
        {
            areaName: '绍兴',
            passRate: '94.45%',
            rank: '+1'
        },
        {
            areaName: '宁波',
            passRate: '94.45%',
            rank: '+1'
        }
      ],
      basicData: [
        {
          orgName: '杭州联合银行',
          passRate: '87%',
          rank: '+1'
        },
        {
          orgName: '萧山农商银行',
          passRate: '56%',
          rank: '-1'
        },
        {
          orgName: '余杭农商银行',
          passRate: '92%',
          rank: '0'
        },
        {
          orgName: '富阳农商银行',
          passRate: '78%',
          rank: '+2'
        },
        {
          orgName: '临安农商银行',
          passRate: '65%',
          rank: '-2'
        },
        {
          orgName: '桐庐农商银行',
          passRate: '89%',
          rank: '+3'
        },
        {
          orgName: '建德农商银行',
          passRate: '72%',
          rank: '-1'
        },
        {
          orgName: '淳安农商银行',
          passRate: '94%',
          rank: '+1'
        },
        {
          orgName: '杭州银行',
          passRate: '81%',
          rank: '-3'
        },
        {
          orgName: '杭州科技银行',
          passRate: '76%',
          rank: '+1'
        }
      ],
      basicDataImproveRate: [
        {
          orgName: '杭州联合银行',
          improveRate: '87%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>校验表名：贷款明细维度表'
        },
        {
          orgName: '萧山农商银行',
          improveRate: '56%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0'
        },
        {
          orgName: '余杭农商银行',
          improveRate: '42%',
          rule: 'JY_DKMX01_155',
          ruleDetail: '规则编号：JY_DKMX01_155<br>规则说明：【贷款明细维度表】.【减值准备】非空时应大于等于0<br>校验强度：强制性校验'
        }
      ],
      failedNumber: [2343, 345673],
      // 地区下拉框内容初始化
      area:'',
      areaOptions: ['杭州', '嘉兴', '台州'],
      //  图表初始化
      rulePieChart: null,
      ruleLineChart: null,
      failedVerificationChart: null,
      failedRecordChart: null,
      // 弹窗名称
      title: '',
      visual: false
    }
  },
  watch: {
  },
  mounted() {
    this.initRulePieChart()
    this.initRuleLineChart()
    this.initFailedVerificationChart()
    this.initFailedRecordChart()
    // 自适应浏览器大小
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前销毁图表实例
    if (this.rulePieChart) {
      this.rulePieChart.dispose();
      this.rulePieChart = null;
    }
    if (this.ruleLineChart) {
      this.ruleLineChart.dispose();
      this.ruleLineChart = null;
    }
    if (this.failedVerificationChart) {
      this.failedVerificationChart.dispose();
      this.failedVerificationChart = null;
    }
    if (this.failedRecordChart) {
      this.failedRecordChart.dispose();
      this.failedRecordChart = null;
    }
    // 移除监听（关键！避免内存泄漏）
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initRulePieChart() {
      const chartDom = document.getElementById('rulePieChart');
      this.rulePieChart = echarts.init(chartDom);
      const data = this.data
      const option = {
        tooltip: {
          trigger: 'item'
        },
        // 饼图中间文字
        title: {
          text: '5566',
          subtext: '规则校验总数',
          left: '21%',
          top: '40%',
          textStyle:{
            fontSize: 38,
            color:'#454c5c',
            align:'center'
          },
          subtextStyle:{
            fontFamily : "微软雅黑",
            fontSize: 16,
            color:'#6c7a89',
          }
        },
        // 标签置于右侧
        legend: {
          orient: 'vertical',
          top: 'center',
          right: '2%',
          formatter: function (name) {
            const maxWidth = 18;
            // 计算需要填充的空格数
            let padding = '  '.repeat(maxWidth - name.length);
            let singleData = data.filter(function(item){
                return item.name == name
            })
            return `${name}${padding}${singleData[0].value}`
          }
        },
        series: [
          {
            name: '规则校验项通过整体情况',
            type: 'pie',
            radius: ['40%', '50%'],
            center: ['30%', '50%'], // 改变饼图位置
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: false
              }
            },
            labelLine: {
              show: false
            },
            data: data
          }
        ]
      }
      this.rulePieChart.setOption(option);
      // 创建观察者
      const observer = new ResizeObserver(entries => {
        for (const entry of entries) {
          const { width } = entry.contentRect;

          this.rulePieChart.setOption({
            title: {
              left: width < 500 ? '41%': '21%',
              textStyle: {
                fontSize: Math.max(12, width * 0.07),
                align:'center'
              },
              subtextStyle: {
                fontSize: Math.max(10, width * 0.03)
              }
            },
            legend: {
              orient: width < 500 ? 'horizontal': 'vertical',
              left: width < 500 ? 'center': '60%',
              bottom: width < 500 ? '3%': 'center',
              type: width < 500 ? 'scroll' : '',
              formatter: width < 550 ? '': function (name) {
                const maxWidth = 17;
                // 计算需要填充的空格数
                let padding = '  '.repeat(maxWidth - name.length);
                let singleData = data.filter(function(item){
                    return item.name == name
                })
                return `${name}${padding}${singleData[0].value}`
              }
            },
            series: {
              center: width < 500 ? ['50%', '50%']: ['30%', '50%']
            }
          });

          this.rulePieChart.resize();
        }
      });

      // 开始观察容器
      observer.observe(chartDom);
    },
    initRuleLineChart() {
      this.ruleLineChart = echarts.init(document.getElementById('ruleLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: this.data.map(item => item.name),
          bottom: '5%',
          type: 'scroll'
        },
        grid: {
          left: '3%',
          right: '8%',
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
          type: 'value'
        },
        series: [
          {
            name: '行社通过率0-20%（不含）',
            type: 'line',
            stack: 'Total',
            data: [520, 480, 610, 590, 455, 380, 420],
            smooth: true
          },
          {
            name: '行社通过率20-40%（不含）',
            type: 'line',
            stack: 'Total',
            data:  [80, 75, 92, 88, 65, 97, 70],
            smooth: true
          },
          {
            name: '行社通过率40-60%（不含）',
            type: 'line',
            stack: 'Total',
            data:  [60, 55, 72, 68, 50, 75, 76],
            smooth: true
          },
          {
            name: '行社通过率60-80%（不含）',
            type: 'line',
            stack: 'Total',
            data: [90, 82, 78, 85, 62, 95, 75],
            smooth: true
          },
          {
            name: '行社通过率80-100%（不含）',
            type: 'line',
            stack: 'Total',
            data: [8, 7, 9, 6, 10, 5, 11],
            smooth: true
          },
          {
            name: '行社通过率100%',
            type: 'line',
            stack: 'Total',
            data: [210, 195, 205, 180, 220, 230, 216],
            smooth: true
          }
        ]
      }
      this.ruleLineChart.setOption(option);
    },
    initFailedVerificationChart() {
      this.failedVerificationChart = echarts.init(document.getElementById('failedVerificationChart'));
      const option = {
        title: {
          text: '未通过校验项'
        },
        xAxis: {
          type: 'category',
          data: ['杭州联合银行', '萧山农商银行', '余杭农商银行', '舟山农商银行', '温州农商银行', '宁波农商银行', '嘉兴农商银行']
        },
        yAxis: {
          type: 'value',
          name: '校验规则项'
        },
        series: [
          {
            data: [120, 200, 150, 80, 70, 110, 130],
            type: 'bar',
            barWidth: '20%'
          }
        ]
      };
      this.failedVerificationChart.setOption(option);
    },
    initFailedRecordChart() {
      this.failedRecordChart = echarts.init(document.getElementById('failedRecordChart'));
      const option = {
        title: {
          text: '未通过记录数'
        },
        xAxis: {
          type: 'category',
          data: ['杭州联合银行', '萧山农商银行', '余杭农商银行', '舟山农商银行', '温州农商银行', '宁波农商银行', '嘉兴农商银行']
        },
        yAxis: {
          type: 'value',
          name: '记录数'
        },
        series: [
          {
            data: [120, 200, 150, 80, 70, 110, 130],
            type: 'bar',
            barWidth: '20%'
          }
        ]
      };
      this.failedRecordChart.setOption(option);
    },
    handleResize() {
      this.rulePieChart.resize()
      this.ruleLineChart.resize()
      this.failedVerificationChart.resize()
      this.failedRecordChart.resize()
    },
    handleDialog(title) {
      this.title = title
      this.visual = true
    },
    handleVisualUpdate(newValue) {
      this.visual = newValue
    }
  },
}
</script>
