# Quick Start Guide: AIOps Service 项目

**Feature**: 001-init-ddd-architecture | **Date**: 2025-11-21 | **Version**: 1.0.0

本指南帮助开发者快速理解和使用 AIOps Service 项目架构。

## 前置要求

### 必需软件

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| **JDK** | 21 (LTS) | Java 运行环境 |
| **Maven** | 3.8+ | 项目构建工具 |
| **Git** | 2.30+ | 版本控制 |
| **IDE** | IntelliJ IDEA 2023+ / Eclipse 2023+ | 开发环境 (推荐 IntelliJ IDEA) |

### 环境变量配置

```bash
# 配置 JAVA_HOME (示例为 macOS)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 验证 Java 版本
java -version
# 应该显示: openjdk version "21.0.x"

# 验证 Maven 版本
mvn -version
# 应该显示: Apache Maven 3.8.x 或更高版本
```

## 快速开始 (5 分钟)

### Step 1: 克隆代码仓库

```bash
git clone <repository-url>
cd aiops-service
```

### Step 2: 首次编译项目

```bash
# 清理并编译整个项目 (首次需要下载依赖,约 2 分钟)
mvn clean compile

# 验证编译成功
# 查看 Maven Reactor Build Order,确认模块构建顺序正确
```

**预期输出**:
```
[INFO] Reactor Build Order:
[INFO]
[INFO] AIOps Service                                              [pom]
[INFO] Common                                                     [jar]
[INFO] Infrastructure                                             [pom]
[INFO] Repository                                                 [pom]
[INFO] Repository API                                             [jar]
[INFO] MySQL Implementation                                       [jar]
[INFO] Cache                                                      [pom]
[INFO] Cache API                                                  [jar]
[INFO] Redis Implementation                                       [jar]
[INFO] MQ                                                         [pom]
[INFO] MQ API                                                     [jar]
[INFO] SQS Implementation                                         [jar]
[INFO] Domain                                                     [pom]
[INFO] Domain API                                                 [jar]
[INFO] Domain Implementation                                      [jar]
[INFO] Application                                                [pom]
[INFO] Application API                                            [jar]
[INFO] Application Implementation                                 [jar]
[INFO] Interface                                                  [pom]
[INFO] Interface HTTP                                             [jar]
[INFO] Interface Consumer                                         [jar]
[INFO] Bootstrap                                                  [jar]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Step 3: 打包应用

```bash
# 打包为可执行 JAR (首次约 3 分钟,后续约 1 分钟)
mvn clean package

# 可执行 JAR 位置
ls -lh bootstrap/target/bootstrap-*.jar
```

### Step 4: 启动应用 (本地环境)

```bash
# 使用 local profile 启动 (默认端口 8080)
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 或者使用 Maven 插件启动
cd bootstrap
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**预期输出** (15 秒内启动完成):
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.4.1)

2025-11-21T10:00:00.123+08:00  INFO 12345 --- [           main] c.c.aiops.bootstrap.Application          : Starting Application using Java 21.0.1
2025-11-21T10:00:00.125+08:00  INFO 12345 --- [           main] c.c.aiops.bootstrap.Application          : The following 1 profile is active: "local"
...
2025-11-21T10:00:15.000+08:00  INFO 12345 --- [           main] c.c.aiops.bootstrap.Application          : Started Application in 14.877 seconds (process running for 15.123)
```

### Step 5: 验证应用运行

```bash
# 验证健康检查端点
curl http://localhost:8080/actuator/health

# 预期响应
{"status":"UP"}

# 验证 Prometheus 监控端点
curl http://localhost:8080/actuator/prometheus | head -n 20

