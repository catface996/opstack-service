-- V39: 修复 model 字段数据
-- 将 model_name 中的 provider model ID 移到 provider_model_id 字段
-- 并根据 provider model ID 设置友好的 model_name

-- 1. 先将 model_name 的值复制到 provider_model_id
UPDATE agent
SET provider_model_id = model_name
WHERE model_name IS NOT NULL
  AND model_name != ''
  AND provider_model_id IS NULL;

-- 2. 根据 provider_model_id 设置友好的 model_name
-- Claude 系列
UPDATE agent SET model_name = 'Claude Opus 4.5'
WHERE provider_model_id LIKE '%claude-opus-4-5%';

UPDATE agent SET model_name = 'Claude Sonnet 4.5'
WHERE provider_model_id LIKE '%claude-sonnet-4-5%';

UPDATE agent SET model_name = 'Claude Sonnet 4'
WHERE provider_model_id LIKE '%claude-sonnet-4-%'
  AND provider_model_id NOT LIKE '%claude-sonnet-4-5%';

UPDATE agent SET model_name = 'Claude Haiku 3.5'
WHERE provider_model_id LIKE '%claude-haiku-3-5%';

-- Gemini 系列
UPDATE agent SET model_name = 'Gemini 2.0 Flash'
WHERE provider_model_id LIKE '%gemini-2.0-flash%'
   OR provider_model_id = 'gemini-2.0-flash';

UPDATE agent SET model_name = 'Gemini 1.5 Pro'
WHERE provider_model_id LIKE '%gemini-1.5-pro%';

UPDATE agent SET model_name = 'Gemini 1.5 Flash'
WHERE provider_model_id LIKE '%gemini-1.5-flash%';

-- GPT 系列
UPDATE agent SET model_name = 'GPT-4 Turbo'
WHERE provider_model_id LIKE '%gpt-4-turbo%';

UPDATE agent SET model_name = 'GPT-4o'
WHERE provider_model_id LIKE '%gpt-4o%';

UPDATE agent SET model_name = 'GPT-4'
WHERE provider_model_id LIKE '%gpt-4%'
  AND provider_model_id NOT LIKE '%gpt-4o%'
  AND provider_model_id NOT LIKE '%gpt-4-turbo%';

-- 对于未匹配的，保持 provider_model_id 作为 model_name（作为fallback）
