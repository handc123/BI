package com.zjrcu.iras.bi.platform.service.impl;

import com.zjrcu.iras.bi.platform.domain.AuditLog;
import com.zjrcu.iras.bi.platform.mapper.AuditLogMapper;
import com.zjrcu.iras.bi.platform.service.IAuditLogService;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.ip.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * BI审计日志服务实现
 *
 * @author iras
 */
@Service
public class AuditLogServiceImpl implements IAuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Autowired
    private AuditLogMapper auditLogMapper;

    /**
     * 查询审计日志列表
     *
     * @param auditLog 审计日志查询条件
     * @return 审计日志列表
     */
    @Override
    public List<AuditLog> selectAuditLogList(AuditLog auditLog) {
        return auditLogMapper.selectAuditLogList(auditLog);
    }

    /**
     * 根据ID查询审计日志
     *
     * @param id 审计日志ID
     * @return 审计日志
     */
    @Override
    public AuditLog selectAuditLogById(Long id) {
        return auditLogMapper.selectAuditLogById(id);
    }

    /**
     * 记录审计日志（简化版本，自动获取当前用户和IP）
     *
     * @param operation 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param operationDetail 操作详情
     */
    @Override
    @Async
    public void log(String operation, String resourceType, Long resourceId, String operationDetail) {
        try {
            Long userId = null;
            String userName = null;
            String ipAddress = null;

            try {
                userId = SecurityUtils.getUserId();
                userName = SecurityUtils.getUsername();
                ipAddress = IpUtils.getIpAddr();
            } catch (Exception e) {
                // 如果获取用户信息失败（如共享链接访问），使用默认值
                log.debug("无法获取当前用户信息: {}", e.getMessage());
            }

            log(userId, userName, operation, resourceType, resourceId, operationDetail, ipAddress);

        } catch (Exception e) {
            log.error("记录审计日志失败: operation={}, resourceType={}, resourceId={}, error={}",
                    operation, resourceType, resourceId, e.getMessage(), e);
        }
    }

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
    @Override
    @Async
    public void log(Long userId, String userName, String operation, String resourceType,
                   Long resourceId, String operationDetail, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUserName(userName);
            auditLog.setOperation(operation);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setOperationDetail(operationDetail);
            auditLog.setIpAddress(ipAddress);
            auditLog.setCreateTime(new Date());

            auditLogMapper.insertAuditLog(auditLog);

            log.info("审计日志记录成功: userId={}, operation={}, resourceType={}, resourceId={}",
                    userId, operation, resourceType, resourceId);

        } catch (Exception e) {
            log.error("记录审计日志失败: userId={}, operation={}, resourceType={}, resourceId={}, error={}",
                    userId, operation, resourceType, resourceId, e.getMessage(), e);
        }
    }

    /**
     * 批量删除审计日志
     *
     * @param ids 审计日志ID数组
     * @return 结果
     */
    @Override
    public int deleteAuditLogByIds(Long[] ids) {
        return auditLogMapper.deleteAuditLogByIds(ids);
    }

    /**
     * 清理过期审计日志
     *
     * @param days 保留天数
     * @return 删除数量
     */
    @Override
    public int cleanExpiredLogs(int days) {
        int count = auditLogMapper.deleteAuditLogBeforeDays(days);
        log.info("清理过期审计日志完成: days={}, count={}", days, count);
        return count;
    }
}
