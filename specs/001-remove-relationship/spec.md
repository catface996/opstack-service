# Feature Specification: 移除 Relationship，用 Node2Node 替代

**Feature Branch**: `001-remove-relationship`
**Created**: 2025-12-28
**Status**: Draft
**Input**: User description: "既然已经移除了Resource，也需要移除RelationShip，用Node2Node完全代替，从application层，到domain，到 infrastructure的Repository，全部替换"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 系统管理员使用 Node2Node 管理节点关系 (Priority: P1)

作为系统管理员，我需要使用统一的 Node2Node 模型来管理节点之间的依赖关系，而不是使用已过时的 Relationship 模型，这样可以保持代码库的一致性和简洁性。

**Why this priority**: 这是核心重构任务，移除 Relationship 后所有关系管理功能必须通过 Node2Node 实现，直接影响系统的可用性。

**Independent Test**: 可以通过创建、查询、更新、删除节点关系来完整测试，验证所有 Relationship API 功能已迁移到 Node2Node。

**Acceptance Scenarios**:

1. **Given** 系统中存在两个节点 A 和 B，**When** 用户通过新的 Node2Node API 创建关系，**Then** 关系成功创建并可查询
2. **Given** 系统中存在节点关系，**When** 用户查询某节点的所有关系，**Then** 返回该节点作为源或目标的所有关系
3. **Given** 系统中存在节点关系，**When** 用户更新关系属性（类型、强度、状态），**Then** 关系属性更新成功
4. **Given** 系统中存在节点关系，**When** 用户删除关系，**Then** 关系被软删除，不再出现在查询结果中

---

### User Story 2 - 系统执行关系图遍历 (Priority: P2)

作为系统，我需要能够从一个节点出发遍历所有相关节点，以支持依赖分析和影响范围评估。

**Why this priority**: 图遍历是关系管理的高级功能，依赖于基础的 CRUD 功能实现。

**Independent Test**: 可以通过创建多层节点关系图，然后执行遍历操作来测试。

**Acceptance Scenarios**:

1. **Given** 存在节点关系链 A->B->C，**When** 从节点 A 执行向下遍历，**Then** 返回 B 和 C
2. **Given** 存在节点关系链 A->B->C，**When** 从节点 C 执行向上遍历，**Then** 返回 B 和 A

---

### User Story 3 - 系统检测循环依赖 (Priority: P3)

作为系统，我需要能够检测节点关系图中的循环依赖，以防止创建无效的依赖关系。

**Why this priority**: 循环检测是数据完整性保护功能，优先级低于基础 CRUD 和遍历功能。

**Independent Test**: 可以通过尝试创建形成循环的关系来测试检测功能。

**Acceptance Scenarios**:

1. **Given** 存在关系 A->B->C，**When** 尝试创建 C->A 的关系，**Then** 系统检测到循环并返回警告

---

### Edge Cases

- 删除节点时如何处理关联的 Node2Node 关系？（假设：级联软删除或阻止删除）
- 创建关系时源节点和目标节点相同如何处理？（假设：拒绝创建自引用关系）
- 批量导入关系时如何处理重复关系？（假设：跳过重复，返回警告）

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须移除所有 Relationship 相关代码，包括：
  - Domain 模型：`Relationship.java` 及相关枚举
  - Domain 服务：`RelationshipDomainService` 及实现
  - Application 服务：`RelationshipApplicationService` 及实现
  - Repository：`RelationshipRepository` 及实现
  - Controller：`RelationshipController` 及所有 Request/Response DTO
  - 持久化对象：`RelationshipPO` 及 Mapper

- **FR-002**: 系统必须保留并完善 Node2Node 模型，支持以下属性：
  - 源节点 ID 和目标节点 ID
  - 关系类型（依赖、包含、调用等）
  - 关系方向（单向/双向）
  - 关系强度（强/弱/可选）
  - 关系状态（正常/警告/异常）
  - 关系描述

- **FR-003**: 系统必须提供 Node2Node 的完整 CRUD API：
  - 创建节点关系
  - 查询节点关系（支持分页、过滤）
  - 查询指定节点的所有关系
  - 更新关系属性
  - 删除关系（软删除）

- **FR-004**: 系统必须保留图遍历功能，支持从指定节点向上或向下遍历

- **FR-005**: 系统必须保留循环依赖检测功能

### Key Entities

- **Node2Node**: 表示两个节点之间的关系，包含源节点、目标节点、关系类型、方向、强度、状态和描述
- **Node**: 已存在的节点实体，Node2Node 关系的端点

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 所有 Relationship 相关文件（约 15-20 个）完全删除，编译无错误
- **SC-002**: Node2Node API 提供与原 Relationship API 等价的功能，100% 功能覆盖
- **SC-003**: 所有关系管理操作响应时间与重构前相当（差异不超过 10%）
- **SC-004**: 现有单元测试和集成测试通过率 100%（更新测试以使用 Node2Node）

## Assumptions

- 现有 node_2_node 表结构已满足关系存储需求，无需修改表结构
- Relationship 相关的枚举类型（RelationshipType、RelationshipDirection、RelationshipStrength、RelationshipStatus）保留复用
- **无需数据迁移**：经代码分析，`RelationshipPO` 已映射到 `node_2_node` 表（`@TableName("node_2_node")`），两者共用同一张表
- **API 路径和参数格式保持不变**：保持 `/api/service/v1/relationships/*` 路径和现有参数格式，仅重构内部实现

## Out of Scope

- 新增关系类型或属性
- 关系权限管理的增强
- 关系数据的历史版本追踪
- 关系变更的审计日志
