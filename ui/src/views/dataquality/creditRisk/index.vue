<template>
  <div class="app-container">
    <el-form :inline="true" label-width="70px">
      <el-form-item label="机构">
        <el-select v-model="orgSelected" placeholder="请选择机构">
          <el-option
            v-for="(item,index) in orgList"
            :key="index"
            :label="item"
            :value="item">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="地区">
        <el-select v-model="areaSelected" placeholder="请选择地区">
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
            不良贷款余额及比例
          </span>
        </div>
        <div id="badLoanChart" style="width: 100%; height: 280px"></div>
      </el-card>
      <el-card class="card-box" style="width: 50%; height: 350px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            预期贷款情况
          </span>
        </div>
        <el-tabs v-model="activeName">
          <el-tab-pane label="预期90天以上" name="over90">
            <div id="over90LineChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="预期60天以上" name="over60">
            <div id="over60LineChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="逾期情况" name="overCondition">
            <div id="overConditionLineChart" style="width: 600px; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="占不良贷款比例" name="percent">
            <div id="percentLineChart" style="width: 600px; height: 230px"></div>
          </el-tab-pane>
        </el-tabs>
      </el-card>

    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%; height: 350px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-pie-chart icon-title"></div>
            不良贷款余额
          </span>
        </div>
        <el-tabs v-model="loanName">
          <el-tab-pane label="地区" name="area">
          </el-tab-pane>
          <el-tab-pane label="行业" name="industry">
          </el-tab-pane>
          <el-tab-pane label="担保方式" name="guarantee">
          </el-tab-pane>
          <el-tab-pane label="客户" name="client">
          </el-tab-pane>
        </el-tabs>
        <div id="badLoanAreaChart" style="width: 100%; height: 230px"></div>
      </el-card>
      <el-card class="card-box" style="width: 50%; height: 350px">
         <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            新增不良贷款金额
          </span>
        </div>
        <el-tabs v-model="orgActiveName">
          <el-tab-pane label="按机构汇总" name="1">
            <div id="orderByOrgChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="分机构" name="2">
            <div id="orgChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%; height: 350px">
         <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            不良贷款处置
          </span>
        </div>
        <el-tabs v-model="badLoanActivaName">
          <el-tab-pane label="不良贷款处置金额" name="1">
            <div id="badLoanAmountChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="分类型当日" name="2">
            <div id="typeDayChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="分类型当年" name="3">
            <div id="typeYearChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      <el-card class="card-box" style="width: 50%; height: 350px">
         <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            特色贷款不良贷款余额及比例
          </span>
        </div>
        <el-tabs v-model="loanTypeName">
          <el-tab-pane label="无还本续贷" name="1">
            <div id="noBackLoanChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="互联网贷款" name="2">
            <div id="internetLoanChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
          <el-tab-pane label="绿色贷款" name="3">
            <div id="greenLoanChart" style="width: 100%; height: 230px"></div>
          </el-tab-pane>
        </el-tabs>
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
      badLoanData: [
        { value: 1048, name: '杭州' },
        { value: 735, name: '宁波' },
        { value: 580, name: '温州' },
        { value: 484, name: '嘉兴' }
      ],
      // 下拉框内容初始化
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
      // 日期选择器限制
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      // tab页初始化
      activeName: 'over90',
      loanName: 'area',
      orgActiveName: '1',
      badLoanActivaName: '1',
      loanTypeName: '1',
      // 图表初始化
      badLoanChart: null,
      over90LineChart: null,
      over60LineChart: null,
      overConditionLineChart: null,
      percentLineChart: null,
      badLoanAreaChart: null,
      orderByOrgChart: null,
      orgChart: null,
      badLoanAmountChart: null,
      typeDayChart: null,
      typeYearChart: null,
      noBackLoanChart: null,
      internetLoanChart: null,
      greenLoanChart: null
    }
  },
  mounted() {
    this.initBadLoanChart();
    this.initOver90LineChart();
    this.initBadLoanAreaChart();
    this.initOrderByOrgChart();
    this.initBadLoanAmountChart();
    this.initNoBackLoanChart();
    // 监听浏览器大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 定义需要销毁的图表数组
    const charts = ['badLoanChart', 'over90LineChart', 'over60LineChart', 'overConditionLineChart',
      'percentLineChart', 'badLoanAreaChart', 'orderByOrgChart', 'orgChart', 'badLoanAmountChart',
      'typeDayChart', 'typeYearChart', 'noBackLoanChart', 'internetLoanChart', 'greenLoanChart'
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
    activeName(newVal, oldVal) {
      if(oldVal == 'over90') {
        this.over90LineChart.dispose()
      }else if(oldVal == 'over60'){
        this.over60LineChart.dispose()
      }else if(oldVal == 'overCondition'){
        this.overConditionLineChart.dispose()
      }else if(oldVal == 'percent'){
        this.percentLineChart.dispose()
      }
      if(newVal == 'over90') {
        this.initOver90LineChart();
        this.$nextTick(() => {
          this.over90LineChart.resize()
        })
      }else if(newVal == 'over60') {
        this.initOver60LineChart();
        this.$nextTick(() => {
          this.over60LineChart.resize()
        })
      }else if(newVal == 'overCondition') {
        this.initOverConditionLineChart();
        this.$nextTick(() => {
          this.overConditionLineChart.resize()
        })
      }else if(newVal == 'percent') {
        this.initPercentLineChart();
        this.$nextTick(() => {
          this.percentLineChart.resize()
        })
      }
    },
    loanName(newVal){
      this.badLoanAreaChart.dispose()
      if(newVal == 'area') {
        this.badLoanData = [
          { value: 1048, name: '杭州' },
          { value: 735, name: '宁波' },
          { value: 580, name: '温州' },
          { value: 484, name: '嘉兴' }
        ]
        this.initBadLoanAreaChart();
      }else if(newVal == 'industry') {
        this.badLoanData = [
          { value: 1273, name: 'A企业' },
          { value: 1372, name: 'B企业' },
          { value: 980, name: 'C企业' },
          { value: 584, name: 'D企业' }
        ]
        this.initBadLoanAreaChart();
      }else if(newVal == 'guarantee') {
        this.badLoanData = [
          { value: 173, name: 'A担保方式' },
          { value: 372, name: 'B担保方式' },
          { value: 820, name: 'C担保方式' },
          { value: 541, name: 'D担保方式' }
        ]
        this.initBadLoanAreaChart();
      }else if(newVal == 'client') {
        this.badLoanData = [
          { value: 73, name: 'A客户' },
          { value: 132, name: 'B客户' },
          { value: 80, name: 'C客户' },
          { value: 84, name: 'D客户' }
        ]
        this.initBadLoanAreaChart();
      }
    },
    orgActiveName(newVal, oldVal) {
      if(oldVal == '1') {
        this.orderByOrgChart.dispose()
      }else if(oldVal == '2') {
        this.orgChart.dispose()
      }
      if(newVal == '1'){
        this.initOrderByOrgChart();
        this.$nextTick(() => {
          this.orderByOrgChart.resize()
        })
      }else if(newVal = '2') {
        this.initOrgChart()
        this.$nextTick(() => {
          this.orgChart.resize()
        })
      }
    },
    badLoanActivaName(newVal, oldVal) {
      if(oldVal == '1') {
        this.badLoanAmountChart.dispose()
      }else if(oldVal == '2') {
        this.typeDayChart.dispose()
      }else if(oldVal == '3') {
        this.typeYearChart.dispose()
      }
      if(newVal == '1'){
        this.initBadLoanAmountChart();
        this.$nextTick(() => {
          this.badLoanAmountChart.resize()
        })
      }else if(newVal == '2'){
        this.initTypeDayChart();
        this.$nextTick(() => {
          this.typeDayChart.resize()
        })
      }else if(newVal == '3'){
        this.initTypeYearChart();
        this.$nextTick(() => {
          this.typeYearChart.resize()
        })
      }
    },
    loanTypeName(newVal, oldVal) {
      if(oldVal == '1') {
        this.noBackLoanChart.dispose()
      }else if(oldVal == '2') {
        this.internetLoanChart.dispose()
      }else if(oldVal == '3') {
        this.greenLoanChart.dispose()
      }
      if(newVal == '1') {
        this.initNoBackLoanChart();
        this.$nextTick(() => {
          this.noBackLoanChart.resize()
        })
      }else if(newVal == '2') {
        this.initInternetLoanChart();
        this.$nextTick(() => {
          this.internetLoanChart.resize()
        })
      }else if(newVal == '3') {
        this.initGreenLoanChart();
        this.$nextTick(() => {
          this.greenLoanChart.resize()
        })
      }
    }
  },
  methods: {
    initBadLoanChart() {
      this.badLoanChart = echarts.init(document.getElementById('badLoanChart'));
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
            name: '不良贷款余额',
            position: 'left',  // 左Y轴
            axisLabel: {
              formatter: '{value} 亿'
            }
          },
          {
            type: 'value',
            name: '不良贷款率',
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
            data: [16, 32, 21, 34, 39, 47, 52],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '不良贷款率',
            type: 'line',
            data: [0.57, 0.64, 0.53, 0.47, 0.58, 0.69, 0.78],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.badLoanChart.setOption(option);
    },
    initOver90LineChart() {
      this.over90LineChart = echarts.init(document.getElementById('over90LineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['逾期90天以上贷款余额', '逾期90天以上贷款占比']
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
            name: '逾期90天以上贷款余额',
            type: 'line',
            data: [16, 32, 21, 34, 39, 47, 54],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '逾期90天以上贷款占比',
            type: 'line',
            data: [0.57, 0.64, 0.53, 0.47, 0.58, 0.69, 0.66],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.over90LineChart.setOption(option);
    },
    initOver60LineChart() {
      this.over60LineChart = echarts.init(document.getElementById('over60LineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['逾期60天以上贷款余额', '逾期60天以上贷款占比']
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
            name: '逾期60天以上贷款余额',
            type: 'line',
            data: [12, 23, 22, 14, 18, 11, 20],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '逾期60天以上贷款占比',
            type: 'line',
            data: [0.57, 0.64, 0.53, 0.47, 0.58, 0.69, 0.66],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.over60LineChart.setOption(option);
    },
    initOverConditionLineChart() {
      this.overConditionLineChart = echarts.init(document.getElementById('overConditionLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['逾期贷款余额', '逾期贷款率']
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
            name: '逾期贷款余额',
            type: 'line',
            data: [11, 13, 21, 30, 19, 37, 44],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '逾期贷款率',
            type: 'line',
            data: [0.27, 0.62, 0.33, 0.4, 0.38, 0.59, 0.66],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.overConditionLineChart.setOption(option);
    },
    initPercentLineChart() {
      this.percentLineChart = echarts.init(document.getElementById('percentLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['逾期90天以上贷款比例', '逾期60天以上贷款比例', '逾期贷款比例']
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
              formatter: '{value} %'
            }
          }
        ],
        series: [
          {
            name: '逾期90天以上贷款比例',
            type: 'line',
            data: [0.12, 0.22, 0.33, 0.32, 0.44, 0.54, 0.51],
            smooth: true
          },
          {
            name: '逾期60天以上贷款比例',
            type: 'line',
            data: [0.57, 0.64, 0.53, 0.47, 0.58, 0.69, 0.66],
            smooth: true
          },
          {
            name: '逾期贷款比例',
            type: 'line',
            data: [0.62, 0.78, 0.82, 0.93, 0.94, 0.82, 0.96],
            smooth: true
          }
        ]
      };
      this.percentLineChart.setOption(option);
    },
    initBadLoanAreaChart() {
      this.badLoanAreaChart = echarts.init(document.getElementById('badLoanAreaChart'));
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          position: 'bottom',
          bottom: '0'
        },
        series: [
          {
            name: '不良贷款余额',
            type: 'pie',
            center:['50%','40%'],
            radius: ['50%','80%'],
            data: this.badLoanData,
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
      this.badLoanAreaChart.setOption(option);
    },
    initOrderByOrgChart() {
      this.orderByOrgChart = echarts.init(document.getElementById('orderByOrgChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['当日新增不良贷款金额']
        },
        grid: {
          left: '3%',
          right: '8%',
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
              formatter: '{value} 万'
            }
          }
        ],
        series: [
          {
            name: '当日新增不良贷款金额',
            type: 'line',
            data: [16, 32, 21, 23, 34, 39, 47],
            yAxisIndex: 0,
            smooth: true
          }
        ]
      };
      this.orderByOrgChart.setOption(option);
    },
    initOrgChart() {
      this.orgChart = echarts.init(document.getElementById('orgChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['杭州银行']
        },
        grid: {
          left: '3%',
          right: '8%',
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
              formatter: '{value} 万'
            }
          }
        ],
        series: [
          {
            name: '杭州银行',
            type: 'line',
            data: [1200, 3002, 2100, 2030, 4002, 3900, 4700],
            yAxisIndex: 0,
            smooth: true
          }
        ]
      };
      this.orgChart.setOption(option);
    },
    initBadLoanAmountChart() {
      this.badLoanAmountChart = echarts.init(document.getElementById('badLoanAmountChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['当日处置贷款总额', '当年处置贷款总额']
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
              formatter: '{value} 万'
            }
          },
          {
            type: 'value',
            name: null,
            position: 'right',  // 右Y轴
            axisLabel: {
              formatter: '{value} 亿'
            }
          }
        ],
        series: [
          {
            name: '当日处置贷款总额',
            type: 'line',
            data: [12, 7, 24, 12, 21, 7, 15],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '当年处置贷款总额',
            type: 'line',
            data: [0.81, 0.73, 0.68, 0.69, 0.72, 0.66, 0.83],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.badLoanAmountChart.setOption(option);
    },
    initTypeDayChart() {
      this.typeDayChart = echarts.init(document.getElementById('typeDayChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['当日核销处置不良金额', '当日转让处置不良金额', '当日现金处置不良金额', '当日以物抵押处置不良金额', '当日其他处置不良金额']
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
              formatter: '{value} 万'
            }
          }
        ],
        series: [
          {
            name: '当日核销处置不良金额',
            type: 'line',
            data: [120, 17, 204, 120, 202, 70, 105],
            smooth: true
          },
          {
            name: '当日转让处置不良金额',
            type: 'line',
            data: [101, 130, 127, 80, 142, 380, 180],
            smooth: true
          },
          {
            name: '当日现金处置不良金额',
            type: 'line',
            data: [30, 52, 120, 73, 94, 151, 230],
            smooth: true
          },
          {
            name: '当日以物抵押处置不良金额',
            type: 'line',
            data: [103, 110, 160, 90, 240, 170, 120],
            smooth: true
          },
          {
            name: '当日其他处置不良金额',
            type: 'line',
            data: [80, 190, 102, 70, 190, 150, 211],
            smooth: true
          }
        ]
      };
      this.typeDayChart.setOption(option);
    },
    initTypeYearChart() {
      this.typeYearChart = echarts.init(document.getElementById('typeYearChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['当年核销处置不良金额', '当年转让处置不良金额', '当年现金处置不良金额', '当年以物抵押处置不良金额', '当年其他处置不良金额']
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
          }
        ],
        series: [
          {
            name: '当年核销处置不良金额',
            type: 'line',
            data: [12, 7, 24, 12, 21, 7, 15],
            smooth: true
          },
          {
            name: '当年转让处置不良金额',
            type: 'line',
            data: [11, 10, 7, 8, 4, 3, 8],
            smooth: true
          },
          {
            name: '当年现金处置不良金额',
            type: 'line',
            data: [3, 5, 2, 7, 9, 5, 3],
            smooth: true
          },
          {
            name: '当年以物抵押处置不良金额',
            type: 'line',
            data: [13, 11, 6, 9, 4, 10, 12],
            smooth: true
          },
          {
            name: '当年其他处置不良金额',
            type: 'line',
            data: [8, 9, 12, 7, 9, 10, 11],
            smooth: true
          }
        ]
      };
      this.typeYearChart.setOption(option);
    },
    initNoBackLoanChart() {
      this.noBackLoanChart = echarts.init(document.getElementById('noBackLoanChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['无还本续贷不良贷款余额', '无还本续贷不良贷款率']
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
              formatter: '{value} 万'
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
            name: '无还本续贷不良贷款余额',
            type: 'line',
            data: [11, 14, 18, 12, 15, 13, 19],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '无还本续贷不良贷款率',
            type: 'line',
            data: [0.22, 0.37, 0.29, 0.41, 0.33, 0.44, 0.52],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.noBackLoanChart.setOption(option);
    },
    initInternetLoanChart() {
      this.internetLoanChart = echarts.init(document.getElementById('internetLoanChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['不良贷款余额（亿元）', '不良贷款率（%）']
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
            name: '不良贷款余额（亿元）',
            type: 'line',
            data: [9, 5, 10, 6, 8, 9, 12],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '不良贷款率（%）',
            type: 'line',
            data: [0.2, 0.31, 0.19, 0.31, 0.33, 0.4, 0.25],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.internetLoanChart.setOption(option);
    },
    initGreenLoanChart() {
      this.greenLoanChart = echarts.init(document.getElementById('greenLoanChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['不良贷款余额（亿元）', '不良贷款率（%）']
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
            name: '不良贷款余额（亿元）',
            type: 'line',
            data: [3, 7, 3, 5, 2, 1, 5],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '不良贷款率（%）',
            type: 'line',
            data: [0.12, 0.31, 0.39, 0.31, 0.23, 0.46, 0.65],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.greenLoanChart.setOption(option);
    },
    handleResize() {
      this.badLoanChart.resize();
      if(this.activeName === 'over90') {
        this.over90LineChart.resize();
      }else if(this.activeName === 'over60') {
        this.over60LineChart.resize();
      }else if(this.activeName === 'overCondition') {
        this.overConditionLineChart.resize();
      }else if(this.activeName === 'percent') {
        this.percentLineChart.resize();
      }
      this.badLoanAreaChart.resize();
      if(this.orgActiveName === '1') {
        this.orderByOrgChart.resize();
      }else {
        this.orgChart.resize();
      }
      if(this.badLoanActivaName === '1') {
        this.badLoanAmountChart.resize();
      }else if(this.badLoanActivaName === '2') {
        this.typeDayChart.resize();
      }else{
        this.typeYearChart.resize();
      }
      if(this.loanTypeName === '1') {
        this.noBackLoanChart.resize();
      }else if(this.loanTypeName === '2'){
        this.internetLoanChart.resize();
      }else{
        this.greenLoanChart.resize();
      }
    }
  }
}
</script>
