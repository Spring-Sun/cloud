package com.cloud.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全 / JWT 相关配置属性（前缀 {@code cloud.security}）。
 */
@Data
@ConfigurationProperties(prefix = "cloud.security")
public class SecurityProperties {

    /** 用于签名 JWT 令牌的 HMAC 密钥，至少 32 个字符。 */
    private String jwtSecret = "cloud-default-secret-key-please-change-in-production-0123456789";

    /** 访问令牌有效期（秒）。 */
    private long tokenExpireSeconds = 7200L;

    /** 携带令牌的 HTTP 请求头。 */
    private String tokenHeader = "Authorization";

    /** 令牌前缀。 */
    private String tokenPrefix = "Bearer ";
}
