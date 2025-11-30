package com.catface996.aiops.application.api.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源类型DTO
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-026: 预置资源类型</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源类型信息")
public class ResourceTypeDTO {

    @Schema(description = "资源类型ID", example = "1")
    private Long id;

    @Schema(description = "类型编码", example = "SERVER")
    private String code;

    @Schema(description = "类型名称", example = "服务器")
    private String name;

    @Schema(description = "类型描述", example = "物理服务器或虚拟机")
    private String description;

    @Schema(description = "类型图标", example = "server-icon")
    private String icon;

    @Schema(description = "是否为系统预置", example = "true")
    private Boolean systemPreset;
}
