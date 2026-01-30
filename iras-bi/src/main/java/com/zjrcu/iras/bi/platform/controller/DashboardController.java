package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.dto.DashboardConfig;
import com.zjrcu.iras.bi.platform.service.IDashboardService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仪表板Controller
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@RestController
@RequestMapping("/bi/dashboard")
public class DashboardController extends BaseController {
    @Autowired
    private IDashboardService dashboardService;

    /**
     * 查询仪表板列表
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:list')")
    @GetMapping("/list")
    public TableDataInfo list(Dashboard dashboard) {
        startPage();
        List<Dashboard> list = dashboardService.selectDashboardList(dashboard);
        return getDataTable(list);
    }

    /**
     * 获取仪表板详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(dashboardService.selectDashboardById(id));
    }

    /**
     * 新增仪表板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:add')")
    @Log(title = "仪表板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Dashboard dashboard) {
        int result = dashboardService.insertDashboard(dashboard);
        if (result > 0) {
            // 返回创建的仪表板对象，包含生成的ID
            return AjaxResult.success(dashboard);
        }
        return AjaxResult.error("新增仪表板失败");
    }

    /**
     * 修改仪表板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "仪表板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Dashboard dashboard) {
        return toAjax(dashboardService.updateDashboard(dashboard));
    }

    /**
     * 删除仪表板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:remove')")
    @Log(title = "仪表板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(dashboardService.deleteDashboardByIds(ids));
    }

    /**
     * 获取仪表板配置(包含所有组件)
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/{id}/config")
    public AjaxResult getConfig(@PathVariable("id") Long id) {
        return success(dashboardService.getDashboardConfig(id));
    }

    /**
     * 保存仪表板配置
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "仪表板配置", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/config")
    public AjaxResult saveConfig(@PathVariable("id") Long id, @RequestBody DashboardConfig config) {
        return toAjax(dashboardService.saveDashboardConfig(id, config));
    }

    /**
     * 发布仪表板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:publish')")
    @Log(title = "仪表板发布", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/publish")
    public AjaxResult publish(@PathVariable("id") Long id) {
        return toAjax(dashboardService.publishDashboard(id));
    }

    /**
     * 取消发布仪表板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:publish')")
    @Log(title = "取消发布仪表板", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/unpublish")
    public AjaxResult unpublish(@PathVariable("id") Long id) {
        return toAjax(dashboardService.unpublishDashboard(id));
    }

    /**
     * 从已发布仪表板创建草稿副本
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "创建仪表板草稿", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/draft")
    public AjaxResult createDraft(@PathVariable("id") Long id) {
        Long draftId = dashboardService.createDraftFromPublished(id);
        return success(draftId);
    }
}

