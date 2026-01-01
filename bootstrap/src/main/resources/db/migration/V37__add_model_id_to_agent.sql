-- V37: 添加 model_id 字段到 agent 表
-- 支持同时存储模型名称（model）和完整模型 ID（model_id）
-- model: 模型友好名称，如 "Claude Opus 4.5", "gemini-2.0-flash"
-- model_id: 完整模型标识，如 "anthropic.claude-opus-4-5-20251124-v1:0"

ALTER TABLE agent
    ADD COLUMN model_id VARCHAR(200) NULL COMMENT '完整模型 ID（如 anthropic.claude-opus-4-5-20251124-v1:0）' AFTER model;

-- 更新现有 model 字段的注释
ALTER TABLE agent
    MODIFY COLUMN model VARCHAR(100) NULL COMMENT '模型名称（如 Claude Opus 4.5）';
