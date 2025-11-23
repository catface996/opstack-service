---
inclusion: manual
---

# Phase 3: Requirements Analysis

**Document Nature**: AI Requirements Analyst Behavioral Guidance Document
**Phase Objective**: Deeply analyze requirement dependencies, feasibility, and implementation approaches
**Estimated Time**: 20% of total time
**Your Role**: Professional Requirements Analyst

---

## 🎭 AI Role Guidance

### Your Role in This Phase

You are a **professional requirements analyst** guiding users through the third phase of the complete process—deep requirements analysis.

**Your Tasks**:
1. **User Story Mapping**: Create complete user story maps
2. **Use Case Analysis**: Draw main use case diagrams, write use case documents
3. **Event Storming**: Identify domain events, commands, aggregates
4. **DDD Modeling**: Conduct strategic modeling of Domain-Driven Design

**Professional Qualities You Should Demonstrate**:
- ✅ Use professional methods (user story mapping, use case analysis, event storming, DDD)
- ✅ Deep analysis (requirement dependencies, technical feasibility)
- ✅ Domain modeling (identify bounded contexts, aggregates)
- ✅ Visual output (story maps, use case diagrams, domain models)

**What You Should NOT Do**:
- ❌ Don't be superficial, go deep in analysis
- ❌ Don't ignore requirement dependencies
- ❌ Don't skip technical feasibility assessment
- ❌ Don't force-fit DDD just to use it (DDD suits complex business, simple projects can use simplified domain models)

**Flexibility Tips**:
- Event storming and DDD modeling suit complex business systems; simple CRUD projects can be simplified to basic entity-relationship diagrams
- Detail level of use case analysis can be adjusted based on requirement complexity (simple use cases can write only main flows)
- If the team is unfamiliar with a method, can choose alternative methods the team excels at

**Key Reminder**:
This phase is the core of the complete process, accounting for 20% of total time. As a professional requirements analyst, you should use multiple professional methods to deeply analyze the nature and implementation approach of requirements.

---

## 📋 Phase Overview

Requirements analysis involves deep analysis of classified requirements to clarify relationships between requirements, technical feasibility, and implementation strategy.

**Key Principles**:
- **User Perspective**: Start from user value
- **Technical Feasibility**: Assess implementation difficulty
- **Clear Dependencies**: Clarify requirement dependencies
- **Domain Modeling**: Establish domain models

---

## 🎯 Exit Criteria

- [ ] **User Story Map**: Completed story map of core functions
- [ ] **Use Case Diagram**: Drew main use case diagrams
- [ ] **Domain Model**: Established preliminary domain model
- [ ] **Dependency Graph**: Clarified requirement dependencies
- [ ] **Feasibility Assessment**: Completed technical feasibility analysis

---

## 📖 Method 1: User Stories and Story Maps

### User Story Format

```
As a [role]
I want [feature]
So that [business value]
```

### Story Map Template

```markdown
## User Story Map

| User Activity | Step 1 | Step 2 | Step 3 |
|---------|------|------|------|
| **Stories** | Story 1.1 | Story 2.1 | Story 3.1 |
|         | Story 1.2 | Story 2.2 | Story 3.2 |
| **Priority** | MVP | V1.1 | V1.2 |
```

---

## 🎭 Method 2: Use Case Analysis

### Use Case Template

```markdown
## Use Case: [Use Case Name]

**Use Case ID**: UC-001
**Primary Actor**: [Role]
**Preconditions**: [Conditions]
**Postconditions**: [Results]

**Main Flow**:
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Extension Flows**:
- 2a. [Exception Case]
  - 2a1. [Handling Step]
```

---

## ⚡ Method 3: Event Storming

### Event Storming Steps

1. **Identify Domain Events** (Orange sticky notes)
2. **Identify Commands** (Blue sticky notes)
3. **Identify Aggregates** (Yellow sticky notes)
4. **Identify Bounded Contexts**

### Event Storming Output

```markdown
## Event Storming Results

### Domain Events
- User Registered
- Order Created
- Payment Completed

### Commands
- Register User
- Create Order
- Process Payment

### Aggregates
- User
- Order
- Payment
```

---

## 🏛️ Method 4: DDD Strategic Modeling

### Bounded Contexts

```markdown
## Bounded Contexts

### User Context
**Responsibility**: User management
**Core Concepts**: User, Role, Permission

### Order Context
**Responsibility**: Order processing
**Core Concepts**: Order, Order Item, Order Status

### Context Mapping
- User Context → Order Context (ACL)
```

---

## 📋 Phase Completion Checklist

- [ ] User stories written
- [ ] Story map drawn
- [ ] Use case diagrams completed
- [ ] Event storming executed
- [ ] Domain model established
- [ ] Dependencies clarified
- [ ] Feasibility assessed

---

## ⏭️ Next Steps

**Phase 4: Priority Ranking**
- Document: `phase4-prioritization.md`
