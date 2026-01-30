package com.zjrcu.iras.bi.globaloverview.domain.model.KeyIndicatorMonitoring;

import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class KeyIndicator extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /**
     * 唯一标识符
     */
    private Long id;
    /**
     * 地区名称
     */
    private String regionName;
    /**
     * 数据日期
     */
    private Date dataDate;
    /**
     * 指标名称
     */
    private String indicatorName;
    /**
     * 指标值
     */
    private BigDecimal indicatorValue;
    /**
     * 较上日变化
     */
    private BigDecimal previousDayChange;
    /**
     * 较年初变化
     */
    private BigDecimal yearStartChange;
    /**
     * 系统内排名
     */
    private Integer rank;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public BigDecimal getIndicatorValue() {
        return indicatorValue;
    }

    public void setIndicatorValue(BigDecimal indicatorValue) {
        this.indicatorValue = indicatorValue;
    }

    public BigDecimal getPreviousDayChange() {
        return previousDayChange;
    }

    public void setPreviousDayChange(BigDecimal previousDayChange) {
        this.previousDayChange = previousDayChange;
    }

    public BigDecimal getYearStartChange() {
        return yearStartChange;
    }

    public void setYearStartChange(BigDecimal yearStartChange) {
        this.yearStartChange = yearStartChange;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }


}
