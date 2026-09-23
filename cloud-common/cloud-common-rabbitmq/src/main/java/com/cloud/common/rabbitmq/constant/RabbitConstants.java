package com.cloud.common.rabbitmq.constant;

/**
 * RabbitMQ 拓扑常量（交换机 / 队列 / 路由键）。
 */
public interface RabbitConstants {

    /** 业务 topic 交换机。 */
    String BUSINESS_EXCHANGE = "cloud.business.exchange";

    /** 绑定到业务交换机的示例业务队列。 */
    String BUSINESS_QUEUE = "cloud.business.queue";

    /** 示例路由键。 */
    String BUSINESS_ROUTING_KEY = "cloud.business.#";

    /** 死信交换机。 */
    String DLX_EXCHANGE = "cloud.dlx.exchange";

    /** 死信队列。 */
    String DLX_QUEUE = "cloud.dlx.queue";

    /** 死信路由键。 */
    String DLX_ROUTING_KEY = "cloud.dlx.key";
}
