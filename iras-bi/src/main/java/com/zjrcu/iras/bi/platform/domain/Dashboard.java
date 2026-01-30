package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 仪表板对象 bi_dashboard
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class Dashboard extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 仪表板ID */
    private Long id;

    /** 仪表板名称 */
    private String dashboardName;

    /** 仪表板描述 */
    private String dashboardDesc;

    /** 主题(light/dark) */
    private String theme;

    /** 画布配置(JSON) */
    private String canvasConfig;

    /** 全局样式配置(JSON) */
    private String globalStyle;

    /** 状态(0草稿 1已发布) */
    private String status;

    /** 已发布版本号 */
    private Integer publishedVersion;

    /** 删除标志(0正常 1删除) */
    private String delFlag;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
    }

    public String getDashboardName() {
        return dashboardName;
    }

    public void setDashboardDesc(String dashboardDesc) {
        this.dashboardDesc = dashboardDesc;
    }

    public String getDashboardDesc() {
        return dashboardDesc;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getTheme() {
        return theme;
    }

    public void setCanvasConfig(String canvasConfig) {
        this.canvasConfig = canvasConfig;
    }

    public String getCanvasConfig() {
        return canvasConfig;
    }

    public void setGlobalStyle(String globalStyle) {
        this.globalStyle = globalStyle;
    }

    public String getGlobalStyle() {
        return globalStyle;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setPublishedVersion(Integer publishedVersion) {
        this.publishedVersion = publishedVersion;
    }

    public Integer getPublishedVersion() {
        return publishedVersion;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("dashboardName", getDashboardName())
            .append("dashboardDesc", getDashboardDesc())
            .append("theme", getTheme())
            .append("canvasConfig", getCanvasConfig())
            .append("globalStyle", getGlobalStyle())
            .append("status", getStatus())
            .append("publishedVersion", getPublishedVersion())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
