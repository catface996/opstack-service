---
inclusion: manual
---

# Spring Boot Best Practices

This document guides AI on how to correctly and efficiently use Spring Boot framework for application development.

**Important Note**: This project adopts DDD multi-module architecture. For project structure and layering standards, please refer to `05-ddd-multi-module-project-best-practices.md`.

## Core Principles

### 1. Convention Over Configuration Principle

**You should comply with**:
- ✅ Use Spring Boot's default configurations
- ✅ Follow DDD multi-module project structure (see `05-ddd-multi-module-project-best-practices.md`)
- ✅ Use Starter dependencies to simplify configuration
- ❌ Don't over-customize configurations

### 2. Layered Architecture Principle

**You must comply with DDD layered architecture** (see `05-ddd-multi-module-project-best-practices.md`):
- ✅ Interface layer (HTTP/Consumer/Job): Provide external service entry
- ✅ Application layer: Orchestrate business flow, coordinate domain services
- ✅ Domain layer: Core business logic and domain model
- ✅ Infrastructure layer: Technical implementation and external dependencies
- ❌ No cross-layer calls

**Dependency Direction**: Interface → Application → Domain ← Infrastructure

### 3. Dependency Injection Principle

**You should comply with**:
- ✅ Use constructor injection (recommended)
- ⚠️ Avoid field injection (@Autowired on fields)
- ✅ Use @RequiredArgsConstructor (Lombok) to simplify constructor injection

## Dependency Injection Standards

### Injection methods you should use

**Recommended: Constructor injection**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Spring automatically injects, no @Autowired needed
}
```

**Not recommended: Field injection**
```java
@Service
public class UserService {
    @Autowired  // Not recommended
    private UserRepository userRepository;
}
```

**Why constructor injection is recommended**:
- Dependencies are clear
- Easy to unit test
- Ensures dependencies are immutable
- Avoids circular dependencies

## Configuration Management Standards

### Configuration rules you should follow

**1. Use application.yml instead of application.properties**
- YAML format is clearer
- Supports hierarchical structure
- Easy to manage

**2. Multi-environment configuration**
- application.yml (common configuration)
- application-dev.yml (development environment)
- application-test.yml (test environment)
- application-prod.yml (production environment)

**3. Configuration property binding**
- Use @ConfigurationProperties to bind configurations
- Don't use @Value to inject many configurations

**4. Sensitive information handling**
- Don't store passwords in plain text in configuration files
- Use environment variables or configuration center
- Use Jasypt to encrypt sensitive information

### Configuration Class Standards

**Configuration class rules you should follow**:

**1. Use @Configuration to mark configuration classes**
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // Configure RedisTemplate
    }
}
```

**2. Configuration class naming**
- End with Config
- Self-explanatory
- Example: RedisConfig, MybatisPlusConfig

**3. Configuration class location**
- Placed in config package
- Organized by functional modules

## Interface Layer Standards (HTTP Controller)

### Controller rules you should follow

**1. Controller location**
- Placed in Interface layer's http module
- Package: `com.{company}.{system}.http.controller`

**2. Use RESTful style**
- GET: Query
- POST: Create
- PUT: Update
- DELETE: Delete

**3. Unified return format**
- Defined in common module
- All interfaces use unified Result format

**4. Parameter validation**
- Use @Valid or @Validated to validate parameters
- Use JSR-303 annotations (@NotNull, @NotBlank, @Size, etc.)

**5. Controller responsibilities**
- Only responsible for receiving requests and returning responses
- No business logic
- Call Application Service to handle business
- Convert between VO and DTO

**6. Unified exception handling**
- Use @RestControllerAdvice
- Placed in handler package of http module

### Controller Best Practices

**You should write Controller like this**:

```java
// Interface layer - http module
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserAppService userAppService;  // Call Application layer

    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        UserDTO dto = userAppService.getUserById(id);
        return Result.success(convertToVO(dto));  // DTO → VO
    }

    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userAppService.createUser(request);
        return Result.success();
    }
}
```

## Application Layer Standards

### Application Service rules you should follow

**1. Application Service location**
- Interface: application-api module
- Implementation: application-impl module
- Package: `com.{company}.{system}.application.service`

**2. Separate interface and implementation**
- Interface naming: `XxxAppService`
- Implementation naming: `XxxAppServiceImpl`

**3. Transaction management**
- Use @Transactional to manage transactions
- Minimize transaction scope
- Avoid long transactions

**4. Application Service responsibilities**
- Orchestrate business flow
- Call Domain Service (domain services)
- Call Infrastructure layer (Repository, Cache, MQ)
- Convert between DTO and Domain Entity
- No core business logic (business logic in Domain layer)

**5. Exception handling**
- Throw business exceptions
- Don't swallow exceptions
- Use custom exceptions

