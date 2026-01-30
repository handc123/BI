# DataExtractScheduler 实现文档

## 概述

DataExtractScheduler 是 BI 平台的数据抽取调度器，负责管理抽取数据集的定时任务和立即抽取功能。

## 实现的功能

### 1. 定时抽取调度

**功能**: 为抽取数据集配置定时任务，按照 Cron 表达式定期执行数据抽取

**实现类**: `DataExtractSchedulerImpl.scheduleExtract()`

**关键特性**:
- 支持标准 Cron 表达式配置
- 自动替换已存在的调度任务
- 使用 Quartz Scheduler 管理任务
- 任务命名规则: `extract-job-{datasetId}`

**示例**:
```java
// 每天凌晨2点执行抽取
dataExtractScheduler.scheduleExtract(1L, "0 0 2 * * ?");

// 每小时执行抽取
dataExtractScheduler.scheduleExtract(2L, "0 0 * * * ?");

// 每周一凌晨3点执行抽取
dataExtractScheduler.scheduleExtract(3L, "0 0 3 ? * MON");
```

### 2. 立即抽取

**功能**: 手动触发数据抽取，立即从数据源抽取数据并存储

**实现类**: `DataExtractSchedulerImpl.executeExtract()`

**关键特性**:
- 支持全量抽取和增量抽取
- 失败自动重试机制(最多3次，指数退避)
- 更新数据集元数据(最后抽取时间、行数)
- 返回详细的抽取结果

**抽取流程**:
1. 验证数据集配置
2. 从数据源查询数据
3. 删除旧数据(非增量抽取)
4. 批量插入新数据到 `bi_extract_data` 表
5. 更新数据集元数据
6. 返回抽取结果

**增量抽取**:
- 根据 `extractConfig.incremental` 配置决定是否增量抽取
- 使用 `extractConfig.incrementalField` 字段作为增量条件
- 只抽取上次抽取时间之后的数据

### 3. 取消调度

**功能**: 取消已调度的抽取任务

**实现类**: `DataExtractSchedulerImpl.cancelSchedule()`

**关键特性**:
- 从 Quartz Scheduler 中删除任务
- 不影响已抽取的数据
- 可以重新调度

### 4. 检查任务状态

**功能**: 检查数据集是否已调度抽取任务

**实现类**: `DataExtractSchedulerImpl.isScheduled()`

**关键特性**:
- 查询 Quartz Scheduler 中的任务状态
- 返回布尔值表示是否已调度

## 数据模型

### ExtractResult (抽取结果)

```java
public class ExtractResult {
    private boolean success;           // 是否成功
    private long rowCount;             // 抽取行数
    private long executionTime;        // 执行时间(毫秒)
    private Date startTime;            // 开始时间
    private Date endTime;              // 结束时间
    private String errorMessage;       // 错误消息
    private int retryCount;            // 重试次数
}
```

### 数据存储

**bi_extract_data 表结构**:
```sql
CREATE TABLE bi_extract_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dataset_id BIGINT NOT NULL,
    data_content TEXT NOT NULL,
    extract_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dataset (dataset_id),
    INDEX idx_extract_time (extract_time)
);
```

**数据格式**:
- `data_content` 字段存储 JSON 格式的数据行
- 每行数据作为一个 JSON 对象存储
- 支持高效查询和过滤

## API 端点

### POST /bi/dataset/{id}/extract

**功能**: 执行立即抽取

**权限**: `bi:dataset:edit`

