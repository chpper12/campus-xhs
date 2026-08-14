package com.chenpperr.xhs.resolver;

import com.chenpperr.xhs.annotation.CurrentUserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 用户ID参数解析器（收银员）
 *
 * 职责：
 * 当 Controller 方法参数上标注了 @CurrentUserId 时，
 * 自动从 SecurityContext（全局上下文）中取出 userId，注入到参数中。
 *
 * 就像收银员：顾客（Controller）说"我要当前用户ID"，
 * 收银员就去后台仓库（SecurityContext）里取出来递给顾客。
 */
@Slf4j
@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 判断参数是否有 @CurrentUserId 注解，且类型是 Long
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // 从 SecurityContext 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            Long userId = (Long) authentication.getPrincipal();
            log.debug("解析用户ID参数：{}", userId);
            return userId;
        }

        log.warn("无法获取当前用户ID");
        return null;
    }
}
