# GitHub 最佳实践项目总结

> 基于项目技术栈搜索的 GitHub 开源项目最佳实践汇总
> 
> 生成时间: 2025-11-21
> 
> 技术栈: Spring Boot 3.4.1, Spring Cloud 2025.0.0, MyBatis-Plus 3.5.7, JDK 21, DDD 架构

---

## 📋 目录

1. [Spring Cloud 微服务架构](#1-spring-cloud-微服务架构)
2. [DDD 领域驱动设计](#2-ddd-领域驱动设计)
3. [Maven 多模块项目](#3-maven-多模块项目)
4. [MyBatis-Plus 集成](#4-mybatis-plus-集成)
5. [Spring Boot 综合示例](#5-spring-boot-综合示例)
6. [日志与监控](#6-日志与监控)
7. [数据库连接池](#7-数据库连接池)
8. [实施建议](#8-实施建议)

---

## 1. Spring Cloud 微服务架构

### 1.1 alibaba/spring-cloud-alibaba ⭐⭐⭐⭐⭐

**项目信息:**
- **GitHub**: https://github.com/alibaba/spring-cloud-alibaba
- **Stars**: 28,877
- **语言**: Java
- **最后更新**: 2025-11-21

**核心特性:**
- 服务发现与注册 (Nacos)
- 分布式配置管理
- 消息驱动能力 (RocketMQ)
- 分布式事务 (Seata)
- 熔断降级 (Sentinel)
- 完整的微服务生态

**适用场景:**
- ✅ 与 Spring Cloud 2025.0.0 兼容
- ✅ 提供完整的微服务治理方案
- ✅ 国内使用广泛，文档丰富
- ✅ 阿里巴巴生产环境验证

**可借鉴内容:**
1. 服务注册与发现的配置方式
2. 分布式配置中心的实现
3. 微服务间通信的最佳实践
4. 链路追踪的集成方式
5. 多环境配置管理

**注意事项:**
- 需要评估是否引入 Nacos、Sentinel 等组件
- 当前项目仅需链路追踪功能，可选择性参考

---

### 1.2 piomin/sample-spring-microservices-new ⭐⭐⭐⭐

**项目信息:**
- **GitHub**: https://github.com/piomin/sample-spring-microservices-new
- **Stars**: 1,323
- **语言**: Java
- **最后更新**: 2025-11-20

**核心特性:**
- 基于 Spring Boot 3 (master 分支)
- Spring Cloud Gateway 网关
- Eureka 服务发现
- Spring Cloud Sleuth/Micrometer OTEL 链路追踪
- Springdoc (OpenAPI 文档)
- 日志关联

**适用场景:**
- ✅ 技术栈与项目高度匹配 (Spring Boot 3)
- ✅ 包含 Micrometer 链路追踪实现
- ✅ 展示了微服务间的通信模式
- ✅ 提供完整的示例代码

**可借鉴内容:**
1. **Micrometer Tracing 配置** - 重点参考
2. Trace ID 和 Span ID 的生成与传播
3. 日志中集成 traceId 的方式
4. Spring Cloud Gateway 的配置
5. 服务间调用的链路追踪

**重点关注:**
- `application.yml` 中的 tracing 配置
- Logback 配置中如何输出 traceId
- 分布式追踪的最佳实践

---

## 2. DDD 领域驱动设计

### 2.1 ttulka/ddd-example-ecommerce ⭐⭐⭐⭐

**项目信息:**
- **GitHub**: https://github.com/ttulka/ddd-example-ecommerce
- **Stars**: 392
- **语言**: Java
- **最后更新**: 2025-11-19

**核心特性:**
- 完整的 DDD 分层架构
- 六边形架构 (Hexagonal Architecture)
- 事件驱动设计
- 高内聚低耦合
- 模块化单体架构 (Modular Monolith)
- 基于 Spring Boot + Spring Framework

**适用场景:**
- ✅ 展示了标准的 DDD 分层结构
- ✅ 电商场景与业务需求相似
- ✅ 模块划分清晰，边界明确
- ✅ 适合学习 DDD 的实践方式

**可借鉴内容:**
1. **模块划分方式** - 重点参考
   - Domain 层的设计
   - Application 层的职责
   - Infrastructure 层的实现
   - Interface 层的适配

2. **依赖关系管理**
   - 单向依赖原则
   - 依赖倒置的实现
   - 接口与实现的分离

3. **聚合根设计**
   - Entity 的设计
   - Value Object 的使用
   - Repository 接口定义

4. **事件驱动**
   - 领域事件的发布
   - 事件处理机制

**项目结构参考:**
```
├── domain/              # 领域层
│   ├── api/            # 领域接口
│   └── impl/           # 领域实现
├── application/         # 应用层
│   ├── api/            # 应用服务接口
│   └── impl/           # 应用服务实现
├── infrastructure/      # 基础设施层
│   ├── repository/     # 仓储实现
│   └── messaging/      # 消息实现
└── interface/          # 接口层
    ├── rest/           # REST API
    └── events/         # 事件消费
```

**注意事项:**
- 该项目采用事件驱动，需根据实际需求调整
- 模块粒度较细，可根据项目规模适当调整

---

## 3. Maven 多模块项目

### 3.1 Java-Techie-jt/spring-multi-module-application ⭐⭐⭐

**项目信息:**
- **GitHub**: https://github.com/Java-Techie-jt/spring-multi-module-application
- **Stars**: 103
- **语言**: Java
- **最后更新**: 2025-11-08

**核心特性:**
- Maven 多模块项目结构
- 父 POM 统一依赖管理
- 模块间依赖关系清晰
- 基于 Spring Boot

**适用场景:**
- ✅ 展示了 Maven 多模块的组织方式
- ✅ 父 POM 的 dependencyManagement 配置
- ✅ 模块间的依赖声明
- ✅ 构建顺序的管理

**可借鉴内容:**
1. **父 POM 配置**
   ```xml
   <dependencyManagement>
       <dependencies>
           <!-- Spring Boot BOM -->
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-dependencies</artifactId>
               <version>${spring-boot.version}</version>
               <type>pom</type>
               <scope>import</scope>
           </dependency>
       </dependencies>
   </dependencyManagement>
   ```

2. **模块聚合**
   ```xml
   <modules>
       <module>common</module>
       <module>domain</module>
       <module>application</module>
       <module>infrastructure</module>
       <module>bootstrap</module>
   </modules>
   ```

3. **子模块依赖声明**
   - 不指定版本号，从父 POM 继承
   - 只声明 groupId 和 artifactId

4. **打包配置**
   - 父 POM: `<packaging>pom</packaging>`
   - 代码模块: `<packaging>jar</packaging>`
   - 启动模块: Spring Boot Maven Plugin

**重点关注:**
- 模块间的依赖传递
- 版本号的统一管理
- 构建顺序的自动解析

---

## 4. MyBatis-Plus 集成

### 4.1 oddfar/campus-example ⭐⭐⭐⭐

**项目信息:**
- **GitHub**: https://github.com/oddfar/campus-example
- **Stars**: 270
- **语言**: Java
- **最后更新**: 2025-11-15

**核心特性:**
- Spring Boot + MyBatis-Plus
- 前后端分离
- 完整的 CRUD 示例
- 包含分页、条件查询等功能

**适用场景:**
- ✅ 展示了 MyBatis-Plus 的完整配置
- ✅ 包含实际业务场景的使用
- ✅ 代码结构清晰

**可借鉴内容:**
1. **MyBatis-Plus 配置类**
   - 分页插件配置
   - 乐观锁插件
   - 自动填充处理器

2. **Mapper 接口设计**
   - 继承 BaseMapper
   - 自定义方法

3. **实体类注解**
   - @TableName
   - @TableId (主键策略)
   - @TableField
   - @TableLogic (逻辑删除)
   - @Version (乐观锁)

4. **Service 层封装**
   - 继承 IService
   - 实现类继承 ServiceImpl

---

### 4.2 fengwenyi/MyBatis-Plus-Example ⭐⭐⭐

**项目信息:**
- **GitHub**: https://github.com/fengwenyi/MyBatis-Plus-Example
- **Stars**: 167
- **语言**: Java
- **最后更新**: 2025-09-15

**核心特性:**
- MyBatis-Plus 各种功能示例
- Lambda 表达式支持
- 代码生成器
- 条件构造器使用

**适用场景:**
- ✅ 学习 MyBatis-Plus 的各种特性
- ✅ Lambda 查询的使用方式
- ✅ 代码生成器的配置

**可借鉴内容:**
1. **Lambda 查询**
   ```java
   LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
   wrapper.eq(User::getName, "张三")
          .gt(User::getAge, 18);
   ```

2. **条件构造器**
   - QueryWrapper
   - UpdateWrapper
   - LambdaQueryWrapper
   - LambdaUpdateWrapper

3. **代码生成器配置**
   - 自动生成 Entity、Mapper、Service、Controller

4. **分页查询**
   ```java
   Page<User> page = new Page<>(1, 10);
   IPage<User> userPage = userMapper.selectPage(page, wrapper);
   ```

**注意事项:**
- 项目需求要求复杂查询使用 XML，不使用 Wrapper
- 可参考其配置方式，但查询方式需调整

---

## 5. Spring Boot 综合示例

### 5.1 xkcoding/spring-boot-demo (推测项目)

**说明:**
虽然在搜索结果中多次提及，但未获取到完整信息。该项目是一个非常全面的 Spring Boot 集成示例项目。

**推荐关注的内容:**
1. Spring Boot Actuator 集成
2. Logback 日志配置
3. 统一异常处理
4. AOP 日志记录
5. 多环境配置

**建议:**
可以直接在 GitHub 搜索 `xkcoding/spring-boot-demo` 查看完整内容。

---

## 6. 日志与监控

### 6.1 Logback + JSON 日志

**最佳实践要点:**

1. **依赖配置**
   ```xml
   <dependency>
       <groupId>net.logstash.logback</groupId>
       <artifactId>logstash-logback-encoder</artifactId>
       <version>7.4</version>
   </dependency>
   ```

2. **logback-spring.xml 配置**
   ```xml
   <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
       <encoder class="net.logstash.logback.encoder.LogstashEncoder">
           <includeMdcKeyName>traceId</includeMdcKeyName>
           <includeMdcKeyName>spanId</includeMdcKeyName>
       </encoder>
   </appender>
   ```

3. **多环境配置**
   - Local: 控制台输出，默认格式
   - Dev/Test/Staging/Prod: 文件输出，JSON 格式

---

### 6.2 Micrometer Tracing

**配置要点:**

1. **依赖配置**
   ```xml
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-tracing-bridge-brave</artifactId>
   </dependency>
   ```

2. **application.yml 配置**
   ```yaml
   management:
     tracing:
       sampling:
         probability: 1.0  # 采样率
   ```

3. **日志集成**
   - 自动在 MDC 中添加 traceId 和 spanId
   - Logback 配置中引用 MDC 变量

---

### 6.3 Prometheus 监控

**配置要点:**

1. **依赖配置**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-prometheus</artifactId>
   </dependency>
   ```

2. **Actuator 配置**
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,prometheus
     metrics:
       export:
         prometheus:
           enabled: true
   ```

3. **访问端点**
   - `/actuator/prometheus` - Prometheus 指标
   - `/actuator/health` - 健康检查

---

## 7. 数据库连接池

### 7.1 Druid 连接池

**官方项目:**
- **GitHub**: https://github.com/alibaba/druid
- **说明**: 阿里巴巴开源的数据库连接池

**配置要点:**

1. **依赖配置**
   ```xml
   <dependency>
       <groupId>com.alibaba</groupId>
       <artifactId>druid-spring-boot-starter</artifactId>
       <version>1.2.20</version>
   </dependency>
   ```

2. **application.yml 配置**
   ```yaml
   spring:
     datasource:
       type: com.alibaba.druid.pool.DruidDataSource
       druid:
         initial-size: 5
         min-idle: 5
         max-active: 20
         max-wait: 60000
         # 监控配置
         stat-view-servlet:
           enabled: true
           url-pattern: /druid/*
         # 过滤器配置
         filter:
           stat:
             enabled: true
             log-slow-sql: true
             slow-sql-millis: 1000
   ```

3. **监控页面**
   - 访问 `/druid/index.html` 查看监控信息
   - 包含 SQL 监控、连接池监控等

---

## 8. 实施建议

### 8.1 优先级排序

**高优先级 (立即参考):**
1. ✅ **piomin/sample-spring-microservices-new**
   - 原因: Micrometer Tracing 配置与项目需求完全匹配
   - 重点: 链路追踪的实现方式

2. ✅ **ttulka/ddd-example-ecommerce**
   - 原因: DDD 分层架构的标准实现
   - 重点: 模块划分和依赖关系

3. ✅ **Java-Techie-jt/spring-multi-module-application**
   - 原因: Maven 多模块项目的组织方式
   - 重点: POM 配置和模块聚合

**中优先级 (选择性参考):**
4. ⭐ **oddfar/campus-example**
   - 原因: MyBatis-Plus 的完整配置
   - 重点: 配置类和注解使用

5. ⭐ **fengwenyi/MyBatis-Plus-Example**
   - 原因: MyBatis-Plus 的各种特性
   - 重点: 功能示例和最佳实践

**低优先级 (了解即可):**
6. 📖 **alibaba/spring-cloud-alibaba**
   - 原因: 功能过于丰富，当前项目仅需部分功能
   - 建议: 了解其架构思想即可

---

### 8.2 学习路径建议

**第一阶段: 架构设计 (1-2天)**
1. 研究 DDD 项目的模块划分方式
2. 确定项目的模块结构和依赖关系
3. 设计 Maven 多模块的 POM 配置

**第二阶段: 基础设施 (2-3天)**
1. 配置 MyBatis-Plus
2. 集成 Druid 连接池
3. 配置多环境数据源

**第三阶段: 日志与监控 (1-2天)**
1. 配置 Logback + JSON 日志
2. 集成 Micrometer Tracing
3. 配置 Prometheus 监控

**第四阶段: 业务开发 (持续)**
1. 实现 NodeEntity 的 CRUD
2. 编写单元测试
3. 验证功能完整性

---

### 8.3 关键注意事项

**版本兼容性:**
- ✅ Spring Boot 3.4.1 需要 JDK 17+，项目使用 JDK 21 ✓
- ✅ MyBatis-Plus 必须使用 `mybatis-plus-spring-boot3-starter`
- ✅ MySQL 驱动使用 `com.mysql.cj.jdbc.Driver`

**配置原则:**
- ✅ 所有版本号在父 POM 的 `<properties>` 中定义
- ✅ 子模块不指定版本号，从父 POM 继承
- ✅ 日志配置在 `logback-spring.xml` 中，不在 `application.yml` 中

**架构原则:**
- ✅ 严格遵循 DDD 分层，单向依赖
- ✅ API 模块不依赖任何框架
- ✅ 实现模块包含所有框架特定代码
- ✅ Entity 是纯 POJO，PO 包含框架注解

**开发规范:**
- ✅ 简单操作使用 MyBatis-Plus API
- ✅ 复杂查询使用 Mapper XML
- ✅ 所有 SQL 必须包含 `deleted = 0` 条件
- ❌ 不使用 Wrapper 构造查询条件

---

### 8.4 快速参考清单

**配置文件清单:**
```
├── pom.xml                                    # 父 POM
├── bootstrap/src/main/resources/
│   ├── application.yml                        # 通用配置
│   ├── application-local.yml                  # 本地环境
│   ├── application-dev.yml                    # 开发环境
│   ├── application-test.yml                   # 测试环境
│   ├── application-staging.yml                # 预发布环境
│   ├── application-prod.yml                   # 生产环境
│   └── logback-spring.xml                     # 日志配置
└── infrastructure/repository/mysql-impl/
    └── src/main/resources/mapper/
        └── NodeMapper.xml                     # SQL 映射文件
```

**核心依赖清单:**
```xml
<!-- Spring Boot -->
<spring-boot.version>3.4.1</spring-boot.version>

<!-- Spring Cloud -->
<spring-cloud.version>2025.0.0</spring-cloud.version>

<!-- MyBatis-Plus (注意使用 Spring Boot 3 版本) -->
<mybatis-plus.version>3.5.7</mybatis-plus.version>

<!-- Druid -->
<druid.version>1.2.20</druid.version>

<!-- Micrometer Tracing -->
<micrometer-tracing.version>1.3.5</micrometer-tracing.version>

<!-- Logstash Logback Encoder -->
<logstash-logback-encoder.version>7.4</logstash-logback-encoder.version>
```

---

## 9. 总结

通过分析这些 GitHub 最佳实践项目，我们可以得出以下结论:

### 9.1 技术选型验证
✅ 项目的技术栈选型是合理的，都有成熟的开源项目作为参考
✅ Spring Boot 3 + Spring Cloud 2025 是最新的稳定版本组合
✅ MyBatis-Plus 3.5.7 完全支持 Spring Boot 3
✅ DDD 架构在 Java 生态中有成熟的实践

### 9.2 重点关注领域
1. **链路追踪**: Micrometer Tracing 的配置是关键
2. **模块划分**: DDD 分层架构需要严格遵循
3. **依赖管理**: Maven 多模块的版本管理很重要
4. **日志配置**: JSON 日志和多环境配置需要精心设计

### 9.3 潜在风险
⚠️ Spring Boot 3 与 Spring Boot 2 的依赖不兼容，需特别注意
⚠️ MyBatis-Plus 必须使用 Spring Boot 3 专用启动器
⚠️ DDD 架构的学习曲线较陡，需要时间理解

### 9.4 下一步行动
1. 克隆推荐的项目到本地，详细研究代码
2. 搭建项目的基础架构，先跑通 Hello World
3. 逐步集成各个技术组件，每次集成后验证
4. 实现第一个业务实体 NodeEntity，验证整体架构

---

## 附录: 项目链接汇总

| 项目名称 | GitHub 链接 | Stars | 重点关注 |
|---------|------------|-------|---------|
| spring-cloud-alibaba | https://github.com/alibaba/spring-cloud-alibaba | 28.8k | 微服务生态 |
| sample-spring-microservices-new | https://github.com/piomin/sample-spring-microservices-new | 1.3k | 链路追踪 |
| ddd-example-ecommerce | https://github.com/ttulka/ddd-example-ecommerce | 392 | DDD 架构 |
| spring-multi-module-application | https://github.com/Java-Techie-jt/spring-multi-module-application | 103 | Maven 多模块 |
| campus-example | https://github.com/oddfar/campus-example | 270 | MyBatis-Plus |
| MyBatis-Plus-Example | https://github.com/fengwenyi/MyBatis-Plus-Example | 167 | MyBatis-Plus |
| druid | https://github.com/alibaba/druid | - | 连接池 |

---

**文档版本**: v1.0  
**最后更新**: 2025-11-21  
**维护者**: AI Assistant  
**状态**: 待审查
