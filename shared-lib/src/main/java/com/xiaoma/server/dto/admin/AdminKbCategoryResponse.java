/**
 * 后台管理知识库Category数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record AdminKbCategoryResponse(
        Integer id,
        Integer parentId,
        String name,
        String description,
        Integer sortOrder,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AdminKbCategoryResponse> children
) {
}
