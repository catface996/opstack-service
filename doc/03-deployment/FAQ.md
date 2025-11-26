# AIOps Service 常见问题 FAQ

本文档汇总 AIOps Service 部署、配置和使用过程中的常见问题及解决方案。

## 目录

1. [部署相关](#1-部署相关)
2. [数据库相关](#2-数据库相关)
3. [Redis 相关](#3-redis-相关)
4. [认证与授权](#4-认证与授权)
5. [会话管理](#5-会话管理)
6. [性能相关](#6-性能相关)
7. [安全相关](#7-安全相关)

---

## 1. 部署相关

### Q1.1: 应用启动失败，提示 "Could not resolve placeholder 'security.jwt.secret'"

**原因**：缺少 JWT 密钥配置。

**解决方案**：

```yaml
# application.yml 或 application-{profile}.yml
security:
  jwt:
    secret: your-256-bit-secret-key-must-be-at-least-64-characters-long
    expiration: 7200
    issuer: aiops-service
```

或通过环境变量设置：

```bash
export JWT_SECRET="your-256-bit-secret-key-must-be-at-least-64-characters-long"
```

---

### Q1.2: 启动时报错 "Address already in use: bind"

**原因**：端口 8080 已被其他进程占用。

**解决方案**：

```bash
# 查找占用端口的进程
lsof -i:8080

# 终止进程
kill -9 <PID>

# 或修改应用端口
java -jar app.jar --server.port=8081
```

---

### Q1.3: Docker 容器启动后无法访问服务

**原因**：网络配置问题或健康检查未通过。

**解决方案**：

```bash
# 检查容器状态
docker ps -a

# 查看容器日志
docker logs <container_id>

# 检查容器网络
docker network ls
docker inspect <container_id> | grep IPAddress

# 确保端口映射正确
docker run -p 8080:8080 aiops-service
```

---

### Q1.4: 应用启动很慢，需要等待很长时间

**原因**：可能是数据库连接、DNS 解析或 Flyway 迁移耗时。

**解决方案**：

```yaml
# 1. 优化数据库连接
spring:
  datasource:
    hikari:
      connection-timeout: 10000
      initialization-fail-timeout: 5000

# 2. 禁用不必要的自动配置
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration

# 3. 延迟初始化（开发环境）
spring:
  main:
    lazy-initialization: true
```

---

## 2. 数据库相关

### Q2.1: 数据库连接失败，提示 "Communications link failure"

**原因**：MySQL 服务未启动或网络不通。

**解决方案**：

```bash
# 检查 MySQL 服务
systemctl status mysql

# 测试连接
mysql -h localhost -u aiops -p

# 检查防火墙
sudo ufw status
sudo ufw allow 3306

# 检查 MySQL 绑定地址
grep bind-address /etc/mysql/mysql.conf.d/mysqld.cnf
```

---

### Q2.2: Flyway 迁移失败，提示 "Migration checksum mismatch"

**原因**：已执行的迁移脚本被修改。

**解决方案**：

```bash
# 开发环境：修复校验和
mvn flyway:repair -pl bootstrap

# 生产环境：不要修改已执行的脚本，创建新的迁移脚本修复问题
```

---

### Q2.3: 数据库连接池耗尽，提示 "Connection is not available"

**原因**：连接池配置过小或存在连接泄漏。

**解决方案**：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      idle-timeout: 300000
      max-lifetime: 1800000
      leak-detection-threshold: 60000  # 检测连接泄漏
```

检查连接泄漏：

```sql
SHOW PROCESSLIST;
SHOW STATUS LIKE 'Threads_connected';
```

---

### Q2.4: 中文乱码或 emoji 存储失败

**原因**：字符集配置不正确。

**解决方案**：

```sql
-- 检查数据库字符集
SHOW CREATE DATABASE aiops_service;

-- 修改数据库字符集
ALTER DATABASE aiops_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 检查表字符集
SHOW CREATE TABLE t_account;
```

连接 URL 添加字符集参数：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiops_service?useUnicode=true&characterEncoding=utf8mb4
```

---

## 3. Redis 相关

### Q3.1: Redis 连接失败，提示 "Unable to connect to Redis"

**原因**：Redis 服务未启动或配置错误。

**解决方案**：

```bash
# 检查 Redis 服务
systemctl status redis
redis-cli ping

# 检查 Redis 配置
redis-cli CONFIG GET bind
redis-cli CONFIG GET requirepass

# 测试连接（带密码）
redis-cli -a your_password ping
```

---

### Q3.2: Redis 内存不足

**原因**：缓存数据过多或未设置过期时间。

**解决方案**：

```bash
# 查看内存使用
redis-cli INFO memory

# 设置最大内存
redis-cli CONFIG SET maxmemory 2gb
redis-cli CONFIG SET maxmemory-policy allkeys-lru

# 清理不需要的 key
redis-cli KEYS "temp:*" | xargs redis-cli DEL
```

---

### Q3.3: 会话数据丢失

**原因**：Redis 重启或未开启持久化。

**解决方案**：

```bash
# 开启 RDB 持久化
redis-cli CONFIG SET save "900 1 300 10 60 10000"

# 开启 AOF 持久化
redis-cli CONFIG SET appendonly yes
redis-cli CONFIG SET appendfsync everysec
```

---

## 4. 认证与授权

### Q4.1: 登录失败，提示 "用户名或密码错误"

**可能原因**：
1. 用户名或密码输入错误
2. 账号不存在
3. 密码大小写敏感

**排查步骤**：

```sql
-- 检查用户是否存在
SELECT id, username, email, status FROM t_account WHERE username = 'testuser';

-- 检查账号状态
SELECT status FROM t_account WHERE username = 'testuser';
```

---

### Q4.2: 登录失败，提示 "账号已锁定，请在X分钟后重试"

**原因**：连续多次登录失败触发账号锁定。

**解决方案**：

```bash
# 方法1：等待锁定时间过期（默认15分钟）

# 方法2：清除 Redis 中的登录失败记录
redis-cli DEL "login:fail:testuser"

# 方法3：如果是数据库锁定，更新状态
mysql -e "UPDATE t_account SET status = 'ACTIVE' WHERE username = 'testuser'"
```

---

### Q4.3: JWT Token 无效或过期

**原因**：Token 过期、被撤销或签名验证失败。

**解决方案**：

1. **Token 过期**：重新登录获取新 Token
2. **签名失败**：检查 JWT secret 配置是否一致

```bash
# 解码 JWT Token 查看内容（仅解码 payload，不验证签名）
echo "eyJhbGciOiJIUzUxMiJ9.xxx.xxx" | cut -d. -f2 | base64 -d

# 检查 Token 过期时间
# payload 中的 exp 字段是 Unix 时间戳
```

---

### Q4.4: 注册失败，提示 "用户名已存在" 或 "邮箱已存在"

**原因**：用户名或邮箱重复。

**解决方案**：

```sql
-- 检查重复数据
SELECT username, email FROM t_account WHERE username = 'testuser' OR email = 'test@example.com';
```

使用不同的用户名或邮箱重新注册。

---

### Q4.5: 密码验证失败，提示 "密码不符合安全要求"

**原因**：密码不满足复杂度要求。

**密码要求**：
- 长度：8-30 个字符
- 必须包含：大写字母、小写字母、数字、特殊字符
- 特殊字符包括：`@$!%*?&`

**有效密码示例**：
- `SecureP@ss123`
- `MyStr0ng!Pass`

---

## 5. 会话管理

### Q5.1: 会话突然失效

**可能原因**：
1. Token 过期
2. Redis 重启
3. 管理员强制登出

**排查步骤**：

```bash
# 检查 Redis 中会话是否存在
redis-cli KEYS "session:*"

# 检查会话详情
redis-cli GET "session:user:123:session_id"
```

---

### Q5.2: 多设备登录时其他设备被踢出

**原因**：调用了"强制登出其他设备"功能。

**说明**：这是预期行为。强制登出会撤销该用户的所有其他会话，并生成新的 Token。

---

### Q5.3: "记住我"功能不生效

**原因**：配置问题或前端未正确传递参数。

**检查点**：

1. 登录请求是否包含 `rememberMe: true`
2. 检查 Token 过期时间是否延长

```bash
# 比较普通登录和记住我登录的 expiresIn 值
# 记住我应该返回更长的过期时间（如7天 vs 2小时）
```

---

## 6. 性能相关

### Q6.1: API 响应缓慢

**排查步骤**：

```bash
# 1. 检查应用健康状态
curl http://localhost:8080/actuator/health

# 2. 查看 Prometheus 指标
curl http://localhost:8080/actuator/prometheus | grep http_server_requests

# 3. 检查数据库慢查询
mysql -e "SHOW PROCESSLIST"

# 4. 检查 JVM 状态
jcmd <pid> GC.heap_info
jstat -gcutil <pid> 1000 10
```

---

### Q6.2: 频繁 Full GC

**原因**：堆内存不足或内存泄漏。

**解决方案**：

```bash
# 1. 增加堆内存
java -Xms4g -Xmx4g -jar app.jar

# 2. 生成堆转储分析
jcmd <pid> GC.heap_dump /tmp/heapdump.hprof

# 3. 使用 MAT 或 VisualVM 分析堆转储
```

---

### Q6.3: 并发请求超时

**原因**：线程池耗尽或连接池不足。

**解决方案**：

```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    accept-count: 100

spring:
  datasource:
    hikari:
      maximum-pool-size: 50
```

---

## 7. 安全相关

### Q7.1: 如何安全地存储敏感配置？

**最佳实践**：

1. **使用环境变量**：
   ```bash
   export DB_PASSWORD="secret"
   export JWT_SECRET="your-secret-key"
   ```

2. **使用 Spring Cloud Config Server**

3. **使用密钥管理服务（如 AWS Secrets Manager、HashiCorp Vault）**

---

### Q7.2: 如何防止暴力破解攻击？

**已实现的防护**：
- 连续5次登录失败后锁定账号15分钟
- 登录失败会记录在 Redis 中

**额外建议**：
- 配置 Web 应用防火墙（WAF）
- 启用 CAPTCHA 验证
- 实施 IP 限流

---

### Q7.3: JWT Secret 应该如何生成？

**推荐方法**：

```bash
# 生成256位（32字节）随机密钥
openssl rand -base64 32

# 或生成512位（64字节）密钥
openssl rand -base64 64
```

**注意**：
- 密钥长度至少256位（对于 HMAC-SHA256）
- 不同环境使用不同密钥
- 定期轮换密钥

---

### Q7.4: 如何安全地传输密码？

**要求**：
1. 始终使用 HTTPS
2. 密码在传输前不需要前端加密（HTTPS 已加密）
3. 服务端使用 BCrypt 存储密码哈希

---

### Q7.5: 发现安全漏洞如何报告？

请发送邮件至安全团队：security@example.com

报告内容应包括：
- 漏洞描述
- 复现步骤
- 影响范围
- 建议修复方案（如有）

---

## 快速问题索引

| 错误信息 | 参考章节 |
|---------|---------|
| Could not resolve placeholder | Q1.1 |
| Address already in use | Q1.2 |
| Communications link failure | Q2.1 |
| Migration checksum mismatch | Q2.2 |
| Connection is not available | Q2.3 |
| Unable to connect to Redis | Q3.1 |
| 用户名或密码错误 | Q4.1 |
| 账号已锁定 | Q4.2 |
| Token 无效 | Q4.3 |
| 用户名已存在 | Q4.4 |
| 密码不符合安全要求 | Q4.5 |

---

文档版本：1.0.0
最后更新：2025-11-26
