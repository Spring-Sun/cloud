package com.cloud.job.domain;

/**
 * 定时任务执行结果。
 *
 * @param jobName    任务名称
 * @param executed   本次是否真正执行（未抢到锁时为 false）
 * @param message    结果描述
 * @param executedAt 执行时间（未执行时为 null）
 */
public record JobExecResult(String jobName, boolean executed, String message, String executedAt) {
}
