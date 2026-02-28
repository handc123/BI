# 🔍 血缘图谱调试指南

## 问题：血缘图谱标签页没有显示图表

### 调试步骤

#### 第1步：打开浏览器开发者工具

1. 按 `F12` 键打开开发者工具
2. 切换到 **Console（控制台）** 标签页
3. 刷新浏览器页面（`Ctrl + F5`）

#### 第2步：点击图表打开对话框

1. 点击看板中的"仪表盘"图表
2. 切换到"血缘图谱"标签页
3. 查看控制台输出

---

## 📊 预期看到的日志

### ✅ 正常情况（应该看到）：

```
[LineageTab] API返回结果: {code: 200, data: {...}, msg: "查询成功"}
[LineageTab] graphData: {nodes: Array(6), edges: Array(8), metadata: {...}}
[LineageTab] 节点数: 6
[LineageTab] 边数: 8
[LineageTab] 开始渲染图谱
[LineageTab] renderGraph 被调用
[LineageTab] graphContainer ref: div.graph-container
[LineageTab] 初始化ECharts图表
[LineageTab] 设置图表option
[LineageTab] 图表渲染完成
```

### ❌ 异常情况（错误示例）：

#### 情况1：API调用失败
```
[LineageTab] API返回结果: {code: 500, msg: "..."}
加载血缘数据失败
```
**解决：** 检查后端服务是否启动，查看后端日志

#### 情况2：数据为空
```
[LineageTab] 节点数: 0
[LineageTab] 边数: 0
[LineageTab] 开始渲染图谱
```
**解决：** 检查数据库是否有血缘关系数据

#### 情况3：图表容器不存在
```
[LineageTab] renderGraph 被调用
[LineageTab] graphContainer ref: undefined
[LineageTab] graphContainer ref 不存在!
```
**解决：** DOM渲染时机问题，增加延迟

#### 情况4：ECharts初始化失败
```
[LineageTab] 初始化ECharts图表
Error: Cannot read property 'init' of undefined
```
**解决：** ECharts库未正确导入

---

## 🔧 快速检查清单

### 检查1：数据库是否有数据

```sql
USE ry-vue;

-- 检查指标6是否存在
SELECT id, metric_code, metric_name
FROM bi_metric_metadata
WHERE id = 6;

-- 检查血缘关系
SELECT COUNT(*) as lineage_count
FROM bi_metric_lineage
WHERE upstream_metric_id = 6 OR downstream_metric_id = 6;
```

**预期结果：**
- 指标6存在
- 血缘关系数 >= 1

---

### 检查2：后端API是否正常

访问Swagger测试API：
```
http://localhost:8080/swagger-ui.html
```

找到指标血缘相关API，测试：
```
GET /bi/lineage/metric/6?mode=graph
```

**预期返回：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "nodes": [...],
    "edges": [...],
    "metadata": {...}
  }
}
```

---

### 检查3：前端Network请求

1. 打开开发者工具 → **Network（网络）** 标签页
2. 点击图表，切换到血缘图谱标签页
3. 查找 `/bi/lineage/metric/6` 请求

**检查项：**
- ✅ Status Code: 200
- ✅ Response有数据
- ✅ 数据格式正确

---

### 检查4：ECharts是否加载

在Console中输入：
```javascript
typeof echarts
```

**预期结果：**
```
"object"  // ✅ ECharts已加载
"undefined"  // ❌ ECharts未加载
```

---

## 🛠️ 常见问题解决

### 问题1：图谱容器是空的

**原因：** 数据为空或图表未渲染

**解决：**
1. 查看Console日志，确认节点数和边数
2. 如果是0，检查数据库血缘关系
3. 如果>0，查看是否有JavaScript错误

---

### 问题2：图谱显示但位置不对

**原因：** 容器高度不够

**解决：**
在Console中输入：
```javascript
document.querySelector('.graph-container').clientHeight
```

查看容器高度，应该是600px。如果不是，可能是CSS样式问题。

---

### 问题3：节点挤在一起

**原因：** force布局参数问题

**解决：**
修改 `LineageTab.vue` 中的force配置：
```javascript
force: {
  repulsion: 500,  // 增加排斥力
  edgeLength: [150, 250],  // 增加边长
  gravity: 0.1
}
```

---

## 📝 完整调试流程

### 步骤1：验证数据库
```sql
USE ry-vue;
SELECT COUNT(*) FROM bi_metric_lineage WHERE upstream_metric_id = 6 OR downstream_metric_id = 6;
```

### 步骤2：验证后端API
访问：`http://localhost:8080/bi/lineage/metric/6?mode=graph`

### 步骤3：验证前端请求
打开F12 → Network，查看API请求

### 步骤4：查看Console日志
确认数据加载和图表渲染过程

### 步骤5：检查ECharts
在Console中输入：`echarts.version`

### 步骤6：检查DOM
在Console中输入：
```javascript
document.querySelector('.graph-container')
```

---

## 🎯 如果一切正常

刷新浏览器后，应该看到：

1. ✅ 点击图表打开对话框
2. ✅ 切换到"血缘图谱"标签页
3. ✅ 显示6个节点的血缘关系图
4. ✅ 节点可以点击查看详情
5. ✅ 可以切换上游/下游血缘模式

---

## 💡 临时解决方案

如果问题暂时无法解决，可以先测试其他功能：

1. **指标规范标签页** - 应该能正常显示
2. **数据查询标签页** - 需要数据源配置

---

**请在控制台中查看到的日志告诉我，我会帮你进一步分析！**
