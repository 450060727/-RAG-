package com.xiaoma.server;

/**
 * 前台服务启动类。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

import com.xiaoma.server.config.KbProperties;

@SpringBootApplication
@EnableConfigurationProperties(KbProperties.class)
@EnableRetry
public class ServerApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
