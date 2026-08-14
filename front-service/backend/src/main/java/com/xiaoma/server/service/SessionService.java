package com.xiaoma.server.service;

/**
 * 用户会话管理服务。
 * 底层 Redis 操作委托给 {@link RedisService}，本类只封装会话语义。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.service.kb.KbModelConfigService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    // 前台会话在 Redis 中的前缀
    private static final String KEY_PREFIX = "xiaoma:session:";

    private final RedisService redisService;
    private final KbModelConfigService modelConfigService;

    /**
     * 构造用户会话管理服务。
     *
     * @param redisService       Redis 服务
     * @param modelConfigService 模型配置服务
     */
    public SessionService(RedisService redisService, KbModelConfigService modelConfigService) {
        this.redisService = redisService;
        this.modelConfigService = modelConfigService;
    }

    /**
     * 获取会话 TTL。
     *
     * @return 会话有效期
     */
    private Duration sessionTtl() {
        return Duration.ofMinutes(modelConfigService.current().getSessionTtlMinutes());
    }

    /**
     * 获取会话续期阈值。
     *
     * @return 续期阈值
     */
    private Duration renewThreshold() {
        return Duration.ofMinutes(modelConfigService.current().getSessionRenewThresholdMinutes());
    }

    /**
     * 构造 Redis 会话 key。
     *
     * @param token 访问令牌
     * @return Redis key
     */
    private String key(String token) {
        return KEY_PREFIX + token;
    }

    /**
     * 创建/刷新会话。
     *
     * @param token  访问令牌
     * @param userId 用户 ID
     */
    public void create(String token, Long userId) {
        redisService.set(key(token), String.valueOf(userId), sessionTtl().toMinutes(), TimeUnit.MINUTES);
    }

    /**
     * 校验会话是否存在，返回 userId。
     *
     * @param token 访问令牌
     * @return 用户 ID，会话不存在返回 null
     */
    public Long validate(String token) {
        String userId = redisService.get(key(token));
        return userId == null ? null : Long.valueOf(userId);
    }

    /**
     * 删除会话。
     *
     * @param token 访问令牌
     */
    public void delete(String token) {
        if (token != null && !token.isBlank()) {
            redisService.delete(key(token));
        }
    }

    /**
     * 当剩余 TTL 小于阈值时续签，返回是否执行了续签。
     *
     * @param token 访问令牌
     * @return true 表示执行了续签
     */
    public boolean renewIfNeeded(String token) {
        Long ttlSeconds = redisService.getExpire(key(token), TimeUnit.SECONDS);
        if (ttlSeconds == null || ttlSeconds < 0) {
            return false;
        }
        if (ttlSeconds < renewThreshold().getSeconds()) {
            redisService.expire(key(token), sessionTtl().toMinutes(), TimeUnit.MINUTES);
            return true;
        }
        return false;
    }
}
