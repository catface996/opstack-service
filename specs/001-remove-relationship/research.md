# Research: 移除 Relationship，用 Node2Node 替代

**Date**: 2025-12-28
**Feature**: 001-remove-relationship

## 1. 现有 Relationship 实现分析

### 1.1 文件清单（保持 API 兼容性）

**保留的文件（Interface 层 + Application API）**:
- `interface/interface-http/src/main/java/.../controller/RelationshipController.java` - 保留
- `interface/interface-http/src/main/java/.../request/relationship/*` - 全部保留
- `application/application-api/src/main/java/.../dto/relationship/*` - 全部保留
- `application/application-api/src/main/java/.../service/relationship/RelationshipApplicationService.java` - 保留接口

**修改的文件（Application Impl）**:
- `application/application-impl/src/main/java/.../service/relationship/RelationshipApplicationServiceImpl.java` - 改为调用 Node2Node 服务

**待删除的文件（Domain + Infrastructure）**:

**Domain 层（5 个文件）**:
- `domain/domain-api/src/main/java/.../service/relationship/RelationshipDomainService.java` - 删除
- `domain/domain-impl/src/main/java/.../service/relationship/RelationshipDomainServiceImpl.java` - 删除
- `domain/domain-impl/src/test/java/.../service/relationship/RelationshipDomainServiceImplTest.java` - 删除
- `domain/domain-model/src/main/java/.../model/relationship/Relationship.java` - 删除
- `domain/repository-api/src/main/java/.../repository/relationship/RelationshipRepository.java` - 删除

**Infrastructure 层（4 个文件）**:
- `infrastructure/repository/mysql-impl/src/main/java/.../impl/relationship/RelationshipRepositoryImpl.java` - 删除
- `infrastructure/repository/mysql-impl/src/main/java/.../mapper/relationship/RelationshipMapper.java` - 删除
- `infrastructure/repository/mysql-impl/src/main/java/.../po/relationship/RelationshipPO.java` - 删除
- `infrastructure/repository/mysql-impl/src/main/resources/mapper/relationship/RelationshipMapper.xml` - 删除

### 1.2 保留的枚举类

以下枚举类在 Relationship 和 Node2Node 中通用，保留：
- `RelationshipType.java` - 关系类型（DEPENDENCY, CONTAINS, CALLS 等）
- `RelationshipDirection.java` - 关系方向（UNIDIRECTIONAL, BIDIRECTIONAL）
- `RelationshipStrength.java` - 关系强度（STRONG, WEAK, OPTIONAL）
- `RelationshipStatus.java` - 关系状态（NORMAL, WARNING, CRITICAL）
- `TraverseResult.java` - 遍历结果

### 1.3 现有 Relationship API 端点

当前 RelationshipController 提供的端点：
| 端点 | 功能 |
|-----|------|
| POST /api/service/v1/relationships/create | 创建关系 |
| POST /api/service/v1/relationships/query | 分页查询关系 |
| POST /api/service/v1/relationships/resource/query | 查询资源的所有关系 |
| POST /api/service/v1/relationships/get | 获取单个关系 |
| POST /api/service/v1/relationships/update | 更新关系 |
| POST /api/service/v1/relationships/delete | 删除关系 |
| POST /api/service/v1/relationships/resource/cycle-detection | 循环依赖检测 |
| POST /api/service/v1/relationships/resource/traverse | 图遍历 |

## 2. 现有 Node2Node 实现分析

### 2.1 已有代码

当前 Node2Node 实现较简单，仅用于拓扑图查询：

**Repository 接口**:
```java
public interface Node2NodeRepository {
    record RelationshipInfo(...) {}
    List<RelationshipInfo> findRelationshipsByNodeIds(List<Long> nodeIds);
    List<RelationshipInfo> findOutgoingBySourceId(Long sourceId);
    List<RelationshipInfo> findIncomingByTargetId(Long targetId);
    int deleteByNodeId(Long nodeId);
}
```

**数据库表 node_2_node**:
```sql
CREATE TABLE node_2_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    relationship_type VARCHAR(50),
    direction VARCHAR(20),
    strength VARCHAR(20),
    status VARCHAR(20),
    description TEXT,
    created_at DATETIME,
    updated_at DATETIME
);
```

### 2.2 需要扩展的功能

Node2Node 需要新增以下功能以替代 Relationship：

1. **CRUD 操作**: create, update, delete, getById
2. **分页查询**: 支持按类型、状态、节点 ID 过滤
3. **图遍历**: 向上/向下遍历
4. **循环检测**: DFS 检测循环依赖
5. **领域模型**: 创建 Node2Node 领域对象

## 3. 技术决策

### 3.1 API 路径决策

**决策**: 保持现有 API 路径 `/api/service/v1/relationships/*` 不变

**理由**:
- 保持 API 兼容性，不影响前端和其他调用方
- 仅重构内部实现，外部接口保持稳定
- 减少变更范围和测试工作量

**被拒绝的替代方案**:
- `/api/service/v1/node2node/*` - 需要修改所有调用方
- `/api/service/v1/nodes/relations/*` - 破坏现有 API 契约

### 3.2 枚举类处理决策

**决策**: 保留 `relationship` 包下的枚举类，不重命名

**理由**:
- 枚举类命名（RelationshipType 等）语义清晰
- 避免大量导入语句修改
- 对外部 API 无影响

### 3.3 数据迁移决策

**决策**: 无需数据迁移，也无需删除任何表

**理由**:
- 经代码分析发现，`RelationshipPO` 已映射到 `node_2_node` 表（`@TableName("node_2_node")`）
- `Node2NodePO` 也映射到同一张 `node_2_node` 表
- 两套代码共用同一张数据库表，数据已在正确位置
- 本次重构纯粹是代码层面的清理，不涉及数据库变更

**证据**:
```java
// RelationshipPO.java
@TableName("node_2_node")
public class RelationshipPO implements Serializable {
    @TableField("source_id")
    private Long sourceResourceId;  // 字段名不同，但映射到同一列
    ...
}

// Node2NodePO.java
@TableName("node_2_node")
public class Node2NodePO implements Serializable {
    @TableField("source_id")
    private Long sourceId;
    ...
}
```

### 3.4 测试策略决策

**决策**: 为 Node2Node 新增完整的单元测试和集成测试

**理由**:
- 确保功能等价性
- 验证所有端点正常工作
- 符合 SC-004 成功标准

## 4. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|-----|------|---------|
| 遗漏 Relationship 引用 | 编译失败 | 删除后执行 `mvn compile` 验证 |
| Node2Node 功能缺失 | API 不完整 | 逐一对比 Relationship API 功能 |
| 枚举类导入错误 | 编译失败 | IDE 自动修复导入 |
| 数据库表删除影响 | 数据丢失 | 生产环境部署前备份 |

## 5. 实现顺序建议

1. **Phase 1**: 创建 Node2Node 领域模型和 Domain Service
2. **Phase 2**: 扩展 Node2NodeRepository 实现完整 CRUD
3. **Phase 3**: 修改 RelationshipApplicationServiceImpl，改为调用 Node2Node 服务
4. **Phase 4**: 删除 Relationship Domain 和 Infrastructure 层代码
5. **Phase 5**: 更新/删除测试
6. **Phase 6**: 验证编译和所有测试通过
