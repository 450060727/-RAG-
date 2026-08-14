/**
 * 后台管理UserPage数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

public record AdminUserPageRequest(
        String keyword,
        Long deptId,
        Integer status,
        Long page,
        Long size
) {
}
