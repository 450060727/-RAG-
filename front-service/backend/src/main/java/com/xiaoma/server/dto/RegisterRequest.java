package com.xiaoma.server.dto;

/**
 * 注册请求 DTO。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求，包含邮箱、验证码、密码与昵称。
 *
 * @param email    邮箱
 * @param code     6 位验证码
 * @param password 密码，长度 6-32 位
 * @param name     昵称，最长 20 字
 */
public record RegisterRequest(
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "验证码不能为空") @Size(min = 6, max = 6, message = "验证码为 6 位") String code,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 32, message = "密码长度需为 6-32 位") String password,
        @NotBlank(message = "姓名不能为空") @Size(max = 20, message = "姓名最长 20 字") String name) {
}
