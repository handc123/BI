package com.zjrcu.iras.bi.metric.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 指标数据查询模板对象 bi_metric_data_query
 *
 * @author iras
 * @date 2025-02-26
 */
public class MetricDataQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 指标ID */
    @Excel(name = "指标ID")
    @NotNull(message = "指标ID不能为空")
    private Long metricId;

    /** 查询名称 */
    @Excel(name = "查询名称")
    private String queryName;

    /** SQL查询模板,使用{{param}}作为参数占位符 */
    @Excel(name = "查询模板")
    private String queryTemplate;

    /** 默认参数配置(JSON格式) */
    private String defaultParameters;

    /** 默认过滤条件(JSON格式) */
    private String defaultFilters;

    /** 所需权限(逗号分隔) */
    @Excel(name = "所需权限")
    private String requiredPermissions;

    /** 默认行数限制 */
    @Excel(name = "行数限制")
    private Integer rowLimit;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 关联的指标对象(不持久化到数据库) */
    private transient MetricMetadata metric;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public Long getMetricId() {
        return metricId;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryTemplate(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }

    public String getQueryTemplate() {
        return queryTemplate;
    }

    public void setDefaultParameters(String defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    public String getDefaultParameters() {
        return defaultParameters;
    }

    public void setDefaultFilters(String defaultFilters) {
        this.defaultFilters = defaultFilters;
    }

    public String getDefaultFilters() {
        return defaultFilters;
    }

    public void setRequiredPermissions(String requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    public String getRequiredPermissions() {
        return requiredPermissions;
    }

    public void setRowLimit(Integer rowLimit) {
        this.rowLimit = rowLimit;
    }

    public Integer getRowLimit() {
        return rowLimit;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public MetricMetadata getMetric() {
        return metric;
    }

    public void setMetric(MetricMetadata metric) {
        this.metric = metric;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("metricId", getMetricId())
                .append("queryName", getQueryName())
                .append("queryTemplate", getQueryTemplate())
                .append("defaultParameters", getDefaultParameters())
                .append("defaultFilters", getDefaultFilters())
                .append("requiredPermissions", getRequiredPermissions())
                .append("rowLimit", getRowLimit())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }

    /**
     * 判断查询模板是否正常
     */
    public boolean isNormal() {
        return "0".equals(this.status);
    }

    /**
     * 判断是否有行数限制
     */
    public boolean hasRowLimit() {
        return this.rowLimit != null && this.rowLimit > 0;
    }

    /**
     * 获取实际行数限制（如果未设置则返回默认值1000）
     */
    public int getActualRowLimit() {
        return hasRowLimit() ? this.rowLimit : 1000;
    }
}
