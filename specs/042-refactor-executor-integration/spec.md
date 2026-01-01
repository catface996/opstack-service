# Feature Specification: Refactor Executor Integration

**Feature Branch**: `042-refactor-executor-integration`
**Created**: 2025-12-30
**Status**: Draft
**Input**: User description: "重构 Executor 集成：使用绑定关系 ID 替代 agentId，system_prompt 来自关联的 prompt_template"

## Clarifications

### Session 2025-12-30

- Q: Executor 服务失败处理策略？ → A: 快速失败，不重试，立即返回错误，由调用方决定后续处理

## Overview

本功能重构 op-stack-service 与 op-stack-executor 的集成方式，主要变更：

1. **agent_id 使用绑定关系 ID**：将 `agent_bound.id` 作为 Executor API 中的 `agent_id`，而非 `agent.id`
2. **system_prompt 来自 PromptTemplate**：从 Agent 关联的 `prompt_template.content` 获取系统提示词
3. **user_message 使用 task 字段**：任务描述通过 `/runs/start` 的 `task` 参数传递

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 执行多智能体任务 (Priority: P1)

作为系统管理员，我希望基于拓扑图的层级结构触发多智能体协作任务，使得 Executor 能够正确识别每个 Agent 的绑定上下文并使用正确的提示词模板。

**Why this priority**: 核心功能，所有其他场景都依赖于此

**Independent Test**: 可以通过调用 `/api/service/v1/executions/start` 触发任务，验证 Executor 收到的 `agent_id` 是绑定关系 ID，且 `system_prompt` 来自正确的提示词模板

**Acceptance Scenarios**:

1. **Given** 拓扑图已配置完整的层级团队（Global Supervisor + Team Supervisor + Workers 都绑定了 Agent 且 Agent 关联了 PromptTemplate），**When** 触发任务执行，**Then** Executor 收到的请求中每个 Agent 的 `agent_id` 是对应的 `agent_bound.id`，`system_prompt` 是 Agent 关联的 `prompt_template.content`

2. **Given** 某个 Agent 未关联 PromptTemplate，**When** 触发任务执行，**Then** 该 Agent 的 `system_prompt` 使用默认提示词（基于 Agent 的 name 和 specialty 生成）

3. **Given** 拓扑图的某个节点没有绑定 Team Supervisor，**When** 触发任务执行，**Then** 该节点的团队使用默认的 supervisor 配置

---

### User Story 2 - 事件追溯到绑定关系 (Priority: P2)

作为系统管理员，我希望 Executor 返回的事件流中的 `agent_id` 能够追溯到具体的绑定关系，便于在 UI 中展示正确的上下文信息。

**Why this priority**: 支持前端正确展示事件来源

**Independent Test**: 监听 SSE 事件流，验证每个事件的 `source.agent_id` 可以通过 `agent_bound.id` 查询到完整的绑定信息

**Acceptance Scenarios**:

1. **Given** 任务正在执行，**When** 收到 `llm.stream` 事件，**Then** `source.agent_id` 值等于发送时的 `agent_bound.id`，可用于关联查询 Agent 和实体信息

2. **Given** 任务执行完成，**When** 查询历史事件列表，**Then** 所有事件的 `agent_id` 都可以映射回绑定关系

---

### User Story 3 - 层级结构转换正确性 (Priority: P3)

作为系统管理员，我希望层级结构转换器能够正确处理各种配置场景，确保数据完整性。

**Why this priority**: 保障系统健壮性

**Independent Test**: 可以通过单元测试验证各种边界场景的转换结果

**Acceptance Scenarios**:

1. **Given** 拓扑图有多个 Global Supervisor 绑定，**When** 转换层级结构，**Then** 使用第一个 Global Supervisor 的配置

2. **Given** 某个节点只有 Workers 没有 Team Supervisor，**When** 转换层级结构，**Then** 该团队使用默认的 supervisor 配置，Workers 正常包含

---

### Edge Cases

- 当 Agent 关联的 PromptTemplate 不存在或已删除时，使用默认提示词
- 当 PromptTemplate 的 content 为空时，使用默认提示词
- 当绑定关系被删除后，相关的历史事件仍可查询（通过 ID 查询返回 null 时的处理）
- 当拓扑图没有任何绑定时，返回明确的错误提示

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 在调用 Executor API 时，使用 `agent_bound.id` 作为 `agent_id` 字段值
- **FR-002**: 系统 MUST 从 Agent 关联的 `prompt_template` 获取当前版本的 `content` 作为 `system_prompt`
- **FR-003**: 当 Agent 未关联 PromptTemplate 或 content 为空时，系统 MUST 生成默认提示词（格式："You are {agent.name}. {agent.specialty}"）
- **FR-004**: 系统 MUST 保持与 INTEGRATION_GUIDE.md 中定义的 API 格式兼容
- **FR-005**: 系统 MUST 在 `/runs/start` 请求中使用 `task` 字段传递用户任务描述
- **FR-006**: 系统 MUST 能够通过返回的 `agent_id` 追溯到绑定关系和关联的 Agent/Entity
- **FR-007**: 当 Executor 服务不可用或返回错误时，系统 MUST 立即返回错误（快速失败），不进行重试，由调用方决定后续处理

### Key Entities

- **AgentBound**: 绑定关系实体，其 `id` 作为 Executor API 的 `agent_id`
- **Agent**: Agent 实体，通过 `promptTemplateId` 关联提示词模板
- **PromptTemplate**: 提示词模板，`content` 字段存储实际的系统提示词
- **HierarchyStructureDTO**: 层级结构 DTO，需要扩展以包含 `agent_bound.id` 和 PromptTemplate 信息

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 所有发送到 Executor 的请求中，`agent_id` 值等于对应的 `agent_bound.id`
- **SC-002**: 90% 以上的 Agent 能够正确获取关联的 PromptTemplate content 作为 system_prompt
- **SC-003**: 事件流中的 `agent_id` 能够 100% 追溯到绑定关系记录
- **SC-004**: 系统能够在 3 秒内完成层级结构的转换和 Executor API 调用

## Assumptions

- Agent 的 `promptTemplateId` 关联的是 PromptTemplate 的当前版本
- PromptTemplate 的 `content` 字段已正确填充
- Executor API 格式与 INTEGRATION_GUIDE.md 保持一致
- 绑定关系 ID（agent_bound.id）是稳定的，不会频繁变更

## Dependencies

- op-stack-executor 服务已部署并可用
- INTEGRATION_GUIDE.md 中定义的 API 格式已实现
- 数据库中已有正确的绑定关系和提示词模板数据
