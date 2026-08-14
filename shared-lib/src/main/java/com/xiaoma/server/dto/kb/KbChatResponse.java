/**
 * 知识库Chat数据传输响应对象。
 */
package com.xiaoma.server.dto.kb;

import java.math.BigDecimal;
import java.util.List;

public record KbChatResponse(
        Integer messageId,
        String answer,
        Boolean useLocal,
        BigDecimal confidence,
        List<Source> sources,
        Integer sessionId
) {

    /**
     * Source 方法。
     * @param docId 参数说明
     * @param title 参数说明
     * @param content 参数说明
     * @param score 参数说明
     * @return 返回值说明
     */
    public record Source(Integer docId, String title, String content, BigDecimal score) {
    }
}
