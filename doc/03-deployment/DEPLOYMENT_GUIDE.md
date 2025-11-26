# AIOps Service 部署指南

本文档详细描述 AIOps Service 的部署流程，包括环境准备、配置说明和部署步骤。

## 1. 环境要求

### 1.1 硬件要求

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 开发环境 | 2核+ | 4GB+ | 20GB+ |
| 测试环境 | 4核+ | 8GB+ | 50GB+ |
| 生产环境 | 8核+ | 16GB+ | 100GB+ SSD |

### 1.2 软件要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 21+ | 推荐 OpenJDK 21 LTS |
| MySQL | 8.0+ | 主数据存储 |
| Redis | 7.0+ | 会话存储、缓存 |
| Maven | 3.9+ | 构建工具 |
| Docker | 24.0+ | 容器化部署（可选） |

### 1.3 网络要求

- 应用服务端口：8080（HTTP）
- MySQL 端口：3306
- Redis 端口：6379
- 确保防火墙开放相应端口

## 2. 环境准备

### 2.1 安装 JDK 21

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# CentOS/RHEL
sudo yum install java-21-openjdk-devel

# macOS (使用 Homebrew)
brew install openjdk@21

# 验证安装
java -version
```

### 2.2 安装 MySQL 8.0

```bash
# Ubuntu/Debian
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# macOS
brew install mysql

# 启动服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

### 2.3 安装 Redis 7

```bash
# Ubuntu/Debian
sudo apt install redis-server

# CentOS/RHEL
sudo yum install redis

# macOS
brew install redis

# 启动服务
sudo systemctl start redis
sudo systemctl enable redis

# 验证
redis-cli ping
```

## 3. 数据库初始化

### 3.1 创建数据库和用户

```sql
-- 连接 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE aiops_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（生产环境请使用强密码）
CREATE USER 'aiops'@'localhost' IDENTIFIED BY 'your_secure_password';
CREATE USER 'aiops'@'%' IDENTIFIED BY 'your_secure_password';

-- 授权
GRANT ALL PRIVILEGES ON aiops_service.* TO 'aiops'@'localhost';
GRANT ALL PRIVILEGES ON aiops_service.* TO 'aiops'@'%';
FLUSH PRIVILEGES;
```

### 3.2 数据库迁移

应用使用 Flyway 进行数据库版本管理，启动时会自动执行迁移脚本。

迁移脚本位置：`bootstrap/src/main/resources/db/migration/`

```
V1__Create_user_account_table.sql  -- 用户账号表
V2__Create_user_session_table.sql  -- 用户会话表
```

## 4. 配置说明

### 4.1 配置文件结构

```
bootstrap/src/main/resources/
├── application.yml           # 主配置文件
├── application-dev.yml       # 开发环境配置
├── application-test.yml      # 测试环境配置
└── application-prod.yml      # 生产环境配置
```

### 4.2 核心配置项

#### 数据源配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiops_service?useSSL=true&serverTimezone=Asia/Shanghai
    username: aiops
    password: ${DB_PASSWORD}  # 从环境变量读取
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
```

#### Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD}  # 从环境变量读取
      database: 0
      timeout: 3000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

#### JWT 安全配置

```yaml
security:
  jwt:
    # JWT 签名密钥（生产环境必须使用强密钥）
    secret: ${JWT_SECRET}
    # Token 过期时间（秒）
    expiration: 7200
    # Token 发行者
    issuer: aiops-service
  login:
    # 登录失败最大次数
    max-failed-attempts: 5
    # 账号锁定时长（秒）
    lock-duration: 900
```

### 4.3 环境变量

生产环境部署时，敏感信息应通过环境变量注入：

| 环境变量 | 说明 | 示例 |
|---------|------|------|
| `DB_PASSWORD` | 数据库密码 | `your_secure_db_password` |
| `REDIS_PASSWORD` | Redis 密码 | `your_secure_redis_password` |
| `JWT_SECRET` | JWT 签名密钥 | `your-256-bit-secret-key-here...` |
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | `prod` |

```bash
# 设置环境变量示例
export DB_PASSWORD="your_secure_db_password"
export REDIS_PASSWORD="your_secure_redis_password"
export JWT_SECRET="your-256-bit-secret-key-for-production-must-be-at-least-64-chars"
export SPRING_PROFILES_ACTIVE="prod"
```

## 5. 构建部署

### 5.1 源码构建

```bash
# 克隆代码
git clone https://github.com/your-org/aiops-service.git
cd aiops-service

