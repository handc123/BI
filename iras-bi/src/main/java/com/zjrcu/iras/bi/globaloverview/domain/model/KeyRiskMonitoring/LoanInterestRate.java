package com.zjrcu.iras.bi.globaloverview.domain.model.KeyRiskMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class LoanInterestRate extends BaseEntity {
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
     * 对公贷款当天发放利率
     */
    private BigDecimal corporateLoanRate;
    /**
     * 普惠小微当天发放利率
     */
    private BigDecimal smeLoanRate;
    /**
     * 房地产当天发放利率
     */
    private BigDecimal realEstateLoanRate;

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

    public BigDecimal getCorporateLoanRate() {
        return corporateLoanRate;
    }

    public void setCorporateLoanRate(BigDecimal corporateLoanRate) {
        this.corporateLoanRate = corporateLoanRate;
    }

    public BigDecimal getSmeLoanRate() {
        return smeLoanRate;
    }

    public void setSmeLoanRate(BigDecimal smeLoanRate) {
        this.smeLoanRate = smeLoanRate;
    }

    public BigDecimal getRealEstateLoanRate() {
        return realEstateLoanRate;
    }

    public void setRealEstateLoanRate(BigDecimal realEstateLoanRate) {
        this.realEstateLoanRate = realEstateLoanRate;
    }
}
