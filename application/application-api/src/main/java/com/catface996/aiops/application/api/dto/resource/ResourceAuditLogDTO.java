package com.catface996.aiops.application.api.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 资源审计日志DTO
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-020: 查看资源变更历史</li>
 *   <li>REQ-FR-028: 审计日志功能</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源审计日志信息")
public class ResourceAuditLogDTO {

    @Schema(description = "日志ID", example = "1")
    private Long id;

    @Schema(description = "资源ID", example = "1")
    private Long resourceId;

    @Schema(description = "操作类型", example = "UPDATE")
    private String operation;

    @Schema(description = "操作类型显示名称", example = "更新")
    private String operationDisplay;

    @Schema(description = "旧值（JSON格式）")
    private String oldValue;

    @Schema(description = "新值（JSON格式）")
    private String newValue;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    @Schema(description = "操作时间")
    private LocalDateTime operatedAt;
}
