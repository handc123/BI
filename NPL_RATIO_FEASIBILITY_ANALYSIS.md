# 不良贷款率配置可行性分析

## 问题
能否使用当前的数据配置功能配置"某机构某日的不良贷款率"？

## 结论
**⚠️ 部分可行，但需要后端支持或调整配置方案**

## 详细分析

### 当前功能状态

#### ✅ 已支持的功能
1. **基础指标配置**: 字段 + 聚合函数
2. **条件比率计算**: 分子条件 + 分母条件
3. **简单比率计算**: 指标间比率
4. **全局过滤条件**: 应用到整个查询的WHERE子句
5. **SQL安全验证**: 支持 IN 操作符

#### ❌ 缺失的功能
1. **基础指标的过滤条件**: 前端已实现，但后端 `BaseMetric` 类缺少 `filterCondition` 字段
2. **指标级别的维度筛选**: 不支持

### 问题根源

#### 前端实现（已完成）
`ui/src/components/MetricConfigDialog/index.vue` 中：
```vue
<el-form-item label="过滤条件">
  <el-input
    v-model="form.filterCondition"
    type="textarea"
    placeholder="可选，例如: status = 'ACTIVE' AND amount > 0"
  >
    <template slot="prepend">WHERE</template>
  </el-input>
</el-form-item>
```

#### 后端实现（缺失）
`iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/MetricConfigDTO.java` 中：
```java
public static class BaseMetric implements Serializable {
    private String name;
    private String alias;
    private String field;
    private String aggregation;
    // ❌ 缺少: private String filterCondition;
}
```

## 可行的配置方案

### 方案A: 使用条件比率（当前可用）✅

**优点**: 无需修改后端代码
**缺点**: 只能配置一个指标，不能单独查看不良贷款余额和总余额

#### 配置步骤

1. **全局过滤条件**（在DataConfig的"过滤条件"中）
   - `sjbsjgid = '001'` (机构)
   - `load_date = '2024-01-15'` (日期)

2. **计算指标 - 不良贷款率**
   - 指标类型: 计算指标
   - 计算类型: 条件比率
   - 字段: `jkye`
   - 分子条件: `wjfl IN ('次级', '可疑', '损失')`
   - 分母条件: `1=1` (或留空，表示所有记录)
   - 显示为百分比: ✓

#### 生成的SQL
```sql
SELECT 
  SUM(CASE WHEN wjfl IN ('次级', '可疑', '损失') THEN jkye ELSE 0 END) / 
    NULLIF(SUM(CASE WHEN 1=1 THEN jkye ELSE 0 END), 0) * 100 AS npl_ratio
FROM t_cbis_t_daikuan_hzwdb
WHERE sjbsjgid = '001' 
  AND load_date = '2024-01-15'
```

### 方案B: 修改后端支持基础指标过滤条件（推荐）⭐

**优点**: 
- 功能完整，可单独查看各个指标
- 基础指标可复用
- 配置更清晰

**缺点**: 需要修改后端代码

#### 需要修改的文件

1. **MetricConfigDTO.java** - 添加 filterCondition 字段
2. **MetricConverter.java** - 处理 filterCondition
3. **SQLDialectAdapter.java** - 生成 FILTER 子句
4. **DatasetServiceImpl.java** - 验证 filterCondition

#### 修改后的配置步骤

1. **全局过滤条件**
   - `sjbsjgid = '001'`
   - `load_date = '2024-01-15'`

2. **基础指标1 - 不良贷款余额**
   - 字段: `jkye`
   - 聚合函数: `SUM`
   - 过滤条件: `wjfl IN ('次级', '可疑', '损失')`

3. **基础指标2 - 贷款余额总计**
   - 字段: `jkye`
   - 聚合函数: `SUM`
   - 过滤条件: (留空)

4. **计算指标 - 不良贷款率**
   - 计算类型: 简单比率
   - 分子指标: `npl_balance`
   - 分母指标: `total_balance`
   - 显示为百分比: ✓

#### 生成的SQL
```sql
SELECT 
  SUM(CASE WHEN wjfl IN ('次级', '可疑', '损失') THEN jkye ELSE 0 END) AS npl_balance,
  SUM(jkye) AS total_balance,
  (SUM(CASE WHEN wjfl IN ('次级', '可疑', '损失') THEN jkye ELSE 0 END) / 
   NULLIF(SUM(jkye), 0)) * 100 AS npl_ratio
FROM t_cbis_t_daikuan_hzwdb
WHERE sjbsjgid = '001' 
  AND load_date = '2024-01-15'
```

