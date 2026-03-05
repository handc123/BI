package com.zjrcu.iras.bi.platform.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 计算字段DTO
 * 用于图表级别的临时计算字段配置
 * 
 * @author IRAS
 */
public class CalculatedFieldDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 字段名称(英文标识符) */
    @NotBlank(message = "字段名称不能为空")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "字段名称只能包含字母、数字和下划线,且必须以字母或下划线开头")
    @Size(max = 100, message = "字段名称长度不能超过100个字符")
    private String name;
    
    /** 字段别名(中文显示名称) */
    @NotBlank(message = "字段别名不能为空")
    @Size(max = 200, message = "字段别名长度不能超过200个字符")
    private String alias;
    
    /** 字段类型: dimension(维度) 或 metric(指标) */
    @NotBlank(message = "字段类型不能为空")
    @Pattern(regexp = "^(dimension|metric)$", message = "字段类型必须是dimension或metric")
    private String fieldType;
    
    /** 表达式(使用数据集字段名、运算符和函数) */
    @NotBlank(message = "表达式不能为空")
    @Size(max = 1000, message = "表达式长度不能超过1000个字符")
    private String expression;
    
    /** 聚合方式: AUTO, SUM, AVG, MAX, MIN, COUNT (仅指标类型使用) */
    @NotBlank(message = "聚合方式不能为空")
    @Pattern(regexp = "^(AUTO|SUM|AVG|MAX|MIN|COUNT)$", message = "聚合方式必须是AUTO、SUM、AVG、MAX、MIN或COUNT之一")
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
    
    public String getFieldType() {
        return fieldType;
    }
    
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public String getAggregation() {
        return aggregation;
    }
    
    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }
}