# 预期响应 (部分指标)
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 1.048576E7
...
```

## 项目结构导览

### 核心模块概览

```
aiops-service/
├── 📦 common/                    # 通用模块 (异常、工具类、Result)
├── 🚀 bootstrap/                 # 启动模块 (主类、配置文件)
│
├── 🌐 interface/                 # 接口层 (处理外部请求)
│   ├── interface-http/           #   - HTTP REST 接口
│   └── interface-consumer/       #   - 消息队列消费者
│
├── 📋 application/               # 应用层 (业务用例编排)
│   ├── application-api/          #   - 应用服务接口
│   └── application-impl/         #   - 应用服务实现
│
├── 💎 domain/                    # 领域层 (核心业务逻辑)
│   ├── domain-api/               #   - 领域模型定义
│   └── domain-impl/              #   - 领域服务实现
│
└── 🏗️ infrastructure/            # 基础设施层 (技术实现)
    ├── repository/               #   - 数据持久化
    │   ├── repository-api/       #     * 仓储接口 + Entity
    │   └── mysql-impl/           #     * MySQL 实现 + PO
    ├── cache/                    #   - 缓存
    │   ├── cache-api/            #     * 缓存接口
    │   └── redis-impl/           #     * Redis 实现
    └── mq/                       #   - 消息队列
        ├── mq-api/               #     * 消息队列接口
        └── sqs-impl/             #     * AWS SQS 实现
```

### 依赖关系图

```
┌────────────────────────────────────────────────────────────────┐
│                        bootstrap                               │
│              (组装所有 *-impl + common)                        │
└───────────────────────────┬────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ interface-http│    │interface-    │    │   common     │
│              │    │  consumer    │    │              │
└──────┬───────┘    └──────┬───────┘    └──────────────┘
       │                   │
       └───────────┬───────┘
                   │
                   ▼
         ┌──────────────────┐
         │ application-api   │
         └─────────┬─────────┘
                   │
                   ▼
         ┌──────────────────┐
         │application-impl   │
         └─────────┬─────────┘
                   │
                   ▼
         ┌──────────────────┐
         │   domain-api      │
         └─────────┬─────────┘
                   │
       ┌───────────┼───────────┐
       │           │           │
       ▼           ▼           ▼
┌────────────┐ ┌────────┐ ┌────────┐
│repository- │ │cache-  │ │mq-api  │
│    api     │ │ api    │ │        │
└──────┬─────┘ └───┬────┘ └───┬────┘
       │           │           │
       ▼           ▼           ▼
┌────────────┐ ┌────────┐ ┌────────┐
│mysql-impl  │ │redis-  │ │sqs-    │
│            │ │impl    │ │impl    │
└────────────┘ └────────┘ └────────┘

规则: 外层依赖内层,内层不依赖外层 (单向依赖)
```

## 多环境配置

### 支持的环境

| Profile | 用途 | 日志输出 | 日志格式 | 项目包日志级别 |
|---------|------|---------|---------|---------------|
| **local** | 本地开发 | 控制台 | 彩色格式 | DEBUG |
| **dev** | 开发环境 | 文件 | JSON | DEBUG |
| **test** | 测试环境 | 文件 | JSON | DEBUG |
| **staging** | 预发布环境 | 文件 | JSON | INFO |
| **prod** | 生产环境 | 文件 | JSON | INFO |

### 切换环境

```bash
# 方式 1: 命令行参数
java -jar bootstrap/target/bootstrap-*.jar --spring.profiles.active=dev

# 方式 2: 环境变量
export SPRING_PROFILES_ACTIVE=dev
java -jar bootstrap/target/bootstrap-*.jar

# 方式 3: 配置文件 (bootstrap/src/main/resources/application.yml)
spring:
  profiles:
    active: dev
```

### 环境配置文件

```
bootstrap/src/main/resources/
├── application.yml               # 通用配置 (所有环境共享)
├── application-local.yml         # 本地开发环境
├── application-dev.yml           # 开发环境
├── application-test.yml          # 测试环境
├── application-staging.yml       # 预发布环境
├── application-prod.yml          # 生产环境
└── logback-spring.xml            # 日志配置 (使用 <springProfile> 标签区分环境)
```

## 日志追踪

### Trace ID 自动生成

应用集成了 Micrometer Tracing,每个 HTTP 请求自动生成唯一的 `traceId` 和 `spanId`:

```bash
# 发送测试请求
curl http://localhost:8080/actuator/health

