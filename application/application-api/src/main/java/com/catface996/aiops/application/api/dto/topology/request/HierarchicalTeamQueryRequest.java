package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 层级团队查询请求
 *
 * <p>用于根据拓扑图 ID 查询层级化 Agent 团队结构。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须支持根据拓扑图 ID 查询层级团队结构</li>
 *   <li>US1: 查询拓扑图的层级团队结构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "层级团队查询请求")
public class HierarchicalTeamQueryRequest {

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long topologyId;

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    @Schema(description = "用户ID（网关注入）", hidden = true)
    private Long userId;
}
