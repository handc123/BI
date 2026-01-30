package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.ShareLink;
import com.zjrcu.iras.bi.platform.domain.dto.DashboardConfig;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.service.IDashboardService;
import com.zjrcu.iras.bi.platform.service.IShareLinkService;
import com.zjrcu.iras.bi.platform.service.ShareLinkAccessResult;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 共享链接访问Controller
 *
 * @author iras
 */
@Tag(name = "共享链接访问")
@RestController
@RequestMapping("/bi/share")
public class ShareLinkController extends BaseController {

    @Autowired
    private IShareLinkService shareLinkService;

    @Autowired
    private IDashboardService dashboardService;

    /**
     * 验证共享链接访问权限
     */
    @Operation(description = "验证共享链接访问权限")
    @PostMapping("/{shareCode}/validate")
    public AjaxResult validateAccess(@PathVariable String shareCode, 
                                     @RequestParam(required = false) String password) {
        try {
            ShareLinkAccessResult result = shareLinkService.validateAccess(shareCode, password);
            
            if (result.isAllowed()) {
                return success(Map.of(
                    "valid", true,
                    "resourceType", result.getResourceType(),
                    "resourceId", result.getResourceId()
                ));
            } else {
                return error(result.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("验证共享链接失败", e);
            return error("验证共享链接失败: " + e.getMessage());
        }
    }

    /**
     * 访问共享仪表板
     */
    @Operation(description = "访问共享仪表板")
    @GetMapping("/{shareCode}/dashboard")
    public AjaxResult getSharedDashboard(@PathVariable String shareCode, 
                                        @RequestParam(required = false) String password) {
        try {
            // 验证访问权限
            ShareLinkAccessResult accessResult = shareLinkService.validateAccess(shareCode, password);
            
            if (!accessResult.isAllowed()) {
                return error(accessResult.getErrorMessage());
            }
            
            // 检查资源类型
            if (!"dashboard".equals(accessResult.getResourceType())) {
                return error("共享链接类型不匹配");
            }
            
            // 获取仪表板
            Dashboard dashboard = dashboardService.selectDashboardById(accessResult.getResourceId());
            if (dashboard == null) {
                return error("仪表板不存在");
            }
            
            // 记录访问
            shareLinkService.recordAccess(shareCode);
            
            return success(dashboard);
            
        } catch (Exception e) {
            logger.error("访问共享仪表板失败", e);
            return error("访问共享仪表板失败: " + e.getMessage());
        }
    }

    /**
     * 获取共享仪表板数据
     */
    @Operation(description = "获取共享仪表板数据")
    @PostMapping("/{shareCode}/dashboard/data")
    public AjaxResult getSharedDashboardData(@PathVariable String shareCode, 
                                            @RequestParam(required = false) String password,
                                            @RequestBody(required = false) List<Filter> globalFilters) {
        try {
            // 验证访问权限
            ShareLinkAccessResult accessResult = shareLinkService.validateAccess(shareCode, password);
            
            if (!accessResult.isAllowed()) {
                return error(accessResult.getErrorMessage());
            }
            
            // 检查资源类型
            if (!"dashboard".equals(accessResult.getResourceType())) {
                return error("共享链接类型不匹配");
            }
            
            // 获取仪表板配置
            DashboardConfig config = dashboardService.getDashboardConfig(accessResult.getResourceId());
            
            // 记录访问
            shareLinkService.recordAccess(shareCode);
            
            return success(config);
            
        } catch (Exception e) {
            logger.error("获取共享仪表板数据失败", e);
            return error("获取共享仪表板数据失败: " + e.getMessage());
        }
    }

    /**
     * 撤销共享链接
     */
    @Operation(description = "撤销共享链接")
    @DeleteMapping("/{shareCode}")
    public AjaxResult revokeShareLink(@PathVariable String shareCode) {
        try {
            int result = shareLinkService.revokeShareLink(shareCode);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("撤销共享链接失败", e);
            return error("撤销共享链接失败: " + e.getMessage());
        }
    }
}
