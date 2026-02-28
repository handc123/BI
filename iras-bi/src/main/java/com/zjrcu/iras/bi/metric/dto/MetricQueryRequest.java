package com.zjrcu.iras.bi.metric.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指标数据查询请求对象
 *
 * @author iras
 * @date 2025-02-26
 */
public class MetricQueryRequest {

    /** 页码 */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    /** 每页数量 */
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 10000, message = "每页数量最大为10000")
    private Integer pageSize;

    /** 排序字段 */
    private String sortField;

    /** 排序方向: ASC, DESC */
    private String sortOrder;

    /** 过滤条件列表 */
    private List<Filter> filters;

    /** 查询参数（用于替换SQL模板中的占位符） */
    private Map<String, Object> parameters;

    /** 是否需要导出 */
    private Boolean export;

    /** 导出格式: excel, csv */
    private String exportFormat;

    /** 时间范围 - 开始时间 */
    private String startTime;

    /** 时间范围 - 结束时间 */
    private String endTime;

    /** 分组字段（用于聚合查询） */
    private String[] groupByFields;

    public MetricQueryRequest() {
        this.pageNum = 1;
        this.pageSize = 20;
        this.sortOrder = "DESC";
        this.filters = new ArrayList<>();
        this.parameters = new HashMap<>();
        this.export = false;
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

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Boolean getExport() {
        return export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String[] getGroupByFields() {
        return groupByFields;
    }

    public void setGroupByFields(String[] groupByFields) {
        this.groupByFields = groupByFields;
    }

    /**
     * 添加过滤条件
     */
    public void addFilter(String field, String operator, Object value) {
        if (this.filters == null) {
            this.filters = new ArrayList<>();
        }
        Filter filter = new Filter();
        filter.setField(field);
        filter.setOperator(operator);
        filter.setValue(value);
        this.filters.add(filter);
    }

    /**
     * 添加查询参数
     */
    public void addParameter(String key, Object value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
    }

    /**
     * 过滤条件内部类
     */
    public static class Filter {
        /** 字段名 */
        private String field;

        /** 操作符: eq, ne, gt, gte, lt, lte, like, in, between */
        private String operator;

        /** 过滤值 */
        private Object value;

        /** 逻辑连接符: AND, OR */
        private String logic;

        public Filter() {
            this.logic = "AND";
        }

        public Filter(String field, String operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.logic = "AND";
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getLogic() {
            return logic;
        }

        public void setLogic(String logic) {
            this.logic = logic;
        }
    }
}
