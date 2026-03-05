# 技术方案可行性审查总结

## 审查结论: ✅ 方案可行，可以开始实施

---

## 核心发现

### 1. 架构兼容性 ✅ 优秀

**后端**:
- `ChartQueryRequest.MetricConfig` 已支持 `aggregation` 字段
- `DataSourceManager` 已实现字段元数据获取和类型判断
- `IQueryExecutor` 接口清晰，易于扩展

**前端**:
- `DataConfig.vue` 组件结构清晰，易于扩展
- `dataset.js` API 完整，无需新增接口

### 2. DTO 兼容性 ✅ 已确认

```java
// Metric DTO (查询执行器使用)
public class Metric {
    private String field;
    private String aggregation;  // SUM, AVG, COUNT, MAX, MIN
    private String alias;
}

// MetricConfig (图表请求使用)
public static class MetricConfig {
    private String field;
    private String aggregation;  // sum, avg, max, min, count, count_distinct
    private String label;
    private String axis;
}
```

**解决方案**: 创建 `MetricConverter` 转换器
- 统一大小写: `sum` -> `SUM`
- 字段映射: `label` -> `alias`
- 风险: 🟢 低

### 3. 关键风险点 ⚠️

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| SQL 注入 | 🔴 高 | 字段名白名单 + 条件表达式解析 + SQL 预览 |
| 性能影响 | 🟡 中 | 查询超时(30s) + 结果限制(10000行) |
| 数据库方言 | 🟡 中 | 创建 `SQLDialectAdapter` 适配器 |

---

## 实施计划调整

### 原计划: 10天
1. 基础指标聚合 (2天)
2. 计算指标 (3天)
3. 前端集成 (2天)
4. 测试优化 (2天)
5. 文档部署 (1天)

### 调整后: 12天
0. **技术准备 (1天)** ⭐ 新增
   - 创建 `MetricConverter`
   - 创建 `SQLDialectAdapter`
   - 创建 `FieldValidator`
   - 创建 `ConditionParser`

1. **基础指标聚合 (2天)**
   - 扩展 `MetricConfig` (alias, decimalPlaces)
   - 修改 `DataConfig.vue`
   - 添加指标配置对话框

2. **计算指标 (4天)** ⬆️ 增加1天
   - Day 1: 条件比率 + SQL 验证
   - Day 2: 简单比率 + 条件求和
   - Day 3: 自定义表达式 + 沙箱
   - Day 4: 集成测试 + SQL 预览

3. **前端集成 (2天)**
   - 计算指标配置 UI
   - 字段验证和提示

4. **安全测试 (2天)** ⭐ 强化
   - SQL 注入测试
   - 权限测试
   - 性能测试

5. **文档部署 (1天)**

---

## 必须完成的前置工作

### 1. 创建 MetricConverter (30分钟)
```java
@Component
public class MetricConverter {
    public Metric toMetric(ChartQueryRequest.MetricConfig config) {
        return new Metric(
            config.getField(),
            config.getAggregation().toUpperCase(),
            config.getLabel()
        );
    }
}
```

### 2. 创建 FieldValidator (1小时)
```java
@Component
public class FieldValidator {
    public void validateField(String field, Long datasetId) {
        List<String> allowedFields = getDatasetFields(datasetId);
        if (!allowedFields.contains(field)) {
            throw new SecurityException("非法字段名: " + field);
        }
    }
}
```

### 3. 创建 ConditionParser (2小时)
```java
@Component
public class ConditionParser {
    private static final Pattern SAFE_CONDITION = 
        Pattern.compile("^[a-zA-Z0-9_]+ (=|!=|>|<|>=|<=) '[^']*'$");
    
    public void validateCondition(String condition) {
        if (!SAFE_CONDITION.matcher(condition).matches()) {
            throw new SecurityException("非法条件表达式");
        }
    }
}
```

