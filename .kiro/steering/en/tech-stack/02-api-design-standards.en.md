---
inclusion: manual
---

# API Design Standards

## Quick Reference

| API Type | Standard Format | Mandatory? | Use When |
|----------|----------------|------------|----------|
| HTTP REST API | OpenAPI 3.0 (YAML) | ✅ Yes | All HTTP APIs |
| GraphQL API | GraphQL Schema (SDL) | ✅ Yes | All GraphQL APIs |
| Internal Interfaces | Interface signatures only | ✅ Yes | Inter-module communication |
| Pagination | 1-based (page starts from 1) | ✅ Yes | All paginated endpoints |
| Error Responses | Unified error format | ✅ Yes | All APIs |

**Critical Rules**:
- ❌ NEVER use code snippets to define APIs in design phase
- ✅ MUST use OpenAPI for HTTP APIs
- ✅ MUST use 1-based pagination (page=1 is first page)
- ✅ MUST define unified error response format

---

## Core Principles

API definitions during the design phase MUST use **industry standard formats** rather than specific code implementations.

**Why It's Important**:
- Design MUST be technology-agnostic
- Facilitates understanding and collaboration across different teams
- Supports automated tool generation of code and documentation
- Facilitates API version management and evolution

## HTTP API Design

### MUST Use OpenAPI (Swagger) Specification (NON-NEGOTIABLE)

HTTP API design **MUST** use OpenAPI 3.0 specification, which is the industry standard.

**❌ WRONG APPROACH - NEVER Do This**: Using code snippets to express API design

```java
// ❌ NEVER do this in design phase - this is implementation code
@GetMapping("/api/users")
public List<User> getUsers() {
    // ...
}
```

**✅ CORRECT APPROACH - MUST Do This**: Using OpenAPI specification

```yaml
# ✅ Use OpenAPI YAML for API design
openapi: 3.0.0
paths:
  /api/users:
    get:
      summary: Get user list
      responses:
        '200':
          description: Success
```

**OpenAPI Specification Should Include**:
- API basic information (title, version, description)
- Path definitions (paths): HTTP methods, parameters, request body, responses for each endpoint
- Data models (components/schemas): data structures for requests and responses
- Error response definitions
- Authentication methods (if applicable)

### Advantages of OpenAPI Specification

1. **Standardization**: Industry standard, everyone can understand
2. **Tool support**: Can automatically generate code, documentation, test cases
3. **Version management**: Facilitates API version evolution
4. **Contract testing**: Can perform contract testing based on specification
5. **Mock services**: Can quickly generate mock services

### OpenAPI Document Organization

In design documents, OpenAPI specifications should:
- Be independent YAML or JSON files
- Be placed in `.kiro/features/{feature-id}/api/` directory
- Be referenced in design documents

## GraphQL API Design

### Use GraphQL Schema Definition

GraphQL API design should use GraphQL Schema Definition Language (SDL).

**✅ Correct Approach**: Using GraphQL Schema

**GraphQL Schema Should Include**:
- Type definitions (type): object types and fields
- Input types (input): input parameters for mutation operations
- Enum types (enum): enumeration values for status, type, etc.
- Query root type (Query): query operation definitions
- Mutation root type (Mutation): mutation operation definitions
- Scalar types (scalar): custom scalar types (such as DateTime)
- Pagination and connection types (if applicable)

## Internal Interface Design

### Use Interface Definitions (Signatures Only)

For internal inter-module interfaces, you can use programming language interface definitions, but **signatures only**, not including implementation.

**✅ Correct Approach**: Interface definition (signatures only)
- Define interface name and method signatures
- Include method parameters, return values, exception declarations
- Add necessary documentation comments
- Do not include any implementation logic

**❌ Wrong Approach**: Including implementation logic

## Pagination Design

### Page Numbers Start from 1 (1-based) (NON-NEGOTIABLE)

All pagination APIs **MUST** use **1-based** pagination, meaning the first page has `page=1`.

**This is NON-NEGOTIABLE**: STRICTLY use 1-based pagination in all APIs. NEVER use 0-based pagination.

**Why 1-based is Mandatory**:

| Dimension | 1-based (Recommended) | 0-based |
|-----------|----------------------|---------|
| **User Intuition** | ✅ "First page" is 1, natural language | ❌ "First page" is 0, counter-intuitive |
| **Communication Cost** | ✅ PM says "page 3", dev passes page=3 | ❌ Need to mentally subtract 1 |
| **URL Readability** | ✅ `/users?page=1` is intuitive | ❌ `/users?page=0` looks odd |
| **UI Consistency** | ✅ Page display matches parameter | ❌ Shows "Page 1" but passes 0 |
| **MyBatis-Plus** | ✅ Native support | ❌ Needs +1 conversion |

