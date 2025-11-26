# AIOps Service 数据库初始化说明

本文档详细描述 AIOps Service 的数据库初始化流程，包括 Flyway 迁移脚本说明和手动初始化方法。

## 1. 概述

AIOps Service 使用 **Flyway** 进行数据库版本管理。应用启动时会自动执行数据库迁移，无需手动执行 SQL 脚本。

### 1.1 技术栈

| 组件 | 版本 | 说明 |
|-----|------|------|
| MySQL | 8.0+ | 关系型数据库 |
| Flyway | 9.x | 数据库迁移工具 |
| Druid | 1.2.x | 数据库连接池 |

### 1.2 字符集配置

- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`
- 支持完整的 Unicode 字符（包括 emoji）

## 2. 数据库创建

### 2.1 创建数据库

```sql
-- 连接 MySQL
mysql -u root -p

-- 创建数据库（UTF-8 字符集）
CREATE DATABASE aiops_service
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 验证数据库创建
SHOW DATABASES LIKE 'aiops%';
SHOW CREATE DATABASE aiops_service;
```

### 2.2 创建数据库用户

```sql
-- 创建专用用户（开发环境）
CREATE USER 'aiops'@'localhost' IDENTIFIED BY 'aiops_dev_password';

-- 创建专用用户（生产环境 - 限制访问来源）
CREATE USER 'aiops'@'10.0.0.%' IDENTIFIED BY 'strong_production_password';

-- 授予权限
GRANT SELECT, INSERT, UPDATE, DELETE ON aiops_service.* TO 'aiops'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON aiops_service.* TO 'aiops'@'10.0.0.%';

-- Flyway 迁移需要额外权限
GRANT CREATE, ALTER, DROP, INDEX, REFERENCES ON aiops_service.* TO 'aiops'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 验证用户权限
SHOW GRANTS FOR 'aiops'@'localhost';
```

## 3. Flyway 迁移脚本

### 3.1 脚本位置

```
bootstrap/src/main/resources/db/migration/
├── V1__Create_account_table.sql    # 用户账号表
└── V2__Create_session_table.sql    # 会话表
```

### 3.2 命名规范

Flyway 迁移脚本遵循以下命名规范：

```
V{版本号}__{描述}.sql
```

- **V**：前缀，表示版本化迁移
- **版本号**：数字，按顺序递增（1, 2, 3...）
- **__**：双下划线分隔符
- **描述**：脚本功能描述（使用下划线连接）
- **.sql**：SQL 文件扩展名

### 3.3 V1__Create_account_table.sql

用户账号表，存储用户认证信息：

```sql
CREATE TABLE t_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账号ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(60) NOT NULL COMMENT '加密后的密码(BCrypt)',
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username) COMMENT '用户名唯一索引',
    UNIQUE KEY uk_email (email) COMMENT '邮箱唯一索引',
    INDEX idx_status (status) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';
