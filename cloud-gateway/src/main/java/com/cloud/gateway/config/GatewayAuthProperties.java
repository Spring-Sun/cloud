package com.cloud.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关认证配置属性（前缀 {@code cloud.gateway.auth}）。
 */
@Data
@ConfigurationProperties(prefix = "cloud.gateway.auth")
public class GatewayAuthProperties {

    /** 是否开启网关的令牌认证。 */
    private boolean enabled = true;

    /** 用于校验 JWT 令牌的 HMAC 密钥（必须与认证服务一致）。 */
    private String jwtSecret = "cloud-default-secret-key-please-change-in-production-0123456789";

    /** 免认证的 Ant 风格路径白名单。 */
    private List<String> whiteList = new ArrayList<>();
}
