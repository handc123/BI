package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import java.math.BigDecimal;

public class NonPerformingLoanDimensionVo {
    /**
     * 维度名称
     */
    private String dimensionName;
    /**
     * 不良贷款余额
     */
    private BigDecimal nonPerformingLoanBalance;
    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public BigDecimal getNonPerformingLoanBalance() {
        return nonPerformingLoanBalance;
    }

    public void setNonPerformingLoanBalance(BigDecimal nonPerformingLoanBalance) {
        this.nonPerformingLoanBalance = nonPerformingLoanBalance;
    }


}
