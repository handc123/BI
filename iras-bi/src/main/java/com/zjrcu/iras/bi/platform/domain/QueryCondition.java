package com.zjrcu.iras.bi.platform.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询条件对象 bi_query_condition
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class QueryCondition implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 条件ID */
    private Long id;

    /** 仪表板ID */
    private Long dashboardId;

    /** 组件ID */
    private Long componentId;

    /** 条件名称 */
    private String conditionName;

    /** 条件类型(time/dropdown/select/range) */
    private String conditionType;

    /** 显示顺序 */
    private Integer displayOrder;

    /** 是否必填(0否 1是) */
    private String isRequired;

    /** 是否显示(0否 1是) */
    private String isVisible;

    /** 默认值 */
    private String defaultValue;

    /** 条件配置(JSON:时间粒度/选项列表/范围限制等) */
    private String config;

    /** 父条件ID(级联) */
    private Long parentConditionId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 临时ID */
    private String tempId;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDashboardId(Long dashboardId) {
        this.dashboardId = dashboardId;
    }

    public Long getDashboardId() {
        return dashboardId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setIsRequired(String isRequired) {
        this.isRequired = isRequired;
    }

    public String getIsRequired() {
        return isRequired;
    }

    public void setIsVisible(String isVisible) {
        this.isVisible = isVisible;
    }

    public String getIsVisible() {
        return isVisible;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConfig() {
        return config;
    }

    public void setParentConditionId(Long parentConditionId) {
        this.parentConditionId = parentConditionId;
    }

    public Long getParentConditionId() {
        return parentConditionId;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("dashboardId", getDashboardId())
            .append("componentId", getComponentId())
            .append("conditionName", getConditionName())
            .append("conditionType", getConditionType())
            .append("displayOrder", getDisplayOrder())
            .append("isRequired", getIsRequired())
            .append("isVisible", getIsVisible())
            .append("defaultValue", getDefaultValue())
            .append("config", getConfig())
            .append("parentConditionId", getParentConditionId())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
