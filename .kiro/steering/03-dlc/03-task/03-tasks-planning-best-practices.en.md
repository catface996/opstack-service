---
inclusion: manual
---

# Task Planning Best Practices

---

## Quick Reference: Task Quality Criteria

| Criterion | Requirement | Failure Consequence |
|-----------|-------------|---------------------|
| Single Responsibility | One clear goal per task | ❌ Complex, hard to verify |
| Appropriate Granularity | 1-4 hours to complete | ❌ Unmanageable or trivial |
| Independently Verifiable | Clear acceptance criteria | ❌ Can't confirm completion |
| Reasonable Dependencies | No circular dependencies | ❌ Execution blocked |
| Incremental Delivery | Each task adds value | ❌ No progress visibility |

---

## Phase -1: Pre-Planning Gates (NON-NEGOTIABLE)

*GATE: Must pass before starting task breakdown.*

### Design Completeness Check
- [ ] Design document exists at `.kiro/features/{feature-id}/plan.md`?
- [ ] Design has been validated and approved by user?
- [ ] All design decisions documented (with ADRs)?
- [ ] Architecture diagrams and interface definitions complete?

### Understanding Check
- [ ] Fully understand the architecture pattern?
- [ ] Clear on module boundaries and responsibilities?
- [ ] Understand inter-module dependencies?
- [ ] Identified all technical difficulties and risks?

**If check fails**: STOP, complete or clarify design first.

---

## Phase Goal

Break down the design solution into **INDEPENDENTLY EXECUTABLE, VERIFIABLE** specific tasks, ensuring each task has **CLEAR** goals and acceptance criteria.

## Why Task Breakdown is So Important

Task breakdown is a **CRITICAL** step in transforming design into executable steps. Good task breakdown can:
- ✅ Improve development efficiency and quality
- ✅ Reduce coupling between tasks
- ✅ Support parallel development and incremental delivery
- ✅ Facilitate progress tracking and risk control
- ✅ Ensure deliverable verifiability

**Key Principle**: Tasks **MUST** be both executable and verifiable.

## Task Breakdown Workflow

### Step 1: Understand Design Solution

Before starting to break down tasks, fully understand the design document.

**Key Activities**:
- Read through design document, understand architecture and module division
- Identify core features and dependency relationships
- Mark technical difficulties and risk points
- List questions that need clarification

### Step 2: Create Task List

Based on design document, transform design into executable task list.

**Task Breakdown Principles**:

#### 1. Single Responsibility
Each task focuses on only one clear goal, avoid tasks being too complex.

**Why Important**:
- Reduce task complexity, easy to understand and execute
- Facilitate independent verification and testing
- Reduce coupling between tasks

#### 2. Independently Verifiable
Each task should be independently verifiable after completion.

**Why Important**:
- Discover problems early, reduce fix costs
- Ensure quality of each task
- Support incremental delivery

#### 3. Reasonable Dependencies
Dependencies between tasks should be clear and explicit, avoid circular dependencies.

**Why Important**:
- Clarify task execution order
- Support parallel development
- Facilitate progress management

#### 4. Incremental Delivery
Tasks should support incremental development, each completed task should move the project forward.

**Why Important**:
- Continuously generate value
- Get feedback early
- Reduce integration risk

### Step 3: Write Task Descriptions

Write clear descriptions and acceptance criteria for each task.

**Task Content Requirements**:

#### 1. Task Description
- Clearly state what functionality to implement or what work to complete
- Describe "what to do", not "how to do"
- Focus on goals and results, not implementation process

#### 2. Preconditions (Optional)
- Conditions that must be met before task starts
- Which other tasks are depended upon
- What prerequisite resources or environment are needed

#### 3. Acceptance Criteria (Required)
- How to verify task is correctly completed
- Must be specific, executable, testable
- **Must clearly specify verification method**: Runtime verification, build verification, unit test, static check
- Sort by verification priority: Runtime > Unit Test > Build > Static

### Step 4: Task List Verification

**Key**: After task list is created, don't start execution immediately, but conduct sufficient verification.

**Verification Dimensions**:

#### 1. Consistency with Requirements and Design
- [ ] Is task list consistent with requirements and design?
- [ ] Are there tasks beyond design scope?
- [ ] Does it completely cover all content in design?
- [ ] Are there missing features or modules?

