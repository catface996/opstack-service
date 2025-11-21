# Implementation Plan: DDD 多模块项目架构初始化

**Branch**: `001-init-ddd-architecture` | **Date**: 2025-11-21 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-init-ddd-architecture/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

本项目旨在构建一个基于 Spring Boot 3.4.1 和 Spring Cloud 2025.0.0 的 DDD 分层架构多模块 Maven 工程,作为 AIOps 服务的基础架构。项目采用严格的领域驱动设计思想,实现清晰的层次划分(Interface、Application、Domain、Infrastructure)和模块边界,确保技术与业务逻辑的解耦,支持系统的可扩展性和可演进性。

核心技术栈包括:
- Java 21 (LTS)
- Spring Boot 3.4.1
- Spring Cloud 2025.0.0
- MyBatis-Plus 3.5.7 (持久化层)
- Micrometer Tracing (分布式链路追踪)
- Prometheus (监控指标)
- Logback + JSON Encoder (结构化日志)

项目将建立统一的依赖管理、异常处理、日志追踪和监控体系,并支持 local/dev/test/staging/prod 多环境配置。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**:
- Spring Boot 3.4.1
- Spring Cloud 2025.0.0
- MyBatis-Plus 3.5.7 (mybatis-plus-spring-boot3-starter)
- Druid 1.2.20 (数据库连接池)
- Micrometer Tracing 1.3.5 (链路追踪)
- Logstash Logback Encoder 7.4 (JSON 日志)
- AWS SDK for SQS 2.20.0
- Prometheus Micrometer Registry (监控指标)

**Storage**: MySQL (通过 MyBatis-Plus), Redis (通过 Spring Data Redis/Lettuce), AWS SQS (消息队列)
**Testing**: JUnit 5 + Spring Boot Test
**Target Platform**: JVM (Linux/macOS/Windows server)
**Project Type**: Backend service (Maven multi-module project)
**Performance Goals**:
- 首次编译时间 < 2分钟 (含依赖下载)
- 后续编译时间 < 30秒
- 应用启动时间 < 15秒
- Prometheus 指标查询响应 < 1秒

**Constraints**:
- 必须使用 Spring Boot 3.x (与 JDK 21 兼容)
- 必须使用 mybatis-plus-spring-boot3-starter (Spring Boot 3 专用)
- 必须遵循 DDD 分层架构和依赖方向规则
- 必须在 logback-spring.xml 中管理所有日志配置 (禁止在 application.yml 中配置日志)
- 必须实现 Entity/PO 分离模式

**Scale/Scope**:
- 14 个 Maven 模块 (1个父POM + 6个聚合模块 + 7个代码模块)
- 基础架构工程,暂无业务逻辑代码
- 支持未来微服务拆分演进

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ Principle I: 渐进式开发 (Incremental Development)

**Status**: PASS (Phase 1 设计完成后重新验证)

**Compliance**:
- ✅ 本计划遵循 需求分析 → 验证 → 架构设计 → 验证 → 任务拆分 → 验证 → 实现 → 验证 的流程
- ✅ 每个阶段有明确的交付物:
  - Phase 0: research.md (10个ADR已完成)
  - Phase 1: quickstart.md (开发者快速开始指南已完成)
  - Phase 1: contracts/pom-structure.md (POM配置规范已完成)
  - Phase 2: tasks.md (待生成)
- ✅ 任务将按照渐进式模块声明原则执行:先创建父POM,再逐步创建子模块
- ✅ Agent context 已更新 (CLAUDE.md)

**Rationale**: 项目架构初始化是最基础的阶段,必须严格遵循渐进式流程,避免一次性创建所有模块导致依赖混乱。Phase 1 设计文档已完整交付。

### ✅ Principle II: DDD 分层架构 (DDD Layered Architecture)

**Status**: PASS

**Compliance**:
- ✅ 项目结构严格遵循 DDD 四层架构: Interface → Application → Domain → Infrastructure
- ✅ 依赖方向正确: 外层依赖内层,内层不依赖外层
- ✅ Application 层依赖 Infrastructure API,而非 Implementation
- ✅ 模块划分清晰: interface-http, interface-consumer (接口层); application-api, application-impl (应用层); domain-api, domain-impl (领域层); repository-api, mysql-impl, cache-api, redis-impl, mq-api, sqs-impl (基础设施层)

