package com.catface996.aiops.domain.model.subgraph;

import java.time.LocalDateTime;

/**
 * 子图权限实体
 *
 * <p>记录用户对子图的访问权限，包括 Owner（完全控制）和 Viewer（只读访问）。</p>
 * <p>每个用户对同一子图只能拥有一种角色。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1.2: 创建者自动成为第一个 Owner</li>
 *   <li>需求3.5: Owner 可以添加或移除其他 Owner</li>
 *   <li>需求3.6: 不能移除最后一个 Owner</li>
 *   <li>需求9: 子图安全和审计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public class SubgraphPermission {

    /**
     * 权限ID（主键）
     */
    private Long id;

    /**
     * 子图ID（外键）
     */
    private Long subgraphId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 权限角色（OWNER 或 VIEWER）
     */
    private PermissionRole role;

    /**
     * 权限授予时间
     */
    private LocalDateTime grantedAt;

    /**
     * 授权者ID
     */
    private Long grantedBy;

    // 构造函数
    public SubgraphPermission() {
    }

    public SubgraphPermission(Long id, Long subgraphId, Long userId, PermissionRole role,
                               LocalDateTime grantedAt, Long grantedBy) {
        this.id = id;
        this.subgraphId = subgraphId;
        this.userId = userId;
        this.role = role;
        this.grantedAt = grantedAt;
        this.grantedBy = grantedBy;
    }

    // 业务方法

    /**
     * 创建新权限的工厂方法
     *
     * @param subgraphId 子图ID
     * @param userId 被授权用户ID
     * @param role 权限角色
     * @param grantedBy 授权者ID
     * @return 新创建的权限实例
     */
    public static SubgraphPermission create(Long subgraphId, Long userId,
                                             PermissionRole role, Long grantedBy) {
        SubgraphPermission permission = new SubgraphPermission();
        permission.setSubgraphId(subgraphId);
        permission.setUserId(userId);
        permission.setRole(role);
        permission.setGrantedAt(LocalDateTime.now());
        permission.setGrantedBy(grantedBy);
        return permission;
    }

    /**
     * 创建 Owner 权限的便捷方法
     *
     * @param subgraphId 子图ID
     * @param userId 被授权用户ID
     * @param grantedBy 授权者ID
     * @return 新创建的 Owner 权限实例
     */
    public static SubgraphPermission createOwner(Long subgraphId, Long userId, Long grantedBy) {
        return create(subgraphId, userId, PermissionRole.OWNER, grantedBy);
    }

    /**
     * 创建 Viewer 权限的便捷方法
     *
     * @param subgraphId 子图ID
     * @param userId 被授权用户ID
     * @param grantedBy 授权者ID
     * @return 新创建的 Viewer 权限实例
     */
    public static SubgraphPermission createViewer(Long subgraphId, Long userId, Long grantedBy) {
        return create(subgraphId, userId, PermissionRole.VIEWER, grantedBy);
    }

    /**
     * 检查是否为 Owner 权限
     *
     * @return true 如果角色是 OWNER
     */
    public boolean isOwner() {
        return PermissionRole.OWNER.equals(this.role);
    }

    /**
     * 检查是否为 Viewer 权限
     *
     * @return true 如果角色是 VIEWER
     */
    public boolean isViewer() {
        return PermissionRole.VIEWER.equals(this.role);
    }

    /**
     * 更新权限角色
     *
     * @param newRole 新的权限角色
     * @param updatedBy 更新者ID
     */
    public void updateRole(PermissionRole newRole, Long updatedBy) {
        this.role = newRole;
        this.grantedAt = LocalDateTime.now();
        this.grantedBy = updatedBy;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public PermissionRole getRole() {
        return role;
    }

    public void setRole(PermissionRole role) {
        this.role = role;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public Long getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(Long grantedBy) {
        this.grantedBy = grantedBy;
    }

    @Override
    public String toString() {
        return "SubgraphPermission{" +
                "id=" + id +
                ", subgraphId=" + subgraphId +
                ", userId=" + userId +
                ", role=" + role +
                ", grantedAt=" + grantedAt +
                ", grantedBy=" + grantedBy +
                '}';
    }
}
