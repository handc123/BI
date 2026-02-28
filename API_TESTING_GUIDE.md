# 后端API测试指南

**测试时间**: 2025-02-26
**服务地址**: http://localhost:8080
**状态**: ✅ 运行中

---

## 📋 测试准备

### 1. 访问Swagger UI（推荐）

在浏览器中打开：
```
http://localhost:8080/swagger-ui.html
```

在Swagger中，您会看到以下API分组：
- **指标管理模块** - 23个新增API
- conversion - 转换质量模块
- field - 字段质量模块
- indicator - 指标质量模块
- overview - 全局模块

### 2. 获取认证Token

由于RuoYi框架需要认证，测试API前需要先登录获取token：

#### 方式1: 通过Swagger UI测试
1. 打开 `http://localhost:8080/swagger-ui.html`
2. 找到 `/auth/login` 或类似登录接口
3. 点击 "Try it out"
4. 输入用户名密码（默认：admin/admin123）
5. 点击 "Execute" 获取响应
6. 复制返回的token值
7. 点击页面顶部的 "Authorize" 按钮
8. 输入 `Bearer <your_token>` （注意Bearer后面有空格）
9. 点击 "Authorize" 完成认证

#### 方式2: 使用curl命令
```bash
# 1. 获取验证码图片
curl http://localhost:8080/captchaImage -o captcha.jpg

# 2. 登录获取token
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\",\"code\":\"1234\"}"

# 3. 使用返回的token测试API
curl -X GET http://localhost:8080/bi/metadata/list \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🧪 API测试清单

### 一、指标元数据API (8个端点)

#### 1.1 查询指标列表
```
GET /bi/metadata/list
```

**测试命令**:
```bash
curl -X GET "http://localhost:8080/bi/metadata/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应**:
```json
{
  "total": 0,
  "rows": [],
  "code": 200,
  "msg": "查询成功"
}
```

#### 1.2 新增指标
```
POST /bi/metadata
```

**测试命令**:
```bash
curl -X POST http://localhost:8080/bi/metadata \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "metricCode": "METRIC_001",
    "metricName": "测试指标",
    "businessDefinition": "这是一个测试指标",
    "technicalFormula": "COUNT(*)",
    "ownerDept": "技术部",
    "dataFreshness": "T-1",
    "updateFrequency": "每日",
    "status": "0"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {指标信息}
}
```

#### 1.3 查询指标详情
```
GET /bi/metadata/{id}
```

**测试命令**:
```bash
curl -X GET http://localhost:8080/bi/metadata/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 1.4 修改指标
```
PUT /bi/metadata
```

**测试命令**:
```bash
curl -X PUT http://localhost:8080/bi/metadata \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "id": 1,
    "metricCode": "METRIC_001",
    "metricName": "测试指标(已更新)",
    "businessDefinition": "更新后的业务定义",
    "status": "0"
  }'
```

#### 1.5 删除指标
```
DELETE /bi/metadata/{ids}
```

**测试命令**:
```bash
curl -X DELETE "http://localhost:8080/bi/metadata/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 1.6 导入指标
```
POST /bi/metadata/import
```

#### 1.7 导出指标
```
POST /bi/metadata/export
```

#### 1.8 根据编码查询
```
GET /bi/metadata/code/{metricCode}
```

---

### 二、指标血缘API (7个端点)

#### 2.1 获取指标血缘图谱
```
GET /bi/lineage/metric/{metricId}?mode=graph
```

**参数说明**:
- `metricId`: 指标ID
- `mode`: graph(全量), upstream(上游), downstream(下游)

**测试命令**:
```bash
# 获取完整血缘图
curl -X GET "http://localhost:8080/bi/lineage/metric/1?mode=graph" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 只获取上游血缘
curl -X GET "http://localhost:8080/bi/lineage/metric/1?mode=upstream" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 只获取下游血缘
curl -X GET "http://localhost:8080/bi/lineage/metric/1?mode=downstream" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "nodes": [
      {
        "id": "1",
        "name": "Metric-1",
        "type": "metric",
        "color": "#1890ff"
      }
    ],
    "edges": [
      {
        "source": "1",
        "target": "2",
        "label": "aggregation"
      }
    ],
    "metadata": {
      "rootMetricId": 1,
      "mode": "graph",
      "nodeCount": 5,
      "edgeCount": 4
    }
  }
}
```

