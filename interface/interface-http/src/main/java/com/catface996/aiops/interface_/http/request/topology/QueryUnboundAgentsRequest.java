package com.catface996.aiops.interface_.http.request.topology;

import com.catface996.aiops.interface_.http.request.common.PageableRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 查询未绑定 Agent 请求
 *
 * <p>查询指定拓扑图未绑定的 GLOBAL_SUPERVISOR 层级 Agent</p>
 *
 * @author AI Assistant
 * @since 2025-12-30
 */
@Schema(description = "查询未绑定 Agent 请求")
public class QueryUnboundAgentsRequest extends PageableRequest {

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long topologyId;

    @Schema(description = "搜索关键词（模糊匹配 Agent 名称和专业领域）", example = "监控")
    private String keyword;

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
}
