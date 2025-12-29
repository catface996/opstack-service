# Feature Specification: Trigger Multi-Agent Execution

**Feature Branch**: `039-trigger-multiagent-execution`
**Created**: 2025-12-29
**Status**: Draft
**Input**: User description: "提供给前端一个endpoint来触发多智能体协作的执行，接收的参数有：TopologyId，用户输入的信息；后端能根据拓扑图，查询到相关的节点和关系数据，以及对应的Hierarchical team agent，然后将这些信息通过 executor的接口，调用executor的服务来获取stream event，将获取的stream event返回给前端"

## Overview

This feature provides a frontend endpoint to trigger multi-agent collaboration execution. The system receives a topology ID and user message, queries the topology's hierarchical team structure, creates a hierarchy in the executor service, starts a run, and streams execution events back to the frontend via Server-Sent Events (SSE).

## External Service Integration

### Op-Stack Executor API

The system integrates with the Op-Stack Executor service at `http://localhost:8080`:

- **Create Hierarchy**: `POST /api/executor/v1/hierarchies/create` - Creates a hierarchy team structure
- **Start Run**: `POST /api/executor/v1/runs/start` - Starts execution with a task
- **Stream Events**: `GET /api/executor/v1/runs/stream?run_id={id}` - Streams execution events via SSE

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Execute Multi-Agent Task via Topology (Priority: P0)

As a frontend user, I want to trigger a multi-agent collaboration task by providing a topology ID and my task description, so that the system automatically orchestrates the appropriate agents and streams the execution results back to me in real-time.

**Why this priority**: This is the core MVP functionality - enabling users to leverage their configured topology and agent hierarchy to execute collaborative AI tasks.

**Independent Test**: Can be fully tested by calling the endpoint with a valid topology ID and message, verifying that SSE events are streamed back containing agent execution progress and results.

**Acceptance Scenarios**:

1. **Given** a topology with bound Global Supervisor and hierarchical teams, **When** user sends a POST request with topologyId and message, **Then** system streams execution events via SSE
2. **Given** a valid topology and message, **When** execution completes, **Then** system streams final result and closes the connection
3. **Given** a running execution, **When** executor service sends intermediate events, **Then** system forwards each event to frontend in real-time

---

### User Story 2 - Handle Missing Topology Configuration (Priority: P1)

As a frontend user, when I try to execute a task on a topology that doesn't exist or isn't properly configured, I want to receive a clear error message so I know what needs to be fixed.

**Why this priority**: Error handling is essential for usability - users need to know why execution failed and how to resolve it.

**Independent Test**: Can be tested by attempting execution on a non-existent topology or one without a Global Supervisor.

**Acceptance Scenarios**:

1. **Given** a non-existent topology ID, **When** user sends execution request, **Then** system returns 404 error with clear message
2. **Given** a topology without Global Supervisor, **When** user sends execution request, **Then** system returns 400 error indicating Global Supervisor is required
3. **Given** a topology with no teams configured, **When** user sends execution request, **Then** system returns 400 error indicating teams are required

---

### User Story 3 - Handle Executor Service Failures (Priority: P1)

As a frontend user, when the executor service is unavailable or returns an error, I want to receive appropriate error information so I understand the execution failed due to service issues.

**Why this priority**: Graceful handling of external service failures is critical for production reliability.

**Independent Test**: Can be tested by simulating executor service failures or timeouts.

**Acceptance Scenarios**:

1. **Given** executor service is unavailable, **When** user sends execution request, **Then** system returns 503 Service Unavailable with retry suggestion
2. **Given** executor service returns error during hierarchy creation, **When** processing request, **Then** system returns error details to user
3. **Given** executor service returns error during run start, **When** processing request, **Then** system returns error details and cleans up created hierarchy

---

### Edge Cases

- What happens when the topology has agents but no workers in any team?
- How does system handle executor service timeout during long-running tasks?
- What happens if SSE connection is interrupted mid-stream?
- How does system handle concurrent execution requests for the same topology?
- What happens when agent model information is missing or invalid?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a POST endpoint to trigger multi-agent execution
- **FR-002**: System MUST accept topologyId and userMessage as input parameters
- **FR-003**: System MUST validate topology exists before proceeding
- **FR-004**: System MUST validate topology has a bound Global Supervisor Agent
- **FR-005**: System MUST query the hierarchical team structure for the topology
- **FR-006**: System MUST transform hierarchical team data to executor hierarchy format
- **FR-007**: System MUST call executor service to create hierarchy
- **FR-008**: System MUST call executor service to start run with user message as task
- **FR-009**: System MUST stream executor events to frontend via SSE
- **FR-010**: System MUST forward all event types from executor to frontend
- **FR-011**: System MUST close SSE connection when executor stream completes
- **FR-012**: System MUST handle executor service errors gracefully
- **FR-013**: System MUST return appropriate HTTP error codes for validation failures

### Non-Functional Requirements

- **NFR-001**: SSE connection MUST support streaming without buffering
- **NFR-002**: System MUST handle concurrent execution requests independently
- **NFR-003**: API MUST follow existing POST-only design pattern

### Key Entities

- **ExecutionRequest**: Request containing topologyId and userMessage
- **ExecutorHierarchy**: Transformed hierarchy structure for executor service (name, global_prompt, teams)
- **ExecutorRun**: Run information from executor service (run_id, hierarchy_id, task)
- **ExecutionEvent**: Streamed event from executor containing type, agent, content, timestamp

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Frontend can successfully trigger execution and receive streamed events within 2 seconds of request
- **SC-002**: All executor events are forwarded to frontend without loss or delay
- **SC-003**: Error responses provide actionable information for troubleshooting
- **SC-004**: API documentation (Swagger) accurately reflects the SSE endpoint behavior
