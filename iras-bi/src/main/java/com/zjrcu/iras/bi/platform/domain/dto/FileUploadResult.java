package com.zjrcu.iras.bi.platform.domain.dto;

import java.util.List;

/**
 * 文件上传结果DTO
 *
 * @author iras
 */
public class FileUploadResult {
    
    /**
     * 数据源ID
     */
    private Long dataSourceId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 行数
     */
    private Long rowCount;
    
    /**
     * 列信息
     */
    private List<ColumnInfo> columns;
    
    /**
     * 预览数据(前10行)
     */
    private List<List<Object>> previewData;
    
    /**
     * 上传是否成功
     */
    private boolean success;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 列信息内部类
     */
    public static class ColumnInfo {
        /**
         * 列名
         */
        private String name;
        
        /**
         * 数据类型: STRING, INTEGER, DECIMAL, DATE, BOOLEAN
         */
        private String type;
        
        /**
         * 列索引
         */
        private int index;
        
        public ColumnInfo() {
        }
        
        public ColumnInfo(String name, String type, int index) {
            this.name = name;
            this.type = type;
            this.index = index;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
    }
    
    public static FileUploadResult success(Long dataSourceId, String fileName, Long fileSize, 
                                          Long rowCount, List<ColumnInfo> columns, 
                                          List<List<Object>> previewData) {
        FileUploadResult result = new FileUploadResult();
        result.setSuccess(true);
        result.setDataSourceId(dataSourceId);
        result.setFileName(fileName);
        result.setFileSize(fileSize);
        result.setRowCount(rowCount);
        result.setColumns(columns);
        result.setPreviewData(previewData);
        return result;
    }
    
    public static FileUploadResult error(String message) {
        FileUploadResult result = new FileUploadResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
    
    public Long getDataSourceId() {
        return dataSourceId;
    }
    
    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Long getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }
    
    public List<ColumnInfo> getColumns() {
        return columns;
    }
    
    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }
    
    public List<List<Object>> getPreviewData() {
        return previewData;
    }
    
    public void setPreviewData(List<List<Object>> previewData) {
        this.previewData = previewData;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
