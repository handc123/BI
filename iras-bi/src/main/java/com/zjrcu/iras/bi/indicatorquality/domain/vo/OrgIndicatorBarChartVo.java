package com.zjrcu.iras.bi.indicatorquality.domain.vo;

import java.util.Date;

public class OrgIndicatorBarChartVo {
    /**
     * 区域名称
     */
    private String regionName;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 无偏离数量
     */
    private Integer noDeviationCount;
    /**
     * 偏离度小于2%数量
     */
    private Integer deviationLt2Count;
    /**
     * 偏离度大于2%数量
     */
    private Integer deviationGt2Count;
    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    private Date dataDate;

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getOrgName() {
        return orgName;
    }
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Integer getNoDeviationCount() {
        return noDeviationCount;
    }
    public void setNoDeviationCount(Integer noDeviationCount) {
        this.noDeviationCount = noDeviationCount;
    }

    public Integer getDeviationLt2Count() {
        return deviationLt2Count;
    }
    public void setDeviationLt2Count(Integer deviationLt2Count) {
        this.deviationLt2Count = deviationLt2Count;
    }

    public Integer getDeviationGt2Count() {
        return deviationGt2Count;
    }
    public void setDeviationGt2Count(Integer deviationGt2Count) {
        this.deviationGt2Count = deviationGt2Count;
    }


}
