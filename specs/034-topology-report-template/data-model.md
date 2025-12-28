# Data Model: Topology 绑定报告模板

**Feature**: 034-topology-report-template
**Date**: 2025-12-28

## 1. 实体关系图

```
┌─────────────┐         ┌──────────────────────────────┐         ┌───────────────────┐
│  topology   │ 1     * │ topology_2_report_template   │ *     1 │  report_template  │
├─────────────┤         ├──────────────────────────────┤         ├───────────────────┤
│ id (PK)     │─────────│ id (PK)                      │─────────│ id (PK)           │
│ name        │         │ topology_id (FK)             │         │ name              │
│ description │         │ report_template_id (FK)      │         │ description       │
│ status      │         │ created_by                   │         │ category          │
│ ...         │         │ created_at                   │         │ content           │
│             │         │ deleted                      │         │ ...               │
└─────────────┘         └──────────────────────────────┘         └───────────────────┘
```

## 2. 新建实体

### 2.1 topology_2_report_template (关联表)

**表描述**: 拓扑图与报告模板的多对多绑定关系表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 关联ID |
| topology_id | BIGINT | NOT NULL, FK | 拓扑图ID |
| report_template_id | BIGINT | NOT NULL, FK | 报告模板ID |
| created_by | BIGINT | NULL | 创建人ID |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 软删除标记: 0-未删除, 1-已删除 |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY uk_topology_template (topology_id, report_template_id, deleted)
- INDEX idx_topology_id (topology_id)
- INDEX idx_report_template_id (report_template_id)

**约束说明**:
- 唯一约束包含 `deleted` 字段，允许软删除后重新绑定
- 不使用外键约束（与项目现有表设计一致），通过应用层保证数据完整性

## 3. 现有实体引用

### 3.1 topology (拓扑图)

**关键字段**:
- `id`: 主键，用于关联
- `name`: 名称，用于显示
- `deleted`: 软删除标记

### 3.2 report_template (报告模板)

**关键字段**:
- `id`: 主键，用于关联
- `name`: 名称，用于显示和搜索
- `description`: 描述，用于搜索
- `category`: 分类，用于筛选
- `deleted`: 软删除标记

## 4. 验证规则

### 4.1 绑定操作验证

| 规则 | 说明 | 错误码 |
|------|------|--------|
| 拓扑图存在性 | topology_id 必须对应未删除的拓扑图 | 404001 |
| 模板存在性 | report_template_id 必须对应未删除的模板 | 404002 |
| 唯一性 | 同一对 (topology_id, report_template_id) 不能重复绑定 | 409001 |
| 批量限制 | 单次最多绑定 100 个模板 | 400001 |

### 4.2 解绑操作验证

| 规则 | 说明 | 错误码 |
|------|------|--------|
| 拓扑图存在性 | topology_id 必须对应未删除的拓扑图 | 404001 |
| 绑定关系存在性 | 绑定关系必须存在（软删除 = 0） | 404003 |

## 5. 状态转换

绑定关系只有两种状态：

```
创建 ──────► 已绑定 (deleted=0)
                │
                │ 解绑操作
                ▼
            已删除 (deleted=1)
                │
                │ 重新绑定
                ▼
            已绑定 (deleted=0，新记录)
```

**说明**:
- 解绑操作执行软删除（deleted = 1）
- 重新绑定时，由于唯一约束包含 deleted 字段，会创建新记录

## 6. 数据迁移

### 6.1 Flyway 迁移脚本

**文件**: `V25__topology_report_template_binding.sql`

```sql
-- =====================================================
-- V25: 创建拓扑图-报告模板绑定关系表
-- Feature: 034-topology-report-template
-- Date: 2025-12-28
-- =====================================================

CREATE TABLE topology_2_report_template (
    -- 主键
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联ID',

    -- 关联字段
    topology_id           BIGINT          NOT NULL COMMENT '拓扑图ID',
    report_template_id    BIGINT          NOT NULL COMMENT '报告模板ID',

    -- 审计字段
    created_by            BIGINT          COMMENT '创建人ID',
    created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 软删除
    deleted               TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    UNIQUE KEY uk_topology_template (topology_id, report_template_id, deleted),
    INDEX idx_topology_id (topology_id),
    INDEX idx_report_template_id (report_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拓扑图-报告模板关联表';
```

## 7. PO 类设计

### 7.1 TopologyReportTemplatePO

```java
@Data
@TableName("topology_2_report_template")
public class TopologyReportTemplatePO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("topology_id")
    private Long topologyId;

    @TableField("report_template_id")
    private Long reportTemplateId;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    // 派生字段（JOIN 查询填充）
    @TableField(exist = false)
    private String templateName;

    @TableField(exist = false)
    private String templateDescription;

    @TableField(exist = false)
    private String templateCategory;
}
```

## 8. 查询模式

### 8.1 查询已绑定模板

```sql
SELECT rt.*, trt.created_at as bound_at, trt.created_by as bound_by
FROM topology_2_report_template trt
INNER JOIN report_template rt ON trt.report_template_id = rt.id AND rt.deleted = 0
WHERE trt.topology_id = ?
  AND trt.deleted = 0
  AND (rt.name LIKE '%keyword%' OR rt.description LIKE '%keyword%')
ORDER BY trt.created_at DESC
LIMIT ?, ?
```

### 8.2 查询未绑定模板

```sql
SELECT rt.*
FROM report_template rt
WHERE rt.deleted = 0
  AND rt.id NOT IN (
      SELECT report_template_id
      FROM topology_2_report_template
      WHERE topology_id = ? AND deleted = 0
  )
  AND (rt.name LIKE '%keyword%' OR rt.description LIKE '%keyword%')
ORDER BY rt.created_at DESC
LIMIT ?, ?
```
