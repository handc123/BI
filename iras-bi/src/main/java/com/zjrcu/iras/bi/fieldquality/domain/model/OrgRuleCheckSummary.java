package com.zjrcu.iras.bi.fieldquality.domain.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 规则校验结果总（含核心指标及折线图数据）对象 org_rule_check_summary
 *
 * @author ruoyi
 * @date 2025-08-01
 */
public class OrgRuleCheckSummary extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 行社编码（101=萧山农商行等）
     */
    @Excel(name = "行社编码")
    private String orgId;

    /**
     * 行社名称
     */
    @Excel(name = "行社名称")
    private String orgName;

    /**
     * 数据日期（T-1，统计基准日）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "数据日期", dateFormat = "yyyy-MM-dd")
    private Date dataDate;

    /**
     * 1. 校验规则项：所有生效规则数量
     */
    @Excel(name = "1. 校验规则项：所有生效规则数量")
    private Long totalValidRules;

    /**
     * 2. 通过项：近30天都通过的规则数量（同时作为折线图“通过项”数据点）
     */
    @Excel(name = "2. 通过项：近30天都通过的规则数量", readConverterExp = "同=时作为折线图“通过项”数据点")
    private Long passItems;

    /**
     * 3. 提升通过项：当前通过但近30天有未通过的规则数量
     */
    @Excel(name = "3. 提升通过项：当前通过但近30天有未通过的规则数量")
    private Long improvedPassItems;

    /**
     * 4. 未通过项：当前未通过的规则数量（同时作为折线图“未通过项”数据点）
     */
    @Excel(name = "4. 未通过项：当前未通过的规则数量", readConverterExp = "同=时作为折线图“未通过项”数据点")
    private Long failedItems;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setTotalValidRules(Long totalValidRules) {
        this.totalValidRules = totalValidRules;
    }

    public Long getTotalValidRules() {
        return totalValidRules;
    }

    public void setPassItems(Long passItems) {
        this.passItems = passItems;
    }

    public Long getPassItems() {
        return passItems;
    }

    public void setImprovedPassItems(Long improvedPassItems) {
        this.improvedPassItems = improvedPassItems;
    }

    public Long getImprovedPassItems() {
        return improvedPassItems;
    }

    public void setFailedItems(Long failedItems) {
        this.failedItems = failedItems;
    }

    public Long getFailedItems() {
        return failedItems;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("orgId", getOrgId())
                .append("orgName", getOrgName())
                .append("dataDate", getDataDate())
                .append("totalValidRules", getTotalValidRules())
                .append("passItems", getPassItems())
                .append("improvedPassItems", getImprovedPassItems())
                .append("failedItems", getFailedItems())
                .toString();
    }
}
