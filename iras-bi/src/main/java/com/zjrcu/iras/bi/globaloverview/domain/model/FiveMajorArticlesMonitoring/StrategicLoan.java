package com.zjrcu.iras.bi.globaloverview.domain.model.FiveMajorArticlesMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class StrategicLoan extends BaseEntity {
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
     * 贷款类别（普惠、科技、绿色)
     */
    private String category;
    /**
     * 贷款余额(亿元)
     */
    private BigDecimal loanBalance;
    /**
     * 当天发放贷款金额
     */
    private BigDecimal loanAmount;
    /**
     * 当天发放贷款年化利率（%）
     */
    private BigDecimal loanInterestRate;

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

    public BigDecimal getLoanBalance() {
        return loanBalance;
    }

    public void setLoanBalance(BigDecimal loanBalance) {
        this.loanBalance = loanBalance;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public BigDecimal getLoanInterestRate() {
        return loanInterestRate;
    }

    public void setLoanInterestRate(BigDecimal loanInterestRate) {
        this.loanInterestRate = loanInterestRate;
    }


}
