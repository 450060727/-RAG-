/**
 * 知识库Chat数据传输请求对象。
 */
package com.xiaoma.server.dto.kb;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KbChatRequest(
        @NotNull Integer categoryId,
        @NotBlank String question,
        Integer sessionId
) {
}
