package com.catface996.aiops.domain.model.subgraph;

/**
 * 子图权限角色枚举
 *
 * <p>定义子图的权限角色类型，用于控制用户对子图的访问权限。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求3: 子图信息编辑 - 权限管理</li>
 *   <li>需求澄清3: 权限角色简化 - 只保留 Owner 和 Viewer</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public enum PermissionRole {

    /**
     * 所有者 - 完全控制权限
     * <ul>
     *   <li>编辑子图基本信息</li>
     *   <li>删除子图</li>
     *   <li>管理权限（添加/移除 Owner 和 Viewer）</li>
     *   <li>添加/移除资源节点</li>
     *   <li>查看子图详情和拓扑</li>
     * </ul>
     */
    OWNER("所有者", "完全控制权限"),

    /**
     * 查看者 - 只读访问权限
     * <ul>
     *   <li>查看子图详情</li>
     *   <li>查看子图拓扑</li>
     * </ul>
     */
    VIEWER("查看者", "只读访问权限");

    private final String displayName;
    private final String description;

    PermissionRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 检查当前角色是否有编辑权限
     *
     * @return true 如果是 OWNER
     */
    public boolean canEdit() {
        return this == OWNER;
    }

    /**
     * 检查当前角色是否有删除权限
     *
     * @return true 如果是 OWNER
     */
    public boolean canDelete() {
        return this == OWNER;
    }

    /**
     * 检查当前角色是否可以管理权限
     *
     * @return true 如果是 OWNER
     */
    public boolean canManagePermissions() {
        return this == OWNER;
    }

    /**
     * 检查当前角色是否可以管理资源节点
     *
     * @return true 如果是 OWNER
     */
    public boolean canManageResources() {
        return this == OWNER;
    }

    /**
     * 检查当前角色是否可以查看子图
     *
     * @return true（所有角色都可以查看）
     */
    public boolean canView() {
        return true;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
