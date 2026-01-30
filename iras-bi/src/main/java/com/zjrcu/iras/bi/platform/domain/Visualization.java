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

import java.util.HashMap;
import java.util.Map;

/**
 * BI可视化对象 bi_visualization
 *
 * @author iras
 */
public class Visualization extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 可视化ID
     */
    private Long id;

    /**
     * 可视化名称
     */
    @NotBlank(message = "可视化名称不能为空")
    @Size(max = 100, message = "可视化名称长度不能超过100个字符")
    private String name;

    /**
     * 数据集ID
     */
    @NotNull(message = "数据集ID不能为空")
    private Long datasetId;

    /**
     * 图表类型: kpi, line, bar, map, table, pie, donut, funnel
     */
    @NotBlank(message = "图表类型不能为空")
    @Size(max = 20, message = "图表类型长度不能超过20个字符")
    private String type;

    /**
     * 可视化配置(JSON格式)
     */
    @NotBlank(message = "可视化配置不能为空")
    private String config;

    /**
     * 关联的数据集对象(不持久化到数据库)
     */
    @JsonIgnore
    private transient Dataset dataset;

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

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
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
        this.configMap = null;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
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
     * 判断是否为指标卡类型
     *
     * @return true表示指标卡
     */
    public boolean isKpi() {
        return "kpi".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为折线图类型
     *
     * @return true表示折线图
     */
    public boolean isLine() {
        return "line".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为柱状图类型
     *
     * @return true表示柱状图
     */
    public boolean isBar() {
        return "bar".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为地图类型
     *
     * @return true表示地图
     */
    public boolean isMap() {
        return "map".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为表格类型
     *
     * @return true表示表格
     */
    public boolean isTable() {
        return "table".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为饼图类型
     *
     * @return true表示饼图
     */
    public boolean isPie() {
        return "pie".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为环形图类型
     *
     * @return true表示环形图
     */
    public boolean isDonut() {
        return "donut".equalsIgnoreCase(type);
    }

    /**
     * 判断是否为漏斗图类型
     *
     * @return true表示漏斗图
     */
    public boolean isFunnel() {
        return "funnel".equalsIgnoreCase(type);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("datasetId", getDatasetId())
                .append("type", getType())
                .append("config", getConfig())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
