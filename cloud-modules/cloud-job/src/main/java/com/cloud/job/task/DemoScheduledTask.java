package com.cloud.job.task;

import com.cloud.job.config.JobProperties;
import com.cloud.job.domain.JobExecResult;
import com.cloud.job.lock.RedisDistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 示例定时任务。通过 Redis 分布式锁保证集群环境下同一时刻仅一个实例执行。
 */
@Component
public class DemoScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(DemoScheduledTask.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String JOB_NAME = "demoJob";

    private final RedisDistributedLock distributedLock;
    private final JobProperties properties;
    private final StringRedisTemplate redisTemplate;

    public DemoScheduledTask(RedisDistributedLock distributedLock,
                             JobProperties properties,
                             StringRedisTemplate redisTemplate) {
        this.distributedLock = distributedLock;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 由调度器按 cron 触发。cron 表达式来自配置 {@code cloud.job.demo-cron}。
     */
    @Scheduled(cron = "${cloud.job.demo-cron:0 * * * * ?}")
    public void scheduledRun() {
        if (!properties.isEnabled()) {
            return;
        }
        execute();
    }

    /**
     * 执行任务（供调度器与手动触发共用）。
     *
     * @return 执行结果；未抢到锁时 {@code executed=false}
     */
    public JobExecResult execute() {
        String lockKey = properties.getLockKeyPrefix() + JOB_NAME;
        String token = distributedLock.tryLock(lockKey, Duration.ofSeconds(properties.getLockTimeoutSeconds()));
        if (token == null) {
            log.debug("任务 {} 未获取到分布式锁，跳过本次执行", JOB_NAME);
            return new JobExecResult(JOB_NAME, false, "其他实例正在执行，已跳过", null);
        }
        try {
            String executedAt = LocalDateTime.now().format(TS);
            // ==== 实际业务逻辑占位：在此编写你的定时任务处理 ====
            log.info("定时任务 {} 开始执行, 时间={}", JOB_NAME, executedAt);

            // 记录最近一次执行时间，便于查询与排查
            redisTemplate.opsForValue().set(properties.getRecordKeyPrefix() + JOB_NAME, executedAt);
            return new JobExecResult(JOB_NAME, true, "执行成功", executedAt);
        } finally {
            distributedLock.unlock(lockKey, token);
        }
    }

    /**
     * 查询最近一次执行时间。
     */
    public String lastExecutedAt() {
        return redisTemplate.opsForValue().get(properties.getRecordKeyPrefix() + JOB_NAME);
    }

    public String getJobName() {
        return JOB_NAME;
    }
}
