/**
 * 后台管理RoleMenu数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import java.util.List;

public record AdminRoleMenuRequest(
        List<Long> menuIds
) {
}
