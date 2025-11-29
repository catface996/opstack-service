# OpenSpec AI交互最佳实践总结

基于 OpenSpec 项目提示词分析，总结与AI交互的核心最佳实践。

---

## 最佳实践清单

| # | 实践 | 核心价值 | 一句话总结 |
|---|------|----------|------------|
| 1 | 分阶段工作流 | 可控性 | 大任务分阶段，每阶段有检查点 |
| 2 | 上下文优先 | 准确性 | 先了解现状，再决定行动 |
| 3 | 触发词匹配 | 效率 | 小事直接做，大事走流程 |
| 4 | 结构化输出 | 可解析 | 固定格式让工具能自动处理 |
| 5 | Delta操作 | 清晰度 | 只记录变化，一眼看出改了什么 |
| 6 | 护栏约束 | 安全性 | 明确边界，防止AI越界 |
| 7 | 简洁优先 | 可维护 | 用证据驱动复杂度，不要过度设计 |
| 8 | 任务跟踪 | 可追溯 | 进度可视化，支持中断续做 |
| 9 | 命名约定 | 一致性 | 统一风格，减少认知负担 |
| 10 | 代码引用 | 精确性 | file:line 格式，点击即达 |
| 11 | 工具选择 | 效率 | 用专用工具，不要万物bash |
| 12 | 层级文档 | 效率 | 按需加载，减少context浪费 |
| 13 | 澄清优先 | 准确性 | 问清楚再做，不要假设 |
| 14 | 验证闭环 | 质量 | 每阶段必验证，自动化把关 |
| 15 | 批准门控 | 可控性 | 重要节点等人类确认 |

---

## 1. 分阶段工作流（Staged Workflow）

**实践要点**：将复杂任务拆分为 Proposal → Apply → Archive 三个阶段，每个阶段有明确边界。

**案例**：
```
❌ 错误做法：
User: "给系统加个双因素认证"
AI: 直接开始写代码，边写边改需求

✅ 正确做法：
User: "给系统加个双因素认证"
AI:
  Stage 1 - Proposal: 创建 proposal.md 说明为什么需要、影响哪些模块
  Stage 2 - Apply: 等用户批准后，按 tasks.md 逐步实现
  Stage 3 - Archive: 部署后归档，更新规格文档
```

**为什么这么做**：
- 防止AI在需求不明确时就开始编码，导致返工
- 让人类有审批节点，保持控制权
- 每个阶段产出可验证，出问题容易定位

**决策树**：
```
New request?
├─ Bug fix? → Fix directly
├─ New feature? → Create proposal
├─ Breaking change? → Create proposal
└─ Unclear? → Create proposal (safer)
```

---

## 2. 上下文优先（Context First）

**实践要点**：执行任何任务前，先读取相关文档和检查现有状态。

**案例**：
```
❌ 错误做法：
User: "帮我加个用户导出功能"
AI: 直接创建 add-user-export 变更，写新的 spec

✅ 正确做法：
User: "帮我加个用户导出功能"
AI:
  1. 运行 openspec list --specs 查看现有能力
  2. 发现已有 data-export 能力
  3. 决定 MODIFY 现有 spec 而非创建重复的新能力
```

**为什么这么做**：
- 避免创建重复功能
- 理解现有架构约束
- 保持系统一致性，减少技术债

**上下文检查清单**：
```markdown
- [ ] Read relevant specs in `specs/[capability]/spec.md`
- [ ] Check pending changes in `changes/` for conflicts
- [ ] Read `openspec/project.md` for conventions
- [ ] Run `openspec list` to see active changes
- [ ] If ambiguous, ask 1-2 clarifying questions before scaffolding
```

---

## 3. 触发词匹配（Trigger Matching）

**实践要点**：通过关键词判断用户意图，决定走哪条工作流。

**案例**：
```
触发词映射表：
┌─────────────────────────────────────┬──────────────────────┐
│ 用户输入                             │ AI动作               │
├─────────────────────────────────────┼──────────────────────┤
│ "帮我创建一个proposal"               │ → 进入 Proposal 流程  │
│ "这个按钮颜色改成蓝色"               │ → 直接修改（小变更）   │
│ "重构整个认证系统"                   │ → 进入 Proposal 流程  │
│ "修复这个typo"                       │ → 直接修改           │
└─────────────────────────────────────┴──────────────────────┘

判断规则：
- 含 proposal/change/spec + create/plan → Proposal流程
- Bug fix / typo / formatting → 直接修复
- 不确定 → 走 Proposal（更安全）
```

**为什么这么做**：
- 小任务不需要走完整流程，提高效率
- 大任务必须有规划，降低风险
- AI行为可预测，用户知道会发生什么

