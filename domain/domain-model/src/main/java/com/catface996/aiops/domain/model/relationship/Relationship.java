package com.catface996.aiops.domain.model.relationship;

import java.time.LocalDateTime;

/**
 * 资源关系聚合根
 *
 * 表示两个IT资源之间的有向或双向关系，描述它们的交互或依赖关系
 *
 * <p>注意：此类保留用于 API 兼容性（TraverseResult）。内部实现已迁移到 Node2Node。</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public class Relationship {

    /**
     * 关系ID
     */
    private Long id;

    /**
     * 源资源ID
     */
    private Long sourceResourceId;

    /**
     * 目标资源ID
     */
    private Long targetResourceId;

    /**
     * 关系类型
     */
    private RelationshipType relationshipType;

    /**
     * 关系方向
     */
    private RelationshipDirection direction;

    /**
     * 关系强度
     */
    private RelationshipStrength strength;

    /**
     * 关系状态
     */
    private RelationshipStatus status;

    /**
     * 关系描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 关联对象（用于展示，非持久化）
    /**
     * 源资源名称
     */
    private String sourceResourceName;

    /**
     * 目标资源名称
     */
    private String targetResourceName;

    // 构造函数
    public Relationship() {
        this.status = RelationshipStatus.NORMAL;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceResourceId() {
        return sourceResourceId;
    }

    public void setSourceResourceId(Long sourceResourceId) {
        this.sourceResourceId = sourceResourceId;
    }

    public Long getTargetResourceId() {
        return targetResourceId;
    }

    public void setTargetResourceId(Long targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public RelationshipDirection getDirection() {
        return direction;
    }

    public void setDirection(RelationshipDirection direction) {
        this.direction = direction;
    }

    public RelationshipStrength getStrength() {
        return strength;
    }

    public void setStrength(RelationshipStrength strength) {
        this.strength = strength;
    }

    public RelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(RelationshipStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getSourceResourceName() {
        return sourceResourceName;
    }

    public void setSourceResourceName(String sourceResourceName) {
        this.sourceResourceName = sourceResourceName;
    }

    public String getTargetResourceName() {
        return targetResourceName;
    }

    public void setTargetResourceName(String targetResourceName) {
        this.targetResourceName = targetResourceName;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "id=" + id +
                ", sourceResourceId=" + sourceResourceId +
                ", targetResourceId=" + targetResourceId +
                ", relationshipType=" + relationshipType +
                ", direction=" + direction +
                ", strength=" + strength +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
