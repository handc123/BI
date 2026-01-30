package com.zjrcu.iras.bi.platform.domain.dto;

import java.util.List;
import java.util.Map;

/**
 * 表数据预览
 *
 * @author iras
 */
public class TablePreviewData {
    
    /**
     * 字段信息
     */
    private List<ColumnMeta> columns;
    
    /**
     * 数据行
     */
    private List<Map<String, Object>> rows;
    
    /**
     * 总行数
     */
    private Long total;
    
    public TablePreviewData() {
    }
    
    public TablePreviewData(List<ColumnMeta> columns, List<Map<String, Object>> rows, Long total) {
        this.columns = columns;
        this.rows = rows;
        this.total = total;
    }
    
    public List<ColumnMeta> getColumns() {
        return columns;
    }
    
    public void setColumns(List<ColumnMeta> columns) {
        this.columns = columns;
    }
    
    public List<Map<String, Object>> getRows() {
        return rows;
    }
    
    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public void setTotal(Long total) {
        this.total = total;
    }
    
    /**
     * 字段元数据
     */
    public static class ColumnMeta {
        /**
         * 字段名
         */
        private String name;
        
        /**
         * 字段注释（中文名）
         */
        private String comment;
        
        /**
         * 字段类型
         */
        private String type;
        
        /**
         * 字段分类：DIMENSION 或 MEASURE
         */
        private String fieldType;
        
        public ColumnMeta() {
        }
        
        public ColumnMeta(String name, String comment, String type, String fieldType) {
            this.name = name;
            this.comment = comment;
            this.type = type;
            this.fieldType = fieldType;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String comment) {
            this.comment = comment;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getFieldType() {
            return fieldType;
        }
        
        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }
    }
}
