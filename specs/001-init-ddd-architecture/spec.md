# Feature Specification: DDD 多模块项目架构初始化

**Feature Branch**: `001-init-ddd-architecture`
**Created**: 2025-11-21
**Status**: Draft
**Input**: User description: "doc/01-init-backend/1-project-architecture-design.md 中是原始需求,请按照原始需求来构建一个用于AIOps的后端应用的代码工程。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 创建基础 Maven 多模块项目结构 (Priority: P1)

作为开发者,我需要创建一个符合 DDD 分层架构的 Maven 多模块项目骨架,以便后续开发能够在清晰的架构基础上进行。

**Why this priority**: 这是整个项目的基础架构,所有后续开发都依赖于此。没有正确的项目结构,无法进行任何业务开发。

**Independent Test**: 可以通过执行 `mvn clean compile` 验证项目结构正确,所有模块按正确的依赖顺序编译成功,无循环依赖。

**Acceptance Scenarios**:

1. **Given** 空的代码仓库, **When** 创建项目结构, **Then** 生成父 POM 和 6 个顶层聚合模块(common, interface, application, domain, infrastructure, bootstrap)
2. **Given** 项目结构已创建, **When** 执行 `mvn clean compile`, **Then** 所有模块按正确顺序编译成功
3. **Given** 项目结构已创建, **When** 查看 Maven Reactor Build Order, **Then** 显示正确的模块构建顺序(被依赖的模块先构建)

---

### User Story 2 - 配置统一依赖管理和技术栈版本 (Priority: P1)

作为开发者,我需要在父 POM 中统一管理所有依赖版本,以确保项目使用一致的技术栈版本,避免依赖冲突。

**Why this priority**: 统一的依赖管理是多模块项目的基础,必须在第一步完成,否则后续添加依赖时会出现版本不一致问题。

**Independent Test**: 可以通过检查父 POM 的 `<dependencyManagement>` 配置,并在子模块中声明依赖时不指定版本号,验证版本统一管理生效。

**Acceptance Scenarios**:

1. **Given** 父 POM 已创建, **When** 配置 dependencyManagement, **Then** 包含 Spring Boot 3.4.1, Spring Cloud 2025.0.0, MyBatis-Plus 3.5.7 等所有技术栈版本
2. **Given** 子模块需要添加依赖, **When** 在子模块 pom.xml 中声明依赖(仅 groupId 和 artifactId), **Then** Maven 自动使用父 POM 中定义的版本
3. **Given** 项目完整构建, **When** 检查构建日志, **Then** 所有依赖版本与父 POM 定义一致,无版本冲突警告

---

### User Story 3 - 集成分布式链路追踪和结构化日志 (Priority: P1)

作为开发者和运维人员,我需要系统自动生成 Trace ID 并输出结构化 JSON 日志,以便在分布式环境中追踪请求链路和快速定位问题。

**Why this priority**: 可观测性是生产系统的基础要求,必须从项目初始就集成,否则后续排查问题会非常困难。

**Independent Test**: 可以通过启动应用并发送 HTTP 请求,检查日志输出是否包含 traceId, spanId 字段的 JSON 格式日志。

**Acceptance Scenarios**:

1. **Given** 应用已启动, **When** 发送 HTTP 请求, **Then** 日志输出包含 traceId 和 spanId 字段
2. **Given** 应用运行在 local 环境, **When** 查看日志, **Then** 输出到控制台,使用彩色格式
3. **Given** 应用运行在 dev/test/staging/prod 环境, **When** 查看日志, **Then** 输出到文件,使用 JSON 格式
4. **Given** 日志配置已完成, **When** 检查 logback-spring.xml, **Then** 项目包(com.catface996.aiops.*)使用 DEBUG/INFO 级别,框架包使用 WARN 级别

---

### User Story 4 - 实现统一异常处理和错误响应 (Priority: P2)

作为开发者,我需要在 common 模块定义统一的异常体系,并在接口层实现全局异常处理,以规范化错误传播和响应格式。

**Why this priority**: 统一的异常处理提高代码质量和用户体验,但不阻塞基础架构搭建,可以在架构就绪后实现。

**Independent Test**: 可以通过在 Controller 中抛出异常,检查返回的 HTTP 响应是否符合统一的 Result 格式。

**Acceptance Scenarios**:

1. **Given** 异常体系已定义, **When** Controller 抛出 BusinessException, **Then** 返回包含错误码和错误消息的标准 Result 对象
2. **Given** 异常体系已定义, **When** Repository 抛出数据库异常, **Then** 被转换为 SystemException 并在接口层捕获
3. **Given** 全局异常处理器已实现, **When** 未捕获的异常发生, **Then** 返回通用错误响应,不暴露内部实现细节

