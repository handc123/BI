package com.zjrcu.iras.bi.globaloverview.domain.qo;

import io.swagger.v3.oas.annotations.media.Schema;

public class GlobalOverviewRequestQO {
    /**
     * 机构名称
     */
    @Schema(description = "机构名称")
    private String orgName;
    /**
     * 数据日期
     */
    @Schema(description = "数据日期")
    private String dataDate;
    /**
     * 地区名称
     */
    @Schema(description = "地区名称")
    private String regionName;
    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String startDate;
    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endDate;
    /**
     * 五篇大文章金融类别
     */
    @Schema(description = "五篇大文章金融类别, GREEN、INCLUSIVE、TECHNOLOGY")
    private String financeType;
    /**
     * 特色贷款类型
     */
    @Schema(description = "特色贷款类型，NONREPAYMENTRENEWAL、INTERNET、GREEN")
    private String characterType;
    /**
     * 逾期期限类别:1:30天，2:60天，3:90天
     */
    @Schema(description = "逾期期限类别:1:30天，2:60天，3:90天")
    private Long overduePeriod;
    /**
     * 房地产当天发放金额类别：STATE，PRIVATE
     */
    @Schema(description = "房地产当天发放金额类别：STATE，PRIVATE")
    private String ownershipType;
    /**
     * 分组维度：ORG：机构，REGION：地区
     */
    @Schema(description = "分组维度：ORG：机构，REGION：地区，TYPE：类型")
    private String groupName;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }






    public Long getOverduePeriod() {
        return overduePeriod;
    }

    public void setOverduePeriod(Long overduePeriod) {
        this.overduePeriod = overduePeriod;
    }



    public String getCharacterType() {
        return characterType;
    }

    public void setCharacterType(String characterType) {
        this.characterType = characterType;
    }



    public String getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }



    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }



    public String getFinanceType() {
        return financeType;
    }

    public void setFinanceType(String financeType) {
        this.financeType = financeType;
    }



}
