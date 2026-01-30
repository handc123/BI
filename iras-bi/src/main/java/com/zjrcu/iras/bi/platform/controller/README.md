# BI平台Controller层实现文档

## 概述

本目录包含BI平台的所有REST API控制器实现，提供数据源、数据集、可视化和仪表板的完整管理功能。

## 控制器列表

### 1. DataSourceController
**文件**: `DataSourceController.java`  
**路径**: `/bi/datasource`  
**功能**: 数据源管理

**API端点**:
- `GET /list` - 查询数据源列表（分页）
- `GET /{id}` - 获取数据源详情
- `POST /test` - 测试数据源连接
- `POST` - 新增数据源
- `PUT` - 修改数据源
- `DELETE /{ids}` - 删除数据源
- `POST /file/upload` - 上传文件数据源
- `GET /file/{id}/preview` - 预览文件数据
- `DELETE /file/{id}` - 删除文件数据源
- `POST /file/cleanup` - 清理未使用的文件数据源

### 2. DatasetController
**文件**: `DatasetController.java`  
**路径**: `/bi/dataset`  
**功能**: 数据集管理

**API端点**:
- `GET /list` - 查询数据集列表（分页）
- `GET /{id}` - 获取数据集详情
- `POST` - 新增数据集
- `PUT` - 修改数据集
- `DELETE /{ids}` - 删除数据集
- `POST /{id}/preview` - 预览数据集数据
- `POST /{id}/extract` - 执行立即抽取

### 3. VisualizationController
**文件**: `VisualizationController.java`  
**路径**: `/bi/visualization`  
**功能**: 可视化管理

**API端点**:
- `GET /list` - 查询可视化列表（分页）
- `GET /{id}` - 获取可视化详情
- `POST` - 新增可视化
- `PUT` - 修改可视化
- `DELETE /{ids}` - 删除可视化
- `POST /{id}/data` - 获取可视化数据

### 4. DashboardController
**文件**: `DashboardController.java`  
**路径**: `/bi/dashboard`  
**功能**: 仪表板管理

**API端点**:
- `GET /list` - 查询仪表板列表（分页）
- `GET /{id}` - 获取仪表板详情
- `POST` - 新增仪表板
- `PUT` - 修改仪表板
- `DELETE /{ids}` - 删除仪表板
- `POST /{id}/data` - 获取仪表板所有组件数据
- `POST /{id}/export` - 导出仪表板为PDF
- `POST /{id}/share` - 生成共享链接

## 权限定义

### 数据源权限
- `bi:datasource:list` - 查询数据源列表
- `bi:datasource:query` - 查询数据源详情
- `bi:datasource:add` - 新增数据源
- `bi:datasource:edit` - 修改数据源
- `bi:datasource:remove` - 删除数据源

### 数据集权限
- `bi:dataset:list` - 查询数据集列表
- `bi:dataset:query` - 查询数据集详情
- `bi:dataset:add` - 新增数据集
- `bi:dataset:edit` - 修改数据集
- `bi:dataset:remove` - 删除数据集

### 可视化权限
- `bi:visualization:list` - 查询可视化列表
- `bi:visualization:query` - 查询可视化详情
- `bi:visualization:add` - 新增可视化
- `bi:visualization:edit` - 修改可视化
- `bi:visualization:remove` - 删除可视化

### 仪表板权限
- `bi:dashboard:list` - 查询仪表板列表
- `bi:dashboard:query` - 查询仪表板详情
- `bi:dashboard:add` - 新增仪表板
- `bi:dashboard:edit` - 修改仪表板
- `bi:dashboard:remove` - 删除仪表板
- `bi:dashboard:view` - 查看仪表板数据
- `bi:dashboard:export` - 导出仪表板
- `bi:dashboard:share` - 共享仪表板

## 使用示例

### 1. 创建数据源

```bash
curl -X POST http://localhost:8080/bi/datasource \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "MySQL数据源",
    "type": "mysql",
    "config": "{\"host\":\"localhost\",\"port\":3306,\"database\":\"iras\",\"username\":\"root\",\"password\":\"password\"}"
  }'
```

### 2. 创建数据集

