package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 穿透明细配置
 */
public class DrillConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long metricId;
    private String metricCode;
    private String metricName;
    private Long datasetId;
    private String technicalFormula;
    private String calculationLogic;
    private List<Map<String, Object>> fields;

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

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

    public String getTechnicalFormula() {
        return technicalFormula;
    }

    public void setTechnicalFormula(String technicalFormula) {
        this.technicalFormula = technicalFormula;
    }

    public String getCalculationLogic() {
        return calculationLogic;
    }

    public void setCalculationLogic(String calculationLogic) {
        this.calculationLogic = calculationLogic;
    }

    public List<Map<String, Object>> getFields() {
        return fields;
    }

    public void setFields(List<Map<String, Object>> fields) {
        this.fields = fields;
    }
}