```

**字段说明：**

| 字段 | 类型 | 说明 | 约束 |
|-----|------|------|------|
| id | BIGINT | 主键ID | AUTO_INCREMENT |
| username | VARCHAR(20) | 用户名 | NOT NULL, UNIQUE |
| email | VARCHAR(100) | 邮箱 | NOT NULL, UNIQUE |
| password | VARCHAR(60) | BCrypt 加密密码 | NOT NULL |
| role | VARCHAR(20) | 用户角色 | DEFAULT 'ROLE_USER' |
| status | VARCHAR(20) | 账号状态 | DEFAULT 'ACTIVE' |
| created_at | DATETIME | 创建时间 | 自动填充 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**角色枚举：**
- `ROLE_USER` - 普通用户
- `ROLE_ADMIN` - 管理员

**状态枚举：**
- `ACTIVE` - 活跃
- `LOCKED` - 锁定
- `DISABLED` - 禁用

### 3.4 V2__Create_session_table.sql

会话表，作为 Redis 不可用时的降级方案：

```sql
CREATE TABLE t_session (
    id VARCHAR(36) PRIMARY KEY COMMENT '会话ID (UUID)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token TEXT NOT NULL COMMENT 'JWT Token',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    device_info TEXT COMMENT '设备信息 (JSON格式)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    INDEX idx_expires_at (expires_at) COMMENT '过期时间索引',
    CONSTRAINT fk_session_user_id FOREIGN KEY (user_id)
        REFERENCES t_account(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表(降级方案)';
```

**字段说明：**

| 字段 | 类型 | 说明 | 约束 |
|-----|------|------|------|
| id | VARCHAR(36) | 会话ID（UUID） | PRIMARY KEY |
| user_id | BIGINT | 关联用户ID | FK -> t_account.id |
| token | TEXT | JWT Token | NOT NULL |
| expires_at | DATETIME | 会话过期时间 | NOT NULL |
| device_info | TEXT | 设备信息（JSON） | 可选 |
| created_at | DATETIME | 创建时间 | 自动填充 |

## 4. Flyway 配置

### 4.1 Spring Boot 配置

```yaml
spring:
  flyway:
    # 启用 Flyway
    enabled: true
    # 迁移脚本位置
    locations: classpath:db/migration
    # 首次迁移时创建基准版本
    baseline-on-migrate: true
    # 基准版本号
    baseline-version: 0
    # 验证迁移脚本
    validate-on-migrate: true
    # 允许乱序迁移（开发环境）
    out-of-order: false
    # 迁移历史表名
    table: flyway_schema_history
```

### 4.2 环境特定配置

```yaml
# application-dev.yml
spring:
  flyway:
    enabled: true
    clean-disabled: false  # 开发环境允许清理

# application-prod.yml
spring:
  flyway:
    enabled: true
    clean-disabled: true   # 生产环境禁止清理
```

## 5. 手动执行迁移

### 5.1 使用 Maven 插件

```bash
# 执行迁移
mvn flyway:migrate -pl bootstrap

# 查看迁移状态
mvn flyway:info -pl bootstrap

# 验证迁移脚本
mvn flyway:validate -pl bootstrap

# 清理数据库（仅开发环境）
mvn flyway:clean -pl bootstrap

# 修复迁移历史
mvn flyway:repair -pl bootstrap
```

### 5.2 直接执行 SQL

如果不使用 Flyway，可以直接执行 SQL 脚本：

```bash
# 执行所有迁移脚本
mysql -u aiops -p aiops_service < bootstrap/src/main/resources/db/migration/V1__Create_account_table.sql
mysql -u aiops -p aiops_service < bootstrap/src/main/resources/db/migration/V2__Create_session_table.sql

# 创建 Flyway 历史表（可选，用于后续使用 Flyway）
mysql -u aiops -p aiops_service -e "
CREATE TABLE flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"
```

## 6. 数据库维护

### 6.1 查看迁移历史

```sql
-- 查看 Flyway 迁移历史
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 查看表结构
DESCRIBE t_account;
DESCRIBE t_session;

-- 查看表索引
SHOW INDEX FROM t_account;
SHOW INDEX FROM t_session;
```

### 6.2 数据备份

```bash
# 完整备份
mysqldump -u root -p --single-transaction \
  --routines --triggers --events \
  aiops_service > aiops_backup_$(date +%Y%m%d_%H%M%S).sql

# 仅备份数据（不含 Flyway 历史）
mysqldump -u root -p --single-transaction \
  --ignore-table=aiops_service.flyway_schema_history \
  aiops_service > aiops_data_backup.sql

# 压缩备份
mysqldump -u root -p --single-transaction aiops_service | gzip > aiops_backup.sql.gz
```

### 6.3 数据恢复

```bash
# 恢复完整备份
mysql -u root -p aiops_service < aiops_backup.sql

# 恢复压缩备份
gunzip < aiops_backup.sql.gz | mysql -u root -p aiops_service
```

### 6.4 清理过期会话

```sql
-- 删除过期会话（定期清理任务）
DELETE FROM t_session WHERE expires_at < NOW();

-- 查看过期会话数量
SELECT COUNT(*) FROM t_session WHERE expires_at < NOW();

-- 创建清理事件（MySQL 事件调度器）
CREATE EVENT IF NOT EXISTS cleanup_expired_sessions
ON SCHEDULE EVERY 1 HOUR
DO DELETE FROM t_session WHERE expires_at < NOW();

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;
```

## 7. 常见问题

### 7.1 迁移失败回滚

如果迁移失败，Flyway 会标记为失败状态。需要手动修复：

```bash
# 1. 修复 Flyway 历史记录
mvn flyway:repair -pl bootstrap

# 2. 或手动删除失败记录
mysql -u root -p aiops_service -e "
DELETE FROM flyway_schema_history WHERE success = 0;
"

# 3. 重新执行迁移
mvn flyway:migrate -pl bootstrap
```

### 7.2 校验和不匹配

如果修改了已执行的迁移脚本，会导致校验和不匹配：

```bash
# 错误信息：Migration checksum mismatch

# 解决方案 1：修复校验和（仅开发环境）
mvn flyway:repair -pl bootstrap

# 解决方案 2：创建新的迁移脚本（推荐生产环境）
# 创建 V3__Fix_xxx.sql 来修正问题
```

### 7.3 表已存在

如果表已存在但 Flyway 历史为空：

```bash
# 设置基准版本
mvn flyway:baseline -pl bootstrap -Dflyway.baselineVersion=2

# 这会跳过 V1 和 V2，从 V3 开始执行
```

## 8. 添加新迁移

### 8.1 创建新迁移脚本

```bash
# 创建新的迁移脚本
touch bootstrap/src/main/resources/db/migration/V3__Add_user_profile_table.sql
```

### 8.2 编写迁移脚本

```sql
-- V3__Add_user_profile_table.sql
-- 添加用户配置文件表

CREATE TABLE t_user_profile (
    id BIGINT PRIMARY KEY COMMENT '用户ID，关联 t_account',
    display_name VARCHAR(50) COMMENT '显示名称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',
    locale VARCHAR(10) DEFAULT 'zh_CN' COMMENT '语言区域',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_account FOREIGN KEY (id) REFERENCES t_account(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户配置文件表';
```

### 8.3 测试迁移

```bash
# 验证脚本语法
mvn flyway:validate -pl bootstrap

# 执行迁移
mvn flyway:migrate -pl bootstrap

# 查看状态
mvn flyway:info -pl bootstrap
```

## 9. ER 图

```
+-------------------+          +-------------------+
|    t_account      |          |    t_session      |
+-------------------+          +-------------------+
| PK id             |<---------| FK user_id        |
|    username       |          | PK id (UUID)      |
|    email          |          |    token          |
|    password       |          |    expires_at     |
|    role           |          |    device_info    |
|    status         |          |    created_at     |
|    created_at     |          +-------------------+
|    updated_at     |
+-------------------+
```

---

文档版本：1.0.0
最后更新：2025-11-26
