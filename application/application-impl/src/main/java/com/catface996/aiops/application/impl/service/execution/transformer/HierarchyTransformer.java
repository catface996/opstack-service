package com.catface996.aiops.application.impl.service.execution.transformer;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyStructureDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyTeamDTO;
import com.catface996.aiops.application.impl.service.execution.client.dto.CreateHierarchyRequest;
import com.catface996.aiops.application.impl.service.execution.client.dto.CreateHierarchyRequest.LlmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 层级结构转换器
 *
 * <p>将 HierarchyStructureDTO 转换为 Executor API 所需的 CreateHierarchyRequest 格式。</p>
 *
 * <p>转换映射：</p>
 * <ul>
 *   <li>topologyName → hierarchy.name</li>
 *   <li>globalSupervisor.boundId → global_supervisor_agent.agent_id</li>
 *   <li>globalSupervisor.promptTemplateContent → global_supervisor_agent.system_prompt</li>
 *   <li>globalSupervisor.model/temperature/topP/maxTokens → global_supervisor_agent.llm_config</li>
 *   <li>teams[].supervisor → team_supervisor_agent (含 llm_config)</li>
 *   <li>teams[].workers[] → workers[] (含 llm_config)</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Slf4j
@Component
public class HierarchyTransformer {

    // 默认 LLM 配置
    private static final String DEFAULT_MODEL = "gemini-2.0-flash";
    private static final Double DEFAULT_TEMPERATURE = 0.7;
    private static final Double DEFAULT_TOP_P = 0.9;
    private static final Integer DEFAULT_MAX_TOKENS = 4096;

    /**
     * 将 HierarchyStructureDTO 转换为 CreateHierarchyRequest
     *
     * @param hierarchyStructure 层级结构 DTO
     * @return Executor API 创建层级请求
     * @throws IllegalArgumentException 如果拓扑图无任何有效绑定
     */
    public CreateHierarchyRequest transform(HierarchyStructureDTO hierarchyStructure) {
        log.debug("Transforming HierarchyStructureDTO to CreateHierarchyRequest for topology: {}",
                hierarchyStructure.getTopologyName());

        // 构建 global_supervisor_agent
        CreateHierarchyRequest.SupervisorAgentConfig globalSupervisorAgent =
                buildSupervisorAgent(hierarchyStructure.getGlobalSupervisor(), "Global Supervisor");

        // 转换团队列表
        List<CreateHierarchyRequest.TeamConfig> teamConfigs = transformTeams(hierarchyStructure.getTeams());

        // 验证拓扑图有任何有效绑定（至少有一个 team 含 workers）
        if (teamConfigs.isEmpty()) {
            throw new IllegalArgumentException(
                    "拓扑图 '" + hierarchyStructure.getTopologyName() + "' 没有任何有效的 Agent 绑定（需要至少一个团队含工作者）");
        }

        // 使用拓扑名称 + 时间戳确保唯一性
        String hierarchyName = hierarchyStructure.getTopologyName() + "_" + Instant.now().toEpochMilli();

        return CreateHierarchyRequest.builder()
                .name(hierarchyName)
                .description("Topology: " + hierarchyStructure.getTopologyName())
                .enableContextSharing(false)
                .executionMode("sequential")
                .globalSupervisorAgent(globalSupervisorAgent)
                .teams(teamConfigs)
                .build();
    }

    /**
     * 构建监督者 Agent 配置（Global Supervisor 或 Team Supervisor）
     *
     * @param agent       Agent DTO
     * @param defaultName 默认名称
     * @return SupervisorAgentConfig
     */
    private CreateHierarchyRequest.SupervisorAgentConfig buildSupervisorAgent(AgentDTO agent, String defaultName) {
        if (agent == null) {
            log.warn("No supervisor found, using default configuration for: {}", defaultName);
            return CreateHierarchyRequest.SupervisorAgentConfig.builder()
                    .agentId("default-" + defaultName.toLowerCase().replace(" ", "-"))
                    .name(defaultName)
                    .systemPrompt("You are " + defaultName + ". Coordinate and oversee the work.")
                    .llmConfig(buildDefaultLlmConfig())
                    .build();
        }

        String agentId = agent.getBoundId() != null
                ? String.valueOf(agent.getBoundId())
                : "supervisor-" + agent.getId();

        String systemPrompt = buildSystemPrompt(
                agent.getPromptTemplateContent(),
                agent.getName(),
                agent.getSpecialty(),
                "Coordinate and oversee the work."
        );

        return CreateHierarchyRequest.SupervisorAgentConfig.builder()
                .agentId(agentId)
                .name(agent.getName())
                .systemPrompt(systemPrompt)
                .llmConfig(buildLlmConfig(agent))
                .build();
    }

