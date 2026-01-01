package com.catface996.aiops.application.impl.service.execution.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建层级结构请求 (Executor API)
 *
 * <p>用于调用 Executor 服务创建多智能体层级结构。</p>
 *
 * <p>Executor API 格式：</p>
 * <pre>
 * {
 *   "name": "customer-service-team",
 *   "description": "客服智能体团队",
 *   "enable_context_sharing": false,
 *   "execution_mode": "sequential",
 *   "global_supervisor_agent": {
 *     "agent_id": "gs-001",
 *     "system_prompt": "You are a global coordinator...",
 *     "llm_config": {
 *       "model_id": "gemini-2.0-flash",
 *       "temperature": 0.7,
 *       "top_p": 0.9,
 *       "max_tokens": 2048
 *     }
 *   },
 *   "teams": [...]
 * }
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHierarchyRequest {

    /**
     * 层级结构名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用上下文共享
     */
    @JsonProperty("enable_context_sharing")
    private Boolean enableContextSharing;

    /**
     * 执行模式: sequential, parallel
     */
    @JsonProperty("execution_mode")
    private String executionMode;

    /**
     * 全局监督者 Agent 配置
     */
    @JsonProperty("global_supervisor_agent")
    private SupervisorAgentConfig globalSupervisorAgent;

    /**
     * 团队配置列表
     */
    private List<TeamConfig> teams;

    /**
     * LLM 配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LlmConfig {
        /**
         * 模型 ID
         */
        @JsonProperty("model_id")
        private String modelId;

        /**
         * 温度参数 (0.0-2.0)
         */
        private Double temperature;

        /**
         * Top-P 采样参数 (0.0-1.0)
         */
        @JsonProperty("top_p")
        private Double topP;

        /**
         * 最大 Token 数
         */
        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }

    /**
     * 监督者 Agent 配置（Global Supervisor 和 Team Supervisor 共用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupervisorAgentConfig {
        /**
         * Agent ID（绑定关系 ID，即 agent_bound.id）
         */
        @JsonProperty("agent_id")
        private String agentId;

        /**
         * Agent 名称
         */
        private String name;

        /**
         * 系统提示词
         */
        @JsonProperty("system_prompt")
        private String systemPrompt;

        /**
         * LLM 配置
         */
        @JsonProperty("llm_config")
        private LlmConfig llmConfig;
    }

    /**
     * 团队配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamConfig {
        /**
         * 团队名称
         */
        private String name;

        /**
         * 是否防止重复执行
         */
        @JsonProperty("prevent_duplicate")
        private Boolean preventDuplicate;

        /**
         * 是否共享上下文
         */
        @JsonProperty("share_context")
        private Boolean shareContext;

        /**
         * 团队监督者 Agent 配置
         */
        @JsonProperty("team_supervisor_agent")
        private SupervisorAgentConfig teamSupervisorAgent;

        /**
         * 工作者 Agent 配置列表
         */
        private List<WorkerConfig> workers;
    }

    /**
     * 工作者 Agent 配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerConfig {
        /**
         * Agent ID（绑定关系 ID，即 agent_bound.id）
         */
        @JsonProperty("agent_id")
        private String agentId;

        /**
         * Agent 名称
         */
        private String name;

        /**
         * Agent 角色
         */
        private String role;

        /**
         * 系统提示词
         */
        @JsonProperty("system_prompt")
        private String systemPrompt;

        /**
         * LLM 配置
         */
        @JsonProperty("llm_config")
        private LlmConfig llmConfig;

        /**
         * 工具列表
         */
        private List<String> tools;
    }
}
