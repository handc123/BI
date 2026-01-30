<template>
  <div class="app-container overflow-x-true">
    <el-form :inline="true" label-width="70px">
      <el-form-item label="机构">
        <el-select v-model="orgSelected" placeholder="请选择机构" class="width-300">
          <el-option
            v-for="(item,index) in orgList"
            :key="index"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="地区">
        <el-select v-model="areaSelected" placeholder="请选择地区" class="width-300">
          <el-option
            v-for="(item,index) in areaList"
            :key="index"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="数据日期">
        <el-date-picker
          v-model="date"
          class="width-300"
          type="date"
          placeholder="选择日期"
          value-format="yyyy-MM-dd"
          :picker-options="pickerOptions">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="dateRange"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
        </el-date-picker>
      </el-form-item>
    </el-form>
    <div style="height: 40px">
      <el-button type="primary" style="float:right">查询</el-button>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%; height: 350px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            房地产贷款余额及占比
          </span>
        </div>
        <div id="estateLoanChart" style="width: 100%; height: 280px"></div>
      </el-card>
      <el-card class="card-box" style="width: 50%; height: 350px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            以房地产为押品的贷款余额
          </span>
        </div>
        <div id="mortgageEstateChart" style="width: 100%; height: 280px"></div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%; height: 350px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            房地产当天发放贷款金额及平均利率
          </span>
        </div>
        <el-tabs v-model="estateName">
          <el-tab-pane label="当天发放金额" name="1">
            <div id="todayLoanChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="当天发放贷款平均利率" name="2">
            <div id="todayLoanPercentChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      <el-card class="card-box" style="width: 50%; height: 350px">
         <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            个人住房按揭贷款余额及比例
          </span>
        </div>
        <div id="personalChart" style="width: 100%; height: 280px"></div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%; height: 350px">
         <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            房地产不良贷款余额及比例
          </span>
        </div>
        <div id="badLoanAmountChart" style="width: 100%; height: 280px"></div>
      </el-card>
      <el-card class="card-box" style="width: 50%; height: 350px">
         <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-pie-chart icon-title"></div>
            房地产贷款余额
          </span>
        </div>
        <el-tabs v-model="loanTypeName">
          <el-tab-pane label="分类型" name="1">
          </el-tab-pane>
          <el-tab-pane label="分机构" name="2">
          </el-tab-pane>
          <el-tab-pane label="区域" name="3">
          </el-tab-pane>
          <el-tab-pane label="分机构类型" name="4">
          </el-tab-pane>
        </el-tabs>
        <div id="typeLoanChart" style="width: 100%; height: 230px"></div>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  data() {
    return {
      date: '',
      time:['2025-06-01','2025-06-02','2025-06-03','2025-06-04','2025-06-05','2025-06-06','2025-06-07'],
      typeLoanData:[
        { value: 1264, name: '个人住房贷款' },
        { value: 824, name: '商业用房开发贷款' },
        { value: 580, name: '其他房地产贷款' },
        { value: 484, name: '房地产开发贷款' }
      ],
      // 初始化下拉框内容
      orgSelected: '杭州农商联合银行',
      orgList: [
        '萧山农商联合银行',
        '余杭农商联合银行',
        '杭州农商联合银行'
      ],
      areaSelected: '杭州',
      areaList: [
        '杭州',
        '宁波',
        '温州',
        '嘉兴',
        '湖州',
        '绍兴',
        '金华',
        '衢州',
        '舟山',
        '台州',
        '丽水'
      ],
      dateRange: [],
      // 限制日期选择器
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      // 初始化tab页
      estateName: '1',
      loanTypeName: '1',
      // 初始化图表
      estateLoanChart: null,
      mortgageEstateChart: null,
      todayLoanChart: null,
      todayLoanPercentChart: null,
      personalChart: null,
      badLoanAmountChart: null,
      typeLoanChart: null
    }
  },
  mounted() {
    this.initEstateLoanChart();
    this.initMortgageEstateChart();
    this.initTodayLoanChart();
    this.initPersonalChart();
    this.initBadLoanAmountChart();
    this.initTypeLoanChart();
    // 添加监听
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    // 定义需要销毁的图表数组
    const charts = ['estateLoanChart', 'mortgageEstateChart', 'todayLoanChart', 'todayLoanPercentChart',
      'personalChart', 'badLoanAmountChart', 'typeLoanChart'
    ];

    // 遍历数组并销毁图表
    charts.forEach(chartName => {
      if (this[chartName]) {
        this[chartName].dispose();
        this[chartName] = null;
      }
    });
    // 移除监听
    window.removeEventListener('resize', this.handleResize);
  },
  watch: {
    // tab页监听并resize
    estateName(newVal, oldVal) {
      if(oldVal == '1') {
        this.todayLoanChart.dispose()
      }else if(oldVal == '2') {
        this.todayLoanPercentChart.dispose()
      }
      if(newVal == '1') {
        this.initTodayLoanChart();
        this.$nextTick(() => {
          this.todayLoanChart.resize()
        })
      }else if(newVal == '2') {
        this.initTodayLoanPercentChart();
        this.$nextTick(() => {
          this.todayLoanPercentChart.resize()
        })
      }
    },
    loanTypeName(newVal) {
      this.typeLoanChart.dispose()
      if(newVal == '1') {
        this.typeLoanData = [
          { value: 1264, name: '个人住房贷款' },
          { value: 824, name: '商业用房开发贷款' },
          { value: 580, name: '其他房地产贷款' },
          { value: 484, name: '房地产开发贷款' }
        ]
        this.initTypeLoanChart();
      }else if(newVal == '2') {
        this.typeLoanData = [
          { value: 1264, name: '杭州银行' },
          { value: 824, name: '宁波银行' },
          { value: 380, name: '温州银行' },
          { value: 1484, name: '上海银行' }
        ]
        this.initTypeLoanChart();
      }else if(newVal == '3') {
        this.typeLoanData = [
          { value: 1064, name: '杭州' },
          { value: 814, name: '宁波' },
          { value: 780, name: '温州' },
          { value: 284, name: '嘉兴' }
        ]
        this.initTypeLoanChart();
      }else if(newVal == '4') {
        this.typeLoanData = [
          { value: 164, name: 'A类' },
          { value: 24, name: 'B类' },
          { value: 80, name: 'C类' },
          { value: 44, name: 'D类' }
        ]
        this.initTypeLoanChart();
      }
    }
  },
  methods: {
    initEstateLoanChart() {
      this.estateLoanChart = echarts.init(document.getElementById('estateLoanChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['贷款余额', '余额占比']
        },
        grid: {
          left: '3%',
          right: '6%',
          bottom: '3%',
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
        yAxis: [
          {
            type: 'value',
            name: '贷款余额',
            position: 'left',  // 左Y轴
            axisLabel: {
              formatter: '{value} 亿'
            }
          },
          {
            type: 'value',
            name: '余额占比',
            position: 'right',  // 右Y轴
            axisLabel: {
              formatter: '{value} %'
            }
          }
        ],
        series: [
          {
            name: '贷款余额',
            type: 'line',
            data: [16, 32, 21, 34, 39, 47, 55],
            yAxisIndex: 0
          },
          {
            name: '余额占比',
            type: 'line',
            data: [0.57, 0.64, 0.53, 0.47, 0.58, 0.69, 0.66],
            yAxisIndex: 1
          }
        ]
      };
      this.estateLoanChart.setOption(option);
    },
    initMortgageEstateChart() {
      this.mortgageEstateChart = echarts.init(document.getElementById('mortgageEstateChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['贷款余额']
        },
        grid: {
          left: '3%',
          right: '7%',
          bottom: '3%',
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
        yAxis: [
          {
            type: 'value',
            name: null,
            position: 'left',  // 左Y轴
            axisLabel: {
              formatter: '{value} 亿'
            }
          }
        ],
        series: [
          {
            name: '贷款余额',
            type: 'line',
            data: [16, 32, 21, 34, 39, 37, 44],
            yAxisIndex: 0,
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,  // 从上到下的渐变
                colorStops: [
                  { offset: 0, color: 'rgba(54, 162, 235, 0.8)' },  // 起始颜色
                  { offset: 1, color: 'rgba(54, 162, 235, 0.1)' }   // 结束颜色（透明度降低）
                ]
              }
            }
          }
        ]
      };
      this.mortgageEstateChart.setOption(option);
    },
    initTodayLoanChart() {
      this.todayLoanChart = echarts.init(document.getElementById('todayLoanChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['国有企业', '民营企业', '全部']
        },
        grid: {
          left: '3%',
          right: '6%',
          bottom: '3%',
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
          axisLabel: {
            formatter: '{value} 万'
          }
        },
        series: [
          {
            name: '国有企业',
            type: 'line',
            stack: '总量',
            data: [16, 32, 21, 34, 39, 47, 33],
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,  // 从上到下的渐变
                colorStops: [
                  { offset: 0, color: 'rgba(54, 162, 235, 0.8)' },  // 起始颜色
                  { offset: 1, color: 'rgba(54, 162, 235, 0.1)' }   // 结束颜色（透明度降低）
                ]
              }
            }
          },
          {
            name: '民营企业',
            type: 'line',
            stack: '总量',
            data: [12, 23, 15, 26, 29, 33, 21],
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,  // 从上到下的渐变
                colorStops: [
                  { offset: 0, color: 'rgba(99, 177, 83, 0.8)' },  // 起始颜色
                  { offset: 1, color: 'rgba(99, 177, 83, 0.1)' }   // 结束颜色（透明度降低）
                ]
              }
            }
          },
          {
            name: '全部',
            type: 'line',
            stack: '总量',
            data: [32, 45, 39, 52, 55, 63, 59],
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,  // 从上到下的渐变
                colorStops: [
                  { offset: 0, color: 'rgba(250, 220, 88, 0.8)' },  // 起始颜色
                  { offset: 1, color: 'rgba(250, 220, 88, 0.1)' }   // 结束颜色（透明度降低）
                ]
              }
            }
          }
        ]
      };
      this.todayLoanChart.setOption(option);
    },
    initTodayLoanPercentChart() {
      this.todayLoanPercentChart = echarts.init(document.getElementById('todayLoanPercentChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['国有企业', '民营企业', '全部']
        },
        grid: {
          left: '3%',
          right: '6%',
          bottom: '3%',
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
          axisLabel: {
            formatter: '{value} %'
          }
        },
        series: [
          {
            name: '国有企业',
            type: 'line',
            stack: '总量',
            data: [0.16, 0.32, 0.21, 0.34, 0.39, 0.47, 0.54],
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,  // 从上到下的渐变
                colorStops: [
                  { offset: 0, color: 'rgba(54, 162, 235, 0.8)' },  // 起始颜色
                  { offset: 1, color: 'rgba(54, 162, 235, 0.1)' }   // 结束颜色（透明度降低）
                ]
              }
            }
          },
          {
            name: '民营企业',
            type: 'line',
            stack: '总量',
            data: [0.12, 0.23, 0.15, 0.26, 0.29, 0.33, 0.41],
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,  // 从上到下的渐变
                colorStops: [
                  { offset: 0, color: 'rgba(99, 177, 83, 0.8)' },  // 起始颜色
                  { offset: 1, color: 'rgba(99, 177, 83, 0.1)' }   // 结束颜色（透明度降低）
                ]
              }
            }
          },
          {
            name: '全部',
            type: 'line',
            stack: '总量',
            data: [0.32, 0.45, 0.39, 0.52, 0.55, 0.63, 0.95],
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,  // 从上到下的渐变
                colorStops: [
                  { offset: 0, color: 'rgba(250, 220, 88, 0.8)' },  // 起始颜色
                  { offset: 1, color: 'rgba(250, 220, 88, 0.1)' }   // 结束颜色（透明度降低）
                ]
              }
            }
          }
        ]
      };
      this.todayLoanPercentChart.setOption(option);
    },
    initPersonalChart() {
      this.personalChart = echarts.init(document.getElementById('personalChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['贷款余额', '贷款比例']
        },
        grid: {
          left: '3%',
          right: '6%',
          bottom: '3%',
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
        yAxis: [
          {
            type: 'value',
            name: null,
            position: 'left',
            axisLabel: {
              formatter: '{value} 万'
            }
          },
          {
            type: 'value',
            name: null,
            position: 'right',
            axisLabel: {
              formatter: '{value} %'
            }
          }
        ],
        series: [
          {
            name: '贷款余额',
            type: 'line',
            data: [22, 31, 23, 25, 26, 27, 33],
            yAxisIndex: 0
          },
          {
            name: '贷款比例',
            type: 'line',
            data: [0.12, 0.23, 0.18, 0.27, 0.31, 0.35, 0.28],
            yAxisIndex: 1
          }
        ]
      };
      this.personalChart.setOption(option);
    },
    initBadLoanAmountChart() {
      this.badLoanAmountChart = echarts.init(document.getElementById('badLoanAmountChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['不良贷款余额', '不良贷款率']
        },
        grid: {
          left: '3%',
          right: '6%',
          bottom: '3%',
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
        yAxis: [
          {
            type: 'value',
            name: null,
            position: 'left',  // 左Y轴
            axisLabel: {
              formatter: '{value} 亿'
            }
          },
          {
            type: 'value',
            name: null,
            position: 'right',  // 右Y轴
            axisLabel: {
              formatter: '{value} %'
            }
          }
        ],
        series: [
          {
            name: '不良贷款余额',
            type: 'line',
            data: [341, 275, 289, 286, 288, 289, 317],
            yAxisIndex: 0
          },
          {
            name: '不良贷款率',
            type: 'line',
            data: [0.52, 0.43, 0.49, 0.65, 0.65, 0.68, 0.69],
            yAxisIndex: 1
          }
        ]
      };
      this.badLoanAmountChart.setOption(option);
    },
    initTypeLoanChart() {
      this.typeLoanChart = echarts.init(document.getElementById('typeLoanChart'));
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          type: 'scroll',
          position: 'bottom',
          bottom: '0'
        },
        series: [
          {
            name: '分类型房地产贷款余额',
            type: 'pie',
            center:['50%','40%'],
            radius: ['50%','80%'],
            data: this.typeLoanData,
            label: {
              show: true,
              formatter: '{b}: {d}%'
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      this.typeLoanChart.setOption(option);
    },
    handleResize() {
      this.estateLoanChart.resize()
      this.mortgageEstateChart.resize()
      if(this.estateName === '1') {
        this.todayLoanChart.resize()
      }else {
        this.todayLoanPercentChart.resize()
      }
      this.personalChart.resize()
      this.badLoanAmountChart.resize()
      this.typeLoanChart.resize()
    }
  }
}
</script>