---

### User Story 5 - 集成 Prometheus 监控指标 (Priority: P2)

作为运维人员,我需要系统暴露 Prometheus 格式的监控指标端点,以便监控系统的运行状态和性能指标。

**Why this priority**: 监控是生产系统的重要能力,但不阻塞开发,可以在架构就绪后集成。

**Independent Test**: 可以通过启动应用并访问 `/actuator/prometheus` 端点,检查是否返回 Prometheus 格式的指标数据。

**Acceptance Scenarios**:

1. **Given** 应用已启动, **When** 访问 /actuator/prometheus, **Then** 返回 Prometheus 格式的指标数据
2. **Given** 应用运行中, **When** 查看 Prometheus 指标, **Then** 包含 JVM 指标(内存、GC、线程)
3. **Given** 应用处理请求, **When** 查看 Prometheus 指标, **Then** 包含 HTTP 请求指标(QPS、延迟、错误率)

---

### User Story 6 - 配置多环境支持 (Priority: P2)

作为开发者,我需要系统支持 local, dev, test, staging, prod 多种环境配置,以便在不同环境下使用不同的配置参数。

**Why this priority**: 多环境支持是企业级应用的标准需求,但可以在基础架构就绪后逐步完善。

**Independent Test**: 可以通过使用不同的 profile 启动应用,检查是否加载了对应环境的配置。

**Acceptance Scenarios**:

1. **Given** 环境配置文件已创建, **When** 使用 --spring.profiles.active=dev 启动, **Then** 加载 application-dev.yml 配置
2. **Given** 环境配置文件已创建, **When** 使用 --spring.profiles.active=prod 启动, **Then** 加载 application-prod.yml 配置
3. **Given** 不同环境配置差异, **When** 启动应用, **Then** 日志输出目标、格式、级别符合对应环境要求

---

### Edge Cases

- 当子模块的 POM 配置错误导致循环依赖时,Maven 构建应该失败并给出明确的错误提示
- 当 logback-spring.xml 配置错误时,应用启动应该失败并给出明确的错误信息
- 当 Prometheus 端点被禁用时,访问 /actuator/prometheus 应该返回 404
- 当使用不存在的 profile 启动应用时,应该回退到默认配置或给出警告

## Requirements *(mandatory)*

### Functional Requirements

#### 项目结构要求

- **FR-001**: THE System SHALL 创建一个父 POM 文件,其 groupId 为 "com.catface996.aiops", artifactId 为 "aiops-service", packaging 为 "pom"
- **FR-002**: THE System SHALL 在父 POM 中声明 6 个顶层模块: common, interface, application, domain, infrastructure, bootstrap
- **FR-003**: THE System SHALL 创建 interface 聚合模块(packaging=pom),包含 interface-http 和 interface-consumer 两个子模块(packaging=jar)
- **FR-004**: THE System SHALL 创建 application 聚合模块(packaging=pom),包含 application-api 和 application-impl 两个子模块(packaging=jar)
- **FR-005**: THE System SHALL 创建 domain 聚合模块(packaging=pom),包含 domain-api 和 domain-impl 两个子模块(packaging=jar)
- **FR-006**: THE System SHALL 创建 infrastructure 聚合模块(packaging=pom),包含 repository, cache, mq 三个子聚合模块
- **FR-007**: THE System SHALL 在 infrastructure/repository 下创建 repository-api 和 mysql-impl 两个子模块(packaging=jar)
- **FR-008**: THE System SHALL 在 infrastructure/cache 下创建 cache-api 和 redis-impl 两个子模块(packaging=jar)
- **FR-009**: THE System SHALL 在 infrastructure/mq 下创建 mq-api 和 sqs-impl 两个子模块(packaging=jar)
- **FR-010**: THE System SHALL 创建 common 模块(packaging=jar),用于存放通用工具类、枚举、异常定义
- **FR-011**: THE System SHALL 创建 bootstrap 模块(packaging=jar),作为应用启动入口

#### 依赖管理要求

