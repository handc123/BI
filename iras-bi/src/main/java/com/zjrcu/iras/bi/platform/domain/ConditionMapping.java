package com.zjrcu.iras.bi.platform.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * 条件映射对象 bi_condition_mapping
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class ConditionMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 映射ID */
    private Long id;

    /** 条件ID */
    private Long conditionId;

    /** 组件ID */
    private Long componentId;

    /** 表名 */
    private String tableName;

    /** 映射字段名 */
    private String fieldName;

    /** 映射类型(auto/custom) */
    private String mappingType;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setConditionId(Long conditionId) {
        this.conditionId = conditionId;
    }

    public Long getConditionId() {
        return conditionId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("conditionId", getConditionId())
            .append("componentId", getComponentId())
            .append("tableName", getTableName())
            .append("fieldName", getFieldName())
            .append("mappingType", getMappingType())
            .append("createTime", getCreateTime())
            .toString();
    }
}
