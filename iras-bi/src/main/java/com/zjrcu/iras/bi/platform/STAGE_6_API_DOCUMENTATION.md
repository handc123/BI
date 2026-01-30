# 阶段6 API文档：导出和共享

## 概述

本文档描述了阶段6实现的所有API端点，包括导出功能和共享链接功能。

## 导出API

### 1. 导出可视化为CSV

**端点**: `POST /bi/visualization/{id}/export/csv`

**描述**: 将可视化数据导出为CSV格式文件

**权限**: `bi:visualization:export`

**路径参数**:
- `id` (Long, 必需): 可视化ID

**请求体** (可选):
```json
{
  "filters": [
    {
      "field": "date",
      "operator": ">=",
      "value": "2024-01-01"
    }
  ]
}
```

**响应**: 
- Content-Type: `text/csv; charset=UTF-8`
- Content-Disposition: `attachment; filename=visualization_{id}_{timestamp}.csv`
- 文件内容: CSV格式数据

**示例**:
```bash
curl -X POST "http://localhost:8080/bi/visualization/1/export/csv" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"filters": []}' \
  --output data.csv
```

---

### 2. 导出可视化为Excel

**端点**: `POST /bi/visualization/{id}/export/excel`

**描述**: 将可视化数据导出为Excel格式文件

**权限**: `bi:visualization:export`

**路径参数**:
- `id` (Long, 必需): 可视化ID

**请求体** (可选):
```json
{
  "filters": [
    {
      "field": "region",
      "operator": "=",
      "value": "华东"
    }
  ]
}
```

**响应**: 
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename=visualization_{id}_{timestamp}.xlsx`
- 文件内容: Excel格式数据

**示例**:
```bash
curl -X POST "http://localhost:8080/bi/visualization/1/export/excel" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"filters": []}' \
  --output data.xlsx
```

---

### 3. 导出仪表板为PDF

**端点**: `POST /bi/dashboard/{id}/export`

**描述**: 将仪表板导出为PDF格式文件

**权限**: `bi:dashboard:export`

**路径参数**:
- `id` (Long, 必需): 仪表板ID

**请求体** (可选):
```json
{
  "globalFilters": [
    {
      "field": "date",
      "operator": "between",
      "value": ["2024-01-01", "2024-12-31"]
    }
  ]
}
```

**响应**: 
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename=dashboard_{id}_{timestamp}.pdf`
- 文件内容: PDF格式文档

**注意**: 当前为占位符实现，需要集成ECharts服务器端渲染

**示例**:
```bash
curl -X POST "http://localhost:8080/bi/dashboard/1/export" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"globalFilters": []}' \
  --output dashboard.pdf
```

---

## 共享链接API

### 4. 生成共享链接

**端点**: `POST /bi/dashboard/{id}/share`

**描述**: 为仪表板生成共享链接

**权限**: `bi:dashboard:share`

**路径参数**:
- `id` (Long, 必需): 仪表板ID

**查询参数**:
- `password` (String, 可选): 访问密码

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "shareLink": "/bi/share/abc123def456/dashboard",
    "hasPassword": true
  }
}
```

**示例**:
```bash
# 无密码共享
curl -X POST "http://localhost:8080/bi/dashboard/1/share" \
  -H "Authorization: Bearer {token}"

# 有密码共享
curl -X POST "http://localhost:8080/bi/dashboard/1/share?password=secret123" \
  -H "Authorization: Bearer {token}"
```

---

### 5. 验证共享链接

**端点**: `POST /bi/share/{shareCode}/validate`

**描述**: 验证共享链接的访问权限

**权限**: 无需认证

**路径参数**:
- `shareCode` (String, 必需): 共享码

**查询参数**:
- `password` (String, 可选): 访问密码

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "valid": true,
    "resourceType": "dashboard",
    "resourceId": 1
  }
}
```

**错误响应**:
```json
{
  "code": 500,
  "msg": "共享链接已过期"
}
```

**示例**:
```bash
# 无密码验证
curl -X POST "http://localhost:8080/bi/share/abc123def456/validate"

# 有密码验证
curl -X POST "http://localhost:8080/bi/share/abc123def456/validate?password=secret123"
```

---

### 6. 访问共享仪表板

**端点**: `GET /bi/share/{shareCode}/dashboard`

**描述**: 获取共享仪表板的配置信息

**权限**: 无需认证（通过共享码验证）

**路径参数**:
- `shareCode` (String, 必需): 共享码

