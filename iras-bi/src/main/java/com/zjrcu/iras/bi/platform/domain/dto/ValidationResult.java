package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果数据传输对象
 * 用于返回配置验证的结果
 * 
 * @author IRAS
 */
public class ValidationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否有效 */
    private boolean valid;

    /** 错误列表 */
    private List<ValidationError> errors;

    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }

    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
    }

    public ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    /**
     * 添加验证错误
     * 
     * @param error 验证错误
     */
    public void addError(ValidationError error) {
        this.valid = false;
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    /**
     * 添加验证错误
     * 
     * @param conditionId 条件ID
     * @param field 字段名
     * @param message 错误消息
     */
    public void addError(String conditionId, String field, String message) {
        addError(new ValidationError(conditionId, field, message));
    }

    /**
     * 检查是否有错误
     * 
     * @return true如果有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}
