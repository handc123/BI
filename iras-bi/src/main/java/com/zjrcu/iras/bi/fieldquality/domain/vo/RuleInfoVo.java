package com.zjrcu.iras.bi.fieldquality.domain.vo;

import com.zjrcu.iras.common.annotation.Excel;

import java.util.List;

public class RuleInfoVo {

    /**
     * 规则编号（业务唯一标识，如：RULE-2025-001）
     */
    @Excel(name = "规则编号", readConverterExp = "业=务唯一标识，如：RULE-2025-001")
    private String ruleCode;

    /**
     * 规则说明（详细描述校验逻辑、适用场景等）
     */
    @Excel(name = "规则说明", readConverterExp = "详=细描述校验逻辑、适用场景等")
    private String ruleDesc;

    /**
     * 校验的目标表名（如：user_info、account_detail）
     */
    @Excel(name = "校验的目标表名", readConverterExp = "如=：user_info、account_detail")
    private String checkTable;

    /**
     * 校验强度
     */
    @Excel(name = "校验强度")
    private String checkStrength;

    /**
     * 校验大类（如：完整性校验、准确性校验、一致性校验）
     */
    @Excel(name = "校验大类", readConverterExp = "如=：完整性校验、准确性校验、一致性校验")
    private String ruleTypeBig;

    /**
     * 校验小类（如：非空校验、格式校验、范围校验）
     */
    @Excel(name = "校验小类", readConverterExp = "如=：非空校验、格式校验、范围校验")
    private String ruleTypeSmall;
    /**
     * 近六个月的通过率
     */
    @Excel(name = "近六个月的通过率")
    private List<MonthPassRateVo> monthPassRates;
    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public String getCheckTable() {
        return checkTable;
    }

    public void setCheckTable(String checkTable) {
        this.checkTable = checkTable;
    }

    public String getCheckStrength() {
        return checkStrength;
    }

    public void setCheckStrength(String checkStrength) {
        this.checkStrength = checkStrength;
    }

    public String getRuleTypeBig() {
        return ruleTypeBig;
    }

    public void setRuleTypeBig(String ruleTypeBig) {
        this.ruleTypeBig = ruleTypeBig;
    }

    public String getRuleTypeSmall() {
        return ruleTypeSmall;
    }

    public void setRuleTypeSmall(String ruleTypeSmall) {
        this.ruleTypeSmall = ruleTypeSmall;
    }

    public List<MonthPassRateVo> getPassRate() {
        return monthPassRates;
    }

    public void setPassRate(List<MonthPassRateVo> passRate) {
        this.monthPassRates = passRate;
    }


}
