# Research: Refactor Executor Integration

**Feature**: 042-refactor-executor-integration
**Date**: 2025-12-30
**Status**: Complete

## Executive Summary

本文档研究了重构 Executor 集成所需的现有代码结构、数据流和变更点。核心变更是将 `agent_bound.id` 用作 Executor API 的 `agent_id`，并从关联的 `prompt_template.content` 获取 `system_prompt`。

## Current Implementation Analysis

### 1. Executor API Integration

**关键文件**: `ExecutorServiceClient.java`

当前实现使用 WebClient 调用 Executor 服务：
- `createHierarchy(CreateHierarchyRequest)` - 创建层级结构
- `startRun(StartRunRequest)` - 启动任务执行
- `streamEvents(String runId)` - 监听 SSE 事件流

配置参数：
- `executor.service.base-url` - Executor 服务地址
- `executor.service.timeout.connect` - 连接超时
- `executor.service.timeout.read` - 读取超时

### 2. Hierarchy Transformation

**关键文件**: `HierarchyTransformer.java`

当前转换逻辑：
```java
// Global Supervisor prompt 构建
"You are {agent.name}. {agent.specialty}"

// Team Supervisor prompt 构建
"You are {agent.name}, the supervisor of {teamName}. {agent.specialty}"

// Worker prompt 构建
"You are {agent.name}. {agent.specialty}"
```

**问题**：
1. `agent_id` 未传递给 Executor（当前请求格式无此字段）
2. `system_prompt` 基于 specialty 动态生成，而非使用 PromptTemplate

### 3. CreateHierarchyRequest 结构

**当前格式**（与 INTEGRATION_GUIDE.md 不一致）：
```java
CreateHierarchyRequest {
    name: String
    globalPrompt: String           // @JsonProperty("global_prompt")
    teams: List<TeamConfig>
}

TeamConfig {
    name: String
    supervisorPrompt: String       // @JsonProperty("supervisor_prompt")
    workers: List<WorkerConfig>
}

WorkerConfig {
    name: String
    role: String
    systemPrompt: String           // @JsonProperty("system_prompt")
    model: String
}
```

**目标格式**（参考 INTEGRATION_GUIDE.md）：
```json
{
    "name": "层级名称",
    "global_supervisor_agent": {
        "agent_id": "gs-001",           // 新增：绑定关系 ID
        "system_prompt": "..."          // 来自 PromptTemplate
    },
    "teams": [{
        "name": "团队名",
        "team_supervisor_agent": {
            "agent_id": "ts-001",       // 新增：绑定关系 ID
            "system_prompt": "..."      // 来自 PromptTemplate
        },
        "workers": [{
            "agent_id": "w-001",        // 新增：绑定关系 ID
            "name": "...",
            "role": "...",
            "system_prompt": "..."      // 来自 PromptTemplate
        }]
    }]
}
```

### 4. Data Flow Analysis

**当前数据流**：
```
queryHierarchy(topologyId)
    → AgentBound (含 agentId, agentName, agentSpecialty)
    → HierarchyStructureDTO (含 AgentDTO)
    → HierarchyTransformer.transform()
    → CreateHierarchyRequest (无 agent_id, prompt 动态生成)
```

**目标数据流**：
```
queryHierarchy(topologyId)
    → AgentBound (含 agentId, agentName, promptTemplateId)
    → 查询 PromptTemplate.content (批量)
    → HierarchyStructureDTO (含 AgentDTO, boundId, promptTemplateContent)
    → HierarchyTransformer.transform()
    → CreateHierarchyRequest (包含 agent_id=boundId, system_prompt=content)
```

### 5. Entity Relationships

```
AgentBound
├── id (Long) ← 将作为 Executor 的 agent_id
├── agentId (Long) → Agent.id
├── hierarchyLevel (GLOBAL_SUPERVISOR/TEAM_SUPERVISOR/TEAM_WORKER)
├── entityId (Long) → Topology.id 或 Node.id
└── entityType (TOPOLOGY/NODE)

Agent
├── id (Long)
├── name (String)
├── specialty (String)
├── promptTemplateId (Long) → PromptTemplate.id [可为 null]
└── model (String)

PromptTemplate
├── id (Long)
├── name (String)
├── currentVersion (Integer)
└── (content 来自版本表 JOIN)

PromptTemplateVersion
├── id (Long)
├── templateId (Long)
├── versionNumber (Integer)
└── content (TEXT) ← 实际的 system_prompt
```

