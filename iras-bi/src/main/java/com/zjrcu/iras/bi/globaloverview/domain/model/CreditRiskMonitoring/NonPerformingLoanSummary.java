package com.zjrcu.iras.bi.globaloverview.domain.model.CreditRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class NonPerformingLoanSummary extends BaseEntity {
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
    private BigDecimal nonPerformingLoanBalance;
    /**
     * 不良贷款比例
     */
    private BigDecimal nonPerformingLoanRatio;
    /**
     * 按机构汇总当天新增不良金额
     */
    private BigDecimal totalDailyNewNplAmount;
    /**
     * 总当日处置贷款金额
     */
    private BigDecimal totalDailyLoanDisposalAmount;
    /**
     * 总当年处置贷款金额
     */
    private BigDecimal totalYearLoanDisposalAmount;
    /**
     * 总逾期贷款余额
     */
    private BigDecimal totalOverdueLoanBalance;
    /**
     * 总逾期贷款比例
     */
    private BigDecimal totalOverdueLoanRatio;
    /**
     * 总逾期贷款占不良贷款占比
     */
    private BigDecimal totalOverdueNplProportion;

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

    public BigDecimal getTotalDailyNewNplAmount() {
        return totalDailyNewNplAmount;
    }

    public void setTotalDailyNewNplAmount(BigDecimal totalDailyNewNplAmount) {
        this.totalDailyNewNplAmount = totalDailyNewNplAmount;
    }

    public BigDecimal getTotalDailyLoanDisposalAmount() {
        return totalDailyLoanDisposalAmount;
    }

    public void setTotalDailyLoanDisposalAmount(BigDecimal totalDailyLoanDisposalAmount) {
        this.totalDailyLoanDisposalAmount = totalDailyLoanDisposalAmount;
    }

    public BigDecimal getTotalYearLoanDisposalAmount() {
        return totalYearLoanDisposalAmount;
    }

    public void setTotalYearLoanDisposalAmount(BigDecimal totalYearLoanDisposalAmount) {
        this.totalYearLoanDisposalAmount = totalYearLoanDisposalAmount;
    }

    public BigDecimal getTotalOverdueLoanBalance() {
        return totalOverdueLoanBalance;
    }

    public void setTotalOverdueLoanBalance(BigDecimal totalOverdueLoanBalance) {
        this.totalOverdueLoanBalance = totalOverdueLoanBalance;
    }

    public BigDecimal getTotalOverdueLoanRatio() {
        return totalOverdueLoanRatio;
    }

    public void setTotalOverdueLoanRatio(BigDecimal totalOverdueLoanRatio) {
        this.totalOverdueLoanRatio = totalOverdueLoanRatio;
    }

    public BigDecimal getTotalOverdueNplProportion() {
        return totalOverdueNplProportion;
    }

    public void setTotalOverdueNplProportion(BigDecimal totalOverdueNplProportion) {
        this.totalOverdueNplProportion = totalOverdueNplProportion;
    }


}
