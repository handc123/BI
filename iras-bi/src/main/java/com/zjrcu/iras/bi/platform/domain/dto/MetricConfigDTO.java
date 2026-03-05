package com.zjrcu.iras.bi.platform.domain.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 指标配置DTO
 * 用于配置数据集的基础指标和计算指标
 * 
 * @author IRAS
 */
public class MetricConfigDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 基础指标列表 */
    @Valid
    private List<BaseMetric> baseMetrics = new ArrayList<>();
    
    /** 计算指标列表 */
    @Valid
    private List<ComputedMetric> computedMetrics = new ArrayList<>();
    
    public List<BaseMetric> getBaseMetrics() {
        return baseMetrics;
    }
    
    public void setBaseMetrics(List<BaseMetric> baseMetrics) {
        this.baseMetrics = baseMetrics;
    }
    
    public List<ComputedMetric> getComputedMetrics() {
        return computedMetrics;
    }
    
    public void setComputedMetrics(List<ComputedMetric> computedMetrics) {
        this.computedMetrics = computedMetrics;
    }
    
    /**
     * 基础指标
     * 使用标准SQL聚合函数的基本字段
     */
    public static class BaseMetric implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 指标名称(唯一标识) */
        @NotBlank(message = "指标名称不能为空")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "指标名称只能包含字母、数字和下划线,且必须以字母或下划线开头")
        @Size(max = 100, message = "指标名称长度不能超过100个字符")
        private String name;
        
        /** 指标别名(显示名称) */
        @NotBlank(message = "指标别名不能为空")
        @Size(max = 200, message = "指标别名长度不能超过200个字符")
        private String alias;
        
        /** 字段名 */
        @NotBlank(message = "字段名不能为空")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "字段名只能包含字母、数字和下划线,且必须以字母或下划线开头")
        @Size(max = 100, message = "字段名长度不能超过100个字符")
        private String field;
        
        /** 聚合函数: SUM, AVG, COUNT, MAX, MIN */
        @NotBlank(message = "聚合函数不能为空")
        @Pattern(regexp = "^(SUM|AVG|COUNT|MAX|MIN)$", message = "聚合函数必须是SUM、AVG、COUNT、MAX或MIN之一")
        private String aggregation;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getAlias() {
            return alias;
        }
        
        public void setAlias(String alias) {
            this.alias = alias;
        }
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public String getAggregation() {
            return aggregation;
        }
        
        public void setAggregation(String aggregation) {
            this.aggregation = aggregation;
        }
    }
    
    /**
     * 计算指标抽象基类
     * 使用Jackson多态序列化支持不同类型的计算指标
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "computeType"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ConditionalRatioMetric.class, name = "conditional_ratio"),
        @JsonSubTypes.Type(value = SimpleRatioMetric.class, name = "simple_ratio"),
        @JsonSubTypes.Type(value = ConditionalSumMetric.class, name = "conditional_sum"),
        @JsonSubTypes.Type(value = CustomExpressionMetric.class, name = "custom_expression")
    })
    public static abstract class ComputedMetric implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 指标名称(唯一标识) */
        @NotBlank(message = "指标名称不能为空")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "指标名称只能包含字母、数字和下划线,且必须以字母或下划线开头")
        @Size(max = 100, message = "指标名称长度不能超过100个字符")
        private String name;
        
        /** 指标别名(显示名称) */
        @NotBlank(message = "指标别名不能为空")
        @Size(max = 200, message = "指标别名长度不能超过200个字符")
        private String alias;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getAlias() {
            return alias;
        }
        
        public void setAlias(String alias) {
            this.alias = alias;
        }
    }
    
    /**
     * 条件比率指标
     * 例如: 不良贷款率 = SUM(CASE WHEN status='NPL' THEN amount ELSE 0 END) / NULLIF(SUM(CASE WHEN status IN ('NORMAL','NPL') THEN amount ELSE 0 END), 0)
     */
    public static class ConditionalRatioMetric extends ComputedMetric {
        
        private static final long serialVersionUID = 1L;
        
        /** 基础字段名 */
        @NotBlank(message = "字段名不能为空")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "字段名只能包含字母、数字和下划线,且必须以字母或下划线开头")
        @Size(max = 100, message = "字段名长度不能超过100个字符")
        private String field;
        
        /** 分子条件表达式 */
        @NotBlank(message = "分子条件不能为空")
        @Size(max = 500, message = "分子条件长度不能超过500个字符")
        private String numeratorCondition;
        
        /** 分母条件表达式 */
        @NotBlank(message = "分母条件不能为空")
        @Size(max = 500, message = "分母条件长度不能超过500个字符")
        private String denominatorCondition;
        
        /** 是否显示为百分比 */
        private Boolean asPercentage = false;
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public String getNumeratorCondition() {
            return numeratorCondition;
        }
        
        public void setNumeratorCondition(String numeratorCondition) {
            this.numeratorCondition = numeratorCondition;
        }
        
        public String getDenominatorCondition() {
            return denominatorCondition;
        }
        
        public void setDenominatorCondition(String denominatorCondition) {
            this.denominatorCondition = denominatorCondition;
        }
        
        public Boolean getAsPercentage() {
            return asPercentage;
        }
        
        public void setAsPercentage(Boolean asPercentage) {
            this.asPercentage = asPercentage;
        }
    }
    
    /**
     * 简单比率指标
     * 例如: 资本充足率 = 资本总额 / 风险加权资产
     */
    public static class SimpleRatioMetric extends ComputedMetric {
        
        private static final long serialVersionUID = 1L;
        
        /** 分子指标名称(引用已配置的指标) */
        @NotBlank(message = "分子指标不能为空")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "分子指标名称只能包含字母、数字和下划线,且必须以字母或下划线开头")
        @Size(max = 100, message = "分子指标名称长度不能超过100个字符")
        private String numeratorMetric;
        
        /** 分母指标名称(引用已配置的指标) */
        @NotBlank(message = "分母指标不能为空")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "分母指标名称只能包含字母、数字和下划线,且必须以字母或下划线开头")
        @Size(max = 100, message = "分母指标名称长度不能超过100个字符")
        private String denominatorMetric;
        
        /** 是否显示为百分比 */
        private Boolean asPercentage = false;
        
        public String getNumeratorMetric() {
            return numeratorMetric;
        }
        
        public void setNumeratorMetric(String numeratorMetric) {
            this.numeratorMetric = numeratorMetric;
        }
        
        public String getDenominatorMetric() {
            return denominatorMetric;
        }
        
        public void setDenominatorMetric(String denominatorMetric) {
            this.denominatorMetric = denominatorMetric;
        }
        
        public Boolean getAsPercentage() {
            return asPercentage;
        }
        
        public void setAsPercentage(Boolean asPercentage) {
            this.asPercentage = asPercentage;
        }
    }
    
    /**
     * 条件求和指标
     * 例如: 不良贷款总额 = SUM(CASE WHEN status='NPL' THEN amount ELSE 0 END)
     */
    public static class ConditionalSumMetric extends ComputedMetric {
        
        private static final long serialVersionUID = 1L;
        
        /** 基础字段名 */
        @NotBlank(message = "字段名不能为空")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "字段名只能包含字母、数字和下划线,且必须以字母或下划线开头")
        @Size(max = 100, message = "字段名长度不能超过100个字符")
        private String field;
        
        /** 条件表达式 */
        @NotBlank(message = "条件不能为空")
        @Size(max = 500, message = "条件长度不能超过500个字符")
        private String condition;
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public String getCondition() {
            return condition;
        }
        
        public void setCondition(String condition) {
            this.condition = condition;
        }
    }
    
    /**
     * 自定义表达式指标
     * 例如: 综合风险指数 = (不良贷款率 * 0.4 + 逾期率 * 0.3 + 关注类贷款率 * 0.3) * 100
     */
    public static class CustomExpressionMetric extends ComputedMetric {
        
        private static final long serialVersionUID = 1L;
        
        /** 自定义数学表达式,可引用其他指标 */
        @NotBlank(message = "表达式不能为空")
        @Size(max = 500, message = "表达式长度不能超过500个字符")
        private String expression;
        
        public String getExpression() {
            return expression;
        }
        
        public void setExpression(String expression) {
            this.expression = expression;
        }
    }
}
