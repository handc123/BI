package com.zjrcu.iras.bi.metric.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;

import java.util.Date;
import java.util.Map;

/**
 * 指标数据视图对象
 * 用于返回指标数据查询结果
 *
 * @author iras
 * @date 2025-02-26
 */
public class MetricDataVO {

    /** 指标ID */
    private Long metricId;

    /** 指标编码 */
    @Excel(name = "指标编码")
    private String metricCode;

    /** 指标名称 */
    @Excel(name = "指标名称")
    private String metricName;

    /** 数据值 */
    @Excel(name = "数据值")
    private Object value;

    /** 数据时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "数据时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date dataTime;

    /** 维度信息（JSON格式） */
    private Map<String, Object> dimensions;

    /** 数据来源 */
    @Excel(name = "数据来源")
    private String dataSource;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 扩展字段（用于存储其他自定义字段） */
    private Map<String, Object> extFields;

    public MetricDataVO() {
    }

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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Date getDataTime() {
        return dataTime;
    }

    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }

    public Map<String, Object> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, Object> dimensions) {
        this.dimensions = dimensions;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Map<String, Object> getExtFields() {
        return extFields;
    }

    public void setExtFields(Map<String, Object> extFields) {
        this.extFields = extFields;
    }

    /**
     * 添加扩展字段
     */
    public void addExtField(String key, Object value) {
        if (this.extFields == null) {
            this.extFields = new java.util.HashMap<>();
        }
        this.extFields.put(key, value);
    }

    /**
     * 获取扩展字段值
     */
    public Object getExtFieldValue(String key) {
        if (this.extFields == null) {
            return null;
        }
        return this.extFields.get(key);
    }
}
