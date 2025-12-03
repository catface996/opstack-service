-- F04 资源关系管理功能 - 数据库表结构
-- 需求: 6.3, 6.4, 7.1, 7.2

-- 资源关系表
CREATE TABLE resource_relationship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    source_resource_id BIGINT NOT NULL COMMENT '源资源ID',
    target_resource_id BIGINT NOT NULL COMMENT '目标资源ID',
    relationship_type VARCHAR(50) NOT NULL COMMENT '关系类型：DEPENDENCY, CALL, DEPLOYMENT, OWNERSHIP, ASSOCIATION',
    direction VARCHAR(20) NOT NULL COMMENT '关系方向：UNIDIRECTIONAL, BIDIRECTIONAL',
    strength VARCHAR(20) NOT NULL COMMENT '关系强度：STRONG, WEAK',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '关系状态：NORMAL, ABNORMAL',
    description VARCHAR(500) COMMENT '关系描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束（级联删除）
    FOREIGN KEY (source_resource_id) REFERENCES resource(id) ON DELETE CASCADE,
    FOREIGN KEY (target_resource_id) REFERENCES resource(id) ON DELETE CASCADE,

    -- 索引设计
    INDEX idx_source_resource (source_resource_id) COMMENT '加速下游查询',
    INDEX idx_target_resource (target_resource_id) COMMENT '加速上游查询',
    INDEX idx_type (relationship_type) COMMENT '加速类型筛选',
    INDEX idx_status (status) COMMENT '加速状态筛选',

    -- 复合唯一索引（防止重复关系，加速重复检测）
    UNIQUE KEY uk_source_target_type (source_resource_id, target_resource_id, relationship_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源关系表';
