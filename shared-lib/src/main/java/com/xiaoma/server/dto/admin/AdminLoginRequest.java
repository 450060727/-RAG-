/**
 * 后台管理Login数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password
) {
}
