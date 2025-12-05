package com.catface996.aiops.application.api.dto.subgraph.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 添加资源到子图请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求5: 向子图添加资源节点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加资源到子图请求")
public class AddResourcesRequest {

    @NotEmpty(message = "资源ID列表不能为空")
    @Schema(description = "要添加的资源节点ID列表", example = "[1, 2, 3]", required = true)
    private List<Long> resourceIds;
}
