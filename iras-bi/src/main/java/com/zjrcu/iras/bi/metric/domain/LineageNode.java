package com.zjrcu.iras.bi.metric.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 血缘节点对象 bi_lineage_node
 *
 * @author iras
 * @date 2025-02-26
 */
public class LineageNode {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 节点类型:metric,dataset,datasource,table,field */
    @Excel(name = "节点类型", readConverterExp = "metric=指标,dataset=数据集,datasource=数据源,table=表,field=字段")
    private String nodeType;

    /** 节点业务ID */
    @Excel(name = "节点ID")
    private Long nodeId;

    /** 节点名称 */
    @Excel(name = "节点名称")
    private String nodeName;

    /** 节点属性(JSON格式) */
    private String nodeAttributes;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeAttributes(String nodeAttributes) {
        this.nodeAttributes = nodeAttributes;
    }

    public String getNodeAttributes() {
        return nodeAttributes;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("nodeType", getNodeType())
                .append("nodeId", getNodeId())
                .append("nodeName", getNodeName())
                .append("nodeAttributes", getNodeAttributes())
                .append("createTime", getCreateTime())
                .toString();
    }

    /**
     * 判断是否为指标节点
     */
    public boolean isMetric() {
        return "metric".equalsIgnoreCase(this.nodeType);
    }

    /**
     * 判断是否为数据集节点
     */
    public boolean isDataset() {
        return "dataset".equalsIgnoreCase(this.nodeType);
    }

    /**
     * 判断是否为数据源节点
     */
    public boolean isDatasource() {
        return "datasource".equalsIgnoreCase(this.nodeType);
    }

    /**
     * 获取节点唯一标识（类型+ID）
     */
    public String getUniqueKey() {
        return this.nodeType + ":" + this.nodeId;
    }
}
