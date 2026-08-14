/**
 * enums 模块的 UserStatus 类/接口定义。
 */
package com.xiaoma.server.enums;

/**
 * 用户账号状态。
 * 0 表示正常，1 表示禁用。
 */
public enum UserStatus {

    /** 正常 */
    ENABLED(0),
    /** 禁用 */
    DISABLED(1);

    private final int code; // code 字段

    UserStatus(int code) {
        this.code = code;
    }

    /**
     * 获取 Code。
     * @return 返回值说明
     */
    public int getCode() {
        return code;
    }

    /**
     * 判断给定状态码是否为禁用。
     *
     * @param status 状态码，可能为 null
     * @return true 表示禁用
     */
    public static boolean isDisabled(Integer status) {
        return DISABLED.code == (status == null ? 0 : status);
    }

    /**
     * 判断给定状态码是否为正常。
     *
     * @param status 状态码，可能为 null
     * @return true 表示正常
     */
    public static boolean isEnabled(Integer status) {
        return !isDisabled(status);
    }
}
