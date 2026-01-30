package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 验证错误数据传输对象
 * 表示单个验证错误的详细信息
 * 
 * @author IRAS
 */
public class ValidationError implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 条件ID (可选,用于标识哪个条件有错误) */
    private String conditionId;

    /** 字段名 (哪个字段有错误) */
    private String field;

    /** 错误消息 */
    private String message;

    public ValidationError() {
    }

    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public ValidationError(String conditionId, String field, String message) {
        this.conditionId = conditionId;
        this.field = field;
        this.message = message;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "conditionId='" + conditionId + '\'' +
                ", field='" + field + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
