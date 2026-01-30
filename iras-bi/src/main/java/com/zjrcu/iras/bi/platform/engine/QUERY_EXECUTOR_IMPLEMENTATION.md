# QueryExecutor 查询引擎实现文档

## 概述

QueryExecutor是BI平台的核心查询引擎组件,负责执行数据集查询并应用筛选、计算字段和权限过滤。

## 实现文件

### 核心类
- **接口**: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IQueryExecutor.java`
- **实现**: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java`

### DTO类
- `QueryResult.java` - 查询结果封装类
- `Filter.java` - 筛选条件类
- `Metric.java` - 度量字段类

### 测试类
- `QueryExecutorTest.java` - 单元测试

## 功能特性

### 1. 数据集类型支持

#### 直连数据集 (Direct)
- 对数据源执行实时查询
- 支持表查询和自定义SQL查询
- 动态构建SQL语句

#### 抽取数据集 (Extract)
- 从`bi_extract_data`表查询预抽取的数据
- 提高查询性能
- 适合大数据量场景

### 2. 查询功能

#### 基础查询 (executeQuery)
```java
QueryResult executeQuery(Long datasetId, List<Filter> filters, SysUser user)
```

**功能**:
- 执行数据集查询
- 应用筛选条件
- 应用行级权限过滤
- 返回完整数据行

#### 聚合查询 (executeAggregation)
```java
QueryResult executeAggregation(Long datasetId, List<String> dimensions, 
                               List<Metric> metrics, List<Filter> filters, SysUser user)
```

**功能**:
- 执行GROUP BY聚合查询
- 支持多维度分组
- 支持多种聚合函数(SUM, AVG, COUNT, MAX, MIN)
- 应用筛选和权限过滤

### 3. 筛选操作符

支持以下筛选操作符:

| 操作符 | 说明 | SQL示例 |
|--------|------|---------|
| eq | 等于 | `field = 'value'` |
| ne | 不等于 | `field != 'value'` |
| gt | 大于 | `field > 100` |
| gte | 大于等于 | `field >= 100` |
| lt | 小于 | `field < 100` |
| lte | 小于等于 | `field <= 100` |
| like | 模糊匹配 | `field LIKE '%value%'` |
| in | 包含 | `field IN ('a', 'b', 'c')` |
| between | 区间 | `field BETWEEN 1 AND 100` |

### 4. 计算字段支持

从数据集的`fieldConfig`中读取计算字段配置:

```json
{
  "name": "profit_margin",
  "alias": "利润率",
  "type": "DECIMAL",
  "calculated": true,
  "expression": "profit / revenue * 100"
}
```

在SELECT子句中动态计算:
```sql
SELECT (profit / revenue * 100) AS profit_margin FROM ...
```

### 5. 行级权限过滤

基于用户的部门ID自动添加权限过滤条件:

```sql
WHERE ... AND dept_id = 100
```

**说明**: 
- 从`SysUser.getDeptId()`获取用户部门ID
- 自动添加到WHERE子句
- 确保用户只能查看本部门数据

### 6. 查询性能控制

#### 超时控制
- 查询超时时间: 30秒
- 超时后抛出`SQLTimeoutException`
- 建议转换为抽取数据集

#### 行数限制
- 最大返回行数: 100,000行
- 防止内存溢出
- 大数据量场景使用抽取数据集

## SQL构建逻辑

### 直连查询SQL结构

```sql
SELECT 
    field1,
    field2,
    (expression) AS calculated_field
FROM 
    table_name
WHERE 
    filter_condition1 AND
    filter_condition2 AND
    dept_id = {user_dept_id}
```

### 聚合查询SQL结构

```sql
SELECT 
    dimension1,
    dimension2,
    SUM(metric1) AS metric1_sum,
    AVG(metric2) AS metric2_avg
FROM 
    table_name
WHERE 
    filter_condition AND
    dept_id = {user_dept_id}
GROUP BY 
    dimension1, dimension2
```

## 错误处理

