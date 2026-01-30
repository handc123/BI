package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.AuditLog;
import com.zjrcu.iras.bi.platform.service.IAuditLogService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BI审计日志Controller
 *
 * @author iras
 */
@Tag(name = "BI审计日志管理")
@RestController
@RequestMapping("/bi/audit")
public class AuditLogController extends BaseController {

    @Autowired
    private IAuditLogService auditLogService;

    /**
     * 查询审计日志列表
     */
    @PreAuthorize("@ss.hasPermi('bi:audit:list')")
    @Operation(description = "查询审计日志列表")
    @GetMapping("/list")
    public TableDataInfo list(AuditLog auditLog) {
        startPage();
        List<AuditLog> list = auditLogService.selectAuditLogList(auditLog);
        return getDataTable(list);
    }

    /**
     * 获取审计日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:audit:query')")
    @Operation(description = "获取审计日志详细信息")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        AuditLog auditLog = auditLogService.selectAuditLogById(id);
        if (auditLog == null) {
            return error("审计日志不存在");
        }
        return success(auditLog);
    }

    /**
     * 删除审计日志
     */
    @PreAuthorize("@ss.hasPermi('bi:audit:remove')")
    @Operation(description = "删除审计日志")
    @Log(title = "审计日志管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            return toAjax(auditLogService.deleteAuditLogByIds(ids));
        } catch (Exception e) {
            logger.error("删除审计日志失败", e);
            return error("删除审计日志失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期审计日志
     */
    @PreAuthorize("@ss.hasPermi('bi:audit:clean')")
    @Operation(description = "清理过期审计日志")
    @Log(title = "审计日志清理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean/{days}")
    public AjaxResult clean(@PathVariable int days) {
        try {
            if (days < 30) {
                return error("保留天数不能少于30天");
            }
            int count = auditLogService.cleanExpiredLogs(days);
            return success("清理完成，共删除" + count + "条记录");
        } catch (Exception e) {
            logger.error("清理审计日志失败", e);
            return error("清理审计日志失败: " + e.getMessage());
        }
    }
}
