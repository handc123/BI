package com.zjrcu.iras.bi.metric.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 血缘路径数据传输对象
 * 用于表示两个指标之间的血缘路径
 *
 * @author iras
 * @date 2025-02-26
 */
public class LineagePathDTO {

    /** 路径上的节点ID列表 */
    private List<Long> nodeIds;

    /** 路径上的节点名称列表 */
    private List<String> nodeNames;

    /** 路径长度（边的数量） */
    private Integer length;

    /** 路径权重（所有边的权重之和） */
    private Double weight;

    /** 路径类型: upstream, downstream, bidirectional */
    private String pathType;

    /** 转换类型列表 */
    private List<String> transformationTypes;

    /** 路径描述 */
    private String description;

    public LineagePathDTO() {
        this.nodeIds = new ArrayList<>();
        this.nodeNames = new ArrayList<>();
        this.transformationTypes = new ArrayList<>();
    }

    public LineagePathDTO(List<Long> nodeIds, List<String> nodeNames) {
        this.nodeIds = nodeIds != null ? nodeIds : new ArrayList<>();
        this.nodeNames = nodeNames != null ? nodeNames : new ArrayList<>();
        this.transformationTypes = new ArrayList<>();
        this.length = this.nodeIds.size() - 1;
    }

    public List<Long> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<Long> nodeIds) {
        this.nodeIds = nodeIds;
        if (this.nodeIds != null && this.nodeIds.size() > 1) {
            this.length = this.nodeIds.size() - 1;
        }
    }

    public List<String> getNodeNames() {
        return nodeNames;
    }

    public void setNodeNames(List<String> nodeNames) {
        this.nodeNames = nodeNames;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getPathType() {
        return pathType;
    }

    public void setPathType(String pathType) {
        this.pathType = pathType;
    }

    public List<String> getTransformationTypes() {
        return transformationTypes;
    }

    public void setTransformationTypes(List<String> transformationTypes) {
        this.transformationTypes = transformationTypes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 添加节点到路径
     */
    public void addNode(Long nodeId, String nodeName) {
        if (this.nodeIds == null) {
            this.nodeIds = new ArrayList<>();
        }
        if (this.nodeNames == null) {
            this.nodeNames = new ArrayList<>();
        }
        this.nodeIds.add(nodeId);
        this.nodeNames.add(nodeName);
        this.length = this.nodeIds.size() - 1;
    }

    /**
     * 添加转换类型
     */
    public void addTransformationType(String transformationType) {
        if (this.transformationTypes == null) {
            this.transformationTypes = new ArrayList<>();
        }
        this.transformationTypes.add(transformationType);
    }

    /**
     * 获取起始节点ID
     */
    public Long getSourceNodeId() {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return null;
        }
        return nodeIds.get(0);
    }

    /**
     * 获取目标节点ID
     */
    public Long getTargetNodeId() {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return null;
        }
        return nodeIds.get(nodeIds.size() - 1);
    }

    /**
     * 判断是否为有效路径
     */
    public boolean isValid() {
        return nodeIds != null && nodeIds.size() >= 2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LineagePath{");
        if (nodeNames != null && !nodeNames.isEmpty()) {
            sb.append("path=");
            for (int i = 0; i < nodeNames.size(); i++) {
                if (i > 0) sb.append(" -> ");
                sb.append(nodeNames.get(i));
            }
        }
        sb.append(", length=").append(length);
        sb.append(", weight=").append(weight);
        sb.append("}");
        return sb.toString();
    }
}
