/**
 * 后台管理Role数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminRoleRequest(
        @NotBlank(message = "角色名称不能为空") String name,
        @NotBlank(message = "角色编码不能为空") String code,
        @NotBlank(message = "数据范围不能为空") String dataScope,
        String remark,
        List<Long> deptIds
) {
}
