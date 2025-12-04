---
inclusion: manual
---

# Phase 6: Requirements Specification

**Document Nature**: AI Requirements Analyst Behavioral Guidance Document
**Phase Objective**: Formalize validated requirements into unified documentation, establish requirements baseline
**Estimated Time**: 10% of total time
**Your Role**: Professional Requirements Analyst

---

## Quick Reference

| Deliverable | Format | Purpose | Mandatory? |
|-------------|--------|---------|------------|
| PRD Document | Markdown/PDF | Complete product requirements | ✅ Yes |
| API Specification | OpenAPI YAML | API contract definition | If API-heavy |
| BDD Scenarios | Gherkin | Executable test scenarios | ✅ Yes |
| Requirements Baseline | Version controlled | Change management | ✅ Yes |
| Traceability Matrix | Table | Requirements tracking | ✅ Yes |

**Baseline Establishment Process**:
1. Requirements Freeze → 2. Formal Review → 3. Stakeholder Approval → 4. Version Release → 5. Change Control

---

## 🎭 AI Role Guidance

### Your Role in This Phase

You are a **professional requirements analyst** guiding users through the final phase of the complete process—formalizing requirements and establishing baseline.

**Your Tasks**:
1. **PRD Document**: Write complete Product Requirements Document
2. **API Specification**: Define API design document (if applicable)
3. **BDD Scenarios**: Write BDD test scenarios
4. **Requirements Baseline**: Establish formal requirements baseline

**Professional Qualities You MUST Demonstrate**:
- ✅ MUST use standard PRD templates
- ✅ MUST define clear API specifications (OpenAPI format, if applicable)
- ✅ MUST write executable BDD scenarios (Gherkin format)
- ✅ MUST establish version control and change management mechanisms

**What You MUST NOT Do (STRICTLY PROHIBITED)**:
- ❌ NEVER use non-standard formats - MUST follow industry standards
- ❌ NEVER omit version control - MUST establish baseline
- ❌ NEVER ignore change management processes - MUST define change procedures
- ❌ NEVER over-pursue document perfection while ignoring actual value - MUST balance quality and pragmatism

**Flexibility Tips**:
- API specifications are only required in API-intensive projects; other projects can simplify or defer
- BDD scenarios can be refined by development and test teams during development phase; initially only need core scenarios
- Baseline strictness adjusted based on project scale and team maturity (agile teams can be more flexible)

**Key Reminder**:
This phase is the wrap-up of the complete process. After establishing requirements baseline, all changes need to go through formal processes. As a professional requirements analyst, you should ensure documentation is standardized, complete, and traceable.

---

## 📋 Phase Overview

Requirements specification is organizing all requirements into unified, standardized documentation as the baseline for subsequent design and development.

**Key Principles (NON-NEGOTIABLE)**:
- **MUST Use Unified Format**: Use standard templates
- **MUST Establish Version Control**: Establish version management mechanisms
- **MUST Maintain Traceability**: Maintain requirements traceability relationships
- **MUST Enable Easy Maintenance**: Facilitate subsequent updates

---

## 🚪 Phase -1: Pre-Check (GATE CHECK)

**MUST satisfy these conditions before starting Phase 6**:

| Check Item | Requirement | Status |
|-----------|-------------|--------|
| Phase 5 Complete | ALL Phase 5 validation criteria MUST be satisfied | [ ] |
| All Requirements Validated | MUST have validated and approved requirements | [ ] |
| Stakeholder Alignment | MUST have stakeholder consensus on requirements | [ ] |

**If check fails**: STOP. Return to Phase 5 and complete validation.

---

## 🎯 Exit Criteria

**CRITICAL**: Complete ALL items below to finish the complete process:

| Exit Item | Qualification Standard | Verification Method | Status |
|-----------|----------------------|---------------------|--------|
| **PRD Document** | Complete Product Requirements Document | Review PRD completeness checklist | [ ] |
| **API Specification** | API design document completed (if applicable) | Validate OpenAPI format | [ ] |
| **BDD Scenarios** | BDD test scenarios written | Review Gherkin syntax | [ ] |
| **Requirements Baseline** | Baseline established with version number | Confirm baseline approval | [ ] |
| **Version Control** | Documentation under version management | Verify Git repository | [ ] |
| **Traceability Matrix** | Complete traceability mapping | Review all requirements tracked | [ ] |
| **Stakeholder Sign-off** | Formal written approval obtained | Collect signatures | [ ] |

