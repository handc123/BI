# 增强数据配置功能 - 详细设计方案

## 1. 概述

### 1.1 背景
当前 DataConfig.vue 组件只支持从数据集中选择预定义的维度和指标字段，对于多维汇总表（如 t_cbis_t_daikuan_hzwdb）的灵活配置存在限制。用户无法配置计算指标（如不良贷款率 = 不良贷款余额 / 贷款总余额）。

### 1.2 目标
- 保持从数据集获取维度和指标字段的方式
- 为指标字段增加聚合方式配置（SUM、AVG、COUNT、MAX、MIN）
- 支持计算指标配置（条件比率、简单比率、自定义表达式）
- 支持字段别名和单位配置
- 保持界面简洁易用

### 1.3 适用场景
- 金融指标计算：不良贷款率、资本充足率、拨备覆盖率
- 业务比率分析：转化率、增长率、占比分析
- 多维度汇总分析：按机构、地区、时间等维度的灵活分析

## 2. 功能设计

### 2.1 功能架构

```
数据配置组件 (DataConfig.vue)
├── 数据源选择
├── 数据集选择
├── 维度配置（保持不变）
│   ├── 可用维度字段列表
│   ├── 已选维度列表
│   └── 拖拽交互
├── 指标配置（增强）
│   ├── 基础指标（新增聚合和别名）
│   │   ├── 可用指标字段列表
│   │   ├── 已选指标列表
│   │   ├── 聚合方式选择
│   │   └── 别名配置
│   └── 计算指标（新增）
│       ├── 条件比率
│       ├── 简单比率
│       ├── 条件求和
│       └── 自定义表达式
├── 过滤条件（保持不变）
└── 操作按钮
```

### 2.2 核心功能

#### 2.2.1 基础指标增强

**功能描述**：
为每个选中的指标字段配置聚合方式和别名。

**配置项**：
- 聚合方式：SUM（求和）、AVG（平均）、COUNT（计数）、MAX（最大值）、MIN（最小值）
- 别名：自定义显示名称
- 小数位：结果保留的小数位数（默认2位）

**示例**：
```
字段: jkye (借款余额)
聚合: SUM
别名: 贷款总余额
小数位: 2
```

生成SQL：
```sql
SUM(jkye) AS 贷款总余额
```

#### 2.2.2 计算指标

**功能描述**：
支持配置复杂的计算指标，无需在数据集中预定义。

**计算类型**：

1. **条件比率 (Conditional Ratio)**
   - 用途：计算满足特定条件的比率
   - 示例：不良贷款率 = 不良贷款余额 / 贷款总余额
   
2. **简单比率 (Simple Ratio)**
   - 用途：两个指标的简单除法
   - 示例：人均贷款 = 贷款总额 / 客户数量
   
3. **条件求和 (Conditional Sum)**
   - 用途：满足条件的字段求和
   - 示例：不良贷款余额 = SUM(CASE WHEN 五级分类 IN ('次级','可疑','损失') THEN 余额 ELSE 0 END)
   
4. **自定义表达式 (Custom Expression)**
   - 用途：完全自定义的SQL表达式
   - 示例：(字段A + 字段B) / 字段C * 100


## 3. 界面设计

### 3.1 整体布局

```
┌─ 数据配置 ─────────────────────────────────────────────────┐
│ 数据源: [下拉选择数据源________________]                    │
│ 数据集: [下拉选择数据集________________]                    │
│                                                            │
│ ━━━━━━━━━━━━━━━━ 字段配置 ━━━━━━━━━━━━━━━━                │
│                                                            │
│ 维度 (根据图表类型显示/隐藏)                                │
│ ┌──────────────────┐  ┌─────────────────────────────┐    │
│ │ 可用维度字段      │  │ 已选维度                     │    │
│ │ ┌──────────────┐ │  │ ┌─────────────────────────┐ │    │
│ │ │🔷 dklx       │ │  │ │ 🔷 dklx  贷款类型    [×]│ │    │
│ │ │  贷款类型     │ │  │ │ 🔷 qydm  区域代码    [×]│ │    │
│ │ │🔷 qydm       │ │  │ └─────────────────────────┘ │    │
│ │ │  区域代码     │ │  │ 拖拽字段到此处               │    │
│ │ └──────────────┘ │  └─────────────────────────────┘    │
│ └──────────────────┘                                      │
│                                                            │
│ 指标                                                       │
│ [基础指标] [计算指标]  ← 标签页切换                        │
│                                                            │
│ ┌─ 基础指标 ─────────────────────────────────────────┐    │
│ │ 可用指标字段                                        │    │
│ │ ┌──────────────┐                                   │    │
│ │ │📊 jkye       │                                   │    │
│ │ │  借款余额     │                                   │    │
│ │ │📊 jkje       │                                   │    │
│ │ │  借款金额     │                                   │    │
│ │ └──────────────┘                                   │    │
│ │                                                     │    │
│ │ 已选指标                                            │    │
│ │ ┌─────────────────────────────────────────────┐   │    │
│ │ │ 📊 jkye 借款余额                             │   │    │
│ │ │   聚合: [SUM ▼]  别名: [总余额_______]       │   │    │
│ │ │   小数位: [2]  [×删除]                       │   │    │
│ │ │─────────────────────────────────────────────│   │    │
│ │ │ 📊 jkbs 借款笔数                             │   │    │
│ │ │   聚合: [COUNT ▼]  别名: [总笔数_____]       │   │    │
│ │ │   小数位: [0]  [×删除]                       │   │    │
│ │ └─────────────────────────────────────────────┘   │    │
│ │ 拖拽字段到此处                                      │    │
│ └────────────────────────────────────────────────────┘    │
│                                                            │
│ ━━━━━━━━━━━━━━━━ 过滤条件 ━━━━━━━━━━━━━━━━                │
│ [dkzt ▼] [等于 ▼] [正常__________] [删除]                 │
│ [+ 添加过滤条件]                                          │
│                                                            │
│ [刷新数据] [预览数据]                                      │
└────────────────────────────────────────────────────────────┘
```

