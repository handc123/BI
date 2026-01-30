package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.cache.CacheManager;
import com.zjrcu.iras.bi.platform.cache.CacheStats;
import com.zjrcu.iras.bi.platform.monitor.QueryPerformanceMonitor;
import com.zjrcu.iras.bi.platform.monitor.QueryStatsSnapshot;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * BI监控管理Controller
 *
 * @author iras
 */
@Tag(name = "BI监控管理")
@RestController
@RequestMapping("/bi/monitor")
public class MonitorController extends BaseController {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired(required = false)
    private QueryPerformanceMonitor performanceMonitor;

    /**
     * 获取缓存统计信息
     */
    @PreAuthorize("@ss.hasPermi('bi:monitor:query')")
    @Operation(description = "获取缓存统计信息")
    @GetMapping("/cache/stats")
    public AjaxResult getCacheStats() {
        if (cacheManager == null) {
            return error("缓存管理器未配置");
        }

        try {
            CacheStats stats = cacheManager.getStats();
            return success(stats);
        } catch (Exception e) {
            logger.error("获取缓存统计信息失败", e);
            return error("获取缓存统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有BI缓存
     */
    @PreAuthorize("@ss.hasPermi('bi:monitor:clear')")
    @Operation(description = "清空所有BI缓存")
    @Log(title = "缓存管理", businessType = BusinessType.CLEAN)
    @PostMapping("/cache/clear")
    public AjaxResult clearCache() {
        if (cacheManager == null) {
            return error("缓存管理器未配置");
        }

        try {
            cacheManager.clear();
            return success("缓存已清空");
        } catch (Exception e) {
            logger.error("清空缓存失败", e);
            return error("清空缓存失败: " + e.getMessage());
        }
    }

    /**
     * 使指定数据集缓存失效
     */
    @PreAuthorize("@ss.hasPermi('bi:monitor:clear')")
    @Operation(description = "使指定数据集缓存失效")
    @Log(title = "缓存管理", businessType = BusinessType.CLEAN)
    @PostMapping("/cache/invalidate/{datasetId}")
    public AjaxResult invalidateCache(@PathVariable Long datasetId) {
        if (cacheManager == null) {
            return error("缓存管理器未配置");
        }

        try {
            cacheManager.invalidate(datasetId);
            return success("数据集缓存已失效");
        } catch (Exception e) {
            logger.error("使缓存失效失败", e);
            return error("使缓存失效失败: " + e.getMessage());
        }
    }

    /**
     * 获取全局查询性能统计
     */
    @PreAuthorize("@ss.hasPermi('bi:monitor:query')")
    @Operation(description = "获取全局查询性能统计")
    @GetMapping("/performance/global")
    public AjaxResult getGlobalPerformanceStats() {
        if (performanceMonitor == null) {
            return error("性能监控器未配置");
        }

        try {
            QueryStatsSnapshot stats = performanceMonitor.getGlobalStats();
            return success(stats);
        } catch (Exception e) {
            logger.error("获取全局性能统计失败", e);
            return error("获取全局性能统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据集查询性能统计
     */
    @PreAuthorize("@ss.hasPermi('bi:monitor:query')")
    @Operation(description = "获取数据集查询性能统计")
    @GetMapping("/performance/dataset/{datasetId}")
    public AjaxResult getDatasetPerformanceStats(@PathVariable Long datasetId) {
        if (performanceMonitor == null) {
            return error("性能监控器未配置");
        }

        try {
            QueryStatsSnapshot stats = performanceMonitor.getDatasetStats(datasetId);
            return success(stats);
        } catch (Exception e) {
            logger.error("获取数据集性能统计失败", e);
            return error("获取数据集性能统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置全局性能统计
     */
    @PreAuthorize("@ss.hasPermi('bi:monitor:clear')")
    @Operation(description = "重置全局性能统计")
    @Log(title = "性能监控", businessType = BusinessType.CLEAN)
    @PostMapping("/performance/reset")
    public AjaxResult resetGlobalPerformanceStats() {
        if (performanceMonitor == null) {
            return error("性能监控器未配置");
        }

        try {
            performanceMonitor.resetGlobalStats();
            return success("全局性能统计已重置");
        } catch (Exception e) {
            logger.error("重置全局性能统计失败", e);
            return error("重置全局性能统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置数据集性能统计
     */
    @PreAuthorize("@ss.hasPermi('bi:monitor:clear')")
    @Operation(description = "重置数据集性能统计")
    @Log(title = "性能监控", businessType = BusinessType.CLEAN)
    @PostMapping("/performance/reset/{datasetId}")
    public AjaxResult resetDatasetPerformanceStats(@PathVariable Long datasetId) {
        if (performanceMonitor == null) {
            return error("性能监控器未配置");
        }

        try {
            performanceMonitor.resetDatasetStats(datasetId);
            return success("数据集性能统计已重置");
        } catch (Exception e) {
            logger.error("重置数据集性能统计失败", e);
            return error("重置数据集性能统计失败: " + e.getMessage());
        }
    }
}
