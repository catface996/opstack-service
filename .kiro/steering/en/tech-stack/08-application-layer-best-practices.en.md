---
inclusion: manual
---

# Application Layer Best Practices

This document guides AI on how to properly write Application Layer (Application Service Layer) code in DDD architecture, ensuring code clarity, maintainability, and adherence to the Single Responsibility Principle.

## Quick Reference

| Rule | Requirement | Priority |
|------|-------------|----------|
| Method Steps | Main method MUST have 5-10 clear numbered steps | P0 |
| No Conditionals in Main | NEVER have if/else or try/catch in main method | P0 |
| Encapsulate Validation | ALL validation logic MUST be in private methods | P0 |
| Encapsulate Conversion | ALL model conversion MUST be in private methods | P0 |
| Main Method Length | Main method MUST NOT exceed 20 lines | P0 |

## Critical Rules (NON-NEGOTIABLE)

| Rule | Description | ✅ Correct | ❌ Wrong |
|------|-------------|------------|----------|
| **5-10 Steps Only** | Main method MUST contain only 5-10 numbered steps | Clear step comments `// 1. Validate` | 30+ lines with mixed logic |
| **Zero if/else in Main** | Main method STRICTLY FORBIDDEN to have if/else | Extract to `validateUsername()` | `if (exists) { throw ... }` in main |
| **Zero try/catch in Main** | Main method MUST NOT catch exceptions | Let exceptions propagate | `try { ... } catch (Exception e)` wrapping main logic |
| **No Inline Conversion** | NEVER have 5+ lines of field mapping in main | `return buildResult(account)` | 10 lines of `result.setXxx()` in main |
| **Single Responsibility Methods** | Each private method MUST do ONE thing only | `validateAccountUniqueness()` | `validateAndCreateAccount()` |
| **Method Naming Clarity** | Method names MUST describe exact action | `logRegistrationSuccess()` | `process()` or `handle()` |

## Core Principles

### 1. Orchestration Principle

The core responsibility of the Application Layer is to **orchestrate business workflows**, not to implement business logic.

**You must follow**:
- ✅ Coordinate domain services to complete business workflows
- ✅ Handle transaction boundaries
- ✅ Convert between DTOs and domain models
- ❌ Don't implement concrete business rules (should be in Domain layer)
- ❌ Don't directly operate on databases (should use Repository)
- ❌ Don't include complex conditional logic (should encapsulate in sub-methods or Domain layer)

### 2. Method Steps Principle

Each application service method should have **clear steps and strong readability**.

**You should ensure**:
- ✅ Method body contains 5-10 clear steps
- ✅ Each step is a method call with clear semantics
- ✅ Avoid if/else, try/catch control structures in main flow
- ✅ Encapsulate conditional logic in private methods
- ✅ Encapsulate exception handling in a unified location

### 3. Separation of Concerns Principle

**You must separate different responsibilities into different methods**:

| Responsibility | Example | Should be placed in |
|----------------|---------|---------------------|
| Data Validation | Check username uniqueness, password strength | Private validation methods |
| Domain Object Creation | Create Account entity | Private factory methods |
| Model Conversion | Account → UserInfo | Private conversion methods |
| Audit Logging | Record operation logs | Private logging methods |
| Business Logic | Password encryption, session creation | Domain layer services |

## Method Writing Standards

### Standard 1: Main Method Only Contains Workflow Orchestration

**What you should NOT do ❌**:
1. Include if/else conditional statements in main method
2. Write detailed model conversion logic (5+ lines of property mapping)
3. Write detailed audit logging logic
4. Wrap business logic with try/catch blocks
5. Create objects with 5+ lines of property setting
6. Write main methods exceeding 20 lines

