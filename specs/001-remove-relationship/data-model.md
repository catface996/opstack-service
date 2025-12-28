# Data Model: Node2Node 节点关系

**Date**: 2025-12-28
**Feature**: 001-remove-relationship

## 1. Entity Definitions

### 1.1 Node2Node（节点关系）

表示两个节点之间的有向或双向关系。

| 字段 | 类型 | 约束 | 说明 |
|-----|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 关系ID |
| sourceId | Long | NOT NULL, FK(node.id) | 源节点ID |
| targetId | Long | NOT NULL, FK(node.id) | 目标节点ID |
| relationshipType | String(50) | NOT NULL | 关系类型枚举 |
| direction | String(20) | NOT NULL | 关系方向枚举 |
| strength | String(20) | | 关系强度枚举 |
| status | String(20) | DEFAULT 'NORMAL' | 关系状态枚举 |
| description | String(500) | | 关系描述 |
| createdAt | LocalDateTime | NOT NULL | 创建时间 |
| updatedAt | LocalDateTime | NOT NULL | 更新时间 |

**业务规则**:
- sourceId 和 targetId 不能相同（禁止自引用）
- 同一对节点间同类型关系唯一（防重复）
- 删除为物理删除（有外键级联删除约束）

### 1.2 枚举类型（保留现有）

**RelationshipType（关系类型）**:
| 值 | 说明 |
|---|------|
| DEPENDENCY | 依赖关系 |
| CONTAINS | 包含关系 |
| CALLS | 调用关系 |
| CONNECTS_TO | 连接关系 |
| RUNS_ON | 运行于 |
| MANAGED_BY | 被管理 |

**RelationshipDirection（关系方向）**:
| 值 | 说明 |
|---|------|
| UNIDIRECTIONAL | 单向 |
| BIDIRECTIONAL | 双向 |

**RelationshipStrength（关系强度）**:
| 值 | 说明 |
|---|------|
| STRONG | 强依赖 |
| WEAK | 弱依赖 |
| OPTIONAL | 可选依赖 |

**RelationshipStatus（关系状态）**:
| 值 | 说明 |
|---|------|
| NORMAL | 正常 |
| WARNING | 警告 |
| CRITICAL | 严重 |

## 2. Database Schema

### 2.1 现有表 node_2_node

```sql
-- 表已存在（V12 迁移脚本创建）
-- RelationshipPO 和 Node2NodePO 都映射到此表
-- 无需数据迁移，无需修改表结构

CREATE TABLE node_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    source_id BIGINT NOT NULL COMMENT '源节点ID',
    target_id BIGINT NOT NULL COMMENT '目标节点ID',
    relationship_type VARCHAR(50) NOT NULL COMMENT '关系类型',
    direction VARCHAR(20) NOT NULL COMMENT '关系方向',
    strength VARCHAR(20) NOT NULL COMMENT '关系强度',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '关系状态',
    description VARCHAR(500) COMMENT '关系描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_source_target_type (source_id, target_id, relationship_type),
    INDEX idx_source (source_id),
    INDEX idx_target (target_id),
    INDEX idx_type (relationship_type),
    INDEX idx_status (status)
);
```

### 2.2 无需删除的表

**重要发现**：不存在单独的 `relationship` 表。`RelationshipPO` 类虽然存在，但其 `@TableName("node_2_node")` 注解表明它也使用 `node_2_node` 表。因此：
- 无需数据迁移
- 无需删除任何表
- 本次重构纯粹是代码清理

## 3. Entity Relationships

```
┌──────────┐         ┌────────────┐         ┌──────────┐
│   Node   │◄────────│  Node2Node │────────►│   Node   │
│ (source) │         │            │         │ (target) │
└──────────┘         └────────────┘         └──────────┘
     │                     │                      │
     │                     │                      │
     ▼                     ▼                      ▼
  sourceId            id, type,              targetId
                     direction,
                     strength,
                      status
```

## 4. State Transitions

### 4.1 Node2Node 状态

```
           ┌─────────────┐
           │   NORMAL    │
           └──────┬──────┘
                  │
        ┌─────────┴─────────┐
        ▼                   ▼
   ┌─────────┐         ┌─────────┐
   │ WARNING │◄───────►│ CRITICAL│
   └─────────┘         └─────────┘
        │                   │
        └─────────┬─────────┘
                  ▼
           ┌─────────────┐
           │   NORMAL    │ (可恢复)
           └─────────────┘
```

**状态转换规则**:
- 新建关系默认 NORMAL 状态
- WARNING/CRITICAL 可相互转换
- WARNING/CRITICAL 可恢复为 NORMAL
- 删除为物理删除（有外键级联删除约束）

## 5. Validation Rules

| 规则 | 描述 | 错误码 |
|-----|------|-------|
| 非自引用 | sourceId != targetId | NODE2NODE_SELF_REFERENCE |
| 节点存在 | sourceId/targetId 必须存在 | NODE2NODE_NODE_NOT_FOUND |
| 关系唯一 | 同对节点同类型关系唯一 | NODE2NODE_DUPLICATE |
| 类型有效 | relationshipType 必须是有效枚举值 | NODE2NODE_INVALID_TYPE |
| 方向有效 | direction 必须是有效枚举值 | NODE2NODE_INVALID_DIRECTION |
