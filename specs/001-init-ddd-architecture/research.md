# Research & Architecture Decisions: DDD 多模块项目架构初始化

**Feature**: 001-init-ddd-architecture
**Date**: 2025-11-21
**Status**: Completed

## Overview

本文档记录了 AIOps Service 项目架构初始化过程中的技术调研和架构决策。项目采用 DDD 分层架构和 Maven 多模块结构,遵循项目宪法的 8 条核心原则。

## ADR-001: 选择 Java 21 作为开发语言

### 状态
已接受 (Accepted)

### 背景
需要选择合适的 JDK 版本来支持 Spring Boot 3.4.1 和现代化的 Java 特性。

### 决策
选择 Java 21 (LTS 版本)作为项目的开发语言版本。

### 理由
1. **LTS 支持**: Java 21 是 2023 年 9 月发布的 LTS 版本,将获得长期支持 (至少到 2026 年 9 月)
2. **Spring Boot 3 兼容性**: Spring Boot 3.x 最低要求 Java 17,Java 21 完全兼容
3. **现代语言特性**:
   - Record类型 (JDK 14+): 简化数据传输对象定义
   - Pattern Matching (JDK 17+): 提高代码可读性
   - Virtual Threads (JDK 21): 提升并发性能
   - Sequenced Collections (JDK 21): 增强集合操作
4. **性能提升**: JDK 21 包含多项性能优化,GC 性能更优
5. **生态成熟**: 主流框架 (Spring、MyBatis) 已完全支持 Java 21

### 替代方案考虑
- **Java 17 (LTS)**: 更保守的选择,但缺少 Java 21 的新特性
- **Java 11 (LTS)**: 不支持 Spring Boot 3.x,已被排除

### 后果
- ✅ 正面: 使用最新的 LTS 版本,获得长期支持和现代语言特性
- ✅ 正面: 与 Spring Boot 3.4.1 完全兼容
- ⚠️ 注意: 部署环境必须支持 Java 21

## ADR-002: 选择 Spring Boot 3.4.1 + Spring Cloud 2025.0.0

### 状态
已接受 (Accepted)

### 背景
需要选择合适的 Spring 技术栈版本,既要保证稳定性,又要使用最新的特性。

### 决策
使用 Spring Boot 3.4.1 (2024年11月发布) 和 Spring Cloud 2025.0.0 作为核心框架。

### 理由
1. **最新稳定版**: Spring Boot 3.4.1 是 2024 年的最新稳定版本,修复了 3.4.0 的已知问题
2. **Jakarta EE 支持**: Spring Boot 3.x 已迁移到 Jakarta EE (javax.* → jakarta.*),符合未来发展方向
3. **原生镜像支持**: 更好的 GraalVM Native Image 支持,虽然当前不使用,但为未来保留可能性
4. **Spring Cloud 2025.0.0**: 与 Spring Boot 3.4.x 完全兼容,提供链路追踪等微服务能力
5. **Micrometer Observation API**: 统一的可观测性 API,支持 Trace, Metrics, Logs

### 关键配置要点
- Spring Boot BOM 通过 `<dependencyManagement>` 导入
- Spring Cloud BOM 通过 `<dependencyManagement>` 导入
- 所有子模块依赖版本由父 POM 统一管理

### 替代方案考虑
- **Spring Boot 2.7.x**: 更稳定,但已进入维护模式,不支持新特性
- **Spring Boot 3.3.x**: 较保守,但 3.4.x 已经足够稳定

### 后果
- ✅ 正面: 使用最新特性,获得官方积极支持
- ✅ 正面: 完整的 Jakarta EE 生态支持
- ⚠️ 注意: 依赖库必须兼容 Spring Boot 3.x

## ADR-003: 选择 MyBatis-Plus 3.5.7 作为 ORM 框架

### 状态
已接受 (Accepted)

### 背景
需要选择合适的 ORM 框架来实现数据持久化层,在 MyBatis-Plus 和 Spring Data JPA 之间抉择。

### 决策
使用 MyBatis-Plus 3.5.7 (mybatis-plus-spring-boot3-starter) 作为 ORM 框架。

