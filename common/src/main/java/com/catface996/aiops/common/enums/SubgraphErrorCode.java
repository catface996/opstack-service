package com.catface996.aiops.common.enums;

/**
 * 子图管理相关错误码
 *
 * <p>包括子图不存在、名称冲突、权限不足等。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public enum SubgraphErrorCode implements ErrorCode {

    // ==================== 子图不存在 (404) ====================

    /**
     * 子图不存在
     */
    SUBGRAPH_NOT_FOUND("SUBGRAPH_NOT_FOUND_001", "子图不存在"),

    // ==================== 名称冲突 (409) ====================

    /**
     * 子图名称已存在
     */
    SUBGRAPH_NAME_CONFLICT("SUBGRAPH_CONFLICT_001", "子图名称已存在"),

    /**
     * 子图版本冲突（乐观锁）
     */
    SUBGRAPH_VERSION_CONFLICT("SUBGRAPH_CONFLICT_002", "子图已被其他用户修改，请刷新后重试"),

    // ==================== 权限不足 (403) ====================

    /**
     * 无权限访问子图
     */
    SUBGRAPH_ACCESS_DENIED("SUBGRAPH_AUTHZ_001", "您没有权限访问此子图"),

    /**
     * 无权限编辑子图
     */
    SUBGRAPH_EDIT_DENIED("SUBGRAPH_AUTHZ_002", "您没有权限编辑此子图，仅Owner可编辑"),

    /**
     * 无权限删除子图
     */
    SUBGRAPH_DELETE_DENIED("SUBGRAPH_AUTHZ_003", "您没有权限删除此子图，仅Owner可删除"),

    /**
     * 无权限管理子图资源
     */
    SUBGRAPH_RESOURCE_DENIED("SUBGRAPH_AUTHZ_004", "您没有权限管理子图资源，仅Owner可操作"),

    /**
     * 无权限管理子图权限
     */
    SUBGRAPH_PERMISSION_DENIED("SUBGRAPH_AUTHZ_005", "您没有权限管理子图权限，仅Owner可操作"),

    // ==================== 业务规则 (400) ====================

    /**
     * 子图非空，无法删除
     */
    SUBGRAPH_NOT_EMPTY("SUBGRAPH_BIZ_001", "子图中包含资源节点，请先移除所有资源后再删除"),

    /**
     * 不能移除最后一个Owner
     */
    SUBGRAPH_LAST_OWNER("SUBGRAPH_BIZ_002", "不能移除子图的最后一个Owner"),

    /**
     * 资源不存在
     */
    RESOURCE_NOT_EXISTS("SUBGRAPH_BIZ_003", "资源节点不存在"),

    /**
     * 资源已在子图中
     */
    RESOURCE_ALREADY_IN_SUBGRAPH("SUBGRAPH_BIZ_004", "资源节点已存在于子图中"),

    /**
     * 资源不在子图中
     */
    RESOURCE_NOT_IN_SUBGRAPH("SUBGRAPH_BIZ_005", "资源节点不在子图中"),

    /**
     * 用户已有权限
     */
    USER_ALREADY_HAS_PERMISSION("SUBGRAPH_BIZ_006", "用户已拥有该子图的权限");

    private final String code;
    private final String message;

    SubgraphErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
