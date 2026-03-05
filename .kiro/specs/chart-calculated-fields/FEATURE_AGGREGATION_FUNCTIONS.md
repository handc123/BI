# 聚合函数功能添加

## 问题描述

用户反馈在新建计算字段时，函数面板中没有 SUM()、AVG() 等聚合函数。

## 解决方案

在 `FunctionPanel` 组件中添加聚合函数分类，包含常用的聚合函数。

## 修改内容

### 文件：`ui/src/components/FunctionPanel/index.vue`

#### 1. 添加聚合函数数据

在 `builtinFunctions` 对象中添加 `aggregation` 分类：

```javascript
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
    // ... 其他函数
  ]
}
```

#### 2. 添加聚合函数显示区域

在模板中添加聚合函数分类的显示：

```vue
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
```

## 功能说明

### 支持的聚合函数

1. **SUM(column)** - 求和
   - 示例：`SUM(jkye)` - 计算贷款余额总和

2. **AVG(column)** - 平均值
   - 示例：`AVG(jkye)` - 计算贷款余额平均值

3. **MAX(column)** - 最大值
   - 示例：`MAX(jkye)` - 查找最大贷款余额

4. **MIN(column)** - 最小值
   - 示例：`MIN(jkye)` - 查找最小贷款余额

5. **COUNT(column)** - 计数
   - 示例：`COUNT(jkye)` - 统计贷款记录数

6. **COUNT(DISTINCT column)** - 去重计数
   - 示例：`COUNT(DISTINCT sjbsjgid)` - 统计不重复的机构数

### 使用方式

1. 打开计算字段对话框
2. 点击"函数面板"按钮
3. 在"系统内置函数"选项卡中找到"聚合函数"分类
4. 点击需要的聚合函数，自动插入到表达式编辑器中
5. 将 `{column}` 替换为实际的字段名

### 注意事项

- 聚合函数会自动设置 `aggregation: 'AUTO'`，表示表达式中已包含聚合
- 后端不会对这些表达式再次应用聚合函数
- 聚合函数可以与其他函数组合使用，例如：`ROUND(SUM(jkye) / 1000000, 2)`

## 测试验证

1. 打开仪表板设计器
2. 添加图表组件
3. 配置数据源和数据集
4. 点击"新建计算字段"
5. 在计算字段对话框中点击"函数"按钮
6. 验证"聚合函数"分类是否显示
7. 点击 SUM 函数，验证是否正确插入 `SUM({column})`
8. 创建计算字段 `SUM(jkye)`，验证功能是否正常

## 相关文件

- `ui/src/components/FunctionPanel/index.vue` - 函数面板组件
- `ui/src/components/CalculatedFieldDialog/index.vue` - 计算字段对话框
- `ui/src/components/ExpressionEditor/index.vue` - 表达式编辑器

## 修复状态

✅ 已添加聚合函数到函数面板
✅ 无语法错误
⏳ 等待用户测试验证
