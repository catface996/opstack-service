# Tasks: Topology 绑定报告模板

**Input**: Design documents from `/specs/034-topology-report-template/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md

**Tests**: 未在功能规格说明中要求测试，本任务列表不包含测试任务。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

本项目采用 DDD 分层架构：
- **bootstrap**: `bootstrap/src/main/resources/db/migration/`
- **interface**: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/`
- **application-api**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **application-impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/`
- **domain-api**: `domain/domain-api/src/main/java/com/catface996/aiops/domain/`
- **domain-impl**: `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/`
- **repository-api**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/`
- **mysql-impl**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/`

---

## Phase 1: Setup (Database Migration)

**Purpose**: 创建数据库关联表，为所有 User Story 提供数据存储基础

- [X] T001 创建 Flyway 迁移脚本 V25__topology_report_template_binding.sql in bootstrap/src/main/resources/db/migration/V25__topology_report_template_binding.sql

**Checkpoint**: 执行 `mvn flyway:migrate` 或启动应用后，数据库中存在 `topology_2_report_template` 表

---

## Phase 2: Foundational (Infrastructure Layer)

**Purpose**: 创建基础设施层组件，为所有 User Story 提供数据访问能力

### PO 和 Mapper

- [X] T002 [P] 创建 TopologyReportTemplatePO.java in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/topology/TopologyReportTemplatePO.java

- [X] T003 [P] 创建 TopologyReportTemplateMapper.java in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/topology/TopologyReportTemplateMapper.java

### Repository 接口和实现

- [X] T004 创建 TopologyReportTemplateRepository.java (接口) in domain/repository-api/src/main/java/com/catface996/aiops/repository/topology2/TopologyReportTemplateRepository.java

- [X] T005 创建 TopologyReportTemplateRepositoryImpl.java in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/topology/TopologyReportTemplateRepositoryImpl.java

**Checkpoint**: 编译通过，无错误 `mvn clean compile -DskipTests`

---

## Phase 3: User Story 1 & 2 - 绑定和解绑报告模板 (Priority: P1) 🎯 MVP

**Goal**: 实现绑定和解绑功能，这是核心的写操作

**Independent Test**:
- 调用绑定接口成功后，数据库中存在绑定记录
- 调用解绑接口成功后，数据库中记录被软删除 (deleted=1)

### Domain Layer

- [X] T006 创建 TopologyReportTemplateDomainService.java (接口) in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/topology2/TopologyReportTemplateDomainService.java

- [X] T007 创建 TopologyReportTemplateDomainServiceImpl.java in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/topology2/TopologyReportTemplateDomainServiceImpl.java

### Application Layer

- [X] T008 创建 TopologyReportTemplateApplicationService.java (接口) in application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyReportTemplateApplicationService.java

- [X] T009 创建 TopologyReportTemplateApplicationServiceImpl.java in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyReportTemplateApplicationServiceImpl.java

### Interface Layer - Request DTOs

- [X] T010 [P] [US1] 创建 BindReportTemplatesRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/topology/BindReportTemplatesRequest.java

- [X] T011 [P] [US2] 创建 UnbindReportTemplatesRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/topology/UnbindReportTemplatesRequest.java

### Interface Layer - Controller

- [X] T012 [US1] [US2] 扩展 TopologyController.java 添加 bind 和 unbind 端点 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java

**Checkpoint**:
- 启动应用，通过 Swagger UI 调用绑定接口成功
- 调用解绑接口成功
- 数据库中绑定记录正确

---

## Phase 4: User Story 3 - 查询已绑定的报告模板 (Priority: P1)

**Goal**: 实现查询已绑定模板列表功能

**Independent Test**: 绑定模板后，调用查询接口能返回已绑定的模板列表

### Interface Layer

- [X] T013 [P] [US3] 创建 QueryBoundTemplatesRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/topology/QueryBoundTemplatesRequest.java

- [X] T014 [US3] 扩展 TopologyController.java 添加 bound 查询端点 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java

- [X] T015 [US3] 在 TopologyReportTemplateMapper.java 中添加 selectBoundTemplates 查询方法

- [X] T016 [US3] 在 TopologyReportTemplateRepository 和 Impl 中添加 findBoundTemplates 方法

- [X] T017 [US3] 在 ApplicationService 中添加 queryBoundTemplates 方法

**Checkpoint**: 通过 Swagger UI 调用查询已绑定接口，返回正确的模板列表

---

## Phase 5: User Story 4 - 查询未绑定的报告模板 (Priority: P2)

**Goal**: 实现查询未绑定模板列表功能

**Independent Test**: 系统中有模板但未绑定时，调用查询接口能返回未绑定的模板列表

### Interface Layer

- [X] T018 [P] [US4] 创建 QueryUnboundTemplatesRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/topology/QueryUnboundTemplatesRequest.java

- [X] T019 [US4] 扩展 TopologyController.java 添加 unbound 查询端点 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java

- [X] T020 [US4] 在 TopologyReportTemplateMapper.java 中添加 selectUnboundTemplates 查询方法

- [X] T021 [US4] 在 TopologyReportTemplateRepository 和 Impl 中添加 findUnboundTemplates 方法

- [X] T022 [US4] 在 ApplicationService 中添加 queryUnboundTemplates 方法

**Checkpoint**: 通过 Swagger UI 调用查询未绑定接口，返回正确的模板列表

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 验证和收尾工作

- [X] T023 编译项目并验证无错误: mvn clean compile -DskipTests

- [X] T024 启动应用并验证 API 功能正常: java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

- [X] T025 按照 quickstart.md 执行完整验证流程

- [X] T026 更新 Swagger 文档标签，确保新接口在"拓扑图管理"下正确显示

**Checkpoint**: 所有验证完成，功能可交付

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 无依赖 - 可立即开始
- **Phase 2 (Foundational)**: 依赖 Phase 1 - 必须先有数据库表
- **Phase 3 (US1 & US2)**: 依赖 Phase 2 - 必须先有基础设施层
- **Phase 4 (US3)**: 依赖 Phase 3 - 需要绑定功能来创建测试数据
- **Phase 5 (US4)**: 依赖 Phase 2 - 仅需基础设施层，可与 Phase 3/4 并行
- **Phase 6 (Polish)**: 依赖 Phase 3-5 - 所有功能完成后进行收尾

### User Story Dependencies

- **User Story 1 (绑定)**: 无依赖 - 核心功能
- **User Story 2 (解绑)**: 无依赖 - 核心功能
- **User Story 3 (查询已绑定)**: 技术上无依赖，但验证需要 US1
- **User Story 4 (查询未绑定)**: 无依赖 - 独立功能

### Parallel Opportunities

Phase 2 中以下任务可并行执行：
```bash
# 可并行执行的 PO/Mapper 创建任务
T002: TopologyReportTemplatePO.java
T003: TopologyReportTemplateMapper.java
```

Phase 3-5 中以下 Request DTO 可并行创建：
```bash
# 可并行执行的 Request 创建任务
T010: BindReportTemplatesRequest.java
T011: UnbindReportTemplatesRequest.java
T013: QueryBoundTemplatesRequest.java
T018: QueryUnboundTemplatesRequest.java
```

---

## Implementation Strategy

### MVP First (User Story 1 & 2 Only)

1. 完成 Phase 1: Setup (数据库迁移)
2. 完成 Phase 2: Foundational (基础设施层)
3. 完成 Phase 3: US1 & US2 (绑定和解绑)
4. **验证**: 通过 Swagger UI 测试绑定和解绑功能
5. 此时核心功能可用，可以进行初步演示

### Incremental Delivery

1. 完成 Setup + Foundational + US1 & US2 → 核心绑定/解绑可用
2. 完成 US3 → 可查询已绑定模板
3. 完成 US4 → 可查询未绑定模板（用户体验提升）
4. 完成 Polish → 功能完整验证

### 推荐执行顺序

由于本功能的 User Story 相对独立，建议按顺序执行：

```
T001 → (T002, T003 并行) → T004 → T005 → T006 → T007 → T008 → T009 →
(T010, T011, T013, T018 并行) → T012 → T014 → T015 → T016 → T017 →
T019 → T020 → T021 → T022 → T023 → T024 → T025 → T026
```

---

## Notes

- [P] tasks = 不同文件，无依赖，可并行执行
- [Story] label = 任务归属的 User Story
- 建议在开发环境先执行完整流程，验证无误后再合并
- 所有 API 接口遵循 POST-Only 规范
- 关联表遵循宪法 VII 数据库设计标准

---

## Summary

| 统计项 | 数量 |
|--------|------|
| 总任务数 | 26 |
| Phase 1 (Setup) | 1 |
| Phase 2 (Foundational) | 4 |
| Phase 3 (US1 & US2) | 5 |
| Phase 4 (US3) | 5 |
| Phase 5 (US4) | 5 |
| Phase 6 (Polish) | 4 |
| 可并行任务 | 6 |

**MVP 范围**: Phase 1-3（数据库迁移 + 基础设施层 + 绑定/解绑功能）
