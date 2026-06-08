package org.example.fitness_backend.interceptor;

import org.example.fitness_backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器。
 * <p>在请求到达 Controller 之前校验 Token 的有效性，
 * 并将解析出的 userId 存入 request 属性，供下游业务使用。</p>
 * <p>采用自定义拦截器而非 Spring Security Filter 的原因：
 * 本项目使用前后端分离架构，Token 校验逻辑相对简单，
 * 拦截器方式更轻量且易于维护。</p>
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求（浏览器跨域时会先发 OPTIONS）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从 Authorization 请求头中提取 Bearer Token
        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("无 Token 凭证，请先登录！");
        }

        try {
            // 验证 Token 并解析用户 ID
            Long userId = jwtUtils.getUserIdFromToken(token);

            // 将解析出的 userId 存入 request 属性，供下游 Controller 使用
            request.setAttribute("userId", userId);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Token 无效或已过期，请重新登录！");
        }
    }
}