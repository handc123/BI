package com.zjrcu.iras.bi.platform.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 条件排序数据传输对象
 * 用于批量更新查询条件的显示顺序
 * 
 * @author IRAS
 */
public class ConditionOrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 条件ID */
    @NotNull(message = "条件ID不能为空")
    private Long conditionId;

    /** 显示顺序 */
    @NotNull(message = "显示顺序不能为空")
    private Integer displayOrder;

    public ConditionOrderDTO() {
    }

    public ConditionOrderDTO(Long conditionId, Integer displayOrder) {
        this.conditionId = conditionId;
        this.displayOrder = displayOrder;
    }

    public Long getConditionId() {
        return conditionId;
    }

    public void setConditionId(Long conditionId) {
        this.conditionId = conditionId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public String toString() {
        return "ConditionOrderDTO{" +
                "conditionId=" + conditionId +
                ", displayOrder=" + displayOrder +
                '}';
    }
}
