---
inclusion: manual
---

# General Best Practices

This document defines universal principles and best practices that should be followed across all Spec development phases, applicable to any type of project.

## Language Usage Guidelines

**Basic Requirement**: All conversations, documents, and communications should use Chinese as much as possible.

**Scope of Application**:
- All conversations and communications with users
- Writing of requirement documents, design documents, and task documents
- Code comments and documentation
- Problem discussions and solution explanations
- Progress reports and summaries

**Exceptions**:
- Code itself (variable names, function names, class names, etc.) can use English
- Technical terms can remain in English (e.g., API, JSON, Maven, POM)
- EARS syntax keywords remain in uppercase English (THE, SHALL, WHEN, WHILE, IF, THEN, WHERE)
- English content from referenced technical documentation and specifications

**Why It Matters**:
- Ensures clear and accurate communication, avoiding language barriers
- Improves document readability and comprehensibility
- Facilitates quick understanding and collaboration among team members
- Aligns with the language habits of the project team

## Why These Practices Are Needed

LLMs are probabilistic models, and single-pass generation may have biases. Through progressive development, multiple validations, and continuous feedback, output quality can be significantly improved and rework costs reduced.

## Three Core Principles

### 1. Progressive Development

**Key Concept**: Progress in phases, with each phase validated before moving to the next.

**Workflow**:
```
Requirements → Validation → Design → Validation → Task Breakdown → Validation → Execution
```

**Why It Matters**:
- Discover issues early, reducing fix costs
- Avoid continuing development based on incorrect assumptions
- Ensure output quality at each phase

**Practice Points**:
- After completing requirements, self-check before entering design
- After completing design, validate before breaking down tasks
- After task breakdown, confirm before execution
- Never try to complete all phases at once

### 2. Continuous Validation

**Key Concept**: Multi-dimensional validation should be performed after completing each phase.

**Validation Dimensions**:
- **Consistency**: Is it consistent with the output of the previous phase?
- **Completeness**: Does it fully cover the requirements of the previous phase?
- **Accuracy**: Is the understanding and expression accurate?
- **Reasonableness**: Are there internal conflicts or over-engineering?

**Validation Methods**:
- Self-check: Review item by item against previous phase documents
- Cross-validation: Check if internal parts are consistent
- User confirmation: Critical decisions must be approved by users

### 3. Proactive Communication

**Key Concept**: When encountering uncertainty, proactively communicate with users rather than making assumptions.

**Situations Requiring Confirmation**:
- Requirements are unclear or ambiguous
- Multiple design options exist
- Potential over-engineering is detected
- Task breakdown granularity is uncertain
- Technical implementation approaches require trade-offs

**Communication Principles**:
- Transparency: Report progress and issues promptly
- Specificity: Clearly express questions and suggestions
- Respect: Take user feedback seriously

## Quality Assurance Checklist

After completing each phase, use the following checklist for self-inspection:

### Completeness Check
- [ ] Have all requirements been covered?
- [ ] Is there any missing content?
- [ ] Have edge cases been considered?

### Consistency Check
- [ ] Is it consistent with previous phase outputs?
- [ ] Are internal parts consistent with each other?
- [ ] Is terminology and concept usage consistent?

### Accuracy Check
- [ ] Is the understanding of requirements accurate?
- [ ] Is the technical solution description accurate?
- [ ] Are there any ambiguous or vague statements?

### Feasibility Check
- [ ] Is the design implementable?
- [ ] Are the tasks executable?
- [ ] Are validation criteria actionable?

### Reasonableness Check
- [ ] Is there over-engineering?
- [ ] Is there unnecessary complexity?
- [ ] Does it comply with project constraints and limitations?

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

**Consequences**: Developing based on incorrect assumptions, leading to extensive rework.

**Countermeasures**:
- Strictly follow progressive development process
- Validate each phase before entering the next
- Ensure correctness first, then consider performance optimization

### Pitfall 2: Over-Engineering
**Manifestation**: Designing complex architectures for potential future needs, implementing currently unnecessary features.

**Consequences**: Increased complexity, extended development time, reduced maintainability.

**Countermeasures**:
- Only implement currently needed features
- Keep design simple
- Reserve extension points but don't implement them in advance

### Pitfall 3: Ignoring Validation
**Manifestation**: Skipping validation steps, assuming everything is correct.

**Consequences**: Problems accumulate to later stages, fix costs grow exponentially.

**Countermeasures**:
- Perform thorough validation at each phase
- Use checklists to ensure nothing is missed
- Discover and fix issues promptly

### Pitfall 4: Insufficient Communication
**Manifestation**: Making assumptions about user intent, hiding problems and risks.

**Consequences**: Understanding deviations, deliverables don't meet expectations.

**Countermeasures**:
- Proactively ask when encountering uncertainty
- Regularly sync progress and issues
- Critical decisions must be confirmed by users

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
