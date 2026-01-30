package com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class InterbankAssetLiability extends BaseEntity {
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
     * 同业金融机构类别
     */
    private String category;
    /**
     * 同业资产余额
     */
    private BigDecimal assetBalance;
    /**
     * 同业负债余额
     */
    private BigDecimal liabilityBalance;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAssetBalance() {
        return assetBalance;
    }

    public void setAssetBalance(BigDecimal assetBalance) {
        this.assetBalance = assetBalance;
    }

    public BigDecimal getLiabilityBalance() {
        return liabilityBalance;
    }

    public void setLiabilityBalance(BigDecimal liabilityBalance) {
        this.liabilityBalance = liabilityBalance;
    }


}
