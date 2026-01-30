# DataExtractScheduler - 数据抽取调度器

## 概述

DataExtractScheduler 是 BI 平台的核心组件之一，负责管理抽取数据集的定时任务和立即抽取功能。

## 文件结构

```
scheduler/
├── IDataExtractScheduler.java          # 调度器接口
├── DataExtractJob.java                 # Quartz 定时任务
├── impl/
│   └── DataExtractSchedulerImpl.java   # 调度器实现
├── IMPLEMENTATION.md                   # 详细实现文档
└── README.md                           # 本文件
```

## 核心功能

### 1. 定时抽取调度 (scheduleExtract)

为抽取数据集配置定时任务，支持标准 Cron 表达式。

**示例**:
```java
// 每天凌晨2点执行抽取
dataExtractScheduler.scheduleExtract(datasetId, "0 0 2 * * ?");
```

### 2. 立即抽取 (executeExtract)

手动触发数据抽取，支持全量和增量抽取。

**特性**:
- 自动重试机制(最多3次)
- 指数退避策略
- 详细的抽取结果反馈

### 3. 取消调度 (cancelSchedule)

取消已调度的抽取任务。

### 4. 检查任务状态 (isScheduled)

检查数据集是否已调度抽取任务。

## API 端点

### POST /bi/dataset/{id}/extract

执行立即抽取

**权限**: `bi:dataset:edit`

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "rowCount": 1250,
    "executionTime": 3500,
    "retryCount": 0
  }
}
```

## 配置

### Quartz 配置

在 `QuartzConfig` 类中配置 Quartz Scheduler：

```java
@Configuration
public class QuartzConfig {
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setStartupDelay(10);
        return factory;
    }
}
```

### 数据集抽取配置

在数据集的 `extractConfig` 字段中配置：

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

## 依赖

### Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### 组件依赖

- `DatasetMapper`: 数据集数据访问
- `DataSourceManager`: 数据源连接管理
- `Scheduler`: Quartz 调度器

## 测试

### 单元测试

运行测试：
```bash
mvn test -Dtest=DataExtractSchedulerTest -pl iras-bi
```

### 测试覆盖

- ✅ 调度任务创建和替换
- ✅ 立即抽取执行
- ✅ 错误处理和验证
- ✅ 任务取消和状态检查

## 使用示例

### 创建抽取数据集并调度

```java
// 1. 创建抽取数据集
Dataset dataset = new Dataset();
dataset.setName("用户数据抽取");
dataset.setDatasourceId(1L);
dataset.setType("extract");
dataset.setQueryConfig("{\"sourceType\":\"table\",\"tableName\":\"sys_user\"}");
dataset.setExtractConfig("{\"schedule\":{\"enabled\":true,\"cronExpression\":\"0 0 2 * * ?\"},\"incremental\":false}");

datasetService.insertDataset(dataset);

// 2. 调度抽取任务
dataExtractScheduler.scheduleExtract(dataset.getId(), "0 0 2 * * ?");
```

### 执行立即抽取

```java
ExtractResult result = dataExtractScheduler.executeExtract(datasetId);

if (result.isSuccess()) {
    logger.info("抽取成功: {} 行, 耗时: {}ms", 
        result.getRowCount(), result.getExecutionTime());
} else {
    logger.error("抽取失败: {}", result.getErrorMessage());
}
```

## 错误处理

### 重试机制

- 最大重试次数: 3次
- 重试策略: 指数退避 (2秒, 4秒, 8秒)
- 失败后保留上一次成功的数据

### 常见错误

1. **数据集不存在**: 检查数据集ID是否正确
2. **数据集类型错误**: 只有抽取类型数据集才能执行抽取
3. **数据源连接失败**: 检查数据源配置和网络连接
4. **查询超时**: 优化查询SQL或增加超时时间
5. **存储失败**: 检查系统数据库连接和磁盘空间

## 性能优化建议

1. **批量插入**: 使用 PreparedStatement 批处理
2. **分批抽取**: 大数据量时分批抽取和插入
3. **并行抽取**: 支持多个数据集并行抽取
4. **增量优化**: 优化增量抽取的查询条件
5. **压缩存储**: 对 JSON 数据进行压缩

## 注意事项

1. **系统数据库**: 当前使用 datasetId=1 的数据源，实际应使用独立的系统数据库连接
2. **并发控制**: 同一数据集不应同时执行多个抽取任务
3. **资源清理**: 确保连接、语句、结果集正确关闭
4. **事务管理**: 抽取操作使用事务，确保数据一致性

## 后续改进

- [ ] 通知机制: 抽取失败时发送通知
- [ ] 进度跟踪: 实时显示抽取进度
- [ ] 断点续传: 支持抽取中断后继续
- [ ] 数据校验: 抽取后校验数据完整性
- [ ] 监控告警: 集成监控系统

## 相关文档

- [详细实现文档](./IMPLEMENTATION.md)
- [设计文档](../../../../../../.kiro/specs/bi-platform-upgrade/design.md)
- [需求文档](../../../../../../.kiro/specs/bi-platform-upgrade/requirements.md)
