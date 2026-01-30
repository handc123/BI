package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 度量字段DTO
 * 用于聚合查询
 *
 * @author iras
 */
public class Metric implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称
     */
    private String field;

    /**
     * 聚合函数: SUM, AVG, COUNT, MAX, MIN
     */
    private String aggregation;

    /**
     * 别名(显示名称)
     */
    private String alias;

    public Metric() {
    }

    public Metric(String field, String aggregation, String alias) {
        this.field = field;
        this.aggregation = aggregation;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
