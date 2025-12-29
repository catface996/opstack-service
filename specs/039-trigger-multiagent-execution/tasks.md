# Tasks: Trigger Multi-Agent Execution

**Input**: Design documents from `/specs/039-trigger-multiagent-execution/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Not requested in this feature specification.

**Organization**: Tasks are grouped by implementation phase. This feature has three user stories (P0, P1, P1).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

This project uses DDD multi-module Maven structure:
- **interface**: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/`
- **application-api**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **application-impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/`
- **config**: `bootstrap/src/main/resources/`

---

## Phase 1: Setup (Dependencies & Configuration)

**Purpose**: Add WebFlux dependency and executor service configuration

- [X] T001 Add spring-boot-starter-webflux dependency to `application/application-impl/pom.xml`
- [X] T002 Add executor service configuration properties to `bootstrap/src/main/resources/application-local.yml`

**Checkpoint**: Dependencies and configuration ready

---

## Phase 2: Foundational (DTO Layer)

**Purpose**: Create all DTOs needed for the execution feature

**⚠️ CRITICAL**: Service layer depends on these DTOs

- [X] T003 [P] Create TriggerExecutionRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/execution/request/TriggerExecutionRequest.java`
- [X] T004 [P] Create ExecutionEventDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/execution/ExecutionEventDTO.java`
- [X] T005 [P] Create CreateHierarchyRequest (executor API) in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/CreateHierarchyRequest.java`
- [X] T006 [P] Create CreateHierarchyResponse (executor API) in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/CreateHierarchyResponse.java`
- [X] T007 [P] Create StartRunRequest (executor API) in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/StartRunRequest.java`
- [X] T008 [P] Create StartRunResponse (executor API) in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/StartRunResponse.java`
- [X] T009 [P] Create ExecutorEvent (executor API) in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/dto/ExecutorEvent.java`

**Checkpoint**: DTO layer complete - all request/response structures defined

---

## Phase 3: User Story 1 - Execute Multi-Agent Task via Topology (Priority: P0) 🎯 MVP

**Goal**: Enable users to trigger multi-agent execution by providing topology ID and message, receiving streamed events

**Independent Test**: Call `POST /api/service/v1/executions/trigger` with valid topologyId and message, verify SSE events are streamed back

### Implementation for User Story 1

