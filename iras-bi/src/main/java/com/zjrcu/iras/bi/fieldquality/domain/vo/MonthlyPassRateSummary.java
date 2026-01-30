package com.zjrcu.iras.bi.fieldquality.domain.vo;

import com.zjrcu.iras.bi.fieldquality.domain.model.RulePassRateDistribution;

import java.util.List;

public class MonthlyPassRateSummary {
    /**
     * 月份
     */
    private String month;
    /**
     * 规则通过率分布
     */
    private List<RulePassRateDistribution> rulePassRateDistribution;
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public List<RulePassRateDistribution> getRulePassRateDistribution() {
        return rulePassRateDistribution;
    }

    public void setRulePassRateDistribution(List<RulePassRateDistribution> rulePassRateDistribution) {
        this.rulePassRateDistribution = rulePassRateDistribution;
    }


}
