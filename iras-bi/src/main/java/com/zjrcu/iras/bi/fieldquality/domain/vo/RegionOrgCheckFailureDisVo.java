package com.zjrcu.iras.bi.fieldquality.domain.vo;

public class RegionOrgCheckFailureDisVo {
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 未通过记录数
     */
    private int unpassedRecordCount;
    /**
     * 未通过规则数
     */
    private int unpassedRuleCount;
    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public int getUnpassedRecordCount() {
        return unpassedRecordCount;
    }

    public void setUnpassedRecordCount(int unpassedRecordCount) {
        this.unpassedRecordCount = unpassedRecordCount;
    }

    public int getUnpassedRuleCount() {
        return unpassedRuleCount;
    }

    public void setUnpassedRuleCount(int unpassedRuleCount) {
        this.unpassedRuleCount = unpassedRuleCount;
    }


}
