/**
 * 后台管理知识库TextInput数据传输请求对象。
 */
package com.xiaoma.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminKbTextInputRequest(
        @NotNull Integer categoryId,
        @NotBlank String title,
        @NotBlank String content
) {
}
