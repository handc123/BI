<template>
  <div class="dashboard-config">
    <el-form :model="form" label-width="100px" size="small">
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      
      <el-form-item label="仪表板名称">
        <el-input
          v-model="form.dashboardName"
          placeholder="请输入仪表板名称"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item label="主题">
        <el-select v-model="form.theme" placeholder="请选择主题" @change="handleChange">
          <el-option label="浅色" value="light" />
          <el-option label="深色" value="dark" />
        </el-select>
      </el-form-item>

      <!-- 画布配置 -->
      <el-divider content-position="left">画布配置</el-divider>

      <el-form-item label="画布宽度">
        <el-input-number
          v-model="form.canvasConfig.width"
          :min="800"
          :max="3840"
          :step="10"
          @change="handleChange"
        />
        <span class="unit">px</span>
      </el-form-item>

      <el-form-item label="画布高度">
        <el-input-number
          v-model="form.canvasConfig.height"
          :min="600"
          :max="2160"
          :step="10"
          @change="handleChange"
        />
        <span class="unit">px</span>
      </el-form-item>

      <el-form-item label="网格大小">
        <el-input-number
          v-model="form.canvasConfig.gridSize"
          :min="5"
          :max="50"
          :step="5"
          @change="handleChange"
        />
        <span class="unit">px</span>
      </el-form-item>

      <el-form-item label="背景类型">
        <el-radio-group v-model="form.canvasConfig.background.type" @change="handleChange">
          <el-radio label="color">纯色</el-radio>
          <el-radio label="gradient">渐变</el-radio>
          <el-radio label="image">图片</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.canvasConfig.background.type === 'color'" label="背景颜色">
        <el-color-picker
          v-model="form.canvasConfig.background.value"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item v-if="form.canvasConfig.background.type === 'image'" label="背景图片">
        <el-input
          v-model="form.canvasConfig.background.value"
          placeholder="请输入图片URL"
          @change="handleChange"
        />
      </el-form-item>

      <!-- 全局样式 -->
      <el-divider content-position="left">全局样式</el-divider>

      <el-form-item label="字体">
        <el-select v-model="form.globalStyle.fontFamily" @change="handleChange">
          <el-option label="微软雅黑" value="Microsoft YaHei" />
          <el-option label="宋体" value="SimSun" />
          <el-option label="黑体" value="SimHei" />
          <el-option label="Arial" value="Arial" />
        </el-select>
      </el-form-item>

      <el-form-item label="配色方案">
        <div class="color-scheme">
          <el-color-picker
            v-for="(color, index) in form.globalStyle.colorScheme"
            :key="index"
            v-model="form.globalStyle.colorScheme[index]"
            @change="handleChange"
          />
        </div>
      </el-form-item>

      <el-form-item label="标题字号">
        <el-input-number
          v-model="form.globalStyle.titleStyle.fontSize"
          :min="12"
          :max="32"
          @change="handleChange"
        />
        <span class="unit">px</span>
      </el-form-item>

      <el-form-item label="标题颜色">
        <el-color-picker
          v-model="form.globalStyle.titleStyle.color"
          @change="handleChange"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'DashboardConfig',
  props: {
    dashboard: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      form: {
        dashboardName: '',
        theme: 'light',
        canvasConfig: {
          width: 1520,
          height: 1080,
          gridSize: 10,
          margin: { top: 20, right: 20, bottom: 20, left: 20 },
          background: { type: 'color', value: '#ffffff', opacity: 1 },
          // 卡片样式
          card: { enabled: true, shadowType: 'default', shadowColor: 'rgba(0, 0, 0, 0.1)', borderRadius: 8 },
          // 布局配置
          layout: { type: 'grid', columns: 2, gap: 16 },
          // 网格配置
          gridColor: '#e0e0e0',
          columnDividerColor: '#409eff',
          showGrid: true,
          // 响应式配置
          responsive: {
            enabled: false,
            breakpoints: { sm: '768px', md: '1024px', lg: '1520px' }
          }
        },
        globalStyle: {
          colorScheme: ['#5470c6', '#91cc75', '#fac858'],
          titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
          fontFamily: 'Microsoft YaHei'
        }
      }
    }
  },
  watch: {
    dashboard: {
      handler(val) {
        if (val) {
          this.form = {
            dashboardName: val.dashboardName || val.name || '',
            theme: val.theme || 'light',
            canvasConfig: val.canvasConfig || this.form.canvasConfig,
            globalStyle: val.globalStyle || this.form.globalStyle
          }
        }
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    // 数据已在 watcher 中初始化
  },
  methods: {
    handleChange() {
      this.$emit('change', {
        type: 'dashboard',
        config: {
          dashboardName: this.form.dashboardName,
          theme: this.form.theme,
          canvasConfig: this.form.canvasConfig,
          globalStyle: this.form.globalStyle
        }
      })
    }
  }
}
</script>

<style scoped>
.dashboard-config {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.color-scheme {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.el-divider {
  margin: 20px 0;
}
</style>
