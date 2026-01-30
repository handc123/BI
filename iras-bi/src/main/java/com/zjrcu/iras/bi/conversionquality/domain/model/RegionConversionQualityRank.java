package com.zjrcu.iras.bi.conversionquality.domain.model;

import java.math.BigDecimal;
import java.util.Date;

import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 地区管理部通过率排名对象 region_conversion_quality_rank
 *
 * @author ruoyi
 * @date 2025-08-07
 */
public class RegionConversionQualityRank extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 数据日期
     */
    private Date dataDate;

    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 地区整体通过率
     */
    @Excel(name = "地区整体通过率")
    private BigDecimal passRate;

    /**
     * 排名变化
     */
    @Excel(name = "排名变化")
    private Long rankChange;



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

    public void setRankChange(Long rankChange) {
        this.rankChange = rankChange;
    }

    public Long getRankChange() {
        return rankChange;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("dataDate", getDataDate())
                .append("regionName", getRegionName())
                .append("passRate", getPassRate())
                .append("rankChange", getRankChange())
                .toString();
    }
}
