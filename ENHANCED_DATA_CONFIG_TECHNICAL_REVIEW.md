# 增强数据配置技术方案可行性审查报告

## 审查日期
2026-03-05

## 审查范围
对《增强数据配置设计方案》(ENHANCED_DATA_CONFIG_DESIGN.md) 进行技术可行性审查，验证方案与现有架构的兼容性。

---

## 1. 现有架构分析

### 1.1 后端架构

#### 1.1.1 ChartQueryRequest DTO 结构
**位置**: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/ChartQueryRequest.java`

**现有结构**:
```java
public class ChartQueryRequest {
    private Long datasetId;
    private List<DimensionConfig> dimensions;
    private List<MetricConfig> metrics;  // ✅ 已存在
    private List<Filter> filters;
    private Integer limit;
    private Map<String, Object> params;
    
    public static class MetricConfig {
        private String field;
        private String aggregation;  // ✅ 已支持聚合
        private String label;
        private String axis;
    }
}
```

**兼容性评估**: ✅ 高度兼容
- `MetricConfig` 已包含 `aggregation` 字段，支持基础聚合
- 现有聚合方法: sum, avg, max, min, count, count_distinct
- **需要扩展**: 添加计算指标支持

#### 1.1.2 数据源管理器
**位置**: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/DataSourceManager.java`

**核心功能**:
- ✅ 多数据源连接池管理 (MySQL, PostgreSQL, ClickHouse, Doris, Oracle)
- ✅ 表结构获取 (`getTableSchema`)
- ✅ 字段类型自动判断 (`determineFieldType`)
- ✅ 数据预览 (`getTablePreview`)

**字段类型判断逻辑**:
```java
private String determineFieldType(ColumnInfo column) {
    // 主键 -> DIMENSION
    // 字段名后缀 (_count, _amount, _rate) -> MEASURE
    // 数值类型 (INT, DECIMAL, DOUBLE) -> MEASURE
    // 其他 -> DIMENSION
}
```

**兼容性评估**: ✅ 完全兼容
- 已实现字段元数据获取
- 已实现字段类型自动分类
- 无需修改

#### 1.1.3 查询执行器
**位置**: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IQueryExecutor.java`

**接口定义**:
```java
public interface IQueryExecutor {
    QueryResult executeQuery(Long datasetId, List<Filter> filters, SysUser user);
    
    QueryResult executeAggregation(Long datasetId, 
                                   List<String> dimensions,
                                   List<Metric> metrics,  // ⚠️ 使用 Metric DTO
                                   List<Filter> filters, 
                                   SysUser user);
}
```

**兼容性评估**: ⚠️ 需要适配
- 现有 `Metric` DTO 与 `ChartQueryRequest.MetricConfig` 可能不同
- 需要确认 `Metric` DTO 结构
- **建议**: 统一使用 `MetricConfig` 或创建转换器

### 1.2 前端架构

#### 1.2.1 数据集 API
**位置**: `ui/src/api/bi/dataset.js`

**现有 API**:
```javascript
// ✅ 获取数据集字段元数据
export function getDatasetFields(id) {
  return request({
    url: `/bi/dataset/${id}/fields`,
    method: 'get'
  })
}

