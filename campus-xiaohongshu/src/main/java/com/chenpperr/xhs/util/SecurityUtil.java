package com.chenpperr.xhs.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 *
 * 职责：从 Spring Security 上下文中获取当前登录用户的信息。
 *
 * 回顾一下数据流：
 *   请求进来 → JwtAuthenticationFilter 解析 Token → 拿到 userId
 *            → 塞进 SecurityContext（principal = userId）
 *            → Controller/Service 里通过 SecurityUtil 取出 userId
 *
 * 使用示例（在任何 Service 或 Controller 中）：
 *   Long userId = SecurityUtil.getCurrentUserId();
 */
@Component
public class SecurityUtil {

    /**
     * 获取当前登录用户的 userId
     *
     * 调用链回顾：
     *   JwtAuthenticationFilter 中：
     *     new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())
     *     ↑ 第 1 个参数 userId 就是 principal
     *
     *   这里：
     *     SecurityContextHolder.getContext().getAuthentication().getPrincipal()
     *     ↑ 取出来的就是那个 userId（Long 类型）
     *
     * @return 当前登录用户的 userId
     * @throws RuntimeException 如果用户未登录（Token 无效或过期）
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }

        // principal 就是 JwtAuthenticationFilter 中塞进去的 userId
        Object principal = authentication.getPrincipal();

        if (principal instanceof Long userId) {
            return userId;
        }

        // 兜底：如果 principal 是 String 类型（某些边界情况），尝试转换
        if (principal instanceof String str) {
            return Long.parseLong(str);
        }

        throw new RuntimeException("无法获取用户信息");
    }

    /**
     * 获取当前登录用户的 userId，未登录时返回 null（不抛异常）
     *
     * 适用于：列表页查询时，未登录用户也能看内容，只是不显示"已点赞"状态
     *
     * @return userId 或 null
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
