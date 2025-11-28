package com.catface996.aiops.application.impl.service.session;

import com.catface996.aiops.application.api.dto.session.DeviceInfoDTO;
import com.catface996.aiops.application.api.dto.session.SessionDTO;
import com.catface996.aiops.application.api.dto.session.SessionValidationResultDTO;
import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.service.session.SessionDomainService;
import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.domain.model.auth.SessionValidationResult;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 会话应用服务单元测试
 *
 * <p>测试 SessionApplicationServiceImpl 的所有功能。</p>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("会话应用服务测试")
class SessionApplicationServiceImplTest {

    @Mock
    private SessionDomainService sessionDomainService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SessionApplicationServiceImpl sessionApplicationService;

    private DeviceInfoDTO testDeviceInfoDTO;
    private DeviceInfo testDeviceInfo;
    private Session testSession;

    @BeforeEach
    void setUp() {
        testDeviceInfoDTO = DeviceInfoDTO.builder()
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .deviceType("Desktop")
                .operatingSystem("Windows 10")
                .browser("Chrome")
                .build();

        testDeviceInfo = new DeviceInfo(
                "192.168.1.100",
                "Mozilla/5.0",
                "Desktop",
                "Windows 10",
                "Chrome"
        );

        LocalDateTime now = LocalDateTime.now();
        testSession = new Session(
                "session-123",
                1L,
                null,
                now.plusHours(8),
                testDeviceInfo,
                now,
                8 * 60 * 60,
                30 * 60,
                false
        );
    }

    @Nested
    @DisplayName("创建会话测试")
    class CreateSessionTests {

        @Test
        @DisplayName("成功创建会话")
        void createSession_success() {
            // Given
            when(sessionDomainService.createSession(
                    eq(1L), any(DeviceInfo.class), eq(8 * 60 * 60), eq(30 * 60), eq(false)))
                    .thenReturn(testSession);

            // When
            SessionDTO result = sessionApplicationService.createSession(1L, testDeviceInfoDTO, false);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo("session-123");
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getIpAddress()).isEqualTo("192.168.1.100");
            assertThat(result.getDeviceType()).isEqualTo("Desktop");
            assertThat(result.isRememberMe()).isFalse();

            verify(sessionDomainService).createSession(
                    eq(1L), any(DeviceInfo.class), eq(8 * 60 * 60), eq(30 * 60), eq(false));
        }

        @Test
        @DisplayName("记住我模式创建会话")
        void createSession_rememberMe() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Session rememberMeSession = new Session(
                    "session-456",
                    1L,
                    null,
                    now.plusDays(30),
                    testDeviceInfo,
                    now,
                    30 * 24 * 60 * 60,
                    30 * 60,
                    true
            );

            when(sessionDomainService.createSession(
                    eq(1L), any(DeviceInfo.class), eq(30 * 24 * 60 * 60), eq(30 * 60), eq(true)))
                    .thenReturn(rememberMeSession);

