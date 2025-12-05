package com.catface996.aiops.application.api.dto.subgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 子图DTO
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求2: 子图列表视图</li>
 *   <li>需求7: 子图详情视图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "子图信息")
public class SubgraphDTO {

    @Schema(description = "子图ID", example = "1")
    private Long id;

    @Schema(description = "子图名称", example = "production-network")
    private String name;

    @Schema(description = "子图描述", example = "生产环境网络拓扑子图")
    private String description;

    @Schema(description = "标签列表", example = "[\"production\", \"network\"]")
    private List<String> tags;

    @Schema(description = "元数据", example = "{\"environment\": \"prod\", \"team\": \"ops\"}")
    private Map<String, String> metadata;

    @Schema(description = "创建者ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;

    @Schema(description = "资源节点数量", example = "15")
    private Integer resourceCount;
}