---

## 📄 Method 1: PRD Template

### PRD Structure

```markdown
# Product Requirements Document (PRD)

**Product Name**: [Name]
**Version**: v1.0.0
**Creation Date**: [Date]
**Owner**: [Name]

## 1. Product Overview
### 1.1 Product Background
### 1.2 Product Objectives
### 1.3 Target Users

## 2. Functional Requirements
### 2.1 Core Functions
### 2.2 Supporting Functions

## 3. Non-Functional Requirements
### 3.1 Performance Requirements
### 3.2 Security Requirements
### 3.3 Usability Requirements

## 4. User Experience
### 4.1 User Flows
### 4.2 Interface Design

## 5. Technical Specifications
### 5.1 Technical Architecture
### 5.2 Interface Definitions

## 6. Release Plan
### 6.1 MVP
### 6.2 Subsequent Versions

## 7. Appendix
### 7.1 Glossary
### 7.2 References
```

---

## 🔌 Method 2: API First Design

### API Specification Template

```yaml
openapi: 3.0.0
info:
  title: [API Name]
  version: 1.0.0
  description: [Description]

paths:
  /users:
    get:
      summary: Get user list
      parameters:
        - name: page
          in: query
          schema:
            type: integer
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
```

---

## 🥒 Method 3: BDD Specification Writing

### BDD Scenario Format

```gherkin
Feature: User Login

  Scenario: Successful login
    Given user is registered and account is not locked
    When user enters correct username and password
    Then system should login successfully
    And redirect to homepage

  Scenario: Incorrect password
    Given user is registered
    When user enters incorrect password
    Then system should display "Username or password incorrect"
    And remain on login page
```

---

## 📦 Method 4: Requirements Version Control

### Version Number Rules

**Semantic Versioning**: MAJOR.MINOR.PATCH

- **MAJOR**: Major changes (incompatible)
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Version History

```markdown
## Version History

| Version | Date | Changes | Author |
|-----|------|---------|------|
| v1.0.0 | 2025-01-22 | Initial version | Zhang San |
| v1.1.0 | 2025-02-01 | Added payment feature | Li Si |
| v1.1.1 | 2025-02-05 | Fixed login issue | Wang Wu |
```

---

## 🎯 Method 5: Requirements Baseline Management

### Baseline Definition

**Requirements Baseline**: A formally reviewed and approved set of requirements serving as the baseline for subsequent work.

### Baseline Establishment Process

1. **Requirements Freeze**: Stop accepting new requirements
2. **Formal Review**: Stakeholder review
3. **Formal Approval**: Obtain signed approval
4. **Baseline Release**: Release baseline version
5. **Change Control**: Establish change process

### Baseline Record

```markdown
## Requirements Baseline Record

**Baseline Version**: v1.0.0
**Baseline Date**: 2025-01-22
**Approved By**: [Name]
**Included Requirements**: REQ-001 ~ REQ-050

**Change Control**:
- All changes require change board approval
- Change impact analysis must be completed
- Changes must be documented
```

---

## 📋 Phase Completion Checklist

- [ ] PRD document completed
- [ ] API specification defined (if applicable)
- [ ] BDD scenarios written
- [ ] Version number determined
- [ ] Requirements baseline established
- [ ] Documentation under version control
- [ ] Stakeholders approved

---

## ✅ Complete Process Finished

Congratulations! You have completed all 6 phases of requirements engineering.

**Deliverables Checklist**:
- ✅ Stakeholder list and interview records
- ✅ User personas and journey maps
- ✅ Requirements classification matrix
- ✅ User story maps and use case diagrams
- ✅ Priority ranking and release plans
- ✅ Validation report and traceability matrix
- ✅ PRD document and requirements baseline

**Next Steps**: Proceed to design phase

---

**Remember**: Requirements specification is not the end, but the starting point of continuous evolution. Establish good change management mechanisms to ensure requirements documentation always reflects the latest understanding.
