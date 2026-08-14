/**
 * 后台管理知识库Category数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminKbCategoryRequest(
        Integer parentId,
        @NotBlank String name,
        String description,
        Integer sortOrder,
        Integer status
) {
}