// ✅ 获取数据集数据
export function getDatasetData(id, params) {
  return request({
    url: `/bi/dataset/${id}/data`,
    method: 'post',
    data: params || {}
  })
}
```

**兼容性评估**: ✅ 完全兼容
- 已有字段元数据获取接口
- 已有数据查询接口
- 无需新增 API

#### 1.2.2 DataConfig.vue 组件
**位置**: `ui/src/components/ConfigPanel/DataConfig.vue`

**现有功能**:
- ✅ 数据源和数据集选择
- ✅ 维度字段配置 (拖拽)
- ✅ 指标字段配置 (拖拽)
- ✅ 过滤条件配置
- ✅ 数据预览

**兼容性评估**: ✅ 可扩展
- 组件结构清晰，易于扩展
- 已有字段拖拽逻辑
- **需要扩展**: 添加指标配置对话框

---

## 2. 方案可行性评估

### 2.1 阶段一：基础指标聚合 ✅ 可行

#### 2.1.1 后端扩展
**需要修改的文件**:
1. `ChartQueryRequest.MetricConfig` - 添加字段
   ```java
   public static class MetricConfig {
       private String field;
       private String aggregation;  // 已存在
       private String label;
       private String axis;
       // 新增字段
       private String alias;         // ✅ 新增
       private Integer decimalPlaces; // ✅ 新增
   }
   ```

**风险评估**: 🟢 低风险
- 向后兼容：新增字段为可选
- 不影响现有功能
- 实现简单

#### 2.1.2 前端扩展
**需要修改的文件**:
1. `DataConfig.vue` - 添加指标配置对话框
2. 创建 `MetricConfigDialog.vue` 组件

**风险评估**: 🟢 低风险
- 组件化设计，隔离影响
- 不修改现有拖拽逻辑
- UI 扩展性强

### 2.2 阶段二：计算指标 ⚠️ 需要注意

#### 2.2.1 DTO 扩展
**需要新增的类**:
```java
// 1. 计算指标配置
public class CalculatedMetricConfig {
    private String id;
    private String name;
    private String type;  // conditional_ratio, simple_ratio, conditional_sum, custom
    private Object config; // 具体配置对象
}

// 2. 条件比率配置
public class ConditionalRatioConfig {
    private String numeratorField;
    private String denominatorField;
    private String numeratorCondition;
    private String denominatorCondition;
    private Integer decimalPlaces;
}
```

**风险评估**: 🟡 中等风险
- 需要扩展 `ChartQueryRequest`
- 需要确保 JSON 序列化/反序列化正确
- **建议**: 使用 `@JsonTypeInfo` 处理多态

#### 2.2.2 SQL 生成器
**需要新增的类**:
```java
public class CalculatedMetricSQLBuilder {
    public String buildConditionalRatio(ConditionalRatioConfig config);
    public String buildSimpleRatio(SimpleRatioConfig config);
    public String buildConditionalSum(ConditionalSumConfig config);
    public String buildCustomExpression(CustomExpressionConfig config);
}
```

**风险评估**: 🟡 中等风险
- SQL 注入风险：需要严格验证
- 不同数据库方言差异
- **建议**: 
  - 使用白名单验证字段名
  - 使用参数化查询
  - 添加 SQL 预览功能

#### 2.2.3 查询执行流程
**现有流程**:
```
ChartQueryRequest 
  -> QueryExecutor.executeAggregation()
  -> DataSourceManager.getConnection()
  -> 执行 SQL
  -> 返回 QueryResult
```

**扩展后流程**:
```
ChartQueryRequest (含计算指标)
  -> CalculatedMetricSQLBuilder.build()  // ✅ 新增
  -> QueryExecutor.executeAggregation()
  -> DataSourceManager.getConnection()
  -> 执行 SQL (含计算字段)
  -> 返回 QueryResult
```

**风险评估**: 🟡 中等风险
- 需要在 SQL 生成阶段插入计算逻辑
- 需要确保与现有聚合逻辑兼容
- **建议**: 在 `QueryExecutor` 实现类中添加计算指标处理

---

## 3. 关键技术问题

### 3.1 ✅ 问题1: Metric DTO 结构确认

**问题描述**:
- `ChartQueryRequest.MetricConfig` 与 `IQueryExecutor` 中的 `Metric` 类型需要确认兼容性

**实际情况**:
```java
// Metric DTO (IQueryExecutor 使用)
public class Metric {
    private String field;
    private String aggregation;  // SUM, AVG, COUNT, MAX, MIN
    private String alias;
}

