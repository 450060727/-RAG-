/**
 * 后台管理ChangePassword数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminChangePasswordRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") String newPassword
) {
}
