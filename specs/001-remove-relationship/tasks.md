# Tasks: 移除 Relationship，用 Node2Node 替代

**Feature**: 001-remove-relationship
**Branch**: `001-remove-relationship`
**Generated**: 2025-12-28
**Plan**: [plan.md](./plan.md) | **Spec**: [spec.md](./spec.md)

## Summary

重构 Relationship 内部实现，使用 Node2Node 模型替代，**保持 API 接口不变**。

**重要**：
- API 路径 `/api/service/v1/relationships/*` 保持不变
- 请求/响应格式保持不变
- 仅重构 Domain 和 Infrastructure 层内部实现

---

## Phase 1: Setup

> 项目准备和验证

- [X] T001 创建并切换到 feature 分支 `001-remove-relationship`
- [X] T002 执行 `mvn clean compile` 确认当前代码编译通过
- [X] T003 执行 `mvn test` 确认当前测试全部通过

---

## Phase 2: Foundational - Node2Node 基础设施

> 创建 Node2Node Domain 层基础设施，为 US1-US3 提供支撑

### 2.1 Domain Model

- [X] T004 [P] 创建 Node2Node 领域模型 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/node2node/Node2Node.java`

### 2.2 Domain Service Interface

- [X] T005 [P] 创建 Node2NodeDomainService 接口 `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/node2node/Node2NodeDomainService.java`

### 2.3 Repository Interface Extension

- [X] T006 扩展 Node2NodeRepository 接口，添加完整 CRUD 方法 `domain/repository-api/src/main/java/com/catface996/aiops/repository/node/Node2NodeRepository.java`

---

## Phase 3: User Story 1 - CRUD 功能实现 (P1)

> **Goal**: 系统管理员使用 Node2Node 管理节点关系
> **Test**: 创建、查询、更新、删除关系操作全部正常工作

### 3.1 Infrastructure Layer

- [X] T007 [US1] 扩展 Node2NodeMapper，添加 CRUD SQL 方法 `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/node/Node2NodeMapper.java`
- [X] T008 [US1] 扩展 Node2NodeMapper XML，添加 CRUD SQL（使用注解方式实现，无需 XML）
- [X] T009 [US1] 扩展 Node2NodeRepositoryImpl，实现完整 CRUD `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/node/Node2NodeRepositoryImpl.java`

### 3.2 Domain Service Implementation

- [X] T010 [US1] 实现 Node2NodeDomainServiceImpl CRUD 方法 `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/node2node/Node2NodeDomainServiceImpl.java`

### 3.3 Application Layer Refactoring

- [X] T011 [US1] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 create 方法 `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/relationship/RelationshipApplicationServiceImpl.java`
- [X] T012 [US1] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 query 分页查询方法
- [X] T013 [US1] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 get 单个查询方法
- [X] T014 [US1] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 update 方法
- [X] T015 [US1] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 delete 方法
- [X] T016 [US1] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 queryByResourceId 方法

### 3.4 Verification

- [X] T017 [US1] 编译验证：执行 `mvn clean compile` 确认无编译错误
- [X] T018 [US1] 启动服务，测试 CRUD API 端点（参考 quickstart.md）

---

## Phase 4: User Story 2 - 图遍历功能 (P2)

> **Goal**: 系统执行关系图遍历
> **Test**: 从指定节点向上/向下遍历，返回正确的关联节点链
> **Dependency**: Phase 3 完成

### 4.1 Domain Service Extension

- [X] T019 [US2] 在 Node2NodeDomainService 接口添加 traverse 遍历方法
- [X] T020 [US2] 在 Node2NodeDomainServiceImpl 实现 traverse 遍历方法（DFS/BFS 算法）

### 4.2 Application Layer

- [X] T021 [US2] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 traverse 方法

### 4.3 Verification

- [X] T022 [US2] 编译验证：执行 `mvn clean compile`
- [X] T023 [US2] 启动服务，测试图遍历 API 端点

---

## Phase 5: User Story 3 - 循环检测功能 (P3)

> **Goal**: 系统检测循环依赖
> **Test**: 检测节点关系图中的循环依赖
> **Dependency**: Phase 3 完成

### 5.1 Domain Service Extension

- [X] T024 [US3] 在 Node2NodeDomainService 接口添加 detectCycle 方法
- [X] T025 [US3] 在 Node2NodeDomainServiceImpl 实现 detectCycle 方法（DFS 检测算法）

### 5.2 Application Layer

- [X] T026 [US3] 修改 RelationshipApplicationServiceImpl，改用 Node2NodeDomainService 实现 cycleDetection 方法

### 5.3 Verification

- [X] T027 [US3] 编译验证：执行 `mvn clean compile`
- [X] T028 [US3] 启动服务，测试循环检测 API 端点

---

## Phase 6: Cleanup - 删除 Relationship 代码

> 删除已废弃的 Relationship 层代码
> **Dependency**: Phase 3-5 全部完成并验证通过

### 6.1 Delete Domain Layer

- [X] T029 [P] 删除 RelationshipDomainService 接口
- [X] T030 [P] 删除 RelationshipDomainServiceImpl 实现
- [X] T031 [P] ~~删除 Relationship 领域模型~~ 保留（TraverseResult 依赖）
- [X] T032 [P] 删除 RelationshipRepository 接口

### 6.2 Delete Infrastructure Layer

- [X] T033 [P] 删除 RelationshipRepositoryImpl
- [X] T034 [P] 删除 RelationshipMapper 接口
- [X] T035 [P] 删除 RelationshipPO
- [X] T036 [P] 删除 RelationshipMapper XML

### 6.3 Delete Tests

- [X] T037 [P] 删除 RelationshipDomainServiceImplTest

### 6.4 Cleanup Empty Directories

- [X] T038 删除空的 relationship 目录结构

---

## Phase 7: Polish & Verification

> 最终验证和代码质量检查

- [X] T039 执行 `mvn clean compile` 确认全部编译通过
- [X] T040 执行 `mvn test` 确认所有测试通过
- [X] T041 启动服务，执行完整 API 测试（参考 quickstart.md 所有端点）
- [X] T042 检查是否有残留的 Relationship 引用：`grep -r "RelationshipDomainService\|RelationshipRepository\|RelationshipPO" --include="*.java"`
- [ ] T043 提交代码并推送到远端

---

## Dependencies

```
Phase 1 (Setup)
    │
    ▼
