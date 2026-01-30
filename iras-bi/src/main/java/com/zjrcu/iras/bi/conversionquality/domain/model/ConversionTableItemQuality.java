package com.zjrcu.iras.bi.conversionquality.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

public class ConversionTableItemQuality extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 机构ID
     */
    private String orgId;
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
     * 表格名称
     */
    private String tableName;
    /**
     * 表格编码
     */
    private String tableCode;
    /**
     * 数据项数量
     */
    private Long itemCount;
    /**
     * 数据项无偏差数量
     */
    private Long noDeviationCount;
    /**
     * 数据项偏差大于%2数量
     */
    private Long deviationGT2Count;
    /**
     * 数据项偏差小于%2数量
     */
    private Long deviationLT2Count;

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableCode() {
        return tableCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }

    public Long getItemCount() {
        return itemCount;
    }

    public void setItemCount(Long itemCount) {
        this.itemCount = itemCount;
    }

    public Long getNoDeviationCount() {
        return noDeviationCount;
    }

    public void setNoDeviationCount(Long noDeviationCount) {
        this.noDeviationCount = noDeviationCount;
    }

    public Long getDeviationGT2Count() {
        return deviationGT2Count;
    }

    public void setDeviationGT2Count(Long deviationGT2Count) {
        this.deviationGT2Count = deviationGT2Count;
    }

    public Long getDeviationLT2Count() {
        return deviationLT2Count;
    }

    public void setDeviationLT2Count(Long deviationLT2Count) {
        this.deviationLT2Count = deviationLT2Count;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

}
