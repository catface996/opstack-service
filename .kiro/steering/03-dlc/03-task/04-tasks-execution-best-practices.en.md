---
inclusion: manual
---

# Task Execution Phase Best Practices

---

## Quick Reference: Execution Checklist

| Stage | Must Complete | Gate |
|-------|--------------|------|
| Before Start | Understand task + confirm preconditions | ✓ |
| During Implementation | Follow code standards + progressive development | - |
| After Implementation | Verify acceptance criteria | ✓ |
| After Verification | **Build must succeed** | ✓ MANDATORY |
| After Build | Consistency check with requirements/design | ✓ |

**IRON RULE #1**: After each task, **PROJECT MUST BUILD SUCCESSFULLY**
**IRON RULE #2**: After each task, **MUST CHECK CONSISTENCY** with requirements and design

---

## Phase -1: Pre-Execution Gates (NON-NEGOTIABLE)

*GATE: Must pass before executing tasks.*

### Task List Readiness Check
- [ ] Task list exists at `.kiro/features/{feature-id}/tasks.md`?
- [ ] Task list validated against requirements and design?
- [ ] Task dependencies clearly mapped?
- [ ] All acceptance criteria include verification methods?

### Environment Readiness Check
- [ ] Development environment set up and working?
- [ ] Project builds successfully in current state?
- [ ] All prerequisite tasks completed?
- [ ] Understand the task's acceptance criteria?

**If check fails**: STOP, resolve issues before starting execution.

---

## Phase Goal

Execute tasks one by one according to task list, transform design into **RUNNABLE CODE**, ensure project is in **HEALTHY STATE** after each task completion.

## Why Task Execution Phase is So Important

Task execution is a **CRITICAL** phase for turning design into reality. Good execution practices can:
- ✅ Ensure project **CONTINUOUS HEALTHY STATE**
- ✅ Discover and solve problems **EARLY**
- ✅ Improve code quality and maintainability
- ✅ Support team collaborative development
- ✅ Reduce integration and deployment risks

**Core Principle (NON-NEGOTIABLE)**: After each task completion, project **MUST SUCCESSFULLY BUILD**.

## Task Execution Workflow

### Step 1: Understand Task

Before starting to execute task, fully understand task requirements.

**Key Activities**:
- Carefully read task description and acceptance criteria
- Understand task goal and expected output
- Confirm preconditions are met
- Identify possible technical difficulties

### Step 2: Implement Functionality

Implement functionality according to task description.

**Implementation Principles**:

#### 1. Keep Project Continuously Buildable (MANDATORY)

**Key Requirement**: After each task completion, entire project **MUST** be successfully buildable.

**Why Important**:
- ✅ Project always in **RUNNABLE STATE**
- ✅ Discover integration problems **IMMEDIATELY**
- ✅ Support continuous integration and continuous delivery
- ✅ Reduce later integration risk (cost savings: 10-50x)

**How to Achieve**:
- ✅ Run build command **IMMEDIATELY** after completing task
- ✅ Ensure build success **BEFORE** entering next task
- ✅ Fix **IMMEDIATELY** when encountering build failure

**If...Then Rules**:
- **If build fails** → THEN **STOP** everything, fix before proceeding
- **If build succeeds** → THEN proceed to verification
- **If uncertain about build** → THEN **BUILD AND CHECK**

**ABSOLUTELY PROHIBITED**:
- ❌ Moving to next task without building
- ❌ Assuming "it will probably build"
- ❌ Accumulating multiple tasks before building

#### 2. Progressive Development

**Key Idea**: In multi-module or multi-component projects, adopt progressive development strategy, avoid declaring modules or components not yet created.

**Basic Principles**:
- Only declare already created components
- Update configuration synchronously when creating components
- Same principle applies to multi-level structures

**Why Important**:
- Avoid build errors
- Keep configuration synchronized with actual structure
- Support incremental development

#### 3. Follow Code Quality Standards

**Coding Standards**:
- Follow project unified code style
- Use meaningful variable and method naming
- Add necessary comments and documentation

**Error Handling**:
- Implement unified exception handling mechanism
- Provide clear error messages
- Avoid exception messages leaking sensitive data

**Testing Requirements**:
- Write unit tests for core business logic (if task requires)
- Ensure test coverage reaches reasonable level
- Integration tests verify module collaboration

### Step 3: Verify Task

After task implementation complete, verify according to acceptance criteria.

**Verification Priority**:

#### 1. Runtime Verification (Highest Priority)

Features that can be verified by actually running application must be verified by running application.

**Applicable Scenarios**:
- Configuration verification (multi-environment, feature toggles, etc.)
- API endpoint functionality verification
- Log output format verification
- Exception handling verification
- Integration functionality verification
- External service connection verification

