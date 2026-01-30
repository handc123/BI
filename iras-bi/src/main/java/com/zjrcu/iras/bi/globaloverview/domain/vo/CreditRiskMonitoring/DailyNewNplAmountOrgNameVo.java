package com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring;

import java.math.BigDecimal;

public class DailyNewNplAmountOrgNameVo {
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 当日新增不良贷款金额
     */
    private BigDecimal totalDailyNewNplAmount;
    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
    public BigDecimal getTotalDailyNewNplAmount() {
        return totalDailyNewNplAmount;
    }

    public void setTotalDailyNewNplAmount(BigDecimal totalDailyNewNplAmount) {
        this.totalDailyNewNplAmount = totalDailyNewNplAmount;
    }


}
