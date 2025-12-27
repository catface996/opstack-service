<!--
Sync Impact Report
==================
Version change: 1.0.0 → 1.1.0 (MINOR: Added Pagination Protocol principle)

Modified principles: None

Added sections:
- VI. Pagination Protocol (新增分页协议规范)

Removed sections: None

Templates requiring updates:
- /.specify/templates/plan-template.md: ✅ No updates required (generic)
- /.specify/templates/spec-template.md: ✅ No updates required (generic)
- /.specify/templates/tasks-template.md: ✅ No updates required (generic)

Follow-up TODOs: None
-->

# OP-Stack Service Constitution

## Core Principles

### I. DDD Architecture

本项目采用领域驱动设计（DDD）分层架构，代码组织 MUST 遵循以下层级：

- **bootstrap**: 应用启动层，负责配置和依赖注入
- **interface**: 接口层，负责 HTTP 控制器和请求/响应定义
- **application**: 应用层，负责用例编排和 DTO 转换
- **domain**: 领域层，负责业务逻辑和领域模型
- **infrastructure**: 基础设施层，负责数据持久化和外部服务集成

层级依赖 MUST 遵循：上层可依赖下层，下层 MUST NOT 依赖上层。

### II. API URL Convention

所有业务相关的 HTTP 接口 MUST 遵循以下 URL 规范：

- **业务接口路径格式**: `/api/service/{version}/{resource}/{action}`
  - 示例: `/api/service/v1/nodes/query`
  - 示例: `/api/service/v1/topologies/create`
  - 示例: `/api/service/v1/relationships/delete`

- **版本号规范**: 使用 `v1`, `v2` 等形式，当前版本为 `v1`

- **排除的路径**（不使用 `/api/service/` 前缀）:
  - 健康检查: `/health`, `/actuator/*`
  - API 文档: `/swagger-ui/*`, `/v3/api-docs`
  - 测试端点: `/test/*`

- **HTTP 方法**: 所有业务接口统一使用 POST 方法（POST-Only API），便于网关参数注入

### III. POST-Only API Design

所有业务接口 MUST 使用 POST 方法，遵循以下规范：

- 请求参数通过 JSON Body 传递
- 响应格式统一为 `Result<T>` 结构: `{code, message, data, success}`
- 分页接口使用 `PageResult<T>` 结构
- 所有接口 MUST 使用 SpringDoc OpenAPI 注解生成文档

### IV. Database Migration

数据库变更 MUST 通过 Flyway 迁移脚本管理：

- 迁移文件位置: `src/main/resources/db/migration/`
- 命名格式: `V{version}__{description}.sql`
- 禁止直接修改数据库 schema，所有变更 MUST 有对应的迁移脚本
- 迁移脚本 MUST 支持回滚

### V. Technology Stack

本项目技术栈 MUST 遵循以下版本要求：

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.x
- **MyBatis-Plus**: 3.5.x
- **MySQL**: 8.0
- **SpringDoc OpenAPI**: 用于 API 文档生成

### VI. Pagination Protocol

所有分页接口 MUST 遵循统一的分页协议：

#### 分页请求参数

分页请求 MUST 继承 `PageableRequest` 基类，包含以下标准字段：

```json
{
  "page": 1,          // 页码（从 1 开始），默认 1，最小 1
  "size": 20,         // 每页大小，默认 20，范围 1-100
  "tenantId": null,   // 租户ID（网关注入，hidden）
  "traceId": null,    // 追踪ID（网关注入，hidden）
  "userId": null      // 用户ID（网关注入，hidden）
}
```

- `page` MUST 从 1 开始计数，最小值为 1
- `size` MUST 限制在 1-100 范围内，默认值为 20
- 网关注入字段（tenantId, traceId, userId）在 Swagger 文档中 MUST 设置为 hidden

#### 分页响应结果

分页响应 MUST 使用 `PageResult<T>` 结构：

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "content": [],           // 数据列表
    "page": 1,               // 当前页码（从1开始）
    "size": 10,              // 每页大小
    "totalElements": 100,    // 总记录数
    "totalPages": 10,        // 总页数
    "first": true,           // 是否为第一页
    "last": false            // 是否为最后一页
  }
}
```

- `content` MUST 包含当前页的数据列表
- `totalPages` MUST 根据 `totalElements` 和 `size` 自动计算
- `first` 和 `last` MUST 正确标识边界状态

## API Design Standards

### Request/Response 规范

- 请求类命名: `{Action}{Resource}Request`，如 `CreateNodeRequest`
- 响应类命名: `{Resource}DTO`，如 `NodeDTO`
- 控制器类命名: `{Resource}Controller`，如 `NodeController`

### Controller 注解规范

每个 Controller MUST 包含以下注解：

```java
@Slf4j
@RestController
@RequestMapping("/api/service/v1/{resource}")
@RequiredArgsConstructor
@Tag(name = "资源描述", description = "接口描述（POST-Only API）")
```

### Swagger 文档规范

每个接口 MUST 包含：

- `@Operation`: 接口说明和描述
- `@ApiResponses`: 响应状态码说明
- `@Valid`: 请求参数校验

## Development Workflow

### 构建与运行

```bash
# 构建
mvn clean package -DskipTests

# 运行（推荐）
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 测试
mvn test
```

### 代码提交

- 提交前 MUST 确保代码编译通过
- 提交前 SHOULD 运行相关单元测试
- 提交信息 MUST 遵循 Conventional Commits 规范

## Governance

本宪法规定了 OP-Stack Service 项目的核心原则和规范，所有开发活动 MUST 遵循。

### 修订流程

1. 修订提案 MUST 以文档形式提交
2. 修订 MUST 包含变更原因和影响分析
3. 重大变更（MAJOR）需要团队评审
4. 修订后 MUST 更新版本号和日期

### 版本规则

- **MAJOR**: 原则删除或重新定义（不兼容变更）
- **MINOR**: 新增原则或实质性扩展
- **PATCH**: 澄清、措辞修正、非语义性改进

### 合规检查

- 代码评审 MUST 验证是否符合宪法原则
- 新功能 MUST 遵循 API URL Convention
- 数据库变更 MUST 遵循 Database Migration 原则
- 分页接口 MUST 遵循 Pagination Protocol

**Version**: 1.1.0 | **Ratified**: 2025-12-27 | **Last Amended**: 2025-12-27