**Verification Method**:
- Start application (using project's startup command)
- Access relevant endpoints or trigger relevant functionality
- Check actual runtime results meet expectations

#### 2. Build Verification (Second Priority)

For structural requirements that cannot be verified through runtime, verify through project build.

**Applicable Scenarios**:
- Module/component structure verification
- Dependency relationship verification
- Build configuration verification
- Code syntax correctness verification

**Verification Method**:
- Execute project's build command
- Check build logs confirm no errors
- Confirm all components built in correct order

#### 3. Static Check (Last Resort)

Only use static file check when cannot verify through above two methods.

**Applicable Scenarios**:
- File existence check
- Configuration file content check
- Directory structure check

### Step 4: Task Completion Confirmation

Confirm task is completely done, meets all acceptance criteria.

**Check Points**:
- All acceptance criteria passed
- Project can build successfully
- Code quality meets standards
- Related documentation updated (if needed)

### Step 5: Requirements and Design Consistency Check (MANDATORY)

**Key**: After task verification passes, **MUST** conduct requirements and design consistency check to ensure implementation conforms to original intent.

**Why Important**:
- ⚠️ Task may pass acceptance criteria but **DEVIATE** from requirement intent
- ⚠️ Implementation may **VIOLATE** design's architecture principles
- ✅ Discover deviations early, avoid accumulating technical debt
- ✅ Ensure overall solution consistency and completeness

---

## Consistency Check Matrix

| Check Type | Questions to Answer | Action if Deviation Found |
|-----------|---------------------|---------------------------|
| Requirements | Does it meet requirement intent? | ❌ **STOP** - Fix or clarify |
| Design | Does it follow architecture? | ❌ **STOP** - Fix or update design |
| Scope | Any over-engineering? | ⚠️ Discuss with user |
| Quality | Meets non-functional requirements? | ⚠️ Improve if needed |

---

#### 1. Requirements Consistency Check

**MUST** verify whether task implementation truly meets corresponding requirements.

**Mandatory Check Points**:
- [ ] What are task-related requirements? (Check `_Requirements:_` marking in task)
- [ ] Does implementation **COMPLETELY** cover all acceptance criteria of requirements?
- [ ] Does implementation **ACCURATELY** understand requirement intent?
- [ ] Is there implementation **BEYOND** requirement scope (over-design)?
- [ ] Are there **MISSING** requirement points?

**Verification Method**:
1. Open requirements document (`.kiro/features/{feature-id}/spec.md`)
2. Find corresponding requirement item by number
3. Check **EACH** requirement acceptance criterion
4. Confirm implementation meets **EVERY** acceptance criterion
5. If deviation exists, record reason and assess impact

**If...Then Rules**:
- **If requirement not met** → THEN **STOP** and fix implementation
- **If over-engineered** → THEN discuss with user, possibly remove
- **If requirement missing** → THEN add to task list

---

#### 2. Design Consistency Check

**MUST** verify whether task implementation follows architecture and technical solution in design document.

**Mandatory Check Points**:
- [ ] Does implementation follow **ARCHITECTURE PATTERN** in design document?
- [ ] Do **MODULE DIVISION** and responsibility boundaries conform to design?
- [ ] Are **INTERFACE DEFINITIONS** consistent with design?
- [ ] Does **DATA MODEL** conform to design specification?
- [ ] Is **TECHNOLOGY SELECTION** consistent with design?
- [ ] Does it follow **NON-FUNCTIONAL REQUIREMENTS** in design (performance, security, etc.)?

**Verification Method**:
1. Open design document (`.kiro/features/{feature-id}/plan.md`)
2. Find related design chapter
3. Compare with architecture diagrams, module division, interface definitions in design
4. Confirm implementation conforms to design specifications
5. If deviation exists, assess whether there's reasonable reason

**If...Then Rules**:
- **If architecture violated** → THEN **STOP** and refactor
- **If interface mismatch** → THEN fix implementation or update design
- **If technology differs** → THEN must have documented ADR justification

---

#### 3. Deviation Handling (Decision Matrix)

| Deviation Type | Impact | Action Required | Who Decides |
|---------------|--------|-----------------|-------------|
| **Minor Deviation** | Low impact on functionality/architecture | Record + assess + possibly create follow-up task | AI can decide |
| **Major Deviation** | Affects core functionality/architecture | **STOP** all tasks + communicate with user | User **MUST** decide |
| **Reasonable Deviation** | Improvement with sufficient reason | Document + update design + confirm with user | User confirms |

**Minor Deviation** (doesn't affect core functionality and architecture):
- ✅ Record deviation reason and impact
- ✅ Assess whether adjustment needed
- ✅ If adjustment needed, create follow-up task

**Major Deviation** (affects core functionality or architecture):
- ❌ **IMMEDIATELY STOP** all subsequent tasks
- ❌ Communicate deviation situation with user
- ❌ User determines whether to correct implementation or update requirements/design
- ✅ Re-verify after correction

**Reasonable Deviation** (improvement with sufficient reason):
- ✅ Record deviation reason and benefits
- ✅ Update design document (if needed)
- ✅ **MUST** confirm changes with user
- ✅ Assess impact on subsequent tasks

---

#### 4. Self-check Prompt

**MANDATORY**: Use following prompt for self-check after **EVERY** task:

> "Please check whether just completed task implementation meets corresponding requirement (requirement number: X.X), whether it follows architecture and technical solution in design document. If deviation exists, please explain reason."

**Check Process (Step-by-Step)**:
1. ✓ Find task-related requirement number from `_Requirements:_` tag
2. ✓ Open requirements document (`.kiro/features/{feature-id}/spec.md`)
3. ✓ Locate corresponding requirement
4. ✓ Check **EACH** requirement acceptance criterion is met
5. ✓ Open design document (`.kiro/features/{feature-id}/plan.md`)
6. ✓ Find related design chapter
7. ✓ Check whether implementation conforms to design specification
8. ✓ Record check results and found problems

**If any check fails**: **MUST** document and address before proceeding to next task.

## Progressive Development Implementation Steps

### 1. Confirm Current State
Check configuration file only contains already created components

### 2. Create New Component
Create component directory and necessary configuration files

### 3. Update Configuration
Add new component declaration in related configuration files

### 4. Verify Build
Run project's build command to ensure build success

## Common Problems and Mitigations

### Problem 1: Build Failure

**Manifestation**: Errors appear after executing build command, project cannot build successfully.

**Common Causes**:
- Component declaration inconsistent with actual structure
- Dependency relationship configuration error
- Language/framework version incompatible
- Code syntax error

**Mitigation**:
- Check whether component declaration correct
- Confirm dependency versions compatible
- Check code syntax errors
- Review build logs to locate specific problem

### Problem 2: Runtime Error

**Manifestation**: Build succeeds but runtime errors occur.

**Common Causes**:
- Configuration file format error
- External service connection configuration error
- Port occupied
- Resource files missing

**Mitigation**:
- Check configuration file format and content
- Verify external service connection configuration
- Confirm port not occupied
- Check resource files exist

### Problem 3: Integration Issues

**Manifestation**: Individual components normal, but component collaboration has problems.

**Common Causes**:
- Component interface inconsistent
- Data format incompatible
- Boundary and responsibility division unclear

**Mitigation**:
- Verify component interface consistency
- Check data format compatibility
- Confirm boundary and responsibility division
- Write integration tests to verify

### Problem 4: Verification Criteria Not Clear

**Manifestation**: Uncertain how to verify task completion.

**Mitigation**:
- Review task's acceptance criteria
- Choose verification method according to verification priority (runtime > build > static)
- If uncertain, confirm verification method with user

## Task Completion Checklist

Before marking task as complete, confirm all items below:

### Basic Check
- [ ] Have fully understood task requirements and acceptance criteria
- [ ] Code implemented to correct location
- [ ] Related configuration files updated
- [ ] Project can build successfully
- [ ] No build errors or warnings
- [ ] Component declarations consistent with actual directory structure
- [ ] Dependency relationships configured correctly
- [ ] Code meets quality standards (coding standards, error handling)

### Verification Check
- [ ] **If functionality can runtime verify, verified by running application**
- [ ] **If structural change, verified through build**
- [ ] All acceptance criteria passed
- [ ] Related documentation updated (if needed)

### Consistency Check (Important)
- [ ] **Found task-related requirement number**
- [ ] **Checked against requirements document, confirmed implementation meets all requirement acceptance criteria**
- [ ] **Checked against design document, confirmed implementation follows architecture and technical solution**
- [ ] **If deviation exists, recorded reason and assessed impact**
- [ ] **Major deviations communicated and confirmed with user**

## Key Benefits

Following task execution phase best practices can:

- ✅ Ensure project continuous healthy state
- ✅ Discover and solve problems early
- ✅ Improve code quality and maintainability
- ✅ Support team collaborative development
- ✅ Reduce integration and deployment risks
- ✅ Facilitate continuous integration and automated testing
- ✅ **Ensure implementation consistent with requirements and design**
- ✅ **Avoid accumulating technical debt and architecture deviation**

**Remember**:
1. After each task completion, project must successfully build
2. After each task verification passes, must check consistency with requirements and design

These are the two iron rules of task execution phase.
