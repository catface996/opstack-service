package com.catface996.aiops.domain.model.relationship;

/**
 * 关系方向枚举
 *
 * 定义资源之间关系的方向性
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public enum RelationshipDirection {
    /**
     * 单向关系
     * 从源节点指向目标节点
     */
    UNIDIRECTIONAL("单向"),

    /**
     * 双向关系
     * 源节点和目标节点互相关联
     */
    BIDIRECTIONAL("双向");

    private final String description;

    RelationshipDirection(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否为双向关系
     */
    public boolean isBidirectional() {
        return this == BIDIRECTIONAL;
    }
}
