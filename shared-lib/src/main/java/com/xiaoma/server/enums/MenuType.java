/**
 * enums 模块的 MenuType 类/接口定义。
 */
package com.xiaoma.server.enums;

/**
 * 系统菜单类型。
 * 1 目录，2 菜单，3 按钮/API。
 */
public enum MenuType {

    /** 目录 */
    DIRECTORY(1),
    /** 菜单 */
    MENU(2),
    /** 按钮/API */
    BUTTON(3);

    private final int code; // code 字段

    MenuType(int code) {
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
     * 根据状态码解析菜单类型。
     *
     * @param code 状态码，可能为 null
     * @return 对应的 MenuType，未找到时返回 DIRECTORY
     */
    public static MenuType of(Integer code) {
        if (code == null) {
            return DIRECTORY;
        }
        for (MenuType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return DIRECTORY;
    }

    /**
     * 判断给定类型码是否为按钮/API。
     *
     * @param code 类型码
     * @return true 表示按钮/API
     */
    public static boolean isButton(Integer code) {
        return BUTTON.code == (code == null ? 0 : code);
    }
}
