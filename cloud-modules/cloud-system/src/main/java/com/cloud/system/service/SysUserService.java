package com.cloud.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.common.core.domain.PageResult;
import com.cloud.system.domain.SysUser;

/**
 * 系统用户服务。
 */
public interface SysUserService extends IService<SysUser> {

    PageResult<SysUser> pageUsers(long pageNum, long pageSize, String username);

    boolean createUser(SysUser sysUser);
}
