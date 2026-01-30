<template>
  <div class="advanced-config">
    <el-form :model="advancedConfig" label-width="90px" size="small">
      <!-- 下钻配置 -->
      <el-divider>下钻配置</el-divider>

      <el-form-item label="启用下钻">
        <el-switch v-model="advancedConfig.drillDown.enabled" />
      </el-form-item>

      <el-form-item v-if="advancedConfig.drillDown.enabled" label="下钻维度">
        <el-select
          v-model="advancedConfig.drillDown.dimensions"
          multiple
          placeholder="选择下钻维度"
          style="width: 100%"
        >
          <el-option
            v-for="field in dimensionFields"
            :key="field.fieldName"
            :label="field.fieldAlias || field.fieldName"
            :value="field.fieldName"
          />
        </el-select>
        <div class="tip">按顺序选择下钻维度,点击图表时将按此顺序下钻</div>
      </el-form-item>

      <!-- 交互配置 -->
      <el-divider>交互配置</el-divider>

      <el-form-item label="启用工具提示">
        <el-switch v-model="advancedConfig.tooltip.show" />
      </el-form-item>

      <el-form-item v-if="advancedConfig.tooltip.show" label="触发方式">
        <el-select v-model="advancedConfig.tooltip.trigger">
          <el-option label="数据项" value="item" />
          <el-option label="坐标轴" value="axis" />
        </el-select>
      </el-form-item>

      <el-form-item label="启用缩放">
        <el-switch v-model="advancedConfig.dataZoom.enabled" />
      </el-form-item>

      <el-form-item v-if="advancedConfig.dataZoom.enabled" label="缩放类型">
        <el-checkbox-group v-model="advancedConfig.dataZoom.type">
          <el-checkbox label="slider">滑动条</el-checkbox>
          <el-checkbox label="inside">内置</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="启用图例选择">
        <el-switch v-model="advancedConfig.legendSelect" />
        <div class="tip">允许点击图例切换系列显示/隐藏</div>
      </el-form-item>

      <!-- 数据处理 -->
      <el-divider>数据处理</el-divider>

      <el-form-item label="空值处理">
        <el-select v-model="advancedConfig.nullValueHandling">
          <el-option label="显示为0" value="zero" />
          <el-option label="跳过" value="skip" />
          <el-option label="连接" value="connect" />
        </el-select>
      </el-form-item>

      <el-form-item label="数据排序">
        <el-select v-model="advancedConfig.dataSort">
          <el-option label="不排序" value="none" />
          <el-option label="升序" value="asc" />
          <el-option label="降序" value="desc" />
        </el-select>
      </el-form-item>

      <el-form-item label="数据限制">
        <el-input-number
          v-model="advancedConfig.dataLimit"
          :min="1"
          :max="100000"
          size="small"
        />
        <div class="tip">最多显示的数据点数量</div>
      </el-form-item>

      <!-- 刷新配置 -->
      <el-divider>刷新配置</el-divider>

      <el-form-item label="自动刷新">
        <el-select v-model="advancedConfig.refreshInterval">
          <el-option label="不刷新" :value="0" />
          <el-option label="每30秒" :value="30" />
          <el-option label="每1分钟" :value="60" />
          <el-option label="每5分钟" :value="300" />
          <el-option label="每10分钟" :value="600" />
          <el-option label="每30分钟" :value="1800" />
          <el-option label="每小时" :value="3600" />
        </el-select>
      </el-form-item>

      <el-form-item label="加载动画">
        <el-switch v-model="advancedConfig.showLoading" />
      </el-form-item>

      <!-- 导出配置 -->
      <el-divider>导出配置</el-divider>

      <el-form-item label="允许导出">
        <el-switch v-model="advancedConfig.export.enabled" />
      </el-form-item>

      <el-form-item v-if="advancedConfig.export.enabled" label="导出格式">
        <el-checkbox-group v-model="advancedConfig.export.formats">
          <el-checkbox label="png">PNG图片</el-checkbox>
          <el-checkbox label="jpg">JPG图片</el-checkbox>
          <el-checkbox label="svg">SVG矢量图</el-checkbox>
          <el-checkbox label="excel">Excel表格</el-checkbox>
          <el-checkbox label="csv">CSV文件</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <!-- 性能优化 -->
      <el-divider>性能优化</el-divider>

      <el-form-item label="启用渐进渲染">
        <el-switch v-model="advancedConfig.progressive.enabled" />
        <div class="tip">大数据量时分批渲染,提升性能</div>
      </el-form-item>

      <el-form-item v-if="advancedConfig.progressive.enabled" label="渐进阈值">
        <el-input-number
          v-model="advancedConfig.progressive.threshold"
          :min="100"
          :max="10000"
          :step="100"
          size="small"
        />
        <div class="tip">超过此数量时启用渐进渲染</div>
      </el-form-item>

      <el-form-item label="启用采样">
        <el-switch v-model="advancedConfig.sampling.enabled" />
        <div class="tip">数据量过大时进行采样,提升性能</div>
      </el-form-item>

      <el-form-item v-if="advancedConfig.sampling.enabled" label="采样方法">
        <el-select v-model="advancedConfig.sampling.method">
          <el-option label="平均采样" value="average" />
          <el-option label="最大值采样" value="max" />
          <el-option label="最小值采样" value="min" />
          <el-option label="求和采样" value="sum" />
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'AdvancedConfig',
  props: {
    component: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      advancedConfig: {
        // 下钻配置
        drillDown: {
          enabled: false,
          dimensions: []
        },
        // 交互配置
        tooltip: {
          show: true,
          trigger: 'item'
        },
        dataZoom: {
          enabled: false,
          type: []
        },
        legendSelect: true,
        // 数据处理
        nullValueHandling: 'skip',
        dataSort: 'none',
        dataLimit: 10000,
        // 刷新配置
        refreshInterval: 0,
        showLoading: true,
        // 导出配置
        export: {
          enabled: true,
          formats: ['png', 'excel']
        },
        // 性能优化
        progressive: {
          enabled: false,
          threshold: 1000
        },
        sampling: {
          enabled: false,
          method: 'average'
        }
      },
      dimensionFields: []
    }
  },
  watch: {
    // 暂时完全禁用 watcher，避免无限循环
    /*
    component: {
      handler(val) {
        const defaultConfig = {
          drillDown: {
            enabled: false,
            dimensions: []
          },
          tooltip: {
            show: true,
            trigger: 'item'
          },
          dataZoom: {
            enabled: false,
            type: []
          },
          legendSelect: true,
          nullValueHandling: 'skip',
          dataSort: 'none',
          dataLimit: 10000,
          refreshInterval: 0,
          showLoading: true,
          export: {
            enabled: true,
            formats: ['png', 'excel']
          },
          progressive: {
            enabled: false,
            threshold: 1000
          },
          sampling: {
            enabled: false,
            method: 'average'
          }
        }

        if (val && val.advancedConfig) {
          const componentConfig = JSON.parse(JSON.stringify(val.advancedConfig))
          this.advancedConfig = this.deepMerge(defaultConfig, componentConfig)
        } else {
          this.advancedConfig = JSON.parse(JSON.stringify(defaultConfig))
        }
        
        if (val && val.dataConfig && val.dataConfig.datasetId) {
          this.loadDimensionFields(val.dataConfig.datasetId)
        }
      },
      deep: false,
      immediate: false
    },
    advancedConfig: {
      handler(val) {
        this.$emit('change', val)
      },
      deep: false
    }
    */
  },
  mounted() {
    // 在 mounted 中初始化，使用 $nextTick 确保 props 已传递
    this.$nextTick(() => {
      this.initializeConfig()
    })
  },
  methods: {
    initializeConfig() {
      const defaultConfig = {
        drillDown: {
          enabled: false,
          dimensions: []
        },
        tooltip: {
          show: true,
          trigger: 'item'
        },
        dataZoom: {
          enabled: false,
          type: []
        },
        legendSelect: true,
        nullValueHandling: 'skip',
        dataSort: 'none',
        dataLimit: 10000,
        refreshInterval: 0,
        showLoading: true,
        export: {
          enabled: true,
          formats: ['png', 'excel']
        },
        progressive: {
          enabled: false,
          threshold: 1000
        },
        sampling: {
          enabled: false,
          method: 'average'
        }
      }

      const val = this.component
      if (val && val.advancedConfig) {
        const componentConfig = JSON.parse(JSON.stringify(val.advancedConfig))
        // 使用深度合并，确保嵌套对象也被正确合并
        this.advancedConfig = this.deepMerge(defaultConfig, componentConfig)
      } else {
        // 没有配置，使用默认值
        this.advancedConfig = JSON.parse(JSON.stringify(defaultConfig))
      }
      
      // 加载维度字段
      if (val && val.dataConfig && val.dataConfig.datasetId) {
        this.loadDimensionFields(val.dataConfig.datasetId)
      }
    },
    async loadDimensionFields(datasetId) {
      // 这里应该从数据集中加载字段
      // 暂时使用空数组
      this.dimensionFields = []
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
.advanced-config {
  width: 400px;
  padding: 12px;
  box-sizing: border-box;
  overflow-y: auto;
}

.tip {
  margin-top: 4px;
  font-size: 11px;
  color: #909399;
  line-height: 1.4;
}

/* 确保表单项不超出宽度 */
.advanced-config >>> .el-form-item {
  margin-bottom: 16px;
}

.advanced-config >>> .el-form-item__content {
  width: calc(100% - 90px);
}

.advanced-config >>> .el-select,
.advanced-config >>> .el-input {
  width: 100%;
}

.advanced-config >>> .el-input-number {
  width: 120px;
}

.advanced-config >>> .el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
