package com.catface996.aiops.interface_.http.request.topology;

import com.catface996.aiops.interface_.http.request.common.PageableRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 查询已绑定报告模板请求
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Schema(description = "查询已绑定报告模板请求")
public class QueryBoundTemplatesRequest extends PageableRequest {

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long topologyId;

    @Schema(description = "搜索关键词（模糊匹配模板名称和描述）", example = "安全")
    private String keyword;

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    // ==================== Getters and Setters ====================

    public Long getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(Long topologyId) {
        this.topologyId = topologyId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
}
