package com.catface996.aiops.bootstrap.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Security 配置测试
 * 
 * 验证：
 * 1. 公开接口无需认证
 * 2. 受保护接口需要认证
 * 3. BCrypt 密码加密器配置正确
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testHealthEndpointIsPublic() throws Exception {
        // 验证健康检查接口无需认证即可访问
        // 返回 200 或 503 都可以，重点是不返回 401/403（不需要认证）
        mockMvc.perform(get("/actuator/health"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                assertTrue(status == 200 || status == 503, 
                    "Health endpoint should be accessible without authentication (200 or 503), but got: " + status);
            });
    }

    @Test
    void testProtectedEndpointRequiresAuthentication() throws Exception {
        // 验证受保护的接口需要认证
        // Spring Security 默认返回 403 Forbidden（而不是 401）当没有认证信息时
        mockMvc.perform(get("/api/test"))
            .andExpect(status().isForbidden()); // 403 Forbidden
    }

    @Test
    void testPasswordEncoderIsConfigured() {
        // 验证 PasswordEncoder 已正确注入
        assertNotNull(passwordEncoder, "PasswordEncoder should be configured");
        
        // 验证是 BCryptPasswordEncoder
        assertTrue(passwordEncoder.getClass().getName().contains("BCrypt"), 
            "Should use BCryptPasswordEncoder");
    }

    @Test
    void testBCryptPasswordEncoding() {
        String rawPassword = "TestPassword123!";
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 验证加密后的密码不等于原始密码
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should differ from raw password");
        
        // 验证 BCrypt 加密后的密码长度为 60
        assertEquals(60, encodedPassword.length(), "BCrypt encoded password should be 60 characters");
        
        // 验证密码匹配
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), 
            "Password should match after encoding");
        
        // 验证错误密码不匹配
        assertFalse(passwordEncoder.matches("WrongPassword", encodedPassword), 
            "Wrong password should not match");
    }

    @Test
    void testBCryptGeneratesDifferentHashesForSamePassword() {
        String rawPassword = "TestPassword123!";
        
        // 同一密码加密两次
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);
        
        // 验证两次加密结果不同（因为使用了不同的盐值）
        assertNotEquals(encoded1, encoded2, 
            "BCrypt should generate different hashes for the same password (different salts)");
        
        // 但两个加密结果都应该能匹配原始密码
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }
}
