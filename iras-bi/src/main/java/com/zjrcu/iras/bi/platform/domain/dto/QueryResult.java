package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询结果DTO
 * 封装查询执行的结果数据和元数据
 *
 * @author iras
 */
public class QueryResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 查询是否成功
     */
    private boolean success;

    /**
     * 错误消息(如果失败)
     */
    private String errorMessage;

    /**
     * 数据行列表
     */
    private List<Map<String, Object>> data;

    /**
     * 字段元数据
     */
    private List<FieldMetadata> fields;

    /**
     * 总行数
     */
    private long totalRows;

    /**
     * 查询执行时间(毫秒)
     */
    private long executionTime;

    /**
     * 是否来自缓存
     */
    private boolean fromCache;

    public QueryResult() {
        this.data = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    /**
     * 创建成功的查询结果
     *
     * @param data 数据行列表
     * @param fields 字段元数据
     * @param executionTime 执行时间
     * @return 查询结果
     */
    public static QueryResult success(List<Map<String, Object>> data, List<FieldMetadata> fields, long executionTime) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setData(data);
        result.setFields(fields);
        result.setTotalRows(data != null ? data.size() : 0);
        result.setExecutionTime(executionTime);
        result.setFromCache(false);
        return result;
    }

    /**
     * 创建失败的查询结果
     *
     * @param errorMessage 错误消息
     * @return 查询结果
     */
    public static QueryResult failure(String errorMessage) {
        QueryResult result = new QueryResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setTotalRows(0);
        return result;
    }

    /**
     * 字段元数据
     */
    public static class FieldMetadata implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 字段名称
         */
        private String name;

        /**
         * 字段别名(显示名称)
         */
        private String alias;

        /**
         * 字段类型
         */
        private String type;

        /**
         * 是否为计算字段
         */
        private boolean calculated;

        /**
         * 字段分类(dimension-维度, metric-指标)
         */
        private String fieldType;

        public FieldMetadata() {
        }

        public FieldMetadata(String name, String alias, String type, boolean calculated) {
            this.name = name;
            this.alias = alias;
            this.type = type;
            this.calculated = calculated;
        }

        public FieldMetadata(String name, String alias, String type, boolean calculated, String fieldType) {
            this.name = name;
            this.alias = alias;
            this.type = type;
            this.calculated = calculated;
            this.fieldType = fieldType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isCalculated() {
            return calculated;
        }

        public void setCalculated(boolean calculated) {
            this.calculated = calculated;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 获取消息（getErrorMessage的别名，用于兼容性）
     *
     * @return 错误消息
     */
    public String getMessage() {
        return errorMessage;
    }

    /**
     * 设置消息（setErrorMessage的别名，用于兼容性）
     *
     * @param message 错误消息
     */
    public void setMessage(String message) {
        this.errorMessage = message;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public List<FieldMetadata> getFields() {
        return fields;
    }

    public void setFields(List<FieldMetadata> fields) {
        this.fields = fields;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
}
