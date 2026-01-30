<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入数据源名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 240px">
          <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
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
          v-hasPermi="['bi:datasource:add']"
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
          v-hasPermi="['bi:datasource:edit']"
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
          v-hasPermi="['bi:datasource:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-link"
          size="mini"
          :disabled="single"
          @click="handleTestSelected"
          v-hasPermi="['bi:datasource:add', 'bi:datasource:edit']"
        >测试连接</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="openUploadDialog"
          v-hasPermi="['bi:datasource:add']"
        >上传文件数据源</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          @click="handleCleanupFiles"
          v-hasPermi="['bi:datasource:remove']"
        >清理未使用文件</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="type" width="120">
        <template slot-scope="scope">
          <span>{{ getTypeLabel(scope.row.type) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="info">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-link"
            @click="handleTest(scope.row)"
            v-hasPermi="['bi:datasource:add', 'bi:datasource:edit']"
          >测试</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bi:datasource:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bi:datasource:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据源名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入数据源名称" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据源类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%" @change="handleTypeChange">
                <el-option v-for="opt in editableTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- DB 配置 -->
        <div v-if="isDbType(form.type)">
          <el-divider content-position="left">数据库连接</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Host" prop="cfg.host">
                <el-input v-model="cfg.host" placeholder="例如：127.0.0.1" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Port" prop="cfg.port">
                <el-input-number v-model="cfg.port" :min="1" :max="65535" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Database" prop="cfg.database">
                <el-input v-model="cfg.database" placeholder="数据库名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Username" prop="cfg.username">
                <el-input v-model="cfg.username" placeholder="用户名" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Password" prop="cfg.password">
                <el-input v-model="cfg.password" placeholder="密码" show-password />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio label="0">正常</el-radio>
                  <el-radio label="1">停用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">连接池</el-divider>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="最小连接" prop="cfg.minConnections">
                <el-input-number v-model="cfg.minConnections" :min="0" :max="200" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="最大连接" prop="cfg.maxConnections">
                <el-input-number v-model="cfg.maxConnections" :min="1" :max="500" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="超时(ms)" prop="cfg.connectionTimeout">
                <el-input-number v-model="cfg.connectionTimeout" :min="1000" :max="300000" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- API 配置 -->
        <div v-else-if="form.type === 'api'">
          <el-divider content-position="left">API 数据源</el-divider>
          <el-row :gutter="20">
            <el-col :span="16">
              <el-form-item label="URL" prop="api.url">
                <el-input v-model="api.url" placeholder="https://example.com/api" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="方法" prop="api.method">
                <el-select v-model="api.method" placeholder="GET/POST" style="width: 100%">
                  <el-option label="GET" value="GET" />
                  <el-option label="POST" value="POST" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="Headers(JSON)" prop="api.headers">
            <el-input v-model="api.headers" type="textarea" :rows="3" placeholder='例如：{"Authorization":"Bearer xxx"}' />
          </el-form-item>
          <el-form-item label="Query(JSON)" prop="api.query">
            <el-input v-model="api.query" type="textarea" :rows="3" placeholder='例如：{"a":1,"b":"x"}' />
          </el-form-item>
          <el-form-item label="Body(JSON)" prop="api.body" v-if="api.method === 'POST'">
            <el-input v-model="api.body" type="textarea" :rows="3" placeholder='例如：{"page":1,"size":10}' />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio label="0">正常</el-radio>
              <el-radio label="1">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>

        <el-alert
          v-if="configHint"
          type="info"
          :closable="false"
          show-icon
          :title="configHint"
          style="margin-top: 5px;"
        />
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="warning" @click="handleTestInDialog" v-hasPermi="['bi:datasource:add','bi:datasource:edit']">测试连接</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 上传文件数据源 -->
    <el-dialog title="上传文件数据源" :visible.sync="uploadOpen" width="700px" append-to-body>
      <bi-file-data-source-upload @success="handleFileUploadSuccess" />
      <div slot="footer" class="dialog-footer">
        <el-button @click="uploadOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDataSource,
  getDataSource,
  addDataSource,
  updateDataSource,
  delDataSource,
  testDataSource,
  cleanupFileDataSource
} from '@/api/bi/datasource'
import BiFileDataSourceUpload from '@/components/BiFileDataSourceUpload/index.vue'

export default {
  name: 'BiDataSource',
  components: { BiFileDataSourceUpload },
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      list: [],
      ids: [],
      single: true,
      multiple: true,
      title: '',
      open: false,
      uploadOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        type: undefined
      },
      form: {},
      // 表单配置（解析后的 config）
      cfg: {
        host: '',
        port: 3306,
        database: '',
        username: '',
        password: '',
        minConnections: 2,
        maxConnections: 10,
        connectionTimeout: 30000
      },
      api: {
        url: '',
        method: 'GET',
        headers: '',
        query: '',
        body: ''
      },
      rules: {
        name: [{ required: true, message: '数据源名称不能为空', trigger: 'blur' }],
        type: [{ required: true, message: '数据源类型不能为空', trigger: 'change' }]
      },
      typeOptions: [
        { label: 'MySQL', value: 'mysql' },
        { label: 'PostgreSQL', value: 'postgresql' },
        { label: 'ClickHouse', value: 'clickhouse' },
        { label: 'Doris', value: 'doris' },
        { label: 'Oracle', value: 'oracle' },
        { label: 'API', value: 'api' },
        { label: 'File', value: 'file' }
      ]
    }
  },
  computed: {
    editableTypeOptions() {
      // file 由上传接口生成，这里不开放“手工新增 file”
      return this.typeOptions.filter(x => x.value !== 'file')
    },
    configHint() {
      if (this.isDbType(this.form.type)) {
        return '保存时会自动组装 config(JSON)：host/port/database/username/password/connectionPool'
      }
      if (this.form.type === 'api') {
        return '保存时会自动组装 config(JSON)：url/method/headers/query/body'
      }
      return ''
    }
  },
  created() {
    this.getList()
  },
  methods: {
    isDbType(type) {
      return ['mysql', 'postgresql', 'clickhouse', 'doris', 'oracle'].indexOf(type) >= 0
    },
    getTypeLabel(type) {
      const hit = this.typeOptions.find(x => x.value === type)
      return hit ? hit.label : type
    },
    /** 查询列表 */
    getList() {
      this.loading = true
      listDataSource(this.queryParams).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams.pageNum = 1
      this.getList()
    },
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        type: 'mysql',
        config: undefined,
        status: '0',
        remark: undefined
      }
      this.resetCfg()
      this.resetApiCfg()
      this.resetForm('form')
    },
    resetCfg() {
      this.cfg = {
        host: '',
        port: 3306,
        database: '',
        username: '',
        password: '',
        minConnections: 2,
        maxConnections: 10,
        connectionTimeout: 30000
      }
    },
    resetApiCfg() {
      this.api = {
        url: '',
        method: 'GET',
        headers: '',
        query: '',
        body: ''
      }
    },
    handleTypeChange() {
      // 切换类型时清理配置，避免混淆
      this.resetCfg()
      this.resetApiCfg()
    },
    buildConfigJson() {
      if (this.isDbType(this.form.type)) {
        return JSON.stringify({
          host: this.cfg.host,
          port: this.cfg.port,
          database: this.cfg.database,
          username: this.cfg.username,
          password: this.cfg.password,
          connectionPool: {
            minConnections: this.cfg.minConnections,
            maxConnections: this.cfg.maxConnections,
            connectionTimeout: this.cfg.connectionTimeout
          }
        })
      }
      if (this.form.type === 'api') {
        const parseOrEmpty = (txt) => {
          if (!txt) return {}
          return JSON.parse(txt)
        }
        return JSON.stringify({
          url: this.api.url,
          method: this.api.method,
          headers: parseOrEmpty(this.api.headers),
          query: parseOrEmpty(this.api.query),
          body: this.api.method === 'POST' ? parseOrEmpty(this.api.body) : {}
        })
      }
      // 兜底：允许直接保留 form.config
      return this.form.config || '{}'
    },
    parseConfigToForm(configStr) {
      try {
        const obj = JSON.parse(configStr || '{}')
        if (this.isDbType(this.form.type)) {
          const pool = obj.connectionPool || {}
          this.cfg.host = obj.host || ''
          this.cfg.port = obj.port || 3306
          this.cfg.database = obj.database || ''
          this.cfg.username = obj.username || ''
          this.cfg.password = obj.password || ''
          this.cfg.minConnections = pool.minConnections != null ? pool.minConnections : 2
          this.cfg.maxConnections = pool.maxConnections != null ? pool.maxConnections : 10
          this.cfg.connectionTimeout = pool.connectionTimeout != null ? pool.connectionTimeout : 30000
        } else if (this.form.type === 'api') {
          this.api.url = obj.url || ''
          this.api.method = obj.method || 'GET'
          this.api.headers = obj.headers ? JSON.stringify(obj.headers, null, 2) : ''
          this.api.query = obj.query ? JSON.stringify(obj.query, null, 2) : ''
          this.api.body = obj.body ? JSON.stringify(obj.body, null, 2) : ''
        }
      } catch (e) {
        // ignore
      }
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增数据源'
    },
    handleUpdate(row) {
      const id = (row && row.id) || this.ids[0]
      this.reset()
      getDataSource(id).then(res => {
        this.form = res.data || {}
        if (!this.form.status) this.form.status = '0'
        this.open = true
        this.title = '修改数据源'
        this.parseConfigToForm(this.form.config)
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) return
        // 组装 config
        try {
          this.form.config = this.buildConfigJson()
        } catch (e) {
          this.$modal.msgError('config JSON 解析失败，请检查 Headers/Query/Body 的 JSON 格式')
          return
        }

        if (this.form.id) {
          updateDataSource(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addDataSource(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const ids = (row && row.id) ? row.id : this.ids.join(',')
      this.$modal.confirm('是否确认删除数据源编号为 "' + ids + '" 的数据项？').then(() => {
        return delDataSource(ids)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    handleTest(row) {
      const payload = {
        id: row.id,
        name: row.name,
        type: row.type,
        config: row.config,
        status: row.status,
        remark: row.remark
      }
      testDataSource(payload).then(res => {
        const data = res.data || {}
        if (data.success) {
          this.$modal.msgSuccess(`连接成功（耗时：${data.duration || 0}ms${data.version ? '，版本：' + data.version : ''}）`)
        } else {
          this.$modal.msgError(data.message || '连接失败')
        }
      })
    },
    handleTestSelected() {
      const row = this.list.find(x => x.id === this.ids[0])
      if (row) this.handleTest(row)
    },
    handleTestInDialog() {
      try {
        const payload = { ...this.form, config: this.buildConfigJson() }
        testDataSource(payload).then(res => {
          const data = res.data || {}
          if (data.success) {
            this.$modal.msgSuccess(`连接成功（耗时：${data.duration || 0}ms${data.version ? '，版本：' + data.version : ''}）`)
          } else {
            this.$modal.msgError(data.message || '连接失败')
          }
        })
      } catch (e) {
        this.$modal.msgError('config JSON 解析失败，请检查填写内容')
      }
    },
    openUploadDialog() {
      this.uploadOpen = true
    },
    handleFileUploadSuccess(payload) {
      // payload: FileUploadResult
      this.uploadOpen = false
      this.getList()
      if (payload && payload.dataSourceId) {
        this.$modal.msgSuccess('已创建文件数据源，ID=' + payload.dataSourceId)
      }
    },
    handleCleanupFiles() {
      this.$modal.confirm('确认清理未使用的文件数据源吗？').then(() => {
        return cleanupFileDataSource()
      }).then(res => {
        this.$modal.msgSuccess(res.msg || '清理成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

