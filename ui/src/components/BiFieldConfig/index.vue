<template>
  <div class="bi-field-config">
    <div class="toolbar">
      <el-button size="mini" type="primary" icon="el-icon-plus" @click="openFieldDialog">新增计算字段</el-button>
    </div>
    <el-table :data="localFields" size="mini" border>
      <el-table-column label="#" width="50">
        <template slot-scope="scope">
          <span>{{ scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="字段名" prop="name" width="160">
        <template slot-scope="scope">
          <span>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="别名" prop="alias" width="160">
        <template slot-scope="scope">
          <span>{{ scope.row.alias }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" prop="type" width="100">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.type === 'STRING' ? 'info' : 'success'">
            {{ getTypeLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数据类型" prop="fieldType" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.fieldType === 'DIMENSION' ? 'primary' : 'warning'">
            {{ scope.row.fieldType === 'DIMENSION' ? '维度' : '指标' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计算字段" prop="calculated" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.calculated" size="mini" type="success">是</el-tag>
          <el-tag v-else size="mini" type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="表达式" prop="expression" show-overflow-tooltip>
        <template slot-scope="scope">
          <span v-if="scope.row.calculated">{{ scope.row.expression }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-edit" @click="editField(scope.$index)">编辑</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="removeField(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑计算字段对话框 -->
    <el-dialog
      :title="fieldDialogTitle"
      :visible.sync="fieldDialogVisible"
      width="1000px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form :model="currentField" :rules="fieldRules" ref="fieldForm" label-width="100px" size="small">
        <el-form-item label="字段名称" prop="name">
          <el-input v-model="currentField.name" placeholder="请输入字段名称" style="width: 400px" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据类型">
              <el-radio-group v-model="currentField.fieldType">
                <el-radio label="DIMENSION">维度</el-radio>
                <el-radio label="MEASURE">指标</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字段类型" prop="type">
              <el-select v-model="currentField.type" placeholder="请选择字段类型" style="width: 100%">
                <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="字段表达式" prop="expression">
          <div class="expression-editor-container">
            <!-- 表达式输入区 -->
            <div class="expression-input">
              <el-input
                v-model="currentField.expression"
                type="textarea"
                :rows="10"
                placeholder="请输入字段表达式，例如：SUM(amount) / COUNT(*)"
                class="expression-textarea"
              />
            </div>

            <!-- 右侧面板 -->
            <div class="reference-panels">
              <!-- 引用字段面板 -->
              <div class="reference-panel">
                <div class="panel-header">
                  <span>点击引用字段</span>
                  <el-input
                    v-model="fieldSearchText"
                    placeholder="通过名称搜索"
                    size="mini"
                    prefix-icon="el-icon-search"
                    clearable
                  />
                </div>
                <div class="panel-content">
                  <div class="field-group">
                    <div class="group-title">维度</div>
                    <div
                      v-for="field in filteredDimensionFields"
                      :key="field.name"
                      class="field-item"
                      @click="insertField(field.name)"
                    >
                      <i class="el-icon-s-grid"></i>
                      <span>{{ field.alias || field.name }}</span>
                    </div>
                  </div>
                  <div class="field-group">
                    <div class="group-title">指标</div>
                    <div
                      v-for="field in filteredMeasureFields"
                      :key="field.name"
                      class="field-item"
                      @click="insertField(field.name)"
                    >
                      <i class="el-icon-s-data"></i>
                      <span>{{ field.alias || field.name }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 引用函数面板 -->
              <div class="reference-panel">
                <div class="panel-header">
                  <span>点击引用函数</span>
                  <el-input
                    v-model="functionSearchText"
                    placeholder="通过名称搜索"
                    size="mini"
                    prefix-icon="el-icon-search"
                    clearable
                  />
                </div>
                <div class="panel-content">
                  <div
                    v-for="func in filteredFunctions"
                    :key="func.name"
                    class="function-item"
                    @click="insertFunction(func.name)"
                  >
                    <span class="function-name">{{ func.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fieldDialogVisible = false">取消</el-button>
        <el-button @click="validateField">校验</el-button>
        <el-button type="primary" @click="saveField">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'BiFieldConfig',
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      localFields: [],
      typeOptions: [
        { label: '字符串', value: 'STRING' },
        { label: '整数', value: 'INTEGER' },
        { label: '小数', value: 'DECIMAL' },
        { label: '日期', value: 'DATE' },
        { label: '布尔', value: 'BOOLEAN' }
      ],
      
      // 对话框相关
      fieldDialogVisible: false,
      fieldDialogTitle: '新增计算字段',
      currentField: this.getEmptyField(),
      editingIndex: -1,
      
      // 搜索文本
      fieldSearchText: '',
      functionSearchText: '',
      
      // 函数列表
      functions: [
        // 字符串函数
        { name: 'SUBSTRING(s,n,len)', category: 'string' },
        { name: 'CONCAT(s1,s2...)', category: 'string' },
        { name: 'UPPER(s)', category: 'string' },
        { name: 'LOWER(s)', category: 'string' },
        { name: 'TRIM(s)', category: 'string' },
        { name: 'LENGTH(s)', category: 'string' },
        { name: 'REPLACE(s,old,new)', category: 'string' },
        
        // 数学函数
        { name: 'ABS(x)', category: 'math' },
        { name: 'CEIL(x)', category: 'math' },
        { name: 'FLOOR(x)', category: 'math' },
        { name: 'ROUND(x)', category: 'math' },
        { name: 'ROUND(x,y)', category: 'math' },
        { name: 'SQRT(x)', category: 'math' },
        { name: 'POW(x,y)', category: 'math' },
        { name: 'MOD(x,y)', category: 'math' },
        
        // 聚合函数
        { name: 'COUNT(x)', category: 'aggregate' },
        { name: 'SUM(x)', category: 'aggregate' },
        { name: 'AVG(x)', category: 'aggregate' },
        { name: 'MAX(x)', category: 'aggregate' },
        { name: 'MIN(x)', category: 'aggregate' },
        { name: 'COUNT(DISTINCT x)', category: 'aggregate' },
        
        // 日期函数
        { name: 'NOW()', category: 'date' },
        { name: 'CURDATE()', category: 'date' },
        { name: 'DATE_FORMAT(date,fmt)', category: 'date' },
        { name: 'YEAR(date)', category: 'date' },
        { name: 'MONTH(date)', category: 'date' },
        { name: 'DAY(date)', category: 'date' },
        { name: 'DATEDIFF(date1,date2)', category: 'date' },
        
        // 条件函数
        { name: 'IF(condition,true,false)', category: 'condition' },
        { name: 'CASE WHEN ... THEN ... END', category: 'condition' },
        { name: 'COALESCE(v1,v2...)', category: 'condition' },
        { name: 'NULLIF(v1,v2)', category: 'condition' }
      ],
      
      // 表单验证规则
      fieldRules: {
        name: [
          { required: true, message: '请输入字段名称', trigger: 'blur' },
          { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '字段名只能包含字母、数字和下划线，且不能以数字开头', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择字段类型', trigger: 'change' }
        ],
        expression: [
          { required: true, message: '请输入字段表达式', trigger: 'blur' }
        ]
      }
    }
  },
  
  computed: {
    // 过滤后的维度字段
    filteredDimensionFields() {
      const searchText = this.fieldSearchText.toLowerCase()
      return this.localFields.filter(f => 
        f.fieldType === 'DIMENSION' && 
        !f.calculated &&
        (f.name.toLowerCase().includes(searchText) || 
         (f.alias && f.alias.toLowerCase().includes(searchText)))
      )
    },
    
    // 过滤后的指标字段
    filteredMeasureFields() {
      const searchText = this.fieldSearchText.toLowerCase()
      return this.localFields.filter(f => 
        f.fieldType === 'MEASURE' && 
        !f.calculated &&
        (f.name.toLowerCase().includes(searchText) || 
         (f.alias && f.alias.toLowerCase().includes(searchText)))
      )
    },
    
    // 过滤后的函数列表
    filteredFunctions() {
      const searchText = this.functionSearchText.toLowerCase()
      if (!searchText) {
        return this.functions
      }
      return this.functions.filter(f => 
        f.name.toLowerCase().includes(searchText)
      )
    }
  },
  
  watch: {
    value: {
      handler(val) {
        // 避免无限循环：只在值真正改变时更新
        const newVal = Array.isArray(val) ? JSON.parse(JSON.stringify(val)) : []
        if (JSON.stringify(newVal) !== JSON.stringify(this.localFields)) {
          this.localFields = newVal
        }
      },
      deep: true,
      immediate: true
    },
    localFields: {
      handler(val) {
        // 使用 nextTick 避免同步更新导致的循环
        this.$nextTick(() => {
          this.$emit('input', val)
        })
      },
      deep: true
    }
  },
  
  methods: {
    getEmptyField() {
      return {
        name: '',
        alias: '',
        type: 'STRING',
        visible: true,
        calculated: true,
        expression: '',
        fieldType: 'DIMENSION'
      }
    },
    
    openFieldDialog() {
      this.fieldDialogTitle = '新增计算字段'
      this.currentField = this.getEmptyField()
      this.editingIndex = -1
      this.fieldSearchText = ''
      this.functionSearchText = ''
      this.fieldDialogVisible = true
      
      // 重置表单验证
      this.$nextTick(() => {
        if (this.$refs.fieldForm) {
          this.$refs.fieldForm.clearValidate()
        }
      })
    },
    
    editField(index) {
      this.fieldDialogTitle = '编辑计算字段'
      this.currentField = JSON.parse(JSON.stringify(this.localFields[index]))
      this.editingIndex = index
      this.fieldSearchText = ''
      this.functionSearchText = ''
      this.fieldDialogVisible = true
      
      // 重置表单验证
      this.$nextTick(() => {
        if (this.$refs.fieldForm) {
          this.$refs.fieldForm.clearValidate()
        }
      })
    },
    
    saveField() {
      this.$refs.fieldForm.validate(valid => {
        if (valid) {
          // 设置别名（如果为空，使用字段名）
          if (!this.currentField.alias) {
            this.currentField.alias = this.currentField.name
          }
          
          if (this.editingIndex >= 0) {
            // 编辑模式
            this.$set(this.localFields, this.editingIndex, { ...this.currentField })
          } else {
            // 新增模式
            this.localFields.push({ ...this.currentField })
          }
          
          this.fieldDialogVisible = false
          this.$message.success(this.editingIndex >= 0 ? '字段更新成功' : '字段添加成功')
        }
      })
    },
    
    removeField(index) {
      this.$confirm('确定要删除这个字段吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.localFields.splice(index, 1)
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    
    insertField(fieldName) {
      // 在光标位置插入字段名
      const textarea = this.$el.querySelector('.expression-textarea textarea')
      if (textarea) {
        const start = textarea.selectionStart
        const end = textarea.selectionEnd
        const text = this.currentField.expression
        
        this.currentField.expression = text.substring(0, start) + fieldName + text.substring(end)
        
        // 设置光标位置
        this.$nextTick(() => {
          textarea.focus()
          const newPos = start + fieldName.length
          textarea.setSelectionRange(newPos, newPos)
        })
      }
    },
    
    insertFunction(functionName) {
      // 在光标位置插入函数
      const textarea = this.$el.querySelector('.expression-textarea textarea')
      if (textarea) {
        const start = textarea.selectionStart
        const end = textarea.selectionEnd
        const text = this.currentField.expression
        
        this.currentField.expression = text.substring(0, start) + functionName + text.substring(end)
        
        // 设置光标位置到函数括号内
        this.$nextTick(() => {
          textarea.focus()
          const openParen = functionName.indexOf('(')
          if (openParen > 0) {
            const newPos = start + openParen + 1
            textarea.setSelectionRange(newPos, newPos)
          }
        })
      }
    },
    
    validateField() {
      // 校验字段表达式
      if (!this.currentField.expression) {
        this.$message.warning('请输入字段表达式')
        return
      }
      
      // 这里可以添加更复杂的表达式校验逻辑
      // 例如：检查括号是否匹配、函数名是否正确等
      
      this.$message.success('表达式格式正确')
    },
    
    getTypeLabel(type) {
      const option = this.typeOptions.find(opt => opt.value === type)
      return option ? option.label : type
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 8px;
}

.text-muted {
  color: #909399;
}

/* 表达式编辑器容器 */
.expression-editor-container {
  display: flex;
  gap: 10px;
  height: 400px;
}

.expression-input {
  flex: 1;
  min-width: 0;
}

.expression-textarea >>> .el-textarea__inner {
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
}

/* 右侧引用面板 */
.reference-panels {
  display: flex;
  gap: 10px;
  width: 450px;
}

.reference-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  overflow: hidden;
}

.panel-header {
  padding: 10px;
  background: #F5F7FA;
  border-bottom: 1px solid #DCDFE6;
}

.panel-header span {
  display: block;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #303133;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

/* 字段组 */
.field-group {
  margin-bottom: 12px;
}

.field-group:last-child {
  margin-bottom: 0;
}

.group-title {
  font-size: 12px;
  color: #909399;
  padding: 4px 8px;
  margin-bottom: 4px;
  font-weight: 500;
}

.field-item {
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 3px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.field-item:hover {
  background: #ECF5FF;
  color: #409EFF;
}

.field-item i {
  font-size: 14px;
}

/* 函数项 */
.function-item {
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 3px;
  font-size: 13px;
  transition: all 0.2s;
  margin-bottom: 2px;
}

.function-item:hover {
  background: #ECF5FF;
  color: #409EFF;
}

.function-name {
  font-family: 'Courier New', Courier, monospace;
  font-weight: 500;
}

/* 滚动条样式 */
.panel-content::-webkit-scrollbar {
  width: 6px;
}

.panel-content::-webkit-scrollbar-thumb {
  background: #DCDFE6;
  border-radius: 3px;
}

.panel-content::-webkit-scrollbar-thumb:hover {
  background: #C0C4CC;
}

/* 对话框底部按钮 */
.dialog-footer {
  text-align: right;
}
</style>

