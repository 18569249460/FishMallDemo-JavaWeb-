package com.fishshop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类。
 *
 * <p>负责生成登录 Token 和解析 Token 中的用户 ID、角色信息，供拦截器做鉴权。</p>
 */
@Component
public class JwtUtil {
    private final Key key;
    private final long expirationMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expirationMillis}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /**
     * 登录成功后生成 Token，subject 存用户 ID，claim 中存用户角色。
     */
    public String generateToken(Integer userId, Integer role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token；签名错误、过期或格式非法时会抛出运行时异常，由拦截器统一处理。
     */
    public JwtPayload parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Integer userId = Integer.valueOf(claims.getSubject());
        Integer role = claims.get("role", Integer.class);
        return new JwtPayload(userId, role);
    }

    /**
     * Token 解析后的最小用户上下文，避免在请求中传递完整用户对象。
     */
    public static class JwtPayload {
        private final Integer userId;
        private final Integer role;

        public JwtPayload(Integer userId, Integer role) {
            this.userId = userId;
            this.role = role;
        }

        public Integer getUserId() {
            return userId;
        }

        public Integer getRole() {
            return role;
        }
    }
}
