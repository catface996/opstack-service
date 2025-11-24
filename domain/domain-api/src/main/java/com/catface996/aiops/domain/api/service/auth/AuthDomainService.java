package com.catface996.aiops.domain.api.service.auth;

import com.catface996.aiops.domain.api.model.auth.Account;
import com.catface996.aiops.domain.api.model.auth.AccountLockInfo;
import com.catface996.aiops.domain.api.model.auth.DeviceInfo;
import com.catface996.aiops.domain.api.model.auth.PasswordStrengthResult;
import com.catface996.aiops.domain.api.model.auth.Session;

import java.util.Optional;

/**
 * 认证领域服务接口
 * 
 * <p>提供认证相关的核心领域服务，包括：</p>
 * <ul>
 *   <li>密码管理：加密、验证、强度检查</li>
 *   <li>会话管理：创建、验证、失效、互斥</li>
 *   <li>账号锁定：记录失败、检查锁定、锁定/解锁账号</li>
 * </ul>
 * 
 * <p>本接口遵循DDD领域驱动设计原则，封装核心业务逻辑。</p>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface AuthDomainService {
    
    // ==================== 密码管理 ====================
    
    /**
     * 加密密码
     * 
     * <p>使用BCrypt算法加密原始密码，Work Factor = 10。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-004: 密码安全存储</li>
     *   <li>REQ-NFR-SEC-006: BCrypt work factor 至少为 10</li>
     * </ul>
     * 
     * @param rawPassword 原始密码（明文）
     * @return 加密后的密码（BCrypt格式，60字符）
     * @throws IllegalArgumentException 如果原始密码为空或null
     */
    String encryptPassword(String rawPassword);
    
    /**
     * 验证密码
     * 
     * <p>使用BCrypt算法验证原始密码与加密后的密码是否匹配。</p>
     * <p>使用恒定时间比较以防止时序攻击。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-004: 密码安全存储</li>
     *   <li>REQ-NFR-PERF-003: 单次验证 < 500ms</li>
     * </ul>
     * 
     * @param rawPassword 原始密码（明文）
     * @param encodedPassword 加密后的密码（BCrypt格式）
     * @return true if passwords match, false otherwise
     * @throws IllegalArgumentException 如果任一参数为空或null
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);
    
    /**
     * 验证密码强度
     * 
     * <p>验证密码是否符合强度要求：</p>
     * <ul>
     *   <li>长度：8-64个字符</li>
     *   <li>字符类型：至少包含大写字母、小写字母、数字、特殊字符中的3类</li>
     *   <li>不包含用户名或邮箱的任何部分</li>
     *   <li>不是常见弱密码（如password123、admin123等）</li>
     * </ul>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-012: 密码强度要求</li>
     * </ul>
     * 
     * @param password 待验证的密码
     * @param username 用户名（用于检查密码是否包含用户名）
     * @param email 邮箱（用于检查密码是否包含邮箱）
     * @return 密码强度验证结果，包含是否有效和错误信息列表
     * @throws IllegalArgumentException 如果任一参数为空或null
     */
    PasswordStrengthResult validatePasswordStrength(String password, String username, String email);
    
    // ==================== 会话管理 ====================
    
    /**
     * 创建会话
     * 
     * <p>为用户创建新的登录会话，生成UUID作为会话ID，生成JWT Token。</p>
     * <p>会话过期时间：</p>
     * <ul>
     *   <li>rememberMe = false: 2小时</li>
     *   <li>rememberMe = true: 30天</li>
     * </ul>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-007: 会话管理</li>
     *   <li>REQ-FR-008: 记住我功能</li>
     * </ul>
     * 
     * @param account 账号实体
     * @param rememberMe 是否记住我
     * @param deviceInfo 设备信息
     * @return 新创建的会话实体
     * @throws IllegalArgumentException 如果account为null
     */
    Session createSession(Account account, boolean rememberMe, DeviceInfo deviceInfo);
    
    /**
     * 验证会话
     * 
     * <p>验证会话是否有效：</p>
     * <ul>
     *   <li>会话存在</li>
     *   <li>会话未过期</li>
     * </ul>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-007: 会话管理</li>
     *   <li>REQ-NFR-PERF-004: 会话验证 < 1秒</li>
     * </ul>
     * 
     * @param sessionId 会话ID
     * @return 会话实体（如果有效）
     * @throws com.catface996.aiops.domain.api.exception.auth.SessionNotFoundException 如果会话不存在
     * @throws com.catface996.aiops.domain.api.exception.auth.SessionExpiredException 如果会话已过期
     */
    Session validateSession(String sessionId);
    
    /**
     * 使会话失效
     * 
     * <p>使指定的会话失效，删除会话缓存和数据库记录。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-010: 安全退出</li>
     * </ul>
     * 
     * @param sessionId 会话ID
     */
    void invalidateSession(String sessionId);
    
    /**
     * 处理会话互斥
     * 
     * <p>实现会话互斥逻辑：同一用户只能有一个活跃会话。</p>
     * <p>当用户在新设备登录时，使旧设备的会话失效。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-009: 会话互斥</li>
     * </ul>
     * 
     * @param account 账号实体
     * @param newSession 新创建的会话
     */
    void handleSessionMutex(Account account, Session newSession);
    
    // ==================== 账号锁定 ====================
    
    /**
     * 记录登录失败
     * 
     * <p>记录用户登录失败，增加失败计数。</p>
     * <p>失败计数存储在Redis中，TTL为30分钟。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-005: 防暴力破解</li>
     * </ul>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @return 当前失败次数
     */
    int recordLoginFailure(String identifier);
    
    /**
     * 检查账号锁定状态
     * 
     * <p>检查账号是否被锁定，返回锁定信息。</p>
     * <p>锁定条件：连续登录失败5次。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-005: 防暴力破解</li>
     * </ul>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @return 账号锁定信息（如果未锁定则返回empty）
     */
    Optional<AccountLockInfo> checkAccountLock(String identifier);
    
    /**
     * 锁定账号
     * 
     * <p>锁定指定的账号，设置锁定时长。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-005: 防暴力破解</li>
     * </ul>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @param lockDurationMinutes 锁定时长（分钟）
     */
    void lockAccount(String identifier, int lockDurationMinutes);
    
    /**
     * 解锁账号
     * 
     * <p>解锁指定的账号，清除登录失败计数。</p>
     * <p>此方法由管理员手动调用。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-006: 管理员手动解锁</li>
     * </ul>
     * 
     * @param accountId 账号ID
     * @throws com.catface996.aiops.domain.api.exception.auth.AccountNotFoundException 如果账号不存在
     */
    void unlockAccount(Long accountId);
    
    /**
     * 重置登录失败计数
     * 
     * <p>重置指定用户的登录失败计数为0。</p>
     * <p>此方法在用户登录成功后调用。</p>
     * 
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-005: 防暴力破解</li>
     * </ul>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     */
    void resetLoginFailureCount(String identifier);
}
