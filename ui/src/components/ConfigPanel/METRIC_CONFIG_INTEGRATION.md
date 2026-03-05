# 指标配置功能集成说明

## 更新内容

已将增强的指标配置功能集成到 ConfigPanel 组件中。

### 修改的文件

1. **ui/src/components/ConfigPanel/index.vue**
   - 将导入从 `DataConfigPanel` 改为 `DataConfig`
   - 现在使用 `./DataConfig.vue` 组件（包含指标配置功能）

2. **ui/src/components/ConfigPanel/DataConfig.vue**
   - 添加了 `config-change` 事件发射，以兼容 ConfigPanel 的事件监听

### 新增的功能

DataConfig 组件现在包含三个选项卡：

1. **数据源配置** - 原有的数据源、数据集、字段配置
2. **基础指标** - 配置基础聚合指标（SUM、AVG、COUNT、MAX、MIN）
3. **计算指标** - 配置计算指标（条件比率、简单比率、条件求和、自定义表达式）

### 如何使用

1. 在仪表板设计器中，点击任意图表组件
2. 在右侧配置面板中，点击"数据"选项卡
3. 你会看到三个子选项卡：
   - **数据源配置**：选择数据源和数据集
   - **基础指标**：点击"添加基础指标"按钮创建聚合指标
   - **计算指标**：点击"添加计算指标"按钮创建计算指标

### 指标配置功能

#### 基础指标
- 选择字段和聚合函数（SUM、AVG、COUNT、MAX、MIN）
- 设置指标名称和别名
- 查看 SQL 预览

#### 计算指标
支持四种类型：
1. **条件比率**：基于条件的分子分母比率（如不良贷款率）
2. **简单比率**：两个指标之间的直接比率
3. **条件求和**：基于条件的聚合计算
4. **自定义表达式**：支持复杂的数学表达式

### 功能特性

- ✅ 拖放排序指标
- ✅ 编辑和删除指标
- ✅ 依赖检查（删除被引用的指标时会警告）
- ✅ SQL 预览
- ✅ 测试指标功能
- ✅ 性能警告（超过20个指标）

### API 集成

已创建 `ui/src/api/bi/metric.js` 文件，包含以下方法：
- `validateMetric(config)` - 验证指标配置
- `testMetric(datasetId, metric)` - 测试指标计算
- `saveMetricConfig(datasetId, config)` - 保存指标配置
- `loadMetricConfig(datasetId)` - 加载指标配置

### 后端支持

后端已实现以下 API 端点：
- `POST /bi/dataset/metric/validate` - 验证指标配置
- `POST /bi/dataset/metric/test` - 测试指标
- `PUT /bi/dataset/{id}/config` - 保存指标配置

## 测试步骤

1. 启动前端：`cd ui && npm run dev`
2. 访问：http://localhost:82
3. 进入仪表板设计器
4. 点击任意图表组件
5. 在右侧配置面板的"数据"选项卡中，查看三个子选项卡
6. 尝试添加基础指标和计算指标

## 注意事项

- 确保后端服务正在运行
- 需要先选择数据集才能添加指标
- 指标配置会自动保存到组件的 dataConfig 中
