# Research: Rename Agent Model Fields

**Feature**: 043-rename-model-fields
**Date**: 2025-12-31

## Research Summary

本功能是简单的字段重命名，无需外部技术研究。以下是实施前的代码分析。

## 1. Current Field Usage Analysis

### 1.1 Database Schema (V37)

当前 `agent` 表字段：
- `model` VARCHAR(100) - 模型名称（如 "Claude Opus 4.5"）
- `model_id` VARCHAR(200) - 完整模型 ID（如 "anthropic.claude-opus-4-5-20251124-v1:0"）

**Decision**: 重命名为 `model_name` 和 `provider_model_id`
**Rationale**:
- `model_name` 明确表示这是显示用的友好名称
- `provider_model_id` 避免与数据库主键 ID 混淆，明确是提供商的模型标识符

### 1.2 Code Layer Analysis

| Layer | Current Fields | New Fields |
|-------|----------------|------------|
| AgentPO | `model`, `modelId` | `modelName`, `providerModelId` |
| Agent (Domain) | `model`, `modelId` | `modelName`, `providerModelId` |
| AgentDTO | `model`, `modelId` | `modelName`, `providerModelId` |
| AgentBound | `agentModel`, `agentModelId` | `agentModelName`, `agentProviderModelId` |
| AgentBoundPO | `agentModel`, `agentModelId` | `agentModelName`, `agentProviderModelId` |
| HierarchyTransformer | `getModel()`, `getModelId()` | `getModelName()`, `getProviderModelId()` |

### 1.3 SQL Mapper Analysis

`AgentBoundMapper.xml` 中的 SQL 查询使用别名：
```sql
a.model AS agentModel, a.model_id AS agentModelId
```

需要更新为：
```sql
a.model_name AS agentModelName, a.provider_model_id AS agentProviderModelId
```

## 2. Migration Strategy

### 2.1 Database Migration (V38)

使用 MySQL `RENAME COLUMN` 语法（MySQL 8.0+）：
```sql
ALTER TABLE agent RENAME COLUMN model TO model_name;
ALTER TABLE agent RENAME COLUMN model_id TO provider_model_id;
```

**Alternative Considered**: `CHANGE COLUMN` 语法
**Rejected Because**: `RENAME COLUMN` 更简洁，MySQL 8.0+ 原生支持

### 2.2 Code Migration Order

自底向上修改，确保编译通过：
1. Database Migration (V38)
2. PO Layer (AgentPO, AgentBoundPO)
3. Domain Layer (Agent, AgentBound)
4. Repository Impl Layer
5. DTO Layer (AgentDTO, Request DTOs)
6. Application Service Layer
7. Transformer Layer

## 3. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| 遗漏文件 | Low | Medium | 使用 IDE 全局搜索验证 |
| SQL 别名不匹配 | Low | High | 仔细检查 XML Mapper |
| API 兼容性 | Low | Medium | JSON 字段名自动由 getter 名称决定 |

## 4. Testing Strategy

1. **编译验证**: `mvn clean compile`
2. **单元测试**: `mvn test`
3. **API 验证**: 调用 Agent 相关 API 验证返回字段名
4. **Executor 集成**: 验证 `llm_config.model_id` 使用正确的 `providerModelId`

## 5. Decisions Log

| Decision | Choice | Alternatives | Rationale |
|----------|--------|--------------|-----------|
| 字段命名 | `model_name`, `provider_model_id` | `model_display_name`, `llm_model_id` | 简洁且语义清晰 |
| 迁移方式 | `RENAME COLUMN` | `CHANGE COLUMN` | MySQL 8.0+ 原生支持，语法更清晰 |
| 修改顺序 | 自底向上 | 自顶向下 | 确保每一层编译通过 |
