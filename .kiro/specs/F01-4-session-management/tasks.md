# 会话管理功能实现任务列表

## 任务概述

本任务列表基于会话管理功能设计文档(v1.3),将设计转化为可执行的实现步骤。

**实现原则**:
- 增量开发,每个任务都能独立验证
- 先实现核心功能,再实现扩展功能
- 测试任务标记为可选(*),可根据需要选择是否实现
- 每个任务完成后确保项目可构建

**技术栈**:
- Java 21 + Spring Boot 3.4.1
- MySQL 8.0 + MyBatis-Plus 3.5.7
- Redis 7.0 + Spring Data Redis
- JJWT 0.12.6
- Spring Security 6.4.x

---

## 任务列表

### 阶段1: 基础设施和领域模型

- [ ] 1. 创建错误码枚举
  - 在 `common` 模块创建 `SessionErrorCode` 枚举
  - 在 `common` 模块创建 `StorageErrorCode` 枚举
  - 实现 `ErrorCode` 接口,定义错误码和消息
  - 错误码格式: AUTH_101, AUTH_102, SYS_001等
  - _需求: 所有需求(错误处理基础)_

- [ ] 2. 创建领域模型和值对象
  - 在 `domain-api` 创建 `Session` 聚合根
  - 在 `domain-api` 创建 `DeviceInfo` 值对象
  - 在 `domain-api` 创建 `TokenClaims` 值对象
  - 在 `domain-api` 创建 `SessionValidationResult` 值对象
  - 在 `domain-api` 创建 `TokenType` 和 `DeviceType` 枚举
  - 实现 Session 的核心业务方法: isExpired(), isIdleTimeout(), updateLastActivity()等
  - _需求: 1.1, 1.2, 1.3, 1.4_

- [ ]* 2.1 编写领域模型单元测试
  - 测试 Session.isExpired() 方法
  - 测试 Session.isIdleTimeout() 方法
  - 测试 Session.updateLastActivity() 方法
  - 测试 DeviceInfo.isSameDevice() 方法
  - _需求: 1.1, 1.2, 1.3_

### 阶段2: 仓储和缓存接口定义

- [ ] 3. 定义仓储接口
  - 在 `repository-api` 创建 `SessionRepository` 接口
  - 定义 save(), findById(), findByUserId(), delete(), batchDelete()方法
  - 在 `repository-api` 创建 `SessionEntity` 实体类(用于持久化)
  - _需求: 1.1, 1.4, 1.5_

- [ ] 4. 定义缓存接口
  - 在 `cache-api` 创建 `SessionCacheService` 接口
  - 定义 cacheSession(), getCachedSession(), evictSession()方法
  - 定义 addToBlacklist(), isInBlacklist()方法(令牌黑名单)
  - _需求: 1.2, 1.4, 2.4_

### 阶段3: MySQL仓储实现

- [ ] 5. 实现MySQL仓储层
  - 在 `mysql-impl` 创建 `SessionRepositoryImpl` 实现类
  - 在 `mysql-impl` 创建 `SessionMapper` MyBatis接口
  - 在 `mysql-impl` 创建 `SessionPO` 持久化对象
  - 实现 Entity ↔ PO 转换逻辑
  - 配置 MyBatis-Plus 自动建表(或提供建表SQL)
  - 在 user_id 和 expires_at 字段上创建索引
  - _需求: 1.1, 1.4, 1.5_

- [ ]* 5.1 编写仓储层单元测试
  - 测试 save() 方法
  - 测试 findById() 方法
  - 测试 findByUserId() 方法
  - 测试 delete() 方法
  - 使用 H2 内存数据库或 TestContainers
  - _需求: 1.1, 1.4_

### 阶段4: Redis缓存实现

- [ ] 6. 实现Redis缓存层
  - 在 `redis-impl` 创建 `SessionCacheServiceImpl` 实现类
  - 配置 RedisTemplate 使用 Jackson 序列化
  - 实现 cacheSession() 方法(设置TTL)
  - 实现 getCachedSession() 方法
  - 实现 evictSession() 方法
  - 实现令牌黑名单相关方法
  - 实现 Redis 连接失败的异常处理(捕获并记录日志)
  - _需求: 1.2, 2.4, 3.6_

- [ ]* 6.1 编写缓存层单元测试
  - 测试缓存写入和读取
  - 测试 TTL 过期机制
  - 测试令牌黑名单功能
  - 使用 Embedded Redis 或 TestContainers
  - _需求: 1.2, 2.4_

### 阶段5: JWT令牌服务实现

- [ ] 7. 实现JWT令牌服务
  - 在 `domain-api` 创建 `TokenService` 接口
  - 在 `security-impl` 创建 `JwtTokenServiceImpl` 实现类
  - 实现 generateAccessToken() 方法(15分钟过期)
  - 实现 generateRefreshToken() 方法(30天过期)
  - 实现 validateAccessToken() 方法
  - 实现 validateRefreshToken() 方法(检查黑名单)
  - 使用 JJWT 库,HS256算法签名
  - 从配置文件读取 JWT 密钥
  - _需求: 2.1, 2.2, 2.3, 2.4_

