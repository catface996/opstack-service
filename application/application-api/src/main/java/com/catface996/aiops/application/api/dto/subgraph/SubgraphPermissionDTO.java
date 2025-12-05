package com.catface996.aiops.application.api.dto.subgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 子图权限DTO
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求3.5: Owner可以添加或移除其他Owner</li>
 *   <li>需求9: 子图安全和审计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "子图权限信息")
public class SubgraphPermissionDTO {

    @Schema(description = "权限ID", example = "1")
    private Long id;

    @Schema(description = "子图ID", example = "1")
    private Long subgraphId;

    @Schema(description = "用户ID", example = "100")
    private Long userId;

    @Schema(description = "权限角色", example = "OWNER", allowableValues = {"OWNER", "VIEWER"})
    private String role;

    @Schema(description = "权限授予时间")
    private LocalDateTime grantedAt;

    @Schema(description = "授权者ID", example = "1")
    private Long grantedBy;
}
