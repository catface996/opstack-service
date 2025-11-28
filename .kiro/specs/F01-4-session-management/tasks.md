# 会话管理功能实现任务列表

## 任务概述

本任务列表基于会话管理功能设计文档(v1.3),将设计转化为可执行的实现步骤。

**实现原则**:
- 增量开发,每个任务都能独立验证
- 先实现核心功能,再实现扩展功能
- 测试任务标记为"可选",可根据时间和需求选择是否实现
- 每个任务完成后确保项目可构建
- 任务编号使用简单数字格式(1., 2., 3.),便于Kiro IDE识别和执行

**验证方法优先级**:
每个任务都包含明确的验证方法标签,按以下优先级验证:
1. **【Runtime验证】**(最高优先级): 通过实际运行应用验证功能
2. **【Unit Test】**(第二优先级): 通过单元测试验证业务逻辑
3. **【Integration Test】**(第三优先级): 通过集成测试验证完整流程
4. **【Build验证】**(第四优先级): 通过项目构建验证结构正确性
5. **【Static检查】**(最后手段): 通过静态检查验证文件和配置

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
  - 创建 `SessionErrorCode` 枚举,包含会话相关错误码(AUTH_101-AUTH_104, AUTH_201-AUTH_203, AUTHZ_001)
  - 创建 `StorageErrorCode` 枚举,包含存储相关错误码(SYS_001-SYS_003)
  - 实现 `ErrorCode` 接口,定义错误码和用户友好的错误消息
  - **验证方法**: 
    - 【Build验证】项目编译成功,无语法错误
    - 【Static检查】检查错误码格式符合 `{类别}_{序号}` 规范
    - 【Static检查】检查每个错误码都有对应的用户消息
  - _需求: 所有需求(错误处理基础)_

- [ ] 2. 创建领域模型和值对象
  - 创建 `Session` 聚合根,包含会话标识符、用户ID、设备信息、时间戳等属性
  - 创建 `DeviceInfo` 值对象,包含IP地址、User-Agent、设备类型等信息
  - 创建 `TokenClaims` 值对象,包含令牌声明信息
  - 创建 `SessionValidationResult` 值对象,包含验证结果和警告信息
  - 创建 `TokenType` 和 `DeviceType` 枚举
  - 实现 Session 的核心业务方法(isExpired, isIdleTimeout, updateLastActivity, getRemainingTime等)
  - **验证方法**:
    - 【Build验证】项目编译成功,所有类和方法定义正确
    - 【Static检查】检查 Session 类包含所有必需的属性和方法
    - 【Static检查】检查值对象是否为 immutable(final字段)
  - _需求: 1.1, 1.2, 1.3, 1.4_

- [ ] 3. 编写领域模型单元测试(可选)
  - 测试 Session 的超时检测逻辑(绝对超时、空闲超时)
  - 测试 Session 的时间计算方法(剩余时间、即将过期判断)
  - 测试 DeviceInfo 的设备比较方法
  - **验证方法**:
    - 【Unit Test】运行 `mvn test -Dtest=SessionTest`,所有测试通过
    - 【Unit Test】测试覆盖率 > 80%
  - _需求: 1.1, 1.2, 1.3_

### 阶段2: 仓储和缓存接口定义

- [ ] 4. 定义仓储接口
  - 定义 `SessionRepository` 接口,包含会话的CRUD操作方法
  - 定义 `SessionEntity` 实体类,用于数据库持久化映射
  - 接口方法包括: save, findById, findByUserId, delete, batchDelete, deleteExpiredSessions
  - **验证方法**:
    - 【Build验证】项目编译成功,接口定义正确
    - 【Static检查】检查接口方法签名符合设计文档
    - 【Static检查】检查 SessionEntity 包含所有必需字段
  - _需求: 1.1, 1.4, 1.5_

- [ ] 5. 定义缓存接口
  - 定义 `SessionCacheService` 接口,包含会话缓存操作方法
  - 接口方法包括: cacheSession, getCachedSession, evictSession, cacheUserSessions, getUserSessionIds
  - 定义令牌黑名单相关方法: addToBlacklist, isInBlacklist
  - **验证方法**:
    - 【Build验证】项目编译成功,接口定义正确
    - 【Static检查】检查接口方法签名符合设计文档
    - 【Static检查】检查方法包含TTL参数(用于设置过期时间)
  - _需求: 1.2, 1.4, 2.4_

### 阶段3: MySQL仓储实现

