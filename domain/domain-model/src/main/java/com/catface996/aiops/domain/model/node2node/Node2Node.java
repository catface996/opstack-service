package com.catface996.aiops.domain.model.node2node;

import com.catface996.aiops.domain.model.relationship.*;

import java.time.LocalDateTime;

/**
 * 节点关系领域模型
 *
 * <p>表示两个节点之间的有向或双向关系，描述它们的交互或依赖关系。</p>
 * <p>此模型替代原有的 Relationship 领域模型。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class Node2Node {

    /**
     * 关系ID
     */
    private Long id;

    /**
     * 源节点ID
     */
    private Long sourceId;

    /**
     * 目标节点ID
     */
    private Long targetId;

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
     * 源节点名称
     */
    private String sourceName;

    /**
     * 目标节点名称
     */
    private String targetName;

    // 构造函数
    public Node2Node() {
        this.status = RelationshipStatus.NORMAL;
    }

    public Node2Node(Long id, Long sourceId, Long targetId,
                     RelationshipType relationshipType, RelationshipDirection direction,
                     RelationshipStrength strength, RelationshipStatus status,
                     String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.relationshipType = relationshipType;
        this.direction = direction;
        this.strength = strength;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 业务方法

    /**
     * 创建新关系的工厂方法
     */
    public static Node2Node create(Long sourceId, Long targetId,
                                   RelationshipType type, RelationshipDirection direction,
                                   RelationshipStrength strength, String description) {
        Node2Node node2Node = new Node2Node();
        node2Node.setSourceId(sourceId);
        node2Node.setTargetId(targetId);
        node2Node.setRelationshipType(type);
        node2Node.setDirection(direction);
        node2Node.setStrength(strength);
        node2Node.setDescription(description);
        node2Node.setStatus(RelationshipStatus.NORMAL);
        node2Node.setCreatedAt(LocalDateTime.now());
        node2Node.setUpdatedAt(LocalDateTime.now());
        return node2Node;
    }

    /**
     * 创建反向关系（用于双向关系）
     */
    public Node2Node createReverseRelationship() {
        return Node2Node.create(
                this.targetId,
                this.sourceId,
                this.relationshipType,
                this.direction,
                this.strength,
                this.description
        );
    }

    /**
     * 判断是否为双向关系
     */
    public boolean isBidirectional() {
        return direction != null && direction.isBidirectional();
    }

    /**
     * 判断用户是否有权限操作此关系
     *
     * @param userId 用户ID
     * @param sourceCreatedBy 源节点创建者ID
     * @param targetCreatedBy 目标节点创建者ID
     * @return true if user is owner of source or target node
     */
    public boolean isOwner(Long userId, Long sourceCreatedBy, Long targetCreatedBy) {
        if (userId == null) {
            return false;
        }
        boolean isSourceOwner = sourceCreatedBy != null && sourceCreatedBy.equals(userId);
        boolean isTargetOwner = targetCreatedBy != null && targetCreatedBy.equals(userId);
        return isSourceOwner || isTargetOwner;
    }

    /**
     * 更新关系属性
     */
    public void update(RelationshipType type, RelationshipStrength strength,
                       RelationshipStatus status, String description) {
        if (type != null) {
            this.relationshipType = type;
        }
        if (strength != null) {
            this.strength = strength;
        }
        if (status != null) {
            this.status = status;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @Override
    public String toString() {
        return "Node2Node{" +
                "id=" + id +
                ", sourceId=" + sourceId +
                ", targetId=" + targetId +
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
