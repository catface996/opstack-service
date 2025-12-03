package com.catface996.aiops.application.api.dto.relationship;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 资源关系汇总DTO
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源关系汇总")
public class ResourceRelationshipsDTO {

    @Schema(description = "资源ID", example = "1")
    private Long resourceId;

    @Schema(description = "资源名称", example = "web-app")
    private String resourceName;

    @Schema(description = "上游依赖列表")
    private List<RelationshipDTO> upstreamDependencies;

    @Schema(description = "下游依赖列表")
    private List<RelationshipDTO> downstreamDependencies;

    @Schema(description = "按类型分组的上游依赖")
    private Map<String, List<RelationshipDTO>> upstreamByType;

    @Schema(description = "按类型分组的下游依赖")
    private Map<String, List<RelationshipDTO>> downstreamByType;

    @Schema(description = "上游依赖总数", example = "5")
    private int upstreamCount;

    @Schema(description = "下游依赖总数", example = "3")
    private int downstreamCount;
}
