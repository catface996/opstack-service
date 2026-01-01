# Implementation Plan: Rename Agent Model Fields

**Branch**: `043-rename-model-fields` | **Date**: 2025-12-31 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/043-rename-model-fields/spec.md`

## Summary

重命名 Agent 实体的模型字段以提高语义清晰度：
- `model` → `model_name`（模型友好名称）
- `model_id` → `provider_model_id`（模型提供商标识符）

技术方案：创建 Flyway 迁移脚本重命名数据库字段，更新所有层级代码（PO、Domain、DTO、Mapper、Service）。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (Flyway migrations)
**Testing**: Maven Surefire (JUnit 5)
**Target Platform**: Linux server
**Project Type**: DDD multi-module
**Performance Goals**: N/A (字段重命名，无性能影响)
**Constraints**: 向后兼容 - 数据迁移无数据丢失
**Scale/Scope**: 影响 ~15 个 Java 文件 + 1 个 SQL 迁移脚本

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| DDD Architecture | ✅ PASS | 遵循分层架构，修改涉及所有层级 |
| API URL Convention | ✅ PASS | API 端点不变，仅字段名变更 |
| POST-Only API Design | ✅ PASS | 不涉及新端点 |
| Database Migration | ✅ PASS | 使用 Flyway V38 迁移脚本 |
| Technology Stack | ✅ PASS | 使用项目标准技术栈 |
| Database Design Standards | ✅ PASS | 字段命名符合蛇形规范 |
| SQL Query Standards | ✅ PASS | XML Mapper 使用明确字段名 |
| Process Management Standards | ✅ PASS | 不涉及进程管理 |

## Project Structure

### Documentation (this feature)

```text
specs/043-rename-model-fields/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── checklists/          # Quality checklists
│   └── requirements.md  # Spec validation checklist
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
# 受影响的文件结构

# Layer 1: Database Migration
bootstrap/src/main/resources/db/migration/
└── V38__rename_model_fields_in_agent.sql    # NEW

# Layer 2: Repository (PO + Mapper)
infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/
├── po/agent/AgentPO.java                    # MODIFY: model→modelName, modelId→providerModelId
├── po/agentbound/AgentBoundPO.java          # MODIFY: agentModel→agentModelName, agentModelId→agentProviderModelId
└── mapper/agentbound/AgentBoundMapper.xml   # MODIFY: SQL column aliases

infrastructure/repository/mysql-impl/src/main/resources/mapper/
└── agentbound/AgentBoundMapper.xml          # MODIFY: SQL field names

# Layer 3: Domain Model
domain/domain-model/src/main/java/.../domain/model/
├── agent/Agent.java                         # MODIFY: model→modelName, modelId→providerModelId
└── agentbound/AgentBound.java               # MODIFY: agentModel→agentModelName, agentModelId→agentProviderModelId

# Layer 4: Repository Implementation
infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/impl/
├── agent/AgentRepositoryImpl.java           # MODIFY: field mapping
└── agentbound/AgentBoundRepositoryImpl.java # MODIFY: field mapping

# Layer 5: Application DTO
application/application-api/src/main/java/.../application/api/dto/
├── agent/AgentDTO.java                      # MODIFY: model→modelName, modelId→providerModelId
└── agent/request/CreateAgentRequest.java    # MODIFY: model→modelName, modelId→providerModelId
└── agent/request/UpdateAgentRequest.java    # MODIFY: model→modelName, modelId→providerModelId

# Layer 6: Application Service
application/application-impl/src/main/java/.../application/impl/service/
├── agent/AgentApplicationServiceImpl.java   # MODIFY: field mapping
├── agentbound/AgentBoundApplicationServiceImpl.java  # MODIFY: field mapping
└── execution/transformer/HierarchyTransformer.java   # MODIFY: getModelId→getProviderModelId
```

**Structure Decision**: 按 DDD 分层架构，自底向上修改：Database → PO → Domain → Repository Impl → DTO → Application Service

## Complexity Tracking

> No violations - straightforward field renaming across layers.

| Aspect | Complexity | Notes |
|--------|------------|-------|
| Database Migration | Low | 简单的 RENAME COLUMN |
| Code Changes | Medium | 涉及多个文件但都是机械性重命名 |
| Testing | Low | 编译通过 + 现有测试 + API 验证 |
| Risk | Low | 字段重命名，无逻辑变更 |
