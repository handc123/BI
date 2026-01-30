package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 组件位置DTO
 * 用于批量更新组件位置和大小
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class ComponentPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 组件ID */
    private Long componentId;

    /** X坐标 */
    private Integer positionX;

    /** Y坐标 */
    private Integer positionY;

    /** 宽度 */
    private Integer width;

    /** 高度 */
    private Integer height;

    /** 层级 */
    private Integer zIndex;

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Integer getPositionX() {
        return positionX;
    }

    public void setPositionX(Integer positionX) {
        this.positionX = positionX;
    }

    public Integer getPositionY() {
        return positionY;
    }

    public void setPositionY(Integer positionY) {
        this.positionY = positionY;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getZIndex() {
        return zIndex;
    }

    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }
}
