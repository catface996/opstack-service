package com.catface996.aiops.application.api.dto.topology;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 团队 DTO
 *
 * <p>表示资源节点对应的团队结构，包含：</p>
 * <ul>
 *   <li>节点基本信息（ID、名称）</li>
 *   <li>Team Supervisor: 团队监管者（TEAM_SUPERVISOR 层级的 Agent）</li>
 *   <li>Team Workers: 团队工作者列表（TEAM_WORKER 层级的 Agent）</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-004: 每个 Team 必须包含节点基本信息（ID、名称）</li>
 *   <li>FR-005: 每个 Team 必须包含该节点绑定的 TEAM_SUPERVISOR 级别 Agent（如有）</li>
 *   <li>FR-006: 每个 Team 必须包含该节点绑定的所有 TEAM_WORKER 级别 Agent 列表</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队信息")
public class TeamDTO {

    @Schema(description = "资源节点ID", example = "10")
    private Long nodeId;

    @Schema(description = "资源节点名称", example = "Web Server")
    private String nodeName;

    @Schema(description = "团队监管者 Agent（可为 null）")
    private AgentDTO supervisor;

    @Schema(description = "团队工作者 Agent 列表（可为空）")
    private List<AgentDTO> workers;
}