### 3.2 计算指标配置界面

```
┌─ 计算指标 ─────────────────────────────────────────────┐
│                                                        │
│ 已配置计算指标:                                         │
│ ┌────────────────────────────────────────────────┐   │
│ │ 📐 不良贷款率                              [×] │   │
│ │   类型: 条件比率                               │   │
│ │   公式: 不良贷款余额 / 贷款总余额 × 100        │   │
│ │   [编辑] [复制]                                │   │
│ └────────────────────────────────────────────────┘   │
│                                                        │
│ [+ 添加计算指标]                                       │
│                                                        │
│ ┌─ 添加计算指标对话框 ──────────────────────────┐     │
│ │                                                │     │
│ │ 指标名称: [不良贷款率_______________]          │     │
│ │                                                │     │
│ │ 计算类型: [条件比率 ▼]                         │     │
│ │           ├─ 条件比率                          │     │
│ │           ├─ 简单比率                          │     │
│ │           ├─ 条件求和                          │     │
│ │           └─ 自定义表达式                       │     │
│ │                                                │     │
│ │ ┌─ 条件比率配置 ────────────────────────────┐ │     │
│ │ │ 分子(不良贷款):                            │ │     │
│ │ │   字段: [jkye ▼] 借款余额                  │ │     │
│ │ │   聚合: [SUM ▼]                            │ │     │
│ │ │   条件字段: [wjfl ▼] 五级分类              │ │     │
│ │ │   操作符: [包含于 ▼]                       │ │     │
│ │ │   值: [次级类] [可疑类] [损失类] [+添加]   │ │     │
│ │ │                                            │ │     │
│ │ │ 分母(总贷款):                              │ │     │
│ │ │   字段: [jkye ▼] 借款余额                  │ │     │
│ │ │   聚合: [SUM ▼]                            │ │     │
│ │ │   条件: 无                                 │ │     │
│ │ │                                            │ │     │
│ │ │ 结果格式:                                  │ │     │
│ │ │   乘数: [100] (转换为百分比)               │ │     │
│ │ │   单位: [%]                                │ │     │
│ │ │   小数位: [2]                              │ │     │
│ │ └────────────────────────────────────────────┘ │     │
│ │                                                │     │
│ │ SQL预览:                                       │     │
│ │ ┌────────────────────────────────────────────┐ │     │
│ │ │ SUM(CASE WHEN wjfl IN ('次级类','可疑类', │ │     │
│ │ │ '损失类') THEN jkye ELSE 0 END) /         │ │     │
│ │ │ NULLIF(SUM(jkye), 0) * 100                │ │     │
│ │ │ AS 不良贷款率                              │ │     │
│ │ └────────────────────────────────────────────┘ │     │
│ │                                                │     │
│ │ [取消] [确定]                                  │     │
│ └────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────┘
```


## 4. 数据结构设计

### 4.1 前端数据结构

```javascript
// 完整的数据配置对象
{
  datasourceId: 1,
  datasetId: 10,
  
  // 维度配置（保持不变）
  dimensions: [
    {
      fieldName: 'dklx',
      fieldType: 'dimension',
      comment: '贷款类型'
    },
    {
      fieldName: 'qydm',
      fieldType: 'dimension',
      comment: '区域代码'
    }
  ],
  
  // 基础指标配置（增强）
  metrics: [
    {
      fieldName: 'jkye',
      fieldType: 'metric',
      comment: '借款余额',
      aggregation: 'sum',      // 聚合方式: sum, avg, count, max, min
      alias: '贷款总余额',      // 别名
      decimals: 2              // 小数位数
    },
    {
      fieldName: 'jkbs',
      fieldType: 'metric',
      comment: '借款笔数',
      aggregation: 'count',
      alias: '贷款笔数',
      decimals: 0
    }
  ],
  
  // 计算指标配置（新增）
  calculatedMetrics: [
    {
      id: 'calc_1',
      name: '不良贷款率',
      type: 'conditionalRatio',  // 计算类型
      config: {
        numerator: {              // 分子配置
          field: 'jkye',
          aggregation: 'sum',
          condition: {
            field: 'wjfl',
            operator: 'in',
            values: ['次级类', '可疑类', '损失类']
          }
        },
        denominator: {            // 分母配置
          field: 'jkye',
          aggregation: 'sum',
          condition: null
        },
        result: {
          multiplier: 100,        // 乘数
          unit: '%',              // 单位
          decimals: 2             // 小数位
        }
      },
      // 生成的SQL表达式（后端生成，前端只读）
      sqlExpression: "SUM(CASE WHEN wjfl IN ('次级类','可疑类','损失类') THEN jkye ELSE 0 END) / NULLIF(SUM(jkye), 0) * 100 AS 不良贷款率"
    }
  ],
  
  // 过滤条件（保持不变）
  filters: [
    {
      field: 'dkzt',
      operator: '=',
      value: '正常'
    }
  ]
}
```

### 4.2 计算指标类型定义

#### 4.2.1 条件比率 (Conditional Ratio)

```javascript
{
  type: 'conditionalRatio',
  config: {
    numerator: {
      field: string,           // 字段名
      aggregation: string,     // 聚合方式
      condition: {
        field: string,         // 条件字段
        operator: string,      // 操作符: =, !=, >, <, >=, <=, in, not in, like
        values: array          // 值列表
      }
    },
    denominator: {
      field: string,
      aggregation: string,
      condition: null | object
    },
    result: {
      multiplier: number,      // 乘数（如100转百分比）
      unit: string,            // 单位
      decimals: number         // 小数位
    }
  }
}
```

#### 4.2.2 简单比率 (Simple Ratio)

