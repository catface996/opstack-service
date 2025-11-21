# 依赖版本管理文档

**项目**: AIOps Service
**日期**: 2025-11-21
**维护者**: Architecture Team

本文档记录 AIOps Service 项目的所有技术栈版本和依赖管理策略。

## 版本管理原则

1. **统一管理**: 所有依赖版本在父 POM 的 `<dependencyManagement>` 中声明
2. **BOM 优先**: Spring Boot 和 Spring Cloud 通过 BOM 导入，避免版本冲突
3. **不指定版本**: 子模块声明依赖时不指定 `<version>` 标签
4. **属性抽取**: 自定义库版本定义在 `<properties>` 中，便于统一升级

## 核心技术栈

### Java 版本

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 21 (LTS) | 项目开发和运行环境 |

### Spring 框架

| 组件 | 版本 | 管理方式 | 说明 |
|------|------|---------|------|
| Spring Boot | 3.4.1 | BOM (spring-boot-dependencies) | 核心框架 |
| Spring Cloud | 2025.0.0 | BOM (spring-cloud-dependencies) | 微服务支持 |
| Spring Framework | 6.2.1 | 通过 Spring Boot BOM | 由 Spring Boot 管理 |

### 持久化框架

| 组件 | 版本 | 管理方式 | 说明 |
|------|------|---------|------|
| MyBatis-Plus | 3.5.7 | dependencyManagement | ORM 框架 (Spring Boot 3 专用) |
| Druid | 1.2.20 | dependencyManagement | 数据库连接池 (Spring Boot 3 专用) |
| MySQL Connector | 9.1.0 | 由 Spring Boot BOM 管理 | MySQL 驱动 |
| MyBatis | 3.5.16 | 通过 MyBatis-Plus | 由 MyBatis-Plus 传递 |

### 可观测性

| 组件 | 版本 | 管理方式 | 说明 |
|------|------|---------|------|
| Micrometer Tracing | 1.3.5 | BOM (micrometer-tracing-bom) | 分布式链路追踪 BOM |
| Micrometer Tracing Bridge Brave | 1.4.1 | 由 Micrometer BOM 管理 | Brave 桥接器 |
| Micrometer Core | 1.14.2 | 由 Spring Boot BOM 管理 | 指标收集核心库 |
| Micrometer Registry Prometheus | 1.14.2 | 由 Spring Boot BOM 管理 | Prometheus 监控 |
| Logstash Logback Encoder | 7.4 | dependencyManagement | JSON 日志编码器 |
| Logback | 1.5.12 | 由 Spring Boot BOM 管理 | 日志框架 |

### 消息队列

| 组件 | 版本 | 管理方式 | 说明 |
|------|------|---------|------|
| AWS SDK for SQS | 2.20.0 | dependencyManagement | AWS SQS 客户端 |

### 缓存

| 组件 | 版本 | 管理方式 | 说明 |
|------|------|---------|------|
| Spring Boot Starter Data Redis | 3.4.1 | 由 Spring Boot BOM 管理 | Redis 客户端 |
| Lettuce Core | 6.4.1.RELEASE | 由 Spring Boot BOM 管理 | Redis 驱动 |

### 其他依赖

| 组件 | 版本 | 管理方式 | 说明 |
|------|------|---------|------|
| Lombok | 1.18.36 | 由 Spring Boot BOM 管理 | 代码简化工具 |
| Jakarta Validation API | 3.0.2 | 由 Spring Boot BOM 管理 | 参数校验 |
| Hibernate Validator | 8.0.2.Final | 由 Spring Boot BOM 管理 | 校验实现 |
| SLF4J API | 2.0.16 | 由 Spring Boot BOM 管理 | 日志接口 |
| Jackson | 2.18.2 | 由 Spring Boot BOM 管理 | JSON 处理 |

## 依赖管理验证

### 验证方法

1. **依赖树检查**:
   ```bash
   mvn dependency:tree
   ```

2. **版本冲突检查**:
   ```bash
   mvn clean compile -X | grep -i "version.*override"
   ```

3. **子模块 POM 检查**:
   - 确认所有子模块的外部依赖不指定 `<version>` 标签
   - 项目内部依赖使用 `${project.version}`

### 验证结果 (2025-11-21)

#### T077: 父 POM BOM 验证

**状态**: ✅ 通过

验证的 BOM:
- spring-boot-dependencies: 3.4.1 ✅
- spring-cloud-dependencies: 2025.0.0 ✅
- micrometer-tracing-bom: 1.3.5 ✅

