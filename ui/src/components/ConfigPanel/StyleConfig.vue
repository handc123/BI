<template>
  <div class="style-config">
    <!-- 图表样式配置 -->
    <div v-if="targetType === 'component'" class="chart-style">
      <el-form :model="styleConfig" label-width="80px" size="small">
        <!-- 主题预设 -->
        <el-form-item label="主题预设">
          <el-select v-model="styleConfig.themePreset" placeholder="选择主题" @change="applyThemePreset" style="width: 100%">
            <el-option label="默认主题" value="default" />
            <el-option label="商务蓝" value="business" />
            <el-option label="科技感" value="tech" />
            <el-option label="清新绿" value="fresh" />
            <el-option label="活力橙" value="vitality" />
            <el-option label="优雅紫" value="elegant" />
            <el-option label="暗夜模式" value="dark" />
          </el-select>
        </el-form-item>

        <!-- 颜色配置 -->
        <el-form-item label="配色方案">
          <el-select v-model="styleConfig.colorScheme" style="width: 100%">
            <el-option label="默认" value="default" />
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

        <!-- 动画效果 -->
        <el-divider>动画效果</el-divider>

        <el-form-item label="启用动画">
          <el-switch v-model="styleConfig.animation.enabled" />
        </el-form-item>

        <el-form-item v-if="styleConfig.animation.enabled" label="动画时长">
          <el-input-number
            v-model="styleConfig.animation.duration"
            :min="300"
            :max="3000"
            :step="100"
            size="small"
          />
          <span class="unit">ms</span>
        </el-form-item>

        <el-form-item v-if="styleConfig.animation.enabled" label="缓动效果">
          <el-select v-model="styleConfig.animation.easing">
            <el-option label="线性" value="linear" />
            <el-option label="匀加速" value="cubicIn" />
            <el-option label="匀减速" value="cubicOut" />
            <el-option label="匀加速减速" value="cubicInOut" />
            <el-option label="弹性" value="elasticOut" />
            <el-option label="回弹" value="backOut" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- 仪表板样式配置 -->
    <div v-else class="dashboard-style">
      <el-form :model="styleConfig" label-width="90px" size="small">
        <!-- 主题预设 -->
        <el-form-item label="主题预设">
          <el-select v-model="styleConfig.themePreset" placeholder="选择主题" @change="applyDashboardTheme" style="width: 100%">
            <el-option label="默认浅色" value="default-light" />
            <el-option label="商务深色" value="business-dark" />
            <el-option label="科技蓝调" value="tech-blue" />
            <el-option label="清新自然" value="fresh-green" />
            <el-option label="简约黑白" value="minimal" />
          </el-select>
        </el-form-item>

        <!-- 背景配置 -->
        <el-divider>背景</el-divider>

        <el-form-item label="背景类型">
          <el-radio-group v-model="styleConfig.background.type">
            <el-radio label="color">纯色</el-radio>
            <el-radio label="gradient">渐变</el-radio>
            <el-radio label="image">图片</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="styleConfig.background.type === 'color'">
          <el-form-item label="背景颜色">
            <el-color-picker v-model="styleConfig.background.value" />
          </el-form-item>
        </template>

        <template v-if="styleConfig.background.type === 'gradient'">
          <el-form-item label="渐变类型">
            <el-select v-model="styleConfig.background.gradientType">
              <el-option label="线性渐变" value="linear" />
              <el-option label="径向渐变" value="radial" />
            </el-select>
          </el-form-item>

          <el-form-item label="起始颜色">
            <el-color-picker v-model="styleConfig.background.startColor" />
          </el-form-item>

          <el-form-item label="结束颜色">
            <el-color-picker v-model="styleConfig.background.endColor" />
          </el-form-item>

          <el-form-item v-if="styleConfig.background.gradientType === 'linear'" label="渐变方向">
            <el-select v-model="styleConfig.background.direction">
              <el-option label="从上到下" value="180" />
              <el-option label="从下到上" value="0" />
              <el-option label="从左到右" value="90" />
              <el-option label="从右到左" value="270" />
              <el-option label="左上到右下" value="135" />
              <el-option label="右上到左下" value="225" />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item v-if="styleConfig.background.type === 'image'" label="背景图片">
          <el-input
            v-model="styleConfig.background.value"
            placeholder="请输入图片URL"
          />
        </el-form-item>

        <el-form-item label="背景透明度">
          <el-slider
            v-model="styleConfig.background.opacity"
            :min="0"
            :max="1"
            :step="0.05"
          />
        </el-form-item>

        <!-- 卡片样式 -->
        <el-divider>卡片样式</el-divider>

        <el-form-item label="卡片阴影">
          <el-switch v-model="styleConfig.card.enabled" />
        </el-form-item>

        <template v-if="styleConfig.card.enabled">
          <el-form-item label="阴影类型">
            <el-select v-model="styleConfig.card.shadowType">
              <el-option label="轻微" value="light" />
              <el-option label="标准" value="default" />
              <el-option label="明显" value="strong" />
            </el-select>
          </el-form-item>

          <el-form-item label="阴影颜色">
            <el-color-picker v-model="styleConfig.card.shadowColor" />
          </el-form-item>

          <el-form-item label="圆角大小">
            <el-slider
              v-model="styleConfig.card.borderRadius"
              :min="0"
              :max="20"
            />
          </el-form-item>
        </template>

        <!-- 边距配置 -->
        <el-divider>布局</el-divider>

        <el-form-item label="外边距">
          <div class="margin-inputs">
            <el-input-number
              v-model="styleConfig.margin.top"
              :min="0"
              :max="100"
              size="mini"
              placeholder="上"
            />
            <el-input-number
              v-model="styleConfig.margin.right"
              :min="0"
              :max="100"
              size="mini"
              placeholder="右"
            />
            <el-input-number
              v-model="styleConfig.margin.bottom"
              :min="0"
              :max="100"
              size="mini"
              placeholder="下"
            />
            <el-input-number
              v-model="styleConfig.margin.left"
              :min="0"
              :max="100"
              size="mini"
              placeholder="左"
            />
          </div>
        </el-form-item>

        <!-- 网格配置 -->
        <el-divider>辅助线</el-divider>

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

        <!-- 响应式配置 -->
        <el-divider>响应式</el-divider>

        <el-form-item label="启用响应式">
          <el-switch v-model="styleConfig.responsive.enabled" />
        </el-form-item>

        <template v-if="styleConfig.responsive.enabled">
          <el-form-item label="断点设置">
            <div class="breakpoint-inputs">
              <el-input
                v-model="styleConfig.responsive.breakpoints.sm"
                placeholder="SM"
              >
                <template slot="prepend">SM</template>
              </el-input>
              <el-input
                v-model="styleConfig.responsive.breakpoints.md"
                placeholder="MD"
              >
                <template slot="prepend">MD</template>
              </el-input>
              <el-input
                v-model="styleConfig.responsive.breakpoints.lg"
                placeholder="LG"
              >
                <template slot="prepend">LG</template>
              </el-input>
            </div>
          </el-form-item>
        </template>
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
        themePreset: 'default',
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
        animation: {
          enabled: true,
          duration: 1000,
          easing: 'cubicInOut'
        },
        // 仪表板样式
        themePreset: 'default-light',
        background: {
          type: 'color',
          value: '#ffffff',
          opacity: 1,
          gradientType: 'linear',
          startColor: '#ffffff',
          endColor: '#f0f2f5',
          direction: 180
        },
        card: {
          enabled: true,
          shadowType: 'default',
          shadowColor: 'rgba(0, 0, 0, 0.1)',
          borderRadius: 8
        },
        margin: {
          top: 20,
          right: 20,
          bottom: 20,
          left: 20
        },
        gridSize: 10,
        showGrid: true,
        responsive: {
          enabled: false,
          breakpoints: {
            sm: '768px',
            md: '1024px',
            lg: '1520px'
          }
        }
      },
      emitEnabled: true  // 控制是否触发 emit，防止循环更新
    }
  },
  computed: {
    // 主题预设配置
    themePresets() {
      return {
        // 图表主题
        default: {
          colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'],
          titleColor: '#333333'
        },
        business: {
          colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'],
          titleColor: '#1a1a1a'
        },
        tech: {
          colors: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
          titleColor: '#00ff00',
          fontFamily: 'SimHei'
        },
        fresh: {
          colors: ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399'],
          titleColor: '#2c3e50'
        },
        vitality: {
          colors: ['#FF6709', '#FFA500', '#FFD700', '#FFEC8B', '#FFE4B5'],
          titleColor: '#ff6709'
        },
        elegant: {
          colors: ['#9a60b4', '#ea7ccc', '#5470c6', '#91cc75', '#fac858'],
          titleColor: '#9a60b4'
        },
        dark: {
          colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'],
          titleColor: '#ffffff',
          backgroundColor: '#1a1a1a'
        },
        // 仪表板主题
        'default-light': {
          background: { type: 'color', value: '#ffffff' },
          card: { enabled: true, shadowType: 'light', borderRadius: 8 }
        },
        'business-dark': {
          background: { type: 'color', value: '#1a1a2e' },
          card: { enabled: true, shadowType: 'strong', borderRadius: 4 }
        },
        'tech-blue': {
          background: { type: 'gradient', gradientType: 'linear', startColor: '#1e3c72', endColor: '#2a5298', direction: 135 },
          card: { enabled: true, shadowType: 'default', borderRadius: 12 }
        },
        'fresh-green': {
          background: { type: 'gradient', gradientType: 'linear', startColor: '#f0f9f0', endColor: '#d4edda', direction: 180 },
          card: { enabled: false, shadowType: 'light', borderRadius: 16 }
        },
        'minimal': {
          background: { type: 'color', value: '#fafafa' },
          card: { enabled: false, shadowType: 'light', borderRadius: 0 }
        }
      }
    }
  },
  watch: {
    target: {
      handler(val, oldVal) {
        // 只在 target 引用变化时重新初始化，避免深度监听导致循环
        if (val && val !== oldVal) {
          this.emitEnabled = false  // 禁用 emit
          this.initializeConfig()
          this.$nextTick(() => {
            this.emitEnabled = true  // 恢复 emit
          })
        }
      },
      immediate: true
      // 移除 deep: true，避免循环
    },
    styleConfig: {
      handler(val) {
        // 深度监听，任何配置变化都触发更新（但在初始化时不触发）
        if (this.emitEnabled) {
          this.$emit('change', val)
        }
      },
      deep: true
    }
  },
  mounted() {
    // 在 mounted 中初始化
    this.$nextTick(() => {
      this.initializeConfig()
    })
  },
  methods: {
    initializeConfig() {
      const defaultConfig = {
        // 图表默认配置
        themePreset: 'default',
        colorScheme: 'default',
        colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
        fontFamily: 'Microsoft YaHei',
        titleFontSize: 16,
        titleColor: '#333333',
        titleFontWeight: 'bold',
        legend: { show: true, position: 'top', align: 'center' },
        xAxis: { show: true, name: '', labelRotate: 0 },
        yAxis: { show: true, name: '', position: 'left' },
        grid: { top: 40, right: 10, bottom: 40, left: 50 },
        animation: { enabled: true, duration: 1000, easing: 'cubicInOut' },
        // 仪表板默认配置
        themePreset: 'default-light',
        background: {
          type: 'color',
          value: '#ffffff',
          opacity: 1,
          gradientType: 'linear',
          startColor: '#ffffff',
          endColor: '#f0f2f5',
          direction: 180
        },
        card: { enabled: true, shadowType: 'default', shadowColor: 'rgba(0, 0, 0, 0.1)', borderRadius: 8 },
        margin: { top: 20, right: 20, bottom: 20, left: 20 },
        gridSize: 10,
        showGrid: true,
        responsive: {
          enabled: false,
          breakpoints: { sm: '768px', md: '1024px', lg: '1520px' }
        }
      }

      const val = this.target
      if (val) {
        if (this.targetType === 'component' && val.styleConfig) {
          let mergedConfig = this.deepMerge(defaultConfig, val.styleConfig)
          this.styleConfig = mergedConfig
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

    // 应用主题预设（图表）
    applyThemePreset() {
      console.log('[StyleConfig] applyThemePreset 被调用:', {
        themePreset: this.styleConfig.themePreset,
        targetType: this.targetType,
        emitEnabled: this.emitEnabled
      })

      const preset = this.themePresets[this.styleConfig.themePreset]
      console.log('[StyleConfig] 主题配置:', preset)

      if (preset && this.targetType === 'component') {
        // 使用 $set 确保响应式更新
        if (preset.colors) {
          console.log('[StyleConfig] 更新颜色:', preset.colors)
          this.$set(this.styleConfig, 'colors', [...preset.colors])
        }
        if (preset.titleColor) {
          console.log('[StyleConfig] 更新标题颜色:', preset.titleColor)
          this.$set(this.styleConfig, 'titleColor', preset.titleColor)
        }
        if (preset.fontFamily) {
          console.log('[StyleConfig] 更新字体:', preset.fontFamily)
          this.$set(this.styleConfig, 'fontFamily', preset.fontFamily)
        }

        console.log('[StyleConfig] 更新后的 styleConfig:', this.styleConfig)

        // 手动触发更新
        this.$nextTick(() => {
          console.log('[StyleConfig] 触发 change 事件')
          this.$emit('change', this.styleConfig)
        })
      }
    },

    // 应用主题预设（仪表板）
    applyDashboardTheme() {
      const preset = this.themePresets[this.styleConfig.themePreset]
      if (preset && this.targetType === 'dashboard') {
        // 使用 $set 确保响应式更新
        if (preset.background) {
          this.$set(this.styleConfig, 'background', { ...preset.background })
        }
        if (preset.card) {
          this.$set(this.styleConfig, 'card', { ...preset.card })
        }
        // 手动触发更新
        this.$nextTick(() => {
          this.$emit('change', this.styleConfig)
        })
      }
    },

    addColor() {
      this.styleConfig.colors.push('#5470c6')
    },
    removeColor(index) {
      this.styleConfig.colors.splice(index, 1)
    },

    // 深度合并对象
    deepMerge(target, source) {
      const result = JSON.parse(JSON.stringify(target))

      for (const key in source) {
        if (source.hasOwnProperty(key)) {
          if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
            result[key] = this.deepMerge(result[key] || {}, source[key])
          } else {
            result[key] = JSON.parse(JSON.stringify(source[key]))
          }
        }
      }

      return result
    }
  }
}
</script>

<style scoped lang="scss">
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

.margin-inputs {
  display: flex;
  gap: 8px;
}

.breakpoint-inputs {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 确保表单项不超出宽度 */
.style-config >>> .el-form-item {
  margin-bottom: 16px;
}

.style-config >>> .el-form-item__content {
  width: calc(100% - 90px);
}

.style-config >>> .el-select,
.style-config >>> .el-input {
  width: 100%;
}

.style-config >>> .el-input-number {
  width: 120px;
}
</style>
