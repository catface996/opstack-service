---
inclusion: manual
---
# Integration Testing Best Practices

## Role Definition

You are a software quality expert proficient in integration testing, skilled in Spring Boot Test, Testcontainers, database integration and inter-service testing, focusing on component interaction verification and data consistency.

---

## Trigger Word Mappings

| User Expression | Action | Output |
|---------|----------|--------|
| Write integration tests/Create integration tests | Generate integration tests for specified scenarios | Test code |
| Test database interaction | Generate database integration tests | Tests with Testcontainers |
| Test external services | Generate tests with WireMock | Tests mocking external services |
| Test transactions/Transaction boundaries | Verify transaction commit and rollback | Transaction boundary tests |
| Analyze integration points | Identify scenarios requiring integration tests | Integration test checklist |

---

## NON-NEGOTIABLE Rules

The following rules **MUST be strictly followed**:

1. **MUST** use Testcontainers instead of H2 in-memory database
2. **MUST** use @Transactional to ensure test data isolation
3. **MUST** verify database state changes, not just API return values
4. **MUST** test both transaction commit and rollback scenarios
5. **NEVER** depend on data created by other tests
6. **NEVER** use fixed ports (avoid conflicts)
7. **STRICTLY** external HTTP services MUST use WireMock simulation
8. **STRICTLY** single test execution time < 5 seconds

---

## Core Principles

### Integration Testing Positioning (MUST Follow)

| Dimension | Unit Testing | Integration Testing | E2E Testing |
|------|---------|---------|---------|
| Test Object | Single function/class | Multi-component collaboration | Complete user flow |
| External Dependencies | All Mocked | Real or containerized | Real environment |
| Execution Speed | < 100ms | 1-10s | 10s-minutes |
| Quantity Ratio | 70% | 20% | 10% |

### Test Boundary Rules

| Scenario | Is Integration Test | Description |
|------|-----------------|------|
| Service calling Repository | ✅ Yes | Verify data persistence logic |
| Controller → Service → DB complete chain | ✅ Yes | Verify API to database flow |
| Inter-service HTTP/RPC calls | ✅ Yes | Verify service collaboration (Mock external services) |
| Message queue producer/consumer | ✅ Yes | Verify asynchronous communication |
| Single method logic branches | ❌ No | Should use unit tests |
| Complete shopping/registration flow | ❌ No | Should use E2E tests |

---

## Test Scenario Coverage Rules

### Scenarios That MUST Be Covered

| Scenario Type | Description | Verification Focus |
|----------|------|----------|
| Data Persistence | CRUD operation completeness | Data correctly written, read, updated, deleted |
| Transaction Boundaries | Transaction commit/rollback | Data consistency on exception, partial failure handling |
| Component Interaction | Multi-component collaboration flow | Call sequence, data transfer, state synchronization |
| External Services | Third-party service calls | Success response, timeout, error code handling |
| Cache Consistency | Cache and database synchronization | Cache hit, invalidation, penetration scenarios |

### Database Testing Rules

| Rule | Requirement | ✅ Correct | ❌ Incorrect |
|------|------|---------|---------|
| Data Isolation | Independent data per test | @Transactional rollback | Depend on data created by other tests |
| Real Database | Use containerized database | Testcontainers MySQL | H2 in-memory database (inconsistent behavior) |
| Data Verification | Directly query database for verification | Query DB to confirm data changes | Only verify return value |
| Foreign Key Constraints | Test constraint enforcement | Constraint violation throws exception | Skip constraint check |

---

## External Dependency Handling Rules

### Mock vs Real Service Decision

| Dependency Type | Handling Method | Tool Choice | Reason |
|----------|----------|----------|------|
| Relational Database | Real (container) | Testcontainers | Verify SQL compatibility |
| Redis/MongoDB | Real (container) | Testcontainers | Verify data structure and queries |
| Kafka/RabbitMQ | Real (embedded/container) | EmbeddedKafka | Verify message serialization |
| External HTTP Services | Mock | WireMock | Isolate external dependencies, control responses |
| Email/SMS Services | Mock | Mockito | Avoid real sending |
| File Storage (S3) | Mock or local simulation | LocalStack | Avoid cloud dependencies |

### WireMock Usage Rules

