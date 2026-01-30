package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;


import java.math.BigDecimal;

public class OverdueLoanSummaryVo {
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 逾期贷款余额
     */
    private BigDecimal totalOverdueBalance;
    /**
     * 逾期贷款比例
     */
    private BigDecimal totalOverdueRatio;


    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getTotalOverdueBalance() {
        return totalOverdueBalance;
    }

    public void setTotalOverdueBalance(BigDecimal totalOverdueBalance) {
        this.totalOverdueBalance = totalOverdueBalance;
    }

    public BigDecimal getTotalOverdueRatio() {
        return totalOverdueRatio;
    }

    public void setTotalOverdueRatio(BigDecimal totalOverdueRatio) {
        this.totalOverdueRatio = totalOverdueRatio;
    }


}