#### 2.2 创建血缘关系
```
POST /bi/lineage
```

**测试命令**:
```bash
curl -X POST http://localhost:8080/bi/lineage \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "upstreamMetricId": 1,
    "downstreamMetricId": 2,
    "transformationType": "aggregation",
    "transformationLogic": "汇总计算",
    "dependencyStrength": 3
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {血缘关系信息}
}
```

#### 2.3 批量创建血缘
```
POST /bi/lineage/batch
```

**测试命令**:
```bash
curl -X POST http://localhost:8080/bi/lineage/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[
    {
      "upstreamMetricId": 1,
      "downstreamMetricId": 2,
      "transformationType": "aggregation"
    },
    {
      "upstreamMetricId": 2,
      "downstreamMetricId": 3,
      "transformationType": "calculation"
    }
  ]'
```

#### 2.4 删除血缘关系
```
DELETE /bi/lineage/{id}
```

#### 2.5 删除指标所有血缘
```
DELETE /bi/lineage/metric/{metricId}
```

#### 2.6 获取上游血缘
```
GET /bi/lineage/upstream/{metricId}?depth=3
```

#### 2.7 获取下游血缘
```
GET /bi/lineage/downstream/{metricId}?depth=3
```

---

### 三、指标数据API (8个端点)

#### 3.1 查询指标数据（分页）
```
POST /bi/metric/data/{metricId}/query
```

**测试命令**:
```bash
curl -X POST http://localhost:8080/bi/metric/data/1/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pageNum": 1,
    "pageSize": 10,
    "sortField": "data_time",
    "sortOrder": "DESC"
  }'
```

**预期响应**:
```json
{
  "total": 0,
  "rows": [],
  "code": 200,
  "msg": "查询成功"
}
```

#### 3.2 导出指标数据
```
POST /bi/metric/data/{metricId}/export
```

**测试命令**:
```bash
curl -X POST http://localhost:8080/bi/metric/data/1/export \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pageNum": 1,
    "pageSize": 1000,
    "exportFormat": "excel"
  }' \
  --output metric_data.xlsx
```

#### 3.3 获取数据概览
```
GET /bi/metric/data/{metricId}/overview?timeRange=7d
```

**测试命令**:
```bash
curl -X GET "http://localhost:8080/bi/metric/data/1/overview?timeRange=7d" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "metricId": 1,
    "metricCode": "METRIC_001",
    "metricName": "测试指标",
    "dataFreshness": "T-1",
    "updateFrequency": "每日",
    "ownerDept": "技术部",
    "timeRange": "7d"
  }
}
```

#### 3.4 获取实时数据
```
GET /bi/metric/data/{metricId}/realtime?limit=10
```

#### 3.5 获取聚合数据
```
POST /bi/metric/data/{metricId}/aggregate
```

#### 3.6 获取趋势数据
```
GET /bi/metric/data/{metricId}/trend?granularity=day&points=30
```

#### 3.7 获取对比数据
```
POST /bi/metric/data/{metricId}/compare
```

#### 3.8 刷新缓存
```
POST /bi/metric/data/{metricId}/refresh
```

---

## 🔐 权限说明

所有API都需要相应的权限：

| 权限标识 | 说明 |
|---------|------|
| `bi:metadata:query` | 查询指标元数据 |
| `bi:metadata:add` | 创建指标 |
| `bi:metadata:edit` | 修改指标 |
| `bi:metadata:remove` | 删除指标 |
| `bi:metadata:export` | 导出指标 |
| `bi:lineage:query` | 查询血缘 |
| `bi:lineage:add` | 创建血缘 |
| `bi:lineage:remove` | 删除血缘 |
| `bi:metric:data:query` | 查询数据 |
| `bi:metric:data:export` | 导出数据 |

---

## 📊 完整测试流程

### 步骤1: 准备测试数据