- [X] T010 [US1] Create ExecutorServiceClient for external API calls in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/client/ExecutorServiceClient.java`
- [X] T011 [US1] Create HierarchyTransformer to convert HierarchicalTeamDTO to executor format in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`
- [X] T012 [US1] Create ExecutionApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/execution/ExecutionApplicationService.java`
- [X] T013 [US1] Implement ExecutionApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/ExecutionApplicationServiceImpl.java`
- [X] T014 [US1] Create ExecutionController with SSE endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ExecutionController.java`
- [X] T015 [US1] Add SpringDoc annotations for SSE endpoint in ExecutionController

**Checkpoint**: User Story 1 complete - basic execution flow works end-to-end

---

## Phase 4: User Story 2 - Handle Missing Topology Configuration (Priority: P1)

**Goal**: Return clear error messages when topology doesn't exist or isn't properly configured

**Independent Test**: Call endpoint with non-existent topologyId, verify 404 error; call with topology without Global Supervisor, verify 400 error

### Implementation for User Story 2

- [X] T016 [US2] Add topology existence validation in ExecutionApplicationServiceImpl
- [X] T017 [US2] Add Global Supervisor validation in ExecutionApplicationServiceImpl
- [X] T018 [US2] Add teams validation in ExecutionApplicationServiceImpl

**Checkpoint**: User Story 2 complete - validation errors return proper HTTP codes

---

## Phase 5: User Story 3 - Handle Executor Service Failures (Priority: P1)

**Goal**: Return appropriate error information when executor service is unavailable or fails

**Independent Test**: Simulate executor service failure, verify 503 error or error SSE event

### Implementation for User Story 3

- [X] T019 [US3] Add connection error handling in ExecutorServiceClient (503 Service Unavailable)
- [X] T020 [US3] Add hierarchy creation error handling in ExecutionApplicationServiceImpl
- [X] T021 [US3] Add run start error handling in ExecutionApplicationServiceImpl
- [X] T022 [US3] Add streaming error handling (error SSE events) in ExecutionApplicationServiceImpl

**Checkpoint**: User Story 3 complete - executor failures handled gracefully

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verification and validation of the implementation

- [X] T023 Build project and verify no compilation errors with `mvn clean package -DskipTests`
- [X] T024 Start application and verify endpoint is accessible
- [X] T025 Run quickstart.md validation scenarios manually
- [X] T026 Verify Swagger documentation shows new endpoint at `/swagger-ui.html`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - configuration first
- **Foundational (Phase 2)**: Setup complete (dependencies installed)
- **User Story 1 (Phase 3)**: Depends on Phase 1 and Phase 2 completion
- **User Story 2 (Phase 4)**: Depends on Phase 3 (adds validation to existing flow)
- **User Story 3 (Phase 5)**: Depends on Phase 3 (adds error handling to existing flow)
- **Polish (Phase 6)**: Depends on all previous phases

### Task Dependencies Within Phases

**Phase 1 (Setup)**:
- T001 → T002 (webflux needed before config makes sense)

**Phase 2 (DTOs)**:
- All tasks [P] can run in parallel (different files)

**Phase 3 (US1)**:
- T010 (client) + T011 (transformer) can run in parallel
- T012 → T013 (interface before implementation)
- T013 → T014 (service before controller)
- T014 → T015 (endpoint before annotations)

**Phase 4 (US2)**:
- T016 → T017 → T018 (sequential validations)

**Phase 5 (US3)**:
- T019 can run first (client-level errors)
- T020 → T021 → T022 (sequential error handling additions)

### User Story Independence

| User Story | Depends On | Can Test Independently? |
|------------|------------|------------------------|
| US1 (P0)   | Phase 1, 2 | ✅ Yes - core happy path |
| US2 (P1)   | US1        | ✅ Yes - validation errors |
| US3 (P1)   | US1        | ✅ Yes - service errors |

### Parallel Opportunities

- **Phase 2**: All 7 DTO tasks (T003-T009) can run in parallel
- **Phase 3**: T010 and T011 can run in parallel
- **Phase 4 & 5**: Can potentially run in parallel (both add to US1 implementation)

---

## Parallel Example: Phase 2

```bash
# Launch all DTO tasks together:
Task: "Create TriggerExecutionRequest in application/application-api/.../TriggerExecutionRequest.java"
Task: "Create ExecutionEventDTO in application/application-api/.../ExecutionEventDTO.java"
Task: "Create CreateHierarchyRequest in application/application-impl/.../CreateHierarchyRequest.java"
Task: "Create CreateHierarchyResponse in application/application-impl/.../CreateHierarchyResponse.java"
Task: "Create StartRunRequest in application/application-impl/.../StartRunRequest.java"
Task: "Create StartRunResponse in application/application-impl/.../StartRunResponse.java"
Task: "Create ExecutorEvent in application/application-impl/.../ExecutorEvent.java"
```

## Parallel Example: Phase 3 (Partial)

```bash
# Launch client and transformer in parallel:
Task: "Create ExecutorServiceClient in application/application-impl/.../ExecutorServiceClient.java"
Task: "Create HierarchyTransformer in application/application-impl/.../HierarchyTransformer.java"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Complete Phase 1: Setup (dependencies + config)
2. Complete Phase 2: Foundational (all DTOs)
3. Complete Phase 3: User Story 1 (core execution flow)
4. **STOP and VALIDATE**: Test with quickstart.md scenarios
5. Complete Phase 4: User Story 2 (validation errors)
6. Complete Phase 5: User Story 3 (service errors)
7. Complete Phase 6: Polish

### Task Count Summary

| Phase | Task Count |
|-------|------------|
| Phase 1: Setup | 2 |
| Phase 2: Foundational (DTOs) | 7 |
| Phase 3: User Story 1 (P0) | 6 |
| Phase 4: User Story 2 (P1) | 3 |
| Phase 5: User Story 3 (P1) | 4 |
| Phase 6: Polish | 4 |
| **Total** | **26** |

---

## Notes

- [P] tasks = different files, no dependencies
- [US1], [US2], [US3] labels map tasks to User Stories
- User Story 1 is the MVP - delivers core value
- User Stories 2 and 3 add robustness and error handling
- Verify application builds after each phase
- Commit after each logical group of tasks
- Reuse existing HierarchicalTeamDTO and TopologyApplicationService from Feature 038
