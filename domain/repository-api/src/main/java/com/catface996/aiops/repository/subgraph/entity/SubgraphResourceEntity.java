package com.catface996.aiops.repository.subgraph.entity;

import java.time.LocalDateTime;

/**
 * 子图资源关联实体（用于数据库持久化）
 *
 * <p>与领域模型 SubgraphResource 的映射关系由仓储实现层处理。</p>
 *
 * <p>数据库表：t_subgraph_resource</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求5: 向子图添加资源节点</li>
 *   <li>需求6: 从子图移除资源节点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public class SubgraphResourceEntity {

    /**
     * 关联ID（主键，自增）
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

    // ==================== Getters and Setters ====================

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
        return "SubgraphResourceEntity{" +
                "id=" + id +
                ", subgraphId=" + subgraphId +
                ", resourceId=" + resourceId +
                ", addedAt=" + addedAt +
                ", addedBy=" + addedBy +
                '}';
    }
}
