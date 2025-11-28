package com.catface996.aiops.application.api.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话数据传输对象
 *
 * <p>用于在应用层和接口层之间传输会话信息。</p>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 操作系统
     */
    private String operatingSystem;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivityAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 是否记住我
     */
    private boolean rememberMe;

    /**
     * 剩余有效时间（秒）
     */
    private long remainingSeconds;

    /**
     * 是否为当前会话
     */
    private boolean currentSession;
}
