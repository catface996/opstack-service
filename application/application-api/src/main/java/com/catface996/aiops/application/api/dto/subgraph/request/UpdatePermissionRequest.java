package com.catface996.aiops.application.api.dto.subgraph.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新子图权限请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求3.5: Owner可以添加或移除其他Owner</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新子图权限请求")
public class UpdatePermissionRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "被授权用户ID", example = "100", required = true)
    private Long userId;

    @NotNull(message = "权限角色不能为空")
    @Pattern(regexp = "^(OWNER|VIEWER)$", message = "权限角色必须是OWNER或VIEWER")
    @Schema(description = "权限角色", example = "VIEWER", allowableValues = {"OWNER", "VIEWER"}, required = true)
    private String role;
}
