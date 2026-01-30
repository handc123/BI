package com.zjrcu.iras.bi.fieldquality.domain.vo;

public class RegionOrgCheckFailureSummaryVo {
    /**
     * 未通过记录数
     */
    private Integer unpassedRecordCount;
    /**
     * 未通过规则数
     */
    private Integer unpassedRuleCount;

    public Integer getUnpassedRecordCount() {
        return unpassedRecordCount;
    }

    public void setUnpassedRecordCount(Integer unpassedRecordCount) {
        this.unpassedRecordCount = unpassedRecordCount;
    }

    public Integer getUnpassedRuleCount() {
        return unpassedRuleCount;
    }

    public void setUnpassedRuleCount(Integer unpassedRuleCount) {
        this.unpassedRuleCount = unpassedRuleCount;
    }


}
