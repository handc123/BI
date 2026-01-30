package com.zjrcu.iras.bi.fieldquality.domain.vo;

import java.math.BigDecimal;

public class MonthPassRateVo {
    /**
     * 月份
     */
    private String month;
    /**
     * 通过率
     */
    private BigDecimal passRate;
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getPassRate() {
        return passRate;
    }

    public void setPassRate(BigDecimal passRate) {
        this.passRate = passRate;
    }


}
