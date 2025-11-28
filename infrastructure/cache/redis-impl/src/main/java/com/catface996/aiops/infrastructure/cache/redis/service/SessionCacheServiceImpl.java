package com.catface996.aiops.infrastructure.cache.redis.service;

import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 会话缓存服务实现（Redis）
 *
 * <p>实现Cache-Aside模式的缓存层，提供会话数据的Redis缓存操作。</p>
 *
 * <p>Redis Key格式：</p>
 * <ul>
 *   <li>会话数据：session:{sessionId}</li>
 *   <li>用户会话列表：user:sessions:{userId} (Set类型)</li>
 *   <li>令牌黑名单：token:blacklist:{tokenId}</li>
 * </ul>
 *
 * <p>异常处理策略：</p>
 * <ul>
 *   <li>Redis连接失败时捕获异常，记录警告日志</li>
 *   <li>不阻塞主流程，由调用方降级到MySQL查询</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.2, 1.4, 2.4: 会话缓存和令牌黑名单</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
@Slf4j
@Service
public class SessionCacheServiceImpl implements SessionCacheService {

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_SESSIONS_KEY_PREFIX = "user:sessions:";
    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "token:blacklist:";
    private static final String BLACKLIST_VALUE = "1";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public SessionCacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void cacheSession(Session session, long ttlSeconds) {
        if (session == null || session.getId() == null) {
            log.warn("Session or sessionId is null, skip caching");
            return;
        }

        String sessionKey = SESSION_KEY_PREFIX + session.getId();

        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(sessionKey, sessionJson, ttlSeconds, TimeUnit.SECONDS);

            // 添加到用户会话列表
            if (session.getUserId() != null) {
                addUserSession(session.getUserId(), session.getId());
            }

            log.debug("Cached session: {}, TTL: {}s", session.getId(), ttlSeconds);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session to JSON, sessionId: {}", session.getId(), e);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot cache session: {}", session.getId(), e);
        } catch (Exception e) {
            log.error("Failed to cache session: {}", session.getId(), e);
        }
    }

    @Override
    public Session getCachedSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        try {
            Object value = redisTemplate.opsForValue().get(sessionKey);

            if (value == null) {
                log.debug("Session not found in cache: {}", sessionId);
                return null;
            }

            if (value instanceof String) {
                return objectMapper.readValue((String) value, Session.class);
            } else {
                log.warn("Unexpected value type in cache for session: {}, type: {}",
                        sessionId, value.getClass().getName());
                return null;
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize session from JSON, sessionId: {}", sessionId, e);
            return null;
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot get cached session: {}", sessionId, e);
            return null;
        } catch (Exception e) {
            log.error("Failed to get cached session: {}", sessionId, e);
            return null;
        }
    }

    @Override
    public void evictSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        try {
            // 先获取会话以取得userId
            Session session = getCachedSession(sessionId);

            // 删除会话缓存
            Boolean deleted = redisTemplate.delete(sessionKey);
            log.debug("Evicted session from cache: {}, deleted: {}", sessionId, deleted);

            // 从用户会话列表中移除
            if (session != null && session.getUserId() != null) {
                removeUserSession(session.getUserId(), sessionId);
            }

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot evict session: {}", sessionId, e);
        } catch (Exception e) {
            log.error("Failed to evict session: {}", sessionId, e);
        }
    }

    @Override
    public void evictUserSessions(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            // 获取用户的所有会话ID
            Set<String> sessionIds = getUserSessionIds(userId);

            // 删除每个会话缓存
            for (String sessionId : sessionIds) {
                String sessionKey = SESSION_KEY_PREFIX + sessionId;
                redisTemplate.delete(sessionKey);
            }

            // 删除用户会话列表
            String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
            redisTemplate.delete(userSessionsKey);

            log.debug("Evicted {} sessions for user: {}", sessionIds.size(), userId);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot evict user sessions: {}", userId, e);
        } catch (Exception e) {
            log.error("Failed to evict user sessions: {}", userId, e);
        }
    }

    @Override
    public void cacheUserSessions(Long userId, Set<String> sessionIds) {
        if (userId == null || sessionIds == null || sessionIds.isEmpty()) {
            return;
        }

        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

        try {
            // 先删除旧的列表
            redisTemplate.delete(userSessionsKey);

            // 添加所有会话ID
            redisTemplate.opsForSet().add(userSessionsKey, sessionIds.toArray());

            log.debug("Cached {} session IDs for user: {}", sessionIds.size(), userId);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot cache user sessions: {}", userId, e);
        } catch (Exception e) {
            log.error("Failed to cache user sessions: {}", userId, e);
        }
    }

    @Override
    public Set<String> getUserSessionIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

        try {
            Set<Object> members = redisTemplate.opsForSet().members(userSessionsKey);

            if (members == null || members.isEmpty()) {
                return Collections.emptySet();
            }

            Set<String> sessionIds = new HashSet<>();
            for (Object member : members) {
                if (member instanceof String) {
                    sessionIds.add((String) member);
                }
            }

            return sessionIds;

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot get user session IDs: {}", userId, e);
            return Collections.emptySet();
        } catch (Exception e) {
            log.error("Failed to get user session IDs: {}", userId, e);
            return Collections.emptySet();
        }
    }

    @Override
    public void addUserSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }

        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

        try {
            redisTemplate.opsForSet().add(userSessionsKey, sessionId);
            log.debug("Added session {} to user {} session list", sessionId, userId);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot add user session: userId={}, sessionId={}",
                    userId, sessionId, e);
        } catch (Exception e) {
            log.error("Failed to add user session: userId={}, sessionId={}", userId, sessionId, e);
        }
    }

    @Override
    public void removeUserSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }

        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

        try {
            redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
            log.debug("Removed session {} from user {} session list", sessionId, userId);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot remove user session: userId={}, sessionId={}",
                    userId, sessionId, e);
        } catch (Exception e) {
            log.error("Failed to remove user session: userId={}, sessionId={}", userId, sessionId, e);
        }
    }

    @Override
    public void addToBlacklist(String tokenId, long ttlSeconds) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            return;
        }

        if (ttlSeconds <= 0) {
            log.debug("Token already expired, no need to blacklist: {}", tokenId);
            return;
        }

        String blacklistKey = TOKEN_BLACKLIST_KEY_PREFIX + tokenId;

        try {
            redisTemplate.opsForValue().set(blacklistKey, BLACKLIST_VALUE, ttlSeconds, TimeUnit.SECONDS);
            log.info("Added token to blacklist: {}, TTL: {}s", tokenId, ttlSeconds);

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot add token to blacklist: {}", tokenId, e);
        } catch (Exception e) {
            log.error("Failed to add token to blacklist: {}", tokenId, e);
        }
    }

    @Override
    public boolean isInBlacklist(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            return false;
        }

        String blacklistKey = TOKEN_BLACKLIST_KEY_PREFIX + tokenId;

        try {
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            return exists != null && exists;

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot check token blacklist: {}", tokenId, e);
            // 安全考虑：Redis不可用时，假设令牌不在黑名单中
            // 这样可能会允许已登出的令牌继续使用，但不会影响正常登录
            return false;
        } catch (Exception e) {
            log.error("Failed to check token blacklist: {}", tokenId, e);
            return false;
        }
    }

    @Override
    public boolean existsInCache(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        try {
            Boolean exists = redisTemplate.hasKey(sessionKey);
            return exists != null && exists;

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot check session existence: {}", sessionId, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to check session existence: {}", sessionId, e);
            return false;
        }
    }

    @Override
    public boolean updateTtl(String sessionId, long ttlSeconds) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        try {
            Boolean updated = redisTemplate.expire(sessionKey, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Updated TTL for session: {}, newTTL: {}s, success: {}",
                    sessionId, ttlSeconds, updated);
            return updated != null && updated;

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, cannot update session TTL: {}", sessionId, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to update session TTL: {}", sessionId, e);
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            return "PONG".equalsIgnoreCase(pong);

        } catch (Exception e) {
            log.warn("Redis is not available", e);
            return false;
        }
    }
}
