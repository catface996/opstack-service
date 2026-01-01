# Frontend Integration Guide: Stream Event 重构

**Feature**: 042-refactor-executor-integration
**Date**: 2025-12-31
**Audience**: 前端开发人员

---

## 变更概述

本次重构涉及 Executor SSE 事件流中 `agent_id` 的含义变化：

| 字段 | 重构前 | 重构后 |
|------|--------|--------|
| `agent_id` | `agent.id`（Agent 表主键） | `agent_bound.id`（绑定关系 ID） |

**核心变化**：`agent_id` 现在代表 Agent 与拓扑实体（Topology/Node）的**绑定关系 ID**，而非 Agent 本身的 ID。

---

## SSE 事件格式

### 事件结构

```
event: {category}.{action}
data: {JSON payload}
```

### 完整 Payload 结构

```typescript
interface ExecutorEvent {
  // 运行 ID
  run_id: string;

  // 事件时间戳 (ISO 8601)
  timestamp: string;

  // 事件序号（用于排序）
  sequence: number;

  // 事件来源 ⚠️ 重点关注
  source: EventSource;

  // 事件详情
  event: {
    category: string;  // lifecycle | llm | dispatch | system
    action: string;    // started | stream | tool_call | ...
  };

  // 事件数据（根据 event 类型不同而变化）
  data: Record<string, any>;
}

interface EventSource {
  // ⚠️ 绑定关系 ID（agent_bound.id），用于追溯
  agent_id: string;

  // Agent 类型
  agent_type: "global_supervisor" | "team_supervisor" | "worker";

  // Agent 名称
  agent_name: string;

  // 所属团队名称（global_supervisor 时为 null）
  team_name: string | null;
}
```

### 事件类型汇总

| Category | Action | 说明 | data 结构 |
|----------|--------|------|-----------|
| `lifecycle` | `started` | 运行开始 | `{}` |
| `lifecycle` | `completed` | 运行完成 | `{ summary: string }` |
| `lifecycle` | `failed` | 运行失败 | `{ error: string }` |
| `lifecycle` | `cancelled` | 运行取消 | `{}` |
| `llm` | `stream` | LLM 流式输出 | `{ content: string }` |
| `llm` | `reasoning` | LLM 推理过程 | `{ thought: string }` |
| `llm` | `tool_call` | 工具调用 | `{ tool: string, args: object }` |
| `llm` | `tool_result` | 工具结果 | `{ tool: string, result: any }` |
| `dispatch` | `team` | 调度团队 | `{ team_name: string, task: string }` |
| `dispatch` | `worker` | 调度 Worker | `{ worker_name: string, task: string }` |
| `system` | `topology` | 拓扑结构 | `{ hierarchy: object }` |
| `system` | `warning` | 警告信息 | `{ message: string }` |
| `system` | `error` | 错误信息 | `{ message: string, code: string }` |

---

## agent_id 追溯

### 追溯查询 API

通过 `source.agent_id` 可以追溯到完整的绑定上下文：

```bash
POST /api/service/v1/agent-bounds/get
Content-Type: application/json

{
  "id": 10  # source.agent_id 的值
}
```

**响应示例**：

```json
{
  "code": 0,
  "data": {
    "id": 10,
    "agentId": 2,
    "agentName": "Team Lead Alpha",
    "agentRole": "TEAM_SUPERVISOR",
    "hierarchyLevel": "TEAM_SUPERVISOR",
    "entityId": 42,
    "entityType": "NODE",
    "entityName": "支付路由数据库-MySQL"
  }
}
```

### 追溯信息说明

| 字段 | 说明 | 用途 |
|------|------|------|
| `id` | 绑定关系 ID | 等于 `source.agent_id` |
| `agentId` | Agent 主键 ID | 查询 Agent 详情 |
| `agentName` | Agent 名称 | UI 显示 |
| `hierarchyLevel` | 层级类型 | 区分 GLOBAL/TEAM_SUPERVISOR/WORKER |
| `entityId` | 关联实体 ID | Topology 或 Node 的 ID |
| `entityType` | 实体类型 | `TOPOLOGY` 或 `NODE` |
| `entityName` | 实体名称 | UI 显示（拓扑图或节点名称） |

---

## 前端改动指南

### 1. TypeScript 类型定义更新

