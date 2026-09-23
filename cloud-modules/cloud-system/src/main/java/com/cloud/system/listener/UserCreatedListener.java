package com.cloud.system.listener;

import com.cloud.common.rabbitmq.constant.RabbitConstants;
import com.cloud.system.domain.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 监听共享业务队列的示例消费者。
 */
@Component
public class UserCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedListener.class);

    @RabbitListener(queues = RabbitConstants.BUSINESS_QUEUE)
    public void onUserCreated(SysUser sysUser) {
        log.info("received user-created event: userId={}, username={}",
                sysUser.getUserId(), sysUser.getUsername());
    }
}
