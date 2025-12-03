package com.catface996.aiops.application.api.dto.relationship;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 循环依赖检测结果DTO
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "循环依赖检测结果")
public class CycleDetectionDTO {

    @Schema(description = "是否存在循环依赖", example = "true")
    private boolean hasCycle;

    @Schema(description = "循环路径（资源ID列表）", example = "[1, 2, 3, 1]")
    private List<Long> cyclePath;

    @Schema(description = "消息", example = "检测到循环依赖")
    private String message;
}
