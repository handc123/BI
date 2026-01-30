package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 共享链接对象
 *
 * @author iras
 */
public class ShareLink extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 共享链接ID
     */
    private Long id;

    /**
     * 共享码（唯一标识）
     */
    private String shareCode;

    /**
     * 资源类型: dashboard, visualization
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 访问密码（可选，加密存储）
     */
    private String password;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 访问次数限制（0表示无限制）
     */
    private Integer accessLimit;

    /**
     * 已访问次数
     */
    private Integer accessCount;

    /**
     * 状态: 0正常 1停用
     */
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getAccessLimit() {
        return accessLimit;
    }

    public void setAccessLimit(Integer accessLimit) {
        this.accessLimit = accessLimit;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 判断链接是否已过期
     *
     * @return true表示已过期
     */
    public boolean isExpired() {
        return expireTime != null && expireTime.before(new Date());
    }

    /**
     * 判断是否达到访问次数限制
     *
     * @return true表示已达到限制
     */
    public boolean isAccessLimitReached() {
        return accessLimit != null && accessLimit > 0 && accessCount >= accessLimit;
    }

    /**
     * 判断链接是否可用
     *
     * @return true表示可用
     */
    public boolean isAvailable() {
        return "0".equals(status) && !isExpired() && !isAccessLimitReached();
    }
}
