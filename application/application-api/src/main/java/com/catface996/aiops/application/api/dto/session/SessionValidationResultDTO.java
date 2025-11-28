package com.catface996.aiops.application.api.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话验证结果数据传输对象
 *
 * <p>用于在应用层和接口层之间传输会话验证结果。</p>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionValidationResultDTO {

    /**
     * 验证是否成功
     */
    private boolean valid;

    /**
     * 会话信息（验证成功时返回）
     */
    private SessionDTO session;

    /**
     * 错误码（验证失败时返回）
     */
    private String errorCode;

    /**
     * 错误消息（验证失败时返回）
     */
    private String errorMessage;

    /**
     * 是否有警告（会话即将过期）
     */
    private boolean hasWarning;

    /**
     * 剩余时间（秒）
     */
    private int remainingTime;

    /**
     * 创建成功的验证结果
     */
    public static SessionValidationResultDTO success(SessionDTO session) {
        return SessionValidationResultDTO.builder()
                .valid(true)
                .session(session)
                .hasWarning(false)
                .remainingTime(session != null ? (int) session.getRemainingSeconds() : 0)
                .build();
    }

    /**
     * 创建带警告的验证结果
     */
    public static SessionValidationResultDTO successWithWarning(SessionDTO session, int remainingTime) {
        return SessionValidationResultDTO.builder()
                .valid(true)
                .session(session)
                .hasWarning(true)
                .remainingTime(remainingTime)
                .build();
    }

    /**
     * 创建失败的验证结果
     */
    public static SessionValidationResultDTO failure(String errorCode, String errorMessage) {
        return SessionValidationResultDTO.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .hasWarning(false)
                .build();
    }
}