```typescript
// 旧类型（已废弃）
interface OldEventSource {
  agent_id: string;  // 原指向 agent.id
}

// 新类型
interface EventSource {
  agent_id: string;      // 现指向 agent_bound.id
  agent_type: string;
  agent_name: string;
  team_name: string | null;
}
```

### 2. 事件处理逻辑更新

```typescript
// 处理 SSE 事件
function handleExecutorEvent(event: ExecutorEvent) {
  const { source, event: eventInfo, data } = event;

  // 使用 source 中的信息显示
  const displayName = source.agent_name;
  const agentType = source.agent_type;
  const teamName = source.team_name;

  // ⚠️ 如果需要查询完整上下文，使用 agent_id 追溯
  if (needDetailedInfo) {
    const bindingDetail = await fetchAgentBinding(source.agent_id);
    // bindingDetail 包含 entityName（拓扑图/节点名称）等
  }

  // 根据事件类型处理
  switch (`${eventInfo.category}.${eventInfo.action}`) {
    case 'llm.stream':
      appendMessage(displayName, data.content);
      break;
    case 'llm.tool_call':
      showToolCall(displayName, data.tool, data.args);
      break;
    // ...
  }
}
```

### 3. Agent 关联显示

```typescript
// 显示 Agent 与拓扑实体的关联
async function showAgentContext(agentId: string) {
  const binding = await fetch(`/api/service/v1/agent-bounds/get`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id: parseInt(agentId) })
  }).then(r => r.json());

  if (binding.data) {
    return {
      agentName: binding.data.agentName,
      bindingContext: binding.data.entityType === 'TOPOLOGY'
        ? `全局监管者 - ${binding.data.entityName}`
        : `${binding.data.entityName} 团队`
    };
  }
}
```

### 4. UI 展示建议

```
┌─────────────────────────────────────────────┐
│ 📍 Team Lead Alpha                          │
│ 角色: TEAM_SUPERVISOR | 团队: 支付路由数据库 │
├─────────────────────────────────────────────┤
│ [llm.stream]                                │
│ 正在分析容器化部署方案...                    │
└─────────────────────────────────────────────┘
```

---

## SSE 连接示例

### JavaScript EventSource

```javascript
const eventSource = new EventSource('/api/executor/v1/runs/stream?id=' + runId);

eventSource.onmessage = (e) => {
  const event = JSON.parse(e.data);
  handleExecutorEvent(event);
};

eventSource.addEventListener('llm.stream', (e) => {
  const event = JSON.parse(e.data);
  appendContent(event.source.agent_name, event.data.content);
});

eventSource.addEventListener('lifecycle.completed', (e) => {
  eventSource.close();
});
```

### Fetch API (POST 方式)

```javascript
async function streamEvents(runId: string) {
  const response = await fetch('/api/executor/v1/runs/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify({ id: runId })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const text = decoder.decode(value);
    const lines = text.split('\n');

    for (const line of lines) {
      if (line.startsWith('data: ')) {
        const event = JSON.parse(line.slice(6));
        handleExecutorEvent(event);
      }
    }
  }
}
```

---

## 迁移检查清单

- [ ] 更新 TypeScript 类型定义（`EventSource` 接口）
- [ ] 更新事件处理逻辑，使用 `source.agent_name` 显示名称
- [ ] 如需显示实体上下文，调用追溯 API `/api/service/v1/agent-bounds/get`
- [ ] 测试所有事件类型的处理
- [ ] 验证 UI 正确显示 Agent 信息和所属团队/拓扑

---

## 常见问题

### Q1: 为什么 agent_id 改为绑定关系 ID？

**A**: 同一个 Agent 可以绑定到多个不同的拓扑实体（不同节点）。使用绑定关系 ID 可以精确定位到具体是哪个实体上的 Agent 产生的事件，而非仅知道是哪个 Agent。

### Q2: 如何获取原来的 agent.id？

**A**: 通过追溯 API 查询绑定详情，响应中的 `agentId` 字段就是原来的 `agent.id`。

### Q3: source.agent_name 和追溯返回的 agentName 一样吗？

**A**: 是的，都是 Agent 的名称。`source` 中已包含基本信息，无需额外查询即可显示。追溯 API 用于获取更多上下文（如所属实体名称）。
