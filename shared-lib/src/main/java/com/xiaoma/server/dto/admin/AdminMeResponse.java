/**
 * 后台管理Me数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.util.List;
import java.util.Set;

public record AdminMeResponse(
        Long id,
        String username,
        String realName,
        String phone,
        String email,
        String avatar,
        Long deptId,
        String deptName,
        Integer superAdmin,
        List<AdminRoleResponse> roles,
        List<AdminMenuTreeResponse> menus,
        Set<String> permissions
) {
}
