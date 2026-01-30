package com.zjrcu.iras.bi.platform.domain.dto;

import java.util.List;

/**
 * 表结构信息
 *
 * @author iras
 */
public class TableSchemaInfo {
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表注释
     */
    private String tableComment;
    
    /**
     * 字段列表
     */
    private List<ColumnInfo> columns;
    
    public TableSchemaInfo() {
    }
    
    public TableSchemaInfo(String tableName, String tableComment, List<ColumnInfo> columns) {
        this.tableName = tableName;
        this.tableComment = tableComment;
        this.columns = columns;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableComment() {
        return tableComment;
    }
    
    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }
    
    public List<ColumnInfo> getColumns() {
        return columns;
    }
    
    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }
    
    /**
     * 字段信息
     */
    public static class ColumnInfo {
        /**
         * 字段名
         */
        private String columnName;
        
        /**
         * 字段注释
         */
        private String columnComment;
        
        /**
         * 数据类型
         */
        private String dataType;
        
        /**
         * 是否可为空
         */
        private Boolean nullable;
        
        /**
         * 默认值
         */
        private String defaultValue;
        
        /**
         * 是否主键
         */
        private Boolean isPrimaryKey;
        
        /**
         * 字段类型：DIMENSION(维度) 或 MEASURE(指标)
         */
        private String fieldType;
        
        /**
         * 字段长度
         */
        private Integer columnSize;
        
        /**
         * 小数位数
         */
        private Integer decimalDigits;
        
        public ColumnInfo() {
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        
        public String getColumnComment() {
            return columnComment;
        }
        
        public void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }
        
        public String getDataType() {
            return dataType;
        }
        
        public void setDataType(String dataType) {
            this.dataType = dataType;
        }
        
        public Boolean getNullable() {
            return nullable;
        }
        
        public void setNullable(Boolean nullable) {
            this.nullable = nullable;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
        
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
        
        public Boolean getIsPrimaryKey() {
            return isPrimaryKey;
        }
        
        public void setIsPrimaryKey(Boolean isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
        }
        
        public String getFieldType() {
            return fieldType;
        }
        
        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }
        
        public Integer getColumnSize() {
            return columnSize;
        }
        
        public void setColumnSize(Integer columnSize) {
            this.columnSize = columnSize;
        }
        
        public Integer getDecimalDigits() {
            return decimalDigits;
        }
        
        public void setDecimalDigits(Integer decimalDigits) {
            this.decimalDigits = decimalDigits;
        }
    }
}
