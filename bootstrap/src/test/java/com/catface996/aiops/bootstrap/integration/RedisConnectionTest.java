package com.catface996.aiops.bootstrap.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 连接集成测试
 * 
 * 前置条件：本地 Docker 环境需要运行 Redis
 * 启动命令：docker run -d --name redis-local -p 6379:6379 --restart unless-stopped redis:7.0
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@SpringBootTest
class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisConnection() {
        // 验证 RedisTemplate 已注入
        assertNotNull(redisTemplate, "RedisTemplate should be injected");
        
        // 测试 Redis 连接 - 写入数据
        String testKey = "test:connection:key";
        String testValue = "Hello Redis!";
        
        redisTemplate.opsForValue().set(testKey, testValue);
        
        // 测试 Redis 连接 - 读取数据
        Object retrievedValue = redisTemplate.opsForValue().get(testKey);
        
        // 验证数据正确
        assertNotNull(retrievedValue, "Retrieved value should not be null");
        assertEquals(testValue, retrievedValue, "Retrieved value should match the original value");
        
        // 清理测试数据
        redisTemplate.delete(testKey);
        
        // 验证数据已删除
        Object deletedValue = redisTemplate.opsForValue().get(testKey);
        assertNull(deletedValue, "Value should be null after deletion");
    }

    @Test
    void testRedisSerializationWithComplexObject() {
        // 测试复杂对象的序列化和反序列化
        String testKey = "test:object:key";
        TestObject testObject = new TestObject("test-id", "test-name", 100);
        
        // 写入对象
        redisTemplate.opsForValue().set(testKey, testObject);
        
        // 读取对象
        Object retrievedObject = redisTemplate.opsForValue().get(testKey);
        
        // 验证对象正确
        assertNotNull(retrievedObject, "Retrieved object should not be null");
        assertTrue(retrievedObject instanceof TestObject, "Retrieved object should be TestObject");
        
        TestObject retrieved = (TestObject) retrievedObject;
        assertEquals(testObject.getId(), retrieved.getId(), "ID should match");
        assertEquals(testObject.getName(), retrieved.getName(), "Name should match");
        assertEquals(testObject.getValue(), retrieved.getValue(), "Value should match");
        
        // 清理测试数据
        redisTemplate.delete(testKey);
    }

    /**
     * 测试对象
     */
    static class TestObject {
        private String id;
        private String name;
        private Integer value;

        public TestObject() {
        }

        public TestObject(String id, String name, Integer value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}
