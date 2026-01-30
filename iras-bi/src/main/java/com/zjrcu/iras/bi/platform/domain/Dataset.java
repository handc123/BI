package com.zjrcu.iras.bi.platform.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * BI数据集对象 bi_dataset
 *
 * @author iras
 */
public class Dataset extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 数据集ID
     */
    private Long id;

    /**
     * 数据集名称
     */
    @NotBlank(message = "数据集名称不能为空")
    @Size(max = 100, message = "数据集名称长度不能超过100个字符")
    private String name;

    /**
     * 数据源ID
     */
    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;

    /**
     * 数据集类型: direct, extract
     */
    @NotBlank(message = "数据集类型不能为空")
    @Size(max = 20, message = "数据集类型长度不能超过20个字符")
    private String type;

    /**
     * 查询配置(JSON格式)
     */
    @NotBlank(message = "查询配置不能为空")
    private String queryConfig;

    /**
     * 字段配置(JSON格式)
     */
    private String fieldConfig;

    /**
     * 抽取配置(JSON格式,仅抽取类型)
     */
    private String extractConfig;

    /**
     * 最后抽取时间
     */
    private Date lastExtractTime;

    /**
     * 数据行数
     */
    private Long rowCount;

    /**
     * 状态: 0正常 1停用
     */
    private String status;

    /**
     * 关联的数据源对象(不持久化到数据库)
     */
    @JsonIgnore
    private transient DataSource dataSource;

    /**
     * 解析后的查询配置对象(不持久化到数据库)
     */
    @JsonIgnore
    private transient Map<String, Object> queryConfigMap;

    /**
     * 解析后的字段配置对象(不持久化到数据库)
     */
    @JsonIgnore
    private transient Map<String, Object> fieldConfigMap;

    /**
     * 解析后的抽取配置对象(不持久化到数据库)
     */
    @JsonIgnore
    private transient Map<String, Object> extractConfigMap;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Long datasourceId) {
        this.datasourceId = datasourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQueryConfig() {
        return queryConfig;
    }

    public void setQueryConfig(String queryConfig) {
        this.queryConfig = queryConfig;
        this.queryConfigMap = null;
    }

    public String getFieldConfig() {
        return fieldConfig;
    }

    public void setFieldConfig(String fieldConfig) {
        this.fieldConfig = fieldConfig;
        this.fieldConfigMap = null;
    }

    public String getExtractConfig() {
        return extractConfig;
    }

    public void setExtractConfig(String extractConfig) {
        this.extractConfig = extractConfig;
        this.extractConfigMap = null;
    }

    public Date getLastExtractTime() {
        return lastExtractTime;
    }

    public void setLastExtractTime(Date lastExtractTime) {
        this.lastExtractTime = lastExtractTime;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取解析后的查询配置对象
     *
     * @return 查询配置Map
     */
    public Map<String, Object> getQueryConfigMap() {
        if (queryConfigMap == null && queryConfig != null && !queryConfig.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                queryConfigMap = mapper.readValue(queryConfig, Map.class);
            } catch (JsonProcessingException e) {
                queryConfigMap = new HashMap<>();
            }
        }
        return queryConfigMap != null ? queryConfigMap : new HashMap<>();
    }

    /**
     * 设置查询配置对象并转换为JSON字符串
     *
     * @param queryConfigMap 查询配置Map
     */
    public void setQueryConfigMap(Map<String, Object> queryConfigMap) {
        this.queryConfigMap = queryConfigMap;
        if (queryConfigMap != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.queryConfig = mapper.writeValueAsString(queryConfigMap);
            } catch (JsonProcessingException e) {
                this.queryConfig = "{}";
            }
        }
    }

    /**
     * 获取解析后的字段配置对象
     *
     * @return 字段配置Map
     */
    public Map<String, Object> getFieldConfigMap() {
        if (fieldConfigMap == null && fieldConfig != null && !fieldConfig.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                fieldConfigMap = mapper.readValue(fieldConfig, Map.class);
            } catch (JsonProcessingException e) {
                fieldConfigMap = new HashMap<>();
            }
        }
        return fieldConfigMap != null ? fieldConfigMap : new HashMap<>();
    }

    /**
     * 设置字段配置对象并转换为JSON字符串
     *
     * @param fieldConfigMap 字段配置Map
     */
    public void setFieldConfigMap(Map<String, Object> fieldConfigMap) {
        this.fieldConfigMap = fieldConfigMap;
        if (fieldConfigMap != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.fieldConfig = mapper.writeValueAsString(fieldConfigMap);
            } catch (JsonProcessingException e) {
                this.fieldConfig = "{}";
            }
        }
    }

    /**
     * 获取解析后的抽取配置对象
     *
     * @return 抽取配置Map
     */
    public Map<String, Object> getExtractConfigMap() {
        if (extractConfigMap == null && extractConfig != null && !extractConfig.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                extractConfigMap = mapper.readValue(extractConfig, Map.class);
            } catch (JsonProcessingException e) {
                extractConfigMap = new HashMap<>();
            }
        }
        return extractConfigMap != null ? extractConfigMap : new HashMap<>();
    }

    /**
     * 设置抽取配置对象并转换为JSON字符串
     *
     * @param extractConfigMap 抽取配置Map
     */
    public void setExtractConfigMap(Map<String, Object> extractConfigMap) {
        this.extractConfigMap = extractConfigMap;
        if (extractConfigMap != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.extractConfig = mapper.writeValueAsString(extractConfigMap);
            } catch (JsonProcessingException e) {
                this.extractConfig = "{}";
            }
        }
    }

    /**
     * 判断是否为直连数据集
     *
     * @return true表示直连数据集
     */
    public boolean isDirect() {
        return "direct".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为抽取数据集
     *
     * @return true表示抽取数据集
     */
    public boolean isExtract() {
        return "extract".equalsIgnoreCase(type);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("datasourceId", getDatasourceId())
                .append("type", getType())
                .append("queryConfig", getQueryConfig())
                .append("fieldConfig", getFieldConfig())
                .append("extractConfig", getExtractConfig())
                .append("lastExtractTime", getLastExtractTime())
                .append("rowCount", getRowCount())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
