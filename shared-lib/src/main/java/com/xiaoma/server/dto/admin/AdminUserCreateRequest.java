/**
 * 后台管理UserCreate数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminUserCreateRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "姓名不能为空") String realName,
        String phone,
        String email,
        @NotNull(message = "部门不能为空") Long deptId,
        List<Long> roleIds
) {
}
