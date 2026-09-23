package com.cloud.common.security.utils;

import com.cloud.common.security.config.SecurityProperties;
import com.cloud.common.security.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 jjwt 0.12.x API 的 JWT 创建/解析工具。
 */
public class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";

    private final SecurityProperties properties;
    private final SecretKey secretKey;

    public JwtUtils(SecurityProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为指定用户创建已签名的访问令牌。
     */
    public String createToken(LoginUser loginUser) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + properties.getTokenExpireSeconds() * 1000L);
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, loginUser.getUserId());
        claims.put(CLAIM_USERNAME, loginUser.getUsername());
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(loginUser.getUserId()))
                .issuedAt(new Date(now))
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析并校验令牌，返回声明（claims）负载。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌声明中提取 {@link LoginUser}。
     */
    public LoginUser getLoginUser(String token) {
        Claims claims = parseToken(token);
        LoginUser loginUser = new LoginUser();
        Object userId = claims.get(CLAIM_USER_ID);
        if (userId != null) {
            loginUser.setUserId(Long.valueOf(userId.toString()));
        }
        loginUser.setUsername(claims.get(CLAIM_USERNAME, String.class));
        return loginUser;
    }

    public long getTokenExpireSeconds() {
        return properties.getTokenExpireSeconds();
    }
}