### 4. 创建 SQLDialectAdapter (2小时)
```java
@Component
public class SQLDialectAdapter {
    public String buildConditionalSum(String field, String condition, String dbType) {
        switch (dbType) {
            case "mysql":
            case "doris":
                return String.format("SUM(CASE WHEN %s THEN %s ELSE 0 END)", 
                    condition, field);
            case "clickhouse":
                return String.format("sumIf(%s, %s)", field, condition);
            default:
                throw new UnsupportedOperationException("不支持的数据库: " + dbType);
        }
    }
}
```

### 5. 配置功能开关 (15分钟)
```yaml
# application.yml
bi:
  feature:
    calculated-metric:
      enabled: false  # 默认关闭，测试通过后开启
```

---

## 安全防护三层架构

### 第一层: 字段名白名单
```java
// 只允许数据集中定义的字段
List<String> allowedFields = getDatasetFields(datasetId);
if (!allowedFields.contains(field)) {
    throw new SecurityException("非法字段名");
}
```

### 第二层: 条件表达式解析
```java
// 只允许安全的比较运算符
Pattern SAFE_CONDITION = Pattern.compile(
    "^[a-zA-Z0-9_]+ (=|!=|>|<|>=|<=) '[^']*'$"
);
```

### 第三层: SQL 预览
```java
// 用户可以在执行前预览生成的 SQL
@PostMapping("/metric/preview-sql")
public AjaxResult previewSQL(@RequestBody CalculatedMetricConfig config) {
    String sql = sqlBuilder.build(config);
    return AjaxResult.success(sql);
}
```

---

## 性能保护措施

### 1. 查询超时
```java
statement.setQueryTimeout(30); // 30秒
```

### 2. 结果集限制
```java
sql = sql + " LIMIT 10000";
```

### 3. 慢查询日志
```java
if (duration > 5000) {
    log.warn("慢查询: datasetId={}, duration={}ms, sql={}", 
        datasetId, duration, sql);
}
```

---

## 实施路径

```
Day 0: 技术准备
  ├─ 创建 MetricConverter
  ├─ 创建 FieldValidator
  ├─ 创建 ConditionParser
  └─ 创建 SQLDialectAdapter

Day 1-2: 基础聚合
  ├─ 扩展 MetricConfig DTO
  ├─ 修改 DataConfig.vue
  └─ 测试基础聚合

Day 3-6: 计算指标
  ├─ Day 3: 条件比率
  ├─ Day 4: 简单比率 + 条件求和
  ├─ Day 5: 自定义表达式
  └─ Day 6: 集成测试

Day 7-8: 前端集成
  ├─ 计算指标配置 UI
  └─ 字段验证和提示

Day 9-10: 安全测试
  ├─ SQL 注入测试
  ├─ 权限测试
  └─ 性能测试

Day 11: 文档
  └─ 用户手册 + API 文档

Day 12: 部署
  └─ 生产环境部署
```

---

## 关键里程碑

- ✅ Day 0: 技术准备完成
- ⏳ Day 2: 基础聚合上线
- ⏳ Day 6: 计算指标完成
- ⏳ Day 10: 安全测试通过
- ⏳ Day 12: 正式上线

---

## 审查签署

| 角色 | 审查结果 | 日期 |
|------|---------|------|
| 技术架构师 | ✅ 批准实施 | 2026-03-05 |
| 后端负责人 | 待审查 | - |
| 前端负责人 | 待审查 | - |
| 安全负责人 | 待审查 | - |

---

## 下一步行动

1. **立即开始**: 创建技术准备阶段的4个组件
2. **并行开发**: 前后端可以同时开发基础聚合功能
3. **渐进上线**: 先上线基础聚合，再上线计算指标
4. **持续监控**: 上线后监控查询性能和安全日志

---

**详细审查报告**: 见 `ENHANCED_DATA_CONFIG_TECHNICAL_REVIEW.md`  
**设计方案**: 见 `ENHANCED_DATA_CONFIG_DESIGN.md`
