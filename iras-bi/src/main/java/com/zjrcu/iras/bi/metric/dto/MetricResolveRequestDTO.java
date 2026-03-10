package com.zjrcu.iras.bi.metric.dto;

import java.io.Serializable;

/**
 * 指标解析请求（按当前机构）
 */
public class MetricResolveRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String metricCode;
    private String metricName;
    private Long datasetId;

    public String getMetricCode() {
        return metricCode;
    }

    public void setMetricCode(String metricCode) {
        this.metricCode = metricCode;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }
}

