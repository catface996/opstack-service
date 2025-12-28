package com.catface996.aiops.interface_.http.request.topology;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 绑定报告模板请求
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Schema(description = "绑定报告模板请求")
public class BindReportTemplatesRequest {

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long topologyId;

    @Schema(description = "报告模板ID列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "报告模板ID列表不能为空")
    @Size(max = 100, message = "单次最多绑定100个模板")
    private List<Long> reportTemplateIds;

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

    public List<Long> getReportTemplateIds() {
        return reportTemplateIds;
    }

    public void setReportTemplateIds(List<Long> reportTemplateIds) {
        this.reportTemplateIds = reportTemplateIds;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
}
