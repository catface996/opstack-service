# Quickstart: Trigger Multi-Agent Execution

**Feature**: 039-trigger-multiagent-execution
**Date**: 2025-12-29

## Prerequisites

1. **Op-Stack Service running** on port 8081
2. **Op-Stack Executor running** on port 8080
3. **Valid topology** with:
   - Bound Global Supervisor Agent
   - At least one team with supervisor and workers

## API Endpoint

```
POST /api/service/v1/executions/trigger
Content-Type: application/json
Accept: text/event-stream
```

## Quick Test Scenarios

### Scenario 1: Successful Execution

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/executions/trigger \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "topologyId": 4,
    "userMessage": "Analyze the system performance and provide recommendations"
  }' \
  --no-buffer
```

**Expected Response** (SSE stream):
```
event: message
data: {"type":"thinking","agentName":"Global Monitor","agentRole":"GLOBAL_SUPERVISOR","content":"Processing request...","timestamp":"2025-12-29T10:00:00"}

event: message
data: {"type":"message","agentName":"Global Monitor","agentRole":"GLOBAL_SUPERVISOR","content":"Delegating analysis to team supervisors...","timestamp":"2025-12-29T10:00:01"}

event: message
data: {"type":"thinking","agentName":"App Server Supervisor","agentRole":"TEAM_SUPERVISOR","content":"Analyzing application metrics...","timestamp":"2025-12-29T10:00:02"}

event: message
data: {"type":"tool_call","agentName":"Metrics Worker","agentRole":"WORKER","content":"Calling cpu_monitor tool...","timestamp":"2025-12-29T10:00:03","metadata":{"toolName":"cpu_monitor"}}

event: message
data: {"type":"tool_result","agentName":"Metrics Worker","agentRole":"WORKER","content":"CPU usage: 75%","timestamp":"2025-12-29T10:00:04"}

event: message
data: {"type":"message","agentName":"Global Monitor","agentRole":"GLOBAL_SUPERVISOR","content":"Analysis complete. CPU usage is elevated at 75%. Recommend scaling application instances.","timestamp":"2025-12-29T10:00:10"}

event: message
data: {"type":"complete","content":"Execution completed successfully","timestamp":"2025-12-29T10:00:11"}
```

### Scenario 2: Topology Not Found

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/executions/trigger \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 9999,
    "userMessage": "Test message"
  }'
```

**Expected Response** (HTTP 404):
```json
{
  "code": 404001,
  "message": "Topology not found",
  "success": false,
  "data": null
}
```

### Scenario 3: Missing Global Supervisor

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/executions/trigger \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 5,
    "userMessage": "Test message"
  }'
```

**Expected Response** (HTTP 400):
```json
{
  "code": 400001,
  "message": "Topology does not have a bound Global Supervisor Agent",
  "success": false,
  "data": null
}
```

### Scenario 4: Executor Service Unavailable

When executor service is down:

**Request**:
```bash
curl -X POST http://localhost:8081/api/service/v1/executions/trigger \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 4,
    "userMessage": "Test message"
  }'
```

**Expected Response** (HTTP 503):
```json
{
  "code": 503001,
  "message": "Executor service is unavailable. Please try again later.",
  "success": false,
  "data": null
}
```

### Scenario 5: Error During Streaming

If executor returns an error after streaming starts:

**SSE Error Event**:
```
event: message
data: {"type":"error","content":"Executor error: Agent timeout exceeded","timestamp":"2025-12-29T10:00:30","metadata":{"code":"EXECUTOR_TIMEOUT"}}
```

## Validation Checklist

| # | Scenario | Expected | Verified |
|---|----------|----------|----------|
| 1 | Valid topology + message | SSE stream with events | [ ] |
| 2 | Non-existent topology | 404 error | [ ] |
| 3 | Topology without Global Supervisor | 400 error | [ ] |
| 4 | Topology without teams | 400 error | [ ] |
| 5 | Executor service down | 503 error | [ ] |
| 6 | Empty userMessage | 400 validation error | [ ] |
| 7 | Very long userMessage (>10000 chars) | 400 validation error | [ ] |

## Setup Test Data

If you need to create test data:

### 1. Create a Topology
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Production Environment",
    "code": "test-prod-001",
    "description": "Test topology for execution"
  }'
```

### 2. Create and Bind Global Supervisor Agent
```bash
# Create agent with GLOBAL_SUPERVISOR role
curl -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Global Monitor",
    "role": "GLOBAL_SUPERVISOR",
    "hierarchyLevel": 0,
    "specialty": "System-wide monitoring and coordination",
    "model": "claude-3-opus"
  }'

# Bind to topology
curl -X POST http://localhost:8081/api/service/v1/topologies/bind-global-supervisor-agent \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": <TOPOLOGY_ID>,
    "agentId": <AGENT_ID>
  }'
```

### 3. Add Nodes and Teams
```bash
# Add node to topology
curl -X POST http://localhost:8081/api/service/v1/topologies/add-members \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": <TOPOLOGY_ID>,
    "nodeIds": [<NODE_ID>]
  }'

# Bind supervisor agent to node
curl -X POST http://localhost:8081/api/service/v1/nodes/bind-agents \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": <NODE_ID>,
    "agentIds": [<SUPERVISOR_AGENT_ID>, <WORKER_AGENT_ID>]
  }'
```

## Swagger Documentation

After starting the application, access Swagger UI at:
```
http://localhost:8081/swagger-ui.html
```

The new endpoint will be available under the "Execution" tag.
