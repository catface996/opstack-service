package com.catface996.aiops.application.api.dto.subgraph;

import com.catface996.aiops.application.api.dto.relationship.RelationshipDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 子图拓扑DTO
 *
 * <p>表示子图中资源节点及其之间关系的拓扑图结构</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求7.3: 拓扑图只显示子图内节点之间的关系</li>
 *   <li>需求7.4: 拓扑图使用与主拓扑图相同的视觉样式</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "子图拓扑信息")
public class SubgraphTopologyDTO {

    @Schema(description = "子图ID", example = "1")
    private Long subgraphId;

    @Schema(description = "子图名称", example = "production-network")
    private String subgraphName;

    @Schema(description = "资源节点ID列表")
    private List<Long> resourceIds;

    @Schema(description = "节点之间的关系列表")
    private List<RelationshipDTO> relationships;

    @Schema(description = "节点总数", example = "15")
    private int nodeCount;

    @Schema(description = "边（关系）总数", example = "20")
    private int edgeCount;
}
