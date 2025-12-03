package com.catface996.aiops.domain.model.relationship;

import java.util.*;

/**
 * 图遍历结果
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public class TraverseResult {

    /**
     * 起始资源ID
     */
    private final Long startResourceId;

    /**
     * 遍历的所有节点（按层级分组）
     */
    private final Map<Integer, List<Long>> nodesByLevel;

    /**
     * 所有遍历的关系
     */
    private final List<Relationship> relationships;

    /**
     * 实际遍历的最大深度
     */
    private final int actualDepth;

    /**
     * 访问的节点总数
     */
    private final int totalNodes;

    public TraverseResult(Long startResourceId, Map<Integer, List<Long>> nodesByLevel,
                          List<Relationship> relationships) {
        this.startResourceId = startResourceId;
        this.nodesByLevel = nodesByLevel != null ? nodesByLevel : Collections.emptyMap();
        this.relationships = relationships != null ? relationships : Collections.emptyList();
        this.actualDepth = this.nodesByLevel.isEmpty() ? 0 : Collections.max(this.nodesByLevel.keySet());
        this.totalNodes = this.nodesByLevel.values().stream().mapToInt(List::size).sum();
    }

    /**
     * 创建空的遍历结果
     */
    public static TraverseResult empty(Long startResourceId) {
        Map<Integer, List<Long>> nodesByLevel = new HashMap<>();
        nodesByLevel.put(0, Collections.singletonList(startResourceId));
        return new TraverseResult(startResourceId, nodesByLevel, Collections.emptyList());
    }

    public Long getStartResourceId() {
        return startResourceId;
    }

    public Map<Integer, List<Long>> getNodesByLevel() {
        return nodesByLevel;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public int getActualDepth() {
        return actualDepth;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    /**
     * 获取所有访问的节点ID
     */
    public Set<Long> getAllNodeIds() {
        Set<Long> allNodes = new HashSet<>();
        nodesByLevel.values().forEach(allNodes::addAll);
        return allNodes;
    }

    /**
     * 获取指定层级的节点
     */
    public List<Long> getNodesAtLevel(int level) {
        return nodesByLevel.getOrDefault(level, Collections.emptyList());
    }

    @Override
    public String toString() {
        return "TraverseResult{" +
                "startResourceId=" + startResourceId +
                ", actualDepth=" + actualDepth +
                ", totalNodes=" + totalNodes +
                ", relationshipsCount=" + relationships.size() +
                '}';
    }
}
