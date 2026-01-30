package com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class PersonRealEstateLoanVo {
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 个人住房贷款余额
     */
    private BigDecimal personRealEstateLoanBalance;
    /**
     * 个人住房贷款占比
     */
    private BigDecimal personRealEstateLoanRatio;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getPersonRealEstateLoanBalance() {
        return personRealEstateLoanBalance;
    }

    public void setPersonRealEstateLoanBalance(BigDecimal personRealEstateLoanBalance) {
        this.personRealEstateLoanBalance = personRealEstateLoanBalance;
    }

    public BigDecimal getPersonRealEstateLoanRatio() {
        return personRealEstateLoanRatio;
    }

    public void setPersonRealEstateLoanRatio(BigDecimal personRealEstateLoanRatio) {
        this.personRealEstateLoanRatio = personRealEstateLoanRatio;
    }


}