# 构建（跳过测试）
mvn clean package -DskipTests

# 构建（运行测试）
mvn clean package

# 构建产物位置
ls bootstrap/target/aiops-bootstrap-*.jar
```

### 5.2 直接运行

```bash
# 开发环境
java -jar bootstrap/target/aiops-bootstrap-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=dev

# 生产环境
java -jar bootstrap/target/aiops-bootstrap-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  -Xms4g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

### 5.3 Systemd 服务部署

创建服务文件 `/etc/systemd/system/aiops-service.service`：

```ini
[Unit]
Description=AIOps Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=aiops
Group=aiops
WorkingDirectory=/opt/aiops-service
Environment="JAVA_HOME=/usr/lib/jvm/java-21-openjdk"
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_PASSWORD=your_secure_db_password"
Environment="REDIS_PASSWORD=your_secure_redis_password"
Environment="JWT_SECRET=your-256-bit-secret-key"
ExecStart=/usr/bin/java \
  -Xms4g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar /opt/aiops-service/aiops-bootstrap.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用并启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable aiops-service
sudo systemctl start aiops-service
sudo systemctl status aiops-service
```

### 5.4 Docker 部署

#### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="your-team@example.com"

# 创建非 root 用户
RUN addgroup -S aiops && adduser -S aiops -G aiops

WORKDIR /app

# 复制应用
COPY bootstrap/target/aiops-bootstrap-*.jar app.jar

# 设置权限
RUN chown -R aiops:aiops /app

USER aiops

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Docker Compose

```yaml
version: '3.8'

services:
  aiops-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/aiops_service
      - SPRING_DATASOURCE_USERNAME=aiops
      - DB_PASSWORD=${DB_PASSWORD}
      - SPRING_DATA_REDIS_HOST=redis
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    restart: always

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=aiops_service
      - MYSQL_USER=aiops
      - MYSQL_PASSWORD=${DB_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: always

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: always

volumes:
  mysql_data:
  redis_data:
```

启动：

```bash
# 创建 .env 文件
cat > .env << EOF
DB_PASSWORD=your_secure_db_password
REDIS_PASSWORD=your_secure_redis_password
JWT_SECRET=your-256-bit-secret-key-for-production
MYSQL_ROOT_PASSWORD=root_password
EOF

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f aiops-service
```

## 6. 验证部署

### 6.1 健康检查

```bash
# 检查应用健康状态
curl http://localhost:8080/actuator/health

# 预期响应
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### 6.2 功能验证

```bash
# 用户注册
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecureP@ss123"
  }'

# 用户登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "SecureP@ss123",
    "rememberMe": false
  }'
```

### 6.3 Prometheus 指标

```bash
# 查看 Prometheus 指标
curl http://localhost:8080/actuator/prometheus
```

## 7. 安全加固

### 7.1 生产环境检查清单

- [ ] 使用强密码（数据库、Redis、JWT Secret）
- [ ] 启用 HTTPS（配置 SSL 证书）
- [ ] 限制数据库远程访问
- [ ] 配置防火墙规则
- [ ] 禁用不必要的 Actuator 端点
- [ ] 定期备份数据库
- [ ] 配置日志轮转
- [ ] 设置资源限制（JVM 内存、连接池）

### 7.2 HTTPS 配置

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: aiops-service
```

### 7.3 Actuator 安全

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

## 8. 故障排查

### 8.1 常见启动问题

| 问题 | 可能原因 | 解决方案 |
|-----|---------|---------|
| 数据库连接失败 | MySQL 未启动或配置错误 | 检查 MySQL 服务状态和连接参数 |
| Redis 连接失败 | Redis 未启动或密码错误 | 检查 Redis 服务状态和密码配置 |
| 端口被占用 | 其他进程占用 8080 | `lsof -i:8080` 查找并处理 |
| OOM 错误 | JVM 内存不足 | 增加 `-Xmx` 参数 |

### 8.2 日志位置

```bash
# 查看应用日志
tail -f /var/log/aiops-service/app.log

# 查看系统日志
journalctl -u aiops-service -f
```

---

文档版本：1.0.0
最后更新：2025-11-26
