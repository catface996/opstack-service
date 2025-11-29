---
inclusion: manual
---

# General Best Practices

This document defines universal principles and best practices that **MUST** be followed across all Spec development phases.

---

## Quick Reference: Core Principles

| Principle | Key Requirement | Consequence of Violation |
|-----------|----------------|--------------------------|
| Progressive Development | MUST pass validation at each phase | ❌ 10-20x higher fix cost later |
| Continuous Validation | MUST verify before proceeding | ❌ Issues compound exponentially |
| Proactive Communication | MUST ask when uncertain | ❌ Understanding deviations |

---

## Phase -1: Pre-Implementation Gates (NON-NEGOTIABLE)

*GATE: Must pass before starting any phase.*

### Context Check
- [ ] Read relevant requirements/design documents?
- [ ] Confirmed current phase?
- [ ] Identified uncertain issues?

### Language Standards Check
- [ ] All outputs in Chinese?
- [ ] Technical terms in English (API, JSON, Maven, etc.)?

**If check fails**: STOP, supplement missing context first.

---

## Language Usage Guidelines

### Mandatory Requirements (MUST)

| Scenario | Language | Example |
|----------|----------|---------|
| Conversations | Chinese | ✅ "我建议使用分层架构" |
| Documentation | Chinese | ✅ "需求：用户登录功能" |
| Code Comments | Chinese | ✅ `// 验证用户密码` |
| Code Itself | English | ✅ `validatePassword()` |
| Technical Terms | English | ✅ API, JSON, Maven |
| EARS Keywords | UPPERCASE | ✅ THE, SHALL, WHEN |

### Correct vs Incorrect Examples

✅ **Correct**:
```
用户可以通过 REST API 提交订单，System SHALL respond within 2 seconds.
```

❌ **Incorrect**:
```
User can submit order via REST API, 系统 shall respond within 2s.
```

---

## Core Concept

**Why These Practices Are Needed**: LLMs are probabilistic models. Through progressive development, multiple validations, and continuous feedback, output quality can be significantly improved and rework costs reduced (fix costs are 10-20x higher if discovered late).

---

## Three Core Principles (NON-NEGOTIABLE)

### 1. Progressive Development

**Key Concept**: Progress in phases. Each phase **MUST** pass validation before proceeding.

**Workflow**:
```
Requirements → Validation ✓ → Design → Validation ✓ → Task Breakdown → Validation ✓ → Execution
       ↑                ↑                    ↑
    GATE 1           GATE 2              GATE 3
```

**Gate Control (GATE)**:
| Gate | Checkpoint | Pass Criteria |
|------|------------|---------------|
| GATE 1 | Requirements Complete | User confirms understanding |
| GATE 2 | Design Complete | User approves solution |
| GATE 3 | Task Breakdown | Tasks executable & verifiable |

**ABSOLUTELY PROHIBITED**:
- ❌ Skip any GATE
- ❌ Proceed without confirmation
- ❌ Complete all phases at once

**Why It Matters**:
- Discover issues early (fix cost is 10-20x lower)
- Avoid development based on incorrect assumptions
- Ensure output quality at each phase

### 2. Continuous Validation

**Key Concept**: Each phase **MUST** undergo multi-dimensional validation.

**Validation Matrix**:

| Dimension | Check Question | Validation Method |
|-----------|----------------|-------------------|
| Consistency | Consistent with previous phase? | Item-by-item comparison |
| Completeness | Fully covers requirements? | Requirements traceability |
| Accuracy | Understanding correct? | User confirmation |
| Reasonableness | Over-engineering present? | Simplicity check |

**Validation Loop**:
```
Output → Self-check → Issues Found?
                         ↓ Yes
                    Fix Issues
                         ↓ No
              → User Confirmation → Pass ✓
```

**Validation Methods**:
- **Self-check**: Review item by item against previous phase documents
- **Cross-validation**: Check internal consistency
- **User confirmation**: Critical decisions **MUST** be approved by users

### 3. Proactive Communication

**Key Concept**: When uncertain, **MUST** communicate with users. **NEVER** make assumptions.

**Situations Requiring Confirmation**:

| Situation | Action Required | Risk of Not Asking |
|-----------|----------------|-------------------|
| Requirements unclear | Ask for clarification | ❌ Wrong implementation |
| Multiple design options | Present alternatives | ❌ Suboptimal choice |
| Potential over-engineering | Seek confirmation | ❌ Wasted effort |
| Task granularity uncertain | Discuss breakdown | ❌ Unexecutable tasks |
| Technical trade-offs | Explain pros/cons | ❌ Hidden issues |

**Communication Principles**:
- ✅ **Transparency**: Report progress and issues promptly
- ✅ **Specificity**: Clearly express questions and suggestions
- ✅ **Respect**: Take user feedback seriously
- ❌ **NEVER assume**: "I think user wants..." → Ask instead!

## Quality Assurance Checklist

After completing each phase, **MUST** use the following checklist for self-inspection:

### Completeness Check
- [ ] All requirements covered?
- [ ] No missing content?
- [ ] Edge cases considered?

### Consistency Check
- [ ] Consistent with previous phase outputs?
- [ ] Internal parts consistent?
- [ ] Terminology usage consistent?

### Accuracy Check
- [ ] Requirements understanding accurate?
- [ ] Technical solution description accurate?
- [ ] No ambiguous statements?

### Feasibility Check
- [ ] Design implementable?
- [ ] Tasks executable?
- [ ] Validation criteria actionable?

