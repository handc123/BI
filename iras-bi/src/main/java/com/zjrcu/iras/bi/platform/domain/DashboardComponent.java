package com.zjrcu.iras.bi.platform.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * 仪表板组件对象 bi_dashboard_component
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class DashboardComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 组件ID */
    private Long id;

    /** 仪表板ID */
    private Long dashboardId;

    /** 组件类型(chart/query/text/media/tabs) */
    private String componentType;

    /** 组件名称 */
    private String componentName;

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

    /** 数据配置(JSON) */
    private String dataConfig;

    /** 样式配置(JSON) */
    private String styleConfig;

    /** 高级配置(JSON) */
    private String advancedConfig;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDashboardId(Long dashboardId) {
        this.dashboardId = dashboardId;
    }

    public Long getDashboardId() {
        return dashboardId;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setPositionX(Integer positionX) {
        this.positionX = positionX;
    }

    public Integer getPositionX() {
        return positionX;
    }

    public void setPositionY(Integer positionY) {
        this.positionY = positionY;
    }

    public Integer getPositionY() {
        return positionY;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getWidth() {
        return width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getHeight() {
        return height;
    }

    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }

    public Integer getZIndex() {
        return zIndex;
    }

    public void setDataConfig(String dataConfig) {
        this.dataConfig = dataConfig;
    }

    public String getDataConfig() {
        return dataConfig;
    }

    public void setStyleConfig(String styleConfig) {
        this.styleConfig = styleConfig;
    }

    public String getStyleConfig() {
        return styleConfig;
    }

    public void setAdvancedConfig(String advancedConfig) {
        this.advancedConfig = advancedConfig;
    }

    public String getAdvancedConfig() {
        return advancedConfig;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("dashboardId", getDashboardId())
            .append("componentType", getComponentType())
            .append("componentName", getComponentName())
            .append("positionX", getPositionX())
            .append("positionY", getPositionY())
            .append("width", getWidth())
            .append("height", getHeight())
            .append("zIndex", getZIndex())
            .append("dataConfig", getDataConfig())
            .append("styleConfig", getStyleConfig())
            .append("advancedConfig", getAdvancedConfig())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
