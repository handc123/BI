package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class NonPerformingBalanceRatioVo {

    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 不良贷款余额
     */
    private BigDecimal nonPerformingLoanBalance;
    /**
     * 不良贷款率
     */
    private BigDecimal nonPerformingLoanRatio;


    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
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


}
