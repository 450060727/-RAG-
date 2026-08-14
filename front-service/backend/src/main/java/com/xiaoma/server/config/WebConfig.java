package com.xiaoma.server.config;

/**
 * 前台 Web 配置：注册拦截器与跨域映射。
 * CORS 默认 dev 宽松；生产环境通过 cors.allowed-origins 配置具体域名。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    // 生产环境允许的跨域来源列表，多个用逗号分隔；为空时回退到 dev 宽松模式
    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 构造前台 Web 配置。
     *
     * @param authInterceptor 前台认证拦截器
     */
    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * 注册拦截器：拦截 /api/**，排除 /api/auth/**。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }

    /**
     * 配置跨域映射，根据 cors.allowed-origins 区分 dev 与生产环境。
     *
     * @param registry 跨域注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = parseOrigins(allowedOrigins);
        var registration = registry.addMapping("/**")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        if (origins.isEmpty()) {
            // dev 环境：允许所有来源，注意 allowCredentials(true) 时必须用 allowedOriginPatterns
            registration.allowedOriginPatterns("*");
        } else {
            // 生产环境：只允许配置的域名
            registration.allowedOrigins(origins.toArray(new String[0]));
        }
    }

    /**
     * 解析配置的跨域来源列表。
     *
     * @param raw 原始字符串，多个来源用逗号分隔
     * @return 来源列表
     */
    private List<String> parseOrigins(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
