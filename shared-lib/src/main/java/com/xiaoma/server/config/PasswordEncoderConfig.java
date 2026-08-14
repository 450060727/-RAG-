/**
 * config 模块的 PasswordEncoderConfig 类/接口定义。
 */
package com.xiaoma.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码器配置。
 * 将 BCryptPasswordEncoder 注册为单例 Bean，避免各 Service/Controller 中重复 new 实例，
 * 同时保证密码哈希强度与随机盐全局一致。
 */
@Configuration
/**
 * PasswordEncoderConfig 类。
 */
public class PasswordEncoderConfig {

    /**
     * 注入 BCrypt 密码编码器。
     *
     * @return BCryptPasswordEncoder 单例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
