package com.catface996.aiops.domain.model.agentbound;

import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent 绑定关系领域实体
 *
 * <p>表示 Agent 与各类实体（Topology、Node）的绑定关系。</p>
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>每个 Topology 只能绑定一个 GLOBAL_SUPERVISOR</li>
 *   <li>每个 Node 只能绑定一个 TEAM_SUPERVISOR</li>
 *   <li>每个 Node 可以绑定多个 TEAM_WORKER</li>
 *   <li>绑定的 Agent 层级必须与 hierarchy_level 匹配</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentBound {

    /**
     * 绑定记录 ID
     */
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 层级（GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER）
     */
    private AgentHierarchyLevel hierarchyLevel;

    /**
     * 绑定实体 ID
     */
    private Long entityId;

    /**
     * 实体类型（TOPOLOGY, NODE）
     */
    private BoundEntityType entityType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 软删除标记
     */
    private Boolean deleted;

    // ==================== 派生字段（用于 JOIN 查询结果）====================

    /**
     * Agent 名称（JOIN 查询填充）
     */
    private String agentName;

    /**
     * Agent 角色（JOIN 查询填充）
     */
    private String agentRole;

    /**
     * 实体名称（JOIN 查询填充）
     */
    private String entityName;

    /**
     * Agent 专长领域（JOIN 查询填充）
     */
    private String agentSpecialty;

    /**
     * Agent 模型友好名称（JOIN 查询填充）
     */
    private String agentModelName;

    /**
     * Agent 模型提供商标识符（JOIN 查询填充）
     */
    private String agentProviderModelId;

    /**
     * Agent 温度参数（JOIN 查询填充）
     */
    private Double agentTemperature;

    /**
     * Agent Top-P 参数（JOIN 查询填充）
     */
    private Double agentTopP;

    /**
     * Agent 最大 Token 数（JOIN 查询填充）
     */
    private Integer agentMaxTokens;

    /**
     * Agent 关联的提示词模板内容（JOIN 查询填充，作为 system_prompt 来源）
     */
    private String promptTemplateContent;

    /**
     * 工厂方法：创建新的绑定关系
     *
     * @param agentId        Agent ID
     * @param hierarchyLevel 层级
     * @param entityId       实体 ID
     * @param entityType     实体类型
     * @return 新的绑定关系
     */
    public static AgentBound create(Long agentId, AgentHierarchyLevel hierarchyLevel,
                                     Long entityId, BoundEntityType entityType) {
        return AgentBound.builder()
                .agentId(agentId)
                .hierarchyLevel(hierarchyLevel)
                .entityId(entityId)
                .entityType(entityType)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    /**
     * 检查是否为 Global Supervisor 绑定
     */
    public boolean isGlobalSupervisorBinding() {
        return hierarchyLevel == AgentHierarchyLevel.GLOBAL_SUPERVISOR;
    }

    /**
     * 检查是否为 Team Supervisor 绑定
     */
    public boolean isTeamSupervisorBinding() {
        return hierarchyLevel == AgentHierarchyLevel.TEAM_SUPERVISOR;
    }

    /**
     * 检查是否为 Worker 绑定
     */
    public boolean isWorkerBinding() {
        return hierarchyLevel == AgentHierarchyLevel.TEAM_WORKER;
    }

    /**
     * 检查是否为 Supervisor 绑定（Global 或 Team）
     */
    public boolean isSupervisorBinding() {
        return hierarchyLevel != null && hierarchyLevel.isSupervisor();
    }

    /**
     * 标记为已删除（软删除）
     */
    public void markDeleted() {
        this.deleted = true;
    }

    /**
     * 设置派生字段（用于 DTO 转换）
     */
    public void setDerivedFields(String agentName, String agentRole, String entityName) {
        this.agentName = agentName;
        this.agentRole = agentRole;
        this.entityName = entityName;
    }

    /**
     * 设置 Agent 专长领域
     */
    public void setAgentSpecialty(String agentSpecialty) {
        this.agentSpecialty = agentSpecialty;
    }

    /**
     * 设置 Agent 模型友好名称
     */
    public void setAgentModelName(String agentModelName) {
        this.agentModelName = agentModelName;
    }

    /**
     * 获取 Agent 模型友好名称
     */
    public String getAgentModelName() {
        return agentModelName;
    }

    /**
     * 设置 Agent 模型提供商标识符
     */
    public void setAgentProviderModelId(String agentProviderModelId) {
        this.agentProviderModelId = agentProviderModelId;
    }

    /**
     * 获取 Agent 模型提供商标识符
     */
    public String getAgentProviderModelId() {
        return agentProviderModelId;
    }

    /**
     * 设置 Agent LLM 配置参数
     */
    public void setLlmConfig(Double temperature, Double topP, Integer maxTokens) {
        this.agentTemperature = temperature;
        this.agentTopP = topP;
        this.agentMaxTokens = maxTokens;
    }

    /**
     * 获取 Agent 温度参数
     */
    public Double getAgentTemperature() {
        return agentTemperature;
    }

    /**
     * 获取 Agent Top-P 参数
     */
    public Double getAgentTopP() {
        return agentTopP;
    }

    /**
     * 获取 Agent 最大 Token 数
     */
    public Integer getAgentMaxTokens() {
        return agentMaxTokens;
    }

    /**
     * 获取 Agent 关联的提示词模板内容
     */
    public String getPromptTemplateContent() {
        return promptTemplateContent;
    }

    /**
     * 设置 Agent 关联的提示词模板内容
     */
    public void setPromptTemplateContent(String promptTemplateContent) {
        this.promptTemplateContent = promptTemplateContent;
    }
}