**位置**: `/Users/catface/Documents/GitHub/AWS/aiops-service/pom.xml`

#### T078: 父 POM 第三方库版本验证

**状态**: ✅ 通过

验证的第三方库:
- mybatis-plus-spring-boot3-starter: 3.5.7 ✅
- druid-spring-boot-3-starter: 1.2.20 ✅
- logstash-logback-encoder: 7.4 ✅
- aws-java-sdk-sqs (实际为 software.amazon.awssdk:sqs): 2.20.0 ✅

#### T079: 子模块依赖配置验证

**状态**: ✅ 通过

检查了 14 个代码模块:
1. common - ✅ 无外部依赖版本声明
2. bootstrap - ✅ 所有外部依赖未指定版本
3. interface-http - ✅ 所有外部依赖未指定版本
4. interface-consumer - ✅ 所有外部依赖未指定版本
5. application-api - ✅ 仅依赖内部模块
6. application-impl - ✅ 所有外部依赖未指定版本
7. domain-api - ✅ 仅依赖内部模块
8. domain-impl - ✅ 所有外部依赖未指定版本
9. repository-api - ✅ 仅依赖内部模块
10. mysql-impl - ✅ 所有外部依赖未指定版本
11. cache-api - ✅ 仅依赖内部模块
12. redis-impl - ✅ 所有外部依赖未指定版本
13. mq-api - ✅ 仅依赖内部模块
14. sqs-impl - ✅ 所有外部依赖未指定版本

**结论**: 所有子模块正确继承父 POM 的依赖版本管理，无版本冲突风险。

#### T080: 依赖树分析

**状态**: ✅ 通过

**执行命令**: `mvn dependency:tree`

**关键发现**:
- 所有 Spring Boot 依赖版本统一为 3.4.1 ✅
- Spring Framework 版本统一为 6.2.1 ✅
- MyBatis-Plus 版本为 3.5.7 ✅
- Druid 版本为 1.2.20 ✅
- AWS SQS SDK 版本为 2.20.0 ✅
- Micrometer 相关版本一致 ✅
- 无版本冲突警告 ✅

**详细结果**: 参见 `dependency-tree.txt`

#### T081: 详细编译日志检查

**状态**: ✅ 通过

**执行命令**: `mvn clean compile -X`

**结果**:
- 编译成功 ✅
- 无版本覆盖警告 ✅
- 无版本冲突消息 ✅
- 所有 22 个模块编译通过 ✅

**构建统计**:
- 总耗时: 2.365 秒
- 所有模块状态: SUCCESS

**注意事项**:
- 编译器警告: 建议使用 `--release 21` 替代 `-source 21 -target 21`（这是编译器建议，不影响依赖管理）

**详细结果**: 参见 `compile-verbose.txt`

## 依赖升级策略

### Spring Boot 升级

