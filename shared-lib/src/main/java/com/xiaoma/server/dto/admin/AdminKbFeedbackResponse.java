/**
 * 后台管理知识库Feedback数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminKbFeedbackResponse(
        Integer id,
        Integer sessionId,
        String question,
        String answer,
        Integer useLocal,
        BigDecimal confidence,
        String feedback,
        String writeBackStatus,
        LocalDateTime createdAt
) {
}
