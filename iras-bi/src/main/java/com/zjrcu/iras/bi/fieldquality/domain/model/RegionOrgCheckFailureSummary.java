package com.zjrcu.iras.bi.fieldquality.domain.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 地区行社未通过项（统计未通过规则项数和记录数）对象 region_org_check_failure_summary
 *
 * @author ruoyi
 * @date 2025-07-30
 */
public class RegionOrgCheckFailureSummary extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "数据日期", dateFormat = "yyyy-MM-dd")
    private Date dataDate;

    /**
     * 地区名称（如：华东地区、江苏省等）
     */
    @Excel(name = "地区名称", readConverterExp = "如=：杭州等")
    private String regionName;

    /**
     * 行社ID（机构唯一标识）
     */
    @Excel(name = "行社ID", readConverterExp = "机构唯一标识")
    private String orgId;

    /**
     * 行社名称（机构全称）
     */
    @Excel(name = "行社名称", readConverterExp = "机构全称")
    private String orgName;

    /**
     * 当日未通过的规则项数（不同规则的数量）
     */
    @Excel(name = "当日未通过的规则项数", readConverterExp = "不同规则的数量")
    private Long unpassedRuleCount;

    /**
     * 当日未通过记录数（所有未通过规则的记录总和）
     */
    @Excel(name = "当日未通过记录数", readConverterExp = "所有未通过规则的记录总和")
    private Long unpassedRecordCount;



    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setUnpassedRuleCount(Long unpassedRuleCount) {
        this.unpassedRuleCount = unpassedRuleCount;
    }

    public Long getUnpassedRuleCount() {
        return unpassedRuleCount;
    }

    public void setUnpassedRecordCount(Long unpassedRecordCount) {
        this.unpassedRecordCount = unpassedRecordCount;
    }

    public Long getUnpassedRecordCount() {
        return unpassedRecordCount;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("dataDate", getDataDate())
                .append("regionName", getRegionName())
                .append("orgId", getOrgId())
                .append("orgName", getOrgName())
                .append("unpassedRuleCount", getUnpassedRuleCount())
                .append("unpassedRecordCount", getUnpassedRecordCount())
                .toString();
    }
}
