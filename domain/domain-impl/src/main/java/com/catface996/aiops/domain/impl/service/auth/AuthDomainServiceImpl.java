package com.catface996.aiops.domain.impl.service.auth;

import com.catface996.aiops.domain.api.exception.auth.SessionExpiredException;
import com.catface996.aiops.domain.api.exception.auth.SessionNotFoundException;
import com.catface996.aiops.domain.api.model.auth.Account;
import com.catface996.aiops.domain.api.model.auth.AccountLockInfo;
import com.catface996.aiops.domain.api.model.auth.DeviceInfo;
import com.catface996.aiops.domain.api.model.auth.PasswordStrengthResult;
import com.catface996.aiops.domain.api.model.auth.Session;
import com.catface996.aiops.domain.api.service.auth.AuthDomainService;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCache;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 认证领域服务实现类
 *
 * 实现密码管理、会话管理、账号锁定等核心业务逻辑
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Service
public class AuthDomainServiceImpl implements AuthDomainService {

    // 密码长度常量
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;

    // 会话过期时间常量（小时）
    private static final int DEFAULT_SESSION_HOURS = 2;
    private static final int REMEMBER_ME_SESSION_DAYS = 30;

    // 密码字符类型正则表达式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");

    // 弱密码检测正则表达式（连续字符至少4位才算弱密码）
    private static final Pattern CONSECUTIVE_CHARS_PATTERN = Pattern.compile(".*(0123|1234|2345|3456|4567|5678|6789|abcd|bcde|cdef|defg|efgh|fghi|ghij|hijk|ijkl|jklm|klmn|lmno|mnop|nopq|opqr|pqrs|qrst|rstu|stuv|tuvw|uvwx|vwxy|wxyz).*");
    private static final Pattern REPEATED_CHARS_PATTERN = Pattern.compile(".*(.)\\1{5,}.*");
    private static final Pattern KEYBOARD_SEQUENCE_PATTERN = Pattern.compile(".*(qwerty|asdfgh|zxcvbn).*");

    // 常见弱密码列表
    private static final String[] COMMON_WEAK_PASSWORDS = {
        "password", "admin", "123456", "qwerty", "letmein",
        "password123", "admin123", "12345678", "123456789"
    };

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionCache sessionCache;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param passwordEncoder Spring Security提供的BCryptPasswordEncoder
     * @param jwtTokenProvider JWT Token提供者
     * @param sessionCache 会话缓存
     */
    public AuthDomainServiceImpl(PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider,
                                  SessionCache sessionCache) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionCache = sessionCache;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // 配置忽略未知属性
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ==================== 密码管理 ====================

