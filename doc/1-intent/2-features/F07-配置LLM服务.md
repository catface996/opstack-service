# F07: 配置 LLM 服务

## 基本信息

- **Feature ID**: F07
- **Feature 名称**: 配置 LLM 服务
- **优先级**: P0（MVP 必须）
- **状态**: 待开发
- **负责人**: 待分配
- **预计工作量**: 待评估

## 用户故事

作为系统管理员，我希望配置 LLM 服务（如 OpenAI、Claude），以便 Agent 可以使用 LLM 进行智能分析。

## 功能价值

- 支持智能分析能力
- 灵活选择 LLM 服务
- 控制成本和性能

## 核心功能

### 1. 添加 LLM 服务
- 选择 LLM 类型（OpenAI、Claude、本地模型）
- 填写基本信息（名称、描述）
- 配置服务优先级

### 2. 配置连接信息
- API Key（加密存储）
- API 端点（支持自定义）
- 超时设置
- 重试策略

### 3. 配置模型参数
- 选择模型（GPT-4、GPT-3.5、Claude-3 等）
- 温度（Temperature）：0-2
- 最大 Token 数：1-128000
- Top-P 参数
- 频率惩罚（Frequency Penalty）
- 存在惩罚（Presence Penalty）

### 4. 测试连接
- 发送测试请求
- 验证 API Key 有效性
- 检查网络连通性
- 显示测试结果

### 5. 设置成本限制
- 每日成本限制
- 每月成本限制
- 单次调用成本限制
- 成本告警阈值

### 6. 查看调用统计
- 调用次数统计（按天、按周、按月）
- 成本统计（按 Token 计费）
- 平均延迟统计
- 成功率统计
- 错误统计

### 7. 管理 LLM 服务
- 启用/禁用服务
- 编辑配置
- 删除服务
- 设置默认服务

## 验收标准

### AC1: 添加 LLM 服务
**WHEN** 用户添加 LLM 服务 **THEN** THE System **SHALL** 支持至少 1 种 LLM 服务（OpenAI 或 Claude）

**WHEN** 用户填写 API Key **THEN** THE System **SHALL** 加密存储 API Key

### AC2: 测试连接
**WHEN** 用户配置 LLM 服务 **THEN** THE System **SHALL** 允许测试连接是否成功

**WHEN** 测试连接失败 **THEN** THE System **SHALL** 显示详细的错误信息

### AC3: 调用统计
**WHEN** 用户查看统计 **THEN** THE System **SHALL** 显示 LLM 调用次数、成本和平均延迟

**WHEN** 用户选择时间范围 **THEN** THE System **SHALL** 显示该时间范围内的统计数据

### AC4: 成本控制
**WHEN** LLM 调用超过成本限制 **THEN** THE System **SHALL** 发送告警通知

**WHEN** 达到成本限制 **THEN** THE System **SHALL** 拒绝新的 LLM 调用请求

### AC5: 多服务支持
**WHEN** 配置多个 LLM 服务 **THEN** THE System **SHALL** 支持设置默认服务

**WHEN** 默认服务不可用 **THEN** THE System **SHALL** 自动切换到备用服务

## 依赖关系

### 前置依赖
- 无

### 后置依赖
- F05: 配置和管理 Agent
- F12: 通过 Chatbot 查询资源信息

## 技术考虑

### LLM 集成
- OpenAI SDK
- Anthropic SDK
- 统一的 LLM 调用接口
- 支持流式响应

### 安全性
- API Key 加密存储
- HTTPS 传输
- 请求签名验证

### 性能优化
- 请求缓存
- 并发控制
- 超时重试
- 降级策略

### 成本控制
- Token 计数
- 成本计算
- 预算管理
- 告警通知

## 界面设计要点

### LLM 服务列表页面
- 服务列表（卡片视图）
- 服务状态（启用/禁用）
- 添加服务按钮
- 统计概览

### LLM 服务配置页面
- 基本信息配置
- 连接信息配置
- 模型参数配置
- 成本限制配置
- 测试连接按钮

### 统计页面
- 调用次数趋势图
- 成本趋势图
- 延迟分布图
- 错误统计表

## 测试要点

### 功能测试
- 测试添加各种 LLM 服务
- 测试连接测试功能
- 测试成本限制
- 测试统计功能

### 集成测试
- 测试与 OpenAI API 的集成
- 测试与 Claude API 的集成
- 测试 API 调用和响应

### 安全测试
- 测试 API Key 加密存储
- 测试权限控制

## 相关文档

- 需求文档: `doc/intent/blueprint.md` - 2.3 LLM 与提示词管理
- Feature List: `doc/intent/feature-list.md` - F09

---

**创建日期**: 2024-11-20  
**最后更新**: 2024-11-20  
**文档版本**: v1.0
