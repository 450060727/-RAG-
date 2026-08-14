/**
 * 知识库ChatFeedback数据传输请求对象。
 */
package com.xiaoma.server.dto.kb;

import jakarta.validation.constraints.NotBlank;

public record KbChatFeedbackRequest(
        @NotBlank String feedback,
        Boolean writeBack
) {
}