**What you SHOULD do ✅**:
1. Main method contains 6-10 clear numbered steps (e.g., // 1. Validate, // 2. Create, etc.)
2. Each step is a single method call or simple assignment
3. No if/else statements in main flow
4. No try/catch blocks in main flow (exceptions propagate naturally)
5. Each step has clear semantic meaning from its method name
6. Main method reads like a high-level business process description

**Result**:
- Main method reduces from 80+ lines to 15-20 lines
- Business workflow is clear at a glance
- Easy to understand, maintain, and test

### Standard 2: Encapsulate Conditional Logic in Private Methods

**Encapsulation rules**:
1. **All if/else statements** must be moved to private methods
2. **Method name must clearly indicate** what condition is being checked
3. **Validation methods** throw exceptions on failure (use `validate` prefix)
4. **Check methods** return boolean or Optional (use `check` prefix)
5. **Private methods** can contain conditional logic, but keep it focused on single responsibility

**Benefits**:
- Main method stays clean and linear
- Conditional logic is testable independently
- Method names self-document the conditions being checked

### Standard 3: Model Conversion Method Best Practices

**Naming conventions**:
- `convertToXxx`: Single object conversion (Account → UserInfo)
- `convertToXxxList`: List conversion (List<Account> → List<UserInfo>)
- `buildXxx`: Build new object that includes additional logic beyond simple field mapping

**Placement conventions**:
- **Simple conversion** (1-10 fields): Place in application service private methods
- **Complex conversion** (10+ fields, nested objects): Consider extracting to dedicated Converter class
- **Common conversion** (used across multiple services): Extract to Common module

**Implementation rules**:
1. Conversion methods should only map fields, no business logic
2. Use builder pattern or setters consistently
3. Handle null values appropriately
4. Conversion methods should be pure (no side effects)

### Standard 4: Exception Handling Best Practices

**Exception handling rules**:
1. **Business exceptions**: Throw directly, don't catch
   - DuplicateUsernameException
   - InvalidPasswordException
   - AccountLockedException
   - AuthenticationException

2. **System exceptions**: Let them propagate to global exception handler
   - DatabaseException
   - NetworkException
   - UnexpectedRuntimeException

3. **Main method exceptions**: Never use try/catch in main public methods
   - Let exceptions propagate naturally
   - Global exception handler will log and convert to HTTP responses

4. **When to use try/catch**: Only in special cases where you need to return a result instead of throwing
   - Example: Session validation that returns `invalid` result instead of exception
   - Must be documented with comment explaining why

**Benefits**:
- Main methods stay clean
- Exception handling is centralized
- Stack traces are preserved
- Easier to debug issues

### Standard 5: Audit Logging Best Practices

**Logging rules**:
1. **Audit logs** use structured format with pipe separators:
   ```
   [Audit Log] Operation Description | field1=value1 | field2=value2 | timestamp=timestamp
   ```

2. **Logging methods** must be extracted to private methods:
   - Method name pattern: `log{Operation}{Status}` (e.g., logRegistrationSuccess, logLoginFailure)
   - Include all relevant business context (accountId, username, operation parameters)
   - Always include timestamp field

3. **Operations requiring audit logs**:
   - User registration, login, logout
   - Account lock, unlock
   - Permission changes
   - Sensitive data access
   - Critical configuration changes
   - Financial operations

4. **Regular logs** use descriptive format with comma separators:
   ```
   User login process started, username: john, IP: 192.168.1.1
   ```

## Method Naming Conventions

### Validation Methods

**Conventions**:
- `validate` prefix: Throws exception on validation failure
- `check` prefix: Returns boolean or Optional on validation failure

**Rules**:
1. Validation method name should describe what is being validated
2. Method should validate only one thing (Single Responsibility)
3. Throw specific business exceptions with clear messages
4. Include relevant context in log warnings before throwing exception

### Creation/Building Methods

**Conventions**:
- `create` prefix: Create domain objects (Account, Order, Session)
- `build` prefix: Build DTOs (RegisterResult, LoginResult)

**Rules**:
1. Create methods return domain entities
2. Build methods return DTOs
3. Method names should indicate what is being created/built
4. May include additional logic like encryption, default value setting

### Conversion Methods

**Conventions**:
- `convertTo` prefix: Model conversion

**Rules**:
1. Single object: `convertToUserInfo(Account account)`
2. List: `convertToUserInfoList(List<Account> accounts)`
3. Should be pure functions (no side effects)
4. Handle null values appropriately

### Logging Methods

**Conventions**:
- `log` prefix + business action

**Rules**:
1. Method name pattern: `log{Operation}{Status}`
   - logRegistrationSuccess
   - logLoginFailure
   - logAccountLocked
2. Use structured audit log format for security-related operations
3. Use regular log format for general business operations
4. Include all relevant business context

### Handler Methods

**Conventions**:
- `handle` prefix: Handle specific business scenarios

**Rules**:
1. Method name should describe the scenario being handled
2. Examples: handleLoginFailure, handleSessionExpired
3. May include multiple steps (recording failure, locking account, etc.)
4. Should encapsulate all logic for handling that scenario

### Finding Methods

**Conventions**:
- `find` prefix: Query and return domain objects

**Rules**:
1. Simple find: `findById`, `findByUsername`, `findByEmail`
2. Complex find: `findAccountByIdentifier` (tries username, then email)
3. Throw specific exception if not found (AccountNotFoundException)
4. Never return null, use Optional or throw exception

## Checklist

When writing or reviewing Application layer code, use this checklist:

### Main Method Check

- [ ] Method body contains 5-10 clear steps
- [ ] Each step is a method call with clear semantics
- [ ] Main method has no if/else conditional statements
- [ ] Main method has no try/catch exception handling
- [ ] Main method has no loop logic
- [ ] Main method length does not exceed 20 lines
- [ ] Each step is numbered with comment (// 1. , // 2. , etc.)

### Separation of Concerns Check

- [ ] Data validation logic encapsulated in private methods
- [ ] Domain object creation encapsulated in private methods
- [ ] Model conversion logic encapsulated in private methods
- [ ] Audit logging logic encapsulated in private methods
- [ ] Business rules implemented in Domain layer (not in Application layer)

### Naming Convention Check

- [ ] Validation methods use validate or check prefix
- [ ] Creation methods use create prefix
- [ ] Building methods use build prefix
- [ ] Conversion methods use convertTo prefix
- [ ] Logging methods use log prefix
- [ ] Handler methods use handle prefix
- [ ] Finding methods use find prefix

### Exception Handling Check

- [ ] Business exceptions thrown directly (not caught)
- [ ] No try/catch in main methods (except special documented cases)
- [ ] System exceptions handled by global exception handler
- [ ] Exception messages clear and explicit

### Code Quality Check

- [ ] Each private method has single responsibility
- [ ] Method names are self-documenting
- [ ] No duplicate code across private methods
- [ ] Private methods are 5-20 lines each
- [ ] Proper use of @Transactional annotation

## Common Mistakes

### Mistake 1: Main Method Contains Numerous if/else

**Problem**: Main method has 3+ if/else statements making it hard to read

**Solution**: Extract each conditional check into a private method with clear name
- Bad: `if (lockInfo.isPresent()) { ... }`
- Good: `checkAccountNotLocked(identifier);`

### Mistake 2: Main Method Contains Detailed Model Conversion

**Problem**: Main method has 10+ lines of DTO building with field mapping

**Solution**: Extract to private `buildXxx` or `convertToXxx` method
- Bad: Inline 10 lines of `result.setXxx()` calls
- Good: `return buildRegisterResult(savedAccount);`

### Mistake 3: Private Method Has Mixed Responsibilities

**Problem**: Single method both validates AND creates/modifies data

**Solution**: Split into separate methods
- Bad: `validateAndCreateAccount()` - does two things
- Good: `validateAccountUniqueness()` and `createAccount()` - each does one thing

### Mistake 4: Try/Catch in Main Method

**Problem**: Main method wrapped in try/catch, catching and re-throwing business exceptions

**Solution**: Remove try/catch, let exceptions propagate naturally
- Bad: Catch business exceptions just to log and re-throw
- Good: No try/catch, exceptions propagate to global handler

### Mistake 5: Unclear Method Names

**Problem**: Method names like `process()`, `handle()`, `check()` without specificity

**Solution**: Use specific names that describe what is being processed/handled/checked
- Bad: `processAccount()`, `handleRequest()`
- Good: `validateAccountUniqueness()`, `handleLoginFailure()`

## Summary

The best practice for Application layer is to **keep main methods clear and concise, encapsulating details in private methods**.

**Remember these three principles**:
1. **5-10 Steps Principle**: Main method contains 5-10 clear numbered steps
2. **No Conditional Logic Principle**: Avoid if/else, try/catch in main method
3. **Separation of Concerns Principle**: Separate validation, conversion, logging responsibilities into different methods

**Key metrics for success**:
- Main method: 15-20 lines
- Each step: Single method call
- if/else in main: 0
- try/catch in main: 0 (except special cases)
- Private helper methods: 5-20 each

Following these principles, your Application layer code will be **clear, maintainable, and easy to test**.
