# Spec-Kit 优秀提示词案例全集

本文档总结了 Spec-Kit 项目中所有优秀的提示词设计案例，分析每个案例的设计技巧和优势。

---

## 一、强制性语言模式

### Case 1: 不可违反的原则声明

```markdown
### III. Test-First (NON-NEGOTIABLE)
TDD mandatory: Tests written → User approved → Tests fail → Then implement
```

**好在哪里**：
- `NON-NEGOTIABLE` 是绝对性词汇，AI 无法找到绕过的理由
- 明确的执行顺序（箭头表示法）消除歧义
- 将主观判断变成硬性规则

---

### Case 2: 门槛检查强制格式

```markdown
## Phase -1: Pre-Implementation Gates

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Simplicity Gate (Article VII)
- [ ] Using ≤3 projects?
- [ ] No future-proofing?

### Anti-Abstraction Gate (Article VIII)
- [ ] Using framework directly?
- [ ] Single model representation?
```

**好在哪里**：
- 使用复选框格式强制 AI 逐项确认
- `Phase -1` 表示必须在正式阶段前完成
- 量化标准（≤3）消除主观判断空间
- 问句格式迫使 AI 给出 yes/no 回答

---

### Case 3: 严格的操作约束

```markdown
**STRICTLY READ-ONLY**: Do **not** modify any files. Output a structured analysis report.

**Constitution Authority**: The project constitution is **non-negotiable** within this analysis scope.
Constitution conflicts are automatically CRITICAL and require adjustment of the spec, plan, or tasks—
not dilution, reinterpretation, or silent ignoring of the principle.
```

**好在哪里**：
- 使用 `STRICTLY`、`Do **not**` 等绝对禁止语
- 预防性地封堵 AI 可能的"变通"行为（dilution, reinterpretation, silent ignoring）
- 明确优先级：宪法 > 其他文档

---

## 二、格式强制模式

### Case 4: 任务格式的严格定义

```markdown
### Checklist Format (REQUIRED)

Every task MUST strictly follow this format:

- [ ] [TaskID] [P?] [Story?] Description with file path

**Format Components**:
1. **Checkbox**: ALWAYS start with `- [ ]` (markdown checkbox)
2. **Task ID**: Sequential number (T001, T002, T003...) in execution order
3. **[P] marker**: Include ONLY if task is parallelizable
4. **[Story] label**: REQUIRED for user story phase tasks only

**Examples**:
- ✅ CORRECT: `- [ ] T012 [P] [US1] Create User model in src/models/user.py`
- ❌ WRONG: `- [ ] Create User model` (missing ID and Story label)
- ❌ WRONG: `T001 [US1] Create model` (missing checkbox)
- ❌ WRONG: `- [ ] [US1] Create User model` (missing Task ID)
```

**好在哪里**：
- 使用 `MUST strictly follow` 强制遵守
- 逐一拆解格式组件，无歧义
- ✅/❌ 对比示例让 AI 明确边界
- 每个错误示例都说明**具体错在哪里**

---

### Case 5: 表格格式规范

```markdown
**CRITICAL - Table Formatting**: Ensure markdown tables are properly formatted:
- Use consistent spacing with pipes aligned
- Each cell should have spaces around content: `| Content |` not `|Content|`
- Header separator must have at least 3 dashes: `|--------|`
- Test that the table renders correctly in markdown preview
```

**好在哪里**：
- 使用 `CRITICAL` 标记重要性
- 正例 vs 反例对比 (`| Content |` not `|Content|`)
- 具体的技术细节（至少3个破折号）

---

## 三、正反对比模式

### Case 6: 规范内容边界

```markdown
## Quick Guidelines

- ✅ Focus on **WHAT** users need and **WHY**
- ❌ Avoid HOW to implement (no tech stack, APIs, code structure)
- Written for business stakeholders, not developers
```

**好在哪里**：
- ✅/❌ 视觉符号一目了然
- 使用 WHAT/WHY vs HOW 的对立概念
- 明确目标受众（business stakeholders, not developers）

---

### Case 7: 成功标准的正反示例