### Reasonableness Check
- [ ] No over-engineering?
- [ ] No unnecessary complexity?
- [ ] Complies with project constraints?

---

## Quality Scoring Matrix

Use this matrix to evaluate each phase output:

| Grade | Score | Criteria | Action |
|-------|-------|----------|--------|
| Excellent | ≥ 90 | All checklist items pass, user satisfied | ✅ Proceed to next phase |
| Good | ≥ 80 | Minor issues, quick fixes possible | ⚠️ Fix issues, then proceed |
| Pass | ≥ 70 | Some issues, requires improvement | ⚠️ **MUST** improve before proceeding |
| Fail | < 70 | Major issues, incomplete work | ❌ **STOP** - Major rework required |

**Scoring Guidelines**:
- Each failed checklist item: -5 points
- Each "partially met" item: -3 points
- User-identified issues: -10 points each
- Missing critical content: -20 points

**If score < 90**: **MUST** document issues and improvement plan.

## Documentation Standards

High-quality documentation is the foundation of success. Follow these standards when writing all Spec documents:

### Clarity
- Use concise and clear language
- Avoid vague and ambiguous expressions
- Provide necessary context
- Use concrete examples to illustrate abstract concepts

### Structure
- Use clear hierarchy (headings, lists, paragraphs)
- Organize content reasonably (from overview to details)
- Facilitate quick finding and understanding
- Maintain format consistency

### Traceability
- Clearly mark sources and basis
- Maintain association with original requirements
- Record important decisions and rationale
- Use references to link related content

## Common Pitfalls and Countermeasures

Understanding common pitfalls can help you avoid repeating mistakes.

### Pitfall 1: Premature Optimization
**Manifestation**: Starting design when requirements are unclear, starting coding when design is incomplete.

**Consequences**: ❌ Developing based on incorrect assumptions → 10-20x rework cost

**Countermeasures**:
- ✅ **STRICTLY** follow progressive development process
- ✅ **MUST** validate each phase before entering the next
- ✅ Ensure correctness first, then consider performance

**If...then logic**:
- **If requirements unclear** → THEN **STOP** and clarify requirements
- **If design incomplete** → THEN **STOP** and complete design
- **If validation skipped** → THEN **GO BACK** and validate

### Pitfall 2: Over-Engineering
**Manifestation**: Designing complex architectures for potential future needs, implementing currently unnecessary features.

**Consequences**: ❌ Increased complexity → Extended timeline → Reduced maintainability

**Countermeasures**:
- ✅ Only implement currently needed features (YAGNI principle)
- ✅ Keep design simple (KISS principle)
- ✅ Reserve extension points but **DON'T** implement in advance

**If...then logic**:
- **If feature not in requirements** → THEN **ASK** user before implementing
- **If complexity high** → THEN question if it's needed now
- **If "maybe we'll need this"** → THEN **DON'T** implement yet

### Pitfall 3: Ignoring Validation
**Manifestation**: Skipping validation steps, assuming everything is correct.

**Consequences**: ❌ Problems compound → Fix costs grow exponentially (10-100x)

**Countermeasures**:
- ✅ **MANDATORY** validation at each phase
- ✅ Use checklists to ensure nothing is missed
- ✅ Discover and fix issues **IMMEDIATELY**

**If...then logic**:
- **If validation skipped** → THEN **STOP** and validate now
- **If issue found** → THEN fix before proceeding
- **If unsure if valid** → THEN ask user

### Pitfall 4: Insufficient Communication
**Manifestation**: Making assumptions about user intent, hiding problems and risks.

**Consequences**: ❌ Understanding deviations → Deliverables don't meet expectations → Rework

**Countermeasures**:
- ✅ **MUST** ask when encountering uncertainty
- ✅ Regularly sync progress and issues
- ✅ Critical decisions **STRICTLY REQUIRE** user confirmation

**If...then logic**:
- **If uncertain** → THEN **ASK** immediately, **DON'T** assume
- **If risky decision** → THEN discuss with user first
- **If assumption made** → THEN validate with user

## Efficiency Improvement Strategies

### Front-Load Investment to Avoid Rework
- Thoroughly validate upfront to avoid major changes later
- Discover issues early to reduce fix costs
- Maintain good documentation and code quality

### Reuse Experience and Patterns
- Reference successful experiences from similar projects
- Use mature design patterns and architectures
- Build reusable components and templates

### Automate Validation
- Use automated testing to validate functionality
- Use continuous integration to discover issues early
- Use code quality checking tools

## Risk Management

### Identify Risks
- **Technical Risks**: Technology selection, implementation difficulty, performance bottlenecks
- **Requirement Risks**: Requirement changes, understanding deviations, scope creep
- **Schedule Risks**: Time estimation, dependencies, resource constraints

### Response Strategies
- Develop alternative plans for high-risk items
- Reserve appropriate buffer time
- Establish risk monitoring and early warning mechanisms
- Adjust plans and strategies promptly

## Continuous Improvement

### Regular Reviews
- Review project execution
- Summarize successful experiences and lessons learned
- Identify improvement opportunities

### Update Practices
- Update guidelines based on practical experience
- Share valuable discoveries
- Continuously optimize workflows

### Cultivate Habits
- Develop the habit of validation
- Develop the habit of communication
- Develop the habit of documentation

## Summary

Following these general best practices can:
- ✅ Improve delivery quality
- ✅ Reduce rework costs
- ✅ Enhance development efficiency
- ✅ Reduce communication costs
- ✅ Accumulate reusable experience

Remember: **Progressive Development, Continuous Validation, Proactive Communication** are the three pillars of success.
