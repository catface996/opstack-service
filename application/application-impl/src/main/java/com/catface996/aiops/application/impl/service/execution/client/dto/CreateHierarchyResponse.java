package com.catface996.aiops.application.impl.service.execution.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建层级结构响应 (Executor API)
 *
 * <p>Executor 服务创建层级结构后的响应。</p>
 *
 * <p>响应格式：</p>
 * <pre>
 * {
 *   "success": true,
 *   "data": {
 *     "id": "hierarchy-uuid",
 *     "name": "Hierarchy Name",
 *     ...
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
public class CreateHierarchyResponse {

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private HierarchyData data;

    /**
     * 获取层级结构 ID
     */
    public String getHierarchyId() {
        return data != null ? data.getId() : null;
    }

    /**
     * 层级结构数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyData {
        /**
         * 层级结构 ID
         */
        private String id;

        /**
         * 层级结构名称
         */
        private String name;

        /**
         * 创建时间戳
         */
        @JsonProperty("created_at")
        private String createdAt;
    }
}
