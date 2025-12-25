package com.catface996.aiops.application.api.dto.resource.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询资源审计日志请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-007: 查看资源操作审计日志</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询资源审计日志请求")
public class GetResourceAuditLogsRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;

    @Schema(description = "页码（从1开始）", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
