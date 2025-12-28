# Quickstart: Topology 绑定报告模板

**Feature**: 034-topology-report-template
**Date**: 2025-12-28

## 1. 功能概述

本功能为 Topology（拓扑图）添加与 ReportTemplate（报告模板）的多对多绑定管理，包含以下 4 个 API 端点：

| 操作 | Endpoint | 描述 |
|------|----------|------|
| 绑定模板 | POST `/api/service/v1/topologies/report-templates/bind` | 将报告模板绑定到拓扑图 |
| 解绑模板 | POST `/api/service/v1/topologies/report-templates/unbind` | 解除绑定关系 |
| 查询已绑定 | POST `/api/service/v1/topologies/report-templates/bound` | 查询已绑定的模板列表 |
| 查询未绑定 | POST `/api/service/v1/topologies/report-templates/unbound` | 查询未绑定的模板列表 |

## 2. 快速验证

### 2.1 前置条件

确保应用已启动并可访问：

```bash
# 健康检查
curl http://localhost:8081/actuator/health
# 预期: {"status":"UP"}
```

### 2.2 绑定报告模板

```bash
curl -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bind' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 1,
    "reportTemplateIds": [1, 2, 3],
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 0,
  "message": "绑定成功",
  "data": {
    "successCount": 3,
    "skipCount": 0
  },
  "success": true
}
```

### 2.3 查询已绑定模板

```bash
curl -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bound' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 1,
    "page": 1,
    "size": 10,
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "安全审计报告模板",
        "description": "用于生成安全审计报告的标准模板",
        "category": "Security",
        "boundAt": "2025-12-28T12:00:00",
        "boundBy": 1
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 3,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "success": true
}
```

### 2.4 查询未绑定模板

```bash
curl -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/unbound' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 1,
    "page": 1,
    "size": 10,
    "keyword": "",
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 4,
        "name": "故障事件报告模板",
        "description": "用于记录和分析系统故障事件",
        "category": "Incident"
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 3,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "success": true
}
```

### 2.5 解绑报告模板

```bash
curl -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/unbind' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 1,
    "reportTemplateIds": [1],
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 0,
  "message": "解绑成功",
  "data": {
    "successCount": 1,
    "skipCount": 0
  },
  "success": true
}
```

## 3. 错误处理

### 3.1 拓扑图不存在

```bash
curl -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bind' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 99999,
    "reportTemplateIds": [1],
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 404001,
  "message": "拓扑图不存在",
  "data": null,
  "success": false
}
```

### 3.2 模板不存在

```bash
curl -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bind' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 1,
    "reportTemplateIds": [99999],
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 404002,
  "message": "报告模板不存在: [99999]",
  "data": null,
  "success": false
}
```

### 3.3 批量限制

```bash
# 超过 100 个模板 ID
curl -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bind' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 1,
    "reportTemplateIds": [1,2,3,...101],
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 400001,
  "message": "单次最多绑定 100 个模板",
  "data": null,
  "success": false
}
```

## 4. 数据库验证

执行迁移后，验证表结构：

```sql
-- 检查表是否创建
SHOW CREATE TABLE topology_2_report_template;

-- 查看绑定关系
SELECT * FROM topology_2_report_template WHERE deleted = 0;

-- 统计某拓扑图绑定的模板数
SELECT COUNT(*) FROM topology_2_report_template
WHERE topology_id = 1 AND deleted = 0;
```

## 5. Swagger UI 验证

访问 Swagger UI 验证接口文档：

```
http://localhost:8081/swagger-ui.html
```

在"拓扑图管理"标签下应该能看到新增的 4 个报告模板绑定相关接口。

## 6. 完整验证流程

```bash
# 1. 查看系统中的拓扑图
curl -s -X POST 'http://localhost:8081/api/service/v1/topologies/query' \
  -H 'Content-Type: application/json' \
  -d '{"page":1,"size":5,"operatorId":1}' | jq '.data.content[0].id'

# 2. 查看系统中的报告模板
curl -s -X POST 'http://localhost:8081/api/service/v1/report-templates/list' \
  -H 'Content-Type: application/json' \
  -d '{"page":1,"size":5}' | jq '.data.content[].id'

# 3. 绑定模板到拓扑图
curl -s -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bind' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId":3,"reportTemplateIds":[1,2],"operatorId":1}'

# 4. 查询已绑定模板
curl -s -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bound' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId":3,"page":1,"size":10,"operatorId":1}'

# 5. 查询未绑定模板
curl -s -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/unbound' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId":3,"page":1,"size":10,"operatorId":1}'

# 6. 解绑模板
curl -s -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/unbind' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId":3,"reportTemplateIds":[1],"operatorId":1}'

# 7. 再次查询确认解绑成功
curl -s -X POST 'http://localhost:8081/api/service/v1/topologies/report-templates/bound' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId":3,"page":1,"size":10,"operatorId":1}'
```
