package com.catface996.aiops.application.api.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 资源DTO
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-002: 资源列表展示</li>
 *   <li>REQ-FR-003: 资源详情查看</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源信息")
public class ResourceDTO {

    @Schema(description = "资源ID", example = "1")
    private Long id;

    @Schema(description = "资源名称", example = "web-server-01")
    private String name;

    @Schema(description = "资源描述", example = "生产环境Web服务器")
    private String description;

    @Schema(description = "资源类型ID", example = "1")
    private Long resourceTypeId;

    @Schema(description = "资源类型名称", example = "服务器")
    private String resourceTypeName;

    @Schema(description = "资源类型编码", example = "SERVER")
    private String resourceTypeCode;

    @Schema(description = "资源状态", example = "RUNNING")
    private String status;

    @Schema(description = "状态显示名称", example = "运行中")
    private String statusDisplay;

    @Schema(description = "扩展属性（JSON格式）", example = "{\"ip\": \"192.168.1.100\", \"port\": 8080}")
    private String attributes;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "创建者ID", example = "1")
    private Long createdBy;
}
