/**
 * enums 模块的 SuperAdminFlag 类/接口定义。
 */
package com.xiaoma.server.enums;

/**
 * 超级管理员标识。
 * 0 表示普通用户，1 表示超级管理员。
 */
public enum SuperAdminFlag {

    /** 普通用户 */
    NORMAL(0),
    /** 超级管理员 */
    SUPER_ADMIN(1);

    private final int code; // code 字段

    SuperAdminFlag(int code) {
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
     * 判断给定标识是否为超级管理员。
     *
     * @param flag 标识，可能为 null
     * @return true 表示超级管理员
     */
    public static boolean isSuperAdmin(Integer flag) {
        return SUPER_ADMIN.code == (flag == null ? 0 : flag);
    }
}
