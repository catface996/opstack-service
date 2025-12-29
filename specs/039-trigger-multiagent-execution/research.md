# Research: Trigger Multi-Agent Execution

**Feature**: 039-trigger-multiagent-execution
**Date**: 2025-12-29

## Research Topics

### 1. Spring WebClient for SSE Consumption

**Decision**: Use Spring WebFlux WebClient with `ParameterizedTypeReference` for SSE stream consumption

**Rationale**:
- WebClient is the modern, non-blocking HTTP client in Spring ecosystem
- Native support for SSE with `bodyToFlux()` method
- Can be used in Spring MVC applications alongside RestTemplate
- Supports reactive streaming without requiring full WebFlux application

**Alternatives Considered**:
1. **RestTemplate + SSE**: RestTemplate doesn't support streaming SSE natively
2. **Apache HttpClient**: More low-level, requires manual SSE parsing
3. **OkHttp**: Good alternative but less Spring integration

**Implementation Pattern**:
```java
WebClient webClient = WebClient.create(baseUrl);
Flux<ServerSentEvent<ExecutorEvent>> eventStream = webClient.get()
    .uri("/api/executor/v1/runs/stream?run_id={runId}", runId)
    .accept(MediaType.TEXT_EVENT_STREAM)
    .retrieve()
    .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<ExecutorEvent>>() {});
```

### 2. Spring MVC SSE Response

**Decision**: Use `SseEmitter` for streaming SSE responses in Spring MVC

**Rationale**:
- Native Spring MVC support for SSE
- Works with existing servlet-based architecture
- Supports timeout configuration
- Can forward reactive Flux events to synchronous SSE output

**Implementation Pattern**:
```java
@PostMapping(value = "/trigger", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter triggerExecution(@RequestBody TriggerExecutionRequest request) {
    SseEmitter emitter = new SseEmitter(0L); // No timeout
    // Subscribe to Flux and forward to emitter
    flux.subscribe(
        event -> emitter.send(SseEmitter.event().data(event)),
        emitter::completeWithError,
        emitter::complete
    );
    return emitter;
}
```

### 3. Executor Service API Integration

**Decision**: Direct HTTP integration using WebClient with DTO mapping

**Rationale**:
- Simple point-to-point integration
- No need for service mesh or API gateway complexity
- DTOs provide type safety and clear contract

**Executor API Endpoints** (from spec):
1. `POST /api/executor/v1/hierarchies/create` - Create hierarchy
2. `POST /api/executor/v1/runs/start` - Start execution run
3. `GET /api/executor/v1/runs/stream?run_id={id}` - Stream events (SSE)

**Request/Response Mapping**:

| Our System | Executor API |
|------------|--------------|
| HierarchicalTeamDTO.globalSupervisor | hierarchy.global_prompt (use agent's specialty) |
| HierarchicalTeamDTO.teams[].supervisor | hierarchy.teams[].supervisor |
| HierarchicalTeamDTO.teams[].workers | hierarchy.teams[].workers |
| TriggerExecutionRequest.userMessage | run.task |

### 4. Data Transformation Strategy

**Decision**: Create dedicated transformer class for DTO conversion

**Rationale**:
- Clean separation of concerns
- Testable transformation logic
- Easy to maintain when executor API changes

**Transformation Flow**:
```
HierarchicalTeamDTO → HierarchyTransformer → CreateHierarchyRequest
    - Map globalSupervisor.specialty → global_prompt
    - Map topology name → hierarchy name
    - For each team:
        - supervisor.name + supervisor.specialty → team supervisor
        - workers[].name + workers[].specialty → team workers
```

### 5. Error Handling Strategy

**Decision**: Two-phase error handling

**Phase 1 - Before Stream Starts**:
- Return standard HTTP error codes (400, 404, 503)
- Use existing `Result<T>` wrapper for error responses

**Phase 2 - After Stream Starts**:
- Send error events through SSE stream
- Event type: "error"
- Include error code and message in event data

**Error Event Format**:
```json
{
  "type": "error",
  "code": "EXECUTOR_ERROR",
  "message": "Failed to communicate with executor service",
  "timestamp": "2025-12-29T10:00:00Z"
}
```

### 6. Configuration Strategy

**Decision**: Externalize executor service configuration

**Configuration Properties**:
```yaml
executor:
  service:
    base-url: http://localhost:8080
    timeout:
      connect: 5000
      read: 30000
    retry:
      max-attempts: 3
      backoff: 1000
```

**Rationale**:
- Different URLs for dev/staging/production
- Configurable timeouts for different environments
- Retry configuration for resilience

### 7. Module Structure Decision

**Decision**: Add WebClient dependency to application-impl instead of creating new infrastructure module

**Rationale**:
- Simpler approach - avoid creating new Maven module
- External service client is specific to this use case
- Can always extract to separate module if needed later

**Revised Structure**:
```
application/
└── application-impl/
    └── src/main/java/com/catface996/aiops/application/impl/
        └── service/execution/
            ├── ExecutionApplicationServiceImpl.java
            └── client/ExecutorServiceClient.java  # Internal client
```

## Summary

| Topic | Decision |
|-------|----------|
| SSE Consumer | Spring WebFlux WebClient |
| SSE Producer | Spring MVC SseEmitter |
| External Integration | Direct HTTP via WebClient |
| Data Transform | Dedicated transformer class |
| Error Handling | Two-phase (HTTP then SSE) |
| Configuration | application.yml properties |
| Module Structure | Keep in application-impl |
