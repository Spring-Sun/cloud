package com.cloud.auth.service;

import com.cloud.common.redis.service.RedisService;
import com.cloud.common.security.model.LoginUser;
import com.cloud.common.security.utils.JwtUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 签发访问令牌，并将登录用户缓存到 Redis。
 */
@Service
public class TokenService {

    private static final String LOGIN_TOKEN_KEY = "cloud:login:token:";

    private final JwtUtils jwtUtils;
    private final RedisService redisService;

    public TokenService(JwtUtils jwtUtils, RedisService redisService) {
        this.jwtUtils = jwtUtils;
        this.redisService = redisService;
    }

    /**
     * 为演示账号构造登录用户并创建访问令牌。
     */
    public Map<String, Object> login(String username, String password) {
        // 注意：请将此演示校验替换为对 cloud-system / 真实用户存储的调用。
        if (!"admin".equals(username) || !"admin123".equals(password)) {
            throw new com.cloud.common.core.exception.BusinessException(400, "用户名或密码错误");
        }

        LoginUser loginUser = new LoginUser(1L, username);
        loginUser.setRoles(Set.of("admin"));
        loginUser.setPermissions(Set.of("*:*:*"));

        String tokenKey = UUID.randomUUID().toString();
        String accessToken = jwtUtils.createToken(loginUser);
        redisService.set(LOGIN_TOKEN_KEY + tokenKey, loginUser,
                jwtUtils.getTokenExpireSeconds(), TimeUnit.SECONDS);

        Map<String, Object> result = new HashMap<>();
        result.put("access_token", accessToken);
        result.put("token_key", tokenKey);
        result.put("expires_in", jwtUtils.getTokenExpireSeconds());
        return result;
    }

    /**
     * 移除缓存的登录用户（登出）。
     */
    public void logout(String tokenKey) {
        if (tokenKey != null && !tokenKey.isBlank()) {
            redisService.delete(LOGIN_TOKEN_KEY + tokenKey);
        }
    }
}
