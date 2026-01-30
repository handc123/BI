package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import java.math.BigDecimal;

public class DailyNewNplAmountVo {
    /**
     * 数据日期
     */
    private String dataDate;
    /**
     * 当日新增不良贷款金额
     */
    private BigDecimal dailyNewNplAmount;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getDailyNewNplAmount() {
        return dailyNewNplAmount;
    }

    public void setDailyNewNplAmount(BigDecimal dailyNewNplAmount) {
        this.dailyNewNplAmount = dailyNewNplAmount;
    }



}
