package org.example.fitness_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 全局配置。
 * <p>本项目采用前后端分离 + JWT 无状态认证架构，因此：</p>
 * <ul>
 *   <li>禁用 CSRF —— API 使用 JWT 而非 Session Cookie，不存在 CSRF 攻击面</li>
 *   <li>设置无状态会话 —— 不使用 HttpSession，每次请求通过 Token 认证</li>
 *   <li>放开所有请求的 Security 拦截 —— 实际鉴权由自定义的 {@code JwtInterceptor} 处理</li>
 * </ul>
 * <p>Spring Security 在此仅用于提供 {@link PasswordEncoder}（BCrypt）Bean，
 * 确保密码加密的一致性。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF：API 无状态认证（JWT），无 Cookie-Session 机制，CSRF 不适用
            .csrf(csrf -> csrf.disable())
            // 无状态会话：不创建 HttpSession，每次请求独立认证
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 放开 Security 拦截，实际认证由 JwtInterceptor 负责
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * 密码加密器，使用 BCrypt 算法。
     * 用于用户注册时加密密码、登录时校验密码。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
