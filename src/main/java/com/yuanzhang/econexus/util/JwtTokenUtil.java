package com.yuanzhang.econexus.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT令牌工具类 - 用于生成和解析密码重置令牌
 */
@Component
public class JwtTokenUtil {

    @Value("${app.reset-password.jwt-secret}")
    private String jwtSecret;

    @Value("${app.reset-password.token-expiration-minutes}")
    private long tokenExpirationMinutes;

    /**
     * 生成密码重置令牌
     */
    public String generateResetToken(String userId) {
        // 创建密钥
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // 计算过期时间
        long expirationMillis = tokenExpirationMinutes * 60 * 1000;
        Date expirationDate = new Date(System.currentTimeMillis() + expirationMillis);

        // 生成JWT令牌
        return Jwts.builder()
                .setSubject(userId)  // 存储用户ID
                .setIssuedAt(new Date())  // 签发时间
                .setExpiration(expirationDate)  // 过期时间
                .signWith(key)  // 签名
                .compact();
    }

    /**
     * 解析重置令牌，获取用户ID
     */
    public String extractUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // 令牌无效或过期
        }
    }

    /**
     * 验证令牌是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}