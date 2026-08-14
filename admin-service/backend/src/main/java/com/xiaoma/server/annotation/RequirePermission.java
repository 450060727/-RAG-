package com.xiaoma.server.annotation;

/**
 * 方法级权限注解，标记后端接口所需的权限码。
 * 超级管理员自动跳过校验。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 所需的权限码。
     *
     * @return 权限码字符串
     */
    String value();
}
