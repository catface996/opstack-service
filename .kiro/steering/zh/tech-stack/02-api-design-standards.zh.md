---
inclusion: manual
---

# API 设计标准

## 快速参考

| 规则 | 要求 | 优先级 | 说明 |
|------|------|--------|------|
| HTTP API使用OpenAPI | MUST 使用OpenAPI 3.0规范 | P0 | 行业标准，支持工具化 |
| 分页从1开始 | MUST 使用1-based分页 | P0 | 符合用户直觉 |
| 统一错误响应 | MUST 包含code/message/details | P0 | 前端统一处理 |
| 接口仅定义签名 | MUST 不包含实现逻辑 | P0 | 设计与实现分离 |
| 设计技术无关 | MUST 使用标准格式 | P1 | 便于协作和演进 |

## 关键规则 (NON-NEGOTIABLE)

| 规则 | 描述 | 正确示例 | 错误示例 |
|------|------|----------|----------|
| **HTTP API必须用OpenAPI** | 设计阶段使用OpenAPI 3.0规范，不用代码片段 | 提供完整的openapi.yaml文件 | 用Java/Python代码示例表达API |
| **分页页码从1开始** | page=1表示第一页，与用户直觉一致 | `GET /users?page=1&size=10` | `GET /users?page=0&size=10` |
| **内部接口仅签名** | 接口定义只包含方法签名和注释，无实现 | `LoginResult login(LoginRequest);` | 包含业务逻辑的方法体 |
| **统一错误响应格式** | 所有API返回统一结构 | `{code,message,details,timestamp,path}` | 各API使用不同错误格式 |
| **API定义独立文件** | API规范放在api/目录，设计文档引用 | `api/openapi.yaml` + 文档引用 | 在plan.md中嵌入完整API定义 |

## 核心原则

设计阶段的 API 定义应该使用**行业标准格式**，而不是具体的代码实现。

**为什么重要**：
- 设计应该是技术无关的
- 便于不同团队理解和协作
- 支持自动化工具生成代码和文档
- 便于 API 版本管理和演进

## HTTP API 设计

### 必须使用 OpenAPI (Swagger) 规范

HTTP API 的设计**必须**使用 OpenAPI 3.0 规范，这是行业标准。

**❌ 错误做法**：使用代码片段表达 API 设计

**✅ 正确做法**：使用 OpenAPI 规范

**OpenAPI 规范应包含**：
- API 基本信息（title、version、description）
- 路径定义（paths）：每个端点的 HTTP 方法、参数、请求体、响应
- 数据模型（components/schemas）：请求和响应的数据结构
- 错误响应定义
- 认证方式（如适用）

### OpenAPI 规范的优势

1. **标准化**：行业标准，所有人都能理解
2. **工具支持**：可以自动生成代码、文档、测试用例
3. **版本管理**：便于 API 版本演进
4. **契约测试**：可以基于规范进行契约测试
5. **Mock 服务**：可以快速生成 Mock 服务

### OpenAPI 文档组织

在设计文档中，OpenAPI 规范应该：
- 作为独立的 YAML 或 JSON 文件
- 放在 `.kiro/features/{feature-id}/api/` 目录下
- 在设计文档中引用该文件

## GraphQL API 设计

### 使用 GraphQL Schema 定义

GraphQL API 的设计应该使用 GraphQL Schema Definition Language (SDL)。

**✅ 正确做法**：使用 GraphQL Schema

**GraphQL Schema 应包含**：
- 类型定义（type）：对象类型和字段
- 输入类型（input）：变更操作的输入参数
- 枚举类型（enum）：状态、类型等枚举值
- 查询根类型（Query）：查询操作定义
- 变更根类型（Mutation）：变更操作定义
- 标量类型（scalar）：自定义标量类型（如 DateTime）
- 分页和连接类型（如适用）

## 内部接口设计

### 使用接口定义（仅签名）

对于内部模块间的接口，可以使用编程语言的接口定义，但**仅限于签名**，不包含实现。

**✅ 正确做法**：接口定义（仅签名）
- 定义接口名称和方法签名
- 包含方法的参数、返回值、异常声明
- 添加必要的文档注释
- 不包含任何实现逻辑

**❌ 错误做法**：包含实现逻辑

## 分页设计

### 页码从 1 开始（1-based）

所有分页 API **必须**采用 **1-based** 分页，即第一页的页码为 `page=1`。

**为什么采用 1-based**：

