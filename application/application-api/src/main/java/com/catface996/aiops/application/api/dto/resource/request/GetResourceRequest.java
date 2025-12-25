package com.catface996.aiops.application.api.dto.resource.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询资源详情请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-005: 查看资源详细信息</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询资源详情请求")
public class GetResourceRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;
}
