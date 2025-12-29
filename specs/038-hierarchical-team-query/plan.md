# Implementation Plan: Hierarchical Team Query

**Branch**: `038-hierarchical-team-query` | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/038-hierarchical-team-query/spec.md`

## Summary

Implement a query API to retrieve the hierarchical agent team structure for a given topology. The response includes a Global Supervisor (bound to topology), and a list of Teams (one per member node), where each Team has a Team Supervisor and Team Workers (bound to node). Uses existing entities and relationships with application-level aggregation.

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (existing tables: topology, node, agent, topology_2_node, node_2_agent)
**Testing**: JUnit 5, MockMvc
**Target Platform**: Linux server (Docker)
**Project Type**: Multi-module Maven project (DDD architecture)
**Performance Goals**: Response time < 3 seconds for topology with 100 nodes
**Constraints**: POST-Only API, standard response format
**Scale/Scope**: Single endpoint, reuses existing infrastructure

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| DDD Architecture | PASS | Follows existing layer structure (interface → application → domain → infrastructure) |
| API URL Convention | PASS | Uses `/api/service/v1/topologies/hierarchical-team/query` |
| POST-Only API Design | PASS | Uses POST method with JSON body |
| Database Migration | N/A | No new tables required |
| Technology Stack | PASS | Uses Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x |
| Pagination Protocol | N/A | Not a paginated endpoint |
| Database Design Standards | N/A | No new tables |

**Gate Status**: PASS - No violations

## Project Structure

### Documentation (this feature)

```text
specs/038-hierarchical-team-query/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Technical research and decisions
├── data-model.md        # Data model documentation
├── quickstart.md        # Integration test scenarios
├── contracts/           # API contracts
│   └── hierarchical-team-api.yaml
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Task breakdown (created by /speckit.tasks)
```

### Source Code (DDD Multi-Module Structure)

```text
interface/
└── src/main/java/com/catface996/aiops/interface_/
    └── controller/topology/
        └── TopologyController.java          # Add new endpoint

application/
├── application-api/src/main/java/com/catface996/aiops/application/
│   ├── request/topology/
│   │   └── HierarchicalTeamQueryRequest.java   # NEW: Request DTO
│   └── dto/topology/
│       ├── HierarchicalTeamDTO.java            # NEW: Response DTO
│       └── TeamDTO.java                        # NEW: Team structure DTO
└── application-impl/src/main/java/com/catface996/aiops/application/impl/
    └── service/topology/
        └── TopologyApplicationServiceImpl.java # Add query method

domain/
└── repository-api/src/main/java/com/catface996/aiops/repository/
    ├── topology2/TopologyRepository.java       # Add method to get with global supervisor
    └── node/NodeAgentRelationRepository.java   # Add batch query method

infrastructure/
└── repository/mysql-impl/src/main/
    ├── java/com/catface996/aiops/repository/mysql/
    │   ├── impl/topology/TopologyRepositoryImpl.java
    │   └── mapper/
    │       └── node/NodeAgentRelationMapper.java    # Add batch query
    └── resources/mapper/
        ├── topology/TopologyMapper.xml              # Add query with agent join
        └── node/NodeAgentRelationMapper.xml         # Add batch query SQL
```

**Structure Decision**: Follows existing DDD multi-module Maven structure. New code is added to existing modules in appropriate layers.

## Implementation Phases

### Phase 1: DTO Layer
- Create `HierarchicalTeamQueryRequest` request DTO
- Create `HierarchicalTeamDTO` and `TeamDTO` response DTOs
- Reuse existing `AgentDTO` for agent information

### Phase 2: Repository Layer
- Add `findByIdWithGlobalSupervisor()` to TopologyRepository
- Add `findAgentsByNodeIds()` batch query to repository
- Add corresponding SQL in MyBatis mappers

### Phase 3: Application Service Layer
- Add `queryHierarchicalTeam()` method to TopologyApplicationService
- Implement three-query strategy with application-level assembly
- Handle edge cases (null supervisor, empty teams, etc.)

### Phase 4: Controller Layer
- Add `/topologies/hierarchical-team/query` endpoint to TopologyController
- Add SpringDoc annotations for API documentation

### Phase 5: Testing
- Unit tests for service layer
- Integration tests for controller endpoint
- Verify all acceptance scenarios from spec

## Complexity Tracking

> No violations - table not needed
