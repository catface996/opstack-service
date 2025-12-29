# Research: Hierarchical Team Query

**Feature**: 038-hierarchical-team-query
**Date**: 2025-12-29

## 1. Existing Data Model Analysis

### Decision: Use existing entities and relationships

**Rationale**:
- All required entities already exist: `Topology`, `Node`, `Agent`
- Relationship tables exist: `topology_2_node`, `node_2_agent`
- Topology already has `global_supervisor_agent_id` field
- Agent already has `hierarchy_level` field with values: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER

**Alternatives Considered**:
- Creating a dedicated `hierarchical_team` table: Rejected because the data can be assembled from existing relationships at query time

## 2. Query Strategy

### Decision: Use application-level aggregation with optimized SQL queries

**Rationale**:
- Single topology query to get basic info and global supervisor
- Single query to get all member nodes with their details
- Batch query to get all agents for all nodes in one query
- Application-level grouping by node and hierarchy level

**Alternatives Considered**:
- Complex SQL JOIN with subqueries: Rejected due to complexity and potential performance issues with large datasets
- Multiple round-trip queries per node: Rejected due to N+1 query problem

## 3. Response Structure Design

### Decision: Hierarchical DTO structure

**Rationale**:
- `HierarchicalTeamDTO` as the root response containing topology info
- `TeamDTO` representing each node's team with supervisor and workers
- `AgentDTO` for agent details (reusing existing DTO structure)
- Matches the spec requirement exactly

**Alternatives Considered**:
- Flat structure with team arrays: Rejected because it doesn't clearly represent the hierarchy

## 4. Performance Optimization

### Decision: Three-query strategy with application-level assembly

**Rationale**:
1. Query 1: Get topology with global supervisor agent (single query)
2. Query 2: Get all member nodes for the topology (single query)
3. Query 3: Batch fetch all agents for all nodes (IN clause with all node IDs)
4. Application assembles the result

This approach minimizes database round trips while keeping queries simple.

**Alternatives Considered**:
- Single complex query with multiple JOINs: Rejected due to Cartesian product risk and difficulty in handling nullable relationships
- Lazy loading: Rejected due to N+1 problem

## 5. Error Handling

### Decision: Return 404 for non-existent topology, allow null/empty for missing agents

**Rationale**:
- Topology not found is a client error (404)
- Missing global supervisor returns null in response (not an error)
- Missing team supervisor/workers returns empty arrays (not an error)
- Consistent with existing API error handling patterns

## 6. Existing Code Reuse

### Components to Reuse:
- `TopologyRepository.findById()` - Get topology basic info
- `TopologyRepository.existsById()` - Validate topology existence
- `Topology2NodeMapper.selectNodeIdsByTopologyId()` - Get member node IDs
- `AgentRepository.findById()` - Get agent details (for global supervisor)
- `NodeAgentRelationMapper.selectAgentIdsByNodeId()` - Get agents per node
- `AgentMapper` - Batch fetch agents by IDs

### New Components Needed:
- `HierarchicalTeamDTO`, `TeamDTO` - Response DTOs
- `HierarchicalTeamQueryRequest` - Request DTO
- `HierarchicalTeamQueryService` - Application service
- New repository method for batch agent queries by node IDs

## 7. API Design

### Decision: POST /api/service/v1/topologies/hierarchical-team/query

**Rationale**:
- Follows project POST-Only API convention
- Path indicates it's a topology sub-resource query
- Request body contains topologyId

**Alternatives Considered**:
- GET /api/service/v1/topologies/{id}/hierarchical-team: Rejected because project uses POST-Only API
