package com.catface996.aiops.domain.model.relationship;

/**
 * 关系类型枚举
 *
 * 定义资源之间关系的类别
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public enum RelationshipType {
    /**
     * 依赖关系
     * 例如：应用依赖数据库
     */
    DEPENDENCY("依赖"),

    /**
     * 调用关系
     * 例如：服务A调用服务B
     */
    CALL("调用"),

    /**
     * 部署关系
     * 例如：应用部署在服务器上
     */
    DEPLOYMENT("部署"),

    /**
     * 归属关系
     * 例如：API归属于应用
     */
    OWNERSHIP("归属"),

    /**
     * 关联关系
     * 例如：两个资源存在某种关联
     */
    ASSOCIATION("关联");

    private final String description;

    RelationshipType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
