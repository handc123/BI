package com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class RealEstateFinanceLoanVo {
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 房地产贷款余额
     */
    private BigDecimal realEstateLoanBalance;
    /**
     * 房地产贷款比例
     */
    private BigDecimal realEstateLoanRatio;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getRealEstateLoanBalance() {
        return realEstateLoanBalance;
    }

    public void setRealEstateLoanBalance(BigDecimal realEstateLoanBalance) {
        this.realEstateLoanBalance = realEstateLoanBalance;
    }

    public BigDecimal getRealEstateLoanRatio() {
        return realEstateLoanRatio;
    }

    public void setRealEstateLoanRatio(BigDecimal realEstateLoanRatio) {
        this.realEstateLoanRatio = realEstateLoanRatio;
    }


}