    @Override
    public String encryptPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("原始密码不能为空");
        }

        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("原始密码不能为空");
        }

        if (encodedPassword == null || encodedPassword.isEmpty()) {
            throw new IllegalArgumentException("加密密码不能为空");
        }

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public PasswordStrengthResult validatePasswordStrength(String password, String username, String email) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        List<String> errors = new ArrayList<>();

        // 规则1：长度检查
        if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add("密码长度至少为" + MIN_PASSWORD_LENGTH + "个字符");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            errors.add("密码长度最多为" + MAX_PASSWORD_LENGTH + "个字符");
        }

        // 规则2：字符类型检查（至少包含3类）
        int typeCount = 0;
        if (UPPERCASE_PATTERN.matcher(password).matches()) {
            typeCount++;
        }
        if (LOWERCASE_PATTERN.matcher(password).matches()) {
            typeCount++;
        }
        if (DIGIT_PATTERN.matcher(password).matches()) {
            typeCount++;
        }
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            typeCount++;
        }

        if (typeCount < 3) {
            errors.add("密码必须包含大写字母、小写字母、数字、特殊字符中的至少3类");
        }

        // 规则3：不包含用户名或邮箱
        String lowerPassword = password.toLowerCase();
        String lowerUsername = username.toLowerCase();

        if (lowerPassword.contains(lowerUsername)) {
            errors.add("密码不能包含用户名");
        }

        // 提取邮箱前缀（@之前的部分）
        String emailPrefix = email.split("@")[0].toLowerCase();
        if (lowerPassword.contains(emailPrefix)) {
            errors.add("密码不能包含邮箱");
        }

        // 规则4：弱密码检查
        if (isWeakPassword(password)) {
            errors.add("密码过于简单，请使用更复杂的密码");
        }

        // 返回验证结果
        if (errors.isEmpty()) {
            return PasswordStrengthResult.valid();
        } else {
            return PasswordStrengthResult.invalid(errors);
        }
    }

    /**
     * 检查是否为弱密码
     *
     * @param password 待检查的密码
     * @return true if password is weak, false otherwise
     */
    private boolean isWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();

        // 检查连续字符
        if (CONSECUTIVE_CHARS_PATTERN.matcher(lowerPassword).matches()) {
            return true;
        }

        // 检查重复字符
        if (REPEATED_CHARS_PATTERN.matcher(lowerPassword).matches()) {
            return true;
        }

        // 检查键盘序列
        if (KEYBOARD_SEQUENCE_PATTERN.matcher(lowerPassword).matches()) {
            return true;
        }

        // 检查常见弱密码
        for (String weakPassword : COMMON_WEAK_PASSWORDS) {
            if (lowerPassword.contains(weakPassword)) {
                return true;
            }
        }

        return false;
    }

    // ==================== 会话管理 ====================

    @Override
    public Session createSession(Account account, boolean rememberMe, DeviceInfo deviceInfo) {
        if (account == null) {
            throw new IllegalArgumentException("账号不能为空");
        }

        if (account.getId() == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }

        if (account.getUsername() == null || account.getUsername().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 生成会话ID（UUID）
        String sessionId = UUID.randomUUID().toString();

        // 计算过期时间
        LocalDateTime expiresAt;
        if (rememberMe) {
            // 记住我：30天
            expiresAt = LocalDateTime.now().plusDays(REMEMBER_ME_SESSION_DAYS);
        } else {
            // 默认：2小时
            expiresAt = LocalDateTime.now().plusHours(DEFAULT_SESSION_HOURS);
        }

        // 生成JWT Token
        String token = jwtTokenProvider.generateToken(
            account.getId(),
            account.getUsername(),
            account.getRole() != null ? account.getRole().name() : "USER",
            rememberMe
        );

        // 创建Session对象
        Session session = new Session(
            sessionId,
            account.getId(),
            token,
            expiresAt,
            deviceInfo,
            LocalDateTime.now()
        );

        // 序列化Session为JSON
        String sessionData;
        try {
            sessionData = objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("会话序列化失败", e);
        }

        // 保存到SessionCache
        sessionCache.save(sessionId, sessionData, expiresAt, account.getId());

        return session;
    }

    @Override
    public Session validateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        // 从SessionCache获取会话数据
        Optional<String> sessionDataOpt = sessionCache.get(sessionId);
        if (!sessionDataOpt.isPresent()) {
            throw SessionNotFoundException.notFound(sessionId);
        }

        // 反序列化Session
        Session session;
        try {
            session = objectMapper.readValue(sessionDataOpt.get(), Session.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("会话反序列化失败", e);
        }

        // 检查会话是否过期
        if (session.isExpired()) {
            // 删除过期的会话
            sessionCache.delete(sessionId);
            throw SessionExpiredException.expired();
        }

        return session;
    }

    @Override
    public void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        // 从SessionCache删除会话
        sessionCache.delete(sessionId);
    }

    @Override
    public void handleSessionMutex(Account account, Session newSession) {
        if (account == null) {
            throw new IllegalArgumentException("账号不能为空");
        }

        if (account.getId() == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }

        if (newSession == null) {
            throw new IllegalArgumentException("新会话不能为空");
        }

        // 查找用户的旧会话
        Optional<String> oldSessionIdOpt = sessionCache.getSessionIdByUserId(account.getId());

        // 如果存在旧会话，使其失效
        if (oldSessionIdOpt.isPresent()) {
            String oldSessionId = oldSessionIdOpt.get();
            // 删除旧会话（会话互斥）
            sessionCache.delete(oldSessionId);
        }
    }

    // ==================== 账号锁定 ====================
    // TODO: 任务12 - 实现账号锁定相关方法

    @Override
    public int recordLoginFailure(String identifier) {
        throw new UnsupportedOperationException("任务12 - 待实现");
    }

    @Override
    public Optional<AccountLockInfo> checkAccountLock(String identifier) {
        throw new UnsupportedOperationException("任务12 - 待实现");
    }

    @Override
    public void lockAccount(String identifier, int lockDurationMinutes) {
        throw new UnsupportedOperationException("任务12 - 待实现");
    }

    @Override
    public void unlockAccount(Long accountId) {
        throw new UnsupportedOperationException("任务12 - 待实现");
    }

    @Override
    public void resetLoginFailureCount(String identifier) {
        throw new UnsupportedOperationException("任务12 - 待实现");
    }
}
