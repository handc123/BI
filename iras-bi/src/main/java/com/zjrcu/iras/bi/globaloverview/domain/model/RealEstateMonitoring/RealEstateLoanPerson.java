package com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateMonitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

public class RealEstateLoanPerson extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    private Long id;
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
     * 贷款人数
     */
    private Long loanPerson;
    /**
     * 贷款类型
     */
    private String loanType;

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

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

    public Long getLoanPerson() {
        return loanPerson;
    }

    public void setLoanPerson(Long loanPerson) {
        this.loanPerson = loanPerson;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }


}
