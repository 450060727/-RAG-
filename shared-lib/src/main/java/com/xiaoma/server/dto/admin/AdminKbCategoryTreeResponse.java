/**
 * 后台管理知识库CategoryTree数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

public record AdminKbCategoryTreeResponse(
        Integer id,
        Integer parentId,
        String name,
        java.util.List<AdminKbCategoryTreeResponse> children
) {
}
