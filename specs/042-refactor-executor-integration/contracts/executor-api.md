# API Contract: Executor Integration

**Feature**: 042-refactor-executor-integration
**Date**: 2025-12-30
**Status**: Complete

## Overview

本文档定义 op-stack-service 与 op-stack-executor 之间的 API 契约。

## Endpoint: Create Hierarchy

**URL**: `POST /api/executor/v1/hierarchies/create`
**Content-Type**: `application/json`

### Request Schema

```json
{
    "name": "string (required)",
    "global_supervisor_agent": {
        "agent_id": "string (required)",
        "system_prompt": "string (required)"
    },
    "teams": [
        {
            "name": "string (required)",
            "team_supervisor_agent": {
                "agent_id": "string (required)",
                "system_prompt": "string (required)"
            },
            "workers": [
                {
                    "agent_id": "string (required)",
                    "name": "string (required)",
                    "role": "string (required)",
                    "system_prompt": "string (required)",
                    "model": "string (optional, default: gemini-2.0-flash)"
                }
            ]
        }
    ]
}
```

### Field Mapping

| Executor Field | op-stack-service Source | Notes |
|----------------|-------------------------|-------|
| `name` | `topology.name + "_" + timestamp` | 唯一层级名称 |
| `global_supervisor_agent.agent_id` | `agent_bound.id` (String) | 绑定关系 ID |
| `global_supervisor_agent.system_prompt` | `prompt_template.content` 或默认生成 | - |
| `team_supervisor_agent.agent_id` | `agent_bound.id` (String) | 绑定关系 ID |
| `team_supervisor_agent.system_prompt` | `prompt_template.content` 或默认生成 | - |
| `workers[].agent_id` | `agent_bound.id` (String) | 绑定关系 ID |
| `workers[].name` | `agent.name` | Agent 名称 |
| `workers[].role` | `agent.role` (lowercase) | Agent 角色 |
| `workers[].system_prompt` | `prompt_template.content` 或默认生成 | - |
| `workers[].model` | `agent.model` | LLM 模型标识 |

### Response Schema

```json
{
    "code": 0,
    "message": "success",
    "data": {
        "hierarchy_id": "string"
    }
}
```

### Error Responses

| HTTP Status | Code | Description |
|-------------|------|-------------|
| 400 | 40001 | 请求参数无效 |
| 500 | 50001 | 内部服务器错误 |

---

## Endpoint: Start Run

**URL**: `POST /api/executor/v1/runs/start`
**Content-Type**: `application/json`

### Request Schema

```json
{
    "hierarchy_id": "string (required)",
    "task": "string (required)"
}
```

### Field Mapping

| Executor Field | op-stack-service Source | Notes |
|----------------|-------------------------|-------|
| `hierarchy_id` | Create Hierarchy 返回值 | - |
| `task` | 用户输入的任务描述 | - |

### Response Schema

```json
{
    "code": 0,
    "message": "success",
    "data": {
        "id": "string"
    }
}
```

---

## Endpoint: Stream Events (SSE)

**URL**: `POST /api/executor/v1/runs/stream`
**Content-Type**: `application/json`
**Accept**: `text/event-stream`

### Request Schema

```json
{
    "id": "string (required)"
}
```

### SSE Event Format

```
event: {category}.{action}
data: {"run_id": "...", "timestamp": "...", "sequence": 123, "source": {...}, "event": {...}, "data": {...}}
```

### Source Object

```json
{
    "agent_id": "string",
    "agent_type": "global_supervisor | team_supervisor | worker",
    "agent_name": "string",
    "team_name": "string | null"
}
```

**Important**: `source.agent_id` 值等于创建层级时传入的 `agent_id`，即 `agent_bound.id`。可用于追溯绑定关系和关联实体。

### Event Types

| Category | Action | Description |
|----------|--------|-------------|
| lifecycle | started | 运行开始 |
| lifecycle | completed | 运行完成 |
| lifecycle | failed | 运行失败 |
| lifecycle | cancelled | 运行取消 |
| llm | stream | LLM 流式输出 |
| llm | reasoning | LLM 推理过程 |
| llm | tool_call | 工具调用 |
| llm | tool_result | 工具结果 |
| dispatch | team | 调度团队 |
| dispatch | worker | 调度 Worker |
| system | topology | 拓扑结构 |
| system | warning | 警告信息 |
| system | error | 错误信息 |

---

## agent_id Traceability

### 从 agent_id 追溯绑定关系

```sql
-- 通过 agent_id (即 agent_bound.id) 查询完整上下文
SELECT
    ab.id AS bound_id,
    ab.agent_id,
    ab.hierarchy_level,
    ab.entity_id,
    ab.entity_type,
    a.name AS agent_name,
    a.role AS agent_role,
    a.specialty AS agent_specialty,
    COALESCE(t.name, n.name) AS entity_name
FROM agent_bound ab
JOIN agent a ON ab.agent_id = a.id
LEFT JOIN topology t ON ab.entity_type = 'TOPOLOGY' AND ab.entity_id = t.id
LEFT JOIN node n ON ab.entity_type = 'NODE' AND ab.entity_id = n.id
WHERE ab.id = #{agentIdFromEvent}
    AND ab.deleted = 0
```

### Java 实现

```java
// 通过 boundId 查询绑定详情
public AgentBoundDTO queryByBoundId(Long boundId) {
    return agentBoundRepository.findById(boundId)
        .map(this::toDTO)
        .orElse(null);
}
```

---

## Default System Prompt

当 Agent 未关联 PromptTemplate 或 content 为空时：

```
格式: "You are {agent.name}. {agent.specialty}"

示例:
- "You are 性能分析师. 负责分析系统性能指标，识别性能瓶颈。"
- "You are Global Supervisor. Coordinate and oversee the work of all team supervisors."
```

---

## Error Handling (Fast-Fail)

### 策略

根据 FR-007，当 Executor 服务不可用或返回错误时：
- **MUST** 立即返回错误
- **MUST NOT** 进行重试
- 由调用方决定后续处理

### 实现

```java
public Mono<CreateHierarchyResponse> createHierarchy(CreateHierarchyRequest request) {
    return webClient.post()
        .uri("/api/executor/v1/hierarchies/create")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(CreateHierarchyResponse.class)
        // 快速失败：不使用 retry
        .doOnError(e -> log.error("Executor service error: {}", e.getMessage()));
}
```

### 错误传播

```java
// 调用方接收到错误后
executorClient.createHierarchy(request)
    .doOnError(e -> {
        // 记录错误
        log.error("Failed to create hierarchy: {}", e.getMessage());
        // 立即返回错误给上层调用方
        throw new ExecutorServiceException("Executor 服务调用失败: " + e.getMessage());
    });
```
