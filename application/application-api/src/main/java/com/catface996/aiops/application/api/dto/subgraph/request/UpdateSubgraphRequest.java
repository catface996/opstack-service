package com.catface996.aiops.application.api.dto.subgraph.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 更新子图请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求3: 子图信息编辑</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新子图请求")
public class UpdateSubgraphRequest {

    @Size(min = 1, max = 255, message = "子图名称长度必须在1-255字符之间")
    @Schema(description = "子图名称（全局唯一，null表示不修改）", example = "production-network-v2")
    private String name;

    @Size(max = 1000, message = "子图描述不能超过1000字符")
    @Schema(description = "子图描述（null表示不修改）", example = "生产环境网络拓扑子图V2")
    private String description;

    @Schema(description = "标签列表（null表示不修改）", example = "[\"production\", \"network\", \"v2\"]")
    private List<String> tags;

    @Schema(description = "元数据（null表示不修改）", example = "{\"environment\": \"prod\", \"team\": \"ops\", \"version\": \"2\"}")
    private Map<String, String> metadata;

    @Schema(description = "版本号（用于乐观锁检查）", example = "1")
    private Integer version;
}
