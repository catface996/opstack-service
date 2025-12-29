# Data Model: Hierarchical Team Query

**Feature**: 038-hierarchical-team-query
**Date**: 2025-12-29

## Overview

This feature does not require new database tables. It assembles data from existing entities and relationships.

## Existing Entities Used

### 1. Topology (topology table)

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(100) | Topology name |
| global_supervisor_agent_id | BIGINT | Foreign key to agent table |

### 2. Node (node table)

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(100) | Node name |

### 3. Agent (agent table)

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(100) | Agent name |
| role | VARCHAR(32) | Agent role (specialty domain) |
| hierarchy_level | VARCHAR(32) | GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER |
| specialty | VARCHAR(500) | Specialty description |
| model | VARCHAR(100) | AI model identifier |

## Existing Relationships Used

### 1. Topology to Node (topology_2_node table)

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| topology_id | BIGINT | Foreign key to topology |
| node_id | BIGINT | Foreign key to node |

### 2. Node to Agent (node_2_agent table)

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| node_id | BIGINT | Foreign key to node |
| agent_id | BIGINT | Foreign key to agent |

## Response DTOs (New)

### HierarchicalTeamDTO

```java
public class HierarchicalTeamDTO {
    private Long topologyId;           // Topology ID
    private String topologyName;       // Topology name
    private AgentDTO globalSupervisor; // Global supervisor (nullable)
    private List<TeamDTO> teams;       // Team list
}
```

### TeamDTO

```java
public class TeamDTO {
    private Long nodeId;               // Node ID
    private String nodeName;           // Node name
    private AgentDTO supervisor;       // Team supervisor (nullable)
    private List<AgentDTO> workers;    // Team workers (can be empty)
}
```

### AgentDTO (Existing - Reuse)

```java
public class AgentDTO {
    private Long id;
    private String name;
    private String role;               // Agent role (specialty domain)
    private String hierarchyLevel;     // Hierarchy level
    private String specialty;          // Specialty description
    private String model;              // AI model
}
```

## Query Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Request: topologyId                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 1: Query topology table                               │
│  - Get topology name                                        │
│  - Get global_supervisor_agent_id                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 2: Query topology_2_node + node tables                │
│  - Get all member nodes (id, name)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 3: Query node_2_agent + agent tables                  │
│  - Batch query: Get all agents for all node IDs             │
│  - Filter: deleted = 0                                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 4: Application-level assembly                         │
│  - Group agents by node_id                                  │
│  - For each node:                                           │
│    - TEAM_SUPERVISOR → supervisor (first by created_at)     │
│    - TEAM_WORKER → workers list                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Response: HierarchicalTeamDTO               │
└─────────────────────────────────────────────────────────────┘
```

## Database Queries Required

### Query 1: Get Topology with Global Supervisor

```sql
SELECT t.id, t.name, t.global_supervisor_agent_id,
       a.id AS gs_id, a.name AS gs_name, a.role AS gs_role,
       a.hierarchy_level AS gs_hierarchy_level, a.specialty AS gs_specialty, a.model AS gs_model
FROM topology t
LEFT JOIN agent a ON t.global_supervisor_agent_id = a.id AND a.deleted = 0
WHERE t.id = #{topologyId} AND t.deleted = 0
```

### Query 2: Get Member Nodes

```sql
SELECT n.id, n.name
FROM topology_2_node t2n
JOIN node n ON t2n.node_id = n.id AND n.deleted = 0
WHERE t2n.topology_id = #{topologyId} AND t2n.deleted = 0
ORDER BY n.name
```

### Query 3: Batch Get Agents by Node IDs

```sql
SELECT n2a.node_id, a.id, a.name, a.role, a.hierarchy_level, a.specialty, a.model, a.created_at
FROM node_2_agent n2a
JOIN agent a ON n2a.agent_id = a.id AND a.deleted = 0
WHERE n2a.node_id IN (#{nodeIds}) AND n2a.deleted = 0
ORDER BY n2a.node_id, a.created_at
```

## Validation Rules

1. **Topology exists**: Return 404 if topology not found
2. **Soft delete filter**: All queries exclude deleted records (deleted = 0)
3. **Multiple TEAM_SUPERVISOR handling**: Take first by created_at
4. **Empty handling**:
   - No global supervisor → null
   - No member nodes → empty teams list
   - No agents on node → supervisor=null, workers=empty
