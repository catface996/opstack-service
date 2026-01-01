# Data Model: Refactor Executor Integration

**Feature**: 042-refactor-executor-integration
**Date**: 2025-12-30
**Status**: Complete

## Overview

本功能不涉及数据库 schema 变更，仅涉及 DTO 扩展和数据流调整。

## Existing Entities (No Changes)

### AgentBound

```
┌─────────────────────────────────────────────────────────┐
│ agent_bound                                             │
├─────────────────────────────────────────────────────────┤
│ id              BIGINT      PK    绑定关系 ID           │
│ agent_id        BIGINT      FK    → agent.id            │
│ hierarchy_level VARCHAR(32)       层级                  │
│ entity_id       BIGINT            绑定实体 ID           │
│ entity_type     VARCHAR(20)       实体类型              │
│ created_at      DATETIME          创建时间              │
│ deleted         TINYINT           软删除标记            │
└─────────────────────────────────────────────────────────┘

关键：id 将作为 Executor API 的 agent_id
```

### Agent

```
┌─────────────────────────────────────────────────────────┐
│ agent                                                   │
├─────────────────────────────────────────────────────────┤
│ id                  BIGINT      PK    Agent ID          │
│ name                VARCHAR(100)      名称              │
│ role                VARCHAR(32)       角色              │
│ hierarchy_level     VARCHAR(32)       层级              │
│ specialty           VARCHAR(500)      专业领域          │
│ prompt_template_id  BIGINT      FK    → prompt_template │
│ model               VARCHAR(50)       模型标识          │
│ ...                                                     │
└─────────────────────────────────────────────────────────┘

关键：prompt_template_id 关联提示词模板
```

### PromptTemplate

```
┌─────────────────────────────────────────────────────────┐
│ prompt_template                                         │
├─────────────────────────────────────────────────────────┤
│ id               BIGINT      PK    模板 ID              │
│ name             VARCHAR(100)      模板名称             │
│ usage_id         BIGINT            用途 ID              │
│ description      VARCHAR(500)      描述                 │
│ current_version  INT               当前版本号           │
│ ...                                                     │
└─────────────────────────────────────────────────────────┘
```

### PromptTemplateVersion

```
┌─────────────────────────────────────────────────────────┐
│ prompt_template_version                                 │
├─────────────────────────────────────────────────────────┤
│ id               BIGINT      PK    版本 ID              │
│ template_id      BIGINT      FK    → prompt_template    │
│ version_number   INT               版本号               │
│ content          TEXT              提示词内容           │
│ ...                                                     │
└─────────────────────────────────────────────────────────┘

关键：content 字段存储实际的 system_prompt
```

## DTO Extensions

### AgentDTO (扩展)

```java
@Data
@Builder
@Schema(description = "Agent 信息")
public class AgentDTO {
    // === 现有字段 ===
    private Long id;
    private String name;
    private String role;
    private String hierarchyLevel;
    private String specialty;
    private Long promptTemplateId;
    private String promptTemplateName;
    private String model;
    // ...

    // === 新增字段 ===
    @Schema(description = "绑定关系 ID（作为 Executor 的 agent_id）")
    private Long boundId;

    @Schema(description = "提示词模板内容（system_prompt 来源）")
    private String promptTemplateContent;
}
```

### CreateHierarchyRequest (重构)

```java
@Data
@Builder
public class CreateHierarchyRequest {
    private String name;

    @JsonProperty("global_supervisor_agent")
    private SupervisorAgentConfig globalSupervisorAgent;  // 新结构

    private List<TeamConfig> teams;

    @Data
    @Builder
    public static class SupervisorAgentConfig {
        @JsonProperty("agent_id")
        private String agentId;           // 绑定关系 ID

        @JsonProperty("system_prompt")
        private String systemPrompt;       // 来自 PromptTemplate
    }

    @Data
    @Builder
    public static class TeamConfig {
        private String name;

        @JsonProperty("team_supervisor_agent")
        private SupervisorAgentConfig teamSupervisorAgent;  // 新结构

        private List<WorkerConfig> workers;
    }

    @Data
    @Builder
    public static class WorkerConfig {
        @JsonProperty("agent_id")
        private String agentId;            // 新增：绑定关系 ID

        private String name;
        private String role;

        @JsonProperty("system_prompt")
        private String systemPrompt;

        private String model;
    }
}
```

## Data Flow Diagram

```
┌─────────────────┐
│   Topology      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐
│   AgentBound    │────▶│     Agent       │
│   (绑定关系)     │     │   (Agent 实体)  │
├─────────────────┤     ├─────────────────┤
│ id ─────────────│─┐   │ promptTemplateId│───┐
│ agentId ────────│─┘   │ specialty       │   │
│ hierarchyLevel  │     │ model           │   │
│ entityId        │     └─────────────────┘   │
└─────────────────┘                           │
                                              ▼
                                   ┌─────────────────┐
                                   │ PromptTemplate  │
                                   ├─────────────────┤
                                   │ content ────────│──▶ system_prompt
                                   └─────────────────┘

转换输出:
┌─────────────────────────────────────────┐
│ CreateHierarchyRequest                  │
├─────────────────────────────────────────┤
│ global_supervisor_agent:                │
│   agent_id: agent_bound.id (String)     │◀── 绑定关系 ID
│   system_prompt: promptTemplate.content │◀── PromptTemplate 内容
├─────────────────────────────────────────┤
│ teams[]:                                │
│   team_supervisor_agent:                │
│     agent_id: agent_bound.id            │
│     system_prompt: ...                  │
│   workers[]:                            │
│     agent_id: agent_bound.id            │
│     system_prompt: ...                  │
└─────────────────────────────────────────┘
```

## Repository Extensions

### PromptTemplateRepository (新增方法)

```java
/**
 * 批量查询模板（含当前版本内容）
 *
 * @param ids 模板 ID 列表
 * @return 模板列表（含 content）
 */
List<PromptTemplate> findByIdsWithDetail(List<Long> ids);
```

### SQL 实现

```sql
-- 批量查询 PromptTemplate 含 content
SELECT
    pt.id,
    pt.name,
    pt.usage_id,
    pt.description,
    pt.current_version,
    ptv.content
FROM prompt_template pt
LEFT JOIN prompt_template_version ptv
    ON pt.id = ptv.template_id
    AND pt.current_version = ptv.version_number
WHERE pt.id IN (#{ids})
    AND pt.deleted = 0
```

## Default Prompt Generation

当 Agent 未关联 PromptTemplate 或 content 为空时，使用默认生成逻辑：

```java
/**
 * 生成默认系统提示词
 *
 * @param agentName Agent 名称
 * @param specialty Agent 专业领域
 * @return 默认提示词
 */
public static String generateDefaultPrompt(String agentName, String specialty) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are ").append(agentName).append(". ");

    if (specialty != null && !specialty.isEmpty()) {
        prompt.append(specialty);
    } else {
        prompt.append("Complete assigned tasks efficiently.");
    }

    return prompt.toString();
}
```

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| boundId | 必须非空 | "绑定关系 ID 不能为空" |
| agentId (Executor) | 字符串格式 | - |
| systemPrompt | 最大 64KB | "提示词内容超过最大长度" |

## Migration Notes

**无数据库迁移**：本功能不涉及数据库 schema 变更。

**代码变更**：
1. DTO 扩展（AgentDTO 添加字段）
2. Request 重构（CreateHierarchyRequest 结构调整）
3. Repository 扩展（批量查询方法）
4. 转换器重构（HierarchyTransformer 逻辑调整）
