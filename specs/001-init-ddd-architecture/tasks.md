# Tasks: DDD 多模块项目架构初始化

**Feature**: 001-init-ddd-architecture | **Input**: Design documents from `/specs/001-init-ddd-architecture/`

**Prerequisites**:
- plan.md (已完成) - 技术栈、项目结构、依赖管理
- spec.md (已完成) - 6 个用户故事 (3个P1, 3个P2)
- research.md (已完成) - 10个ADR架构决策记录
- contracts/pom-structure.md (已完成) - POM配置规范和模块依赖关系
- quickstart.md (已完成) - 开发者快速开始指南

**Tests**: 本项目采用编译验证和运行时验证策略,不需要单独编写单元测试代码。每个任务完成后通过 `mvn clean compile` 或实际运行验证。

**Organization**: 任务按用户故事组织,每个故事独立完成后可验证。遵循渐进式模块声明原则:只声明已创建的模块,每个模块创建后立即编译验证。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可以并行执行 (不同文件,无依赖)
- **[Story]**: 任务所属用户故事 (US1, US2, US3...)
- 包含精确的文件路径

## Path Conventions

本项目为 Maven 多模块结构,路径从项目根目录 `aiops-service/` 开始:
- 父 POM: `aiops-service/pom.xml`
- 聚合模块: `aiops-service/interface/pom.xml`
- 代码模块: `aiops-service/common/pom.xml`, `aiops-service/interface/interface-http/pom.xml`
- Java 代码: `aiops-service/common/src/main/java/com/catface996/aiops/common/`
- 资源文件: `aiops-service/bootstrap/src/main/resources/`

---

## Phase 1: Setup (项目初始化)

**Purpose**: 建立 Maven 父 POM 和基本项目结构

- [X] T001 创建父 POM 在 pom.xml (groupId: com.catface996.aiops, artifactId: aiops-service, packaging: pom, name: "AIOps Service")
- [X] T002 配置父 POM 的 properties 在 pom.xml (java.version=21, maven.compiler.source=21, maven.compiler.target=21, UTF-8 encoding)
- [X] T003 配置父 POM 的 dependencyManagement 在 pom.xml (导入 Spring Boot 3.4.1 BOM 和 Spring Cloud 2025.0.0 BOM)
- [X] T004 在父 POM 的 dependencyManagement 中声明第三方库版本 (MyBatis-Plus 3.5.7, Druid 1.2.20, Micrometer Tracing 1.3.5, Logstash Logback Encoder 7.4, AWS SDK 2.20.0)
- [X] T005 配置父 POM 的 build/pluginManagement 在 pom.xml (spring-boot-maven-plugin, maven-compiler-plugin)
- [X] T006 验证父 POM 配置正确 (运行 mvn clean compile,预期成功)

**Checkpoint**: 父 POM 已创建,基础依赖管理已配置,可以开始创建子模块

---

## Phase 2: Foundational (基础模块 - 阻塞所有用户故事)

**Purpose**: 创建 common 模块和基础聚合模块骨架,这些是所有后续模块的依赖基础

**⚠️ CRITICAL**: 此阶段必须完成后才能开始任何用户故事实现

### 创建 common 模块

- [X] T007 在父 POM 中声明 common 模块 (在 pom.xml 的 modules 中添加 <module>common</module>)
- [X] T008 创建 common/pom.xml (parent: aiops-service, artifactId: common, packaging: jar, name: "Common")
- [X] T009 [P] 创建 common 模块基本包结构 (common/src/main/java/com/catface996/aiops/common/)
- [X] T010 [P] 在 common/pom.xml 中添加基础依赖 (slf4j-api, lombok, jakarta.validation-api)
- [X] T011 验证 common 模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order 包含 common)

### 创建基础设施层聚合模块骨架

- [X] T012 在父 POM 中声明 infrastructure 模块 (在 pom.xml 的 modules 中添加 <module>infrastructure</module>)
- [X] T013 创建 infrastructure/pom.xml (parent: aiops-service, artifactId: infrastructure, packaging: pom, name: "Infrastructure", modules 为空)
- [X] T014 在 infrastructure 下创建 repository/pom.xml (parent: infrastructure, artifactId: repository, packaging: pom, name: "Repository", modules 为空)
- [X] T015 [P] 在 infrastructure 下创建 cache/pom.xml (parent: infrastructure, artifactId: cache, packaging: pom, name: "Cache", modules 为空)
- [X] T016 [P] 在 infrastructure 下创建 mq/pom.xml (parent: infrastructure, artifactId: mq, packaging: pom, name: "MQ", modules 为空)
- [X] T017 在 infrastructure/pom.xml 的 modules 中声明 repository, cache, mq 三个子模块
- [X] T018 验证基础设施层聚合模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order 包含 infrastructure → repository/cache/mq)

### 创建领域层聚合模块骨架

- [X] T019 在父 POM 中声明 domain 模块 (在 pom.xml 的 modules 中添加 <module>domain</module>)
- [X] T020 创建 domain/pom.xml (parent: aiops-service, artifactId: domain, packaging: pom, name: "Domain", modules 为空)
- [X] T021 验证 domain 聚合模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order 包含 domain)

### 创建应用层聚合模块骨架

- [X] T022 在父 POM 中声明 application 模块 (在 pom.xml 的 modules 中添加 <module>application</module>)
- [X] T023 创建 application/pom.xml (parent: aiops-service, artifactId: application, packaging: pom, name: "Application", modules 为空)
- [X] T024 验证 application 聚合模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order 包含 application)

