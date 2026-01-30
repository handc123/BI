package com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class Deposit extends BaseEntity {
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
     * 当天吸收定期存款金额（亿元）
     */
    private BigDecimal depositAmount;
    /**
     * 当天存款平均利率（%）
     */
    private BigDecimal depositRate;

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

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getDepositRate() {
        return depositRate;
    }

    public void setDepositRate(BigDecimal depositRate) {
        this.depositRate = depositRate;
    }


}
