package com.catface996.aiops.domain.model.relationship;

/**
 * 关系强度枚举
 *
 * 定义资源之间依赖的关键程度
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public enum RelationshipStrength {
    /**
     * 强依赖
     * 目标资源不可用时，源资源将完全无法工作
     */
    STRONG("强依赖"),

    /**
     * 弱依赖
     * 目标资源不可用时，源资源仍可部分工作或降级运行
     */
    WEAK("弱依赖");

    private final String description;

    RelationshipStrength(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否为强依赖
     */
    public boolean isStrong() {
        return this == STRONG;
    }
}
