<template>
  <div class="function-panel">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 系统内置函数 -->
      <el-tab-pane label="系统内置函数" name="builtin">
        <div class="function-categories">
          <!-- 聚合函数 -->
          <div class="function-category">
            <div class="category-title">
              <i class="el-icon-s-marketing"></i>
              <span>聚合函数</span>
            </div>
            <div class="function-list">
              <div
                v-for="func in builtinFunctions.aggregation"
                :key="func.name"
                class="function-item"
                @click="insertFunction(func)"
              >
                <div class="function-header">
                  <span class="function-name">{{ func.name }}</span>
                  <span class="function-desc">{{ func.desc }}</span>
                </div>
                <div class="function-syntax">{{ func.syntax }}</div>
                <div class="function-example">示例: {{ func.example }}</div>
              </div>
            </div>
          </div>

          <!-- 逻辑函数 -->
          <div class="function-category">
            <div class="category-title">
              <i class="el-icon-s-operation"></i>
              <span>逻辑函数</span>
            </div>
            <div class="function-list">
              <div
                v-for="func in builtinFunctions.logical"
                :key="func.name"
                class="function-item"
                @click="insertFunction(func)"
              >
                <div class="function-header">
                  <span class="function-name">{{ func.name }}</span>
                  <span class="function-desc">{{ func.desc }}</span>
                </div>
                <div class="function-syntax">{{ func.syntax }}</div>
                <div class="function-example">示例: {{ func.example }}</div>
              </div>
            </div>
          </div>

          <!-- 数值函数 -->
          <div class="function-category">
            <div class="category-title">
              <i class="el-icon-s-data"></i>
              <span>数值函数</span>
            </div>
            <div class="function-list">
              <div
                v-for="func in builtinFunctions.numeric"
                :key="func.name"
                class="function-item"
                @click="insertFunction(func)"
              >
                <div class="function-header">
                  <span class="function-name">{{ func.name }}</span>
                  <span class="function-desc">{{ func.desc }}</span>
                </div>
                <div class="function-syntax">{{ func.syntax }}</div>
                <div class="function-example">示例: {{ func.example }}</div>
              </div>
            </div>
          </div>

          <!-- 文本函数 -->
          <div class="function-category">
            <div class="category-title">
              <i class="el-icon-document"></i>
              <span>文本函数</span>
            </div>
            <div class="function-list">
              <div
                v-for="func in builtinFunctions.text"
                :key="func.name"
                class="function-item"
                @click="insertFunction(func)"
              >
                <div class="function-header">
                  <span class="function-name">{{ func.name }}</span>
                  <span class="function-desc">{{ func.desc }}</span>
                </div>
                <div class="function-syntax">{{ func.syntax }}</div>
                <div class="function-example">示例: {{ func.example }}</div>
              </div>
            </div>
          </div>

          <!-- 日期函数 -->
          <div class="function-category">
            <div class="category-title">
              <i class="el-icon-date"></i>
              <span>日期函数</span>
            </div>
            <div class="function-list">
              <div
                v-for="func in builtinFunctions.date"
                :key="func.name"
                class="function-item"
                @click="insertFunction(func)"
              >
                <div class="function-header">
                  <span class="function-name">{{ func.name }}</span>
                  <span class="function-desc">{{ func.desc }}</span>
                </div>
                <div class="function-syntax">{{ func.syntax }}</div>
                <div class="function-example">示例: {{ func.example }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 数据库函数 -->
      <el-tab-pane :label="`数据库函数(${datasourceType})`" name="native">
        <div v-if="nativeFunctions.length > 0" class="function-categories">
          <div
            v-for="category in nativeFunctionCategories"
            :key="category.name"
            class="function-category"
          >
            <div class="category-title">
              <i :class="category.icon"></i>
              <span>{{ category.label }}</span>
            </div>
            <div class="function-list">
              <div
                v-for="func in category.functions"
                :key="func.name"
                class="function-item"
                @click="insertFunction(func)"
              >
                <div class="function-header">
                  <span class="function-name">{{ func.name }}</span>
                  <span class="function-desc">{{ func.desc }}</span>
                  <el-tag size="mini" type="info">{{ func.database }}</el-tag>
                </div>
                <div class="function-syntax">{{ func.syntax }}</div>
                <div class="function-example">示例: {{ func.example }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-hint">
          <i class="el-icon-info"></i>
          <p>当前数据源类型暂无特定函数</p>
        </div>
      </el-tab-pane>
    </el-tabs>

    <div class="panel-footer">
      <el-button type="text" size="small" @click="showDocumentation">
        <i class="el-icon-document"></i>
        查看函数文档
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FunctionPanel',
  props: {
    datasourceType: {
      type: String,
      default: 'MySQL'
    }
  },
  data() {
    return {
      activeTab: 'builtin',
      builtinFunctions: {
        aggregation: [
          {
            name: 'SUM',
            syntax: 'SUM({column})',
            desc: '求和(系统内置)',
            example: 'SUM(jkye)',
            type: 'builtin'
          },
          {
            name: 'AVG',
            syntax: 'AVG({column})',
            desc: '平均值(系统内置)',
            example: 'AVG(jkye)',
            type: 'builtin'
          },
          {
            name: 'MAX',
            syntax: 'MAX({column})',
            desc: '最大值(系统内置)',
            example: 'MAX(jkye)',
            type: 'builtin'
          },
          {
            name: 'MIN',
            syntax: 'MIN({column})',
            desc: '最小值(系统内置)',
            example: 'MIN(jkye)',
            type: 'builtin'
          },
          {
            name: 'COUNT',
            syntax: 'COUNT({column})',
            desc: '计数(系统内置)',
            example: 'COUNT(jkye)',
            type: 'builtin'
          },
          {
            name: 'COUNT_DISTINCT',
            syntax: 'COUNT(DISTINCT {column})',
            desc: '去重计数(系统内置)',
            example: 'COUNT(DISTINCT sjbsjgid)',
            type: 'builtin'
          }
        ],
        logical: [
          {
            name: 'CASE',
            syntax: 'CASE WHEN {condition} THEN {value} ELSE {default} END',
            desc: '条件判断(系统内置)',
            example: "CASE WHEN wjfl IN ('次级', '可疑', '损失') THEN jkye ELSE 0 END",
            type: 'builtin'
          },
          {
            name: 'IF',
            syntax: 'IF({condition}, {true_value}, {false_value})',
            desc: '简单条件(系统内置)',
            example: "IF(jkye > 1000000, '大额', '小额')",
            type: 'builtin'
          },
          {
            name: 'COALESCE',
            syntax: 'COALESCE({val1}, {val2}, ...)',
            desc: '返回第一个非NULL值(系统内置)',
            example: 'COALESCE(jkye, 0)',
            type: 'builtin'
          },
          {
            name: 'NULLIF',
            syntax: 'NULLIF({val1}, {val2})',
            desc: '如果两值相等返回NULL(系统内置)',
            example: 'NULLIF(jkye, 0)',
            type: 'builtin'
          }
        ],
        numeric: [
          {
            name: 'ROUND',
            syntax: 'ROUND({number}, {decimals})',
            desc: '四舍五入(系统内置)',
            example: 'ROUND(jkye / 1000000, 2)',
            type: 'builtin'
          },
          {
            name: 'ABS',
            syntax: 'ABS({number})',
            desc: '绝对值(系统内置)',
            example: 'ABS(jkye)',
            type: 'builtin'
          },
          {
            name: 'FLOOR',
            syntax: 'FLOOR({number})',
            desc: '向下取整(系统内置)',
            example: 'FLOOR(jkye / 1000000)',
            type: 'builtin'
          },
          {
            name: 'CEIL',
            syntax: 'CEIL({number})',
            desc: '向上取整(系统内置)',
            example: 'CEIL(jkye / 1000000)',
            type: 'builtin'
          },
          {
            name: 'MOD',
            syntax: 'MOD({number}, {divisor})',
            desc: '取模运算(系统内置)',
            example: 'MOD(jkye, 1000)',
            type: 'builtin'
          },
          {
            name: 'POWER',
            syntax: 'POWER({base}, {exponent})',
            desc: '幂运算(系统内置)',
            example: 'POWER(jkye, 2)',
            type: 'builtin'
          }
        ],
        text: [
          {
            name: 'CONCAT',
            syntax: 'CONCAT({str1}, {str2}, ...)',
            desc: '连接字符串(系统内置)',
            example: "CONCAT(sjbsjgid, '-', load_date)",
            type: 'builtin'
          },
          {
            name: 'SUBSTRING',
            syntax: 'SUBSTRING({str}, {start}, {length})',
            desc: '提取子字符串(系统内置)',
            example: 'SUBSTRING(sjbsjgid, 1, 4)',
            type: 'builtin'
          },
          {
            name: 'LENGTH',
            syntax: 'LENGTH({str})',
            desc: '字符串长度(系统内置)',
            example: 'LENGTH(sjbsjgid)',
            type: 'builtin'
          },
          {
            name: 'UPPER',
            syntax: 'UPPER({str})',
            desc: '转大写(系统内置)',
            example: 'UPPER(sjbsjgid)',
            type: 'builtin'
          },
          {
            name: 'LOWER',
            syntax: 'LOWER({str})',
            desc: '转小写(系统内置)',
            example: 'LOWER(sjbsjgid)',
            type: 'builtin'
          },
          {
            name: 'TRIM',
            syntax: 'TRIM({str})',
            desc: '去除首尾空格(系统内置)',
            example: 'TRIM(sjbsjgid)',
            type: 'builtin'
          }
        ],
        date: [
          {
            name: 'YEAR',
            syntax: 'YEAR({date})',
            desc: '提取年份(系统内置)',
            example: 'YEAR(load_date)',
            type: 'builtin'
          },
          {
            name: 'MONTH',
            syntax: 'MONTH({date})',
            desc: '提取月份(系统内置)',
            example: 'MONTH(load_date)',
            type: 'builtin'
          },
          {
            name: 'DAY',
            syntax: 'DAY({date})',
            desc: '提取日期(系统内置)',
            example: 'DAY(load_date)',
            type: 'builtin'
          },
          {
            name: 'DATEDIFF',
            syntax: 'DATEDIFF({date1}, {date2})',
            desc: '日期差(天数)(系统内置)',
            example: 'DATEDIFF(load_date, create_date)',
            type: 'builtin'
          }
        ]
      },
      databaseFunctions: {
        MySQL: {
          text: [
            {
              name: 'GROUP_CONCAT',
              syntax: 'GROUP_CONCAT({column} SEPARATOR {sep})',
              desc: '分组连接(MySQL)',
              example: "GROUP_CONCAT(sjbsjgid SEPARATOR ',')",
              type: 'native',
              database: 'MySQL'
            }
          ],
          date: [
            {
              name: 'DATE_FORMAT',
              syntax: 'DATE_FORMAT({date}, {format})',
              desc: '格式化日期(MySQL)',
              example: "DATE_FORMAT(load_date, '%Y-%m')",
              type: 'native',
              database: 'MySQL'
            }
          ]
        },
        PostgreSQL: {
          text: [
            {
              name: 'STRING_AGG',
              syntax: 'STRING_AGG({column}, {sep})',
              desc: '分组连接(PostgreSQL)',
              example: "STRING_AGG(sjbsjgid, ',')",
              type: 'native',
              database: 'PostgreSQL'
            }
          ],
          date: [
            {
              name: 'TO_CHAR',
              syntax: 'TO_CHAR({date}, {format})',
              desc: '格式化日期(PostgreSQL)',
              example: "TO_CHAR(load_date, 'YYYY-MM')",
              type: 'native',
              database: 'PostgreSQL'
            }
          ]
        },
        ClickHouse: {
          text: [
            {
              name: 'groupArray',
              syntax: 'groupArray({column})',
              desc: '分组数组(ClickHouse)',
              example: 'groupArray(sjbsjgid)',
              type: 'native',
              database: 'ClickHouse'
            }
          ],
          date: [
            {
              name: 'formatDateTime',
              syntax: 'formatDateTime({date}, {format})',
              desc: '格式化日期(ClickHouse)',
              example: "formatDateTime(load_date, '%Y-%m')",
              type: 'native',
              database: 'ClickHouse'
            }
          ]
        }
      }
    };
  },
  computed: {
    nativeFunctions() {
      const dbFuncs = this.databaseFunctions[this.datasourceType];
      if (!dbFuncs) return [];
      
      const allFuncs = [];
      Object.keys(dbFuncs).forEach(category => {
        allFuncs.push(...dbFuncs[category]);
      });
      return allFuncs;
    },
    nativeFunctionCategories() {
      const dbFuncs = this.databaseFunctions[this.datasourceType];
      if (!dbFuncs) return [];
      
      const categories = [];
      if (dbFuncs.text && dbFuncs.text.length > 0) {
        categories.push({
          name: 'text',
          label: '文本函数',
          icon: 'el-icon-document',
          functions: dbFuncs.text
        });
      }
      if (dbFuncs.date && dbFuncs.date.length > 0) {
        categories.push({
          name: 'date',
          label: '日期函数',
          icon: 'el-icon-date',
          functions: dbFuncs.date
        });
      }
      if (dbFuncs.numeric && dbFuncs.numeric.length > 0) {
        categories.push({
          name: 'numeric',
          label: '数值函数',
          icon: 'el-icon-s-data',
          functions: dbFuncs.numeric
        });
      }
      return categories;
    }
  },
  methods: {
    insertFunction(func) {
      this.$emit('insert-function', func.syntax);
      
      if (func.type === 'native') {
        this.$message.info(`已插入${func.database}函数: ${func.name}`);
      } else {
        this.$message.success(`已插入函数: ${func.name}`);
      }
    },
    showDocumentation() {
      this.$alert(
        '函数文档包含所有可用函数的详细说明和示例。系统内置函数会自动转换为目标数据库的SQL语法，而数据库函数则直接传递给数据库执行。',
        '函数文档',
        {
          confirmButtonText: '知道了',
          type: 'info'
        }
      );
    }
  }
};
</script>

<style scoped lang="scss">
.function-panel {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fff;

  ::v-deep .el-tabs--border-card {
    border: none;
    box-shadow: none;
  }

  ::v-deep .el-tabs__header {
    background: #f5f7fa;
    border-bottom: 1px solid #e4e7ed;
  }

  ::v-deep .el-tabs__content {
    padding: 12px;
    max-height: 400px;
    overflow-y: auto;
  }

  .function-categories {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .function-category {
    .category-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: #606266;
      margin-bottom: 8px;
      padding-bottom: 6px;
      border-bottom: 1px solid #e4e7ed;

      i {
        font-size: 14px;
      }
    }

    .function-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .function-item {
      padding: 10px 12px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;
      background: #fafafa;

      &:hover {
        border-color: #409eff;
        background: #ecf5ff;
        box-shadow: 0 2px 4px rgba(64, 158, 255, 0.1);
      }

      .function-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;

        .function-name {
          font-size: 13px;
          font-weight: 600;
          color: #409eff;
          font-family: 'Courier New', monospace;
        }

        .function-desc {
          flex: 1;
          font-size: 12px;
          color: #606266;
        }
      }

      .function-syntax {
        font-size: 12px;
        color: #909399;
        font-family: 'Courier New', monospace;
        margin-bottom: 4px;
        padding: 4px 8px;
        background: #f5f7fa;
        border-radius: 3px;
      }

      .function-example {
        font-size: 11px;
        color: #67c23a;
        font-family: 'Courier New', monospace;
        padding-left: 8px;
      }
    }
  }

  .empty-hint {
    text-align: center;
    padding: 40px 20px;
    color: #909399;

    i {
      font-size: 48px;
      margin-bottom: 12px;
      display: block;
    }

    p {
      font-size: 13px;
      margin: 0;
    }
  }

  .panel-footer {
    padding: 8px 12px;
    border-top: 1px solid #e4e7ed;
    background: #f5f7fa;
    text-align: center;
  }
}
</style>
