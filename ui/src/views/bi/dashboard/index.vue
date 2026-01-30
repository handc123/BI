<template>
  <div class="app-container dashboard-designer">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="仪表板名称" prop="dashboardName">
        <el-input
          v-model="queryParams.dashboardName"
          placeholder="请输入仪表板名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['bi:dashboard:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['bi:dashboard:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['bi:dashboard:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-view"
          size="mini"
          :disabled="single"
          @click="handleView"
          v-hasPermi="['bi:dashboard:view']"
        >查看</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-share"
          size="mini"
          :disabled="single"
          @click="handleShare"
          v-hasPermi="['bi:dashboard:view']"
        >共享</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dashboardList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="仪表板ID" align="center" prop="id" width="80" />
      <el-table-column label="仪表板名称" align="center" prop="dashboardName" :show-overflow-tooltip="true" />
      <el-table-column label="组件数量" align="center" prop="componentCount" width="100">
        <template slot-scope="scope">
          <el-tag type="info">{{ scope.row.componentCount || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="info">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建者" align="center" prop="createBy" width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['bi:dashboard:view']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bi:dashboard:edit']"
          >设计</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-share"
            @click="handleShare(scope.row)"
            v-hasPermi="['bi:dashboard:view']"
          >共享</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bi:dashboard:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 设计器对话框 -->
    <el-dialog
      :title="title"
      :visible.sync="open"
      width="95%"
      top="5vh"
      append-to-body
      :close-on-click-modal="false"
      @close="handleDesignerClose"
    >
      <bi-dashboard-designer
        v-if="open"
        ref="dashboardDesigner"
        :dashboard-id="form.id"
        :dashboard-data="form"
        @save="handleDesignerSave"
      />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm" :loading="submitLoading">保 存</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 共享对话框 -->
    <el-dialog title="共享仪表板" :visible.sync="shareDialogVisible" width="600px" append-to-body>
      <el-form :model="shareForm" label-width="100px">
        <el-form-item label="共享链接">
          <el-input v-model="shareLink" readonly>
            <el-button slot="append" icon="el-icon-document-copy" @click="copyShareLink">复制</el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="密码保护">
          <el-switch v-model="shareForm.passwordEnabled" @change="handlePasswordToggle" />
        </el-form-item>
        <el-form-item label="访问密码" v-if="shareForm.passwordEnabled">
          <el-input v-model="shareForm.password" placeholder="请输入访问密码" show-password />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="shareForm.expiryDate"
            type="datetime"
            placeholder="选择过期时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="generateShareLink">生成链接</el-button>
        <el-button @click="shareDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDashboard,
  getDashboard,
  addDashboard,
  updateDashboard,
  delDashboard,
  generateShareLink
} from '@/api/bi/dashboard'
import BiDashboardDesigner from '@/components/BiDashboardDesigner'

export default {
  name: 'Dashboard',
  components: {
    BiDashboardDesigner
  },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      dashboardList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        dashboardName: undefined,
        status: undefined
      },
      form: {},
      submitLoading: false,
      shareDialogVisible: false,
      shareForm: {
        dashboardId: null,
        passwordEnabled: false,
        password: '',
        expiryDate: null
      },
      shareLink: ''
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDashboard(this.queryParams).then(response => {
        this.dashboardList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      // 清理可能残留的遮罩层
      this.$cleanupModals()
      // 跳转到设计器页面创建新仪表板
      this.$router.push({
        path: '/bi/dashboard/designer'
      })
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      const dashboardId = row.id || this.ids[0]
      // 清理可能残留的遮罩层
      this.$cleanupModals()
      // 跳转到设计器页面
      this.$router.push({
        path: '/bi/dashboard/designer/' + dashboardId
      })
    },
    handleView(row) {
      const dashboardId = row.id || this.ids[0]
      // 清理可能残留的遮罩层
      this.$cleanupModals()
      this.$router.push({
        path: '/bi/dashboard/view/' + dashboardId
      })
    },
    handleShare(row) {
      const dashboardId = row.id || this.ids[0]
      this.shareForm.dashboardId = dashboardId
      this.shareDialogVisible = true
      this.shareLink = ''
    },
    generateShareLink() {
      if (!this.shareForm.dashboardId) {
        this.$modal.msgError('请选择要共享的仪表板')
        return
      }
      
      generateShareLink(this.shareForm).then(response => {
        this.shareLink = response.data.shareUrl
        this.$modal.msgSuccess('共享链接生成成功')
      })
    },
    copyShareLink() {
      if (!this.shareLink) {
        this.$modal.msgWarning('请先生成共享链接')
        return
      }
      
      const input = document.createElement('input')
      input.value = this.shareLink
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      this.$modal.msgSuccess('链接已复制到剪贴板')
    },
    handlePasswordToggle(enabled) {
      if (!enabled) {
        this.shareForm.password = ''
      }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        layoutConfig: undefined,
        filterConfig: undefined,
        themeConfig: undefined,
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    submitForm() {
      // 如果是设计器模式,先保存设计器配置
      if (this.$refs.dashboardDesigner && this.form.id) {
        this.$refs.dashboardDesigner.saveDashboard().then(success => {
          if (success) {
            this.open = false
            this.getList()
          }
        })
      } else {
        // 普通表单提交
        if (this.$refs.dashboardDesigner) {
          const designerData = this.$refs.dashboardDesigner.getData()
          this.form = { ...this.form, ...designerData }
        }

        this.submitLoading = true
        if (this.form.id) {
          updateDashboard(this.form).then(response => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
            this.submitLoading = false
          }).catch(() => {
            this.submitLoading = false
          })
        } else {
          addDashboard(this.form).then(response => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
            this.submitLoading = false
          }).catch(() => {
            this.submitLoading = false
          })
        }
      }
    },
    handleDesignerSave(data) {
      this.form = { ...this.form, ...data }
    },
    handleDesignerClose() {
      // 检查未保存更改
      if (this.$refs.dashboardDesigner && this.$refs.dashboardDesigner.hasUnsavedChanges()) {
        this.$refs.dashboardDesigner.promptUnsavedChanges().then(() => {
          // 用户选择保存
          return this.$refs.dashboardDesigner.saveDashboard()
        }).catch(() => {
          // 用户选择不保存或取消
        })
      }
    },
    handleDelete(row) {
      const dashboardIds = row.id || this.ids
      this.$modal.confirm('是否确认删除仪表板编号为"' + dashboardIds + '"的数据项？').then(() => {
        return delDashboard(dashboardIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.dashboard-designer {
  height: 100%;
}
</style>
