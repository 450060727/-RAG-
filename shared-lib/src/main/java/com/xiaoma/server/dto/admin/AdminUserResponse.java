/**
 * 后台管理User数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String username,
        String realName,
        String phone,
        String email,
        Long deptId,
        String deptName,
        Integer status,
        Integer superAdmin,
        LocalDateTime lastLoginTime,
        List<AdminRoleResponse> roles,
        LocalDateTime createdAt
) {
}
