package com.chenpperr.xhs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器（安检员）
 *
 * 职责：
 * 1. 每个请求进来时，从 Header 中提取 "Bearer xxx" Token
 * 2. 验证 Token 是否有效（是否过期、是否被篡改）
 * 3. 如果有效，解析出 userId，塞进 SecurityContext（全局上下文）
 * 4. 放行请求，交给后面的 Controller 处理
 *
 * 就像地铁安检员：检查你的通行证（Token），确认没问题后放行，
 * 并在你身上贴个标签（SecurityContext），后面的人一看标签就知道你是谁。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        // ========== 第1步：从请求头提取 Token ==========
        String token = extractToken(request);

        // ========== 第2步：验证 Token 并解析用户 ==========
        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);

            // ========== 第3步：把 userId 存入 SecurityContext（全局上下文） ==========
            // 参数说明：
            //   第1个参数 userId → principal（当前用户身份，后面用 getPrincipal() 取出）
            //   第2个参数 null   → credentials（密码，已经验证过了，不需要存）
            //   第3个参数 空列表  → authorities（权限列表，MVP阶段暂不实现）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT认证成功，userId: {}", userId);
        }

        // ========== 第4步：放行，交给下一个环节 ==========
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token
     * 格式：Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // 去掉 "Bearer " 前缀
        }
        return null;
    }
}
