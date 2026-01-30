package com.zjrcu.iras.bi.indicatorquality.domain.vo;


public class OrgIndicatorDevationCountVo {
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 总数
     */
    private Integer totalCount;
    /**
     * 无偏差数
     */
    private Integer noDeviationCount;
    /**
     * 偏差数小于2
     */
    private Integer deviationLt2Count;
    /**
     * 偏差数大于2
     */
    private Integer deviationGt2Count;
    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
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
