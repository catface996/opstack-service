# Tasks: Refactor Executor Integration

**Input**: Design documents from `/specs/042-refactor-executor-integration/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: 本功能规格中未明确要求测试任务，但包含单元测试以验证核心转换逻辑正确性。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions (DDD Multi-Module)

```text
application/application-api/src/main/java/com/catface996/aiops/application/api/
application/application-impl/src/main/java/com/catface996/aiops/application/impl/
domain/repository-api/src/main/java/com/catface996/aiops/repository/
infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/
infrastructure/repository/mysql-impl/src/main/resources/mapper/
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 准备工作，无需额外配置

- [X] T001 确认分支 `042-refactor-executor-integration` 已创建并切换

**Checkpoint**: Setup complete

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 核心基础设施变更，所有用户故事都依赖于此

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### DTO 扩展

- [X] T002 [P] 扩展 AgentDTO 添加 `boundId` 字段 in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/AgentDTO.java`
- [X] T003 [P] 扩展 AgentDTO 添加 `promptTemplateContent` 字段 in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/AgentDTO.java`

### Repository 扩展

- [X] T004 [P] 新增 `findByIdsWithDetail(List<Long> ids)` 方法声明 in `domain/repository-api/src/main/java/com/catface996/aiops/repository/prompt/PromptTemplateRepository.java`
- [X] T005 [P] 实现批量查询 PromptTemplate 含 content 的 SQL in `infrastructure/repository/mysql-impl/src/main/resources/mapper/prompt/PromptTemplateMapper.xml`
- [X] T006 实现 `findByIdsWithDetail` 方法 in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/prompt/PromptTemplateRepositoryImpl.java` (依赖 T004, T005)

### Request DTO 重构

- [X] T007 [P] 重构 CreateHierarchyRequest 添加 SupervisorAgentConfig 内部类 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/CreateHierarchyRequest.java`
- [X] T008 修改 WorkerConfig 添加 `agentId` 字段 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/CreateHierarchyRequest.java` (依赖 T007)

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - 执行多智能体任务 (Priority: P1) 🎯 MVP

**Goal**: 基于拓扑图层级结构触发多智能体协作任务，Executor 收到的 `agent_id` 是绑定关系 ID，`system_prompt` 来自 PromptTemplate

**Independent Test**: 调用 `/api/service/v1/executions/start` 触发任务，验证 Executor 请求格式正确

### Implementation for User Story 1

- [X] T009 [US1] 修改 `toAgentDTO` 方法填充 `boundId` 字段 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agentbound/AgentBoundApplicationServiceImpl.java`
- [X] T010 [US1] 在 SQL 中直接 JOIN prompt_template 获取 content（优化：一次查询获取所有数据）in `infrastructure/repository/mysql-impl/src/main/resources/mapper/agentbound/AgentBoundMapper.xml`
- [X] T011 [US1] 修改 `toAgentDTO` 方法填充 `promptTemplateContent` 字段 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agentbound/AgentBoundApplicationServiceImpl.java`
- [X] T012 [US1] 重构 `buildGlobalPrompt` 方法：优先使用 promptTemplateContent，回退到默认生成 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T013 [US1] 重构 `buildSupervisorPrompt` 方法：优先使用 promptTemplateContent，回退到默认生成 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T014 [US1] 重构 `buildWorkerSystemPrompt` 方法：优先使用 promptTemplateContent，回退到默认生成 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T015 [US1] 修改 `transform` 方法：构建 `global_supervisor_agent` 结构（含 agent_id=boundId） in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T016 [US1] 修改 `transformTeams` 方法：构建 `team_supervisor_agent` 结构（含 agent_id=boundId） in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T017 [US1] 修改 `transformWorker` 方法：添加 agent_id=boundId in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T018 [US1] 添加默认提示词生成工具方法 `generateDefaultPrompt(name, specialty)` in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`

**Checkpoint**: User Story 1 should be fully functional - Executor 收到正确的 agent_id 和 system_prompt

---

## Phase 4: User Story 2 - 事件追溯到绑定关系 (Priority: P2)

**Goal**: Executor 返回的事件流中的 `agent_id` 能够追溯到具体的绑定关系

**Independent Test**: 监听 SSE 事件流，验证 `source.agent_id` 可通过 `AgentBound.id` 查询到完整绑定信息

### Implementation for User Story 2

