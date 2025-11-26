# AIOps Service 安全测试指南

本文档描述 AIOps Service 认证模块的安全测试方案，包括测试工具、测试场景、安全需求和执行步骤。

## 1. 安全需求

根据需求文档，系统需要满足以下安全要求：

| 需求ID | 描述 | 测试方法 |
|--------|------|---------|
| REQ-NFR-SEC-001 | 密码使用 BCrypt 加密存储 | 数据库检查 |
| REQ-NFR-SEC-002 | JWT Token 不可伪造 | Token 安全测试 |
| REQ-NFR-SEC-003 | 连续5次登录失败锁定账号30分钟 | 暴力破解测试 |
| REQ-NFR-SEC-004 | 不泄露敏感技术细节 | 信息泄露测试 |
| REQ-NFR-SEC-005 | 防止 SQL 注入攻击 | SQL 注入测试 |
| REQ-NFR-SEC-006 | 防止 XSS 攻击 | XSS 测试 |

## 2. 测试工具

### 2.1 OWASP ZAP

OWASP ZAP (Zed Attack Proxy) 是一款开源的 Web 应用安全扫描工具。

**版本要求**：2.14+

**安装方式**：

```bash
# Docker 方式（推荐）
docker pull ghcr.io/zaproxy/zaproxy:stable

# macOS
brew install --cask owasp-zap

# Linux
wget https://github.com/zaproxy/zaproxy/releases/download/v2.14.0/ZAP_2.14.0_Linux.tar.gz
tar -xzf ZAP_2.14.0_Linux.tar.gz
```

### 2.2 Shell 测试脚本

提供了自动化 Shell 脚本进行快速安全测试。

```bash
# 运行安全测试
./doc/04-testing/security/scripts/security-test.sh

# 指定目标 URL
./doc/04-testing/security/scripts/security-test.sh http://localhost:8080
```

## 3. 测试场景

### 3.1 SQL 注入测试

**测试目标**：验证系统能防止 SQL 注入攻击

**测试 Payload**：
```
' OR '1'='1
admin'--
1; DROP TABLE users;--
' UNION SELECT * FROM users--
1' AND '1'='1
" OR "1"="1
```

**测试接口**：
- POST /api/v1/auth/login（identifier 参数）
- POST /api/v1/auth/register（username、email 参数）

**预期结果**：
- 所有 SQL 注入尝试被阻止
- 不返回数据库错误信息
- 不执行非预期的 SQL 语句

**验证命令**：
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"'\'' OR '\''1'\''='\''1","password":"test","rememberMe":false}'
```

### 3.2 XSS 攻击测试

**测试目标**：验证系统能防止跨站脚本攻击

**测试 Payload**：
```html
<script>alert('XSS')</script>
<img src=x onerror=alert('XSS')>
javascript:alert('XSS')
<svg onload=alert('XSS')>
'"><script>alert('XSS')</script>
```

**测试接口**：
- POST /api/v1/auth/register（username 参数）
- 所有返回用户输入的接口

**预期结果**：
- XSS payload 被过滤或转义
- 响应中不包含可执行的脚本代码
- Content-Type 正确设置为 application/json

**验证命令**：
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"<script>alert(1)</script>","email":"xss@test.com","password":"SecureP@ss123"}'
```

### 3.3 暴力破解防护测试

**测试目标**：验证连续5次登录失败后账号被锁定

**测试步骤**：
1. 创建测试用户
2. 使用错误密码连续登录6次
3. 验证第6次返回 HTTP 423 (Locked)
4. 验证锁定消息包含剩余时间

**预期结果**：
- 前5次返回登录失败（HTTP 200，success=false）
- 第6次返回账号锁定（HTTP 423）
- 锁定时间为30分钟

**验证脚本**：
```bash
#!/bin/bash
for i in {1..6}; do
  echo "尝试 $i:"
  curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"identifier":"testuser","password":"wrong","rememberMe":false}'
  sleep 0.5
done
```

### 3.4 Token 安全测试

**测试目标**：验证 JWT Token 安全性

#### 3.4.1 伪造 Token 测试

**测试 Payload**：
```
fake.token.here
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkZha2UifQ.xxx
eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiIxIn0.
```

**预期结果**：返回 HTTP 401 Unauthorized

#### 3.4.2 篡改 Token 测试

**测试步骤**：
1. 获取有效 Token
2. 修改 Token 的 header（如改为 alg:none）
3. 修改 Token 的 payload（如改变用户ID）
4. 修改 Token 的 signature
5. 验证都被拒绝

**预期结果**：所有篡改的 Token 返回 HTTP 401

#### 3.4.3 过期 Token 测试

**测试步骤**：
1. 获取有效 Token
2. 等待 Token 过期（或修改系统时间）
3. 使用过期 Token 访问接口

**预期结果**：返回 HTTP 401，提示 Token 已过期

### 3.5 敏感信息泄露测试

**测试目标**：验证错误响应不泄露敏感信息

**检查项**：
- 不泄露堆栈跟踪信息
- 不泄露数据库类型和版本
- 不泄露框架和库信息
- 不区分用户是否存在

**测试命令**：
```bash
# 测试不存在的用户
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"nonexistent_user","password":"test","rememberMe":false}'

# 检查响应是否包含敏感信息
# 预期：只返回 "用户名或密码错误"，不区分用户是否存在
```

### 3.6 HTTP 安全头测试

**检查的安全头**：

| 安全头 | 推荐值 | 作用 |
|--------|--------|------|
| X-Content-Type-Options | nosniff | 防止 MIME 类型嗅探 |
| X-Frame-Options | DENY 或 SAMEORIGIN | 防止点击劫持 |
| Cache-Control | no-store | 防止敏感数据缓存 |
| Strict-Transport-Security | max-age=31536000 | 强制 HTTPS |

