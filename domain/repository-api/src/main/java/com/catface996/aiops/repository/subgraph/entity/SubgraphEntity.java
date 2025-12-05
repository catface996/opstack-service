package com.catface996.aiops.repository.subgraph.entity;

import java.time.LocalDateTime;

/**
 * 子图实体（用于数据库持久化）
 *
 * <p>与领域模型 Subgraph 的映射关系由仓储实现层处理。</p>
 *
 * <p>数据库表：t_subgraph</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1: 子图创建</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public class SubgraphEntity {

    /**
     * 子图ID（主键，自增）
     */
    private Long id;

    /**
     * 子图名称（全局唯一）
     */
    private String name;

    /**
     * 子图描述
     */
    private String description;

    /**
     * 标签（JSON数组格式）
     */
    private String tags;

    /**
     * 元数据（JSON对象格式）
     */
    private String metadata;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    // ==================== Getters and Setters ====================

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SubgraphEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tags='" + tags + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
