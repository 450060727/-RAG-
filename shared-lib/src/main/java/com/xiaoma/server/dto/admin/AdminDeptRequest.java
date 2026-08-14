/**
 * 后台管理Dept数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminDeptRequest(
        Long parentId,
        @NotBlank(message = "部门名称不能为空") String name,
        @NotBlank(message = "部门编码不能为空") String code,
        @NotNull(message = "排序不能为空") Integer sort
) {
}
