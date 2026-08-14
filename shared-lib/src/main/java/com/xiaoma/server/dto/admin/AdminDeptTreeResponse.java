/**
 * 后台管理DeptTree数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.util.List;

public record AdminDeptTreeResponse(
        Long id,
        Long parentId,
        String name,
        String code,
        Integer sort,
        Integer status,
        List<AdminDeptTreeResponse> children
) {
}