### 创建接口层聚合模块骨架

- [X] T025 在父 POM 中声明 interface 模块 (在 pom.xml 的 modules 中添加 <module>interface</module>)
- [X] T026 创建 interface/pom.xml (parent: aiops-service, artifactId: interface, packaging: pom, name: "Interface", modules 为空)
- [X] T027 验证 interface 聚合模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order 包含 interface)

**Checkpoint**: 基础模块骨架已创建 (common + 4个聚合模块),项目可成功编译,用户故事实现可以开始

---

## Phase 3: User Story 1 - 创建基础 Maven 多模块项目结构 (Priority: P1) 🎯 MVP

**Goal**: 创建完整的 Maven 多模块项目结构,包括所有代码模块 (14个模块),确保项目按正确的依赖顺序编译成功

**Independent Test**: 执行 `mvn clean compile` 验证所有模块按正确顺序编译成功,查看 Maven Reactor Build Order 确认依赖顺序正确,无循环依赖

### 创建基础设施层 API 模块

- [ ] T028 [P] [US1] 在 repository/pom.xml 的 modules 中声明 repository-api 子模块
- [ ] T029 [P] [US1] 创建 repository/repository-api/pom.xml (parent: repository, artifactId: repository-api, packaging: jar, name: "Repository API", 依赖 common)
- [ ] T030 [P] [US1] 创建 repository-api 模块基本包结构 (repository-api/src/main/java/com/catface996/aiops/infrastructure/repository/api/, 包含 entity/ 和 repository/ 子包)
- [ ] T031 [P] [US1] 在 cache/pom.xml 的 modules 中声明 cache-api 子模块
- [ ] T032 [P] [US1] 创建 cache/cache-api/pom.xml (parent: cache, artifactId: cache-api, packaging: jar, name: "Cache API", 依赖 common)
- [ ] T033 [P] [US1] 创建 cache-api 模块基本包结构 (cache-api/src/main/java/com/catface996/aiops/infrastructure/cache/api/service/)
- [ ] T034 [P] [US1] 在 mq/pom.xml 的 modules 中声明 mq-api 子模块
- [ ] T035 [P] [US1] 创建 mq/mq-api/pom.xml (parent: mq, artifactId: mq-api, packaging: jar, name: "MQ API", 依赖 common)
- [ ] T036 [P] [US1] 创建 mq-api 模块基本包结构 (mq-api/src/main/java/com/catface996/aiops/infrastructure/mq/api/, 包含 producer/ 和 dto/ 子包)
- [ ] T037 [US1] 验证基础设施层 API 模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order: common → repository-api/cache-api/mq-api)

### 创建基础设施层实现模块

- [ ] T038 [P] [US1] 在 repository/pom.xml 的 modules 中声明 mysql-impl 子模块
- [ ] T039 [P] [US1] 创建 repository/mysql-impl/pom.xml (parent: repository, artifactId: mysql-impl, packaging: jar, name: "MySQL Implementation", 依赖 repository-api, common, mybatis-plus-spring-boot3-starter, druid-spring-boot-3-starter, mysql-connector-j)
- [ ] T040 [P] [US1] 创建 mysql-impl 模块基本包结构 (mysql-impl/src/main/java/com/catface996/aiops/infrastructure/repository/mysql/, 包含 po/, mapper/, repository/, config/ 子包)
- [ ] T041 [P] [US1] 创建 mysql-impl 的 resources 目录结构 (mysql-impl/src/main/resources/mapper/)
- [ ] T042 [P] [US1] 在 cache/pom.xml 的 modules 中声明 redis-impl 子模块
- [ ] T043 [P] [US1] 创建 cache/redis-impl/pom.xml (parent: cache, artifactId: redis-impl, packaging: jar, name: "Redis Implementation", 依赖 cache-api, common, spring-boot-starter-data-redis)
- [ ] T044 [P] [US1] 创建 redis-impl 模块基本包结构 (redis-impl/src/main/java/com/catface996/aiops/infrastructure/cache/redis/, 包含 service/ 和 config/ 子包)
- [ ] T045 [P] [US1] 在 mq/pom.xml 的 modules 中声明 sqs-impl 子模块
- [ ] T046 [P] [US1] 创建 mq/sqs-impl/pom.xml (parent: mq, artifactId: sqs-impl, packaging: jar, name: "SQS Implementation", 依赖 mq-api, common, aws-java-sdk-sqs)
- [ ] T047 [P] [US1] 创建 sqs-impl 模块基本包结构 (sqs-impl/src/main/java/com/catface996/aiops/infrastructure/mq/sqs/, 包含 producer/ 和 config/ 子包)
- [ ] T048 [US1] 验证基础设施层实现模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order: *-api → *-impl)

### 创建领域层模块

- [ ] T049 [P] [US1] 在 domain/pom.xml 的 modules 中声明 domain-api 和 domain-impl 子模块
- [ ] T050 [P] [US1] 创建 domain/domain-api/pom.xml (parent: domain, artifactId: domain-api, packaging: jar, name: "Domain API", 依赖 common)
- [ ] T051 [P] [US1] 创建 domain-api 模块基本包结构 (domain-api/src/main/java/com/catface996/aiops/domain/api/, 包含 model/, repository/, service/, event/ 子包)
- [ ] T052 [P] [US1] 创建 domain/domain-impl/pom.xml (parent: domain, artifactId: domain-impl, packaging: jar, name: "Domain Implementation", 依赖 domain-api, repository-api, cache-api, mq-api, common)
- [ ] T053 [P] [US1] 创建 domain-impl 模块基本包结构 (domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/)
- [ ] T054 [US1] 验证领域层模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order: *-api → domain-api → domain-impl)

