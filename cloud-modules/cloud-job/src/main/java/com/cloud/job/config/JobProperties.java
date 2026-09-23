package com.cloud.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定时任务相关配置属性（前缀 {@code cloud.job}）。
 */
@Data
@ConfigurationProperties(prefix = "cloud.job")
public class JobProperties {

    /** 是否启用定时调度。 */
    private boolean enabled = true;

    /** 示例任务的 cron 表达式，默认每分钟执行一次。 */
    private String demoCron = "0 * * * * ?";

    /** 分布式锁持有超时时间（秒），需大于任务最长执行时间。 */
    private long lockTimeoutSeconds = 30L;

    /** 分布式锁 Redis 键前缀。 */
    private String lockKeyPrefix = "cloud:job:lock:";

    /** 最近执行记录的 Redis 键前缀。 */
    private String recordKeyPrefix = "cloud:job:last:";

    /** 调度线程池大小。 */
    private int schedulerPoolSize = 4;

}
