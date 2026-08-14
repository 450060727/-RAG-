/**
 * 后台管理知识库DocumentPage数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

public record AdminKbDocumentPageRequest(
        Integer categoryId,
        String keyword,
        String sourceType,
        Integer status,
        Long page,
        Long size
) {
}
