package com.catface996.aiops.application.api.dto.relationship;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 资源关系DTO
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源关系信息")
public class RelationshipDTO {

    @Schema(description = "关系ID", example = "1")
    private Long id;

    @Schema(description = "源资源ID", example = "1")
    private Long sourceResourceId;

    @Schema(description = "源资源名称", example = "web-app")
    private String sourceResourceName;

    @Schema(description = "目标资源ID", example = "2")
    private Long targetResourceId;

    @Schema(description = "目标资源名称", example = "mysql-db")
    private String targetResourceName;

    @Schema(description = "关系类型", example = "DEPENDENCY")
    private String relationshipType;

    @Schema(description = "关系类型描述", example = "依赖")
    private String relationshipTypeDesc;

    @Schema(description = "关系方向", example = "UNIDIRECTIONAL")
    private String direction;

    @Schema(description = "关系方向描述", example = "单向")
    private String directionDesc;

    @Schema(description = "关系强度", example = "STRONG")
    private String strength;

    @Schema(description = "关系强度描述", example = "强依赖")
    private String strengthDesc;

    @Schema(description = "关系状态", example = "NORMAL")
    private String status;

    @Schema(description = "关系状态描述", example = "正常")
    private String statusDesc;

    @Schema(description = "关系描述", example = "Web应用依赖MySQL数据库")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
