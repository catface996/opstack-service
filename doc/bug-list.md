# Bug 清单 - AIOps Service

**项目名称**: AIOps Service
**文档版本**: v1.0.0
**创建日期**: 2025-11-26
**最后更新**: 2025-12-04

---

## Bug 状态说明

| 状态 | 说明 |
|-----|------|
| 🔴 NEW | 新发现，待处理 |
| 🟡 IN_PROGRESS | 修复中 |
| 🟢 FIXED | 已修复，待验证 |
| ✅ VERIFIED | 已验证通过 |
| ⬜ WONTFIX | 不修复 |

## Bug 优先级说明

| 优先级 | 说明 |
|-------|------|
| P0 | 致命 - 系统无法使用 |
| P1 | 严重 - 核心功能不可用 |
| P2 | 一般 - 功能受影响但有替代方案 |
| P3 | 轻微 - 不影响主要功能 |

---

## Bug 列表

### BUG-001: 账号锁定检查逻辑错误导致所有账号无法登录

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P0 |
| **模块** | 用户认证 (username-password-login) |
| **发现日期** | 2025-11-26 |
| **修复日期** | 2025-11-26 |

**问题描述**: `AuthApplicationServiceImpl.checkAccountNotLocked()` 方法中的锁定检查逻辑有误，导致所有账号都被误判为已锁定。

**修复方案**: 修改判断逻辑为 `lockInfo.isPresent() && lockInfo.get().isLocked()`

**影响文件**: `application/application-impl/.../AuthApplicationServiceImpl.java:343-351`

---

### BUG-002: 会话验证使用临时sessionId导致验证失败

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P1 |
| **模块** | 用户认证 (username-password-login) |
| **发现日期** | 2025-11-26 |
| **修复日期** | 2025-11-26 |

**问题描述**: 会话验证和登出操作时，系统使用硬编码的 `temp-session-id` 而非从 JWT Token 中提取的实际 sessionId。

**修复方案**: 在 JWT Token 中添加 sessionId claim，通过 Domain API 暴露解析功能。

**影响文件**:
- `domain/security-api/.../JwtTokenProvider.java`
- `infrastructure/security/jwt-impl/.../JwtTokenProviderImpl.java`
- `application/application-impl/.../AuthApplicationServiceImpl.java`

---

### BUG-003: 密码强度验证拒绝包含连续数字的密码

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P3 |
| **模块** | 用户认证 (username-password-login) |
| **发现日期** | 2025-11-26 |
| **修复日期** | 2025-11-26 |

**问题描述**: 密码强度验证过于严格，包含任何4位连续数字的密码都会被拒绝。

**修复方案**: 将连续字符检测阈值从4位提高到6位。

**影响文件**: `domain/domain-impl/.../AuthDomainServiceImpl.java`

---

### BUG-004: /health 端点需要认证但应该公开访问

| 属性 | 值 |
|-----|-----|
| **状态** | 🔴 NEW |
| **优先级** | P2 |
| **模块** | 安全配置 |
| **发现日期** | 2025-11-26 |
| **修复日期** | - |

**问题描述**: `/health` 端点在访问时需要认证，但应该是公开访问的。

**修复方案**: 在 `SecurityConfig.java` 中添加 `/health` 到公开接口列表。

**影响文件**: `bootstrap/.../SecurityConfig.java`

---

### BUG-005: 权限不足返回401而非403

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P2 |
| **模块** | 资源关系管理 (f04-resource-relationships) |
| **发现日期** | 2025-12-04 |
| **修复日期** | 2025-12-04 |

**问题描述**:

当用户已认证但没有权限操作某个资源时（如删除不属于自己的资源关系），系统返回 HTTP 401 状态码，但根据 HTTP 标准应该返回 403：

- **401 Unauthorized**: 未认证（需要登录）
- **403 Forbidden**: 已认证但权限不足

**复现步骤**:

```bash
# 用户 testuser001 尝试删除不属于自己的关系
curl -X DELETE http://localhost:8080/api/v1/relationships/2 \
  -H "Authorization: Bearer <valid_token>"

# 返回 401 (错误)
{"code":401002,"message":"您没有权限执行此操作","data":null,"success":false}
```

**根本原因**:

