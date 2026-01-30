package com.zjrcu.iras.bi.platform.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 查询条件配置数据传输对象
 * 用于保存和加载组件的查询条件配置
 * 
 * @author IRAS
 */
public class QueryConditionConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 组件ID */
    @NotNull(message = "组件ID不能为空")
    private Long componentId;

    /** 仪表板ID */
    @NotNull(message = "仪表板ID不能为空")
    private Long dashboardId;

    /** 条件列表 */
    @Valid
    @NotEmpty(message = "条件列表不能为空")
    private List<QueryConditionDTO> conditions;

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Long getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(Long dashboardId) {
        this.dashboardId = dashboardId;
    }

    public List<QueryConditionDTO> getConditions() {
        return conditions;
    }

    public void setConditions(List<QueryConditionDTO> conditions) {
        this.conditions = conditions;
    }

    /**
     * 查询条件DTO
     */
    public static class QueryConditionDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 条件ID (新增时为null) */
        private Long id;

        /** 条件名称 */
        @NotEmpty(message = "条件名称不能为空")
        private String conditionName;

        /** 条件类型 */
        @NotEmpty(message = "条件类型不能为空")
        private String conditionType;

        /** 显示顺序 */
        private Integer displayOrder;

        /** 是否必填 (0=否, 1=是) */
        private String isRequired;

        /** 是否可见 (0=否, 1=是) */
        private String isVisible;

        /** 默认值 */
        private String defaultValue;

        /** 配置对象 */
        @Valid
        private ConditionConfigDTO config;

        /** 字段映射列表 */
        @Valid
        @NotEmpty(message = "字段映射列表不能为空")
        private List<ConditionMappingDTO> mappings;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getConditionName() {
            return conditionName;
        }

        public void setConditionName(String conditionName) {
            this.conditionName = conditionName;
        }

        public String getConditionType() {
            return conditionType;
        }

        public void setConditionType(String conditionType) {
            this.conditionType = conditionType;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }

        public String getIsRequired() {
            return isRequired;
        }

        public void setIsRequired(String isRequired) {
            this.isRequired = isRequired;
        }

        public String getIsVisible() {
            return isVisible;
        }

        public void setIsVisible(String isVisible) {
            this.isVisible = isVisible;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public ConditionConfigDTO getConfig() {
            return config;
        }

        public void setConfig(ConditionConfigDTO config) {
            this.config = config;
        }

        public List<ConditionMappingDTO> getMappings() {
            return mappings;
        }

        public void setMappings(List<ConditionMappingDTO> mappings) {
            this.mappings = mappings;
        }
    }

    /**
     * 条件配置DTO
     */
    public static class ConditionConfigDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 显示模式 (auto=自动, custom=自定义) */
        private String displayMode;

        /** 显示类型 (time/dropdown/text/number/range) */
        private String displayType;

        /** 时间粒度 (year/month/day/hour/minute/second) */
        private String timeGranularity;

        /** 默认时间范围 (today/thisWeek/thisMonth/thisQuarter/thisYear/last7Days/last30Days/custom) */
        private String defaultTimeRange;

        /** 自定义范围开始 */
        private String customRangeStart;

        /** 自定义范围结束 */
        private String customRangeEnd;

        /** 下拉选项 */
        private List<DropdownOption> dropdownOptions;

        /** 验证规则 */
        private Map<String, Object> validationRules;

        public String getDisplayMode() {
            return displayMode;
        }

        public void setDisplayMode(String displayMode) {
            this.displayMode = displayMode;
        }

        public String getDisplayType() {
            return displayType;
        }

        public void setDisplayType(String displayType) {
            this.displayType = displayType;
        }

        public String getTimeGranularity() {
            return timeGranularity;
        }

        public void setTimeGranularity(String timeGranularity) {
            this.timeGranularity = timeGranularity;
        }

        public String getDefaultTimeRange() {
            return defaultTimeRange;
        }

        public void setDefaultTimeRange(String defaultTimeRange) {
            this.defaultTimeRange = defaultTimeRange;
        }

        public String getCustomRangeStart() {
            return customRangeStart;
        }

        public void setCustomRangeStart(String customRangeStart) {
            this.customRangeStart = customRangeStart;
        }

        public String getCustomRangeEnd() {
            return customRangeEnd;
        }

        public void setCustomRangeEnd(String customRangeEnd) {
            this.customRangeEnd = customRangeEnd;
        }

        public List<DropdownOption> getDropdownOptions() {
            return dropdownOptions;
        }

        public void setDropdownOptions(List<DropdownOption> dropdownOptions) {
            this.dropdownOptions = dropdownOptions;
        }

        public Map<String, Object> getValidationRules() {
            return validationRules;
        }

        public void setValidationRules(Map<String, Object> validationRules) {
            this.validationRules = validationRules;
        }
    }

    /**
     * 下拉选项
     */
    public static class DropdownOption implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 选项标签 */
        private String label;

        /** 选项值 */
        private String value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 条件映射DTO
     */
    public static class ConditionMappingDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 映射ID (新增时为null) */
        private Long id;

        /** 表名 */
        @NotEmpty(message = "表名不能为空")
        private String tableName;

        /** 字段名 */
        @NotEmpty(message = "字段名不能为空")
        private String fieldName;

        /** 映射类型 (auto=自动, custom=自定义) */
        private String mappingType;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getMappingType() {
            return mappingType;
        }

        public void setMappingType(String mappingType) {
            this.mappingType = mappingType;
        }
    }
}
