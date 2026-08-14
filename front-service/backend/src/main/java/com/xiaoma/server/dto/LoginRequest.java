package com.xiaoma.server.dto;

/**
 * 登录请求 DTO。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求，包含邮箱与密码。
 *
 * @param email    邮箱
 * @param password 密码
 */
public record LoginRequest(
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "密码不能为空") String password) {
}
