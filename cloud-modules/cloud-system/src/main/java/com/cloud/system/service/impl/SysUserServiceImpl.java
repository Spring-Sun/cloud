package com.cloud.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.common.core.domain.PageResult;
import com.cloud.common.rabbitmq.constant.RabbitConstants;
import com.cloud.system.domain.SysUser;
import com.cloud.system.mapper.SysUserMapper;
import com.cloud.system.service.SysUserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 基于 MyBatis-Plus 的 {@link SysUserService} 默认实现。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final RabbitTemplate rabbitTemplate;

    public SysUserServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public PageResult<SysUser> pageUsers(long pageNum, long pageSize, String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .orderByDesc(SysUser::getUserId);
        Page<SysUser> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean createUser(SysUser sysUser) {
        if (sysUser.getStatus() == null) {
            sysUser.setStatus(1);
        }
        boolean saved = this.save(sysUser);
        if (saved) {
            // 向 RabbitMQ 发布领域事件（通过共享转换器的 JSON 消息）
            rabbitTemplate.convertAndSend(RabbitConstants.BUSINESS_EXCHANGE,
                    "cloud.business.user.created", sysUser);
        }
        return saved;
    }
}