### Application Service Best Practices

**You should write Application Service like this**:

```java
// Application API module
public interface UserAppService {
    UserDTO getUserById(Long id);
    void createUser(CreateUserRequest request);
}

// Application Impl module
@Service
@RequiredArgsConstructor
public class UserAppServiceImpl implements UserAppService {
    private final UserRepository userRepository;  // Infrastructure layer
    private final RedisTemplate<String, Object> redisTemplate;  // Infrastructure layer
    private final UserDomainService userDomainService;  // Domain layer (if needed)

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        // Check cache first
        String key = "user:info:" + id;
        UserDTO cached = (UserDTO) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        // Query database
        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("User not found"));

        UserDTO dto = convertToDTO(entity);

        // Write to cache
        redisTemplate.opsForValue().set(key, dto, 30, TimeUnit.MINUTES);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // Orchestrate business flow
        // 1. Call Domain Service to handle core business logic
        // 2. Call Repository to save data
        UserEntity entity = new UserEntity();
        // Set properties
        userRepository.save(entity);
    }
}
```

## Domain Layer Standards

### Domain rules you should follow

**See**: `05-ddd-multi-module-project-best-practices.md`

**Key principles**:
- Domain layer uses pure business language (no technical suffixes)
- Domain layer doesn't depend on any framework
- Domain layer doesn't depend on Infrastructure layer
- Core business logic implemented in Domain Service

## Infrastructure Layer Standards (Repository)

### Repository rules you should follow

**See**: `05-ddd-multi-module-project-best-practices.md`

**1. Repository location**
- Interface: repository-api module
- Implementation: mysql-impl module (or other persistence implementation)

**2. Separate Entity and PO**
- Entity: in repository-api, pure POJO, no framework annotations
- PO: in mysql-impl, contains framework annotations
- RepositoryImpl responsible for conversion between Entity and PO

**3. Use MyBatis-Plus**
- Mapper interface operates on PO
- Repository interface operates on Entity
- Configuration classes in mysql-impl module

**4. Repository responsibilities**
- Only responsible for data access
- No business logic
- Provide persistence and queries for domain objects

## Exception Handling Standards

### Exception handling rules you should follow

**1. Custom business exceptions**
```java
public class BusinessException extends RuntimeException {
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

**2. Global exception handling**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("System exception", e);
        return Result.error(500, "System exception");
    }
}
```

**3. Exception handling principles**
- Don't swallow exceptions
- Log exceptions
- Return friendly error messages
- Don't expose sensitive information

## Logging Standards

### Logging rules you should follow

**1. Use SLF4J + Logback**
- Spring Boot integrates by default
- Don't use System.out.println

**2. Log levels**
- ERROR: Error information
- WARN: Warning information
- INFO: Important information
- DEBUG: Debug information
- TRACE: Detailed information

**3. Log content**
- Record key business operations
- Record exception information
- Record performance metrics
- Don't record sensitive information (passwords, ID numbers, etc.)

**4. Log format**
```java
@Slf4j
@Service
public class UserService {
    public void createUser(CreateUserRequest request) {
        log.info("Creating user, username: {}", request.getUsername());
        try {
            // Business logic
            log.info("User created successfully, userID: {}", userId);
        } catch (Exception e) {
            log.error("User creation failed, username: {}", request.getUsername(), e);
            throw e;
        }
    }
}
```

## Parameter Validation Standards

### Validation rules you should follow

**1. Use JSR-303 annotations**
```java
@Data
public class CreateUserRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username length must be 3-20 characters")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 20, message = "Password length must be 6-20 characters")
    private String password;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format incorrect")
    private String email;

    @NotNull(message = "Age cannot be empty")
    @Min(value = 1, message = "Age must be greater than 0")
    @Max(value = 150, message = "Age must be less than 150")
    private Integer age;
}
```

**2. Enable validation in Controller**
```java
@PostMapping
public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
    userService.createUser(request);
    return Result.success();
}
```

**3. Custom validation annotations**
- Use custom annotations for complex validation logic
- Implement ConstraintValidator interface

## Transaction Management Standards

### Transaction rules you should follow

**1. Use @Transactional annotation**
```java
@Transactional(rollbackFor = Exception.class)
public void createOrder(CreateOrderRequest request) {
    // Business logic
}
```

**2. Transaction properties**
- rollbackFor: Specify exception types for rollback (recommend Exception.class)
- readOnly: Read-only transaction (for query operations)
- propagation: Transaction propagation behavior
- isolation: Transaction isolation level

**3. Transaction scope**
- Minimize transaction scope
- Avoid long transactions
- Don't call remote services in transactions

