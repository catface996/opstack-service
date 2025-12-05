package com.catface996.aiops.domain.model.subgraph;

import java.time.LocalDateTime;

/**
 * 子图资源关联实体
 *
 * <p>记录子图与资源节点之间的关联关系。</p>
 * <p>一个资源节点可以同时属于多个子图（多对多关系）。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求5: 向子图添加资源节点</li>
 *   <li>需求6: 从子图移除资源节点</li>
 *   <li>需求澄清2: 资源节点与子图的多对多关系</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public class SubgraphResource {

    /**
     * 关联ID（主键）
     */
    private Long id;

    /**
     * 子图ID（外键）
     */
    private Long subgraphId;

    /**
     * 资源节点ID（外键）
     */
    private Long resourceId;

    /**
     * 添加时间
     */
    private LocalDateTime addedAt;

    /**
     * 添加者ID
     */
    private Long addedBy;

    // 构造函数
    public SubgraphResource() {
    }

    public SubgraphResource(Long id, Long subgraphId, Long resourceId,
                             LocalDateTime addedAt, Long addedBy) {
        this.id = id;
        this.subgraphId = subgraphId;
        this.resourceId = resourceId;
        this.addedAt = addedAt;
        this.addedBy = addedBy;
    }

    // 业务方法

    /**
     * 创建新关联的工厂方法
     *
     * @param subgraphId 子图ID
     * @param resourceId 资源节点ID
     * @param addedBy 添加者ID
     * @return 新创建的关联实例
     */
    public static SubgraphResource create(Long subgraphId, Long resourceId, Long addedBy) {
        SubgraphResource subgraphResource = new SubgraphResource();
        subgraphResource.setSubgraphId(subgraphId);
        subgraphResource.setResourceId(resourceId);
        subgraphResource.setAddedAt(LocalDateTime.now());
        subgraphResource.setAddedBy(addedBy);
        return subgraphResource;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Long getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Long addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public String toString() {
        return "SubgraphResource{" +
                "id=" + id +
                ", subgraphId=" + subgraphId +
                ", resourceId=" + resourceId +
                ", addedAt=" + addedAt +
                ", addedBy=" + addedBy +
                '}';
    }
}
