package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public class OverdueLoanProportionVo {
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dataDate;
    /**
     * 逾期60天不良贷款比例
     */
    private BigDecimal overdue60NplProportion;
    /**
     * 逾期90天不良贷款比例
     */
    private BigDecimal overdue90NplProportion;
    /**
     * 累计逾期不良贷款比例
     */
    private BigDecimal totalOverdueNplProportion;

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getOverdue60NplProportion() {
        return overdue60NplProportion;
    }

    public void setOverdue60NplProportion(BigDecimal overdue60NplProportion) {
        this.overdue60NplProportion = overdue60NplProportion;
    }

    public BigDecimal getOverdue90NplProportion() {
        return overdue90NplProportion;
    }

    public void setOverdue90NplProportion(BigDecimal overdue90NplProportion) {
        this.overdue90NplProportion = overdue90NplProportion;
    }

    public BigDecimal getTotalOverdueNplProportion() {
        return totalOverdueNplProportion;
    }

    public void setTotalOverdueNplProportion(BigDecimal totalOverdueNplProportion) {
        this.totalOverdueNplProportion = totalOverdueNplProportion;
    }





}
