# Data Model: Trigger Multi-Agent Execution

**Feature**: 039-trigger-multiagent-execution
**Date**: 2025-12-29

## Overview

This feature does not introduce new database entities. It primarily involves:
1. Request/Response DTOs for the execution endpoint
2. DTOs for executor service integration
3. Reuse of existing HierarchicalTeamDTO from Feature 038

## DTO Definitions

### 1. Execution Request DTOs

#### TriggerExecutionRequest

**Location**: `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/execution/request/TriggerExecutionRequest.java`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| topologyId | Long | Yes | Topology ID to execute |
| userMessage | String | Yes | User's task/message for execution |

**Validation Rules**:
- `topologyId`: NotNull, Positive
- `userMessage`: NotBlank, Size(max=10000)

#### ExecutionEventDTO

**Location**: `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/execution/ExecutionEventDTO.java`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| type | String | Yes | Event type (message, thinking, tool_call, tool_result, error, complete) |
| agentName | String | No | Name of the agent producing the event |
| agentRole | String | No | Role of the agent (GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER) |
| content | String | No | Event content/message |
| timestamp | LocalDateTime | Yes | Event timestamp |
| metadata | Map<String, Object> | No | Additional event metadata |

### 2. Executor Service DTOs

These DTOs map to the Op-Stack Executor API.

#### CreateHierarchyRequest

**Location**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/CreateHierarchyRequest.java`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Hierarchy name (from topology name) |
| global_prompt | String | Yes | Global supervisor prompt (from agent specialty) |
| teams | List<TeamConfig> | Yes | Team configurations |

**TeamConfig**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Team name (from node name) |
| supervisor | AgentConfig | Yes | Team supervisor configuration |
| workers | List<AgentConfig> | Yes | Worker agent configurations |

**AgentConfig**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Agent name |
| model | String | Yes | LLM model identifier |
| prompt | String | Yes | Agent prompt/specialty |

#### CreateHierarchyResponse

| Field | Type | Description |
|-------|------|-------------|
| hierarchy_id | String | Created hierarchy identifier |
| name | String | Hierarchy name |
| created_at | String | Creation timestamp |

#### StartRunRequest

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| hierarchy_id | String | Yes | Hierarchy to execute |
| task | String | Yes | Task/message from user |

#### StartRunResponse

| Field | Type | Description |
|-------|------|-------------|
| run_id | String | Started run identifier |
| hierarchy_id | String | Associated hierarchy |
| task | String | The task being executed |
| status | String | Run status |
| started_at | String | Start timestamp |

#### ExecutorEvent

Represents an SSE event from the executor service.

| Field | Type | Description |
|-------|------|-------------|
| type | String | Event type |
| agent | String | Agent name |
| content | String | Event content |
| timestamp | String | Event timestamp |
| data | Map<String, Object> | Additional data |

## Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend Request                         │
├─────────────────────────────────────────────────────────────────┤
│  TriggerExecutionRequest                                        │
│  ├── topologyId: 4                                              │
│  └── userMessage: "Analyze the system performance"              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Existing System Query                         │
├─────────────────────────────────────────────────────────────────┤
│  HierarchicalTeamDTO (from Feature 038)                         │
│  ├── topologyId: 4                                              │
│  ├── topologyName: "Production Environment"                     │
│  ├── globalSupervisor: AgentDTO                                 │
│  │   ├── name: "Global Monitor"                                 │
│  │   ├── specialty: "System-wide coordination..."               │
│  │   └── model: "claude-3-opus"                                 │
│  └── teams: List<TeamDTO>                                       │
│      └── [0]: TeamDTO                                           │
│          ├── nodeId: 101                                        │
│          ├── nodeName: "Application Server"                     │
│          ├── supervisor: AgentDTO                               │
│          └── workers: List<AgentDTO>                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ Transform
┌─────────────────────────────────────────────────────────────────┐
│                   Executor API Request                          │
├─────────────────────────────────────────────────────────────────┤
│  CreateHierarchyRequest                                         │
│  ├── name: "Production Environment"                             │
│  ├── global_prompt: "System-wide coordination..."               │
│  └── teams:                                                     │
│      └── [0]:                                                   │
│          ├── name: "Application Server"                         │
│          ├── supervisor: AgentConfig                            │
│          └── workers: List<AgentConfig>                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ Start Run
┌─────────────────────────────────────────────────────────────────┐
│                   Executor SSE Events                           │
├─────────────────────────────────────────────────────────────────┤
│  ExecutorEvent stream                                           │
│  ├── {type: "thinking", agent: "Global Monitor", ...}           │
│  ├── {type: "message", agent: "Team Supervisor", ...}           │
│  ├── {type: "tool_call", agent: "Worker 1", ...}                │
│  └── {type: "complete", ...}                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ Transform
┌─────────────────────────────────────────────────────────────────┐
│                   Frontend SSE Response                         │
├─────────────────────────────────────────────────────────────────┤
│  ExecutionEventDTO stream                                       │
│  ├── {type: "thinking", agentName: "Global Monitor", ...}       │
│  ├── {type: "message", agentName: "Team Supervisor", ...}       │
│  └── {type: "complete", ...}                                    │
└─────────────────────────────────────────────────────────────────┘
```

## Existing Entities Used

This feature reuses the following existing entities/DTOs without modification:

1. **HierarchicalTeamDTO** (Feature 038)
   - Contains topology information and team structure
   - Already provides all data needed for executor hierarchy creation

2. **AgentDTO** (existing)
   - Contains agent name, role, specialty, model
   - Used within HierarchicalTeamDTO

3. **TeamDTO** (Feature 038)
   - Contains node information and agent assignments
   - Supervisor and workers already structured

## No Database Changes Required

This feature operates purely at the service layer:
- Reads existing topology data via HierarchicalTeamDTO
- Calls external executor service
- Streams events to frontend

No new database tables, columns, or migrations are needed.
