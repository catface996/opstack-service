# AIOps Service 运维手册

本文档提供 AIOps Service 的日常运维指南，包括监控指标、告警规则和故障排查。

## 1. 监控指标

### 1.1 Actuator 端点

| 端点 | 路径 | 说明 |
|-----|------|------|
| 健康检查 | `/actuator/health` | 应用健康状态 |
| 应用信息 | `/actuator/info` | 应用版本信息 |
| Prometheus 指标 | `/actuator/prometheus` | Prometheus 格式指标 |
| 环境信息 | `/actuator/env` | 配置环境（需授权） |

### 1.2 关键业务指标

#### 认证相关

| 指标名称 | 类型 | 说明 |
|---------|------|------|
| `auth_login_total` | Counter | 登录请求总数 |
| `auth_login_success_total` | Counter | 登录成功次数 |
| `auth_login_failure_total` | Counter | 登录失败次数 |
| `auth_register_total` | Counter | 注册请求总数 |
| `auth_logout_total` | Counter | 登出请求总数 |
| `auth_account_locked_total` | Counter | 账号锁定次数 |

#### 会话相关

| 指标名称 | 类型 | 说明 |
|---------|------|------|
| `session_active_count` | Gauge | 当前活跃会话数 |
| `session_created_total` | Counter | 会话创建总数 |
| `session_expired_total` | Counter | 会话过期总数 |
| `session_force_logout_total` | Counter | 强制登出次数 |

### 1.3 JVM 指标

| 指标名称 | 说明 | 告警阈值 |
|---------|------|---------|
| `jvm_memory_used_bytes` | JVM 内存使用量 | > 80% 堆内存 |
| `jvm_gc_pause_seconds` | GC 暂停时间 | > 500ms |
| `jvm_threads_live` | 活跃线程数 | > 500 |

### 1.4 HTTP 请求指标

| 指标名称 | 说明 |
|---------|------|
| `http_server_requests_seconds_count` | HTTP 请求数 |
| `http_server_requests_seconds_sum` | HTTP 请求总耗时 |
| `http_server_requests_seconds_max` | HTTP 请求最大耗时 |

### 1.5 数据库指标

| 指标名称 | 说明 | 告警阈值 |
|---------|------|---------|
| `hikaricp_connections_active` | 活跃连接数 | > 80% 最大连接 |
| `hikaricp_connections_pending` | 等待连接数 | > 5 |
| `hikaricp_connections_timeout_total` | 连接超时次数 | > 0 |

### 1.6 Redis 指标

| 指标名称 | 说明 | 告警阈值 |
|---------|------|---------|
| `redis_connected_clients` | 连接客户端数 | - |
| `redis_used_memory_bytes` | Redis 内存使用 | > 80% |
| `redis_commands_processed_total` | 命令处理数 | - |

## 2. Prometheus 配置

### 2.1 scrape 配置

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'aiops-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['aiops-service:8080']
        labels:
          application: 'aiops-service'
          environment: 'production'
```

### 2.2 常用 PromQL 查询

```promql
# 登录成功率（最近5分钟）
sum(rate(auth_login_success_total[5m])) / sum(rate(auth_login_total[5m])) * 100

# HTTP 请求 P99 延迟
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))

# 活跃会话数
session_active_count

# 数据库连接池使用率
hikaricp_connections_active / hikaricp_connections_max * 100

# JVM 堆内存使用率
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

## 3. 告警规则

### 3.1 Prometheus AlertManager 配置

```yaml
# alert_rules.yml
groups:
  - name: aiops-service-alerts
    rules:
      # 服务不可用
      - alert: ServiceDown
        expr: up{job="aiops-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "AIOps Service 不可用"
          description: "服务已停止响应超过1分钟"

      # 高错误率
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          / sum(rate(http_server_requests_seconds_count[5m])) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "HTTP 5xx 错误率过高"
          description: "5xx 错误率超过 5%"

      # 高延迟
      - alert: HighLatency
        expr: |
          histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API 响应延迟过高"
          description: "P95 延迟超过 1 秒"

      # 数据库连接池耗尽
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_pending > 5
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "数据库连接池即将耗尽"
          description: "等待数据库连接的请求超过 5 个"

      # JVM 堆内存过高
      - alert: HighHeapMemoryUsage
        expr: |
          jvm_memory_used_bytes{area="heap"}
          / jvm_memory_max_bytes{area="heap"} > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM 堆内存使用过高"
          description: "堆内存使用率超过 85%"

      # 登录失败率过高
      - alert: HighLoginFailureRate
        expr: |
          sum(rate(auth_login_failure_total[5m]))
          / sum(rate(auth_login_total[5m])) > 0.3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "登录失败率过高"
          description: "登录失败率超过 30%，可能存在暴力破解攻击"

      # 账号锁定数过多
      - alert: TooManyAccountLocks
        expr: sum(increase(auth_account_locked_total[1h])) > 100
        for: 0m
        labels:
          severity: warning
        annotations:
          summary: "账号锁定数量异常"
          description: "过去1小时内锁定账号数超过100，可能存在攻击"

      # Redis 不可用
      - alert: RedisDown
        expr: redis_up == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Redis 不可用"
          description: "Redis 服务已停止"
```

