---
inclusion: manual
---
# Testing Best Practices

## Overview

This directory contains best practice guides for various types of testing in the software development lifecycle, helping teams establish a comprehensive testing system. Each document follows a unified structure, including: trigger word mappings, NON-NEGOTIABLE rules, execution steps, Gate Check validation checklist, and output format templates.

---

## Trigger Word Mappings

| User Expression | Testing Type | Reference Document |
|---------|-------------|----------|
| Review requirements/Inspect requirements | Requirements static testing | 00-static-review |
| Write unit tests/Unit testing | Unit testing | 01-unit |
| Write integration tests | Integration testing | 02-integration |
| E2E testing/End-to-end testing | E2E testing | 03-e2e |
| API testing/Interface testing | API testing | 04-api |
| Performance testing/Load testing | Performance testing | 05-performance |
| Security testing/Security audit | Security testing | 06-security |
| Compatibility testing/Cross-browser testing | Compatibility testing | 07-compatibility |
| Regression testing | Regression testing | 08-regression |

---

## Directory Structure

```
04-testing/
├── 00-static-review/  # Requirements static testing (shift-left testing)
├── 01-unit/           # Unit testing
├── 02-integration/    # Integration testing
├── 03-e2e/           # End-to-end testing
├── 04-api/           # API testing
├── 05-performance/   # Performance testing
├── 06-security/      # Security testing
├── 07-compatibility/ # Compatibility testing
└── 08-regression/    # Regression testing
```

## Testing Pyramid and Shift-Left Testing

```
    Requirements Static Testing ──────────────────► Find issues before coding (lowest cost)
         │
         ▼
        /\
       /  \      E2E Testing (Few)
      /----\
     /      \    Integration Testing (Moderate)
    /--------\
   /          \  Unit Testing (Many)
  /------------\
```

**Shift-Left Testing Principle**: Introduce testing activities as early as possible. Find issues during the requirements phase. The earlier defects are discovered, the lower the cost to fix them.

## Testing Type Descriptions

| Type | Purpose | Execution Timing | Coverage Scope |
|------|------|----------|----------|
| Requirements Static Testing | Discover ambiguities and omissions in requirements | Before coding | All requirements |
| Unit Testing | Verify behavior of individual functions/classes | Every commit | 70-80% |
| Integration Testing | Verify interactions between modules | Every commit | Critical paths |
| E2E Testing | Verify complete user flows | Daily/PR | Core flows |
| API Testing | Verify interface contracts | Every commit | All interfaces |
| Performance Testing | Verify system performance metrics | Periodic/Pre-release | Critical interfaces |
| Security Testing | Discover security vulnerabilities | Periodic/Pre-release | Entire system |
| Compatibility Testing | Verify cross-environment compatibility | Pre-release | Target environments |
| Regression Testing | Verify existing functionality is not broken | Every release | Full functionality |

## Testing Strategy Principles

1. **Test Early** - Continuously test during development
2. **Automation First** - Automate test cases whenever possible
3. **Fast Feedback** - Maintain test execution speed
4. **Isolation** - Tests are independent of each other
5. **Repeatability** - Test results are consistently reproducible
6. **Maintainability** - Test code requires maintenance as well

## Recommended Tools

### Backend (Java)
- Unit Testing: JUnit 5, Mockito
- Integration Testing: Spring Boot Test, Testcontainers
- API Testing: REST Assured
- Performance Testing: JMeter, Gatling

### Frontend (JavaScript/TypeScript)
- Unit Testing: Jest, Vitest
- Component Testing: React Testing Library
- E2E Testing: Playwright (recommended), Cypress
- Performance Testing: Lighthouse

---

## NON-NEGOTIABLE Global Rules

The following rules apply to all testing types:

1. **MUST** treat test code as equally important as production code, requires maintenance
2. **MUST** follow the testing pyramid principle (Unit 70% > Integration 20% > E2E 10%)
3. **MUST** name test methods to clearly describe scenarios
4. **NEVER** ignore failing tests
5. **NEVER** write tests without assertions
6. **STRICTLY** ensure tests are independent with no execution order dependencies

---

## Document Structure Description

Each subdirectory's README.md contains:

| Section | Description |
|------|------|
| Role Definition | The expert role AI plays |
| Trigger Word Mappings | User expression → Action → Output |
| NON-NEGOTIABLE Rules | Mandatory rules (MUST/NEVER/STRICTLY) |
| Core Principles | Core concepts and positioning for this testing type |
| Detailed Rules | Detailed execution rules and checklists |
| Execution Steps | Step-by-step execution process |
| Gate Check Validation Checklist | Checkpoints that must be confirmed before completion |
| Output Format Template | Standardized output format |
| Prompt Template | Ready-to-use prompts |
| Best Practices Checklist | Quick reference checklist |

---

## Usage Guide

1. **Identify Testing Needs**: Determine the required testing type based on trigger word mappings
2. **Reference Corresponding Documentation**: Navigate to the corresponding subdirectory to view detailed guides
3. **Follow NON-NEGOTIABLE Rules**: Ensure mandatory requirements are met
4. **Use Prompt Templates**: Use or modify templates to interact with AI
5. **Execute Gate Check**: Confirm all checkpoints before completion
