package com.xiaoma.server.dto;

/**
 * 用户资料响应 DTO。
 *
 * @param id    用户 ID
 * @param email 邮箱
 * @param name  昵称
 * @author xiaoma
 * @since 2026-07-23
 */
public record ProfileResponse(Long id, String email, String name) {
}
