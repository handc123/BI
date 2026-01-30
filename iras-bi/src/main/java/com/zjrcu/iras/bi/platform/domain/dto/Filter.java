package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 查询筛选条件DTO
 *
 * @author iras
 */
public class Filter implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称
     */
    private String field;

    /**
     * 操作符: eq(等于), ne(不等于), gt(大于), gte(大于等于), lt(小于), lte(小于等于), 
     *        like(模糊匹配), in(包含), between(区间)
     */
    private String operator;

    /**
     * 筛选值
     */
    private Object value;

    /**
     * 筛选值列表(用于in操作符)
     */
    private List<Object> values;

    public Filter() {
    }

    public Filter(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }
}
