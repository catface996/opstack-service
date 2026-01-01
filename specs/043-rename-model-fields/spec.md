# Feature Specification: Rename Agent Model Fields

**Feature Branch**: `043-rename-model-fields`
**Created**: 2025-12-31
**Status**: Draft
**Input**: User description: "agent目前记录了字段model，现在要改成 provider_model_id 和 model_name, 这个模型id不是表结构中的id，而是模型提供商提供的模型id，可以用一个更准确的名字。"

## Background

当前 Agent 实体使用 `model` 和 `model_id` 字段存储模型信息，但命名不够清晰：
- `model_id` 容易与数据库主键 ID 混淆
- 实际含义是模型提供商（如 Anthropic、Google）提供的模型标识符

需要重命名为更准确的字段名：
- `provider_model_id`: 模型提供商的模型标识符（如 `anthropic.claude-opus-4-5-20251124-v1:0`）
- `model_name`: 模型友好名称（如 `Claude Opus 4.5`）

## User Scenarios & Testing *(mandatory)*

### User Story 1 - System Administrator Views Agent Configuration (Priority: P1)

系统管理员查看 Agent 配置时，能够清晰区分模型的友好名称和提供商模型标识符。

**Why this priority**: 这是最基础的功能，确保字段命名清晰，避免混淆。

**Independent Test**: 可通过 API 查询 Agent 详情验证返回的字段名称是否正确。

**Acceptance Scenarios**:

1. **Given** Agent 已配置模型信息, **When** 管理员通过 API 查询 Agent 详情, **Then** 返回结果包含 `providerModelId` 和 `modelName` 字段
2. **Given** Agent 已配置模型信息, **When** 管理员查看层级结构 API 返回, **Then** 每个 Agent 都包含正确的 `providerModelId` 和 `modelName`

---

### User Story 2 - System Calls Executor Service with Correct Model ID (Priority: P1)

系统调用 Executor 服务创建层级团队时，使用 `providerModelId` 作为 LLM 配置的模型标识。

**Why this priority**: 这是核心集成功能，确保正确的模型 ID 传递给 Executor。

**Independent Test**: 触发执行时验证发送给 Executor 的请求中 `llm_config.model_id` 使用正确的提供商模型 ID。

**Acceptance Scenarios**:

1. **Given** Agent 配置了 `providerModelId`, **When** 系统调用 Executor 创建层级, **Then** 请求中的 `llm_config.model_id` 使用 Agent 的 `providerModelId` 值
2. **Given** Agent 只配置了 `modelName` 未配置 `providerModelId`, **When** 系统调用 Executor, **Then** 系统使用默认模型 ID 或给出明确提示

---

### User Story 3 - Administrator Updates Agent Model Configuration (Priority: P2)

管理员通过 API 更新 Agent 的模型配置，使用新的字段名称。

**Why this priority**: 支持配置更新是完整功能的必要部分。

**Independent Test**: 通过 API 更新 Agent 并验证字段正确保存。

**Acceptance Scenarios**:

1. **Given** 管理员准备更新 Agent 配置, **When** 通过 API 提交包含 `providerModelId` 和 `modelName` 的请求, **Then** 系统正确保存并返回更新后的配置

---

### Edge Cases

- 当 `providerModelId` 为空时，系统如何处理？（使用默认值或报错）
- 当 `modelName` 为空但 `providerModelId` 存在时，显示什么？
- 历史数据迁移：旧的 `model` 和 `model_id` 字段数据如何迁移到新字段？

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 将 `model` 字段重命名为 `model_name`
- **FR-002**: 系统 MUST 将 `model_id` 字段重命名为 `provider_model_id`
- **FR-003**: API 响应 MUST 使用新的字段名称 `providerModelId` 和 `modelName`
- **FR-004**: 调用 Executor 服务时 MUST 优先使用 `provider_model_id` 作为 `llm_config.model_id`
- **FR-005**: 系统 MUST 提供数据迁移脚本将现有数据从旧字段迁移到新字段
- **FR-006**: 当 `provider_model_id` 为空时，系统 SHOULD 使用默认模型 ID（`gemini-2.0-flash`）

### Key Entities

- **Agent**:
  - `model_name` (VARCHAR): 模型友好名称，用于显示（如 "Claude Opus 4.5"）
  - `provider_model_id` (VARCHAR): 模型提供商标识符，用于 API 调用（如 "anthropic.claude-opus-4-5-20251124-v1:0"）

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 所有 Agent 相关 API 返回的字段名使用 `providerModelId` 和 `modelName`
- **SC-002**: 调用 Executor 服务的请求中 100% 使用正确的 `provider_model_id` 值
- **SC-003**: 数据库迁移后，所有现有 Agent 数据保持完整，无数据丢失
- **SC-004**: 字段命名符合业务语义，开发者能够清晰理解各字段用途

## Assumptions

- 当前已存在 `model` 和 `model_id` 字段（V37 迁移脚本已添加）
- 需要向后兼容：旧数据迁移到新字段
- Executor 服务的 API 不变，仍然接受 `llm_config.model_id` 字段

## Out of Scope

- 模型列表管理功能（创建、编辑模型配置）
- 模型验证（验证提供的 model ID 是否有效）
- 前端 UI 变更
