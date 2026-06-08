package org.example.fitness_backend.interceptor;

import org.example.fitness_backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 全局保安拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("无 Token 凭证，请先登录！");
        }

        try {
            // 3. 验证 Token
            Long userId = jwtUtils.getUserIdFromToken(token);

            // 4. 【神仙操作】把解析出来的 userId 顺手塞进 request 域里
            request.setAttribute("userId", userId);

            return true; // 验证通过，放行！
        } catch (Exception e) {
            throw new RuntimeException("Token 无效或已过期，请重新登录！");
        }
    }
}