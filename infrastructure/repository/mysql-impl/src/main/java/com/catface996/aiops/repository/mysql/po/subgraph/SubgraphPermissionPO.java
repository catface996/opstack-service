package com.catface996.aiops.repository.mysql.po.subgraph;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 子图权限持久化对象
 *
 * <p>数据库表 subgraph_permission 的映射对象</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1.2: 创建者自动成为第一个 Owner</li>
 *   <li>需求3.5: Owner 可以添加或移除其他 Owner</li>
 *   <li>需求3.6: 不能移除最后一个 Owner</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@TableName("subgraph_permission")
public class SubgraphPermissionPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 子图ID（外键）
     */
    @TableField("subgraph_id")
    private Long subgraphId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 权限角色（OWNER/VIEWER）
     */
    @TableField("role")
    private String role;

    /**
     * 授权时间
     */
    @TableField("granted_at")
    private LocalDateTime grantedAt;

    /**
     * 授权者ID
     */
    @TableField("granted_by")
    private Long grantedBy;
}