### 理由
1. **项目宪法要求**: 项目宪法第 VII 条明确规定了 MyBatis-Plus 数据操作规范
2. **SQL 可控性强**: 所有条件查询必须在 Mapper XML 中实现,SQL 语句清晰可见,便于 DBA 审查和性能优化
3. **MyBatis 增强**: 继承 MyBatis 的灵活性,同时提供 CRUD 增强功能
4. **Spring Boot 3 支持**: 3.5.7 版本提供 mybatis-plus-spring-boot3-starter,完全兼容 Spring Boot 3.x
5. **Entity/PO 分离友好**: 支持灵活的 Entity ↔ PO 转换,符合 DDD 原则

### 关键规范
**允许使用的 MyBatis-Plus API**:
- ✅ 插入操作: `save()`, `saveBatch()`, `saveOrUpdate()`
- ✅ 根据主键更新: `updateById()`, `updateBatchById()`
- ✅ 根据主键查询: `getById()`, `listByIds()`

**必须在 Mapper XML 中实现**:
- ❌ 所有条件查询 (不使用 Wrapper)
- ❌ 所有条件更新 (不使用 UpdateWrapper)
- ❌ 所有条件删除 (不使用 QueryWrapper)
- ❌ 所有复杂查询 (多表关联、子查询、聚合)

### 替代方案考虑
- **Spring Data JPA**: 更高层的抽象,但 SQL 不够透明,性能优化困难
- **纯 MyBatis**: 更灵活,但缺少 CRUD 增强功能,开发效率较低

### 后果
- ✅ 正面: SQL 语句集中管理,便于审查和优化
- ✅ 正面: Entity/PO 分离清晰,符合 DDD 架构
- ✅ 正面: 学习曲线平缓,团队容易上手
- ⚠️ 注意: 必须严格遵循规范,禁止使用 Wrapper 进行条件查询

## ADR-004: 选择 Micrometer Tracing + Logback JSON 作为日志追踪方案

### 状态
已接受 (Accepted)

### 背景
需要实现分布式链路追踪和结构化日志输出,支持跨模块、跨请求的日志关联。

### 决策
使用 Micrometer Tracing 1.3.5 生成 Trace ID/Span ID,结合 Logstash Logback Encoder 7.4 输出 JSON 格式日志。

### 理由
1. **Spring Cloud 集成**: Micrometer Tracing 是 Spring Cloud Sleuth 的替代品,与 Spring Boot 3.x 深度集成
2. **自动传播**: Trace ID 和 Span ID 自动在 HTTP 请求、消息队列、异步任务中传播
3. **MDC 支持**: 自动将 traceId、spanId 写入 MDC (Mapped Diagnostic Context),Logback 可直接使用
4. **JSON 格式**: Logstash Logback Encoder 输出结构化 JSON 日志,便于日志收集系统 (ELK、Loki) 解析
5. **多环境支持**: 通过 `<springProfile>` 标签在 logback-spring.xml 中实现多环境差异化配置

### 架构方案
```
HTTP Request → Micrometer Tracing → 生成 traceId/spanId → MDC → Logback → JSON 输出
```

### 日志字段规范
```json
{
  "timestamp": "2025-11-21T10:30:00.123+08:00",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.catface996.aiops.interface_.http.controller.UserController",
  "traceId": "64cf4e1a7c8e4f2b9d1a3e5f7b9c1d3e",
  "spanId": "9d1a3e5f7b9c1d3e",
  "message": "用户登录成功",
  "exception": "java.lang.Exception: ..."
}
```

### 多环境日志策略

| 环境 | Profile | 输出目标 | 日志格式 | 项目包日志级别 | 框架包日志级别 |
|------|---------|---------|---------|--------------|--------------|
| 本地开发 | local | 控制台 | 彩色格式 | DEBUG | WARN |
| 开发环境 | dev | 文件 | JSON | DEBUG | WARN |
| 测试环境 | test | 文件 | JSON | DEBUG | WARN |
| 预发布环境 | staging | 文件 | JSON | INFO | WARN |
| 生产环境 | prod | 文件 | JSON | INFO | WARN |

**关键配置原则**:
- ✅ 所有日志配置在 logback-spring.xml 中管理
- ❌ 禁止在 application.yml 中配置日志 (logging.level.*, logging.pattern.*, logging.file.*)
- ✅ 使用 `<springProfile>` 标签区分环境
- ✅ 项目包 (com.catface996.aiops.*) 和框架包 (org.springframework.*, com.baomidou.*, com.amazonaws.*) 使用不同日志级别
- ✅ 生产环境使用异步 Appender (AsyncAppender) 提高性能
- ✅ ERROR 级别日志单独输出到 error.log

