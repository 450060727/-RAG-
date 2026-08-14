package com.xiaoma.server.dto;

/**
 * 发送验证码请求 DTO。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送注册验证码请求。
 *
 * @param email 邮箱
 */
public record SendCodeRequest(
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email) {
}
