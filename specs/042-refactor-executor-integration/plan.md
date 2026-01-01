# Implementation Plan: Refactor Executor Integration

**Branch**: `042-refactor-executor-integration` | **Date**: 2025-12-30 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/042-refactor-executor-integration/spec.md`

## Summary

重构 op-stack-service 与 op-stack-executor 的集成方式：

1. 将 `agent_bound.id` 作为 Executor API 中的 `agent_id`（替代 `agent.id`）
2. 从 Agent 关联的 `prompt_template.content` 获取 `system_prompt`（替代基于 specialty 生成）
3. 快速失败策略：Executor 服务错误时立即返回，不重试

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI, WebClient (Reactive)
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, MockWebServer (for WebClient testing)
**Target Platform**: Linux server / Docker container
**Project Type**: DDD multi-module backend service
**Performance Goals**: 层级结构转换和 API 调用在 3 秒内完成
**Constraints**: 快速失败策略，不进行重试
**Scale/Scope**: 支持多层级 Agent 协作（Global Supervisor → Team Supervisor → Workers）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | ✅ PASS | 变更涉及 application-impl 层（HierarchyTransformer）、domain-model（实体扩展） |
| II. API URL Convention | ✅ PASS | 无新增 API，仅修改现有实现 |
| III. POST-Only API Design | ✅ PASS | 现有 Executor 调用已使用 POST |
| IV. Database Migration | ✅ PASS | 无数据库结构变更 |
| V. Technology Stack | ✅ PASS | Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x |
| VI. Pagination Protocol | ✅ N/A | 无分页接口变更 |
| VII. Database Design Standards | ✅ N/A | 无数据库表变更 |
| VIII. SQL Query Standards | ✅ PASS | 需新增 JOIN 查询获取 PromptTemplate content |
| IX. Process Management Standards | ✅ PASS | 遵循端口号终止进程方式 |

## Project Structure

### Documentation (this feature)

```text
specs/042-refactor-executor-integration/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── executor-api.md  # Executor API 契约
└── quickstart.md        # Phase 1 output
```

### Source Code (repository root)

```text
# DDD Multi-Module Structure
application/
├── application-api/
│   └── src/main/java/.../dto/
│       ├── agent/AgentDTO.java                    # 扩展：添加 promptTemplateContent, boundId
│       └── agentbound/HierarchyStructureDTO.java  # 扩展：携带绑定关系信息
└── application-impl/
    └── src/main/java/.../service/
        ├── agentbound/AgentBoundApplicationServiceImpl.java  # 修改：填充 PromptTemplate content
        └── execution/
            ├── transformer/HierarchyTransformer.java         # 重点修改：使用 boundId, promptTemplate
            └── client/dto/CreateHierarchyRequest.java        # 修改：添加 agent_id 字段

domain/
├── domain-model/
│   └── src/main/java/.../model/
│       ├── agent/Agent.java                       # 已有 promptTemplateId
│       ├── agentbound/AgentBound.java             # id 作为 agent_id
│       └── prompt/PromptTemplate.java             # content 作为 system_prompt
└── repository-api/
    └── src/main/java/.../repository/
        └── prompt/PromptTemplateRepository.java   # 可能需要批量查询方法

infrastructure/
└── repository/mysql-impl/
    └── src/main/java/.../mysql/
        ├── impl/prompt/PromptTemplateRepositoryImpl.java
        └── mapper/prompt/PromptTemplateMapper.java  # 可能需要新增查询

tests/
├── unit/
│   └── transformer/HierarchyTransformerTest.java  # 单元测试
└── integration/
    └── ExecutorIntegrationTest.java               # 集成测试
```

**Structure Decision**: 使用现有 DDD 多模块架构，主要变更集中在 application-impl 层的 HierarchyTransformer 和相关 DTO。

## Complexity Tracking

> **No constitution violations requiring justification**

本功能变更范围明确，无新增复杂性：
- 无新增模块或项目
- 无新增设计模式
- 仅修改现有转换逻辑
