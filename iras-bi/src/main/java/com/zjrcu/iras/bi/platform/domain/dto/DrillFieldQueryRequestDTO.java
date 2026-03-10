package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 原始字段穿透查询请求
 */
public class DrillFieldQueryRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long datasetId;
    private String metricField;
    private String metricName;
    private Long dashboardId;
    private Long componentId;
    private Integer pageNum;
    private Integer pageSize;
    private List<DrillConditionDTO> inheritedConditions;
    private List<DrillRuleGroupDTO> ruleGroups;
    private List<DrillSortRuleDTO> sortRules;

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getMetricField() {
        return metricField;
    }

    public void setMetricField(String metricField) {
        this.metricField = metricField;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Long getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(Long dashboardId) {
        this.dashboardId = dashboardId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<DrillConditionDTO> getInheritedConditions() {
        return inheritedConditions;
    }

    public void setInheritedConditions(List<DrillConditionDTO> inheritedConditions) {
        this.inheritedConditions = inheritedConditions;
    }

    public List<DrillRuleGroupDTO> getRuleGroups() {
        return ruleGroups;
    }

    public void setRuleGroups(List<DrillRuleGroupDTO> ruleGroups) {
        this.ruleGroups = ruleGroups;
    }

    public List<DrillSortRuleDTO> getSortRules() {
        return sortRules;
    }

    public void setSortRules(List<DrillSortRuleDTO> sortRules) {
        this.sortRules = sortRules;
    }
}