package com.zjrcu.iras.bi.conversionquality.domain.model;

import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

public class OrgConversionTable extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id;
    /**
     * 机构ID
     */
    private String orgId;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 数据日期
     */
    private Date dataDate;
    /**
     * 未通过表格数量
     */
    private Long unpassedTableCount;
    /**
     * 通过表格数量
     */
    private Long passedTableCount;

    public Long getPassedTableCount() {
        return passedTableCount;
    }

    public void setPassedTableCount(Long passedTableCount) {
        this.passedTableCount = passedTableCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public Long getUnpassedTableCount() {
        return unpassedTableCount;
    }

    public void setUnpassedTableCount(Long unpassedTableCount) {
        this.unpassedTableCount = unpassedTableCount;
    }


}
