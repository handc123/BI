package com.zjrcu.iras.bi.indicatorquality.domain.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 行社指标维度偏离度排名对象 org_indicator_deviation_rank
 *
 * @author ruoyi
 * @date 2025-08-06
 */
public class OrgIndicatorDeviationRank extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 统计月份
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计月份", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dataDate;

    /**
     * 行社ID
     */
    @Excel(name = "行社ID")
    private String orgId;

    /**
     * 行社名称
     */
    @Excel(name = "行社名称")
    private String orgName;

    /**
     * 指标ID
     */
    @Excel(name = "指标ID")
    private Long indicatorId;

    /**
     * 指标名称
     */
    @Excel(name = "指标名称")
    private String indicatorName;

    /**
     * 当前偏离度
     */
    @Excel(name = "当前偏离度")
    private BigDecimal deviationValue;

    /**
     * 当前偏离度排名（降序）
     */
    @Excel(name = "当前偏离度排名")
    private Long rankValue;

    /**
     * 排名变化
     */
    @Excel(name = "排名变化")
    private Long rankChange;



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

    public void setIndicatorId(Long indicatorId) {
        this.indicatorId = indicatorId;
    }

    public Long getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setDeviationValue(BigDecimal deviationValue) {
        this.deviationValue = deviationValue;
    }

    public BigDecimal getDeviationValue() {
        return deviationValue;
    }

    public void setRankValue(Long rankValue) {
        this.rankValue = rankValue;
    }

    public Long getRankValue() {
        return rankValue;
    }

    public void setRankChange(Long rankChange) {
        this.rankChange = rankChange;
    }

    public Long getRankChange() {
        return rankChange;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("dataDate", getDataDate())
                .append("orgId", getOrgId())
                .append("orgName", getOrgName())
                .append("indicatorId", getIndicatorId())
                .append("indicatorName", getIndicatorName())
                .append("deviationValue", getDeviationValue())
                .append("rankValue", getRankValue())
                .append("rankChange", getRankChange())
                .toString();
    }
}
