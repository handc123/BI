package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.QueryCondition;
import com.zjrcu.iras.bi.platform.domain.ConditionMapping;
import com.zjrcu.iras.bi.platform.domain.dto.ChartQueryRequest;
import com.zjrcu.iras.bi.platform.domain.dto.ConditionOrderDTO;
import com.zjrcu.iras.bi.platform.domain.dto.QueryConditionConfigDTO;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.domain.dto.ValidationResult;
import com.zjrcu.iras.bi.platform.service.IQueryConditionService;
import com.zjrcu.iras.bi.platform.service.IDatasetService;
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
import java.util.Map;

/**
 * 查询条件Controller
 * 提供查询条件配置管理的REST API端点
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@RestController
@RequestMapping("/bi/condition")
public class QueryConditionController extends BaseController {
    @Autowired
    private IQueryConditionService queryConditionService;

    @Autowired
    private IDatasetService datasetService;

    /**
     * 查询查询条件列表（按组件ID）
     * 用于配置面板加载特定组件的查询条件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) Long componentId) {
        if (componentId != null) {
            // 按组件ID查询
            List<QueryCondition> list = queryConditionService.selectConditionsByComponentId(componentId);
            return success(list);
        } else {
            // 查询所有（用于管理页面）
            QueryCondition condition = new QueryCondition();
            startPage();
            List<QueryCondition> list = queryConditionService.selectConditionList(condition);
            return success(getDataTable(list));
        }
    }

    /**
     * 获取查询条件详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(queryConditionService.selectConditionById(id));
    }

    /**
     * 新增查询条件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QueryCondition condition) {
        return toAjax(queryConditionService.insertCondition(condition));
    }

    /**
     * 修改查询条件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QueryCondition condition) {
        return toAjax(queryConditionService.updateCondition(condition));
    }

    /**
     * 删除查询条件
     * 支持级联删除关联的字段映射
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "查询条件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(queryConditionService.deleteConditionByIds(ids));
    }

    /**
     * 保存查询条件配置
     * 用于配置面板保存完整的条件配置（包括条件和映射）
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "查询条件配置", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult saveConfig(@Validated @RequestBody QueryConditionConfigDTO config) {
        try {
            // 先验证配置
            ValidationResult validationResult = queryConditionService.validateConditionConfig(config);
            if (!validationResult.isValid()) {
                return error("配置验证失败").put("errors", validationResult.getErrors());
            }
            
            // 保存配置
            int result = queryConditionService.saveConditionConfig(config);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("保存查询条件配置失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 重新排序查询条件
     * 用于拖拽排序后批量更新显示顺序
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "查询条件排序", businessType = BusinessType.UPDATE)
    @PutMapping("/reorder")
    public AjaxResult reorder(@Validated @RequestBody List<ConditionOrderDTO> orders) {
        try {
            int result = queryConditionService.reorderConditions(orders);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("重新排序查询条件失败", e);
            return error("排序失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据集字段列表
     * 用于配置面板显示可用的字段供选择
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/fields/{datasetId}")
    public AjaxResult getFields(@PathVariable("datasetId") Long datasetId) {
        try {
            List<Map<String, Object>> fields = datasetService.getDatasetFields(datasetId);
            return success(fields);
        } catch (Exception e) {
            logger.error("获取数据集字段失败", e);
            return error("获取字段失败: " + e.getMessage());
        }
    }

    /**
     * 验证查询条件配置
     * 用于配置面板实时验证配置的有效性
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @PostMapping("/validate")
    public AjaxResult validate(@Validated @RequestBody QueryConditionConfigDTO config) {
        try {
            ValidationResult result = queryConditionService.validateConditionConfig(config);
            return success(result);
        } catch (Exception e) {
            logger.error("验证查询条件配置失败", e);
            return error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取条件映射
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/{dashboardId}/mappings")
    public AjaxResult getMappings(@PathVariable("dashboardId") Long dashboardId) {
        return success(queryConditionService.getMappings(dashboardId));
    }

    /**
     * 保存条件映射
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @PostMapping("/{dashboardId}/mappings")
    public AjaxResult saveMappings(@PathVariable("dashboardId") Long dashboardId,
                                   @RequestBody List<ConditionMapping> mappings) {
        return toAjax(queryConditionService.saveMappings(dashboardId, mappings));
    }

    /**
     * 获取级联选项
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/cascade-options")
    public AjaxResult getCascadeOptions(@RequestParam("conditionId") Long conditionId,
                                       @RequestParam Map<String, Object> parentValues) {
        return success(queryConditionService.getCascadeOptions(conditionId, parentValues));
    }

    /**
     * 执行图表查询
     * 用于获取图表数据
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @PostMapping("/execute")
    public AjaxResult executeQuery(@RequestBody ChartQueryRequest queryRequest) {
        try {
            QueryResult result = queryConditionService.executeChartQuery(queryRequest);
            
            if (result.isSuccess()) {
                return success(result);
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("执行图表查询失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }
}
