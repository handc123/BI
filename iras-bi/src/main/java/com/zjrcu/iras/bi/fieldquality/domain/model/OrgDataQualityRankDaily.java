package com.zjrcu.iras.bi.fieldquality.domain.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 行社数据质量每日排名对象 org_data_quality_rank_daily
 *
 * @author ruoyi
 * @date 2025-07-31
 */
public class OrgDataQualityRankDaily extends BaseEntity {
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
     * 行社编码（机构唯一标识）
     */
    @Excel(name = "行社编码")
    private String orgId;

    /**
     * 行社名称
     */
    @Excel(name = "行社名称")
    private String orgName;

    /**
     * 排名类型：综合通过率、通过率、提升项
     */
    @Excel(name = "排名类型：综合通过率、通过率、提升项")
    private String rankType;

    /**
     * 排名值
     */
    @Excel(name = "排名值")
    private Long value;

    /**
     * 排名变化（当前排名 - 上期排名，正数下降，负数上升，0不变；首次排名可为空）
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

    public void setRankType(String rankType) {
        this.rankType = rankType;
    }

    public String getRankType() {
        return rankType;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
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
                .append("rankType", getRankType())
                .append("value", getValue())
                .append("rankChange", getRankChange())
                .toString();
    }
}
