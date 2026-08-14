/**
 * 后台管理RegisteredUser数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.time.LocalDateTime;

public record AdminRegisteredUserResponse(
        Long id,
        String email,
        String name,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
