package com.catface996.aiops.application.api.dto.execution.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 触发多智能体执行请求
 *
 * <p>用于触发基于拓扑图的多智能体协作执行。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Schema(description = "触发多智能体执行请求")
public class TriggerExecutionRequest {

    @Schema(description = "拓扑图ID", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    @Positive(message = "拓扑图ID必须为正数")
    private Long topologyId;

    @Schema(description = "用户任务/消息", example = "分析系统性能并提供优化建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户消息不能为空")
    @Size(max = 10000, message = "用户消息长度不能超过10000字符")
    private String userMessage;

    @Schema(description = "租户ID (网关注入)", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID (网关注入)", hidden = true)
    private String traceId;

    @Schema(description = "用户ID (网关注入)", hidden = true)
    private Long userId;
}
