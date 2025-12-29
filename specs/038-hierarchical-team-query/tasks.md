# Tasks: Hierarchical Team Query

**Input**: Design documents from `/specs/038-hierarchical-team-query/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Not requested in this feature specification.

**Organization**: Tasks are grouped by implementation phase. This feature has a single user story (P0).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1)
- Include exact file paths in descriptions

## Path Conventions

This project uses DDD multi-module Maven structure:
- **interface**: `interface/src/main/java/com/catface996/aiops/interface_/`
- **application-api**: `application/application-api/src/main/java/com/catface996/aiops/application/`
- **application-impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/`
- **domain/repository-api**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/`
- **infrastructure/mysql-impl**: `infrastructure/repository/mysql-impl/src/main/`

---

## Phase 1: Setup (DTO Layer)

**Purpose**: Create request and response DTOs for the hierarchical team query

- [X] T001 [P] Create HierarchicalTeamQueryRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/HierarchicalTeamQueryRequest.java`
- [X] T002 [P] Create HierarchicalTeamDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/HierarchicalTeamDTO.java`
- [X] T003 [P] Create TeamDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/TeamDTO.java`

**Checkpoint**: DTO layer complete - request/response structures defined

---

## Phase 2: Foundational (Repository Layer)

**Purpose**: Add repository methods for batch queries needed by the service layer

**⚠️ CRITICAL**: Service layer depends on these repository methods

- [X] T004 SKIPPED - Reuse existing queries (findById already returns global_supervisor_agent_id)
- [X] T005 SKIPPED - Reuse existing queries
- [X] T006 SKIPPED - Reuse existing queries
- [X] T007 SKIPPED - Reuse existing queries
- [X] T008 Add selectAgentsWithHierarchyByNodeIds SQL in `infrastructure/repository/mysql-impl/src/main/resources/mapper/node/NodeAgentRelationMapper.xml`
- [X] T009 Add selectAgentsWithHierarchyByNodeIds method to NodeAgentRelationMapper in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/node/NodeAgentRelationMapper.java`
- [X] T010 Add findAgentsWithHierarchyByNodeIds method to NodeAgentRelationRepository interface in `domain/repository-api/src/main/java/com/catface996/aiops/repository/node/NodeAgentRelationRepository.java`
- [X] T011 Implement findAgentsWithHierarchyByNodeIds in NodeAgentRelationRepositoryImpl in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/node/NodeAgentRelationRepositoryImpl.java`

**Checkpoint**: Repository layer complete - all data access methods ready

---

## Phase 3: User Story 1 - Query Hierarchical Team Structure (Priority: P0) 🎯 MVP

**Goal**: Enable users to query the complete hierarchical agent team structure for a given topology

**Independent Test**: Call `POST /api/service/v1/topologies/hierarchical-team/query` with a topology ID and verify the response contains globalSupervisor and teams list

### Implementation for User Story 1

- [X] T012 [US1] Add TopologyApplicationService interface method queryHierarchicalTeam in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyApplicationService.java`
- [X] T013 [US1] Implement queryHierarchicalTeam in TopologyApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java`
- [X] T014 [US1] Add queryHierarchicalTeam endpoint to TopologyController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`
- [X] T015 [US1] Add SpringDoc annotations for the new endpoint in TopologyController

**Checkpoint**: User Story 1 complete - hierarchical team query is fully functional

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Verification and validation of the implementation

- [X] T016 Build project and verify no compilation errors with `mvn clean package -DskipTests`
- [X] T017 Start application and verify endpoint is accessible
- [X] T018 Run quickstart.md validation scenarios manually
- [X] T019 Verify Swagger documentation shows new endpoint at `/swagger-ui.html`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - DTOs can be created first
- **Foundational (Phase 2)**: DTOs from Phase 1 (for return types), but can mostly proceed in parallel
- **User Story (Phase 3)**: Depends on Phase 1 and Phase 2 completion
- **Polish (Phase 4)**: Depends on Phase 3 completion

### Task Dependencies Within Phases

**Phase 2 (Repository Layer)**:
- T004 → T005 (XML before Java mapper)
- T005 → T006 → T007 (interface before implementation)
- T008 → T009 (XML before Java mapper)
- T009 → T010 → T011 (interface before implementation)

**Phase 3 (Service/Controller)**:
- T012 → T013 (interface before implementation)
- T013 → T014 (service before controller)
- T014 → T015 (endpoint before annotations)

### Parallel Opportunities

- **Phase 1**: All three DTOs (T001, T002, T003) can be created in parallel
- **Phase 2**: Topology repo tasks (T004-T007) and Node-Agent repo tasks (T008-T011) can run in parallel

---

## Parallel Example: Phase 1

```bash
# Launch all DTO tasks together:
Task: "Create HierarchicalTeamQueryRequest in application/application-api/.../HierarchicalTeamQueryRequest.java"
Task: "Create HierarchicalTeamDTO in application/application-api/.../HierarchicalTeamDTO.java"
Task: "Create TeamDTO in application/application-api/.../TeamDTO.java"
```

## Parallel Example: Phase 2

```bash
# Launch topology repo and node-agent repo streams in parallel:
# Stream A (Topology):
Task: "Add selectByIdWithGlobalSupervisor SQL in TopologyMapper.xml"
# Stream B (Node-Agent):
Task: "Add selectAgentsByNodeIdsWithHierarchy SQL in NodeAgentRelationMapper.xml"
```

---

## Implementation Strategy

### MVP First (Single User Story)

1. Complete Phase 1: Setup (DTOs)
2. Complete Phase 2: Foundational (Repository methods)
3. Complete Phase 3: User Story 1 (Service and Controller)
4. **STOP and VALIDATE**: Test with quickstart.md scenarios
5. Complete Phase 4: Polish

### Task Count Summary

| Phase | Task Count |
|-------|------------|
| Phase 1: Setup | 3 |
| Phase 2: Foundational | 8 |
| Phase 3: User Story 1 | 4 |
| Phase 4: Polish | 4 |
| **Total** | **19** |

---

## Notes

- [P] tasks = different files, no dependencies
- [US1] label maps task to User Story 1 (the only user story in this feature)
- This feature has a single user story (P0), so it can be delivered as a complete MVP
- Verify application builds after each phase
- Commit after each logical group of tasks
- Use existing AgentDTO for agent information - no need to create a new one
