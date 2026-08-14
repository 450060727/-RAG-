/**
 * 后台管理MenuTree数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.util.List;

public record AdminMenuTreeResponse(
        Long id,
        Long parentId,
        String name,
        Integer type,
        String permission,
        String path,
        String component,
        String icon,
        Integer sort,
        List<AdminMenuTreeResponse> children
) {
}
