<template>
  <div class="app-container">
    <el-card class="card-box" style="width: 100%">
      <div slot="header">
        <span class="head-title display-flex alicenter">
          <div class="el-icon-pie-chart icon-title"></div>
          <div class="display-flex">
            一键转表格整体通过情况
            <el-tooltip content="通过表格指单表格数据项通过率超过80%的表格" placement="top">
              <div>﹖</div>
            </el-tooltip>
          </div>
        </span>
      </div>
      <el-row>
        <el-col :span="5" v-html="'\u00a0'"></el-col>
        <el-col :span="14" class="alicenter justify-direction display-flex">
          <div id="changePieChart" style="width: 100%; height: 350px"></div>
        </el-col>
        <el-col :span="5" v-html="'\u00a0'"></el-col>
      </el-row>
    </el-card>
    <el-card class="card-box" style="width: 100%">
      <div slot="header">
        <span class="head-title display-flex alicenter">
          <div class="el-icon-s-data icon-title"></div>
          一键转1104表格质量情况
        </span>
      </div>
      <div id="changeBarChart" style="width: 100%; height: 450px"></div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
export default {
  props: {
    orgSelected: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      data: [
        { value: 1, name: '通过表格' },
        { value: 1, name: '未通过表格' }
      ],
      //  图表初始化
      changePieChart: null,
      changeBarChart: null
    }
  },
  mounted() {
    this.initChangePieChart()
    this.initChangeBarChart()
    // 自适应浏览器大小
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前销毁图表实例
    if (this.changePieChart) {
      this.changePieChart.dispose();
      this.changePieChart = null;
    }
    if (this.changeBarChart) {
      this.changeBarChart.dispose();
      this.changeBarChart = null;
    }
    // 移除监听
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initChangePieChart() {
      this.changePieChart = echarts.init(document.getElementById('changePieChart'))
      const data = this.data
      const option = {
        tooltip: {
          trigger: 'item'
        },
        // 饼图中间文字
        title: {
          text: '50%',
          left: '24%',
          top: '45%',
          textStyle:{
            fontSize: 38,
            color:'#454c5c',
            align:'center'
          }
        },
        // 标签置于右侧
        legend: {
          orient: 'vertical',
          top: 'center',
          right: '5%',
          formatter: function (name) {
            console.log(data, data.indexOf(name))
            let singleData = data.filter(function(item){
                return item.name == name
            })
            return `${name}     ${singleData[0].value}`
          }
        },
        series: [
          {
            type: 'pie',
            radius: ['50%', '70%'],
            center: ['30%', '50%'], // 改变饼图位置
            avoidLabelOverlap: false,
            label: {
              show: true,
              position: 'outside'
            },
            emphasis: {
              label: {
                show: true
              }
            },
            labelLine: {
              show: true
            },
            data: data
          }
        ]
      };
      this.changePieChart.setOption(option)
    },
    initChangeBarChart() {
      this.changeBarChart = echarts.init(document.getElementById('changeBarChart'))
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999'
            }
          }
        },
        legend: {
          data: ['无偏离指标', '偏离度低于2%指标', '偏离度高于2%指标']
        },
        xAxis: {
          type: 'value',
          name: '数据项（个）',
          axisLine: {
            show: true
          }
        },
        yAxis: {
          type: 'category',
          data: ['G107', 'G102'],
          axisLine: {
            show: true
          }
        },
        series: [
          {
            name: '无偏离指标',
            type: 'bar',
            stack: '总量',
            data: [120, 132],
            barWidth: '30%'
          },
          {
            name: '偏离度低于2%指标',
            type: 'bar',
            stack: '总量',
            data: [82, 93],
            barWidth: '30%'
          },
          {
            name: '偏离度高于2%指标',
            type: 'bar',
            stack: '总量',
            data: [22, 33],
            barWidth: '30%'
          }
        ]
      }
      this.changeBarChart.setOption(option)
    },
    handleResize() {
      this.changePieChart.resize()
      this.changeBarChart.resize()
    }
  }
}
</script>
