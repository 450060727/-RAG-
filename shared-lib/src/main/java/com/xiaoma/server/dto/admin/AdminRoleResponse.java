/**
 * 后台管理Role数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.util.List;

public record AdminRoleResponse(
        Long id,
        String name,
        String code,
        String dataScope,
        String remark,
        List<Long> menuIds,
        List<Long> deptIds
) {
}
