/**
 * 后台管理Page数据传输响应对象。
 */
package com.xiaoma.server.dto.admin;

import java.util.List;

public record AdminPageResponse<T>(
        List<T> records,
        long total,
        long size,
        long current
) {
}
