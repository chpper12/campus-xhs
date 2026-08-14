package com.chenpperr.xhs.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 轻量级鉴权拦截器
 *
 * 从请求头中获取 userId 并存入 ThreadLocal
 */
@Slf4j
@Component
public class UserIdInterceptor implements HandlerInterceptor {

    /**
     * ThreadLocal 存储当前请求的 userId
     */
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的 userId
        String userIdStr = request.getHeader("userId");

        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                USER_ID_HOLDER.set(userId);
                log.debug("获取到用户ID：{}", userId);
            } catch (NumberFormatException e) {
                log.warn("userId格式错误：{}", userIdStr);
                // 不拦截，允许继续访问（某些接口可能不需要userId）
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清除 ThreadLocal，防止内存泄漏
        USER_ID_HOLDER.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return USER_ID_HOLDER.get();
    }
}