    /**
     * 转换团队列表
     */
    private List<CreateHierarchyRequest.TeamConfig> transformTeams(List<HierarchyTeamDTO> teams) {
        if (teams == null || teams.isEmpty()) {
            return Collections.emptyList();
        }

        List<CreateHierarchyRequest.TeamConfig> teamConfigs = new ArrayList<>();

        for (HierarchyTeamDTO team : teams) {
            // 构建 team_supervisor_agent
            CreateHierarchyRequest.SupervisorAgentConfig teamSupervisorAgent =
                    buildSupervisorAgent(team.getSupervisor(), "Team Supervisor - " + team.getNodeName());

            // 转换工作者列表
            List<CreateHierarchyRequest.WorkerConfig> workers = transformWorkers(team.getWorkers());

            // 如果没有工作者，跳过这个团队
            if (workers.isEmpty()) {
                log.warn("Skipping team {} - no workers assigned", team.getNodeName());
                continue;
            }

            CreateHierarchyRequest.TeamConfig teamConfig = CreateHierarchyRequest.TeamConfig.builder()
                    .name(team.getNodeName())
                    .preventDuplicate(true)
                    .shareContext(false)
                    .teamSupervisorAgent(teamSupervisorAgent)
                    .workers(workers)
                    .build();

            teamConfigs.add(teamConfig);
        }

        return teamConfigs;
    }

    /**
     * 转换工作者 Agent 列表
     */
    private List<CreateHierarchyRequest.WorkerConfig> transformWorkers(List<AgentDTO> workers) {
        if (workers == null || workers.isEmpty()) {
            return Collections.emptyList();
        }

        List<CreateHierarchyRequest.WorkerConfig> workerConfigs = new ArrayList<>();
        for (AgentDTO worker : workers) {
            workerConfigs.add(transformWorker(worker));
        }
        return workerConfigs;
    }

    /**
     * 将 AgentDTO 转换为 WorkerConfig
     *
     * @param agent AgentDTO
     * @return WorkerConfig
     */
    private CreateHierarchyRequest.WorkerConfig transformWorker(AgentDTO agent) {
        String agentId = agent.getBoundId() != null
                ? String.valueOf(agent.getBoundId())
                : "worker-" + agent.getId();

        String systemPrompt = buildSystemPrompt(
                agent.getPromptTemplateContent(),
                agent.getName(),
                agent.getSpecialty(),
                "Complete assigned tasks efficiently."
        );

        String role = agent.getRole() != null ? agent.getRole() : "worker";

        return CreateHierarchyRequest.WorkerConfig.builder()
                .agentId(agentId)
                .name(agent.getName())
                .role(role)
                .systemPrompt(systemPrompt)
                .llmConfig(buildLlmConfig(agent))
                .tools(null) // TODO: 从 Agent 配置中获取工具列表
                .build();
    }

    /**
     * 构建 LLM 配置
     *
     * <p>优先使用 providerModelId（提供商模型标识符），如果为空则回退到 modelName（模型友好名称）。</p>
     *
     * @param agent Agent DTO
     * @return LlmConfig
     */
    private LlmConfig buildLlmConfig(AgentDTO agent) {
        // 优先使用 providerModelId，如果为空则回退到 modelName
        String effectiveModelId = agent.getProviderModelId() != null && !agent.getProviderModelId().trim().isEmpty()
                ? agent.getProviderModelId()
                : (agent.getModelName() != null ? agent.getModelName() : DEFAULT_MODEL);

        return LlmConfig.builder()
                .modelId(effectiveModelId)
                .temperature(agent.getTemperature() != null ? agent.getTemperature() : DEFAULT_TEMPERATURE)
                .topP(agent.getTopP() != null ? agent.getTopP() : DEFAULT_TOP_P)
                .maxTokens(agent.getMaxTokens() != null ? agent.getMaxTokens() : DEFAULT_MAX_TOKENS)
                .build();
    }

    /**
     * 构建默认 LLM 配置
     *
     * @return LlmConfig
     */
    private LlmConfig buildDefaultLlmConfig() {
        return LlmConfig.builder()
                .modelId(DEFAULT_MODEL)
                .temperature(DEFAULT_TEMPERATURE)
                .topP(DEFAULT_TOP_P)
                .maxTokens(DEFAULT_MAX_TOKENS)
                .build();
    }

    /**
     * 构建系统提示词
     *
     * <p>优先使用 promptTemplateContent，如果为空则回退到默认生成。</p>
     *
     * @param promptTemplateContent 提示词模板内容（优先使用）
     * @param name                  Agent 名称
     * @param specialty             Agent 专长
     * @param defaultAction         默认动作描述
     * @return 系统提示词
     */
    private String buildSystemPrompt(String promptTemplateContent, String name, String specialty, String defaultAction) {
        // 优先使用 promptTemplateContent
        if (promptTemplateContent != null && !promptTemplateContent.trim().isEmpty()) {
            return promptTemplateContent;
        }

        // 回退到默认生成
        return generateDefaultPrompt(name, specialty, defaultAction);
    }

    /**
     * 生成默认提示词
     *
     * @param name          Agent 名称
     * @param specialty     Agent 专长
     * @param defaultAction 默认动作描述
     * @return 默认提示词
     */
    private String generateDefaultPrompt(String name, String specialty, String defaultAction) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are ").append(name != null ? name : "an AI assistant").append(". ");

        if (specialty != null && !specialty.trim().isEmpty()) {
            prompt.append(specialty);
        } else {
            prompt.append(defaultAction);
        }

        return prompt.toString();
    }
}