`ResourceErrorCode.FORBIDDEN` 使用的错误码是 `AUTH_002`，而全局异常处理器 `GlobalExceptionHandler` 根据错误码前缀映射 HTTP 状态码：
- `AUTH_` 前缀 → 401 Unauthorized
- `AUTHZ_` 前缀 → 403 Forbidden

**修复方案**:

将 `ResourceErrorCode.FORBIDDEN` 的错误码从 `AUTH_002` 改为 `AUTHZ_001`：

```java
// 修复前
FORBIDDEN("AUTH_002", "您没有权限执行此操作"),

// 修复后
FORBIDDEN("AUTHZ_001", "您没有权限执行此操作"),
```

**验证结果**:

```bash
# 修复后返回 403 (正确)
curl -X DELETE http://localhost:8080/api/v1/relationships/2 \
  -H "Authorization: Bearer <valid_token>"

{"code":403001,"message":"您没有权限执行此操作","data":null,"success":false}
# HTTP Status: 403
```

**影响文件**: `common/src/main/java/com/catface996/aiops/common/enums/ResourceErrorCode.java`

---

### BUG-006: 创建关系时要求对源和目标资源都有权限

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P2 |
| **模块** | 资源关系管理 (f04-resource-relationships) |
| **发现日期** | 2025-12-04 |
| **修复日期** | 2025-12-04 |

**问题描述**:

创建关系时，系统要求用户对源资源和目标资源都有权限才能创建关系。但根据实际业务场景，用户只需要拥有**源资源**的权限即可。

例如：用户维护的业务应用系统（源资源）可以依赖 DBA 维护的数据库（目标资源），这是一个很自然的场景。

**复现步骤**:

```bash
# 用户 testuser001 尝试创建关系：
# 源资源 ID=4 (属于其他用户)
# 目标资源 ID=72 (属于 testuser001)
curl -X POST http://localhost:8080/api/v1/relationships \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"sourceResourceId":4,"targetResourceId":72,"relationshipType":"DEPENDENCY","direction":"UNIDIRECTIONAL","strength":"STRONG"}'

# 返回 403 Forbidden
```

**根本原因**:

`RelationshipDomainServiceImpl.createRelationship()` 中的权限检查逻辑：

```java
// 错误：要求对两个资源之一有权限
if (!hasSourceAccess && !hasTargetAccess) {
    throw new BusinessException(ResourceErrorCode.FORBIDDEN);
}
```

**修复方案**:

修改为只检查源资源的权限：

```java
// 正确：只需要对源资源有权限
if (!sourceResource.isOwner(operatorId)) {
    throw new BusinessException(ResourceErrorCode.FORBIDDEN);
}
```

同时修改 `updateRelationship()` 和 `deleteRelationship()` 方法，统一只检查源资源权限。

**影响文件**: `domain/domain-impl/.../RelationshipDomainServiceImpl.java`

---

## Bug 统计

| 状态 | 数量 |
|-----|------|
| 🔴 NEW | 1 |
| 🟡 IN_PROGRESS | 0 |
| 🟢 FIXED | 0 |
| ✅ VERIFIED | 5 |
| **总计** | **6** |

| 优先级 | 数量 |
|-------|------|
| P0 (致命) | 1 (已修复) |
| P1 (严重) | 1 (已修复) |
| P2 (一般) | 3 (1待修复, 2已修复) |
| P3 (轻微) | 1 (已修复) |

---

## 修复历史

| 日期 | Bug ID | 操作 | 说明 |
|-----|--------|------|------|
| 2025-11-26 | BUG-001 | 修复+验证 | 修改 checkAccountNotLocked() 判断逻辑 |
| 2025-11-26 | BUG-002 | 修复+验证 | JWT Token 添加 sessionId claim |
| 2025-11-26 | BUG-003 | 修复+验证 | 调整密码强度检测阈值 |
| 2025-11-26 | BUG-004 | 新建 | /health 端点认证问题 |
| 2025-12-04 | BUG-005 | 修复+验证 | 权限不足返回 403 而非 401 |
| 2025-12-04 | BUG-006 | 修复+验证 | 关系权限检查改为只检查源资源 |

---

## 下一步行动

- [ ] 修复 BUG-004: 在 SecurityConfig 中添加 /health 到公开接口列表

---

**文档维护人**: AI Assistant
**最后更新**: 2025-12-04
