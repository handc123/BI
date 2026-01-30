package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 组件模板对象 bi_component_template
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class ComponentTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 模板描述 */
    private String templateDesc;

    /** 组件类型 */
    private String componentType;

    /** 模板配置(JSON) */
    private String templateConfig;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDesc = templateDesc;
    }

    public String getTemplateDesc() {
        return templateDesc;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = templateConfig;
    }

    public String getTemplateConfig() {
        return templateConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("templateName", getTemplateName())
            .append("templateDesc", getTemplateDesc())
            .append("componentType", getComponentType())
            .append("templateConfig", getTemplateConfig())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