---

## 4. 结构化输出格式（Structured Output Format）

**实践要点**：使用固定的 markdown 格式描述需求和场景。

**案例**：
```markdown
❌ 错误格式：
用户登录时要验证密码，成功就返回token，失败就报错

✅ 正确格式：
### Requirement: User Authentication
The system SHALL validate user credentials during login.

#### Scenario: Successful login
- **WHEN** user provides valid credentials
- **THEN** system SHALL return a JWT token
- **AND** set token expiry to 24 hours

#### Scenario: Failed login
- **WHEN** user provides invalid credentials
- **THEN** system SHALL return 401 error
- **AND** increment failed attempt counter
```

**为什么这么做**：
- 格式统一，工具可以自动解析和验证
- 场景明确，测试用例可以直接从中生成
- 减少歧义，`SHALL` 表示强制要求，`SHOULD` 表示建议

**格式规则**：
- 使用 `### Requirement:` 定义需求（三级标题）
- 使用 `#### Scenario:` 定义场景（四级标题）
- 使用 **WHEN/THEN/AND** 关键词描述行为
- 每个需求必须至少有一个场景

---

## 5. Delta操作模式（Delta Operations）

**实践要点**：变更只记录增量（增/改/删），不复制完整文档。

**案例**：
```markdown
❌ 错误做法（复制整个spec再修改）：
changes/add-2fa/specs/auth/spec.md
# Auth Specification（复制了原有的100行内容）
...
### Requirement: Two-Factor Authentication（新增的10行）

✅ 正确做法（只记录变更）：
changes/add-2fa/specs/auth/spec.md

## ADDED Requirements
### Requirement: Two-Factor Authentication
Users SHALL provide a second factor during login.

#### Scenario: OTP challenge
- **WHEN** valid password is provided
- **THEN** system SHALL send OTP to registered device
```

**为什么这么做**：
- 一眼看出这次改了什么，Review效率高
- 不会意外覆盖其他人的修改
- 归档时工具自动合并，减少人工错误

**Delta操作类型**：
| 操作 | 符号 | 用途 |
|------|------|------|
| `## ADDED Requirements` | `+` (绿色) | 新增功能 |
| `## MODIFIED Requirements` | `~` (黄色) | 修改行为 |
| `## REMOVED Requirements` | `-` (红色) | 删除功能 |
| `## RENAMED Requirements` | `→` (青色) | 重命名 |

---

## 6. 护栏约束（Guardrails）

**实践要点**：在提示词中明确禁止和限制AI的行为边界。

**案例**：
```markdown
Proposal阶段的护栏：

✅ 允许：
- 创建 proposal.md, tasks.md, design.md
- 创建 spec delta 文件
- 询问澄清问题

❌ 禁止：
- 写任何实现代码
- 修改现有代码文件
- 跳过验证步骤

实际效果：
User: "加个支付功能，顺便把代码写了"
AI: "我已创建 proposal 和 spec，但根据工作流规范，
     代码实现需要在 proposal 批准后的 Apply 阶段进行。
     请先审阅 proposal.md，确认后我再开始实现。"
```

**为什么这么做**：
- 防止AI过度积极，做超出预期的事
- 保留人类决策点
- 错误可控，proposal写错了改文档比改代码容易

**通用护栏模板**：
```markdown
**Guardrails**
- Favor straightforward, minimal implementations first
- Keep changes tightly scoped to the requested outcome
- Identify vague details and ask follow-up questions before editing
- Do not write code during proposal stage
```

---

## 7. 简洁优先（Simplicity First）

**实践要点**：默认选择最简单的实现，只在有证据支持时才增加复杂度。

**案例**：
```
需求：存储用户配置

❌ 过度设计：
"我来设计一个可扩展的配置系统：
 - 抽象 ConfigProvider 接口
 - 实现 FileConfigProvider, RedisConfigProvider, S3ConfigProvider
 - 添加配置热更新机制
 - 引入配置版本控制..."

✅ 简洁实现：
"用户配置存JSON文件，代码约50行。

 复杂度触发条件（满足任一才考虑扩展）：
 - 性能数据显示文件读写成为瓶颈
 - 用户数 > 1000 需要分布式存储
 - 多个服务需要共享配置"
```

**为什么这么做**：
- 简单代码易理解、易维护、易测试
- 避免"以防万一"的过度工程
- 复杂度是有成本的，用数据驱动决策

**简洁原则**：
```markdown
- Default to <100 lines of new code
- Single-file implementations until proven insufficient
- Avoid frameworks without clear justification
- Choose boring, proven patterns
```

