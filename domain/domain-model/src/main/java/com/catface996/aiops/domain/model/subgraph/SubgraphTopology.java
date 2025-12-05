package com.catface996.aiops.domain.model.subgraph;

import com.catface996.aiops.domain.model.relationship.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * 子图拓扑结构
 *
 * <p>表示子图中资源节点及其之间关系的拓扑图结构。</p>
 * <p>只包含子图内节点之间的关系，不包含与外部节点的关系。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求7.3: 拓扑图只显示子图内节点之间的关系</li>
 *   <li>需求7.4: 拓扑图使用与主拓扑图相同的视觉样式</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public class SubgraphTopology {

    /**
     * 子图ID
     */
    private Long subgraphId;

    /**
     * 子图名称
     */
    private String subgraphName;

    /**
     * 子图中的资源节点ID列表
     */
    private List<Long> resourceIds;

    /**
     * 子图内节点之间的关系列表
     */
    private List<Relationship> relationships;

    /**
     * 节点总数
     */
    private int nodeCount;

    /**
     * 边（关系）总数
     */
    private int edgeCount;

    public SubgraphTopology() {
        this.resourceIds = new ArrayList<>();
        this.relationships = new ArrayList<>();
    }

    public SubgraphTopology(Long subgraphId, String subgraphName, List<Long> resourceIds, List<Relationship> relationships) {
        this.subgraphId = subgraphId;
        this.subgraphName = subgraphName;
        this.resourceIds = resourceIds != null ? new ArrayList<>(resourceIds) : new ArrayList<>();
        this.relationships = relationships != null ? new ArrayList<>(relationships) : new ArrayList<>();
        this.nodeCount = this.resourceIds.size();
        this.edgeCount = this.relationships.size();
    }

    // Getters and Setters

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public String getSubgraphName() {
        return subgraphName;
    }

    public void setSubgraphName(String subgraphName) {
        this.subgraphName = subgraphName;
    }

    public List<Long> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds != null ? new ArrayList<>(resourceIds) : new ArrayList<>();
        this.nodeCount = this.resourceIds.size();
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships != null ? new ArrayList<>(relationships) : new ArrayList<>();
        this.edgeCount = this.relationships.size();
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    @Override
    public String toString() {
        return "SubgraphTopology{" +
                "subgraphId=" + subgraphId +
                ", subgraphName='" + subgraphName + '\'' +
                ", nodeCount=" + nodeCount +
                ", edgeCount=" + edgeCount +
                '}';
    }
}
