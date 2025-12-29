package com.catface996.aiops.application.impl.service.execution.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 启动运行响应 (Executor API)
 *
 * <p>Executor 服务启动运行后的响应。</p>
 *
 * <p>响应格式：</p>
 * <pre>
 * {
 *   "success": true,
 *   "data": {
 *     "id": "run-uuid",
 *     "hierarchy_id": "hierarchy-uuid",
 *     "task": "...",
 *     "status": "pending",
 *     "stream_url": "/api/executor/v1/runs/stream"
 *   }
 * }
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartRunResponse {

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 响应数据
     */
    private RunData data;

    /**
     * 获取运行 ID
     */
    public String getRunId() {
        return data != null ? data.getId() : null;
    }

    /**
     * 运行数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunData {
        /**
         * 运行 ID
         */
        private String id;

        /**
         * 关联的层级结构 ID
         */
        @JsonProperty("hierarchy_id")
        private String hierarchyId;

        /**
         * 执行的任务
         */
        private String task;

        /**
         * 运行状态
         */
        private String status;

        /**
         * 流式事件 URL
         */
        @JsonProperty("stream_url")
        private String streamUrl;
    }
}
