package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 穿透明细查询请求
 */
public class DrillQueryRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long metricId;
    private Long dashboardId;
    private Long componentId;
    private Long datasetId;

    private Integer pageNum;
    private Integer pageSize;

    /**
     * 已带入条件(查询组件 + 口径条件)
     */
    private List<DrillConditionDTO> inheritedConditions;

    /**
     * 用户动态规则组
     */
    private List<DrillRuleGroupDTO> ruleGroups;

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
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

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
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
}

