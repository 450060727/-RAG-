/**
 * 知识库ChatSession数据传输响应对象。
 */
package com.xiaoma.server.dto.kb;

import java.time.LocalDateTime;

public record KbChatSessionResponse(
        Integer id,
        Integer categoryId,
        String title,
        LocalDateTime createdAt
) {
}
