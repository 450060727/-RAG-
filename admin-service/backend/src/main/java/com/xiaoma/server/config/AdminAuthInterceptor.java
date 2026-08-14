package com.xiaoma.server.config;

/**
 * 后台管理接口认证拦截器。
 * 仅拦截 /api/admin/**，会话 key 使用 xiaoma:admin:session: 前缀，与 C 端隔离。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.AuthContext;
import com.xiaoma.server.common.AuthResponseWriter;
import com.xiaoma.server.entity.admin.SysUser;
import com.xiaoma.server.enums.UserStatus;
import com.xiaoma.server.mapper.admin.SysUserMapper;
import com.xiaoma.server.service.RedisService;
import com.xiaoma.server.service.kb.KbModelConfigService;
import com.xiaoma.server.util.JwtUtil;
import com.xiaoma.server.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthInterceptor.class);

    // 后台会话在 Redis 中的前缀
    public static final String SESSION_PREFIX = "xiaoma:admin:session:";
    // 请求属性中保存后台管理员 ID 的 key
    public static final String ADMIN_ID_ATTR = "adminId";

    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final SysUserMapper sysUserMapper;
    private final KbModelConfigService modelConfigService;
    private final TokenExtractor tokenExtractor;

    /**
     * 构造后台认证拦截器。
     *
     * @param jwtUtil           JWT 工具
     * @param redisService      Redis 服务
     * @param sysUserMapper     用户 Mapper
     * @param modelConfigService 模型配置服务
     * @param tokenExtractor    Token 提取器
     */
    public AdminAuthInterceptor(JwtUtil jwtUtil,
                                RedisService redisService,
                                SysUserMapper sysUserMapper,
                                KbModelConfigService modelConfigService,
                                TokenExtractor tokenExtractor) {
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
        this.sysUserMapper = sysUserMapper;
        this.modelConfigService = modelConfigService;
        this.tokenExtractor = tokenExtractor;
    }

    /**
     * 获取后台会话 TTL。
     *
     * @return 会话有效期
     */
    private Duration sessionTtl() {
        return Duration.ofMinutes(modelConfigService.current().getSessionTtlMinutes());
    }

    /**
     * 获取会话续期阈值：剩余时间小于该值时自动续期。
     *
     * @return 续期阈值
     */
    private Duration renewThreshold() {
        return Duration.ofMinutes(modelConfigService.current().getSessionRenewThresholdMinutes());
    }

    /**
     * 请求前置处理：放行 OPTIONS，校验 token 与会话有效性，写入线程上下文。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return 是否放行
     * @throws Exception 处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行 CORS 预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        Optional<String> tokenOpt = tokenExtractor.extract(request);
        if (tokenOpt.isPresent()) {
            String token = tokenOpt.get();
            try {
                Long adminId = jwtUtil.parseUserId(token);
                String type = jwtUtil.parseType(token);
                if (!"admin".equals(type)) {
                    throw new IllegalStateException("token type mismatch: " + type);
                }
                String redisUid = redisService.get(SESSION_PREFIX + token);
                if (redisUid == null || !redisUid.equals(String.valueOf(adminId))) {
                    throw new IllegalStateException("session not found");
                }
                renewIfNeeded(token);

                SysUser user = sysUserMapper.selectById(adminId);
                // status == 1 表示账号被禁用
                if (user == null || UserStatus.isDisabled(user.getStatus())) {
                    redisService.delete(SESSION_PREFIX + token);
                    log.warn("后台管理员 [{}] 账号已被禁用或不存在，拒绝访问 {}", adminId, request.getRequestURI());
                    AuthResponseWriter.writeForbidden(response, "账号已被禁用，请联系管理员");
                    return false;
                }

                // 将当前管理员写入请求属性与线程上下文
                request.setAttribute(ADMIN_ID_ATTR, adminId);
                AuthContext.set(adminId, "admin");
                return true;
            } catch (Exception e) {
                // token 过期/非法或 Redis 会话不存在，记录原因后返回 401
                log.warn("后台 token 校验失败 [{}]: {}", request.getRequestURI(), e.getMessage());
            }
        }

        AuthResponseWriter.writeUnauthorized(response);
        return false;
    }

    /**
     * 请求完成后清理线程上下文，防止线程池复用导致泄漏。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @param ex       处理异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束后清理线程上下文，防止线程池复用导致泄漏
        AuthContext.clear();
    }

    /**
     * 会话续期：当剩余有效期低于阈值时自动延长。
     *
     * @param token 访问令牌
     */
    private void renewIfNeeded(String token) {
        Long ttlSeconds = redisService.getExpire(SESSION_PREFIX + token, TimeUnit.SECONDS);
        if (ttlSeconds == null || ttlSeconds < 0) {
            return;
        }
        if (ttlSeconds < renewThreshold().getSeconds()) {
            redisService.expire(SESSION_PREFIX + token, sessionTtl().toMinutes(), TimeUnit.MINUTES);
        }
    }
}
