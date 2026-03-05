<template>
  <div class="expression-editor">
    <div class="editor-layout">
      <!-- 左侧：可用字段列表 -->
      <div class="fields-panel">
        <div class="panel-header">
          <span>可用字段</span>
          <el-tooltip content="点击字段名插入到表达式中" placement="top">
            <i class="el-icon-question"></i>
          </el-tooltip>
        </div>
        <div class="fields-list">
          <!-- 维度字段 -->
          <div v-if="dimensionFields.length > 0" class="field-group">
            <div class="group-title">维度字段</div>
            <div
              v-for="field in dimensionFields"
              :key="field.name"
              class="field-item"
              :title="`${field.comment || field.name} (${field.name})`"
              @click="insertField(field)"
            >
              <i class="el-icon-s-grid"></i>
              <span class="field-label">{{ field.comment || field.name }}</span>
            </div>
          </div>

          <!-- 指标字段 -->
          <div v-if="metricFields.length > 0" class="field-group">
            <div class="group-title">指标字段</div>
            <div
              v-for="field in metricFields"
              :key="field.name"
              class="field-item"
              :title="`${field.comment || field.name} (${field.name})`"
              @click="insertField(field)"
            >
              <i class="el-icon-s-data"></i>
              <span class="field-label">{{ field.comment || field.name }}</span>
            </div>
          </div>

          <div v-if="datasetFields.length === 0" class="empty-hint">
            暂无可用字段
          </div>
        </div>
      </div>

      <!-- 右侧：表达式编辑区 -->
      <div class="editor-panel">
        <div class="panel-header">
          <span>表达式</span>
          <span class="hint-text">使用英文字段名</span>
        </div>

        <!-- 表达式输入框 -->
        <el-input
          ref="expressionInput"
          v-model="localExpression"
          type="textarea"
          :rows="6"
          placeholder="请输入表达式，如: jkye / 1000000"
          @input="handleExpressionChange"
          @blur="handleBlur"
        ></el-input>

        <!-- 运算符按钮 -->
        <div class="operators-bar">
          <span class="operators-label">运算符:</span>
          <el-button-group>
            <el-button size="mini" @click="insertOperator('+')">+</el-button>
            <el-button size="mini" @click="insertOperator('-')">-</el-button>
            <el-button size="mini" @click="insertOperator('*')">*</el-button>
            <el-button size="mini" @click="insertOperator('/')">/</el-button>
            <el-button size="mini" @click="insertOperator('(')">(</el-button>
            <el-button size="mini" @click="insertOperator(')')">)</el-button>
          </el-button-group>
        </div>

        <!-- 函数面板（可折叠） -->
        <div class="function-toggle">
          <el-button
            type="text"
            size="small"
            @click="showFunctionPanel = !showFunctionPanel"
          >
            <i :class="showFunctionPanel ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
            {{ showFunctionPanel ? '隐藏函数' : '显示函数' }}
          </el-button>
        </div>

        <function-panel
          v-if="showFunctionPanel"
          :datasource-type="datasourceType"
          @insert-function="insertFunction"
        />

        <!-- 实时验证结果 -->
        <div class="validation-result">
          <div v-if="validationMessage" :class="['validation-message', validationStatus]">
            <i :class="validationStatus === 'success' ? 'el-icon-success' : 'el-icon-error'"></i>
            <span>{{ validationMessage }}</span>
          </div>
        </div>

        <!-- 说明文字 -->
        <div class="editor-hint">
          <i class="el-icon-info"></i>
          <span>点击左侧字段名可快速插入到表达式中</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FunctionPanel from '../FunctionPanel';

