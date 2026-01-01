package com.catface996.aiops.application.impl.service.execution.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Executor 事件 (Executor API SSE)
 *
 * <p>表示从 Executor 服务接收的 SSE 事件。</p>
 *
 * <p>事件格式示例：</p>
 * <pre>
 * {
 *   "run_id": "abc-123",
 *   "timestamp": "2025-12-31T10:00:00Z",
 *   "sequence": 1,
 *   "source": {"agent_id": "100", "agent_type": "worker", "agent_name": "Agent A", "team_name": "Team 1"},
 *   "event": {"category": "llm", "action": "stream"},
 *   "data": {"content": "Hello..."}
 * }
 * </pre>
 *
 * <p>事件中的 source.agent_id 对应 AgentBound.id，可用于追溯到具体的绑定关系。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorEvent {

    /**
     * 运行 ID
     */
    @JsonProperty("run_id")
    private String runId;

    /**
     * 事件时间戳 (ISO 8601)
     */
    private String timestamp;

    /**
     * 事件序号
     */
    private Integer sequence;

    /**
     * 事件来源（含 agent_id，用于追溯绑定关系）
     */
    private EventSource source;

    /**
     * 事件类型信息
     */
    private EventType event;

    /**
     * 事件数据
     */
    private Map<String, Object> data;

    // ==================== 兼容旧格式字段 ====================

    /**
     * 事件类型（兼容旧格式）
     */
    private String type;

    /**
     * Agent 名称（兼容旧格式）
     */
    private String agent;

    /**
     * 事件内容（兼容旧格式）
     */
    private String content;

    // ==================== 便捷方法 ====================

    /**
     * 获取 Agent ID（优先从 source 获取）
     *
     * @return Agent ID（对应 AgentBound.id）
     */
    public String getAgentId() {
        if (source != null && source.getAgentId() != null) {
            return source.getAgentId();
        }
        return null;
    }

    /**
     * 获取 Agent 名称（优先从 source 获取，回退到 agent 字段）
     */
    public String getAgentName() {
        if (source != null && source.getAgentName() != null) {
            return source.getAgentName();
        }
        return agent;
    }

    /**
     * 获取 Agent 类型/角色（从 source 获取）
     */
    public String getAgentType() {
        if (source != null && source.getAgentType() != null) {
            return source.getAgentType();
        }
        return null;
    }

    /**
     * 获取团队名称（从 source 获取）
     */
    public String getTeamName() {
        if (source != null) {
            return source.getTeamName();
        }
        return null;
    }

    /**
     * 获取事件类型（优先从 event 获取，回退到 type 字段）
     * 格式: category.action 或直接 type
     */
    public String getEventType() {
        if (event != null) {
            String category = event.getCategory();
            String action = event.getAction();
            if (category != null && action != null) {
                return category + "." + action;
            }
        }
        return type;
    }

    /**
     * 获取事件内容（优先从 data.content 获取，回退到 content 字段）
     */
    public String getEventContent() {
        if (data != null && data.containsKey("content")) {
            Object contentObj = data.get("content");
            return contentObj != null ? contentObj.toString() : null;
        }
        return content;
    }

    /**
     * 事件来源
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSource {
        /**
         * Agent ID（对应 AgentBound.id）
         */
        @JsonProperty("agent_id")
        private String agentId;

        /**
         * Agent 类型 (global_supervisor, team_supervisor, worker)
         */
        @JsonProperty("agent_type")
        private String agentType;

        /**
         * Agent 名称
         */
        @JsonProperty("agent_name")
        private String agentName;

        /**
         * 团队名称（global_supervisor 时为 null）
         */
        @JsonProperty("team_name")
        private String teamName;
    }

    /**
     * 事件类型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventType {
        /**
         * 事件类别 (lifecycle, llm, dispatch, system)
         */
        private String category;

        /**
         * 事件动作 (started, stream, tool_call, etc.)
         */
        private String action;
    }
}