```markdown
### Success Criteria Guidelines

**Good examples**:
- "Users can complete checkout in under 3 minutes"
- "System supports 10,000 concurrent users"
- "95% of searches return results in under 1 second"

**Bad examples** (implementation-focused):
- "API response time is under 200ms" (too technical, use "Users see results instantly")
- "Database can handle 1000 TPS" (implementation detail, use user-facing metric)
- "React components render efficiently" (framework-specific)
```

**好在哪里**：
- Good/Bad 明确分类
- 每个 Bad 示例都给出**替代方案**
- 括号内解释**为什么是错的**

---

### Case 8: 清单项目的核心概念重塑

```markdown
## Checklist Purpose: "Unit Tests for English"

**CRITICAL CONCEPT**: Checklists are **UNIT TESTS FOR REQUIREMENTS WRITING** -
they validate the quality, clarity, and completeness of requirements in a given domain.

**NOT for verification/testing**:
- ❌ NOT "Verify the button clicks correctly"
- ❌ NOT "Test error handling works"

**FOR requirements quality validation**:
- ✅ "Are visual hierarchy requirements defined for all card types?" (completeness)
- ✅ "Is 'prominent display' quantified with specific sizing/positioning?" (clarity)

**Metaphor**: If your spec is code written in English, the checklist is its unit test suite.
```

**好在哪里**：
- 用隐喻重新定义概念（"Unit Tests for English"）
- `NOT for` / `FOR` 明确划定边界
- 每个示例后括号说明检查维度（completeness, clarity）
- 隐喻帮助 AI 理解抽象概念

---

## 四、执行流程控制模式

### Case 9: 编号执行步骤

```markdown
## Execution Steps

1. **Setup**: Run `{SCRIPT}` from repo root and parse JSON for FEATURE_DIR...
2. **Load context**: Read FEATURE_SPEC and `/memory/constitution.md`...
3. **Execute plan workflow**: Follow the structure in IMPL_PLAN template to:
   - Fill Technical Context (mark unknowns as "NEEDS CLARIFICATION")
   - Fill Constitution Check section from constitution
   - Evaluate gates (ERROR if violations unjustified)
4. **Stop and report**: Command ends after Phase 2 planning...
```

**好在哪里**：
- 编号顺序明确，AI 无法跳步
- 每步有明确的动作动词（Run, Read, Fill, Evaluate, Stop）
- 嵌套子步骤处理复杂逻辑
- 明确终止条件（Command ends after...）

---

### Case 10: 条件分支处理

```markdown
6. **Handle Validation Results**:

   - **If all items pass**: Mark checklist complete and proceed to step 6

   - **If items fail (excluding [NEEDS CLARIFICATION])**:
     1. List the failing items and specific issues
     2. Update the spec to address each issue
     3. Re-run validation until all items pass (max 3 iterations)
     4. If still failing after 3 iterations, document remaining issues and warn user

   - **If [NEEDS CLARIFICATION] markers remain**:
     1. Extract all markers from the spec
     2. **LIMIT CHECK**: If more than 3 markers exist, keep only the 3 most critical
     3. Present options to user in this format...
```

**好在哪里**：
- 使用 `If...` 明确条件分支
- 每个分支有详细子步骤
- 设置安全上限（max 3 iterations, keep only 3 most critical）
- 处理边界情况（still failing after 3 iterations）

---

### Case 11: 循环控制与终止条件

```markdown
4. Sequential questioning loop (interactive):
   - Present EXACTLY ONE question at a time
   - After the user answers:
     - Validate the answer maps to one option
     - If ambiguous, ask for disambiguation (count still belongs to same question)
     - Once satisfactory, record and move to next
   - Stop asking further questions when:
     - All critical ambiguities resolved early, OR
     - User signals completion ("done", "good", "no more"), OR
     - You reach 5 asked questions
   - Never reveal future queued questions in advance
```

**好在哪里**：
- `EXACTLY ONE` 防止批量输出
- 明确三个终止条件（用 OR 连接）
- 处理异常情况（ambiguous answer）
- 禁止特定行为（Never reveal future questions）

---

## 五、不确定性处理模式

### Case 12: 强制标记不确定性

```markdown
When creating this spec from a user prompt:
1. **Mark all ambiguities**: Use [NEEDS CLARIFICATION: specific question]
2. **Don't guess**: If the prompt doesn't specify something, mark it

*Example*:
- **FR-006**: System MUST authenticate users via
  [NEEDS CLARIFICATION: auth method not specified - email/password, SSO, OAuth?]
```

