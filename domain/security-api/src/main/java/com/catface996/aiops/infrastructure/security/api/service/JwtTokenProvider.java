package com.catface996.aiops.infrastructure.security.api.service;

import com.catface996.aiops.domain.model.auth.TokenClaims;

import java.util.Date;
import java.util.Map;

/**
 * JWT Token 提供者接口
 *
 * <p>负责 JWT Token 的生成、验证和解析功能。</p>
 * <p>支持不同的过期时间（2小时 vs 30天）。</p>
 * <p>处理 Token 过期、无效等异常情况。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 2.1, 2.2, 2.3, 2.4: JWT令牌管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface JwtTokenProvider {

    /**
     * 生成 JWT Token
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param role       用户角色
     * @param rememberMe 是否记住我
     * @return JWT Token 字符串
     */
    String generateToken(Long userId, String username, String role, boolean rememberMe);

    /**
     * 生成 JWT Token（包含 sessionId）
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param role       用户角色
     * @param sessionId  会话ID
     * @param rememberMe 是否记住我
     * @return JWT Token 字符串
     */
    String generateToken(Long userId, String username, String role, String sessionId, boolean rememberMe);

    /**
     * 生成带有唯一标识的 JWT Token
     *
     * <p>生成的Token包含jti（JWT ID），用于令牌黑名单管理。</p>
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param role       用户角色
     * @param sessionId  会话ID
     * @param rememberMe 是否记住我
     * @return JWT Token 字符串
     */
    String generateTokenWithJti(Long userId, String username, String role, String sessionId, boolean rememberMe);

    /**
     * 从 Token 中提取会话ID
     *
     * @param token JWT Token 字符串
     * @return 会话ID，如果不存在则返回 null
     */
    String getSessionIdFromToken(String token);

    /**
     * 从 Token 中提取令牌ID（jti）
     *
     * @param token JWT Token 字符串
     * @return 令牌ID，如果不存在则返回 null
     */
    String getTokenIdFromToken(String token);

    /**
     * 验证并解析 JWT Token
     *
     * @param token JWT Token 字符串
     * @return Claims Map，包含 Token 中的所有声明
     * @throws io.jsonwebtoken.ExpiredJwtException      Token 已过期
     * @throws io.jsonwebtoken.UnsupportedJwtException  不支持的 JWT
     * @throws io.jsonwebtoken.MalformedJwtException    JWT 格式错误
     * @throws io.jsonwebtoken.security.SignatureException 签名验证失败
     * @throws IllegalArgumentException                  Token 为空或无效
     */
    Map<String, Object> validateAndParseToken(String token);

    /**
     * 解析 Token 为 TokenClaims 值对象
     *
     * @param token JWT Token 字符串
     * @return TokenClaims 值对象
     * @throws io.jsonwebtoken.ExpiredJwtException Token 已过期
     * @throws io.jsonwebtoken.security.SignatureException 签名验证失败
     */
    TokenClaims parseTokenClaims(String token);

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT Token 字符串
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 从 Token 中提取用户角色
     *
     * @param token JWT Token 字符串
     * @return 用户角色
     */
    String getRoleFromToken(String token);

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token 字符串
     * @return true 如果 Token 已过期，否则 false
     */
    boolean isTokenExpired(String token);

    /**
     * 获取 Token 的过期时间
     *
     * @param token JWT Token 字符串
     * @return 过期时间
     */
    Date getExpirationDateFromToken(String token);

    /**
     * 获取 Token 的剩余有效时间（秒）
     *
     * @param token JWT Token 字符串
     * @return 剩余有效时间（秒），如果已过期返回0
     */
    long getRemainingTtl(String token);
}