Phase 2 (Foundational: Node2Node Domain Model & Service Interface)
    │
    ├──────────────────┬──────────────────┐
    ▼                  ▼                  ▼
Phase 3 (US1)     Phase 4 (US2)     Phase 5 (US3)
   CRUD              Traverse         Cycle Detection
    │                  │                  │
    └──────────────────┴──────────────────┘
                       │
                       ▼
               Phase 6 (Cleanup)
                       │
                       ▼
               Phase 7 (Polish)
```

**Note**: US2 和 US3 依赖 US1 的基础 CRUD 实现，但两者之间相互独立。

---

## Parallel Execution Opportunities

### Phase 2 (Foundational)
```
T004 (Node2Node Model) ──┐
T005 (DomainService)  ───┼── 可并行
T006 (Repository)     ───┘   （不同文件，无依赖）
```

### Phase 6 (Cleanup)
```
T029-T037 ── 全部可并行（删除独立文件，无依赖关系）
```

---

## Implementation Strategy

### MVP Scope (Minimum Viable Product)

**Phase 1-3 (US1)** 构成 MVP：
- 基础设施搭建
- 完整的 CRUD 功能
- 验证 API 兼容性

### Incremental Delivery

1. **Increment 1**: Phase 1-3 → MVP，验证 CRUD 功能
2. **Increment 2**: Phase 4 → 添加图遍历功能
3. **Increment 3**: Phase 5 → 添加循环检测
4. **Increment 4**: Phase 6-7 → 清理和最终验证

---

## Summary

| Phase | Tasks | Parallel | Description |
|-------|-------|----------|-------------|
| 1 | 3 | 0 | Setup |
| 2 | 3 | 3 | Foundational |
| 3 | 12 | 0 | US1 - CRUD |
| 4 | 5 | 0 | US2 - Traverse |
| 5 | 5 | 0 | US3 - Cycle Detection |
| 6 | 10 | 9 | Cleanup |
| 7 | 5 | 0 | Polish |
| **Total** | **43** | **12** | |

**Format Validation**: ✓ All tasks follow checklist format with checkbox, ID, optional [P]/[Story] labels, and file paths.