```javascript
{
  type: 'simpleRatio',
  config: {
    numerator: {
      field: string,
      aggregation: string
    },
    denominator: {
      field: string,
      aggregation: string
    },
    result: {
      multiplier: number,
      unit: string,
      decimals: number
    }
  }
}
```

#### 4.2.3 条件求和 (Conditional Sum)

```javascript
{
  type: 'conditionalSum',
  config: {
    field: string,
    aggregation: string,
    condition: {
      field: string,
      operator: string,
      values: array
    },
    result: {
      alias: string,
      decimals: number
    }
  }
}
```

#### 4.2.4 自定义表达式 (Custom Expression)

```javascript
{
  type: 'customExpression',
  config: {
    expression: string,      // SQL表达式，使用{fieldName}作为占位符
    fields: array,           // 使用的字段列表
    result: {
      alias: string,
      unit: string,
      decimals: number
    }
  }
}
```


## 5. 后端设计

### 5.1 DTO 设计

#### 5.1.1 MetricConfig.java

```java
package com.zjrcu.iras.bi.platform.domain.dto;

import lombok.Data;

/**
 * 基础指标配置
 */
@Data
public class MetricConfig {
    /** 字段名 */
    private String fieldName;
    
    /** 字段类型 */
    private String fieldType;
    
    /** 字段注释 */
    private String comment;
    
    /** 聚合方式: sum, avg, count, max, min */
    private String aggregation;
    
    /** 别名 */
    private String alias;
    
    /** 小数位数 */
    private Integer decimals;
}
```

#### 5.1.2 CalculatedMetricConfig.java

```java
package com.zjrcu.iras.bi.platform.domain.dto;

import lombok.Data;
import java.util.Map;

/**
 * 计算指标配置
 */
@Data
public class CalculatedMetricConfig {
    /** 指标ID */
    private String id;
    
    /** 指标名称 */
    private String name;
    
    /** 计算类型: conditionalRatio, simpleRatio, conditionalSum, customExpression */
    private String type;
    
    /** 配置详情（JSON格式） */
    private Map<String, Object> config;
    
    /** 生成的SQL表达式 */
    private String sqlExpression;
}
```

#### 5.1.3 ConditionalRatioConfig.java

```java
package com.zjrcu.iras.bi.platform.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 条件比率配置
 */
@Data
public class ConditionalRatioConfig {
    /** 分子配置 */
    private AggregationWithCondition numerator;
    
    /** 分母配置 */
    private AggregationWithCondition denominator;
    
    /** 结果配置 */
    private ResultFormat result;
    
    @Data
    public static class AggregationWithCondition {
        /** 字段名 */
        private String field;
        
        /** 聚合方式 */
        private String aggregation;
        
        /** 条件（可选） */
        private Condition condition;
    }
    
    @Data
    public static class Condition {
        /** 条件字段 */
        private String field;
        
        /** 操作符 */
        private String operator;
        
        /** 值列表 */
        private List<String> values;
    }
    
    @Data
    public static class ResultFormat {
        /** 乘数 */
        private Integer multiplier;
        
        /** 单位 */
        private String unit;
        
        /** 小数位 */
        private Integer decimals;
    }
}
```

### 5.2 SQL生成器设计

#### 5.2.1 CalculatedMetricSQLBuilder.java

```java
package com.zjrcu.iras.bi.platform.engine;

import com.zjrcu.iras.bi.platform.domain.dto.CalculatedMetricConfig;
import com.zjrcu.iras.bi.platform.domain.dto.ConditionalRatioConfig;
import org.springframework.stereotype.Component;

/**
 * 计算指标SQL生成器
 */
@Component
public class CalculatedMetricSQLBuilder {
    
    /**
     * 生成计算指标SQL
     */
    public String buildSQL(CalculatedMetricConfig metric) {
        switch (metric.getType()) {
            case "conditionalRatio":
                return buildConditionalRatioSQL(metric);
            case "simpleRatio":
                return buildSimpleRatioSQL(metric);
            case "conditionalSum":
                return buildConditionalSumSQL(metric);
            case "customExpression":
                return buildCustomExpressionSQL(metric);
            default:
                throw new IllegalArgumentException("Unknown metric type: " + metric.getType());
        }
    }
    
    /**
     * 生成条件比率SQL
     */
    private String buildConditionalRatioSQL(CalculatedMetricConfig metric) {
        ConditionalRatioConfig config = parseConfig(metric.getConfig(), ConditionalRatioConfig.class);
        
        // 构建分子
        String numerator = buildAggregationWithCondition(config.getNumerator());
        
        // 构建分母
        String denominator = buildAggregationWithCondition(config.getDenominator());
        
        // 构建完整表达式
        StringBuilder sql = new StringBuilder();
        sql.append(numerator);
        sql.append(" / NULLIF(");
        sql.append(denominator);
        sql.append(", 0)");
        
        // 应用乘数
        if (config.getResult().getMultiplier() != null && config.getResult().getMultiplier() != 1) {
            sql.append(" * ").append(config.getResult().getMultiplier());
        }
        
        // 添加别名
        sql.append(" AS ").append(metric.getName());
        
        return sql.toString();
    }
    
    /**
     * 构建带条件的聚合表达式
     */
    private String buildAggregationWithCondition(ConditionalRatioConfig.AggregationWithCondition agg) {
        String aggregation = agg.getAggregation().toUpperCase();
        String field = agg.getField();
        
        if (agg.getCondition() == null) {
            // 无条件，简单聚合
            return String.format("%s(%s)", aggregation, field);
        } else {
            // 有条件，使用CASE WHEN
            String condition = buildCondition(agg.getCondition());
            return String.format(
                "%s(CASE WHEN %s THEN %s ELSE 0 END)",
                aggregation,
                condition,
                field
            );
        }
    }
    
    /**
     * 构建条件表达式
     */
    private String buildCondition(ConditionalRatioConfig.Condition condition) {
        String field = condition.getField();
        String operator = condition.getOperator();
        List<String> values = condition.getValues();
        
        switch (operator.toLowerCase()) {
            case "in":
                String valueList = values.stream()
                    .map(v -> "'" + v + "'")
                    .collect(Collectors.joining(", "));
                return String.format("%s IN (%s)", field, valueList);
                
            case "not in":
                valueList = values.stream()
                    .map(v -> "'" + v + "'")
                    .collect(Collectors.joining(", "));
                return String.format("%s NOT IN (%s)", field, valueList);
                
            case "=":
            case "!=":
            case ">":
            case "<":
            case ">=":
            case "<=":
                return String.format("%s %s '%s'", field, operator, values.get(0));
                
            case "like":
                return String.format("%s LIKE '%%%s%%'", field, values.get(0));
                
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
    
    /**
     * 解析配置对象
     */
    private <T> T parseConfig(Map<String, Object> config, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(config, clazz);
    }
}
```