- [X] T019 [US2] 验证 `AgentBoundRepository.findById` 能够正确返回绑定详情（已有实现，确认工作正常）
- [X] T020 [US2] 添加日志记录：在事件处理时记录 agent_id 以便追溯 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/ExecutorServiceClient.java`

**Checkpoint**: User Story 2 should be fully functional - 事件中的 agent_id 可追溯

---

## Phase 5: User Story 3 - 层级结构转换正确性 (Priority: P3)

**Goal**: 层级结构转换器能够正确处理各种配置场景，确保数据完整性

**Independent Test**: 单元测试验证各种边界场景的转换结果

### Implementation for User Story 3

- [X] T021 [US3] 处理多个 Global Supervisor 绑定场景：使用第一个 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agentbound/AgentBoundApplicationServiceImpl.java`
- [X] T022 [US3] 处理节点无 Team Supervisor 场景：使用默认 supervisor 配置 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T023 [US3] 处理 PromptTemplate 不存在或已删除场景：回退到默认提示词 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T024 [US3] 处理 PromptTemplate content 为空场景：回退到默认提示词 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T025 [US3] 处理拓扑图无任何绑定场景：返回明确错误提示 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`

**Checkpoint**: User Story 3 should be fully functional - 所有边界场景正确处理

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 跨用户故事的改进和验证

- [X] T026 [P] 确保快速失败策略：已确认无 retry 逻辑 in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/ExecutorServiceClient.java`
- [ ] T027 [P] 添加 HierarchyTransformer 单元测试验证转换逻辑 in `application/application-impl/src/test/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformerTest.java`
- [X] T028 代码编译验证：执行 `mvn clean package -DskipTests` ✓
- [X] T029 运行 quickstart.md 验证场景 ✓ (boundId + promptTemplateContent 验证通过)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational phase completion
- **User Story 2 (Phase 4)**: Depends on Foundational phase completion, integrates with US1
- **User Story 3 (Phase 5)**: Depends on Foundational phase completion, builds on US1 implementation
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Core functionality - no dependencies on other stories
- **User Story 2 (P2)**: Depends on US1 (uses the agent_id sent by US1 implementation)
- **User Story 3 (P3)**: Extends US1 implementation for edge cases

### Within Each User Story

- DTO 扩展 before 服务层修改
- Repository 扩展 before 服务层调用
- Transform 方法修改 before 验证

### Parallel Opportunities

**Foundational Phase (Phase 2)**:
```bash
# 可并行执行：
Task: T002 扩展 AgentDTO 添加 boundId 字段
Task: T003 扩展 AgentDTO 添加 promptTemplateContent 字段
Task: T004 新增 findByIdsWithDetail 方法声明
Task: T005 实现批量查询 SQL
Task: T007 重构 CreateHierarchyRequest
```

---

## Parallel Example: User Story 1

```bash
# Prompt 构建方法可并行修改（不同方法）:
Task: T012 重构 buildGlobalPrompt 方法
Task: T013 重构 buildSupervisorPrompt 方法
Task: T014 重构 buildWorkerSystemPrompt 方法

# Transform 方法需顺序执行（依赖上述修改）:
Task: T015 修改 transform 方法
Task: T016 修改 transformTeams 方法
Task: T017 修改 transformWorker 方法
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002-T008)
3. Complete Phase 3: User Story 1 (T009-T018)
4. **STOP and VALIDATE**: 测试 Executor 请求格式是否正确
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test → Deploy (MVP!)
3. Add User Story 2 → Test → Deploy
4. Add User Story 3 → Test → Deploy
5. Polish phase for final improvements

### Key Files Changed

| File | Changes |
|------|---------|
| `AgentDTO.java` | +boundId, +promptTemplateContent |
| `PromptTemplateRepository.java` | +findByIdsWithDetail |
| `PromptTemplateMapper.xml` | +selectByIdsWithDetail SQL |
| `PromptTemplateRepositoryImpl.java` | +findByIdsWithDetail impl |
| `CreateHierarchyRequest.java` | +SupervisorAgentConfig, +agentId |
| `AgentBoundApplicationServiceImpl.java` | Fill PromptTemplate content |
| `HierarchyTransformer.java` | Major refactor for agent_id and prompt |
| `ExecutorServiceClient.java` | Logging + fast-fail validation |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- 所有提示词相关方法需要统一使用 `generateDefaultPrompt` 工具方法
- `agent_id` 字段类型为 String（Long 转换为 String）
- 快速失败策略：不使用 retry，错误直接传播
- 遵循宪法：终止进程使用 `lsof -ti :8081 | xargs kill`
