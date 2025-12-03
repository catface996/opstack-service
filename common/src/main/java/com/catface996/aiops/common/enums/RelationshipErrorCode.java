package com.catface996.aiops.common.enums;

/**
 * 资源关系相关错误码
 *
 * <p>包括关系不存在、关系冲突、循环依赖等。</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public enum RelationshipErrorCode implements ErrorCode {

    // ==================== 关系不存在 (404) ====================

    /**
     * 关系不存在
     */
    RELATIONSHIP_NOT_FOUND("REL_NOT_FOUND_001", "关系不存在: {0}"),

    /**
     * 源资源不存在
     */
    SOURCE_RESOURCE_NOT_FOUND("REL_NOT_FOUND_002", "源资源不存在: {0}"),

    /**
     * 目标资源不存在
     */
    TARGET_RESOURCE_NOT_FOUND("REL_NOT_FOUND_003", "目标资源不存在: {0}"),

    // ==================== 关系冲突 (409) ====================

    /**
     * 关系已存在
     */
    RELATIONSHIP_ALREADY_EXISTS("REL_CONFLICT_001", "源资源{0}和目标资源{1}之间的{2}关系已存在"),

    /**
     * 不能创建自引用关系
     */
    SELF_REFERENCE_NOT_ALLOWED("REL_CONFLICT_002", "资源不能与自身建立关系"),

    // ==================== 业务规则 (400) ====================

    /**
     * 检测到循环依赖
     */
    CYCLE_DEPENDENCY_DETECTED("REL_CYCLE_001", "检测到循环依赖，路径: {0}"),

    /**
     * 无效的关系类型
     */
    INVALID_RELATIONSHIP_TYPE("REL_PARAM_001", "无效的关系类型: {0}"),

    /**
     * 无效的关系方向
     */
    INVALID_RELATIONSHIP_DIRECTION("REL_PARAM_002", "无效的关系方向: {0}"),

    /**
     * 无效的关系强度
     */
    INVALID_RELATIONSHIP_STRENGTH("REL_PARAM_003", "无效的关系强度: {0}"),

    /**
     * 无效的关系状态
     */
    INVALID_RELATIONSHIP_STATUS("REL_PARAM_004", "无效的关系状态: {0}"),

    /**
     * 遍历深度超出限制
     */
    TRAVERSE_DEPTH_EXCEEDED("REL_LIMIT_001", "遍历深度超出限制，最大允许: {0}");

    private final String code;
    private final String message;

    RelationshipErrorCode(String code, String message) {
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
