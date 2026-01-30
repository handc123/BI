package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import java.math.BigDecimal;

public class CharacteristicLoanVo {
    /**
     * 数据日期
     */
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
