/**
 * Redis业务服务类。
 */
package com.xiaoma.server.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 通用 Redis 字符串操作服务。
 * 其他业务服务应优先使用本类，而不是直接依赖 StringRedisTemplate。
 */
@Service
public class RedisService {

    private final StringRedisTemplate redis; // redis 字段

    /**
     * 构造 RedisService 实例。
     * @param redis 参数说明
     */
    public RedisService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 设置字符串值，并指定过期时间 */
    /**
     * set 方法。
     * @param key 参数说明
     * @param value 参数说明
     * @param timeout 参数说明
     * @param unit 参数说明
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redis.opsForValue().set(key, value, timeout, unit);
    }

    /** 获取字符串值 */
    /**
     * get 方法。
     * @param key 参数说明
     * @return 返回值说明
     */
    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    /** 删除 key */
    /**
     * delete 方法。
     * @param key 参数说明
     * @return 返回值说明
     */
    public Boolean delete(String key) {
        return redis.delete(key);
    }

    /** 批量删除 key */
    /**
     * delete 方法。
     * @param keys 参数说明
     * @return 返回值说明
     */
    public Long delete(Collection<String> keys) {
        return redis.delete(keys);
    }

    /** 设置字符串值，无过期时间 */
    /**
     * set 方法。
     * @param key 参数说明
     * @param value 参数说明
     */
    public void set(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    /** 按 pattern 查找 key */
    /**
     * keys 方法。
     * @param pattern 参数说明
     * @return 返回值说明
     */
    public Set<String> keys(String pattern) {
        return redis.keys(pattern);
    }

    /** 判断 key 是否存在 */
    /**
     * hasKey 方法。
     * @param key 参数说明
     * @return 返回值说明
     */
    public Boolean hasKey(String key) {
        return redis.hasKey(key);
    }

    /** 获取 key 的剩余过期时间 */
    /**
     * 获取 Expire。
     * @param key 参数说明
     * @param unit 参数说明
     * @return 返回值说明
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redis.getExpire(key, unit);
    }

    /** 重新设置 key 的过期时间 */
    /**
     * expire 方法。
     * @param key 参数说明
     * @param timeout 参数说明
     * @param unit 参数说明
     * @return 返回值说明
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redis.expire(key, timeout, unit);
    }

    /** 向 Set 中添加成员 */
    /**
     * sAdd 方法。
     * @param key 参数说明
     * @param members 参数说明
     * @return 返回值说明
     */
    public Long sAdd(String key, String... members) {
        return redis.opsForSet().add(key, members);
    }

    /** 获取 Set 全部成员 */
    /**
     * sMembers 方法。
     * @param key 参数说明
     * @return 返回值说明
     */
    public Set<String> sMembers(String key) {
        return redis.opsForSet().members(key);
    }

    /** 判断 member 是否在 Set 中 */
    /**
     * sIsMember 方法。
     * @param key 参数说明
     * @param member 参数说明
     * @return 返回值说明
     */
    public Boolean sIsMember(String key, String member) {
        return redis.opsForSet().isMember(key, member);
    }

    /** 删除 Set 中的成员 */
    /**
     * sRemove 方法。
     * @param key 参数说明
     * @param members 参数说明
     * @return 返回值说明
     */
    public Long sRemove(String key, String... members) {
        return redis.opsForSet().remove(key, members);
    }

    /** 按 pattern 扫描并删除匹配的 key（生产慎用，数据量大时用 unlink） */
    /**
     * deleteByPattern 方法。
     * @param pattern 参数说明
     */
    public void deleteByPattern(String pattern) {
        var conn = redis.getConnectionFactory().getConnection();
        var scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();
        try (var cursor = conn.scan(scanOptions)) {
            while (cursor.hasNext()) {
                conn.del(cursor.next());
            }
        }
    }
}
