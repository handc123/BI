package com.zjrcu.iras.bi.platform.domain.dto;

import java.io.Serializable;

/**
 * 模板信息DTO
 * 用于保存组件为模板时传递模板名称和描述
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
public class TemplateInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 模板名称 */
    private String templateName;

    /** 模板描述 */
    private String templateDesc;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDesc() {
        return templateDesc;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDesc = templateDesc;
    }
}
