package com.catface996.aiops.application.impl.service.execution.client.dto;

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
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorEvent {

    /**
     * 事件类型 (thinking, message, tool_call, tool_result, error, complete)
     */
    private String type;

    /**
     * Agent 名称
     */
    private String agent;

    /**
     * 事件内容
     */
    private String content;

    /**
     * 事件时间戳
     */
    private String timestamp;

    /**
     * 附加数据
     */
    private Map<String, Object> data;
}