### 创建应用层模块

- [ ] T055 [P] [US1] 在 application/pom.xml 的 modules 中声明 application-api 和 application-impl 子模块
- [ ] T056 [P] [US1] 创建 application/application-api/pom.xml (parent: application, artifactId: application-api, packaging: jar, name: "Application API", 依赖 common)
- [ ] T057 [P] [US1] 创建 application-api 模块基本包结构 (application-api/src/main/java/com/catface996/aiops/application/api/, 包含 service/, dto/, command/, query/ 子包)
- [ ] T058 [P] [US1] 创建 application/application-impl/pom.xml (parent: application, artifactId: application-impl, packaging: jar, name: "Application Implementation", 依赖 application-api, domain-api, common)
- [ ] T059 [P] [US1] 创建 application-impl 模块基本包结构 (application-impl/src/main/java/com/catface996/aiops/application/impl/service/)
- [ ] T060 [US1] 验证应用层模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order: domain-api → application-api → application-impl)

### 创建接口层模块

- [ ] T061 [P] [US1] 在 interface/pom.xml 的 modules 中声明 interface-http 和 interface-consumer 子模块
- [ ] T062 [P] [US1] 创建 interface/interface-http/pom.xml (parent: interface, artifactId: interface-http, packaging: jar, name: "Interface HTTP", 依赖 application-api, common, spring-web, spring-webmvc, hibernate-validator)
- [ ] T063 [P] [US1] 创建 interface-http 模块基本包结构 (interface-http/src/main/java/com/catface996/aiops/interface_/http/, 包含 controller/, dto/, exception/ 子包)
- [ ] T064 [P] [US1] 创建 interface/interface-consumer/pom.xml (parent: interface, artifactId: interface-consumer, packaging: jar, name: "Interface Consumer", 依赖 application-api, common)
- [ ] T065 [P] [US1] 创建 interface-consumer 模块基本包结构 (interface-consumer/src/main/java/com/catface996/aiops/interface_/consumer/, 包含 listener/ 和 exception/ 子包)
- [ ] T066 [US1] 验证接口层模块编译成功 (运行 mvn clean compile,检查 Reactor Build Order: application-api → interface-http/interface-consumer)

### 创建 bootstrap 启动模块

- [ ] T067 [US1] 在父 POM 中声明 bootstrap 模块 (在 pom.xml 的 modules 中添加 <module>bootstrap</module>)
- [ ] T068 [US1] 创建 bootstrap/pom.xml (parent: aiops-service, artifactId: bootstrap, packaging: jar, name: "Bootstrap")
- [ ] T069 [US1] 在 bootstrap/pom.xml 中添加所有实现模块依赖 (interface-http, interface-consumer, application-impl, domain-impl, mysql-impl, redis-impl, sqs-impl, common)
- [ ] T070 [US1] 在 bootstrap/pom.xml 中添加 Spring Boot 核心依赖 (spring-boot-starter-web, spring-boot-starter-actuator, micrometer-tracing-bridge-brave, micrometer-registry-prometheus, logstash-logback-encoder, spring-boot-starter-test)
- [ ] T071 [US1] 在 bootstrap/pom.xml 的 build/plugins 中配置 spring-boot-maven-plugin (repackage goal)
- [ ] T072 [US1] 创建 bootstrap 模块包结构 (bootstrap/src/main/java/com/catface996/aiops/bootstrap/)
- [ ] T073 [US1] 创建 bootstrap 的 resources 目录 (bootstrap/src/main/resources/)
- [ ] T074 [US1] 创建 Spring Boot 主启动类 Application.java (在 bootstrap/src/main/java/com/catface996/aiops/bootstrap/Application.java, @SpringBootApplication, @ComponentScan("com.catface996.aiops"))
- [ ] T075 [US1] 验证整个项目编译成功 (运行 mvn clean compile,检查完整的 Reactor Build Order 显示所有 14 个模块按正确顺序编译)
- [ ] T076 [US1] 验证项目打包成功 (运行 mvn clean package,检查 bootstrap/target/bootstrap-*.jar 生成)

**Checkpoint**: User Story 1 完成 - 完整的 Maven 多模块项目结构已创建,所有模块编译成功,依赖关系正确

---

## Phase 4: User Story 2 - 配置统一依赖管理和技术栈版本 (Priority: P1)

**Goal**: 确保父 POM 的 dependencyManagement 配置完整,所有技术栈版本统一管理,子模块依赖不指定版本号

**Independent Test**: 检查父 POM 的 `<dependencyManagement>` 包含所有必需的依赖版本,子模块 pom.xml 中依赖未指定版本号,运行 `mvn clean compile` 无版本冲突警告

**Note**: 此 User Story 的大部分工作已在 Phase 1 (T003-T005) 和 Phase 3 (子模块创建) 中完成,此阶段主要进行验证和补充

### 验证和补充依赖管理配置

