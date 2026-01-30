package com.zjrcu.iras.bi.fieldquality.domain.model;

import java.util.Date;

import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 校验规则（支持拉链多版本管理，记录规则的创建、更新及历史版本）对象 rule_info
 *
 * @author ruoyi
 * @date 2025-08-01
 */
public class RuleInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（与rule_id配合，支持拉链表多版本）
     */
    private Long id;

    /**
     * 规则自增ID（系统内部唯一标识，同一规则的不同版本共享此ID）
     */
    @Excel(name = "规则自增ID", readConverterExp = "系=统内部唯一标识")
    private Long ruleId;

    /**
     * 规则编号（业务唯一标识，如：RULE-2025-001）
     */
    @Excel(name = "规则编号", readConverterExp = "业=务唯一标识，如：RULE-2025-001")
    private String ruleCode;

    /**
     * 规则名称（如：客户姓名非空校验）
     */
    @Excel(name = "规则名称", readConverterExp = "如=：客户姓名非空校验")
    private String ruleName;

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
     * 是否启用（1=启用，0=停用）
     */
    @Excel(name = "是否启用", readConverterExp = "1==启用，0=停用")
    private Integer isActive;

    /**
     * 生效时间（拉链表字段，版本开始时间）
     */
    @Excel(name = "生效时间", readConverterExp = "拉=链表字段，版本开始时间")
    private Date startTime;

    /**
     * 失效时间（拉链表字段，版本结束时间，默认永久有效）
     */
    @Excel(name = "失效时间", readConverterExp = "拉=链表字段，版本结束时间，默认永久有效")
    private Date endTime;

    /**
     * 是否是当前版本（1=是，0=否，用于快速查询最新规则）
     */
    @Excel(name = "是否是当前版本", readConverterExp = "1==是，0=否，用于快速查询最新规则")
    private Integer isLatest;



    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setCheckTable(String checkTable) {
        this.checkTable = checkTable;
    }

    public String getCheckTable() {
        return checkTable;
    }

    public void setCheckStrength(String checkStrength) {
        this.checkStrength = checkStrength;
    }

    public String getCheckStrength() {
        return checkStrength;
    }

    public void setRuleTypeBig(String ruleTypeBig) {
        this.ruleTypeBig = ruleTypeBig;
    }

    public String getRuleTypeBig() {
        return ruleTypeBig;
    }

    public void setRuleTypeSmall(String ruleTypeSmall) {
        this.ruleTypeSmall = ruleTypeSmall;
    }

    public String getRuleTypeSmall() {
        return ruleTypeSmall;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setIsLatest(Integer isLatest) {
        this.isLatest = isLatest;
    }

    public Integer getIsLatest() {
        return isLatest;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("ruleId", getRuleId())
                .append("ruleCode", getRuleCode())
                .append("ruleName", getRuleName())
                .append("ruleDesc", getRuleDesc())
                .append("checkTable", getCheckTable())
                .append("checkStrength", getCheckStrength())
                .append("ruleTypeBig", getRuleTypeBig())
                .append("ruleTypeSmall", getRuleTypeSmall())
                .append("isActive", getIsActive())
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("isLatest", getIsLatest())
                .toString();
    }
}
