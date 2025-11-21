# 环境配置说明

## 支持的环境

本项目支持以下 5 种环境配置：

| Profile | 用途 | 日志输出 | 日志格式 | 项目包日志级别 | 配置文件 |
|---------|------|---------|---------|---------------|---------|
| **local** | 本地开发 | 控制台 | 彩色格式 | DEBUG | application-local.yml |
| **dev** | 开发环境 | 文件 | JSON | DEBUG | application-dev.yml |
| **test** | 测试环境 | 文件 | JSON | DEBUG | application-test.yml |
| **staging** | 预发布环境 | 文件 | JSON | INFO | application-staging.yml |
| **prod** | 生产环境 | 文件 | JSON | INFO | application-prod.yml |

## 环境切换方式

### 方式 1: 命令行参数
```bash
java -jar bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### 方式 2: 环境变量
```bash
export SPRING_PROFILES_ACTIVE=dev
java -jar bootstrap-1.0.0-SNAPSHOT.jar
```

### 方式 3: 配置文件 (application.yml)
```yaml
spring:
  profiles:
    active: dev
```

## 环境配置差异

### 数据库配置示例
在各环境配置文件中添加：

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://dev-mysql:3306/aiops?useUnicode=true&characterEncoding=utf8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}

# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/aiops?useUnicode=true&characterEncoding=utf8
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Redis 配置示例
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

### AWS SQS 配置示例
```yaml
aws:
  sqs:
    endpoint: ${AWS_SQS_ENDPOINT}
    region: ${AWS_REGION:us-east-1}
```

## 验证方法

### 1. 检查日志输出
启动应用后，查看日志确认使用的 profile：
```
The following 1 profile is active: "dev"
```

### 2. 检查日志格式
- local 环境: 控制台彩色日志
- 其他环境: logs/app.log 文件，JSON 格式

### 3. 检查日志级别
- local/dev/test: DEBUG 级别
- staging/prod: INFO 级别

## 注意事项

1. **敏感信息**: 生产环境配置使用环境变量，不在代码中硬编码
2. **日志配置**: 所有日志配置在 logback-spring.xml 中管理，不在 application.yml 中配置
3. **端口冲突**: 确保 8080 端口未被占用
4. **数据库连接**: 启动前确保数据库/Redis/SQS 服务可访问

## 相关文档

- 日志配置: logback-spring.xml
- 快速开始指南: ../../specs/001-init-ddd-architecture/quickstart.md
- 依赖管理: ../../DEPENDENCIES.md
