package com.catface996.aiops.application.api.dto.topology;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 层级团队 DTO
 *
 * <p>表示拓扑图的层级化 Agent 团队结构，包含：</p>
 * <ul>
 *   <li>Global Supervisor: 拓扑图绑定的全局监管者</li>
 *   <li>Teams: 资源节点对应的团队列表</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须支持根据拓扑图 ID 查询层级团队结构</li>
 *   <li>FR-002: 系统必须返回拓扑图绑定的 Global Supervisor Agent 信息</li>
 *   <li>FR-003: 系统必须返回拓扑图所有成员节点对应的 Team 列表</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "层级团队信息")
public class HierarchicalTeamDTO {

    @Schema(description = "拓扑图ID", example = "1")
    private Long topologyId;

    @Schema(description = "拓扑图名称", example = "生产环境")
    private String topologyName;

    @Schema(description = "全局监管者 Agent（可为 null）")
    private AgentDTO globalSupervisor;

    @Schema(description = "团队列表（每个节点对应一个团队）")
    private List<TeamDTO> teams;
}