1. 修改父 POM 的 `<spring-boot.version>` 属性
2. 检查 [Spring Boot Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
3. 运行 `mvn clean compile` 验证兼容性
4. 运行 `mvn dependency:tree` 检查依赖变化
5. 运行测试套件确认功能正常

### Spring Cloud 升级

1. 修改父 POM 的 `<spring-cloud.version>` 属性
2. 检查 [Spring Cloud Release Train](https://spring.io/projects/spring-cloud#learn)
3. 确保 Spring Cloud 版本与 Spring Boot 版本兼容
4. 运行完整测试套件

### 第三方库升级

1. 修改父 POM 的对应版本属性
2. 运行 `mvn clean compile` 验证兼容性
3. 检查 CHANGELOG 确认 breaking changes
4. 运行测试套件确认功能正常

### 升级原则

- **稳定性优先**: 只升级 LTS 版本或稳定版本
- **兼容性验证**: 升级前检查版本兼容性
- **渐进式升级**: 一次只升级一个主要依赖
- **充分测试**: 升级后运行完整测试套件

### 版本兼容性参考

**Spring Boot 3.4.x 要求**:
- Java 21+ (推荐) 或 Java 17+
- Spring Framework 6.2.x
- Jakarta EE 10

**MyBatis-Plus 3.5.7 要求**:
- Spring Boot 3.0+
- MyBatis 3.5.x

**Druid 1.2.20 要求**:
- Spring Boot 3.0+

## 常见问题

### Q1: 如何添加新的依赖？

**步骤**:
1. 在父 POM 的 `<dependencyManagement>` 中声明版本
2. 在子模块中声明依赖（不指定版本）
3. 运行 `mvn clean compile` 验证

**示例**:
```xml
<!-- 父 POM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>new-library</artifactId>
            <version>1.2.3</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 子模块 -->
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>new-library</artifactId>
        <!-- 不指定 <version> -->
    </dependency>
</dependencies>
```

### Q2: 子模块中出现版本冲突怎么办？

**排查步骤**:
1. 运行 `mvn dependency:tree -Dverbose` 查看依赖树
2. 找出冲突的依赖
3. 在父 POM 的 `<dependencyManagement>` 中显式声明版本
4. 必要时使用 `<exclusions>` 排除传递依赖

**示例**:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>library-a</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.example</groupId>
            <artifactId>conflicting-lib</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### Q3: 如何确认依赖版本生效？

**验证方法**:
```bash
# 查看有效 POM
mvn help:effective-pom

# 查看依赖树
mvn dependency:tree

# 查看特定模块的依赖
cd <module-path>
mvn dependency:tree
```

### Q4: 为什么要使用 dependencyManagement？

**优点**:
- 集中管理所有依赖版本，避免版本不一致
- 子模块无需关心版本号，降低维护成本
- 通过 BOM 导入，可以一次性引入一组兼容的依赖
- 易于统一升级和回滚

### Q5: BOM 和 dependencyManagement 有什么区别？

**BOM (Bill of Materials)**:
- 一种特殊的 POM 文件，只包含 `<dependencyManagement>`
- 通过 `<scope>import</scope>` 导入
- 用于批量管理一组相关依赖的版本

**dependencyManagement**:
- Maven 的依赖版本管理机制
- 父 POM 中声明，子模块继承
- 不会实际引入依赖，只是声明版本

## 项目模块结构

### 代码模块 (14个)

| 模块 | 路径 | 说明 | 外部依赖数 |
|------|------|------|-----------|
| common | /common | 通用工具类 | 3 |
| bootstrap | /bootstrap | 启动引导模块 | 10 |
| interface-http | /interface/interface-http | HTTP 接口层 | 5 |
| interface-consumer | /interface/interface-consumer | 消息消费者层 | 2 |
| application-api | /application/application-api | 应用服务接口 | 0 |
| application-impl | /application/application-impl | 应用服务实现 | 2 |
| domain-api | /domain/domain-api | 领域模型接口 | 0 |
| domain-impl | /domain/domain-impl | 领域服务实现 | 2 |
| repository-api | /infrastructure/repository/repository-api | 仓储接口 | 0 |
| mysql-impl | /infrastructure/repository/mysql-impl | MySQL 实现 | 6 |
| cache-api | /infrastructure/cache/cache-api | 缓存接口 | 0 |
| redis-impl | /infrastructure/cache/redis-impl | Redis 实现 | 3 |
| mq-api | /infrastructure/mq/mq-api | 消息队列接口 | 0 |
| sqs-impl | /infrastructure/mq/sqs-impl | SQS 实现 | 3 |

### 聚合模块 (7个)

| 模块 | 路径 | 说明 |
|------|------|------|
| aiops-service | / | 根聚合模块 |
| infrastructure | /infrastructure | 基础设施聚合模块 |
| repository | /infrastructure/repository | 仓储聚合模块 |
| cache | /infrastructure/cache | 缓存聚合模块 |
| mq | /infrastructure/mq | 消息队列聚合模块 |
| domain | /domain | 领域聚合模块 |
| application | /application | 应用聚合模块 |
| interface | /interface | 接口聚合模块 |

## 参考文档

### 官方文档

- [Spring Boot 依赖管理](https://docs.spring.io/spring-boot/docs/3.4.1/reference/html/using.html#using.build-systems.dependency-management)
- [Spring Cloud 文档](https://spring.io/projects/spring-cloud)
- [Maven 依赖管理](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Druid 官方文档](https://github.com/alibaba/druid)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)

### 技术博客

- [Spring Boot 3.x 迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [MyBatis-Plus Spring Boot 3 适配](https://baomidou.com/pages/56bac0/)

### 版本兼容性

- [Spring Boot 版本兼容性](https://github.com/spring-projects/spring-boot/wiki/Supported-Versions)
- [Spring Cloud 版本对应关系](https://spring.io/projects/spring-cloud#learn)

---

**文档版本**: 1.0.0
**最后更新**: 2025-11-21
**下次审核**: 2025-12-21
**负责人**: Architecture Team
