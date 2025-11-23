package com.catface996.aiops.bootstrap.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Redis 配置测试
 * 
 * 验证 RedisTemplate Bean 已定义
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@SpringBootTest
class RedisConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testRedisTemplateBeanDefined() {
        // 验证 RedisTemplate Bean 已定义（即使 Redis 未连接）
        boolean beanDefined = applicationContext.containsBean("redisTemplate");
        assertTrue(beanDefined, "RedisTemplate bean should be defined");
    }
}