### 替代方案考虑
- **Spring Cloud Sleuth**: 已不再维护,被 Micrometer Tracing 替代
- **SLF4J + Logback (无链路追踪)**: 缺少 Trace ID 自动生成和传播
- **Log4j2**: 性能略优,但 Logback 与 Spring Boot 集成更好

### 后果
- ✅ 正面: 自动生成和传播 Trace ID,无需手动编码
- ✅ 正面: JSON 日志便于日志系统解析和检索
- ✅ 正面: 多环境配置清晰,维护简单
- ⚠️ 注意: 必须在 logback-spring.xml 中管理所有日志配置,不得在 application.yml 中配置

## ADR-005: 选择 Prometheus + Micrometer 作为监控指标方案

### 状态
已接受 (Accepted)

### 背景
需要暴露系统监控指标,支持运维团队监控应用的运行状态和性能。

### 决策
使用 Spring Boot Actuator + Micrometer Registry Prometheus 暴露 Prometheus 格式的监控指标。

### 理由
1. **Spring Boot 集成**: Actuator 是 Spring Boot 官方监控方案,集成简单
2. **Prometheus 标准**: Prometheus 是云原生监控的事实标准,广泛支持
3. **丰富的指标**: 自动包含 JVM 指标 (内存、GC、线程)、HTTP 请求指标 (QPS、延迟、错误率)、数据库连接池指标
4. **可扩展**: 可以自定义业务指标 (如订单数、用户数等)
5. **无侵入性**: 通过注解和自动配置实现,对业务代码无侵入

### 暴露的指标端点
```
GET /actuator/prometheus
```

### 关键指标类别
1. **JVM 指标**:
   - `jvm.memory.used`: 内存使用量
   - `jvm.gc.pause`: GC 暂停时间
   - `jvm.threads.live`: 活跃线程数

2. **HTTP 请求指标**:
   - `http.server.requests`: 请求总数、响应时间分布
   - `http.server.requests.error`: 错误请求数

3. **数据库连接池指标**:
   - `hikaricp.connections.active`: 活跃连接数
   - `hikaricp.connections.pending`: 等待连接数

### 配置要点
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    tags:
      application: aiops-service
```

### 替代方案考虑
- **Micrometer + StatsD**: 需要额外的 StatsD 服务,架构更复杂
- **Micrometer + InfluxDB**: 时序数据库方案,但生态不如 Prometheus 成熟
- **自定义监控**: 开发成本高,不推荐

### 后果
- ✅ 正面: 暴露标准 Prometheus 指标,与监控系统无缝集成
- ✅ 正面: 开箱即用,无需额外开发
- ✅ 正面: 指标丰富,覆盖 JVM、HTTP、数据库等维度
- ⚠️ 注意: 需要在生产环境限制 /actuator/* 端点的访问权限

## ADR-006: 采用 DDD 分层架构而非传统三层架构

### 状态
已接受 (Accepted)

### 背景
需要选择合适的架构模式来组织项目代码,确保系统的可维护性和可演进性。

### 决策
采用 DDD (Domain-Driven Design) 分层架构,包括 Interface、Application、Domain、Infrastructure 四层。

### 理由
1. **项目宪法要求**: 项目宪法第 II 条明确规定必须采用 DDD 分层架构
2. **业务逻辑隔离**: Domain 层保持技术无关性,业务规则封装在领域模型中
3. **清晰的依赖方向**: 外层依赖内层,内层不依赖外层,依赖方向单向且清晰
4. **技术可替换**: 基础设施层 (Infrastructure) 的技术实现可以轻松替换 (如 MySQL → PostgreSQL)
5. **微服务演进友好**: 领域模块可以作为独立的微服务拆分

### 层次职责

| 层次 | 职责 | 示例模块 |
|------|------|---------|
| **Interface** | 处理外部请求,输入输出转换 | interface-http, interface-consumer |
| **Application** | 编排业务用例,协调领域服务 | application-api, application-impl |
| **Domain** | 封装核心业务规则和领域逻辑 | domain-api, domain-impl |
| **Infrastructure** | 提供技术实现 (数据库、缓存、MQ) | repository-*, cache-*, mq-* |

### 依赖方向规则
```
Interface → Application → Domain
                ↓
         Infrastructure API (不依赖 Implementation)