### 5.3 查询执行器增强

#### 5.3.1 QueryExecutor.java 增强

```java
package com.zjrcu.iras.bi.platform.engine;

import com.zjrcu.iras.bi.platform.domain.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 查询执行器（增强版）
 */
@Component
public class QueryExecutor {
    
    @Autowired
    private CalculatedMetricSQLBuilder calculatedMetricBuilder;
    
    /**
     * 构建完整查询SQL
     */
    public String buildQuerySQL(ChartQueryRequest request) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // 1. 添加维度字段
        if (request.getDimensions() != null && !request.getDimensions().isEmpty()) {
            for (DimensionConfig dim : request.getDimensions()) {
                sql.append(dim.getFieldName()).append(" AS ").append(dim.getComment()).append(", ");
            }
        }
        
        // 2. 添加基础指标
        if (request.getMetrics() != null && !request.getMetrics().isEmpty()) {
            for (MetricConfig metric : request.getMetrics()) {
                sql.append(buildMetricSQL(metric)).append(", ");
            }
        }
        
        // 3. 添加计算指标
        if (request.getCalculatedMetrics() != null && !request.getCalculatedMetrics().isEmpty()) {
            for (CalculatedMetricConfig calcMetric : request.getCalculatedMetrics()) {
                sql.append(calculatedMetricBuilder.buildSQL(calcMetric)).append(", ");
            }
        }
        
        // 移除最后的逗号
        sql.setLength(sql.length() - 2);
        
        // 4. FROM子句
        sql.append(" FROM ").append(request.getTableName());
        
        // 5. WHERE子句
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < request.getFilters().size(); i++) {
                if (i > 0) sql.append(" AND ");
                FilterConfig filter = request.getFilters().get(i);
                sql.append(buildFilterSQL(filter));
            }
        }
        
        // 6. GROUP BY子句
        if (request.getDimensions() != null && !request.getDimensions().isEmpty()) {
            sql.append(" GROUP BY ");
            for (int i = 0; i < request.getDimensions().size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append(request.getDimensions().get(i).getFieldName());
            }
        }
        
        // 7. ORDER BY子句（可选）
        if (request.getOrderBy() != null) {
            sql.append(" ORDER BY ").append(request.getOrderBy());
        }
        
        // 8. LIMIT子句（可选）
        if (request.getLimit() != null) {
            sql.append(" LIMIT ").append(request.getLimit());
        }
        
        return sql.toString();
    }
    
    /**
     * 构建基础指标SQL
     */
    private String buildMetricSQL(MetricConfig metric) {
        String aggregation = metric.getAggregation().toUpperCase();
        String field = metric.getFieldName();
        String alias = metric.getAlias() != null ? metric.getAlias() : metric.getComment();
        
        return String.format("%s(%s) AS %s", aggregation, field, alias);
    }
    
    /**
     * 构建过滤条件SQL
     */
    private String buildFilterSQL(FilterConfig filter) {
        String field = filter.getField();
        String operator = filter.getOperator();
        String value = filter.getValue();
        
        switch (operator) {
            case "=":
            case "!=":
            case ">":
            case "<":
            case ">=":
            case "<=":
                return String.format("%s %s '%s'", field, operator, value);
            case "like":
                return String.format("%s LIKE '%%%s%%'", field, value);
            case "not like":
                return String.format("%s NOT LIKE '%%%s%%'", field, value);
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
}
```

### 5.4 API接口设计

#### 5.4.1 ComponentController.java 增强

```java
/**
 * 验证计算指标配置
 */
@PostMapping("/validateCalculatedMetric")
public AjaxResult validateCalculatedMetric(@RequestBody CalculatedMetricConfig metric) {
    try {
        // 生成SQL
        String sql = calculatedMetricBuilder.buildSQL(metric);
        
        // 验证SQL语法（可选）
        // sqlValidator.validate(sql);
        
        return AjaxResult.success("验证通过", sql);
    } catch (Exception e) {
        return AjaxResult.error("验证失败: " + e.getMessage());
    }
}

/**
 * 预览计算指标结果
 */
@PostMapping("/previewCalculatedMetric")
public AjaxResult previewCalculatedMetric(@RequestBody ChartQueryRequest request) {
    try {
        // 构建查询SQL
        String sql = queryExecutor.buildQuerySQL(request);
        
        // 执行查询（限制10条）
        request.setLimit(10);
        QueryResult result = queryExecutor.executeQuery(request);
        
        return AjaxResult.success(result);
    } catch (Exception e) {
        return AjaxResult.error("预览失败: " + e.getMessage());
    }
}
```


## 6. 前端实现

### 6.1 组件结构

```
ui/src/components/ConfigPanel/
├── DataConfig.vue                    # 主组件（增强）
├── MetricConfigItem.vue              # 基础指标配置项（新增）
├── CalculatedMetricDialog.vue        # 计算指标配置对话框（新增）
├── ConditionalRatioConfig.vue        # 条件比率配置（新增）
├── SimpleRatioConfig.vue             # 简单比率配置（新增）
├── ConditionalSumConfig.vue          # 条件求和配置（新增）
└── CustomExpressionConfig.vue        # 自定义表达式配置（新增）
```

