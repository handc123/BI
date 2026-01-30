package com.zjrcu.iras.bi.globaloverview.domain.model.NonPerformingMonitoring;

import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class NonPerformingLoan extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /**
     * 唯一标识符
     */
    private Long id;
    /**
     * 地区名称
     */
    private String regionName;
    /**
     * 数据日期
     */
    private Date dataDate;
    /**
     * 不良贷款率
     */
    private BigDecimal nonPerformingLoanRatio;
    /**
     * 关注不良贷款率
     */
    private BigDecimal concernNonPerformingLoanRatio;
    /**
     * 逾期90天以上贷款占比
     */
    private BigDecimal overdue90daysLoanRatio;

    public BigDecimal getNonPerformingLoanRatio() {
        return nonPerformingLoanRatio;
    }

    public void setNonPerformingLoanRatio(BigDecimal nonPerformingLoanRatio) {
        this.nonPerformingLoanRatio = nonPerformingLoanRatio;
    }

    public BigDecimal getConcernNonPerformingLoanRatio() {
        return concernNonPerformingLoanRatio;
    }

    public void setConcernNonPerformingLoanRatio(BigDecimal concernNonPerformingLoanRatio) {
        this.concernNonPerformingLoanRatio = concernNonPerformingLoanRatio;
    }

    public BigDecimal getOverdue90daysLoanRatio() {
        return overdue90daysLoanRatio;
    }

    public void setOverdue90daysLoanRatio(BigDecimal overdue90daysLoanRatio) {
        this.overdue90daysLoanRatio = overdue90daysLoanRatio;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }




    
}
