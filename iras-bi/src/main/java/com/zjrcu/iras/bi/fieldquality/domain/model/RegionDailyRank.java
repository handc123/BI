package com.zjrcu.iras.bi.fieldquality.domain.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 地区管理部排名（地区通过率排名）对象 region_daily_rank
 *
 * @author ruoyi
 * @date 2025-07-30
 */
public class RegionDailyRank extends BaseEntity {
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
     * 地区名称（如：华东地区、华北地区等）
     */
    @Excel(name = "地区名称", readConverterExp = "如=：华东地区、华北地区等")
    private String regionName;

    /**
     * 地区整体通过率（保留2位小数，范围0-100，单位：%）
     */
    @Excel(name = "地区整体通过率", readConverterExp = "保=留2位小数，范围0-100，单位：%")
    private BigDecimal passRate;

    /**
     * 当前排名（1表示第一名）
     */
    @Excel(name = "当前排名", readConverterExp = "1=表示第一名")
    private Long currentRank;

    /**
     * 上期排名（可为空，如首次统计时无上期数据）
     */
    @Excel(name = "上期排名", readConverterExp = "可=为空，如首次统计时无上期数据")
    private Long previousRank;

    /**
     * 排名变化（当前排名 - 上期排名，正数表示排名下降，负数表示上升）
     */
    @Excel(name = "排名变化", readConverterExp = "当=前排名,-=,上=期排名，正数表示排名下降，负数表示上升")
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

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setPassRate(BigDecimal passRate) {
        this.passRate = passRate;
    }

    public BigDecimal getPassRate() {
        return passRate;
    }

    public void setCurrentRank(Long currentRank) {
        this.currentRank = currentRank;
    }

    public Long getCurrentRank() {
        return currentRank;
    }

    public void setPreviousRank(Long previousRank) {
        this.previousRank = previousRank;
    }

    public Long getPreviousRank() {
        return previousRank;
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
                .append("regionName", getRegionName())
                .append("passRate", getPassRate())
                .append("currentRank", getCurrentRank())
                .append("previousRank", getPreviousRank())
                .append("rankChange", getRankChange())
                .toString();
    }
}
