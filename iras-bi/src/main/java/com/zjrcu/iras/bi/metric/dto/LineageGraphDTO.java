package com.zjrcu.iras.bi.metric.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 血缘图数据传输对象
 *
 * @author iras
 * @date 2025-02-26
 */
public class LineageGraphDTO {

    /** 图节点列表 */
    private List<Node> nodes;

    /** 图边列表 */
    private List<Edge> edges;

    /** 图元数据 */
    private Map<String, Object> metadata;

    public LineageGraphDTO() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public LineageGraphDTO(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * 添加节点
     */
    public void addNode(Node node) {
        if (this.nodes == null) {
            this.nodes = new ArrayList<>();
        }
        this.nodes.add(node);
    }

    /**
     * 添加边
     */
    public void addEdge(Edge edge) {
        if (this.edges == null) {
            this.edges = new ArrayList<>();
        }
        this.edges.add(edge);
    }

    /**
     * 图节点内部类
     */
    public static class Node {
        /** 节点唯一ID */
        private String id;

        /** 节点类型: metric, dataset, datasource, table, field */
        private String type;

        /** 节点名称 */
        private String name;

        /** 节点属性 */
        private Map<String, Object> attributes;

        /** 节点在图中的X坐标（可选，用于前端布局） */
        private Double x;

        /** 节点在图中的Y坐标（可选，用于前端布局） */
        private Double y;

        /** 节点大小（可选） */
        private Integer size;

        /** 节点颜色（可选） */
        private String color;

        public Node() {
            this.attributes = new HashMap<>();
        }

        public Node(String id, String type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.attributes = new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        /**
         * 添加属性
         */
        public void addAttribute(String key, Object value) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }
            this.attributes.put(key, value);
        }
    }

    /**
     * 图边内部类
     */
    public static class Edge {
        /** 边唯一ID */
        private String id;

        /** 源节点ID */
        private String source;

        /** 目标节点ID */
        private String target;

        /** 边类型: flows_to, derived_from, depends_on */
        private String type;

        /** 边属性 */
        private Map<String, Object> attributes;

        /** 边权重（可选） */
        private Double weight;

        /** 边标签（可选） */
        private String label;

        public Edge() {
            this.attributes = new HashMap<>();
        }

        public Edge(String id, String source, String target, String type) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.type = type;
            this.attributes = new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * 添加属性
         */
        public void addAttribute(String key, Object value) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }
            this.attributes.put(key, value);
        }
    }
}