- **FR-012**: THE System SHALL 在父 POM 的 dependencyManagement 中导入 spring-boot-dependencies BOM, version 3.4.1
- **FR-013**: THE System SHALL 在父 POM 的 dependencyManagement 中导入 spring-cloud-dependencies BOM, version 2025.0.0
- **FR-014**: THE System SHALL 在父 POM 的 dependencyManagement 中声明 mybatis-plus-spring-boot3-starter, version 3.5.7
- **FR-015**: THE System SHALL 在父 POM 的 dependencyManagement 中声明 druid-spring-boot-3-starter, version 1.2.20
- **FR-016**: THE System SHALL 在父 POM 的 dependencyManagement 中声明 micrometer-tracing, version 1.3.5
- **FR-017**: THE System SHALL 在父 POM 的 dependencyManagement 中声明 logstash-logback-encoder, version 7.4
- **FR-018**: THE System SHALL 在父 POM 的 dependencyManagement 中声明 aws-java-sdk-sqs, version 2.20.0
- **FR-019**: THE System SHALL 确保所有子模块在声明依赖时不指定 version 标签,版本由父 POM 统一管理
- **FR-020**: THE System SHALL 在父 POM 中配置 Java 编译器版本为 21
- **FR-021**: THE System SHALL 在父 POM 中配置项目编码为 UTF-8

#### 模块依赖关系要求

- **FR-022**: THE bootstrap 模块 SHALL 依赖 interface-http, interface-consumer, application-impl, domain-impl, mysql-impl, redis-impl, sqs-impl, common
- **FR-023**: THE interface-http 模块 SHALL 依赖 application-api, common
- **FR-024**: THE interface-consumer 模块 SHALL 依赖 application-api, common
- **FR-025**: THE application-impl 模块 SHALL 依赖 application-api, domain-api, common
- **FR-026**: THE domain-impl 模块 SHALL 依赖 domain-api, repository-api, cache-api, mq-api, common
- **FR-027**: THE mysql-impl 模块 SHALL 依赖 repository-api, common
- **FR-028**: THE redis-impl 模块 SHALL 依赖 cache-api, common
- **FR-029**: THE sqs-impl 模块 SHALL 依赖 mq-api, common
- **FR-030**: THE System SHALL 确保模块依赖关系遵循单向依赖原则,外层依赖内层,内层不依赖外层

#### 日志配置要求

- **FR-031**: THE System SHALL 在 bootstrap 模块的 resources 目录下创建 logback-spring.xml 配置文件
- **FR-032**: THE System SHALL 配置 Micrometer Tracing 自动生成 traceId 和 spanId
- **FR-033**: THE System SHALL 配置日志输出包含以下字段: timestamp, level, thread, logger, traceId, spanId, message
- **FR-034**: WHEN 应用运行在 local profile 时, THEN THE System SHALL 输出日志到控制台,使用 Spring Boot 默认彩色格式
- **FR-035**: WHEN 应用运行在 dev/test/staging/prod profile 时, THEN THE System SHALL 输出日志到文件,使用 JSON 格式
- **FR-036**: THE System SHALL 配置项目包(com.catface996.aiops.*)的日志级别: local/dev/test 环境为 DEBUG, staging/prod 环境为 INFO
- **FR-037**: THE System SHALL 配置框架包(org.springframework.*, com.baomidou.*, com.amazonaws.*)的日志级别为 WARN (所有环境)
- **FR-038**: THE System SHALL 配置日志文件按日期滚动,单个文件超过 100MB 时自动分割
- **FR-039**: THE System SHALL 配置非生产环境保留最近 30 天日志,生产环境保留最近 90 天日志
- **FR-040**: THE System SHALL 配置生产环境使用异步 Appender 提高日志性能
- **FR-041**: THE System SHALL 配置 ERROR 级别日志单独输出到 error.log 文件

#### 异常处理要求

- **FR-042**: THE System SHALL 在 common 模块定义异常基类 BaseException
- **FR-043**: THE System SHALL 在 common 模块定义 BusinessException 用于业务异常
- **FR-044**: THE System SHALL 在 common 模块定义 SystemException 用于系统异常
- **FR-045**: THE System SHALL 在 common 模块定义统一响应对象 Result,包含 code, message, data 字段
- **FR-046**: THE System SHALL 在 interface-http 模块实现 @RestControllerAdvice 全局异常处理器
- **FR-047**: THE System SHALL 在 interface-consumer 模块实现 @ControllerAdvice 全局异常处理器
- **FR-048**: WHEN BusinessException 被抛出时, THEN THE System SHALL 转换为包含错误码和错误消息的 Result 对象
- **FR-049**: WHEN SystemException 或未知异常被抛出时, THEN THE System SHALL 转换为通用错误响应,不暴露内部实现细节

#### 监控指标要求

