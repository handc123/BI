package com.zjrcu.iras.bi.indicatorquality.domain.vo;

public class OrgIndicatorDeviationTrendItem {
    /** 统计月份（yyyy-MM） */
    private String dataDate;
    /** 指标数量 */
    private Integer value;
    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }


}
