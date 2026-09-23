package com.cloud.auth.controller;

import com.cloud.auth.domain.LoginBody;
import com.cloud.auth.service.TokenService;
import com.cloud.common.core.domain.R;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口。通过网关以 {@code /auth/**} 暴露（前缀会被剔除）。
 */
@RestController
@RequestMapping
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginBody loginBody) {
        return R.ok(tokenService.login(loginBody.getUsername(), loginBody.getPassword()));
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestParam(required = false) String tokenKey) {
        tokenService.logout(tokenKey);
        return R.ok();
    }
}
