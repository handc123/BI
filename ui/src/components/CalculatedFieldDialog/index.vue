<template>
  <el-drawer
    :visible.sync="dialogVisible"
    :before-close="handleClose"
    direction="rtl"
    size="60%"
    :title="isEdit ? '编辑计算字段' : '新建计算字段'"
    class="calculated-field-drawer"
  >
    <div class="drawer-content">
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="英文名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入字段英文名称，如: npl_ratio"
            :disabled="isEdit"
          >
            <template slot="append">
              <el-tooltip content="字段的唯一标识符，只能包含字母、数字和下划线" placement="top">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="中文名称" prop="alias">
          <el-input
            v-model="form.alias"
            placeholder="请输入字段中文名称，如: 不良贷款率(%)"
          >
            <template slot="append">
              <el-tooltip content="字段的显示名称，用于界面展示" placement="top">
                <i class="el-icon-question"></i>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="字段类型" prop="fieldType">
          <el-radio-group v-model="form.fieldType">
            <el-radio label="dimension">维度</el-radio>
            <el-radio label="metric">指标</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="表达式" prop="expression" class="expression-form-item">
          <expression-editor
            ref="expressionEditor"
            v-model="form.expression"
            :dataset-fields="datasetFields"
            :datasource-type="datasourceType"
            @validate="handleExpressionValidate"
          />
        </el-form-item>

        <el-form-item
          v-if="form.fieldType === 'metric'"
          label="聚合方式"
          prop="aggregation"
        >
          <el-select v-model="form.aggregation" placeholder="请选择聚合方式">
            <el-option label="自动" value="AUTO"></el-option>
            <el-option label="求和(SUM)" value="SUM"></el-option>
            <el-option label="平均值(AVG)" value="AVG"></el-option>
            <el-option label="最大值(MAX)" value="MAX"></el-option>
            <el-option label="最小值(MIN)" value="MIN"></el-option>
            <el-option label="计数(COUNT)" value="COUNT"></el-option>
          </el-select>
          <div class="form-hint">
            自动模式：如果表达式已包含聚合函数，则不再添加聚合
          </div>
        </el-form-item>

        <el-form-item label="SQL预览">
          <div class="sql-preview">
            <el-input
              v-model="sqlPreview"
              type="textarea"
              :rows="3"
              readonly
              placeholder="保存后将显示生成的SQL"
            ></el-input>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <div class="drawer-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="validating" @click="handleValidate">
        验证表达式
      </el-button>
      <el-button type="success" :loading="saving" @click="handleSave">
        保存
      </el-button>
      <el-button
        v-if="isEdit"
        type="danger"
        :loading="deleting"
        @click="handleDelete"
      >
        删除
      </el-button>
    </div>
  </el-drawer>
</template>

<script>
import ExpressionEditor from '../ExpressionEditor';
import { validateCalculatedField } from '@/api/bi/component';

