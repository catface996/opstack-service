# AIOps Service 性能测试指南

本文档描述 AIOps Service 认证模块的性能测试方案，包括测试工具、测试场景、性能指标和执行步骤。

## 1. 性能需求

根据需求文档，系统需要满足以下性能指标：

| 指标 | 需求 | 说明 |
|-----|------|------|
| REQ-NFR-PERF-001 | 登录响应时间 P95 < 2秒 | 95%的登录请求在2秒内完成 |
| REQ-NFR-PERF-002 | 系统支持 1000 并发用户 | 峰值并发用户数 |
| REQ-NFR-PERF-003 | BCrypt 单次验证 < 500ms | 密码哈希验证性能 |

## 2. 测试工具

### 2.1 JMeter

Apache JMeter 是一款开源的性能测试工具，支持多种协议。

**版本要求**：5.6+

**安装方式**：
```bash
# macOS
brew install jmeter

# Linux
wget https://dlcdn.apache.org/jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
export PATH=$PATH:/path/to/apache-jmeter-5.6.3/bin
```

### 2.2 Gatling

Gatling 是一款基于 Scala 的高性能负载测试工具。

**版本要求**：3.9+

**安装方式**：
```bash
# 使用 Maven 插件
<plugin>
    <groupId>io.gatling</groupId>
    <artifactId>gatling-maven-plugin</artifactId>
    <version>4.3.0</version>
</plugin>
```

## 3. 测试场景

### 3.1 场景概览

| 场景 | 并发数 | 持续时间 | 目标 |
|-----|--------|---------|------|
| 用户注册 | 100 | 30秒 | 验证注册接口吞吐量 |
| 用户登录 | 200 | 60秒 | 验证 BCrypt 性能 |
| 会话验证 | 500 | 60秒 | 验证 Redis 缓存性能 |
| 完整流程 | 100 | 30秒 | 验证端到端性能 |
| 峰值负载 | 1000 | 120秒 | 验证系统极限 |

### 3.2 场景1：用户注册性能测试

**目的**：验证注册接口的吞吐量和响应时间

**配置**：
- 并发用户：100
- 爬坡时间：30秒
- 每用户请求数：10
- 总请求数：1000

**请求示例**：
```json
POST /api/v1/auth/register
{
  "username": "perfuser_${counter}",
  "email": "perfuser_${counter}@test.com",
  "password": "SecureP@ss123"
}
```

**预期指标**：
- 响应时间 P95 < 3秒
- 成功率 > 95%
- TPS > 30

### 3.3 场景2：用户登录性能测试

**目的**：验证登录接口性能，特别是 BCrypt 密码验证

**配置**：
- 并发用户：200
- 爬坡时间：60秒
- 每用户请求数：50
- 总请求数：10000

**请求示例**：
```json
POST /api/v1/auth/login
{
  "identifier": "${username}",
  "password": "${password}",
  "rememberMe": false
}
```

**预期指标**：
- 响应时间 P95 < 2秒（REQ-NFR-PERF-001）
- BCrypt 验证时间 < 500ms（REQ-NFR-PERF-003）
- 成功率 > 95%
- TPS > 50

### 3.4 场景3：会话验证性能测试

**目的**：验证会话验证接口性能（Redis 缓存）

**配置**：
- 并发用户：500
- 爬坡时间：60秒
- 每用户请求数：100
- 总请求数：50000

**请求示例**：
```
GET /api/v1/session/validate
Authorization: Bearer ${token}
```

**预期指标**：
- 响应时间 P95 < 500ms
- 响应时间平均 < 100ms
- 成功率 > 99%
- TPS > 500

### 3.5 场景4：完整业务流程测试

**目的**：模拟真实用户行为，测试端到端性能

**流程**：
1. 用户注册
2. 用户登录
3. 会话验证（5次）
4. 用户登出

**配置**：
- 并发用户：100
- 爬坡时间：30秒

**预期指标**：
- 端到端响应时间 < 5秒
- 成功率 > 95%

### 3.6 场景5：峰值负载测试

**目的**：验证系统支持 1000 并发用户（REQ-NFR-PERF-002）

**配置**：
- 并发用户：1000
- 爬坡时间：120秒
- 阶梯增长：每10秒增加100用户

**预期指标**：
- 响应时间 P95 < 3秒
- 成功率 > 90%
- 系统无崩溃

## 4. 执行步骤

### 4.1 环境准备

```bash
# 1. 启动应用
cd /path/to/aiops-service
mvn spring-boot:run -pl bootstrap

# 2. 确认应用健康
curl http://localhost:8080/actuator/health

# 3. 创建测试用户（用于登录测试）
./scripts/create-test-users.sh
```

### 4.2 创建测试用户脚本

```bash
#!/bin/bash
# create-test-users.sh

BASE_URL="http://localhost:8080"

for i in $(seq -w 1 50); do
  curl -s -X POST "$BASE_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"perfuser${i}\",
      \"email\": \"perfuser${i}@test.com\",
      \"password\": \"SecureP@ss123\"
    }"
  echo ""
done

# 创建 BCrypt 测试用户
curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bcrypttest",
    "email": "bcrypttest@test.com",
    "password": "SecureP@ss123"
  }'
```

### 4.3 运行 JMeter 测试

