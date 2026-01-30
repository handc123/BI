<template>
  <div class="app-container">
    <div class="display-flex">
      <el-card class="card-box" style="width: 100%;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-tickets icon-title"></div>
            1104指标对比偏离度整体情况
          </span>
        </div>
        <div class="display-flex">
          <div id="over2perChart" style="width: 50%; height: 450px"></div>
          <div id="noDeviationChart" style="width: 50%; height: 450px"></div>
        </div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%;">
        <div slot="header">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            地区1104指标对比质量排名
          </span>
        </div>
        <el-table :data="compareData">
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
          <el-table-column label="偏离率低于2%指标（含无偏离）占比" align="center" prop="deviationPercent">
            <template header-align="center">
              <div class="bolder-text">偏离率低于2%指标（含无偏离）占比</div>
            </template>
            <template slot-scope="scope">
              <div class="bold-text">{{ scope.row.deviationPercent }}</div>
            </template>
          </el-table-column>
          <el-table-column label="无偏离指标占比" align="center" prop="noDeviationPercent"></el-table-column>
        </el-table>
      </el-card>
      <el-card class="card-box" style="width: 50%">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-tickets icon-title"></div>
            指标偏离度情况
          </span>
          <div class="display-flex alicenter">地区
            <el-select v-model="areaSelected" class="margin-left-10">
              <el-option
                v-for="(item, index) in areaOptions"
                :key="index"
                :label="item"
                :value="item">
              </el-option>
            </el-select>
          </div>
        </div>
        <div id="stackBarChart" style="width: 100%; height: 450px"></div>
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            行社无偏离指标数排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('行社无偏离指标数排名')">更多</el-button>
        </div>
        <el-table :data="branchIndexNumData" style="height: 420px;" class="overflow-true">
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
      </el-card>
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            行社偏离度通过指标数排名
          </span>
          <el-button class="type-text-button" type="text"  @click="handleDialog('行社偏离度通过指标数排名')">更多</el-button>
        </div>
        <el-table :data="branchIndexNumData" style="height: 420px;" class="overflow-true">
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
      </el-card>
    </div>
    <div class="display-flex">
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            指标偏离度排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('指标偏离度排名')">更多</el-button>
        </div>
        <el-table :data="indexDeviationData" style="height: 420px;" class="overflow-true">
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
      </el-card>
      <el-card class="card-box" style="width: 50%;">
        <div slot="header" class="display-flex alicenter justify-between">
          <span class="head-title display-flex alicenter">
            <div class="el-icon-s-data icon-title"></div>
            行社提升指标数排名
          </span>
          <el-button class="type-text-button" type="text" @click="handleDialog('行社提升指标数排名')">更多</el-button>
        </div>
        <el-table :data="branchImproveNumData" style="height: 420px;" class="overflow-true">
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
  data() {
    return {
      compareData:[
        {
          areaName: "杭州",
          rank: "+2",
          deviationPercent: "60.87%",
          noDeviationPercent: "39.13%"
        },
        {
          areaName: "宁波",
          rank: "+1",
          deviationPercent: "45.67%",
          noDeviationPercent: "14.33%"
        },
        {
          areaName: "嘉兴",
          rank: "-1",
          deviationPercent: "50.87%",
          noDeviationPercent: "89.13%"
        },
        {
          areaName: "温州",
          rank: "-2",
          deviationPercent: "60.87%",
          noDeviationPercent: "69.13%"
        },
        {
          areaName: "丽水",
          rank: "-",
          deviationPercent: "20.87%",
          noDeviationPercent: "79.13%"
        }
      ],
      branchIndexNumData: [
        {
          orgName: '杭州联合银行',
          indexNum: '87',
          rank: '+1'
        },
        {
          orgName: '萧山农商银行',
          indexNum: '56',
          rank: '-1'
        }
      ],
      indexDeviationData: [
        {
          orgName: '杭州联合银行',
          indexName: '指标1',
          deviationPercent: '60.87%'
        },
        {
          orgName: '杭州联合银行',
          indexName: '指标2',
          deviationPercent: '45.67%'
        },
        {
          orgName: '杭州联合银行',
          indexName: '指标3',
          deviationPercent: '50.87%'
        }
      ],
      branchImproveNumData: [
        {
          orgName: '杭州联合银行',
          improveIndexNum: '3'
        },
        {
          orgName: '余杭农商银行',
          improveIndexNum: '5'
        },
        {
          orgName: '萧山农商银行',
          improveIndexNum: '4'
        },
        {
          orgName: '杭州联合银行',
          improveIndexNum: '3'
        },
        {
          orgName: '余杭农商银行',
          improveIndexNum: '5'
        },
        {
          orgName: '萧山农商银行',
          improveIndexNum: '4'
        },
        {
          orgName: '杭州联合银行',
          improveIndexNum: '3'
        },
        {
          orgName: '余杭农商银行',
          improveIndexNum: '5'
        },
        {
          orgName: '萧山农商银行',
          improveIndexNum: '4'
        }
      ],
      // 地区下拉框内容初始化
      areaSelected: '',
      areaOptions: ['杭州', '嘉兴', '台州', '温州', '丽水'],
      //  各图表初始化
      over2perChart: null,
      noDeviationChart: null,
      stackBarChart: null,
      // 弹窗名称
      title: '',
      visual: false
    }
  },
  mounted() {
    this.initOver2perChart();
    this.initNoDeviationChart();
    this.initStackBarChart();
    // 自适应浏览器大小
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    // 组件销毁前销毁图表实例
    if (this.over2perChart) {
      this.over2perChart.dispose();
      this.over2perChart = null;
    }
    if (this.noDeviationChart) {
      this.noDeviationChart.dispose();
      this.noDeviationChart = null;
    }
    if (this.stackBarChart) {
      this.stackBarChart.dispose();
      this.stackBarChart = null;
    }
    // 移除监听
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initOver2perChart() {
      this.over2perChart = echarts.init(document.getElementById('over2perChart'));
      var option = {
        title: {
          text: '指标排名-指标偏离度大于2%'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['指标1', '指标2', '指标3', '指标4', '指标5', '指标6', '指标7']
        },
        yAxis: {
          type: 'value',
          name: '行社数'
        },
        series: [
          {
            data: [78, 72, 71, 71, 66, 59, 42],
            type: 'bar',
            barWidth: '30%',
            label: {
              show: true,          // 显示标签
              position: 'top',     // 位置在顶部
              formatter: '{c}',    // 标签内容为数据值
              color: '#333',       // 标签颜色
              fontSize: 12         // 字体大小
            }
          }
        ]
      }
      this.over2perChart.setOption(option);
    },
    initNoDeviationChart() {
      this.noDeviationChart = echarts.init(document.getElementById('noDeviationChart'));
      var option = {
        title: {
          text: '指标排名-指标无偏离'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['指标1', '指标2', '指标3', '指标4', '指标5', '指标6', '指标7']
        },
        yAxis: {
          type: 'value',
          name: '行社数'
        },
        series: [
          {
            data: [80, 75, 75, 71, 60, 59, 42],
            type: 'bar',
            barWidth: '30%',
            label: {
              show: true,          // 显示标签
              position: 'top',     // 位置在顶部
              formatter: '{c}',    // 标签内容为数据值
              color: '#333',       // 标签颜色
              fontSize: 12         // 字体大小
            }
          }
        ]
      }
      this.noDeviationChart.setOption(option);
    },
    initStackBarChart() {
      this.stackBarChart = echarts.init(document.getElementById('stackBarChart'));
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
        grid: {
          left: '3%',
          right: '12%',
          bottom: '10%',
          containLabel: true
        },
        legend: {
          data: ['无偏离指标', '偏离度低于2%指标', '偏离度高于2%指标']
        },
        xAxis: {
          type: 'value',
          name: '指标数量'
        },
        yAxis: {
          type: 'category',
          data: ['行社1', '行社2', '行社3', '行社4', '行社5', '行社6', '行社7']
        },
        series: [
          {
            name: '无偏离指标',
            type: 'bar',
            stack: '总量',
            data: [120, 132, 101, 134, 210, 230, 210],
            barWidth: '30%'
          },
          {
            name: '偏离度低于2%指标',
            type: 'bar',
            stack: '总量',
            data: [82, 93, 90, 130, 120, 130, 120],
            barWidth: '30%'
          },
          {
            name: '偏离度高于2%指标',
            type: 'bar',
            stack: '总量',
            data: [22, 33, 44, 30, 40, 30, 20],
            barWidth: '30%'
          }
        ]
      }
      this.stackBarChart.setOption(option);
    },
    handleResize() {
      this.over2perChart.resize()
      this.noDeviationChart.resize()
      this.stackBarChart.resize()
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
