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
 *   "name": "Hierarchy Name",
 *   "global_prompt": "Global supervisor instructions",
 *   "teams": [
 *     {
 *       "name": "Team A",
 *       "supervisor_prompt": "Team supervisor instructions",
 *       "workers": [
 *         {
 *           "name": "Worker 1",
 *           "role": "analyst",
 *           "system_prompt": "Worker instructions",
 *           "model": "gemini-2.0-flash"
 *         }
 *       ]
 *     }
 *   ]
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
     * 层级结构名称 (来自拓扑图名称)
     */
    private String name;

    /**
     * 全局监督者提示词 (来自 Global Supervisor Agent 的 specialty)
     */
    @JsonProperty("global_prompt")
    private String globalPrompt;

    /**
     * 团队配置列表
     */
    private List<TeamConfig> teams;

    /**
     * 团队配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamConfig {
        /**
         * 团队名称 (来自节点名称)
         */
        private String name;

        /**
         * 团队监督者提示词 (来自 Team Supervisor Agent 的 specialty)
         */
        @JsonProperty("supervisor_prompt")
        private String supervisorPrompt;

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
         * Agent 名称
         */
        private String name;

        /**
         * Agent 角色
         */
        private String role;

        /**
         * Agent 系统提示词
         */
        @JsonProperty("system_prompt")
        private String systemPrompt;

        /**
         * LLM 模型标识
         */
        private String model;
    }
}