# 查看日志输出 (local 环境 - 控制台彩色格式)
2025-11-21T10:30:00.123+08:00  INFO [aiops-service,64cf4e1a7c8e4f2b,9d1a3e5f7b9c1d3e] 12345 --- [nio-8080-exec-1] c.c.aiops.interface_.http.controller     : 处理健康检查请求
```

### JSON 日志格式 (非 local 环境)

在 dev/test/staging/prod 环境,日志以 JSON 格式输出到文件:

```json
{
  "timestamp": "2025-11-21T10:30:00.123+08:00",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.catface996.aiops.interface_.http.controller.HealthController",
  "traceId": "64cf4e1a7c8e4f2b9d1a3e5f7b9c1d3e",
  "spanId": "9d1a3e5f7b9c1d3e",
  "message": "处理健康检查请求"
}
```

### 日志级别配置

**项目包** (`com.catface996.aiops.*`):
- local/dev/test: **DEBUG**
- staging/prod: **INFO**

**框架包** (`org.springframework.*`, `com.baomidou.*`, `com.amazonaws.*`):
- 所有环境: **WARN**

日志配置在 `logback-spring.xml` 中管理,**禁止**在 `application.yml` 中配置日志。

## 监控指标

### Prometheus 端点

应用暴露 Prometheus 格式的监控指标:

```bash
# 访问 Prometheus 端点
curl http://localhost:8080/actuator/prometheus
```

### 关键指标类别

**1. JVM 指标**:
- `jvm.memory.used`: 内存使用量
- `jvm.gc.pause`: GC 暂停时间
- `jvm.threads.live`: 活跃线程数

**2. HTTP 请求指标**:
- `http.server.requests`: 请求总数、响应时间分布
- `http.server.requests.error`: 错误请求数

**3. 数据库连接池指标** (需要配置数据源后):
- `hikaricp.connections.active`: 活跃连接数
- `hikaricp.connections.pending`: 等待连接数

## 常见任务

### 添加新的依赖

**原则**: 子模块不指定版本,由父 POM 统一管理

**Step 1**: 在父 POM 的 `<dependencyManagement>` 中声明版本

```xml
<!-- pom.xml -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>new-library</artifactId>
            <version>1.2.3</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Step 2**: 在子模块中声明依赖 (不指定版本)

```xml
<!-- 例如: common/pom.xml -->
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>new-library</artifactId>
        <!-- 不指定 <version> -->
    </dependency>
</dependencies>
```

**Step 3**: 验证编译

```bash
mvn clean compile
```

### 创建新的模块

**原则**: 渐进式模块声明 - 只声明已创建的模块

**Step 1**: 创建模块目录和 pom.xml

```bash
mkdir -p new-module/src/main/java/com/catface996/aiops/newmodule
cd new-module
# 创建 pom.xml (参考现有模块)
```

**Step 2**: 在父 POM 或聚合模块中声明

```xml
<!-- 例如: pom.xml 或 interface/pom.xml -->
<modules>
    <module>common</module>
    <module>new-module</module>  <!-- 新增 -->
    <!-- ... 其他模块 ... -->
</modules>
```

**Step 3**: 立即验证编译

```bash
mvn clean compile
```

### 查看依赖树

```bash
# 查看整个项目的依赖树
mvn dependency:tree

# 查看特定模块的依赖树
cd bootstrap
mvn dependency:tree
```

### 解决依赖冲突

```bash
# 分析依赖冲突
mvn dependency:tree -Dverbose

# 查看有效 POM (包含继承的配置)
mvn help:effective-pom
```

## IDE 配置

### IntelliJ IDEA (推荐)

**Step 1**: 导入项目

```
File → Open → 选择 aiops-service 根目录 → Open as Project
```

**Step 2**: 配置 JDK

```
File → Project Structure → Project Settings → Project
  - SDK: 选择 Java 21
  - Language Level: 21 - Pattern matching for switch
```