- **FR-050**: THE System SHALL 在 bootstrap 模块添加 spring-boot-starter-actuator 依赖
- **FR-051**: THE System SHALL 在 bootstrap 模块添加 micrometer-registry-prometheus 依赖
- **FR-052**: THE System SHALL 配置 /actuator/prometheus 端点暴露 Prometheus 格式的指标
- **FR-053**: THE System SHALL 确保指标包含 JVM 指标(内存、GC、线程)
- **FR-054**: THE System SHALL 确保指标包含 HTTP 请求指标(QPS、延迟、错误率)

#### 多环境配置要求

- **FR-055**: THE System SHALL 在 bootstrap 模块的 resources 目录下创建 application.yml 通用配置文件
- **FR-056**: THE System SHALL 在 bootstrap 模块的 resources 目录下创建 application-local.yml, application-dev.yml, application-test.yml, application-staging.yml, application-prod.yml 环境配置文件
- **FR-057**: THE System SHALL 在 application.yml 中配置 spring.profiles.active 默认值为 dev
- **FR-058**: THE System SHALL 确保不同环境的配置差异体现在数据库连接、缓存配置、消息队列配置等方面
- **FR-059**: THE System SHALL 禁止在 application.yml 中配置日志相关配置(logging.level.*, logging.pattern.*, logging.file.*),所有日志配置在 logback-spring.xml 中管理

#### 模块命名规范要求

- **FR-060**: THE System SHALL 确保所有 Maven 模块的 <name> 标签使用首字母大写的英文单词,单词之间用空格分隔
- **FR-061**: THE System SHALL 为父 POM 配置 name 为 "AIOps Service"
- **FR-062**: THE System SHALL 为 common 模块配置 name 为 "Common"
- **FR-063**: THE System SHALL 为 bootstrap 模块配置 name 为 "Bootstrap"
- **FR-064**: THE System SHALL 为 interface 模块配置 name 为 "Interface"
- **FR-065**: THE System SHALL 为 application 模块配置 name 为 "Application"
- **FR-066**: THE System SHALL 为 domain 模块配置 name 为 "Domain"
- **FR-067**: THE System SHALL 为 infrastructure 模块配置 name 为 "Infrastructure"

#### 启动入口要求

- **FR-068**: THE System SHALL 在 bootstrap 模块创建 Spring Boot 主启动类
- **FR-069**: THE System SHALL 在 bootstrap 模块的 pom.xml 中配置 spring-boot-maven-plugin 用于打包可执行 JAR
- **FR-070**: THE System SHALL 确保 bootstrap 模块的 main 方法使用 @SpringBootApplication 注解
- **FR-071**: THE System SHALL 配置组件扫描路径为 "com.catface996.aiops"

### Key Entities

本需求主要涉及项目架构和配置,不涉及业务领域实体。涉及的技术概念包括:

- **Maven Module**: Maven 模块,可以是聚合模块(pom)或代码模块(jar)
- **POM (Project Object Model)**: Maven 项目对象模型,定义模块信息、依赖、构建配置
- **Spring Profile**: Spring 环境配置,用于区分不同运行环境
- **Trace Context**: 链路追踪上下文,包含 traceId 和 spanId
- **Exception Hierarchy**: 异常层次结构,包含 BaseException, BusinessException, SystemException
- **Result Object**: 统一响应对象,包含 code, message, data 字段

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 首次编译时,开发者可以在 2 分钟内执行 `mvn clean compile` 成功编译整个项目(包含依赖下载);后续编译可以在 30 秒内完成
- **SC-002**: 首次打包时,开发者可以在 3 分钟内执行 `mvn clean package` 成功打包 bootstrap 模块为可执行 JAR;后续打包可以在 1 分钟内完成
- **SC-003**: 开发者可以在 15 秒内启动应用(bootstrap JAR)到 Ready 状态
- **SC-004**: 运维人员可以通过访问 /actuator/prometheus 端点在 1 秒内获取监控指标
- **SC-005**: 链路追踪覆盖率达到 100%,所有 HTTP 请求的日志都包含 traceId 和 spanId
- **SC-006**: 项目结构文档化程度达到 100%,所有模块的职责和依赖关系在 README 或文档中有明确说明
- **SC-007**: 依赖版本一致性达到 100%,所有子模块使用父 POM 统一管理的版本,构建日志中无版本冲突警告
- **SC-008**: 多环境配置准确性达到 100%,使用不同 profile 启动应用时加载正确的配置文件且无配置缺失错误
- **SC-009**: 异常处理覆盖率达到 100%,所有接口层可能抛出的异常都被全局异常处理器捕获并转换为统一格式
- **SC-010**: 代码质量门禁通过率达到 100%,执行 `mvn clean compile` 时无编译错误,警告数量为 0
