package com.zjrcu.iras.bi.platform.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 指标测试请求DTO
 * 用于测试指标计算
 * 
 * @author IRAS
 */
public class MetricTestRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 数据集ID */
    @NotNull(message = "数据集ID不能为空")
    private Long datasetId;
    
    /** 指标配置 */
    @NotNull(message = "指标配置不能为空")
    @Valid
    private MetricConfigDTO metric;
    
    public Long getDatasetId() {
        return datasetId;
    }
    
    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }
    
    public MetricConfigDTO getMetric() {
        return metric;
    }
    
    public void setMetric(MetricConfigDTO metric) {
        this.metric = metric;
    }
}
