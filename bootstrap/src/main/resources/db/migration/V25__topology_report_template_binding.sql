-- =====================================================
-- V25: 创建拓扑图-报告模板绑定关系表
-- Feature: 034-topology-report-template
-- Date: 2025-12-29
-- =====================================================

CREATE TABLE topology_2_report_template (
    -- 主键
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联ID',

    -- 关联字段
    topology_id           BIGINT          NOT NULL COMMENT '拓扑图ID',
    report_template_id    BIGINT          NOT NULL COMMENT '报告模板ID',

    -- 审计字段
    created_by            BIGINT          COMMENT '创建人ID',
    created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 软删除
    deleted               TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    UNIQUE KEY uk_topology_template (topology_id, report_template_id, deleted),
    INDEX idx_topology_id (topology_id),
    INDEX idx_report_template_id (report_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拓扑图-报告模板关联表';
