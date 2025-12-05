-- F08 子图管理功能 - 数据库表结构
-- 需求追溯: 需求1-10，需求澄清1-5

-- ==============================================
-- 1. 子图表 (subgraph)
-- ==============================================
-- 用于存储子图的基本信息，包括名称、描述、标签和元数据
-- 子图名称全局唯一（需求澄清1）
CREATE TABLE subgraph (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '子图ID（主键）',
    name VARCHAR(255) NOT NULL COMMENT '子图名称（全局唯一，1-255字符）',
    description TEXT COMMENT '子图描述',
    tags JSON COMMENT '标签列表（JSON数组格式）',
    metadata JSON COMMENT '元数据（JSON对象格式，如业务域、环境、团队等）',
    created_by BIGINT NOT NULL COMMENT '创建者用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version INT DEFAULT 0 NOT NULL COMMENT '版本号（乐观锁）',

    -- 索引定义
    UNIQUE KEY uk_name (name) COMMENT '确保子图名称全局唯一',
    INDEX idx_created_by (created_by) COMMENT '按创建者查询子图',
    INDEX idx_created_at (created_at DESC) COMMENT '按创建时间排序',
    INDEX idx_updated_at (updated_at DESC) COMMENT '按更新时间排序',
    FULLTEXT INDEX ft_name_desc (name, description) COMMENT '全文搜索索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子图表';

-- ==============================================
-- 2. 子图权限表 (subgraph_permission)
-- ==============================================
-- 用于存储用户对子图的权限记录
-- 每个用户对同一子图只能拥有一种角色（复合唯一索引）
-- 角色类型：OWNER（完全控制）、VIEWER（只读访问）
CREATE TABLE subgraph_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID（主键）',
    subgraph_id BIGINT NOT NULL COMMENT '子图ID（外键）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '权限角色：OWNER（所有者）、VIEWER（查看者）',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    granted_by BIGINT NOT NULL COMMENT '授权者用户ID',

    -- 外键约束：删除子图时级联删除权限记录（需求10.1）
    CONSTRAINT fk_sp_subgraph FOREIGN KEY (subgraph_id)
        REFERENCES subgraph(id) ON DELETE CASCADE,

    -- 索引定义
    UNIQUE KEY uk_subgraph_user (subgraph_id, user_id) COMMENT '每个用户对同一子图只能有一种角色',
    INDEX idx_user_id (user_id) COMMENT '按用户ID查询权限',
    INDEX idx_subgraph_id (subgraph_id) COMMENT '按子图ID查询权限',
    INDEX idx_role (role) COMMENT '按角色过滤'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子图权限表';

-- ==============================================
-- 3. 子图资源关联表 (subgraph_resource)
-- ==============================================
-- 用于存储子图与资源节点之间的多对多关系（需求澄清2）
-- 一个资源节点可以同时属于多个子图
-- 每个子图中的资源节点唯一（复合唯一索引）
CREATE TABLE subgraph_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID（主键）',
    subgraph_id BIGINT NOT NULL COMMENT '子图ID（外键）',
    resource_id BIGINT NOT NULL COMMENT '资源节点ID（外键）',
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    added_by BIGINT NOT NULL COMMENT '添加者用户ID',

    -- 外键约束：删除子图时级联删除关联记录
    CONSTRAINT fk_sr_subgraph FOREIGN KEY (subgraph_id)
        REFERENCES subgraph(id) ON DELETE CASCADE,

    -- 外键约束：删除资源节点时级联删除关联记录（需求10.2）
    CONSTRAINT fk_sr_resource FOREIGN KEY (resource_id)
        REFERENCES resource(id) ON DELETE CASCADE,

    -- 索引定义
    UNIQUE KEY uk_subgraph_resource (subgraph_id, resource_id) COMMENT '每个子图中的资源节点唯一',
    INDEX idx_resource_id (resource_id) COMMENT '按资源ID查询所属子图',
    INDEX idx_subgraph_id (subgraph_id) COMMENT '按子图ID查询资源',
    INDEX idx_added_at (added_at DESC) COMMENT '按添加时间排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子图资源关联表';

-- ==============================================
-- 4. 子图审计日志表 (subgraph_audit_log)
-- ==============================================
-- 用于记录子图相关的所有操作审计日志（需求9）
CREATE TABLE subgraph_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID（主键）',
    subgraph_id BIGINT NOT NULL COMMENT '子图ID',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE_SUBGRAPH, UPDATE_SUBGRAPH, DELETE_SUBGRAPH, ADD_PERMISSION, REMOVE_PERMISSION, ADD_RESOURCE, REMOVE_RESOURCE',
    old_value JSON COMMENT '操作前的值（JSON格式）',
    new_value JSON COMMENT '操作后的值（JSON格式）',
    operator_id BIGINT NOT NULL COMMENT '操作者用户ID',
    operator_name VARCHAR(100) COMMENT '操作者用户名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    -- 索引定义
    INDEX idx_subgraph_id (subgraph_id) COMMENT '按子图ID查询审计日志',
    INDEX idx_operator_id (operator_id) COMMENT '按操作者查询审计日志',
    INDEX idx_operation (operation) COMMENT '按操作类型过滤',
    INDEX idx_created_at (created_at DESC) COMMENT '按操作时间排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子图审计日志表';