### 6.2 核心组件实现

#### 6.2.1 MetricConfigItem.vue

```vue
<template>
  <div class="metric-config-item">
    <!-- 字段信息 -->
    <div class="metric-header">
      <i class="el-icon-s-data"></i>
      <span class="field-name">{{ metric.comment || metric.fieldName }}</span>
      <el-button 
        type="text" 
        icon="el-icon-close" 
        @click="$emit('remove')"
      />
    </div>
    
    <!-- 配置项 -->
    <div class="metric-config-row">
      <label>聚合:</label>
      <el-select 
        v-model="localMetric.aggregation" 
        size="mini"
        @change="handleChange"
      >
        <el-option label="求和" value="sum" />
        <el-option label="平均" value="avg" />
        <el-option label="计数" value="count" />
        <el-option label="最大值" value="max" />
        <el-option label="最小值" value="min" />
      </el-select>
      
      <label>别名:</label>
      <el-input 
        v-model="localMetric.alias" 
        size="mini"
        placeholder="显示名称"
        @change="handleChange"
      />
    </div>
    
    <div class="metric-config-row">
      <label>小数位:</label>
      <el-input-number 
        v-model="localMetric.decimals" 
        size="mini"
        :min="0"
        :max="6"
        @change="handleChange"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'MetricConfigItem',
  props: {
    metric: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      localMetric: { ...this.metric }
    }
  },
  methods: {
    handleChange() {
      this.$emit('change', this.localMetric)
    }
  },
  watch: {
    metric: {
      handler(val) {
        this.localMetric = { ...val }
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.metric-config-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.metric-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.field-name {
  flex: 1;
  font-weight: 500;
  margin-left: 6px;
}

.metric-config-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.metric-config-row label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.metric-config-row:last-child {
  margin-bottom: 0;
}
</style>
```

#### 6.2.2 CalculatedMetricDialog.vue

```vue
<template>
  <el-dialog
    title="配置计算指标"
    :visible.sync="dialogVisible"
    width="700px"
    @close="handleClose"
  >
    <el-form :model="form" label-width="100px" size="small">
      <!-- 指标名称 -->
      <el-form-item label="指标名称" required>
        <el-input v-model="form.name" placeholder="请输入指标名称" />
      </el-form-item>
      
      <!-- 计算类型 -->
      <el-form-item label="计算类型" required>
        <el-select v-model="form.type" @change="handleTypeChange">
          <el-option label="条件比率" value="conditionalRatio" />
          <el-option label="简单比率" value="simpleRatio" />
          <el-option label="条件求和" value="conditionalSum" />
          <el-option label="自定义表达式" value="customExpression" />
        </el-select>
      </el-form-item>
      
      <!-- 动态配置组件 -->
      <component
        :is="configComponent"
        v-model="form.config"
        :available-fields="availableFields"
      />
      
      <!-- SQL预览 -->
      <el-form-item label="SQL预览">
        <el-input
          v-model="sqlPreview"
          type="textarea"
          :rows="4"
          readonly
        />
      </el-form-item>
    </el-form>
    
    <div slot="footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import ConditionalRatioConfig from './ConditionalRatioConfig.vue'
import SimpleRatioConfig from './SimpleRatioConfig.vue'
import ConditionalSumConfig from './ConditionalSumConfig.vue'
import CustomExpressionConfig from './CustomExpressionConfig.vue'
import { validateCalculatedMetric } from '@/api/bi/component'

export default {
  name: 'CalculatedMetricDialog',
  components: {
    ConditionalRatioConfig,
    SimpleRatioConfig,
    ConditionalSumConfig,
    CustomExpressionConfig
  },
  props: {
    visible: Boolean,
    availableFields: Array,
    metric: Object
  },
  data() {
    return {
      dialogVisible: this.visible,
      form: {
        id: null,
        name: '',
        type: 'conditionalRatio',
        config: {}
      },
      sqlPreview: ''
    }
  },
  computed: {
    configComponent() {
      const componentMap = {
        'conditionalRatio': 'ConditionalRatioConfig',
        'simpleRatio': 'SimpleRatioConfig',
        'conditionalSum': 'ConditionalSumConfig',
        'customExpression': 'CustomExpressionConfig'
      }
      return componentMap[this.form.type]
    }
  },
  methods: {
    handleTypeChange() {
      this.form.config = {}
      this.sqlPreview = ''
    },
    
    async handleConfirm() {
      // 验证配置
      try {
        const response = await validateCalculatedMetric(this.form)
        if (response.code === 200) {
          this.form.sqlExpression = response.data
          this.$emit('confirm', this.form)
          this.handleClose()
        } else {
          this.$message.error(response.msg)
        }
      } catch (error) {
        this.$message.error('验证失败')
      }
    },
    
    handleClose() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val && this.metric) {
        this.form = { ...this.metric }
      }
    },
    'form.config': {
      handler() {
        // 实时生成SQL预览
        this.generateSQLPreview()
      },
      deep: true
    }
  }
}
</script>
```


#### 6.2.3 ConditionalRatioConfig.vue