**Step 3**: 配置 Maven

```
File → Settings → Build, Execution, Deployment → Build Tools → Maven
  - Maven home path: 选择 Maven 安装路径
  - User settings file: 选择 settings.xml (如果有自定义配置)
  - JRE for importer: 选择 Java 21
```

**Step 4**: 启用 Annotation Processing (MyBatis-Plus 可能需要)

```
File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
  - 勾选 "Enable annotation processing"
```

**Step 5**: 运行配置

```
Run → Edit Configurations → + → Spring Boot
  - Name: AIOps Service (Local)
  - Main class: com.catface996.aiops.bootstrap.Application
  - Active profiles: local
  - Working directory: $MODULE_WORKING_DIR$
```

### Eclipse

**Step 1**: 导入 Maven 项目

```
File → Import → Maven → Existing Maven Projects
  - Root Directory: aiops-service 根目录
  - 勾选所有模块 → Finish
```

**Step 2**: 配置 JDK

```
Window → Preferences → Java → Installed JREs
  - Add... → Standard VM → 选择 JDK 21 安装路径
  - 勾选为默认 JRE
```

## 故障排查

### 编译失败: "找不到符号"

**原因**: 模块依赖配置错误或 Maven 缓存问题

**解决方案**:
```bash
# 清理 Maven 本地缓存
rm -rf ~/.m2/repository/com/catface996/aiops

# 重新编译
mvn clean compile
```

### 启动失败: "无法找到主清单属性"

**原因**: bootstrap 模块未正确配置 spring-boot-maven-plugin

**解决方案**: 检查 `bootstrap/pom.xml` 是否包含:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

### 日志不包含 traceId

**原因**: Micrometer Tracing 未正确配置

**解决方案**: 检查 `bootstrap/pom.xml` 是否包含:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

### Prometheus 端点返回 404

**原因**: Actuator 端点未暴露

**解决方案**: 检查 `application.yml` 是否包含:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

## 最佳实践

### 1. 编译验证习惯

每次修改 POM 配置后立即运行:
```bash
mvn clean compile
```

### 2. 日志规范

- ✅ 使用 SLF4J API: `LoggerFactory.getLogger()`
- ✅ 项目包日志使用 DEBUG/INFO 级别
- ❌ 禁止使用 `System.out.println()` 或 `e.printStackTrace()`
- ❌ 禁止在 `application.yml` 中配置日志 (统一在 `logback-spring.xml` 中)

### 3. 异常处理

- ✅ 业务异常使用 `BusinessException`
- ✅ 系统异常使用 `SystemException`
- ✅ 所有异常在接口层统一处理 (`@RestControllerAdvice`)
- ❌ 禁止吞掉异常 (`catch (Exception e) {}`)

### 4. 依赖管理

- ✅ 所有版本在父 POM 的 `<dependencyManagement>` 中管理
- ✅ 子模块声明依赖时不指定版本
- ❌ 禁止在子模块中直接指定版本号

### 5. 模块命名

- ✅ `<name>` 标签使用首字母大写英文单词 + 空格: "Domain API"
- ✅ `<artifactId>` 使用小写 + 连字符: "domain-api"
- ❌ 禁止在 `<name>` 中使用连字符或小写

## 下一步

- 📖 阅读 [contracts/pom-structure.md](./contracts/pom-structure.md) 了解详细的 POM 配置规范
- 📖 阅读 [research.md](./research.md) 了解架构决策记录 (ADR)
- 📖 阅读项目宪法 [.specify/memory/constitution.md](../../.specify/memory/constitution.md) 了解开发规范
- 🚀 开始实现第一个业务功能 (参考 `specs/` 目录下的其他特性)

## 帮助与支持

- **项目文档**: `specs/` 目录
- **架构决策**: `specs/001-init-ddd-architecture/research.md`
- **项目宪法**: `.specify/memory/constitution.md`
- **POM 配置规范**: `specs/001-init-ddd-architecture/contracts/pom-structure.md`