// MetricConfig (ChartQueryRequest 使用)
public static class MetricConfig {
    private String field;
    private String aggregation;  // sum, avg, max, min, count, count_distinct
    private String label;
    private String axis;
}
```

**兼容性分析**:
- ✅ 核心字段一致: `field`, `aggregation`
- ⚠️ 命名差异: `alias` vs `label`
- ⚠️ 大小写差异: `SUM` vs `sum`
- ✅ `MetricConfig` 包含额外的 `axis` 字段（用于图表配置）

**解决方案**:
创建轻量级转换器，统一两种 DTO：

```java
@Component
public class MetricConverter {
    public Metric toMetric(ChartQueryRequest.MetricConfig config) {
        return new Metric(
            config.getField(),
            config.getAggregation().toUpperCase(),  // 统一大写
            config.getLabel()  // label -> alias
        );
    }
    
    public List<Metric> toMetrics(List<ChartQueryRequest.MetricConfig> configs) {
        return configs.stream()
            .map(this::toMetric)
            .collect(Collectors.toList());
    }
}
```

**推荐**: 创建转换器 (风险低，改动小)

### 3.2 ⚠️ 问题2: SQL 注入风险

**问题描述**:
- 计算指标允许用户输入条件表达式
- 自定义表达式允许用户输入 SQL 片段
- 存在 SQL 注入风险

**解决方案**:
1. **字段名白名单验证**
   ```java
   public boolean isValidFieldName(String field, List<String> allowedFields) {
       return allowedFields.contains(field);
   }
   ```

2. **条件表达式解析**
   ```java
   public String parseCondition(String condition) {
       // 只允许: =, !=, >, <, >=, <=, IN, BETWEEN
       // 不允许: OR, AND, UNION, SELECT, DROP, etc.
   }
   ```

3. **自定义表达式沙箱**
   ```java
   public String validateCustomExpression(String expression) {
       // 使用 JSQLParser 解析
       // 只允许: 字段引用、算术运算、函数调用
       // 不允许: 子查询、JOIN、DDL/DML
   }
   ```

**推荐**: 三层防护 + SQL 预览

### 3.3 ✅ 问题3: 数据库方言差异

**问题描述**:
- MySQL, PostgreSQL, ClickHouse 语法差异
- 条件聚合语法不同

**解决方案**:
```java
public class SQLDialectAdapter {
    public String buildConditionalSum(String field, String condition, String dbType) {
        switch (dbType) {
            case "mysql":
            case "doris":
                return String.format("SUM(CASE WHEN %s THEN %s ELSE 0 END)", condition, field);
            case "postgresql":
                return String.format("SUM(CASE WHEN %s THEN %s ELSE 0 END)", condition, field);
            case "clickhouse":
                return String.format("sumIf(%s, %s)", field, condition);
            default:
                throw new UnsupportedOperationException("不支持的数据库类型: " + dbType);
        }
    }
}
```

**推荐**: 创建方言适配器

---

## 4. 性能影响评估

### 4.1 查询性能

**影响因素**:
1. 计算指标增加 SQL 复杂度
2. CASE WHEN 表达式可能影响索引使用
3. 多个计算指标可能导致全表扫描

**优化建议**:
1. 添加查询超时限制 (30秒)
2. 添加结果集大小限制 (10000行)
3. 建议用户在数据库层面创建物化视图
4. 添加查询计划分析工具

### 4.2 前端性能

**影响因素**:
1. 配置对话框增加 DOM 复杂度
2. 计算指标配置增加数据传输量

**优化建议**:
1. 使用虚拟滚动 (字段列表)
2. 配置对话框懒加载
3. 使用 Web Worker 处理复杂计算

---

## 5. 兼容性矩阵

| 组件 | 现有功能 | 需要修改 | 向后兼容 | 风险等级 |
|------|---------|---------|---------|---------|
| ChartQueryRequest | ✅ | ✅ 扩展字段 | ✅ | 🟢 低 |
| DataSourceManager | ✅ | ❌ 无需修改 | ✅ | 🟢 低 |
| IQueryExecutor | ✅ | ✅ 添加计算逻辑 | ✅ | 🟡 中 |
| DataConfig.vue | ✅ | ✅ 添加配置UI | ✅ | 🟢 低 |
| dataset.js API | ✅ | ❌ 无需修改 | ✅ | 🟢 低 |

---

## 6. 实施建议

### 6.1 优先级调整

**原计划**:
1. 阶段一：基础指标聚合 (2天)
2. 阶段二：计算指标 (3天)
3. 阶段三：前端集成 (2天)
4. 阶段四：测试优化 (2天)
5. 阶段五：文档部署 (1天)

**调整后**:
1. **阶段0：技术准备** (1天) ⭐ 新增
   - 创建 `MetricConverter` 工具类
   - 创建 `SQLDialectAdapter` 方言适配器
   - 创建 `SQLValidator` SQL 验证器
   
2. **阶段一：基础指标聚合** (2天)
   - 扩展 `MetricConfig` DTO
   - 修改 `DataConfig.vue`
   - 添加基础聚合测试
   
3. **阶段二：计算指标（分步实施）** (4天) ⬆️ 增加1天
   - Day 1: 条件比率 + SQL 验证
   - Day 2: 简单比率 + 条件求和
   - Day 3: 自定义表达式 + 沙箱
   - Day 4: 集成测试 + SQL 预览
   
4. **阶段三：前端集成** (2天)
   - 计算指标配置 UI
   - 字段验证和提示
   
5. **阶段四：安全测试** (2天) ⭐ 强化
   - SQL 注入测试
   - 权限测试
   - 性能测试
   
6. **阶段五：文档部署** (1天)

**总计**: 12天 (原10天)

### 6.2 风险缓解措施

#### 6.2.1 SQL 注入防护
```java
// 1. 字段名白名单
@Component
public class FieldValidator {
    public void validateField(String field, Long datasetId) {
        List<String> allowedFields = getDatasetFields(datasetId);
        if (!allowedFields.contains(field)) {
            throw new SecurityException("非法字段名: " + field);
        }
    }
}

