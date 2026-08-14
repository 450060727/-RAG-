/**
 * common 模块的 AuthContext 类/接口定义。
 */
package com.xiaoma.server.common;

import java.util.Optional;

/**
 * 当前登录用户上下文。
 * 基于 ThreadLocal 实现，在线程内安全保存当前用户 ID 与类型；必须在请求结束后清理，防止线程复用导致泄漏。
 */
public final class AuthContext {

    private AuthContext() {
        // 工具类禁止实例化
    }

    // ThreadLocal 保存当前认证信息，每个请求线程独立
    private static final ThreadLocal<AuthInfo> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前登录用户。
     *
     * @param userId 用户 ID
     * @param type   用户类型，如 "admin" / "user"
     */
    public static void set(Long userId, String type) {
        HOLDER.set(new AuthInfo(userId, type));
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID 的可选包装
     */
    public static Optional<Long> currentUserId() {
        AuthInfo info = HOLDER.get();
        return info == null ? Optional.empty() : Optional.ofNullable(info.userId());
    }

    /**
     * 获取当前登录用户类型。
     *
     * @return 用户类型的可选包装
     */
    public static Optional<String> currentType() {
        AuthInfo info = HOLDER.get();
        return info == null ? Optional.empty() : Optional.ofNullable(info.type());
    }

    /**
     * 判断当前用户是否为管理员类型。
     *
     * @return true 表示管理员
     */
    public static boolean isAdmin() {
        return "admin".equals(currentType().orElse(""));
    }

    /**
     * 清理当前线程的认证信息，防止线程池复用导致上下文泄漏。
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 认证信息不可变记录。
     *
     * @param userId 用户 ID
     * @param type   用户类型
     */
    public record AuthInfo(Long userId, String type) {
    }
}
