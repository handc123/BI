package com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateMonitoring;

import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class RealEstateLoan extends BaseEntity {
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
     * 房地产贷款总余额
     */
    private BigDecimal totalRealEstateLoan;
    /**
     * 国企房地产贷款余额
     */
    private BigDecimal stateOwnedRealEstateLoan;
    /**
     * 民企房地产贷款余额
     */
    private BigDecimal privateRealEstateLoan;

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

    public BigDecimal getTotalRealEstateLoan() {
        return totalRealEstateLoan;
    }

    public void setTotalRealEstateLoan(BigDecimal totalRealEstateLoan) {
        this.totalRealEstateLoan = totalRealEstateLoan;
    }

    public BigDecimal getStateOwnedRealEstateLoan() {
        return stateOwnedRealEstateLoan;
    }

    public void setStateOwnedRealEstateLoan(BigDecimal stateOwnedRealEstateLoan) {
        this.stateOwnedRealEstateLoan = stateOwnedRealEstateLoan;
    }

    public BigDecimal getPrivateRealEstateLoan() {
        return privateRealEstateLoan;
    }

    public void setPrivateRealEstateLoan(BigDecimal privateRealEstateLoan) {
        this.privateRealEstateLoan = privateRealEstateLoan;
    }


}