```vue
<template>
  <div class="conditional-ratio-config">
    <el-divider>条件比率配置</el-divider>
    
    <!-- 分子配置 -->
    <div class="config-section">
      <div class="section-title">分子（满足条件的值）</div>
      
      <el-form-item label="字段">
        <el-select v-model="localConfig.numerator.field" @change="handleChange">
          <el-option
            v-for="field in metricFields"
            :key="field.fieldName"
            :label="field.comment || field.fieldName"
            :value="field.fieldName"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="聚合">
        <el-select v-model="localConfig.numerator.aggregation" @change="handleChange">
          <el-option label="求和" value="sum" />
          <el-option label="平均" value="avg" />
          <el-option label="计数" value="count" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="条件字段">
        <el-select v-model="localConfig.numerator.condition.field" @change="handleChange">
          <el-option
            v-for="field in dimensionFields"
            :key="field.fieldName"
            :label="field.comment || field.fieldName"
            :value="field.fieldName"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="操作符">
        <el-select v-model="localConfig.numerator.condition.operator" @change="handleChange">
          <el-option label="等于" value="=" />
          <el-option label="不等于" value="!=" />
          <el-option label="包含于" value="in" />
          <el-option label="不包含于" value="not in" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="值">
        <el-select
          v-model="localConfig.numerator.condition.values"
          multiple
          filterable
          allow-create
          @change="handleChange"
        >
          <el-option
            v-for="value in conditionFieldValues"
            :key="value"
            :label="value"
            :value="value"
          />
        </el-select>
      </el-form-item>
    </div>
    
    <!-- 分母配置 -->
    <div class="config-section">
      <div class="section-title">分母（总值）</div>
      
      <el-form-item label="字段">
        <el-select v-model="localConfig.denominator.field" @change="handleChange">
          <el-option
            v-for="field in metricFields"
            :key="field.fieldName"
            :label="field.comment || field.fieldName"
            :value="field.fieldName"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="聚合">
        <el-select v-model="localConfig.denominator.aggregation" @change="handleChange">
          <el-option label="求和" value="sum" />
          <el-option label="平均" value="avg" />
          <el-option label="计数" value="count" />
        </el-select>
      </el-form-item>
    </div>
    
    <!-- 结果格式 -->
    <div class="config-section">
      <div class="section-title">结果格式</div>
      
      <el-form-item label="乘数">
        <el-input-number
          v-model="localConfig.result.multiplier"
          :min="1"
          @change="handleChange"
        />
        <span class="hint">（如100转换为百分比）</span>
      </el-form-item>
      
      <el-form-item label="单位">
        <el-input v-model="localConfig.result.unit" @change="handleChange" />
      </el-form-item>
      
      <el-form-item label="小数位">
        <el-input-number
          v-model="localConfig.result.decimals"
          :min="0"
          :max="6"
          @change="handleChange"
        />
      </el-form-item>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConditionalRatioConfig',
  props: {
    value: Object,
    availableFields: Array
  },
  data() {
    return {
      localConfig: {
        numerator: {
          field: '',
          aggregation: 'sum',
          condition: {
            field: '',
            operator: 'in',
            values: []
          }
        },
        denominator: {
          field: '',
          aggregation: 'sum',
          condition: null
        },
        result: {
          multiplier: 100,
          unit: '%',
          decimals: 2
        }
      },
      conditionFieldValues: []
    }
  },
  computed: {
    metricFields() {
      return this.availableFields.filter(f => f.fieldType === 'metric')
    },
    dimensionFields() {
      return this.availableFields.filter(f => f.fieldType === 'dimension')
    }
  },
  methods: {
    handleChange() {
      this.$emit('input', this.localConfig)
    },
    
    async loadConditionFieldValues() {
      // 加载条件字段的可选值
      // 可以从后端API获取字段的唯一值列表
    }
  },
  mounted() {
    if (this.value) {
      this.localConfig = { ...this.value }
    }
  }
}
</script>

<style scoped>
.conditional-ratio-config {
  padding: 16px;
}

.config-section {
  margin-bottom: 20px;
  padding: 12px;
  background: #f9fafc;
  border-radius: 4px;
}

.section-title {
  font-weight: 500;
  margin-bottom: 12px;
  color: #303133;
}

.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
```

### 6.3 API服务

#### 6.3.1 component.js 增强

```javascript
import request from '@/utils/request'

/**
 * 验证计算指标配置
 */
export function validateCalculatedMetric(data) {
  return request({
    url: '/bi/component/validateCalculatedMetric',
    method: 'post',
    data: data
  })
}

/**
 * 预览计算指标结果
 */
export function previewCalculatedMetric(data) {
  return request({
    url: '/bi/component/previewCalculatedMetric',
    method: 'post',
    data: data
  })
}

/**
 * 获取字段唯一值列表
 */
export function getFieldDistinctValues(datasetId, fieldName) {
  return request({
    url: `/bi/dataset/${datasetId}/field/${fieldName}/values`,
    method: 'get'
  })
}
```


## 7. 实施计划

### 7.1 Phase 1: 基础指标聚合（1-2天）

**目标**: 为基础指标添加聚合方式和别名配置

**任务**:
1. 前端
   - 创建 MetricConfigItem.vue 组件
   - 修改 DataConfig.vue，集成 MetricConfigItem
   - 添加聚合方式下拉框
   - 添加别名输入框
   - 添加小数位配置

2. 后端
   - 增强 MetricConfig DTO
   - 修改 QueryExecutor 支持聚合SQL生成
   - 测试基础聚合功能

**验收标准**:
- 用户可以为每个指标选择聚合方式
- 用户可以自定义指标别名
- 生成的SQL正确包含聚合函数
- 图表正确显示聚合后的数据

### 7.2 Phase 2: 计算指标 - 条件比率（2-3天）

**目标**: 实现条件比率计算（如不良贷款率）

**任务**:
1. 前端
   - 创建 CalculatedMetricDialog.vue
   - 创建 ConditionalRatioConfig.vue
   - 添加计算指标标签页
   - 实现配置界面和交互

2. 后端
   - 创建 CalculatedMetricConfig DTO
   - 创建 ConditionalRatioConfig DTO
   - 实现 CalculatedMetricSQLBuilder
   - 实现条件比率SQL生成逻辑
   - 添加验证和预览API

3. 测试
   - 单元测试：SQL生成逻辑
   - 集成测试：完整查询流程
   - UI测试：配置界面交互