**测试命令**：
```bash
curl -I http://localhost:8080/api/v1/auth/login
```

## 4. 使用 OWASP ZAP 进行测试

### 4.1 启动 ZAP

```bash
# Docker 方式
docker run -v $(pwd)/doc/04-testing/security:/zap/wrk/:rw \
  -t ghcr.io/zaproxy/zaproxy:stable \
  zap.sh -cmd -autorun /zap/wrk/zap/aiops-auth-scan.yaml

# GUI 方式
zaproxy
```

### 4.2 配置扫描

1. 导入 OpenAPI 规范：
   - File -> Import -> Import OpenAPI Definition
   - URL: http://localhost:8080/v3/api-docs

2. 配置认证：
   - 创建 Context
   - 配置 JSON Body Authentication
   - 设置 Authorization Header

3. 运行扫描：
   - Active Scan -> 选择 Context
   - 配置扫描策略（SQL Injection, XSS 等）

### 4.3 自动化扫描

使用提供的 YAML 配置文件：

```bash
# 运行自动化扫描
docker run -v $(pwd)/doc/04-testing/security:/zap/wrk/:rw \
  -t ghcr.io/zaproxy/zaproxy:stable \
  zap.sh -cmd -autorun /zap/wrk/zap/aiops-auth-scan.yaml

# 查看报告
open doc/04-testing/security/reports/aiops-security-report.html
```

## 5. 测试执行步骤

### 5.1 环境准备

```bash
# 1. 启动应用
cd /path/to/aiops-service
mvn spring-boot:run -pl bootstrap

# 2. 确认应用健康
curl http://localhost:8080/actuator/health

# 3. 创建安全测试用户
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "securitytest",
    "email": "securitytest@test.com",
    "password": "SecureP@ss123"
  }'
```

### 5.2 运行快速测试

```bash
# 运行 Shell 测试脚本
./doc/04-testing/security/scripts/security-test.sh http://localhost:8080
```

### 5.3 运行完整扫描

```bash
# 使用 OWASP ZAP 进行完整扫描
docker run -v $(pwd)/doc/04-testing/security:/zap/wrk/:rw \
  --network host \
  -t ghcr.io/zaproxy/zaproxy:stable \
  zap.sh -cmd -autorun /zap/wrk/zap/aiops-auth-scan.yaml
```

## 6. 结果分析

### 6.1 风险等级

| 等级 | 描述 | 处理要求 |
|------|------|---------|
| High | 高危漏洞 | 必须立即修复 |
| Medium | 中危漏洞 | 尽快修复 |
| Low | 低危漏洞 | 计划修复 |
| Informational | 信息性问题 | 可选修复 |

### 6.2 常见漏洞处理

#### SQL 注入

**修复方案**：
- 使用参数化查询（MyBatis-Plus 默认支持）
- 避免字符串拼接 SQL
- 使用 ORM 框架

#### XSS

**修复方案**：
- 输出时进行 HTML 转义
- 设置 Content-Type: application/json
- 使用 CSP 头

#### 暴力破解

**已实现**：
- 登录失败计数（Redis）
- 账号锁定机制
- 锁定时间限制

#### Token 安全

**已实现**：
- JWT 签名验证
- Token 过期检查
- 会话绑定

## 7. 安全测试报告模板

```markdown
# 安全测试报告

## 测试信息
- 测试日期：2025-11-26
- 测试环境：开发环境 / 测试环境
- 测试工具：OWASP ZAP 2.14 / Shell Script
- 测试人员：xxx

## 测试结果摘要

| 测试项 | 结果 | 风险等级 |
|--------|------|---------|
| SQL 注入 | ✅ 通过 | - |
| XSS 攻击 | ✅ 通过 | - |
| 暴力破解防护 | ✅ 通过 | - |
| Token 安全 | ✅ 通过 | - |
| 信息泄露 | ✅ 通过 | - |
| 安全头 | ⚠️ 部分 | Low |

## 发现的问题

### 问题1：[问题标题]
- **风险等级**：High/Medium/Low
- **描述**：xxx
- **复现步骤**：xxx
- **建议修复**：xxx

## 需求验证

| 需求ID | 描述 | 状态 |
|--------|------|------|
| REQ-NFR-SEC-001 | BCrypt 加密 | ✅ |
| REQ-NFR-SEC-002 | Token 不可伪造 | ✅ |
| REQ-NFR-SEC-003 | 暴力破解防护 | ✅ |
| REQ-NFR-SEC-004 | 不泄露敏感信息 | ✅ |
| REQ-NFR-SEC-005 | SQL 注入防护 | ✅ |
| REQ-NFR-SEC-006 | XSS 防护 | ✅ |

## 附件
- ZAP 扫描报告：reports/aiops-security-report.html
- 测试日志：reports/summary.txt
```

## 8. 最佳实践

### 8.1 安全开发

- 使用参数化查询，避免 SQL 拼接
- 对用户输入进行验证和转义
- 使用安全的密码哈希算法（BCrypt）
- 实现登录失败限制
- 使用 HTTPS 传输
- 设置安全响应头

### 8.2 安全测试

- 在每次发布前运行安全扫描
- 将安全测试集成到 CI/CD
- 定期进行渗透测试
- 关注 OWASP Top 10 漏洞

### 8.3 安全监控

- 监控异常登录行为
- 记录安全审计日志
- 设置安全告警规则
- 定期审查访问日志

---

文档版本：1.0.0
最后更新：2025-11-26