**Rationale**: 这是本项目的核心架构原则,所有模块设计都基于此。

### ✅ Principle III: 持续编译验证 (Continuous Compilation Validation)

**Status**: PASS

**Compliance**:
- ✅ 任务执行策略明确要求:每个模块创建后立即运行 `mvn clean compile` 验证
- ✅ 采用渐进式模块声明:只声明已创建的模块,禁止预先声明
- ✅ 验证优先级策略:运行时验证 > 编译验证 > 静态检查

**Rationale**: 多模块项目的编译依赖关系复杂,必须持续验证以避免依赖错误累积。

### ✅ Principle IV: 中文优先 (Chinese-First Communication)

**Status**: PASS

**Compliance**:
- ✅ 所有文档、注释使用中文
- ✅ 代码本身 (类名、变量名) 使用英文
- ✅ 技术术语保留英文 (Maven、POM、DDD、Spring Boot)

**Rationale**: 符合团队沟通习惯,文档清晰易读。

### ✅ Principle V: 依赖版本统一管理 (Unified Dependency Management)

**Status**: PASS

**Compliance**:
- ✅ 父 POM 统一管理所有依赖版本
- ✅ 导入 Spring Boot 3.4.1 BOM 和 Spring Cloud 2025.0.0 BOM
- ✅ 第三方库版本在 dependencyManagement 中声明: MyBatis-Plus 3.5.7, Druid 1.2.20, Micrometer Tracing 1.3.5, Logstash Logback Encoder 7.4, AWS SDK 2.20.0
- ✅ 子模块声明依赖时不指定版本号
- ✅ 模块命名使用首字母大写英文单词 + 空格 (如 "Domain API", "MySQL Implementation")

**Rationale**: 统一版本管理是多模块项目的基础,避免依赖冲突。

### ✅ Principle VI: Entity/PO 分离 (Entity/PO Separation)

**Status**: PASS

**Compliance**:
- ✅ Entity 位于 repository-api 模块,纯 POJO,无框架注解
- ✅ PO 位于 mysql-impl 模块,包含 MyBatis-Plus 注解
- ✅ 命名规范: Domain 层使用业务概念 (User, Order), Repository API 层使用 UserEntity/OrderEntity, MySQL 实现层使用 UserPO/OrderPO
- ✅ MybatisPlusConfig 放在 mysql-impl 模块

**Rationale**: 框架无关的领域实体易于测试,支持持久化技术替换。

### ✅ Principle VII: MyBatis-Plus 数据操作规范 (MyBatis-Plus Data Operation Standards)

**Status**: PASS (架构阶段)

**Compliance**:
- ✅ 规范已在架构设计中明确:允许使用 save/updateById/getById API,所有条件查询必须在 Mapper XML 中实现
- ✅ Mapper XML 路径规范: mysql-impl/src/main/resources/mapper/
- ✅ 后续实现阶段将严格遵循此规范

**Rationale**: 统一数据操作规范,便于代码审查和性能优化。

### ✅ Principle VIII: ADR 架构决策记录 (Architecture Decision Record)

**Status**: PASS (Phase 1 设计完成后重新验证)

**Compliance**:
- ✅ 本次架构初始化已在 research.md 中记录 10 个完整的 ADR
- ✅ 关键决策已记录:
  - ADR-001: 选择 Java 21 作为开发语言
  - ADR-002: 选择 Spring Boot 3.4.1 + Spring Cloud 2025.0.0
  - ADR-003: 选择 MyBatis-Plus 3.5.7 作为 ORM 框架
  - ADR-004: 选择 Micrometer Tracing + Logback JSON 作为日志追踪方案
  - ADR-005: 选择 Prometheus + Micrometer 作为监控指标方案
  - ADR-006: 采用 DDD 分层架构而非传统三层架构
  - ADR-007: 采用 Entity/PO 分离模式
  - ADR-008: 采用渐进式模块声明策略
  - ADR-009: 选择 Druid 作为数据库连接池
  - ADR-010: 多环境配置策略
- ✅ 每个 ADR 包含完整的结构: 状态、背景、决策、理由、替代方案考虑、后果

