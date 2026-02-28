package com.zjrcu.iras.bi.metric.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 指标血缘关系对象 bi_metric_lineage
 *
 * @author iras
 * @date 2025-02-26
 */
public class MetricLineage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 上游指标ID */
    @Excel(name = "上游指标ID")
    @NotNull(message = "上游指标ID不能为空")
    private Long upstreamMetricId;

    /** 下游指标ID */
    @Excel(name = "下游指标ID")
    @NotNull(message = "下游指标ID不能为空")
    private Long downstreamMetricId;

    /** 转换类型:aggregation,calculation,filter */
    @Excel(name = "转换类型", readConverterExp = "aggregation=聚合,calculation=计算,filter=过滤")
    private String transformationType;

    /** 转换逻辑说明 */
    @Excel(name = "转换逻辑")
    private String transformationLogic;

    /** 依赖强度1-5 */
    @Excel(name = "依赖强度")
    private Integer dependencyStrength;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 上游指标对象(不持久化到数据库) */
    private transient MetricMetadata upstreamMetric;

    /** 下游指标对象(不持久化到数据库) */
    private transient MetricMetadata downstreamMetric;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUpstreamMetricId(Long upstreamMetricId) {
        this.upstreamMetricId = upstreamMetricId;
    }

    public Long getUpstreamMetricId() {
        return upstreamMetricId;
    }

    public void setDownstreamMetricId(Long downstreamMetricId) {
        this.downstreamMetricId = downstreamMetricId;
    }

    public Long getDownstreamMetricId() {
        return downstreamMetricId;
    }

    public void setTransformationType(String transformationType) {
        this.transformationType = transformationType;
    }

    public String getTransformationType() {
        return transformationType;
    }

    public void setTransformationLogic(String transformationLogic) {
        this.transformationLogic = transformationLogic;
    }

    public String getTransformationLogic() {
        return transformationLogic;
    }

    public void setDependencyStrength(Integer dependencyStrength) {
        this.dependencyStrength = dependencyStrength;
    }

    public Integer getDependencyStrength() {
        return dependencyStrength;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public MetricMetadata getUpstreamMetric() {
        return upstreamMetric;
    }

    public void setUpstreamMetric(MetricMetadata upstreamMetric) {
        this.upstreamMetric = upstreamMetric;
    }

    public MetricMetadata getDownstreamMetric() {
        return downstreamMetric;
    }

    public void setDownstreamMetric(MetricMetadata downstreamMetric) {
        this.downstreamMetric = downstreamMetric;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("upstreamMetricId", getUpstreamMetricId())
                .append("downstreamMetricId", getDownstreamMetricId())
                .append("transformationType", getTransformationType())
                .append("transformationLogic", getTransformationLogic())
                .append("dependencyStrength", getDependencyStrength())
                .append("createTime", getCreateTime())
                .toString();
    }

    /**
     * 判断是否为聚合类型
     */
    public boolean isAggregation() {
        return "aggregation".equalsIgnoreCase(this.transformationType);
    }

    /**
     * 判断是否为计算类型
     */
    public boolean isCalculation() {
        return "calculation".equalsIgnoreCase(this.transformationType);
    }

    /**
     * 判断是否为过滤类型
     */
    public boolean isFilter() {
        return "filter".equalsIgnoreCase(this.transformationType);
    }

    /**
     * 获取转换类型的显示名称
     */
    public String getTransformationTypeName() {
        if (transformationType == null) {
            return "未知";
        }
        switch (transformationType.toLowerCase()) {
            case "aggregation":
                return "聚合";
            case "calculation":
                return "计算";
            case "filter":
                return "过滤";
            default:
                return transformationType;
        }
    }
}
