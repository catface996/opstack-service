# Quickstart: Rename Agent Model Fields

**Feature**: 043-rename-model-fields
**Date**: 2025-12-31

## Overview

本功能将 Agent 实体的模型字段重命名为更清晰的名称：
- `model` → `model_name`（模型友好名称）
- `model_id` → `provider_model_id`（模型提供商标识符）

## Quick Verification

### 1. Database Migration Verification

```sql
-- 验证字段已重命名
DESCRIBE agent;

-- 应该看到:
-- model_name VARCHAR(100)
-- provider_model_id VARCHAR(200)
```

### 2. API Response Verification

```bash
# 查询 Agent 详情
curl -X POST 'http://localhost:8081/api/service/v1/agents/query-by-id' \
  -H 'Content-Type: application/json' \
  -d '{"id": 1}'

# 预期响应包含:
# {
#   "data": {
#     "modelName": "Claude Opus 4.5",
#     "providerModelId": "anthropic.claude-opus-4-5-20251124-v1:0"
#   }
# }
```

### 3. Hierarchy Query Verification

```bash
# 查询层级结构
curl -X POST 'http://localhost:8081/api/service/v1/agent-bounds/query-hierarchy' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId": 4}'

# 预期每个 Agent 包含 modelName 和 providerModelId 字段
```

### 4. Executor Integration Verification

```bash
# 触发执行并检查日志
curl -X POST 'http://localhost:8081/api/service/v1/executions/trigger' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId": 4, "userMessage": "test"}'

# 查看应用日志中 CreateHierarchyRequest JSON
# llm_config.model_id 应该使用 provider_model_id 的值
```

## Usage Examples

### Creating Agent with New Fields

```bash
curl -X POST 'http://localhost:8081/api/service/v1/agents/create' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Test Agent",
    "role": "WORKER",
    "hierarchyLevel": "TEAM_WORKER",
    "modelName": "Claude Opus 4.5",
    "providerModelId": "anthropic.claude-opus-4-5-20251124-v1:0",
    "temperature": 0.7,
    "topP": 0.9,
    "maxTokens": 4096
  }'
```

### Updating Agent Model Configuration

```bash
curl -X POST 'http://localhost:8081/api/service/v1/agents/update' \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 1,
    "modelName": "Gemini 2.0 Flash",
    "providerModelId": "gemini-2.0-flash"
  }'
```

## Field Semantics

| Field | Purpose | Example Values |
|-------|---------|----------------|
| `modelName` | 用于 UI 显示的友好名称 | "Claude Opus 4.5", "Gemini 2.0 Flash" |
| `providerModelId` | 调用 LLM API 的模型标识符 | "anthropic.claude-opus-4-5-20251124-v1:0", "gemini-2.0-flash" |

## Default Behavior

当 `providerModelId` 为空时，系统行为：
1. 如果 `modelName` 非空，使用 `modelName` 作为 `llm_config.model_id`
2. 如果两者都为空，使用默认值 `gemini-2.0-flash`

## Migration Notes

- 数据库迁移脚本 V38 会自动重命名现有字段
- 现有数据保持不变，仅字段名变更
- 前端需要更新字段访问：`agent.model` → `agent.modelName`
