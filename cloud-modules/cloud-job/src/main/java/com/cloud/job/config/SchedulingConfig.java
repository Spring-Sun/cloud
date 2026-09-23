package com.cloud.job.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 调度线程池配置：为 {@code @Scheduled} 任务提供多线程调度器，避免默认单线程串行执行。
 *
 * <p>当全局开启虚拟线程（{@code spring.threads.virtual.enabled=true}）时，本配置不生效，
 * 改由 Spring Boot 自动装配基于虚拟线程的 {@code SimpleAsyncTaskScheduler} 承载定时任务，
 * 以保持“全局虚拟线程”的一致性。</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.threads.virtual", name = "enabled",
        havingValue = "false", matchIfMissing = true)
public class SchedulingConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(JobProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getSchedulerPoolSize());
        scheduler.setThreadNamePrefix("cloud-job-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }
}
