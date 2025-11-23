---
inclusion: manual
---

# Spring Cloud Best Practices

This document guides AI on how to correctly and efficiently use Spring Cloud to build microservice architecture.

## Core Principles

### 1. Service Decomposition Principles

**You should comply with**:
- ✅ Decompose services by business domain (DDD)
- ✅ Single responsibility principle
- ✅ Service autonomy (independent deployment, independent database)
- ❌ Don't over-decompose (avoid distributed transactions)

### 2. Service Communication Principles

**You should comply with**:
- ✅ Use REST API or RPC communication
- ✅ Use service registration and discovery
- ✅ Use load balancing
- ❌ Don't directly use IP addresses for calls

### 3. Fault Tolerance Design Principles

**You should comply with**:
- ✅ Service degradation (Fallback)
- ✅ Circuit breaking (Circuit Breaker)
- ✅ Rate limiting (Rate Limiting)
- ✅ Timeout control

## Microservice Architecture Components

### Core components you should know

| Component | Purpose | Recommended Solutions |
|------|------|---------|
| **Service Registration & Discovery** | Service registration, discovery | Nacos, Eureka |
| **Configuration Center** | Centralized configuration management | Nacos Config, Apollo |
| **API Gateway** | Unified entry, routing, authentication | Spring Cloud Gateway |
| **Load Balancing** | Client-side load balancing | Spring Cloud LoadBalancer |
| **Service Invocation** | Declarative HTTP client | OpenFeign |
| **Circuit Breaking & Degradation** | Fault tolerance protection | Sentinel, Resilience4j |
| **Distributed Tracing** | Distributed tracing | Sleuth + Zipkin |
| **Message Bus** | Configuration refresh, event notification | Spring Cloud Bus |

## Service Registration and Discovery

### Registration and discovery rules you should follow

**1. Use Nacos as registration center**
- Supports service registration and discovery
- Supports configuration management
- Supports health checks

**2. Service naming conventions**
- Use lowercase letters and hyphens
- Self-explanatory
- Example: `user-service`, `order-service`

**3. Service instance configuration**
- Configure service name
- Configure service port
- Configure health check path

**4. Service discovery methods**
- Use OpenFeign for declarative calls
- Use RestTemplate + @LoadBalanced
- Use WebClient + @LoadBalanced

### Nacos Configuration Example

**You should configure Nacos like this**:

```yaml
spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
```

## Configuration Center

### Configuration center rules you should follow

**1. Use Nacos Config to manage configurations**
- Centralized configuration management
- Supports dynamic refresh
- Supports multi-environment configuration

**2. Configuration file naming conventions**
- Format: `${spring.application.name}-${profile}.${file-extension}`
- Example: `user-service-dev.yml`

**3. Configuration priority**
- Nacos Config > application.yml
- profile configuration > default configuration

**4. Configuration refresh**
- Use @RefreshScope annotation
- Supports dynamic configuration refresh

### Nacos Config Configuration Example

**You should configure Nacos Config like this**:

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        file-extension: yml
        refresh-enabled: true
```

## API Gateway

### Gateway rules you should follow

**1. Use Spring Cloud Gateway**
- Based on WebFlux, better performance
- Supports dynamic routing
- Supports filter chains

**2. Gateway responsibilities**
- Route forwarding
- Unified authentication
- Rate limiting and circuit breaking
- Logging
- CORS handling

**3. Route configuration**
- Configure routes using configuration files
- Use predicates to match requests
- Use filters to process requests

**4. Gateway security**
- JWT token validation
- Blacklist and whitelist
- Prevent SQL injection, XSS attacks

### Gateway Route Configuration Example

**You should configure routes like this**:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## Service Invocation (OpenFeign)

### Feign rules you should follow

**1. Use OpenFeign for declarative calls**
- Simplify inter-service calls
- Supports load balancing
- Supports circuit breaking and degradation

**2. Feign interface definition**
- Interface naming: `XxxFeignClient`
- Use @FeignClient annotation
- Specify service name

**3. Feign configuration**
- Configure timeout
- Configure retry strategy
- Configure log level

**4. Feign degradation**
- Use fallback or fallbackFactory
- Provide degradation logic

### Feign Interface Example

**You should define Feign interface like this**:

```java
@FeignClient(
    name = "user-service",
    fallbackFactory = UserFeignClientFallbackFactory.class
)
public interface UserFeignClient {