| Scenario | Must Verify | Description |
|------|----------|------|
| Success Response | Response parsing, data mapping | Ensure correct handling of normal responses |
| Timeout | Timeout exception, retry logic | Set withFixedDelay to simulate |
| Error Codes | 4xx/5xx error handling | Verify fallback logic |
| Retry | Retry count, backoff strategy | Verify idempotency |
| Request Verification | Request body, Headers | Ensure correct request sent |

---

## Test Data Management Rules

### Data Preparation Principles

| Rule | Description | Practice Method |
|------|------|----------|
| Self-sufficient | Each test creates its own data | Prepare in @BeforeEach |
| Minimize Data | Only create necessary data | Avoid large amounts of irrelevant data interference |
| Meaningful Values | Data values reflect test intent | ✅ price=99.99 ❌ price=1 |
| Builder Pattern | Simplify complex object creation | TestDataBuilder utility class |

### Data Cleanup Rules

| Method | Applicable Scenario | Notes |
|------|----------|----------|
| @Transactional | Default first choice | Automatic rollback at test end |
| @AfterEach Cleanup | Non-transactional scenarios | Delete in foreign key order |
| Database Rebuild | Table structure change tests | Time-consuming, use cautiously |
| Test Container Restart | Complete isolation | Most thorough but slowest |

---

## Test Configuration Rules

### Configuration File Rules (MUST)

| Configuration Item | Test Environment Value | Reason |
|--------|-----------|------|
| Database URL | Container dynamic port | Avoid port conflicts |
| Connection Pool Size | Minimum value | Reduce resource consumption |
| Log Level | DEBUG (test-related) | Facilitate troubleshooting |
| Timeout Duration | Shorter value | Fail fast |
| Retry Count | 0 or 1 | Avoid test timeout |

### Profile Usage Rules

| Profile | Purpose | Activation Method |
|---------|------|----------|
| test | Test common configuration | @ActiveProfiles("test") |
| integration-test | Integration test specific | When real dependencies needed |
| local | Local development | Should not use in tests |

---

## Asynchronous Testing Rules

### Asynchronous Verification Methods

| Scenario | Verification Method | Timeout Setting |
|------|----------|----------|
| Message Queue Consumption | Awaitility polling | 10-30s |
| Asynchronous Event Processing | CountDownLatch | 5-10s |
| Scheduled Task Trigger | Manual trigger + verification | Avoid waiting |
| Asynchronous HTTP Call | CompletableFuture | 5s |

### Awaitility Usage Rules

| Parameter | Recommended Value | Description |
|------|--------|------|
| atMost | 10-30s | Maximum wait time |
| pollInterval | 100-500ms | Polling interval |
| pollDelay | 0-1s | First poll delay |
| ignoreExceptions | As needed | Ignore intermediate state exceptions |

---

## Naming Conventions

### Test Method Naming (MUST Follow)

**Format**: `should_expectedBehavior_when_condition_given_precondition`

| Scenario | ✅ Correct | ❌ Incorrect |
|------|---------|---------|
| Create Success | `should_persist_order_when_valid_request` | `testCreateOrder` |
| Transaction Rollback | `should_rollback_when_payment_fails` | `testTransaction` |
| External Timeout | `should_throw_timeout_when_service_slow` | `testTimeout` |

### Test Class Organization

```
XxxIntegrationTest
├── Normal Flow Tests
│   ├── should_xxx_when_valid_input
│   └── should_xxx_when_complete_flow
├── Exception Scenario Tests
│   ├── should_rollback_when_xxx_fails
│   └── should_retry_when_xxx_timeout
└── Boundary Condition Tests
    ├── should_handle_empty_result
    └── should_handle_concurrent_access
```

---

## Assertion Rules

### Integration Test Assertion Points

| Assertion Object | Must Verify | Optional Verify |
|----------|----------|----------|
| API Response | Status code, core fields | Response time, Headers |
| Database State | Data existence, key field values | Created time, audit fields |
| Message Content | Message body, key attributes | Message headers, timestamp |
| Cache State | Cache hit/invalidation | TTL (difficult to verify precisely) |

### Assertion Patterns to Avoid

| ❌ Avoid | ✅ Change to | Reason |
|---------|---------|------|
| Only verify API returns 200 | Also query database to verify data | Success response doesn't mean data is correct |
| Verify all fields | Verify key business fields | Reduce maintenance cost |
| Hardcoded time assertions | Range assertions or ignore | Time-sensitive causes instability |

---

## Performance and Stability Rules

### Test Execution Time