// 2. 条件表达式解析
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

// 3. SQL 预览
@RestController
public class MetricController {
    @PostMapping("/metric/preview-sql")
    public AjaxResult previewSQL(@RequestBody CalculatedMetricConfig config) {
        String sql = sqlBuilder.build(config);
        return AjaxResult.success(sql);
    }
}
```

#### 6.2.2 性能保护
```java
@Component
public class QueryGuard {
    private static final int MAX_QUERY_TIME = 30000; // 30秒
    private static final int MAX_RESULT_SIZE = 10000; // 10000行
    
    public QueryResult executeWithGuard(String sql) {
        // 设置超时
        statement.setQueryTimeout(MAX_QUERY_TIME / 1000);
        
        // 限制结果集
        sql = sql + " LIMIT " + MAX_RESULT_SIZE;
        
        return execute(sql);
    }
}
```

#### 6.2.3 向后兼容
```java
// 1. DTO 版本控制
public class ChartQueryRequest {
    private String version = "2.0"; // 新增版本字段
    
    // 兼容旧版本
    public List<MetricConfig> getMetrics() {
        if (version.equals("1.0")) {
            return convertLegacyMetrics();
        }
        return metrics;
    }
}

// 2. 渐进式启用
@Configuration
public class FeatureToggle {
    @Value("${bi.feature.calculated-metric.enabled:false}")
    private boolean calculatedMetricEnabled;
    