**请求参数**:
- `id`: 数据集ID (路径参数)

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "rowCount": 1250,
    "executionTime": 3500,
    "startTime": "2024-01-15T10:30:00",
    "endTime": "2024-01-15T10:30:03",
    "retryCount": 0
  }
}
```

## 配置

### Quartz 配置

**配置类**: `QuartzConfig`

**关键配置**:
- `overwriteExistingJobs`: true (允许覆盖已存在的任务)
- `startupDelay`: 10秒 (延迟启动，等待应用完全初始化)

### 抽取配置示例

**数据集 extractConfig 字段**:
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

## 错误处理

### 重试机制

**策略**: 指数退避
- 第1次重试: 等待 2秒
- 第2次重试: 等待 4秒
- 第3次重试: 等待 8秒
- 超过3次: 返回失败结果

**失败处理**:
1. 记录详细错误日志
2. 保留上一次成功的抽取数据
3. 返回失败结果给调用方
4. TODO: 发送通知给管理员

### 常见错误

1. **数据集不存在**: 返回错误消息
2. **数据集类型错误**: 只有抽取类型数据集才能执行抽取
3. **数据集已停用**: 不允许抽取已停用的数据集
4. **数据源连接失败**: 重试3次后返回失败
5. **查询超时**: 超时时间300秒，超时后重试
6. **存储失败**: 事务回滚，保留旧数据

## 性能优化

### 当前实现

1. **批量插入**: 使用 Statement 批量插入数据
2. **事务管理**: 使用 `@Transactional` 确保数据一致性
3. **连接池**: 复用 DataSourceManager 的连接池
4. **超时控制**: 查询超时300秒

### 待优化项

1. **PreparedStatement**: 使用 PreparedStatement 批处理提高性能
2. **分批插入**: 大数据量时分批插入，避免内存溢出
3. **并行抽取**: 支持多个数据集并行抽取
4. **增量优化**: 优化增量抽取的查询条件
5. **压缩存储**: 对 JSON 数据进行压缩存储

## 测试

### 单元测试

**测试类**: `DataExtractSchedulerTest`

**测试覆盖**:
- ✅ 调度抽取任务 - 成功场景
- ✅ 调度抽取任务 - 数据集ID为空
- ✅ 调度抽取任务 - 无效的Cron表达式
- ✅ 调度抽取任务 - 替换已存在的任务
- ✅ 执行立即抽取 - 数据集不存在
- ✅ 执行立即抽取 - 数据集类型不是抽取类型
- ✅ 执行立即抽取 - 数据集已停用
- ✅ 取消调度任务 - 成功场景
- ✅ 取消调度任务 - 任务不存在
- ✅ 检查任务是否存在 - 任务存在
- ✅ 检查任务是否存在 - 任务不存在
- ✅ 检查任务是否存在 - 数据集ID为空

### 集成测试

**待实现**:
- 端到端抽取流程测试
- 大数据量抽取性能测试
- 并发抽取测试
- 失败重试测试

## 使用示例

### 1. 配置定时抽取

```java
// 创建抽取数据集
Dataset dataset = new Dataset();
dataset.setName("用户数据抽取");
dataset.setDatasourceId(1L);
dataset.setType("extract");
dataset.setQueryConfig("{\"sourceType\":\"table\",\"tableName\":\"sys_user\"}");
dataset.setExtractConfig("{\"schedule\":{\"enabled\":true,\"cronExpression\":\"0 0 2 * * ?\"},\"incremental\":false}");

datasetService.insertDataset(dataset);

// 调度抽取任务
dataExtractScheduler.scheduleExtract(dataset.getId(), "0 0 2 * * ?");
```

### 2. 执行立即抽取

```java
// 执行立即抽取
ExtractResult result = dataExtractScheduler.executeExtract(datasetId);

if (result.isSuccess()) {
    System.out.println("抽取成功: " + result.getRowCount() + " 行");
} else {
    System.out.println("抽取失败: " + result.getErrorMessage());
}
```

### 3. 取消调度

```java
// 取消调度任务
dataExtractScheduler.cancelSchedule(datasetId);
```

### 4. 检查任务状态

```java
// 检查任务是否已调度
boolean isScheduled = dataExtractScheduler.isScheduled(datasetId);
System.out.println("任务已调度: " + isScheduled);
```

## 依赖

### Maven 依赖

```xml
<!-- Quartz Scheduler -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### 组件依赖

- `DatasetMapper`: 数据集数据访问
- `DataSourceManager`: 数据源连接管理
- `Scheduler`: Quartz 调度器

## 注意事项

1. **系统数据库连接**: 当前实现使用 `datasetId=1` 的数据源作为系统数据库，实际应该使用独立的系统数据库连接
2. **事务管理**: 抽取操作使用事务，确保数据一致性
3. **并发控制**: 同一数据集不应同时执行多个抽取任务
4. **资源清理**: 确保连接、语句、结果集正确关闭
5. **日志记录**: 详细记录抽取过程，便于问题排查

## 后续改进

1. **通知机制**: 抽取失败时发送邮件或消息通知管理员
2. **进度跟踪**: 实时显示抽取进度百分比
3. **断点续传**: 支持抽取中断后从断点继续
4. **数据校验**: 抽取后校验数据完整性和准确性
5. **监控告警**: 集成监控系统，实时监控抽取任务状态
