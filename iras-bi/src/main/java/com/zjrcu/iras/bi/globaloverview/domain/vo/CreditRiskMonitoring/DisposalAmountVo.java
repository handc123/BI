package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class DisposalAmountVo {
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 当日贷款处置金额
     */
    private BigDecimal totalDailyLoanDisposalAmount;
    /**
     * 当年贷款处置金额
     */
    private BigDecimal totalYearLoanDisposalAmount;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
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


}
