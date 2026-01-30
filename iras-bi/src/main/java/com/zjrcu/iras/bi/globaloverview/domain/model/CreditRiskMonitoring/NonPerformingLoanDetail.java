package com.zjrcu.iras.bi.globaloverview.domain.model.CreditRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class NonPerformingLoanDetail extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识符
     */
    private Long id;
    /**
     * 行社名称
     */
    private String orgName;
    /**
     * 地区名称
     */
    private String regionName;
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dataDate;
    /**
     * 不良贷款余额
     */
    private BigDecimal  nonPerformingLoanBalance;
    /**
     * 不良贷款率
     */
    private BigDecimal nonPerformingLoanRatio;
    /**
     * 当天新增不良金额
     */
    private BigDecimal dailyNewNplAmount;
    /**
     * 当日贷款处置金额
     */
    private BigDecimal dailyLoanDisposalAmount;
    /**
     * 当年贷款处置金额
     */
    private BigDecimal yearLoanDisposalAmount;
    /**
     * 处置类别
     */
    private String disposalType;
    /**
     * 行业名称
     */
    private String industryName;
    /**
     * 担保方式
     */
    private String guaranteeType;
    /**
     * 客户类别
     */
    private String customerType;

    /**
     * 逾期标识
     */
    private Boolean overdue;
    /**
     * 逾期期限
     */
    private Integer overduePeriod;
    /**
     * 逾期贷款占不良贷款比例
     */
    private BigDecimal overdueNplProportion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getNonPerformingLoanBalance() {
        return nonPerformingLoanBalance;
    }

    public void setNonPerformingLoanBalance(BigDecimal nonPerformingLoanBalance) {
        this.nonPerformingLoanBalance = nonPerformingLoanBalance;
    }

    public BigDecimal getNonPerformingLoanRatio() {
        return nonPerformingLoanRatio;
    }

    public void setNonPerformingLoanRatio(BigDecimal nonPerformingLoanRatio) {
        this.nonPerformingLoanRatio = nonPerformingLoanRatio;
    }

    public BigDecimal getDailyNewNplAmount() {
        return dailyNewNplAmount;
    }

    public void setDailyNewNplAmount(BigDecimal dailyNewNplAmount) {
        this.dailyNewNplAmount = dailyNewNplAmount;
    }

    public BigDecimal getDailyLoanDisposalAmount() {
        return dailyLoanDisposalAmount;
    }

    public void setDailyLoanDisposalAmount(BigDecimal dailyLoanDisposalAmount) {
        this.dailyLoanDisposalAmount = dailyLoanDisposalAmount;
    }

    public BigDecimal getYearLoanDisposalAmount() {
        return yearLoanDisposalAmount;
    }

    public void setYearLoanDisposalAmount(BigDecimal yearLoanDisposalAmount) {
        this.yearLoanDisposalAmount = yearLoanDisposalAmount;
    }

    public String getDisposalType() {
        return disposalType;
    }

    public void setDisposalType(String disposalType) {
        this.disposalType = disposalType;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }

    public String getGuaranteeType() {
        return guaranteeType;
    }

    public void setGuaranteeType(String guaranteeType) {
        this.guaranteeType = guaranteeType;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }


    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public Integer getOverduePeriod() {
        return overduePeriod;
    }

    public void setOverduePeriod(Integer overduePeriod) {
        this.overduePeriod = overduePeriod;
    }

    public BigDecimal getOverdueNplProportion() {
        return overdueNplProportion;
    }

    public void setOverdueNplProportion(BigDecimal overdueNplProportion) {
        this.overdueNplProportion = overdueNplProportion;
    }



}
