package com.zjrcu.iras.bi.indicatorquality.domain.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 行社维度的排名对象 org_indicator_rank
 *
 * @author ruoyi
 * @date 2025-08-06
 */
public class OrgIndicatorRank extends BaseEntity {
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
    private Long orgId;

    /**
     * 行社名称
     */
    @Excel(name = "行社名称")
    private String orgName;

    /**
     * 排名维度标识（如：无偏离指标数,偏离度通过指标数,提升指标数）
     */
    @Excel(name = "排名维度标识")
    private String dimensionType;

    /**
     * 指标个数
     */
    @Excel(name = "指标个数")
    private Long indicatorCount;

    public Long getRankValue() {
        return rankValue;
    }

    public void setRankValue(Long rankValue) {
        this.rankValue = rankValue;
    }

    /**
     * 当前排名
     */
    @Excel(name = "当前排名")
    private Long rankValue;

    /**
     * 与上一期相比的排名变化值（正数为上升，负数为下降，0为不变）
     */
    @Excel(name = "与上一期相比的排名变化值")
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

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setDimensionType(String dimensionType) {
        this.dimensionType = dimensionType;
    }

    public String getDimensionType() {
        return dimensionType;
    }

    public void setIndicatorCount(Long indicatorCount) {
        this.indicatorCount = indicatorCount;
    }

    public Long getIndicatorCount() {
        return indicatorCount;
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
                .append("dimensionType", getDimensionType())
                .append("indicatorCount", getIndicatorCount())
                .append("rankValue", getRankValue())
                .append("rankChange", getRankChange())
                .toString();
    }
}
