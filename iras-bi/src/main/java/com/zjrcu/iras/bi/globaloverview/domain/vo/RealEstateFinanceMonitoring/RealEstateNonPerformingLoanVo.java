package com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class RealEstateNonPerformingLoanVo {

    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 房地产不良贷款金额
     */
    private BigDecimal realEstateNonPerformingLoanBalance;
    /**
     * 房地产不良贷款占比
     */
    private BigDecimal realEstateNonPerformingLoanRatio;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getRealEstateNonPerformingLoanBalance() {
        return realEstateNonPerformingLoanBalance;
    }

    public void setRealEstateNonPerformingLoanBalance(BigDecimal realEstateNonPerformingLoanBalance) {
        this.realEstateNonPerformingLoanBalance = realEstateNonPerformingLoanBalance;
    }

    public BigDecimal getRealEstateNonPerformingLoanRatio() {
        return realEstateNonPerformingLoanRatio;
    }

    public void setRealEstateNonPerformingLoanRatio(BigDecimal realEstateNonPerformingLoanRatio) {
        this.realEstateNonPerformingLoanRatio = realEstateNonPerformingLoanRatio;
    }


}
