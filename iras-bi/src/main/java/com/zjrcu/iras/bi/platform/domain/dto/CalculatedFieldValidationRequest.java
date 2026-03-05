package com.zjrcu.iras.bi.platform.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 计算字段验证请求
 *
 * @author iras
 */
public class CalculatedFieldValidationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据集ID
     */
    @NotNull(message = "数据集ID不能为空")
    private Long datasetId;

    /**
     * 计算字段配置
     */
    @NotNull(message = "计算字段配置不能为空")
    private CalculatedFieldDTO field;

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public CalculatedFieldDTO getField() {
        return field;
    }

    public void setField(CalculatedFieldDTO field) {
        this.field = field;
    }
}
