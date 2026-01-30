package com.zjrcu.iras.bi.globaloverview.domain.model.KeyRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class NonPerformingLoanTransaction extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识符
     */
    private Long id;
    /**
     * 行社名称
     */
    private String orgName;
    /**
     * 地区名称
     */
    private String regionName;
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dataDate;
    /**
     * 当日新增不良金额
     */
    private BigDecimal newNonPerformingLoanAmount;
    /**
     * 当日不良处置贷款总额
     */
    private BigDecimal disposedNonPerformingLoanAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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

    public BigDecimal getNewNonPerformingLoanAmount() {
        return newNonPerformingLoanAmount;
    }

    public void setNewNonPerformingLoanAmount(BigDecimal newNonPerformingLoanAmount) {
        this.newNonPerformingLoanAmount = newNonPerformingLoanAmount;
    }

    public BigDecimal getDisposedNonPerformingLoanAmount() {
        return disposedNonPerformingLoanAmount;
    }

    public void setDisposedNonPerformingLoanAmount(BigDecimal disposedNonPerformingLoanAmount) {
        this.disposedNonPerformingLoanAmount = disposedNonPerformingLoanAmount;
    }


}