### 方案C: 使用数据集级别的过滤（备选）

**优点**: 无需修改代码
**缺点**: 
- 不够灵活
- 每个机构/日期组合需要单独的数据集

#### 配置步骤

1. **创建专用数据集**
   - SQL: 
   ```sql
   SELECT * FROM t_cbis_t_daikuan_hzwdb 
   WHERE sjbsjgid = '001' AND load_date = '2024-01-15'
   ```

2. **配置条件比率指标**（同方案A）

## 推荐方案

### 短期方案（立即可用）
使用**方案A：条件比率**
- ✅ 无需修改代码
- ✅ 可以立即配置使用
- ⚠️ 功能有限

### 长期方案（推荐）
实施**方案B：修改后端支持基础指标过滤条件**
- ✅ 功能完整
- ✅ 配置灵活
- ✅ 指标可复用
- ⚠️ 需要开发工作

## 后端修改清单

如果选择方案B，需要进行以下修改：

### 1. MetricConfigDTO.java
```java
public static class BaseMetric implements Serializable {
    // ... 现有字段 ...
    
    /** 过滤条件(可选) */
    @Size(max = 500, message = "过滤条件长度不能超过500个字符")
    private String filterCondition;
    
    public String getFilterCondition() {
        return filterCondition;
    }
    
    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }
}
```

### 2. MetricConverter.java
```java
public String convertBaseMetric(MetricConfigDTO.BaseMetric metric, SQLDialectAdapter.DatabaseType dbType) {
    // ... 现有代码 ...
    
    // 验证过滤条件（如果存在）
    if (StringUtils.isNotEmpty(metric.getFilterCondition())) {
        sqlValidator.validateCondition(metric.getFilterCondition());
    }
    
    // 生成SQL
    String aggregationSql = dialectAdapter.generateAggregation(
        dbType, 
        metric.getAggregation(), 
        metric.getField(),
        metric.getFilterCondition()  // 传递过滤条件
    );
    
    return String.format("%s AS %s", 
        aggregationSql, 
        dialectAdapter.quoteIdentifier(dbType, metric.getName())
    );
}
```

### 3. SQLDialectAdapter.java
```java
public String generateAggregation(
        DatabaseType dbType, 
        String aggregation, 
        String field,
        String filterCondition) {
    
    String quotedField = quoteIdentifier(dbType, field);
    
    // 如果有过滤条件，使用CASE WHEN
    if (StringUtils.isNotEmpty(filterCondition)) {
        return String.format(
            "SUM(CASE WHEN %s THEN %s ELSE 0 END)",
            filterCondition,
            quotedField
        );
    }
    
    // 否则使用标准聚合
    return String.format("%s(%s)", aggregation, quotedField);
}
```

### 4. DatasetServiceImpl.java
```java
private void validateBaseMetric(MetricConfigDTO.BaseMetric metric) {
    sqlValidator.validateFieldName(metric.getField());
    sqlValidator.validateAggregation(metric.getAggregation());
    
    // 验证过滤条件（如果存在）
    if (StringUtils.isNotEmpty(metric.getFilterCondition())) {
        sqlValidator.validateCondition(metric.getFilterCondition());
    }
}
```

## 测试验证

### 测试数据
```sql
INSERT INTO t_cbis_t_daikuan_hzwdb 
  (id, sjbsjgid, load_date, wjfl, jkye) 
VALUES
  ('1', '001', '2024-01-15', '正常', 1000000.00),
  ('2', '001', '2024-01-15', '关注', 200000.00),
  ('3', '001', '2024-01-15', '次级', 50000.00),
  ('4', '001', '2024-01-15', '可疑', 30000.00),
  ('5', '001', '2024-01-15', '损失', 20000.00);
```

### 预期结果
- 不良贷款余额: 100,000
- 贷款余额总计: 1,300,000
- 不良贷款率: 7.69%

## 总结

| 方案 | 可行性 | 开发工作量 | 功能完整度 | 推荐度 |
|------|--------|-----------|-----------|--------|
| A. 条件比率 | ✅ 立即可用 | 无 | ⭐⭐⭐ | 短期推荐 |
| B. 后端支持 | ⚠️ 需开发 | 中等 | ⭐⭐⭐⭐⭐ | 长期推荐 |
| C. 数据集过滤 | ✅ 立即可用 | 无 | ⭐⭐ | 不推荐 |

**最终建议**:
1. **立即使用方案A**配置不良贷款率，满足当前需求
2. **规划实施方案B**，提升系统功能完整性和灵活性
3. 方案B的修改工作量不大，预计1-2天可完成
