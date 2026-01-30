package com.zjrcu.iras.bi.platform.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;

/**
 * BI数据源对象 bi_datasource
 *
 * @author iras
 */
public class DataSource extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 数据源ID
     */
    private Long id;

    /**
     * 数据源名称
     */
    @NotBlank(message = "数据源名称不能为空")
    @Size(max = 100, message = "数据源名称长度不能超过100个字符")
    private String name;

    /**
     * 数据源类型: mysql, postgresql, clickhouse, doris, oracle, api, file
     */
    @NotBlank(message = "数据源类型不能为空")
    @Size(max = 20, message = "数据源类型长度不能超过20个字符")
    private String type;

    /**
     * 连接配置(JSON格式,加密存储)
     */
    @NotBlank(message = "连接配置不能为空")
    private String config;

    /**
     * 状态: 0正常 1停用
     */
    private String status;

    /**
     * 解析后的配置对象(不持久化到数据库)
     */
    @JsonIgnore
    private transient Map<String, Object> configMap;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        // 清除缓存的配置对象
        this.configMap = null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取解析后的配置对象
     *
     * @return 配置Map
     */
    public Map<String, Object> getConfigMap() {
        if (configMap == null && config != null && !config.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                configMap = mapper.readValue(config, Map.class);
            } catch (JsonProcessingException e) {
                configMap = new HashMap<>();
            }
        }
        return configMap != null ? configMap : new HashMap<>();
    }

    /**
     * 设置配置对象并转换为JSON字符串
     *
     * @param configMap 配置Map
     */
    public void setConfigMap(Map<String, Object> configMap) {
        this.configMap = configMap;
        if (configMap != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.config = mapper.writeValueAsString(configMap);
            } catch (JsonProcessingException e) {
                this.config = "{}";
            }
        }
    }

    /**
     * 获取配置项的值
     *
     * @param key 配置键
     * @return 配置值
     */
    public Object getConfigValue(String key) {
        return getConfigMap().get(key);
    }

    /**
     * 获取配置项的字符串值
     *
     * @param key 配置键
     * @return 配置值
     */
    public String getConfigString(String key) {
        Object value = getConfigValue(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取配置项的整数值
     *
     * @param key 配置键
     * @return 配置值
     */
    public Integer getConfigInteger(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("type", getType())
                .append("config", "[PROTECTED]") // 不输出敏感配置信息
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