```

### 关键设计模式
1. **API/Implementation 分离**: 每层都分为 API (接口定义) 和 Implementation (实现),符合依赖倒置原则
2. **Entity/PO 分离**: Domain 层使用 Entity,Infrastructure 层使用 PO,通过 Repository 转换
3. **DTO 隔离**: 每层有自己的 DTO,避免跨层数据对象污染

### 替代方案考虑
- **传统三层架构 (Controller-Service-DAO)**: 简单,但业务逻辑容易与技术实现耦合
- **六边形架构 (Hexagonal Architecture)**: 更复杂,对团队要求更高
- **微服务架构**: 当前是单体应用,暂不适用

### 后果
- ✅ 正面: 业务逻辑与技术实现解耦,易于测试和维护
- ✅ 正面: 技术栈可替换,支持长期演进
- ✅ 正面: 为微服务拆分做准备
- ⚠️ 注意: 模块较多 (14个模块),初期搭建成本较高
- ⚠️ 注意: 需要团队理解 DDD 思想和分层架构原则

## ADR-007: 采用 Entity/PO 分离模式

### 状态
已接受 (Accepted)

### 背景
需要在领域层和持久化层之间建立清晰的边界,避免持久化框架注解污染领域模型。

### 决策
采用 Entity/PO 分离模式:
- Entity 位于 repository-api 模块,纯 POJO,无框架注解
- PO 位于 mysql-impl 模块,包含 MyBatis-Plus 注解
- RepositoryImpl 负责 Entity ↔ PO 转换

### 理由
1. **项目宪法要求**: 项目宪法第 VI 条明确规定 Entity/PO 分离
2. **领域模型纯净**: Entity 不包含任何持久化框架注解,保持技术无关性
3. **易于测试**: Entity 是纯 POJO,可以在单元测试中直接使用,无需 Mock 数据库
4. **持久化技术可替换**: 更换 ORM 框架时,只需修改 PO 和 RepositoryImpl,Entity 不受影响
5. **符合 DDD 原则**: 领域模型与基础设施技术解耦

### 命名规范

| 层次 | 命名 | 示例 | 说明 |
|------|------|------|------|
| Domain | 业务概念,无技术后缀 | `User`, `Order` | 纯业务概念 |
| Repository API | 实体后缀 Entity | `UserEntity`, `OrderEntity` | 领域实体 |
| MySQL Implementation | 持久化对象后缀 PO | `UserPO`, `OrderPO` | 数据库表映射 |

### 转换职责
```java
// repository-api 模块
public interface UserRepository {
    UserEntity findById(Long id);
    void save(UserEntity entity);
}

// mysql-impl 模块
@Repository
public class UserRepositoryImpl implements UserRepository {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserEntity findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return convertToEntity(po); // PO → Entity 转换
    }

    @Override
    public void save(UserEntity entity) {
        UserPO po = convertToPO(entity); // Entity → PO 转换
        userMapper.insert(po);
    }
}
```

### 替代方案考虑
- **Entity = PO (单一对象)**: 简单,但领域模型被持久化注解污染
- **使用 JPA 的 @Entity**: 高度耦合 JPA,更换持久化技术困难

### 后果
- ✅ 正面: 领域模型保持纯净,易于测试
- ✅ 正面: 持久化技术可替换,架构灵活
- ✅ 正面: 符合 DDD 和 Clean Architecture 原则
- ⚠️ 注意: 需要编写 Entity ↔ PO 转换代码,略增加开发工作量
- ⚠️ 注意: 需要团队理解分离的必要性,避免混淆

## ADR-008: 采用渐进式模块声明策略

### 状态
已接受 (Accepted)

### 背景
多模块 Maven 项目的模块依赖关系复杂,如果一次性声明所有模块,容易导致编译失败和依赖混乱。

### 决策
采用渐进式模块声明策略:
- 只声明已创建的模块,禁止预先声明尚未创建的模块
- 每创建一个模块,立即在父 POM 或聚合模块中声明
- 每次声明后立即运行 `mvn clean compile` 验证

### 理由
1. **项目宪法要求**: 项目宪法第 III 条明确规定持续编译验证和渐进式模块声明
2. **及早发现问题**: 每次模块创建后立即编译,可以及早发现依赖配置错误
3. **避免依赖混乱**: 预先声明未创建的模块会导致编译失败,阻塞开发
4. **降低复杂度**: 逐步增加模块,每次只关注一个模块的依赖配置

### 实施步骤示例
```
Step 1: 创建父 POM
  → 不声明任何子模块
  → mvn clean compile (成功)

