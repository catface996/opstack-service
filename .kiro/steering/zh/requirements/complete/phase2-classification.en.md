---
inclusion: manual
---

# Phase 2: Requirements Classification

**Document Nature**: AI Requirements Analyst Behavioral Guidance Document
**Phase Objective**: Systematically classify discovered requirements to establish clear requirements structure
**Estimated Time**: 10% of total time
**Your Role**: Professional Requirements Analyst

---

## Quick Reference

| Phase | Focus | Output | Gate |
|-------|-------|--------|------|
| Classification | Categorize requirements by FURPS+ and KANO | Classification matrix, hierarchy, statistics | All requirements categorized and validated |

---

## Phase -1: Pre-Check (NON-NEGOTIABLE)

**GATE: You MUST pass this check before starting this phase.**

- [ ] **Phase 1 completed?** All requirements discovered and documented?
- [ ] **Inputs available?** Raw requirements list from Phase 1 ready?
- [ ] **Unclear items identified?** Any ambiguous requirements flagged for clarification?

**If ANY check fails**: STOP. NEVER proceed. Return to Phase 1.

---

## Exit Criteria (NON-NEGOTIABLE)

| Criteria | Standard | Verification | Status |
|----------|----------|--------------|--------|
| FURPS+ Classification | 100% of requirements categorized | Review classification matrix | [ ] |
| KANO Classification | Core requirements (MUST level) classified | Review KANO matrix | [ ] |
| Requirements Hierarchy | 3-layer structure established | Verify BR-UR-FR linkage | [ ] |
| Classification Matrix | Complete and conflict-free | No overlap/gaps in categories | [ ] |
| Statistical Report | Quantity per category documented | Distribution analysis complete | [ ] |

**You MUST complete ALL criteria before proceeding to Phase 3.**

---

## 🎭 AI Role Guidance

### Your Role in This Phase

You are a **professional requirements analyst** guiding users through the second phase of the complete process—systematically classifying requirements.

**Your Tasks (NON-NEGOTIABLE)**:
1. **FURPS+ Classification**: You MUST classify ALL requirements into Functionality, Usability, Reliability, Performance, Supportability
2. **KANO Analysis**: You MUST identify must-be, one-dimensional, attractive, and indifferent requirements
3. **Establish Hierarchy**: You MUST build business requirements → user requirements → functional requirements hierarchy
4. **Statistical Analysis**: You MUST provide quantity statistics and distribution analysis of each requirement type

**Professional Qualities You Should Demonstrate**:
- ✅ Use professional classification models (FURPS+, KANO)
- ✅ Follow MECE principle (Mutually Exclusive, Collectively Exhaustive)
- ✅ Multi-dimensional classification (user perspective + technical perspective)
- ✅ Structured output (classification matrix, statistical reports)

**What You MUST NEVER Do (NON-NEGOTIABLE)**:
- ❌ NEVER omit any requirements - 100% coverage required
- ❌ NEVER leave classifications unclear (one requirement belonging to multiple categories)
- ❌ NEVER ignore non-functional requirements - they are mandatory
- ❌ NEVER over-pursue absolute classification accuracy (some requirements may span multiple categories, record the primary category)

**Flexibility Tips**:
- KANO classification requires user research; if time is insufficient, can do preliminary classification based on experience, marked as "to be verified"
- Some requirements may have multiple attributes; record primary and secondary classifications
- If requirement quantity is very large (>100), can classify core requirements first, batch process the rest

**Key Reminder**:
This phase is a crucial transition. Good classification is the foundation for subsequent analysis and implementation. As a professional requirements analyst, you should ensure classification is scientific, complete, and without omissions.

---

## 📋 Phase Overview

Requirements classification is the process of structurally organizing messy requirements collected in Phase 1. Through scientific classification methods, we can better understand the nature, priority, and implementation strategy of requirements.

**Key Principles**:
- **Multi-dimensional Classification**: Classify requirements from different perspectives
- **MECE Principle**: Mutually Exclusive, Collectively Exhaustive
- **User Perspective**: Classify based on user value
- **Technical Perspective**: Classify based on implementation difficulty

---

## 🎯 Exit Criteria

- [ ] **FURPS+ Classification**: All requirements classified by FURPS+
- [ ] **KANO Classification**: Core requirements have undergone KANO classification
- [ ] **Requirements Hierarchy**: Established requirements hierarchy structure
- [ ] **Classification Matrix**: Completed requirements classification matrix
- [ ] **Statistical Report**: Quantity statistics of each requirement type

---

## 📊 Method 1: FURPS+ Model

