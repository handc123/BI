package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import java.math.BigDecimal;

public class DisposalAmountTypeVo {
    /**
     * 数据日期
     */
    private String dataDate;
    /**
     * 处置类别
     */
    private String disposalType;
    /**
     * 日处置金额
     */
    private BigDecimal dailyLoanDisposalAmount;
    /**
     * 年度处置金额
     */
    private BigDecimal yearLoanDisposalAmount;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public String getDisposalType() {
        return disposalType;
    }

    public void setDisposalType(String disposalType) {
        this.disposalType = disposalType;
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


}
