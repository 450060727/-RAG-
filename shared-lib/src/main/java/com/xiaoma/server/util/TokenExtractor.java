/**
 * util 模块的 TokenExtractor 类/接口定义。
 */
package com.xiaoma.server.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * HTTP Authorization 头中 Bearer Token 提取工具。
 * 统一处理 "Authorization: Bearer <token>" 的解析逻辑，供拦截器和控制器复用。
 */
@Component
/**
 * TokenExtractor 类。
 */
public class TokenExtractor {

    // Bearer 令牌前缀，按 RFC 6750 规范
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从请求头中提取 token。
     *
     * @param request HTTP 请求
     * @return 提取到的 token；若头不存在或格式不正确则返回 Optional.empty()
     */
    public Optional<String> extract(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }
}
