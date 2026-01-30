package com.zjrcu.iras.bi.fieldquality.domain.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 行社规则提升率排名（用于本月基础数据提升率排名及数据质量提升排名）对象 org_rule_improve_rate_rank
 *
 * @author ruoyi
 * @date 2025-07-31
 */
public class OrgRuleImproveRateRank extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 数据日期（T-1，即统计前一天的数据）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "数据日期", dateFormat = "yyyy-MM-dd")
    private Date dataDate;

    /**
     * 比较周期（如10天、30天，代表当前周期与过去N天的对比）
     */
    @Excel(name = "比较周期")
    private Long compareDays;

    /**
     * 行社编码（机构唯一标识）
     */
    @Excel(name = "行社编码")
    private String orgId;

    /**
     * 行社名称
     */
    @Excel(name = "行社名称")
    private String orgName;

    /**
     * 规则ID（关联具体校验规则）
     */
    @Excel(name = "规则ID")
    private Long ruleId;

    /**
     * 规则编号（规则的唯一编码）
     */
    @Excel(name = "规则编号")
    private String ruleCode;

    /**
     * 当前周期通过率（百分比，保留2位小数，如98.50表示98.50%）
     */
    @Excel(name = "当前周期通过率")
    private BigDecimal passRateNow;

    /**
     * 上个周期通过率（同格式，与当前周期对比的基准值）
     */
    @Excel(name = "上个周期通过率")
    private BigDecimal passRateLastMonth;

    /**
     * 提升率（(当前通过率-上个周期通过率)/上个周期通过率*100%，保留2位小数；可为空，如首次统计无基准时）
     */
    @Excel(name = "提升率")
    private BigDecimal improveRate;



    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setCompareDays(Long compareDays) {
        this.compareDays = compareDays;
    }

    public Long getCompareDays() {
        return compareDays;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgName() {
        return orgName;
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

    public void setPassRateNow(BigDecimal passRateNow) {
        this.passRateNow = passRateNow;
    }

    public BigDecimal getPassRateNow() {
        return passRateNow;
    }

    public void setPassRateLastMonth(BigDecimal passRateLastMonth) {
        this.passRateLastMonth = passRateLastMonth;
    }

    public BigDecimal getPassRateLastMonth() {
        return passRateLastMonth;
    }

    public void setImproveRate(BigDecimal improveRate) {
        this.improveRate = improveRate;
    }

    public BigDecimal getImproveRate() {
        return improveRate;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("dataDate", getDataDate())
                .append("compareDays", getCompareDays())
                .append("orgId", getOrgId())
                .append("orgName", getOrgName())
                .append("ruleId", getRuleId())
                .append("ruleCode", getRuleCode())
                .append("passRateNow", getPassRateNow())
                .append("passRateLastMonth", getPassRateLastMonth())
                .append("improveRate", getImproveRate())
                .toString();
    }
}
