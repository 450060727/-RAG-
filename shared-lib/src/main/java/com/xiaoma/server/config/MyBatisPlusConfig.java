/**
 * MyBatisPlus配置类。
 */
package com.xiaoma.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatisPlus配置类。
 * 本类定义了 MyBatisPlusConfig 的公共契约与数据结构。
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * mybatisPlusInterceptor 方法。
     * @return 返回值说明
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