- [ ]* 7.1 编写JWT令牌服务单元测试
  - 测试访问令牌生成和验证
  - 测试刷新令牌生成和验证
  - 测试令牌过期检测
  - 测试令牌黑名单检查
  - _需求: 2.1, 2.2, 2.3, 2.4_

### 阶段6: 领域服务实现

- [ ] 8. 实现会话领域服务
  - 在 `domain-api` 创建 `SessionDomainService` 接口
  - 在 `domain-impl` 创建 `SessionDomainServiceImpl` 实现类
  - 实现 createSession() 方法
    - 生成 UUID v4 会话标识符
    - 调用 enforceSessionLimit() 清理超限会话
    - 先保存到 MySQL,再缓存到 Redis
    - Redis 失败时记录 WARNING 日志但继续
  - 实现 validateAndRefreshSession() 方法
    - 先读 Redis 缓存,未命中则读 MySQL
    - 检查绝对超时和空闲超时
    - 更新最后活动时间
    - 先更新 MySQL,再更新 Redis
  - 实现 destroySession() 方法
    - 先删除 MySQL,再删除 Redis
  - 实现 findUserSessions() 方法
  - 实现 enforceSessionLimit() 方法(最多5个会话)
  - 实现 checkIpChange() 方法(记录IP变化)
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [ ]* 8.1 编写领域服务单元测试
  - Mock SessionRepository 和 SessionCacheService
  - 测试会话创建流程
  - 测试会话验证流程(正常、过期、空闲超时)
  - 测试会话销毁流程
  - 测试会话数量限制
  - 测试 Redis 故障降级逻辑
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.6_

### 阶段7: 应用服务实现

- [ ] 9. 实现会话应用服务
  - 在 `application-api` 创建 `SessionApplicationService` 接口
  - 在 `application-impl` 创建 `SessionApplicationServiceImpl` 实现类
  - 实现 createSession() 方法
    - 调用 SessionDomainService.createSession()
    - 添加 @Transactional 注解控制事务
  - 实现 validateSession() 方法
    - 调用 SessionDomainService.validateAndRefreshSession()
    - 捕获 BusinessException 并转换为 SessionValidationResult
  - 实现 destroySession() 方法
  - 实现 listUserSessions() 方法
  - 实现 terminateSession() 方法(权限检查)
  - 实现 terminateOtherSessions() 方法
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3_

- [ ]* 9.1 编写应用服务单元测试
  - Mock SessionDomainService
  - 测试事务控制
  - 测试权限检查逻辑
  - 测试异常转换逻辑
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

### 阶段8: HTTP接口实现

- [ ] 10. 实现会话管理HTTP接口
  - 在 `interface-http` 创建 `SessionController`
  - 实现 GET /api/v1/sessions (查询当前用户所有会话)
  - 实现 DELETE /api/v1/sessions/{sessionId} (终止指定会话)
  - 实现 POST /api/v1/sessions/terminate-others (终止其他会话)
  - 实现 POST /api/v1/auth/refresh (刷新访问令牌)
  - 创建 Request/Response DTO 类
  - 使用 @Valid 注解进行参数校验
  - 使用统一的 Result<T> 响应格式
  - _需求: 1.5, 2.3, 3.1, 3.2, 3.3_

- [ ] 11. 实现会话验证过滤器
  - 在 `interface-http` 创建 `SessionValidationFilter`
  - 从 Cookie 或 Authorization Header 获取会话标识符/令牌
  - 调用 SessionApplicationService.validateSession() 验证会话
  - 验证成功后设置 SecurityContext
  - 验证失败时返回 401 错误(使用 BusinessException)
  - 配置过滤器到 Spring Security 过滤器链
  - _需求: 1.2, 2.2, 3.4_

- [ ] 12. 实现全局异常处理器
  - 在 `interface-http` 创建 `GlobalExceptionHandler`
  - 处理 BusinessException,返回对应的错误码和消息
  - 处理 SystemException,返回 500 错误
  - 处理参数校验异常(@Valid)
  - 记录异常日志(BusinessException 为 WARN,SystemException 为 ERROR)
  - _需求: 所有需求(统一错误处理)_

### 阶段9: 配置和集成

- [ ] 13. 配置Spring Security
  - 在 `security-impl` 创建 `SecurityConfig` 配置类
  - 配置会话管理策略(无状态)
  - 配置 SessionValidationFilter 到过滤器链
  - 配置 Cookie 安全属性(HttpOnly, Secure, SameSite=Strict)
  - 配置 CSRF 防护
  - 配置公开接口(登录、注册)和受保护接口
  - _需求: 3.4, 3.5_

