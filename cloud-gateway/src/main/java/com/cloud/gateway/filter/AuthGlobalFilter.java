package com.cloud.gateway.filter;

import com.cloud.common.core.constant.CommonConstants;
import com.cloud.gateway.config.GatewayAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 全局认证过滤器。对每个非白名单请求校验 Bearer 令牌，
 * 然后通过内部请求头将可信的用户身份透传给下游服务。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final GatewayAuthProperties properties;
    private final SecretKey secretKey;

    public AuthGlobalFilter(GatewayAuthProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!properties.isEnabled() || isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "缺少访问令牌");
        }

        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();
            String userId = String.valueOf(claims.get("userId"));
            String username = claims.get("username", String.class);

            ServerHttpRequest mutated = request.mutate()
                    // 剔除客户端伪造的内部请求头，再设置可信值
                    .headers(headers -> {
                        headers.remove(CommonConstants.HEADER_USER_ID);
                        headers.remove(CommonConstants.HEADER_USERNAME);
                        if (StringUtils.hasText(userId) && !"null".equals(userId)) {
                            headers.set(CommonConstants.HEADER_USER_ID, userId);
                        }
                        if (StringUtils.hasText(username)) {
                            headers.set(CommonConstants.HEADER_USERNAME, username);
                        }
                    })
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            log.debug("invalid token for path {}: {}", path, e.getMessage());
            return unauthorized(exchange, "令牌无效或已过期");
        }
    }

    private boolean isWhiteListed(String path) {
        for (String pattern : properties.getWhiteList()) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String resolveToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(CommonConstants.HEADER_AUTHORIZATION);
        if (StringUtils.hasText(header)) {
            return header.startsWith(CommonConstants.TOKEN_PREFIX)
                    ? header.substring(CommonConstants.TOKEN_PREFIX.length())
                    : header;
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"msg\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
