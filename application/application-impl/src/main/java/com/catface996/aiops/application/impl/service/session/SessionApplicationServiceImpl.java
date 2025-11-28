package com.catface996.aiops.application.impl.service.session;

import com.catface996.aiops.application.api.dto.session.DeviceInfoDTO;
import com.catface996.aiops.application.api.dto.session.SessionDTO;
import com.catface996.aiops.application.api.dto.session.SessionValidationResultDTO;
import com.catface996.aiops.application.api.service.session.SessionApplicationService;
import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.service.session.SessionDomainService;
import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.domain.model.auth.SessionValidationResult;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话应用服务实现类
 *
 * <p>实现会话管理的应用层逻辑，包括：</p>
 * <ul>
 *   <li>编排领域服务完成业务用例</li>
 *   <li>控制事务边界</li>
 *   <li>执行权限检查</li>
 *   <li>转换DTO和领域模型</li>
 *   <li>记录审计日志</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.2, 1.3, 1.4, 1.5: 会话生命周期管理</li>
 *   <li>REQ 3.1, 3.2, 3.3: 多设备会话管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Slf4j
@Service
public class SessionApplicationServiceImpl implements SessionApplicationService {

    /**
     * 默认绝对超时时长：8小时（秒）
     */
    private static final int DEFAULT_ABSOLUTE_TIMEOUT = 8 * 60 * 60;

    /**
     * 默认空闲超时时长：30分钟（秒）
     */
    private static final int DEFAULT_IDLE_TIMEOUT = 30 * 60;

    /**
     * 记住我绝对超时时长：30天（秒）
     */
    private static final int REMEMBER_ME_ABSOLUTE_TIMEOUT = 30 * 24 * 60 * 60;

    private final SessionDomainService sessionDomainService;
    private final JwtTokenProvider jwtTokenProvider;

    public SessionApplicationServiceImpl(SessionDomainService sessionDomainService,
                                         JwtTokenProvider jwtTokenProvider) {
        this.sessionDomainService = sessionDomainService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public SessionDTO createSession(Long userId, DeviceInfoDTO deviceInfoDTO, boolean rememberMe) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (deviceInfoDTO == null) {
            throw new IllegalArgumentException("设备信息不能为空");
        }

        log.info("创建会话，用户ID：{}，设备IP：{}，记住我：{}", userId, deviceInfoDTO.getIpAddress(), rememberMe);

        // 转换DTO为领域模型
        DeviceInfo deviceInfo = convertToDeviceInfo(deviceInfoDTO);

        // 计算超时时长
        int absoluteTimeout = rememberMe ? REMEMBER_ME_ABSOLUTE_TIMEOUT : DEFAULT_ABSOLUTE_TIMEOUT;
        int idleTimeout = DEFAULT_IDLE_TIMEOUT;

        // 调用领域服务创建会话
        Session session = sessionDomainService.createSession(
                userId, deviceInfo, absoluteTimeout, idleTimeout, rememberMe);

        log.info("会话创建成功，会话ID：{}，用户ID：{}", session.getId(), userId);

        return convertToDTO(session, null);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionValidationResultDTO validateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return SessionValidationResultDTO.failure(
                    SessionErrorCode.SESSION_NOT_FOUND.getCode(),
                    SessionErrorCode.SESSION_NOT_FOUND.getMessage());
        }

        log.debug("验证会话，会话ID：{}", sessionId);

