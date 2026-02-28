package com.zjrcu.iras.bi.metric.controller;

import com.zjrcu.iras.bi.metric.domain.MetricLineage;
import com.zjrcu.iras.bi.metric.dto.LineageGraphDTO;
import com.zjrcu.iras.bi.metric.service.IMetricLineageService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 指标血缘关系Controller
 *
 * @author iras
 * @date 2025-02-26
 */
@Tag(name = "指标血缘管理")
@RestController
@RequestMapping("/bi/lineage")
public class MetricLineageController extends BaseController {

    @Autowired
    private IMetricLineageService metricLineageService;

    /**
     * 获取指标血缘图谱
     */
    @PreAuthorize("@ss.hasPermi('bi:lineage:query')")
    @Operation(description = "获取指标血缘图谱")
    @GetMapping("/metric/{metricId}")
    public AjaxResult getLineageGraph(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Parameter(description = "模式: graph-全量, upstream-上游, downstream-下游") @RequestParam(defaultValue = "graph") String mode) {
        try {
            LineageGraphDTO graph = metricLineageService.getLineageGraph(metricId, mode);
            return success(graph);
        } catch (Exception e) {
            logger.error("获取指标血缘图谱失败, metricId: {}, mode: {}", metricId, mode, e);
            return error("获取指标血缘图谱失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标上游血缘
     */
    @PreAuthorize("@ss.hasPermi('bi:lineage:query')")
    @Operation(description = "获取指标上游血缘")
    @GetMapping("/upstream/{metricId}")
    public AjaxResult getUpstreamLineage(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Parameter(description = "层级深度") @RequestParam(defaultValue = "3") Integer depth) {
        try {
            List<MetricLineage> upstreamList = metricLineageService.getUpstreamLineage(metricId, depth);
            return success(upstreamList);
        } catch (Exception e) {
            logger.error("获取指标上游血缘失败, metricId: {}", metricId, e);
            return error("获取指标上游血缘失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标下游血缘
     */
    @PreAuthorize("@ss.hasPermi('bi:lineage:query')")
    @Operation(description = "获取指标下游血缘")
    @GetMapping("/downstream/{metricId}")
    public AjaxResult getDownstreamLineage(
            @Parameter(description = "指标ID") @PathVariable Long metricId,
            @Parameter(description = "层级深度") @RequestParam(defaultValue = "3") Integer depth) {
        try {
            List<MetricLineage> downstreamList = metricLineageService.getDownstreamLineage(metricId, depth);
            return success(downstreamList);
        } catch (Exception e) {
            logger.error("获取指标下游血缘失败, metricId: {}", metricId, e);
            return error("获取指标下游血缘失败: " + e.getMessage());
        }
    }

    /**
     * 新增指标血缘关系
     */
    @PreAuthorize("@ss.hasPermi('bi:lineage:add')")
    @Operation(description = "新增指标血缘关系")
    @Log(title = "指标血缘关系", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MetricLineage metricLineage) {
        try {
            // 设置创建者
            metricLineage.setCreateBy(getUsername());

            // 校验血缘关系有效性
            if (!metricLineageService.validateLineage(metricLineage)) {
                return error("新增血缘关系失败，存在循环依赖或无效关系");
            }

            // 插入血缘关系
            int result = metricLineageService.insertMetricLineage(metricLineage);
            if (result > 0) {
                return success(metricLineage);
            }
            return error("新增指标血缘关系失败");
        } catch (Exception e) {
            logger.error("新增指标血缘关系失败", e);
            return error("新增指标血缘关系失败: " + e.getMessage());
        }
    }

    /**
     * 删除指标血缘关系
     */
    @PreAuthorize("@ss.hasPermi('bi:lineage:remove')")
    @Operation(description = "删除指标血缘关系")
    @Log(title = "指标血缘关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@Parameter(description = "血缘关系ID") @PathVariable Long id) {
        try {
            return toAjax(metricLineageService.deleteLineageById(id));
        } catch (Exception e) {
            logger.error("删除指标血缘关系失败, id: {}", id, e);
            return error("删除指标血缘关系失败: " + e.getMessage());
        }
    }

    /**
     * 删除指标的所有血缘关系
     */
    @PreAuthorize("@ss.hasPermi('bi:lineage:remove')")
    @Operation(description = "删除指标的所有血缘关系")
    @Log(title = "指标血缘关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/metric/{metricId}")
    public AjaxResult removeByMetricId(@Parameter(description = "指标ID") @PathVariable Long metricId) {
        try {
            int count = metricLineageService.deleteLineageByMetricId(metricId);
            return success("成功删除 " + count + " 条血缘关系");
        } catch (Exception e) {
            logger.error("删除指标血缘关系失败, metricId: {}", metricId, e);
            return error("删除指标血缘关系失败: " + e.getMessage());
        }
    }

    /**
     * 批量新增指标血缘关系
     */
    @PreAuthorize("@ss.hasPermi('bi:lineage:add')")
    @Operation(description = "批量新增指标血缘关系")
    @Log(title = "指标血缘关系", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult addBatch(@Validated @RequestBody List<MetricLineage> lineageList) {
        try {
            String username = getUsername();
            int successCount = 0;
            int failCount = 0;

            for (MetricLineage lineage : lineageList) {
                try {
                    lineage.setCreateBy(username);

                    // 校验血缘关系有效性
                    if (!metricLineageService.validateLineage(lineage)) {
                        failCount++;
                        continue;
                    }

                    // 插入血缘关系
                    int result = metricLineageService.insertMetricLineage(lineage);
                    if (result > 0) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    logger.error("新增血缘关系失败: {}", lineage, e);
                    failCount++;
                }
            }

            if (successCount > 0) {
                return success(String.format("批量新增完成，成功 %d 条，失败 %d 条", successCount, failCount));
            } else {
                return error("批量新增失败");
            }
        } catch (Exception e) {
            logger.error("批量新增指标血缘关系失败", e);
            return error("批量新增指标血缘关系失败: " + e.getMessage());
        }
    }
}