- [ ] 6. 实现MySQL仓储层
  - 实现 `SessionRepositoryImpl` 类,提供会话数据的MySQL持久化能力
  - 创建 MyBatis Mapper 接口和XML映射文件
  - 创建 `SessionPO` 持久化对象,实现 Entity ↔ PO 转换
  - 配置数据库表结构(自动建表或SQL脚本),在 user_id 和 expires_at 字段创建索引
  - **验证方法**:
    - 【Runtime验证】启动应用,检查数据库表自动创建成功
    - 【Runtime验证】执行 save 操作,检查数据库中数据正确保存
    - 【Runtime验证】执行 findById 操作,检查能正确查询数据
    - 【Runtime验证】检查索引创建成功(`SHOW INDEX FROM t_session`)
  - _需求: 1.1, 1.4, 1.5_

- [ ] 7. 编写仓储层单元测试(可选)
  - 测试会话的增删改查操作
  - 测试批量删除和过期会话清理功能
  - 测试 Entity ↔ PO 转换的正确性
  - **验证方法**:
    - 【Unit Test】运行 `mvn test -Dtest=SessionRepositoryImplTest`,所有测试通过
    - 【Unit Test】使用 H2 内存数据库或 TestContainers 进行测试
  - _需求: 1.1, 1.4_

### 阶段4: Redis缓存实现

- [ ] 8. 实现Redis缓存层
  - 实现 `SessionCacheServiceImpl` 类,提供会话数据的Redis缓存能力
  - 配置 RedisTemplate 使用 Jackson 进行JSON序列化
  - 实现缓存的写入、读取、删除操作,支持TTL自动过期
  - 实现令牌黑名单功能
  - 实现 Redis 连接失败的异常处理(捕获异常,记录日志,不影响主流程)
  - **验证方法**:
    - 【Runtime验证】启动应用,使用 redis-cli 检查缓存数据格式正确(JSON格式)
    - 【Runtime验证】缓存会话后,使用 `TTL session:{sessionId}` 检查TTL设置正确
    - 【Runtime验证】停止Redis服务,验证应用仍能正常运行(降级到MySQL)
    - 【Runtime验证】检查日志中有Redis连接失败的ERROR日志
  - _需求: 1.2, 2.4, 3.6_

- [ ] 9. 编写缓存层单元测试(可选)
  - 测试缓存的写入、读取、删除操作
  - 测试TTL过期机制
  - 测试令牌黑名单功能
  - 测试Redis连接失败时的异常处理
  - **验证方法**:
    - 【Unit Test】运行 `mvn test -Dtest=SessionCacheServiceImplTest`,所有测试通过
    - 【Unit Test】使用 Embedded Redis 或 TestContainers 进行测试
  - _需求: 1.2, 2.4_

### 阶段5: JWT令牌服务实现

- [ ] 10. 实现JWT令牌服务
  - 定义 `TokenService` 接口,声明令牌生成和验证方法
  - 实现 `JwtTokenServiceImpl` 类,使用JJWT库生成和验证JWT令牌
  - 实现访问令牌生成(15分钟过期)和刷新令牌生成(30天过期)
  - 实现令牌验证,包括签名验证、过期检测、黑名单检查
  - 使用HS256算法签名,JWT密钥从配置文件读取
  - **验证方法**:
    - 【Runtime验证】启动应用,调用令牌生成接口,检查返回的JWT格式正确
    - 【Runtime验证】使用 jwt.io 解析生成的令牌,检查payload包含sessionId、userId、过期时间等
    - 【Runtime验证】使用过期的令牌访问接口,检查返回401错误(AUTH_201)
    - 【Runtime验证】将令牌加入黑名单后访问接口,检查返回401错误(AUTH_203)
  - _需求: 2.1, 2.2, 2.3, 2.4_

- [ ] 11. 编写JWT令牌服务单元测试(可选)
  - 测试访问令牌和刷新令牌的生成与验证
  - 测试令牌过期检测逻辑
  - 测试令牌黑名单检查逻辑
  - 测试无效令牌的异常处理
  - **验证方法**:
    - 【Unit Test】运行 `mvn test -Dtest=JwtTokenServiceImplTest`,所有测试通过
    - 【Unit Test】测试覆盖率 > 80%
  - _需求: 2.1, 2.2, 2.3, 2.4_

### 阶段6: 领域服务实现