### 3.2 告警通知配置

```yaml
# alertmanager.yml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h
  receiver: 'default-receiver'
  routes:
    - match:
        severity: critical
      receiver: 'pagerduty-critical'
    - match:
        severity: warning
      receiver: 'slack-warning'

receivers:
  - name: 'default-receiver'
    email_configs:
      - to: 'ops-team@example.com'

  - name: 'pagerduty-critical'
    pagerduty_configs:
      - service_key: 'your-pagerduty-key'

  - name: 'slack-warning'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/xxx/xxx/xxx'
        channel: '#ops-alerts'
```

## 4. 日志管理

### 4.1 日志配置

```yaml
# application-prod.yml
logging:
  level:
    root: INFO
    com.catface996.aiops: INFO
    org.springframework.security: WARN
  file:
    name: /var/log/aiops-service/app.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 3GB
```

### 4.2 日志格式

```xml
<!-- logback-spring.xml -->
<configuration>
    <property name="LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n"/>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/aiops-service/app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/aiops-service/app.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
            <maxFileSize>100MB</maxFileSize>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
</configuration>
```

### 4.3 日志关键字监控

| 关键字 | 级别 | 说明 |
|-------|------|------|
| `ERROR` | 严重 | 需要立即关注的错误 |
| `账号已锁定` | 警告 | 账号安全事件 |
| `OutOfMemoryError` | 严重 | 内存溢出 |
| `Connection refused` | 严重 | 连接被拒绝 |
| `Timeout` | 警告 | 超时错误 |

## 5. 故障排查

### 5.1 服务无法启动

```bash
# 检查服务状态
systemctl status aiops-service

# 查看启动日志
journalctl -u aiops-service -n 100

# 检查端口占用
lsof -i:8080

# 检查 Java 进程
ps aux | grep aiops

# 检查配置文件
cat /opt/aiops-service/application-prod.yml
```

### 5.2 数据库连接问题

```bash
# 测试数据库连接
mysql -h localhost -u aiops -p aiops_service -e "SELECT 1"

# 检查数据库状态
systemctl status mysql

# 检查连接数
mysql -e "SHOW STATUS LIKE 'Threads_connected'"

# 检查最大连接数
mysql -e "SHOW VARIABLES LIKE 'max_connections'"

# 检查慢查询
mysql -e "SHOW PROCESSLIST"
```

### 5.3 Redis 连接问题

```bash
# 测试 Redis 连接
redis-cli -a your_password ping

# 检查 Redis 状态
redis-cli -a your_password INFO

# 查看 Redis 内存
redis-cli -a your_password INFO memory

# 查看 Redis 客户端
redis-cli -a your_password CLIENT LIST

# 清理指定 key 模式
redis-cli -a your_password KEYS "login:fail:*" | xargs redis-cli -a your_password DEL
```

### 5.4 内存问题

```bash
# 查看 JVM 堆内存
jcmd <pid> GC.heap_info

# 触发 Full GC
jcmd <pid> GC.run

# 生成堆转储
jcmd <pid> GC.heap_dump /tmp/heapdump.hprof

# 查看线程栈
jcmd <pid> Thread.print > /tmp/threaddump.txt

# 使用 jstat 监控 GC
jstat -gcutil <pid> 1000 10
```

### 5.5 性能问题