    @GetMapping("/api/users/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    @PostMapping("/api/users")
    Result<Void> createUser(@RequestBody CreateUserRequest request);
}

@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {

    @Override
    public UserFeignClient create(Throwable cause) {
        return new UserFeignClient() {
            @Override
            public Result<UserDTO> getUserById(Long id) {
                log.error("Failed to call user service", cause);
                return Result.error("User service temporarily unavailable");
            }

            @Override
            public Result<Void> createUser(CreateUserRequest request) {
                log.error("Failed to call user service", cause);
                return Result.error("User service temporarily unavailable");
            }
        };
    }
}
```

## Circuit Breaking and Degradation (Sentinel)

### Circuit breaking and degradation rules you should follow

**1. Use Sentinel for circuit breaking and degradation**
- Traffic control
- Circuit breaking and degradation
- System load protection

**2. Circuit breaking strategies**
- Slow call ratio: response time exceeds threshold
- Error ratio: error ratio exceeds threshold
- Error count: error count exceeds threshold

**3. Degradation strategies**
- Return default value
- Return cached data
- Return friendly message

**4. Rate limiting strategies**
- QPS rate limiting
- Thread count rate limiting
- Hot parameter rate limiting

### Sentinel Configuration Example

**You should configure Sentinel like this**:

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
        port: 8719
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-sentinel
            groupId: DEFAULT_GROUP
            rule-type: flow
```

## Distributed Transactions

### Distributed transaction rules you should follow

**1. Try to avoid distributed transactions**
- Decompose services reasonably
- Use eventual consistency

**2. Distributed transaction solutions**
- Seata (AT, TCC, SAGA modes)
- Local message table
- Reliable message eventual consistency
- Best effort notification

**3. Seata usage recommendations**
- AT mode: suitable for simple scenarios
- TCC mode: suitable for scenarios with high consistency requirements
- SAGA mode: suitable for long transactions

**4. Eventual consistency solutions**
- Use message queues (RocketMQ, Kafka)
- Use scheduled task compensation
- Use event-driven

## Distributed Tracing

### Distributed tracing rules you should follow

**1. Use Sleuth + Zipkin**
- Sleuth: generates TraceId and SpanId
- Zipkin: collects and displays trace data

**2. TraceId propagation**
- Automatically propagated in HTTP headers
- Automatically printed in logs

**3. Trace data collection**
- Configure sampling rate (production environment recommend 0.1)
- Configure data reporting method

**4. Trace analysis**
- Analyze slow requests
- Analyze exception requests
- Analyze service dependency relationships

### Sleuth Configuration Example

**You should configure Sleuth like this**:

```yaml
spring:
  sleuth:
    sampler:
      probability: 0.1  # Sampling rate 10%
  zipkin:
    base-url: http://localhost:9411
    sender:
      type: web
```

## Service Monitoring

### Monitoring rules you should follow

**1. Use Spring Boot Actuator**
- Expose health check endpoints
- Expose metrics endpoints
- Expose info endpoints

**2. Use Prometheus + Grafana**
- Prometheus: collect metrics
- Grafana: visualization display

**3. Monitoring metrics**
- JVM metrics (memory, GC, threads)
- HTTP metrics (QPS, response time, error rate)
- Database metrics (connections, slow queries)
- Redis metrics (hit rate, connections)

**4. Alert rules**
- CPU usage > 80%
- Memory usage > 80%
- Error rate > 1%
- Response time > 1s

### Actuator Configuration Example

**You should configure Actuator like this**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

## Log Management

### Logging rules you should follow

**1. Unified log format**
- Include TraceId and SpanId
- Include service name
- Include timestamp

**2. Log collection**
- Use ELK (Elasticsearch + Logstash + Kibana)
- Use Loki + Grafana

**3. Log levels**
- ERROR: Error logs
- WARN: Warning logs
- INFO: Important business logs
- DEBUG: Debug logs

**4. Log content**
- Record request input and output
- Record exception stack
- Don't record sensitive information

## Service Security

### Security rules you should follow

**1. Authentication and authorization**
- Use JWT tokens
- Gateway unified authentication
- Inter-service calls use internal tokens

**2. Interface security**
- Parameter validation
- Prevent SQL injection
- Prevent XSS attacks
- Prevent CSRF attacks

**3. Data security**
- Encrypt sensitive data
- HTTPS transmission
- Data masking

**4. Rate limiting protection**
- Gateway rate limiting
- Service rate limiting
- Interface rate limiting

## Service Deployment

### Deployment rules you should follow

**1. Containerized deployment**
- Use Docker containerization
- Use Kubernetes orchestration

**2. Configuration externalization**
- Use configuration center
- Use environment variables
- Don't hardcode configuration in images

**3. Health checks**
- Configure Liveness Probe
- Configure Readiness Probe

**4. Rolling updates**
- Canary release
- Blue-green deployment
- Gray release

## Common Errors and Corrections

### Errors you should avoid

| Error Type | Wrong Approach | Correct Approach | Reason |
|---------|---------|---------|------|
| **Over-decomposition** | Too fine-grained services | Decompose reasonably by business domain | Increases complexity |
| **Direct calls** | Use IP addresses for calls | Use service names for calls | No load balancing support |
| **No circuit breaking** | Don't handle service exceptions | Configure circuit breaking and degradation | Avalanche effect |
| **No timeout control** | Don't set timeout | Set reasonable timeout | Resource exhaustion |
| **Distributed transactions** | Overuse distributed transactions | Use eventual consistency | Performance issues |
| **No tracing** | Don't configure tracing | Configure Sleuth + Zipkin | Difficult to troubleshoot |
| **No monitoring** | Don't configure monitoring | Configure Prometheus + Grafana | Problems not discovered timely |
| **Hardcoded configuration** | Write configuration in code | Use configuration center | Not convenient to maintain |

## Your Checklist

When building microservices, you should check:

### Architecture Check
- [ ] Service decomposition reasonable (by business domain)
- [ ] Service autonomy (independent deployment, independent database)
- [ ] Service communication uses registration center
- [ ] Configuration uses configuration center

### Fault Tolerance Check
- [ ] Configured circuit breaking and degradation
- [ ] Configured timeout control
- [ ] Configured retry strategy
- [ ] Configured rate limiting protection

### Monitoring Check
- [ ] Configured distributed tracing
- [ ] Configured service monitoring
- [ ] Configured log collection
- [ ] Configured alert rules

### Security Check
- [ ] Configured authentication and authorization
- [ ] Configured interface security
- [ ] Configured data encryption
- [ ] Configured rate limiting protection

### Deployment Check
- [ ] Use containerized deployment
- [ ] Configuration externalization
- [ ] Configured health checks
- [ ] Support rolling updates

## Key Principles Summary

### Architecture Principles
1. **Reasonable decomposition**: Decompose by business domain, avoid over-decomposition
2. **Service autonomy**: Independent deployment, independent database
3. **Service communication**: Use registration center and load balancing
4. **Configuration management**: Use configuration center for centralized management

### Fault Tolerance Principles
1. **Circuit breaking and degradation**: Prevent avalanche effect
2. **Timeout control**: Avoid resource exhaustion
3. **Rate limiting protection**: Protect system stability
4. **Retry strategy**: Improve success rate

### Monitoring Principles
1. **Distributed tracing**: Quickly locate problems
2. **Service monitoring**: Real-time grasp of system status
3. **Log collection**: Easy to troubleshoot
4. **Alert notification**: Discover problems timely

### Security Principles
1. **Authentication and authorization**: Protect interface security
2. **Data encryption**: Protect sensitive data
3. **Rate limiting protection**: Prevent malicious attacks
4. **Parameter validation**: Prevent illegal input

## Key Benefits

Following these standards can achieve:

- ✅ High-availability microservice architecture
- ✅ Quickly locate and solve problems
- ✅ Flexible service scaling capabilities
- ✅ Secure and reliable systems
- ✅ Easy to maintain and upgrade
- ✅ Improve development efficiency
