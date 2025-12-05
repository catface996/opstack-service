package com.catface996.aiops.repository.mysql.po.subgraph;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 子图资源关联持久化对象
 *
 * <p>数据库表 subgraph_resource 的映射对象</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求5: 向子图添加资源节点</li>
 *   <li>需求6: 从子图移除资源节点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@TableName("subgraph_resource")
public class SubgraphResourcePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 子图ID（外键）
     */
    @TableField("subgraph_id")
    private Long subgraphId;

    /**
     * 资源节点ID（外键）
     */
    @TableField("resource_id")
    private Long resourceId;

    /**
     * 添加时间
     */
    @TableField("added_at")
    private LocalDateTime addedAt;

    /**
     * 添加者ID
     */
    @TableField("added_by")
    private Long addedBy;
}