**4. Transaction failure scenarios**
- Method is not public
- Same class method call (using this)
- Exception caught and not thrown
- Database doesn't support transactions

## Performance Optimization Standards

### Performance optimization principles you should follow

**1. Use caching**
- Use @Cacheable, @CachePut, @CacheEvict annotations
- Set reasonable cache expiration times
- Prevent cache penetration, breakdown, avalanche

**2. Asynchronous processing**
- Use @Async annotation
- Configure thread pool
- Suitable for: sending emails, SMS, logging

**3. Batch operations**
- Batch insert, batch update
- Reduce database interaction frequency

**4. Lazy loading**
- JPA use lazy loading
- Avoid N+1 query problems

**5. Connection pool configuration**
- Database connection pool (HikariCP)
- Redis connection pool
- HTTP connection pool

## Security Standards

### Security rules you should follow

**1. Parameter validation**
- Validate all external inputs
- Prevent SQL injection, XSS attacks

**2. Authentication and authorization**
- Use Spring Security or JWT
- Interface permission control

**3. Sensitive information**
- Encrypt password storage (BCrypt)
- Don't log sensitive information
- Don't return sensitive information in responses

**4. HTTPS**
- Use HTTPS in production environment
- Configure SSL certificates

## Testing Standards

### Testing rules you should follow

**1. Unit testing**
- Use JUnit 5
- Use Mockito to mock dependencies
- Test coverage > 80%

**2. Integration testing**
- Use @SpringBootTest
- Test complete business flows

**3. Test naming**
- Method name: should_expectedResult_when_condition
- Example: should_returnUser_when_userExists

## Common Errors and Corrections

### Errors you should avoid

| Error Type | Wrong Approach | Correct Approach | Reason |
|---------|---------|---------|------|
| **Field injection** | @Autowired on fields | Constructor injection | Not convenient for testing |
| **Cross-layer calls** | Controller directly calls Repository | Controller → Service → Repository | Violates layering principles |
| **Transaction failure** | Same class method call | Inject self or use AopContext | Proxy failure |
| **Long transactions** | Call remote services in transaction | Minimize transaction scope | Performance issues |
| **Swallow exceptions** | catch without handling | Log and throw | Difficult to troubleshoot |
| **Hardcoded configuration** | Write configuration in code | Use configuration files | Not convenient to maintain |
| **No expiration time** | Cache permanently valid | Set reasonable expiration time | Memory overflow |
| **SQL injection** | String concatenation SQL | Use parameterized queries | Security risk |

## Your Checklist

When writing Spring Boot code, you should check:

### Architecture Check
- [ ] Follow layered architecture (Controller → Service → Repository)
- [ ] Use constructor injection instead of field injection
- [ ] Startup class in root package
- [ ] Configuration classes in config package

### Controller Check
- [ ] Use RESTful style
- [ ] Unified return format
- [ ] Parameter validation (@Valid)
- [ ] No business logic

### Service Check
- [ ] Separate interface and implementation
- [ ] Use @Transactional to manage transactions
- [ ] Minimize transaction scope
- [ ] Correct exception handling

### Configuration Check
- [ ] Use application.yml
- [ ] Multi-environment configuration
- [ ] Encrypt sensitive information
- [ ] Configuration property binding

### Security Check
- [ ] Parameter validation
- [ ] Authentication and authorization
- [ ] Password encryption
- [ ] Don't log sensitive information

### Performance Check
- [ ] Use caching
- [ ] Asynchronous processing
- [ ] Batch operations
- [ ] Connection pool configuration

## Key Principles Summary

### Architecture Principles
1. **Convention over configuration**: Use Spring Boot default configurations
2. **Layered architecture**: Controller → Service → Repository
3. **Dependency injection**: Use constructor injection
4. **Separate interface and implementation**: Easy to extend and test

### Development Principles
1. **Unified return format**: Easy for frontend processing
2. **Unified exception handling**: Improve code maintainability
3. **Parameter validation**: Ensure data security
4. **Transaction management**: Ensure data consistency

### Performance Principles
1. **Use caching**: Reduce database pressure
2. **Asynchronous processing**: Improve response speed
3. **Batch operations**: Reduce network overhead
4. **Connection pool configuration**: Improve resource utilization

### Security Principles
1. **Parameter validation**: Prevent malicious input
2. **Authentication and authorization**: Protect interface security
3. **Encrypt sensitive information**: Protect user privacy
4. **Use HTTPS**: Ensure transmission security

## Key Benefits

Following these standards can achieve:

- ✅ Clear code structure, easy to maintain
- ✅ Unified development standards, easy for team collaboration
- ✅ High-performance applications, improve user experience
- ✅ Secure systems, protect user data
- ✅ Easy-to-test code, ensure quality
- ✅ Fast development efficiency, reduce costs