- [X] T077 [US2] 验证父 POM 的 dependencyManagement 包含所有必需的 BOM (spring-boot-dependencies 3.4.1, spring-cloud-dependencies 2025.0.0, micrometer-tracing-bom 1.3.5)
- [X] T078 [US2] 验证父 POM 的 dependencyManagement 包含所有第三方库版本声明 (mybatis-plus-spring-boot3-starter 3.5.7, druid-spring-boot-3-starter 1.2.20, logstash-logback-encoder 7.4, aws-java-sdk-sqs 2.20.0)
- [X] T079 [US2] 检查所有子模块的 pom.xml,确保依赖声明不包含 <version> 标签 (检查 14 个模块的 pom.xml)
- [X] T080 [US2] 运行 `mvn dependency:tree` 检查依赖树,确认所有依赖版本与父 POM 定义一致
- [X] T081 [US2] 运行 `mvn clean compile -X` 查看详细构建日志,确认无版本冲突警告 (如 "version XX is being overridden")
- [X] T082 [US2] 创建依赖版本验证文档 (在项目根目录创建 DEPENDENCIES.md,列出所有技术栈版本和来源)

**Checkpoint**: User Story 2 完成 - 依赖版本统一管理已验证,所有子模块使用父 POM 定义的版本,无版本冲突

---

## Phase 5: User Story 3 - 集成分布式链路追踪和结构化日志 (Priority: P1)

**Goal**: 配置 Micrometer Tracing 自动生成 Trace ID/Span ID,配置 Logback 输出结构化 JSON 日志,支持多环境日志策略

**Independent Test**: 启动应用并发送 HTTP 请求,检查日志输出包含 traceId 和 spanId 字段;验证 local 环境输出彩色日志到控制台,其他环境输出 JSON 日志到文件

### 创建基础配置文件

- [X] T083 [P] [US3] 创建 application.yml 在 bootstrap/src/main/resources/ (配置 spring.profiles.active=local, spring.application.name=aiops-service)
- [X] T084 [P] [US3] 创建 application-local.yml 在 bootstrap/src/main/resources/ (本地环境配置:空文件或基本配置)
- [X] T085 [P] [US3] 创建 application-dev.yml 在 bootstrap/src/main/resources/ (开发环境配置)
- [X] T086 [P] [US3] 创建 application-test.yml 在 bootstrap/src/main/resources/ (测试环境配置)
- [X] T087 [P] [US3] 创建 application-staging.yml 在 bootstrap/src/main/resources/ (预发布环境配置)
- [X] T088 [P] [US3] 创建 application-prod.yml 在 bootstrap/src/main/resources/ (生产环境配置)

### 配置 Logback 多环境日志

- [X] T089 [US3] 创建 logback-spring.xml 在 bootstrap/src/main/resources/ (使用 <springProfile> 标签区分环境)
- [X] T090 [US3] 在 logback-spring.xml 中配置 local profile (输出到控制台,Spring Boot 默认彩色格式,项目包 DEBUG 级别,框架包 WARN 级别)
- [X] T091 [US3] 在 logback-spring.xml 中配置 dev/test profile (输出到文件 logs/app.log,JSON 格式,项目包 DEBUG 级别,框架包 WARN 级别)
- [X] T092 [US3] 在 logback-spring.xml 中配置 staging/prod profile (输出到文件,JSON 格式,项目包 INFO 级别,框架包 WARN 级别,使用 AsyncAppender)
- [X] T093 [US3] 在 logback-spring.xml 中配置日志字段 (timestamp, level, thread, logger, traceId, spanId, message, exception)
- [X] T094 [US3] 在 logback-spring.xml 中配置日志滚动策略 (按日期滚动,单文件超过 100MB 分割,非生产环境保留 30 天,生产环境保留 90 天)
- [X] T095 [US3] 在 logback-spring.xml 中配置 ERROR 级别日志单独输出到 error.log

### 配置 Micrometer Tracing

- [X] T096 [US3] 在 application.yml 中配置 Micrometer Tracing (management.tracing.sampling.probability=1.0 确保所有请求都追踪)
- [X] T097 [US3] 验证 bootstrap/pom.xml 包含 micrometer-tracing-bridge-brave 依赖 (在 Phase 3 T070 已添加)

### 运行时验证

