package com.zjrcu.iras.bi.platform.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * BI审计日志对象 bi_audit_log
 *
 * @author iras
 */
public class AuditLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 操作类型 */
    private String operation;

    /** 资源类型: datasource, dataset, dashboard, visualization */
    private String resourceType;

    /** 资源ID */
    private Long resourceId;

    /** 操作详情(JSON格式) */
    private String operationDetail;

    /** IP地址 */
    private String ipAddress;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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

    public String getOperationDetail() {
        return operationDetail;
    }

    public void setOperationDetail(String operationDetail) {
        this.operationDetail = operationDetail;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", operation='" + operation + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", operationDetail='" + operationDetail + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
