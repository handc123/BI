package com.zjrcu.iras.bi.globaloverview.domain.vo.NonPerformingMonitoring;

import java.math.BigDecimal;

public class NonPerformingLoanTrendVo {
    /**
     * 数据日期
     */
    private String dataDate;
    /**
     * 逾期90天贷款占比
     */
    private BigDecimal overdue90daysLoanRatio;
    /**
     * 关注不良贷款占比
     */
    private BigDecimal concernNonPerformingLoanRatio;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getOverdue90daysLoanRatio() {
        return overdue90daysLoanRatio;
    }

    public void setOverdue90daysLoanRatio(BigDecimal overdue90daysLoanRatio) {
        this.overdue90daysLoanRatio = overdue90daysLoanRatio;
    }

    public BigDecimal getConcernNonPerformingLoanRatio() {
        return concernNonPerformingLoanRatio;
    }

    public void setConcernNonPerformingLoanRatio(BigDecimal concernNonPerformingLoanRatio) {
        this.concernNonPerformingLoanRatio = concernNonPerformingLoanRatio;
    }


}
