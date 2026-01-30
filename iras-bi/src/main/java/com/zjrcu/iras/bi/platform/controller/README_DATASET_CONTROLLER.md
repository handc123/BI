# Dataset Controller - 快速参考

## 文件位置

- **Controller**: `DatasetController.java`
- **测试**: `DatasetControllerTest.java`
- **详细文档**: `DATASET_CONTROLLER_IMPLEMENTATION.md`

## API 端点总览

| 方法 | 端点 | 权限 | 说明 |
|------|------|------|------|
| GET | `/bi/dataset/list` | `bi:dataset:list` | 查询数据集列表（分页） |
| GET | `/bi/dataset/{id}` | `bi:dataset:query` | 获取数据集详情 |
| POST | `/bi/dataset` | `bi:dataset:add` | 新增数据集 |
| PUT | `/bi/dataset` | `bi:dataset:edit` | 修改数据集 |
| DELETE | `/bi/dataset/{ids}` | `bi:dataset:remove` | 删除数据集 |
| POST | `/bi/dataset/{id}/preview` | `bi:dataset:query` | 预览数据集数据 |
| POST | `/bi/dataset/{id}/extract` | `bi:dataset:edit` | 执行立即抽取 |

## 快速使用示例

### 1. 创建直连数据集

```bash
curl -X POST http://localhost:8080/bi/dataset \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "用户数据集",
    "datasourceId": 1,
    "type": "direct",
    "queryConfig": "{\"sourceType\":\"table\",\"tableName\":\"sys_user\"}",
    "status": "0"
  }'
```

### 2. 创建抽取数据集

```bash
curl -X POST http://localhost:8080/bi/dataset \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "贷款数据集",
    "datasourceId": 2,
    "type": "extract",
    "queryConfig": "{\"sourceType\":\"sql\",\"sql\":\"SELECT * FROM loans\"}",
    "extractConfig": "{\"schedule\":{\"enabled\":true,\"cronExpression\":\"0 0 2 * * ?\"}}"
  }'
```

### 3. 预览数据集（带筛选）

```bash
curl -X POST http://localhost:8080/bi/dataset/1/preview \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[
    {"field": "status", "operator": "eq", "value": "0"},
    {"field": "create_time", "operator": "gte", "value": "2024-01-01"}
  ]'
```

### 4. 查询数据集列表

```bash
curl -X GET "http://localhost:8080/bi/dataset/list?pageNum=1&pageSize=10&name=用户" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. 删除数据集

```bash
curl -X DELETE http://localhost:8080/bi/dataset/1,2,3 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 配置格式参考

### queryConfig (查询配置)

**表模式**:
```json
{
  "sourceType": "table",
  "tableName": "sys_user"
}
```

**SQL模式**:
```json
{
  "sourceType": "sql",
  "sql": "SELECT * FROM sys_user WHERE status = '0'"
}
```

**带JOIN**:
```json
{
  "sourceType": "table",
  "tableName": "sys_user",
  "joins": [
    {
      "type": "LEFT",
      "table": "sys_dept",
      "on": "sys_user.dept_id = sys_dept.dept_id"
    }
  ]
}
```

### fieldConfig (字段配置)

```json
{
  "fields": [
    {
      "name": "user_id",
      "alias": "用户ID",
      "type": "BIGINT",
      "visible": true
    },
    {
      "name": "profit_margin",
      "alias": "利润率",
      "type": "DECIMAL",
      "calculated": true,
      "expression": "profit / revenue * 100"
    }
  ]
}
```

### extractConfig (抽取配置)

```json
{
  "schedule": {
    "enabled": true,
    "cronExpression": "0 0 2 * * ?",
    "timezone": "Asia/Shanghai"
  },
  "incremental": false,
  "incrementalField": "update_time"
}
```

## 常见错误处理

### 错误 1: 配置验证失败

**错误信息**: "查询配置JSON格式错误"

**解决方案**: 检查 JSON 格式是否正确，确保所有必需字段都存在

### 错误 2: 数据集被使用

**错误信息**: "数据集被3个可视化使用,无法删除"

**解决方案**: 先删除或修改依赖的可视化，再删除数据集

### 错误 3: 类型错误

**错误信息**: "只有抽取类型数据集才能执行抽取操作"

**解决方案**: 确认数据集类型为 "extract"

## 测试运行

```bash
# 运行所有测试
mvn test -Dtest=DatasetControllerTest -pl iras-bi

# 运行单个测试
mvn test -Dtest=DatasetControllerTest#testAdd_Success -pl iras-bi
```

## 下一步

- ✅ 任务 3.3 已完成
- ⏭️ 下一个任务: 3.4 实现 DataExtractScheduler 定时抽取

## 相关文档

- 详细实现文档: `DATASET_CONTROLLER_IMPLEMENTATION.md`
- Service 层文档: `../service/DATASET_SERVICE_IMPLEMENTATION.md`
- 设计文档: `.kiro/specs/bi-platform-upgrade/design.md`
