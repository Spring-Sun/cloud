package com.cloud.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.mybatis.core.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 示例系统用户实体，映射到表 {@code sys_user}。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String nickName;

    private String email;

    private String phone;

    /** 0 = 禁用，1 = 启用 */
    private Integer status;

}
