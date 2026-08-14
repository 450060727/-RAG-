/**
 * enums 模块的 DataScope 类/接口定义。
 */
package com.xiaoma.server.enums;

/**
 * 角色数据权限范围。
 */
public enum DataScope {

    /** 全部数据 */
    ALL("ALL"),
    /** 本部门数据 */
    DEPT("DEPT"),
    /** 本部门及子部门数据 */
    DEPT_AND_CHILD("DEPT_AND_CHILD"),
    /** 自定义部门数据 */
    CUSTOM("CUSTOM");

    private final String code; // code 字段

    DataScope(String code) {
        this.code = code;
    }

    /**
     * 获取 Code。
     * @return 返回值说明
     */
    public String getCode() {
        return code;
    }

    /**
     * 根据编码解析数据权限范围。
     *
     * @param code 编码，可能为 null
     * @return 对应的 DataScope，未找到时返回 DEPT（最严格）
     */
    public static DataScope of(String code) {
        if (code == null || code.isBlank()) {
            return DEPT;
        }
        for (DataScope scope : values()) {
            if (scope.code.equals(code)) {
                return scope;
            }
        }
        return DEPT;
    }
}
