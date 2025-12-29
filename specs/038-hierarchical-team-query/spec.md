# Feature Specification: Hierarchical Team Query by Topology

**Feature Branch**: `038-hierarchical-team-query`
**Created**: 2025-12-29
**Status**: Draft
**Input**: "实现根据拓扑图ID来查询Hierarchical Team的需求，返回结果是一个Hierarchical Team Agent，有Global Supervisor Agent，有Team列表，每个Team有一个Team Supervisor Agent，有多个Team worker agent"

## Overview

本功能实现根据拓扑图 ID 查询该拓扑图关联的层级化 Agent 团队结构。返回的结构包含：
- **Global Supervisor Agent**: 拓扑图绑定的全局监管者（一个）
- **Team 列表**: 资源节点对应的团队列表，每个团队包含：
  - **Team Supervisor Agent**: 团队监管者（一个）
  - **Team Worker Agents**: 团队工作者列表（多个）

层级关系基于 Agent 的 `hierarchyLevel` 字段区分：
- `GLOBAL_SUPERVISOR`: 全局监管者，绑定在拓扑图上
- `TEAM_SUPERVISOR`: 团队监管者，绑定在资源节点上
- `TEAM_WORKER`: 团队工作者，绑定在资源节点上

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 查询拓扑图的层级团队结构 (Priority: P0)

运维人员需要查看某个拓扑图完整的 Agent 层级团队结构，以了解该拓扑图的 AI 诊断团队配置情况。

**Why this priority**: 这是本功能的核心需求，是唯一的用户场景。

**Independent Test**: 调用 `POST /api/service/v1/topologies/hierarchical-team/query` 接口，传入拓扑图 ID，能够返回完整的层级团队结构。

**Acceptance Scenarios**:

1. **Given** 拓扑图已绑定 Global Supervisor Agent 且包含多个资源节点，每个节点已绑定 Agent，**When** 用户查询层级团队，**Then** 系统返回完整的层级结构，包含 Global Supervisor、Team 列表（每个 Team 包含 Supervisor 和 Workers）

2. **Given** 拓扑图未绑定 Global Supervisor Agent，**When** 用户查询层级团队，**Then** 系统返回结构中 globalSupervisor 为 null，Team 列表正常返回

3. **Given** 拓扑图包含资源节点但节点未绑定任何 Agent，**When** 用户查询层级团队，**Then** 系统返回 Team 列表，每个 Team 的 supervisor 和 workers 为空

4. **Given** 拓扑图不存在，**When** 用户查询层级团队，**Then** 系统返回 404 错误

5. **Given** 资源节点只绑定了 TEAM_WORKER 级别的 Agent（无 TEAM_SUPERVISOR），**When** 用户查询层级团队，**Then** 该 Team 的 supervisor 为 null，workers 列表包含所有 TEAM_WORKER

6. **Given** 资源节点绑定了多个 TEAM_SUPERVISOR 级别的 Agent，**When** 用户查询层级团队，**Then** 系统取第一个作为 supervisor（按创建时间排序）

---

### Edge Cases

- **空拓扑图**: 拓扑图没有任何资源节点成员时，返回空的 teams 列表
- **无绑定 Agent**: 节点存在但未绑定任何 Agent，返回该 Team 但 supervisor 和 workers 均为空
- **层级混合**: 同一节点可能同时绑定 TEAM_SUPERVISOR 和 TEAM_WORKER，需正确分类
- **删除的 Agent**: 软删除的 Agent 不应出现在结果中

## Requirements *(mandatory)*

### Functional Requirements

**核心查询功能**:
- **FR-001**: 系统必须支持根据拓扑图 ID 查询层级团队结构
- **FR-002**: 系统必须返回拓扑图绑定的 Global Supervisor Agent 信息
- **FR-003**: 系统必须返回拓扑图所有成员节点对应的 Team 列表
- **FR-004**: 每个 Team 必须包含节点基本信息（ID、名称）
- **FR-005**: 每个 Team 必须包含该节点绑定的 TEAM_SUPERVISOR 级别 Agent（如有）
- **FR-006**: 每个 Team 必须包含该节点绑定的所有 TEAM_WORKER 级别 Agent 列表

**数据过滤规则**:
- **FR-007**: 系统必须根据 Agent 的 hierarchyLevel 字段区分 Supervisor 和 Worker
- **FR-008**: 系统必须排除已软删除的 Agent
- **FR-009**: 当节点绑定多个 TEAM_SUPERVISOR 时，系统取创建时间最早的一个

**错误处理**:
- **FR-010**: 拓扑图不存在时，系统必须返回 404 错误

### Key Entities

- **HierarchicalTeam**: 层级团队结构，包含 topologyId、topologyName、globalSupervisor、teams 列表
- **TeamInfo**: 团队信息，包含 nodeId、nodeName、supervisor、workers 列表
- **AgentInfo**: Agent 简要信息，包含 id、name、role、hierarchyLevel、specialty、model

### Response Structure

```
HierarchicalTeamDTO {
    topologyId: Long           // 拓扑图 ID
    topologyName: String       // 拓扑图名称
    globalSupervisor: AgentDTO // Global Supervisor（可为 null）
    teams: List<TeamDTO>       // Team 列表
}

TeamDTO {
    nodeId: Long               // 资源节点 ID
    nodeName: String           // 资源节点名称
    supervisor: AgentDTO       // Team Supervisor（可为 null）
    workers: List<AgentDTO>    // Team Worker 列表（可为空）
}

AgentDTO {
    id: Long
    name: String
    role: String               // Agent 角色（专业领域）
    hierarchyLevel: String     // 层级
    specialty: String          // 专业领域描述
    model: String              // AI 模型
}
```

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 用户能够在 3 秒内获取包含 100 个资源节点的拓扑图层级团队结构
- **SC-002**: 返回的层级结构能够正确区分 Global Supervisor、Team Supervisor 和 Team Worker
- **SC-003**: 所有绑定到资源节点的 Agent 都能在对应 Team 中找到
- **SC-004**: API 接口返回格式符合项目标准响应格式（code、success、message、data）

## Assumptions

1. **拓扑图与 Global Supervisor 绑定关系已实现**: 通过 topology 表的 global_supervisor_agent_id 字段关联
2. **资源节点与 Agent 绑定关系已实现**: 通过 node_2_agent 表关联
3. **Agent hierarchyLevel 字段已实现**: Agent 表已有 hierarchy_level 字段区分层级
4. **拓扑图成员关系已实现**: 通过 topology_2_node 表关联拓扑图与资源节点

## Out of Scope

1. **层级团队的增删改操作**: 本功能仅提供查询，不涉及绑定关系的修改
2. **Agent 执行状态信息**: 不返回 Agent 的当前任务、执行统计等运行时信息
3. **层级团队的权限控制**: 不涉及访问权限验证
4. **分页功能**: Team 列表不支持分页，一次返回全部数据
