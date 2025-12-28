# Research: Topology 绑定报告模板

**Feature**: 034-topology-report-template
**Date**: 2025-12-28

## 1. 现有实现模式研究

### 1.1 关联表实现模式（topology_2_node）

**Decision**: 采用与 `topology_2_node` 相同的关联表实现模式

**Rationale**:
- 项目已有成熟的多对多关联表实现（`topology_2_node`, `node_2_node`）
- 命名规范遵循宪法 `{tableA}_2_{tableB}` 格式
- 包含软删除支持（`@TableLogic`）
- 审计字段完整

**Alternatives considered**:
- JPA `@ManyToMany` 注解：项目使用 MyBatis-Plus，不使用 JPA
- JSON 数组存储：不利于查询和索引，不符合规范化设计

### 1.2 Controller 设计模式

**Decision**: 在现有 `TopologyController` 中添加报告模板绑定相关端点

**Rationale**:
- 报告模板绑定是 Topology 的子功能，保持 URL 路径一致性
- 遵循现有的 `/api/service/v1/topologies/{sub-resource}/{action}` 模式
- 参考现有的 `/members/add`, `/members/remove`, `/members/query` 实现

**Alternatives considered**:
- 独立的 Controller：会打破现有的资源组织结构
- ReportTemplateController 中实现：绑定关系属于 Topology 侧管理

### 1.3 批量操作设计

**Decision**: 支持批量绑定/解绑，单次最多 100 个模板 ID

**Rationale**:
- 参考现有 `AddMembersRequest.nodeIds` 的批量操作模式
- 100 条限制防止单次请求过大
- 幂等设计：已绑定的跳过，未绑定的跳过

**Alternatives considered**:
- 单个操作接口：需要多次调用，用户体验差
- 无限制批量：可能导致性能问题

## 2. 数据库设计研究

### 2.1 关联表结构

**Decision**: 创建 `topology_2_report_template` 表，遵循宪法关联表模板

**表结构**:
```sql
CREATE TABLE topology_2_report_template (
    id                    BIGINT    NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    topology_id           BIGINT    NOT NULL COMMENT '拓扑图ID',
    report_template_id    BIGINT    NOT NULL COMMENT '报告模板ID',
    created_by            BIGINT    COMMENT '创建人ID',
    created_at            DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted               TINYINT   NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    PRIMARY KEY (id),
    UNIQUE KEY uk_topology_template (topology_id, report_template_id, deleted),
    INDEX idx_topology_id (topology_id),
    INDEX idx_report_template_id (report_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拓扑图-报告模板关联表';
```

**Rationale**:
- 唯一约束包含 `deleted` 字段，支持软删除后重新绑定
- 双向索引支持从两侧查询
- 审计字段简化（关联表只需 created_by/created_at）

## 3. API 设计研究

### 3.1 接口命名

**Decision**: 使用 `/report-templates/{action}` 子路径

| 操作 | Endpoint | 说明 |
|------|----------|------|
| 绑定 | `/api/service/v1/topologies/report-templates/bind` | 批量绑定模板 |
| 解绑 | `/api/service/v1/topologies/report-templates/unbind` | 批量解绑模板 |
| 查询已绑定 | `/api/service/v1/topologies/report-templates/bound` | 分页查询已绑定列表 |
| 查询未绑定 | `/api/service/v1/topologies/report-templates/unbound` | 分页查询未绑定列表 |

**Rationale**:
- 与现有 `/members/{action}` 模式一致
- 语义清晰，bound/unbound 直观表达绑定状态

### 3.2 请求/响应设计

**Decision**: 遵循现有的 Request/DTO 命名规范

- `BindReportTemplatesRequest`: 绑定请求，包含 topologyId + reportTemplateIds[]
- `UnbindReportTemplatesRequest`: 解绑请求，包含 topologyId + reportTemplateIds[]
- `QueryBoundTemplatesRequest`: 查询已绑定，包含 topologyId + 分页参数 + keyword
- `QueryUnboundTemplatesRequest`: 查询未绑定，包含 topologyId + 分页参数 + keyword

**响应**:
- 绑定/解绑：返回操作结果（成功/失败数量）
- 查询列表：返回 `PageResult<ReportTemplateDTO>`

## 4. 分层实现研究

### 4.1 DDD 分层

**Decision**: 遵循现有 DDD 分层架构

| 层 | 职责 | 文件 |
|---|------|------|
| Interface | HTTP 控制器 | TopologyController (扩展) |
| Application | 用例编排 | TopologyApplicationService (扩展) |
| Domain | 业务逻辑 | TopologyReportTemplateDomainService (新建) |
| Infrastructure | 数据持久化 | TopologyReportTemplateRepository (新建) |

**Rationale**:
- 控制器和应用服务扩展现有类，保持资源聚合
- 领域服务和仓储新建，关注点分离

## 5. 性能考虑

### 5.1 查询未绑定模板优化

**Decision**: 使用 NOT IN 子查询或 LEFT JOIN ... IS NULL

**SQL 示例**:
```sql
-- 方案 1: NOT IN (适合小数据量)
SELECT * FROM report_template
WHERE deleted = 0
AND id NOT IN (
    SELECT report_template_id FROM topology_2_report_template
    WHERE topology_id = ? AND deleted = 0
)

-- 方案 2: LEFT JOIN (适合大数据量)
SELECT rt.* FROM report_template rt
LEFT JOIN topology_2_report_template trt
    ON rt.id = trt.report_template_id
    AND trt.topology_id = ?
    AND trt.deleted = 0
WHERE rt.deleted = 0 AND trt.id IS NULL
```

**Decision**: 采用方案 1（NOT IN），数据量在千级别内性能足够

## 6. 总结

本功能完全遵循项目现有模式和宪法规范，无技术风险。主要实现包括：

1. **数据库**: 新建 `topology_2_report_template` 关联表 (V25 迁移脚本)
2. **基础设施层**: 新建 PO、Mapper、Repository
3. **领域层**: 新建 DomainService 处理绑定业务逻辑
4. **应用层**: 扩展 ApplicationService 添加绑定用例
5. **接口层**: 扩展 TopologyController 添加 4 个端点
