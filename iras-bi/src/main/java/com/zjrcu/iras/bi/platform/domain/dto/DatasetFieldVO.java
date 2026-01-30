package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 数据集字段视图对象
 * 用于返回数据集的字段信息
 * 
 * @author IRAS
 */
public class DatasetFieldVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 表名 */
    private String tableName;

    /** 字段名 */
    private String fieldName;

    /** 字段类型 (VARCHAR, INT, DATE, DATETIME, DECIMAL等) */
    private String fieldType;

    /** 字段注释/描述 */
    private String fieldComment;

    /** 是否为主键 */
    private Boolean isPrimaryKey;

    /** 是否可为空 */
    private Boolean isNullable;

    public DatasetFieldVO() {
    }

    public DatasetFieldVO(String tableName, String fieldName, String fieldType) {
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldComment() {
        return fieldComment;
    }

    public void setFieldComment(String fieldComment) {
        this.fieldComment = fieldComment;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public Boolean getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }

    @Override
    public String toString() {
        return "DatasetFieldVO{" +
                "tableName='" + tableName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", fieldComment='" + fieldComment + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isNullable=" + isNullable +
                '}';
    }
}