            // When
            SessionDTO result = sessionApplicationService.createSession(1L, testDeviceInfoDTO, true);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isRememberMe()).isTrue();

            verify(sessionDomainService).createSession(
                    eq(1L), any(DeviceInfo.class), eq(30 * 24 * 60 * 60), eq(30 * 60), eq(true));
        }

        @Test
        @DisplayName("用户ID为空时抛出异常")
        void createSession_nullUserId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.createSession(null, testDeviceInfoDTO, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("用户ID不能为空");
        }

        @Test
        @DisplayName("设备信息为空时抛出异常")
        void createSession_nullDeviceInfo_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.createSession(1L, null, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("设备信息不能为空");
        }
    }

    @Nested
    @DisplayName("验证会话测试")
    class ValidateSessionTests {

        @Test
        @DisplayName("验证有效会话")
        void validateSession_valid() {
            // Given
            SessionValidationResult validResult = SessionValidationResult.success(testSession);
            when(sessionDomainService.validateAndRefreshSession("session-123"))
                    .thenReturn(validResult);

            // When
            SessionValidationResultDTO result = sessionApplicationService.validateSession("session-123");

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getSession()).isNotNull();
            assertThat(result.getSession().getSessionId()).isEqualTo("session-123");
            assertThat(result.isHasWarning()).isFalse();
        }

        @Test
        @DisplayName("验证即将过期的会话")
        void validateSession_withWarning() {
            // Given
            SessionValidationResult warningResult = SessionValidationResult.successWithWarning(testSession, 180);
            when(sessionDomainService.validateAndRefreshSession("session-123"))
                    .thenReturn(warningResult);

            // When
            SessionValidationResultDTO result = sessionApplicationService.validateSession("session-123");

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.isHasWarning()).isTrue();
            assertThat(result.getRemainingTime()).isEqualTo(180);
        }

        @Test
        @DisplayName("验证无效会话")
        void validateSession_invalid() {
            // Given
            SessionValidationResult invalidResult = SessionValidationResult.failure(
                    SessionErrorCode.SESSION_EXPIRED.getCode(),
                    SessionErrorCode.SESSION_EXPIRED.getMessage());
            when(sessionDomainService.validateAndRefreshSession("session-123"))
                    .thenReturn(invalidResult);

            // When
            SessionValidationResultDTO result = sessionApplicationService.validateSession("session-123");

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_EXPIRED.getCode());
        }

        @Test
        @DisplayName("会话ID为空时返回失败")
        void validateSession_nullSessionId_returnFailure() {
            // When
            SessionValidationResultDTO result = sessionApplicationService.validateSession(null);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("会话ID为空字符串时返回失败")
        void validateSession_emptySessionId_returnFailure() {
            // When
            SessionValidationResultDTO result = sessionApplicationService.validateSession("");

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("领域服务抛出异常时返回失败")
        void validateSession_domainServiceThrows_returnFailure() {
            // Given
            when(sessionDomainService.validateAndRefreshSession("session-123"))
                    .thenThrow(new BusinessException(SessionErrorCode.SESSION_EXPIRED));

            // When
            SessionValidationResultDTO result = sessionApplicationService.validateSession("session-123");

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_EXPIRED.getCode());
        }
    }

    @Nested
    @DisplayName("销毁会话测试")
    class DestroySessionTests {

        @Test
        @DisplayName("成功销毁会话")
        void destroySession_success() {
            // Given
            doNothing().when(sessionDomainService).destroySession("session-123");

            // When
            sessionApplicationService.destroySession("session-123");

            // Then
            verify(sessionDomainService).destroySession("session-123");
        }

        @Test
        @DisplayName("会话ID为空时抛出异常")
        void destroySession_nullSessionId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.destroySession(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("会话ID不能为空");
        }

        @Test
        @DisplayName("会话ID为空字符串时抛出异常")
        void destroySession_emptySessionId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.destroySession(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("会话ID不能为空");
        }
    }

    @Nested
    @DisplayName("获取用户会话列表测试")
    class GetUserSessionsTests {

        @Test
        @DisplayName("成功获取用户会话列表")
        void getUserSessions_success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Session session2 = new Session(
                    "session-456",
                    1L,
                    null,
                    now.plusHours(8),
                    testDeviceInfo,
                    now,
                    8 * 60 * 60,
                    30 * 60,
                    false
            );

            when(sessionDomainService.findUserSessions(1L))
                    .thenReturn(Arrays.asList(testSession, session2));

            // When
            List<SessionDTO> result = sessionApplicationService.getUserSessions(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSessionId()).isEqualTo("session-123");
            assertThat(result.get(1).getSessionId()).isEqualTo("session-456");
        }

        @Test
        @DisplayName("用户没有会话时返回空列表")
        void getUserSessions_empty() {
            // Given
            when(sessionDomainService.findUserSessions(1L))
                    .thenReturn(Collections.emptyList());

            // When
            List<SessionDTO> result = sessionApplicationService.getUserSessions(1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("用户ID为空时抛出异常")
        void getUserSessions_nullUserId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.getUserSessions(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("用户ID不能为空");
        }
    }

    @Nested
    @DisplayName("终止指定会话测试")
    class TerminateSessionTests {

        @Test
        @DisplayName("成功终止自己的会话")
        void terminateSession_ownSession_success() {
            // Given
            when(sessionDomainService.findUserSessions(1L))
                    .thenReturn(Collections.singletonList(testSession));
            doNothing().when(sessionDomainService).destroySession("session-123");

            // When
            sessionApplicationService.terminateSession("session-123", 1L);

            // Then
            verify(sessionDomainService).destroySession("session-123");
        }

        @Test
        @DisplayName("尝试终止他人会话时抛出异常")
        void terminateSession_otherUserSession_throwsException() {
            // Given
            when(sessionDomainService.findUserSessions(1L))
                    .thenReturn(Collections.singletonList(testSession));

            // When/Then
            assertThatThrownBy(() ->
                    sessionApplicationService.terminateSession("other-session", 1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.FORBIDDEN.getCode());
                    });
        }

        @Test
        @DisplayName("会话ID为空时抛出异常")
        void terminateSession_nullSessionId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.terminateSession(null, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("会话ID不能为空");
        }

        @Test
        @DisplayName("当前用户ID为空时抛出异常")
        void terminateSession_nullUserId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.terminateSession("session-123", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("当前用户ID不能为空");
        }
    }

    @Nested
    @DisplayName("终止其他会话测试")
    class TerminateOtherSessionsTests {

        @Test
        @DisplayName("成功终止其他会话")
        void terminateOtherSessions_success() {
            // Given
            when(sessionDomainService.terminateOtherSessions("session-123", 1L))
                    .thenReturn(2);

            // When
            int result = sessionApplicationService.terminateOtherSessions("session-123", 1L);

            // Then
            assertThat(result).isEqualTo(2);
            verify(sessionDomainService).terminateOtherSessions("session-123", 1L);
        }

        @Test
        @DisplayName("当前会话ID为空时抛出异常")
        void terminateOtherSessions_nullSessionId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.terminateOtherSessions(null, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("当前会话ID不能为空");
        }

        @Test
        @DisplayName("用户ID为空时抛出异常")
        void terminateOtherSessions_nullUserId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.terminateOtherSessions("session-123", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("用户ID不能为空");
        }
    }

    @Nested
    @DisplayName("刷新访问令牌测试")
    class RefreshAccessTokenTests {

        @Test
        @DisplayName("成功刷新访问令牌")
        void refreshAccessToken_success() {
            // Given
            SessionValidationResult validResult = SessionValidationResult.success(testSession);
            when(sessionDomainService.validateAndRefreshSession("session-123"))
                    .thenReturn(validResult);
            when(jwtTokenProvider.generateTokenWithJti(1L, "testuser", "ROLE_USER", "session-123", false))
                    .thenReturn("new-jwt-token");

            // When
            String result = sessionApplicationService.refreshAccessToken(
                    "session-123", 1L, "testuser", "ROLE_USER", false);

            // Then
            assertThat(result).isEqualTo("new-jwt-token");
            verify(jwtTokenProvider).generateTokenWithJti(1L, "testuser", "ROLE_USER", "session-123", false);
        }

        @Test
        @DisplayName("会话无效时刷新令牌失败")
        void refreshAccessToken_invalidSession_throwsException() {
            // Given
            when(sessionDomainService.validateAndRefreshSession("session-123"))
                    .thenThrow(new BusinessException(SessionErrorCode.SESSION_EXPIRED));

            // When/Then
            assertThatThrownBy(() ->
                    sessionApplicationService.refreshAccessToken(
                            "session-123", 1L, "testuser", "ROLE_USER", false))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_EXPIRED.getCode());
                    });
        }

        @Test
        @DisplayName("会话ID为空时抛出异常")
        void refreshAccessToken_nullSessionId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.refreshAccessToken(
                            null, 1L, "testuser", "ROLE_USER", false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("会话ID不能为空");
        }

        @Test
        @DisplayName("用户ID为空时抛出异常")
        void refreshAccessToken_nullUserId_throwsException() {
            assertThatThrownBy(() ->
                    sessionApplicationService.refreshAccessToken(
                            "session-123", null, "testuser", "ROLE_USER", false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("用户ID不能为空");
        }
    }
}
