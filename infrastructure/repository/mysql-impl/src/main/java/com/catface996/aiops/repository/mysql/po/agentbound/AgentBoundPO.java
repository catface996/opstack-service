package com.catface996.aiops.repository.mysql.po.agentbound;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 绑定关系持久化对象
 *
 * <p>数据库表 agent_bound 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@TableName("agent_bound")
public class AgentBoundPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 绑定记录 ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Agent ID
     */
    @TableField("agent_id")
    private Long agentId;

    /**
     * 层级: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER
     */
    @TableField("hierarchy_level")
    private String hierarchyLevel;

    /**
     * 绑定实体 ID
     */
    @TableField("entity_id")
    private Long entityId;

    /**
     * 实体类型: TOPOLOGY, NODE
     */
    @TableField("entity_type")
    private String entityType;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 软删除标记: 0-未删除, 1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    // ==================== 派生字段（不映射到数据库，用于 JOIN 查询结果）====================

    /**
     * Agent 名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String agentName;

    /**
     * Agent 角色（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String agentRole;

    /**
     * 实体名称（JOIN 查询填充，Topology 或 Node 名称）
     */
    @TableField(exist = false)
    private String entityName;

    /**
     * Agent 专长领域（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String agentSpecialty;

    /**
     * Agent 模型友好名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String agentModelName;

    /**
     * Agent 模型提供商标识符（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String agentProviderModelId;

    /**
     * Agent 温度参数（JOIN 查询填充）
     */
    @TableField(exist = false)
    private Double agentTemperature;

    /**
     * Agent Top-P 参数（JOIN 查询填充）
     */
    @TableField(exist = false)
    private Double agentTopP;

    /**
     * Agent 最大 Token 数（JOIN 查询填充）
     */
    @TableField(exist = false)
    private Integer agentMaxTokens;

    /**
     * Agent 关联的提示词模板内容（JOIN 查询填充，作为 system_prompt 来源）
     */
    @TableField(exist = false)
    private String promptTemplateContent;
}
