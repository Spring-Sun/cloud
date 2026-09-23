package com.cloud.common.security.context;

import com.cloud.common.security.model.LoginUser;

/**
 * 当前已认证用户的请求级持有器，由网关注入的请求头填充。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static String getUsername() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
