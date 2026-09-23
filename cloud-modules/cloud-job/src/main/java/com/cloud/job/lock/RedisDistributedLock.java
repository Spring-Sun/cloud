package com.cloud.job.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 基于 Redis 的简易分布式锁，保证多实例部署时定时任务只被一个节点执行。
 *
 * <p>加锁使用 {@code SET key value NX PX ttl} 保证原子性；解锁使用 Lua 脚本
 * 校验持有者后再删除，避免误删其他节点的锁。</p>
 */
@Component
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    /** 解锁脚本：仅当值匹配（持有者一致）时才删除键。 */
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取锁。
     *
     * @param key   锁键
     * @param ttl   锁自动过期时间，防止持有者宕机导致死锁
     * @return 获取成功返回锁令牌（用于解锁），失败返回 {@code null}
     */
    public String tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        if (Boolean.TRUE.equals(acquired)) {
            return token;
        }
        return null;
    }

    /**
     * 释放锁（仅释放自己持有的锁）。
     *
     * @param key   锁键
     * @param token 加锁时返回的令牌
     */
    public void unlock(String key, String token) {
        if (token == null) {
            return;
        }
        try {
            redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
        } catch (Exception e) {
            log.warn("释放分布式锁失败, key={}, 原因={}", key, e.getMessage());
        }
    }
}
