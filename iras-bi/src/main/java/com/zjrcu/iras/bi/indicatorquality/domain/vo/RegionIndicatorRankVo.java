package com.zjrcu.iras.bi.indicatorquality.domain.vo;



import java.math.BigDecimal;
import java.util.Date;


public class RegionIndicatorRankVo {
    /**
     * 数据日期
     */
    private Date dataDate;
    /**
     * 地区名称
     */
    private String regionName;
    /**
     * 偏差小于2的占比
     */
    private BigDecimal deviationLt2Ratio;
    /**
     * 偏差大于2的占比
     */
    private BigDecimal noDeviationRatio;
    /**
     * 排名
     */
    private Integer rank;
    /**
     * 排名变化
     */
    private Integer rankChange;
    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }


    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getRankChange() {
        return rankChange;
    }

    public void setRankChange(Integer rankChange) {
        this.rankChange = rankChange;
    }


    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }



    public BigDecimal getDeviationLt2Ratio() {
        return deviationLt2Ratio;
    }

    public void setDeviationLt2Ratio(BigDecimal deviationLt2Ratio) {
        this.deviationLt2Ratio = deviationLt2Ratio;
    }

    public BigDecimal getNoDeviationRatio() {
        return noDeviationRatio;
    }

    public void setNoDeviationRatio(BigDecimal noDeviationRatio) {
        this.noDeviationRatio = noDeviationRatio;
    }


}