## Key Findings

### 1. PromptTemplate Content 获取

`findByIdWithDetail(Long id)` 已实现，可获取含 content 的 PromptTemplate。

**潜在优化**：需要新增批量查询方法，避免 N+1 查询问题：
```java
// 建议新增
List<PromptTemplate> findByIdsWithDetail(List<Long> ids);
```

### 2. AgentDTO 需要扩展

当前 `AgentDTO` 缺少：
- `boundId` - 绑定关系 ID（作为 Executor 的 agent_id）
- `promptTemplateContent` - PromptTemplate 的实际内容

### 3. AgentBound 派生字段

`AgentBound` 已有 `agentSpecialty` 和 `agentModel` 派生字段，但缺少 `promptTemplateId`。

### 4. 默认提示词策略

当 Agent 未关联 PromptTemplate 或 content 为空时，需要回退到默认提示词：
```
格式："You are {agent.name}. {agent.specialty}"
```

### 5. 快速失败策略

当前 `ExecutorServiceClient` 已使用 `.doOnError()` 记录错误，但未实现快速失败。
需要确保错误时立即返回，不进行重试（符合 FR-007）。

## Technical Decisions

### Decision 1: agent_id 使用绑定关系 ID

**选择**：使用 `agent_bound.id` 作为 Executor 的 `agent_id`

**理由**：
- 绑定关系 ID 唯一标识了 Agent 在特定 Topology/Node 上的角色
- 同一 Agent 可能在多个位置被绑定，使用 `agent.id` 会导致歧义
- 事件回溯时可直接通过 `agent_id` 查询 `AgentBound`，获取完整上下文

### Decision 2: system_prompt 来源

**选择**：优先使用 PromptTemplate.content，回退到默认生成

**策略**：
```
if (promptTemplateId != null) {
    PromptTemplate pt = repository.findByIdWithDetail(promptTemplateId);
    if (pt != null && pt.getContent() != null && !pt.getContent().isEmpty()) {
        return pt.getContent();
    }
}
// 回退到默认
return "You are " + agent.getName() + ". " + agent.getSpecialty();
```

### Decision 3: API 格式兼容性

**选择**：修改 `CreateHierarchyRequest` 结构以匹配 INTEGRATION_GUIDE.md

**变更**：
- 新增 `GlobalSupervisorAgent` 内部类（含 agent_id, system_prompt）
- 新增 `TeamSupervisorAgent` 内部类（含 agent_id, system_prompt）
- 修改 `WorkerConfig` 添加 agent_id 字段

### Decision 4: 批量查询优化

**选择**：新增批量查询方法，减少数据库交互

**方案**：
1. 收集所有 Agent 的 promptTemplateId
2. 批量查询 PromptTemplate（含 content）
3. 构建 Map<Long, String> 供快速查找

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Executor API 格式变更 | Low | High | 严格参照 INTEGRATION_GUIDE.md |
| PromptTemplate 内容为空 | Medium | Low | 默认提示词回退策略 |
| 批量查询性能问题 | Low | Medium | 使用 IN 查询，限制批量大小 |
| 事件追溯失败 | Low | Medium | agent_id 使用稳定的 bound.id |

## Implementation Recommendations

### Phase 1: DTO 扩展
1. 扩展 `AgentDTO`，添加 `boundId` 和 `promptTemplateContent`
2. 修改 `AgentBoundApplicationServiceImpl.queryHierarchy()` 填充新字段

### Phase 2: Repository 优化
1. 新增 `PromptTemplateRepository.findByIdsWithDetail(List<Long> ids)`
2. 实现批量查询 PromptTemplate 含 content

### Phase 3: 转换器重构
1. 修改 `CreateHierarchyRequest` 结构，添加 agent_id 字段
2. 重构 `HierarchyTransformer`，使用 boundId 和 promptTemplateContent

### Phase 4: 测试验证
1. 单元测试：转换逻辑、默认提示词
2. 集成测试：完整流程验证

## References

- [INTEGRATION_GUIDE.md](../../../op-stack-executor/docs/INTEGRATION_GUIDE.md) - Executor API 规范
- [spec.md](./spec.md) - 功能规格说明
- [HierarchyTransformer.java](../../application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java) - 现有转换器
