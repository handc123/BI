package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.Visualization;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.service.IExportService;
import com.zjrcu.iras.bi.platform.service.IVisualizationService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import com.zjrcu.iras.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BI可视化管理Controller
 *
 * @author iras
 */
@Tag(name = "BI可视化管理")
@RestController
@RequestMapping("/bi/visualization")
public class VisualizationController extends BaseController {

    @Autowired
    private IVisualizationService visualizationService;

    @Autowired(required = false)
    private IExportService exportService;

    /**
     * 查询可视化列表
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:list')")
    @Operation(description = "查询可视化列表")
    @GetMapping("/list")
    public TableDataInfo list(Visualization visualization) {
        startPage();
        List<Visualization> list = visualizationService.selectVisualizationList(visualization);
        return getDataTable(list);
    }

    /**
     * 获取可视化详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:query')")
    @Operation(description = "获取可视化详细信息")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Visualization visualization = visualizationService.selectVisualizationById(id);
        if (visualization == null) {
            return error("可视化不存在");
        }
        return success(visualization);
    }

    /**
     * 新增可视化
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:add')")
    @Operation(description = "新增可视化")
    @Log(title = "可视化管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Visualization visualization) {
        try {
            // 设置创建者
            visualization.setCreateBy(getUsername());
            
            // 验证可视化配置
            String validationError = visualizationService.validateVisualizationConfig(visualization);
            if (StringUtils.isNotEmpty(validationError)) {
                return error(validationError);
            }
            
            // 插入可视化
            int result = visualizationService.insertVisualization(visualization);
            if (result > 0) {
                return success(visualization);
            }
            return error("新增可视化失败");
        } catch (Exception e) {
            logger.error("新增可视化失败", e);
            return error("新增可视化失败: " + e.getMessage());
        }
    }

    /**
     * 修改可视化
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:edit')")
    @Operation(description = "修改可视化")
    @Log(title = "可视化管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Visualization visualization) {
        try {
            // 设置更新者
            visualization.setUpdateBy(getUsername());
            
            // 验证可视化配置
            String validationError = visualizationService.validateVisualizationConfig(visualization);
            if (StringUtils.isNotEmpty(validationError)) {
                return error(validationError);
            }
            
            // 更新可视化
            return toAjax(visualizationService.updateVisualization(visualization));
        } catch (Exception e) {
            logger.error("修改可视化失败", e);
            return error("修改可视化失败: " + e.getMessage());
        }
    }

    /**
     * 删除可视化
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:remove')")
    @Operation(description = "删除可视化")
    @Log(title = "可视化管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            return toAjax(visualizationService.deleteVisualizationByIds(ids));
        } catch (Exception e) {
            logger.error("删除可视化失败", e);
            return error("删除可视化失败: " + e.getMessage());
        }
    }

    /**
     * 获取可视化数据
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:query')")
    @Operation(description = "获取可视化数据")
    @PostMapping("/{id}/data")
    public AjaxResult getData(@PathVariable Long id, @RequestBody(required = false) List<Filter> filters) {
        try {
            // 检查可视化是否存在
            Visualization visualization = visualizationService.selectVisualizationById(id);
            if (visualization == null) {
                return error("可视化不存在");
            }
            
            // 获取可视化数据
            QueryResult result = visualizationService.getVisualizationData(id, filters);
            
            if (result.isSuccess()) {
                return success(result);
            } else {
                return error(result.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("获取可视化数据失败", e);
            return error("获取可视化数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出可视化数据为CSV
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:export')")
    @Operation(description = "导出可视化数据为CSV")
    @Log(title = "可视化导出", businessType = BusinessType.EXPORT)
    @PostMapping("/{id}/export/csv")
    public void exportCSV(@PathVariable Long id, 
                         @RequestBody(required = false) List<Filter> filters,
                         HttpServletResponse response) {
        try {
            // 检查可视化是否存在
            Visualization visualization = visualizationService.selectVisualizationById(id);
            if (visualization == null) {
                logger.error("可视化不存在: id={}", id);
                return;
            }
            
            // 检查导出服务是否可用
            if (exportService == null) {
                logger.error("导出服务未配置");
                return;
            }
            
            // 导出为CSV
            exportService.exportToCSV(id, filters, response);
            
        } catch (Exception e) {
            logger.error("导出CSV失败", e);
        }
    }

    /**
     * 导出可视化数据为Excel
     */
    @PreAuthorize("@ss.hasPermi('bi:visualization:export')")
    @Operation(description = "导出可视化数据为Excel")
    @Log(title = "可视化导出", businessType = BusinessType.EXPORT)
    @PostMapping("/{id}/export/excel")
    public void exportExcel(@PathVariable Long id, 
                           @RequestBody(required = false) List<Filter> filters,
                           HttpServletResponse response) {
        try {
            // 检查可视化是否存在
            Visualization visualization = visualizationService.selectVisualizationById(id);
            if (visualization == null) {
                logger.error("可视化不存在: id={}", id);
                return;
            }
            
            // 检查导出服务是否可用
            if (exportService == null) {
                logger.error("导出服务未配置");
                return;
            }
            
            // 导出为Excel
            exportService.exportToExcel(id, filters, response);
            
        } catch (Exception e) {
            logger.error("导出Excel失败", e);
        }
    }
}