**验收标准**:
- 用户可以配置条件比率指标
- SQL生成正确（包含CASE WHEN）
- 计算结果准确
- 支持多个条件值

### 7.3 Phase 3: 计算指标 - 简单比率和条件求和（1-2天）

**目标**: 扩展更多计算类型

**任务**:
1. 前端
   - 创建 SimpleRatioConfig.vue
   - 创建 ConditionalSumConfig.vue
   - 集成到 CalculatedMetricDialog

2. 后端
   - 实现简单比率SQL生成
   - 实现条件求和SQL生成
   - 添加相应的DTO

**验收标准**:
- 支持简单比率计算（A/B）
- 支持条件求和计算
- SQL生成正确

### 7.4 Phase 4: 自定义表达式（2-3天）

**目标**: 支持完全自定义的计算表达式

**任务**:
1. 前端
   - 创建 CustomExpressionConfig.vue
   - 实现表达式编辑器
   - 添加字段占位符支持

2. 后端
   - 实现表达式解析器
   - 实现表达式验证
   - 实现SQL生成

3. 安全
   - SQL注入防护
   - 表达式白名单验证

**验收标准**:
- 用户可以输入自定义表达式
- 支持字段占位符（如{fieldName}）
- 表达式验证正确
- 防止SQL注入

### 7.5 Phase 5: 优化和完善（1-2天）

**任务**:
1. 性能优化
   - SQL查询优化
   - 前端渲染优化
   - 缓存机制

2. 用户体验
   - 添加帮助文档
   - 添加示例模板
   - 优化错误提示

3. 测试
   - 完整的端到端测试
   - 性能测试
   - 兼容性测试

**验收标准**:
- 查询响应时间 < 3秒
- 界面流畅无卡顿
- 错误提示清晰友好

## 8. 测试用例

### 8.1 基础指标聚合测试

**测试场景1: 求和聚合**
```
输入:
- 字段: jkye (借款余额)
- 聚合: SUM
- 别名: 贷款总余额

期望SQL:
SUM(jkye) AS 贷款总余额

期望结果:
正确计算所有记录的余额总和
```

**测试场景2: 平均值聚合**
```
输入:
- 字段: jkye (借款余额)
- 聚合: AVG
- 别名: 平均贷款余额
- 小数位: 2

期望SQL:
AVG(jkye) AS 平均贷款余额

期望结果:
正确计算平均值，保留2位小数
```

### 8.2 条件比率测试

**测试场景1: 不良贷款率**
```
输入:
- 名称: 不良贷款率
- 分子字段: jkye
- 分子聚合: SUM
- 分子条件: wjfl IN ('次级类', '可疑类', '损失类')
- 分母字段: jkye
- 分母聚合: SUM
- 乘数: 100
- 单位: %

期望SQL:
SUM(CASE WHEN wjfl IN ('次级类','可疑类','损失类') THEN jkye ELSE 0 END) 
/ NULLIF(SUM(jkye), 0) * 100 AS 不良贷款率

期望结果:
- 分子 = 不良贷款余额
- 分母 = 贷款总余额
- 结果 = (分子/分母) × 100
- 避免除零错误
```

**测试场景2: 边界情况**
```
测试点:
1. 分母为0的情况 -> 返回NULL
2. 分子为0的情况 -> 返回0
3. 条件值为空列表 -> 提示错误
4. 字段不存在 -> 提示错误
```

### 8.3 SQL注入防护测试

**测试场景: 恶意输入**
```
输入:
- 条件值: "'; DROP TABLE users; --"

期望:
- 输入被转义或拒绝
- 不执行恶意SQL
- 返回安全错误提示
```


## 9. 安全考虑

### 9.1 SQL注入防护

**风险点**:
- 用户输入的字段名
- 用户输入的条件值
- 自定义表达式

**防护措施**:
1. **字段名白名单验证**
   ```java
   public boolean isValidFieldName(String fieldName, Long datasetId) {
       List<String> allowedFields = getDatasetFields(datasetId);
       return allowedFields.contains(fieldName);
   }
   ```

2. **参数化查询**
   ```java
   // 使用PreparedStatement
   String sql = "SELECT * FROM table WHERE field = ?";
   PreparedStatement stmt = conn.prepareStatement(sql);
   stmt.setString(1, userInput);
   ```

3. **值转义**
   ```java
   public String escapeValue(String value) {
       return value.replace("'", "''")
                   .replace("\\", "\\\\");
   }
   ```

4. **表达式白名单**
   ```java
   private static final Set<String> ALLOWED_FUNCTIONS = Set.of(
       "SUM", "AVG", "COUNT", "MAX", "MIN",
       "CASE", "WHEN", "THEN", "ELSE", "END"
   );
   ```

### 9.2 权限控制

**权限检查点**:
1. 数据源访问权限
2. 数据集访问权限
3. 字段访问权限（敏感字段）

**实现**:
```java
@PreAuthorize("@ss.hasPermi('bi:dataset:query')")
public AjaxResult queryData(ChartQueryRequest request) {
    // 检查数据集权限
    if (!hasDatasetPermission(request.getDatasetId())) {
        return AjaxResult.error("无权访问该数据集");
    }
    
    // 检查字段权限
    for (String field : request.getFields()) {
        if (isSensitiveField(field) && !hasSensitiveFieldPermission()) {
            return AjaxResult.error("无权访问敏感字段: " + field);
        }
    }
    
    // 执行查询
    return executeQuery(request);
}
```

### 9.3 性能保护

**限制措施**:
1. **查询超时**
   ```java
   @Value("${bi.query.timeout:30}")
   private int queryTimeout; // 秒
   
   statement.setQueryTimeout(queryTimeout);
   ```

2. **结果集限制**
   ```java
   @Value("${bi.query.maxRows:10000}")
   private int maxRows;
   
   if (resultSet.size() > maxRows) {
       throw new BusinessException("查询结果超过限制");
   }
   ```

