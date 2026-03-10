package com.zjrcu.iras.bi.metric.controller;

import com.zjrcu.iras.bi.metric.domain.MetricMetadata;
import com.zjrcu.iras.bi.metric.dto.MetricResolveRequestDTO;
import com.zjrcu.iras.bi.metric.dto.MetricResolveResponseDTO;
import com.zjrcu.iras.bi.metric.service.IMetricMetadataService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 指标元数据管理Controller
 *
 * @author iras
 * @date 2025-02-26
 */
@Tag(name = "指标元数据管理")
@RestController
@RequestMapping("/bi/metadata")
public class MetricMetadataController extends BaseController {

    @Autowired
    private IMetricMetadataService metricMetadataService;

    /**
     * 查询指标元数据列表
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:list')")
    @Operation(description = "查询指标元数据列表")
    @GetMapping("/list")
    public TableDataInfo list(MetricMetadata metricMetadata) {
        startPage();
        List<MetricMetadata> list = metricMetadataService.selectMetricMetadataList(metricMetadata);
        return getDataTable(list);
    }

    /**
     * 导出指标元数据列表
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:export')")
    @Operation(description = "导出指标元数据列表")
    @Log(title = "指标元数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MetricMetadata metricMetadata) {
        List<MetricMetadata> list = metricMetadataService.selectMetricMetadataList(metricMetadata);
        // ExcelUtil would be used here for export
        // ExcelUtil<MetricMetadata> util = new ExcelUtil<MetricMetadata>(MetricMetadata.class);
        // util.exportExcel(response, list, "指标元数据");
    }

    /**
     * 获取指标元数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:query')")
    @Operation(description = "获取指标元数据详细信息")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        MetricMetadata metricMetadata = metricMetadataService.selectMetricMetadataById(id);
        if (metricMetadata == null) {
            return error("指标元数据不存在");
        }
        return success(metricMetadata);
    }

    /**
     * 根据指标编码获取指标元数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:query')")
    @Operation(description = "根据指标编码获取指标元数据")
    @GetMapping("/code/{metricCode}")
    public AjaxResult getInfoByCode(@PathVariable String metricCode) {
        MetricMetadata metricMetadata = metricMetadataService.selectMetricMetadataByCode(metricCode);
        if (metricMetadata == null) {
            return error("指标元数据不存在");
        }
        return success(metricMetadata);
    }

    /**
     * 按当前登录机构解析指标
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:query')")
    @Operation(description = "按当前登录机构解析指标")
    @PostMapping("/resolve")
    public AjaxResult resolve(@RequestBody MetricResolveRequestDTO request) {
        MetricResolveResponseDTO resolved = metricMetadataService.resolveMetric(request);
        return success(resolved);
    }

    /**
     * 查询当前机构在指定数据集下可用的指标
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:query')")
    @Operation(description = "查询当前机构在指定数据集下可用的指标")
    @GetMapping("/available")
    public AjaxResult available(@RequestParam("datasetId") Long datasetId) {
        return success(metricMetadataService.selectAvailableMetricsByDataset(datasetId));
    }

    /**
     * 新增指标元数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:add')")
    @Operation(description = "新增指标元数据")
    @Log(title = "指标元数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MetricMetadata metricMetadata) {
        try {
            // 设置创建者
            metricMetadata.setCreateBy(getUsername());
            if (metricMetadata.getDeptId() == null) {
                metricMetadata.setDeptId(SecurityUtils.getDeptId());
            }

            // 幂等插入：已存在时由服务层复用已有指标并回填ID
            int result = metricMetadataService.insertMetricMetadata(metricMetadata);
            if (result > 0) {
                return success(metricMetadata);
            }
            return error("新增指标元数据失败");
        } catch (Exception e) {
            logger.error("新增指标元数据失败", e);
            return error("新增指标元数据失败: " + e.getMessage());
        }
    }

    /**
     * 修改指标元数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:edit')")
    @Operation(description = "修改指标元数据")
    @Log(title = "指标元数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MetricMetadata metricMetadata) {
        try {
            // 设置更新者
            metricMetadata.setUpdateBy(getUsername());

            // 校验指标编码唯一性
            if (!metricMetadataService.checkMetricCodeUnique(metricMetadata)) {
                return error("修改指标失败，指标编码已存在");
            }

            // 更新指标元数据
            return toAjax(metricMetadataService.updateMetricMetadata(metricMetadata));
        } catch (Exception e) {
            logger.error("修改指标元数据失败", e);
            return error("修改指标元数据失败: " + e.getMessage());
        }
    }

    /**
     * 删除指标元数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:remove')")
    @Operation(description = "删除指标元数据")
    @Log(title = "指标元数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            return toAjax(metricMetadataService.deleteMetricMetadataByIds(ids));
        } catch (Exception e) {
            logger.error("删除指标元数据失败", e);
            return error("删除指标元数据失败: " + e.getMessage());
        }
    }

    /**
     * 导入指标元数据
     */
    @PreAuthorize("@ss.hasPermi('bi:metadata:import')")
    @Operation(description = "导入指标元数据")
    @Log(title = "指标元数据", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importData(@RequestBody List<MetricMetadata> metricMetadataList,
                                  @RequestParam(required = false, defaultValue = "true") Boolean isUpdateSupport) {
        try {
            String operName = getUsername();
            String message = metricMetadataService.importMetricMetadata(metricMetadataList, isUpdateSupport, operName);
            return success(message);
        } catch (Exception e) {
            logger.error("导入指标元数据失败", e);
            return error("导入指标元数据失败: " + e.getMessage());
        }
    }
}
