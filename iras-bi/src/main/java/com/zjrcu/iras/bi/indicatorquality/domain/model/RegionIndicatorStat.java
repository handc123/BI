package com.zjrcu.iras.bi.indicatorquality.domain.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 地区维度统计对象 region_indicator_stat
 *
 * @author ruoyi
 * @date 2025-08-05
 */
public class RegionIndicatorStat extends BaseEntity {
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
     * 地区名称
     */
    @Excel(name = "地区名称")
    private String regionName;

    /**
     * 指标偏离度类别（无偏离指标、偏离度低于2%指标、偏离度超过2%指标）
     */
    @Excel(name = "指标偏离度类别", readConverterExp = "无=偏离指标、偏离度低于2%指标、偏离度超过2%指标")
    private String indicatorDevationType;

    /**
     * 指标占比
     */
    @Excel(name = "指标占比")
    private BigDecimal ratio;

    /**
     * 当前排名
     */
    @Excel(name = "当前排名")
    private Long rank;

    /**
     * 与上一期相比的排名变化
     */
    @Excel(name = "与上一期相比的排名变化")
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

    public void setIndicatorDevationType(String indicatorDevationType) {
        this.indicatorDevationType = indicatorDevationType;
    }

    public String getIndicatorDevationType() {
        return indicatorDevationType;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public Long getRank() {
        return rank;
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
                .append("indicatorDevationType", getIndicatorDevationType())
                .append("ratio", getRatio())
                .append("rank", getRank())
                .append("rankChange", getRankChange())
                .toString();
    }
}
