package com.xiaoma.server.dto;

/**
 * 修改密码请求 DTO。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求，包含原密码与新密码。
 *
 * @param oldPassword 原密码
 * @param newPassword 新密码，长度 6-32 位
 */
public record ChangePasswordRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") @Size(min = 6, max = 32, message = "密码长度需为 6-32 位") String newPassword) {
}