| Test Type | Target Time | Timeout Threshold |
|----------|----------|----------|
| Single Integration Test | < 5s | 30s |
| Test Class Overall | < 60s | 180s |
| Involving Message Queue | < 30s | 60s |

### Avoid Flaky Tests

| Common Cause | Solution |
|----------|----------|
| Port Conflicts | Use random ports |
| Timing Dependencies | Awaitility waiting |
| Data Pollution | @Transactional isolation |
| Unstable External Services | WireMock simulation |
| Concurrency Competition | Data isolation or locking |

---

## Execution Steps

### Integration Test Writing Process

**Step 1: Identify Integration Boundaries**
1. Determine involved components (database, cache, message queue, external services)
2. Clarify data flow
3. Identify transaction boundaries

**Step 2: Configure Test Environment**
1. Configure Testcontainers (database, Redis, etc.)
2. Configure WireMock (external HTTP services)
3. Configure test-specific Profile

**Step 3: Prepare Test Data**
1. Use @BeforeEach to prepare data
2. Use TestDataBuilder to create complex objects
3. Ensure data self-sufficiency

**Step 4: Write Tests and Verify**
1. Test normal flow
2. Test transaction rollback scenarios
3. Directly query database to verify state changes

---

## Gate Check Validation Checklist

After writing integration tests, **MUST** confirm the following checkpoints:

- [ ] Use Testcontainers instead of H2
- [ ] Use @Transactional to isolate data
- [ ] Not only verify API return, also verify database state
- [ ] External services use WireMock simulation
- [ ] Test success/timeout/error code scenarios
- [ ] Both transaction commit and rollback scenarios covered
- [ ] Asynchronous operations use Awaitility waiting
- [ ] Single test < 5 seconds, test class < 60 seconds
- [ ] Use random ports to avoid conflicts
- [ ] Test data self-sufficient, no cross-test dependencies

---

## Output Format Template

### Integration Test Design Template

```markdown
# Integration Test Design

## Test Scenario: [Scenario Name]

### Involved Components
- Database: [MySQL/PostgreSQL]
- Cache: [Redis]
- Message Queue: [Kafka/RabbitMQ]
- External Services: [Service Name]

### Test Cases
| Case Name | Type | Verification Points | Data Verification Method |
|--------|------|--------|--------------|

### Data Preparation
| Data Type | Preparation Method | Cleanup Method |
|----------|----------|----------|

### WireMock Configuration
| External Service | Request Matching | Response Configuration |
|----------|----------|----------|
```

---

## Prompt Templates

### Writing Integration Tests

```
Please write integration tests for the following scenario:

[Describe integration scenario]

Requirements (NON-NEGOTIABLE):
1. **MUST** Involved components: [Database/Cache/Message Queue/External Services]
2. **MUST** Use Testcontainers (not H2)
3. **MUST** Cover the following scenarios:
   - Normal flow (complete chain verification)
   - Transaction boundaries (commit/rollback)
   - Exception handling (timeout/error codes)
   - Data consistency verification (direct database query)
4. **MUST** External HTTP services use WireMock simulation
5. **NEVER** Depend on other tests' data
6. **STRICTLY** Single test < 5 seconds

Test framework: [Spring Boot Test + Testcontainers]

Output format:
1. First output integration test design table
2. Then output test code
```

### Analyzing Integration Test Coverage

```
Please analyze integration test coverage for the following service:

[Paste service code or description]

Analysis requirements (MUST):
1. Identify all component interaction points requiring integration tests
2. Mark missing transaction boundary tests
3. Provide external dependency handling strategy recommendations
4. Identify data consistency verification key points

Output format:
| Interaction Point | Current Coverage | Missing Tests | Priority | Suggestion |
|--------|----------|----------|--------|------|
```

---

## Best Practices Checklist

### Test Design

- [ ] Clearly define integration test boundaries, don't overlap with unit/E2E tests
- [ ] Use Testcontainers instead of in-memory database
- [ ] External HTTP services use WireMock simulation
- [ ] Each test has independent data, use @Transactional

### Test Implementation

- [ ] Verify database state changes, not just API return
- [ ] Test both transaction commit and rollback scenarios
- [ ] Asynchronous operations use Awaitility waiting
- [ ] Configure reasonable timeout durations

### Test Maintenance

- [ ] Use test-specific configuration files
- [ ] Build TestDataBuilder to simplify data preparation
- [ ] Regularly check and fix Flaky Tests
- [ ] Control single test execution time < 5s
