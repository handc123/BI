<template>
  <div class="specification-tab">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="el-icon-loading" />
      <span>加载中...</span>
    </div>

    <!-- 规范信息展示 -->
    <div v-else class="specification-content">
      <!-- 基本信息卡片 -->
      <el-card class="info-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">基本信息</span>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="指标编码">
            <el-tag type="info">{{ metricInfo.metricCode || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="指标名称">
            {{ metricInfo.metricName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="责任部门">
            {{ metricInfo.ownerDept || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="所属部门ID">
            {{ metricInfo.deptId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="数据新鲜度">
            <el-tag v-if="metricInfo.dataFreshness" type="success" size="small">
              {{ metricInfo.dataFreshness }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="更新频率">
            {{ metricInfo.updateFrequency || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="metricInfo.status === '0' ? 'success' : 'danger'" size="small">
              {{ metricInfo.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 业务定义卡片 -->
      <el-card class="info-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">业务定义</span>
          <el-button
            v-hasPermi="['bi:metadata:edit']"
            size="mini"
            icon="el-icon-edit"
            @click="editBusinessDefinition"
          >
            编辑
          </el-button>
        </div>
        <div class="definition-content">
          <div v-if="metricInfo.businessDefinition" class="definition-text">
            {{ metricInfo.businessDefinition }}
          </div>
          <el-empty v-else description="暂无业务定义" :image-size="80" />
        </div>
      </el-card>

      <!-- 技术信息卡片 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="info-card" shadow="never">
            <div slot="header" class="card-header">
              <span class="card-title">技术公式</span>
            </div>
            <div class="code-block">
              <pre v-if="metricInfo.technicalFormula">{{ metricInfo.technicalFormula }}</pre>
              <el-empty v-else description="暂无技术公式" :image-size="60" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="info-card" shadow="never">
            <div slot="header" class="card-header">
              <span class="card-title">计算逻辑</span>
            </div>
            <div class="code-block">
              <pre v-if="metricInfo.calculationLogic">{{ metricInfo.calculationLogic }}</pre>
              <el-empty v-else description="暂无计算逻辑" :image-size="60" />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 关联信息卡片 -->
      <el-card v-if="hasRelations" class="info-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">关联信息</span>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item v-if="metricInfo.datasetId" label="关联数据集ID">
            {{ metricInfo.datasetId }}
          </el-descriptions-item>
          <el-descriptions-item v-if="metricInfo.visualizationId" label="关联可视化ID">
            {{ metricInfo.visualizationId }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 审计信息 -->
      <el-card class="info-card" shadow="never">
        <div slot="header" class="card-header">
          <span class="card-title">审计信息</span>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="创建者">
            {{ metricInfo.createBy || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ metricInfo.createTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新者">
            {{ metricInfo.updateBy || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ metricInfo.updateTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">
            {{ metricInfo.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <!-- 编辑业务定义对话框 -->
    <el-dialog
      title="编辑业务定义"
      :visible.sync="editDialogVisible"
      width="600px"
      append-to-body
    >
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="业务定义" prop="businessDefinition">
          <el-input
            v-model="editForm.businessDefinition"
            type="textarea"
            :rows="6"
            placeholder="请输入业务定义"
          />
        </el-form-item>
        <el-form-item label="技术公式" prop="technicalFormula">
          <el-input
            v-model="editForm.technicalFormula"
            type="textarea"
            :rows="4"
            placeholder="请输入技术公式"
          />
        </el-form-item>
        <el-form-item label="计算逻辑" prop="calculationLogic">
          <el-input
            v-model="editForm.calculationLogic"
            type="textarea"
            :rows="4"
            placeholder="请输入计算逻辑说明"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { updateMetricMetadata } from '@/api/bi/metric'

export default {
  name: 'SpecificationTab',
  props: {
    metricId: {
      type: [Number, String],
      required: true
    },
    metricInfo: {
      type: Object,
      default: function() {
        return {}
      }
    }
  },
  data() {
    return {
      loading: false,
      editDialogVisible: false,
      submitting: false,
      editForm: {
        id: null,
        businessDefinition: '',
        technicalFormula: '',
        calculationLogic: ''
      },
      editRules: {
        businessDefinition: [
          { max: 1000, message: '业务定义长度不能超过1000个字符' }
        ],
        technicalFormula: [
          { max: 500, message: '技术公式长度不能超过500个字符' }
        ],
        calculationLogic: [
          { max: 1000, message: '计算逻辑长度不能超过1000个字符' }
        ]
      }
    }
  },
  computed: {
    hasRelations() {
      return this.metricInfo.datasetId || this.metricInfo.visualizationId
    }
  },
  methods: {
    editBusinessDefinition() {
      this.editForm = {
        id: this.metricId,
        businessDefinition: this.metricInfo.businessDefinition || '',
        technicalFormula: this.metricInfo.technicalFormula || '',
        calculationLogic: this.metricInfo.calculationLogic || ''
      }
      this.editDialogVisible = true
    },

    async submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) return

        this.submitting = true
        try {
          const res = await updateMetricMetadata(this.editForm)
          if (res.code === 200) {
            this.$message.success('更新成功')
            this.editDialogVisible = false
            // 通知父组件刷新数据
            this.$emit('refresh')
          } else {
            this.$message.error(res.msg || '更新失败')
          }
        } catch (error) {
          console.error('更新失败:', error)
          this.$message.error('更新失败')
        } finally {
          this.submitting = false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.specification-tab {
  padding: 20px;

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;

    .el-icon-loading {
      font-size: 32px;
      margin-right: 10px;
    }
  }

  .specification-content {
    .info-card {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .definition-content {
      min-height: 100px;

      .definition-text {
        line-height: 1.8;
        color: #606266;
        white-space: pre-wrap;
        word-break: break-all;
      }
    }

    .code-block {
      background: #f5f7fa;
      border: 1px solid #dfe4ed;
      border-radius: 4px;
      padding: 12px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      line-height: 1.6;
      color: #303133;
      min-height: 80px;

      pre {
        margin: 0;
        white-space: pre-wrap;
        word-break: break-all;
      }
    }
  }
}
</style>
