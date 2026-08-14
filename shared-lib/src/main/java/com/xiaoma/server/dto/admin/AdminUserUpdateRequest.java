/**
 * 后台管理UserUpdate数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminUserUpdateRequest(
        @NotBlank(message = "姓名不能为空") String realName,
        String phone,
        String email,
        @NotNull(message = "部门不能为空") Long deptId,
        List<Long> roleIds,
        Integer status
) {
}
