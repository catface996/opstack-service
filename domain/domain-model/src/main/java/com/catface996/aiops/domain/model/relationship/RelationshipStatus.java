package com.catface996.aiops.domain.model.relationship;

/**
 * 关系状态枚举
 *
 * 定义资源关系的运行状态
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public enum RelationshipStatus {
    /**
     * 正常状态
     * 关系中的源节点和目标节点均正常
     */
    NORMAL("正常"),

    /**
     * 异常状态
     * 关系中的源节点或目标节点存在异常
     */
    ABNORMAL("异常");

    private final String description;

    RelationshipStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否为正常状态
     */
    public boolean isNormal() {
        return this == NORMAL;
    }
}
