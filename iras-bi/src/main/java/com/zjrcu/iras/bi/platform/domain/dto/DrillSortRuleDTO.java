package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 穿透排序规则
 */
public class DrillSortRuleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String field;
    private String order;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}