Step 2: 创建 common 模块
  → 在父 POM 中声明 <module>common</module>
  → mvn clean compile (成功)

Step 3: 创建 interface 聚合模块
  → 在父 POM 中声明 <module>interface</module>
  → mvn clean compile (成功)

Step 4: 创建 interface-http 模块
  → 在 interface/pom.xml 中声明 <module>interface-http</module>
  → 在 interface-http/pom.xml 中声明对 common 的依赖
  → mvn clean compile (成功)

...依次类推
```

### 验证策略
1. **编译验证** (每次模块创建后): `mvn clean compile`
2. **依赖树检查** (关键节点): `mvn dependency:tree`
3. **Reactor Build Order 检查**: 查看 Maven 构建日志,确认模块构建顺序正确

### 替代方案考虑
- **一次性声明所有模块**: 简单,但极易出错,调试困难
- **分批声明模块**: 比渐进式好,但不如逐个声明精确

### 后果
- ✅ 正面: 每次只关注一个模块,降低复杂度
- ✅ 正面: 及早发现依赖配置错误,调试简单
- ✅ 正面: 确保项目始终处于可编译状态
- ⚠️ 注意: 需要严格遵循流程,不得跳步或预先声明

## ADR-009: 选择 Druid 作为数据库连接池

### 状态
已接受 (Accepted)

### 背景
需要选择合适的数据库连接池,在性能、监控能力和易用性之间取得平衡。

### 决策
使用 Druid 1.2.20 (druid-spring-boot-3-starter) 作为数据库连接池。

### 理由
1. **监控能力强**: 内置 Web 监控界面,可查看 SQL 执行情况、连接池状态、慢 SQL 统计
2. **SQL 防火墙**: 支持 SQL 防注入、SQL 审计等安全功能
3. **扩展性好**: 支持 Filter 机制,可自定义扩展功能
4. **Spring Boot 3 支持**: 1.2.20 版本提供 druid-spring-boot-3-starter,完全兼容
5. **国内生态成熟**: 阿里开源,国内文档和社区支持好

### 配置要点
```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin
```

### 替代方案考虑
- **HikariCP**: Spring Boot 默认连接池,性能略优,但监控能力不如 Druid
- **Tomcat JDBC Pool**: 性能和功能介于 Druid 和 HikariCP 之间
- **DBCP2**: 老牌连接池,但性能不如新一代连接池

### 后果
- ✅ 正面: 强大的监控和统计能力,便于生产问题排查
- ✅ 正面: SQL 防火墙提供额外的安全保障
- ✅ 正面: 国内生态成熟,文档和社区支持好
- ⚠️ 注意: 性能略低于 HikariCP (但差异不大)
- ⚠️ 注意: 需要配置监控界面的访问权限

## ADR-010: 多环境配置策略

### 状态
已接受 (Accepted)

### 背景
系统需要支持本地开发、开发、测试、预发布、生产 5 种环境,每种环境的配置需求不同。

### 决策
使用 Spring Profiles 机制实现多环境配置:
- `application.yml`: 通用配置 (所有环境共享)
- `application-local.yml`: 本地开发环境
- `application-dev.yml`: 开发环境
- `application-test.yml`: 测试环境
- `application-staging.yml`: 预发布环境
- `application-prod.yml`: 生产环境

### 理由
1. **Spring Boot 原生支持**: 无需额外依赖或插件
2. **配置清晰**: 每个环境有独立的配置文件,易于维护
3. **灵活激活**: 支持通过配置文件、命令行参数、环境变量激活
4. **配置优先级明确**: 环境特定配置覆盖通用配置
5. **与日志配置联动**: logback-spring.xml 使用 `<springProfile>` 标签实现多环境日志配置

### 环境配置差异

| 配置项 | local | dev | test | staging | prod |
|--------|-------|-----|------|---------|------|
| 日志输出目标 | 控制台 | 文件 | 文件 | 文件 | 文件 |
| 日志格式 | 彩色 | JSON | JSON | JSON | JSON |
| 项目包日志级别 | DEBUG | DEBUG | DEBUG | INFO | INFO |
| 框架包日志级别 | WARN | WARN | WARN | WARN | WARN |
| 数据库连接池大小 | 5 | 10 | 10 | 15 | 20 |
| 缓存过期时间 | 短 | 中 | 中 | 长 | 长 |
| 监控指标采集频率 | 低 | 中 | 中 | 高 | 高 |

### 激活方式
```bash
# 方式 1: 配置文件激活 (默认)
spring.profiles.active=dev