- [ ] 14. 配置应用参数
  - 在 `bootstrap` 模块配置 application.yml
  - 配置会话超时参数(绝对超时、空闲超时、记住我超时)
  - 配置 JWT 参数(密钥、过期时间、签发者)
  - 配置设备管理参数(最大设备数、单设备模式)
  - 配置安全参数(严格IP检查、会话固定防护)
  - 配置 Cookie 参数(名称、域名、路径)
  - 配置存储参数(MySQL、Redis连接信息)
  - 配置审计参数(日志保留天数)
  - _需求: 4.1, 4.2, 4.3_

- [ ] 15. 实现会话清理定时任务
  - 在 `domain-impl` 创建 `SessionCleanupTask`
  - 使用 @Scheduled 注解配置定时任务(每小时执行)
  - 调用 SessionRepository.deleteExpiredSessions() 清理过期会话
  - 记录清理日志(清理数量)
  - _需求: 1.3_

### 阶段10: 集成测试

- [ ]* 16. 编写集成测试
  - 在 `bootstrap` 模块创建 `SessionIntegrationTest`
  - 使用 @SpringBootTest 和 TestContainers
  - 测试完整的会话创建流程(登录 → 创建会话 → 返回Cookie和JWT)
  - 测试完整的会话验证流程(携带Cookie → 验证会话 → 访问受保护资源)
  - 测试完整的会话销毁流程(登出 → 销毁会话 → 清除Cookie)
  - 测试令牌刷新流程
  - 测试多设备管理(超过5个会话时清理最旧的)
  - 测试 Redis 故障降级(停止Redis → 验证仍可工作)
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 2.3, 3.6_

### 阶段11: 监控和审计

- [ ] 17. 实现审计日志
  - 在 SessionDomainService 中记录会话创建、销毁事件(INFO级别)
  - 记录 IP 地址变化事件(WARNING级别)
  - 记录验证失败事件(WARNING级别)
  - 记录 Redis 故障事件(ERROR级别)
  - 日志包含 traceId(使用 Micrometer Tracing)
  - _需求: 4.4_

- [ ] 18. 配置监控指标
  - 使用 Micrometer 记录会话验证响应时间
  - 使用 Micrometer 记录会话创建响应时间
  - 使用 Micrometer 记录活跃会话数量
  - 使用 Micrometer 记录 Redis 命中率
  - 使用 Micrometer 记录验证失败次数
  - 配置 Prometheus 端点(/actuator/prometheus)
  - _需求: 4.4_

### 阶段12: 文档和验收

- [ ] 19. 编写API文档
  - 使用 Swagger/SpringDoc 生成 API 文档
  - 配置 OpenAPI 3.0 规范
  - 添加接口描述和示例
  - 配置 Swagger UI 访问路径(/swagger-ui.html)
  - _需求: 所有需求(文档)_

- [ ] 20. 最终验收测试
  - 确保所有单元测试通过
  - 确保所有集成测试通过
  - 验证性能指标(验证<50ms, 创建<200ms)
  - 验证安全配置(Cookie属性、CSRF防护)
  - 验证监控指标正常上报
  - 验证审计日志正常记录
  - 验证 Redis 故障降级正常工作
  - _需求: 所有需求_

---

## 任务执行说明

### 执行顺序
1. 按阶段顺序执行,每个阶段完成后进行验证
2. 标记为 `*` 的测试任务为可选,可根据时间和需求选择是否实现
3. 每个任务完成后确保项目可以成功构建(`mvn clean install`)

### 验证方式
- **单元测试**: 运行 `mvn test -DskipITs`
- **集成测试**: 运行 `mvn verify`
- **手动测试**: 启动应用,使用 Postman 或 curl 测试接口

### 依赖关系
- 阶段1-2: 基础设施,无依赖
- 阶段3-4: 依赖阶段1-2
- 阶段5: 可与阶段3-4并行
- 阶段6: 依赖阶段3-5
- 阶段7: 依赖阶段6
- 阶段8: 依赖阶段7
- 阶段9: 依赖阶段8
- 阶段10-12: 依赖所有前置阶段

### 注意事项
1. 每个任务实现时参考设计文档的详细设计部分
2. 错误处理统一使用 BusinessException + ErrorCode 模式
3. 缓存操作失败不影响主流程,记录日志即可
4. 所有配置参数从 application.yml 读取,不硬编码
5. 敏感信息(JWT密钥、数据库密码)使用环境变量

---

## 参考文档

- **设计文档**: `.kiro/specs/F01-4-session-management/design.md`
- **需求文档**: `.kiro/specs/F01-4-session-management/requirements.md`
- **项目架构指南**: `ARCHITECTURE_GUIDELINES.md`
- **技术栈说明**: `tech.md`
- **项目结构说明**: `structure.md`

---

**任务列表创建日期**: 2024-11-28  
**任务列表版本**: v1.0  
**对应设计文档版本**: v1.3
