package com.cloud.system.controller;

import com.cloud.common.core.domain.PageResult;
import com.cloud.common.core.domain.R;
import com.cloud.common.security.context.UserContext;
import com.cloud.system.domain.SysUser;
import com.cloud.system.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统用户增删改查。通过网关以 {@code /system/user/**} 访问。
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/page")
    public R<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") long pageNum,
                                       @RequestParam(defaultValue = "10") long pageSize,
                                       @RequestParam(required = false) String username) {
        return R.ok(sysUserService.pageUsers(pageNum, pageSize, username));
    }

    @GetMapping("/{userId}")
    public R<SysUser> getById(@PathVariable Long userId) {
        return R.ok(sysUserService.getById(userId));
    }

    @PostMapping
    public R<Boolean> create(@Valid @RequestBody SysUser sysUser) {
        return R.ok(sysUserService.createUser(sysUser));
    }

    @PutMapping
    public R<Boolean> update(@RequestBody SysUser sysUser) {
        return R.ok(sysUserService.updateById(sysUser));
    }

    @DeleteMapping("/{userId}")
    public R<Boolean> delete(@PathVariable Long userId) {
        return R.ok(sysUserService.removeById(userId));
    }

    /** 回显网关转发的用户身份，用于验证请求头透传是否生效。 */
    @GetMapping("/me")
    public R<String> me() {
        return R.ok("current user: " + UserContext.getUsername() + " (id=" + UserContext.getUserId() + ")");
    }
}
