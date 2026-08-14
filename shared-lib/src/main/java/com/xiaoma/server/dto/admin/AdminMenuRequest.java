/**
 * 后台管理Menu数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminMenuRequest(
        Long parentId,
        @NotBlank(message = "菜单名称不能为空") String name,
        @NotNull(message = "类型不能为空") Integer type,
        String permission,
        String path,
        String component,
        String icon,
        @NotNull(message = "排序不能为空") Integer sort
) {
}
