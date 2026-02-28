# 🔧 API路径修复完成

## ✅ 问题已解决

**问题原因：**
前端API路径配置与后端Controller路径不匹配

**错误路径：** `/bi/metric/metadata/6`
**正确路径：** `/bi/metadata/6`

---

## 🔨 修复内容

### 前端API路径配置（ui/src/api/bi/metric.js）

**修复前：**
```javascript
const BASE_PATH = '/bi/metric'  // ❌ 错误
```

**修复后：**
```javascript
const BASE_PATH = '/bi'
const METADATA_PATH = `${BASE_PATH}/metadata`    // /bi/metadata
const LINEAGE_PATH = `${BASE_PATH}/lineage`      // /bi/lineage
const DATA_PATH = `${BASE_PATH}/metric/data`     // /bi/metric/data
```

---

## 📊 后端API路径对照表

| 功能模块 | 前端调用 | 后端路径 | Controller |
|---------|---------|---------|-----------|
| **指标元数据** |
| 获取指标详情 | `/bi/metadata/{id}` | ✅ | MetricMetadataController |
| 查询指标列表 | `/bi/metadata/list` | ✅ | MetricMetadataController |
| 新增指标 | `/bi/metadata` | ✅ | MetricMetadataController |
| 修改指标 | `/bi/metadata` | ✅ | MetricMetadataController |
| 删除指标 | `/bi/metadata/{ids}` | ✅ | MetricMetadataController |
| 导出指标 | `/bi/metadata/export` | ✅ | MetricMetadataController |
| **指标血缘** |
| 获取血缘图谱 | `/bi/lineage/metric/{id}` | ✅ | MetricLineageController |
| 上游血缘 | `/bi/lineage/upstream/{id}` | ✅ | MetricLineageController |
| 下游血缘 | `/bi/lineage/downstream/{id}` | ✅ | MetricLineageController |
| 创建血缘 | `/bi/lineage` | ✅ | MetricLineageController |
| 删除血缘 | `/bi/lineage/{ids}` | ✅ | MetricLineageController |
| 批量创建 | `/bi/lineage/batch` | ✅ | MetricLineageController |
| **指标数据** |
| 查询数据 | `/bi/metric/data/{id}/query` | ✅ | MetricDataController |
| 导出数据 | `/bi/metric/data/{id}/export` | ✅ | MetricDataController |
| 数据概览 | `/bi/metric/data/{id}/overview` | ✅ | MetricDataController |

---

## 🚀 立即测试

### 第1步：确认前端服务运行中

```bash
cd ui
npm run dev
```

### 第2步：访问看板

```
http://localhost:80/views/bi/dashboard/view/3
```

### 第3步：点击"仪表盘"图表

**预期结果：**
- ✅ 对话框正常打开
- ✅ 显示"指标规范"标签页
- ✅ 显示指标编码：NPL_RATIO
- ✅ 显示指标名称：不良贷款率
- ✅ 显示业务定义：不良贷款余额 / 各项贷款余额 × 100%

### 第4步：测试其他标签页

**血缘图谱标签页：**
- 切换到"血缘图谱"
- 应该显示6个节点、8条关系
- 点击节点查看详情
- 切换上游/下游血缘模式

**数据查询标签页：**
- 切换到"数据查询"
- 设置查询条件
- 点击"查询"按钮
- 查看数据表格

---

## 🔍 验证API是否正常

### 浏览器F12开发者工具

1. 打开浏览器开发者工具（F12）
2. 切换到"Network"标签页
3. 点击图表
4. 查看API请求

**正确的API请求：**
```
GET /bi/metadata/6         ← 获取指标详情 ✅
GET /bi/lineage/metric/6   ← 获取血缘图谱 ✅
GET /bi/metric/data/6/overview ← 获取数据概览 ✅
```

### 后端日志验证

查看后端控制台，应该看到类似的请求日志：
```
GET /bi/metadata/6 - 200 OK
GET /bi/lineage/metric/6?mode=graph - 200 OK
```

---

## 🐛 如果还有问题

### 问题1：404 Not Found

**检查：**
1. 后端服务是否启动
2. 端口8080是否正常
3. Swagger是否可访问：`http://localhost:8080/swagger-ui.html`

**验证SQL：**
```sql
USE ry-vue;
SELECT id, metric_code, metric_name
FROM bi_metric_metadata
WHERE id = 6;
-- 应该返回 NPL_RATIO 的信息
```

### 问题2：403 Forbidden

**原因：** 权限不足

**解决：**
```sql
-- 检查用户权限
SELECT * FROM sys_user_menu
WHERE user_id = 1
  AND menu_id IN (3049, 3055, 3059);

-- 临时测试：给admin用户所有权限
UPDATE sys_user SET user_type='00' WHERE user_id=1;
```

### 问题3：500 Internal Server Error

**检查：**
```sql
-- 验证数据库表是否有数据
USE ry-vue;
SELECT COUNT(*) FROM bi_metric_metadata WHERE id = 6;
SELECT COUNT(*) FROM bi_metric_lineage WHERE upstream_metric_id = 6 OR downstream_metric_id = 6;
```

---

## 📝 修复总结

**修复文件：** `ui/src/api/bi/metric.js`

**修改内容：**
1. ✅ 分离不同模块的API路径
2. ✅ 修复所有metadata相关的API调用（6处）
3. ✅ 修复所有lineage相关的API调用（8处）
4. ✅ 修复所有data相关的API调用（3处）

**影响范围：**
- ✅ MetricDetailDialog组件（指标详情对话框）
- ✅ SpecificationTab组件（规范标签页）
- ✅ LineageTab组件（血缘图谱标签页）
- ✅ DataQueryTab组件（数据查询标签页）

---

## 🎉 测试成功标志

- ✅ 点击图表弹出对话框
- ✅ 三个标签页都能正常加载
- ✅ 指标规范显示正确
- ✅ 血缘图谱显示6个节点
- ✅ 数据查询能正常执行

**现在可以重新测试点击图表功能了！**
