package com.zjrcu.iras.bi.platform.domain.dto;

import java.util.List;
import java.util.Map;

/**
 * 图表查询请求
 * 
 * @author zjrcu
 * @date 2026-01-21
 */
public class ChartQueryRequest {
    
    /** 数据集ID */
    private Long datasetId;
    
    /** 维度配置列表 */
    private List<DimensionConfig> dimensions;
    
    /** 指标配置列表 */
    private List<MetricConfig> metrics;
    
    /** 过滤条件列表 */
    private List<Filter> filters;
    
    /** 结果限制 */
    private Integer limit;
    
    /** 查询参数(来自查询条件) */
    private Map<String, Object> params;
    
    /**
     * 维度配置
     */
    public static class DimensionConfig {
        /** 字段名 */
        private String field;
        
        /** 轴类型: x(类别轴), series(子类别) */
        private String axis;
        
        /** 显示标签 */
        private String label;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getAxis() {
            return axis;
        }

        public void setAxis(String axis) {
            this.axis = axis;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
    
    /**
     * 指标配置
     */
    public static class MetricConfig {
        /** 字段名 */
        private String field;
        
        /** 聚合方法: sum, avg, max, min, count, count_distinct */
        private String aggregation;
        
        /** 显示标签 */
        private String label;
        
        /** 轴类型: left(左轴), right(右轴) */
        private String axis;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getAggregation() {
            return aggregation;
        }

        public void setAggregation(String aggregation) {
            this.aggregation = aggregation;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getAxis() {
            return axis;
        }

        public void setAxis(String axis) {
            this.axis = axis;
        }
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public List<DimensionConfig> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<DimensionConfig> dimensions) {
        this.dimensions = dimensions;
    }

    public List<MetricConfig> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricConfig> metrics) {
        this.metrics = metrics;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