```bash
# GUI 模式（用于调试）
jmeter -t doc/04-testing/performance/jmeter/auth-performance-test.jmx

# 命令行模式（用于正式测试）
jmeter -n \
  -t doc/04-testing/performance/jmeter/auth-performance-test.jmx \
  -l results.jtl \
  -e -o report/

# 指定参数
jmeter -n \
  -t auth-performance-test.jmx \
  -JBASE_URL=localhost \
  -JPORT=8080 \
  -l results.jtl
```

### 4.4 运行 Gatling 测试

```bash
# 运行所有测试
mvn gatling:test

# 运行特定测试
mvn gatling:test -Dgatling.simulationClass=aiops.performance.AuthSimulation

# 指定参数
mvn gatling:test \
  -Dgatling.simulationClass=aiops.performance.AuthSimulation \
  -DbaseUrl=http://localhost:8080 \
  -Dusers=100 \
  -Dduration=60

# 运行 BCrypt 专项测试
mvn gatling:test -Dgatling.simulationClass=aiops.performance.BCryptPerformanceSimulation

# 运行 1000 并发测试
mvn gatling:test -Dgatling.simulationClass=aiops.performance.ConcurrentUsersSimulation
```

## 5. 结果分析

### 5.1 JMeter 报告

JMeter 生成的 HTML 报告包含：
- 聚合报告（Aggregate Report）
- 响应时间分布图
- 吞吐量图
- 错误分析

关键指标位置：
```
report/
├── index.html           # 主报告
├── content/
│   ├── js/
│   └── pages/
│       ├── OverTime.html      # 时间趋势
│       ├── ResponseTimes.html # 响应时间
│       └── Throughput.html    # 吞吐量
```

### 5.2 Gatling 报告

Gatling 报告位于 `target/gatling/` 目录：

```
target/gatling/
└── authsimulation-20251126120000/
    ├── index.html           # 主报告
    ├── simulation.log       # 详细日志
    └── js/
        ├── global_stats.json
        └── stats.json
```

关键指标：
- **Response Time Distribution**：响应时间分布
- **Number of Requests per Second**：每秒请求数
- **Response Time Percentiles**：响应时间百分位

### 5.3 性能指标判定

| 指标 | 通过条件 | 检查方法 |
|-----|---------|---------|
| 登录 P95 | < 2秒 | Gatling: `details("用户登录").responseTime.percentile(95)` |
| BCrypt | < 500ms | Gatling: `details("用户登录").responseTime.mean` |
| 并发用户 | 1000 | 运行 `ConcurrentUsersSimulation` 无错误 |
| 成功率 | > 95% | `global.successfulRequests.percent` |

## 6. 性能调优建议

### 6.1 应用层优化

```yaml
# application-prod.yml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 20
    accept-count: 100
    max-connections: 10000

spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
```

### 6.2 JVM 优化

```bash
java -jar app.jar \
  -Xms4g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+ParallelRefProcEnabled
```

### 6.3 Redis 优化

```conf
# redis.conf
maxclients 10000
tcp-backlog 511
timeout 0
tcp-keepalive 300
```

### 6.4 MySQL 优化

```sql
-- 检查慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- 优化连接数
SET GLOBAL max_connections = 500;
```

## 7. 常见问题

### Q1: JMeter 报 "Connection refused"

**原因**：应用未启动或端口错误

**解决**：
```bash
# 检查应用状态
curl http://localhost:8080/actuator/health

# 检查端口
lsof -i:8080
```

### Q2: Gatling 用户数据文件找不到

**原因**：CSV 文件路径错误

**解决**：
```bash
# 将 users.csv 放到 Gatling 资源目录
cp users.csv src/test/resources/
```

### Q3: BCrypt 性能超过 500ms

**原因**：Work Factor 设置过高或服务器 CPU 不足

**解决**：
```java
// 检查 BCrypt Work Factor（默认应为 10）
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

### Q4: 高并发时大量超时

**原因**：连接池或线程池不足

**解决**：增加连接池大小和线程数，参考 6.1 节配置

## 8. 测试报告模板

```markdown
# 性能测试报告

## 测试信息
- 测试日期：2025-11-26
- 测试环境：开发环境 / 测试环境 / 生产环境
- 测试工具：JMeter 5.6.3 / Gatling 3.9.5

## 测试结果摘要

| 场景 | 并发数 | 平均响应时间 | P95 | 成功率 | TPS |
|-----|--------|------------|-----|--------|-----|
| 用户注册 | 100 | xxxms | xxxms | xx% | xx |
| 用户登录 | 200 | xxxms | xxxms | xx% | xx |
| 会话验证 | 500 | xxxms | xxxms | xx% | xx |
| 峰值负载 | 1000 | xxxms | xxxms | xx% | xx |

## 性能需求验证

| 需求 | 指标 | 实际值 | 状态 |
|-----|------|-------|------|
| REQ-NFR-PERF-001 | 登录 P95 < 2秒 | xxxms | ✅/❌ |
| REQ-NFR-PERF-002 | 支持1000并发 | 最大xxx | ✅/❌ |
| REQ-NFR-PERF-003 | BCrypt < 500ms | xxxms | ✅/❌ |

## 问题和建议
- [问题1]：xxx
- [建议1]：xxx

## 附件
- JMeter 报告：report/index.html
- Gatling 报告：target/gatling/xxx/index.html
```

---

文档版本：1.0.0
最后更新：2025-11-26
