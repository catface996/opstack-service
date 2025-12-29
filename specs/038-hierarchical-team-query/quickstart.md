# Quickstart: Hierarchical Team Query

**Feature**: 038-hierarchical-team-query
**Date**: 2025-12-29

## Prerequisites

1. Application is running on port 8081
2. At least one topology exists with member nodes
3. Agents are created with different hierarchy levels
4. Global Supervisor is bound to topology
5. Team Supervisors and Workers are bound to nodes

## Quick Test Scenarios

### Scenario 1: Query Full Hierarchical Team

**Setup**:
- Topology ID 1 exists with global supervisor bound
- Has multiple member nodes with agents bound

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/hierarchical-team/query \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 1
  }'
```

**Expected Response**:
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "topologyId": 1,
    "topologyName": "Production Environment",
    "globalSupervisor": {
      "id": 1,
      "name": "Global Coordinator",
      "role": "GLOBAL_SUPERVISOR",
      "hierarchyLevel": "GLOBAL_SUPERVISOR",
      "specialty": "System-wide coordination",
      "model": "claude-3-opus"
    },
    "teams": [
      {
        "nodeId": 10,
        "nodeName": "Web Server",
        "supervisor": {
          "id": 2,
          "name": "Web Team Lead",
          "role": "SCOUTER",
          "hierarchyLevel": "TEAM_SUPERVISOR",
          "specialty": "Web diagnostics",
          "model": "gemini-2.0-flash"
        },
        "workers": [
          {
            "id": 3,
            "name": "Log Analyzer",
            "role": "WORKER",
            "hierarchyLevel": "TEAM_WORKER",
            "specialty": "Log analysis",
            "model": "gemini-2.0-flash"
          }
        ]
      }
    ]
  }
}
```

### Scenario 2: Query Topology Without Global Supervisor

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/hierarchical-team/query \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 2
  }'
```

**Expected Response** (globalSupervisor is null):
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "topologyId": 2,
    "topologyName": "Staging Environment",
    "globalSupervisor": null,
    "teams": [
      {
        "nodeId": 20,
        "nodeName": "App Server",
        "supervisor": null,
        "workers": []
      }
    ]
  }
}
```

### Scenario 3: Query Non-existent Topology

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/hierarchical-team/query \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 99999
  }'
```

**Expected Response** (404 error):
```json
{
  "code": 404,
  "message": "Topology not found: 99999",
  "success": false,
  "data": null
}
```

### Scenario 4: Empty Topology (No Member Nodes)

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/hierarchical-team/query \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 3
  }'
```

**Expected Response** (empty teams list):
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "topologyId": 3,
    "topologyName": "Empty Topology",
    "globalSupervisor": null,
    "teams": []
  }
}
```

## Data Setup for Testing

### Step 1: Create Agents

```bash
# Create Global Supervisor
curl -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Global Coordinator",
    "role": "GLOBAL_SUPERVISOR",
    "hierarchyLevel": "GLOBAL_SUPERVISOR",
    "specialty": "System-wide coordination"
  }'

# Create Team Supervisor
curl -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Web Team Lead",
    "role": "SCOUTER",
    "hierarchyLevel": "TEAM_SUPERVISOR",
    "specialty": "Web diagnostics"
  }'

# Create Team Worker
curl -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Log Analyzer",
    "role": "WORKER",
    "hierarchyLevel": "TEAM_WORKER",
    "specialty": "Log analysis"
  }'
```

### Step 2: Bind Global Supervisor to Topology

```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/bind-agent \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 1,
    "agentId": 1
  }'
```

### Step 3: Bind Agents to Nodes

```bash
# Bind Team Supervisor to node
curl -X POST http://localhost:8081/api/service/v1/nodes/bind-agent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 10,
    "agentId": 2
  }'

# Bind Team Worker to node
curl -X POST http://localhost:8081/api/service/v1/nodes/bind-agent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 10,
    "agentId": 3
  }'
```

## Verification Checklist

- [ ] Query returns complete hierarchical structure
- [ ] Global supervisor is correctly retrieved from topology
- [ ] Teams are correctly assembled from member nodes
- [ ] Agents are correctly grouped by hierarchy level
- [ ] Null values handled for missing supervisors
- [ ] Empty arrays returned for nodes without workers
- [ ] 404 returned for non-existent topology
- [ ] Soft-deleted agents are excluded
- [ ] Multiple TEAM_SUPERVISOR case: first by created_at is used