### 1. 参数验证错误
- 数据集ID为空
- 数据集不存在
- 数据集已停用
- 维度和度量同时为空

### 2. 查询执行错误
- 连接失败
- SQL语法错误
- 查询超时
- 权限不足

### 3. 错误响应格式

```java
QueryResult.failure("错误消息")
```

返回:
```json
{
  "success": false,
  "errorMessage": "查询执行失败: ...",
  "data": [],
  "fields": [],
  "totalRows": 0
}
```

## 使用示例

### 示例1: 基础查询

```java
@Autowired
private IQueryExecutor queryExecutor;

// 创建筛选条件
List<Filter> filters = new ArrayList<>();
filters.add(new Filter("status", "eq", "0"));
filters.add(new Filter("create_time", "gte", "2024-01-01"));

// 执行查询
SysUser user = SecurityUtils.getLoginUser().getUser();
QueryResult result = queryExecutor.executeQuery(datasetId, filters, user);

if (result.isSuccess()) {
    List<Map<String, Object>> data = result.getData();
    // 处理数据
}
```

### 示例2: 聚合查询

```java
// 定义维度
List<String> dimensions = Arrays.asList("dept_id", "status");

// 定义度量
List<Metric> metrics = new ArrayList<>();
metrics.add(new Metric("user_id", "COUNT", "user_count"));
metrics.add(new Metric("salary", "AVG", "avg_salary"));

// 执行聚合查询
QueryResult result = queryExecutor.executeAggregation(
    datasetId, dimensions, metrics, filters, user
);
```

## 测试覆盖

### 单元测试用例

1. **参数验证测试**
   - 数据集ID为空
   - 数据集不存在
   - 数据集已停用

2. **查询功能测试**
   - 直连数据集查询成功
   - 抽取数据集查询成功
   - 带筛选条件查询
   - 带权限过滤查询

3. **聚合查询测试**
   - 聚合查询成功
   - 维度和度量为空

4. **筛选操作符测试**
   - eq, ne, gt, gte, lt, lte
   - like, in, between

5. **计算字段测试**
   - 计算字段表达式解析
   - SELECT子句包含计算字段

6. **错误处理测试**
   - 查询超时
   - SQL异常

### 测试运行

```bash
# 运行QueryExecutor测试
mvn test -Dtest=QueryExecutorTest -pl iras-bi

# 运行所有BI平台测试
mvn test -pl iras-bi
```

## 性能优化建议

### 1. 使用抽取数据集
- 大数据量查询(>100万行)
- 频繁访问的查询
- 复杂JOIN查询

### 2. 添加索引
- 筛选字段添加索引
- dept_id字段添加索引
- 聚合维度字段添加索引

### 3. 查询优化
- 限制返回字段数量
- 使用分页查询
- 避免SELECT *

## 后续扩展

### 1. 缓存集成
- 集成CacheManager
- 缓存查询结果
- 配置TTL

### 2. JOIN支持
- 解析queryConfig中的joins配置
- 构建多表JOIN SQL
- 验证JOIN字段类型

### 3. 高级筛选
- 支持OR条件
- 支持嵌套条件
- 支持自定义函数

### 4. 查询优化
- SQL解析和优化
- 执行计划分析
- 慢查询日志

## 依赖关系

```
QueryExecutor
├── DatasetMapper (查询数据集配置)
├── DataSourceManager (获取数据源连接)
└── SysUser (获取用户权限信息)
```

## 注意事项

1. **SQL注入防护**: 当前实现使用字符串拼接,生产环境应使用PreparedStatement
2. **权限字段**: 假设数据表有dept_id字段,实际应根据数据集配置决定
3. **抽取数据查询**: 当前为简化实现,完整版需要解析JSON并应用筛选
4. **连接管理**: 确保在finally块中释放连接,避免连接泄漏
5. **错误日志**: 记录详细错误信息,但不暴露敏感数据

## 版本历史

- **v1.0** (2024-01): 初始实现
  - 基础查询功能
  - 聚合查询功能
  - 筛选和权限过滤
  - 计算字段支持
