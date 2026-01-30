<template>
  <div class="app-container">
    <div class="display-flex">
      <el-card class="card-box" style="width: 100%; height: 250px">
        <el-row>
          <el-col :span="6" class="alicenter justify-direction display-flex">
            <p class="bold-text font-size-22">1104对比指标总数</p>
            <p class="bold-text font-size-50">90</p>
          </el-col>
          <el-col :span="6">
            <div id="pieChart1" style="width: 200px; height: 200px;"></div>
          </el-col>
          <el-col :span="6">
            <div id="pieChart2" style="width: 200px; height: 200px;"></div>
          </el-col>
          <el-col :span="6">
            <div id="pieChart3" style="width: 200px; height: 200px;"></div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 30%">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            指标偏离度排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('指标偏离度排名 ')">更多</el-button>
        </div>
        <el-table :data="indexDeviationData" style="height: 420px" class="overflow-true">
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
      </el-card>
      <el-card class="card-box" style="width: 70%">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            指标偏离度变动趋势
          </span>
        </div>
        <div id="lineChart" style="width: 100%; height: 450px"></div>
      </el-card>
    </div>
    <Dialog :title="title" :visual="visual" @update:visual="handleVisualUpdate" ></Dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import Dialog from '../../component/dialog.vue';
export default {
  components: {
    Dialog
  },
  props: {
    orgSelected: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      time:['2025-06-01','2025-06-02','2025-06-03','2025-06-04','2025-06-05','2025-06-06','2025-06-07'],
      indexDeviationData: [
        {
          indexName: '指标1',
          deviationPercent: '60.87%'
        },
        {
          indexName: '指标2',
          deviationPercent: '45.67%'
        },
        {
          indexName: '指标3',
          deviationPercent: '50.87%'
        },
        {
          indexName: '指标4',
          deviationPercent: '60.87%'
        },
        {
          indexName: '指标5',
          deviationPercent: '45.67%'
        },
        {
          indexName: '指标6',
          deviationPercent: '50.87%'
        },
        {
          indexName: '指标7',
          deviationPercent: '60.87%'
        },
        {
          indexName: '指标8',
          deviationPercent: '45.67%'
        },
        {
          indexName: '指标9',
          deviationPercent: '50.87%'
        }
      ],
      //  图表初始化
      pieChart1: null,
      pieChart2: null,
      pieChart3: null,
      lineChart: null,
      // 弹窗名称
      title: '',
      visual: false
    }
  },
  watch: {
    // 监听机构变化
    orgSelected(newVal) {
      // 机构变化处理
    }
  },
  mounted() {
    this.initPieChart1()
    this.initPieChart2()
    this.initPieChart3()
    this.initLineChart()
    // 自适应浏览器大小
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前销毁图表实例
    if (this.pieChart1) {
      this.pieChart1.dispose();
      this.pieChart1 = null;
    }
    if (this.pieChart2) {
      this.pieChart2.dispose();
      this.pieChart2 = null;
    }
    if (this.pieChart3) {
      this.pieChart3.dispose();
      this.pieChart3 = null;
    }
    if (this.lineChart) {
      this.lineChart.dispose();
      this.lineChart = null;
    }
    // 移除监听
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initPieChart1() {
      this.pieChart1 = echarts.init(document.getElementById('pieChart1'));
      const option = {
        tooltip: {
          trigger: 'item'
        },
        // 饼图中间文字
        title: {
          text: '54',
          subtext: '无偏离',
          left: '48%',
          top: '40%',
          textAlign: 'center',
          textStyle:{
            fontSize: 24,
            color:'#454c5c',
            align:'center'
          },
          subtextStyle:{
            fontFamily : "微软雅黑",
            fontSize: 14,
            color:'#6c7a89',
          }
        },
        legend: {
          show: false
        },
        series: [
          {
            name: 'Access From',
            type: 'pie',
            radius: ['55%', '70%'],
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
            data: [{name:'无偏离', value: '54'}, {name:'有偏离', value: '104'}]
          }
        ]
      }
      this.pieChart1.setOption(option);
    },
    initPieChart2() {
      this.pieChart2 = echarts.init(document.getElementById('pieChart2'));
      const option = {
        tooltip: {
          trigger: 'item'
        },
        // 饼图中间文字
        title: {
          text: '36',
          subtext: '偏离度低于2%',
          left: '48%',
          top: '40%',
          textAlign: 'center',
          textStyle:{
            fontSize: 24,
            color:'#454c5c',
            align:'center'
          },
          subtextStyle:{
            fontFamily : "微软雅黑",
            fontSize: 14,
            color:'#6c7a89',
          }
        },
        legend: {
          show: false
        },
        series: [
          {
            name: 'Access From',
            type: 'pie',
            radius: ['55%', '70%'],
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
            data: [{name:'偏离度低于2%', value: '36'}, {name:'有偏离', value: '30'}]
          }
        ]
      }
      this.pieChart2.setOption(option);
    },
    initPieChart3() {
      this.pieChart3 = echarts.init(document.getElementById('pieChart3'));
      const option = {
        tooltip: {
          trigger: 'item'
        },
        // 饼图中间文字
        title: {
          text: '34',
          subtext: '偏离度高于2%',
          left: '48%',
          top: '40%',
          textAlign: 'center',
          textStyle:{
            fontSize: 24,
            color:'#454c5c',
            align:'center'
          },
          subtextStyle:{
            fontFamily : "微软雅黑",
            fontSize: 14,
            color:'#6c7a89',
          }
        },
        legend: {
          show: false
        },
        series: [
          {
            name: 'Access From',
            type: 'pie',
            radius: ['55%', '70%'],
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
            data: [{name:'偏离度高于2%', value: '34'}, {name:'有偏离', value: '45'}]
          }
        ]
      }
      this.pieChart3.setOption(option);
    },
    initLineChart() {
      this.lineChart = echarts.init(document.getElementById('lineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['指标偏离度=0', '0＜指标偏离度≤2%', '指标偏离度＞2%'],
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
          name: '指标数'
        },
        series: [
          {
            name: '指标偏离度=0',
            type: 'line',
            stack: 'Total',
            data: [120, 132, 101, 134, 90, 230, 210],
            smooth: true
          },
          {
            name: '0＜指标偏离度≤2%',
            type: 'line',
            stack: 'Total',
            data: [220, 182, 191, 234, 290, 330, 310],
            smooth: true
          },
          {
            name: '指标偏离度＞2%',
            type: 'line',
            stack: 'Total',
            data: [150, 232, 201, 154, 190, 330, 410],
            smooth: true
          }
        ]
      }
      this.lineChart.setOption(option);
    },
    handleResize() {
      this.pieChart1.resize();
      this.pieChart2.resize();
      this.pieChart3.resize();
      this.lineChart.resize()
    },
    handleDialog(title) {
      this.title = title
      this.visual = true
    },
    handleVisualUpdate(newValue) {
      this.visual = newValue
    }
  }
}
</script>
