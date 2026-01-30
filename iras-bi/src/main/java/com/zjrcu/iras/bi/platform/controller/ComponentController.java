package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.DashboardComponent;
import com.zjrcu.iras.bi.platform.domain.ComponentTemplate;
import com.zjrcu.iras.bi.platform.domain.dto.ComponentPosition;
import com.zjrcu.iras.bi.platform.domain.dto.TemplateInfo;
import com.zjrcu.iras.bi.platform.service.IComponentService;
import com.zjrcu.iras.bi.platform.mapper.ComponentTemplateMapper;
import com.zjrcu.iras.common.annotation.Log;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.enums.BusinessType;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仪表板组件Controller
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@RestController
@RequestMapping("/bi/dashboard/component")
public class ComponentController extends BaseController {
    @Autowired
    private IComponentService componentService;

    @Autowired
    private ComponentTemplateMapper componentTemplateMapper;

    /**
     * 查询组件列表
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/list")
    public TableDataInfo list(DashboardComponent component) {
        startPage();
        List<DashboardComponent> list = componentService.selectComponentList(component);
        return getDataTable(list);
    }

    /**
     * 获取组件详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(componentService.selectComponentById(id));
    }

    /**
     * 新增组件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "仪表板组件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DashboardComponent component) {
        return toAjax(componentService.insertComponent(component));
    }

    /**
     * 修改组件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "仪表板组件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DashboardComponent component) {
        return toAjax(componentService.updateComponent(component));
    }

    /**
     * 删除组件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "仪表板组件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(componentService.deleteComponentByIds(ids));
    }

    /**
     * 批量更新组件位置
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @PostMapping("/batch-position")
    public AjaxResult batchUpdatePosition(@RequestBody List<ComponentPosition> positions) {
        return toAjax(componentService.batchUpdatePosition(positions));
    }

    /**
     * 复制组件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @PostMapping("/{id}/copy")
    public AjaxResult copy(@PathVariable("id") Long id) {
        return success(componentService.copyComponent(id));
    }

    /**
     * 保存为模板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @PostMapping("/{id}/template")
    public AjaxResult saveAsTemplate(@PathVariable("id") Long id, @RequestBody TemplateInfo info) {
        DashboardComponent component = componentService.selectComponentById(id);
        if (component == null) {
            throw new ServiceException("组件不存在");
        }

        if (StringUtils.isEmpty(info.getTemplateName())) {
            throw new ServiceException("模板名称不能为空");
        }

        // 创建模板
        ComponentTemplate template = new ComponentTemplate();
        template.setTemplateName(info.getTemplateName());
        template.setTemplateDesc(info.getTemplateDesc());
        template.setComponentType(component.getComponentType());

        // 构建模板配置JSON
        StringBuilder configJson = new StringBuilder();
        configJson.append("{");
        if (StringUtils.isNotEmpty(component.getDataConfig())) {
            configJson.append("\"dataConfig\":").append(component.getDataConfig()).append(",");
        }
        if (StringUtils.isNotEmpty(component.getStyleConfig())) {
            configJson.append("\"styleConfig\":").append(component.getStyleConfig()).append(",");
        }
        if (StringUtils.isNotEmpty(component.getAdvancedConfig())) {
            configJson.append("\"advancedConfig\":").append(component.getAdvancedConfig());
        }
        configJson.append("}");

        template.setTemplateConfig(configJson.toString());

        return toAjax(componentTemplateMapper.insertTemplate(template));
    }

    /**
     * 查询模板列表
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/template/list")
    public TableDataInfo listTemplates(ComponentTemplate template) {
        startPage();
        List<ComponentTemplate> list = componentTemplateMapper.selectTemplateList(template);
        return getDataTable(list);
    }

    /**
     * 获取模板详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/template/{id}")
    public AjaxResult getTemplate(@PathVariable("id") Long id) {
        return success(componentTemplateMapper.selectTemplateById(id));
    }

    /**
     * 使用模板创建组件
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @PostMapping("/template/{templateId}/create")
    public AjaxResult createFromTemplate(@PathVariable("templateId") Long templateId, @RequestBody DashboardComponent component) {
        ComponentTemplate template = componentTemplateMapper.selectTemplateById(templateId);
        if (template == null) {
            throw new ServiceException("模板不存在");
        }

        // 从模板配置中提取配置信息
        String templateConfig = template.getTemplateConfig();
        if (StringUtils.isNotEmpty(templateConfig)) {
            // 简单的JSON解析，提取dataConfig, styleConfig, advancedConfig
            if (templateConfig.contains("\"dataConfig\":")) {
                int start = templateConfig.indexOf("\"dataConfig\":") + 13;
                int end = templateConfig.indexOf(",", start);
                if (end == -1) end = templateConfig.indexOf("}", start);
                if (end > start) {
                    component.setDataConfig(templateConfig.substring(start, end).trim());
                }
            }
            if (templateConfig.contains("\"styleConfig\":")) {
                int start = templateConfig.indexOf("\"styleConfig\":") + 14;
                int end = templateConfig.indexOf(",", start);
                if (end == -1) end = templateConfig.indexOf("}", start);
                if (end > start) {
                    component.setStyleConfig(templateConfig.substring(start, end).trim());
                }
            }
            if (templateConfig.contains("\"advancedConfig\":")) {
                int start = templateConfig.indexOf("\"advancedConfig\":") + 17;
                int end = templateConfig.indexOf("}", start);
                if (end > start) {
                    component.setAdvancedConfig(templateConfig.substring(start, end).trim());
                }
            }
        }

        // 设置组件类型
        component.setComponentType(template.getComponentType());

        // 插入组件
        return toAjax(componentService.insertComponent(component));
    }

    /**
     * 修改模板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "组件模板", businessType = BusinessType.UPDATE)
    @PutMapping("/template")
    public AjaxResult updateTemplate(@Validated @RequestBody ComponentTemplate template) {
        return toAjax(componentTemplateMapper.updateTemplate(template));
    }

    /**
     * 删除模板
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @Log(title = "组件模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/template/{ids}")
    public AjaxResult removeTemplate(@PathVariable Long[] ids) {
        return toAjax(componentTemplateMapper.deleteTemplateByIds(ids));
    }
}
