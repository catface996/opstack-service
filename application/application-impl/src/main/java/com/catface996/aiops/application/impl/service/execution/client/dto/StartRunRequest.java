package com.catface996.aiops.application.impl.service.execution.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 启动运行请求 (Executor API)
 *
 * <p>用于调用 Executor 服务启动多智能体执行运行。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartRunRequest {

    /**
     * 层级结构 ID
     */
    @JsonProperty("hierarchy_id")
    private String hierarchyId;

    /**
     * 用户任务/消息
     */
    private String task;
}
