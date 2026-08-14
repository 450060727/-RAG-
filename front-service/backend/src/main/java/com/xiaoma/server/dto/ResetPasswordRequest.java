package com.xiaoma.server.dto;

/**
 * 重置密码请求 DTO。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求，包含邮箱、验证码与新密码。
 *
 * @param email    邮箱
 * @param code     6 位验证码
 * @param password 新密码，长度 6-32 位
 */
public record ResetPasswordRequest(
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "验证码不能为空") @Size(min = 6, max = 6, message = "验证码为 6 位") String code,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 32, message = "密码长度需为 6-32 位") String password) {
}
