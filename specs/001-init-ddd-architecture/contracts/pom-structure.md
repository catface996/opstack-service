# POM 配置规范和模块依赖关系

**Feature**: 001-init-ddd-architecture | **Date**: 2025-11-21 | **Version**: 1.0.0

本文档详细定义了 AIOps Service 项目的 POM 配置规范和模块依赖关系。所有模块创建和依赖配置必须严格遵循本文档。

## 目录

- [1. 父 POM 配置规范](#1-父-pom-配置规范)
- [2. 聚合模块配置规范](#2-聚合模块配置规范)
- [3. 代码模块配置规范](#3-代码模块配置规范)
- [4. 模块依赖关系矩阵](#4-模块依赖关系矩阵)
- [5. 依赖版本管理规范](#5-依赖版本管理规范)
- [6. 模块创建检查清单](#6-模块创建检查清单)

## 1. 父 POM 配置规范

### 1.1 基本信息

**文件路径**: `aiops-service/pom.xml`

**必需配置**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 项目坐标 -->
    <groupId>com.catface996.aiops</groupId>
    <artifactId>aiops-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>AIOps Service</name>
    <description>AIOps Service - DDD Multi-Module Project</description>

    <!-- 顶层聚合模块 -->
    <modules>
        <module>common</module>
        <module>infrastructure</module>
        <module>domain</module>
        <module>application</module>
        <module>interface</module>
        <module>bootstrap</module>
    </modules>

    <!-- 属性配置 -->
    <properties>
        <!-- Java 版本 -->
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>

        <!-- 项目编码 -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- 核心框架版本 -->
        <spring-boot.version>3.4.1</spring-boot.version>
        <spring-cloud.version>2025.0.0</spring-cloud.version>

        <!-- 持久化框架版本 -->
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <druid.version>1.2.20</druid.version>

        <!-- 监控和日志版本 -->
        <micrometer-tracing.version>1.3.5</micrometer-tracing.version>
        <logstash-logback-encoder.version>7.4</logstash-logback-encoder.version>

        <!-- 消息队列版本 -->
        <aws-sdk.version>2.20.0</aws-sdk.version>
    </properties>

    <!-- 依赖管理 -->
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

            <!-- Spring Cloud BOM -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- MyBatis-Plus (Spring Boot 3 专用) -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <!-- Druid (Spring Boot 3 专用) -->
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid-spring-boot-3-starter</artifactId>
                <version>${druid.version}</version>
            </dependency>

            <!-- Micrometer Tracing -->
            <dependency>
                <groupId>io.micrometer</groupId>
                <artifactId>micrometer-tracing-bom</artifactId>
                <version>${micrometer-tracing.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Logstash Logback Encoder (JSON 日志) -->
            <dependency>
                <groupId>net.logstash.logback</groupId>
                <artifactId>logstash-logback-encoder</artifactId>
                <version>${logstash-logback-encoder.version}</version>
            </dependency>

            <!-- AWS SDK for SQS -->
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>sqs</artifactId>
                <version>${aws-sdk.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 构建配置 -->
    <build>
        <pluginManagement>
            <plugins>
                <!-- Spring Boot Maven Plugin -->
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>

                <!-- Maven Compiler Plugin -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                    <configuration>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                        <encoding>${project.build.sourceEncoding}</encoding>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### 1.2 配置要点

| 配置项 | 要求 | 说明 |
|-------|------|------|
| `<packaging>` | 必须为 `pom` | 父 POM 是聚合项目,不生成 JAR |
| `<modules>` | 只声明已创建的模块 | 渐进式模块声明原则 |
| `<java.version>` | 必须为 `21` | 项目使用 Java 21 LTS |
| `<dependencyManagement>` | 导入 BOM + 声明版本 | 统一管理所有依赖版本 |
| `<build.pluginManagement>` | 定义插件版本 | 子模块继承插件配置 |

## 2. 聚合模块配置规范

聚合模块 (Aggregation Module) 用于组织子模块,本身不包含代码。

### 2.1 聚合模块列表

| 模块路径 | artifactId | name | 职责 |
|---------|-----------|------|------|
| `interface/` | interface | Interface | 接口层聚合模块 |
| `application/` | application | Application | 应用层聚合模块 |
| `domain/` | domain | Domain | 领域层聚合模块 |
| `infrastructure/` | infrastructure | Infrastructure | 基础设施层聚合模块 |
| `infrastructure/repository/` | repository | Repository | 仓储层聚合模块 |
| `infrastructure/cache/` | cache | Cache | 缓存层聚合模块 |
| `infrastructure/mq/` | mq | MQ | 消息队列层聚合模块 |

### 2.2 聚合模块 POM 模板

**示例**: `interface/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父 POM 坐标 -->
    <parent>
        <groupId>com.catface996.aiops</groupId>
        <artifactId>aiops-service</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 模块坐标 -->
    <artifactId>interface</artifactId>
    <packaging>pom</packaging>
    <name>Interface</name>
    <description>Interface Layer - Aggregation Module</description>

    <!-- 子模块 -->
    <modules>
        <module>interface-http</module>
        <module>interface-consumer</module>
    </modules>
</project>
```

### 2.3 聚合模块配置要点

| 配置项 | 要求 | 说明 |
|-------|------|------|
| `<parent>` | 必须指向父 POM | 继承依赖管理配置 |
| `<packaging>` | 必须为 `pom` | 聚合模块不生成 JAR |
| `<modules>` | 只声明已创建的子模块 | 渐进式模块声明 |
| `<name>` | 首字母大写英文单词 + 空格 | 如 "Domain API", "MySQL Implementation" |
| `<dependencies>` | 聚合模块不声明依赖 | 依赖由子模块自行声明 |

## 3. 代码模块配置规范

代码模块 (Code Module) 包含实际的 Java 代码,生成 JAR 文件。

### 3.1 代码模块列表

| 模块路径 | artifactId | name | packaging | 职责 |
|---------|-----------|------|-----------|------|
| `common/` | common | Common | jar | 通用工具类、异常、Result |
| `bootstrap/` | bootstrap | Bootstrap | jar | 应用启动入口 |
| `interface/interface-http/` | interface-http | Interface HTTP | jar | HTTP REST 接口 |
| `interface/interface-consumer/` | interface-consumer | Interface Consumer | jar | 消息队列消费者 |
| `application/application-api/` | application-api | Application API | jar | 应用服务接口 |
| `application/application-impl/` | application-impl | Application Implementation | jar | 应用服务实现 |
| `domain/domain-api/` | domain-api | Domain API | jar | 领域模型定义 |
| `domain/domain-impl/` | domain-impl | Domain Implementation | jar | 领域服务实现 |
| `infrastructure/repository/repository-api/` | repository-api | Repository API | jar | 仓储接口 + Entity |
| `infrastructure/repository/mysql-impl/` | mysql-impl | MySQL Implementation | jar | MySQL 实现 + PO |
| `infrastructure/cache/cache-api/` | cache-api | Cache API | jar | 缓存接口 |
| `infrastructure/cache/redis-impl/` | redis-impl | Redis Implementation | jar | Redis 实现 |
| `infrastructure/mq/mq-api/` | mq-api | MQ API | jar | 消息队列接口 |
| `infrastructure/mq/sqs-impl/` | sqs-impl | SQS Implementation | jar | AWS SQS 实现 |

### 3.2 代码模块 POM 模板

#### 3.2.1 Common 模块

**文件路径**: `common/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父 POM -->
    <parent>
        <groupId>com.catface996.aiops</groupId>
        <artifactId>aiops-service</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 模块坐标 -->
    <artifactId>common</artifactId>
    <packaging>jar</packaging>
    <name>Common</name>
    <description>Common utilities, exceptions, and base classes</description>

    <!-- 依赖 -->
    <dependencies>
        <!-- SLF4J API (日志接口) -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>

        <!-- Lombok (简化代码) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- Jakarta Validation API (参数校验) -->
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

#### 3.2.2 Bootstrap 模块

**文件路径**: `bootstrap/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父 POM -->
    <parent>
        <groupId>com.catface996.aiops</groupId>
        <artifactId>aiops-service</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 模块坐标 -->
    <artifactId>bootstrap</artifactId>
    <packaging>jar</packaging>
    <name>Bootstrap</name>
    <description>Application bootstrap and main entry point</description>

    <!-- 依赖 -->
    <dependencies>
        <!-- 通用模块 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 接口层实现 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>interface-http</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>interface-consumer</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 应用层实现 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>application-impl</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 领域层实现 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>domain-impl</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 基础设施层实现 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>mysql-impl</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>redis-impl</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>sqs-impl</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Starter Actuator (监控) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Micrometer Tracing (链路追踪) -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>

        <!-- Micrometer Registry Prometheus (监控指标) -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Logstash Logback Encoder (JSON 日志) -->
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
        </dependency>

        <!-- Spring Boot Starter Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!-- 构建配置 -->
    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin (打包可执行 JAR) -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 3.2.3 Interface HTTP 模块

**文件路径**: `interface/interface-http/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父 POM (聚合模块) -->
    <parent>
        <groupId>com.catface996.aiops</groupId>
        <artifactId>interface</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 模块坐标 -->
    <artifactId>interface-http</artifactId>
    <packaging>jar</packaging>
    <name>Interface HTTP</name>
    <description>HTTP REST API controllers and DTOs</description>

    <!-- 依赖 -->
    <dependencies>
        <!-- 应用层 API -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>application-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 通用模块 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Spring Web (Controller 支持) -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
        </dependency>

        <!-- Spring WebMVC (REST 支持) -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
        </dependency>

        <!-- Jakarta Validation (参数校验) -->
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

#### 3.2.4 Application Implementation 模块

**文件路径**: `application/application-impl/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父 POM (聚合模块) -->
    <parent>
        <groupId>com.catface996.aiops</groupId>
        <artifactId>application</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 模块坐标 -->
    <artifactId>application-impl</artifactId>
    <packaging>jar</packaging>
    <name>Application Implementation</name>
    <description>Application service implementations</description>

    <!-- 依赖 -->
    <dependencies>
        <!-- 应用层 API -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>application-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 领域层 API -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>domain-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 通用模块 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Spring Context (Service 支持) -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

#### 3.2.5 MySQL Implementation 模块

**文件路径**: `infrastructure/repository/mysql-impl/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父 POM (聚合模块) -->
    <parent>
        <groupId>com.catface996.aiops</groupId>
        <artifactId>repository</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 模块坐标 -->
    <artifactId>mysql-impl</artifactId>
    <packaging>jar</packaging>
    <name>MySQL Implementation</name>
    <description>MySQL repository implementations with MyBatis-Plus</description>

    <!-- 依赖 -->
    <dependencies>
        <!-- 仓储层 API -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>repository-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 通用模块 -->
        <dependency>
            <groupId>com.catface996.aiops</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- MyBatis-Plus (Spring Boot 3 专用) -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>

        <!-- Druid (数据库连接池) -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Spring Context -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

### 3.3 代码模块配置要点

| 配置项 | 要求 | 说明 |
|-------|------|------|
| `<parent>` | 指向聚合模块或父 POM | common/bootstrap 指向父 POM,其他指向聚合模块 |
| `<packaging>` | 必须为 `jar` | 代码模块生成 JAR 文件 |
| `<dependencies>` | 不指定版本号 | 版本由父 POM 统一管理 |
| `<artifactId>` | 小写 + 连字符 | 如 "application-impl", "mysql-impl" |
| `<name>` | 首字母大写 + 空格 | 如 "Application Implementation" |

## 4. 模块依赖关系矩阵

### 4.1 依赖关系总览

```
┌─────────────┐
│  bootstrap  │ ← 最终组装,依赖所有 *-impl
└──────┬──────┘
       │
       ├─────────────────────────────────────────┐
       │                                         │
       ▼                                         ▼
┌──────────────┐                         ┌──────────────┐
│interface-http│                         │ interface-   │
│              │                         │   consumer   │
└──────┬───────┘                         └──────┬───────┘
       │                                        │
       └────────────────┬───────────────────────┘
                        │
                        ▼
                ┌──────────────┐
                │application-  │
                │     api      │
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │application-  │
                │     impl     │
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │  domain-api  │
                └──────┬───────┘
                       │
           ┌───────────┼───────────┐
           │           │           │
           ▼           ▼           ▼
    ┌──────────┐ ┌─────────┐ ┌────────┐
    │repository│ │ cache-  │ │mq-api  │
    │   -api   │ │  api    │ │        │
    └────┬─────┘ └────┬────┘ └───┬────┘
         │            │           │
         ▼            ▼           ▼
    ┌──────────┐ ┌─────────┐ ┌────────┐
    │ mysql-   │ │ redis-  │ │sqs-impl│
    │  impl    │ │  impl   │ │        │
    └──────────┘ └─────────┘ └────────┘

注: common 模块被所有模块依赖,为简化图示未画出
```

### 4.2 依赖关系矩阵表

| 模块 | 依赖的模块 |
|------|-----------|
| **bootstrap** | interface-http, interface-consumer, application-impl, domain-impl, mysql-impl, redis-impl, sqs-impl, common |
| **interface-http** | application-api, common |
| **interface-consumer** | application-api, common |
| **application-api** | common |
| **application-impl** | application-api, domain-api, common |
| **domain-api** | common |
| **domain-impl** | domain-api, repository-api, cache-api, mq-api, common |
| **repository-api** | common |
| **mysql-impl** | repository-api, common |
| **cache-api** | common |
| **redis-impl** | cache-api, common |
| **mq-api** | common |
| **sqs-impl** | mq-api, common |
| **common** | (无依赖) |

### 4.3 依赖方向规则

**✅ 允许的依赖方向**:
- 外层 → 内层: 接口层 → 应用层 → 领域层
- 实现 → API: application-impl → application-api
- 领域层 → 基础设施 API: domain-impl → repository-api, cache-api, mq-api
- 基础设施实现 → 基础设施 API: mysql-impl → repository-api

**❌ 禁止的依赖方向**:
- 内层 → 外层: 领域层 ❌ 应用层,应用层 ❌ 接口层
- API → 实现: application-api ❌ application-impl
- 领域层 → 基础设施实现: domain-impl ❌ mysql-impl, redis-impl, sqs-impl
- 平行层互相依赖: interface-http ❌ interface-consumer

## 5. 依赖版本管理规范

### 5.1 版本管理原则

1. **统一管理**: 所有依赖版本在父 POM 的 `<dependencyManagement>` 中声明
2. **不指定版本**: 子模块声明依赖时不指定 `<version>` 标签
3. **BOM 优先**: Spring Boot 和 Spring Cloud 通过 BOM 导入,避免版本冲突
4. **属性抽取**: 自定义库版本定义在 `<properties>` 中,便于统一升级

### 5.2 版本管理表

| 依赖 | 版本 | 管理方式 |
|------|------|---------|
| **JDK** | 21 | `<java.version>` 属性 |
| **Spring Boot** | 3.4.1 | BOM 导入 (`spring-boot-dependencies`) |
| **Spring Cloud** | 2025.0.0 | BOM 导入 (`spring-cloud-dependencies`) |
| **MyBatis-Plus** | 3.5.7 | `<dependencyManagement>` 显式声明 |
| **Druid** | 1.2.20 | `<dependencyManagement>` 显式声明 |
| **Micrometer Tracing** | 1.3.5 | BOM 导入 (`micrometer-tracing-bom`) |
| **Logstash Logback Encoder** | 7.4 | `<dependencyManagement>` 显式声明 |
| **AWS SDK** | 2.20.0 | `<dependencyManagement>` 显式声明 |

### 5.3 子模块依赖声明示例

**正确示例** (不指定版本):
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <!-- 不指定 <version>,由父 POM 管理 -->
</dependency>
```

**错误示例** (指定版本):
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version> <!-- ❌ 子模块不应该指定版本 -->
</dependency>
```

## 6. 模块创建检查清单

当创建新模块或修改现有模块时,使用此检查清单确保配置正确。

### 6.1 聚合模块检查清单

- [ ] `<packaging>` 配置为 `pom`
- [ ] `<parent>` 正确指向父 POM 或上层聚合模块
- [ ] `<modules>` 只声明已创建的子模块
- [ ] `<name>` 使用首字母大写英文单词 + 空格
- [ ] 不包含 `<dependencies>` 配置
- [ ] 不包含 `<build>` 配置
- [ ] 父 POM/聚合模块已声明此模块
- [ ] 运行 `mvn clean compile` 验证编译成功

### 6.2 代码模块检查清单

- [ ] `<packaging>` 配置为 `jar`
- [ ] `<parent>` 正确指向父 POM 或聚合模块
- [ ] `<dependencies>` 中所有依赖不指定版本号
- [ ] `<name>` 使用首字母大写英文单词 + 空格
- [ ] `<artifactId>` 使用小写 + 连字符
- [ ] 依赖关系符合 DDD 分层原则 (外层 → 内层)
- [ ] 不存在循环依赖
- [ ] 父 POM/聚合模块已声明此模块
- [ ] 创建了基本的包结构 (`src/main/java`)
- [ ] 运行 `mvn clean compile` 验证编译成功

### 6.3 Bootstrap 模块特殊检查

- [ ] `<build>` 配置了 `spring-boot-maven-plugin`
- [ ] 依赖了所有 `*-impl` 模块
- [ ] 依赖了 `spring-boot-starter-web`
- [ ] 依赖了 `spring-boot-starter-actuator`
- [ ] 依赖了 `micrometer-tracing-bridge-brave`
- [ ] 依赖了 `micrometer-registry-prometheus`
- [ ] 依赖了 `logstash-logback-encoder`
- [ ] 创建了 `Application.java` 主启动类
- [ ] 创建了 `application.yml` 和环境配置文件
- [ ] 创建了 `logback-spring.xml`
- [ ] 运行 `mvn clean package` 可生成可执行 JAR
- [ ] 运行 `java -jar bootstrap/target/bootstrap-*.jar` 可启动应用

## 7. 验证命令

### 7.1 编译验证

```bash
# 清理并编译整个项目
mvn clean compile

# 预期输出: BUILD SUCCESS
# 检查 Reactor Build Order,确认模块构建顺序正确
```

### 7.2 依赖树验证

```bash
# 查看整个项目的依赖树
mvn dependency:tree

# 查看特定模块的依赖树
cd bootstrap
mvn dependency:tree
```

### 7.3 有效 POM 验证

```bash
# 查看有效 POM (包含继承的配置)
mvn help:effective-pom
```

### 7.4 打包验证

```bash
# 打包 bootstrap 模块为可执行 JAR
mvn clean package

# 检查生成的 JAR
ls -lh bootstrap/target/bootstrap-*.jar
```

### 7.5 启动验证

```bash
# 启动应用
java -jar bootstrap/target/bootstrap-*.jar --spring.profiles.active=local

# 验证健康检查端点
curl http://localhost:8080/actuator/health

# 验证 Prometheus 端点
curl http://localhost:8080/actuator/prometheus
```

## 8. 常见问题和解决方案

### 8.1 编译失败: "Cannot resolve symbol"

**原因**: 模块依赖配置错误或 Maven 未正确识别模块

**解决方案**:
1. 检查父 POM/聚合模块是否已声明此模块
2. 检查依赖的模块是否已创建
3. 运行 `mvn clean install` 重新安装所有模块
4. 刷新 IDE 的 Maven 项目

### 8.2 编译失败: "Circular reference"

**原因**: 模块之间存在循环依赖

**解决方案**:
1. 使用 `mvn dependency:tree` 查看依赖树
2. 识别循环依赖的模块
3. 重构代码,将共享代码提取到 common 模块
4. 确保依赖方向符合 DDD 分层原则

### 8.3 启动失败: "ClassNotFoundException"

**原因**: bootstrap 模块未依赖某个必需的实现模块

**解决方案**:
1. 检查 bootstrap/pom.xml 是否依赖了所有 `*-impl` 模块
2. 检查 Application.java 的 `@ComponentScan` 路径是否正确
3. 运行 `mvn clean package` 重新打包

### 8.4 版本冲突警告

**原因**: 子模块指定了版本号,与父 POM 定义的版本冲突

**解决方案**:
1. 在子模块中移除 `<version>` 标签
2. 确保父 POM 的 `<dependencyManagement>` 中已声明此依赖
3. 运行 `mvn clean compile` 验证

## 9. 参考文档

- **项目宪法**: `.specify/memory/constitution.md`
- **架构决策记录**: `specs/001-init-ddd-architecture/research.md`
- **快速开始指南**: `specs/001-init-ddd-architecture/quickstart.md`
- **功能规格说明**: `specs/001-init-ddd-architecture/spec.md`
- **实施计划**: `specs/001-init-ddd-architecture/plan.md`

---

**文档版本**: 1.0.0 | **最后更新**: 2025-11-21 | **维护者**: Architecture Team
