/**
 * Jwt工具类。
 */
package com.xiaoma.server.util;

import com.xiaoma.server.entity.kb.KbModelConfig;
import com.xiaoma.server.service.kb.KbModelConfigService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Jwt工具类。
 * 本类定义了 JwtUtil 的公共契约与数据结构。
 */
@Component
public class JwtUtil {

    private final SecretKey key; // key 字段
    private final long expireMillis; // expireMillis 字段

    /**
     * 构造 JwtUtil 实例。
     * 优先从 kb_model_config 默认配置读取 JWT 参数，未配置则使用本地开发默认值。
     */
    public JwtUtil(KbModelConfigService configService) {
        KbModelConfig config = configService.current();
        String secret = config.getJwtSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("kb_model_config 默认配置缺少 jwt_secret");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("jwt_secret 必须 >= 32 字节（HS256）");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Integer expireMinutes = config.getJwtExpireMinutes();
        this.expireMillis = (expireMinutes == null ? 1440 : expireMinutes) * 60_000L;
    }

    /** C 端用户 token，type=user */
    /**
     * generate 方法。
     * @param userId 参数说明
     * @return 返回值说明
     */
    public String generate(Long userId) {
        return generate(userId, "user");
    }

    /** 指定 token 类型（user/admin），用于前后台会话隔离 */
    /**
     * generate 方法。
     * @param userId 参数说明
     * @param type 参数说明
     * @return 返回值说明
     */
    public String generate(Long userId, String type) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /** 解析失败（过期/篡改）抛 JwtException，由调用方处理 */
    /**
     * parseUserId 方法。
     * @param token 参数说明
     * @return 返回值说明
     */
    public Long parseUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }

    /** 获取 JWT 中的 type claim，默认 user */
    /**
     * parseType 方法。
     * @param token 参数说明
     * @return 返回值说明
     */
    public String parseType(String token) {
        Object type = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type");
        return type == null ? "user" : String.valueOf(type);
    }
}
