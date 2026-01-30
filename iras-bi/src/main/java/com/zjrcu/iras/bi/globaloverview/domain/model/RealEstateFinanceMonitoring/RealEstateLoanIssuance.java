package com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

public class RealEstateLoanIssuance {

    /**
     * 主键
     */
    private Long id;
    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dataDate;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 地区名称
     */
    private String regionName;
    /**
     * 企业性质
     */
    private String ownershipType;
    /**
     * 当天发放房地产贷款金额
     */
    private BigDecimal issuedAmount;
    /**
     * 当天发放房地产贷款平均利率
     */
    private BigDecimal avgInterestRate;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
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

    public String getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }

    public BigDecimal getIssuedAmount() {
        return issuedAmount;
    }

    public void setIssuedAmount(BigDecimal issuedAmount) {
        this.issuedAmount = issuedAmount;
    }

    public BigDecimal getAvgInterestRate() {
        return avgInterestRate;
    }

    public void setAvgInterestRate(BigDecimal avgInterestRate) {
        this.avgInterestRate = avgInterestRate;
    }



}
