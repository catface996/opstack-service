# Quickstart: Refactor Executor Integration

**Feature**: 042-refactor-executor-integration
**Date**: 2025-12-30
**Status**: Complete

## Prerequisites

1. MySQL 8.0 运行中（端口 3306）
2. op-stack-executor 服务运行中（端口 8082）
3. 已有 Topology、Node、Agent、AgentBound、PromptTemplate 测试数据

## Scenario 1: 基本层级结构转换

### 准备测试数据

```sql
-- 1. 创建 PromptTemplate
INSERT INTO prompt_template (id, name, description, current_version, deleted)
VALUES (1, '全局监管者模板', '负责协调所有团队', 1, 0);

INSERT INTO prompt_template_version (template_id, version_number, content)
VALUES (1, 1, '你是全球运营总监。你的职责是：
1. 协调各团队的工作
2. 分配任务给合适的团队
3. 汇总各团队的工作成果
4. 做出最终决策');

-- 2. 创建关联 PromptTemplate 的 Agent
INSERT INTO agent (id, name, role, hierarchy_level, specialty, prompt_template_id, model, deleted)
VALUES (1, 'Global Chief', 'ADMINISTRATOR', 'GLOBAL_SUPERVISOR', '全局协调', 1, 'claude-3-opus', 0);

-- 3. 创建绑定关系
INSERT INTO agent_bound (id, agent_id, hierarchy_level, entity_id, entity_type, deleted)
VALUES (100, 1, 'GLOBAL_SUPERVISOR', 1, 'TOPOLOGY', 0);  -- entity_id=1 是 Topology ID
```

### 测试转换

```bash
# 查询层级结构
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/query-hierarchy \
  -H "Content-Type: application/json" \
  -d '{"topologyId": 1}'
```

**期望响应（重构后）**:
```json
{
  "code": 0,
  "data": {
    "topologyId": 1,
    "topologyName": "生产环境",
    "globalSupervisor": {
      "id": 1,
      "boundId": 100,
      "name": "Global Chief",
      "promptTemplateContent": "你是全球运营总监..."
    },
    "teams": [...]
  }
}
```

### 验证 Executor 请求格式

重构后发送到 Executor 的请求应为：

```json
{
  "name": "生产环境_1735590000000",
  "global_supervisor_agent": {
    "agent_id": "100",
    "system_prompt": "你是全球运营总监。你的职责是：\n1. 协调各团队的工作\n2. 分配任务给合适的团队..."
  },
  "teams": [...]
}
```

**关键验证点**：
- `agent_id` = `"100"` (绑定关系 ID，非 Agent ID)
- `system_prompt` 来自 PromptTemplate content

---

## Scenario 2: 默认提示词回退

### 准备测试数据

```sql
-- Agent 未关联 PromptTemplate
INSERT INTO agent (id, name, role, hierarchy_level, specialty, prompt_template_id, model, deleted)
VALUES (2, 'Team Leader', 'WORKER', 'TEAM_SUPERVISOR', '团队管理和任务分配', NULL, 'gemini-2.0-flash', 0);

INSERT INTO agent_bound (id, agent_id, hierarchy_level, entity_id, entity_type, deleted)
VALUES (101, 2, 'TEAM_SUPERVISOR', 10, 'NODE', 0);  -- entity_id=10 是 Node ID
```

### 验证默认提示词

重构后发送到 Executor 的请求：

```json
{
  "teams": [{
    "name": "分析组",
    "team_supervisor_agent": {
      "agent_id": "101",
      "system_prompt": "You are Team Leader. 团队管理和任务分配"
    },
    "workers": [...]
  }]
}
```

**关键验证点**：
- `system_prompt` 使用默认格式：`"You are {name}. {specialty}"`

---

## Scenario 3: 事件追溯

### 启动任务并监听事件

```bash
# 1. 启动任务
curl -X POST http://localhost:8081/api/service/v1/executions/start \
  -H "Content-Type: application/json" \
  -d '{"topologyId": 1, "task": "分析系统性能"}'

# 2. 监听事件流（示例使用 curl）
curl -X POST http://localhost:8082/api/executor/v1/runs/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"id": "<run_id>"}'
```

### 验证事件追溯

收到的事件示例：
```
event: llm.stream
data: {"run_id":"abc","source":{"agent_id":"100","agent_type":"global_supervisor",...},...}
```

通过 `agent_id=100` 追溯绑定关系：
```bash
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/get \
  -H "Content-Type: application/json" \
  -d '{"id": 100}'
```

**期望响应**：
```json
{
  "code": 0,
  "data": {
    "id": 100,
    "agentId": 1,
    "agentName": "Global Chief",
    "hierarchyLevel": "GLOBAL_SUPERVISOR",
    "entityId": 1,
    "entityType": "TOPOLOGY",
    "entityName": "生产环境"
  }
}
```

---

## Scenario 4: Executor 服务失败处理

### 模拟 Executor 不可用

```bash
# 停止 Executor 服务
docker stop op-stack-executor

# 尝试启动任务
curl -X POST http://localhost:8081/api/service/v1/executions/start \
  -H "Content-Type: application/json" \
  -d '{"topologyId": 1, "task": "分析系统性能"}'
```

**期望响应**（快速失败）：
```json
{
  "code": 50001,
  "message": "Executor 服务调用失败: Connection refused",
  "success": false
}
```

**关键验证点**：
- 立即返回错误（不重试）
- 错误信息清晰描述原因

---

## Test Checklist

| # | 测试场景 | 验证点 | 状态 |
|---|---------|--------|------|
| 1 | 基本层级转换 | agent_id 使用绑定关系 ID | [ ] |
| 2 | PromptTemplate 获取 | system_prompt 来自 content | [ ] |
| 3 | 默认提示词 | 无关联模板时使用默认生成 | [ ] |
| 4 | 空 content 回退 | content 为空时使用默认生成 | [ ] |
| 5 | 事件追溯 | agent_id 可追溯到绑定关系 | [ ] |
| 6 | 快速失败 | Executor 错误时立即返回 | [ ] |
| 7 | 多 Team 支持 | 多个 Team 的转换正确 | [ ] |
| 8 | 多 Worker 支持 | 多个 Worker 的转换正确 | [ ] |

---

## Development Commands

```bash
# 构建
mvn clean package -DskipTests

# 运行服务
java -jar bootstrap/target/aiops-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 运行单元测试
mvn test -pl application/application-impl -Dtest=HierarchyTransformerTest

# 终止服务（按宪法规定使用端口方式）
lsof -ti :8081 | xargs kill 2>/dev/null || echo "No process on port 8081"
```
