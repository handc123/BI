package com.zjrcu.iras.bi.fieldquality.domain.model;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 规则项通过率分布统计（用于看板环形图及趋势图展示）对象 rule_pass_rate_distribution
 *
 * @author ruoyi
 * @date 2025-07-28
 */

public class RulePassRateDistribution extends BaseEntity {
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
     * 机构ID
     */
    @Excel(name = "机构ID")
    private String orgId;

    /**
     * 机构名称
     */
    @Excel(name = "机构名称")
    private String orgName;

    /**
     * 通过率区间（枚举值：0-20%、20-40%、40-60%、60-80%、80-100%、100%）
     */

    @Excel(name = "通过率区间", readConverterExp = "枚=举值：0-20%、20-40%、40-60%、60-80%、80-100%、100%")
    private String passRateRange;

    /**
     * 当前区间内的规则项数量
     */
    @Excel(name = "当前区间内的规则项数量")
    private Long ruleCount;

    /**
     * 当前机构下总规则项数量
     */
    @Excel(name = "当前机构下总规则项数量")
    private Long totalRules;


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

    public void setPassRateRange(String passRateRange) {
        this.passRateRange = passRateRange;
    }

    public String getPassRateRange() {
        return passRateRange;
    }

    public void setRuleCount(Long ruleCount) {
        this.ruleCount = ruleCount;
    }

    public Long getRuleCount() {
        return ruleCount;
    }

    public void setTotalRules(Long totalRules) {
        this.totalRules = totalRules;
    }

    public Long getTotalRules() {
        return totalRules;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("dataDate", getDataDate())
                .append("orgId", getOrgId())
                .append("orgName", getOrgName())
                .append("passRateRange", getPassRateRange())
                .append("ruleCount", getRuleCount())
                .append("totalRules", getTotalRules()).toString();
    }
}
