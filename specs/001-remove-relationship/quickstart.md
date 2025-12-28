# Quickstart: Relationship API 测试指南（API 保持不变）

**Date**: 2025-12-28
**Feature**: 001-remove-relationship

**重要说明**：本次重构保持 API 路径和参数格式不变，仅修改内部实现。以下测试用例使用现有 API。

## 1. 前置条件

```bash
# 确保服务已启动
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 验证服务健康
curl -s http://localhost:8081/actuator/health
```

## 2. 基础 CRUD 测试

### 2.1 创建关系

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/create \
  -H "Content-Type: application/json" \
  -d '{
    "sourceResourceId": 1,
    "targetResourceId": 2,
    "relationshipType": "DEPENDENCY",
    "direction": "UNIDIRECTIONAL",
    "strength": "STRONG",
    "description": "节点1依赖节点2"
  }'
```

### 2.2 查询关系（分页）

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/query \
  -H "Content-Type: application/json" \
  -d '{
    "relationshipType": "DEPENDENCY",
    "page": 1,
    "size": 10
  }'
```

### 2.3 获取单个关系

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/get \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'
```

### 2.4 更新关系

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "strength": "WEAK",
    "status": "WARNING",
    "description": "更新后的描述"
  }'
```

### 2.5 删除关系

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/delete \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'
```

## 3. 高级功能测试

### 3.1 查询资源的所有关系

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/resource/query \
  -H "Content-Type: application/json" \
  -d '{"resourceId": 1}'
```

### 3.2 图遍历

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/resource/traverse \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "direction": "DOWNSTREAM",
    "maxDepth": 5
  }'
```

### 3.3 循环依赖检测

```bash
curl -X POST http://localhost:8081/api/service/v1/relationships/resource/cycle-detection \
  -H "Content-Type: application/json" \
  -d '{"resourceId": 1}'
```

## 4. 验证清单（重构后功能等价性）

- [ ] 创建关系成功（API 参数不变）
- [ ] 分页查询正常（响应格式不变）
- [ ] 获取单个关系正常
- [ ] 更新关系成功
- [ ] 删除关系成功
- [ ] 查询资源所有关系正常
- [ ] 图遍历正常
- [ ] 循环检测正常
- [ ] 错误处理正常（错误码不变）

## 5. 重构验证要点

- [ ] 所有现有 API 端点正常工作
- [ ] 请求参数格式未变
- [ ] 响应数据格式未变
- [ ] 错误码和错误消息未变
- [ ] 性能无明显下降
