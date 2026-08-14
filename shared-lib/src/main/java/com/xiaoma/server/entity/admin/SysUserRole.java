/**
 * SysUserRole。
 */
package com.xiaoma.server.entity.admin;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * SysUserRole 实体。
 * 本类定义了 SysUserRole 的公共契约与数据结构。
 */
@TableName("sys_user_role")
public class SysUserRole {

    private Long userId; // userId 字段

    private Long roleId; // roleId 字段

    /**
     * 获取 UserId。
     * @return 返回值说明
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置 UserId。
     * @param userId 参数说明
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

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
}
