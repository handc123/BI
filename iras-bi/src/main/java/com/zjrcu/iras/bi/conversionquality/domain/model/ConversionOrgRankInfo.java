package com.zjrcu.iras.bi.conversionquality.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

public class ConversionOrgRankInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;



    /**
     * 主键
     */
    private Long id;

    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "数据日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dataDate;
    /**
     * 机构编号
     */
    @Excel(name = "机构编号")
    private String orgId;
    /**
     * 机构名称
     */
    @Excel(name = "机构名称")
    private String orgName;
    /**
     * 排名类别
     */
    @Excel(name = "排名类别")
    private String rankType;
    /**
     * 数量
     */
    @Excel(name = "通过项数量")
    private Long count;
    /**
     * 排名变化
     */
    @Excel(name = "排名变化")
    private String rankChange;
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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getRankType() {
        return rankType;
    }

    public void setRankType(String rankType) {
        this.rankType = rankType;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getRankChange() {
        return rankChange;
    }

    public void setRankChange(String rankChange) {
        this.rankChange = rankChange;
    }
}
