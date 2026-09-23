package com.cloud.common.security.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 已认证的用户主体，承载于 JWT 以及请求级用户上下文中。
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private Set<String> roles;
    private Set<String> permissions;

    public LoginUser() {
    }

    public LoginUser(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
