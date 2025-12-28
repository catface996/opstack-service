# Implementation Plan: Topology 绑定报告模板

**Branch**: `034-topology-report-template` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/034-topology-report-template/spec.md`

## Summary

实现 Topology 与 ReportTemplate 的多对多绑定关系管理功能，提供绑定、解绑、查询已绑定和未绑定模板四个 API 端点。通过新建关联表 `topology_2_report_template` 存储绑定关系，遵循项目现有的 DDD 分层架构和 POST-Only API 规范。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, Spring Boot Test
**Target Platform**: Linux server (Docker container)
**Project Type**: DDD layered architecture (bootstrap/interface/application/domain/infrastructure)
**Performance Goals**: < 500ms 响应时间 (1000 条模板以内)
**Constraints**: 单次批量操作最多 100 个模板
**Scale/Scope**: 与现有 topology_2_node 关联表模式一致

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | ✅ PASS | 遵循 bootstrap/interface/application/domain/infrastructure 分层 |
| II. API URL Convention | ✅ PASS | 使用 `/api/service/v1/topologies/report-templates/{action}` 格式 |
| III. POST-Only API Design | ✅ PASS | 所有接口使用 POST 方法，JSON Body 传参 |
| IV. Database Migration | ✅ PASS | 使用 Flyway V25 迁移脚本创建关联表 |
| V. Technology Stack | ✅ PASS | Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x, MySQL 8.0 |
| VI. Pagination Protocol | ✅ PASS | 查询接口支持分页，遵循 PageableRequest/PageResult 规范 |
| VII. Database Design Standards | ✅ PASS | 关联表遵循 `{tableA}_2_{tableB}` 命名，包含审计字段和软删除 |

## Project Structure

### Documentation (this feature)

```text
specs/034-topology-report-template/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── checklists/          # Quality checklists
└── tasks.md             # Phase 2 output (by /speckit.tasks)
```

### Source Code (repository root)

```text
# DDD Layered Architecture

bootstrap/
└── src/main/resources/db/migration/
    └── V25__topology_report_template_binding.sql   # 新建关联表迁移脚本

interface/interface-http/
└── src/main/java/com/catface996/aiops/interface_/http/
    ├── controller/TopologyReportTemplateController.java  # 新建控制器
    └── request/topology/
        ├── BindReportTemplatesRequest.java
        ├── UnbindReportTemplatesRequest.java
        ├── QueryBoundTemplatesRequest.java
        └── QueryUnboundTemplatesRequest.java

application/application-api/
└── src/main/java/com/catface996/aiops/application/api/
    └── service/topology/TopologyReportTemplateApplicationService.java

application/application-impl/
└── src/main/java/com/catface996/aiops/application/impl/
    └── service/topology/TopologyReportTemplateApplicationServiceImpl.java

domain/domain-api/
└── src/main/java/com/catface996/aiops/domain/
    └── service/topology/TopologyReportTemplateDomainService.java

domain/domain-impl/
└── src/main/java/com/catface996/aiops/domain/impl/
    └── service/topology/TopologyReportTemplateDomainServiceImpl.java

infrastructure/repository/mysql-impl/
└── src/main/java/com/catface996/aiops/repository/mysql/
    ├── mapper/topology/TopologyReportTemplateMapper.java
    ├── po/topology/TopologyReportTemplatePO.java
    └── impl/topology/TopologyReportTemplateRepositoryImpl.java

domain/repository-api/
└── src/main/java/com/catface996/aiops/repository/topology/
    └── TopologyReportTemplateRepository.java
```

**Structure Decision**: 遵循现有 DDD 分层架构，参考 `topology_2_node` 的实现模式，在各层创建对应的类文件。

## Complexity Tracking

> 无违规项需要说明，本功能完全遵循项目宪法规范。
