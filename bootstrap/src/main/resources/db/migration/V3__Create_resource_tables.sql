-- F03 资源管理功能 - 数据库表结构
-- 需求: REQ-FR-001, REQ-FR-026

-- 1. 资源类型表
CREATE TABLE resource_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '类型编码：SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT',
    name VARCHAR(100) NOT NULL COMMENT '类型名称',
    description TEXT COMMENT '类型描述',
    icon VARCHAR(100) COMMENT '图标URL',
    is_system BOOLEAN DEFAULT TRUE COMMENT '是否系统预置',
    attribute_schema JSON COMMENT '属性定义Schema（为F02-1预留）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    INDEX idx_code (code),
    INDEX idx_is_system (is_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源类型表';

-- 2. 资源表
CREATE TABLE resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '资源名称',
    description TEXT COMMENT '资源描述',
    resource_type_id BIGINT NOT NULL COMMENT '资源类型ID',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING, STOPPED, MAINTENANCE, OFFLINE',
    attributes JSON COMMENT '扩展属性（JSON格式）',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建者（第一个Owner）',
    FOREIGN KEY (resource_type_id) REFERENCES resource_type(id),
    INDEX idx_name (name),
    INDEX idx_type (resource_type_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_updated_at (updated_at DESC),
    UNIQUE KEY uk_type_name (resource_type_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- 3. 资源标签关联表
CREATE TABLE resource_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人ID',
    FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE,
    UNIQUE KEY uk_resource_tag (resource_id, tag_name),
    INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源标签关联表';

-- 4. 审计日志表
CREATE TABLE resource_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    operation VARCHAR(20) NOT NULL COMMENT '操作：CREATE, UPDATE, DELETE, STATUS_CHANGE',
    old_value JSON COMMENT '旧值',
    new_value JSON COMMENT '新值',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE,
    INDEX idx_resource_id (resource_id),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源审计日志表';

-- 5. 插入6种预置资源类型数据
INSERT INTO resource_type (code, name, description, icon, is_system, created_by) VALUES
('SERVER', '服务器', '物理服务器或虚拟机资源', '/icons/server.svg', TRUE, 1),
('APPLICATION', '应用', '应用程序或微服务', '/icons/application.svg', TRUE, 1),
('DATABASE', '数据库', '数据库实例', '/icons/database.svg', TRUE, 1),
('API', 'API接口', 'REST API或其他接口服务', '/icons/api.svg', TRUE, 1),
('MIDDLEWARE', '中间件', '消息队列、缓存等中间件服务', '/icons/middleware.svg', TRUE, 1),
('REPORT', '报表', '数据报表和分析', '/icons/report.svg', TRUE, 1);
