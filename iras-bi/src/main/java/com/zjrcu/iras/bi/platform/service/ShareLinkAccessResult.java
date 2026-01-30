package com.zjrcu.iras.bi.platform.service;

import java.io.Serializable;

/**
 * 共享链接访问结果
 *
 * @author iras
 */
public class ShareLinkAccessResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否允许访问
     */
    private boolean allowed;

    /**
     * 错误消息（如果不允许访问）
     */
    private String errorMessage;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private Long resourceId;

    public ShareLinkAccessResult() {
    }

    public ShareLinkAccessResult(boolean allowed, String errorMessage) {
        this.allowed = allowed;
        this.errorMessage = errorMessage;
    }

    public static ShareLinkAccessResult allowed(String resourceType, Long resourceId) {
        ShareLinkAccessResult result = new ShareLinkAccessResult(true, null);
        result.setResourceType(resourceType);
        result.setResourceId(resourceId);
        return result;
    }

    public static ShareLinkAccessResult denied(String errorMessage) {
        return new ShareLinkAccessResult(false, errorMessage);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "ShareLinkAccessResult{" +
                "allowed=" + allowed +
                ", errorMessage='" + errorMessage + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                '}';
    }
}