    public boolean isCalculatedMetricEnabled() {
        return calculatedMetricEnabled;
    }
}
```

---

## 7. 审查结论

### 7.1 总体评估: ✅ 技术方案可行

**优势**:
1. ✅ 与现有架构高度兼容
2. ✅ 扩展点设计合理
3. ✅ 向后兼容性好
4. ✅ 实施风险可控

**风险点**:
1. ⚠️ SQL 注入风险 - 需要严格防护
2. ⚠️ 性能影响 - 需要监控和优化
3. ⚠️ 数据库方言差异 - 需要适配器

### 7.2 前置条件

在开始实施前，必须完成以下准备工作:

1. **✅ Metric DTO 结构已确认**
   - ✅ 已检查 `com.zjrcu.iras.bi.platform.domain.dto.Metric` 类
   - ✅ 核心字段兼容 (`field`, `aggregation`)
   - ✅ 决定使用转换器方案 (`MetricConverter`)

2. **✅ 创建安全组件**
   - `FieldValidator` - 字段名验证
   - `ConditionParser` - 条件表达式解析
   - `SQLValidator` - SQL 验证器
   - `SQLDialectAdapter` - 数据库方言适配器

3. **✅ 配置功能开关**
   - 在 `application.yml` 中添加功能开关
   - 支持渐进式启用

### 7.3 实施建议

**推荐实施路径**:
```
阶段0 (技术准备) 
  ↓
阶段1 (基础聚合) → 测试 → 上线
  ↓
阶段2.1 (条件比率) → 测试 → 上线
  ↓
阶段2.2 (简单比率) → 测试 → 上线
  ↓
阶段2.3 (自定义表达式) → 安全审计 → 测试 → 上线
```

**关键里程碑**:
- Day 1: 技术准备完成
- Day 3: 基础聚合上线
- Day 7: 条件比率上线
- Day 10: 全功能上线
- Day 12: 文档完成

### 7.4 后续优化

**短期优化** (1-2周):
1. 添加查询计划分析
2. 添加慢查询日志
3. 优化前端性能

**中期优化** (1-2月):
1. 支持更多计算类型
2. 添加计算指标模板库
3. 支持跨数据集计算

**长期优化** (3-6月):
1. 支持实时计算
2. 支持分布式查询
3. 支持 AI 辅助配置

---

## 8. 审查签署

| 角色 | 姓名 | 审查意见 | 日期 |
|------|------|---------|------|
| 技术架构师 | Kiro | ✅ 批准，需完成前置条件 | 2026-03-05 |
| 后端负责人 | - | 待审查 | - |
| 前端负责人 | - | 待审查 | - |
| 安全负责人 | - | 待审查 | - |

---

## 附录A: 已检查的文件

### A.1 已完成检查 ✅
1. ✅ `com.zjrcu.iras.bi.platform.domain.dto.Metric` - DTO 结构已确认
2. ✅ `com.zjrcu.iras.bi.platform.domain.dto.ChartQueryRequest` - 请求结构已确认
3. ✅ `com.zjrcu.iras.bi.platform.service.IQueryExecutor` - 接口定义已确认
4. ✅ `com.zjrcu.iras.bi.platform.engine.DataSourceManager` - 数据源管理已确认
5. ✅ `ui/src/api/bi/dataset.js` - 前端 API 已确认

### A.2 待检查（实施阶段）
1. `com.zjrcu.iras.bi.platform.service.impl.QueryExecutorImpl` - 查询执行实现
2. `com.zjrcu.iras.bi.platform.engine.QueryExecutor` - SQL 生成逻辑
3. 现有聚合查询的 SQL 生成代码
4. 现有的 SQL 注入防护措施
5. 现有的查询性能监控

---

## 附录B: 参考资料

1. [MyBatis 防止 SQL 注入](https://mybatis.org/mybatis-3/sqlmap-xml.html#Parameters)
2. [JSQLParser 使用指南](https://github.com/JSQLParser/JSqlParser)
3. [HikariCP 性能优化](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
4. [ECharts 数据转换](https://echarts.apache.org/handbook/zh/concepts/dataset)

---

**文档版本**: 1.0  
**最后更新**: 2026-03-05  
**下次审查**: 实施完成后
