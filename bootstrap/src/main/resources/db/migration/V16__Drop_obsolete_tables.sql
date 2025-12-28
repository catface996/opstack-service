-- ==============================================
-- 清理过时表迁移脚本
-- Feature: 028-cleanup-obsolete-tables
-- Date: 2025-12-28
-- ==============================================
-- 说明：
-- V12 迁移将 resource 拆分为 topology 和 node 后，
-- 以下表已不再使用，需要清理：
-- - subgraph 相关表（已被 topology 替代）
-- - resource_audit_log（resource 表已删除）
-- - resource_tag（resource 表已删除）
-- ==============================================

-- 1. 删除 subgraph 相关表（无外键依赖，可直接删除）
DROP TABLE IF EXISTS subgraph_permission;
DROP TABLE IF EXISTS subgraph_audit_log;
DROP TABLE IF EXISTS subgraph_resource;
DROP TABLE IF EXISTS subgraph;

-- 2. 删除 resource 遗留表
DROP TABLE IF EXISTS resource_tag;
DROP TABLE IF EXISTS resource_audit_log;
