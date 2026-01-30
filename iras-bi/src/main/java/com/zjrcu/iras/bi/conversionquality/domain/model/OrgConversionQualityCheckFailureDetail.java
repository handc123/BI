package com.zjrcu.iras.bi.conversionquality.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

public class OrgConversionQualityCheckFailureDetail extends BaseEntity {
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
     * 表格名称
     */
    @Excel(name = "表格名称")
    private String tableName;
    /**
     * 数据项名称
     */
    @Excel(name = "数据项名称")
    private String fieldName;
    /**
     * 未通过的数据项数量
     */
    @Excel(name = "未通过数据项数量")
    private Long failureCount;

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

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


}
