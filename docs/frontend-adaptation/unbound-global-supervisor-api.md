# 查询未绑定 Global Supervisor Agent API 前端集成指南

## 概述

本文档描述了新增的"查询拓扑图未绑定的 Global Supervisor Agent"接口，用于在绑定 Global Supervisor 时提供可选择的 Agent 列表。

## API 详情

### 接口信息

| 属性 | 值 |
|------|-----|
| 路径 | `POST /api/service/v1/topologies/agents/unbound` |
| 方法 | POST |
| Content-Type | application/json |
| 认证 | Bearer Token |

### 请求参数

```typescript
interface QueryUnboundAgentsRequest {
  /** 拓扑图ID（必填） */
  topologyId: number;

  /** 搜索关键词（可选，模糊匹配 Agent 名称和专业领域） */
  keyword?: string;

  /** 页码，从 1 开始（默认 1） */
  page?: number;

  /** 每页大小，1-100（默认 20） */
  size?: number;
}
```

### 响应结构

```typescript
interface Response {
  code: number;        // 0 表示成功
  message: string;
  success: boolean;
  data: PageResult<UnboundAgentDTO>;
}

interface PageResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

interface UnboundAgentDTO {
  /** Agent ID */
  id: number;

  /** Agent 名称 */
  name: string;

  /** 专业领域 */
  specialty: string | null;

  /** AI 模型标识 */
  model: string | null;
}
```

### 请求示例

```bash
curl -X POST 'http://localhost:8081/api/service/v1/topologies/agents/unbound' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "topologyId": 4,
    "keyword": "",
    "page": 1,
    "size": 20
  }'
```

### 响应示例

#### 成功响应

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 30,
        "name": "Global Supervisor Alpha",
        "specialty": "全局协调与决策",
        "model": "gemini-2.0-flash"
      },
      {
        "id": 31,
        "name": "Global Supervisor Beta",
        "specialty": "跨团队资源调度",
        "model": "gemini-2.0-flash"
      },
      {
        "id": 32,
        "name": "Global Supervisor Gamma",
        "specialty": "系统级监控分析",
        "model": "gemini-2.0-flash"
      },
      {
        "id": 33,
        "name": "Global Supervisor Delta",
        "specialty": "紧急事件响应",
        "model": "gemini-2.0-flash"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 4,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "success": true
}
```

#### 空结果响应

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "content": [],
    "page": 1,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  },
  "success": true
}
```

## 前端集成示例

### TypeScript/React 示例

```typescript
// api/topology.ts
import { request } from '@/utils/request';

export interface UnboundAgentDTO {
  id: number;
  name: string;
  specialty: string | null;
  model: string | null;
}

export interface QueryUnboundAgentsParams {
  topologyId: number;
  keyword?: string;
  page?: number;
  size?: number;
}

export async function queryUnboundGlobalSupervisors(
  params: QueryUnboundAgentsParams
): Promise<PageResult<UnboundAgentDTO>> {
  const response = await request.post(
    '/api/service/v1/topologies/agents/unbound',
    {
      topologyId: params.topologyId,
      keyword: params.keyword || '',
      page: params.page || 1,
      size: params.size || 20,
    }
  );
  return response.data;
}
```

### React 组件示例

```tsx
// components/GlobalSupervisorSelector.tsx
import React, { useState, useEffect } from 'react';
import { Select, Input, Spin } from 'antd';
import { queryUnboundGlobalSupervisors, UnboundAgentDTO } from '@/api/topology';

interface Props {
  topologyId: number;
  value?: number;
  onChange?: (agentId: number) => void;
}

export const GlobalSupervisorSelector: React.FC<Props> = ({
  topologyId,
  value,
  onChange,
}) => {
  const [loading, setLoading] = useState(false);
  const [agents, setAgents] = useState<UnboundAgentDTO[]>([]);
  const [keyword, setKeyword] = useState('');

  const loadAgents = async (searchKeyword?: string) => {
    setLoading(true);
    try {
      const result = await queryUnboundGlobalSupervisors({
        topologyId,
        keyword: searchKeyword,
        page: 1,
        size: 100,
      });
      setAgents(result.content);
    } catch (error) {
      console.error('Failed to load agents:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAgents();
  }, [topologyId]);

  const handleSearch = (value: string) => {
    setKeyword(value);
    loadAgents(value);
  };

  return (
    <Select
      showSearch
      value={value}
      onChange={onChange}
      onSearch={handleSearch}
      filterOption={false}
      loading={loading}
      placeholder="选择 Global Supervisor"
      notFoundContent={loading ? <Spin size="small" /> : '暂无可用的 Agent'}
      style={{ width: '100%' }}
    >
      {agents.map((agent) => (
        <Select.Option key={agent.id} value={agent.id}>
          <div>
            <span style={{ fontWeight: 500 }}>{agent.name}</span>
            {agent.specialty && (
              <span style={{ color: '#999', marginLeft: 8 }}>
                {agent.specialty}
              </span>
            )}
          </div>
        </Select.Option>
      ))}
    </Select>
  );
};
```

### Vue 3 示例

