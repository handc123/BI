package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import java.math.BigDecimal;

public class NonPerformingLoanGuaranteeVo {
    /**
     * 担保方式
     */
    private String guaranteeType;
    /**
     * 不良贷款余额
     */
    private BigDecimal nonPerformingLoanBalance;
    public String getGuaranteeType() {
        return guaranteeType;
    }

    public void setGuaranteeType(String guaranteeType) {
        this.guaranteeType = guaranteeType;
    }

    public BigDecimal getNonPerformingLoanBalance() {
        return nonPerformingLoanBalance;
    }

    public void setNonPerformingLoanBalance(BigDecimal nonPerformingLoanBalance) {
        this.nonPerformingLoanBalance = nonPerformingLoanBalance;
    }


}
