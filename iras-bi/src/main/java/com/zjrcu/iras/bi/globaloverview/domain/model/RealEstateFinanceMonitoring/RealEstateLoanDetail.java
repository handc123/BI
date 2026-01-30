package com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class RealEstateLoanDetail extends BaseEntity {

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
     * 机构类型
     */
    private String orgType;

    public String getOrgLevel() {
        return orgLevel;
    }

    public void setOrgLevel(String orgLevel) {
        this.orgLevel = orgLevel;
    }

    /**
     * 机构层级
     */
    private String orgLevel;

    /**
     * 贷款类型（开发贷/按揭/商业房贷等）
     */
    private String loanCategory;

    /**
     * 不良贷款标识（0=否，1=是）
     */
    private Integer nonPerformingLoan;

    /**
     * 以房地产为押品标识（0=否，1=是）
     */
    private Integer realEstateCollateralLoan;

    /**
     * 房地产贷款余额
     */
    private BigDecimal realEstateLoanBalance;

    /**
     * 房地产贷款占比
     */
    private BigDecimal realEstateLoanRatio;

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

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

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getLoanCategory() {
        return loanCategory;
    }

    public void setLoanCategory(String loanCategory) {
        this.loanCategory = loanCategory;
    }

    public Integer getNonPerformingLoan() {
        return nonPerformingLoan;
    }

    public void setNonPerformingLoan(Integer nonPerformingLoan) {
        this.nonPerformingLoan = nonPerformingLoan;
    }

    public Integer getRealEstateCollateralLoan() {
        return realEstateCollateralLoan;
    }

    public void setRealEstateCollateralLoan(Integer realEstateCollateralLoan) {
        this.realEstateCollateralLoan = realEstateCollateralLoan;
    }

    public BigDecimal getRealEstateLoanBalance() {
        return realEstateLoanBalance;
    }

    public void setRealEstateLoanBalance(BigDecimal realEstateLoanBalance) {
        this.realEstateLoanBalance = realEstateLoanBalance;
    }

    public BigDecimal getRealEstateLoanRatio() {
        return realEstateLoanRatio;
    }

    public void setRealEstateLoanRatio(BigDecimal realEstateLoanRatio) {
        this.realEstateLoanRatio = realEstateLoanRatio;
    }



}