```bash
# 查看 HTTP 请求延迟
curl -s http://localhost:8080/actuator/prometheus | grep http_server_requests

# 查看数据库连接池
curl -s http://localhost:8080/actuator/prometheus | grep hikaricp

# 查看活跃线程
curl -s http://localhost:8080/actuator/prometheus | grep jvm_threads

# 使用 arthas 诊断
curl -O https://arthas.aliyun.com/arthas-boot.jar
java -jar arthas-boot.jar

# arthas 常用命令
dashboard           # 实时监控面板
thread -n 10        # 查看最忙的10个线程
trace com.catface996.aiops.* *   # 方法调用追踪
```

## 6. 常用运维操作

### 6.1 服务管理

```bash
# 启动服务
systemctl start aiops-service

# 停止服务
systemctl stop aiops-service

# 重启服务
systemctl restart aiops-service

# 优雅重启（等待请求处理完成）
kill -15 <pid>

# 查看服务状态
systemctl status aiops-service

# 查看实时日志
journalctl -u aiops-service -f
```

### 6.2 配置热更新

某些配置支持热更新（通过 Actuator）：

```bash
# 刷新配置（需要 spring-cloud-starter-config）
curl -X POST http://localhost:8080/actuator/refresh

# 查看当前配置
curl http://localhost:8080/actuator/env
```

### 6.3 数据库维护

```bash
# 数据库备份
mysqldump -u root -p aiops_service > backup_$(date +%Y%m%d).sql

# 数据库恢复
mysql -u root -p aiops_service < backup_20250126.sql

# 优化表
mysqlcheck -u root -p --optimize aiops_service

# 分析表
mysqlcheck -u root -p --analyze aiops_service
```

### 6.4 Redis 维护

```bash
# 备份 Redis 数据
redis-cli -a your_password BGSAVE

# 查看备份状态
redis-cli -a your_password LASTSAVE

# 清理过期 key
redis-cli -a your_password --scan --pattern "session:*" | head -100

# 查看内存使用 Top Keys
redis-cli -a your_password --bigkeys
```

### 6.5 清理会话

```bash
# 清理所有会话（强制所有用户重新登录）
redis-cli -a your_password KEYS "session:*" | xargs redis-cli -a your_password DEL

# 清理特定用户会话
redis-cli -a your_password KEYS "session:user:123:*" | xargs redis-cli -a your_password DEL

# 清理登录失败记录（解锁所有账号）
redis-cli -a your_password KEYS "login:fail:*" | xargs redis-cli -a your_password DEL
```

## 7. 容量规划

### 7.1 资源估算

| 并发用户 | CPU | 内存 | 数据库连接 | Redis 连接 |
|---------|-----|------|-----------|-----------|
| 100 | 2核 | 4GB | 20 | 10 |
| 500 | 4核 | 8GB | 50 | 20 |
| 1000 | 8核 | 16GB | 100 | 50 |
| 5000 | 16核 | 32GB | 200 | 100 |

### 7.2 扩展策略

1. **垂直扩展**：增加单机资源（CPU、内存）
2. **水平扩展**：增加应用实例，使用负载均衡
3. **读写分离**：MySQL 主从复制，读操作分发到从库
4. **Redis 集群**：使用 Redis Cluster 或 Sentinel

### 7.3 负载均衡配置

```nginx
# nginx.conf
upstream aiops-service {
    least_conn;
    server 10.0.0.1:8080 weight=5;
    server 10.0.0.2:8080 weight=5;
    server 10.0.0.3:8080 backup;
}

server {
    listen 80;
    server_name api.example.com;

    location / {
        proxy_pass http://aiops-service;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 10s;
        proxy_read_timeout 60s;
    }
}
```

## 8. 安全运维

### 8.1 安全审计

```bash
# 查看登录失败日志
grep "登录失败" /var/log/aiops-service/app.log

# 查看账号锁定事件
grep "账号已锁定" /var/log/aiops-service/app.log

# 统计登录来源 IP
grep "login" /var/log/aiops-service/app.log | awk '{print $NF}' | sort | uniq -c | sort -rn
```

### 8.2 安全检查清单

- [ ] 定期更新系统和依赖包
- [ ] 定期轮换密码和密钥
- [ ] 检查异常登录行为
- [ ] 检查数据库备份完整性
- [ ] 检查日志文件权限
- [ ] 检查 SSL 证书有效期

### 8.3 应急响应

1. **服务中断**：立即检查服务状态，尝试重启
2. **数据泄露**：立即轮换所有密钥，通知安全团队
3. **暴力攻击**：启用 IP 黑名单，增加限流规则
4. **数据库故障**：切换到从库，恢复数据

---

文档版本：1.0.0
最后更新：2025-11-26
