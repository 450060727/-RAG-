package com.xiaoma.server.config;

/**
 * 前台（C 端）接口认证拦截器。
 * 拦截 /api/** 路径，排除 /api/auth/**；通过 Authorization: Bearer <token> 校验用户身份与会话有效性。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.AuthContext;
import com.xiaoma.server.common.AuthResponseWriter;
import com.xiaoma.server.entity.User;
import com.xiaoma.server.enums.UserStatus;
import com.xiaoma.server.mapper.UserMapper;
import com.xiaoma.server.service.SessionService;
import com.xiaoma.server.util.JwtUtil;
import com.xiaoma.server.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    // 请求属性中保存当前用户 ID 的 key，保持与旧代码兼容
    public static final String UID_ATTR = "uid";

    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    private final UserMapper userMapper;
    private final TokenExtractor tokenExtractor;

    /**
     * 构造前台认证拦截器。
     *
     * @param jwtUtil         JWT 工具
     * @param sessionService  会话服务
     * @param userMapper      用户 Mapper
     * @param tokenExtractor  Token 提取器
     */
    public AuthInterceptor(JwtUtil jwtUtil,
                           SessionService sessionService,
                           UserMapper userMapper,
                           TokenExtractor tokenExtractor) {
        this.jwtUtil = jwtUtil;
        this.sessionService = sessionService;
        this.userMapper = userMapper;
        this.tokenExtractor = tokenExtractor;
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
                Long uid = jwtUtil.parseUserId(token);
                Long redisUid = sessionService.validate(token);
                if (redisUid == null || !redisUid.equals(uid)) {
                    throw new IllegalStateException("session not found");
                }
                sessionService.renewIfNeeded(token);

                User u = userMapper.selectById(uid);
                // status == 1 表示账号被禁用
                if (u == null || UserStatus.isDisabled(u.getStatus())) {
                    sessionService.delete(token);
                    log.warn("前台用户 [{}] 账号已被禁用或不存在，拒绝访问 {}", uid, request.getRequestURI());
                    AuthResponseWriter.writeForbidden(response, "账号已被禁用，请联系管理员");
                    return false;
                }

                // 将当前用户写入请求属性与线程上下文
                request.setAttribute(UID_ATTR, uid);
                AuthContext.set(uid, "user");
                return true;
            } catch (Exception e) {
                // token 过期/非法或 Redis 会话不存在，记录原因后返回 401
                log.warn("前台 token 校验失败 [{}]: {}", request.getRequestURI(), e.getMessage());
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
}
