package com.zjrcu.iras.bi.platform.domain.dto;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.DashboardComponent;
import com.zjrcu.iras.bi.platform.domain.QueryCondition;
import com.zjrcu.iras.bi.platform.domain.ConditionMapping;

import java.io.Serializable;
import java.util.List;

/**
 * 仪表板配置DTO
 * 包含仪表板及其所有关联的组件、查询条件和条件映射
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class DashboardConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 仪表板基本信息 */
    private Dashboard dashboard;

    /** 组件列表 */
    private List<DashboardComponent> components;

    /** 查询条件列表 */
    private List<QueryCondition> queryConditions;

    /** 条件映射列表 */
    private List<ConditionMapping> conditionMappings;

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public List<DashboardComponent> getComponents() {
        return components;
    }

    public void setComponents(List<DashboardComponent> components) {
        this.components = components;
    }

    public List<QueryCondition> getQueryConditions() {
        return queryConditions;
    }

    public void setQueryConditions(List<QueryCondition> queryConditions) {
        this.queryConditions = queryConditions;
    }

    public List<ConditionMapping> getConditionMappings() {
        return conditionMappings;
    }

    public void setConditionMappings(List<ConditionMapping> conditionMappings) {
        this.conditionMappings = conditionMappings;
    }
}
