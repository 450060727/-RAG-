/**
 * SysRoleMenu。
 */
package com.xiaoma.server.entity.admin;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * SysRoleMenu 实体。
 * 本类定义了 SysRoleMenu 的公共契约与数据结构。
 */
@TableName("sys_role_menu")
public class SysRoleMenu {

    private Long roleId; // roleId 字段

    private Long menuId; // menuId 字段

    /**
     * 获取 RoleId。
     * @return 返回值说明
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * 设置 RoleId。
     * @param roleId 参数说明
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    /**
     * 获取 MenuId。
     * @return 返回值说明
     */
    public Long getMenuId() {
        return menuId;
    }

    /**
     * 设置 MenuId。
     * @param menuId 参数说明
     */
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
