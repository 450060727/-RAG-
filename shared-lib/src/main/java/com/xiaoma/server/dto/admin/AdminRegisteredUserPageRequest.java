/**
 * 后台管理RegisteredUserPage数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

public record AdminRegisteredUserPageRequest(
        String keyword,
        Integer status,
        Long page,
        Long size
) {
}
