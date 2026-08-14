/**
 * 后台管理知识库FeedbackPage数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

public record AdminKbFeedbackPageRequest(
        Integer categoryId,
        String feedback,
        String writeBackStatus,
        Long page,
        Long size
) {
}