#### 2. Task Executability
- [ ] Is each task's goal clear?
- [ ] Is task granularity appropriate (1-4 hours)?
- [ ] Are dependency relationships between tasks clear?
- [ ] Are there circular dependencies?

#### 3. Task Verifiability
- [ ] Is each task's verification criteria clear?
- [ ] Are verification criteria specific and executable?
- [ ] Are verification methods operable?
- [ ] Is verification priority clearly specified (runtime > build > static)?

**Verification Method**:

Use the following prompt for Kiro to self-check:

> "Please check whether the task list is consistent with requirements and design, whether there are tasks beyond design scope. Also, please clarify acceptance criteria for each task and verify the tasks."

### Step 5: User Confirmation

Confirm task list with user, ensure consistent understanding.

**Must Confirm**:
- Is task breakdown granularity appropriate
- Are task priority and order reasonable
- Are there missing or unnecessary tasks
- Are acceptance criteria clear and feasible

**Only after user explicitly confirms can task execution begin.**

## Acceptance Principles (NON-NEGOTIABLE)

**Core Requirement**: Each acceptance criterion **MUST** clearly mark verification method using 【Verification Method】tag.

Task acceptance **MUST** follow this priority order:

---

### Verification Priority Hierarchy

| Priority | Method | Use When | DON'T Use If... |
|----------|--------|----------|-----------------|
| 1️⃣ Highest | 【Runtime Verification】 | Feature can run in app | - |
| 2️⃣ High | 【Unit Test】 | Business logic testable | Runtime verification possible |
| 3️⃣ Medium | 【Build Verification】 | Structural changes | Runtime/unit test possible |
| 4️⃣ Last Resort | 【Static Check】 | File/config checks only | Any higher method possible |

**STRICT RULE**: **ALWAYS** use the highest possible verification method. **NEVER** use lower methods when higher ones are applicable.

---

### 1. 【Runtime Verification】(Highest Priority) ⭐

**MANDATORY FOR**: Features that can be verified by actually running the application **MUST** be verified by running the application.

**Applicable Scenarios**:
- ✅ Configuration verification (multi-environment, feature toggles, etc.)
- ✅ API endpoint functionality verification
- ✅ Log output format verification
- ✅ Exception handling verification
- ✅ Integration functionality verification
- ✅ External service connection verification

