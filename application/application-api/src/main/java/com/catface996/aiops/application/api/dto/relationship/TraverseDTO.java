package com.catface996.aiops.application.api.dto.relationship;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 图遍历结果DTO
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图遍历结果")
public class TraverseDTO {

    @Schema(description = "起始资源ID", example = "1")
    private Long startResourceId;

    @Schema(description = "按层级分组的节点")
    private Map<Integer, List<Long>> nodesByLevel;

    @Schema(description = "遍历的关系列表")
    private List<RelationshipDTO> relationships;

    @Schema(description = "实际遍历深度", example = "3")
    private int actualDepth;

    @Schema(description = "访问的节点总数", example = "10")
    private int totalNodes;
}
