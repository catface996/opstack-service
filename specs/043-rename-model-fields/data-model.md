# Data Model: Rename Agent Model Fields

**Feature**: 043-rename-model-fields
**Date**: 2025-12-31

## Entity Changes

### Agent Entity

**Before**:
```
Agent
├── id: Long (PK)
├── name: String
├── role: String
├── hierarchyLevel: String
├── specialty: String
├── promptTemplateId: Long (FK)
├── model: String          ← TO BE RENAMED
├── modelId: String        ← TO BE RENAMED
├── temperature: Double
├── topP: Double
├── maxTokens: Integer
├── maxRuntime: Integer
└── ... (audit fields)
```

**After**:
```
Agent
├── id: Long (PK)
├── name: String
├── role: String
├── hierarchyLevel: String
├── specialty: String
├── promptTemplateId: Long (FK)
├── modelName: String         ← RENAMED FROM model
├── providerModelId: String   ← RENAMED FROM modelId
├── temperature: Double
├── topP: Double
├── maxTokens: Integer
├── maxRuntime: Integer
└── ... (audit fields)
```

### Database Schema Change

**Migration V38**:
```sql
-- Rename model → model_name
ALTER TABLE agent RENAME COLUMN model TO model_name;

-- Rename model_id → provider_model_id
ALTER TABLE agent RENAME COLUMN model_id TO provider_model_id;

-- Update column comments for clarity
ALTER TABLE agent
    MODIFY COLUMN model_name VARCHAR(100) NULL
    COMMENT '模型友好名称（如 Claude Opus 4.5, gemini-2.0-flash）';

ALTER TABLE agent
    MODIFY COLUMN provider_model_id VARCHAR(200) NULL
    COMMENT '模型提供商标识符（如 anthropic.claude-opus-4-5-20251124-v1:0）';
```

## Field Mapping

### Java Field Names (camelCase)

| Layer | Old Field | New Field |
|-------|-----------|-----------|
| AgentPO | `model` | `modelName` |
| AgentPO | `modelId` | `providerModelId` |
| Agent (Domain) | `model` | `modelName` |
| Agent (Domain) | `modelId` | `providerModelId` |
| AgentDTO | `model` | `modelName` |
| AgentDTO | `modelId` | `providerModelId` |
| CreateAgentRequest | `model` | `modelName` |
| CreateAgentRequest | `modelId` | `providerModelId` |
| UpdateAgentRequest | `model` | `modelName` |
| UpdateAgentRequest | `modelId` | `providerModelId` |

### Database Column Names (snake_case)

| Old Column | New Column |
|------------|------------|
| `model` | `model_name` |
| `model_id` | `provider_model_id` |

### AgentBound Related Fields

| Layer | Old Field | New Field |
|-------|-----------|-----------|
| AgentBoundPO | `agentModel` | `agentModelName` |
| AgentBoundPO | `agentModelId` | `agentProviderModelId` |
| AgentBound (Domain) | `agentModel` | `agentModelName` |
| AgentBound (Domain) | `agentModelId` | `agentProviderModelId` |

### SQL Mapper Aliases

**AgentBoundMapper.xml**:
```sql
-- Old
a.model AS agentModel, a.model_id AS agentModelId

-- New
a.model_name AS agentModelName, a.provider_model_id AS agentProviderModelId
```

## API Response Format

### AgentDTO JSON Response

**Before**:
```json
{
  "id": 1,
  "name": "Global Supervisor",
  "model": "Claude Opus 4.5",
  "modelId": "anthropic.claude-opus-4-5-20251124-v1:0"
}
```

**After**:
```json
{
  "id": 1,
  "name": "Global Supervisor",
  "modelName": "Claude Opus 4.5",
  "providerModelId": "anthropic.claude-opus-4-5-20251124-v1:0"
}
```

## Executor Integration

### LlmConfig Mapping

`HierarchyTransformer.buildLlmConfig()` 方法：

**Before**:
```java
String effectiveModelId = agent.getModelId() != null && !agent.getModelId().trim().isEmpty()
        ? agent.getModelId()
        : (agent.getModel() != null ? agent.getModel() : DEFAULT_MODEL);
```

**After**:
```java
String effectiveModelId = agent.getProviderModelId() != null && !agent.getProviderModelId().trim().isEmpty()
        ? agent.getProviderModelId()
        : (agent.getModelName() != null ? agent.getModelName() : DEFAULT_MODEL);
```

**Output to Executor** (unchanged):
```json
{
  "llm_config": {
    "model_id": "anthropic.claude-opus-4-5-20251124-v1:0",
    "temperature": 0.7,
    "top_p": 0.9,
    "max_tokens": 4096
  }
}
```

## Validation Rules

| Field | Validation | Notes |
|-------|------------|-------|
| `modelName` | Optional, max 100 chars | 用于显示的友好名称 |
| `providerModelId` | Optional, max 200 chars | 用于 API 调用的模型标识符 |

## Default Behavior

当 `providerModelId` 为空时：
1. 优先使用 `modelName` 作为 `llm_config.model_id`
2. 如果 `modelName` 也为空，使用默认值 `gemini-2.0-flash`
