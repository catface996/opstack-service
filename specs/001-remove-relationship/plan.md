# Implementation Plan: 移除 Relationship，用 Node2Node 替代

**Branch**: `001-remove-relationship` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-remove-relationship/spec.md`

## Summary

重构 Relationship 内部实现，使用 Node2Node 模型替代，同时**保持 API 接口不变**。这是一个**纯代码层面的重构**，主要工作包括：
1. 保留 RelationshipController、DTO 和 API 路径（`/api/service/v1/relationships/*`）
2. 删除 Relationship Domain 层代码（Domain Service、Repository）
3. 修改 RelationshipApplicationService 实现，改为调用 Node2Node 服务
4. 扩展 Node2Node Domain/Infrastructure 实现

**重要发现**：`RelationshipPO` 和 `Node2NodePO` 都映射到同一张 `node_2_node` 表，无需数据迁移

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (通过 Flyway 迁移)
**Testing**: JUnit 5, Spring Boot Test
**Target Platform**: Linux server / Docker container
**Project Type**: DDD 多模块架构（bootstrap, interface, application, domain, infrastructure）
**Performance Goals**: 与重构前相当（差异不超过 10%）
**Constraints**: 遵循 POST-Only API 设计、分页协议
**Scale/Scope**: 删除约 33 个文件，新增/修改约 15 个文件

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | PASS | 遵循现有分层架构，Node2Node 实现在各层均有对应 |
| II. API URL Convention | PASS | 保持现有 API 路径 `/api/service/v1/relationships/*` |
| III. POST-Only API Design | PASS | 所有新端点使用 POST 方法 |
| IV. Database Migration | PASS | 无需迁移脚本，RelationshipPO 和 Node2NodePO 共用 node_2_node 表 |
| V. Technology Stack | PASS | 使用现有技术栈 |
| VI. Pagination Protocol | PASS | 分页接口遵循 PageResult 规范 |

## Project Structure

### Documentation (this feature)

```text
specs/001-remove-relationship/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── relationship-api.yaml  # 现有 API 契约（保持不变）
└── tasks.md             # Phase 2 output (by /speckit.tasks)
```

### Source Code (repository root)

```text
# DDD 多模块架构（保持 API 兼容，重构内部实现）

interface/interface-http/           # 接口层（保留）
├── src/main/java/.../controller/
│   └── RelationshipController.java            # 保留（不变）
└── src/main/java/.../request/
    └── relationship/                          # 保留（不变）

application/                        # 应用层
├── application-api/
│   └── src/main/java/.../
│       ├── dto/relationship/                  # 保留（不变）
│       └── service/
│           └── relationship/
│               └── RelationshipApplicationService.java  # 保留接口
└── application-impl/
    └── src/main/java/.../
        └── relationship/
            └── RelationshipApplicationServiceImpl.java  # 修改：改用 Node2Node 服务

domain/                             # 领域层
├── domain-api/
│   └── src/main/java/.../service/
│       ├── relationship/
│       │   └── RelationshipDomainService.java     # 删除
│       └── node2node/
│           └── Node2NodeDomainService.java        # 新增
├── domain-impl/
│   └── src/main/java/.../service/
│       ├── relationship/
│       │   └── RelationshipDomainServiceImpl.java # 删除
│       └── node2node/
│           └── Node2NodeDomainServiceImpl.java    # 新增
├── domain-model/
│   └── src/main/java/.../model/
│       ├── relationship/
│       │   ├── Relationship.java              # 删除
│       │   ├── TraverseResult.java            # 保留
│       │   ├── RelationshipType.java          # 保留
│       │   ├── RelationshipDirection.java     # 保留
│       │   ├── RelationshipStrength.java      # 保留
│       │   └── RelationshipStatus.java        # 保留
│       └── node2node/
│           └── Node2Node.java                 # 新增领域模型
└── repository-api/
    └── src/main/java/.../repository/
        ├── relationship/
        │   └── RelationshipRepository.java    # 删除
        └── node/
            └── Node2NodeRepository.java       # 扩展

infrastructure/repository/mysql-impl/  # 基础设施层
├── src/main/java/.../
│   ├── impl/
│   │   ├── relationship/
│   │   │   └── RelationshipRepositoryImpl.java    # 删除
│   │   └── node/
│   │       └── Node2NodeRepositoryImpl.java       # 扩展
│   ├── mapper/
│   │   ├── relationship/
│   │   │   └── RelationshipMapper.java            # 删除
│   │   └── node/
│   │       └── Node2NodeMapper.java               # 扩展
│   └── po/
│       ├── relationship/
│       │   └── RelationshipPO.java                # 删除
│       └── node/
│           └── Node2NodePO.java                   # 已存在

common/                             # 公共模块
└── src/main/java/.../enums/
    └── RelationshipErrorCode.java             # 保留
```

**Structure Decision**: 保持 API 兼容性，仅重构 Domain 和 Infrastructure 层，Application 层改为调用 Node2Node 服务。

## Complexity Tracking

> 本次重构不引入新的复杂性，仅简化内部实现

| 变更类型 | 说明 |
|---------|------|
| 保留文件 | RelationshipController、DTO、Request（API 层保持不变） |
| 删除文件 | 约 8 个（Domain Service、Repository、PO、Mapper） |
| 新增文件 | 约 4 个（Node2Node Domain Service、领域模型） |
| 修改文件 | RelationshipApplicationServiceImpl（改用 Node2Node 服务） |