```bash
curl -X POST http://localhost:8080/bi/dataset \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "用户数据集",
    "datasourceId": 1,
    "type": "direct",
    "queryConfig": "{\"sourceType\":\"table\",\"tableName\":\"sys_user\"}",
    "fieldConfig": "{\"fields\":[{\"name\":\"user_id\",\"alias\":\"用户ID\",\"type\":\"BIGINT\",\"visible\":true}]}"
  }'
```

### 3. 创建可视化

```bash
curl -X POST http://localhost:8080/bi/visualization \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "用户趋势图",
    "datasetId": 1,
    "type": "line",
    "config": "{\"dimensions\":[\"create_time\"],\"metrics\":[{\"field\":\"user_id\",\"aggregation\":\"COUNT\"}]}"
  }'
```

### 4. 创建仪表板

```bash
curl -X POST http://localhost:8080/bi/dashboard \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "用户分析仪表板",
    "layoutConfig": "{\"components\":[{\"id\":\"comp_1\",\"visualizationId\":1,\"position\":{\"x\":0,\"y\":0,\"w\":12,\"h\":6}}]}",
    "filterConfig": "{\"filters\":[]}",
    "themeConfig": "{\"primaryColor\":\"#409EFF\"}"
  }'
```

### 5. 获取可视化数据

```bash
curl -X POST http://localhost:8080/bi/visualization/1/data \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[
    {
      "field": "create_time",
      "operator": ">=",
      "value": "2024-01-01"
    }
  ]'
```

### 6. 获取仪表板数据

```bash
curl -X POST http://localhost:8080/bi/dashboard/1/data \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[
    {
      "field": "date",
      "operator": "between",
      "value": ["2024-01-01", "2024-12-31"]
    }
  ]'
```

### 7. 导出仪表板为PDF

```bash
curl -X POST http://localhost:8080/bi/dashboard/1/export \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[]' \
  --output dashboard.pdf
```

### 8. 生成共享链接

```bash
curl -X POST "http://localhost:8080/bi/dashboard/1/share?password=abc123" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 响应格式

所有API遵循若依框架的AjaxResult响应格式：

### 成功响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "示例数据"
  }
}
```

### 错误响应
```json
{
  "code": 500,
  "msg": "操作失败: 具体错误信息",
  "data": null
}
```

### 分页响应
```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {"id": 1, "name": "数据1"},
    {"id": 2, "name": "数据2"}
  ],
  "total": 100
}
```

## 错误处理

所有Controller都实现了统一的错误处理机制：

1. **参数验证错误**: 使用`@Validated`注解自动验证
2. **业务逻辑错误**: 捕获ServiceException并返回友好错误信息
3. **系统错误**: 记录详细日志并返回通用错误信息
4. **权限错误**: 通过`@PreAuthorize`注解自动处理

## 日志记录

所有修改操作都使用`@Log`注解记录操作日志：

- **title**: 操作模块名称（如"数据源管理"）
- **businessType**: 操作类型（INSERT、UPDATE、DELETE、EXPORT等）
- **自动记录**: 操作人、操作时间、IP地址、请求参数

## 测试

每个Controller都有对应的单元测试类：

- `DataSourceControllerTest.java`
- `DatasetControllerTest.java`
- `VisualizationControllerTest.java`
- `DashboardControllerTest.java`

运行测试：
```bash
mvn test -Dtest=*ControllerTest -pl iras-bi
```

## 依赖关系

```
Controller层
  ├── Service层
  │   ├── Mapper层
  │   └── Engine层
  └── Domain层
```

## 注意事项

1. **认证**: 所有API都需要JWT token认证
2. **权限**: 使用`@PreAuthorize`注解进行权限控制
3. **分页**: 列表查询自动支持分页（pageNum、pageSize参数）
4. **事务**: 修改操作自动包含在事务中
5. **审计**: 所有操作自动记录审计日志
6. **缓存**: 查询结果可能被缓存，修改操作会自动清除相关缓存

## API文档

访问Springdoc OpenAPI文档：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 下一步

1. 实现缓存管理（阶段5）
2. 实现导出服务（阶段6）
3. 实现审计日志（阶段7）
4. 实现前端页面（阶段8-12）