**好在哪里**：
- 提供标准化标记格式 `[NEEDS CLARIFICATION: ...]`
- 明确"不要猜测"原则
- 示例展示如何列出可能选项

---

### Case 13: 合理默认值 vs 必须询问

```markdown
**Examples of reasonable defaults** (don't ask about these):
- Data retention: Industry-standard practices for the domain
- Performance targets: Standard web/mobile app expectations unless specified
- Authentication method: Standard session-based or OAuth2 for web apps

**Common areas needing clarification** (only if no reasonable default exists):
- Feature scope and boundaries (include/exclude specific use cases)
- User types and permissions (if multiple conflicting interpretations)
- Security/compliance requirements (when legally/financially significant)
```

**好在哪里**：
- 区分"可以假设"和"必须询问"
- 减少不必要的用户交互
- 用括号说明判断依据（if multiple conflicting interpretations）

---

### Case 14: 数量限制防止过度询问

```markdown
3. For unclear aspects:
   - Make informed guesses based on context and industry standards
   - Only mark with [NEEDS CLARIFICATION] if:
     - The choice significantly impacts feature scope or user experience
     - Multiple reasonable interpretations exist with different implications
     - No reasonable default exists
   - **LIMIT: Maximum 3 [NEEDS CLARIFICATION] markers total**
   - Prioritize clarifications by impact: scope > security/privacy > user experience > technical details
```

**好在哪里**：
- 硬性数量上限（Maximum 3）
- 明确的判断标准（significantly impacts...）
- 优先级排序（scope > security > UX > technical）

---

## 六、输出结构化模式

### Case 15: 强制输出格式

```markdown
For each clarification needed (max 3), present options to user in this format:

## Question [N]: [Topic]

**Context**: [Quote relevant spec section]

**What we need to know**: [Specific question]

**Suggested Answers**:

| Option | Answer | Implications |
|--------|--------|--------------|
| A      | [First answer] | [What this means] |
| B      | [Second answer] | [What this means] |
| Custom | Provide your own | [How to provide] |

**Your choice**: _[Wait for user response]_
```

**好在哪里**：
- 完整的模板格式，AI 只需填空
- 包含上下文（Quote relevant spec section）
- 表格结构化选项
- 明确等待用户响应

---

### Case 16: 分析报告结构

```markdown
### 6. Produce Compact Analysis Report

Output a Markdown report with the following structure:

## Specification Analysis Report

| ID | Category | Severity | Location(s) | Summary | Recommendation |
|----|----------|----------|-------------|---------|----------------|
| A1 | Duplication | HIGH | spec.md:L120-134 | Two similar... | Merge phrasing |

**Coverage Summary Table:**
| Requirement Key | Has Task? | Task IDs | Notes |
|-----------------|-----------|----------|-------|

**Metrics:**
- Total Requirements
- Total Tasks
- Coverage % (requirements with >=1 task)
- Critical Issues Count
```

**好在哪里**：
- 预定义表格列，AI 只需填充数据
- 包含具体位置（spec.md:L120-134）
- 量化指标（Coverage %）使结果可比较

---

## 七、禁止行为模式

### Case 17: 绝对禁止列表

```markdown
**ABSOLUTELY PROHIBITED** - These make it an implementation test, not a requirements test:
- ❌ Any item starting with "Verify", "Test", "Confirm", "Check" + implementation behavior
- ❌ References to code execution, user actions, system behavior
- ❌ "Displays correctly", "works properly", "functions as expected"
- ❌ "Click", "navigate", "render", "load", "execute"
- ❌ Test cases, test plans, QA procedures
- ❌ Implementation details (frameworks, APIs, algorithms)

**REQUIRED PATTERNS** - These test requirements quality:
- ✅ "Are [requirement type] defined/specified/documented for [scenario]?"
- ✅ "Is [vague term] quantified/clarified with specific criteria?"
```

**好在哪里**：
- 使用 `ABSOLUTELY PROHIBITED` 强烈视觉标记
- 列出具体禁止词汇（Verify, Test, Click, navigate...）
- 禁止列表 vs 必须模式形成对比
- 提供句式模板（Are [X] defined for [Y]?）