| 维度 | 1-based (推荐) | 0-based |
|------|---------------|---------|
| **用户直觉** | ✅ "第一页"传 1，符合自然语言 | ❌ "第一页"传 0，反直觉 |
| **沟通成本** | ✅ 产品说"第3页"，开发传 page=3 | ❌ 需要心算 -1 |
| **URL 可读性** | ✅ `/users?page=1` 直观 | ❌ `/users?page=0` 奇怪 |
| **与 UI 一致** | ✅ 页码显示与参数一致 | ❌ 显示"第1页"但传 0 |
| **MyBatis-Plus** | ✅ 原生支持 | ❌ 需要 +1 转换 |

**✅ 正确做法**：

```java
// Controller 层：页码从 1 开始，默认值为 1
@GetMapping("/users")
public ResponseEntity<PageResult<UserDTO>> getUsers(
        @RequestParam(defaultValue = "1") int page,   // 从 1 开始
        @RequestParam(defaultValue = "10") int size) {
    // ...
}

// 调用示例
// GET /api/v1/users?page=1&size=10  → 第一页
// GET /api/v1/users?page=2&size=10  → 第二页
```

**❌ 错误做法**：

```java
// 不要使用 0-based 分页
@RequestParam(defaultValue = "0") int page  // 错误：从 0 开始
```

### 分页参数命名规范

| 参数名 | 含义 | 默认值 | 说明 |
|-------|------|-------|------|
| `page` | 页码 | 1 | 从 1 开始，表示第几页 |
| `size` | 每页大小 | 10 | 单页返回的记录数 |

### 分页响应格式

分页响应应包含以下字段：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "content": [...],        // 当前页数据列表
    "page": 1,               // 当前页码（从 1 开始）
    "size": 10,              // 每页大小
    "totalElements": 100,    // 总记录数
    "totalPages": 10,        // 总页数
    "first": true,           // 是否第一页
    "last": false            // 是否最后一页
  }
}
```

### 边界处理

- `page < 1`：返回第一页数据，或返回 400 错误
- `page > totalPages`：返回空列表
- `size` 应设置上限（如最大 100），防止一次查询过多数据

## 错误码设计

### 统一的错误响应格式

所有 API 应该使用统一的错误响应格式。

**错误响应应包含**：
- 错误码（code）：机器可读的错误标识
- 错误信息（message）：人类可读的错误描述
- 详细信息（details）：可选的详细错误信息数组
- 时间戳（timestamp）：错误发生时间
- 请求路径（path）：发生错误的请求路径

### 错误码定义原则

使用表格定义错误码，应包含：
- 错误码：唯一标识符
- HTTP 状态码：对应的 HTTP 状态码
- 说明：错误的含义
- 示例消息：典型的错误消息

## 设计文档中的 API 定义

### 文件组织原则

API 定义文件应该独立于设计文档，按类型组织：
- HTTP API：放在 `api/` 目录下，使用 `openapi.yaml` 文件
- GraphQL API：放在 `graphql/` 目录下，使用 `schema.graphql` 文件
- 错误码定义：放在 `api/` 目录下，使用 `errors.md` 文件

### 在设计文档中引用

在设计文档（`plan.md`）中应该：
- 引用独立的 API 定义文件
- 列出核心端点或服务的摘要
- 说明 API 的主要功能和用途
- 不在设计文档中重复完整的 API 定义

## 检查清单

在完成 API 设计后，确认：

- [ ] HTTP API 使用了 OpenAPI 规范（不是代码片段）
- [ ] GraphQL API 使用了 Schema 定义（如适用）
- [ ] 内部接口仅定义签名，不包含实现
- [ ] 所有 API 都有清晰的文档说明
- [ ] 错误响应格式统一
- [ ] 错误码定义完整
- [ ] API 定义文件已放在正确的目录
- [ ] 设计文档中正确引用了 API 定义文件
- [ ] 分页 API 采用 1-based 分页（page 从 1 开始）
- [ ] 分页响应包含完整的分页信息（page、size、totalElements、totalPages）

## 总结

API 设计的核心原则：

1. **使用行业标准格式**：OpenAPI、GraphQL Schema
2. **设计与实现分离**：设计阶段不写实现代码
3. **技术无关性**：设计应该独立于具体实现技术
4. **文档化**：完整的 API 文档和错误码定义
5. **可工具化**：支持自动生成代码和文档

遵循这些原则可以：
- ✅ 提高 API 设计质量
- ✅ 便于团队协作和理解
- ✅ 支持自动化工具
- ✅ 便于 API 版本管理
- ✅ 降低沟通成本
