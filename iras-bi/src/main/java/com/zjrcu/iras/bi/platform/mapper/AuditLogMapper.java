package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.AuditLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * BI审计日志Mapper接口
 *
 * @author iras
 */
@Mapper
public interface AuditLogMapper {

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
     * 新增审计日志
     *
     * @param auditLog 审计日志
     * @return 结果
     */
    int insertAuditLog(AuditLog auditLog);

    /**
     * 批量删除审计日志
     *
     * @param ids 审计日志ID数组
     * @return 结果
     */
    int deleteAuditLogByIds(Long[] ids);

    /**
     * 删除指定时间之前的审计日志
     *
     * @param days 保留天数
     * @return 删除数量
     */
    int deleteAuditLogBeforeDays(int days);
}
