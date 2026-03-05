<template>
  <div class="test-dataconfig-container">
    <el-card header="DataConfig 字段显示测试">
      <el-form label-width="100px">
        <el-form-item label="测试组件">
          <el-button type="primary" @click="testComponent">创建测试组件</el-button>
          <el-button @click="showDebugInfo">显示调试信息</el-button>
        </el-form-item>
      </el-form>

      <el-divider>测试组件预览</el-divider>

      <div v-if="testComponentData" class="test-preview">
        <data-config
          ref="dataConfig"
          :component="testComponentData"
          @change="handleConfigChange"
        />
      </div>

      <el-divider>调试信息</el-divider>

      <div class="debug-info">
        <h4>可用字段 (availableFields):</h4>
        <pre>{{ debugInfo.availableFields }}</pre>

        <h4>指标字段 (metricFields):</h4>
        <pre>{{ debugInfo.metricFields }}</pre>

        <h4>维度字段 (dimensionFields):</h4>
        <pre>{{ debugInfo.dimensionFields }}</pre>

        <h4>配置变化:</h4>
        <pre>{{ debugInfo.configChanges }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script>
import DataConfig from '@/components/ConfigPanel/DataConfig'

export default {
  name: 'TestDataConfig',
  components: {
    DataConfig
  },
  data() {
    return {
      testComponentData: null,
      debugInfo: {
        availableFields: [],
        metricFields: [],
        dimensionFields: [],
        configChanges: []
      }
    }
  },
  methods: {
    testComponent() {
      // 创建一个测试组件
      this.testComponentData = {
        id: 'test-component-1',
        type: 'chart',
        styleConfig: {
          chartType: 'bar'
        },
        dataConfig: {
          datasourceId: null,
          datasetId: null,
          dimensions: [],
          metrics: [],
          filters: []
        }
      }
      this.$message.success('测试组件已创建，请选择数据源和数据集')
    },

    showDebugInfo() {
      if (!this.$refs.dataConfig) {
        this.$message.warning('请先创建测试组件')
        return
      }

      const dataConfig = this.$refs.dataConfig

      this.debugInfo = {
        availableFields: JSON.stringify(dataConfig.availableFields, null, 2),
        metricFields: JSON.stringify(dataConfig.metricFields, null, 2),
        dimensionFields: JSON.stringify(dataConfig.dimensionFields, null, 2),
        configChanges: JSON.stringify(this.debugInfo.configChanges, null, 2)
      }

      console.log('=== DataConfig 调试信息 ===')
      console.log('可用字段:', dataConfig.availableFields)
      console.log('指标字段:', dataConfig.metricFields)
      console.log('维度字段:', dataConfig.dimensionFields)
      console.log('数据配置:', dataConfig.dataConfig)
    },

    handleConfigChange(config) {
      console.log('[TestDataConfig] 配置变化:', config)
      this.debugInfo.configChanges.push({
        timestamp: new Date().toISOString(),
        config: config
      })
    }
  }
}
</script>

<style scoped>
.test-dataconfig-container {
  padding: 20px;
}

.test-preview {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  background: #f5f7fa;
}

.debug-info {
  margin-top: 20px;
}

.debug-info h4 {
  margin-top: 20px;
  margin-bottom: 10px;
  color: #303133;
}

.debug-info pre {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow-y: auto;
}
</style>
