package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 穿透规则组
 */
public class DrillRuleGroupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 与前一组关系: AND / OR
     */
    private String relationWithPrev;

    private List<DrillConditionDTO> rules;

    public String getRelationWithPrev() {
        return relationWithPrev;
    }

    public void setRelationWithPrev(String relationWithPrev) {
        this.relationWithPrev = relationWithPrev;
    }

    public List<DrillConditionDTO> getRules() {
        return rules;
    }

    public void setRules(List<DrillConditionDTO> rules) {
        this.rules = rules;
    }
}