export default {
  name: 'ExpressionEditor',
  components: {
    FunctionPanel
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    datasetFields: {
      type: Array,
      default: () => []
    },
    datasourceType: {
      type: String,
      default: 'MySQL'
    }
  },
  data() {
    return {
      localExpression: '',
      showFunctionPanel: false,
      cursorPosition: 0,
      validationMessage: '',
      validationStatus: '', // 'success' or 'error'
      validationTimer: null
    };
  },
  computed: {
    dimensionFields() {
      return this.datasetFields.filter(field => field.fieldType === 'dimension');
    },
    metricFields() {
      return this.datasetFields.filter(field => field.fieldType === 'metric');
    }
  },
  watch: {
    value(val) {
      if (val !== this.localExpression) {
        this.localExpression = val;
      }
    }
  },
  mounted() {
    this.localExpression = this.value;
  },
  methods: {
    insertField(field) {
      this.insertText(field.name);
      this.$message.success(`已插入字段: ${field.name}`);
    },
    insertOperator(operator) {
      this.insertText(` ${operator} `);
    },
    insertFunction(functionTemplate) {
      this.insertText(functionTemplate);
    },
    insertText(text) {
      const textarea = this.$refs.expressionInput.$refs.textarea;
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const before = this.localExpression.substring(0, start);
      const after = this.localExpression.substring(end);
      
      this.localExpression = before + text + after;
      this.cursorPosition = start + text.length;
      
      this.$nextTick(() => {
        textarea.focus();
        textarea.setSelectionRange(this.cursorPosition, this.cursorPosition);
      });
      
      this.handleExpressionChange();
    },
    handleExpressionChange() {
      this.$emit('input', this.localExpression);
      
      // Debounce validation
      if (this.validationTimer) {
        clearTimeout(this.validationTimer);
      }
      
      this.validationTimer = setTimeout(() => {
        this.validateExpression();
      }, 500);
    },
    handleBlur() {
      const textarea = this.$refs.expressionInput.$refs.textarea;
      this.cursorPosition = textarea.selectionStart;
    },
    validateExpression() {
      if (!this.localExpression.trim()) {
        this.validationMessage = '';
        this.validationStatus = '';
        this.$emit('validate', false, '');
        return;
      }

      // Basic client-side validation
      const errors = [];

      // Check for balanced parentheses
      let parenCount = 0;
      for (const char of this.localExpression) {
        if (char === '(') parenCount++;
        if (char === ')') parenCount--;
        if (parenCount < 0) {
          errors.push('括号不匹配');
          break;
        }
      }
      if (parenCount > 0) {
        errors.push('括号不匹配');
      }

      // Check for field references
      const fieldNames = this.datasetFields.map(f => f.name);
      const words = this.localExpression.match(/[a-zA-Z_][a-zA-Z0-9_]*/g) || [];
      const unknownFields = words.filter(word => {
        // Skip SQL keywords and function names
        const sqlKeywords = ['CASE', 'WHEN', 'THEN', 'ELSE', 'END', 'IF', 'AND', 'OR', 'NOT', 'IN', 'IS', 'NULL'];
        const functions = ['SUM', 'AVG', 'MAX', 'MIN', 'COUNT', 'ROUND', 'FLOOR', 'CEIL', 'ABS', 'CONCAT', 'SUBSTRING', 'LENGTH', 'UPPER', 'LOWER', 'TRIM', 'COALESCE', 'NULLIF', 'YEAR', 'MONTH', 'DAY'];
        
        if (sqlKeywords.includes(word.toUpperCase()) || functions.includes(word.toUpperCase())) {
          return false;
        }
        
        return !fieldNames.includes(word);
      });

      if (unknownFields.length > 0) {
        errors.push(`未知字段: ${unknownFields.join(', ')}`);
      }

      if (errors.length > 0) {
        this.validationMessage = errors.join('; ');
        this.validationStatus = 'error';
        this.$emit('validate', false, this.validationMessage);
      } else {
        this.validationMessage = '表达式格式正确';
        this.validationStatus = 'success';
        this.$emit('validate', true, '');
      }
    }
  }
};
</script>

<style scoped lang="scss">
.expression-editor {
  .editor-layout {
    display: flex;
    gap: 16px;
    min-height: 400px;
  }

  .fields-panel {
    width: 200px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    background: #fff;

    .panel-header {
      padding: 10px 12px;
      border-bottom: 1px solid #e4e7ed;
      background: #f5f7fa;
      font-size: 13px;
      font-weight: 600;
      display: flex;
      justify-content: space-between;
      align-items: center;

      i {
        color: #909399;
        cursor: help;
      }
    }

    .fields-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
    }

    .field-group {
      margin-bottom: 12px;

      .group-title {
        font-size: 12px;
        color: #909399;
        padding: 4px 8px;
        font-weight: 600;
      }

      .field-item {
        padding: 6px 8px;
        margin: 2px 0;
        border-radius: 3px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 6px;
        transition: all 0.2s;
        font-size: 12px;

        &:hover {
          background: #ecf5ff;
          color: #409eff;
        }

        i {
          font-size: 12px;
        }

        .field-label {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .empty-hint {
      padding: 20px;
      text-align: center;
      font-size: 12px;
      color: #909399;
    }
  }

  .editor-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      font-weight: 600;

      .hint-text {
        font-size: 12px;
        color: #909399;
        font-weight: normal;
      }
    }

    ::v-deep .el-textarea__inner {
      font-family: 'Courier New', monospace;
      font-size: 13px;
    }

    .operators-bar {
      display: flex;
      align-items: center;
      gap: 8px;

      .operators-label {
        font-size: 12px;
        color: #606266;
      }
    }

    .function-toggle {
      border-top: 1px solid #e4e7ed;
      padding-top: 8px;
    }

    .validation-result {
      min-height: 24px;

      .validation-message {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        padding: 6px 12px;
        border-radius: 4px;

        &.success {
          color: #67c23a;
          background: #f0f9ff;
          border: 1px solid #c6e2ff;

          i {
            color: #67c23a;
          }
        }

        &.error {
          color: #f56c6c;
          background: #fef0f0;
          border: 1px solid #fde2e2;

          i {
            color: #f56c6c;
          }
        }
      }
    }

    .editor-hint {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #909399;
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 4px;

      i {
        font-size: 14px;
      }
    }
  }
}
</style>
