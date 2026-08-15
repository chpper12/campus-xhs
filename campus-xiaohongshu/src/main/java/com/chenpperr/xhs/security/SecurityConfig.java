package com.chenpperr.xhs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Spring Security 配置（安保公司总配置）
 *
 * 职责：
 * 1. 告诉 Spring Security 哪些接口需要"验票"，哪些可以"免票通行"
 * 2. 把我们的 JWT过滤器 注册到 Spring Security 的过滤器链中
 * 3. 禁用不需要的功能（CSRF、Session）
 *
 * 就像安保公司的规章制度：哪些区域需要通行证，哪些区域随便进，
 * 以及安检员（JWT过滤器）站在哪个位置。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ========== 1. 禁用 CSRF（前后端分离不需要） ==========
            .csrf(AbstractHttpConfigurer::disable)

            // ========== 2. 设置为无状态模式（不用 Session，用 JWT） ==========
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ========== 3. 配置哪些接口需要认证、哪些放行 ==========
            .authorizeHttpRequests(auth -> auth
                // 认证接口：登录和注册都可以免 Token 访问
                .requestMatchers("/api/v1/auth/**").permitAll()
                // 上传接口：放行（或由前端直接传 OSS）
                .requestMatchers("/api/v1/upload/**").permitAll()
                // AI润色接口：放行（不需要 Token）
                .requestMatchers("/api/v1/posts/ai-polish").permitAll()
                // 静态资源：上传的图片可以公开访问
                .requestMatchers("/uploads/**").permitAll()
                // 其他所有接口：必须携带有效 Token
                .anyRequest().authenticated()
            )

            // ========== 4. 把 JWT 过滤器加到 Spring Security 过滤器链中 ==========
            // 位置：在 UsernamePasswordAuthenticationFilter 之前
            // 这样请求进来时，先经过我们的 JWT 过滤器验票，再走后面的流程
            .addFilterBefore(jwtAuthenticationFilter,
                           UsernamePasswordAuthenticationFilter.class)

            // ========== 5. 自定义未认证时的响应（返回 JSON 而不是默认页面） ==========
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write(objectMapper.writeValueAsString(
                        com.chenpperr.xhs.common.Result.error(401, "未认证，请先登录")
                    ));
                })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
