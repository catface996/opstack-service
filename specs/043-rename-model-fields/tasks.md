# Tasks: Rename Agent Model Fields

**Input**: Design documents from `/specs/043-rename-model-fields/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: No tests requested - this is a field renaming refactor.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

DDD multi-module structure:
- **bootstrap**: `bootstrap/src/main/resources/db/migration/`
- **infrastructure**: `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/`
- **domain**: `domain/domain-model/src/main/java/.../domain/model/`
- **application**: `application/application-api/src/main/java/.../application/api/dto/`

---

## Phase 1: Setup (Database Migration)

**Purpose**: Create database migration to rename columns

- [X] T001 Create V38 migration script to rename `model` → `model_name` and `model_id` → `provider_model_id` in `bootstrap/src/main/resources/db/migration/V38__rename_model_fields_in_agent.sql`

**Checkpoint**: Database schema ready for code changes

---

## Phase 2: Foundational (PO & Domain Layer)

**Purpose**: Update persistence and domain models - MUST complete before application layer changes

**⚠️ CRITICAL**: No application layer work can begin until this phase is complete

- [X] T002 [P] Update AgentPO: rename `model` → `modelName`, `modelId` → `providerModelId` in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/AgentPO.java`
- [X] T003 [P] Update AgentBoundPO: rename `agentModel` → `agentModelName`, `agentModelId` → `agentProviderModelId` in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agentbound/AgentBoundPO.java`
- [X] T004 [P] Update Agent domain model: rename `model` → `modelName`, `modelId` → `providerModelId` in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/Agent.java`
- [X] T005 [P] Update AgentBound domain model: rename `agentModel` → `agentModelName`, `agentModelId` → `agentProviderModelId` in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agentbound/AgentBound.java`

**Checkpoint**: Foundation ready - PO and Domain models updated

---

## Phase 3: User Story 1 - View Agent Configuration (Priority: P1) 🎯 MVP

**Goal**: API returns `providerModelId` and `modelName` fields correctly

**Independent Test**: Call `/api/service/v1/agents/query-by-id` and verify response contains new field names

### Implementation for User Story 1

- [X] T006 [P] [US1] Update AgentDTO: rename `model` → `modelName`, `modelId` → `providerModelId` in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/AgentDTO.java`
- [X] T007 [P] [US1] Update AgentBoundMapper.xml: change SQL aliases `a.model AS agentModel` → `a.model_name AS agentModelName`, `a.model_id AS agentModelId` → `a.provider_model_id AS agentProviderModelId` in `infrastructure/repository/mysql-impl/src/main/resources/mapper/agentbound/AgentBoundMapper.xml`
- [X] T008 [US1] Update AgentRepositoryImpl: update field mappings in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentRepositoryImpl.java`
- [X] T009 [US1] Update AgentBoundRepositoryImpl: update field mappings in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agentbound/AgentBoundRepositoryImpl.java`
- [X] T010 [US1] Update AgentBoundApplicationServiceImpl: update toAgentDTO method field mappings in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agentbound/AgentBoundApplicationServiceImpl.java`

**Checkpoint**: US1 complete - API returns correct field names

---

## Phase 4: User Story 2 - Executor Integration (Priority: P1)

**Goal**: System uses `providerModelId` for `llm_config.model_id` when calling Executor

**Independent Test**: Trigger execution and verify request JSON uses correct `provider_model_id` value

### Implementation for User Story 2

- [X] T011 [US2] Update HierarchyTransformer: change `getModelId()` → `getProviderModelId()`, `getModel()` → `getModelName()` in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/execution/transformer/HierarchyTransformer.java`

**Checkpoint**: US2 complete - Executor receives correct model ID

---

## Phase 5: User Story 3 - Update Agent Configuration (Priority: P2)

**Goal**: Admin can update Agent with new field names via API

**Independent Test**: Call `/api/service/v1/agents/update` with new field names and verify save

### Implementation for User Story 3

- [X] T012 [P] [US3] Update CreateAgentRequest: rename `model` → `modelName`, `modelId` → `providerModelId` in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/CreateAgentRequest.java`
- [X] T013 [P] [US3] Update UpdateAgentRequest: rename `model` → `modelName`, `modelId` → `providerModelId` in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/UpdateAgentRequest.java`
- [X] T014 [US3] Update AgentApplicationServiceImpl: update field mappings in create/update methods in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java`

**Checkpoint**: US3 complete - Agent CRUD uses new field names

---

## Phase 6: Polish & Validation

**Purpose**: Final verification and cleanup

- [X] T015 Compile and verify no errors: run `mvn clean compile`
- [X] T016 Restart service and run database migration
- [X] T017 Verify API responses using quickstart.md test commands
- [X] T018 Verify Executor integration by triggering execution

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - create migration first
- **Foundational (Phase 2)**: Depends on Phase 1 - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Phase 2 completion
  - US1 and US2 can proceed in parallel
  - US3 can proceed in parallel with US1/US2
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - No dependencies on other stories

### Within Each Phase

- PO files can be updated in parallel [P]
- Domain files can be updated in parallel [P]
- DTO files can be updated in parallel [P]
- Repository implementations depend on PO/Domain changes
- Application services depend on DTO/Repository changes

### Parallel Opportunities

- T002, T003, T004, T005 can all run in parallel (Phase 2)
- T006, T007 can run in parallel (Phase 3)
- T012, T013 can run in parallel (Phase 5)

---

## Parallel Example: Foundational Phase

```bash
# Launch all model updates together:
Task: "Update AgentPO in infrastructure/.../po/agent/AgentPO.java"
Task: "Update AgentBoundPO in infrastructure/.../po/agentbound/AgentBoundPO.java"
Task: "Update Agent in domain/.../model/agent/Agent.java"
Task: "Update AgentBound in domain/.../model/agentbound/AgentBound.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002-T005)
3. Complete Phase 3: User Story 1 (T006-T010)
4. **STOP and VALIDATE**: Test API returns correct field names
5. Proceed to US2/US3 if needed

### Full Implementation

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → API returns correct fields
3. Add User Story 2 → Executor uses correct model ID
4. Add User Story 3 → CRUD uses new field names
5. Polish → Final verification

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- This is a field renaming refactor - no new functionality
- All changes are mechanical - search & replace with verification
- Commit after each phase for easy rollback
