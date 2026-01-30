package com.zjrcu.iras.bi.fieldquality.domain.vo;

public class RuleTypeCountVo {
    /**
     * 规则类型大类
     */
    private String ruleTypeBig;
    /**
     * 规则数量
     */
    private Integer ruleCount;
    public String getRuleTypeBig() {
        return ruleTypeBig;
    }

    public void setRuleTypeBig(String ruleTypeBig) {
        this.ruleTypeBig = ruleTypeBig;
    }

    public Integer getRuleCount() {
        return ruleCount;
    }

    public void setRuleCount(Integer ruleCount) {
        this.ruleCount = ruleCount;
    }


}
