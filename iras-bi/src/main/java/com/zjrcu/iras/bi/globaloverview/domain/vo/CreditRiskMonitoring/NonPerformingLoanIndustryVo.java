package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import java.math.BigDecimal;

public class NonPerformingLoanIndustryVo {
    /**
     * 行业名称
     */
    private String industryName;
    /**
     * 不良贷款余额
     */
    private BigDecimal nonPerformingLoanBalance;
    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }

    public BigDecimal getNonPerformingLoanBalance() {
        return nonPerformingLoanBalance;
    }

    public void setNonPerformingLoanBalance(BigDecimal nonPerformingLoanBalance) {
        this.nonPerformingLoanBalance = nonPerformingLoanBalance;
    }


}
