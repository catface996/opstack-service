package com.catface996.aiops.application.api.dto.resource.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询资源列表请求
 *
 * <p>支持按资源类型、状态和关键词过滤。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-002: 资源列表展示</li>
 *   <li>REQ-FR-007: 列表搜索功能</li>
 *   <li>REQ-FR-008: 按类型筛选</li>
 *   <li>REQ-FR-009: 按状态筛选</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询资源列表请求")
public class ListResourcesRequest {

    @Schema(description = "资源类型ID（可选，不传则查询所有类型）", example = "1")
    private Long resourceTypeId;

    @Schema(description = "资源状态（可选，RUNNING/STOPPED/MAINTENANCE/OFFLINE）", example = "RUNNING")
    private String status;

    @Schema(description = "搜索关键词（可选，搜索名称和描述）", example = "web")
    private String keyword;

    @Schema(description = "页码（从1开始）", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