**Verification Method**:
1. Start application (using project's startup command)
2. Access relevant endpoints or trigger relevant functionality
3. Check actual runtime results meet expectations

**Example Acceptance Criterion**:
```
- [ ] User login API returns 200 status code
  【Runtime Verification】: Start app, POST to /api/login, verify response
```

---

### 2. 【Unit Test】(Second Priority)

**USE FOR**: Business logic and algorithms that **CANNOT** be easily verified at runtime.

**Applicable Scenarios**:
- ✅ Service layer business logic
- ✅ Utility class methods
- ✅ Algorithm implementations
- ✅ Data transformation logic

**Verification Method**:
- Execute unit test command (e.g., `mvn test`)
- Check test coverage ≥ 80%
- Verify all test cases pass

**Example Acceptance Criterion**:
```
- [ ] Password validation logic handles all edge cases
  【Unit Test】: Run `mvn test`, verify PasswordValidator tests pass
```

---

### 3. 【Build Verification】(Third Priority)

**USE FOR**: Structural requirements that **CANNOT** be verified through runtime or tests.

**Applicable Scenarios**:
- ✅ Module/component structure verification
- ✅ Dependency relationship verification
- ✅ Build configuration verification
- ✅ Code syntax correctness verification

**Verification Method**:
- Execute project's build command to ensure build success
- Check build logs for component order and dependencies
- Confirm build artifacts meet expectations

**Example Acceptance Criterion**:
```
- [ ] New module successfully integrated into build
  【Build Verification】: Run `mvn clean package`, verify build success
```

---

### 4. 【Static Check】(Last Resort) ⚠️

**ONLY USE IF**: **ABSOLUTELY NO** higher verification method is applicable.

**Applicable Scenarios**:
- ⚠️ File existence check
- ⚠️ Configuration file content check
- ⚠️ Directory structure check

**Verification Method**:
- Check if files exist
- Check if file content meets requirements

**Example Acceptance Criterion**:
```
- [ ] Configuration file created with correct format
  【Static Check】: Verify config.yml exists and contains required fields
```

---

## Verification Method Selection Rules

### Decision Tree

```
Can feature be tested by running the app?
├─ YES → 【Runtime Verification】 ✓
└─ NO → Can it be unit tested?
    ├─ YES → 【Unit Test】 ✓
    └─ NO → Does it affect build structure?
        ├─ YES → 【Build Verification】 ✓
        └─ NO → 【Static Check】 (last resort)
```

### If...Then Rules

- **If feature has API endpoint** → THEN **MUST** use 【Runtime Verification】
- **If feature has business logic** → THEN **MUST** use 【Unit Test】 (if runtime not possible)
- **If task adds new module** → THEN **MUST** use 【Build Verification】
- **If only file creation** → THEN can use 【Static Check】

**ABSOLUTELY PROHIBITED**:
- ❌ Using 【Static Check】 when 【Runtime Verification】 is possible
- ❌ Using 【Build Verification】 when 【Unit Test】 is possible
- ❌ Mixing verification levels inappropriately

**BEST PRACTICE**: One task can include multiple verification methods (e.g., 【Build Verification】 + 【Runtime Verification】)

## Three Levels of Task Description

**Core Principle**: Task description should be "what to do", not "how to do"

The goal of task breakdown is to transform design into executable objectives, not to write detailed coding instructions. Tasks should describe the result to be achieved, not step-by-step implementation details.

### 1. Goal Level (Recommended)

**Characteristics**: Describe result to be achieved, not specific implementation steps

**Applicable Scenarios**: Most tasks

**Advantages**:
- Give executor implementation space
- Focus on results not process
- Avoid excessive detail

### 2. Step Level (Use Cautiously)

**Characteristics**: List main steps, but don't involve specific code details

**Applicable Scenarios**: Complex tasks that need clear sub-steps

**Notes**:
- Steps should be high-level
- Avoid involving specific configuration items or code lines
- Stay at "what to do" level

### 3. Detail Level (Avoid Using)

**Characteristics**: Detail to every configuration item, every line of code

**Problem**: This is coding instruction, not task description, no different from direct coding

### Writing Acceptance Criteria

**Important**: Acceptance criteria should be detailed, but focus on "how to verify", not "how to implement"

**Characteristics**:
- Executable verification steps
- Clear checkpoints
- Specific expected results

### Practical Tips

#### 1. Use Action Verbs Like "Create", "Implement", "Configure" at Beginning

Recommend using clear action verbs to describe task goals.

#### 2. Avoid Detail Verbs Like "Add", "In..."

Avoid using too specific position descriptions and detail verbs.

#### 3. Focus on "What" Not "Where"

Task description should focus on what to create, not where to create.

#### 4. Acceptance Criteria Be Specific, But Task Description Be Abstract

Task description maintains abstraction level, acceptance criteria provides specific verification steps.

## Grasping Task Granularity

### Appropriate Task Granularity

**Single task should**:
- Can be completed in 1-4 hours
- Have clear start and end
- Can be independently verified
- Produce visible results

### Signs of Task Too Large

- Needs more than 4 hours to complete
- Contains multiple unrelated features
- Difficult to define acceptance criteria

**Solution**: Break into sub-tasks

### Signs of Task Too Small

- Only needs few minutes to complete
- Just one configuration item or line of code
- Cannot be independently verified

**Solution**: Merge into related tasks

## Common Problems and Mitigations

### Problem 1: Task Granularity Too Large

**Manifestation**: Task needs more than 4 hours to complete, contains multiple unrelated features, difficult to define acceptance criteria.

**Mitigation**:
- Break large task into multiple sub-tasks
- Each sub-task focuses on one clear goal
- Ensure each sub-task can be independently verified

### Problem 2: Task Granularity Too Small

**Manifestation**: Task only needs few minutes to complete, just one configuration item or line of code, cannot be independently verified.

**Mitigation**:
- Merge small tasks into related tasks
- As a step or acceptance point of larger task
- Maintain task independence and completeness

### Problem 3: Task Description Too Detailed

**Manifestation**: Task description contains specific code implementation, configuration details, reads like coding instruction.

**Mitigation**:
- Tasks should describe "what to do", not "how to do"
- Focus on goals and results, not implementation process
- Give executor implementation space
- Leave implementation details to coding phase

### Problem 4: Acceptance Criteria Not Clear

**Manifestation**: Acceptance criteria vague, cannot judge if task complete, lacks specific verification methods.

**Mitigation**:
- Use EARS syntax to write acceptance criteria
- Provide specific verification commands and steps
- Clarify expected output and results
- Specify verification method (runtime/build/static)

## Checklist

Before starting to execute tasks, confirm all items below:

- [ ] Have fully understood design document
- [ ] Have created complete task list
- [ ] Each task has clear goal and acceptance criteria
- [ ] Task description focuses on "what to do" not "how to do"
- [ ] Task granularity appropriate (1-4 hours completable)
- [ ] Dependencies between tasks clear
- [ ] Acceptance criteria specific, executable, testable
- [ ] Have conducted task list verification (consistency, executability, verifiability)
- [ ] Task list completely covers design content
- [ ] No tasks beyond design scope
- [ ] Have confirmed task list with user
- [ ] Have obtained user's explicit approval of task list

## Excellent Task Example

Below is a task example that conforms to best practices, demonstrating standard task format:

### Example: Create Graph Builder Core Functionality

**Task Description**:
- [x] 4. Create Graph Builder Core Functionality
  - Write GraphBuilder class using LangGraph technology to build collaboration graph
  - Implement team graph and supervisor graph building methods
  - Create node and edge definition logic
  - **Verification Method**: Test graph building success, verify node and edge correctness, check LangGraph compilation no errors
  - _Requirements: 1.1, 4.3_

**Example Analysis**:

1. **Task ID Clear**: Use simple numeric numbering (4.) for easy reference and tracking
2. **Goal Clear**: Create graph builder core functionality
3. **Key Points Listed**:
   - Use LangGraph technology to build collaboration graph
   - Implement team graph and supervisor graph building methods
   - Create node and edge definition logic
4. **Verification Method Clear**:
   - Test graph building success
   - Verify node and edge correctness
   - Check LangGraph compilation no errors
5. **Requirement Traceability**: Clearly associated with requirements 1.1 and 4.3

**Format Key Points**:
- Use `- [x]` or `- [ ]` to mark task status
- **Task numbering must use simple numeric format (like 1., 2., 3.), prohibited to use hierarchical format (like 1.1, 2.1, 3.1)**
- Key implementation points listed as sub-items
- **Verification Method** marked in bold, clearly explain how to verify
- Use `_Requirements:_` to mark requirement traceability relationship

**Task Numbering Standard**:
- ✅ **Correct Format**: `- [ ] 1. Create Maven Parent Project`
- ❌ **Wrong Format**: `- [ ] 1.1 Create Maven Parent Project`
- **Reason**: Kiro's task execution function requires simple numeric numbering, hierarchical numbering will cause execute button not to display

**Why This is Good Task**:
- ✅ Describes "what to do" not "how to do"
- ✅ Goal clear, scope explicit
- ✅ Verification criteria specific and executable
- ✅ Granularity appropriate (estimated 2-3 hours)
- ✅ Can be independently verified
- ✅ Traceable to requirements

## Golden Rules of Task Breakdown

1. **Task Description Be Abstract**: State what goal to achieve, not how to implement
2. **Acceptance Criteria Be Specific**: State how to verify goal achieved, provide specific steps
3. **Avoid Coding Instructions**: Don't tell executor how to write code line by line
4. **Focus on Results Not Process**: Describe what to produce, not how to produce
5. **Maintain Appropriate Granularity**: 1-4 hours completable, independently verifiable

## Key Benefits

Following task breakdown phase best practices can:

- ✅ Ensure task executability, reduce implementation risk
- ✅ Improve development efficiency and quality
- ✅ Facilitate progress tracking and risk control
- ✅ Support parallel development and incremental delivery
- ✅ Ensure deliverable verifiability

**Remember**: Tasks are goal guidance for developers, not coding instructions for AI. If task description reads like writing code, it means too detailed.