**Rationale**: 记录架构决策的上下文和理由,为未来的技术演进提供参考。所有重要架构决策已完整记录在 research.md 中。

### 🎯 Constitution Check Summary

**Overall Status**: ✅ ALL GATES PASSED (Phase 1 设计完成后重新验证)

所有 8 条宪法原则均已通过检查。Phase 0 (研究) 和 Phase 1 (设计) 已完成,可以继续进入 Phase 2 任务拆分阶段。

**Phase 1 交付物清单**:
- ✅ research.md: 10个完整的ADR架构决策记录
- ✅ quickstart.md: 开发者快速开始指南 (包含环境配置、编译、启动、验证、故障排查)
- ✅ contracts/pom-structure.md: POM配置规范和模块依赖关系文档 (包含父POM模板、14个模块配置示例、依赖关系矩阵)
- ✅ CLAUDE.md: Agent context 已更新 (添加 Java 21 和数据库信息)
- ℹ️ data-model.md: N/A (本特性为架构初始化,无业务实体)

## Project Structure

### Documentation (this feature)

```text
specs/001-init-ddd-architecture/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command) - N/A for this feature
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command) - N/A for this feature
│   └── pom-structure.md # POM 配置规范和模块依赖关系
├── checklists/          # Quality checklists
│   └── requirements.md  # Requirements quality checklist
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

**Note**: 由于本特性是项目架构初始化,不涉及业务实体和 API 接口,因此:
- data-model.md 不适用 (无业务实体)
- contracts/ 目录调整为存放 POM 配置规范文档而非 API 契约

### Source Code (repository root)

项目采用 Maven 多模块结构,遵循 DDD 分层架构:

```text
aiops-service/                          # 项目根目录
├── pom.xml                              # 父 POM (packaging=pom, 聚合所有顶层模块)
│
├── common/                              # 通用模块 (packaging=jar)
│   ├── pom.xml
│   └── src/main/java/com/catface996/aiops/common/
│       ├── exception/                   # 异常体系 (BaseException, BusinessException, SystemException)
│       ├── result/                      # 统一响应对象 (Result)
│       └── util/                        # 通用工具类
│
├── bootstrap/                           # 启动模块 (packaging=jar)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/catface996/aiops/bootstrap/
│       │   │   └── Application.java     # Spring Boot 主启动类
│       │   └── resources/
│       │       ├── application.yml                  # 通用配置
│       │       ├── application-local.yml            # 本地环境配置
│       │       ├── application-dev.yml              # 开发环境配置
│       │       ├── application-test.yml             # 测试环境配置
│       │       ├── application-staging.yml          # 预发布环境配置
│       │       ├── application-prod.yml             # 生产环境配置
│       │       └── logback-spring.xml               # 日志配置
│       └── test/                        # 集成测试
│
├── interface/                           # 接口层聚合模块 (packaging=pom)
│   ├── pom.xml
│   ├── interface-http/                  # HTTP 接口模块 (packaging=jar)
│   │   ├── pom.xml
│   │   └── src/main/java/com/catface996/aiops/interface_/http/
│   │       ├── controller/              # REST Controllers
│   │       ├── dto/                     # 数据传输对象 (Request/Response)
│   │       └── exception/               # 全局异常处理器 (@RestControllerAdvice)
│   └── interface-consumer/              # 消息消费者模块 (packaging=jar)
│       ├── pom.xml
│       └── src/main/java/com/catface996/aiops/interface_/consumer/
│           ├── listener/                # 消息监听器
│           └── exception/               # 全局异常处理器 (@ControllerAdvice)
│
├── application/                         # 应用层聚合模块 (packaging=pom)
│   ├── pom.xml
│   ├── application-api/                 # 应用服务接口 (packaging=jar)
│   │   ├── pom.xml
│   │   └── src/main/java/com/catface996/aiops/application/api/
│   │       ├── service/                 # 应用服务接口
│   │       ├── dto/                     # 数据传输对象
│   │       ├── command/                 # 命令对象
│   │       └── query/                   # 查询对象
│   └── application-impl/                # 应用服务实现 (packaging=jar)
│       ├── pom.xml
│       └── src/main/java/com/catface996/aiops/application/impl/
│           └── service/                 # 应用服务实现类
│
├── domain/                              # 领域层聚合模块 (packaging=pom)
│   ├── pom.xml
│   ├── domain-api/                      # 领域模型定义 (packaging=jar)
│   │   ├── pom.xml
│   │   └── src/main/java/com/catface996/aiops/domain/api/
│   │       ├── model/                   # 领域模型 (聚合根、实体、值对象)
│   │       ├── repository/              # 仓储接口
│   │       ├── service/                 # 领域服务接口
│   │       └── event/                   # 领域事件
│   └── domain-impl/                     # 领域逻辑实现 (packaging=jar)
│       ├── pom.xml
│       └── src/main/java/com/catface996/aiops/domain/impl/
│           └── service/                 # 领域服务实现类
│
└── infrastructure/                      # 基础设施层聚合模块 (packaging=pom)
    ├── pom.xml
    ├── repository/                      # 仓储层聚合模块 (packaging=pom)
    │   ├── pom.xml
    │   ├── repository-api/              # 仓储接口 (packaging=jar)
    │   │   ├── pom.xml
    │   │   └── src/main/java/com/catface996/aiops/infrastructure/repository/api/
    │   │       ├── entity/              # 领域实体 (UserEntity, OrderEntity)
    │   │       └── repository/          # 仓储接口定义
    │   └── mysql-impl/                  # MySQL 实现 (packaging=jar)
    │       ├── pom.xml
    │       └── src/
    │           └── main/
    │               ├── java/com/catface996/aiops/infrastructure/repository/mysql/
    │               │   ├── po/          # 持久化对象 (UserPO, OrderPO)
    │               │   ├── mapper/      # MyBatis Mapper 接口
    │               │   ├── repository/  # 仓储实现类
    │               │   └── config/      # MybatisPlusConfig
    │               └── resources/
    │                   └── mapper/      # Mapper XML 文件
    │
    ├── cache/                           # 缓存层聚合模块 (packaging=pom)
    │   ├── pom.xml
    │   ├── cache-api/                   # 缓存接口 (packaging=jar)
    │   │   ├── pom.xml
    │   │   └── src/main/java/com/catface996/aiops/infrastructure/cache/api/
    │   │       └── service/             # 缓存服务接口
    │   └── redis-impl/                  # Redis 实现 (packaging=jar)
    │       ├── pom.xml
    │       └── src/main/java/com/catface996/aiops/infrastructure/cache/redis/
    │           ├── service/             # 缓存服务实现类
    │           └── config/              # RedisConfig
    │
    └── mq/                              # 消息队列层聚合模块 (packaging=pom)
        ├── pom.xml
        ├── mq-api/                      # 消息队列接口 (packaging=jar)
        │   ├── pom.xml
        │   └── src/main/java/com/catface996/aiops/infrastructure/mq/api/
        │       ├── producer/            # 消息生产者接口
        │       └── dto/                 # 消息传输对象
        └── sqs-impl/                    # SQS 实现 (packaging=jar)
            ├── pom.xml
            └── src/main/java/com/catface996/aiops/infrastructure/mq/sqs/
                ├── producer/            # 消息生产者实现类
                └── config/              # SQS Config
```

**Structure Decision**:

本项目采用 Maven 多模块结构,结合 DDD 分层架构原则,实现了清晰的模块划分和依赖管理:

1. **聚合模块 (packaging=pom)**: interface, application, domain, infrastructure, repository, cache, mq - 用于聚合子模块,体现 DDD 分层结构

2. **代码模块 (packaging=jar)**: common, bootstrap, interface-http, interface-consumer, application-api, application-impl, domain-api, domain-impl, repository-api, mysql-impl, cache-api, redis-impl, mq-api, sqs-impl - 包含实际代码

3. **依赖方向**:
   - bootstrap → 所有 *-impl 模块 + common (最终组装)
   - interface-* → application-api + common
   - application-impl → application-api + domain-api + common
   - domain-impl → domain-api + repository-api + cache-api + mq-api + common
   - *-impl (基础设施) → *-api + common

4. **包命名规范**: com.catface996.aiops.{layer}.{module}
   - 使用 interface_ 而非 interface (因为 interface 是 Java 关键字)

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

本项目无宪法原则违规,此章节不适用。

所有架构设计严格遵循项目宪法的 8 条核心原则,未引入额外的复杂度。
