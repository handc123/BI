<template>
  <div class="app-container">
    <div class="display-flex">
      <el-card class="card-box" style="width: 60%">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-pie-chart icon-title"></div>
            一键转1104表格通过整体情况
          </span>
        </div>
        <div id="1104PieChart" style="width: 100%; height: 450px"></div>
      </el-card>
      <el-card class="card-box" style="width: 40%">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            表格通过数排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('表格通过数排名')">更多</el-button>
        </div>
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
          <el-table-column label="通过数" align="center" prop="passNum" width="60px"></el-table-column>
        </el-table>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 30%">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            地区表格通过率排名
          </span>
        </div>
        <el-table :data="basicPercentData" style="height: 420px;" class="overflow-true">
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
      </el-card>
      <el-card class="card-box" style="width: 70%">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-tickets icon-title"></div>
            未通过表格和数据记录项
          </span>
          <div class="display-flex alicenter">
            <p style="margin: 0;">地区</p>
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
        <div>
          <div id="failedTableChart" class="card-boxs" style="width: 100%; height: 205px;"></div>
          <div id="failedDataChart" class="card-boxs" style="width: 100%; height: 205px;"></div>
        </div>
      </el-card>
    </div>
    <Dialog :title="title" :visual="visual" @update:visual="handleVisualUpdate" ></Dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import Dialog from '../../component/dialog.vue';
export default{
  components: {
    Dialog
  },
  data() {
    return {
      data: [
        {name: '行社通过率0-20%（不含）', value: 0},
        {name: '行社通过率20-40%（不含）', value: 0},
        {name: '行社通过率40-60%（不含）', value: 1},
        {name: '行社通过率60-80%（不含）', value: 1},
        {name: '行社通过率80-100%（不含）', value: 0},
        {name: '行社通过率100%', value: 0},
      ],
      basicData: [
        {
          orgName: '杭州联合银行',
          passNum: '87',
          rank: '-'
        },
        {
          orgName: '萧山农商银行',
          passNum: '56',
          rank: '+1'
        },
        {
          orgName: '余杭农商银行',
          passNum: '45',
          rank: '-1'
        }
      ],
      basicPercentData: [
        {
          orgName: '杭州联合银行',
          passRate: '87%',
          rank: '-'
        },
        {
          orgName: '萧山农商银行',
          passRate: '56%',
          rank: '+1'
        },
        {
          orgName: '余杭农商银行',
          passRate: '45%',
          rank: '-1'
        },
        {
          orgName: '杭州联合银行',
          passRate: '87%',
          rank: '-'
        },
        {
          orgName: '萧山农商银行',
          passRate: '56%',
          rank: '+1'
        },
        {
          orgName: '余杭农商银行',
          passRate: '45%',
          rank: '-1'
        },
        {
          orgName: '杭州联合银行',
          passRate: '87%',
          rank: '-'
        },
        {
          orgName: '萧山农商银行',
          passRate: '56%',
          rank: '+1'
        },
        {
          orgName: '余杭农商银行',
          passRate: '45%',
          rank: '-1'
        }
      ],
      // 下拉框内容初始化
      area: '',
      areaOptions: ['杭州', '嘉兴', '台州'],
      // 图表初始化
      pieChart: null,
      failedTableChart: null,
      failedDataChart: null,
      // 弹窗名称
      title: '',
      visual: false
    }
  },
  mounted() {
    this.initPieChart()
    this.initFailedTableChart()
    this.initFailedDataChart()
    // 自适应浏览器大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前销毁图表实例
    if(this.pieChart) {
      this.pieChart.dispose()
      this.pieChart = null
    }
    if(this.failedTableChart) {
      this.failedTableChart.dispose()
      this.failedTableChart = null
    }
    if(this.failedDataChart) {
      this.failedDataChart.dispose()
      this.failedDataChart = null
    }
    // 移除监听
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initPieChart() {
      const chartDom = document.getElementById('1104PieChart');
      this.pieChart = echarts.init(chartDom);
      const data = this.data
      const option = {
        tooltip: {
          trigger: 'item'
        },
        // 饼图中间文字
        title: {
          text: '2',
          subtext: '一键转1104表格总数',
          left: '29%',
          top: '40%',
          textAlign: 'center',
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
          right: '3%',
          padding: [20, 10],
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
            radius: ['40%', '60%'],
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
      this.pieChart.setOption(option)
      // 创建观察者
      const observer = new ResizeObserver(entries => {
        for (const entry of entries) {
          const { width } = entry.contentRect;

          this.pieChart.setOption({
            title: {
              left: width < 500 ? '50%': '29%',
              textStyle: {
                fontSize: Math.max(12, width * 0.07),
                align:'center'
              },
              subtextStyle: {
                fontSize: Math.max(10, width * 0.025)
              }
            },
            legend: {
              orient: width < 500 ? 'horizontal': 'vertical',
              left: width < 500 ? 'center': '60%',
              bottom: width < 500 ? '3%': 'center',
              type: width < 500 ? 'scroll' : '',
              formatter: width < 600 ? '': function (name) {
                let singleData = data.filter(function(item){
                    return item.name == name
                })
                return `${name}     ${singleData[0].value}`
              }
            },
            series: {
              center: width < 500 ? ['50%', '50%']: ['30%', '50%']
            }
          });

          this.pieChart.resize();
        }
      });

      // 开始观察容器
      observer.observe(chartDom);
    },
    initFailedTableChart() {
      this.failedTableChart = echarts.init(document.getElementById('failedTableChart'));
      const option = {
        title: {
          text: '未通过表格'
        },
        xAxis: {
          type: 'category',
          data: ['杭州联合银行', '萧山农商银行', '余杭农商银行', '舟山农商银行', '温州农商银行', '宁波农商银行', '嘉兴农商银行']
        },
        yAxis: {
          type: 'value',
          name: '表格数'
        },
        series: [
          {
            data: [120, 200, 150, 80, 70, 110, 130],
            type: 'bar',
            barWidth: '20%'
          }
        ]
      };
      this.failedTableChart.setOption(option);
    },
    initFailedDataChart() {
      this.failedDataChart = echarts.init(document.getElementById('failedDataChart'));
      const option = {
        title: {
          text: '未通过数据项'
        },
        xAxis: {
          type: 'category',
          data: ['杭州联合银行', '萧山农商银行', '余杭农商银行', '舟山农商银行', '温州农商银行', '宁波农商银行', '嘉兴农商银行']
        },
        yAxis: {
          type: 'value',
          name: '数据项'
        },
        series: [
          {
            data: [120, 200, 150, 80, 70, 110, 130],
            type: 'bar',
            barWidth: '20%'
          }
        ]
      };
      this.failedDataChart.setOption(option);
    },
    handleResize() {
      this.pieChart.resize()
      this.failedTableChart.resize()
      this.failedDataChart.resize()
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
