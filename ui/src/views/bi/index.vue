<template>
  <div class="bi-home-container">
    <!-- 顶部欢迎区域 -->
    <el-row :gutter="20" class="welcome-section">
      <el-col :span="24">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎使用 BI 报表平台</h2>
              <p>自助式商业智能分析平台，让数据洞察触手可及</p>
            </div>
            <div class="welcome-actions">
              <el-button type="primary" icon="el-icon-plus" @click="handleQuickCreate">快速创建仪表板</el-button>
              <el-button icon="el-icon-document" @click="handleViewDocs">查看文档</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon datasource-icon">
              <i class="el-icon-s-data"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.datasourceCount }}</div>
              <div class="stat-label">数据源</div>
            </div>
          </div>
          <div class="stat-footer">
            <el-button type="text" size="small" @click="goToPage('/bi/datasource')">
              管理数据源 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon dataset-icon">
              <i class="el-icon-s-grid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.datasetCount }}</div>
              <div class="stat-label">数据集</div>
            </div>
          </div>
          <div class="stat-footer">
            <el-button type="text" size="small" @click="goToPage('/bi/dataset')">
              管理数据集 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon visualization-icon">
              <i class="el-icon-s-marketing"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.visualizationCount }}</div>
              <div class="stat-label">可视化</div>
            </div>
          </div>
          <div class="stat-footer">
            <el-button type="text" size="small" @click="goToPage('/bi/visualization')">
              管理可视化 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon dashboard-icon">
              <i class="el-icon-s-platform"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.dashboardCount }}</div>
              <div class="stat-label">仪表板</div>
            </div>
          </div>
          <div class="stat-footer">
            <el-button type="text" size="small" @click="goToPage('/bi/dashboard')">
              管理仪表板 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近访问的仪表板 -->
    <el-row :gutter="20" class="recent-section">
      <el-col :span="24">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span class="section-title">最近访问的仪表板</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToPage('/bi/dashboard')">
              查看全部
            </el-button>
          </div>
          <div v-if="recentDashboards.length > 0" class="dashboard-grid">
            <div
              v-for="dashboard in recentDashboards"
              :key="dashboard.id"
              class="dashboard-item"
              @click="viewDashboard(dashboard.id)"
            >
              <div class="dashboard-icon">
                <i class="el-icon-s-data"></i>
              </div>
              <div class="dashboard-info">
                <div class="dashboard-name">{{ dashboard.name }}</div>
                <div class="dashboard-meta">
                  <span class="dashboard-time">{{ formatTime(dashboard.updateTime) }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无最近访问的仪表板" :image-size="100"></el-empty>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速入门指南 -->
    <el-row :gutter="20" class="guide-section">
      <el-col :span="24">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span class="section-title">快速入门</span>
          </div>
          <el-steps :active="activeStep" finish-status="success" align-center>
            <el-step title="配置数据源" description="连接数据库或上传文件">
              <template slot="icon">
                <i class="el-icon-s-data"></i>
              </template>
            </el-step>
            <el-step title="创建数据集" description="定义数据模型和字段">
              <template slot="icon">
                <i class="el-icon-s-grid"></i>
              </template>
            </el-step>
            <el-step title="设计可视化" description="选择图表类型和配置">
              <template slot="icon">
                <i class="el-icon-s-marketing"></i>
              </template>
            </el-step>
            <el-step title="创建仪表板" description="组合可视化组件">
              <template slot="icon">
                <i class="el-icon-s-platform"></i>
              </template>
            </el-step>
          </el-steps>
          <div class="guide-actions">
            <el-button v-if="activeStep === 0" type="primary" @click="goToPage('/bi/datasource')">
              开始配置数据源
            </el-button>
            <el-button v-else-if="activeStep === 1" type="primary" @click="goToPage('/bi/dataset')">
              创建数据集
            </el-button>
            <el-button v-else-if="activeStep === 2" type="primary" @click="goToPage('/bi/visualization')">
              设计可视化
            </el-button>
            <el-button v-else-if="activeStep === 3" type="primary" @click="goToPage('/bi/dashboard')">
              创建仪表板
            </el-button>
            <el-button v-else type="success" @click="goToPage('/bi/dashboard')">
              查看我的仪表板
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速创建对话框 -->
    <el-dialog title="快速创建仪表板" :visible.sync="quickCreateVisible" width="500px" append-to-body>
      <el-form ref="quickCreateForm" :model="quickCreateForm" :rules="quickCreateRules" label-width="100px">
        <el-form-item label="仪表板名称" prop="name">
          <el-input v-model="quickCreateForm.name" placeholder="请输入仪表板名称" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="quickCreateForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="quickCreateVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitQuickCreate">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDashboard, addDashboard } from '@/api/bi/dashboard'

export default {
  name: 'BiIndex',
  data() {
    return {
      // 统计数据
      stats: {
        datasourceCount: 0,
        datasetCount: 0,
        visualizationCount: 0,
        dashboardCount: 0
      },
      // 最近访问的仪表板
      recentDashboards: [],
      // 快速入门步骤
      activeStep: 0,
      // 快速创建对话框
      quickCreateVisible: false,
      quickCreateForm: {
        name: '',
        remark: ''
      },
      quickCreateRules: {
        name: [
          { required: true, message: '请输入仪表板名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadStats()
    this.loadRecentDashboards()
    this.calculateActiveStep()
  },
  methods: {
    // 加载统计数据
    loadStats() {
      // 这里应该调用实际的API获取统计数据
      // 暂时使用模拟数据
      this.stats = {
        datasourceCount: 5,
        datasetCount: 12,
        visualizationCount: 28,
        dashboardCount: 8
      }
    },
    // 加载最近访问的仪表板
    loadRecentDashboards() {
      const queryParams = {
        pageNum: 1,
        pageSize: 6,
        orderByColumn: 'update_time',
        isAsc: 'desc'
      }
      listDashboard(queryParams).then(response => {
        this.recentDashboards = response.rows || []
      })
    },
    // 计算当前步骤
    calculateActiveStep() {
      // 根据统计数据判断用户当前处于哪个步骤
      if (this.stats.datasourceCount === 0) {
        this.activeStep = 0
      } else if (this.stats.datasetCount === 0) {
        this.activeStep = 1
      } else if (this.stats.visualizationCount === 0) {
        this.activeStep = 2
      } else if (this.stats.dashboardCount === 0) {
        this.activeStep = 3
      } else {
        this.activeStep = 4
      }
    },
    // 跳转到指定页面
    goToPage(path) {
      this.$router.push(path)
    },
    // 查看仪表板
    viewDashboard(id) {
      this.$router.push(`/bi/dashboard/view/${id}`)
    },
    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      const minute = 60 * 1000
      const hour = 60 * minute
      const day = 24 * hour

      if (diff < minute) {
        return '刚刚'
      } else if (diff < hour) {
        return Math.floor(diff / minute) + ' 分钟前'
      } else if (diff < day) {
        return Math.floor(diff / hour) + ' 小时前'
      } else if (diff < 7 * day) {
        return Math.floor(diff / day) + ' 天前'
      } else {
        return date.toLocaleDateString()
      }
    },
    // 快速创建仪表板
    handleQuickCreate() {
      this.quickCreateVisible = true
      this.quickCreateForm = {
        name: '',
        remark: ''
      }
    },
    // 提交快速创建
    submitQuickCreate() {
      this.$refs.quickCreateForm.validate(valid => {
        if (valid) {
          const dashboard = {
            name: this.quickCreateForm.name,
            remark: this.quickCreateForm.remark,
            layoutConfig: JSON.stringify({ components: [], linkages: [] }),
            filterConfig: JSON.stringify({ filters: [] }),
            themeConfig: JSON.stringify({
              colorScheme: 'default',
              backgroundColor: '#f5f5f5',
              fontFamily: 'Microsoft YaHei',
              fontSize: 14
            }),
            status: '0'
          }
          
          addDashboard(dashboard).then(response => {
            this.$modal.msgSuccess('创建成功')
            this.quickCreateVisible = false
            // 跳转到仪表板编辑页面
            this.$router.push(`/bi/dashboard?id=${response.data.id}`)
          })
        }
      })
    },
    // 查看文档
    handleViewDocs() {
      this.$message.info('文档功能开发中...')
    }
  }
}
</script>

<style scoped lang="scss">
.bi-home-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;

  ::v-deep .el-card__body {
    padding: 30px;
  }
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .welcome-text {
    h2 {
      margin: 0 0 10px 0;
      font-size: 28px;
      font-weight: 500;
    }

    p {
      margin: 0;
      font-size: 16px;
      opacity: 0.9;
    }
  }

  .welcome-actions {
    .el-button {
      margin-left: 10px;
    }
  }
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s;

  &:hover {
    transform: translateY(-5px);
  }

  ::v-deep .el-card__body {
    padding: 20px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;

  i {
    font-size: 28px;
    color: white;
  }

  &.datasource-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.dataset-icon {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  &.visualization-icon {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }

  &.dashboard-icon {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
}

.stat-info {
  flex: 1;

  .stat-value {
    font-size: 32px;
    font-weight: bold;
    color: #303133;
    line-height: 1;
    margin-bottom: 8px;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
  }
}

.stat-footer {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
  text-align: center;

  .el-button {
    padding: 0;
  }
}

.recent-section,
.guide-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.dashboard-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
  }

  .dashboard-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;

    i {
      font-size: 24px;
      color: white;
    }
  }

  .dashboard-info {
    flex: 1;
    min-width: 0;

    .dashboard-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .dashboard-meta {
      font-size: 12px;
      color: #909399;
    }
  }
}

.guide-actions {
  margin-top: 30px;
  text-align: center;
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;

    .welcome-actions {
      margin-top: 20px;
    }
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