3. **并发控制**
   ```java
   @RateLimiter(value = 10, timeout = 1000)
   public QueryResult executeQuery(ChartQueryRequest request) {
       // 限制每秒最多10个查询
   }
   ```

## 10. 配置示例

### 10.1 不良贷款率配置

```json
{
  "datasourceId": 1,
  "datasetId": 10,
  "dimensions": [
    {
      "fieldName": "qydm",
      "comment": "区域代码"
    }
  ],
  "metrics": [],
  "calculatedMetrics": [
    {
      "id": "npl_ratio",
      "name": "不良贷款率",
      "type": "conditionalRatio",
      "config": {
        "numerator": {
          "field": "jkye",
          "aggregation": "sum",
          "condition": {
            "field": "wjfl",
            "operator": "in",
            "values": ["次级类", "可疑类", "损失类"]
          }
        },
        "denominator": {
          "field": "jkye",
          "aggregation": "sum",
          "condition": null
        },
        "result": {
          "multiplier": 100,
          "unit": "%",
          "decimals": 2
        }
      }
    }
  ]
}
```

### 10.2 人均贷款金额配置

```json
{
  "datasourceId": 1,
  "datasetId": 10,
  "dimensions": [
    {
      "fieldName": "dklx",
      "comment": "贷款类型"
    }
  ],
  "metrics": [],
  "calculatedMetrics": [
    {
      "id": "avg_loan_per_person",
      "name": "人均贷款金额",
      "type": "simpleRatio",
      "config": {
        "numerator": {
          "field": "jkje",
          "aggregation": "sum"
        },
        "denominator": {
          "field": "jkbs",
          "aggregation": "count"
        },
        "result": {
          "multiplier": 1,
          "unit": "元",
          "decimals": 2
        }
      }
    }
  ]
}
```

### 10.3 同比增长率配置（预留）

```json
{
  "datasourceId": 1,
  "datasetId": 10,
  "dimensions": [
    {
      "fieldName": "load_date",
      "comment": "统计日期"
    }
  ],
  "metrics": [
    {
      "fieldName": "jkye",
      "aggregation": "sum",
      "alias": "贷款余额"
    }
  ],
  "calculatedMetrics": [
    {
      "id": "yoy_growth",
      "name": "同比增长率",
      "type": "yoy",
      "config": {
        "baseMetric": "jkye",
        "timeDimension": "load_date",
        "compareType": "lastYear",
        "result": {
          "multiplier": 100,
          "unit": "%",
          "decimals": 2
        }
      }
    }
  ]
}
```

## 11. 常见问题 (FAQ)

### Q1: 为什么需要计算指标功能？
A: 在多维汇总表中，很多业务指标（如不良率、增长率）需要通过计算得出。如果每次都要创建新数据集，会导致数据集数量爆炸，难以维护。计算指标功能允许用户在图表配置时灵活定义计算逻辑。

### Q2: 计算指标和数据集中的计算字段有什么区别？
A: 
- **数据集计算字段**: 在数据集层面定义，所有使用该数据集的图表都可以使用
- **计算指标**: 在图表层面定义，只对当前图表生效，更灵活

### Q3: 如何保证计算指标的性能？
A: 
1. 计算在数据库层面完成（SQL聚合），而非前端计算
2. 使用索引优化查询
3. 设置查询超时和结果集限制
4. 对复杂计算建议创建物化视图

### Q4: 支持哪些聚合函数？
A: 当前支持：SUM（求和）、AVG（平均）、COUNT（计数）、MAX（最大值）、MIN（最小值）。后续可扩展支持更多函数。

### Q5: 如何处理除零错误？
A: 使用 NULLIF 函数避免除零：
```sql
分子 / NULLIF(分母, 0)
```
当分母为0时，返回NULL而不是报错。

### Q6: 计算指标可以嵌套吗？
A: 当前版本不支持嵌套（计算指标引用另一个计算指标）。如需复杂嵌套计算，建议使用自定义表达式或在数据集层面定义。

### Q7: 如何调试计算指标？
A: 
1. 使用SQL预览功能查看生成的SQL
2. 使用预览功能查看前10条结果
3. 检查后端日志中的完整SQL语句

## 12. 后续扩展

### 12.1 高级计算类型

1. **同比/环比计算**
   - 需要时间维度支持
   - 使用窗口函数（LAG/LEAD）
   - 自动识别时间粒度

2. **占比计算**
   - 使用窗口函数（OVER PARTITION BY）
   - 支持分组占比和总体占比

3. **移动平均**
   - 支持N天/月移动平均
   - 使用窗口函数（ROWS BETWEEN）

4. **累计计算**
   - 年初至今累计
   - 月初至今累计

### 12.2 可视化增强

1. **计算指标库**
   - 保存常用计算指标模板
   - 跨图表复用
   - 团队共享

2. **智能推荐**
   - 根据字段类型推荐计算方式
   - 根据业务场景推荐指标

3. **表达式编辑器**
   - 语法高亮
   - 自动补全
   - 实时验证

### 12.3 性能优化

1. **查询缓存**
   - 缓存常用查询结果
   - 智能缓存失效

2. **预计算**
   - 定时预计算复杂指标
   - 存储到中间表

3. **查询优化**
   - 自动添加索引提示
   - SQL执行计划分析

## 13. 总结

本设计方案通过增强 DataConfig.vue 组件，为 IRAS BI 平台添加了灵活的计算指标配置能力。主要特点：

1. **保持简单**: 基础功能保持不变，新功能作为增强
2. **灵活强大**: 支持多种计算类型，满足复杂业务需求
3. **安全可靠**: 完善的SQL注入防护和权限控制
4. **易于使用**: 直观的配置界面，实时预览和验证
5. **可扩展**: 预留扩展接口，支持后续功能增强

通过分阶段实施，可以快速交付基础功能，然后逐步完善高级特性。