- [ ] 12. 实现会话领域服务
  - 定义 `SessionDomainService` 接口,声明会话管理的核心业务方法
  - 实现 `SessionDomainServiceImpl` 类,实现会话的创建、验证、销毁等核心业务逻辑
  - 实现会话创建逻辑: 生成UUID标识符、清理超限会话、先写MySQL再写Redis(Cache-Aside模式)
  - 实现会话验证逻辑: 先读Redis缓存、缓存未命中则读MySQL、检查超时、更新活动时间
  - 实现会话销毁逻辑: 先删MySQL再删Redis
  - 实现会话数量限制(默认5个)和IP变化检测
  - 实现Redis故障时的降级处理(捕获异常、记录日志、继续使用MySQL)
  - **验证方法**:
    - 【Runtime验证】启动应用,调用登录接口,检查MySQL和Redis中都创建了会话数据
    - 【Runtime验证】创建6个会话,检查最旧的会话被自动清理
    - 【Runtime验证】停止Redis,调用登录接口,检查仍能创建会话(数据在MySQL中)
    - 【Runtime验证】检查日志中有Redis故障的WARNING日志
    - 【Runtime验证】会话空闲30分钟后访问接口,检查返回401错误(AUTH_102)
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [ ] 13. 编写领域服务单元测试(可选)
  - Mock SessionRepository 和 SessionCacheService 依赖
  - 测试会话创建、验证、销毁的完整流程
  - 测试超时检测逻辑(绝对超时、空闲超时)
  - 测试会话数量限制逻辑
  - 测试Redis故障时的降级逻辑
  - **验证方法**:
    - 【Unit Test】运行 `mvn test -Dtest=SessionDomainServiceImplTest`,所有测试通过
    - 【Unit Test】测试覆盖率 > 80%
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.6_

### 阶段7: 应用服务实现

- [ ] 14. 实现会话应用服务
  - 定义 `SessionApplicationService` 接口,声明应用层的会话管理方法
  - 实现 `SessionApplicationServiceImpl` 类,编排领域服务完成业务用例
  - 实现会话创建、验证、销毁等方法,添加@Transactional注解控制事务边界
  - 实现多设备会话管理(查询用户所有会话、终止指定会话、终止其他会话)
  - 实现权限检查逻辑(只能终止自己的会话)
  - 实现异常转换逻辑(将BusinessException转换为SessionValidationResult)
  - **验证方法**:
    - 【Runtime验证】启动应用,调用会话管理接口,检查事务正确提交
    - 【Runtime验证】尝试终止其他用户的会话,检查返回403错误(AUTHZ_001)
    - 【Runtime验证】查询当前用户所有会话,检查返回正确的会话列表
    - 【Runtime验证】终止其他会话,检查只保留当前会话
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3_

- [ ] 15. 编写应用服务单元测试(可选)
  - Mock SessionDomainService 依赖
  - 测试事务控制(@Transactional注解生效)
  - 测试权限检查逻辑
  - 测试异常转换逻辑
  - **验证方法**:
    - 【Unit Test】运行 `mvn test -Dtest=SessionApplicationServiceImplTest`,所有测试通过
    - 【Unit Test】测试覆盖率 > 80%
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

### 阶段8: HTTP接口实现

- [ ] 16. 实现会话管理HTTP接口
  - 创建 `SessionController`,提供会话管理的REST API
  - 实现查询当前用户所有会话接口(GET /api/v1/sessions)
  - 实现终止指定会话接口(DELETE /api/v1/sessions/{sessionId})
  - 实现终止其他会话接口(POST /api/v1/sessions/terminate-others)
  - 实现刷新访问令牌接口(POST /api/v1/auth/refresh)
  - 创建Request/Response DTO类,使用@Valid注解进行参数校验
  - 使用统一的Result<T>响应格式
  - **验证方法**:
    - 【Runtime验证】启动应用,使用curl或Postman调用 GET /api/v1/sessions,检查返回当前用户的会话列表
    - 【Runtime验证】调用 DELETE /api/v1/sessions/{sessionId},检查会话被成功终止
    - 【Runtime验证】调用 POST /api/v1/sessions/terminate-others,检查其他会话被终止
    - 【Runtime验证】调用 POST /api/v1/auth/refresh,检查返回新的访问令牌
    - 【Runtime验证】使用无效参数调用接口,检查返回400错误和参数校验信息
  - _需求: 1.5, 2.3, 3.1, 3.2, 3.3_

- [ ] 17. 实现会话验证过滤器
  - 创建 `SessionValidationFilter`,拦截所有受保护的HTTP请求
  - 从Cookie或Authorization Header提取会话标识符或JWT令牌
  - 调用SessionApplicationService验证会话有效性
  - 验证成功后设置Spring Security的SecurityContext
  - 验证失败时返回401错误,使用BusinessException抛出对应错误码
  - 配置过滤器到Spring Security过滤器链
  - **验证方法**:
    - 【Runtime验证】启动应用,不携带Cookie访问受保护接口,检查返回401错误
    - 【Runtime验证】携带有效Cookie访问受保护接口,检查请求成功
    - 【Runtime验证】携带过期Cookie访问接口,检查返回401错误(AUTH_101或AUTH_102)
    - 【Runtime验证】使用JWT令牌访问接口,检查请求成功
    - 【Runtime验证】检查SecurityContext中包含当前用户信息
  - _需求: 1.2, 2.2, 3.4_

