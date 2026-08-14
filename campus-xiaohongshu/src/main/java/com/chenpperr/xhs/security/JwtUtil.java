package com.chenpperr.xhs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 *
 * 职责：签发Token、解析Token、验证Token
 * 就像一个"印章工厂"：能盖章（生成）、能验章（验证）、能读章（解析）
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 签发 Token（生成一张"通行证"）
     *
     * @param userId 用户ID
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))    // 把 userId 塞进 Token 里
                .issuedAt(new Date())               // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 过期时间
                .signWith(getSigningKey())          // 用密钥签名（防伪造）
                .compact();
    }

    /**
     * 从 Token 中解析 userId（读取通行证上的信息）
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())        // 验证签名
                .build()
                .parseSignedClaims(token)           // 解析 Token
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证 Token 是否有效（检查通行证是否过期/伪造）
     *
     * @param token JWT Token
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