export default {
  name: 'CalculatedFieldDialog',
  components: {
    ExpressionEditor
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    field: {
      type: Object,
      default: null
    },
    datasetFields: {
      type: Array,
      default: () => []
    },
    existingFields: {
      type: Array,
      default: () => []
    },
    datasetId: {
      type: Number,
      default: null
    },
    datasourceType: {
      type: String,
      default: 'MySQL'
    }
  },
  data() {
    return {
      dialogVisible: false,
      form: {
        name: '',
        alias: '',
        fieldType: 'metric',
        expression: '',
        aggregation: 'AUTO'
      },
      rules: {
        name: [
          { required: true, message: '请输入英文名称', trigger: 'blur' },
          {
            pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/,
            message: '只能包含字母、数字和下划线，且必须以字母或下划线开头',
            trigger: 'blur'
          },
          { validator: this.validateFieldName, trigger: 'blur' }
        ],
        alias: [
          { required: true, message: '请输入中文名称', trigger: 'blur' }
        ],
        fieldType: [
          { required: true, message: '请选择字段类型', trigger: 'change' }
        ],
        expression: [
          { required: true, message: '请输入表达式', trigger: 'blur' }
        ]
      },
      sqlPreview: '',
      validating: false,
      saving: false,
      deleting: false,
      isEdit: false,
      expressionValid: false
    };
  },
  watch: {
    visible(val) {
      this.dialogVisible = val;
      if (val) {
        this.initForm();
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('update:visible', false);
      }
    }
  },
  methods: {
    initForm() {
      if (this.field) {
        this.isEdit = true;
        this.form = {
          name: this.field.name,
          alias: this.field.alias,
          fieldType: this.field.fieldType,
          expression: this.field.expression,
          aggregation: this.field.aggregation || 'AUTO'
        };
        this.sqlPreview = this.field.sqlPreview || '';
      } else {
        this.isEdit = false;
        this.form = {
          name: '',
          alias: '',
          fieldType: 'metric',
          expression: '',
          aggregation: 'AUTO'
        };
        this.sqlPreview = '';
      }
      this.expressionValid = false;
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate();
        }
      });
    },
    validateFieldName(rule, value, callback) {
      if (!value) {
        callback();
        return;
      }
      // Check for duplicate names
      const duplicate = this.existingFields.find(
        f => f.name === value && (!this.isEdit || f.name !== this.field.name)
      );
      if (duplicate) {
        callback(new Error('字段名称已存在'));
      } else {
        callback();
      }
    },
    handleExpressionValidate(valid, message) {
      this.expressionValid = valid;
      if (!valid && message) {
        this.$message.error(message);
      }
    },
    async handleValidate() {
      try {
        await this.$refs.form.validate();
      } catch (error) {
        this.$message.warning('请先完善表单信息');
        return;
      }

      if (!this.datasetId) {
        this.$message.warning('请先选择数据集');
        return;
      }

      this.validating = true;
      try {
        const response = await validateCalculatedField({
          datasetId: this.datasetId,
          field: {
            name: this.form.name,
            alias: this.form.alias,
            fieldType: this.form.fieldType,
            expression: this.form.expression,
            aggregation: this.form.aggregation
          }
        });

        if (response.data.valid) {
          this.sqlPreview = response.data.sqlPreview || '';
          this.$message.success('表达式验证通过');
          this.expressionValid = true;
        } else {
          this.$message.error(response.data.message || '表达式验证失败');
          this.expressionValid = false;
        }
      } catch (error) {
        console.error('Validation error:', error);
        this.$message.error(error.message || '验证失败');
        this.expressionValid = false;
      } finally {
        this.validating = false;
      }
    },
    async handleSave() {
      try {
        await this.$refs.form.validate();
      } catch (error) {
        this.$message.warning('请先完善表单信息');
        return;
      }

      if (!this.expressionValid) {
        this.$message.warning('请先验证表达式');
        return;
      }

      this.saving = true;
      try {
        const fieldData = {
          name: this.form.name,
          alias: this.form.alias,
          fieldType: this.form.fieldType,
          expression: this.form.expression,
          aggregation: this.form.aggregation,
          sqlPreview: this.sqlPreview
        };

        this.$emit('save', fieldData);
        this.$message.success(this.isEdit ? '更新成功' : '创建成功');
        this.handleClose();
      } catch (error) {
        console.error('Save error:', error);
        this.$message.error(error.message || '保存失败');
      } finally {
        this.saving = false;
      }
    },
    handleDelete() {
      this.$confirm('确定要删除此计算字段吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.deleting = true;
        try {
          this.$emit('delete', this.field);
          this.$message.success('删除成功');
          this.handleClose();
        } catch (error) {
          console.error('Delete error:', error);
          this.$message.error(error.message || '删除失败');
        } finally {
          this.deleting = false;
        }
      }).catch(() => {});
    },
    handleClose() {
      this.dialogVisible = false;
      this.$emit('close');
    }
  }
};
</script>

<style scoped lang="scss">
.calculated-field-drawer {
  ::v-deep .el-drawer__body {
    display: flex;
    flex-direction: column;
    padding: 0;
  }

  .drawer-content {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
  }

  .drawer-footer {
    padding: 16px 20px;
    border-top: 1px solid #e4e7ed;
    text-align: right;
    background: #f5f7fa;

    .el-button {
      margin-left: 8px;
    }
  }

  .expression-form-item {
    ::v-deep .el-form-item__content {
      line-height: normal;
    }
  }

  .form-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.5;
  }

  .sql-preview {
    ::v-deep .el-textarea__inner {
      font-family: 'Courier New', monospace;
      font-size: 12px;
      background: #f5f7fa;
    }
  }
}
</style>
