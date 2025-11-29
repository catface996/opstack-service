package com.catface996.aiops.application.api.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户账号 DTO
 *
 * <p>用于管理员用户列表查询的数据传输对象。</p>
 *
 * @author AI Assistant
 * @since 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户账号信息")
public class AccountDTO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "邮箱", example = "john@example.com")
    private String email;

    @Schema(description = "角色", example = "ROLE_USER")
    private String role;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    @Schema(description = "创建时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "最后登录时间", example = "2025-01-01T12:00:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "是否锁定", example = "false")
    private Boolean isLocked;

    @Schema(description = "锁定截止时间", example = "2025-01-01T13:00:00")
    private LocalDateTime lockUntil;
}
