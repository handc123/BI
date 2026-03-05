package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 计算字段验证响应
 *
 * @author iras
 */
public class CalculatedFieldValidationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否有效
     */
    private boolean valid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * SQL预览
     */
    private String sqlPreview;

    public CalculatedFieldValidationResponse() {
    }

    public CalculatedFieldValidationResponse(boolean valid, String message, String sqlPreview) {
        this.valid = valid;
        this.message = message;
        this.sqlPreview = sqlPreview;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSqlPreview() {
        return sqlPreview;
    }

    public void setSqlPreview(String sqlPreview) {
        this.sqlPreview = sqlPreview;
    }
}
