package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.ExtractResult;
import com.zjrcu.iras.bi.platform.domain.dto.Filter;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.bi.platform.scheduler.IDataExtractScheduler;
import com.zjrcu.iras.bi.platform.service.IDatasetService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import com.zjrcu.iras.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BI数据集管理Controller
 *
 * @author iras
 */
@Tag(name = "BI数据集管理")
@RestController
@RequestMapping("/bi/dataset")
public class DatasetController extends BaseController {

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private IDataExtractScheduler dataExtractScheduler;

    /**
     * 查询数据集列表
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:list')")
    @Operation(description = "查询数据集列表")
    @GetMapping("/list")
    public TableDataInfo list(Dataset dataset) {
        startPage();
        List<Dataset> list = datasetService.selectDatasetList(dataset);
        return getDataTable(list);
    }

    /**
     * 获取数据集详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:query')")
    @Operation(description = "获取数据集详细信息")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Dataset dataset = datasetService.selectDatasetById(id);
        if (dataset == null) {
            return error("数据集不存在");
        }
        return success(dataset);
    }

    /**
     * 新增数据集
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:add')")
    @Operation(description = "新增数据集")
    @Log(title = "数据集管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Dataset dataset) {
        try {
            // 设置创建者
            dataset.setCreateBy(getUsername());
            
            // 验证数据集配置
            String validationError = datasetService.validateDatasetConfig(dataset);
            if (StringUtils.isNotEmpty(validationError)) {
                return error(validationError);
            }
            
            // 插入数据集
            int result = datasetService.insertDataset(dataset);
            if (result > 0) {
                return success(dataset);
            }
            return error("新增数据集失败");
        } catch (Exception e) {
            logger.error("新增数据集失败", e);
            return error("新增数据集失败: " + e.getMessage());
        }
    }

    /**
     * 修改数据集
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:edit')")
    @Operation(description = "修改数据集")
    @Log(title = "数据集管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Dataset dataset) {
        try {
            // 设置更新者
            dataset.setUpdateBy(getUsername());
            
            // 验证数据集配置
            String validationError = datasetService.validateDatasetConfig(dataset);
            if (StringUtils.isNotEmpty(validationError)) {
                return error(validationError);
            }
            
            // 更新数据集
            return toAjax(datasetService.updateDataset(dataset));
        } catch (Exception e) {
            logger.error("修改数据集失败", e);
            return error("修改数据集失败: " + e.getMessage());
        }
    }

    /**
     * 删除数据集
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:remove')")
    @Operation(description = "删除数据集")
    @Log(title = "数据集管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            return toAjax(datasetService.deleteDatasetByIds(ids));
        } catch (Exception e) {
            logger.error("删除数据集失败", e);
            return error("删除数据集失败: " + e.getMessage());
        }
    }

    /**
     * 预览数据集数据
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:query')")
    @Operation(description = "预览数据集数据")
    @PostMapping("/{id}/preview")
    public AjaxResult preview(@PathVariable Long id, @RequestBody(required = false) List<Filter> filters) {
        try {
            QueryResult result = datasetService.previewDataset(id, filters);
            if (result.isSuccess()) {
                return success(result);
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("预览数据集失败", e);
            return error("预览数据集失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据集数据（用于字段配置）
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:query')")
    @Operation(description = "获取数据集数据和字段信息")
    @PostMapping("/{id}/data")
    public AjaxResult getData(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> params) {
        try {
            // 获取分页参数，默认只返回1条用于获取字段信息
            Integer pageSize = params != null && params.containsKey("pageSize") 
                ? Integer.parseInt(params.get("pageSize").toString()) 
                : 1;
            
            // 获取过滤条件
            @SuppressWarnings("unchecked")
            List<Filter> filters = params != null && params.containsKey("filters")
                ? (List<Filter>) params.get("filters")
                : null;
            
            // 调用预览接口获取数据
            QueryResult result = datasetService.previewDataset(id, filters);
            
            if (result.isSuccess()) {
                // 限制返回的行数
                if (result.getData() != null && result.getData().size() > pageSize) {
                    result.setData(result.getData().subList(0, pageSize));
                }
                return success(result);
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("获取数据集数据失败", e);
            return error("获取数据集数据失败: " + e.getMessage());
        }
    }

    /**
     * 执行立即抽取
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:edit')")
    @Operation(description = "执行立即抽取")
    @Log(title = "数据集抽取", businessType = BusinessType.OTHER)
    @PostMapping("/{id}/extract")
    public AjaxResult extract(@PathVariable Long id) {
        try {
            // 检查数据集是否存在
            Dataset dataset = datasetService.selectDatasetById(id);
            if (dataset == null) {
                return error("数据集不存在");
            }

            // 检查是否为抽取类型数据集
            if (!dataset.isExtract()) {
                return error("只有抽取类型数据集才能执行抽取操作");
            }

            // 执行立即抽取
            ExtractResult result = dataExtractScheduler.executeExtract(id);

            if (result.isSuccess()) {
                return success(result);
            } else {
                return error(result.getErrorMessage());
            }

        } catch (Exception e) {
            logger.error("执行数据抽取失败", e);
            return error("执行数据抽取失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据集字段元数据
     */
    @PreAuthorize("@ss.hasPermi('bi:dataset:query')")
    @Operation(description = "获取数据集字段元数据")
    @GetMapping("/{id}/fields")
    public AjaxResult getFields(@PathVariable Long id) {
        try {
            Dataset dataset = datasetService.selectDatasetById(id);
            if (dataset == null) {
                return error("数据集不存在");
            }

            // 获取字段元数据
            List<Map<String, Object>> fields = datasetService.getDatasetFields(id);
            return success(fields);
        } catch (Exception e) {
            logger.error("获取字段元数据失败", e);
            return error("获取字段元数据失败: " + e.getMessage());
        }
    }
}
