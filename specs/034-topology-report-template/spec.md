# Feature Specification: Topology 绑定报告模板

**Feature Branch**: `034-topology-report-template`
**Created**: 2025-12-28
**Status**: Draft
**Input**: Topology 与报告模板的多对多绑定关系，提供绑定、解绑、查看已绑定和未绑定模板的四个接口

## 背景

Topology（拓扑图）需要与报告模板（Report Template）建立多对多的绑定关系，以便在生成报告时可以选择预设的模板。一个拓扑图可以绑定多个报告模板，一个报告模板也可以被多个拓扑图使用。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 绑定报告模板 (Priority: P1)

作为运维人员，我需要将报告模板绑定到指定的拓扑图，以便后续为该拓扑图生成报告时可以使用这些模板。

**Why this priority**: 绑定功能是整个特性的核心基础操作。

**Independent Test**: 可以通过调用绑定接口后查询已绑定列表来验证。

**Acceptance Scenarios**:

1. **Given** 存在拓扑图 A 和报告模板 T1, **When** 执行绑定操作, **Then** 绑定关系创建成功，查询已绑定列表时能看到 T1
2. **Given** 拓扑图 A 已绑定模板 T1, **When** 再次绑定同一模板, **Then** 系统返回"已绑定"提示，不重复创建记录
3. **Given** 需要批量绑定多个模板, **When** 传入多个模板 ID 列表, **Then** 所有未绑定的模板成功绑定，已绑定的跳过
4. **Given** 传入不存在的模板 ID, **When** 执行绑定操作, **Then** 系统返回"模板不存在"错误

---

### User Story 2 - 解除绑定报告模板 (Priority: P1)

作为运维人员，我需要解除拓扑图与报告模板的绑定关系，当某个模板不再适用于该拓扑图时。

**Why this priority**: 解绑功能与绑定功能同等重要，是完整的 CRUD 操作。

**Independent Test**: 可以通过解绑后查询已绑定列表来验证模板已移除。

**Acceptance Scenarios**:

1. **Given** 拓扑图 A 已绑定模板 T1, **When** 执行解绑操作, **Then** 绑定关系删除成功，查询已绑定列表时不再看到 T1
2. **Given** 拓扑图 A 未绑定模板 T2, **When** 尝试解绑 T2, **Then** 系统返回"绑定关系不存在"提示
3. **Given** 需要批量解绑多个模板, **When** 传入多个模板 ID 列表, **Then** 所有已绑定的模板成功解绑，未绑定的跳过

---

### User Story 3 - 查询已绑定的报告模板 (Priority: P1)

作为运维人员，我需要查看某个拓扑图已绑定的所有报告模板列表，以便了解可用的报告模板选项。

**Why this priority**: 查询已绑定列表是使用绑定功能的前提。

**Independent Test**: 可以先绑定几个模板，再查询验证返回结果正确。

**Acceptance Scenarios**:

1. **Given** 拓扑图 A 已绑定 3 个模板, **When** 查询已绑定列表, **Then** 返回包含这 3 个模板的详细信息
2. **Given** 拓扑图 A 未绑定任何模板, **When** 查询已绑定列表, **Then** 返回空列表
3. **Given** 需要分页查询已绑定列表, **When** 传入分页参数, **Then** 返回正确的分页结果
4. **Given** 需要搜索特定模板, **When** 传入关键词参数, **Then** 返回名称或描述匹配的模板

---

### User Story 4 - 查询未绑定的报告模板 (Priority: P2)

作为运维人员，我需要查看某个拓扑图尚未绑定的报告模板列表，以便选择新的模板进行绑定。

**Why this priority**: 此功能提升用户体验，但不是核心流程的必需。

**Independent Test**: 可以对比全部模板列表和已绑定列表来验证结果正确性。

**Acceptance Scenarios**:

1. **Given** 系统中有 10 个模板，拓扑图 A 已绑定 3 个, **When** 查询未绑定列表, **Then** 返回剩余 7 个模板
2. **Given** 拓扑图 A 已绑定所有模板, **When** 查询未绑定列表, **Then** 返回空列表
3. **Given** 需要分页查询未绑定列表, **When** 传入分页参数, **Then** 返回正确的分页结果
4. **Given** 需要搜索特定未绑定模板, **When** 传入关键词参数, **Then** 返回名称或描述匹配的未绑定模板

---

### Edge Cases

- 拓扑图被删除时：绑定关系应自动级联删除或标记为无效
- 报告模板被删除时：绑定关系应自动级联删除或标记为无效
- 并发绑定同一模板：需要防止重复创建记录
- 大量模板时的分页：确保分页性能正常

## Requirements *(mandatory)*

### Functional Requirements

**绑定管理**:

- **FR-001**: 系统 MUST 提供将报告模板绑定到拓扑图的接口
- **FR-002**: 系统 MUST 支持批量绑定多个模板到一个拓扑图
- **FR-003**: 系统 MUST 防止重复绑定（同一拓扑图和模板的组合唯一）
- **FR-004**: 系统 MUST 在绑定时验证拓扑图和模板的存在性

**解绑管理**:

- **FR-005**: 系统 MUST 提供解除拓扑图与报告模板绑定的接口
- **FR-006**: 系统 MUST 支持批量解绑多个模板
- **FR-007**: 系统 SHOULD 在解绑不存在的绑定关系时返回友好提示

**查询功能**:

- **FR-008**: 系统 MUST 提供查询拓扑图已绑定报告模板列表的接口
- **FR-009**: 系统 MUST 提供查询拓扑图未绑定报告模板列表的接口
- **FR-010**: 系统 MUST 支持分页查询绑定/未绑定模板列表
- **FR-011**: 系统 SHOULD 支持按名称关键词过滤模板

**数据完整性**:

- **FR-012**: 系统 MUST 确保绑定关系的外键约束正确
- **FR-013**: 系统 SHOULD 在拓扑图或模板被删除时处理相关绑定记录

### Key Entities

- **topology**: 拓扑图，可绑定多个报告模板
- **report_template**: 报告模板，可被多个拓扑图绑定
- **topology_report_template** (新建): 绑定关系表，存储拓扑图与报告模板的多对多关系

### API Endpoints

| 操作 | Endpoint | Method | 描述 |
|------|----------|--------|------|
| 绑定模板 | `/api/service/v1/topologies/report-templates/bind` | POST | 将报告模板绑定到拓扑图 |
| 解绑模板 | `/api/service/v1/topologies/report-templates/unbind` | POST | 解除拓扑图与报告模板的绑定 |
| 查询已绑定 | `/api/service/v1/topologies/report-templates/bound` | POST | 查询拓扑图已绑定的模板列表 |
| 查询未绑定 | `/api/service/v1/topologies/report-templates/unbound` | POST | 查询拓扑图未绑定的模板列表 |

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 绑定操作成功率达到 100%（有效参数情况下）
- **SC-002**: 解绑操作成功率达到 100%（绑定关系存在情况下）
- **SC-003**: 查询已绑定/未绑定列表响应时间低于 500ms（1000 条模板以内）
- **SC-004**: 支持单次批量绑定/解绑最多 100 个模板
- **SC-005**: 绑定关系唯一性约束 100% 生效，无重复记录

## Assumptions

1. 报告模板表 `report_template` 已存在且包含必要字段
2. 拓扑图表 `topology` 已存在且包含必要字段
3. 所有接口遵循项目现有的 POST-Only API 规范
4. 绑定关系表需要遵循项目宪法的数据库设计标准（包含审计字段、软删除等）

## Out of Scope

- 绑定关系的排序（如优先级）
- 绑定关系的额外属性（如绑定备注）
- 报告自动生成功能（仅建立绑定关系）
- 模板内容预览功能
