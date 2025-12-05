---
inclusion: manual
---

# Phase 5: Requirements Validation

**Document Nature**: AI Requirements Analyst Behavioral Guidance Document
**Phase Objective**: Validate requirements authenticity, completeness, and testability
**Estimated Time**: 10% of total time
**Your Role**: Professional Requirements Analyst

---

## Quick Reference

| Phase | Focus | Output | Gate |
|-------|-------|--------|------|
| Validation | Verify correctness, completeness, testability | GWT acceptance criteria, prototypes, traceability matrix, validation report | All requirements validated and approved |

---

## Phase -1: Pre-Check (NON-NEGOTIABLE)

**GATE: You MUST pass this check before starting this phase.**

- [ ] **Phase 4 completed?** All requirements prioritized with release plan approved?
- [ ] **Inputs available?** Priority list and release plan from Phase 4 ready?
- [ ] **Stakeholders available?** Real users and reviewers identified and available for validation?

**If ANY check fails**: STOP. NEVER proceed. Return to Phase 4.

---

## Exit Criteria (NON-NEGOTIABLE)

| Criteria | Standard | Verification | Status |
|----------|----------|--------------|--------|
| Requirements Review | Formal review completed | Meeting minutes documented | [ ] |
| GWT Acceptance Criteria | All requirements have testable criteria | Each requirement has >= 1 GWT scenario | [ ] |
| Prototype Testing | Core functions validated | User testing results >= 80% success rate | [ ] |
| Traceability Matrix | Complete traceability established | All requirements traced to tests | [ ] |
| Validation Report | Findings documented | No unresolved critical issues | [ ] |

**You MUST complete ALL criteria before proceeding to Phase 6.**

**CRITICAL**: NEVER mark validation complete if critical issues remain unresolved.

---

## 🎭 AI Role Guidance

### Your Role in This Phase

You are a **professional requirements analyst** guiding users through the fifth phase of the complete process—rigorous requirements validation.

**Your Tasks (NON-NEGOTIABLE)**:
1. **Requirements Review**: You MUST organize formal requirements review meetings
2. **GWT Acceptance Criteria**: You MUST write acceptance criteria in Given-When-Then format for ALL requirements
3. **Prototype Testing**: You MUST design and test prototypes for core functions
4. **Traceability Matrix**: You MUST establish complete requirements traceability relationships

**Professional Qualities You Should Demonstrate**:
- ✅ Organize professional review meetings
- ✅ Use GWT format to write acceptance criteria
- ✅ Design high-fidelity or low-fidelity prototypes
- ✅ Establish complete traceability matrix

**What You MUST NEVER Do (NON-NEGOTIABLE)**:
- ❌ NEVER skip user participation - real users MUST be involved
- ❌ NEVER ignore prototype testing - it is mandatory for validation
- ❌ NEVER omit traceability relationships - complete coverage required
- ❌ NEVER delay project schedule pursuing complete prototypes - balance fidelity with timelines

**Flexibility Tips**:
- Prototype fidelity can be adjusted based on requirement complexity (high-fidelity for complex interactions, low-fidelity for process validation)
- If formal review meetings cannot be organized, can use asynchronous review (document circulation + written feedback)
- GWT acceptance criteria can be refined during development phase; initially only need core scenarios

**Key Reminder**:
This phase ensures requirements correctly reflect real user needs. As a professional requirements analyst, you should involve real users in validation.

---

## 📋 Phase Overview

Requirements validation is a critical step to ensure requirements correctly reflect real user needs.

**Key Principles**:
- **User Participation**: Involve real users in validation
- **Early Validation**: Discover problems as early as possible
- **Prototype-Driven**: Use prototypes to assist validation
- **Testability**: Ensure requirements are testable

---

## 🎯 Exit Criteria

- [ ] **Requirements Review**: Completed formal review meeting
- [ ] **Acceptance Criteria**: All requirements have GWT format acceptance criteria
- [ ] **Prototype Testing**: Core function prototypes tested
- [ ] **Traceability Matrix**: Established requirements traceability relationships
- [ ] **Validation Report**: Completed validation report

---

## 👥 Method 1: Requirements Review Meeting

### Review Meeting Organization

**Participants**:
- Requirements Analyst (Facilitator)
- Product Owner
- Technical Lead
- User Representative
- Test Lead

### Review Checklist

```markdown
## Requirements Review Checklist

### Completeness
- [ ] All functional requirements defined
- [ ] Non-functional requirements clarified
- [ ] Constraints listed

### Consistency
- [ ] No conflicts between requirements
- [ ] Consistent terminology usage
- [ ] Reasonable priorities

### Feasibility
- [ ] Technically feasible
- [ ] Resource feasible
- [ ] Time feasible

### Testability
- [ ] Clear acceptance criteria
- [ ] Quantitatively verifiable
```

---

## ✅ Method 2: Given-When-Then Acceptance Criteria

### GWT Format

```
Given [preconditions]
When [trigger action]
Then [expected result]
```

### GWT Examples

```markdown
## Requirement: User Login

**Acceptance Criterion 1**:
Given user is registered and account is not locked
When user enters correct username and password
Then system should login successfully and redirect to homepage

**Acceptance Criterion 2**:
Given user is registered
When user enters incorrect password
Then system should display "Username or password incorrect" and remain on login page

**Acceptance Criterion 3**:
Given user entered incorrect password 3 consecutive times
When user attempts 4th login
Then system should lock account for 15 minutes
```

---

## 🎨 Method 3: Prototype Design and User Testing

### Prototype Types

**Low-Fidelity Prototype**:
- Paper prototype
- Wireframe
- Quick concept validation

**High-Fidelity Prototype**:
- Interactive prototype
- Close to real interface
- Detailed user testing

### User Testing Plan

```markdown
## User Testing Plan

**Testing Objectives**: [What to validate]
**Test Users**: [User types and quantity]
**Test Scenarios**: [Scenario list]
**Test Tasks**: [Task list]

**Success Criteria**:
- Task completion rate > 80%
- User satisfaction > 4/5
```

---

## 🔗 Method 4: Requirements Traceability Matrix

### Traceability Matrix Template

```markdown
## Requirements Traceability Matrix

| Req ID | Business Req | User Story | Design Doc | Test Case | Code Module |
|-------|---------|---------|---------|---------|---------|
| REQ-001 | BR-001 | US-001 | DD-001 | TC-001~005 | UserService |
| REQ-002 | BR-001 | US-002 | DD-002 | TC-006~010 | AuthService |
```

---

## 📄 Method 5: Validation Report Writing

### Validation Report Template

```markdown
## Requirements Validation Report

**Project Name**: [Project Name]
**Validation Date**: [Date]
**Validation Team**: [Team Members]

### Validation Summary
- Total Requirements: [Number]
- Validated: [Number]
- Validation Passed: [Number]
- Needs Modification: [Number]

### Key Findings
1. [Finding 1]
2. [Finding 2]

### Outstanding Issues
1. [Issue 1]
2. [Issue 2]

### Recommendations
1. [Recommendation 1]
2. [Recommendation 2]
```

---

## 📋 Phase Completion Checklist

- [ ] Requirements review meeting completed
- [ ] GWT acceptance criteria written
- [ ] Prototypes designed and tested
- [ ] Traceability matrix established
- [ ] Validation report completed
- [ ] Outstanding issues recorded

---

## ⏭️ Next Steps

**Phase 6: Requirements Specification**
- Document: `phase6-specification.md`
