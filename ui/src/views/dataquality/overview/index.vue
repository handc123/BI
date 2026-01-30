<template>
  <div class="app-container">
    <el-form :inline="true" label-width="80px">
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
    <div class="display-flex">
      <el-card class="card-box" style="height: 800px; width: 50%">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            不良监测
          </span>
        </div>
        <div class="display-flex">
          <div class="card-boxs" style="width: 50%; height: 250px;">
            <p class="box-title bold-text font-size-16">不良贷款率</p>
            <div id="badLoanChart" style="width: 100%; height: 230px;"></div>
          </div>
          <div class="card-boxs" style="width: 50%; height: 250px;">
            <p class="box-title bold-text font-size-16">贷款占比</p>
            <div id="estatePieChart" style="width: 100%; height: 210px;"></div>
          </div>
        </div>
        <div class="display-flex">
          <div class="card-boxs" style="width: 100%; height: 230px;">
            <p class="box-title bold-text font-size-16">房地产及民营、国有企业贷款余额变化趋势图</p>
            <div id="estateLineChart" style="width: 100%; height: 200px;"></div>
          </div>
        </div>
        <div class="display-flex">
          <div class="card-boxs" style="width: 100%; height: 230px;">
            <p class="box-title bold-text font-size-16">逾期及关注类不良率变化趋势图</p>
            <div id="badLineChart" style="width: 100%; height: 200px;"></div>
          </div>
        </div>
      </el-card>
      <el-card class="card-box" style="height: 800px; width: 50%">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-data-line icon-title"></div>
            重点指标监测
          </span>
        </div>
        <el-table :data="importIndexData" style="height: 720px" class="overflow-true">
          <el-table-column label="指标名称" align="center" prop="indexName" :show-overflow-tooltip="true"></el-table-column>
          <el-table-column label="指标数值" align="center" prop="indexNumber" :show-overflow-tooltip="true"></el-table-column>
          <el-table-column label="系统内排名" align="center" prop="systemRank"></el-table-column>
          <el-table-column label="较上日" align="center" prop="compareWithYesterday">
            <template slot-scope="scope">
              <div :style="{color: scope.row.compareWithYesterday[0] === '-' ? 'green' : 'red'}">{{ scope.row.compareWithYesterday }}</div>
            </template>
          </el-table-column>
          <el-table-column label="较年初" align="center" prop="compareWithBegining">
            <template slot-scope="scope">
              <div :style="{color: scope.row.compareWithBegining[0] === '-' ? 'green' : 'red'}">{{ scope.row.compareWithBegining }}</div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="height: 600px">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-office-building icon-title"></div>
            房地产金融
          </span>
        </div>
        <div class="card-boxs" style="width: 100%; height: 250px;">
          <el-button type="text" class="float-right">查看更多></el-button>
          <p class="box-title bold-text font-size-16">不良贷款当日新增金额以及处置总额变化趋势图</p>
          <div id="riskLineChart1" style="width: 100%; height: 220px"></div>
        </div>
        <div class="card-boxs" style="width: 100%; height: 250px;">
          <el-button type="text" class="float-right">查看更多></el-button>
          <p class="box-title bold-text font-size-16">重点风险</p>
          <div id="riskLineChart2" style="width: 100%; height: 220px"></div>
        </div>
      </el-card>
      <el-card class="card-box" style="height: 600px">
        <div slot="header">
          <div class="display-flex alicenter justify-between">
            <span class="head-title display-flex alicenter">
              <div class="el-icon-document icon-title"></div>
              五篇大文章
            </span>
            <div class="float-right">
              <el-tabs v-model="activeName" class="margin-top--10">
                <el-tab-pane label="普惠金融" name="inclusive">
                </el-tab-pane>
                <el-tab-pane label="科技金融" name="technology">
                </el-tab-pane>
                <el-tab-pane label="绿色金融" name="green">
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </div>
        <div v-if="activeName === 'inclusive'" class="overflow-true">
          <div class="card-boxs" style="width: 98%; height: 250px">
            <p class="box-title bold-text font-size-16">当日新增不良金额变化趋势图</p>
            <div id="inclusiveLineChart" style="width: 100%; height: 220px"></div>
          </div>
          <div class="card-boxs" style="width: 98%; height: 250px">
            <p class="box-title bold-text font-size-16">普通型小微企业当天发放贷款金额及年化利率变化趋势图</p>
            <div id="inclusiveBarChart" style="width: 100%; height: 220px"></div>
          </div>
        </div>
        <div v-if="activeName === 'technology'" class="overflow-true">
          <div class="card-boxs" style="width: 98%; height: 250px">
            <p class="box-title bold-text font-size-16">借款金额变化趋势图</p>
            <div id="technologyLineChart" style="width: 100%; height: 220px"></div>
          </div>
          <div class="card-boxs" style="width: 98%; height: 250px">
            <p class="box-title bold-text font-size-16">科技型小微企业当天发放贷款金额及年化利率变化趋势图</p>
            <div id="technologyBarChart" style="width: 100%; height: 220px"></div>
          </div>
        </div>
        <div v-if="activeName === 'green'" class="overflow-true">
          <div class="card-boxs" style="width: 98%; height: 250px">
            <p class="box-title bold-text font-size-16">金额变化趋势图</p>
            <div id="greenLineChart" style="width: 100%; height: 220px"></div>
          </div>
          <div class="card-boxs" style="width: 98%; height: 250px">
            <p class="box-title bold-text font-size-16">绿色型小微企业当天发放贷款金额及年化利率变化趋势图</p>
            <div id="greenBarChart" style="width: 100%; height: 220px"></div>
          </div>
        </div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-boxs" style="width: 100%; height: 420px;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-suitcase icon-title"></div>
            重点业务
          </span>
        </div>
        <p class="box-title bold-text font-size-16">当天吸收的定期存款金额及当天存量平均利率变化趋势图</p>
        <div id="businessChart" style="width: 100%; height: 320px"></div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 40%; height: 420px;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            分机构类型同业资产负责条形图
          </span>
        </div>
        <div id="orgChart" style="width: 100%; height: 350px"></div>
      </el-card>
      <el-card class="card-box" style="width: 30%; height: 420px;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-pie-chart icon-title"></div>
            投资业务结构
          </span>
        </div>
        <div id="investChart" style="width: 100%; height: 350px"></div>
      </el-card>
      <el-card class="card-box" style="width: 30%; height: 420px;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-pie-chart icon-title"></div>
            债券投资结构
          </span>
        </div>
        <div id="bondChart" style="width: 100%; height: 350px"></div>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default {
  name: "DataQuality",
  data() {
    return {
      time:['2025-06-01','2025-06-02','2025-06-03','2025-06-04','2025-06-05','2025-06-06','2025-06-07'],
      date: '',
      importIndexData: [
        {indexName: '各项贷款余额', indexNumber: '32245', systemRank: '5', compareWithYesterday: '-0.27%', compareWithBegining: '5.78%'},
        {indexName: '对公贷款余额', indexNumber: '14989', systemRank: '4', compareWithYesterday: '-0.13%', compareWithBegining: '2.64%'}
      ],
      // 日期选择器限制
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      // 下拉框初始化
      orgSelected: '杭州农商联合银行',
      orgList: [
        '萧山农商联合银行',
        '余杭农商联合银行',
        '杭州农商联合银行'
      ],
      dateRange: [],
      // tab页初始化
      activeName: 'inclusive',
      // 图表初始化
      badLoanChart: null,
      estatePieChart: null,
      estateLineChart: null,
      badLineChart: null,
      riskLineChart1: null,
      riskLineChart2: null,
      inclusiveLineChart: null,
      inclusiveBarChart: null,
      technologyLineChart: null,
      technologyBarChart: null,
      greenLineChart: null,
      greenBarChart: null,
      businessChart: null,
      orgChart: null,
      investChart: null,
      bondChart: null
    }
  },
  mounted() {
    this.initBadLoanChart();
    this.initBadLineChart();
    this.initEstateLineChart();
    this.initEstatePieChart();
    this.initRiskLineChart1();
    this.initRiskLineChart2();
    this.initInclusiveLineChart();
    this.initInclusiveBarChart();
    this.initBussnessChart();
    this.initOrgChart();
    this.initInvestChart();
    this.initBondChart();
    // 添加监听
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 定义需要销毁的图表数组
    const charts = ['badLoanChart', 'estatePieChart', 'estateLineChart', 'badLineChart', 'bondChart',
      'riskLineChart1', 'riskLineChart2', 'inclusiveLineChart', 'inclusiveBarChart', 'technologyLineChart',
      'technologyBarChart', 'greenLineChart', 'greenBarChart', 'businessChart', 'orgChart', 'investChart'
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
      if(oldVal == 'inclusive') {
        if(this.inclusiveLineChart) {
          this.inclusiveLineChart.dispose();
        }
        if(this.inclusiveBarChart) {
          this.inclusiveBarChart.dispose();
        }
      }else if(oldVal == 'technology') {
        if(this.technologyLineChart) {
          this.technologyLineChart.dispose();
        }
        if(this.technologyBarChart) {
          this.technologyBarChart.dispose();
        }
      }else {
        if(this.greenLineChart) {
          this.greenLineChart.dispose();
        }
        if(this.greenBarChart) {
          this.greenBarChart.dispose();
        }
      }
      this.$nextTick(() => {
        if(newVal == 'inclusive') {
          this.initInclusiveLineChart();
          this.initInclusiveBarChart();
        }else if(newVal == 'technology'){
          this.initTechnologyLineChart();
          this.initTechnologyBarChart();
        }else if(newVal == 'green'){
          this.initGreenLineChart();
          this.initGreenBarChart();
        }
      });
    }
  },
  methods: {
    initBadLoanChart() {
      const chartDom = document.getElementById('badLoanChart');
      this.badLoanChart = echarts.init(chartDom);
      const option = {
        series: [
          {
            name: 'Pressure',
            type: 'gauge',
            title: {
              show: true,
              offsetCenter: [0, '30%'],  // 位置调整
              textStyle: {
                fontSize: 15,          // 字体大小
                color: '#333'         // 字体颜色
              }
            },
            progress: {
              show: true,
              width: 7
            },
            axisLine: {
              lineStyle: {
                width: 7
              }
            },
            axisTick: {
              show: false
            },
            splitLine: {
              show: false,
              length: 15,
              lineStyle: {
                width: 2,
                color: '#999'
              }
            },
            axisLabel: {
              show: false,
              distance: 7,
              color: '#999',
              fontSize: 10
            },
            anchor: {
              show: true,
              showAbove: true,
              size: 7,
              itemStyle: {
                borderWidth: 7
              }
            },
            detail: {
              valueAnimation: true,
              fontSize: 22,
              offsetCenter: [0, '70%'],
              formatter: '{value}%'
            },
            data: [
              {
                value: 60,
                name: '不良贷款率'
              }
            ]
          }
        ]
      }
      this.badLoanChart.setOption(option);
    },
    initBadLineChart() {
      this.badLineChart = echarts.init(document.getElementById('badLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['预期90天以上占比', '关注+不良率']
        },
        grid: {
          left: '3%',
          right: '4%',
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
          data: this.time,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '预期90天以上占比',
            type: 'line',
            stack: 'Total',
            data: [120, 132, 101, 134, 90, 230, 210],
            smooth: true
          },
          {
            name: '关注+不良率',
            type: 'line',
            stack: 'Total',
            data: [150, 232, 201, 190, 330, 410, 490],
            smooth: true
          }
        ]
      };
      this.badLineChart.setOption(option);
    },
    initEstateLineChart() {
      this.estateLineChart = echarts.init(document.getElementById('estateLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          }
        },
        legend: {
          data: ['民营企业', '国有企业']
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        grid: {
          left: '4%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            data: this.time,
            axisLabel: {
              rotate: 45
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '房地产贷款余额',
          }
        ],
        series: [
          {
            name: '民营企业',
            type: 'line',
            stack: 'Total',
            areaStyle: {},
            emphasis: {
              focus: 'series'
            },
            data: [120, 132, 101, 134, 90, 230, 210],
            smooth: true
          },
          {
            name: '国有企业',
            type: 'line',
            stack: 'Total',
            areaStyle: {},
            emphasis: {
              focus: 'series'
            },
            data: [220, 182, 191, 234, 290, 330, 310],
            smooth: true
          }
        ]
      };
      this.estateLineChart.setOption(option);
    },
    initEstatePieChart() {
      this.estatePieChart = echarts.init(document.getElementById('estatePieChart'));
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
            name: '个人住房贷款',
            type: 'pie',
            center: ['50%', '45%'],
            radius: ['50%', '80%'],
            data: [
              { value: 1048, name: '个人住房贷款' },
              { value: 735, name: 'XX贷款' },
              { value: 580, name: 'AA贷款' },
              { value: 484, name: 'BB贷款' }
            ],
            label: {
              show: false
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
      this.estatePieChart.setOption(option);
    },
    initRiskLineChart1() {
      this.riskLineChart1 = echarts.init(document.getElementById('riskLineChart1'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['对公贷款当天发放利率', '普惠小微当天发放利率', '房地产当天发放利率'],
          type: 'scroll'
        },
        grid: {
          left: '4%',
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
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '对公贷款当天发放利率',
            type: 'line',
            stack: 'Total',
            data: [1.6, 1.32, 1.01, 1.34, 2.9, 2.3, 1.5],
            smooth: true
          },
          {
            name: '普惠小微当天发放利率',
            type: 'line',
            stack: 'Total',
            data: [1.5, 2.32, 2.3, 1.2, 1.3, 3.1, 2.5],
            smooth: true
          },
          {
            name: '房地产当天发放利率',
            type: 'line',
            stack: 'Total',
            data: [2.5, 1.2, 2.3, 2.2, 3.0, 2.1, 2.5]
          }
        ]
      };
      this.riskLineChart1.setOption(option);
    },
    initRiskLineChart2() {
      this.riskLineChart2 = echarts.init(document.getElementById('riskLineChart2'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['当日新增不良金额', '当日不良处置贷款总额']
        },
        grid: {
          left: '4%',
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
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '当日新增不良金额',
            type: 'line',
            stack: 'Total',
            data: [160, 132, 201, 534, 390, 330, 220],
            smooth: true
          },
          {
            name: '当日不良处置贷款总额',
            type: 'line',
            stack: 'Total',
            data: [378, 290, 503, 280, 330, 310, 410],
            smooth: true
          }
        ]
      };
      this.riskLineChart2.setOption(option);
    },
    initInclusiveLineChart() {
      this.inclusiveLineChart = echarts.init(document.getElementById('inclusiveLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['当日新增不良金额']
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
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '当日新增不良金额',
            type: 'line',
            stack: 'Total',
            data: [400, 332, 301, 434, 350, 330, 310],
            smooth: true
          }
        ]
      };
      this.inclusiveLineChart.setOption(option);
    },
    initInclusiveBarChart() {
      this.inclusiveBarChart = echarts.init(document.getElementById('inclusiveBarChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['普惠型小微企业当天发放贷款年化利率', '普惠型小微企业当天发放贷款金额'],
          type: 'scroll'
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
          boundaryGap: ['20%', '20%'],
          data: this.time
        },
        yAxis: [
          {
            type: 'value',
            yAxisIndex: 0
          },
          {
            type: 'value',
            yAxisIndex: 1
          }
        ],
        series: [
          {
            name: '普惠型小微企业当天发放贷款年化利率',
            type: 'bar',
            barWidth: '30%',
            data: [400, 332, 301, 434, 350, 330, 310],
            yAxisIndex: 0
          },
          {
            name: '普惠型小微企业当天发放贷款金额',
            type: 'line',
            data: [420, 302, 311, 484, 300, 230, 210],
            smooth: true,
            yAxisIndex: 1
          }
        ]
      };
      this.inclusiveBarChart.setOption(option);
    },
    initTechnologyLineChart() {
      this.technologyLineChart = echarts.init(document.getElementById('technologyLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['借款金额']
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
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '借款金额',
            type: 'line',
            stack: 'Total',
            data: [300, 350, 380, 390, 420, 450, 480],
            smooth: true
          }
        ]
      };
      this.technologyLineChart.setOption(option);
    },
    initTechnologyBarChart() {
      this.technologyBarChart = echarts.init(document.getElementById('technologyBarChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['科技型小微企业当天发放贷款年化利率', '科技型小微企业当天发放贷款金额'],
          type: 'scroll'
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
          boundaryGap: ['20%', '20%'],
          data: this.time
        },
        yAxis: [
          {
            type: 'value',
            yAxisIndex: 0
          },
          {
            type: 'value',
            yAxisIndex: 1
          }
        ],
        series: [
          {
            name: '科技型小微企业当天发放贷款年化利率',
            type: 'bar',
            barWidth: '30%',
            data: [102, 239, 283, 372, 481, 382, 293],
            yAxisIndex: 0
          },
          {
            name: '科技型小微企业当天发放贷款金额',
            type: 'line',
            data: [371, 420, 500, 600, 700, 800, 900],
            smooth: true,
            yAxisIndex: 1
          }
        ]
      };
      this.technologyBarChart.setOption(option);
    },
    initGreenLineChart() {
      this.greenLineChart = echarts.init(document.getElementById('greenLineChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['金额']
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
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '金额',
            type: 'line',
            stack: 'Total',
            data: [100, 120, 130, 140, 150, 160, 170],
            smooth: true
          }
        ]
      };
      this.greenLineChart.setOption(option);
    },
    initGreenBarChart() {
      this.greenBarChart = echarts.init(document.getElementById('greenBarChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['绿色型小微企业当天发放贷款年化利率', '绿色型小微企业当天发放贷款金额'],
          type: 'scroll'
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
          boundaryGap: ['20%', '20%'],
          data: this.time
        },
        yAxis: [
          {
            type: 'value',
            yAxisIndex: 0
          },
          {
            type: 'value',
            yAxisIndex: 1
          }
        ],
        series: [
          {
            name: '绿色型小微企业当天发放贷款年化利率',
            type: 'bar',
            barWidth: '30%',
            data: [201, 202, 203, 264, 206, 102, 209],
            yAxisIndex: 0
          },
          {
            name: '绿色型小微企业当天发放贷款金额',
            type: 'line',
            data: [371, 372, 373, 374, 375, 376, 377],
            smooth: true,
            yAxisIndex: 1
          }
        ]
      };
      this.greenBarChart.setOption(option);
    },
    initBussnessChart() {
      this.businessChart = echarts.init(document.getElementById('businessChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['当天吸收的定期存款金额', '当天存款平均利率']
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
          data: ['1990', '1994', '1998', '2002', '2006', '2010']
        },
        yAxis: [
          {
            type: 'value',
            name: '当天吸收的定期存款金额',
            position: 'left',  // 左Y轴
            axisLabel: {
              formatter: '{value} 亿'
            }
          },
          {
            type: 'value',
            name: '当天存款平均利率',
            position: 'right',  // 右Y轴
            axisLabel: {
              formatter: '{value} %'
            }
          }
        ],
        series: [
          {
            name: '当天吸收的定期存款金额',
            type: 'line',
            data: [16, 32, 21, 34, 39, 47],
            yAxisIndex: 0,
            smooth: true
          },
          {
            name: '当天存款平均利率',
            type: 'line',
            data: [0.57, 0.64, 0.53, 0.47, 0.58, 0.69],
            yAxisIndex: 1,
            smooth: true
          }
        ]
      };
      this.businessChart.setOption(option);
    },
    initOrgChart() {
      this.orgChart = echarts.init(document.getElementById('orgChart'));
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {},
        grid: {
          left: '3%',
          right: '10%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          boundaryGap: [0, 0.01],
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'category',
          data: ['公司企业客户', '证券业金融机构', '金融控股公司', '保险业机构', '银行业金融机构', '其他']
        },
        series: [
          {
            name: '同业负债合同余额',
            type: 'bar',
            data: [18203, 23489, 29034, 104970, 131744, 630230]
          },
          {
            name: '同业资产合同余额',
            type: 'bar',
            data: [19325, 23438, 31000, 121594, 134141, 681807]
          }
        ]
      };
      this.orgChart.setOption(option);
    },
    initInvestChart() {
      this.investChart = echarts.init(document.getElementById('investChart'));
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          show: true,
          position: 'bottom',
          bottom: '0',
          type: 'scroll'
        },
        series: [
          {
            name: '投资业务结构',
            type: 'pie',
            radius: '70%',
            data: [
              { value: 82.78, name: 'AA基金' },
              { value: 5.27, name: 'BB基金' },
              { value: 3.29, name: 'CC基金' },
              { value: 8.45, name: 'DD基金' }
            ],
            label: {
              formatter: '{c} %',
              position: 'inside'
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
      this.investChart.setOption(option);
    },
    initBondChart() {
      this.bondChart = echarts.init(document.getElementById('bondChart'));
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          show: true,
          position: 'bottom',
          bottom: '0',
          type: 'scroll'
        },
        series: [
          {
            name: '债券投资结构',
            type: 'pie',
            radius: '70%',
            data: [
              { value: 38.24, name: '商业性投资' },
              { value: 28.53, name: '企业型投资' },
              { value: 15.23, name: '个体投资' },
              { value: 10.32, name: 'XX投资' }
            ],
            label: {
              formatter: '{c} %',
              position: 'inside'
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
      this.bondChart.setOption(option);
    },
    handleResize() {
      this.badLoanChart.resize()
      this.estatePieChart.resize()
      this.estateLineChart.resize()
      this.badLineChart.resize()
      this.riskLineChart1.resize()
      this.riskLineChart2.resize()
      if(this.activeName === 'inclusive') {
        this.inclusiveLineChart.resize()
        this.inclusiveBarChart.resize()
      }else if(this.activeName === 'technology') {
        this.technologyLineChart.resize()
        this.technologyBarChart.resize()
      }else {
        this.greenLineChart.resize()
        this.greenBarChart.resize()
      }
      this.businessChart.resize()
      this.orgChart.resize()
      this.investChart.resize()
      this.bondChart.resize()
    }
  }
}
</script>
