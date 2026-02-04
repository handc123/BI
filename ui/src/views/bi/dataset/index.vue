<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="数据集名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入数据集名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="数据源" prop="datasourceId">
        <el-select v-model="queryParams.datasourceId" placeholder="请选择数据源" clearable style="width: 240px">
          <el-option
            v-for="ds in datasourceList"
            :key="ds.id"
            :label="ds.name"
            :value="ds.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 240px">
          <el-option label="直连数据集" value="direct" />
          <el-option label="抽取数据集" value="extract" />
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
          v-hasPermi="['bi:dataset:add']"
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
          v-hasPermi="['bi:dataset:edit']"
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
          v-hasPermi="['bi:dataset:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-view"
          size="mini"
          :disabled="single"
          @click="handlePreview"
          v-hasPermi="['bi:dataset:query']"
        >预览数据</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-download"
          size="mini"
          :disabled="single"
          @click="handleExtract"
          v-hasPermi="['bi:dataset:edit']"
        >立即抽取</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="datasetList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="数据集ID" align="center" prop="id" width="80" />
      <el-table-column label="数据集名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="数据源" align="center" prop="datasourceId" width="200">
        <template slot-scope="scope">
          <span>{{ getDatasourceName(scope.row.datasourceId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="type" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.type === 'direct'" type="success">直连数据集</el-tag>
          <el-tag v-else-if="scope.row.type === 'extract'" type="warning">抽取数据集</el-tag>
          <span v-else>{{ scope.row.type }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="info">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数据行数" align="center" prop="rowCount" width="120">
        <template slot-scope="scope">
          <span v-if="scope.row.rowCount">{{ scope.row.rowCount.toLocaleString() }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="最后抽取" align="center" prop="lastExtractTime" width="180">
        <template slot-scope="scope">
          <span v-if="scope.row.lastExtractTime">{{ parseTime(scope.row.lastExtractTime) }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
            v-hasPermi="['bi:dataset:query']"
          >预览</el-button>
          <el-button
            v-if="scope.row.type === 'extract'"
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleExtract(scope.row)"
            v-hasPermi="['bi:dataset:edit']"
          >抽取</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bi:dataset:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bi:dataset:remove']"
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

    <!-- 添加或修改数据集对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据集名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入数据集名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据源" prop="datasourceId">
              <el-select 
                v-model="form.datasourceId" 
                placeholder="请选择数据源" 
                style="width: 100%"
                @change="handleDataSourceChange"
              >
                <el-option
                  v-for="ds in datasourceList"
                  :key="ds.id"
                  :label="ds.name"
                  :value="ds.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="数据集类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio label="direct">直连数据集</el-radio>
                <el-radio label="extract">抽取数据集</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 查询配置 -->
        <el-card class="box-card" shadow="never" style="margin-bottom: 20px;">
          <div slot="header" class="clearfix">
            <span>查询配置</span>
          </div>
          <el-form-item label="查询类型" prop="queryConfig.sourceType">
            <el-radio-group v-model="form.queryConfig.sourceType">
              <el-radio label="table">表查询</el-radio>
              <el-radio label="sql">SQL查询</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item
            v-if="form.queryConfig && form.queryConfig.sourceType === 'table'"
            label="表名"
            prop="queryConfig.tableName"
          >
            <el-select 
              v-model="form.queryConfig.tableName" 
              placeholder="请选择表名" 
              filterable
              :loading="tableListLoading"
              style="width: 100%"
            >
              <el-option
                v-for="table in tableList"
                :key="table.tableName"
                :label="table.tableComment || table.tableName"
                :value="table.tableName"
              >
                <span style="float: left">{{ table.tableComment }}</span>
                <span style="float: right; color: #8492a6; font-size: 12px">{{ table.tableName }}</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item
            v-if="form.queryConfig && form.queryConfig.sourceType === 'sql'"
            label="SQL语句"
            prop="queryConfig.sql"
          >
            <el-input
              v-model="form.queryConfig.sql"
              type="textarea"
              :rows="3"
              placeholder="请输入SQL查询语句"
            />
          </el-form-item>

          <!-- JOIN 配置 -->
          <bi-join-config v-if="form.queryConfig" v-model="form.queryConfig.joins" />
        </el-card>

        <!-- 表结构预览（维度和指标） -->
        <el-card 
          v-if="tableSchema && form.queryConfig.sourceType === 'table'" 
          class="box-card" 
          shadow="never" 
          style="margin-bottom: 20px;"
        >
          <div slot="header" class="clearfix">
            <span>表结构预览</span>
            <span style="margin-left: 10px; color: #909399; font-size: 12px;">
              {{ tableSchema.tableComment || tableSchema.tableName }}
            </span>
          </div>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="field-category-title">
                <i class="el-icon-s-grid"></i> 维度字段 ({{ dimensions.length }})
              </div>
              <el-table 
                :data="dimensions" 
                size="small" 
                max-height="200"
                :show-header="true"
              >
                <el-table-column prop="columnName" label="字段名" width="120" />
                <el-table-column prop="columnComment" label="中文名" :show-overflow-tooltip="true" />
                <el-table-column prop="dataType" label="类型" width="100" />
              </el-table>
            </el-col>
            
            <el-col :span="12">
              <div class="field-category-title">
                <i class="el-icon-s-data"></i> 指标字段 ({{ measures.length }})
              </div>
              <el-table 
                :data="measures" 
                size="small" 
                max-height="200"
                :show-header="true"
              >
                <el-table-column prop="columnName" label="字段名" width="120" />
                <el-table-column prop="columnComment" label="中文名" :show-overflow-tooltip="true" />
                <el-table-column prop="dataType" label="类型" width="100" />
              </el-table>
            </el-col>
          </el-row>
        </el-card>

        <!-- 数据预览 -->
        <el-card 
          v-if="tablePreviewRows.length > 0 && form.queryConfig.sourceType === 'table'" 
          class="box-card" 
          shadow="never" 
          style="margin-bottom: 20px;"
        >
          <div slot="header" class="clearfix">
            <span>数据预览</span>
            <span style="margin-left: 10px; color: #909399; font-size: 12px;">
              共 {{ tablePreviewTotal }} 行，显示前 10 行
            </span>
          </div>
          
          <el-table 
            v-loading="tablePreviewLoading"
            :data="tablePreviewRows" 
            size="small" 
            border
            max-height="300"
          >
            <el-table-column
              v-for="column in tablePreviewColumns"
              :key="column.name"
              :prop="column.name"
              :label="column.comment"
              :show-overflow-tooltip="true"
              min-width="120"
            >
              <template slot="header">
                <div>
                  <div>{{ column.comment }}</div>
                  <div style="color: #909399; font-size: 11px; font-weight: normal;">
                    {{ column.name }}
                    <el-tag 
                      :type="column.fieldType === 'DIMENSION' ? 'info' : 'warning'" 
                      size="mini"
                      style="margin-left: 5px;"
                    >
                      {{ column.fieldType === 'DIMENSION' ? '维度' : '指标' }}
                    </el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 字段配置 -->
        <bi-field-config v-if="form.fieldConfig" v-model="form.fieldConfig.fields" @change="handleFieldChange" />

        <!-- 抽取配置 -->
        <el-card v-if="form.type === 'extract' && form.extractConfig" class="box-card" shadow="never" style="margin-bottom: 20px;">
          <div slot="header" class="clearfix">
            <span>抽取配置</span>
          </div>
          <el-form-item label="定时抽取">
            <el-switch v-model="form.extractConfig.schedule.enabled" />
          </el-form-item>
          <el-form-item
            v-if="form.extractConfig.schedule && form.extractConfig.schedule.enabled"
            label="Cron表达式"
            prop="extractConfig.schedule.cronExpression"
          >
            <el-input
              v-model="form.extractConfig.schedule.cronExpression"
              placeholder="例如: 0 0 2 * * ? (每天凌晨2点)"
            />
            <div class="form-tip">常用Cron表达式：
              <br>0 0 2 * * ? - 每天凌晨2点
              <br>0 0/30 * * * ? - 每30分钟
              <br>0 0 2 ? * MON - 每周一凌晨2点
            </div>
          </el-form-item>
          <el-form-item label="增量抽取">
            <el-switch v-model="form.extractConfig.incremental" />
          </el-form-item>
          <el-form-item
            v-if="form.extractConfig.incremental"
            label="增量字段"
            prop="extractConfig.incrementalField"
          >
            <el-input
              v-model="form.extractConfig.incrementalField"
              placeholder="用于增量抽取的时间戳字段"
            />
          </el-form-item>
        </el-card>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 数据预览对话框 -->
    <el-dialog title="数据预览" :visible.sync="previewDialogVisible" width="1200px" append-to-body>
      <div class="preview-toolbar">
        <el-button size="small" @click="addPreviewFilter">添加筛选</el-button>
        <el-button size="small" type="primary" @click="executePreview">执行预览</el-button>
      </div>

      <div class="preview-filters" v-if="previewFilters.length > 0">
        <el-row :gutter="10" v-for="(filter, index) in previewFilters" :key="index">
          <el-col :span="4">
            <el-select v-model="filter.field" placeholder="选择字段" size="small">
              <el-option
                v-for="field in currentDatasetFields"
                :key="field.name"
                :label="field.alias || field.name"
                :value="field.name"
              />
            </el-select>
          </el-col>
          <el-col :span="3">
            <el-select v-model="filter.operator" placeholder="操作符" size="small">
              <el-option label="等于" value="eq" />
              <el-option label="不等于" value="ne" />
              <el-option label="大于" value="gt" />
              <el-option label="大于等于" value="gte" />
              <el-option label="小于" value="lt" />
              <el-option label="小于等于" value="lte" />
              <el-option label="包含" value="like" />
              <el-option label="在列表中" value="in" />
              <el-option label="不在列表中" value="not_in" />
              <el-option label="为空" value="is_null" />
              <el-option label="不为空" value="not_null" />
              <el-option label="介于" value="between" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input
              v-if="!['is_null', 'not_null'].includes(filter.operator)"
              v-model="filter.value"
              placeholder="输入值"
              size="small"
            />
            <el-input
              v-if="filter.operator === 'between'"
              v-model="filter.value2"
              placeholder="结束值"
              size="small"
              style="margin-left: 5px;"
            />
          </el-col>
          <el-col :span="2">
            <el-button type="danger" size="small" icon="el-icon-delete" @click="removePreviewFilter(index)">删除</el-button>
          </el-col>
        </el-row>
      </div>

      <el-table
        v-loading="previewLoading"
        :data="previewData"
        size="small"
        border
        max-height="400"
      >
        <el-table-column
          v-for="column in previewColumns"
          :key="column.name"
          :label="column.alias || column.name"
          :prop="column.name"
          :show-overflow-tooltip="true"
        />
      </el-table>

      <el-pagination
        v-if="previewData.length > 0"
        @size-change="handlePreviewSizeChange"
        @current-change="handlePreviewCurrentChange"
        :current-page="previewQueryParams.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="previewQueryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="previewTotal"
        style="margin-top: 20px;"
      />
    </el-dialog>
  </div>
</template>

<script>
import {
  listDataset,
  getDataset,
  addDataset,
  updateDataset,
  delDataset,
  previewDataset,
  executeExtract,
  getDataSources,
  getDataSourceTables,
  getTableSchema,
  getTablePreview
} from "@/api/bi/dataset"
import BiFieldConfig from '@/components/BiFieldConfig'
import BiJoinConfig from '@/components/BiJoinConfig'

export default {
  name: "Dataset",
  components: {
    BiFieldConfig,
    BiJoinConfig
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 数据集表格数据
      datasetList: [],
      // 数据源列表
      datasourceList: [],
      // 表列表
      tableList: [],
      // 表列表加载状态
      tableListLoading: false,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        datasourceId: undefined,
        type: undefined
      },
      // 表单参数
      form: {
        name: undefined,
        datasourceId: undefined,
        type: "direct",
        queryConfig: {
          sourceType: "table",
          tableName: "",
          sql: "",
          joins: []
        },
        fieldConfig: {
          fields: []
        },
        extractConfig: {
          schedule: {
            enabled: false,
            cronExpression: "0 0 2 * * ?"
          },
          incremental: false,
          incrementalField: ""
        },
        status: "0",
        remark: undefined
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "数据集名称不能为空", trigger: "blur" }
        ],
        datasourceId: [
          { required: true, message: "请选择数据源", trigger: "change" }
        ],
        type: [
          { required: true, message: "请选择数据集类型", trigger: "change" }
        ],
        "queryConfig.sourceType": [
          { required: true, message: "请选择查询类型", trigger: "change" }
        ],
        "queryConfig.tableName": [
          { required: true, message: "请输入表名", trigger: "blur" }
        ],
        "queryConfig.sql": [
          { required: true, message: "请输入SQL语句", trigger: "blur" }
        ],
        "extractConfig.schedule.cronExpression": [
          { required: true, message: "请输入Cron表达式", trigger: "blur" }
        ],
        "extractConfig.incrementalField": [
          { required: true, message: "请输入增量字段", trigger: "blur" }
        ]
      },
      // 数据预览相关
      previewDialogVisible: false,
      previewLoading: false,
      previewData: [],
      previewColumns: [],
      previewFilters: [],
      previewTotal: 0,
      previewQueryParams: {
        pageNum: 1,
        pageSize: 10
      },
      currentDatasetFields: [],
      // 表结构信息
      tableSchema: null,
      dimensions: [],
      measures: [],
      // 表数据预览
      tablePreviewColumns: [],
      tablePreviewRows: [],
      tablePreviewTotal: 0,
      tablePreviewLoading: false
    }
  },
  created() {
    this.getList()
    this.getDataSources()
  },
  watch: {
    // 监听表名变化，自动加载表结构和数据预览
    'form.queryConfig.tableName'(newVal) {
      if (newVal && this.form.datasourceId && this.form.queryConfig.sourceType === 'table') {
        this.loadTableSchemaAndPreview()
      }
    }
  },
  methods: {
    /** 查询数据集列表 */
    getList() {
      this.loading = true
      listDataset(this.queryParams).then(response => {
        this.datasetList = response.rows
        this.total = response.total
        this.loading = false
      })
    },

    /** 获取数据源列表 */
    getDataSources() {
      getDataSources().then(response => {
        this.datasourceList = response.rows
      })
    },

    /** 根据数据源ID获取名称 */
    getDatasourceName(datasourceId) {
      const ds = this.datasourceList.find(item => item.id === datasourceId)
      return ds ? ds.name : '未知数据源'
    },

    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },

    // 表单重置
    reset() {
      this.form = {
        name: undefined,
        datasourceId: undefined,
        type: "direct",
        queryConfig: {
          sourceType: "table",
          tableName: "",
          sql: "",
          joins: []
        },
        fieldConfig: {
          fields: []
        },
        extractConfig: {
          schedule: {
            enabled: false,
            cronExpression: "0 0 2 * * ?"
          },
          incremental: false,
          incrementalField: ""
        },
        status: "0",
        remark: undefined
      }
      this.resetForm("form")
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },

    /** 新增按钮操作 */
    handleAdd() {
      try {
        this.reset()
        this.$nextTick(() => {
          this.open = true
          this.title = "添加数据集"
        })
      } catch (error) {
        console.error('打开对话框失败:', error)
        this.$modal.msgError("打开对话框失败: " + error.message)
      }
    },

    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const datasetId = row.id || this.ids[0]
      getDataset(datasetId).then(response => {
        this.form = response.data

        // 解析JSON配置
        if (this.form.queryConfig && typeof this.form.queryConfig === 'string') {
          this.form.queryConfig = JSON.parse(this.form.queryConfig)
        } else if (!this.form.queryConfig) {
          this.form.queryConfig = {
            sourceType: "table",
            tableName: "",
            sql: "",
            joins: []
          }
        }

        if (this.form.fieldConfig && typeof this.form.fieldConfig === 'string') {
          this.form.fieldConfig = JSON.parse(this.form.fieldConfig)
        } else if (!this.form.fieldConfig) {
          this.form.fieldConfig = { fields: [] }
        }

        if (this.form.extractConfig && typeof this.form.extractConfig === 'string') {
          this.form.extractConfig = JSON.parse(this.form.extractConfig)
        } else if (!this.form.extractConfig) {
          this.form.extractConfig = {
            schedule: { enabled: false, cronExpression: "0 0 2 * * ?" },
            incremental: false,
            incrementalField: ""
          }
        }

        this.open = true
        this.title = "修改数据集"
      })
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const formData = {
            ...this.form,
            queryConfig: JSON.stringify(this.form.queryConfig),
            fieldConfig: JSON.stringify(this.form.fieldConfig),
            extractConfig: JSON.stringify(this.form.extractConfig)
          }

          if (this.form.id != undefined) {
            updateDataset(formData).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addDataset(formData).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const datasetIds = row.id || this.ids
      this.$modal.confirm('是否确认删除数据集编号为"' + datasetIds + '"的数据项？').then(function () {
        return delDataset(datasetIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },

    /** 预览数据 */
    handlePreview(row) {
      const datasetId = row.id || this.ids[0]
      this.previewDialogVisible = true
      this.previewFilters = []
      this.previewData = []
      this.previewColumns = []
      this.previewTotal = 0

      // 获取数据集详情以获取字段信息
      getDataset(datasetId).then(response => {
        const dataset = response.data
        if (dataset.fieldConfig && typeof dataset.fieldConfig === 'string') {
          dataset.fieldConfig = JSON.parse(dataset.fieldConfig)
        }
        this.currentDatasetFields = dataset.fieldConfig?.fields || []
        this.executePreview(datasetId)
      })
    },

    /** 执行数据预览 */
    executePreview(datasetId) {
      this.previewLoading = true
      const filters = this.previewFilters.filter(f =>
        f.field && (f.value || ['is_null', 'not_null'].includes(f.operator))
      )

      previewDataset(datasetId || this.currentPreviewDatasetId, filters).then(response => {
        this.previewData = response.data
        if (this.previewData.length > 0) {
          // 从第一行数据推断列结构
          this.previewColumns = Object.keys(this.previewData[0]).map(key => {
            const field = this.currentDatasetFields.find(f => f.name === key)
            return {
              name: key,
              alias: field ? (field.alias || key) : key
            }
          })
        }
        this.previewLoading = false
      }).catch(() => {
        this.previewLoading = false
      })
    },

    /** 添加预览筛选 */
    addPreviewFilter() {
      this.previewFilters.push({
        field: '',
        operator: 'eq',
        value: '',
        value2: ''
      })
    },

    /** 删除预览筛选 */
    removePreviewFilter(index) {
      this.previewFilters.splice(index, 1)
    },

    /** 预览分页大小改变 */
    handlePreviewSizeChange(val) {
      this.previewQueryParams.pageSize = val
      this.executePreview()
    },

    /** 预览页码改变 */
    handlePreviewCurrentChange(val) {
      this.previewQueryParams.pageNum = val
      this.executePreview()
    },

    /** 立即抽取 */
    handleExtract(row) {
      const datasetId = row.id || this.ids[0]
      if (row.type !== 'extract') {
        this.$modal.msgError("只有抽取类型数据集才能执行抽取操作")
        return
      }

      this.$modal.confirm('确定要立即执行数据抽取吗？').then(() => {
        this.$modal.loading("正在执行抽取...")
        executeExtract(datasetId).then(response => {
          this.$modal.closeLoading()
          this.$modal.msgSuccess("抽取完成")
          this.getList()
        }).catch(() => {
          this.$modal.closeLoading()
        })
      })
    },

    /** 字段配置变更处理 */
    handleFieldChange(fields) {
      // 可以在这里添加字段验证逻辑
    },

    /** 数据源变更处理 */
    handleDataSourceChange(dataSourceId) {
      // 清空表列表和表名
      this.tableList = []
      if (this.form.queryConfig) {
        this.form.queryConfig.tableName = ''
      }
      
      // 自动加载表列表
      if (dataSourceId && this.form.queryConfig && this.form.queryConfig.sourceType === 'table') {
        this.loadTableList()
      }
    },

    /** 加载表列表 */
    loadTableList() {
      if (!this.form.datasourceId) {
        this.$modal.msgWarning("请先选择数据源")
        return
      }

      this.tableListLoading = true
      getDataSourceTables(this.form.datasourceId).then(response => {
        this.tableList = response.data || []
        if (this.tableList.length === 0) {
          this.$modal.msgWarning("该数据源没有可用的表")
        }
        this.tableListLoading = false
      }).catch(error => {
        console.error('加载表列表失败:', error)
        this.$modal.msgError("加载表列表失败: " + (error.message || '未知错误'))
        this.tableList = []
        this.tableListLoading = false
      })
    },

    /** 加载表结构和数据预览 */
    async loadTableSchemaAndPreview() {
      try {
        this.tablePreviewLoading = true
        
        // 加载表结构
        await this.loadTableSchema()
        
        // 加载数据预览
        await this.loadTableDataPreview()
        
        this.$modal.msgSuccess('数据预览加载成功')
      } catch (error) {
        console.error('加载失败:', error)
        this.$modal.msgError('加载失败: ' + (error.message || '未知错误'))
      } finally {
        this.tablePreviewLoading = false
      }
    },

    /** 加载表结构 */
    async loadTableSchema() {
      const response = await getTableSchema(
        this.form.datasourceId,
        this.form.queryConfig.tableName
      )
      
      this.tableSchema = response.data
      
      // 分类字段为维度和指标
      this.dimensions = response.data.columns.filter(c => c.fieldType === 'DIMENSION')
      this.measures = response.data.columns.filter(c => c.fieldType === 'MEASURE')

      // 自动填充字段配置
      this.form.fieldConfig.fields = response.data.columns.map(col => ({
        name: col.columnName,
        alias: col.columnComment || col.columnName,
        type: this.mapDataType(col.dataType),
        visible: true,
        calculated: false,
        expression: '',
        fieldType: col.fieldType
      }))
    },

    /** 加载表数据预览 */
    async loadTableDataPreview() {
      const response = await getTablePreview(
        this.form.datasourceId,
        this.form.queryConfig.tableName,
        10
      )
      
      this.tablePreviewColumns = response.data.columns
      this.tablePreviewRows = response.data.rows
      this.tablePreviewTotal = response.data.total
    },

    /** 映射数据库类型到系统类型 */
    mapDataType(dbType) {
      const type = dbType.toUpperCase()
      if (type.includes('INT')) return 'INTEGER'
      if (type.includes('DECIMAL') || type.includes('NUMERIC') || 
          type.includes('DOUBLE') || type.includes('FLOAT') || type.includes('NUMBER')) return 'DECIMAL'
      if (type.includes('DATE') || type.includes('TIME')) return 'DATE'
      if (type.includes('BOOL')) return 'BOOLEAN'
      return 'STRING'
    }
  }
}
</script>

<style scoped>
.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.preview-toolbar {
  margin-bottom: 15px;
}

.preview-filters {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.preview-filters .el-row {
  margin-bottom: 10px;
}

.text-muted {
  color: #909399;
}

.field-category-title {
  font-weight: bold;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.field-category-title i {
  margin-right: 5px;
  color: #409eff;
}
</style>