package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.dto.ConnectionTestResult;
import com.zjrcu.iras.bi.platform.domain.dto.FileUploadResult;
import com.zjrcu.iras.bi.platform.service.IDataSourceService;
import com.zjrcu.iras.bi.platform.service.IFileUploadService;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * BI数据源管理Controller
 *
 * @author iras
 */
@Tag(name = "BI数据源管理")
@RestController
@RequestMapping("/bi/datasource")
public class DataSourceController extends BaseController {

    @Autowired
    private IDataSourceService dataSourceService;

    @Autowired
    private IFileUploadService fileUploadService;

    /**
     * 查询数据源列表
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:list')")
    @Operation(description = "查询数据源列表")
    @GetMapping("/list")
    public TableDataInfo list(DataSource dataSource) {
        startPage();
        List<DataSource> list = dataSourceService.selectDataSourceList(dataSource);
        return getDataTable(list);
    }

    /**
     * 获取数据源详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:query')")
    @Operation(description = "获取数据源详细信息")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(dataSourceService.selectDataSourceById(id));
    }

    /**
     * 测试数据源连接
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:add') or @ss.hasPermi('bi:datasource:edit')")
    @Operation(description = "测试数据源连接")
    @Log(title = "数据源管理", businessType = BusinessType.OTHER)
    @PostMapping("/test")
    public AjaxResult testConnection(@Validated @RequestBody DataSource dataSource) {
        try {
            ConnectionTestResult result = dataSourceService.testConnection(dataSource);
            if (result.isSuccess()) {
                return success(result);
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("数据源连接测试失败", e);
            return error("数据源连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据源的表列表
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:query')")
    @Operation(description = "获取数据源的表列表")
    @GetMapping("/{id}/tables")
    public AjaxResult getTables(@PathVariable Long id) {
        try {
            List<Map<String, String>> tables = dataSourceService.getTableListWithComments(id);
            return success(tables);
        } catch (Exception e) {
            logger.error("获取表列表失败: dataSourceId={}", id, e);
            return error("获取表列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取表结构信息
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:query')")
    @Operation(description = "获取表结构信息")
    @GetMapping("/{id}/tables/{tableName}/schema")
    public AjaxResult getTableSchema(@PathVariable Long id, @PathVariable String tableName) {
        try {
            com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo schema = 
                    dataSourceService.getTableSchema(id, tableName);
            return success(schema);
        } catch (Exception e) {
            logger.error("获取表结构失败: dataSourceId={}, tableName={}", id, tableName, e);
            return error("获取表结构失败: " + e.getMessage());
        }
    }

    /**
     * 获取表数据预览
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:query')")
    @Operation(description = "获取表数据预览")
    @GetMapping("/{id}/tables/{tableName}/preview")
    public AjaxResult getTablePreview(@PathVariable Long id, 
                                     @PathVariable String tableName,
                                     @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData preview = 
                    dataSourceService.getTablePreview(id, tableName, limit);
            return success(preview);
        } catch (Exception e) {
            logger.error("获取表数据预览失败: dataSourceId={}, tableName={}", id, tableName, e);
            return error("获取表数据预览失败: " + e.getMessage());
        }
    }

    /**
     * 新增数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:add')")
    @Operation(description = "新增数据源")
    @Log(title = "数据源管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DataSource dataSource) {
        // 设置创建者
        dataSource.setCreateBy(getUsername());
        
        // 先测试连接
        try {
            ConnectionTestResult testResult = dataSourceService.testConnection(dataSource);
            if (!testResult.isSuccess()) {
                return error("数据源连接测试失败: " + testResult.getMessage());
            }
        } catch (Exception e) {
            logger.error("数据源连接测试失败", e);
            return error("数据源连接测试失败: " + e.getMessage());
        }
        
        // 插入数据源
        int result = dataSourceService.insertDataSource(dataSource);
        if (result > 0) {
            return success(dataSource);
        }
        return error("新增数据源失败");
    }

    /**
     * 修改数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:edit')")
    @Operation(description = "修改数据源")
    @Log(title = "数据源管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DataSource dataSource) {
        // 设置更新者
        dataSource.setUpdateBy(getUsername());
        
        // 先测试连接
        try {
            ConnectionTestResult testResult = dataSourceService.testConnection(dataSource);
            if (!testResult.isSuccess()) {
                return error("数据源连接测试失败: " + testResult.getMessage());
            }
        } catch (Exception e) {
            logger.error("数据源连接测试失败", e);
            return error("数据源连接测试失败: " + e.getMessage());
        }
        
        // 更新数据源
        return toAjax(dataSourceService.updateDataSource(dataSource));
    }

    /**
     * 删除数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:remove')")
    @Operation(description = "删除数据源")
    @Log(title = "数据源管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            return toAjax(dataSourceService.deleteDataSourceByIds(ids));
        } catch (Exception e) {
            logger.error("删除数据源失败", e);
            return error("删除数据源失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:add')")
    @Operation(description = "上传文件数据源")
    @Log(title = "文件数据源上传", businessType = BusinessType.INSERT)
    @PostMapping("/file/upload")
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "remark", required = false) String remark) {
        try {
            FileUploadResult result = fileUploadService.uploadFile(file, name, remark);
            if (result.isSuccess()) {
                return success(result);
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 预览文件数据源数据
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:query')")
    @Operation(description = "预览文件数据源数据")
    @GetMapping("/file/{id}/preview")
    public AjaxResult previewFileData(@PathVariable Long id,
                                     @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            List<List<Object>> data = fileUploadService.previewFileData(id, pageNum, pageSize);
            return success(data);
        } catch (Exception e) {
            logger.error("预览文件数据失败", e);
            return error("预览文件数据失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:remove')")
    @Operation(description = "删除文件数据源")
    @Log(title = "文件数据源删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/file/{id}")
    public AjaxResult removeFileDataSource(@PathVariable Long id) {
        try {
            boolean result = fileUploadService.deleteFileDataSource(id);
            return result ? success() : error("删除文件数据源失败");
        } catch (Exception e) {
            logger.error("删除文件数据源失败", e);
            return error("删除文件数据源失败: " + e.getMessage());
        }
    }

    /**
     * 清理未使用的文件数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:remove')")
    @Operation(description = "清理未使用的文件数据源")
    @Log(title = "清理未使用文件数据源", businessType = BusinessType.CLEAN)
    @PostMapping("/file/cleanup")
    public AjaxResult cleanupUnusedFiles() {
        try {
            int count = fileUploadService.cleanupUnusedFileSources();
            return success("成功清理 " + count + " 个未使用的文件数据源");
        } catch (Exception e) {
            logger.error("清理未使用文件数据源失败", e);
            return error("清理未使用文件数据源失败: " + e.getMessage());
        }
    }
}
