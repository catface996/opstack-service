-- V38: Rename model fields in agent table for clarity
-- model → model_name (friendly display name)
-- model_id → provider_model_id (provider's model identifier)

-- Rename model → model_name
ALTER TABLE agent RENAME COLUMN model TO model_name;

-- Rename model_id → provider_model_id
ALTER TABLE agent RENAME COLUMN model_id TO provider_model_id;

-- Update column comments for clarity
ALTER TABLE agent
    MODIFY COLUMN model_name VARCHAR(100) NULL
    COMMENT '模型友好名称（如 Claude Opus 4.5, gemini-2.0-flash）';

ALTER TABLE agent
    MODIFY COLUMN provider_model_id VARCHAR(200) NULL
    COMMENT '模型提供商标识符（如 anthropic.claude-opus-4-5-20251124-v1:0）';
