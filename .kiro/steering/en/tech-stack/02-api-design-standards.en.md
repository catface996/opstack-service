---
inclusion: manual
---

# API Design Standards

## Core Principles

API definitions during the design phase should use **industry standard formats** rather than specific code implementations.

**Why It's Important**:
- Design should be technology-agnostic
- Facilitates understanding and collaboration across different teams
- Supports automated tool generation of code and documentation
- Facilitates API version management and evolution

## HTTP API Design

### Must Use OpenAPI (Swagger) Specification

HTTP API design **must** use OpenAPI 3.0 specification, which is the industry standard.

**❌ Wrong Approach**: Using code snippets to express API design

**✅ Correct Approach**: Using OpenAPI specification

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

## Checklist

After completing API design, confirm:

- [ ] HTTP API uses OpenAPI specification (not code snippets)
- [ ] GraphQL API uses Schema definition (if applicable)
- [ ] Internal interfaces only define signatures, not including implementation
- [ ] All APIs have clear documentation
- [ ] Error response format is unified
- [ ] Error code definitions are complete
- [ ] API definition files are in correct directories
- [ ] Design documents correctly reference API definition files

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