# 方式 2: 命令行参数激活
java -jar app.jar --spring.profiles.active=prod

# 方式 3: 环境变量激活
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

### 配置管理原则
- ✅ 敏感信息 (数据库密码) 使用环境变量或配置中心,不直接写入配置文件
- ✅ 通用配置写入 application.yml,减少重复
- ✅ 环境特定配置覆盖通用配置
- ❌ 禁止在 application.yml 中配置日志,所有日志配置在 logback-spring.xml 中

### 替代方案考虑
- **Maven Profiles**: 需要为每个环境打不同的包,不灵活
- **外部配置中心 (Spring Cloud Config, Nacos)**: 当前项目规模不需要,未来可考虑
- **多 application.yml 文件**: 不如 Spring Profiles 机制清晰

### 后果
- ✅ 正面: 多环境配置清晰,易于维护
- ✅ 正面: 激活方式灵活,支持多种场景
- ✅ 正面: 与 Spring Boot 生态深度集成
- ⚠️ 注意: 敏感信息不得直接写入配置文件
- ⚠️ 注意: 配置文件需要纳入版本控制 (除包含敏感信息的文件外)

## Summary

本次架构初始化共记录了 10 个架构决策 (ADR-001 到 ADR-010),涵盖了技术栈选型、架构模式、数据持久化、日志追踪、监控指标、多环境配置等核心领域。所有决策严格遵循项目宪法的 8 条核心原则,为 AIOps Service 项目的长期演进奠定了坚实的基础。

### 核心技术栈总结

| 类别 | 技术选型 | 版本 | 理由 |
|------|---------|------|------|
| 语言 | Java | 21 (LTS) | 现代语言特性 + 长期支持 |
| 框架 | Spring Boot | 3.4.1 | 最新稳定版 + Jakarta EE |
| 微服务 | Spring Cloud | 2025.0.0 | 链路追踪 + 微服务能力 |
| ORM | MyBatis-Plus | 3.5.7 | SQL 可控 + CRUD 增强 |
| 连接池 | Druid | 1.2.20 | 监控能力强 + 安全功能 |
| 链路追踪 | Micrometer Tracing | 1.3.5 | 自动传播 Trace ID |
| 日志 | Logback + JSON Encoder | 7.4 | 结构化日志 + 多环境支持 |
| 监控 | Prometheus + Micrometer | - | 云原生标准 + 丰富指标 |
| 消息队列 | AWS SQS | 2.20.0 | 云原生 + 高可用 |
| 缓存 | Redis (Lettuce) | - | Spring Data Redis 推荐 |

### 架构原则总结

1. **DDD 分层架构**: Interface → Application → Domain → Infrastructure
2. **API/Implementation 分离**: 每层分为接口和实现,符合依赖倒置原则
3. **Entity/PO 分离**: 领域模型保持纯净,持久化对象包含框架注解
4. **渐进式模块声明**: 只声明已创建的模块,持续编译验证
5. **日志配置集中管理**: 所有日志配置在 logback-spring.xml 中,禁止在 application.yml 中配置
6. **MyBatis-Plus 规范**: 条件查询必须在 Mapper XML 中实现,不使用 Wrapper
7. **多环境配置**: 支持 local/dev/test/staging/prod 5 种环境
8. **监控可观测性**: Prometheus 指标 + Trace ID 追踪 + JSON 结构化日志

### 下一步

架构决策已完成,可以进入 Phase 1: 详细设计阶段,生成以下文档:
- ✅ quickstart.md: 快速开始指南
- ✅ contracts/pom-structure.md: POM 配置规范和模块依赖关系文档
