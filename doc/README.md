# 项目文档目录

本目录包含项目的所有文档，按功能和阶段组织。

## 目录结构

```
doc/
├── README.md                           # 本文件 - 文档目录说明
├── 01-init-backend/                    # 后端初始化相关文档
│   ├── 1-project-architecture-design.md    # 原始需求：项目架构设计
│   ├── 2-mybatis-plus-integration.md       # MyBatis-Plus 集成方案
│   ├── DEPENDENCIES.md                     # 依赖管理文档
│   └── github-best-practices-summary.md    # GitHub 最佳实践总结
├── 02-verification/                    # 生产就绪验证文档
│   └── 001-init-ddd-architecture/          # 对应 specs/001-init-ddd-architecture
│       ├── README.md                       # 验证文档说明
│       ├── PRODUCTION_READINESS_VERIFICATION.md  # 完整验证报告
│       └── QUICK_VERIFICATION.md           # 快速验证清单
└── 04-scoring/                         # 评分标准文档
    ├── design-scoring-criteria.md          # 设计评分标准
    ├── requirements-scoring-criteria.md    # 需求评分标准
    └── tasks-planning-scoring-criteria.md  # 任务规划评分标准
```

## 文档分类说明

### 01-init-backend - 后端初始化

包含项目初始化阶段的所有设计文档和技术选型说明：
- **1-project-architecture-design.md**: 项目架构的原始需求和设计思路
- **2-mybatis-plus-integration.md**: MyBatis-Plus 集成方案和最佳实践
- **DEPENDENCIES.md**: 依赖版本管理策略和技术栈说明
- **github-best-practices-summary.md**: GitHub 使用最佳实践

### 02-verification - 生产就绪验证

包含项目生产就绪状态的验证文档，按 spec 编号组织：

- **001-init-ddd-architecture/**: 对应 `specs/001-init-ddd-architecture`
  - 完整的生产就绪验证报告（基于 10 条成功标准）
  - 快速验证清单（5 分钟验证）
  - 自动化验证脚本说明

未来其他 spec 的验证文档也将按此结构组织。

### 04-scoring - 评分标准

包含项目质量评分标准文档，用于自动化评估：
- **design-scoring-criteria.md**: 设计阶段评分标准
- **requirements-scoring-criteria.md**: 需求阶段评分标准
- **tasks-planning-scoring-criteria.md**: 任务规划阶段评分标准

## 文档编号规则

文档目录采用编号前缀，便于组织和排序：
- `01-xx`: 项目初始化和基础设施
- `02-xx`: 验证和质量保证
- `03-xx`: 保留（未来功能）
- `04-xx`: 评分标准和规范
- `05-xx`: 保留（未来功能）

## 与其他文档的关系

### specs/ 目录
包含功能规格说明（Specification），定义"做什么"：
- 功能需求、用户故事、验收标准
- 实现计划、架构决策记录（ADR）
- 任务清单、合约定义

### doc/ 目录（本目录）
包含支持性文档（Documentation），说明"为什么"和"怎么做"：
- 原始需求和背景说明
- 技术选型和集成方案
- 验证报告和评分标准

### bootstrap/src/main/resources/
包含运行时配置文档：
- 环境配置说明
- 日志配置说明

## 文档查找指南

### 我想了解...

**项目架构是如何设计的？**
→ `01-init-backend/1-project-architecture-design.md`

**依赖版本是如何管理的？**
→ `01-init-backend/DEPENDENCIES.md`

**如何验证项目已达到生产就绪状态？**
→ `02-verification/001-init-ddd-architecture/`

**如何评估项目质量？**
→ `04-scoring/` 目录下的评分标准文档

**功能需求和验收标准是什么？**
→ `specs/001-init-ddd-architecture/spec.md`

**实现计划和任务清单？**
→ `specs/001-init-ddd-architecture/plan.md` 和 `tasks.md`

## 文档维护规范

1. **新增验证文档**: 在 `02-verification/` 下创建对应 spec 编号的子目录
2. **新增功能文档**: 根据功能类型选择合适的编号目录，或创建新的编号目录
3. **文档命名**: 使用小写字母和连字符，避免空格和特殊字符
4. **目录 README**: 每个子目录应包含 README.md 说明其用途和内容

## 更新记录

- **2025-11-21**: 初始创建，重组文档结构
  - 创建编号目录结构（01, 02, 04）
  - 将 DEPENDENCIES.md 移至 01-init-backend/
  - 创建 02-verification/001-init-ddd-architecture/ 对应 spec