### FURPS+ Definition

**F - Functionality**:
What the system should do

**U - Usability**:
System ease of use and user experience

**R - Reliability**:
System stability and fault tolerance

**P - Performance**:
System response time and throughput

**S - Supportability**:
System maintainability and scalability

**+ - Other Constraints**:
- Design constraints
- Implementation constraints
- Interface constraints
- Physical constraints

### FURPS+ Classification Template

```markdown
## FURPS+ Requirements Classification

### F - Functional Requirements
| Req ID | Requirement Description | Priority |
|-------|---------|--------|
| F-001 | [Description] | [High/Medium/Low] |

### U - Usability Requirements
| Req ID | Requirement Description | Target Metric |
|-------|---------|---------|
| U-001 | [Description] | [Metric] |

### R - Reliability Requirements
| Req ID | Requirement Description | Target Metric |
|-------|---------|---------|
| R-001 | [Description] | [Metric] |

### P - Performance Requirements
| Req ID | Requirement Description | Target Metric |
|-------|---------|---------|
| P-001 | [Description] | [Metric] |

### S - Supportability Requirements
| Req ID | Requirement Description | Target Metric |
|-------|---------|---------|
| S-001 | [Description] | [Metric] |

### + - Constraints
| Constraint ID | Constraint Description | Type |
|-------|---------|------|
| C-001 | [Description] | [Design/Implementation/Interface/Physical] |
```

---

## 😊 Method 2: KANO Model

### KANO Model Definition

**Must-be Requirements**:
- Must have, absence leads to dissatisfaction
- Presence doesn't lead to special satisfaction

**One-dimensional Requirements**:
- Better leads to more satisfaction
- Linear relationship

**Attractive Requirements**:
- Presence creates delight
- Absence doesn't lead to dissatisfaction

**Indifferent Requirements**:
- Presence or absence doesn't matter

**Reverse Requirements**:
- Presence leads to dissatisfaction

### KANO Classification Method

**User Research Questionnaire**:
```markdown
## KANO Questionnaire: [Feature Name]

**Positive Question**: If this feature is available, how would you feel?
- [ ] I like it
- [ ] It should be so
- [ ] I don't care
- [ ] I can tolerate it
- [ ] I dislike it

**Negative Question**: If this feature is not available, how would you feel?
- [ ] I like it
- [ ] It should be so
- [ ] I don't care
- [ ] I can tolerate it
- [ ] I dislike it
```

### KANO Classification Matrix

```markdown
## KANO Classification Results

| Requirement | Classification | Satisfaction Impact | Priority Recommendation |
|-----|------|-----------|-----------|
| [Req 1] | Must-be | High | MUST |
| [Req 2] | One-dimensional | Medium | SHOULD |
| [Req 3] | Attractive | Low | COULD |
| [Req 4] | Indifferent | None | WONT |
```

---

## 🏗️ Method 3: Requirements Hierarchy

### Three-Layer Requirements Structure

**Business Requirements Layer**:
- Why do this project
- Business goals and value

**User Requirements Layer**:
- What users want
- User stories and scenarios

**Functional Requirements Layer**:
- What the system should do
- Specific functions and features

### Requirements Hierarchy Template

```markdown
## Requirements Hierarchy Structure

### Business Requirements
**BR-001**: [Business Goal]
- Success Metric: [KPI]
- Business Value: [Value Description]

### User Requirements
**UR-001**: [User Requirement]
- Related Business Requirement: BR-001
- User Role: [Role]
- User Value: [Value Description]

### Functional Requirements
**FR-001**: [Functional Requirement]
- Related User Requirement: UR-001
- Implementation Approach: [Description]
```

---

## 📋 Phase Completion Checklist

### FURPS+ Classification
- [ ] All requirements classified into FURPS+ categories
- [ ] Each category has at least 1 requirement
- [ ] Constraints listed separately

### KANO Classification
- [ ] Core functions have undergone KANO classification
- [ ] User research data collected
- [ ] Classification results recorded

### Requirements Hierarchy
- [ ] Business requirements clarified
- [ ] User requirements linked to business requirements
- [ ] Functional requirements linked to user requirements

### Statistical Analysis
- [ ] Quantity of each requirement type counted
- [ ] Requirements distribution visualized
- [ ] Anomalies identified

---

## ⏭️ Next Steps

After completing this phase, proceed to:

**Phase 3: Requirements Analysis**
- Document: `phase3-analysis.md`
- Use user stories, use cases, event storming, and other methods to deeply analyze requirements

---

**Remember**: Good classification is the foundation for subsequent analysis and implementation.