- [ ] 18. 实现全局异常处理器
  - 创建 `GlobalExceptionHandler`,统一处理应用中的所有异常
  - 处理BusinessException,返回对应的错误码和用户友好的错误消息
  - 处理SystemException,返回500错误和通用错误消息
  - 处理参数校验异常(@Valid),返回400错误和校验失败信息
  - 记录异常日志(BusinessException为WARN级别,SystemException为ERROR级别)
  - **验证方法**:
    - 【Runtime验证】触发BusinessException(如会话过期),检查返回正确的错误码和消息
    - 【Runtime验证】触发SystemException(如数据库连接失败),检查返回500错误
    - 【Runtime验证】发送无效参数,检查返回400错误和参数校验信息
    - 【Runtime验证】检查日志文件中有对应级别的异常日志
  - _需求: 所有需求(统一错误处理)_

### 阶段9: 配置和集成

- [ ] 19. 配置Spring Security
  - 创建 `SecurityConfig` 配置类,配置Spring Security安全策略
  - 配置会话管理策略为无状态(STATELESS)
  - 将SessionValidationFilter添加到Spring Security过滤器链
  - 配置Cookie安全属性(HttpOnly、Secure、SameSite=Strict)
  - 配置CSRF防护策略
  - 配置公开接口(登录、注册)和受保护接口的访问规则
  - **验证方法**:
    - 【Runtime验证】启动应用,访问公开接口(如登录),检查无需认证即可访问
    - 【Runtime验证】访问受保护接口,检查需要认证才能访问
    - 【Runtime验证】检查响应头中Cookie包含HttpOnly、Secure、SameSite属性
    - 【Runtime验证】尝试CSRF攻击,检查请求被拒绝
  - _需求: 3.4, 3.5_

- [ ] 20. 配置应用参数
  - 在application.yml配置文件中添加会话管理相关配置
  - 配置会话超时参数(绝对超时8小时、空闲超时30分钟、记住我30天)
  - 配置JWT参数(密钥、访问令牌15分钟、刷新令牌30天、签发者)
  - 配置设备管理参数(最大设备数5个、单设备模式false)
  - 配置安全参数(严格IP检查、会话固定防护)
  - 配置Cookie参数(名称、域名、路径)
  - 配置MySQL和Redis连接信息
  - 配置审计参数(日志保留30天)
  - **验证方法**:
    - 【Runtime验证】启动应用,检查配置参数正确加载(通过日志或actuator端点)
    - 【Runtime验证】创建会话,检查超时时间符合配置(8小时)
    - 【Runtime验证】生成JWT令牌,检查过期时间符合配置(15分钟)
    - 【Runtime验证】创建6个会话,检查最多保留5个
  - _需求: 4.1, 4.2, 4.3_

- [ ] 21. 实现会话清理定时任务
  - 创建 `SessionCleanupTask` 定时任务类
  - 使用@Scheduled注解配置定时执行(每小时执行一次)
  - 调用SessionRepository.deleteExpiredSessions()清理MySQL中的过期会话
  - 记录清理日志,包含清理的会话数量
  - **验证方法**:
    - 【Runtime验证】启动应用,等待1小时,检查日志中有定时任务执行记录
    - 【Runtime验证】手动创建过期会话数据,等待定时任务执行,检查过期会话被清理
    - 【Runtime验证】检查日志中记录了清理的会话数量
  - _需求: 1.3_

### 阶段10: 集成测试

- [ ] 22. 编写集成测试(可选)
  - 创建 `SessionIntegrationTest` 集成测试类
  - 使用@SpringBootTest启动完整应用上下文
  - 使用TestContainers启动MySQL和Redis容器
  - 测试完整的会话生命周期(创建、验证、刷新、销毁)
  - 测试多设备会话管理(超过5个会话时自动清理)
  - 测试Redis故障降级场景
  - **验证方法**:
    - 【Integration Test】运行 `mvn verify`,所有集成测试通过
    - 【Integration Test】测试会话创建流程,检查MySQL和Redis中都有数据
    - 【Integration Test】测试会话验证流程,检查能正确访问受保护资源
    - 【Integration Test】测试会话销毁流程,检查MySQL和Redis中数据被删除
    - 【Integration Test】测试令牌刷新流程,检查返回新的访问令牌
    - 【Integration Test】测试多设备管理,检查超限会话被清理
    - 【Integration Test】停止Redis容器,测试应用仍能正常工作
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 2.3, 3.6_

