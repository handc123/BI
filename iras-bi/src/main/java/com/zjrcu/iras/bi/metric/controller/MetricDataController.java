package com.zjrcu.iras.bi.metric.controller;


import com.zjrcu.iras.bi.metric.dto.MetricDataVO;
import com.zjrcu.iras.bi.metric.dto.MetricQueryRequest;
import com.zjrcu.iras.bi.metric.service.IMetricDataService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import com.zjrcu.iras.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 指标数据查询Controller
 *
 * @author iras
 * @date 2025-02-26
 */
@Tag(name = "指标数据查询")
@RestController
@RequestMapping("/bi/metric/data")
public class MetricDataController extends BaseController {

    @Autowired
    private IMetricDataService metricDataService;

    /**
     * 查询指标数据（分页）
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:query')")
    @Operation(description = "查询指标数据")
    @PostMapping("/{metricId}/query")
    public TableDataInfo query(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Validated @RequestBody MetricQueryRequest request) {
        try {
            startPage();
            TableDataInfo result = metricDataService.queryMetricData(metricId, request);
            return result;
        } catch (Exception e) {
            logger.error("查询指标数据失败, metricId: {}", metricId, e);
            // 返回空数据列表
            return getDataTable(List.of());
        }
    }

    /**
     * 导出指标数据（Excel）
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:export')")
    @Operation(description = "导出指标数据")
    @Log(title = "指标数据", businessType = BusinessType.EXPORT)
    @PostMapping("/{metricId}/export")
    public void export(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Validated @RequestBody MetricQueryRequest request,
            HttpServletResponse response) {
        try {
            // 直接调用exportMetricData方法
            metricDataService.exportMetricData(metricId, request, response);
        } catch (Exception e) {
            logger.error("导出指标数据失败, metricId: {}", metricId, e);
        }
    }

    /**
     * 获取指标数据概览
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:query')")
    @Operation(description = "获取指标数据概览")
    @GetMapping("/{metricId}/overview")
    public AjaxResult getOverview(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Parameter(description = "时间范围") @RequestParam(required = false) String timeRange) {
        try {
            Map<String, Object> overview = metricDataService.getMetricDataOverview(metricId, timeRange);
            if (overview == null) {
                return error("指标数据概览不存在");
            }
            return success(overview);
        } catch (Exception e) {
            logger.error("获取指标数据概览失败, metricId: {}, timeRange: {}", metricId, timeRange, e);
            return error("获取指标数据概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标实时数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:query')")
    @Operation(description = "获取指标实时数据")
    @GetMapping("/{metricId}/realtime")
    public AjaxResult getRealtimeData(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Parameter(description = "数据点数量") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> dataList = metricDataService.getRealtimeMetricData(metricId, limit);
            return success(dataList);
        } catch (Exception e) {
            logger.error("获取指标实时数据失败, metricId: {}, limit: {}", metricId, limit, e);
            return error("获取指标实时数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标聚合数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:query')")
    @Operation(description = "获取指标聚合数据")
    @PostMapping("/{metricId}/aggregate")
    public AjaxResult getAggregateData(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @RequestBody MetricQueryRequest request) {
        try {
            Map<String, Object> aggregateData = metricDataService.getAggregateMetricData(metricId, request);
            return success(aggregateData);
        } catch (Exception e) {
            logger.error("获取指标聚合数据失败, metricId: {}", metricId, e);
            return error("获取指标聚合数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标趋势数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:query')")
    @Operation(description = "获取指标趋势数据")
    @GetMapping("/{metricId}/trend")
    public AjaxResult getTrendData(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Parameter(description = "时间粒度: hour/day/week/month") @RequestParam(defaultValue = "day") String granularity,
            @Parameter(description = "数据点数量") @RequestParam(defaultValue = "30") Integer points) {
        try {
            List<Map<String, Object>> trendData = metricDataService.getMetricTrendData(metricId, granularity, points);
            return success(trendData);
        } catch (Exception e) {
            logger.error("获取指标趋势数据失败, metricId: {}, granularity: {}", metricId, granularity, e);
            return error("获取指标趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标对比数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:query')")
    @Operation(description = "获取指标对比数据")
    @PostMapping("/{metricId}/compare")
    public AjaxResult getCompareData(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> compareMetricIds = (List<Long>) params.get("compareMetricIds");

            if (compareMetricIds == null || compareMetricIds.isEmpty()) {
                return error("请选择要对比的指标");
            }

            Map<String, Object> compareData = metricDataService.getMetricCompareData(metricId, compareMetricIds, params);
            return success(compareData);
        } catch (Exception e) {
            logger.error("获取指标对比数据失败, metricId: {}", metricId, e);
            return error("获取指标对比数据失败: " + e.getMessage());
        }
    }

    /**
     * 刷新指标数据缓存
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:data:refresh')")
    @Operation(description = "刷新指标数据缓存")
    @Log(title = "指标数据刷新", businessType = BusinessType.OTHER)
    @PostMapping("/{metricId}/refresh")
    public AjaxResult refresh(@Parameter(description = "指标ID") @PathVariable Long metricId) {
        try {
            boolean success = metricDataService.refreshMetricData(metricId);
            return toAjax(success);
        } catch (Exception e) {
            logger.error("刷新指标数据缓存失败, metricId: {}", metricId, e);
            return error("刷新指标数据缓存失败: " + e.getMessage());
        }
    }
}
