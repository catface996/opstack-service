package com.catface996.aiops.repository.subgraph.entity;

import java.time.LocalDateTime;

/**
 * 子图权限实体（用于数据库持久化）
 *
 * <p>与领域模型 SubgraphPermission 的映射关系由仓储实现层处理。</p>
 *
 * <p>数据库表：t_subgraph_permission</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求3: 子图信息编辑 - 权限管理</li>
 *   <li>需求9: 子图安全和审计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public class SubgraphPermissionEntity {

    /**
     * 权限ID（主键，自增）
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
    private String role;

    /**
     * 授权时间
     */
    private LocalDateTime grantedAt;

    /**
     * 授权者ID
     */
    private Long grantedBy;

    // ==================== Getters and Setters ====================

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
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
        return "SubgraphPermissionEntity{" +
                "id=" + id +
                ", subgraphId=" + subgraphId +
                ", userId=" + userId +
                ", role='" + role + '\'' +
                ", grantedAt=" + grantedAt +
                ", grantedBy=" + grantedBy +
                '}';
    }
}
