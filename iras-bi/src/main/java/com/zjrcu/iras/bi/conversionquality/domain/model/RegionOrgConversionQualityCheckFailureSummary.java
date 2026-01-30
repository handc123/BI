package com.zjrcu.iras.bi.conversionquality.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

public class RegionOrgConversionQualityCheckFailureSummary extends BaseEntity {
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
     * 地区
     */
    @Excel(name = "地区")
    private String regionName;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

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
     * 未通过表格数量
     */
    @Excel(name = "未通过表格数量")
    private Long unpassedTableCount;
    /**
     * 未通过数据项数量
     */
    @Excel(name = "未通过数据项数量")
    private Long unpassedFieldCount;
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

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }



    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Long getUnpassedTableCount() {
        return unpassedTableCount;
    }

    public void setUnpassedTableCount(Long unpassedTableCount) {
        this.unpassedTableCount = unpassedTableCount;
    }

    public Long getUnpassedFieldCount() {
        return unpassedFieldCount;
    }

    public void setUnpassedFieldCount(Long unpassedFieldCount) {
        this.unpassedFieldCount = unpassedFieldCount;
    }


}
