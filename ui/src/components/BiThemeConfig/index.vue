<template>
  <div class="theme-config">
    <el-form :model="theme" label-width="80px" size="small">
      <el-form-item label="配色方案">
        <el-select v-model="theme.colorScheme" placeholder="请选择配色方案" style="width: 100%" @change="handleColorSchemeChange">
          <el-option label="默认" value="default">
            <div class="color-scheme-option">
              <span>默认</span>
              <div class="color-preview">
                <span class="color-dot" style="background: #409eff"></span>
                <span class="color-dot" style="background: #67c23a"></span>
                <span class="color-dot" style="background: #e6a23c"></span>
                <span class="color-dot" style="background: #f56c6c"></span>
              </div>
            </div>
          </el-option>
          <el-option label="商务蓝" value="business">
            <div class="color-scheme-option">
              <span>商务蓝</span>
              <div class="color-preview">
                <span class="color-dot" style="background: #1890ff"></span>
                <span class="color-dot" style="background: #13c2c2"></span>
                <span class="color-dot" style="background: #52c41a"></span>
                <span class="color-dot" style="background: #faad14"></span>
              </div>
            </div>
          </el-option>
          <el-option label="科技紫" value="tech">
            <div class="color-scheme-option">
              <span>科技紫</span>
              <div class="color-preview">
                <span class="color-dot" style="background: #722ed1"></span>
                <span class="color-dot" style="background: #eb2f96"></span>
                <span class="color-dot" style="background: #fa8c16"></span>
                <span class="color-dot" style="background: #13c2c2"></span>
              </div>
            </div>
          </el-option>
          <el-option label="清新绿" value="fresh">
            <div class="color-scheme-option">
              <span>清新绿</span>
              <div class="color-preview">
                <span class="color-dot" style="background: #52c41a"></span>
                <span class="color-dot" style="background: #13c2c2"></span>
                <span class="color-dot" style="background: #1890ff"></span>
                <span class="color-dot" style="background: #faad14"></span>
              </div>
            </div>
          </el-option>
          <el-option label="自定义" value="custom">
            <div class="color-scheme-option">
              <span>自定义</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="自定义颜色" v-if="theme.colorScheme === 'custom'">
        <div class="custom-colors">
          <div
            v-for="(color, index) in theme.customColors"
            :key="index"
            class="color-picker-item"
          >
            <el-color-picker v-model="theme.customColors[index]" />
            <el-button
              v-if="theme.customColors.length > 1"
              type="text"
              size="mini"
              icon="el-icon-delete"
              @click="removeCustomColor(index)"
            />
          </div>
          <el-button
            size="small"
            type="text"
            icon="el-icon-plus"
            @click="addCustomColor"
          >添加颜色</el-button>
        </div>
      </el-form-item>

      <el-divider content-position="left">背景设置</el-divider>

      <el-form-item label="背景颜色">
        <el-color-picker v-model="theme.backgroundColor" show-alpha />
      </el-form-item>

      <el-form-item label="背景图片">
        <el-radio-group v-model="theme.backgroundType">
          <el-radio label="color">纯色</el-radio>
          <el-radio label="image">图片</el-radio>
          <el-radio label="gradient">渐变</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="图片URL" v-if="theme.backgroundType === 'image'">
        <el-input v-model="theme.backgroundImage" placeholder="请输入图片URL" />
        <div class="form-tip">支持上传或输入图片URL</div>
      </el-form-item>

      <el-form-item label="渐变配置" v-if="theme.backgroundType === 'gradient'">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-color-picker v-model="theme.gradientStart" />
            <span style="margin-left: 5px">起始色</span>
          </el-col>
          <el-col :span="12">
            <el-color-picker v-model="theme.gradientEnd" />
            <span style="margin-left: 5px">结束色</span>
          </el-col>
        </el-row>
      </el-form-item>

      <el-divider content-position="left">字体设置</el-divider>

      <el-form-item label="字体">
        <el-select v-model="theme.fontFamily" placeholder="请选择字体" style="width: 100%">
          <el-option label="微软雅黑" value="Microsoft YaHei" />
          <el-option label="宋体" value="SimSun" />
          <el-option label="黑体" value="SimHei" />
          <el-option label="Arial" value="Arial" />
          <el-option label="Helvetica" value="Helvetica" />
          <el-option label="Times New Roman" value="Times New Roman" />
        </el-select>
      </el-form-item>

      <el-form-item label="字号">
        <el-slider
          v-model="theme.fontSize"
          :min="12"
          :max="24"
          :step="1"
          show-input
          :show-input-controls="false"
        />
      </el-form-item>

      <el-form-item label="字体粗细">
        <el-radio-group v-model="theme.fontWeight">
          <el-radio label="normal">正常</el-radio>
          <el-radio label="bold">加粗</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-divider content-position="left">组件样式</el-divider>

      <el-form-item label="组件圆角">
        <el-slider
          v-model="theme.componentBorderRadius"
          :min="0"
          :max="20"
          :step="1"
          show-input
          :show-input-controls="false"
        />
      </el-form-item>

      <el-form-item label="组件阴影">
        <el-switch v-model="theme.componentShadow" />
      </el-form-item>

      <el-form-item label="组件边框">
        <el-switch v-model="theme.componentBorder" />
      </el-form-item>

      <el-form-item label="组件间距">
        <el-slider
          v-model="theme.componentSpacing"
          :min="5"
          :max="30"
          :step="5"
          show-input
          :show-input-controls="false"
        />
      </el-form-item>

      <el-divider content-position="left">预览</el-divider>

      <div class="theme-preview" :style="getPreviewStyle()">
        <div class="preview-component" :style="getComponentStyle()">
          <div class="preview-title" :style="getTitleStyle()">示例组件</div>
          <div class="preview-content">
            <div class="preview-chart">
              <div
                v-for="(color, index) in getCurrentColors()"
                :key="index"
                class="preview-bar"
                :style="{ backgroundColor: color, height: `${(index + 1) * 20}%` }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <el-form-item>
        <el-button type="primary" size="small" @click="saveTheme">保存主题</el-button>
        <el-button size="small" @click="resetTheme">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'BiThemeConfig',
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      theme: {
        colorScheme: 'default',
        customColors: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'],
        backgroundColor: '#f5f5f5',
        backgroundType: 'color',
        backgroundImage: '',
        gradientStart: '#409eff',
        gradientEnd: '#67c23a',
        fontFamily: 'Microsoft YaHei',
        fontSize: 14,
        fontWeight: 'normal',
        componentBorderRadius: 4,
        componentShadow: true,
        componentBorder: true,
        componentSpacing: 10
      },
      colorSchemes: {
        default: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
        business: ['#1890ff', '#13c2c2', '#52c41a', '#faad14', '#f5222d'],
        tech: ['#722ed1', '#eb2f96', '#fa8c16', '#13c2c2', '#1890ff'],
        fresh: ['#52c41a', '#13c2c2', '#1890ff', '#faad14', '#f5222d']
      }
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val && Object.keys(val).length > 0) {
          this.theme = { ...this.theme, ...val }
        }
      },
      immediate: true,
      deep: true
    },
    theme: {
      handler(val) {
        this.$emit('input', val)
      },
      deep: true
    }
  },
  methods: {
    handleColorSchemeChange(scheme) {
      if (scheme !== 'custom' && this.colorSchemes[scheme]) {
        this.theme.customColors = [...this.colorSchemes[scheme]]
      }
    },
    addCustomColor() {
      this.theme.customColors.push('#409eff')
    },
    removeCustomColor(index) {
      if (this.theme.customColors.length > 1) {
        this.theme.customColors.splice(index, 1)
      }
    },
    getCurrentColors() {
      if (this.theme.colorScheme === 'custom') {
        return this.theme.customColors
      }
      return this.colorSchemes[this.theme.colorScheme] || this.colorSchemes.default
    },
    getPreviewStyle() {
      let background = this.theme.backgroundColor
      
      if (this.theme.backgroundType === 'image' && this.theme.backgroundImage) {
        background = `url(${this.theme.backgroundImage}) center/cover`
      } else if (this.theme.backgroundType === 'gradient') {
        background = `linear-gradient(135deg, ${this.theme.gradientStart}, ${this.theme.gradientEnd})`
      }
      
      return {
        background,
        padding: `${this.theme.componentSpacing}px`,
        fontFamily: this.theme.fontFamily,
        fontSize: `${this.theme.fontSize}px`,
        fontWeight: this.theme.fontWeight
      }
    },
    getComponentStyle() {
      return {
        borderRadius: `${this.theme.componentBorderRadius}px`,
        boxShadow: this.theme.componentShadow ? '0 2px 12px 0 rgba(0, 0, 0, 0.1)' : 'none',
        border: this.theme.componentBorder ? '1px solid #e4e7ed' : 'none'
      }
    },
    getTitleStyle() {
      return {
        fontFamily: this.theme.fontFamily,
        fontSize: `${this.theme.fontSize + 2}px`,
        fontWeight: this.theme.fontWeight
      }
    },
    saveTheme() {
      this.$emit('save', this.theme)
      this.$message.success('主题配置已保存')
    },
    resetTheme() {
      this.theme = {
        colorScheme: 'default',
        customColors: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'],
        backgroundColor: '#f5f5f5',
        backgroundType: 'color',
        backgroundImage: '',
        gradientStart: '#409eff',
        gradientEnd: '#67c23a',
        fontFamily: 'Microsoft YaHei',
        fontSize: 14,
        fontWeight: 'normal',
        componentBorderRadius: 4,
        componentShadow: true,
        componentBorder: true,
        componentSpacing: 10
      }
      this.$message.success('主题已重置为默认')
    }
  }
}
</script>

<style scoped>
.theme-config {
  padding: 10px;
}

.color-scheme-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.color-preview {
  display: flex;
  gap: 4px;
}

.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #fff;
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.2);
}

.custom-colors {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.color-picker-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.theme-preview {
  min-height: 200px;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.preview-component {
  background: white;
  padding: 15px;
  height: 180px;
}

.preview-title {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.preview-content {
  height: 120px;
}

.preview-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 100%;
  gap: 10px;
}

.preview-bar {
  flex: 1;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s;
}
</style>