---

## 8. 任务跟踪（Task Tracking）

**实践要点**：将实现步骤写成可勾选的任务清单，逐项完成并标记。

**案例**：
```markdown
tasks.md:

## 1. Database Layer
- [x] 1.1 Create user_2fa table migration
- [x] 1.2 Add OTP secret column to users table
- [ ] 1.3 Write repository methods

## 2. API Layer
- [ ] 2.1 Create POST /auth/2fa/setup endpoint
- [ ] 2.2 Create POST /auth/2fa/verify endpoint
- [ ] 2.3 Add 2FA check to login flow

## 3. Testing
- [ ] 3.1 Unit tests for OTP generation
- [ ] 3.2 Integration tests for 2FA flow

AI工作模式：
1. 读取 tasks.md
2. 找到第一个未完成项 (1.3)
3. 完成后立即标记 [x]
4. 继续下一项
```

**为什么这么做**：
- 进度可视化，用户随时知道做到哪了
- AI不会遗漏步骤
- 中断后可以继续，不用从头开始
- 便于多人/多次会话协作

---

## 9. 命名约定（Naming Conventions）

**实践要点**：使用统一的命名模式：change-id用动词开头的kebab-case，capability用动词-名词模式。

**案例**：
```
Change ID 命名：

❌ 错误命名：
- auth_update（下划线）
- AddUserFeature（驼峰）
- feature-123（无意义编号）
- changes（太模糊）

✅ 正确命名：
- add-two-factor-auth（动词开头，描述清晰）
- update-password-policy
- remove-legacy-oauth
- refactor-session-handling

Capability 命名：

❌ 错误：authentication_module, UserAuth
✅ 正确：user-auth, payment-capture, cli-init
```

**为什么这么做**：
- 一致性让项目结构可预测
- 动词开头一眼看出是新增/修改/删除
- kebab-case适合URL和文件路径
- 避免命名冲突和歧义

**命名规则**：
- Change ID: `add-`, `update-`, `remove-`, `refactor-` + 描述
- Capability: 动词-名词模式，如 `user-auth`, `payment-capture`
- 全小写，用连字符分隔

---

## 10. 代码引用规范（Reference Format）

**实践要点**：使用 `file.ts:42` 格式引用代码位置。

**案例**：
```markdown
❌ 模糊引用：
"认证逻辑在auth文件里"
"那个处理登录的函数"

✅ 精确引用：
"认证逻辑在 src/services/auth.ts:127"
"登录处理函数 handleLogin() 在 src/controllers/user.ts:45"

在 proposal.md 中：
## Impact
- Affected code:
  - src/services/auth.ts:120-150 (add 2FA validation)
  - src/controllers/login.ts:45 (modify login flow)
  - src/models/user.ts:23 (add otp_secret field)
```

**为什么这么做**：
- 精确定位，点击即可跳转
- Review时快速找到相关代码
- 减少"你说的是哪个文件"的来回沟通

---

## 11. 工具选择策略（Tool Selection Strategy）

**实践要点**：根据任务类型选择最合适的工具，而不是什么都用bash。

**案例**：
```
任务：查找所有包含 "TODO" 的文件

❌ 低效做法：
AI: 运行 bash: grep -r "TODO" .
（可能遇到权限问题，输出格式不友好）

✅ 高效做法：
AI: 使用 Grep 工具，pattern="TODO", output_mode="files_with_matches"
（工具优化过，结果结构化）
```

**为什么这么做**：
- 专用工具有更好的错误处理和输出格式
- 减少token消耗（bash输出往往很冗长）
- 跨平台兼容性更好

**工具选择速查表**：
| 任务 | 正确工具 | 避免 |
|------|----------|------|
| 按名称找文件 | Glob | bash: find |
| 搜索代码内容 | Grep | bash: grep/rg |
| 读取文件 | Read | bash: cat |
| 编辑文件 | Edit | bash: sed |
| 复杂探索 | Task agent | 多轮手动搜索 |
| 运行命令 | Bash | - |

---

## 12. 层级文档引用（Hierarchical Documentation）

**实践要点**：文档分层，简短指引指向详细文档，按需加载。

**案例**：
```
文档层级：

AGENTS.md (根目录, ~20行)
└── "当涉及proposal/change时，打开 openspec/AGENTS.md"

openspec/AGENTS.md (~450行)
├── TL;DR 快速清单（给熟悉的人）
├── 三阶段工作流详解
├── Spec格式规范
└── "更多项目约定见 project.md"

openspec/project.md (~50行)
├── 技术栈
├── 代码约定
└── 错误处理规范

实际加载过程：
1. AI首先只加载根目录 AGENTS.md（20行）
2. 用户说"创建proposal" → 触发加载 openspec/AGENTS.md
3. 需要项目约定时 → 再加载 project.md
```

