package com.catface996.aiops.repository.mysql.po.subgraph;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 子图持久化对象
 *
 * <p>数据库表 subgraph 的映射对象</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1: 子图创建</li>
 *   <li>需求2: 子图列表视图</li>
 *   <li>需求3: 子图信息编辑</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@TableName("subgraph")
public class SubgraphPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 子图ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 子图名称（全局唯一）
     */
    @TableField("name")
    private String name;

    /**
     * 子图描述
     */
    @TableField("description")
    private String description;

    /**
     * 标签列表（JSON数组格式）
     */
    @TableField("tags")
    private String tags;

    /**
     * 元数据（JSON对象格式）
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