- [X] T098 [US3] 编译项目 (运行 mvn clean package 确保配置文件正确)
- [X] T099 [US3] 启动应用 (local 环境: java -jar bootstrap/target/bootstrap-*.jar --spring.profiles.active=local)
- [X] T100 [US3] 验证应用启动成功 (检查日志输出,确认在 15 秒内启动完成)
- [X] T101 [US3] 发送测试请求 (curl http://localhost:8080/actuator/health)
- [X] T102 [US3] 验证日志包含 traceId 和 spanId (检查控制台日志输出格式)
- [X] T103 [US3] 验证 local 环境彩色日志输出 (确认日志有颜色,可读性好)
- [X] T104 [US3] 测试 dev 环境日志输出 (重新启动: --spring.profiles.active=dev,检查 logs/app.log 为 JSON 格式)
- [X] T105 [US3] 测试 prod 环境日志输出 (重新启动: --spring.profiles.active=prod,检查 logs/app.log 为 JSON 格式,INFO 级别)
- [X] T106 [US3] 验证日志级别配置正确 (检查项目包 com.catface996.aiops.* 为 DEBUG/INFO,框架包为 WARN)

**Checkpoint**: User Story 3 完成 - 分布式链路追踪和结构化日志已集成,支持多环境配置,日志输出包含 traceId/spanId

---

## Phase 6: User Story 4 - 实现统一异常处理和错误响应 (Priority: P2)

**Goal**: 在 common 模块定义异常体系 (BaseException, BusinessException, SystemException) 和统一响应对象 (Result),在接口层实现全局异常处理器

**Independent Test**: 在 Controller 中抛出 BusinessException,检查返回的 HTTP 响应是否符合统一的 Result 格式

### 定义异常体系

- [X] T107 [P] [US4] 创建 BaseException.java 在 common/src/main/java/com/catface996/aiops/common/exception/ (包含 errorCode, errorMessage 字段)
- [X] T108 [P] [US4] 创建 BusinessException.java 在 common/src/main/java/com/catface996/aiops/common/exception/ (继承 BaseException,用于业务异常)
- [X] T109 [P] [US4] 创建 SystemException.java 在 common/src/main/java/com/catface996/aiops/common/exception/ (继承 BaseException,用于系统异常)

### 定义统一响应对象

- [X] T110 [US4] 创建 Result.java 在 common/src/main/java/com/catface996/aiops/common/result/ (包含 code, message, data 字段,提供 success(), failure() 静态方法)
- [X] T111 [US4] 验证 common 模块编译成功 (运行 mvn clean compile -pl common)

### 实现全局异常处理器

- [X] T112 [US4] 创建 GlobalExceptionHandler.java 在 interface-http/src/main/java/com/catface996/aiops/interface_/http/exception/ (使用 @RestControllerAdvice 注解)
- [X] T113 [US4] 在 GlobalExceptionHandler 中处理 BusinessException (返回包含错误码和错误消息的 Result 对象)
- [X] T114 [US4] 在 GlobalExceptionHandler 中处理 SystemException (返回通用系统错误响应)
- [X] T115 [US4] 在 GlobalExceptionHandler 中处理未知异常 (Exception.class,返回通用错误响应,不暴露内部实现细节)
- [X] T116 [US4] 创建 GlobalConsumerExceptionHandler.java 在 interface-consumer/src/main/java/com/catface996/aiops/interface_/consumer/exception/ (使用 @ControllerAdvice 注解,处理消息消费者异常)

### 创建测试 Controller

- [X] T117 [US4] 创建 HealthController.java 在 interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ (提供 /health 端点,用于测试)
- [X] T118 [US4] 创建 ExceptionTestController.java 在 interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ (提供 /test/business-exception 和 /test/system-exception 端点,用于测试异常处理)

### 运行时验证

- [X] T119 [US4] 编译项目 (运行 mvn clean package)
- [X] T120 [US4] 启动应用 (java -jar bootstrap/target/bootstrap-*.jar --spring.profiles.active=local)
- [X] T121 [US4] 测试 BusinessException 处理 (curl http://localhost:8080/test/business-exception,检查返回 JSON 格式的 Result 对象)
- [X] T122 [US4] 测试 SystemException 处理 (curl http://localhost:8080/test/system-exception,检查返回通用错误响应)
- [X] T123 [US4] 验证错误响应不暴露内部实现细节 (检查响应中不包含堆栈跟踪或敏感信息)

**Checkpoint**: User Story 4 完成 - 统一异常处理和错误响应已实现,所有异常被正确捕获并转换为标准格式

---

## Phase 7: User Story 5 - 集成 Prometheus 监控指标 (Priority: P2)

**Goal**: 配置 Spring Boot Actuator 暴露 Prometheus 格式的监控指标端点,包含 JVM 指标、HTTP 请求指标、数据库连接池指标

**Independent Test**: 启动应用并访问 `/actuator/prometheus` 端点,检查返回 Prometheus 格式的指标数据

### 配置 Actuator 端点

- [X] T124 [US5] 验证 bootstrap/pom.xml 包含 spring-boot-starter-actuator 和 micrometer-registry-prometheus 依赖 (在 Phase 3 T070 已添加)
- [X] T125 [US5] 在 application.yml 中配置 Actuator 端点暴露 (management.endpoints.web.exposure.include=health,info,prometheus)
- [X] T126 [US5] 在 application.yml 中配置 Actuator 端点基础路径 (management.endpoints.web.base-path=/actuator)
- [X] T127 [US5] 在 application.yml 中配置指标标签 (management.metrics.tags.application=aiops-service)

### 运行时验证

- [X] T128 [US5] 编译项目 (运行 mvn clean package)
- [X] T129 [US5] 启动应用 (java -jar bootstrap/target/bootstrap-*.jar --spring.profiles.active=local)
- [X] T130 [US5] 验证 health 端点可访问 (curl http://localhost:8080/actuator/health,预期返回 {"status":"UP"})
- [X] T131 [US5] 验证 prometheus 端点可访问 (curl http://localhost:8080/actuator/prometheus,预期返回 Prometheus 文本格式指标)
- [X] T132 [US5] 验证 JVM 指标存在 (检查 prometheus 输出包含 jvm_memory_used, jvm_gc_pause, jvm_threads_live 等指标)
- [X] T133 [US5] 发送 HTTP 请求并验证请求指标 (curl http://localhost:8080/actuator/health 多次,然后检查 prometheus 输出包含 http_server_requests 指标)
- [X] T134 [US5] 验证指标包含 application 标签 (检查指标是否包含 application="aiops-service" 标签)

**Checkpoint**: User Story 5 完成 - Prometheus 监控指标已集成,暴露 /actuator/prometheus 端点,包含 JVM 和 HTTP 请求指标

---

## Phase 8: User Story 6 - 配置多环境支持 (Priority: P2)

**Goal**: 确保系统支持 local, dev, test, staging, prod 多种环境配置,不同环境使用不同的配置参数

**Independent Test**: 使用不同的 profile 启动应用,检查是否加载了对应环境的配置,日志输出目标、格式、级别符合对应环境要求

**Note**: 此 User Story 的大部分工作已在 Phase 5 (User Story 3) 中完成,此阶段主要进行验证和补充

### 补充环境特定配置

- [X] T135 [P] [US6] 在 application-dev.yml 中添加环境特定配置 (如数据库连接 URL 占位符、Redis 配置占位符)
- [X] T136 [P] [US6] 在 application-test.yml 中添加环境特定配置 (如测试数据库连接)
- [X] T137 [P] [US6] 在 application-staging.yml 中添加环境特定配置 (如预发布环境连接信息)
- [X] T138 [P] [US6] 在 application-prod.yml 中添加环境特定配置 (如生产环境连接信息,使用环境变量占位符)
- [X] T139 [US6] 在 application.yml 中添加通用配置说明注释 (说明哪些配置在所有环境共享,哪些需要在环境文件中覆盖)

### 运行时验证

- [X] T140 [US6] 验证 local 环境配置加载 (启动: --spring.profiles.active=local,检查日志显示 "The following 1 profile is active: \"local\"")
- [X] T141 [US6] 验证 dev 环境配置加载 (启动: --spring.profiles.active=dev,检查日志显示 active profile 为 dev,日志输出到文件)
- [X] T142 [US6] 验证 test 环境配置加载 (启动: --spring.profiles.active=test,检查日志显示 active profile 为 test)
- [X] T143 [US6] 验证 staging 环境配置加载 (启动: --spring.profiles.active=staging,检查日志级别为 INFO)
- [X] T144 [US6] 验证 prod 环境配置加载 (启动: --spring.profiles.active=prod,检查日志级别为 INFO,使用异步 Appender)
- [X] T145 [US6] 验证不同环境的日志输出差异 (local: 控制台彩色;dev/test: 文件 JSON + DEBUG;staging/prod: 文件 JSON + INFO)
- [X] T146 [US6] 测试不存在的 profile (启动: --spring.profiles.active=unknown,检查是否回退到默认配置或给出警告)
- [X] T147 [US6] 创建环境配置说明文档 (在 bootstrap/src/main/resources/ 目录创建 README.md,说明各环境配置差异和使用方法)

**Checkpoint**: User Story 6 完成 - 多环境支持已配置并验证,支持 local/dev/test/staging/prod 5 种环境,每种环境配置正确

---

## Phase 9: Polish & Cross-Cutting Concerns (最终优化)

**Purpose**: 完善文档、代码清理、最终验证

- [X] T148 [P] 创建项目 README.md 在根目录 (包含项目简介、技术栈、快速开始、模块说明、参考文档链接)
- [X] T149 [P] 创建 .gitignore 文件 (排除 target/, *.iml, .idea/, .DS_Store, logs/ 等)
- [X] T150 [P] 验证 quickstart.md 文档的所有步骤可执行 (按照 quickstart.md 从头到尾验证一遍)
- [X] T151 检查所有 pom.xml 文件的格式一致性 (检查缩进、标签顺序、命名规范)
- [X] T152 最终编译验证 (运行 mvn clean compile,确认所有模块编译成功,无警告)
- [X] T153 最终打包验证 (运行 mvn clean package,确认 bootstrap JAR 生成,大小合理)
- [X] T154 最终启动验证 (启动应用,检查所有用户故事的功能都可用)
- [X] T155 验证成功标准 (SC-001 到 SC-010 全部通过,参考 spec.md 的 Success Criteria)
- [X] T156 创建首次部署检查清单 (在 docs/ 目录创建 DEPLOYMENT_CHECKLIST.md,列出部署前的检查项)

**Checkpoint**: 项目完成 - 所有用户故事已实现并验证,文档完善,代码质量良好,可以交付使用

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 无依赖 - 可以立即开始
- **Foundational (Phase 2)**: 依赖 Phase 1 完成 - **阻塞所有用户故事**
- **User Story 1 (Phase 3)**: 依赖 Phase 2 完成 - 创建完整的模块结构
- **User Story 2 (Phase 4)**: 依赖 Phase 3 完成 - 验证依赖管理
- **User Story 3 (Phase 5)**: 依赖 Phase 3 完成 - 配置日志和追踪
- **User Story 4 (Phase 6)**: 依赖 Phase 3 和 Phase 5 完成 - 需要启动类和配置文件
- **User Story 5 (Phase 7)**: 依赖 Phase 3 和 Phase 5 完成 - 需要启动类和配置文件
- **User Story 6 (Phase 8)**: 依赖 Phase 5 完成 - 多环境配置已在 US3 中创建
- **Polish (Phase 9)**: 依赖所有用户故事完成

### User Story Dependencies

```
Phase 1 (Setup) → Phase 2 (Foundational) → Phase 3 (US1: 项目结构)
                                              ↓
                                      ┌───────┼───────┬───────┬───────┐
                                      ↓       ↓       ↓       ↓       ↓
                              Phase 4  Phase 5  Phase 6  Phase 7  Phase 8
                              (US2)    (US3)    (US4)    (US5)    (US6)
                               依赖管理  日志追踪  异常处理  监控指标  多环境
                                      ↓       ↓       ↓       ↓       ↓
                                      └───────┴───────┴───────┴───────┘
                                                  ↓
                                          Phase 9 (Polish)
```

**关键路径**:
1. Phase 1 → Phase 2 → Phase 3 (US1) 是必须顺序执行的
2. Phase 4 (US2) 依赖 Phase 3,但主要是验证工作
3. Phase 5 (US3) 依赖 Phase 3,需要启动类和模块结构
4. Phase 6 (US4) 依赖 Phase 3 和 Phase 5,需要启动应用验证
5. Phase 7 (US5) 依赖 Phase 3 和 Phase 5,需要启动应用验证
6. Phase 8 (US6) 依赖 Phase 5,主要是验证多环境配置

**并行机会**:
- Phase 4/5/6/7/8 可以部分并行 (如果有多个开发者)
- 但实际上由于都依赖 Phase 3 (US1) 的完整模块结构,建议按 P1 优先级顺序完成 US1-US3,再完成 P2 优先级的 US4-US6

### Within Each User Story

- **US1 (Phase 3)**: T028-T037 可并行 (创建 API 模块) → T038-T048 可并行 (创建实现模块) → T049-T054 (领域层) → T055-T060 (应用层) → T061-T066 (接口层) → T067-T076 (bootstrap)
- **US2 (Phase 4)**: 主要是验证任务,按顺序执行
- **US3 (Phase 5)**: T083-T088 可并行 (创建配置文件) → T089-T095 (配置 Logback) → T096-T097 (配置 Tracing) → T098-T106 (运行时验证)
- **US4 (Phase 6)**: T107-T109 可并行 (创建异常类) → T110-T111 (创建 Result) → T112-T116 (全局异常处理器) → T117-T118 (测试 Controller) → T119-T123 (运行时验证)
- **US5 (Phase 7)**: T124-T127 (配置) → T128-T134 (运行时验证)
- **US6 (Phase 8)**: T135-T139 可并行 (补充配置) → T140-T147 (运行时验证)
- **Polish (Phase 9)**: T148-T149 可并行 → T150-T156 按顺序执行

### Parallel Opportunities

#### Phase 2: Foundational 阶段并行机会

```bash
# 可以并行创建基础设施层的三个聚合模块:
Task T015: "在 infrastructure 下创建 cache/pom.xml"
Task T016: "在 infrastructure 下创建 mq/pom.xml"
```

#### Phase 3: User Story 1 并行机会

```bash
# 创建基础设施层 API 模块可以并行:
Task T028-T036: "创建 repository-api, cache-api, mq-api 模块"

# 创建基础设施层实现模块可以并行:
Task T038-T047: "创建 mysql-impl, redis-impl, sqs-impl 模块"

# 创建领域层模块可以并行:
Task T050-T053: "创建 domain-api 和 domain-impl 模块"

# 创建应用层模块可以并行:
Task T056-T059: "创建 application-api 和 application-impl 模块"

# 创建接口层模块可以并行:
Task T062-T065: "创建 interface-http 和 interface-consumer 模块"
```

#### Phase 5: User Story 3 并行机会

```bash
# 创建配置文件可以并行:
Task T083-T088: "创建所有环境的 application-*.yml 文件"
```

#### Phase 6: User Story 4 并行机会

```bash
# 创建异常类可以并行:
Task T107-T109: "创建 BaseException, BusinessException, SystemException"
```

#### Phase 8: User Story 6 并行机会

```bash
# 补充环境配置可以并行:
Task T135-T138: "在各环境配置文件中添加环境特定配置"
```

#### Phase 9: Polish 阶段并行机会

```bash
# 文档创建可以并行:
Task T148-T149: "创建 README.md 和 .gitignore"
```

---

## Parallel Example: User Story 1 (创建模块结构)

```bash
# 第一批并行任务:创建基础设施层 API 模块
Task T028-T030: "创建 repository-api 模块及其基本结构"
Task T031-T033: "创建 cache-api 模块及其基本结构"
Task T034-T036: "创建 mq-api 模块及其基本结构"

# 第二批并行任务:创建基础设施层实现模块
Task T038-T041: "创建 mysql-impl 模块及其基本结构"
Task T042-T044: "创建 redis-impl 模块及其基本结构"
Task T045-T047: "创建 sqs-impl 模块及其基本结构"

# 第三批并行任务:创建领域层模块
Task T050-T051: "创建 domain-api 模块及其基本结构"
Task T052-T053: "创建 domain-impl 模块及其基本结构"
```

---

## Implementation Strategy

### MVP First (User Stories 1-3 Only - 核心架构)

**推荐的 MVP 范围**:

1. **Phase 1**: Setup (T001-T006) - 创建父 POM
2. **Phase 2**: Foundational (T007-T027) - 创建基础模块骨架
3. **Phase 3**: User Story 1 (T028-T076) - 创建完整的模块结构
4. **Phase 5**: User Story 3 (T083-T106) - 集成日志和链路追踪
5. **STOP and VALIDATE**: 启动应用,检查日志输出包含 traceId/spanId
6. **可选**: Phase 4 (US2) - 验证依赖管理

**MVP 验收标准**:
- ✅ 项目可以编译成功 (mvn clean compile)
- ✅ 项目可以打包成功 (mvn clean package)
- ✅ 应用可以启动 (15秒内启动完成)
- ✅ 日志输出包含 traceId 和 spanId
- ✅ 所有模块依赖关系正确,无循环依赖

### Incremental Delivery (按优先级添加功能)

1. **MVP**: Phase 1 + 2 + 3 + 5 (US1 + US3) → **核心架构 + 可观测性**
2. **增量 1**: Phase 4 (US2) → **依赖管理验证**
3. **增量 2**: Phase 6 (US4) → **统一异常处理**
4. **增量 3**: Phase 7 (US5) → **监控指标**
5. **增量 4**: Phase 8 (US6) → **多环境配置完善**
6. **增量 5**: Phase 9 (Polish) → **文档完善和最终验证**

每个增量交付后都进行独立验证,确保新增功能不影响已有功能。

### Parallel Team Strategy

如果有多个开发者,可以在 Phase 3 (US1) 后并行开发:

**阶段 1: 基础架构 (必须顺序完成)**
- 所有人协作: Phase 1 + Phase 2 + Phase 3 (US1)
- **Checkpoint**: 完整的模块结构已创建

**阶段 2: 功能并行开发 (Phase 3 完成后)**
- 开发者 A: Phase 5 (US3 - 日志和追踪)
- 开发者 B: Phase 4 (US2 - 依赖管理验证) + Phase 6 (US4 - 异常处理)
- 开发者 C: Phase 7 (US5 - 监控指标) + Phase 8 (US6 - 多环境配置)

**阶段 3: 集成和优化**
- 所有人协作: Phase 9 (Polish) + 最终验证

**注意**: 由于本项目是架构初始化,大部分任务依赖项目基础结构,因此并行开发的机会相对有限。建议优先完成 Phase 1-3 (US1),再考虑并行开发其他用户故事。

---

## Notes

- **[P] 标记**: 表示任务可以并行执行 (不同文件,无依赖)
- **[Story] 标签**: 将任务映射到具体用户故事,便于跟踪和验证
- **渐进式模块声明**: 只声明已创建的模块,每个模块创建后立即编译验证
- **验证优先**: 每个阶段完成后都进行编译验证 (mvn clean compile) 或运行时验证
- **提交策略**: 建议在每个 Checkpoint 后提交代码,或者每完成 3-5 个任务提交一次
- **停止点**: 可以在任何 Checkpoint 处停止并验证,确保当前阶段功能完整
- **避免**: 模糊的任务描述、同一文件的并发修改、破坏用户故事独立性的跨故事依赖

---

## Task Summary

**Total Tasks**: 156

**Tasks per Phase**:
- Phase 1 (Setup): 6 tasks
- Phase 2 (Foundational): 21 tasks
- Phase 3 (US1): 49 tasks
- Phase 4 (US2): 6 tasks
- Phase 5 (US3): 24 tasks
- Phase 6 (US4): 17 tasks
- Phase 7 (US5): 11 tasks
- Phase 8 (US6): 13 tasks
- Phase 9 (Polish): 9 tasks

**Tasks per User Story**:
- US1 (创建基础 Maven 多模块项目结构): 49 tasks (P1)
- US2 (配置统一依赖管理和技术栈版本): 6 tasks (P1)
- US3 (集成分布式链路追踪和结构化日志): 24 tasks (P1)
- US4 (实现统一异常处理和错误响应): 17 tasks (P2)
- US5 (集成 Prometheus 监控指标): 11 tasks (P2)
- US6 (配置多环境支持): 13 tasks (P2)

**Parallel Opportunities Identified**:
- Phase 2: 2 tasks can run in parallel
- Phase 3 (US1): 30+ tasks can run in parallel (grouped in batches)
- Phase 5 (US3): 6 tasks can run in parallel
- Phase 6 (US4): 3 tasks can run in parallel
- Phase 8 (US6): 4 tasks can run in parallel
- Phase 9 (Polish): 2 tasks can run in parallel

**Independent Test Criteria**:
- **US1**: `mvn clean compile` 成功,所有模块按正确顺序编译,查看 Maven Reactor Build Order
- **US2**: 检查父 POM 的 `<dependencyManagement>`,子模块依赖无版本号,`mvn dependency:tree` 无版本冲突
- **US3**: 启动应用,发送 HTTP 请求,日志输出包含 traceId 和 spanId,多环境日志格式正确
- **US4**: 在 Controller 中抛出异常,检查返回的 HTTP 响应是否符合统一的 Result 格式
- **US5**: 访问 `/actuator/prometheus` 端点,检查返回 Prometheus 格式的指标数据
- **US6**: 使用不同 profile 启动应用,检查加载对应环境配置,日志输出符合环境要求

**Suggested MVP Scope**:
- Phase 1 (Setup) + Phase 2 (Foundational) + Phase 3 (US1) + Phase 5 (US3)
- **核心价值**: 完整的 DDD 多模块项目结构 + 分布式链路追踪和结构化日志
- **验收标准**: 项目可编译、打包、启动,日志输出包含 traceId/spanId
- **交付时间**: 约 1-2 天 (单人开发,熟悉技术栈的情况下)

---

## Format Validation ✅

**All tasks follow the checklist format**:
- ✅ Every task starts with `- [ ]` (markdown checkbox)
- ✅ Every task has a Task ID (T001-T156) in sequential order
- ✅ Parallelizable tasks are marked with `[P]`
- ✅ User story tasks are marked with story label `[US1]` to `[US6]`
- ✅ Setup and Foundational tasks have NO story label
- ✅ Polish tasks have NO story label
- ✅ Every task includes a clear description with exact file path

**Example validation**:
- `- [ ] T001 创建父 POM 在 pom.xml` ✅
- `- [ ] T028 [P] [US1] 在 repository/pom.xml 的 modules 中声明 repository-api 子模块` ✅
- `- [ ] T083 [P] [US3] 创建 application.yml 在 bootstrap/src/main/resources/` ✅
- `- [ ] T148 [P] 创建项目 README.md 在根目录` ✅