**为什么这么做**：
- 减少初始context占用
- 按需加载，只读取相关内容
- 维护成本低，改一处文档即可

---

## 13. 澄清优先于假设（Clarify Before Assume）

**实践要点**：遇到模糊需求时，先提问澄清，不要假设。

**案例**：
```
User: "加个导出功能"

❌ 假设并执行：
AI: "好的，我来添加CSV导出功能..."
（用户可能想要的是PDF，或者Excel，或者API导出）

✅ 先澄清：
AI: "关于导出功能，我需要澄清几点：
     1. 导出格式：CSV / Excel / PDF / JSON？
     2. 导出内容：全部数据还是当前筛选结果？
     3. 导出方式：浏览器下载还是发送邮件？"

User: "Excel格式，当前筛选结果，浏览器下载"
AI: "明白了，我来创建 proposal..."
```

**为什么这么做**：
- 一次做对比返工三次效率高
- 用户参与决策，结果更符合预期
- 建立信任感，AI不会"自作主张"

---

## 14. 验证闭环（Validation Loop）

**实践要点**：每个阶段完成后必须运行验证命令。

**案例**：
```bash
# Proposal 阶段结束时
openspec validate add-2fa --strict
# 检查：spec格式正确、scenario存在、无重复requirement

# Apply 阶段结束时
pnpm test        # 单元测试通过
pnpm build       # 构建成功
openspec validate --strict  # spec仍然有效

# Archive 阶段结束时
openspec archive add-2fa --yes
openspec validate --strict  # 归档后spec仍有效

验证失败时的处理：
❌ 忽略错误继续
✅ 停下来修复，直到验证通过
```

**为什么这么做**：
- 早发现问题，修复成本低
- 保证产出质量一致
- 自动化验证比人工review更可靠

---

## 15. 批准门控（Approval Gate）

**实践要点**：重要操作（如开始实现、归档）必须等待人类明确批准。

**案例**：
```
工作流中的批准点：

Proposal阶段 ──────────────────────────────────
  │  AI: 创建 proposal.md, tasks.md, spec deltas
  │  AI: 运行 openspec validate --strict
  │  AI: "Proposal 已创建，请审阅后告知是否可以开始实现"
  ▼
┌─────────────────────────────────────────────┐
│  🔒 APPROVAL GATE: 等待用户确认             │
│     User: "Approved" / "需要修改xxx"        │
└─────────────────────────────────────────────┘
  │
  ▼
Apply阶段 ─────────────────────────────────────
  │  AI: 按 tasks.md 实现
  │  ...
```

**为什么这么做**：
- 防止AI在错误方向上走太远
- 给人类review和调整的机会
- 责任明确：proposal有问题是AI的，批准后有问题是审批者的

---

## 附录：OpenSpec 目录结构

```
openspec/
├── project.md              # 项目约定
├── AGENTS.md               # AI助手指令
├── specs/                  # 当前已部署的能力（真相来源）
│   └── [capability]/
│       ├── spec.md         # 需求和场景
│       └── design.md       # 技术设计（可选）
└── changes/                # 变更提案
    ├── [change-name]/
    │   ├── proposal.md     # 为什么改、改什么
    │   ├── tasks.md        # 实现清单
    │   ├── design.md       # 技术决策（可选）
    │   └── specs/          # Delta变更
    │       └── [capability]/
    │           └── spec.md # ADDED/MODIFIED/REMOVED
    └── archive/            # 已完成的变更
        └── YYYY-MM-DD-[name]/
```

---

## 附录：斜杠命令快速参考

| 命令 | 用途 | 阶段 |
|------|------|------|
| `/proposal` | 创建新的变更提案 | Stage 1 |
| `/apply` | 实现已批准的变更 | Stage 2 |
| `/archive` | 归档已部署的变更 | Stage 3 |

---

## 附录：CLI 常用命令

```bash
# 查看状态
openspec list              # 列出活跃变更
openspec list --specs      # 列出所有规格

# 查看详情
openspec show [item]       # 显示变更或规格
openspec show [id] --json --deltas-only  # JSON格式查看delta

# 验证
openspec validate [item]   # 验证变更或规格
openspec validate --strict # 严格模式验证

# 归档
openspec archive <id> --yes  # 非交互式归档
openspec archive <id> --skip-specs  # 跳过spec更新（仅工具变更）
```

---

*文档生成自 OpenSpec 项目提示词分析*
