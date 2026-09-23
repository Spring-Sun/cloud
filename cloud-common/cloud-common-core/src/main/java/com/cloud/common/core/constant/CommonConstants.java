package com.cloud.common.core.constant;

/**
 * 各服务共享的通用常量。
 */
public interface CommonConstants {

    /** 携带已认证用户 ID 的请求头（由网关注入）。 */
    String HEADER_USER_ID = "X-User-Id";

    /** 携带已认证用户名的请求头（由网关注入）。 */
    String HEADER_USERNAME = "X-Username";

    /** 认证请求头。 */
    String HEADER_AUTHORIZATION = "Authorization";

    /** Bearer 令牌前缀。 */
    String TOKEN_PREFIX = "Bearer ";

    /** UTF-8 字符集名称。 */
    String UTF8 = "UTF-8";
}
