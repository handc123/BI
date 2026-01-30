package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class OverdueLoanVo {
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 逾期贷款余额
     */
    private BigDecimal overdueLoanBalance;
    /**
     * 逾期贷款占比
     */
    private BigDecimal overdueLoanProportion;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getOverdueLoanBalance() {
        return overdueLoanBalance;
    }

    public void setOverdueLoanBalance(BigDecimal overdueLoanBalance) {
        this.overdueLoanBalance = overdueLoanBalance;
    }

    public BigDecimal getOverdueLoanProportion() {
        return overdueLoanProportion;
    }

    public void setOverdueLoanProportion(BigDecimal overdueLoanProportion) {
        this.overdueLoanProportion = overdueLoanProportion;
    }


}