---

### Case 18: 防止越界行为

```markdown
## Operating Principles

- **NEVER modify files** (this is read-only analysis)
- **NEVER hallucinate missing sections** (if absent, report them accurately)
- **Prioritize constitution violations** (these are always CRITICAL)
- **Use examples over exhaustive rules** (cite specific instances)
- **Report zero issues gracefully** (emit success report with coverage statistics)
```

**好在哪里**：
- 每条规则都有括号解释**为什么**
- 覆盖正常情况和边界情况（zero issues）
- 明确优先级（constitution violations always CRITICAL）

---

## 八、上下文加载优化模式

### Case 19: 渐进式信息加载

```markdown
### 2. Load Artifacts (Progressive Disclosure)

Load only the minimal necessary context from each artifact:

**From spec.md:**
- Overview/Context
- Functional Requirements
- Non-Functional Requirements
- User Stories
- Edge Cases (if present)

**From plan.md:**
- Architecture/stack choices
- Data Model references
- Phases
- Technical constraints
```

**好在哪里**：
- `Progressive Disclosure` 原则防止上下文过载
- 列出每个文件需要提取的**具体字段**
- 条件字段（if present）避免报错

---

### Case 20: Token 效率优化

```markdown
### 4. Detection Passes (Token-Efficient Analysis)

Focus on high-signal findings. Limit to 50 findings total; aggregate remainder in overflow summary.

### Context Efficiency

- **Minimal high-signal tokens**: Focus on actionable findings, not exhaustive documentation
- **Progressive disclosure**: Load artifacts incrementally; don't dump all content into analysis
- **Token-efficient output**: Limit findings table to 50 rows; summarize overflow
- **Deterministic results**: Rerunning without changes should produce consistent IDs and counts
```

**好在哪里**：
- 明确数量上限（50 findings）
- 强调"high-signal"而非穷举
- 处理溢出情况（aggregate remainder）
- 要求确定性输出

---

## 九、错误处理与恢复模式

### Case 21: 明确错误条件

```markdown
4. Follow this execution flow:

   1. Parse user description from Input
      If empty: ERROR "No feature description provided"
   2. Extract key concepts from description
      Identify: actors, actions, data, constraints
   3. For unclear aspects:
      ...
   4. Fill User Scenarios & Testing section
      If no clear user flow: ERROR "Cannot determine user scenarios"
```

**好在哪里**：
- 每步都有失败条件（If empty, If no clear...）
- 明确错误消息文本
- AI 知道何时应该停止而非继续猜测

---

### Case 22: 重试机制

```markdown
- **If items fail (excluding [NEEDS CLARIFICATION])**:
  1. List the failing items and specific issues
  2. Update the spec to address each issue
  3. Re-run validation until all items pass (max 3 iterations)
  4. If still failing after 3 iterations, document remaining issues in checklist notes and warn user
```

**好在哪里**：
- 允许重试但有上限（max 3）
- 最终失败时的降级处理（document and warn）
- 不会无限循环

---

## 十、用户交互优化模式

### Case 23: 智能推荐 + 用户确认

```markdown
- For multiple-choice questions:
  - **Analyze all options** and determine the **most suitable option** based on:
    - Best practices for the project type
    - Common patterns in similar implementations
    - Risk reduction (security, performance, maintainability)
  - Present your **recommended option prominently** at the top with clear reasoning
  - Format as: `**Recommended:** Option [X] - <reasoning>`
  - Then render all options as a Markdown table
  - After the table, add: "You can reply with the option letter (e.g., 'A'),
    accept the recommendation by saying 'yes', or provide your own short answer."
```

**好在哪里**：
- AI 主动推荐但不替用户决定
- 推荐带有明确理由
- 多种响应方式（字母/yes/自定义）
- 减少用户认知负担

---

### Case 24: 用户终止信号处理

```markdown
- Stop asking further questions when:
  - All critical ambiguities resolved early (remaining queued items become unnecessary), OR
  - User signals completion ("done", "good", "no more"), OR
  - You reach 5 asked questions
- Never reveal future queued questions in advance
- Respect user early termination signals ("stop", "done", "proceed")
```