**查询参数**:
- `password` (String, 可选): 访问密码

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "销售仪表板",
    "layoutConfig": "{\"components\":[...]}",
    "filterConfig": "{\"filters\":[...]}",
    "themeConfig": "{\"colors\":[...]}",
    "status": "0",
    "createTime": "2024-01-15 10:30:00"
  }
}
```

**错误响应**:
```json
{
  "code": 500,
  "msg": "共享链接已过期"
}
```

**示例**:
```bash
curl -X GET "http://localhost:8080/bi/share/abc123def456/dashboard?password=secret123"
```

---

### 7. 获取共享仪表板数据

**端点**: `POST /bi/share/{shareCode}/dashboard/data`

**描述**: 获取共享仪表板的所有组件数据

**权限**: 无需认证（通过共享码验证）

**路径参数**:
- `shareCode` (String, 必需): 共享码

**查询参数**:
- `password` (String, 可选): 访问密码

**请求体** (可选):
```json
{
  "globalFilters": [
    {
      "field": "date",
      "operator": ">=",
      "value": "2024-01-01"
    }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "1": {
      "columns": ["date", "amount"],
      "rows": [
        {"date": "2024-01-01", "amount": 1000},
        {"date": "2024-01-02", "amount": 1500}
      ]
    },
    "2": {
      "columns": ["region", "count"],
      "rows": [
        {"region": "华东", "count": 100},
        {"region": "华北", "count": 80}
      ]
    }
  }
}
```

**示例**:
```bash
curl -X POST "http://localhost:8080/bi/share/abc123def456/dashboard/data?password=secret123" \
  -H "Content-Type: application/json" \
  -d '{"globalFilters": []}'
```

---

### 8. 撤销共享链接

**端点**: `DELETE /bi/share/{shareCode}`

**描述**: 撤销共享链接，使其失效

**权限**: 无需认证（任何人都可以撤销）

**路径参数**:
- `shareCode` (String, 必需): 共享码

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功"
}
```

**错误响应**:
```json
{
  "code": 500,
  "msg": "共享链接不存在"
}
```

**示例**:
```bash
curl -X DELETE "http://localhost:8080/bi/share/abc123def456"
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 401 | 未认证或认证失败 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 常见错误消息

### 导出相关
- "可视化不存在" - 指定的可视化ID不存在
- "仪表板不存在" - 指定的仪表板ID不存在
- "导出服务未配置" - ExportService未正确配置
- "获取可视化数据失败" - 查询数据时发生错误

### 共享链接相关
- "共享链接不存在" - 指定的共享码不存在
- "共享链接已过期" - 共享链接已超过有效期
- "访问次数已达上限" - 共享链接访问次数已用完
- "密码错误" - 提供的访问密码不正确
- "共享链接类型不匹配" - 共享码对应的资源类型不是仪表板

## 使用场景

### 场景1: 导出数据进行离线分析

1. 用户在前端查看可视化
2. 点击"导出"按钮
3. 选择导出格式（CSV或Excel）
4. 前端调用导出API
5. 浏览器下载文件

### 场景2: 分享仪表板给外部用户

1. 用户在前端查看仪表板
2. 点击"分享"按钮
3. 设置访问密码（可选）
4. 前端调用生成共享链接API
5. 用户复制链接发送给他人
6. 接收者打开链接，输入密码（如果有）
7. 查看共享的仪表板

### 场景3: 定期导出报表

1. 管理员配置定时任务
2. 定时任务调用导出API
3. 将导出文件保存到指定位置
4. 发送邮件通知相关人员

## 安全考虑

### 导出功能
1. **权限控制**: 所有导出操作都需要相应的权限
2. **数据过滤**: 导出时应用用户的数据权限（行级安全）
3. **文件大小限制**: 建议限制导出文件大小，防止资源耗尽
4. **并发限制**: 限制同时进行的导出任务数量

### 共享链接
1. **密码保护**: 支持可选的访问密码
2. **过期时间**: 默认30天过期，可配置
3. **访问限制**: 支持访问次数限制
4. **撤销机制**: 支持随时撤销共享链接
5. **无需认证**: 共享链接访问不需要系统账号，但需要共享码和密码

## 性能优化建议

### 导出优化
1. **异步导出**: 大数据量使用异步导出
2. **流式处理**: 使用流式写入，减少内存占用
3. **缓存利用**: 优先使用缓存数据
4. **分页导出**: 超大数据集分批导出

### 共享链接优化
1. **缓存共享链接**: 将常用共享链接缓存到Redis
2. **定期清理**: 定期删除过期的共享链接
3. **访问限流**: 对共享链接访问进行限流

## 监控指标

### 导出监控
- 导出请求数（按格式统计）
- 导出成功率
- 导出文件大小分布
- 导出耗时分布

### 共享链接监控
- 共享链接生成数
- 共享链接访问数
- 共享链接过期数
- 密码验证失败次数

---

**文档版本**: 1.0
**最后更新**: 2024-01-20
**维护者**: Kiro AI Assistant
