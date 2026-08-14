package com.xiaoma.server.dto;

/**
 * 更新用户资料请求 DTO。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新用户资料请求。
 *
 * @param name 昵称，最长 20 字
 */
public record UpdateProfileRequest(
        @NotBlank(message = "姓名不能为空") @Size(max = 20, message = "姓名最长 20 字") String name) {
}
