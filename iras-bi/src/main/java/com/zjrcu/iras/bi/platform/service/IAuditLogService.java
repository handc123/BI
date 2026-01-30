package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.AuditLog;

import java.util.List;

/**
 * BI审计日志服务接口
 *
 * @author iras
 */
public interface IAuditLogService {

    /**
     * 查询审计日志列表
     *
     * @param auditLog 审计日志查询条件
     * @return 审计日志列表
     */
    List<AuditLog> selectAuditLogList(AuditLog auditLog);

    /**
     * 根据ID查询审计日志
     *
     * @param id 审计日志ID
     * @return 审计日志
     */
    AuditLog selectAuditLogById(Long id);

    /**
     * 记录审计日志
     *
     * @param operation 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param operationDetail 操作详情
     */
    void log(String operation, String resourceType, Long resourceId, String operationDetail);

    /**
     * 记录审计日志（完整参数）
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param operation 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param operationDetail 操作详情
     * @param ipAddress IP地址
     */
    void log(Long userId, String userName, String operation, String resourceType, 
            Long resourceId, String operationDetail, String ipAddress);

    /**
     * 批量删除审计日志
     *
     * @param ids 审计日志ID数组
     * @return 结果
     */
    int deleteAuditLogByIds(Long[] ids);

    /**
     * 清理过期审计日志
     *
     * @param days 保留天数
     * @return 删除数量
     */
    int cleanExpiredLogs(int days);
}
