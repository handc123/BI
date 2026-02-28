package com.zjrcu.iras.bi.metric.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.common.annotation.Excel;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 指标元数据对象 bi_metric_metadata
 *
 * @author iras
 * @date 2025-02-26
 */
public class MetricMetadata extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 业务唯一编码 */
    @Excel(name = "指标编码")
    @NotBlank(message = "指标编码不能为空")
    @Size(max = 50, message = "指标编码长度不能超过50个字符")
    private String metricCode;

    /** 指标名称 */
    @Excel(name = "指标名称")
    @NotBlank(message = "指标名称不能为空")
    @Size(max = 100, message = "指标名称长度不能超过100个字符")
    private String metricName;

    /** 关联数据集ID */
    @Excel(name = "数据集ID")
    private Long datasetId;

    /** 关联可视化ID */
    @Excel(name = "可视化ID")
    private Long visualizationId;

    /** 业务定义 */
    @Excel(name = "业务定义")
    private String businessDefinition;

    /** 技术计算公式 */
    @Excel(name = "技术公式")
    private String technicalFormula;

    /** 计算逻辑说明 */
    @Excel(name = "计算逻辑")
    private String calculationLogic;

    /** 责任部门 */
    @Excel(name = "责任部门")
    @Size(max = 64, message = "责任部门长度不能超过64个字符")
    private String ownerDept;

    /** 所属部门ID(用于行级权限控制) */
    @Excel(name = "所属部门ID")
    private Long deptId;

    /** 数据新鲜度(T-1,RT) */
    @Excel(name = "数据新鲜度")
    @Size(max = 20, message = "数据新鲜度长度不能超过20个字符")
    private String dataFreshness;

    /** 更新频率 */
    @Excel(name = "更新频率", readConverterExp = "实时=RT,每日=Daily,每周=Weekly,每月=Monthly")
    @Size(max = 20, message = "更新频率长度不能超过20个字符")
    private String updateFrequency;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志(0正常 1删除) */
    private String delFlag;

    /** 关联的数据集对象(不持久化到数据库) */
    private transient Dataset dataset;

    /** 关联的可视化对象(不持久化到数据库) */
    private transient Visualization visualization;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setMetricCode(String metricCode) {
        this.metricCode = metricCode;
    }

    public String getMetricCode() {
        return metricCode;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setVisualizationId(Long visualizationId) {
        this.visualizationId = visualizationId;
    }

    public Long getVisualizationId() {
        return visualizationId;
    }

    public void setBusinessDefinition(String businessDefinition) {
        this.businessDefinition = businessDefinition;
    }

    public String getBusinessDefinition() {
        return businessDefinition;
    }

    public void setTechnicalFormula(String technicalFormula) {
        this.technicalFormula = technicalFormula;
    }

    public String getTechnicalFormula() {
        return technicalFormula;
    }

    public void setCalculationLogic(String calculationLogic) {
        this.calculationLogic = calculationLogic;
    }

    public String getCalculationLogic() {
        return calculationLogic;
    }

    public void setOwnerDept(String ownerDept) {
        this.ownerDept = ownerDept;
    }

    public String getOwnerDept() {
        return ownerDept;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDataFreshness(String dataFreshness) {
        this.dataFreshness = dataFreshness;
    }

    public String getDataFreshness() {
        return dataFreshness;
    }

    public void setUpdateFrequency(String updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public String getUpdateFrequency() {
        return updateFrequency;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Visualization getVisualization() {
        return visualization;
    }

    public void setVisualization(Visualization visualization) {
        this.visualization = visualization;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("metricCode", getMetricCode())
                .append("metricName", getMetricName())
                .append("datasetId", getDatasetId())
                .append("visualizationId", getVisualizationId())
                .append("businessDefinition", getBusinessDefinition())
                .append("technicalFormula", getTechnicalFormula())
                .append("calculationLogic", getCalculationLogic())
                .append("ownerDept", getOwnerDept())
                .append("deptId", getDeptId())
                .append("dataFreshness", getDataFreshness())
                .append("updateFrequency", getUpdateFrequency())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }

    /**
     * 判断指标是否正常
     */
    public boolean isNormal() {
        return "0".equals(this.status);
    }

    /**
     * 判断是否为实时数据
     */
    public boolean isRealtime() {
        return "RT".equalsIgnoreCase(this.dataFreshness);
    }

    /**
     * 判断是否为T-1数据
     */
    public boolean isT1() {
        return "T-1".equalsIgnoreCase(this.dataFreshness);
    }
}
