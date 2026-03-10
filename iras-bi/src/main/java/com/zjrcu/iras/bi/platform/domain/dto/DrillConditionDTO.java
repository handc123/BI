package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 穿透查询条件
 */
public class DrillConditionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String field;
    private String operator;
    private Object value;
    private List<Object> values;

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