        try {
            SessionValidationResult result = sessionDomainService.validateAndRefreshSession(sessionId);

            if (!result.isValid()) {
                return SessionValidationResultDTO.failure(
                        result.getErrorCode(),
                        result.getErrorMessage());
            }

            SessionDTO sessionDTO = convertToDTO(result.getSession(), null);

            if (result.hasWarning()) {
                log.warn("会话即将过期，会话ID：{}，剩余时间：{}秒", sessionId, result.getRemainingTime());
                return SessionValidationResultDTO.successWithWarning(sessionDTO, result.getRemainingTime());
            }

            return SessionValidationResultDTO.success(sessionDTO);

        } catch (BusinessException e) {
            log.warn("会话验证失败，会话ID：{}，错误码：{}", sessionId, e.getErrorCode());
            return SessionValidationResultDTO.failure(e.getErrorCode(), e.getErrorMessage());
        }
    }

    @Override
    @Transactional
    public void destroySession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        log.info("销毁会话，会话ID：{}", sessionId);

        sessionDomainService.destroySession(sessionId);

        log.info("会话销毁成功，会话ID：{}", sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> getUserSessions(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        log.debug("查询用户会话，用户ID：{}", userId);

        List<Session> sessions = sessionDomainService.findUserSessions(userId);

        log.debug("查询到用户会话，用户ID：{}，会话数量：{}", userId, sessions.size());

        return sessions.stream()
                .map(session -> convertToDTO(session, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void terminateSession(String sessionId, Long currentUserId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("当前用户ID不能为空");
        }

        log.info("终止会话，会话ID：{}，操作用户：{}", sessionId, currentUserId);

        // 权限检查：获取会话信息
        List<Session> userSessions = sessionDomainService.findUserSessions(currentUserId);
        boolean isOwnSession = userSessions.stream()
                .anyMatch(s -> s.getId().equals(sessionId));

        if (!isOwnSession) {
            log.warn("无权限终止会话，会话ID：{}，操作用户：{}", sessionId, currentUserId);
            throw new BusinessException(SessionErrorCode.FORBIDDEN);
        }

        // 销毁会话
        sessionDomainService.destroySession(sessionId);

        log.info("会话终止成功，会话ID：{}", sessionId);
    }

    @Override
    @Transactional
    public int terminateOtherSessions(String currentSessionId, Long userId) {
        if (currentSessionId == null || currentSessionId.isEmpty()) {
            throw new IllegalArgumentException("当前会话ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        log.info("终止其他会话，当前会话ID：{}，用户ID：{}", currentSessionId, userId);

        int terminatedCount = sessionDomainService.terminateOtherSessions(currentSessionId, userId);

        log.info("其他会话终止成功，终止数量：{}", terminatedCount);

        return terminatedCount;
    }

    @Override
    @Transactional
    public String refreshAccessToken(String sessionId, Long userId, String username,
                                     String role, boolean rememberMe) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        log.info("刷新访问令牌，会话ID：{}，用户ID：{}", sessionId, userId);

        // 验证会话有效性
        try {
            sessionDomainService.validateAndRefreshSession(sessionId);
        } catch (BusinessException e) {
            log.warn("刷新令牌失败，会话无效，会话ID：{}，错误码：{}", sessionId, e.getErrorCode());
            throw e;
        }

        // 生成新的访问令牌
        String newToken = jwtTokenProvider.generateTokenWithJti(userId, username, role, sessionId, rememberMe);

        log.info("访问令牌刷新成功，会话ID：{}", sessionId);

        return newToken;
    }

    /**
     * 将DeviceInfoDTO转换为DeviceInfo领域模型
     *
     * @param dto 设备信息DTO
     * @return DeviceInfo领域模型
     */
    private DeviceInfo convertToDeviceInfo(DeviceInfoDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DeviceInfo(
                dto.getIpAddress(),
                dto.getUserAgent(),
                dto.getDeviceType(),
                dto.getOperatingSystem(),
                dto.getBrowser()
        );
    }

    /**
     * 将Session领域模型转换为SessionDTO
     *
     * @param session 会话领域模型
     * @param currentSessionId 当前会话ID（用于标记当前会话）
     * @return SessionDTO
     */
    private SessionDTO convertToDTO(Session session, String currentSessionId) {
        if (session == null) {
            return null;
        }

        DeviceInfo deviceInfo = session.getDeviceInfo();

        return SessionDTO.builder()
                .sessionId(session.getId())
                .userId(session.getUserId())
                .token(session.getToken())
                .ipAddress(deviceInfo != null ? deviceInfo.getIpAddress() : null)
                .deviceType(deviceInfo != null ? deviceInfo.getDeviceType() : null)
                .operatingSystem(deviceInfo != null ? deviceInfo.getOperatingSystem() : null)
                .browser(deviceInfo != null ? deviceInfo.getBrowser() : null)
                .createdAt(session.getCreatedAt())
                .lastActivityAt(session.getLastActivityAt())
                .expiresAt(session.getExpiresAt())
                .rememberMe(session.isRememberMe())
                .remainingSeconds(session.getRemainingSeconds())
                .currentSession(session.getId().equals(currentSessionId))
                .build();
    }
}
