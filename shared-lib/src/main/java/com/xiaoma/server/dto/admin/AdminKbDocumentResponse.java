/**
 * 后台管理知识库Document数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.time.LocalDateTime;

public record AdminKbDocumentResponse(
        Integer id,
        Integer categoryId,
        String categoryName,
        String title,
        String sourceType,
        String fileType,
        String filePath,
        Long fileSize,
        String fileMime,
        Integer chunkCount,
        Integer status,
        Integer createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
