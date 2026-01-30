<template>
  <div class="style-config">
    <!-- 图表样式配置 -->
    <div v-if="targetType === 'component'" class="chart-style">
      <el-form :model="styleConfig" label-width="80px" size="small">
        <!-- 颜色配置 -->
        <el-form-item label="配色方案">
          <el-select v-model="styleConfig.colorScheme" style="width: 100%">
            <el-option label="默认" value="default" />
            <el-option label="科技蓝" value="blue" />
            <el-option label="活力橙" value="orange" />
            <el-option label="清新绿" value="green" />
            <el-option label="优雅紫" value="purple" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="styleConfig.colorScheme === 'custom'" label="自定义颜色">
          <div class="color-picker-list">
            <div
              v-for="(color, index) in styleConfig.colors"
              :key="index"
              class="color-picker-item"
            >
              <el-color-picker v-model="styleConfig.colors[index]" />
              <el-button
                v-if="styleConfig.colors.length > 1"
                type="text"
                icon="el-icon-delete"
                @click="removeColor(index)"
              />
            </div>
          </div>
          <el-button
            type="text"
            icon="el-icon-plus"
            size="small"
            @click="addColor"
          >
            添加颜色
          </el-button>
        </el-form-item>

        <!-- 字体配置 -->
        <el-divider>字体</el-divider>

        <el-form-item label="字体系列">
          <el-select v-model="styleConfig.fontFamily" style="width: 100%">
            <el-option label="微软雅黑" value="Microsoft YaHei" />
            <el-option label="宋体" value="SimSun" />
            <el-option label="黑体" value="SimHei" />
            <el-option label="Arial" value="Arial" />
            <el-option label="Helvetica" value="Helvetica" />
          </el-select>
        </el-form-item>

        <el-form-item label="标题字号">
          <el-input-number
            v-model="styleConfig.titleFontSize"
            :min="12"
            :max="48"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="标题颜色">
          <el-color-picker v-model="styleConfig.titleColor" />
        </el-form-item>

        <el-form-item label="标题粗细">
          <el-select v-model="styleConfig.titleFontWeight">
            <el-option label="正常" value="normal" />
            <el-option label="粗体" value="bold" />
            <el-option label="更粗" value="bolder" />
          </el-select>
        </el-form-item>

        <!-- 图例配置 -->
        <el-divider>图例</el-divider>

        <el-form-item label="显示图例">
          <el-switch v-model="styleConfig.legend.show" />
        </el-form-item>

        <el-form-item v-if="styleConfig.legend.show" label="图例位置">
          <el-select v-model="styleConfig.legend.position">
            <el-option label="顶部" value="top" />
            <el-option label="底部" value="bottom" />
            <el-option label="左侧" value="left" />
            <el-option label="右侧" value="right" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="styleConfig.legend.show" label="图例对齐">
          <el-select v-model="styleConfig.legend.align">
            <el-option label="左对齐" value="left" />
            <el-option label="居中" value="center" />
            <el-option label="右对齐" value="right" />
          </el-select>
        </el-form-item>

        <!-- 坐标轴配置 -->
        <el-divider>坐标轴</el-divider>

        <el-form-item label="显示X轴">
          <el-switch v-model="styleConfig.xAxis.show" />
        </el-form-item>

        <el-form-item v-if="styleConfig.xAxis.show" label="X轴名称">
          <el-input v-model="styleConfig.xAxis.name" placeholder="X轴名称" />
        </el-form-item>

        <el-form-item v-if="styleConfig.xAxis.show" label="X轴标签旋转">
          <el-input-number
            v-model="styleConfig.xAxis.labelRotate"
            :min="-90"
            :max="90"
            size="small"
          />
          <span class="unit">度</span>
        </el-form-item>

        <el-form-item label="显示Y轴">
          <el-switch v-model="styleConfig.yAxis.show" />
        </el-form-item>

        <el-form-item v-if="styleConfig.yAxis.show" label="Y轴名称">
          <el-input v-model="styleConfig.yAxis.name" placeholder="Y轴名称" />
        </el-form-item>

        <el-form-item v-if="styleConfig.yAxis.show" label="Y轴位置">
          <el-select v-model="styleConfig.yAxis.position">
            <el-option label="左侧" value="left" />
            <el-option label="右侧" value="right" />
          </el-select>
        </el-form-item>

        <!-- 网格配置 -->
        <el-divider>网格</el-divider>

        <el-form-item label="上边距">
          <el-input-number
            v-model="styleConfig.grid.top"
            :min="0"
            :max="200"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="右边距">
          <el-input-number
            v-model="styleConfig.grid.right"
            :min="0"
            :max="200"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="下边距">
          <el-input-number
            v-model="styleConfig.grid.bottom"
            :min="0"
            :max="200"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="左边距">
          <el-input-number
            v-model="styleConfig.grid.left"
            :min="0"
            :max="200"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>
      </el-form>
    </div>

    <!-- 仪表板样式配置 -->
    <div v-else class="dashboard-style">
      <el-form :model="styleConfig" label-width="80px" size="small">
        <!-- 主题配置 -->
        <el-form-item label="主题">
          <el-radio-group v-model="styleConfig.theme">
            <el-radio label="light">浅色</el-radio>
            <el-radio label="dark">深色</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 背景配置 -->
        <el-divider>背景</el-divider>

        <el-form-item label="背景类型">
          <el-radio-group v-model="styleConfig.background.type">
            <el-radio label="color">纯色</el-radio>
            <el-radio label="image">图片</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="styleConfig.background.type === 'color'" label="背景颜色">
          <el-color-picker v-model="styleConfig.background.value" />
        </el-form-item>

        <el-form-item v-if="styleConfig.background.type === 'image'" label="背景图片">
          <el-input
            v-model="styleConfig.background.value"
            placeholder="图片URL"
          />
        </el-form-item>

        <el-form-item label="背景透明度">
          <el-slider
            v-model="styleConfig.background.opacity"
            :min="0"
            :max="1"
            :step="0.1"
          />
        </el-form-item>

        <!-- 边距配置 -->
        <el-divider>边距</el-divider>

        <el-form-item label="上边距">
          <el-input-number
            v-model="styleConfig.margin.top"
            :min="0"
            :max="100"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="右边距">
          <el-input-number
            v-model="styleConfig.margin.right"
            :min="0"
            :max="100"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="下边距">
          <el-input-number
            v-model="styleConfig.margin.bottom"
            :min="0"
            :max="100"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="左边距">
          <el-input-number
            v-model="styleConfig.margin.left"
            :min="0"
            :max="100"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <!-- 网格配置 -->
        <el-divider>网格</el-divider>

        <el-form-item label="网格大小">
          <el-input-number
            v-model="styleConfig.gridSize"
            :min="5"
            :max="50"
            size="small"
          />
          <span class="unit">px</span>
        </el-form-item>

        <el-form-item label="显示网格">
          <el-switch v-model="styleConfig.showGrid" />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StyleConfig',
  props: {
    target: {
      type: Object,
      required: true
    },
    targetType: {
      type: String,
      required: true,
      validator: value => ['component', 'dashboard'].includes(value)
    }
  },
  data() {
    return {
      styleConfig: {
        // 图表样式
        colorScheme: 'default',
        colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'],
        fontFamily: 'Microsoft YaHei',
        titleFontSize: 16,
        titleColor: '#333333',
        titleFontWeight: 'bold',
        legend: {
          show: true,
          position: 'top',
          align: 'center'
        },
        xAxis: {
          show: true,
          name: '',
          labelRotate: 0
        },
        yAxis: {
          show: true,
          name: '',
          position: 'left'
        },
        grid: {
          top: 40,
          right: 10,
          bottom: 40,
          left: 50
        },
        // 仪表板样式
        theme: 'light',
        background: {
          type: 'color',
          value: '#ffffff',
          opacity: 1
        },
        margin: {
          top: 20,
          right: 20,
          bottom: 20,
          left: 20
        },
        gridSize: 10,
        showGrid: true
      }
    }
  },
  watch: {
    // 暂时完全禁用 watcher，避免无限循环
    /*
    target: {
      handler(val) {
        const defaultConfig = {
          colorScheme: 'default',
          colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'],
          fontFamily: 'Microsoft YaHei',
          titleFontSize: 16,
          titleColor: '#333333',
          titleFontWeight: 'bold',
          legend: {
            show: true,
            position: 'top',
            align: 'center'
          },
          xAxis: {
            show: true,
            name: '',
            labelRotate: 0
          },
          yAxis: {
            show: true,
            name: '',
            position: 'left'
          },
          grid: {
            top: 40,
            right: 10,
            bottom: 40,
            left: 50
          },
          theme: 'light',
          background: {
            type: 'color',
            value: '#ffffff',
            opacity: 1
          },
          margin: {
            top: 20,
            right: 20,
            bottom: 20,
            left: 20
          },
          gridSize: 10,
          showGrid: true
        }

        if (val) {
          if (this.targetType === 'component' && val.styleConfig) {
            const componentConfig = JSON.parse(JSON.stringify(val.styleConfig))
            this.styleConfig = this.deepMerge(defaultConfig, componentConfig)
          } else if (this.targetType === 'dashboard') {
            let mergedConfig = { ...defaultConfig }
            if (val.canvasConfig) {
              const canvasConfig = typeof val.canvasConfig === 'string'
                ? JSON.parse(val.canvasConfig)
                : JSON.parse(JSON.stringify(val.canvasConfig))
              mergedConfig = this.deepMerge(mergedConfig, canvasConfig)
            }
            if (val.globalStyle) {
              const globalStyle = typeof val.globalStyle === 'string'
                ? JSON.parse(val.globalStyle)
                : JSON.parse(JSON.stringify(val.globalStyle))
              mergedConfig = this.deepMerge(mergedConfig, globalStyle)
            }
            if (val.theme) {
              mergedConfig.theme = val.theme
            }
            this.styleConfig = mergedConfig
          } else {
            this.styleConfig = JSON.parse(JSON.stringify(defaultConfig))
          }
        } else {
          this.styleConfig = JSON.parse(JSON.stringify(defaultConfig))
        }
      },
      deep: false,
      immediate: false
    },
    styleConfig: {
      handler(val) {
        this.$emit('change', val)
      },
      deep: false
    }
    */
  },
  mounted() {
    // 在 mounted 中初始化，而不是在 watcher 中
    // 添加 $nextTick 确保 props 已经传递
    this.$nextTick(() => {
      this.initializeConfig()
    })
  },
  methods: {
    initializeConfig() {
      const defaultConfig = {
        colorScheme: 'default',
        colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
        fontFamily: 'Microsoft YaHei',
        titleFontSize: 16,
        titleColor: '#333333',
        titleFontWeight: 'bold',
        legend: {
          show: true,
          position: 'top',
          align: 'center'
        },
        xAxis: {
          show: true,
          name: '',
          labelRotate: 0
        },
        yAxis: {
          show: true,
          name: '',
          position: 'left'
        },
        grid: {
          top: 40,
          right: 10,
          bottom: 40,
          left: 50
        },
        theme: 'light',
        background: {
          type: 'color',
          value: '#ffffff',
          opacity: 1
        },
        margin: {
          top: 20,
          right: 20,
          bottom: 20,
          left: 20
        },
        gridSize: 10,
        showGrid: true
      }

      const val = this.target
      if (val) {
        if (this.targetType === 'component' && val.styleConfig) {
          // 使用深度合并确保所有默认值都存在
          let mergedConfig = this.deepMerge(defaultConfig, val.styleConfig)
          this.styleConfig = mergedConfig
        } else if (this.targetType === 'dashboard') {
          // 从仪表板的canvasConfig和globalStyle中加载
          let mergedConfig = { ...defaultConfig }
          if (val.canvasConfig) {
            const canvasConfig = typeof val.canvasConfig === 'string'
              ? JSON.parse(val.canvasConfig)
              : JSON.parse(JSON.stringify(val.canvasConfig))
            mergedConfig = this.deepMerge(mergedConfig, canvasConfig)
          }
          if (val.globalStyle) {
            const globalStyle = typeof val.globalStyle === 'string'
              ? JSON.parse(val.globalStyle)
              : JSON.parse(JSON.stringify(val.globalStyle))
            mergedConfig = this.deepMerge(mergedConfig, globalStyle)
          }
          if (val.theme) {
            mergedConfig.theme = val.theme
          }
          this.styleConfig = mergedConfig
        } else {
          // 没有配置，使用默认值
          this.styleConfig = JSON.parse(JSON.stringify(defaultConfig))
        }
      } else {
        // 没有配置，使用默认值
        this.styleConfig = JSON.parse(JSON.stringify(defaultConfig))
      }
    },
    addColor() {
      this.styleConfig.colors.push('#5470c6')
    },
    removeColor(index) {
      this.styleConfig.colors.splice(index, 1)
    },
    // 深度合并对象，确保嵌套对象也被正确合并
    deepMerge(target, source) {
      const result = JSON.parse(JSON.stringify(target))
      
      for (const key in source) {
        if (source.hasOwnProperty(key)) {
          if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
            // 递归合并嵌套对象
            result[key] = this.deepMerge(result[key] || {}, source[key])
          } else {
            // 直接赋值（包括数组和基本类型）
            result[key] = JSON.parse(JSON.stringify(source[key]))
          }
        }
      }
      
      return result
    }
  }
}
</script>

<style scoped>
.style-config {
  width: 400px;
  padding: 12px;
  box-sizing: border-box;
  overflow-y: auto;
}

.chart-style,
.dashboard-style {
  width: 100%;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.color-picker-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.color-picker-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 确保表单项不超出宽度 */
.style-config >>> .el-form-item {
  margin-bottom: 16px;
}

.style-config >>> .el-form-item__content {
  width: calc(100% - 80px);
}

.style-config >>> .el-select,
.style-config >>> .el-input {
  width: 100%;
}

.style-config >>> .el-input-number {
  width: 120px;
}
</style>