### 阶段11: 监控和审计

- [ ] 23. 实现审计日志
  - 在SessionDomainService中添加审计日志记录
  - 记录会话创建和销毁事件(INFO级别)
  - 记录IP地址变化事件(WARNING级别)
  - 记录验证失败事件(WARNING级别)
  - 记录Redis故障事件(ERROR级别)
  - 日志包含traceId(使用Micrometer Tracing自动注入)
  - **验证方法**:
    - 【Runtime验证】启动应用,创建会话,检查日志中有INFO级别的会话创建记录
    - 【Runtime验证】销毁会话,检查日志中有INFO级别的会话销毁记录
    - 【Runtime验证】从不同IP访问,检查日志中有WARNING级别的IP变化记录
    - 【Runtime验证】使用过期会话访问,检查日志中有WARNING级别的验证失败记录
    - 【Runtime验证】停止Redis,检查日志中有ERROR级别的Redis故障记录
    - 【Runtime验证】检查所有日志都包含traceId字段
  - _需求: 4.4_

- [ ] 24. 配置监控指标
  - 使用Micrometer记录会话管理相关的性能指标
  - 记录会话验证响应时间(P50、P95、P99)
  - 记录会话创建响应时间(P50、P95、P99)
  - 记录活跃会话数量(Gauge指标)
  - 记录Redis缓存命中率
  - 记录验证失败次数(Counter指标)
  - 配置Prometheus端点(/actuator/prometheus)暴露指标
  - **验证方法**:
    - 【Runtime验证】启动应用,访问 /actuator/prometheus,检查端点可访问
    - 【Runtime验证】创建和验证会话,检查Prometheus端点中有响应时间指标
    - 【Runtime验证】检查Prometheus端点中有活跃会话数量指标
    - 【Runtime验证】检查Prometheus端点中有Redis命中率指标
    - 【Runtime验证】触发验证失败,检查失败次数指标增加
  - _需求: 4.4_

### 阶段12: 文档和验收

- [ ] 25. 编写API文档
  - 集成Swagger/SpringDoc自动生成API文档
  - 配置OpenAPI 3.0规范
  - 为所有接口添加描述、参数说明和响应示例
  - 配置Swagger UI访问路径(/swagger-ui.html)
  - **验证方法**:
    - 【Runtime验证】启动应用,访问 /swagger-ui.html,检查Swagger UI页面正常显示
    - 【Runtime验证】检查所有会话管理接口都在文档中
    - 【Runtime验证】检查接口文档包含完整的参数说明和响应示例
    - 【Runtime验证】在Swagger UI中测试接口,检查能正常调用
  - _需求: 所有需求(文档)_

- [ ] 26. 最终验收测试
  - 执行完整的验收测试,确保所有功能正常工作
  - 验证所有单元测试和集成测试通过
  - 验证性能指标达标
  - 验证安全配置正确
  - 验证监控和审计功能正常
  - 验证Redis故障降级正常工作
  - **验证方法**:
    - 【Unit Test】运行 `mvn test`,检查所有单元测试通过
    - 【Integration Test】运行 `mvn verify`,检查所有集成测试通过
    - 【Runtime验证】使用JMeter或k6进行性能测试,检查会话验证P95<50ms,创建P95<200ms
    - 【Runtime验证】检查Cookie包含HttpOnly、Secure、SameSite属性
    - 【Runtime验证】尝试CSRF攻击,检查请求被拒绝
    - 【Runtime验证】访问 /actuator/prometheus,检查监控指标正常上报
    - 【Runtime验证】检查日志文件中有完整的审计日志
    - 【Runtime验证】停止Redis服务,检查应用仍能正常工作(降级到MySQL)
    - 【Runtime验证】创建、验证、销毁会话的完整流程正常工作
  - _需求: 所有需求_

---

## 任务执行说明

### 执行顺序
1. 按阶段顺序执行,每个阶段完成后进行验证
2. 标记为"可选"的测试任务可根据时间和需求选择是否实现
3. 每个任务完成后确保项目可以成功构建(`mvn clean install`)
4. 使用Kiro IDE时,点击任务旁的"Start task"按钮开始执行

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
**任务列表版本**: v1.2  
**对应设计文档版本**: v1.3  
**最后更新**: 2024-11-28 - 修正任务编号格式,使用简单数字编号(1-26)