**好在哪里**：
- 列出多种终止触发词
- 支持提前终止和正常完成
- 尊重用户意愿

---

## 十一、一致性验证模式

### Case 25: 跨文档一致性检查

```markdown
#### F. Inconsistency

- Terminology drift (same concept named differently across files)
- Data entities referenced in plan but absent in spec (or vice versa)
- Task ordering contradictions (e.g., integration tasks before foundational setup)
- Conflicting requirements (e.g., one requires Next.js while other specifies Vue)
```

**好在哪里**：
- 具体的不一致类型（terminology drift, ordering contradictions）
- 举出真实场景（Next.js vs Vue）
- 覆盖多种交叉验证维度

---

### Case 26: 术语规范化

```markdown
5. Integration after EACH accepted answer:
   ...
   - Terminology conflict → Normalize term across spec;
     retain original only if necessary by adding `(formerly referred to as "X")` once
   - If the clarification invalidates an earlier ambiguous statement,
     replace that statement instead of duplicating; leave no obsolete contradictory text
```

**好在哪里**：
- 具体的处理方法（添加"formerly referred to as"）
- 明确替换而非追加（replace instead of duplicating）
- 清理过时内容

---

## 十二、元认知指令模式

### Case 27: 行为规则汇总

```markdown
Behavior rules:

- If no meaningful ambiguities found, respond: "No critical ambiguities detected worth formal clarification."
- If spec file missing, instruct user to run `/speckit.specify` first
- Never exceed 5 total asked questions
- Avoid speculative tech stack questions unless the absence blocks functional clarity
- Respect user early termination signals
- If quota reached with unresolved high-impact categories remaining, explicitly flag them under Deferred
```

**好在哪里**：
- 集中列出所有"边界情况"处理规则
- 使用 If-Then 格式
- 覆盖成功、失败、边界多种情况

---

### Case 28: 指导思维方式

```markdown
**Think like a tester**: Every vague requirement should fail the "testable and unambiguous" checklist item

**Metaphor**: If your spec is code written in English, the checklist is its unit test suite.
You're testing whether the requirements are well-written, complete, unambiguous, and ready for implementation.
```

**好在哪里**：
- 角色扮演指令（Think like a tester）
- 隐喻帮助理解抽象概念
- 将检查行为具象化

---

## 总结：核心提示词设计技巧

| 技巧 | 作用 | 关键词示例 |
|------|------|-----------|
| **强制性语言** | 消除AI绕过的可能 | `MUST`, `NON-NEGOTIABLE`, `STRICTLY`, `NEVER` |
| **正反对比** | 明确边界 | ✅/❌, Good/Bad, CORRECT/WRONG |
| **格式模板** | 标准化输出 | 固定格式 + 填空位置 |
| **数量上限** | 防止过度输出 | `max 3`, `limit to 50`, `≤3 projects` |
| **条件分支** | 处理多种情况 | `If...then`, `If still failing after...` |
| **终止条件** | 防止无限循环 | `Stop when...`, `ERROR if...` |
| **渐进加载** | 优化上下文效率 | `Progressive Disclosure`, `load only necessary` |
| **隐喻重塑** | 帮助理解抽象概念 | "Unit Tests for English", "Think like a tester" |
| **禁止列表** | 排除错误行为 | `ABSOLUTELY PROHIBITED`, `NEVER` |
| **推荐+确认** | 平衡效率与用户控制 | `**Recommended:** Option X - <reasoning>` |

---

## 应用建议

### 1. 建立项目宪法
在项目开始时定义不可违反的原则，使用 `NON-NEGOTIABLE`、`MUST` 等强制性语言。

### 2. 使用模板约束输出
为每种输出类型定义固定格式，提供正确和错误示例的对比。

### 3. 设置安全边界
- 数量上限防止过度输出
- 重试次数限制防止无限循环
- 明确的错误条件和终止信号

### 4. 分阶段控制流程
使用编号步骤、条件分支和门槛检查，确保 AI 按正确顺序执行。

### 5. 处理不确定性
- 提供标准化的不确定标记格式
- 区分"可以假设"和"必须询问"的情况
- 限制询问次数，按优先级排序

---

*文档来源：GitHub spec-kit 项目分析*
*生成日期：2025-11-29*
