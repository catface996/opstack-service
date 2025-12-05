package com.catface996.aiops.application.api.dto.subgraph.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 创建子图请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1: 子图创建</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建子图请求")
public class CreateSubgraphRequest {

    @NotBlank(message = "子图名称不能为空")
    @Size(min = 1, max = 255, message = "子图名称长度必须在1-255字符之间")
    @Schema(description = "子图名称（全局唯一）", example = "production-network", required = true)
    private String name;

    @Size(max = 1000, message = "子图描述不能超过1000字符")
    @Schema(description = "子图描述", example = "生产环境网络拓扑子图")
    private String description;

    @Schema(description = "标签列表", example = "[\"production\", \"network\"]")
    private List<String> tags;

    @Schema(description = "元数据", example = "{\"environment\": \"prod\", \"team\": \"ops\"}")
    private Map<String, String> metadata;
}
