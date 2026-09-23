package com.cloud.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link SysUser} 的 MyBatis-Plus Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