1. **创建3个指标**
```bash
# 指标1：基础指标
curl -X POST http://localhost:8080/bi/metadata \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "metricCode": "BASE_001",
    "metricName": "基础指标A",
    "businessDefinition": "原始数据基础指标",
    "technicalFormula": "COUNT(*)",
    "ownerDept": "技术部",
    "dataFreshness": "RT",
    "updateFrequency": "实时"
  }'

# 指标2：聚合指标
curl -X POST http://localhost:8080/bi/metadata \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "metricCode": "AGG_001",
    "metricName": "聚合指标B",
    "businessDefinition": "基于指标A的聚合指标",
    "technicalFormula": "SUM(BASE_001)",
    "ownerDept": "数据部",
    "dataFreshness": "T-1",
    "updateFrequency": "每日"
  }'

# 指标3：衍生指标
curl -X POST http://localhost:8080/bi/metadata \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "metricCode": "DERIVED_001",
    "metricName": "衍生指标C",
    "businessDefinition": "基于指标B的计算指标",
    "technicalFormula": "AGG_001 * 1.2",
    "ownerDept": "业务部",
    "dataFreshness": "T-1",
    "updateFrequency": "每周"
  }'
```

2. **创建血缘关系**
```bash
# BASE_001 -> AGG_001
curl -X POST http://localhost:8080/bi/lineage \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "upstreamMetricId": 1,
    "downstreamMetricId": 2,
    "transformationType": "aggregation",
    "transformationLogic": "求和聚合",
    "dependencyStrength": 5
  }'

# AGG_001 -> DERIVED_001
curl -X POST http://localhost:8080/bi/lineage \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "upstreamMetricId": 2,
    "downstreamMetricId": 3,
    "transformationType": "calculation",
    "transformationLogic": "乘以系数1.2",
    "dependencyStrength": 3
  }'
```

### 步骤2: 测试血缘查询

```bash
# 查看完整血缘图
curl -X GET "http://localhost:8080/bi/lineage/metric/1?mode=graph" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 只看上游
curl -X GET "http://localhost:8080/bi/lineage/metric/3?mode=upstream" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 步骤3: 测试数据查询

```bash
# 查询指标数据
curl -X POST http://localhost:8080/bi/metric/data/1/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pageNum": 1,
    "pageSize": 10
  }'

# 获取数据概览
curl -X GET "http://localhost:8080/bi/metric/data/1/overview" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📝 测试检查清单

- [ ] 服务成功启动在8080端口
- [ ] Swagger UI可以访问
- [ ] 成功登录并获取token
- [ ] 创建指标元数据成功
- [ ] 查询指标列表成功
- [ ] 创建血缘关系成功
- [ ] 查询血缘图谱成功
- [ ] 查询指标数据成功
- [ ] 数据概览接口正常
- [ ] 权限控制生效

---

## 🐛 常见问题排查

### 1. 401 Unauthorized
**原因**: 未提供token或token无效
**解决**: 重新登录获取新token

### 2. 403 Forbidden
**原因**: 权限不足
**解决**: 确认用户有相应权限，或联系管理员分配

### 3. 500 Internal Server Error
**原因**: 服务器内部错误
**解决**: 查看后端日志，检查数据库表是否存在

### 4. 验证码失效
**原因**: 验证码有效期通常是几分钟
**解决**: 重新获取验证码图片

---

## 📊 预期测试结果

### 成功标准

1. ✅ 所有23个API端点都能正常访问
2. ✅ 创建的指标数据能正确保存到数据库
3. ✅ 血缘关系能正确建立和查询
4. ✅ 数据查询接口返回正确格式
5. ✅ 权限控制正常工作

### 数据验证

**数据库表验证**:
```sql
-- 查看指标元数据
SELECT * FROM bi_metric_metadata;

-- 查看血缘关系
SELECT * FROM bi_metric_lineage;

-- 查看血缘节点
SELECT * FROM bi_lineage_node;

-- 查看血缘边
SELECT * FROM bi_lineage_edge;
```

---

**测试报告生成**: 待测试完成后生成
**API端点总数**: 23个
**覆盖模块**: 指标元数据、指标血缘、指标数据

---

**下一步**: 测试完成后，我们将生成详细的测试报告并开始第三阶段（前端实现）