**✅ Correct Approach**:

```java
// Controller layer: page starts from 1, default value is 1
@GetMapping("/users")
public ResponseEntity<PageResult<UserDTO>> getUsers(
        @RequestParam(defaultValue = "1") int page,   // Starts from 1
        @RequestParam(defaultValue = "10") int size) {
    // ...
}

// Usage examples
// GET /api/v1/users?page=1&size=10  → First page
// GET /api/v1/users?page=2&size=10  → Second page
```

**❌ Wrong Approach**:

```java
// Don't use 0-based pagination
@RequestParam(defaultValue = "0") int page  // Wrong: starts from 0
```

### Pagination Parameter Naming Conventions

| Parameter | Meaning | Default | Description |
|-----------|---------|---------|-------------|
| `page` | Page number | 1 | Starts from 1, indicates which page |
| `size` | Page size | 10 | Number of records returned per page |

### Pagination Response Format

Pagination response should include the following fields:

```json
{
  "code": 0,
  "message": "Success",
  "data": {
    "content": [...],        // Data list for current page
    "page": 1,               // Current page number (from 1)
    "size": 10,              // Page size
    "totalElements": 100,    // Total record count
    "totalPages": 10,        // Total page count
    "first": true,           // Is first page
    "last": false            // Is last page
  }
}
```

### Boundary Handling

- `page < 1`: Return first page data, or return 400 error
- `page > totalPages`: Return empty list
- `size` should have an upper limit (e.g., max 100) to prevent querying too much data at once

## Error Code Design

### Unified Error Response Format

All APIs should use a unified error response format.

**Error Response Should Include**:
- Error code (code): machine-readable error identifier
- Error message (message): human-readable error description
- Detailed information (details): optional array of detailed error information
- Timestamp (timestamp): time when error occurred
- Request path (path): request path where error occurred

### Error Code Definition Principles

Use tables to define error codes, should include:
- Error code: unique identifier
- HTTP status code: corresponding HTTP status code
- Description: meaning of the error
- Example message: typical error message

## API Definitions in Design Documents

### File Organization Principles

API definition files should be independent of design documents and organized by type:
- HTTP API: placed in `api/` directory, using `openapi.yaml` file
- GraphQL API: placed in `graphql/` directory, using `schema.graphql` file
- Error code definitions: placed in `api/` directory, using `errors.md` file

### Referencing in Design Documents

In design documents (`plan.md`) should:
- Reference independent API definition files
- List summary of core endpoints or services
- Explain main functions and purposes of APIs
- Not repeat complete API definitions in design documents

## Pre-Check (GATE CHECK)

**MUST verify these before starting API design**:

| Check Item | Requirement | Status |
|-----------|-------------|--------|
| Requirements Complete | MUST have finalized requirements document | [ ] |
| API Scope Identified | MUST know which APIs need to be designed | [ ] |
| Tools Available | MUST have OpenAPI editor/validator | [ ] |

**If check fails**: STOP. Complete requirements and setup tools first.

---

## Checklist (NON-NEGOTIABLE)

After completing API design, MUST confirm ALL items:

| Item | Requirement | Verification | Status |
|------|-------------|--------------|--------|
| HTTP API Format | MUST use OpenAPI specification (NOT code snippets) | Validate YAML syntax | [ ] |
| GraphQL API Format | MUST use Schema definition (if applicable) | Validate SDL syntax | [ ] |
| Interface Signatures | MUST only define signatures, NO implementation | Review for implementation code | [ ] |
| Documentation | ALL APIs MUST have clear documentation | Review completeness | [ ] |
| Error Format | Error response format MUST be unified | Check consistency | [ ] |
| Error Codes | Error code definitions MUST be complete | Review error table | [ ] |
| File Organization | API files MUST be in correct directories | Check file paths | [ ] |
| Design References | Design docs MUST correctly reference API files | Verify links | [ ] |
| Pagination Base | Pagination MUST use 1-based (page starts from 1) | Check default values | [ ] |
| Pagination Response | Response MUST include complete pagination info | Verify response schema | [ ] |

**STRICTLY ENFORCE**: If any item fails, MUST fix before proceeding to implementation.

## Summary

Core principles of API design:

1. **Use industry standard formats**: OpenAPI, GraphQL Schema
2. **Separate design from implementation**: Don't write implementation code during design phase
3. **Technology agnostic**: Design should be independent of specific implementation technology
4. **Documentation**: Complete API documentation and error code definitions
5. **Toolable**: Support automatic code and documentation generation

Following these principles can:
- ✅ Improve API design quality
- ✅ Facilitate team collaboration and understanding
- ✅ Support automated tools
- ✅ Facilitate API version management
- ✅ Reduce communication costs
