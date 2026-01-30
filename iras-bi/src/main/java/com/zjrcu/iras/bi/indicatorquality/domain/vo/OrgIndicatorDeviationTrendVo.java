package com.zjrcu.iras.bi.indicatorquality.domain.vo;

import java.util.List;

public class OrgIndicatorDeviationTrendVo {
    /**
     * 无偏离指标趋势
     */
    private List<OrgIndicatorDeviationTrendItem> noDeviation;

    /**
     * 偏离度低于2%指标趋势
     */
    private List<OrgIndicatorDeviationTrendItem> deviationLt2;

    /**
     * 偏离度超过2%指标趋势
     */
    private List<OrgIndicatorDeviationTrendItem> deviationGt2;
    public List<OrgIndicatorDeviationTrendItem> getNoDeviation() {
        return noDeviation;
    }

    public void setNoDeviation(List<OrgIndicatorDeviationTrendItem> noDeviation) {
        this.noDeviation = noDeviation;
    }

    public List<OrgIndicatorDeviationTrendItem> getDeviationLt2() {
        return deviationLt2;
    }

    public void setDeviationLt2(List<OrgIndicatorDeviationTrendItem> deviationLt2) {
        this.deviationLt2 = deviationLt2;
    }

    public List<OrgIndicatorDeviationTrendItem> getDeviationGt2() {
        return deviationGt2;
    }

    public void setDeviationGt2(List<OrgIndicatorDeviationTrendItem> deviationGt2) {
        this.deviationGt2 = deviationGt2;
    }





}
