# Implementation Plan: Trigger Multi-Agent Execution

**Branch**: `039-trigger-multiagent-execution` | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/039-trigger-multiagent-execution/spec.md`

## Summary

Provide a frontend endpoint to trigger multi-agent collaboration execution by receiving a topology ID and user message, querying the hierarchical team structure, creating an executor hierarchy, starting a run, and streaming events back to the frontend via SSE.

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, Spring WebFlux (for WebClient SSE), MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (existing topology/agent data)
**Testing**: JUnit 5, Spring Boot Test
**Target Platform**: Linux server (Docker container)
**Project Type**: DDD Multi-Module Maven
**Performance Goals**: SSE streaming without buffering, <2s initial response
**Constraints**: Must integrate with external executor service at `http://localhost:8080`
**Scale/Scope**: Single endpoint, reuses existing hierarchical team query

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | ✅ PASS | New code follows layer conventions |
| II. API URL Convention | ✅ PASS | Endpoint: `/api/service/v1/executions/trigger` |
| III. POST-Only API Design | ✅ PASS | Uses POST method |
| IV. Database Migration | ⬜ N/A | No database changes required |
| V. Technology Stack | ✅ PASS | Java 21, Spring Boot 3.4.x |
| VI. Pagination Protocol | ⬜ N/A | Non-paginated endpoint |
| VII. Database Design Standards | ⬜ N/A | No new tables |

**Gate Result**: ✅ PASS - All applicable principles satisfied

## Project Structure

### Documentation (this feature)

```text
specs/039-trigger-multiagent-execution/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (DTOs only, no DB entities)
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── execution-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
# DDD Multi-Module Maven Structure
interface/
└── interface-http/
    └── src/main/java/com/catface996/aiops/interface_/http/controller/
        └── ExecutionController.java          # NEW: SSE streaming endpoint

application/
├── application-api/
│   └── src/main/java/com/catface996/aiops/application/api/
│       ├── dto/execution/                    # NEW: Execution DTOs
│       │   ├── request/TriggerExecutionRequest.java
│       │   └── ExecutionEventDTO.java
│       └── service/execution/
│           └── ExecutionApplicationService.java  # NEW: Service interface
└── application-impl/
    └── src/main/java/com/catface996/aiops/application/impl/service/execution/
        └── ExecutionApplicationServiceImpl.java  # NEW: Service implementation

infrastructure/
└── external-service/                         # NEW: External service client module
    └── executor-client/
        └── src/main/java/com/catface996/aiops/infrastructure/executor/
            ├── config/ExecutorClientConfig.java
            ├── client/ExecutorServiceClient.java
            └── dto/                          # Executor API DTOs
                ├── CreateHierarchyRequest.java
                ├── CreateHierarchyResponse.java
                ├── StartRunRequest.java
                ├── StartRunResponse.java
                └── ExecutorEvent.java
```

**Structure Decision**: Following existing DDD architecture. Adding new infrastructure module `executor-client` for external service integration, following the pattern of existing infrastructure modules (mysql-impl, redis-impl, sqs-impl).

## Complexity Tracking

> No violations requiring justification

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |

## Implementation Approach

### Key Design Decisions

1. **SSE Streaming**: Use Spring WebFlux WebClient for consuming executor SSE stream, forward to frontend via Spring MVC SSE
2. **External Service Client**: Create dedicated infrastructure module for executor service integration
3. **Data Transformation**: Convert HierarchicalTeamDTO to executor's CreateHierarchyRequest format
4. **Error Handling**: Return SSE error events for service failures instead of HTTP errors (after stream starts)

### Integration Flow

```
Frontend → ExecutionController (POST /trigger)
    → ExecutionApplicationService
        → TopologyApplicationService.queryHierarchicalTeam()
        → ExecutorServiceClient.createHierarchy()
        → ExecutorServiceClient.startRun()
        → ExecutorServiceClient.streamEvents() → SSE to Frontend
```

### Dependencies to Add

1. **spring-boot-starter-webflux**: For WebClient (SSE consumer)
2. No additional database changes required

## Phase Summary

| Phase | Output |
|-------|--------|
| Phase 0 | research.md - WebClient SSE patterns, executor API integration |
| Phase 1 | data-model.md, contracts/execution-api.yaml, quickstart.md |
| Phase 2 | tasks.md (via /speckit.tasks) |
