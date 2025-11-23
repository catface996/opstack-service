package com.catface996.aiops.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 * 
 * 配置：
 * - 无状态会话管理（使用 JWT）
 * - 公开接口：/actuator/health
 * - 其他接口需要认证
 * - BCrypt 密码加密器（Work Factor = 10）
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置 HTTP 安全
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（使用 JWT，不需要 CSRF 保护）
            .csrf(csrf -> csrf.disable())
            
            // 配置会话管理：无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开接口：健康检查
                .requestMatchers("/actuator/health").permitAll()
                // 其他所有接口都需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * 配置密码加密器
     * 
     * 使用 BCrypt 算法，Work Factor = 10
     * 
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