```vue
<template>
  <a-select
    v-model:value="selectedAgent"
    show-search
    :filter-option="false"
    :loading="loading"
    placeholder="选择 Global Supervisor"
    @search="handleSearch"
    @change="handleChange"
  >
    <a-select-option v-for="agent in agents" :key="agent.id" :value="agent.id">
      <span class="agent-name">{{ agent.name }}</span>
      <span v-if="agent.specialty" class="agent-specialty">
        {{ agent.specialty }}
      </span>
    </a-select-option>
  </a-select>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { queryUnboundGlobalSupervisors, UnboundAgentDTO } from '@/api/topology';

const props = defineProps<{
  topologyId: number;
  modelValue?: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void;
}>();

const loading = ref(false);
const agents = ref<UnboundAgentDTO[]>([]);
const selectedAgent = ref(props.modelValue);

const loadAgents = async (keyword?: string) => {
  loading.value = true;
  try {
    const result = await queryUnboundGlobalSupervisors({
      topologyId: props.topologyId,
      keyword,
      page: 1,
      size: 100,
    });
    agents.value = result.content;
  } finally {
    loading.value = false;
  }
};

const handleSearch = (value: string) => {
  loadAgents(value);
};

const handleChange = (value: number) => {
  emit('update:modelValue', value);
};

onMounted(() => {
  loadAgents();
});

watch(() => props.topologyId, () => {
  loadAgents();
});
</script>

<style scoped>
.agent-name {
  font-weight: 500;
}
.agent-specialty {
  color: #999;
  margin-left: 8px;
}
</style>
```

## 使用场景

### 1. 绑定 Global Supervisor 对话框

当用户需要为拓扑图绑定 Global Supervisor 时：

1. 打开绑定对话框
2. 调用此接口获取可选的 Agent 列表
3. 用户选择一个 Agent
4. 调用 `/api/service/v1/agent-bounds/bind` 完成绑定

### 2. 搜索过滤

支持按 Agent 名称和专业领域进行模糊搜索：

```typescript
// 搜索包含"监控"的 Agent
const result = await queryUnboundGlobalSupervisors({
  topologyId: 4,
  keyword: '监控',
  page: 1,
  size: 20,
});
```

## 相关接口

| 接口 | 路径 | 说明 |
|------|------|------|
| 绑定 Agent | `POST /api/service/v1/agent-bounds/bind` | 绑定选中的 Agent 到拓扑图 |
| 查询已绑定 | `POST /api/service/v1/agent-bounds/query-by-entity` | 查询拓扑图已绑定的 Agent |
| 解绑 Agent | `POST /api/service/v1/agent-bounds/unbind` | 解除 Agent 绑定 |

## 绑定完整流程

```typescript
// 1. 查询未绑定的 Global Supervisor
const unboundAgents = await queryUnboundGlobalSupervisors({
  topologyId: 4,
  page: 1,
  size: 100,
});

// 2. 用户选择一个 Agent（假设选择 ID=30）
const selectedAgentId = 30;

// 3. 执行绑定（必须传 operatorId）
await request.post('/api/service/v1/agent-bounds/bind', {
  agentId: selectedAgentId,
  entityId: 4,
  entityType: 'TOPOLOGY',
  operatorId: currentUserId,  // 必填：当前操作人 ID
});

// 4. 绑定成功后，刷新未绑定列表（该 Agent 将不再出现）
const updatedUnboundAgents = await queryUnboundGlobalSupervisors({
  topologyId: 4,
  page: 1,
  size: 100,
});
```

## 绑定接口详情

### 请求

```typescript
POST /api/service/v1/agent-bounds/bind

interface BindAgentRequest {
  /** Agent ID（必填） */
  agentId: number;

  /** 实体 ID - Topology ID 或 Node ID（必填） */
  entityId: number;

  /** 实体类型：TOPOLOGY 或 NODE（必填） */
  entityType: 'TOPOLOGY' | 'NODE';

  /** 操作人 ID（必填） */
  operatorId: number;
}
```

### 响应

```typescript
interface BindAgentResponse {
  code: number;
  message: string;
  success: boolean;
  data: {
    id: number;
    agentId: number;
    agentName: string;
    agentRole: string;
    hierarchyLevel: string;
    entityId: number;
    entityType: string;
    entityName: string | null;
    createdAt: string;
  };
}
```

### 示例

```bash
curl -X POST 'http://localhost:8081/api/service/v1/agent-bounds/bind' \
  -H 'Content-Type: application/json' \
  -d '{
    "agentId": 30,
    "entityId": 4,
    "entityType": "TOPOLOGY",
    "operatorId": 1
  }'
```

## 注意事项

1. **仅返回 GLOBAL_SUPERVISOR 层级**: 此接口只返回 `hierarchy_level = 'GLOBAL_SUPERVISOR'` 的 Agent
2. **自动排除已绑定**: 已绑定到指定拓扑图的 Global Supervisor 会被自动排除
3. **分页支持**: 支持标准分页，建议每页不超过 100 条
4. **关键词搜索**: 同时搜索 `name` 和 `specialty` 字段

## 错误处理

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 401 | 未认证 | 跳转登录页 |
| 500 | 服务器错误 | 提示用户稍后重试 |

## 更新日志

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0.0 | 2025-12-30 | 新增接口 |
