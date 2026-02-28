package com.zjrcu.iras.bi.metric.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 血缘边对象 bi_lineage_edge
 *
 * @author iras
 * @date 2025-02-26
 */
public class LineageEdge {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 源节点ID */
    @Excel(name = "源节点ID")
    private Long sourceNodeId;

    /** 目标节点ID */
    @Excel(name = "目标节点ID")
    private Long targetNodeId;

    /** 边类型:flows_to,derived_from,depends_on */
    @Excel(name = "边类型", readConverterExp = "flows_to=流向,derived_from=派生自,depends_on=依赖于")
    private String edgeType;

    /** 边属性(JSON格式) */
    private String edgeAttributes;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 源节点对象(不持久化到数据库) */
    private transient LineageNode sourceNode;

    /** 目标节点对象(不持久化到数据库) */
    private transient LineageNode targetNode;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public Long getSourceNodeId() {
        return sourceNodeId;
    }

    public void setTargetNodeId(Long targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public Long getTargetNodeId() {
        return targetNodeId;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeAttributes(String edgeAttributes) {
        this.edgeAttributes = edgeAttributes;
    }

    public String getEdgeAttributes() {
        return edgeAttributes;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public LineageNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(LineageNode sourceNode) {
        this.sourceNode = sourceNode;
    }

    public LineageNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(LineageNode targetNode) {
        this.targetNode = targetNode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("sourceNodeId", getSourceNodeId())
                .append("targetNodeId", getTargetNodeId())
                .append("edgeType", getEdgeType())
                .append("edgeAttributes", getEdgeAttributes())
                .append("createTime", getCreateTime())
                .toString();
    }

    /**
     * 判断是否为流向关系
     */
    public boolean isFlowsTo() {
        return "flows_to".equalsIgnoreCase(this.edgeType);
    }

    /**
     * 判断是否为派生关系
     */
    public boolean isDerivedFrom() {
        return "derived_from".equalsIgnoreCase(this.edgeType);
    }

    /**
     * 判断是否为依赖关系
     */
    public boolean isDependsOn() {
        return "depends_on".equalsIgnoreCase(this.edgeType);
    }

    /**
     * 获取边类型的显示名称
     */
    public String getEdgeTypeName() {
        if (edgeType == null) {
            return "未知";
        }
        switch (edgeType.toLowerCase()) {
            case "flows_to":
                return "流向";
            case "derived_from":
                return "派生自";
            case "depends_on":
                return "依赖于";
            default:
                return edgeType;
        }
    }
}
